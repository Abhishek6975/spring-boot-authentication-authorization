package com.koyta.auth.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.koyta.auth.entities.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(value = {"createdAt", "updatedAt", "enable", "roles"}, allowGetters = true)
public class UserDto {
    private UUID id;

    @NotBlank(message = "Name must not be blank")
    @Size(min = 3, message = "Min 3 Characters is required")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String image;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean enable = true;

    private Provider provider = Provider.LOCAL;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<RoleDto> roles = new HashSet<>();
}
