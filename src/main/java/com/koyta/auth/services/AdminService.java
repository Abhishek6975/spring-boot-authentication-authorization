package com.koyta.auth.services;

import com.koyta.auth.dtos.AdminResponse;
import com.koyta.auth.dtos.CreateAdminRequest;

import java.util.UUID;

public interface AdminService {

    public AdminResponse createAdmin(CreateAdminRequest request) ;

    public void assignAdminRole(UUID userId);
}
