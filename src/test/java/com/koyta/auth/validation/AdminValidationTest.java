package com.koyta.auth.validation;


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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminValidationTest {

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

    //    NAME VALIDATION
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenNameIsBlank() throws Exception {

        validAdminRequest.setName("");

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenEmailInvalid() throws Exception {

        validAdminRequest.setEmail("invalid");

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFailWhenPasswordShort() throws Exception {

        validAdminRequest.setPassword("123");

        mockMvc.perform(post("/api/v1/admin/create-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAdminRequest)))
                .andExpect(status().isBadRequest());
    }



}
