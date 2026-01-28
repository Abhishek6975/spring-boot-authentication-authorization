package com.koyta.auth.services;

import com.koyta.auth.dtos.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    TokenResponse login(LoginRequest loginRequest, HttpServletResponse response);

    UserDto registerUser(RegisterUserRequest request);

    TokenResponse readRefreshTokenRequest(RefreshTokenRequest body, HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
