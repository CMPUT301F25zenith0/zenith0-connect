package com.example.connect.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

/**
 * Represents an entry in an event's waiting list
 * Links a User to their status and timestamps for an event
 *
 * @author Zenith Team
 * @version 1.0
 */
public class WaitingListEntry {
    private String userId;
    private String status; // "waiting", "selected", "enrolled", "canceled"
    private Timestamp joinedDate;
    private Timestamp selectedDate;
    private Timestamp enrolledDate;
    private Timestamp canceledDate;

    // User data (fetched separately)
    private User user;

    /**
     * Empty constructor required for Firestore
     */
    public WaitingListEntry() {
    }

    /**
     * Constructor for creating a new waiting list entry
     */
    public WaitingListEntry(String userId, String status, Timestamp joinedDate) {
        this.userId = userId;
        this.status = status;
        this.joinedDate = joinedDate;
    }

    // Getters and Setters
    @PropertyName("user_id")
    public String getUserId() {
        return userId;
    }

    @PropertyName("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("joined_date")
    public Timestamp getJoinedDate() {
        return joinedDate;
    }

    @PropertyName("joined_date")
    public void setJoinedDate(Timestamp joinedDate) {
        this.joinedDate = joinedDate;
    }

    @PropertyName("selected_date")
    public Timestamp getSelectedDate() {
        return selectedDate;
    }

    @PropertyName("selected_date")
    public void setSelectedDate(Timestamp selectedDate) {
        this.selectedDate = selectedDate;
    }

    @PropertyName("enrolled_date")
    public Timestamp getEnrolledDate() {
        return enrolledDate;
    }

    @PropertyName("enrolled_date")
    public void setEnrolledDate(Timestamp enrolledDate) {
        this.enrolledDate = enrolledDate;
    }

    @PropertyName("canceled_date")
    public Timestamp getCanceledDate() {
        return canceledDate;
    }

    @PropertyName("canceled_date")
    public void setCanceledDate(Timestamp canceledDate) {
        this.canceledDate = canceledDate;
    }

    // User object (not stored in Firestore, fetched separately)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}