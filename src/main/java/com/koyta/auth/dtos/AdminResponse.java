package com.koyta.auth.dtos;

import com.koyta.auth.entities.Provider;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminResponse {

    private UUID id;
    private String name;
    private String email;
    private boolean enable;
    private Provider provider;
    private Set<RoleDto> roles;
}
