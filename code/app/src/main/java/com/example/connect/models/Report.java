package com.example.connect.models;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

/**
 * Model class representing a user report stored in the 'reports' Firestore collection.
 * Implements Serializable to allow passing objects between Fragments/Activities.
 */
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    // Unique ID of the report document (set manually after fetching from Firestore)
    private String reportId;

    // Fields matching the Firestore document keys
    private String event_id;
    private String reporter_id;
    private String description;
    private float severity_rating;
    private Date timestamp;
    private String status;

    public Report() {
        // Required empty public constructor
    }

    // --- Getters and Setters ---

    // ID is usually manual, so no PropertyName needed unless it's a field in DB
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    // --- Explicitly Map "event_id" ---
    @PropertyName("event_id")
    public String getEvent_id() {
        return event_id;
    }

    @PropertyName("event_id")
    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    @PropertyName("reporter_id")
    public String getReporter_id() {
        return reporter_id;
    }

    @PropertyName("reporter_id")
    public void setReporter_id(String reporter_id) {
        this.reporter_id = reporter_id;
    }

    // --- Description usually maps fine, but explicit is safer ---
    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    // --- Explicitly Map "severity_rating" ---
    @PropertyName("severity_rating")
    public float getSeverity_rating() {
        return severity_rating;
    }

    @PropertyName("severity_rating")
    public void setSeverity_rating(float severity_rating) {
        this.severity_rating = severity_rating;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    // --- Helper methods for the Adapter ---

    public String getItemType() {
        return "Event";
    }

    public String getReportedItemId() {
        return event_id;
    }

    public String getReason() {
        return description;
    }
}