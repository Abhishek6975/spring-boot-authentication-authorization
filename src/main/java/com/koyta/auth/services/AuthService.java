package com.koyta.auth.services;

import com.koyta.auth.dtos.LoginRequest;
import com.koyta.auth.dtos.RefreshTokenRequest;
import com.koyta.auth.dtos.TokenResponse;
import com.koyta.auth.dtos.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    TokenResponse login(LoginRequest loginRequest, HttpServletResponse response);

    UserDto registerUser(UserDto userDto);

    TokenResponse readRefreshTokenRequest(RefreshTokenRequest body, HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
