package com.koyta.auth.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class AppConstantsTest {

    // 1. Load class (static coverage)
    @Test
    void shouldLoadConstants() {

        assertNotNull(AppConstants.AUTH_PUBLIC_URLS);
        assertNotNull(AppConstants.AUTH_ADMIN_URLS);
        assertNotNull(AppConstants.AUTH_GUEST_URLS);

        assertEquals("ADMIN", AppConstants.ADMIN_ROLE);
        assertEquals("USER", AppConstants.USER);
        assertEquals("admin@123", AppConstants.DEFAULT_ADMIN_PSWD);
    }

    // 2. Constructor coverage (IMPORTANT)
    @Test
    void shouldCoverPrivateConstructor() throws Exception {

        Constructor<AppConstants> constructor =
                AppConstants.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        AppConstants instance = constructor.newInstance();

        assertNotNull(instance);
    }
}