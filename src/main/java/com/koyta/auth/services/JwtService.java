package com.koyta.auth.services;

import com.koyta.auth.entities.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user, String jti);

    boolean isAccessToken(String token);

    boolean isRefreshToken(String token);

    String getUserId(String token);

    String getJti(String token);

    String extractUserName(String token);

    Boolean validateToken(String token, UserDetails userDetails);


}
