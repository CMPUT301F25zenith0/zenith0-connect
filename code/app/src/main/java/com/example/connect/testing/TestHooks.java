package com.example.connect.testing;

/**
 * Simple test-only switches that allow instrumentation tests to
 * disable network-heavy code paths.
 */
public final class TestHooks {

    private static volatile boolean uiTestMode = false;

    private TestHooks() {
        // Utility class
    }

    /**
     * Enables or disables UI test mode. When enabled, activities can skip
     * realtime Firebase calls to keep Espresso tests deterministic.
     */
    public static void setUiTestMode(boolean enabled) {
        uiTestMode = enabled;
    }

    /**
     * @return true when UI tests have requested network-heavy calls to be skipped.
     */
    public static boolean isUiTestMode() {
        return uiTestMode;
    }
}

