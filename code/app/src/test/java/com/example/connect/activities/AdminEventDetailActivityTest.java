package com.example.connect.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

/**
 * Unit tests for {@link AdminEventDetailActivity} UI binding and helper logic.
 */
public class AdminEventDetailActivityTest {

    private AdminEventDetailActivity activity;

    private TextView tvTitle;
    private TextView tvOrganizer;
    private TextView tvDate;
    private TextView tvLocation;
    private TextView tvPrice;
    private TextView tvDescription;
    private TextView tvRegWindow;
    private TextView tvWaitingList;
    private ProgressBar progressBar;
    private View contentGroup;

    @Before
    public void setUp() throws Exception {
        activity = mock(AdminEventDetailActivity.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

        tvTitle = mock(TextView.class);
        tvOrganizer = mock(TextView.class);
        tvDate = mock(TextView.class);
        tvLocation = mock(TextView.class);
        tvPrice = mock(TextView.class);
        tvDescription = mock(TextView.class);
        tvRegWindow = mock(TextView.class);
        tvWaitingList = mock(TextView.class);
        progressBar = mock(ProgressBar.class);
        contentGroup = mock(View.class);

        setField("tvTitle", tvTitle);
        setField("tvOrganizer", tvOrganizer);
        setField("tvDate", tvDate);
        setField("tvLocation", tvLocation);
        setField("tvPrice", tvPrice);
        setField("tvDescription", tvDescription);
        setField("tvRegWindow", tvRegWindow);
        setField("tvWaitingList", tvWaitingList);
        setField("progressBar", progressBar);
        setField("contentGroup", contentGroup);
    }

    @Test
    public void bindEvent_withCompleteData_setsAllViews() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.exists()).thenReturn(true);
        when(doc.getString("event_title")).thenReturn("Tech Summit");
        when(doc.getString("org_name")).thenReturn("Innovation Hub");
        when(doc.getString("date_time")).thenReturn("Jan 25, 2026");
        when(doc.getString("location")).thenReturn("Campus Hall");
        when(doc.get("price")).thenReturn(49);
        when(doc.getString("description")).thenReturn("A day of talks.");
        when(doc.getString("reg_start")).thenReturn("Jan 1");
        when(doc.getString("reg_stop")).thenReturn("Jan 15");

        invokeBindEvent(doc);

