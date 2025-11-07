package com.example.connect.activities;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.widget.EditText;
import android.text.TextUtils;

import org.junit.Before;
import org.junit.Test;

public class ProfileActivityTest {

    private ProfileActivity activity;

    @Before
    public void setUp() {
        activity = new ProfileActivity();

        // Mock EditTexts (to avoid Android framework dependencies)
        activity.etName = mock(EditText.class);
        activity.etEmail = mock(EditText.class);
        activity.etPhone = mock(EditText.class);
    }

    @Test
    public void validateInputs_validData_returnsTrue() {
        assertTrue(activity.validateInputs("Aalpesh", "aalpesh@example.com", "1234567890"));
    }

    @Test
    public void validateInputs_emptyName_returnsFalse() {
        assertFalse(activity.validateInputs("", "aalpesh@example.com", "1234567890"));
    }

    @Test
    public void validateInputs_invalidEmail_returnsFalse() {
        assertFalse(activity.validateInputs("Aalpesh", "invalidemail", "1234567890"));
    }

    @Test
    public void validateInputs_shortPhone_returnsFalse() {
        assertFalse(activity.validateInputs("Aalpesh", "aalpesh@example.com", "1234"));
    }
}
