package com.example.connect.models;


import com.example.connect.activities.ProfileActivity;
import com.example.connect.models.User;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ProfileActivity feature.
 * These tests simulate creating/updating a User profile through the activity.
 */
public class ProfileActivityTest {

    private ProfileActivity activity;

    @Before
    public void setUp() {
        activity = new ProfileActivity();
    }

    @Test
    public void testProfileSubmissionUpdatesUser() {
        // Simulate user input
        activity.setNameInput("Alice Johnson");
        activity.setEmailInput("alicej@example.com");
        activity.setPhoneInput("0987654321");

        // Simulate clicking Submit
        activity.submitProfile();

        // Verify User object was updated correctly
        User user = activity.getUser();
        assertEquals("Alice Johnson", user.getName());
        assertEquals("alicej@example.com", user.getEmail());
        assertEquals("0987654321", user.getPhone());
    }
}
