package com.koyta.auth.services;

import com.koyta.auth.dtos.UserDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<UserDto> createUser(UserDto userDto);

    Mono<UserDto> getUserByEmail(String email);

    Mono<UserDto> updateUser(UserDto userDto , String userId);

    Mono<Void> deleteUser(String userId);

    Mono<UserDto> getUserById(String userId);

    Flux<UserDto> getAllUsers();



}

