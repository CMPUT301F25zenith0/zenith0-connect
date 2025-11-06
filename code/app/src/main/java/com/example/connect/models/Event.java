package com.example.connect.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

/**
 * Represents a single event in the Event Planner system.
 */
public class Event {

    @DocumentId
    private String eventId;

    @PropertyName("event_title")
    private String name;

    private String description;

    @PropertyName("date_time")
    private String dateTime;

    private String location;
    private String price;

    @PropertyName("max_participants")
    private int maxParticipants;

    @PropertyName("current_participants")
    private int currentParticipants;

    @PropertyName("org_name")
    private String organizerId;

    @PropertyName("imageUrl")
    private String imageUrl;

    private String category;

    @PropertyName("reg_start")
    private String regStart;

    @PropertyName("reg_stop")
    private String regStop;

    @PropertyName("waiting_list")
    private Long waitingListCount;

    /** Default constructor required for Firestore */
    public Event() {
        // Required for Firestore deserialization
    }

    /** Constructs a new Event with basic info */
    public Event(String name, String dateTime, int maxParticipants) {
        this.name = name;
        this.dateTime = dateTime;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 0;
        this.price = "Free";
    }

    // Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    @PropertyName("event_title")
    public String getName() { return name; }

    @PropertyName("event_title")
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @PropertyName("date_time")
    public String getDateTime() { return dateTime; }

    @PropertyName("date_time")
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    @PropertyName("max_participants")
    public int getMaxParticipants() { return maxParticipants; }

    @PropertyName("max_participants")
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    @PropertyName("current_participants")
    public int getCurrentParticipants() { return currentParticipants; }

    @PropertyName("current_participants")
    public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }

    @PropertyName("org_name")
    public String getOrganizerId() { return organizerId; }

    @PropertyName("org_name")
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @PropertyName("reg_start")
    public String getRegStart() { return regStart; }

    @PropertyName("reg_start")
    public void setRegStart(String regStart) { this.regStart = regStart; }

    @PropertyName("reg_stop")
    public String getRegStop() { return regStop; }

    @PropertyName("reg_stop")
    public void setRegStop(String regStop) { this.regStop = regStop; }

    @PropertyName("waiting_list")
    public Long getWaitingListCount() { return waitingListCount; }

    @PropertyName("waiting_list")
    public void setWaitingListCount(Long waitingListCount) { this.waitingListCount = waitingListCount; }

    /**
     * Check if event is at capacity
     */
    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }
}