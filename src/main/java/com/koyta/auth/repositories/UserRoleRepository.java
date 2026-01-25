package com.koyta.auth.repositories;

import com.koyta.auth.entities.UserRole;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface UserRoleRepository
        extends ReactiveCrudRepository<UserRole, Long> {

    Flux<UserRole> findByUserId(UUID userId);
}
