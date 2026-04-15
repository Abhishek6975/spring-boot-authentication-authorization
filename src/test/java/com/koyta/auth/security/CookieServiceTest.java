package com.koyta.auth.security;


import com.koyta.auth.dtos.RefreshTokenRequest;
import com.koyta.auth.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieServiceTest {

    private CookieService cookieService;

    @Mock
    private JwtService jwtService;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setup() {
        cookieService = new CookieService(
                "refreshToken",
                true,
                false,
                "localhost",
                "Lax",
                jwtService
        );

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    // 1. attach cookie with domain
    @Test
    void shouldAttachCookieWithDomain() {

        cookieService.attachRefreshCookie(response, "token", 100);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Domain=localhost"));
    }

//    2. attach cookie without main
    @Test
    void shouldAttachCookieWithoutDomain() {

        CookieService service = new CookieService(
                "refreshToken",
                true,
                false,
                null, // no domain
                "Lax",
                jwtService
        );

        service.attachRefreshCookie(response, "token", 100);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE),
                not(contains("Domain")));
    }

    // 3. clear cookie
    @Test
    void shouldClearCookie() {

        cookieService.clearRefreshCookie(response);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Max-Age=0"));
    }

    // 4. read from cookie
    @Test
    void shouldReadTokenFromCookie() {

        Cookie cookie = new Cookie("refreshToken", "abc");

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Optional<String> token =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(token.isPresent());
        assertEquals("abc", token.get());
    }

    // 5. cookie blank skip
    @Test
    void shouldSkipBlankCookie() {

        Cookie cookie = new Cookie("refreshToken", "");

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Optional<String> token =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(token.isEmpty());
    }

    // 6. read from body
    @Test
    void shouldReadTokenFromBody() {

        RefreshTokenRequest body = new RefreshTokenRequest("bodyToken");

        when(request.getCookies()).thenReturn(null);

        Optional<String> token =
                cookieService.readRefreshTokenFromRequest(body, request);

        assertEquals("bodyToken", token.get());
    }

    // 7. read from header
    @Test
    void shouldReadTokenFromHeader() {

        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("X-Refresh-Token")).thenReturn("headerToken");

        Optional<String> token =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertEquals("headerToken", token.get());
    }

    // 8. read from Authorization header
    @Test
    void shouldReadFromAuthorizationHeader() {

        when(request.getCookies()).thenReturn(null);

        when(request.getHeader(anyString())).thenReturn(null);

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer validToken");

        when(jwtService.isRefreshToken("validToken")).thenReturn(true);

        Optional<String> token =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(token.isPresent());
        assertEquals("validToken", token.get());
    }

    // 9. invalid bearer token
    @Test
    void shouldSkipInvalidBearerToken() {

        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(anyString())).thenReturn(null);

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer invalid");

        when(jwtService.isRefreshToken("invalid"))
                .thenThrow(new RuntimeException());

        Optional<String> token =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(token.isEmpty());
    }

    // 10. fallback empty
    @Test
    void shouldReturnEmptyWhenNoSource() {

        when(request.getCookies()).thenReturn(null);

        Optional<String> token =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(token.isEmpty());
    }

    // 11. add no-store header
    @Test
    void shouldAddNoStoreHeader() {

        cookieService.addNoStoreHeader(response);

        verify(response).setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        verify(response).setHeader("Pragma", "no-cache");
    }

//    12.Clear Cookies wihout Domain
    @Test
    void shouldClearCookieWithoutDomain() {

        CookieService service = new CookieService(
                "refreshToken",
                true,
                false,
                null,
                "Lax",
                jwtService
        );

        service.clearRefreshCookie(response);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE),
                not(contains("Domain")));
    }

    // CATCH BLOCK COVER
    @Test
    void shouldHandleJwtExceptionInAuthorizationHeader() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getCookies()).thenReturn(null);

        when(request.getHeader("X-Refresh-Token")).thenReturn(null);

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer invalidToken");

        when(jwtService.isRefreshToken("invalidToken"))
                .thenThrow(new JwtException("Invalid"));

        Optional<String> result =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSkipDomainWhenBlank() {

        CookieService service = new CookieService(
                "refreshToken",
                true,
                false,
                "",
                "Lax",
                jwtService
        );
        HttpServletResponse response = mock(HttpServletResponse.class);

        service.attachRefreshCookie(response, "token", 100);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), not(contains("Domain")));
    }

    @Test
    void shouldSkipWhenBodyNull() {

        Optional<String> result =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSkipWhenBodyTokenBlank() {

        RefreshTokenRequest body = new RefreshTokenRequest("");

        Optional<String> result =
                cookieService.readRefreshTokenFromRequest(body, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSkipWhenHeaderBlank() {

        when(request.getHeader("X-Refresh-Token")).thenReturn("");

        Optional<String> result =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSkipWhenAuthorizationInvalidFormat() {

        CookieService service = new CookieService(
                "refreshToken",
                true,
                false,
                "",
                "Lax",
                jwtService
        );
        when(request.getHeader("X-Refresh-Token")).thenReturn(null);

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("InvalidToken");

        Optional<String> result =
                service.readRefreshTokenFromRequest(null, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSkipWhenCandidateEmpty() {

        CookieService service = new CookieService(
                "refreshToken",
                true,
                false,
                "",
                "Lax",
                jwtService
        );
        when(request.getHeader("X-Refresh-Token")).thenReturn(null);

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer   "); // empty after trim

        Optional<String> result =
                cookieService.readRefreshTokenFromRequest(null, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSkipWhenNotRefreshToken() {

        CookieService service = new CookieService(
                "refreshToken",
                true,
                false,
                "",
                "Lax",
                jwtService
        );

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Refresh-Token")).thenReturn(null);

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer token");

        when(jwtService.isRefreshToken("token")).thenReturn(false);

        Optional<String> result =
                service.readRefreshTokenFromRequest(null, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotAddDomainWhenNull_inClearRefreshCookie() {

        CookieService service = new CookieService(
                "refreshToken",
                true,
                false,
                null,   // 🔥 null case
                "Lax",
                jwtService
        );

        HttpServletResponse response = mock(HttpServletResponse.class);

        service.clearRefreshCookie(response);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), not(contains("Domain")));
    }

    @Test
    void shouldNotAddDomainWhenBlank_inClearRefreshCookie() {

        CookieService service = new CookieService(
                "refreshToken",
                true,
                false,
                "",   // 🔥 blank case
                "Lax",
                jwtService
        );

        HttpServletResponse response = mock(HttpServletResponse.class);

        service.clearRefreshCookie(response);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), not(contains("Domain")));
    }
}
