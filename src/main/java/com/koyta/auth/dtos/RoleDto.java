package com.koyta.auth.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UUID id;
    private String name;
}
