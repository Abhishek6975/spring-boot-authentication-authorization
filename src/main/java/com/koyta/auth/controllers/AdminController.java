package com.koyta.auth.controllers;


import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.services.impl.AdminServiceImpl;
import com.koyta.auth.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.status;

@RestController
@Tag(name = "Admin", description = "Admin authentication and user management APIs")
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminServiceImpl adminService;

    @Operation(summary = "Create Admin User", description = "Creates a new admin user in the system",
            tags = {"Admin"})
    @PreAuthorize(AppConstants.ROLE_ADMIN)
    @PostMapping("/create-admin")
    public ResponseEntity<UserDto> createAdmin(@RequestBody UserDto userDto) {

        return status(HttpStatus.CREATED).body(adminService.createAdmin(userDto));
    }

    @Operation(
            summary = "Assign Admin Role", description = "Assigns ADMIN role to an existing user",
            tags = {"Admin"}
    )
    @PreAuthorize(AppConstants.ROLE_ADMIN)
    @PostMapping("/assign-admin/{userId}")
    public ResponseEntity<String> assignAdminRole(@PathVariable("userId") UUID userId) {

        adminService.assignAdminRole(userId);
        return ResponseEntity.ok("ADMIN role assigned successfully");

    }
}

