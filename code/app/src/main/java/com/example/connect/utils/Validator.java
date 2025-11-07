package com.example.connect.utils;

/**
 * Centralized input validation helpers so UI stays clean and testable.
 * Add more methods here as features grow.
 */
public final class Validator {
    private Validator() {}

    /** Result of validating a numeric field. */
    public static class Result {
        /** true if input is valid */
        public final boolean ok;
        /** helpful message for the user if not ok (empty otherwise) */
        public final String message;
        /** sanitized value or suggested value (e.g., clamped max) */
        public final int value;

        public Result(boolean ok, String message, int value) {
            this.ok = ok;
            this.message = message;
            this.value = value;
        }
    }

    /**
     * Validate the organizer's "number to draw" input.
     *
     * @param raw      text from EditText
     * @param capacity optional upper bound (e.g., maxParticipants); null = no bound
     */
    public static Result validateDrawCount(String raw, Integer capacity) {
        if (raw == null || raw.trim().isEmpty()) {
            return new Result(false, "Enter a number", 0);
        }

        final int val;
        try {
            val = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return new Result(false, "Must be an integer", 0);
        }

        if (val < 0) {
            return new Result(false, "Must be ≥ 0", 0);
        }

        if (capacity != null && capacity >= 0 && val > capacity) {
            // Suggest the maximum allowed
            return new Result(false, "Too large (max " + capacity + ")", capacity);
        }

        return new Result(true, "", val);
    }
}
