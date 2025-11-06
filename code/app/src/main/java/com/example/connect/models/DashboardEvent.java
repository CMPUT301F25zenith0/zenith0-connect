package com.example.connect.models;

public class DashboardEvent {
    public enum Status { OPEN, CLOSED, DRAWN }

    private final String id;
    private final String title;
    private final String description;
    private final Status status;

    public DashboardEvent(String id, String title, String description, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
}
