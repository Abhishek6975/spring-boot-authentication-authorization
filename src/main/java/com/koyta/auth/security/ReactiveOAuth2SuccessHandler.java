package com.koyta.auth.security;

import com.koyta.auth.entities.Provider;
import com.koyta.auth.entities.RefreshToken;
import com.koyta.auth.entities.User;
import com.koyta.auth.repositories.RefreshTokenRepository;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.services.JwtService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ReactiveOAuth2SuccessHandler
        implements ServerAuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final CookieService cookieService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final long refreshTtlSeconds;

    @Value("${app.auth.frontend.success-redirect}")
    private String frontEndSuccessUrl;

    public ReactiveOAuth2SuccessHandler(UserRepository userRepository,
                                JwtService jwtService,
                                CookieService cookieService,
                                RefreshTokenRepository refreshTokenRepository,
                                @Value("${security.jwt.refresh-ttl-seconds:1209600}") long refreshTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.cookieService = cookieService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }


    @Override
    public Mono<Void> onAuthenticationSuccess(@NotNull WebFilterExchange webFilterExchange, @NotNull Authentication authentication) {

        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpResponse response = exchange.getResponse();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationId = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        logger.info("registrationId:" + registrationId);
        logger.info("user:" + oAuth2User.getAttributes().toString());

        Mono<User> userMono = switch (registrationId) {

            case "google" -> handleGoogleLogin(oAuth2User);
            case "github" -> handleGithubLogin(oAuth2User);
            default -> Mono.error(
                    new IllegalStateException("Invalid OAuth2 provider")
            );
        };

        return userMono
                .flatMap(user -> {

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
                            .map(savedToken -> {

                                String accessToken =
                                        jwtService.generateAccessToken(user, List.of());

                                String refreshToken =
                                        jwtService.generateRefreshToken(user, savedToken.getJti());

                                cookieService.attachRefreshCookie(
                                        response,
                                        refreshToken,
                                        (int) refreshTtlSeconds
                                );

                                response.setStatusCode(HttpStatus.FOUND);
                                response.getHeaders().setLocation(
                                        URI.create(frontEndSuccessUrl)
                                );

                                return savedToken;
                            });
                })
                .then(response.setComplete());
    }

    /* =========================
       PROVIDER-SPECIFIC HANDLERS
       ========================= */

    private Mono<User> handleGoogleLogin(OAuth2User oAuth2User) {

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        return userRepository.findByEmail(email)
                .switchIfEmpty(
                        userRepository.save(
                                User.builder()
                                        .email(email)
                                        .name(name)
                                        .image(picture)
                                        .enable(true)
                                        .provider(Provider.GOOGLE)
                                        .providerId(googleId)
                                        .build()
                        )
                );
    }

    private Mono<User> handleGithubLogin(OAuth2User oAuth2User) {

        String githubId = String.valueOf(oAuth2User.getAttribute("id"));
        String name = oAuth2User.getAttribute("login");
        String image = oAuth2User.getAttribute("avatar_url");

        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            email = name + "@github.com";
        }

        String finalEmail = email;

        return userRepository.findByEmail(finalEmail)
                .switchIfEmpty(
                        userRepository.save(
                                User.builder()
                                        .email(finalEmail)
                                        .name(name)
                                        .image(image)
                                        .enable(true)
                                        .provider(Provider.GITHUB)
                                        .providerId(githubId)
                                        .build()
                        )
                );
    }
}