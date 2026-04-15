package com.koyta.auth.service;

import com.koyta.auth.dtos.LoginRequest;
import com.koyta.auth.dtos.RegisterUserRequest;
import com.koyta.auth.dtos.TokenResponse;
import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.RefreshToken;
import com.koyta.auth.entities.User;
import com.koyta.auth.exceptions.AuthenticationFailedException;
import com.koyta.auth.repositories.RefreshTokenRepository;
import com.koyta.auth.security.CookieService;
import com.koyta.auth.security.CustomUserDetails;
import com.koyta.auth.services.JwtService;
import com.koyta.auth.services.UserService;
import com.koyta.auth.services.impl.AuthServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceEdgeTest {

    private AuthServiceImpl authService; // ❌ no @InjectMocks

    @Mock private UserService userService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private ModelMapper modelMapper;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private CookieService cookieService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private User user;
    private RefreshToken refreshToken;

    private LoginRequest loginRequest;

    @BeforeEach
    void setup() {

        // ✅ manual constructor injection (IMPORTANT)
        authService = new AuthServiceImpl(
                userService,
                authenticationManager,
                passwordEncoder,
                jwtService,
                3600L,
                1209600L,
                modelMapper,
                refreshTokenRepository,
                cookieService
        );

        user = new User();
        user.setEmail("test@test.com");
        user.setEnable(true);

        refreshToken = RefreshToken.builder()
                .jti("jti123")
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(1000))
                .build();

        loginRequest = new LoginRequest("test@test.com", "password123");
    }

    // =========================
    // ❌ INVALID INPUT
    // =========================

    @Test
    void shouldFailWhenRefreshTokenMissing() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
                authService.readRefreshTokenRequest(null, request, response));
    }

    // =========================
    // ✅ SUCCESS FLOW
    // =========================

    @Test
    void shouldRefreshTokenSuccessfully() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("validToken"));

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.getJti(any())).thenReturn("jti123");
        when(jwtService.getUserId(any())).thenReturn("test@test.com");

        when(refreshTokenRepository.findByJti("jti123"))
                .thenReturn(Optional.of(refreshToken));

        assertDoesNotThrow(() ->
                authService.readRefreshTokenRequest(null, request, response));

        verify(refreshTokenRepository, atLeastOnce()).save(any());
    }

    // =========================
    // ❌ EDGE CASES
    // =========================

    @Test
    void shouldFailWhenTokenWrongType() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(false);

        assertThrows(Exception.class, () ->
                authService.readRefreshTokenRequest(null, request, response));
    }

    @Test
    void shouldFailWhenTokenNotFound() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.getJti(any())).thenReturn("jti123");

        when(refreshTokenRepository.findByJti("jti123"))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
                authService.readRefreshTokenRequest(null, request, response));
    }

    @Test
    void shouldFailWhenTokenRevoked() {

        refreshToken.setRevoked(true);

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.getJti(any())).thenReturn("jti123");

        when(refreshTokenRepository.findByJti("jti123"))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(Exception.class, () ->
                authService.readRefreshTokenRequest(null, request, response));
    }

    @Test
    void shouldFailWhenTokenExpired() {

        refreshToken.setExpiresAt(Instant.now().minusSeconds(10));

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.getJti(any())).thenReturn("jti123");

        when(refreshTokenRepository.findByJti("jti123"))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(Exception.class, () ->
                authService.readRefreshTokenRequest(null, request, response));
    }

    @Test
    void shouldFailWhenUserMismatch() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.getJti(any())).thenReturn("jti123");
        when(jwtService.getUserId(any())).thenReturn("wrong@test.com");

        when(refreshTokenRepository.findByJti("jti123"))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(Exception.class, () ->
                authService.readRefreshTokenRequest(null, request, response));
    }

    @Test
    void shouldThrowWhenAuthenticateReturnsFalseWithoutException() {

        Authentication auth = mock(Authentication.class);

        when(auth.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(loginRequest, response));
    }

    @Test
    void shouldFailWhenJwtGenerationFails() {

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);

        when(authenticationManager.authenticate(any())).thenReturn(auth);

        when(jwtService.generateAccessToken(any()))
                .thenThrow(new RuntimeException("JWT error"));

        assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest, response));
    }

    @Test
    void shouldRotateRefreshTokenProperly() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.getJti(any())).thenReturn("jti123");
        when(jwtService.getUserId(any())).thenReturn("test@test.com");

        when(refreshTokenRepository.findByJti("jti123"))
                .thenReturn(Optional.of(refreshToken));

        authService.readRefreshTokenRequest(null, request, response);

        assertTrue(refreshToken.isRevoked());
        assertNotNull(refreshToken.getReplacedByToken()); // 🔥 important
    }

    @Test
    void shouldLoginSuccessfully() {

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        User user = new User();
        user.setEmail("test@test.com");

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.getUser()).thenReturn(user);

        when(authenticationManager.authenticate(any())).thenReturn(auth);

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh");

        when(modelMapper.map(any(), eq(UserDto.class)))
                .thenReturn(new UserDto());

        TokenResponse res = authService.login(loginRequest, response);

        assertNotNull(res);

        verify(refreshTokenRepository).save(any());
        verify(cookieService).attachRefreshCookie(eq(response), eq("refresh"), anyInt());
        verify(cookieService).addNoStoreHeader(response);
    }

    @Test
    void shouldHandleDisabledExceptionFromAuthManager() {

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("disabled"));

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(loginRequest, response));
    }

    @Test
    void shouldNotSaveWhenTokenNotFoundInLogout() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.getJti(any())).thenReturn("jti");

        when(refreshTokenRepository.findByJti("jti"))
                .thenReturn(Optional.empty());

        authService.logout(request, response);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldReturnTokenResponseOnRefreshSuccess() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.getJti(any())).thenReturn("jti123");
        when(jwtService.getUserId(any())).thenReturn("test@test.com");

        when(refreshTokenRepository.findByJti("jti123"))
                .thenReturn(Optional.of(refreshToken));

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh");

        when(modelMapper.map(any(), eq(UserDto.class)))
                .thenReturn(new UserDto());

        TokenResponse res =
                authService.readRefreshTokenRequest(null, request, response);

        assertNotNull(res);
    }

    @Test
    void shouldConvertEmailToLowercase() {

        RegisterUserRequest req = new RegisterUserRequest();
        req.setEmail("ABHI@TEST.COM");
        req.setPassword("pass");
        req.setName("Abhi");

        authService.registerUser(req);

        verify(userService).createUser(argThat(user ->
                user.getEmail().equals("abhi@test.com")
        ));
    }

    @Test
    void shouldThrowWhenUserDisabledDuringLogin() {

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(userDetails.isEnabled()).thenReturn(false);

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(loginRequest, response));
    }

    @Test
    void shouldHandleLogoutWhenNoTokenPresent() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                authService.logout(request, response));

        verify(cookieService).clearRefreshCookie(response); // ensure flow continues
    }

    @Test
    void shouldIgnoreJwtExceptionDuringLogout() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any()))
                .thenThrow(new JwtException("error"));

        assertDoesNotThrow(() ->
                authService.logout(request, response));
    }

    @Test
    void shouldLoginSuccessfullyFullFlow() {

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        User user = new User();
        user.setEmail("test@test.com");

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.getUser()).thenReturn(user);

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any()))
                .thenReturn("refresh");

        when(modelMapper.map(any(), eq(UserDto.class)))
                .thenReturn(new UserDto());

        TokenResponse responseObj =
                authService.login(loginRequest, response);

        assertNotNull(responseObj);

        verify(refreshTokenRepository).save(any()); // refresh token saved
        verify(cookieService).attachRefreshCookie(eq(response), eq("refresh"), anyInt());
        verify(cookieService).addNoStoreHeader(response);
    }

    @Test
    void shouldHandleBadCredentialsException() {

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(loginRequest, response));
    }

    @Test
    void shouldHandleDisabledExceptionFromAuthenticationManager() {

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("disabled"));

        assertThrows(AuthenticationFailedException.class,
                () -> authService.login(loginRequest, response));
    }

    @Test
    void shouldLoginSuccessfully_fullFlow() {

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        User user = new User();
        user.setEmail("test@test.com");

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.getUser()).thenReturn(user);

        when(authenticationManager.authenticate(any())).thenReturn(auth);

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh");

        when(modelMapper.map(any(), eq(UserDto.class)))
                .thenReturn(new UserDto());

        TokenResponse res = authService.login(loginRequest, response);

        assertNotNull(res);

        verify(refreshTokenRepository).save(any());
        verify(cookieService).attachRefreshCookie(eq(response), eq("refresh"), anyInt());
        verify(cookieService).addNoStoreHeader(response);
    }

    @Test
    void shouldSkipLogoutWhenTokenNotRefreshToken() {

        when(cookieService.readRefreshTokenFromRequest(any(), any()))
                .thenReturn(Optional.of("token"));

        when(jwtService.isRefreshToken(any())).thenReturn(false);

        authService.logout(request, response);

        verify(refreshTokenRepository, never()).save(any());
    }

}