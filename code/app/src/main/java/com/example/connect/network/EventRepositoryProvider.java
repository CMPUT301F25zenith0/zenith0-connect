package com.example.connect.network;

/**
 * Simple locator that allows EventRepository to be swapped for tests.
 * <p>
 * Production code receives the default Firestore-backed repository, while
 * instrumentation/unit tests can inject a fake implementation before launching
 * the activity under test.
 */
public final class EventRepositoryProvider {

    private static EventRepository repository;

    private EventRepositoryProvider() {
        // no-op
    }

    /**
     * Returns the shared EventRepository instance, creating the default
     * Firestore-backed implementation on first access.
     */
    public static synchronized EventRepository getRepository() {
        if (repository == null) {
            repository = new EventRepository();
        }
        return repository;
    }

    /**
     * Replaces the repository instance. Intended for instrumentation/unit tests.
     */
    public static synchronized void setRepositoryForTesting(EventRepository testRepository) {
        repository = testRepository;
    }

    /**
     * Clears the cached repository so future calls receive a fresh instance.
     */
    public static synchronized void reset() {
        repository = null;
    }
}


