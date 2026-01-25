package com.koyta.auth.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("refresh_tokens")
public class RefreshToken {

    @Id
    private UUID id;

    private String jti;

    // Instead of @ManyToOne User
    private UUID userId;

    private Instant createdAt;
    private Instant expiresAt;

    private boolean revoked;

    private String replacedByToken;
}
