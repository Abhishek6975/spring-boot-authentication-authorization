package com.koyta.auth.dtos;

public record LoginRequest(
        String email,
        String password
) {
}
