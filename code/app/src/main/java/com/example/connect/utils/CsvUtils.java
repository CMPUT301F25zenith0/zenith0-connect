package com.example.connect.utils;

import java.util.List;

/**
 * Helper utilities for building CSV strings.
 *
 * Currently used to export enrolled entrants for an event.
 *
 * Columns:
 *  - Name
 *  - Email
 *  - Phone
 *  - Joined Date
 *  - Event Name
 *  - Event Id
 */
public class CsvUtils {

    /**
     * Simple data holder for one CSV row.
     */
    public static class CsvRow {
        public final String name;
        public final String email;
        public final String phone;
        public final String joinedDate; // pre-formatted string

        public CsvRow(String name, String email, String phone, String joinedDate) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.joinedDate = joinedDate;
        }
    }

    /**
     * Builds a CSV string for enrolled entrants of a single event.
     *
     * @param eventName Name of the event (may be null)
     * @param eventId   Firestore document ID of the event
     * @param rows      List of rows (one per enrolled entrant)
     * @return CSV text (including header row)
     */
    public static String buildEnrolledEntrantsCsv(String eventName,
                                                  String eventId,
                                                  List<CsvRow> rows) {

        String safeEventName = eventName != null ? eventName : "";
        String safeEventId = eventId != null ? eventId : "";

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("Name,Email,Phone,Joined Date,Event Name,Event Id\n");

        // Rows
        if (rows != null) {
            for (CsvRow row : rows) {
                sb.append(escape(row.name)).append(",");
                sb.append(escape(row.email)).append(",");
                sb.append(escape(row.phone)).append(",");
                sb.append(escape(row.joinedDate)).append(",");
                sb.append(escape(safeEventName)).append(",");
                sb.append(escape(safeEventId)).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Escapes a value for safe CSV output.
     * - Null -> empty string
     * - Doubles any double-quotes
     * - Wraps the whole value in double-quotes
     */
    public static String escape(String value) {
        if (value == null) {
            value = "";
        }
        // Replace " with ""
        String escaped = value.replace("\"", "\"\"");
        // Always wrap in quotes (simpler & safe for commas/newlines)
        return "\"" + escaped + "\"";
    }
}
