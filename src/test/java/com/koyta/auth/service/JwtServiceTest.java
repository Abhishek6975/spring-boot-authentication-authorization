package com.koyta.auth.service;

import com.koyta.auth.entities.User;
import com.koyta.auth.exceptions.JwtTokenExpiredException;
import com.koyta.auth.services.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtServiceImpl jwtService;

    private final String validSecret =
            "this-is-a-very-long-secret-key-at-least-64-characters-long-123456";

    @BeforeEach
    void setup() {
        jwtService = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");
    }

    @Test
    void shouldThrowWhenSecretTooShort() {

        String shortSecret = "abc"; // < 64

        assertThrows(IllegalStateException.class, () ->
                new JwtServiceImpl(shortSecret, 3600, 7200, "issuer"));
    }

    @Test
    void shouldGenerateAndValidateAccessToken() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        User user = new User();
        user.setEmail("test@test.com");
        user.setId(UUID.randomUUID());


        String token = jwt.generateAccessToken(user);

        assertTrue(jwt.isAccessToken(token));
        assertEquals("test@test.com", jwt.extractUserName(token));
    }

    @Test
    void shouldGenerateRefreshToken() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        User user = new User();
        user.setEmail("test@test.com");

        String token = jwt.generateRefreshToken(user, "jti123");

        assertTrue(jwt.isRefreshToken(token));
        assertEquals("jti123", jwt.getJti(token));
    }

    @Test
    void shouldValidateTokenSuccessfully() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        User user = new User();
        user.setEmail("test@test.com");
        user.setId(UUID.randomUUID());


        String token = jwt.generateAccessToken(user);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");

        assertTrue(jwt.validateToken(token, userDetails));
    }

    @Test
    void shouldFailValidationWhenUsernameMismatch() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        User user = new User();
        user.setEmail("test@test.com");
        user.setId(UUID.randomUUID());


        String token = jwt.generateAccessToken(user);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("wrong@test.com");

        assertFalse(jwt.validateToken(token, userDetails));
    }

    @Test
    void shouldThrowWhenInvalidToken() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        assertThrows(JwtTokenExpiredException.class,
                () -> jwt.extractUserName("invalid.token.value"));
    }

    @Test
    void shouldExtractUserId() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        User user = new User();
        user.setEmail("test@test.com");
        user.setId(UUID.randomUUID());


        String token = jwt.generateAccessToken(user);

        assertEquals("test@test.com", jwt.getUserId(token));
    }

    @Test
    void shouldHandleNullRoles() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        User user = new User();
        user.setEmail("test@test.com");
        user.setId(UUID.randomUUID());
        user.setRoles(null);

        String token = jwt.generateAccessToken(user);

        assertNotNull(token);
    }

    @Test
    void shouldThrowWhenSecretIsNull() {

        assertThrows(IllegalStateException.class, () ->
                new JwtServiceImpl(null, 3600, 7200, "issuer"));
    }

    @Test
    void shouldReturnTrueWhenTokenValidAndUsernameMatches() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");

        String token = jwt.generateAccessToken(user);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");

        Boolean result = jwt.validateToken(token, userDetails);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenUsernameMismatch() {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 3600, 7200, "issuer");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");

        String token = jwt.generateAccessToken(user);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("wrong@test.com");

        Boolean result = jwt.validateToken(token, userDetails);

        assertFalse(result);
    }

    @Test
    void shouldThrowWhenTokenExpired() throws InterruptedException {

        JwtServiceImpl jwt = new JwtServiceImpl(validSecret, 1, 1, "issuer");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");

        String token = jwt.generateAccessToken(user);

        Thread.sleep(2000); // expire

        UserDetails userDetails = mock(UserDetails.class);

        assertThrows(JwtTokenExpiredException.class,
                () -> jwt.validateToken(token, userDetails));
    }
}