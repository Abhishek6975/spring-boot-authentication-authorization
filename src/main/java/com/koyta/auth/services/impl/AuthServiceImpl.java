package com.koyta.auth.services.impl;

import com.koyta.auth.dtos.*;
import com.koyta.auth.entities.Provider;
import com.koyta.auth.entities.RefreshToken;
import com.koyta.auth.entities.User;
import com.koyta.auth.exceptions.AuthenticationFailedException;
import com.koyta.auth.repositories.RefreshTokenRepository;
import com.koyta.auth.security.CookieService;
import com.koyta.auth.security.CustomUserDetails;
import com.koyta.auth.services.AuthService;
import com.koyta.auth.services.JwtService;
import com.koyta.auth.services.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final long accessTtlSeconds;

    private final long refreshTtlSeconds;

    private final ModelMapper modelMapper;

    private final RefreshTokenRepository refreshTokenRepository;

    private final CookieService cookieService;

    public AuthServiceImpl(UserService userService,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           @Value("${security.jwt.access-ttl-seconds:3600}") long accessTtlSeconds,
                           @Value("${security.jwt.refresh-ttl-seconds:1209600}") long refreshTtlSeconds,
                           ModelMapper modelMapper, RefreshTokenRepository refreshTokenRepository, CookieService cookieService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.modelMapper = modelMapper;
        this.refreshTokenRepository = refreshTokenRepository;
        this.cookieService = cookieService;
    }

    @Override
    public TokenResponse login(LoginRequest loginRequest, HttpServletResponse response) {

        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

            if (!authenticate.isAuthenticated()) {
                throw new AuthenticationFailedException("Invalid email or password");
            }

            CustomUserDetails customUserDetails = (CustomUserDetails) authenticate.getPrincipal();

            if(!customUserDetails.isEnabled()) {
                throw new DisabledException("Account is disabled. Please verify your email.");
            }


            String jti = UUID.randomUUID().toString();
            RefreshToken refreshTokenOb = RefreshToken.builder()
                    .jti(jti)
                    .user(customUserDetails.getUser())
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
                    .revoked(false)
                    .build();

            // refresh token save
            refreshTokenRepository.save(refreshTokenOb);

            // access token generated
            String accessToken = jwtService.generateAccessToken(customUserDetails.getUser());
            String refreshToken = jwtService.generateRefreshToken(customUserDetails.getUser(), refreshTokenOb.getJti());

            // use cookie service to attach refresh token in cookie
            cookieService.attachRefreshCookie(response, refreshToken, (int) refreshTtlSeconds);
            cookieService.addNoStoreHeader(response);

            return TokenResponse.of(accessToken, accessTtlSeconds, modelMapper.map(customUserDetails.getUser(), UserDto.class)
            );


        } catch (BadCredentialsException ex) {
            throw new AuthenticationFailedException("Invalid email or password");
        } catch (DisabledException ex) {
            throw new AuthenticationFailedException("Account is disabled. Please verify your email.");
        }

    }

    @Override
    public UserDto registerUser(RegisterUserRequest request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setImage(request.getImage());
        user.setProvider(Provider.LOCAL);

       // User savedUser = userService.createUser(user);
        return  userService.createUser(user);
    }

    @Override
    public TokenResponse readRefreshTokenRequest(RefreshTokenRequest body, HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieService.readRefreshTokenFromRequest(body, request).orElseThrow(() -> new BadCredentialsException("Invalid Refresh Token"));

        if(!jwtService.isRefreshToken(refreshToken)){
            throw new BadCredentialsException("Invalid Refresh Token Type");
        }

        String jti = jwtService.getJti(refreshToken);
        String userId = jwtService.getUserId(refreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not recognized"));

        if(storedRefreshToken.isRevoked()){
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        if(storedRefreshToken.getExpiresAt().isBefore(Instant.now())){
            throw new BadCredentialsException("Refresh token expired");
        }

        if(!storedRefreshToken.getUser().getEmail().equals(userId)){
            throw new BadCredentialsException("Refresh token does not belong to this user");
        }

        //refresh token ko rotate:
        storedRefreshToken.setRevoked(true);
        String newJti= UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);

        refreshTokenRepository.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();

        var newRefreshTokenOb = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshTtlSeconds))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenOb);
        String newAccessToken= jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, newRefreshTokenOb.getJti());

        cookieService.attachRefreshCookie(response, newRefreshToken, (int) refreshTtlSeconds);
        cookieService.addNoStoreHeader(response);

        return TokenResponse.of(newAccessToken, accessTtlSeconds, modelMapper.map(user, UserDto.class));

    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
       cookieService.readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jti = jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
                }
            } catch (JwtException ignored) {

            }
        });

        // Use CookieUtil (same behavior)
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeader(response);
        SecurityContextHolder.clearContext();
        
    }


}
