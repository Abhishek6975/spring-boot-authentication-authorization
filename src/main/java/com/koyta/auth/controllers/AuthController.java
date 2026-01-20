package com.koyta.auth.controllers;

import com.koyta.auth.dtos.LoginRequest;
import com.koyta.auth.dtos.RefreshTokenRequest;
import com.koyta.auth.dtos.TokenResponse;
import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication",description = "All the user Authentication API's")
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){

        TokenResponse TokenResponse = authService.login(loginRequest, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(TokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        authService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // refresh token Renew Api
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {

        TokenResponse tokenResponse = authService.readRefreshTokenRequest(body, request, response);

        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);

    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

}
