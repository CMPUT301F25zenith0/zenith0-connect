package com.example.connect.models;

/**
 * Represents a single event in the Event Planner system.
 */
public class Event {

    private String name;
    private String date; // ISO format YYYY-MM-DD
    private int maxParticipants;

    /** Constructs a new Event */
    public Event(String name, String date, int maxParticipants) {
        this.name = name;
        this.date = date;
        this.maxParticipants = maxParticipants;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
}