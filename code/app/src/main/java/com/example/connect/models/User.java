package com.example.connect.models;

/**
 * Represents a user/entrant in the Event Planner system.
 * This model corresponds to user data stored in Firebase Firestore.
 */
public class User {

    /** Unique user ID (typically Firebase UID) */
    private String userId;

    /** Full name of the user */
    private String name;

    /** Email address */
    private String email;

    /** Phone number */
    private String phone;

    /** Profile image URL (optional) */
    private String profileImageUrl;

    /** Whether the user wants to be remembered on this device */
    private boolean rememberMe;

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
     * @param name   full name
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
     * @param name            full name
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

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @com.google.firebase.firestore.PropertyName("full_name")
    public String getName() {
        return name;
    }

    @com.google.firebase.firestore.PropertyName("full_name")
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @com.google.firebase.firestore.PropertyName("mobile_num")
    public String getPhone() {
        return phone;
    }

    @com.google.firebase.firestore.PropertyName("mobile_num")
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @com.google.firebase.firestore.PropertyName("profile_image_url")
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    @com.google.firebase.firestore.PropertyName("profile_image_url")
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}