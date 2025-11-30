package com.example.connect.models;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

/**
 * Represents a user/entrant in the Event Planner system.
 * This model corresponds to user data stored in Firebase Firestore.
 */
public class User {

    /** Unique user ID (typically Firebase UID) */
    @PropertyName("user_id")
    private String userId;

    /** Display name of the user (maps to display_name in Firestore) */
    @PropertyName("display_name")
    private String name;

    /** Full name of the user */
    @PropertyName("full_name")
    private String fullName;

    /** Email address */
    private String email;

    /** Phone number */
    @PropertyName("mobile_num")
    private String phone;

    /** Profile image URL (optional) */
    private String profileImageUrl;

    /** Whether the user wants to be remembered on this device */
    private boolean rememberMe;

    /** User active status */
    @PropertyName("is_active")
    private boolean isActive;

    /** Last active timestamp */
    @PropertyName("last_active_timestamp")
    private Long lastActiveTimestamp;



    /**
     * Default constructor required for Firebase Firestore deserialization.
     */
    public User() {
        // Empty constructor needed for Firestore
    }

    /**
     * Constructs a new User with basic information.
     *
     * @param userId unique user identifier
     * @param name   display name
     * @param email  email address
     * @param phone  phone number
     */
    public User(String userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Constructs a new User with all information.
     *
     * @param userId          unique user identifier
     * @param name            display name
     * @param email           email address
     * @param phone           phone number
     * @param profileImageUrl URL to profile image
     */
    public User(String userId, String name, String email, String phone, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters with @PropertyName annotations

    @PropertyName("user_id")
    public String getUserId() { return userId; }

    @PropertyName("user_id")
    public void setUserId(String userId) { this.userId = userId; }

    @PropertyName("display_name")
    public String getName() { return name; }

    @PropertyName("display_name")
    public void setName(String name) { this.name = name; }

    @PropertyName("full_name")
    public String getFullName() { return fullName; }

    @PropertyName("full_name")
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @PropertyName("mobile_num")
    public String getPhone() { return phone; }

    @PropertyName("mobile_num")
    public void setPhone(String phone) { this.phone = phone; }


    @com.google.firebase.firestore.PropertyName("profile_image_url")
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    @com.google.firebase.firestore.PropertyName("profile_image_url")
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    // Inside User.java
    private List<String> interests;

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }


    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @PropertyName("is_active")
    public boolean isActive() { return isActive; }

    @PropertyName("is_active")
    public void setActive(boolean active) { isActive = active; }

    @PropertyName("last_active_timestamp")
    public Long getLastActiveTimestamp() { return lastActiveTimestamp; }

    @PropertyName("last_active_timestamp")
    public void setLastActiveTimestamp(Long lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}