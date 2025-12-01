package com.example.connect.activities;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import android.util.Log;

import com.example.connect.adapters.EventAdapter;
import com.example.connect.adapters.PopularEventsAdapter;
import com.example.connect.models.Event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * JVM unit tests focused on the filtering logic inside {@link EventListActivity}.
 * These tests verify that search text and each filter chip (date, interest, location)
 * constrain the main list and the popular carousel consistently without requiring
 * Android instrumentation.
 */
public class EventListActivityTest {

    private EventListActivity activity;
    private EventAdapter eventAdapterMock;
    private PopularEventsAdapter popularEventsAdapterMock;

    private List<Event> eventList;
    private List<Event> allEventsList;
    private List<Event> popularEventsList;

    @Before
    public void setUp() throws Exception {
        activity = mock(EventListActivity.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        eventAdapterMock = mock(EventAdapter.class);
        popularEventsAdapterMock = mock(PopularEventsAdapter.class);

        setField("eventAdapter", eventAdapterMock);
        setField("popularEventsAdapter", popularEventsAdapterMock);

        eventList = getListField("eventList");
        allEventsList = getListField("allEventsList");
        popularEventsList = getListField("popularEventsList");

        resetFilters();
        clearLists();
    }

    @Test
    public void applyAllFilters_searchQueryFiltersMainAndPopularLists() throws Exception {
        withMockedLog(() -> {
            Event tech = createEvent("tech", "Tech Mixer", daysFromNow(1), "Campus A",
                    "Technology", Arrays.asList("tech", "hardware"));
            Event music = createEvent("music", "Music Gala", daysFromNow(2), "Campus B",
                    "Entertainment", Arrays.asList("music", "festival"));

            populateEvents(tech, music);
            setField("currentSearchQuery", "tech");

            invokeApplyAllFilters();

            assertEquals(Collections.singletonList("tech"), extractIds(eventList));
            assertEquals(Collections.singletonList("tech"), extractIds(popularEventsList));
            verify(eventAdapterMock).notifyDataSetChanged();
            verify(popularEventsAdapterMock).notifyDataSetChanged();
        });
    }

    @Test
    public void applyAllFilters_dateFilterKeepsExactMatches() throws Exception {
        withMockedLog(() -> {
            String targetDate = daysFromNow(1);
            Event early = createEvent("early", "AI Summit", targetDate, "Lab 1",
                    "Tech", Arrays.asList("ai"));
            Event later = createEvent("later", "Hackathon", daysFromNow(3), "Auditorium",
                    "Tech", Arrays.asList("coding"));

            populateEvents(early, later);
            setField("selectedDate", targetDate);

            invokeApplyAllFilters();

            assertEquals(Collections.singletonList("early"), extractIds(eventList));
            assertEquals(Collections.singletonList("early"), extractIds(popularEventsList));
        });
    }

    @Test
    public void applyAllFilters_interestFilterMatchesLabelsCaseInsensitive() throws Exception {
        withMockedLog(() -> {
            Event tech = createEvent("tech", "Robotics Expo", daysFromNow(1), "Campus Center",
                    "Technology", Arrays.asList("Robotics", "AI"));
            Event music = createEvent("music", "Indie Night", daysFromNow(1), "Studio 5",
                    "Entertainment", Arrays.asList("indie music", "live"));

            populateEvents(tech, music);
            setField("selectedInterest", "MUSIC");

            invokeApplyAllFilters();

            assertEquals(Collections.singletonList("music"), extractIds(eventList));
        });
    }

    @Test
    public void applyAllFilters_locationFilterIsCaseInsensitiveAndPartial() throws Exception {
        withMockedLog(() -> {
            Event campus = createEvent("campus", "Career Fair", daysFromNow(1), "Campus West Hall",
                    "Career", Arrays.asList("jobs"));
            Event downtown = createEvent("downtown", "Design Conference", daysFromNow(1),
                    "Downtown Innovation Hub", "Design", Arrays.asList("design", "ux"));

            populateEvents(campus, downtown);
            setField("selectedLocation", "DOWNTOWN");

            invokeApplyAllFilters();

            assertEquals(Collections.singletonList("downtown"), extractIds(eventList));
        });
    }

    private void populateEvents(Event... events) {
        clearLists();
        Collections.addAll(allEventsList, events);
    }

    private void clearLists() {
        eventList.clear();
        allEventsList.clear();
        popularEventsList.clear();
    }

    private void resetFilters() throws Exception {
        setField("currentSearchQuery", "");
        setField("selectedDate", "");
        setField("selectedInterest", "");
        setField("selectedLocation", "");
    }

    private void withMockedLog(ThrowingRunnable runnable) throws Exception {
        try (MockedStatic<Log> logMock = mockStatic(Log.class)) {
            logMock.when(() -> Log.d(anyString(), anyString())).thenReturn(0);
            runnable.run();
        }
    }

    private void invokeApplyAllFilters() throws Exception {
        Method method = EventListActivity.class.getDeclaredMethod("applyAllFilters");
        method.setAccessible(true);
        method.invoke(activity);
    }

    private Event createEvent(String id, String name, String date, String location,
                              String category, List<String> labels) {
        Event event = new Event();
        event.setEventId(id);
        event.setName(name);
        event.setDateTime(date);
        event.setLocation(location);
        event.setCategory(category);
        event.setDescription(name + " description");
        event.setLabels(labels != null ? new ArrayList<>(labels) : new ArrayList<>());
        return event;
    }

    private List<String> extractIds(List<Event> events) {
        List<String> ids = new ArrayList<>();
        for (Event event : events) {
            ids.add(event.getEventId());
        }
        return ids;
    }

    private String daysFromNow(int daysAhead) {
        return LocalDate.now().plusDays(daysAhead).toString(); // yyyy-MM-dd
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = EventListActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(activity, value);
    }

    @SuppressWarnings("unchecked")
    private List<Event> getListField(String fieldName) throws Exception {
        Field field = EventListActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        List<Event> list = (List<Event>) field.get(activity);
        if (list == null) {
            list = new ArrayList<>();
            field.set(activity, list);
        }
        return list;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}

