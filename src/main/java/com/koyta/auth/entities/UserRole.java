package com.koyta.auth.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("user_roles")
public class UserRole {

    @Id
    private Long id;

    private UUID userId;
    private UUID roleId;
}

