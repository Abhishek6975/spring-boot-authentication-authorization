package com.koyta.auth.services;

import com.koyta.auth.dtos.UserDto;

import java.util.UUID;

public interface AdminService {

    public UserDto createAdmin(UserDto userDto) ;

    public void assignAdminRole(UUID userId);
}
