package com.koyta.auth.dtos;

import java.time.OffsetDateTime;

public record ApiError(
        Integer status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp
) {

    public static ApiError of(Integer status, String error, String message, String path){
        return new ApiError(status, error, message, path, OffsetDateTime.now());
    }

    public static ApiError of(Integer status, String error, String message, String path, boolean noDateTime){
        return new ApiError(status, error, message, path, null);
    }
}
