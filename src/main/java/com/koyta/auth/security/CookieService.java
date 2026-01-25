package com.koyta.auth.security;

import com.koyta.auth.dtos.RefreshTokenRequest;
import com.koyta.auth.services.JwtService;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Getter
@Setter
@Service
public class CookieService {

    private final String refreshTokenCookieName;

    private final boolean cookieHttpOnly;

    private final boolean cookieSecure;

    private final String cookieDomain;

    private final String cookieSameSite;

    private final JwtService jwtService;

    public CookieService(@Value("${security.jwt.refresh-cookie-name}") String refreshTokenCookieName,
                         @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
                         @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
                         @Value("${security.jwt.cookie-domain}") String cookieDomain,
                         @Value("${security.jwt.cookie-same-site}") String cookieSameSite,
                         JwtService jwtService) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
        this.jwtService = jwtService;
    }

    // create method to attached cookie to response

    public void attachRefreshCookie(ServerHttpResponse response, String value, int maxAge) {

        ResponseCookie.ResponseCookieBuilder builder =
                ResponseCookie.from(refreshTokenCookieName, value)
                        .httpOnly(cookieHttpOnly)
                        .secure(cookieSecure)
                        .path("/")
                        .maxAge(maxAge)
                        .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        ResponseCookie build = builder.build();
        response.addCookie(builder.build());
    }


    // Clear refresh Cookie
    public void clearRefreshCookie(ServerHttpResponse response) {

        ResponseCookie.ResponseCookieBuilder builder =
                ResponseCookie.from(refreshTokenCookieName, "")
                        .maxAge(0)
                        .httpOnly(cookieHttpOnly)
                        .secure(cookieSecure)
                        .path("/")
                        .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        response.addCookie(builder.build());
    }

    public void addNoStoreHeader(ServerHttpResponse response) {
        response.getHeaders().setCacheControl("no-store");
        response.getHeaders().add("Pragma", "no-cache");
    }

    /* ==============================
       READ TOKEN (REACTIVE)
       ============================== */

    public Mono<String> readRefreshTokenFromRequest( @Nullable RefreshTokenRequest body, ServerHttpRequest request) {

        // 1️⃣ Cookie (highest priority)
        Mono<String> fromCookie =
                Mono.justOrEmpty(request.getCookies().getFirst(refreshTokenCookieName))
                        .map(HttpCookie::getValue)
                        .filter(v -> !v.isBlank());

        // 2️⃣ Request body
        Mono<String> fromBody =
                Mono.justOrEmpty(body)
                        .map(RefreshTokenRequest::refreshToken)
                        .filter(v -> v != null && !v.isBlank());

        // 3️⃣ Custom header
        Mono<String> fromHeader =
                Mono.justOrEmpty(request.getHeaders().getFirst("X-Refresh-Token"))
                        .map(String::trim)
                        .filter(v -> !v.isBlank());

        // 4️⃣ Authorization header (Bearer)
        Mono<String> fromAuthorization =
                Mono.justOrEmpty(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                        .filter(h -> h.regionMatches(true, 0, "Bearer ", 0, 7))
                        .map(h -> h.substring(7).trim())
                        .filter(v -> !v.isBlank())
                        .filter(token -> {
                            try {
                                return jwtService.isRefreshToken(token);
                            } catch (Exception e) {
                                return false;
                            }
                        });

        // 🔥 Priority order
        return fromCookie
                .switchIfEmpty(fromBody)
                .switchIfEmpty(fromHeader)
                .switchIfEmpty(fromAuthorization);
    }
}
