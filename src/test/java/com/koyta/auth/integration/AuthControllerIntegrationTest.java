package com.koyta.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koyta.auth.dtos.RegisterUserRequest;
import com.koyta.auth.repositories.RefreshTokenRepository;
import com.koyta.auth.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    RegisterUserRequest validRequest;

    @BeforeEach
    void setup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        validRequest = new RegisterUserRequest(
                "Abhi",
                "abhi@test.com",
                "password123",
                null
        );
    }

    // ================= REGISTER =================

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                .content(objectMapper.writeValueAsString(validRequest))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/v1/auth/register")
                        .content(objectMapper.writeValueAsString(validRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailWhenEmailInvalid() throws Exception {
        validRequest.setEmail("invalid");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenPasswordShort() throws Exception {
        validRequest.setPassword("123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenExtraFieldInRegister() throws Exception {
        String json = """
    {
      "name": "Abhi",
      "email": "abhi@test.com",
      "password": "password123",
      "extra": "invalid"
    }
    """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenNameTooShort() throws Exception {
        validRequest.setName("A");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // ================= LOGIN =================

    @Test
    void shouldLoginSuccessfully() throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        String loginJson = """
        {
          "email": "abhi@test.com",
          "password": "password123"
        }
        """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void shouldFailLoginInvalidCredentials() throws Exception {

        String json = """
        {
          "email": "wrong@test.com",
          "password": "wrong123"
        }
        """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailWhenLoginEmailMissing() throws Exception {
        String json = """
    {
      "password": "password123"
    }
    """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenLoginPasswordMissing() throws Exception {
        String json = """
    {
      "email": "abhi@test.com"
    }
    """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ================= REFRESH =================

    @Test
    void shouldFailWhenRefreshTokenMissing() throws Exception {

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    void shouldFailWhenRefreshTokenInvalidButFormatCorrect() throws Exception {
//
//        String json = """
//    {
//      "refreshToken": "thisIsAValidLengthButInvalidToken12345"
//    }
//    """;
//
//        mockMvc.perform(post("/api/v1/auth/refresh")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isUnauthorized());
//    }

    // ================= LOGOUT =================

    @Test
    void shouldLogoutSuccessfully() throws Exception {

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailWhenRequestBodyEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenMalformedJson() throws Exception {
        String json = "{ name: Abhi ";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
