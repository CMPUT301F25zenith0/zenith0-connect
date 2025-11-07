package com.example.connect.constants;

/**
 * App-wide constants for Zenith-Connect.
 * Update Firestore field/collection names here if your schema changes.
 */
public final class AppConstants {
    private AppConstants() {} // no instances

    // ---------------------------------------------------------------------
    // Default limits / feature flags
    // ---------------------------------------------------------------------
    public static final int MAX_EVENT_PARTICIPANTS = 20;
    public static final int MIN_AGE_LIMIT = 5;

    public static final boolean FEATURE_ENABLE_LOTTERY = true;

    // ---------------------------------------------------------------------
    // SharedPreferences keys
    // ---------------------------------------------------------------------
    public static final String PREF_USER_ID = "pref_user_id";
    public static final String PREF_PROFILE_COMPLETED = "pref_profile_completed";

    // ---------------------------------------------------------------------
    // Firestore: collections
    // ---------------------------------------------------------------------
    /** Top-level collection for events */
    public static final String COL_EVENTS = "events";

    // ---------------------------------------------------------------------
    // Firestore: event document field names (match your console)
    // ---------------------------------------------------------------------
    /** Event name (String) */
    public static final String F_NAME = "name";
    /** Event date as ISO-8601 string, e.g., "2025-11-05" (String) */
    public static final String F_DATE = "date";
    /** Maximum participants / capacity (Number) */
    public static final String F_CAPACITY = "maxParticipants";
    /** Registration opens (String date, e.g., "2025-11-03") */
    public static final String F_REG_OPENS = "regOpens";
    /** Registration closes (String date, e.g., "2025-11-10") */
    public static final String F_REG_CLOSES = "regCloses";
    /** Number to draw in lottery (Number) — will be created if missing */
    public static final String F_DRAW_COUNT = "drawCount";
    /** Waiting list (Array<String>) — not present yet, reserved for future stories */
    public static final String F_WAITING_LIST = "waitingList";

    // ---------------------------------------------------------------------
    // Intent / Bundle keys
    // ---------------------------------------------------------------------
    /** Pass the target event id between screens */
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
}
