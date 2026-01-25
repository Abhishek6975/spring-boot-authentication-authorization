package com.koyta.auth.exceptions;

import com.koyta.auth.dtos.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ReactiveControllerExceptionHandler {

    @ExceptionHandler(DisabledException.class)
    public Mono<ResponseEntity<ApiError>> handleDisabledUser(DisabledException ex, ServerHttpRequest request) {
        ApiError error = ApiError.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden", ex.getMessage(),
                request.getPath().value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ApiError>> handleValidation(MethodArgumentNotValidException ex, ServerHttpRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        ApiError error = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                request.getPath().value()
        );

        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiError>> handleIllegalArgumentException(IllegalArgumentException ex, ServerHttpRequest request) {

        ApiError apiError = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Bad Request",
                ex.getMessage(),
                request.getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(apiError));
    }

    @ExceptionHandler(ExistDataException.class)
    public Mono<ResponseEntity<ApiError>> handleExistData(ExistDataException ex, ServerHttpRequest request) {

        ApiError error = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getPath().value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ApiError>> handleAccessDenied(AccessDeniedException ex,
            ServerHttpRequest request) {

        ApiError error = ApiError.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Access is denied",
                request.getPath().value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(error));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleResourceNotFound(ResourceNotFoundException ex, ServerHttpRequest request) {

        ApiError error = ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getPath().value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGlobalException(Exception ex, ServerHttpRequest request) {
        ApiError error = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Something went wrong",
                request.getPath().value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}