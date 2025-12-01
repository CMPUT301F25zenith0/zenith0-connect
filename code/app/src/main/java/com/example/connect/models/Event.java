package com.example.connect.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.util.List;
import java.util.Objects;

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
    @PropertyName("location_latitude")
    private Double locationLatitude;
    @PropertyName("location_longitude")
    private Double locationLongitude;
    private String price;

    @PropertyName("max_participants")
    private int maxParticipants;

    @PropertyName("current_participants")
    private int currentParticipants;

    @PropertyName("org_name")
    private String organizerId;

    @PropertyName("imageUrl")
    private String imageUrl;

    @PropertyName("image_base64")
    private String imageBase64;

    private String category;

    @PropertyName("labels")
    private List<String> labels;

    @PropertyName("reg_start")
    private String regStart;

    @PropertyName("reg_stop")
    private String regStop;

    @PropertyName("waiting_list")
    private Long waitingListCount;

    @PropertyName("draw_capacity")
    private int drawCapacity;

    @PropertyName("end_time")
    private String endTime;

    // NEW: Lottery-related fields
    @PropertyName("draw_completed")
    private boolean drawCompleted = false;

    @PropertyName("draw_date")
    private Timestamp drawDate;

    @PropertyName("selected_count")
    private int selectedCount = 0;

    // US 02.02.03: Geolocation requirement toggle
    @PropertyName("require_geolocation")
    private boolean requireGeolocation = false;

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
        this.drawCompleted = false;
        this.selectedCount = 0;
        this.unresponsiveDurationHours = 24L;
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

    @PropertyName("location_latitude")
    public Double getLocationLatitude() {
        return locationLatitude;
    }

    @PropertyName("location_latitude")
    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    @PropertyName("location_longitude")
    public Double getLocationLongitude() {
        return locationLongitude;
    }

    @PropertyName("location_longitude")
    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

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

    @PropertyName("image_base64")
    public String getImageBase64() { return imageBase64; }

    @PropertyName("image_base64")
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @PropertyName("labels")
    public List<String> getLabels() { return labels; }

    @PropertyName("labels")
    public void setLabels(List<String> labels) { this.labels = labels; }

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

    @PropertyName("draw_capacity")
    public int getDrawCapacity() { return drawCapacity; }

    @PropertyName("draw_capacity")
    public void setDrawCapacity(int drawCapacity) { this.drawCapacity = drawCapacity; }

    @PropertyName("end_time")
    public String getEndTime() { return endTime; }

    @PropertyName("end_time")
    public void setEndTime(String endTime) { this.endTime = endTime; }

    // NEW: Lottery getters and setters
    @PropertyName("draw_completed")
    public boolean isDrawCompleted() { return drawCompleted; }

    @PropertyName("draw_completed")
    public void setDrawCompleted(boolean drawCompleted) { this.drawCompleted = drawCompleted; }

    @PropertyName("draw_date")
    public Timestamp getDrawDate() { return drawDate; }

    @PropertyName("draw_date")
    public void setDrawDate(Timestamp drawDate) { this.drawDate = drawDate; }

    @PropertyName("selected_count")
    public int getSelectedCount() { return selectedCount; }

    @PropertyName("selected_count")
    public void setSelectedCount(int selectedCount) { this.selectedCount = selectedCount; }

    // US 02.02.03: Geolocation requirement getters and setters
    @PropertyName("require_geolocation")
    public boolean isRequireGeolocation() {
        return requireGeolocation;
    }

    @PropertyName("require_geolocation")
    public void setRequireGeolocation(boolean requireGeolocation) {
        this.requireGeolocation = requireGeolocation;
    }

    @PropertyName("unresponsive_hours")
    private Long unresponsiveDurationHours; // nullable, optional

    /**
     * Check if event is at capacity
     */
    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return maxParticipants == event.maxParticipants &&
                currentParticipants == event.currentParticipants &&
                drawCapacity == event.drawCapacity &&
                drawCompleted == event.drawCompleted &&
                selectedCount == event.selectedCount &&
                Objects.equals(eventId, event.eventId) &&
                Objects.equals(name, event.name) &&
                Objects.equals(description, event.description) &&
                Objects.equals(dateTime, event.dateTime) &&
                Objects.equals(location, event.location) &&
                Objects.equals(locationLatitude, event.locationLatitude) &&
                Objects.equals(locationLongitude, event.locationLongitude) &&
                Objects.equals(price, event.price) &&
                Objects.equals(organizerId, event.organizerId) &&
                Objects.equals(imageUrl, event.imageUrl) &&
                Objects.equals(imageBase64, event.imageBase64) &&
                Objects.equals(category, event.category) &&
                Objects.equals(labels, event.labels) &&
                Objects.equals(regStart, event.regStart) &&
                Objects.equals(regStop, event.regStop) &&
                Objects.equals(endTime, event.endTime) &&
                Objects.equals(drawDate, event.drawDate) &&
                Objects.equals(waitingListCount, event.waitingListCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, name, description, dateTime, location, locationLatitude,
                locationLongitude, price,
                maxParticipants, currentParticipants, organizerId, imageUrl, imageBase64,
                category, labels, regStart, regStop, waitingListCount, drawCapacity, endTime,
                drawCompleted, drawDate, selectedCount);
    }

    @PropertyName("unresponsive_hours")
    public Long getUnresponsiveDurationHours() {
        return unresponsiveDurationHours != null ? unresponsiveDurationHours : 24L; // default 24h
    }

    @PropertyName("unresponsive_hours")
    public void setUnresponsiveDurationHours(Long unresponsiveDurationHours) {
        this.unresponsiveDurationHours = unresponsiveDurationHours;
    }

}