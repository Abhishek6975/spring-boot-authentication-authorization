package com.koyta.auth.controllers;

import com.koyta.auth.dtos.LoginRequest;
import com.koyta.auth.dtos.RefreshTokenRequest;
import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "Authentication",description = "All the user Authentication API's")
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity> login(@RequestBody LoginRequest loginRequest, ServerHttpResponse response){

        return authService.login(loginRequest, response)
                .map(tokenResponse ->
                        ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse));
    }

    @PostMapping("/logout")
    public  Mono<ResponseEntity<Void>> logout(ServerHttpRequest request, ServerHttpResponse response) {

       return authService.logout(request, response)
               .then(Mono.just(ResponseEntity.noContent().build()));
    }

    // refresh token Renew Api
    @PostMapping("/refresh")
    public Mono<ResponseEntity> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
                                                      ServerHttpRequest request,
                                                      ServerHttpResponse response) {

       return authService.readRefreshTokenRequest(body, request, response)
               .map(tokenResponse ->  ResponseEntity.status(HttpStatus.OK).body(tokenResponse));

    }


    @PostMapping("/register")
    public Mono<ResponseEntity> registerUser(@RequestBody UserDto userDto) {

        return authService.registerUser(userDto)
                .map(userdto -> ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto)));
    }

}
