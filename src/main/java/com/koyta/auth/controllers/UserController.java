package com.koyta.auth.controllers;

import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.UserRole;
import com.koyta.auth.repositories.RoleRepository;
import com.koyta.auth.repositories.UserRoleRepository;
import com.koyta.auth.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Tag(name = "User",description = "Authetication User Operation API's")
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;

    @PostMapping
    public Mono<ResponseEntity<UserDto>> createUser(@RequestBody UserDto userDto) {

        return userService.createUser(userDto).flatMap(user ->
                assignRoleToUser(user.getId(), "ROLE_USER")
                        .thenReturn(user))
                .map(savedUser ->
                        ResponseEntity.status(HttpStatus.CREATED).body(savedUser));
    }

    private Mono<Void> assignRoleToUser(UUID userId, String roleName) {

        return roleRepository.findByName(roleName)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("Role not found")))
                .flatMap(role -> {
                    UserRole userRole = UserRole.builder()
                            .userId(userId)
                            .roleId(role.getId())
                            .build();

                    return userRoleRepository.save(userRole).then();
                });
    }

    @GetMapping("/{userid}")
    public Mono<ResponseEntity<UserDto>> getUserById(@PathVariable String userid) {

        return userService.getUserById(userid).map(ResponseEntity::ok);
    }

    @GetMapping("/email/{email}")
    public Mono<ResponseEntity<UserDto>> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{userid}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String userid) {

        return userService.deleteUser(userid).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PutMapping("/{userid}")
    public Mono<ResponseEntity<UserDto>> updateUser(@RequestBody UserDto userDto, @PathVariable String userid) {

        return userService.updateUser(userDto, userid).map(ResponseEntity::ok);
    }

    @GetMapping("/")
    public Flux<ResponseEntity<UserDto>> getAllUsers() {

        return userService.getAllUsers().map(ResponseEntity::ok);
    }


}
