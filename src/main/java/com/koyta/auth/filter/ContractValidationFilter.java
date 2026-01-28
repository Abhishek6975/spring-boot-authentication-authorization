package com.koyta.auth.filter;


import com.koyta.auth.contract.JsonContractValidator;
import com.koyta.auth.filter.wrapper.CachedBodyHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContractValidationFilter extends OncePerRequestFilter {

    private final JsonContractValidator validator;

    private final Map<String, String> schemaMapping = Map.of(
            "POST:/api/v1/auth/login", "contracts/auth/login.schema.json",
            "POST:/api/v1/auth/register", "contracts/auth/register-user.schema.json",
            "POST:/api/v1/auth/refresh", "contracts/auth/refresh.schema.json",
            "POST:/api/v1/user", "contracts/user/create-user.schema.json",
            "PUT:/api/v1/user/*", "contracts/user/update-user.schema.json",
            "GET:/api/v1/user/*", "contracts/user/user-response.schema.json",
            "GET:/api/v1/user/email/*", "contracts/user/user-response.schema.json"
    );

    public ContractValidationFilter(JsonContractValidator validator) {
        this.validator = validator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String key = request.getMethod() + ":" + request.getRequestURI();

        if (schemaMapping.containsKey(key)) {
            String body = request.getReader()
                    .lines()
                    .collect(Collectors.joining());

            validator.validate(body, schemaMapping.get(key));

            request = new CachedBodyHttpServletRequest(request, body);
        }

        filterChain.doFilter(request, response);
    }
}

