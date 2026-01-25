package com.koyta.auth.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    private String name;
    private String email;
    private String password;
    private String image;
    private boolean enable;

    private Instant createdAt;
    private Instant updatedAt;

    private Provider provider;
    private String providerId;
}
