package com.example.connect.models;

import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class EventTest {
    private Event mk(String o, String c) {
        Event e = new Event();
        e.setRegOpens(o); e.setRegCloses(c);
        return e;
    }

    @Test public void withinRange_isJoinable() {
        var t = LocalDate.now();
        assertTrue(mk(t.minusDays(1).toString(), t.plusDays(1).toString()).isJoinableToday());
    }
    @Test public void beforeOpen_notJoinable() {
        var t = LocalDate.now();
        assertFalse(mk(t.plusDays(1).toString(), t.plusDays(2).toString()).isJoinableToday());
    }
    @Test public void afterClose_notJoinable() {
        var t = LocalDate.now();
        assertFalse(mk(t.minusDays(3).toString(), t.minusDays(1).toString()).isJoinableToday());
    }
    @Test public void boundsInclusive() {
        var t = LocalDate.now();
        assertTrue(mk(t.toString(), t.toString()).isJoinableToday());
    }
}
