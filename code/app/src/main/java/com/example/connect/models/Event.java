package com.example.connect.models;

import com.google.firebase.Timestamp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a single event visible to entrants.
 * Minimal fields for listing + "joinable" logic.
 */
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;                 // Firestore doc id
    private String name;
    private String date;               // ISO yyyy-MM-dd (first session or display date)
    private String time;               // Event time (e.g., "6:00 PM")
    private String location;            // Event location
    private String price;               // Event price (e.g., "$60")
    private transient com.google.firebase.Timestamp regOpens;   // Firestore Timestamp (transient for serialization)
    private transient com.google.firebase.Timestamp regCloses;  // Firestore Timestamp (transient for serialization)
    private int maxParticipants;
    private String posterUrl;          // optional
    
    // Helper fields for serialization (store as Long milliseconds)
    private Long regOpensMillis;       // For serialization
    private Long regClosesMillis;      // For serialization

    /** Required empty ctor for Firestore/serialization */
    @SuppressWarnings("unused")
    public Event() { }

    // Legacy constructor for backward compatibility (string dates)
    public Event(String id, String name, String date,
                 String regOpens, String regCloses,
                 int maxParticipants, String posterUrl) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = null;
        this.location = null;
        this.price = null;
        // Convert string dates to Timestamps if provided
        this.regOpens = regOpens != null ? dateToTimestamp(parseDateString(regOpens)) : null;
        this.regCloses = regCloses != null ? dateToTimestamp(parseDateString(regCloses)) : null;
        this.maxParticipants = maxParticipants;
        this.posterUrl = posterUrl;
    }

    public Event(String id, String name, String date, String time,
                 String location, String price,
                 com.google.firebase.Timestamp regOpens, com.google.firebase.Timestamp regCloses,
                 int maxParticipants, String posterUrl) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.location = location;
        this.price = price;
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

    // Timestamp getters/setters for Firestore
    public com.google.firebase.Timestamp getRegOpensTimestamp() { return regOpens; }
    public void setRegOpensTimestamp(com.google.firebase.Timestamp regOpens) { this.regOpens = regOpens; }
    
    public com.google.firebase.Timestamp getRegClosesTimestamp() { return regCloses; }
    public void setRegClosesTimestamp(com.google.firebase.Timestamp regCloses) { this.regCloses = regCloses; }
    
    // String getters for backward compatibility and display (converts Timestamp to formatted string)
    public String getRegOpens() {
        if (regOpens == null) return null;
        return formatDate(regOpens.toDate());
    }
    
    public String getRegCloses() {
        if (regCloses == null) return null;
        return formatDate(regCloses.toDate());
    }
    
    // Setters that accept strings (for backward compatibility)
    public void setRegOpens(String regOpens) {
        if (regOpens == null || regOpens.isEmpty()) {
            this.regOpens = null;
        } else {
            this.regOpens = dateToTimestamp(parseDateString(regOpens));
        }
    }
    
    public void setRegCloses(String regCloses) {
        if (regCloses == null || regCloses.isEmpty()) {
            this.regCloses = null;
        } else {
            this.regCloses = dateToTimestamp(parseDateString(regCloses));
        }
    }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    // --- Helpers ---
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /** True if today is within [regOpens, regCloses] (inclusive). */
    public boolean isJoinableToday() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate open = getRegOpensAsLocalDate();
            LocalDate close = getRegClosesAsLocalDate();
            
            android.util.Log.d("Event", "isJoinableToday check for event " + id + ": today=" + today + 
                ", open=" + open + ", close=" + close + ", regOpens null=" + (regOpens == null) + 
                ", regCloses null=" + (regCloses == null));
            
            boolean result = ( !today.isBefore(open) ) && ( !today.isAfter(close) );
            android.util.Log.d("Event", "isJoinableToday result for event " + id + ": " + result);
            return result;
        } catch (Exception e) {
            // If dates are malformed, default to visible (fail-open) so the list still shows
            android.util.Log.e("Event", "Error in isJoinableToday for event " + id, e);
            return true;
        }
    }
    
    /** Convert regOpens Timestamp to LocalDate */
    private LocalDate getRegOpensAsLocalDate() {
        if (regOpens == null) {
            android.util.Log.w("Event", "regOpens is null, using LocalDate.MIN");
            return LocalDate.MIN;
        }
        try {
            return regOpens.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            android.util.Log.e("Event", "Error converting regOpens to LocalDate", e);
            return LocalDate.MIN;
        }
    }
    
    /** Convert regCloses Timestamp to LocalDate */
    private LocalDate getRegClosesAsLocalDate() {
        if (regCloses == null) {
            android.util.Log.w("Event", "regCloses is null, using LocalDate.MAX");
            return LocalDate.MAX;
        }
        try {
            return regCloses.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            android.util.Log.e("Event", "Error converting regCloses to LocalDate", e);
            return LocalDate.MAX;
        }
    }
    
    /** Format date for display (e.g., "Dec 15, 2024") */
    public String formatDateForDisplay(Date date) {
        if (date == null) return "";
        return DISPLAY_FORMAT.format(date);
    }
    
    /** Format regular date field for display */
    public String getFormattedDate() {
        if (date == null || date.isEmpty()) return "";
        try {
            // Try to parse ISO format and format nicely
            Date parsedDate = ISO_DATE_FORMAT.parse(date);
            return formatDateForDisplay(parsedDate);
        } catch (Exception e) {
            // If parsing fails, return as-is
            return date;
        }
    }
    
    /** Format date as readable string */
    private String formatDate(Date date) {
        if (date == null) return "";
        return DISPLAY_FORMAT.format(date);
    }
    
    /** Parse date string (ISO format: yyyy-MM-dd) */
    private Date parseDateString(String dateStr) {
        try {
            return ISO_DATE_FORMAT.parse(dateStr);
        } catch (Exception e) {
            android.util.Log.w("Event", "Failed to parse date: " + dateStr, e);
            return new Date();
        }
    }
    
    /** Convert Date to Firebase Timestamp */
    private com.google.firebase.Timestamp dateToTimestamp(Date date) {
        if (date == null) return null;
        long millis = date.getTime();
        long seconds = millis / 1000;
        int nanoseconds = (int) ((millis % 1000) * 1_000_000);
        return new com.google.firebase.Timestamp(seconds, nanoseconds);
    }
    
    // Custom serialization to handle Timestamp fields (which are not Serializable)
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // Convert Timestamps to milliseconds for serialization
        regOpensMillis = (regOpens != null) ? regOpens.toDate().getTime() : null;
        regClosesMillis = (regCloses != null) ? regCloses.toDate().getTime() : null;
        out.writeObject(regOpensMillis);
        out.writeObject(regClosesMillis);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reconstruct Timestamps from milliseconds
        regOpensMillis = (Long) in.readObject();
        regClosesMillis = (Long) in.readObject();
        if (regOpensMillis != null) {
            regOpens = dateToTimestamp(new Date(regOpensMillis));
        }
        if (regClosesMillis != null) {
            regCloses = dateToTimestamp(new Date(regClosesMillis));
        }
    }
}
