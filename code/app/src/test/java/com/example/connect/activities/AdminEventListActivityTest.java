package com.example.connect.activities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.text.Editable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connect.adapters.AdminEventAdapter;
import com.example.connect.models.Event;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Focused unit tests for {@link AdminEventListActivity}.
 * These tests cover local filtering logic, Firestore success/failure callback handling,
 * and guard clauses that can run on the JVM without Android framework dependencies.
 */
public class AdminEventListActivityTest {

    private AdminEventListActivity activity;
    private AdminEventAdapter adapterMock;
    private TextView emptyStateMock;
    private TextInputEditText searchInputMock;
    private ProgressBar progressBarMock;
    private FirebaseFirestore firestoreMock;
    private CollectionReference eventsCollectionMock;
    private CollectionReference waitingListCollectionMock;
    private DocumentReference waitingListDocumentMock;

    @Before
    public void setUp() throws Exception {
        activity = mock(AdminEventListActivity.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        adapterMock = mock(AdminEventAdapter.class);
        emptyStateMock = mock(TextView.class);
        searchInputMock = mock(TextInputEditText.class);
        progressBarMock = mock(ProgressBar.class);
        firestoreMock = mock(FirebaseFirestore.class);
        eventsCollectionMock = mock(CollectionReference.class);
        waitingListCollectionMock = mock(CollectionReference.class);
        waitingListDocumentMock = mock(DocumentReference.class);

        setField("adapter", adapterMock);
        setField("tvEmptyState", emptyStateMock);
        setField("searchInput", searchInputMock);
        setField("progressBar", progressBarMock);
        setField("db", firestoreMock);

        Editable defaultEditable = mock(Editable.class);
        when(defaultEditable.toString()).thenReturn("");
        when(searchInputMock.getText()).thenReturn(defaultEditable);

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
        invokeFilter("TECH expo");

        List<Event> filtered = captureFilteredEvents();
        assertEquals(1, filtered.size());
        assertEquals("Tech Expo", filtered.get(0).getName());
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void filterEvents_withOrganizerMatch_checksOrganizerId() throws Exception {
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
    public void applyCurrentFilter_readsSearchInputValue() throws Exception {
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn("Showcase");
        when(searchInputMock.getText()).thenReturn(editable);

        invokeApplyCurrentFilter();

        List<Event> filtered = captureFilteredEvents();
        assertEquals(1, filtered.size());
        assertEquals("Art Showcase", filtered.get(0).getName());
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void loadEvents_onSuccessPopulatesAdapter() throws Exception {
        when(firestoreMock.collection("events")).thenReturn(eventsCollectionMock);

        QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
        Event fetched = createEvent("Space Fair", "astro");
        when(doc.toObject(Event.class)).thenReturn(fetched);
        when(doc.getId()).thenReturn("event-1");
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        when(snapshot.iterator()).thenReturn(Arrays.asList(doc).iterator());

        Task<QuerySnapshot> task = mockTask(invocation -> {
            OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(snapshot);
            return getMock(invocation);
        });
        when(eventsCollectionMock.get()).thenReturn(task);

        invokeLoadEvents();

        verify(progressBarMock).setVisibility(View.VISIBLE);
        verify(progressBarMock, atLeastOnce()).setVisibility(View.GONE);
        List<Event> filtered = captureFilteredEvents();
        assertEquals(1, filtered.size());
        assertEquals("event-1", filtered.get(0).getEventId());
    }

    @Test
    public void loadEvents_onFailureShowsToastAndEmptyState() throws Exception {
        when(firestoreMock.collection("events")).thenReturn(eventsCollectionMock);

        Task<QuerySnapshot> task = mockTask(AdminEventListActivityTest::getMock, invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new RuntimeException("boom"));
            return getMock(invocation);
        });
        when(eventsCollectionMock.get()).thenReturn(task);

        Toast toastInstance = mock(Toast.class);
        try (MockedStatic<Toast> toastStatic = mockStatic(Toast.class);
             MockedStatic<android.util.Log> logStatic = mockStatic(android.util.Log.class)) {
            toastStatic.when(() ->
                    Toast.makeText(
                            ArgumentMatchers.eq(activity),
                            ArgumentMatchers.anyString(),
                            ArgumentMatchers.anyInt()))
                    .thenReturn(toastInstance);

            invokeLoadEvents();

            toastStatic.verify(() ->
                    Toast.makeText(
                            ArgumentMatchers.eq(activity),
                            ArgumentMatchers.contains("Error loading events"),
                            ArgumentMatchers.eq(Toast.LENGTH_SHORT)));
            verify(toastInstance).show();
        }

        verify(progressBarMock).setVisibility(View.VISIBLE);
        verify(progressBarMock, atLeastOnce()).setVisibility(View.GONE);
    }

    @Test
    public void deleteEvent_withNullId_showsToastAndSkipsFirestore() throws Exception {
        Event event = new Event();
        event.setEventId(null);

        Toast toastInstance = mock(Toast.class);
        try (MockedStatic<Toast> toastStatic = mockStatic(Toast.class)) {
            toastStatic.when(() ->
                    Toast.makeText(activity, "Error: Event ID is missing.", Toast.LENGTH_SHORT))
                    .thenReturn(toastInstance);

            invokeDeleteEvent(event);

            toastStatic.verify(() ->
                    Toast.makeText(activity, "Error: Event ID is missing.", Toast.LENGTH_SHORT));
            verify(toastInstance).show();
        }

        verifyNoInteractions(firestoreMock);
    }

    @Test
    public void deleteWaitlistForEvent_targetsWaitingListsCollection() throws Exception {
        when(firestoreMock.collection("waiting_lists")).thenReturn(waitingListCollectionMock);
        when(waitingListCollectionMock.document("event-42")).thenReturn(waitingListDocumentMock);
        Task<Void> deleteTask = mock(Task.class);
        when(waitingListDocumentMock.delete()).thenReturn(deleteTask);

        Task<Void> returnedTask = invokeDeleteWaitlistForEvent("event-42");

        assertEquals(deleteTask, returnedTask);
        verify(waitingListCollectionMock).document("event-42");
        verify(waitingListDocumentMock).delete();
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
        List<Event> list = (List<Event>) field.get(activity);
        if (list == null) {
            list = new ArrayList<>();
            field.set(activity, list);
        }
        return list;
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

    private void invokeLoadEvents() throws Exception {
        Method method = AdminEventListActivity.class.getDeclaredMethod("loadEvents");
        method.setAccessible(true);
        method.invoke(activity);
    }

    private void invokeDeleteEvent(Event event) throws Exception {
        Method method = AdminEventListActivity.class.getDeclaredMethod("deleteEvent", Event.class);
        method.setAccessible(true);
        method.invoke(activity, event);
    }

    @SuppressWarnings("unchecked")
    private Task<Void> invokeDeleteWaitlistForEvent(String eventId) throws Exception {
        Method method = AdminEventListActivity.class.getDeclaredMethod("deleteWaitlistForEvent", String.class);
        method.setAccessible(true);
        return (Task<Void>) method.invoke(activity, eventId);
    }

    private Task<QuerySnapshot> mockTask(Answer<Task<QuerySnapshot>> successAnswer) {
        return mockTask(successAnswer, AdminEventListActivityTest::getMock);
    }

    private Task<QuerySnapshot> mockTask(Answer<Task<QuerySnapshot>> successAnswer,
                                         Answer<Task<QuerySnapshot>> failureAnswer) {
        @SuppressWarnings("unchecked")
        Task<QuerySnapshot> task = mock(Task.class);
        when(task.addOnSuccessListener(ArgumentMatchers.<OnSuccessListener<QuerySnapshot>>any()))
                .thenAnswer(successAnswer);
        when(task.addOnFailureListener(ArgumentMatchers.<OnFailureListener>any()))
                .thenAnswer(failureAnswer);
        return task;
    }

    @SuppressWarnings("unchecked")
    private static <T> Task<T> getMock(InvocationOnMock invocation) {
        return (Task<T>) invocation.getMock();
    }
}

