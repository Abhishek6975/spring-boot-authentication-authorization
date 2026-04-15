package com.koyta.auth.exceptions;

import com.koyta.auth.dtos.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    @Test
    void shouldHandleDisabledUser() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");

        DisabledException ex = new DisabledException("User disabled");

        ResponseEntity<ApiError> response =
                handler.handleDisabledUser(ex, request);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Forbidden", response.getBody().error());
    }

    @Test
    void shouldHandleExistData() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");

        ExistDataException ex = new ExistDataException("Already exists");

        ResponseEntity<ApiError> response =
                handler.handleExistData(ex, request);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Conflict", response.getBody().error());
    }

    @Test
    void shouldHandleContractValidation() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");

        ContractValidationException ex =
                new ContractValidationException("Invalid contract");

        ResponseEntity<ApiError> response =
                handler.handleContractValidation(ex, request);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Contract Validation Failed", response.getBody().error());
    }
}
