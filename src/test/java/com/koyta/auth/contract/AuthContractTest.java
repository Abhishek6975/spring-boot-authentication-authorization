package com.koyta.auth.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthContractTest {

    @Autowired
    private MockMvc mockMvc;

    // ================= LOGIN CONTRACT =================

    @Test
    void shouldFailWhenLoginEmailMissing() throws Exception {
        String json = """
        { "password": "password123" }
        """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenLoginPasswordShort() throws Exception {
        String json = """
        { "email": "test@test.com", "password": "123" }
        """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenLoginEmailInvalid() throws Exception {

        String json = """
    {
      "email": "invalid",
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
      "email": "test@test.com"
    }
    """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenLoginExtraFieldPresent() throws Exception {
        String json = """
        {
          "email": "test@test.com",
          "password": "password123",
          "extra": "invalid"
        }
        """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ================= REGISTER CONTRACT =================

    @Test
    void shouldFailWhenRegisterMissingFields() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenRegisterExtraField() throws Exception {
        String json = """
        {
          "name": "Abhi",
          "email": "abhi@test.com",
          "password": "password123",
          "unknown": "test"
        }
        """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenRegisterEmailInvalidFormat() throws Exception {

        String json = """
    {
      "name": "Abhi",
      "email": "invalid",
      "password": "password123"
    }
    """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenRegisterPasswordShort() throws Exception {

        String json = """
    {
      "name": "Abhi",
      "email": "abhi@test.com",
      "password": "123"
    }
    """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ================= REFRESH CONTRACT =================

    @Test
    void shouldFailWhenRefreshExtraField() throws Exception {
        String json = """
        { "refreshToken": "abc", "extra": "invalid" }
        """;

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenRefreshTokenTooShort() throws Exception {

        String json = """
    {
      "refreshToken": "short"
    }
    """;

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

}