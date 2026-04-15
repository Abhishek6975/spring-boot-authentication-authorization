package com.koyta.auth.helpers;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserHelperTest {

    @Test
    void shouldCoverConstructor() throws Exception {

        var constructor = UserHelper.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        UserHelper instance = constructor.newInstance();

        assertNotNull(instance);
    }

    @Test
    void shouldParseValidUUID() {

        String uuidStr = UUID.randomUUID().toString();

        UUID result = UserHelper.parseUUID(uuidStr);

        assertEquals(uuidStr, result.toString());
    }

    @Test
    void shouldThrowForInvalidUUID() {

        assertThrows(IllegalArgumentException.class, () ->
                UserHelper.parseUUID("invalid-uuid"));
    }
}
