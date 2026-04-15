package com.koyta.auth.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koyta.auth.dtos.CreateUserRequest;
import com.koyta.auth.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CreateUserRequest validRequest;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        validRequest = new CreateUserRequest();
        validRequest.setName("Abhishek");
        validRequest.setEmail("abhi@test.com");
        validRequest.setPassword("password123");

    }

    // Missing email
    @Test
    @WithMockUser
    void shouldFailContractValidationWhenEmailIsMissing() throws Exception {

        String json = """
        {
          "name": "Abhishek",
          "password": "password123",
          "image": "test.png",
          "provider": "LOCAL"
        }
        """;
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // Null email
    @Test
    @WithMockUser
    void shouldFailContractValidationWhenEmailIsNull() throws Exception {

        String json = """
        {
          "name": "Abhishek",
          "email": null,
          "password": "password123",
          "image": "test.png",
          "provider": "LOCAL"
        }
        """;

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // Password missing
    @Test
    @WithMockUser
    void shouldFailWhenPasswordIsMissing() throws Exception {

        String json = """
    {
      "name": "Abhishek",
      "email": "abhi@test.com"
    }
    """;

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // EXTRA FIELD (VERY IMPORTANT 🔥)
    @Test
    @WithMockUser
    void shouldFailWhenExtraFieldIsPresent() throws Exception {

        String json = """
    {
      "name": "Abhishek",
      "email": "abhi@test.com",
      "password": "password123",
      "unknownField": "test"
    }
    """;

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    //    INVALID PROVIDER ENUM
    @Test
    @WithMockUser
    void shouldFailWhenProviderIsInvalid() throws Exception {

        String json = """
    {
      "name": "Abhishek",
      "email": "abhi@test.com",
      "password": "password123",
      "provider": "INVALID"
    }
    """;

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // Empty body
    @Test
    @WithMockUser
    void shouldFailWhenRequestBodyIsEmpty() throws Exception {

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    //    MALFORMED JSON
    @Test
    @WithMockUser
    void shouldFailWhenJsonIsMalformed() throws Exception {

        String json = "{ name: Abhishek "; // broken JSON

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }




}

