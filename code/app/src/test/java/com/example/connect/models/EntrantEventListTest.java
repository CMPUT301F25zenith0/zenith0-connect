// app/src/test/java/com/example/connect/models/EntrantEventListTest.java
package com.example.connect.models;

import org.junit.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.Assert.*;

public class EntrantEventListTest {

    /** Simple filter identical to what the list should show. */
    private static List<Event> filterJoinable(List<Event> all) {
        List<Event> out = new ArrayList<>();
        for (Event e : all) {
            if (e != null && e.isJoinableToday()) out.add(e);
        }
        return out;
    }

    private static Event mk(String name, LocalDate opens, LocalDate closes) {
        Event e = new Event();
        e.setName(name);
        e.setDate(opens.toString()); // display date not used for filter
        e.setRegOpens(opens.toString());
        e.setRegCloses(closes.toString());
        e.setMaxParticipants(10);
        return e;
    }

    @Test
    public void showsOnlyJoinable_inclusiveBounds() {
        LocalDate t = LocalDate.now();

        Event open      = mk("Open",        t.minusDays(1), t.plusDays(1)); // joinable
        Event closesNow = mk("ClosesToday", t.minusDays(2), t);             // joinable (inclusive)
        Event closed    = mk("Closed",      t.minusDays(5), t.minusDays(1)); // not joinable
        Event future    = mk("Future",      t.plusDays(1),  t.plusDays(3));  // not joinable yet

        List<Event> result = filterJoinable(Arrays.asList(open, closesNow, closed, future));
        // Expect exactly the two joinable ones
        assertEquals(2, result.size());
        List<String> names = Arrays.asList(result.get(0).getName(), result.get(1).getName());
        assertTrue(names.contains("Open"));
        assertTrue(names.contains("ClosesToday"));
    }

    @Test
    public void malformedDates_failOpenButStillDetectsValidOnes() {
        LocalDate t = LocalDate.now();

        Event bad = new Event();
        bad.setName("BadDates");
        bad.setRegOpens("not-a-date");
        bad.setRegCloses("also-bad");

        Event valid = mk("Valid", t.minusDays(1), t.plusDays(2));

        List<Event> result = filterJoinable(Arrays.asList(bad, valid));
        // Our Event.isJoinableToday() fails open on malformed dates to avoid hiding events.
        // So both appear.
        assertEquals(2, result.size());
    }
}
