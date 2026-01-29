package com.koyta.auth.config;
import com.koyta.auth.entities.Role;
import com.koyta.auth.entities.User;
import com.koyta.auth.repositories.RoleRepository;
import com.koyta.auth.repositories.UserRepository;
import com.koyta.auth.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {

        Role adminRole = roleRepository.findByName(AppConstants.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, AppConstants.ADMIN)));

        boolean adminExists = userRepository.existsByRoles_Name(AppConstants.ADMIN);

        if (!adminExists) {
            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail(AppConstants.DEFAULT_ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(AppConstants.DEFAULT_ADMIN_PSWD));
            admin.setEnable(true);
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
        }
    }
}

