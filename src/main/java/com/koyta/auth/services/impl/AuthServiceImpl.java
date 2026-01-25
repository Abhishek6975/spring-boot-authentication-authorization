package com.koyta.auth.services.impl;

import com.koyta.auth.dtos.LoginRequest;
import com.koyta.auth.dtos.RefreshTokenRequest;
import com.koyta.auth.dtos.TokenResponse;
import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.RefreshToken;
import com.koyta.auth.entities.Role;
import com.koyta.auth.exceptions.AuthenticationFailedException;
import com.koyta.auth.repositories.RefreshTokenRepository;
import com.koyta.auth.repositories.RoleRepository;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.repositories.UserRoleRepository;
import com.koyta.auth.security.CookieService;
import com.koyta.auth.services.AuthService;
import com.koyta.auth.services.JwtService;
import com.koyta.auth.services.UserService;
import io.jsonwebtoken.JwtException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final long accessTtlSeconds;

    private final long refreshTtlSeconds;

    private final ModelMapper modelMapper;

    private final RefreshTokenRepository refreshTokenRepository;

    private final CookieService cookieService;

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    private final RoleRepository roleRepository;

    public AuthServiceImpl(UserService userService,
                           ReactiveAuthenticationManager reactiveAuthenticationManager,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           @Value("${security.jwt.access-ttl-seconds:3600}") long accessTtlSeconds,
                           @Value("${security.jwt.refresh-ttl-seconds:1209600}") long refreshTtlSeconds,
                           ModelMapper modelMapper, RefreshTokenRepository refreshTokenRepository,
                           CookieService cookieService,
                           UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository) {
        this.userService = userService;
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.modelMapper = modelMapper;
        this.refreshTokenRepository = refreshTokenRepository;
        this.cookieService = cookieService;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }


    @Override
    public Mono<TokenResponse> login(LoginRequest loginRequest, ServerHttpResponse response) {

        return userRepository.findByEmail(loginRequest.email())
                .switchIfEmpty(Mono.error(
                        new AuthenticationFailedException("Invalid email or password")
                ))
                .flatMap(user -> {

                    if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
                        return Mono.error(
                                new AuthenticationFailedException("Invalid email or password")
                        );
                    }

                    if (!user.isEnable()) {
                        return Mono.error(
                                new AuthenticationFailedException("Account is disabled. Please verify your email.")
                        );
                    }

                    // generate refresh token entity
                    String jti = UUID.randomUUID().toString();

                    RefreshToken refreshTokenEntity = RefreshToken.builder()
                            .id(UUID.randomUUID())
                            .jti(jti)
                            .userId(user.getId())
                            .createdAt(Instant.now())
                            .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
                            .revoked(false)
                            .build();

                    return refreshTokenRepository.save(refreshTokenEntity)
                            .flatMap(savedToken -> getUserRoles(user.getId())
                                    .map(roles -> {
                                        String accessToken =
                                                jwtService.generateAccessToken(user, roles);
                                        String refreshToken =
                                                jwtService.generateRefreshToken(user, savedToken.getJti());

                                // 🍪 attach cookie (REACTIVE WAY)
                                        attachRefreshCookie(response, refreshToken);

                                addNoStoreHeader(response);

                                return TokenResponse.of(
                                        accessToken,
                                        refreshToken,
                                        accessTtlSeconds,
                                        modelMapper.map(user, UserDto.class)
                                );
                            }));
                });
    }

    public Mono<List<String>> getUserRoles(UUID userId) {
        return userRoleRepository.findByUserId(userId)
                .flatMap(userRole ->
                        roleRepository.findById(userRole.getRoleId()))
                .map(Role::getName)
                .collectList();
    }

    private void attachRefreshCookie(ServerHttpResponse response, String refreshToken) {

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // true in prod
                .path("/")
                .maxAge(Duration.ofSeconds(refreshTtlSeconds))
                .sameSite("Lax")
                .build();

        response.addCookie(cookie);
    }

    private void addNoStoreHeader(ServerHttpResponse response) {
        response.getHeaders().add("Cache-Control", "no-store");
    }

    @Override
    public Mono<UserDto> registerUser(UserDto userDto) {

        return Mono.fromCallable(() -> passwordEncoder.encode(userDto.getPassword()))
                .map(encodedPassword -> {
                    userDto.setPassword(encodedPassword);
                    return userDto;
        }).flatMap(userService::createUser);
    }

    @Override
    public Mono<TokenResponse> readRefreshTokenRequest(RefreshTokenRequest body, ServerHttpRequest request, ServerHttpResponse response) {

        return cookieService.readRefreshTokenFromRequest(body, request)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid Refresh Token")))
                .flatMap(refreshToken -> {

                    // 1️. Validate token type
                    if (!jwtService.isRefreshToken(refreshToken)) {
                        return Mono.error(new BadCredentialsException("Invalid Refresh Token Type"));
                    }
                    String jti;
                    String userId;

                    try {
                        jti = jwtService.getJti(refreshToken);
                        userId = jwtService.getUserId(refreshToken);
                    } catch (Exception e) {
                        return Mono.error(new BadCredentialsException("Invalid Refresh Token"));
                    }

                    // 2️. Fetch stored refresh token
                    return refreshTokenRepository.findByJti(jti)
                            .switchIfEmpty(Mono.error(
                                    new BadCredentialsException("Refresh token not recognized")
                            ))
                            .flatMap(stored -> {

                                // 3. Validate stored token
                                if (stored.isRevoked()) {
                                    return Mono.error(
                                            new BadCredentialsException("Refresh token expired or revoked")
                                    );
                                }

                                if (stored.getExpiresAt().isBefore(Instant.now())) {
                                    return Mono.error(
                                            new BadCredentialsException("Refresh token expired")
                                    );
                                }

                                if (!stored.getUserId().toString().equals(userId)) {
                                    return Mono.error(
                                            new BadCredentialsException("Refresh token does not belong to this user")
                                    );
                                }

                                // 4️. Rotate refresh token
                                String newJti = UUID.randomUUID().toString();
                                stored.setRevoked(true);
                                stored.setReplacedByToken(newJti);

                                return refreshTokenRepository.save(stored)
                                        .then(userRepository.findById(stored.getUserId())
                                                .switchIfEmpty(Mono.error(new BadCredentialsException("User not found")))
                                                        .flatMap(user -> {

                                                            RefreshToken newRefreshTokenEntity =
                                                                    RefreshToken.builder()
                                                                            .id(UUID.randomUUID())
                                                                            .jti(newJti)
                                                                            .userId(user.getId())
                                                                            .createdAt(Instant.now())
                                                                            .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
                                                                            .revoked(false)
                                                                            .build();

                                                            return refreshTokenRepository.save(newRefreshTokenEntity)
                                                                    .flatMap(savedToken -> getUserRoles(user.getId())

                                                                    .map(roles -> {

                                                                        String newAccessToken =
                                                                                jwtService.generateAccessToken(user, roles);

                                                                        String newRefreshToken =
                                                                                jwtService.generateRefreshToken(user, savedToken.getJti());

                                                                        // 🍪 Attach cookie (reactive-safe side effect)
                                                                        cookieService.attachRefreshCookie(
                                                                                response,
                                                                                newRefreshToken,
                                                                                (int) refreshTtlSeconds
                                                                        );
                                                                        cookieService.addNoStoreHeader(response);

                                                                        return TokenResponse.of(
                                                                                newAccessToken,
                                                                                newRefreshToken,
                                                                                accessTtlSeconds,
                                                                                modelMapper.map(user, UserDto.class)
                                                                        );
                                                                    }));
                                                        })
                                        );
                            });
                });
    }





    @Override
    public Mono<Void> logout(ServerHttpRequest request, ServerHttpResponse response) {

        return cookieService.readRefreshTokenFromRequest(null, request)
                .flatMap(token -> {

                    if (!jwtService.isRefreshToken(token)) {
                        return Mono.empty();
                    }

                    String jti;
                    try {
                        jti = jwtService.getJti(token);
                    } catch (JwtException ex) {
                        return Mono.empty();
                    }

                  return refreshTokenRepository.findByJti(jti)
                            .flatMap(rt -> {
                                rt.setRevoked(true);
                                return refreshTokenRepository.save(rt);
                            })
                            .switchIfEmpty(Mono.empty());
                })
                .doFinally(signalType -> {
                    // Always clear cookie & headers
                    cookieService.clearRefreshCookie(response);
                    cookieService.addNoStoreHeader(response);
                })
                .then();
    }



}
