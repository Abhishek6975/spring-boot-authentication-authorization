package com.koyta.auth.services.impl;

import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.Role;
import com.koyta.auth.entities.User;
import com.koyta.auth.exceptions.ResourceNotFoundException;
import com.koyta.auth.repositories.RoleRepository;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.services.AdminService;
import com.koyta.auth.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

import static com.koyta.auth.util.AppConstants.ADMIN;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    public UserDto createAdmin(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role adminRole = roleRepository.findByName(ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        User admin = new User();
        admin.setName(userDto.getName());
        admin.setEmail(userDto.getEmail());
        admin.setPassword(passwordEncoder.encode(userDto.getPassword()));
        admin.setEnable(true);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);

        return mapToDto(admin);
    }

    @Override
    @PreAuthorize(AppConstants.ROLE_ADMIN)
    public void assignAdminRole(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("ADMIN role not found"));

        if (user.getRoles().contains(adminRole)) {
            throw new IllegalArgumentException("User is already ADMIN");
        }

        user.getRoles().add(adminRole);
        userRepository.save(user);
    }

    private UserDto mapToDto(User admin) {
        return modelMapper.map(admin , UserDto.class);
    }
}

