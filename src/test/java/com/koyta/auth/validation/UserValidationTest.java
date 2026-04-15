package com.koyta.auth.validation;


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
public class UserValidationTest {

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

    // Invalid email format
    @Test
    @WithMockUser
    void shouldFailWhenEmailFormatIsInvalid() throws Exception {

        validRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // Password too short
    @Test
    @WithMockUser
    void shouldFailWhenPasswordIsShort() throws Exception {
        validRequest.setPassword("123");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // Name blank
    @Test
    @WithMockUser
    void shouldFailWhenNameIsBlank() throws Exception {
        validRequest.setName("");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // Name too long
    @Test
    @WithMockUser
    void shouldFailWhenNameTooLong() throws Exception {

        validRequest.setName("A".repeat(1000));

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

}
