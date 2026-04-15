package com.koyta.auth.entity;

import com.koyta.auth.entities.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserEntityTest {

    @Test
    void shouldSetCreatedAtWhenNull_onPrePersist() {

        User user = new User();

        // 🔥 force null (important)
        user.setCreatedAt(null);

        user.onCreate();

        assertNotNull(user.getCreatedAt()); // branch hit
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldNotOverrideCreatedAtWhenAlreadySet() {

        Instant fixed = Instant.now();

        User user = new User();
        user.setCreatedAt(fixed);

        user.onCreate();

        assertEquals(fixed, user.getCreatedAt()); // second branch
    }
}
