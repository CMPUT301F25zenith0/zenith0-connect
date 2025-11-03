package com.example.connect.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Represents a single event visible to entrants.
 * Minimal fields for listing + "joinable" logic.
 */
public class Event {

    private String id;                 // Firestore doc id
    private String name;
    private String date;               // ISO yyyy-MM-dd (first session or display date)
    private String regOpens;           // ISO yyyy-MM-dd
    private String regCloses;          // ISO yyyy-MM-dd
    private int maxParticipants;
    private String posterUrl;          // optional

    /** Required empty ctor for Firestore/serialization */
    @SuppressWarnings("unused")
    public Event() { }

    public Event(String id, String name, String date,
                 String regOpens, String regCloses,
                 int maxParticipants, String posterUrl) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.regOpens = regOpens;
        this.regCloses = regCloses;
        this.maxParticipants = maxParticipants;
        this.posterUrl = posterUrl;
    }

    // --- Getters/Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getRegOpens() { return regOpens; }
    public void setRegOpens(String regOpens) { this.regOpens = regOpens; }

    public String getRegCloses() { return regCloses; }
    public void setRegCloses(String regCloses) { this.regCloses = regCloses; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    // --- Helpers ---
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    /** True if today is within [regOpens, regCloses] (inclusive). */
    public boolean isJoinableToday() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate open = (regOpens == null || regOpens.isEmpty()) ? LocalDate.MIN : LocalDate.parse(regOpens, ISO);
            LocalDate close = (regCloses == null || regCloses.isEmpty()) ? LocalDate.MAX : LocalDate.parse(regCloses, ISO);
            return ( !today.isBefore(open) ) && ( !today.isAfter(close) );
        } catch (DateTimeParseException e) {
            // If dates are malformed, default to visible (fail-open) so the list still shows
            return true;
        }
    }
}
