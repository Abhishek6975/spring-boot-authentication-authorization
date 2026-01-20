package com.koyta.auth.services;

import com.koyta.auth.dtos.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUserByEmail(String email);

    UserDto updateUser(UserDto userDto , String userId);

    void deleteUser(String userId);

    UserDto getUserById(String userId);

    List<UserDto> getAllUsers();
}
