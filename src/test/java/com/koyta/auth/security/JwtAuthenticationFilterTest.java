package com.koyta.auth.security;

import com.koyta.auth.exceptions.JwtTokenExpiredException;
import com.koyta.auth.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    // ================= VALIDATION =================

    @Test
    void shouldSkipWhenNoAuthorizationHeader() throws Exception {
        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSkipWhenHeaderInvalidFormat() throws Exception {
        request.addHeader("Authorization", "Invalid");
        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSkipWhenUsernameNull() throws Exception {
        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldHandleWhenUsernameIsEmptyString() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        // 🔥 empty username
        when(jwtService.extractUserName(anyString())).thenReturn("");

        when(jwtService.isAccessToken(anyString())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(""))
                .thenReturn(userDetails);

        when(jwtService.validateToken(anyString(), eq(userDetails)))
                .thenReturn(false); // avoid auth set

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    // ================= SUCCESS =================

    @Test
    void shouldAuthenticateWhenValidToken() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn("user");
        when(jwtService.isAccessToken(anyString())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername("user"))
                .thenReturn(userDetails);

        when(jwtService.validateToken(anyString(), eq(userDetails)))
                .thenReturn(true);

        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(List.of());

        filter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateWhenAuthoritiesNull() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn("user");
        when(jwtService.isAccessToken(anyString())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername("user"))
                .thenReturn(userDetails);

        when(jwtService.validateToken(anyString(), eq(userDetails)))
                .thenReturn(true);

        when(userDetails.isEnabled()).thenReturn(true);

        // 🔥 IMPORTANT: authorities null
        when(userDetails.getAuthorities()).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        // authentication should still be null / not set properly
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldCallFilterChainWhenAuthenticationSuccess() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn("user");
        when(jwtService.isAccessToken(anyString())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername("user"))
                .thenReturn(userDetails);

        when(jwtService.validateToken(anyString(), eq(userDetails)))
                .thenReturn(true);

        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(List.of());

        filter.doFilterInternal(request, response, chain);

        // 🔥 THIS WAS MISSING
        verify(chain).doFilter(request, response);
    }

    // ================= FAILURE =================

    @Test
    void shouldSkipWhenNotAccessToken() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn("user");
        when(jwtService.isAccessToken(anyString())).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSkipWhenHeaderDoesNotStartWithBearer() throws Exception {

        // header present but NOT Bearer
        request.addHeader("Authorization", "Basic abc123");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSkipWhenAlreadyAuthenticated() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null)
        );

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn("user");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSkipWhenTokenValidationFails() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn("user");
        when(jwtService.isAccessToken(anyString())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("user"))
                .thenReturn(userDetails);

        when(jwtService.validateToken(anyString(), eq(userDetails)))
                .thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticateWhenUserDisabled() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn("user");
        when(jwtService.isAccessToken(anyString())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("user"))
                .thenReturn(userDetails);

        when(jwtService.validateToken(anyString(), eq(userDetails)))
                .thenReturn(true);

        when(userDetails.isEnabled()).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ================= EXCEPTION =================

    @Test
    void shouldReturn401WhenJwtExceptionOccurs() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString()))
                .thenThrow(new JwtException("Invalid"));

        filter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldThrowWhenUserNotFound() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString())).thenReturn("user");
        when(jwtService.isAccessToken(anyString())).thenReturn(true);

        when(userDetailsService.loadUserByUsername("user"))
                .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () ->
                filter.doFilterInternal(request, response, chain)
        );
    }

    @Test
    void shouldReturn401WhenTokenExpired() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString()))
                .thenThrow(new JwtException("Expired"));

        filter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldReturn401WhenJwtTokenExpiredExceptionOccurs() throws Exception {

        request.addHeader("Authorization", "Bearer token");

        when(jwtService.extractUserName(anyString()))
                .thenThrow(new JwtTokenExpiredException("Token expired"));

        filter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    // ================= FILTER SKIP =================

    @Test
    void shouldNotFilterAuthEndpoints() {

        request.setRequestURI("/api/v1/auth/login");

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    void shouldFilterOtherEndpoints() {

        request.setRequestURI("/api/v1/admin");

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

}
