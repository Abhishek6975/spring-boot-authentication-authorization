package com.koyta.auth.filter;

import com.koyta.auth.contract.JsonContractValidator;
import com.koyta.auth.exceptions.ContractValidationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractValidationFilterTest {

    @Test
    void shouldValidateAndPassRequest() throws Exception {

        JsonContractValidator validator = mock(JsonContractValidator.class);

        ContractValidationFilter filter =
                new ContractValidationFilter(validator);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        when(request.getReader())
                .thenReturn(new BufferedReader(new StringReader("{\"a\":1}")));

        filter.doFilterInternal(request, response, chain);

        verify(validator).validate(anyString(), anyString());
        verify(chain).doFilter(any(), eq(response));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {

        JsonContractValidator validator = mock(JsonContractValidator.class);

        ContractValidationFilter filter =
                new ContractValidationFilter(validator);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        when(request.getReader())
                .thenReturn(new BufferedReader(new StringReader("{}")));

        PrintWriter writer = new PrintWriter(new StringWriter());
        when(response.getWriter()).thenReturn(writer);

        doThrow(new ContractValidationException("Invalid"))
                .when(validator).validate(anyString(), anyString());

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).getWriter();
        verify(chain, never()).doFilter(any(), any());
    }

    //  KEY NOT FOUND CASE
    @Test
    void shouldSkipValidationWhenNoSchemaMapping() throws Exception {

        JsonContractValidator validator = mock(JsonContractValidator.class);

        ContractValidationFilter filter =
                new ContractValidationFilter(validator);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/unknown");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(validator);
    }
}
