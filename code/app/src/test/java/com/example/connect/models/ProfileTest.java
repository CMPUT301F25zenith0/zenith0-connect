package com.example.connect.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for User profile creation and updates.
 */
public class ProfileTest {

    @Test
    public void testUserCreation() {
        User user = new User("Alice Smith", "alice@example.com", "1234567890", "");

        // Initially, values should match constructor
        assertEquals("Alice Smith", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
    }

    @Test
    public void testProfileUpdate() {
        User user = new User("Alice Smith", "alice@example.com", "1234567890","");

        // Update profile info
        user.setName("Alice Johnson");
        user.setEmail("alicej@example.com");
        user.setPhone("0987654321");

        assertEquals("Alice Johnson", user.getName());
        assertEquals("alicej@example.com", user.getEmail());
        assertEquals("0987654321", user.getPhone());
    }
}
