package com.koyta.auth.integration;

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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

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

    // ==========================
    // ✅ CREATE USER TESTS
    // ==========================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldCreateUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("abhi@test.com"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        // first user
        mockMvc.perform(post("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        // duplicate
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    //    DUPLICATE EMAIL CASE SENSITIVITY
    @Test
    @WithMockUser
    void shouldFailWhenEmailExistsWithDifferentCase() throws Exception {

        mockMvc.perform(post("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        validRequest.setEmail("ABHI@test.com");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    /*
    @Test //you can not define  email field as null Because JSON Contract Required email as String Format Compulsory.
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldFailWhenEmailIsNull() throws Exception {
        validRequest.setEmail(null);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
     */


    // ==========================
    // ✅ GET USER BY EMAIL
    // ==========================

    @Test
    @WithMockUser
    void shouldGetUserByEmail() throws Exception {
        mockMvc.perform(post("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)));

        mockMvc.perform(get("/api/v1/user/email/{email}", "abhi@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("abhi@test.com"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/user/email/{email}", "notfound@test.com"))
                .andExpect(status().isNotFound());
    }


    // ==========================
    // ✅ GET USER BY ID
    // ==========================

    @Test
    @WithMockUser
    void shouldGetUserById() throws Exception {
        String response = mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/v1/user/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/user/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ==========================
    // ✅ UPDATE USER
    // ==========================

    @Test
    @WithMockUser
    void shouldUpdateUserSuccessfully() throws Exception {
        String response = mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        String updateJson = """
                {
                  "name": "Updated Name",
                  "password": "newpassword123",
                  "enable": true
                }
                """;

        mockMvc.perform(put("/api/v1/user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }


//    UPDATE USER NOT FOUND
    @Test
    @WithMockUser
    void shouldFailWhenUpdatingNonExistingUser() throws Exception {

        String updateJson = """
    {
      "name": "Updated"
    }
    """;

        mockMvc.perform(put("/api/v1/user/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }


//    UPDATE PARTIAL EDGE CASE
//    Null update ignore hona chahiye
    @Test
    @WithMockUser
    void shouldIgnoreNullFieldsDuringUpdate() throws Exception {
        String response = mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        String updateJson = """
    {
      "name": null
    }
    """;

        mockMvc.perform(put("/api/v1/user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());
    }


    // ==========================
    // ✅ DELETE USER
    // ==========================

    @Test
    @WithMockUser
    void shouldDeleteUserSuccessfully() throws Exception {
        String response = mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/api/v1/user/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenDeletingInvalidUser() throws Exception {
        mockMvc.perform(delete("/api/v1/user/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

// DELETE TWICE (IDEMPOTENCY CHECK)
// Edge case:
//    Delete → success
//    Delete again → should be NOT FOUND
    @Test
    @WithMockUser
    void shouldFailWhenDeletingAlreadyDeletedUser() throws Exception {

        String response = mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/api/v1/user/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/user/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ==========================
    // 🔒 SECURITY TESTS
    // ==========================

    @Test
    void shouldReturnUnauthorizedWhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/user/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }


}
