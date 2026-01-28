package com.koyta.auth.services;

import com.koyta.auth.dtos.UserDto;
import com.koyta.auth.entities.User;

import java.util.List;

public interface UserService {

    public UserDto createUser(User user);

    UserDto getUserByEmail(String email);

    UserDto updateUser(UserDto userDto , String userId);

    void deleteUser(String userId);

    UserDto getUserById(String userId);

    List<UserDto> getAllUsers();
}
