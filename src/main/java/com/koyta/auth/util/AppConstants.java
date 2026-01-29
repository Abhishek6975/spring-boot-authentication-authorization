package com.koyta.auth.util;

public final class AppConstants {

    public static final String[] AUTH_PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"

    };

    public static final String[] AUTH_ADMIN_URLS= {
            "/api/v1/users/**"
    };

    public static final String[] AUTH_GUEST_URLS= {

    };

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String GUEST_ROLE = "GUEST";
    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String DEFAULT_ADMIN_PSWD = "admin@123";
    public static final String DEFAULT_ADMIN_EMAIL = "admin@system.com";
    public static final String ROLE_ADMIN = "hasRole('ADMIN')";

    public static final String ROLE_USER = "hasRole('USER')";

    public static final String ROLE_USER_ADMIN = "hasAnyRole('USER','ADMIN')";


}
