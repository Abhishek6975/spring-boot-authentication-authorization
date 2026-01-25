package com.koyta.auth.repositories;

import com.koyta.auth.entities.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoleRepository
        extends ReactiveCrudRepository<Role, UUID> {

    Mono<Role> findByName(String name);

}
