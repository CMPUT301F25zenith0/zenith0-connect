package com.example.connect.models;

import com.google.firebase.Timestamp;

/**
 * Represents a single event in the Event Planner system.
 */
public class Event {

    private String name;
    private Timestamp date; // Use Timestamp instead of String for consistency
    private int maxParticipants;

    private String organizerId;
    private Timestamp registrationStart;
    private Timestamp registrationEnd;

    /** Empty constructor required for Firestore */
    public Event() {}

    /** Full constructor */
    public Event(String name, Timestamp date, int maxParticipants,
                 String organizerId, Timestamp registrationStart, Timestamp registrationEnd) {
        this.name = name;
        this.date = date;
        this.maxParticipants = maxParticipants;
        this.organizerId = organizerId;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public Timestamp getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(Timestamp registrationStart) { this.registrationStart = registrationStart; }

    public Timestamp getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(Timestamp registrationEnd) { this.registrationEnd = registrationEnd; }
}
