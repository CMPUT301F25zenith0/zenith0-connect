package com.example.connect.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model class representing a user report stored in the 'reports' Firestore collection.
 */
public class Report {

    // Unique ID of the report document (set manually after fetching from Firestore)
    private String reportId;

    // Fields matching the data saved in ReportDialogFragment
    private String event_id;        // The ID of the event being reported
    private String reporter_id;     // The ID of the user who filed the report
    private String description;     // The text provided by the user (the reason)
    private float severity_rating;  // The 1-5 star severity rating
    private Date timestamp;         // Server timestamp when the report was created
    private String status;          // e.g., "pending", "reviewed", "resolved"


    // Required empty public constructor for Firestore automatic data mapping
    public Report() {
    }

    // --- Getters and Setters ---

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getReporter_id() {
        return reporter_id;
    }

    public void setReporter_id(String reporter_id) {
        this.reporter_id = reporter_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getSeverity_rating() {
        return severity_rating;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods for the Adapter (since all reports are currently for events)

    /**
     * Used by the adapter for display purposes.
     * Since your fragment only handles event reports, this is hardcoded.
     */
    public String getItemType() {
        return "Event";
    }

    /**
     * Used by the adapter to display the ID of the reported item.
     * Since your fragment only handles event reports, this returns event_id.
     */
    public String getReportedItemId() {
        return event_id;
    }

    /**
     * Used by the adapter to display the reason/description.
     */
    public String getReason() {
        return description;
    }
}