package com.koyta.auth.security;

import com.koyta.auth.entities.User;
import com.koyta.auth.repositories.RefreshTokenRepository;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private CookieService cookieService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private OAuth2SuccessHandler handler;

    @BeforeEach
    void setup() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        handler = new OAuth2SuccessHandler(
                userRepository,
                jwtService,
                cookieService,
                refreshTokenRepository,
                1209600L // 👈 manually pass
        );

        // 👇 VERY IMPORTANT (warna null pointer aayega)
        ReflectionTestUtils.setField(
                handler,
                "frontEndSuccessUrl",
                "http://localhost:3000"
        );
    }

    // ================= GOOGLE =================

    @Test
    void shouldCreateUserForGoogle() throws Exception {

        OAuth2User user = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "123",
                        "email", "test@test.com",
                        "name", "Test User",
                        "picture", "img"
                ),
                "sub"
        );

        Authentication auth = new OAuth2AuthenticationToken(
                user, List.of(), "google"
        );

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                auth
        );

        verify(userRepository, atLeastOnce()).save(any());
    }

    @Test
    void shouldUseExistingUserForGoogle() throws Exception {

        User existing = new User();

        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(),
                Map.of("sub", "1", "email", "test@test.com"),
                "sub"
        );

        Authentication auth = new OAuth2AuthenticationToken(oAuth2User, List.of(), "google");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(existing));

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh");

        handler.onAuthenticationSuccess(request, response, auth);

        verify(userRepository, never()).save(any());
    }

//    GOOGLE EXISTING USER
    @Test
    void shouldUseExistingUserForGoogleProperly() throws Exception {

        User existing = new User();

        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "1",
                        "email", "test@test.com",
                        "name", "Test"
                ),
                "sub"
        );

        Authentication auth = new OAuth2AuthenticationToken(oAuth2User, List.of(), "google");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(existing));

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh");

        handler.onAuthenticationSuccess(request, response, auth);

        verify(userRepository, never()).save(any()); // 🔥 important
    }

    // ================= GITHUB =================

    @Test
    void shouldCreateUserForGithubWithoutEmail() throws Exception {

        OAuth2User user = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "id", "123",
                        "login", "testuser",
                        "avatar_url", "img"
                ),
                "id"
        );

        Authentication auth = new OAuth2AuthenticationToken(
                user, List.of(), "github"
        );

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                auth
        );

        verify(userRepository).save(any());
    }

    @Test
    void shouldCreateGithubUserWhenEmailNull() throws Exception {

        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "id", "1",
                        "login", "user",
                        "avatar_url", "img"
                ),
                "id"
        );

        Authentication auth = new OAuth2AuthenticationToken(oAuth2User, List.of(), "github");

        when(userRepository.findByEmail("user@github.com"))
                .thenReturn(Optional.empty());

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh");

        handler.onAuthenticationSuccess(request, response, auth);

        verify(userRepository).save(any());
    }


//    GITHUB EXISTING USER
    @Test
    void shouldUseExistingUserForGithub() throws Exception {

        User existing = new User();

        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "id", "1",
                        "login", "user",
                        "avatar_url", "img",
                        "email", "test@test.com"
                ),
                "id"
        );

        Authentication auth = new OAuth2AuthenticationToken(oAuth2User, List.of(), "github");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(existing));

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh");

        handler.onAuthenticationSuccess(request, response, auth);

        verify(userRepository, never()).save(any());
    }

    // ================= FAILURE =================

    @Test
    void shouldThrowWhenInvalidProvider() {

        OAuth2User user = new DefaultOAuth2User(
                List.of(),
                Map.of( "id", "123",
                        "email", "test@test.com"), // ✅ NOT EMPTY
                "id"
        );

        Authentication auth = new OAuth2AuthenticationToken(
                user, List.of(), "unknown"
        );

        assertThrows(RuntimeException.class, () ->
                handler.onAuthenticationSuccess(
                        new MockHttpServletRequest(),
                        new MockHttpServletResponse(),
                        auth
                )
        );
    }

//    INVALID PROVIDER
    @Test
    void shouldThrowExceptionForInvalidProvider() {

        OAuth2User user = new DefaultOAuth2User(
                List.of(),
                Map.of("id", "1"),
                "id"
        );

        Authentication auth = new OAuth2AuthenticationToken(user, List.of(), "facebook");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                handler.onAuthenticationSuccess(request, response, auth)
        );

        assertEquals("Invalid registration id", ex.getMessage());
    }

    // ================= EDGE =================

    @Test
    void shouldGenerateTokensAndRedirect() throws Exception {

        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(),
                Map.of("sub", "1", "email", "test@test.com"),
                "sub"
        );

        Authentication auth = new OAuth2AuthenticationToken(oAuth2User, List.of(), "google");

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(new User()));

        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh");

        handler.onAuthenticationSuccess(request, response, auth);

        assertEquals(302, response.getStatus()); // redirect
    }

    @Test
    void shouldHandleWhenAuthenticationIsNotOAuth2Token() throws Exception {

        Authentication auth = mock(Authentication.class);

        OAuth2User oAuth2User = mock(OAuth2User.class);

        when(auth.getPrincipal()).thenReturn(oAuth2User);

        // registrationId remains "unknown"

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                handler.onAuthenticationSuccess(request, response, auth)
        );

        assertEquals("Invalid registration id", ex.getMessage());
    }
}
