package com.example.connect.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the User class.
 */
public class UserTest {

    @Test
    public void testUserCreationAndGetters() {
        User u = new User("John Doe", "john@example.com", "1234567890");
        assertEquals("John Doe", u.getName());
        assertEquals("john@example.com", u.getEmail());
        assertEquals("1234567890", u.getPhone());
    }
}
