package com.example.connect.activities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.text.Editable;
import android.view.View;
import android.widget.TextView;

import com.example.connect.adapters.AdminEventAdapter;
import com.example.connect.models.Event;
import com.google.android.material.textfield.TextInputEditText;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests that exercise the filtering logic inside {@link AdminEventListActivity}.
 */
public class AdminEventListActivityTest {

    private AdminEventListActivity activity;
    private AdminEventAdapter adapterMock;
    private TextView emptyStateMock;
    private TextInputEditText searchInputMock;

    @Before
    public void setUp() throws Exception {
        activity = mock(AdminEventListActivity.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        adapterMock = mock(AdminEventAdapter.class);
        emptyStateMock = mock(TextView.class);
        searchInputMock = mock(TextInputEditText.class);

        setField("adapter", adapterMock);
        setField("tvEmptyState", emptyStateMock);
        setField("searchInput", searchInputMock);

        List<Event> events = accessAllEvents();
        events.clear();
        events.add(createEvent("Tech Expo", "alpha_org"));
        events.add(createEvent("Music Fest", "music_club"));
        events.add(createEvent("Art Showcase", "gallery_group"));
    }

    @Test
    public void filterEvents_withEmptyQuery_returnsAllEvents() throws Exception {
        invokeFilter("");

        List<Event> filtered = captureFilteredEvents();
        assertEquals(3, filtered.size());
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void filterEvents_withNameMatch_isCaseInsensitive() throws Exception {
        invokeFilter("tech EXPO");

        List<Event> filtered = captureFilteredEvents();
        assertEquals(1, filtered.size());
        assertEquals("Tech Expo", filtered.get(0).getName());
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void filterEvents_withOrganizerMatch_usesOrganizerField() throws Exception {
        invokeFilter("CLUB");

        List<Event> filtered = captureFilteredEvents();
        assertEquals(1, filtered.size());
        assertEquals("Music Fest", filtered.get(0).getName());
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void filterEvents_withNoMatches_showsEmptyState() throws Exception {
        invokeFilter("missing");

        List<Event> filtered = captureFilteredEvents();
        assertEquals(0, filtered.size());
        verify(emptyStateMock).setVisibility(View.VISIBLE);
    }

    @Test
    public void applyCurrentFilter_usesSearchInputText() throws Exception {
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn("showcase");
        when(searchInputMock.getText()).thenReturn(editable);

        invokeApplyCurrentFilter();

        List<Event> filtered = captureFilteredEvents();
        assertEquals(1, filtered.size());
        assertEquals("Art Showcase", filtered.get(0).getName());
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    private Event createEvent(String name, String organizerId) {
        Event event = new Event();
        event.setName(name);
        event.setOrganizerId(organizerId);
        return event;
    }

    @SuppressWarnings("unchecked")
    private List<Event> accessAllEvents() throws Exception {
        Field field = AdminEventListActivity.class.getDeclaredField("allEvents");
        field.setAccessible(true);
        List<Event> events = (List<Event>) field.get(activity);
        if (events == null) {
            events = new ArrayList<>();
            field.set(activity, events);
        }
        return events;
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = AdminEventListActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(activity, value);
    }

    @SuppressWarnings("unchecked")
    private List<Event> captureFilteredEvents() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(adapterMock).setEvents(captor.capture());
        return captor.getValue();
    }

    private void invokeFilter(String query) throws Exception {
        Method method = AdminEventListActivity.class.getDeclaredMethod("filterEvents", String.class);
        method.setAccessible(true);
        method.invoke(activity, query);
    }

    private void invokeApplyCurrentFilter() throws Exception {
        Method method = AdminEventListActivity.class.getDeclaredMethod("applyCurrentFilter");
        method.setAccessible(true);
        method.invoke(activity);
    }
}

