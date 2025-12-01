package com.example.connect.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.DocumentId;


/**
 * Represents an entry in an event's waiting list
 * Links a User to their status and timestamps for an event
 *
 * @author Zenith Team
 * @version 1.0
 */
public class WaitingListEntry {

    @DocumentId
    private String documentId;

    private String userId;
    private String status; // "waiting", "selected", "enrolled", "canceled"
    private Timestamp joinedDate;
    private Timestamp selectedDate;
    private Timestamp enrolledDate;
    private Timestamp canceledDate;

    // Location fields for map view (US 02.02.02)
    @PropertyName("latitude")
    private Double latitude;
    @PropertyName("longitude")
    private Double longitude;
    @PropertyName("location_captured_at")
    private Timestamp locationCapturedAt;

    // User data (fetched separately)
    private User user;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


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

    // Location getters and setters (US 02.02.02)
    @PropertyName("latitude")
    public Double getLatitude() {
        return latitude;
    }

    @PropertyName("latitude")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @PropertyName("longitude")
    public Double getLongitude() {
        return longitude;
    }

    @PropertyName("longitude")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @PropertyName("location_captured_at")
    public Timestamp getLocationCapturedAt() {
        return locationCapturedAt;
    }

    @PropertyName("location_captured_at")
    public void setLocationCapturedAt(Timestamp locationCapturedAt) {
        this.locationCapturedAt = locationCapturedAt;
    }

    // User object (not stored in Firestore, fetched separately)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}