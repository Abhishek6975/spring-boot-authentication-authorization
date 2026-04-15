package com.koyta.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koyta.auth.dtos.CreateAdminRequest;
import com.koyta.auth.entities.Role;
import com.koyta.auth.repositories.RoleRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    CreateAdminRequest validAdminRequest;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // ✅ EXACT SAME NAME as AppConstants.ADMIN
        roleRepository.save(new Role(null, "ADMIN"));

        validAdminRequest = new CreateAdminRequest();
        validAdminRequest.setName("Admin User");
        validAdminRequest.setEmail("admin@test.com");
        validAdminRequest.setPassword("password123");
    }

    // ==========================
    // ✅ CREATE ADMIN
    // ==========================

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAdminSuccessfully() throws Exception {

        System.out.println("values : " + objectMapper.writeValueAsString(validAdminRequest));

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin@test.com"));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenAdminEmailAlreadyExists() throws Exception {

        mockMvc.perform(post("/api/v1/admin/create-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAdminRequest)));

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isConflict());
    }


    //    ROLE NOT FOUND (VERY IMPORTANT
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenAdminRoleNotPresent() throws Exception {

        roleRepository.deleteAll(); // remove ADMIN role

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isInternalServerError());
    }

    // ==========================
    // 🔒 SECURITY TESTS
    // ==========================

    @Test
    @WithMockUser(roles = "USER")
    void shouldFailWhenUserIsNotAdmin() throws Exception {

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailWhenNoAuthentication() throws Exception {

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isUnauthorized());
    }

//    PASSWORD NULL / MISSING
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenPasswordIsMissing() throws Exception {

        String json = """
    {
      "name": "Admin User",
      "email": "admin@test.com"
    }
    """;

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }


    // ==========================
    // ❌ VALIDATION TESTS
    // ==========================

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenEmailInvalid() throws Exception {

        validAdminRequest.setEmail("invalid");

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isBadRequest());
    }


//    EMPTY BODY
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenRequestBodyIsEmpty() throws Exception {

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

//    MALFORMED JSON
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenJsonIsMalformed() throws Exception {

        String json = "{ name: Admin ";

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ==========================
    // ✅ ASSIGN ADMIN ROLE
    // ==========================

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAssignAdminRoleSuccessfully() throws Exception {

        // create normal user
        String response = mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "User",
                          "email": "user@test.com",
                          "password": "password123"
                        }
                        """))
                .andReturn().getResponse().getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/v1/admin/assign-admin/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("ADMIN role assigned successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenUserNotFound() throws Exception {

        mockMvc.perform(post("/api/v1/admin/assign-admin/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenUserAlreadyAdmin() throws Exception {

        // create admin user
        String response = mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andReturn().getResponse().getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/v1/admin/assign-admin/{id}", userId))
                .andExpect(status().isConflict());
    }

//    ASSIGN ADMIN WITHOUT AUTH
    @Test
    void shouldFailAssignAdminWhenNoAuth() throws Exception {

        mockMvc.perform(post("/api/v1/admin/assign-admin/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

//    ASSIGN ADMIN WITH NON-ADMIN ROLE
    @Test
    @WithMockUser(roles = "USER")
    void shouldFailAssignAdminWhenUserIsNotAdmin() throws Exception {

        mockMvc.perform(post("/api/v1/admin/assign-admin/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // ==========================
    // ❌ INVALID UUID
    // ==========================

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenInvalidUUID() throws Exception {

        mockMvc.perform(post("/api/v1/admin/assign-admin/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

}
