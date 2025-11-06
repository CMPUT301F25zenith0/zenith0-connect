package com.example.connect.models;

public class Message {
    private final String id, title, subtext, delivered, date, time, bodyText;

    public Message(String id, String title, String subtext,
                   String delivered, String date, String time, String bodyText) {
        this.id = id;
        this.title = title;
        this.subtext = subtext;
        this.delivered = delivered;
        this.date = date;
        this.time = time;
        this.bodyText = bodyText;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtext() { return subtext; }
    public String getDelivered() { return delivered; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getBodyText() { return bodyText; }
}
