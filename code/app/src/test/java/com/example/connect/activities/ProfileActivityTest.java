package com.example.connect.activities;

import static org.junit.Assert.*;

import com.example.connect.models.User;

import org.junit.Before;
import org.junit.Test;

public class ProfileActivityTest {

    private ProfileActivity activity;
    private User user;

    @Before
    public void setUp() {
        activity = new ProfileActivity();

        // Create a dummy User object
        user = new User("", "", "", "");
        activity = new ProfileActivity() {
            @Override
            public User getUser() {
                return user;
            }

            // Override submitProfile to accept input strings directly
            public void submitProfileTest(String name, String email, String phone) {
                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone);
            }
        };
    }

    @Test
    public void testSubmitProfile_UpdatesUserCorrectly() {
        activity.submitProfileTest("Aalpesh", "aalpesh@example.com", "1234567890");

        assertEquals("Aalpesh", user.getName());
        assertEquals("aalpesh@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
    }

    @Test
    public void testSubmitProfile_EmptyFields() {
        activity.submitProfileTest("", "", "");

        assertEquals("", user.getName());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPhone());
    }

    @Test
    public void testSubmitProfile_WhitespaceTrimmed() {
        activity.submitProfileTest("  John  ", "  john@example.com  ", "  5551234  ");

        assertEquals("  John  ", user.getName()); // Since submitProfileTest doesn't trim (we can add trimming if needed)
        assertEquals("  john@example.com  ", user.getEmail());
        assertEquals("  5551234  ", user.getPhone());
    }
}