        verify(tvTitle).setText("Tech Summit");
        verify(tvOrganizer).setText("Innovation Hub");
        verify(tvDate).setText("Jan 25, 2026");
        verify(tvLocation).setText("Campus Hall");
        verify(tvPrice).setText("$49");
        verify(tvDescription).setText("A day of talks.");
        verify(tvRegWindow).setText("Registration: Jan 1 - Jan 15");
        verify(tvWaitingList).setText("Live Waitlist: --");
        verify(progressBar).setVisibility(View.GONE);
        verify(contentGroup).setVisibility(View.VISIBLE);
    }

    @Test
    public void bindEvent_withMissingData_appliesFallbacks() throws Exception {
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        when(doc.exists()).thenReturn(true);
        when(doc.getString("event_title")).thenReturn("");
        when(doc.getString("org_name")).thenReturn(null);
        when(doc.getString("date_time")).thenReturn("");
        when(doc.getString("location")).thenReturn(null);
        when(doc.get("price")).thenReturn(null);
        when(doc.getString("description")).thenReturn("");
        when(doc.getString("reg_start")).thenReturn(null);
        when(doc.getString("reg_stop")).thenReturn(null);

        invokeBindEvent(doc);

        verify(tvTitle).setText("Unnamed Event");
        verify(tvOrganizer).setText("Unknown Organizer");
        verify(tvDate).setText("Date TBD");
        verify(tvLocation).setText("Location TBD");
        verify(tvPrice).setText("Free");
        verify(tvDescription).setText("No description available.");
        verify(tvRegWindow).setText("Registration window not configured");
        verify(tvWaitingList).setText("Live Waitlist: --");
    }

    @Test
    public void valueOrFallback_handlesEmptyAndRealValues() throws Exception {
        assertEquals("Fallback", invokeValueOrFallback(null, "Fallback"));
        assertEquals("Fallback", invokeValueOrFallback("", "Fallback"));
        assertEquals("Actual", invokeValueOrFallback("Actual", "Fallback"));
    }

    @Test
    public void formatLiveWaitlist_pluralizesEntrantText() throws Exception {
        assertEquals("Live Waitlist: 0 entrants", invokeFormatLiveWaitlist(0));
        assertEquals("Live Waitlist: 1 entrant", invokeFormatLiveWaitlist(1));
        assertEquals("Live Waitlist: 2 entrants", invokeFormatLiveWaitlist(2));
    }

    @Test
    public void listenForWaitlist_withNullEvent_doesNothing() throws Exception {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        setField("db", firestore);

        invokeListenForWaitlist(null);

        verifyNoInteractions(firestore);
    }

    @Test
    public void listenForWaitlist_replacesExistingListener() throws Exception {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        ListenerRegistration initialRegistration = mock(ListenerRegistration.class);
        ListenerRegistration newRegistration = mock(ListenerRegistration.class);

        when(firestore.collection("waiting_lists")).thenReturn(collection);
        when(collection.document("event-123")).thenReturn(document);
        when(document.addSnapshotListener(any())).thenReturn(newRegistration);

        setField("db", firestore);
        setField("waitlistListener", initialRegistration);

        invokeListenForWaitlist("event-123");

        verify(initialRegistration).remove();
        verify(document).addSnapshotListener(any());
        assertSame(newRegistration, getField("waitlistListener"));
    }

    @Test
    public void listenForWaitlist_updatesWaitingListTextFromSnapshots() throws Exception {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        ListenerRegistration registration = mock(ListenerRegistration.class);

        when(firestore.collection("waiting_lists")).thenReturn(collection);
        when(collection.document("event-live")).thenReturn(document);

        final EventListener<DocumentSnapshot>[] capturedListener = new EventListener[1];
        when(document.addSnapshotListener(any())).thenAnswer(invocation -> {
            capturedListener[0] = invocation.getArgument(0);
            return registration;
        });

        setField("db", firestore);

        invokeListenForWaitlist("event-live");

        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.get("entries")).thenReturn(Arrays.asList("uid-1", "uid-2"));

        capturedListener[0].onEvent(snapshot, null);
        verify(tvWaitingList).setText("Live Waitlist: 2 entrants");
        clearInvocations(tvWaitingList);

        capturedListener[0].onEvent(null, null);
        verify(tvWaitingList, times(1)).setText("Live Waitlist: 0 entrants");

        DocumentSnapshot emptySnapshot = mock(DocumentSnapshot.class);
        when(emptySnapshot.exists()).thenReturn(true);
        when(emptySnapshot.get("entries")).thenReturn(Collections.emptyList());
        capturedListener[0].onEvent(emptySnapshot, null);
        verify(tvWaitingList, times(2)).setText("Live Waitlist: 0 entrants");
    }

    private void invokeBindEvent(DocumentSnapshot doc) throws Exception {
        Method method = AdminEventDetailActivity.class.getDeclaredMethod("bindEvent", DocumentSnapshot.class);
        method.setAccessible(true);
        method.invoke(activity, doc);
    }

    private String invokeValueOrFallback(String value, String fallback) throws Exception {
        Method method = AdminEventDetailActivity.class.getDeclaredMethod("valueOrFallback", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(activity, value, fallback);
    }

    private String invokeFormatLiveWaitlist(int count) throws Exception {
        Method method = AdminEventDetailActivity.class.getDeclaredMethod("formatLiveWaitlist", int.class);
        method.setAccessible(true);
        return (String) method.invoke(activity, count);
    }

    private void invokeListenForWaitlist(String eventId) throws Exception {
        Method method = AdminEventDetailActivity.class.getDeclaredMethod("listenForWaitlist", String.class);
        method.setAccessible(true);
        method.invoke(activity, eventId);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = AdminEventDetailActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(activity, value);
    }

    private Object getField(String fieldName) throws Exception {
        Field field = AdminEventDetailActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(activity);
    }
}

