package com.koyta.auth.exceptions;

public class JwtTokenExpiredException extends RuntimeException{

    public JwtTokenExpiredException(String message) {
        super(message);
    }

}

