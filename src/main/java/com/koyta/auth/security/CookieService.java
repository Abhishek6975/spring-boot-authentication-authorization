package com.koyta.auth.security;

import com.koyta.auth.dtos.RefreshTokenRequest;
import com.koyta.auth.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

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

    public void attachRefreshCookie(HttpServletResponse response, String value, int maxAge){

        ResponseCookie.ResponseCookieBuilder responseCookieBuilder =
                ResponseCookie.from(refreshTokenCookieName, value)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);

        if(cookieDomain != null && !cookieDomain.isBlank()){
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    // Clear refresh Cookie
    public void clearRefreshCookie(HttpServletResponse response){

        ResponseCookie.ResponseCookieBuilder responseCookieBuilder =
                ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0)
                .httpOnly(cookieHttpOnly)
                .path("/")
                .sameSite(cookieSameSite)
                .secure(cookieSecure);

        if(cookieDomain != null && !cookieDomain.isBlank()) {
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

    }

    public void addNoStoreHeader(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("Pragma" , "no-cache");
    }

    public Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        // prefer reading refresh token from cookie

        if(request.getCookies() != null){
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> refreshTokenCookieName.equals(cookie.getName()))
                    .map(c -> c.getValue())
                    .filter(v -> !v.isBlank())
                    .findFirst();

            if(fromCookie.isPresent()) {
                return fromCookie;
            }
        }

        if(body != null && body.refreshToken() != null && !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken());
        }

        String header = request.getHeader("X-Refresh-Token");

        if(header != null && !header.isBlank()) {
            return Optional.of(header.trim());
        }

        //Authorization = Bearer <token>
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()) {
                try {
                    if (jwtService.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return Optional.empty();
    }
}
