package com.koyta.auth.services;

import com.koyta.auth.dtos.LoginRequest;
import com.koyta.auth.dtos.RefreshTokenRequest;
import com.koyta.auth.dtos.TokenResponse;
import com.koyta.auth.dtos.UserDto;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<TokenResponse> login(LoginRequest loginRequest, ServerHttpResponse response);

    Mono<UserDto> registerUser(UserDto userDto);

     Mono<TokenResponse> readRefreshTokenRequest(RefreshTokenRequest body, ServerHttpRequest request, ServerHttpResponse response) ;

    Mono<Void> logout(ServerHttpRequest request, ServerHttpResponse response);
}
