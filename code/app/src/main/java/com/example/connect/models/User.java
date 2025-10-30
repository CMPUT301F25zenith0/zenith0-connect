package com.example.connect.models;


/**
 * Represents a user/entrant in the Event Planner system.
 */
public class User {

    /** Full name of the user */
    private String name;

    /** Email address */
    private String email;

    /** Phone number */
    private String phone;

    /**
     * Constructs a new User.
     *
     * @param name  full name
     * @param email email address
     * @param phone phone number
     */
    public User(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}