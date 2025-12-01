package com.example.connect.models;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for User model
 * Tests constructors, getters, setters, and data validation
 *
 * @author Digaant Chhokra
 * @date 12/01/2025
 */
public class UserTest {

    private static final String TEST_USER_ID = "user_123";
    private static final String TEST_NAME = "John Doe";
    private static final String TEST_FULL_NAME = "John Michael Doe";
    private static final String TEST_EMAIL = "john.doe@example.com";
    private static final String TEST_PHONE = "555-1234";
    private static final String TEST_IMAGE_URL = "https://example.com/profile.jpg";

    @Before
    public void setUp() {
        // Setup if needed
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    public void testDefaultConstructor() {
        User user = new User();

        assertNotNull("User should be created", user);
        assertNull("Default userId should be null", user.getUserId());
        assertNull("Default name should be null", user.getName());
        assertNull("Default email should be null", user.getEmail());
        assertNull("Default phone should be null", user.getPhone());
    }

    @Test
    public void testBasicConstructor() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE);

        assertNotNull("User should be created", user);
        assertEquals("User ID should match", TEST_USER_ID, user.getUserId());
        assertEquals("Name should match", TEST_NAME, user.getName());
        assertEquals("Email should match", TEST_EMAIL, user.getEmail());
        assertEquals("Phone should match", TEST_PHONE, user.getPhone());
    }

    @Test
    public void testFullConstructor() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE, TEST_IMAGE_URL);

        assertNotNull("User should be created", user);
        assertEquals("User ID should match", TEST_USER_ID, user.getUserId());
        assertEquals("Name should match", TEST_NAME, user.getName());
        assertEquals("Email should match", TEST_EMAIL, user.getEmail());
        assertEquals("Phone should match", TEST_PHONE, user.getPhone());
        assertEquals("Profile image URL should match", TEST_IMAGE_URL, user.getProfileImageUrl());
    }

    @Test
    public void testConstructorWithNullValues() {
        User user = new User(null, null, null, null);

        assertNotNull("User should be created", user);
        assertNull("Null userId should be accepted", user.getUserId());
        assertNull("Null name should be accepted", user.getName());
        assertNull("Null email should be accepted", user.getEmail());
        assertNull("Null phone should be accepted", user.getPhone());
    }

    // ========== GETTER AND SETTER TESTS ==========

    @Test
    public void testUserIdGetterSetter() {
        User user = new User();
        user.setUserId(TEST_USER_ID);

        assertEquals("User ID should match", TEST_USER_ID, user.getUserId());
    }

    @Test
    public void testNameGetterSetter() {
        User user = new User();
        user.setName(TEST_NAME);

        assertEquals("Name should match", TEST_NAME, user.getName());
    }

    @Test
    public void testFullNameGetterSetter() {
        User user = new User();
        user.setFullName(TEST_FULL_NAME);

        assertEquals("Full name should match", TEST_FULL_NAME, user.getFullName());
    }

    @Test
    public void testEmailGetterSetter() {
        User user = new User();
        user.setEmail(TEST_EMAIL);

        assertEquals("Email should match", TEST_EMAIL, user.getEmail());
    }

    @Test
    public void testPhoneGetterSetter() {
        User user = new User();
        user.setPhone(TEST_PHONE);

        assertEquals("Phone should match", TEST_PHONE, user.getPhone());
    }

    @Test
    public void testProfileImageUrlGetterSetter() {
        User user = new User();
        user.setProfileImageUrl(TEST_IMAGE_URL);

        assertEquals("Profile image URL should match", TEST_IMAGE_URL, user.getProfileImageUrl());
    }

    @Test
    public void testRememberMeGetterSetter() {
        User user = new User();
        user.setRememberMe(true);

        assertTrue("Remember me should be true", user.isRememberMe());

        user.setRememberMe(false);
        assertFalse("Remember me should be false", user.isRememberMe());
    }

    @Test
    public void testIsActiveGetterSetter() {
        User user = new User();
        user.setActive(true);

        assertTrue("User should be active", user.isActive());

        user.setActive(false);
        assertFalse("User should not be active", user.isActive());
    }

    @Test
    public void testLastActiveTimestampGetterSetter() {
        User user = new User();
        Long timestamp = System.currentTimeMillis();
        user.setLastActiveTimestamp(timestamp);

        assertEquals("Timestamp should match", timestamp, user.getLastActiveTimestamp());
    }

    @Test
    public void testInterestsGetterSetter() {
        User user = new User();
        List<String> interests = Arrays.asList("Technology", "Sports", "Music");
        user.setInterests(interests);

        assertNotNull("Interests should not be null", user.getInterests());
        assertEquals("Interests size should match", 3, user.getInterests().size());
        assertTrue("Should contain Technology", user.getInterests().contains("Technology"));
        assertTrue("Should contain Sports", user.getInterests().contains("Sports"));
        assertTrue("Should contain Music", user.getInterests().contains("Music"));
    }

    // ========== NULL VALUE TESTS ==========

    @Test
    public void testSetNullUserId() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE);
        user.setUserId(null);

        assertNull("User ID should be null", user.getUserId());
    }

    @Test
    public void testSetNullName() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE);
        user.setName(null);

        assertNull("Name should be null", user.getName());
    }

    @Test
    public void testSetNullEmail() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE);
        user.setEmail(null);

        assertNull("Email should be null", user.getEmail());
    }

    @Test
    public void testSetNullPhone() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE);
        user.setPhone(null);

        assertNull("Phone should be null", user.getPhone());
    }

    @Test
    public void testSetNullProfileImageUrl() {
        User user = new User();
        user.setProfileImageUrl(TEST_IMAGE_URL);
        user.setProfileImageUrl(null);

        assertNull("Profile image URL should be null", user.getProfileImageUrl());
    }

    @Test
    public void testSetNullInterests() {
        User user = new User();
        user.setInterests(Arrays.asList("Reading", "Gaming"));
        user.setInterests(null);

        assertNull("Interests should be null", user.getInterests());
    }

    @Test
    public void testSetNullLastActiveTimestamp() {
        User user = new User();
        user.setLastActiveTimestamp(System.currentTimeMillis());
        user.setLastActiveTimestamp(null);

        assertNull("Last active timestamp should be null", user.getLastActiveTimestamp());
    }

    // ========== EMPTY VALUE TESTS ==========

    @Test
    public void testSetEmptyStringUserId() {
        User user = new User();
        user.setUserId("");

        assertEquals("Empty user ID should be stored", "", user.getUserId());
        assertTrue("User ID should be empty", user.getUserId().isEmpty());
    }

    @Test
    public void testSetEmptyStringName() {
        User user = new User();
        user.setName("");

        assertEquals("Empty name should be stored", "", user.getName());
        assertTrue("Name should be empty", user.getName().isEmpty());
    }

    @Test
    public void testSetEmptyStringEmail() {
        User user = new User();
        user.setEmail("");

        assertEquals("Empty email should be stored", "", user.getEmail());
        assertTrue("Email should be empty", user.getEmail().isEmpty());
    }

    @Test
    public void testSetEmptyInterestsList() {
        User user = new User();
        List<String> emptyList = new ArrayList<>();
        user.setInterests(emptyList);

        assertNotNull("Interests should not be null", user.getInterests());
        assertTrue("Interests should be empty", user.getInterests().isEmpty());
        assertEquals("Interests size should be 0", 0, user.getInterests().size());
    }

    // ========== DATA VALIDATION TESTS ==========

    @Test
    public void testValidEmail() {
        User user = new User();
        String validEmail = "test@example.com";
        user.setEmail(validEmail);

        assertTrue("Valid email should contain @", user.getEmail().contains("@"));
        assertTrue("Valid email should contain domain", user.getEmail().contains("."));
    }

    @Test
    public void testInvalidEmail() {
        User user = new User();
        String invalidEmail = "notanemail";
        user.setEmail(invalidEmail);

        assertFalse("Invalid email should not contain @", user.getEmail().contains("@"));
    }

    @Test
    public void testPhoneNumberFormat() {
        User user = new User();
        String[] phoneFormats = {"555-1234", "(555) 123-4567", "5551234567", "+1-555-123-4567"};

        for (String phone : phoneFormats) {
            user.setPhone(phone);
            assertEquals("Phone should match format", phone, user.getPhone());
        }
    }

    @Test
    public void testImageUrlFormat() {
        User user = new User();
        String[] validUrls = {
                "https://example.com/image.jpg",
                "http://example.com/profile.png",
                "https://cdn.example.com/users/123/avatar.gif"
        };

        for (String url : validUrls) {
            user.setProfileImageUrl(url);
            assertTrue("URL should start with http",
                    user.getProfileImageUrl().startsWith("http"));
        }
    }

    // ========== INTERESTS TESTS ==========

    @Test
    public void testAddSingleInterest() {
        User user = new User();
        List<String> interests = new ArrayList<>();
        interests.add("Reading");
        user.setInterests(interests);

        assertEquals("Should have 1 interest", 1, user.getInterests().size());
        assertEquals("Interest should be Reading", "Reading", user.getInterests().get(0));
    }

    @Test
    public void testAddMultipleInterests() {
        User user = new User();
        List<String> interests = Arrays.asList("Reading", "Gaming", "Cooking", "Travel");
        user.setInterests(interests);

        assertEquals("Should have 4 interests", 4, user.getInterests().size());
    }

    @Test
    public void testModifyInterestsList() {
        User user = new User();
        List<String> interests = new ArrayList<>(Arrays.asList("Reading", "Gaming"));
        user.setInterests(interests);

        List<String> userInterests = user.getInterests();
        userInterests.add("Cooking");

        assertTrue("Should contain new interest", user.getInterests().contains("Cooking"));
        assertEquals("Should have 3 interests", 3, user.getInterests().size());
    }

    // ========== BOOLEAN FLAG TESTS ==========

    @Test
    public void testRememberMeDefaultValue() {
        User user = new User();

        assertFalse("Remember me should be false by default", user.isRememberMe());
    }

    @Test
    public void testIsActiveDefaultValue() {
        User user = new User();

        assertFalse("Is active should be false by default", user.isActive());
    }

    @Test
    public void testToggleRememberMe() {
        User user = new User();

        user.setRememberMe(true);
        assertTrue("Should be true after setting", user.isRememberMe());

        user.setRememberMe(false);
        assertFalse("Should be false after toggling", user.isRememberMe());
    }

    @Test
    public void testToggleIsActive() {
        User user = new User();

        user.setActive(true);
        assertTrue("Should be active after setting", user.isActive());

        user.setActive(false);
        assertFalse("Should be inactive after toggling", user.isActive());
    }

    // ========== TIMESTAMP TESTS ==========

    @Test
    public void testLastActiveTimestampValidValue() {
        User user = new User();
        Long currentTime = System.currentTimeMillis();
        user.setLastActiveTimestamp(currentTime);

        assertNotNull("Timestamp should not be null", user.getLastActiveTimestamp());
        assertTrue("Timestamp should be positive", user.getLastActiveTimestamp() > 0);
        assertEquals("Timestamp should match", currentTime, user.getLastActiveTimestamp());
    }

    @Test
    public void testLastActiveTimestampPastDate() {
        User user = new User();
        Long pastTime = 1609459200000L; // Jan 1, 2021
        user.setLastActiveTimestamp(pastTime);

        assertTrue("Past timestamp should be accepted", user.getLastActiveTimestamp() > 0);
        assertTrue("Past timestamp should be less than current",
                user.getLastActiveTimestamp() < System.currentTimeMillis());
    }

    @Test
    public void testLastActiveTimestampZero() {
        User user = new User();
        user.setLastActiveTimestamp(0L);

        assertEquals("Zero timestamp should be stored", Long.valueOf(0L), user.getLastActiveTimestamp());
    }

    // ========== toString() TESTS ==========

    @Test
    public void testToStringWithAllFields() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE);
        user.setFullName(TEST_FULL_NAME);
        String result = user.toString();

        assertNotNull("toString should not return null", result);
        assertTrue("Should contain userId", result.contains(TEST_USER_ID));
        assertTrue("Should contain name", result.contains(TEST_NAME));
        assertTrue("Should contain fullName", result.contains(TEST_FULL_NAME));
        assertTrue("Should contain email", result.contains(TEST_EMAIL));
        assertTrue("Should contain phone", result.contains(TEST_PHONE));
    }

    @Test
    public void testToStringWithNullFields() {
        User user = new User();
        String result = user.toString();

        assertNotNull("toString should not return null", result);
        assertTrue("Should contain 'null' for null fields", result.contains("null"));
    }

    @Test
    public void testToStringFormat() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE);
        String result = user.toString();

        assertTrue("Should start with User{", result.startsWith("User{"));
        assertTrue("Should end with }", result.endsWith("}"));
        assertTrue("Should contain equals signs", result.contains("="));
        assertTrue("Should contain single quotes", result.contains("'"));
    }

    // ========== OBJECT STATE TESTS ==========

    @Test
    public void testCompleteUserObject() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE, TEST_IMAGE_URL);
        user.setFullName(TEST_FULL_NAME);
        user.setRememberMe(true);
        user.setActive(true);
        user.setLastActiveTimestamp(System.currentTimeMillis());
        user.setInterests(Arrays.asList("Technology", "Sports"));

        // Verify all fields are set correctly
        assertEquals("User ID should be set", TEST_USER_ID, user.getUserId());
        assertEquals("Name should be set", TEST_NAME, user.getName());
        assertEquals("Full name should be set", TEST_FULL_NAME, user.getFullName());
        assertEquals("Email should be set", TEST_EMAIL, user.getEmail());
        assertEquals("Phone should be set", TEST_PHONE, user.getPhone());
        assertEquals("Profile image URL should be set", TEST_IMAGE_URL, user.getProfileImageUrl());
        assertTrue("Remember me should be true", user.isRememberMe());
        assertTrue("Should be active", user.isActive());
        assertNotNull("Timestamp should be set", user.getLastActiveTimestamp());
        assertNotNull("Interests should be set", user.getInterests());
        assertEquals("Should have 2 interests", 2, user.getInterests().size());
    }

    @Test
    public void testPartialUserObject() {
        User user = new User();
        user.setUserId(TEST_USER_ID);
        user.setName(TEST_NAME);

        assertEquals("User ID should be set", TEST_USER_ID, user.getUserId());
        assertEquals("Name should be set", TEST_NAME, user.getName());
        assertNull("Email should be null", user.getEmail());
        assertNull("Phone should be null", user.getPhone());
        assertNull("Full name should be null", user.getFullName());
    }

    @Test
    public void testUserObjectImmutability() {
        User user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE);
        String originalUserId = user.getUserId();
        String originalName = user.getName();

        // Modify the user
        user.setUserId("new_id");
        user.setName("New Name");

        // Verify values changed (User is mutable)
        assertNotEquals("User ID should be different", originalUserId, user.getUserId());
        assertNotEquals("Name should be different", originalName, user.getName());
    }
}