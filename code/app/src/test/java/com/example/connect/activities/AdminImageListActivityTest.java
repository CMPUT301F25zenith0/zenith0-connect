package com.example.connect.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.text.Editable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.connect.adapters.AdminImageAdapter;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Unit tests for {@link AdminImageListActivity} focusing on local filtering logic,
 * Firestore success/failure handling, and delete operations.
 */
public class AdminImageListActivityTest {

    private AdminImageListActivity activity;
    private AdminImageAdapter adapterMock;
    private TextView emptyStateMock;
    private TextInputEditText searchInputMock;
    private ProgressBar progressBarMock;
    private FirebaseFirestore firestoreMock;
    private CollectionReference eventsCollectionMock;
    private CollectionReference accountsCollectionMock;
    private DocumentReference documentReferenceMock;

    @Before
    public void setUp() throws Exception {
        activity = mock(AdminImageListActivity.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        adapterMock = mock(AdminImageAdapter.class);
        emptyStateMock = mock(TextView.class);
        searchInputMock = mock(TextInputEditText.class);
        progressBarMock = mock(ProgressBar.class);
        firestoreMock = mock(FirebaseFirestore.class);
        eventsCollectionMock = mock(CollectionReference.class);
        accountsCollectionMock = mock(CollectionReference.class);
        documentReferenceMock = mock(DocumentReference.class);

        setField("adapter", adapterMock);
        setField("tvEmptyState", emptyStateMock);
        setField("searchInput", searchInputMock);
        setField("progressBar", progressBarMock);
        setField("db", firestoreMock);

        Editable defaultEditable = editableWithText("");
        when(searchInputMock.getText()).thenReturn(defaultEditable);

        List<AdminImageAdapter.ImageItem> seed = accessAllImages();
        seed.clear();
        seed.add(new AdminImageAdapter.ImageItem("event-1", "http://poster", "Event Poster", "event-1", "Tech Expo"));
        seed.add(new AdminImageAdapter.ImageItem("user-22", "http://profile", "Profile Picture", "user-22", "Jamie"));
    }

    @Test
    public void filterImages_withEmptyQuery_returnsAllImages() throws Exception {
        invokeFilter("");

        List<AdminImageAdapter.ImageItem> filtered = captureAdapterImages();
        assertEquals(2, filtered.size());
        verify(emptyStateMock, atLeastOnce()).setVisibility(View.GONE);
        verify(emptyStateMock, never()).setVisibility(View.VISIBLE);
    }

    @Test
    public void filterImages_matchesDisplayName_caseInsensitive() throws Exception {
        invokeFilter("tech expo");

        List<AdminImageAdapter.ImageItem> filtered = captureAdapterImages();
        assertEquals(1, filtered.size());
        assertEquals("Tech Expo", filtered.get(0).displayName);
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void filterImages_matchesRelatedId() throws Exception {
        invokeFilter("USER-22");

        List<AdminImageAdapter.ImageItem> filtered = captureAdapterImages();
        assertEquals(1, filtered.size());
        assertEquals("user-22", filtered.get(0).relatedId);
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void filterImages_withNoMatches_showsEmptyState() throws Exception {
        invokeFilter("missing");

        List<AdminImageAdapter.ImageItem> filtered = captureAdapterImages();
        assertEquals(0, filtered.size());
        verify(emptyStateMock).setVisibility(View.VISIBLE);
    }

    @Test
    public void applyCurrentFilter_readsSearchInputValue() throws Exception {
        Editable editable = editableWithText("jamie");
        when(searchInputMock.getText()).thenReturn(editable);

        invokeApplyCurrentFilter();

        List<AdminImageAdapter.ImageItem> filtered = captureAdapterImages();
        assertEquals(1, filtered.size());
        assertEquals("Jamie", filtered.get(0).displayName);
    }

    @Test
    public void normalize_trimsAndLowercasesWhitespace() throws Exception {
        String normalized = invokeNormalize("  HeLLo   WORLD  ");
        assertEquals("hello world", normalized);
    }

    @Test
    public void buildSearchSource_combinesDisplayNameAndId() throws Exception {
        AdminImageAdapter.ImageItem item =
                new AdminImageAdapter.ImageItem("id", "url", "Event Poster", "EVT-9", "Campus Map");

        String source = invokeBuildSearchSource(item);

        assertEquals("campus map evt-9", source);
    }

    @Test
    public void updateImages_replacesBackingListAndAppliesFilter() throws Exception {
        List<AdminImageAdapter.ImageItem> fresh = new ArrayList<>();
        fresh.add(new AdminImageAdapter.ImageItem("new", "http://new", "Profile Picture", "user-99", "Alex"));

        invokeUpdateImages(fresh);

        List<AdminImageAdapter.ImageItem> filtered = captureAdapterImages();
        assertEquals(1, filtered.size());

        List<AdminImageAdapter.ImageItem> backing = accessAllImages();
        assertEquals(1, backing.size());
        assertEquals("new", backing.get(0).id);
    }

    @Test
    public void loadImages_onSuccessCombinesEventAndProfileData() throws Exception {
        when(firestoreMock.collection("events")).thenReturn(eventsCollectionMock);
        when(firestoreMock.collection("accounts")).thenReturn(accountsCollectionMock);

        QueryDocumentSnapshot eventDocUrl = mock(QueryDocumentSnapshot.class);
        when(eventDocUrl.getId()).thenReturn("event-100");
        when(eventDocUrl.getString("imageUrl")).thenReturn("https://poster.jpg");
        when(eventDocUrl.getString("image_base64")).thenReturn(null);
        when(eventDocUrl.getString("event_title")).thenReturn("Poster One");

        QueryDocumentSnapshot eventDocBase64 = mock(QueryDocumentSnapshot.class);
        when(eventDocBase64.getId()).thenReturn("event-101");
        when(eventDocBase64.getString("imageUrl")).thenReturn(null);
        when(eventDocBase64.getString("image_base64")).thenReturn("BASE64");
        when(eventDocBase64.getString("event_title")).thenReturn(null);
        when(eventDocBase64.getString("name")).thenReturn("Fallback Event");

        QuerySnapshot eventSnapshot = mock(QuerySnapshot.class);
        when(eventSnapshot.iterator()).thenReturn(Arrays.asList(eventDocUrl, eventDocBase64).iterator());

        Task<QuerySnapshot> eventTask = taskThatSucceeds(eventSnapshot);
        when(eventsCollectionMock.get()).thenReturn(eventTask);

        QueryDocumentSnapshot accountDoc = mock(QueryDocumentSnapshot.class);
        when(accountDoc.getId()).thenReturn("user-200");
        when(accountDoc.getString("profile_image_url")).thenReturn("https://profile.png");
        when(accountDoc.getString("display_name")).thenReturn("Casey");
        when(accountDoc.getString("full_name")).thenReturn(null);

        QuerySnapshot accountSnapshot = mock(QuerySnapshot.class);
        when(accountSnapshot.iterator()).thenReturn(Collections.singletonList(accountDoc).iterator());

        Task<QuerySnapshot> accountsTask = taskThatSucceeds(accountSnapshot);
        when(accountsCollectionMock.get()).thenReturn(accountsTask);

        invokeLoadImages();

        verify(progressBarMock).setVisibility(View.VISIBLE);
        verify(progressBarMock, atLeastOnce()).setVisibility(View.GONE);

        ArgumentCaptor<Integer> visCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(emptyStateMock, atLeastOnce()).setVisibility(visCaptor.capture());
        assertEquals(View.GONE, visCaptor.getValue().intValue());

        List<AdminImageAdapter.ImageItem> filtered = captureAdapterImages();
        assertEquals(3, filtered.size());
        assertEquals("Poster One", filtered.get(0).displayName);
        assertEquals("Fallback Event", filtered.get(1).displayName);
        assertEquals("Casey", filtered.get(2).displayName);
    }

    @Test
    public void loadImages_whenEventQueryFails_showsToast() throws Exception {
        when(firestoreMock.collection("events")).thenReturn(eventsCollectionMock);
        Task<QuerySnapshot> failingTask = taskThatFails(new RuntimeException("boom"));
        when(eventsCollectionMock.get()).thenReturn(failingTask);

        Toast toastInstance = mock(Toast.class);
        try (MockedStatic<Toast> toastStatic = mockStatic(Toast.class);
             MockedStatic<android.util.Log> logStatic = mockStatic(android.util.Log.class)) {
            toastStatic.when(() ->
                    Toast.makeText(
                            ArgumentMatchers.eq(activity),
                            ArgumentMatchers.contains("Error loading event images"),
                            ArgumentMatchers.eq(Toast.LENGTH_SHORT)))
                    .thenReturn(toastInstance);

            invokeLoadImages();

            toastStatic.verify(() ->
                    Toast.makeText(
                            ArgumentMatchers.eq(activity),
                            ArgumentMatchers.contains("Error loading event images"),
                            ArgumentMatchers.eq(Toast.LENGTH_SHORT)));
            verify(toastInstance).show();
        }

        verify(progressBarMock).setVisibility(View.VISIBLE);
        verify(progressBarMock, atLeastOnce()).setVisibility(View.GONE);
        verify(firestoreMock, never()).collection("accounts");
    }

    @Test
    public void loadImages_whenAccountsQueryFails_showsToast() throws Exception {
        when(firestoreMock.collection("events")).thenReturn(eventsCollectionMock);
        when(firestoreMock.collection("accounts")).thenReturn(accountsCollectionMock);

        QuerySnapshot eventSnapshot = mock(QuerySnapshot.class);
        when(eventSnapshot.iterator()).thenReturn(Collections.<QueryDocumentSnapshot>emptyList().iterator());
        Task<QuerySnapshot> eventsTask = taskThatSucceeds(eventSnapshot);
        when(eventsCollectionMock.get()).thenReturn(eventsTask);

        Task<QuerySnapshot> accountsTask = taskThatFails(new RuntimeException("oops"));
        when(accountsCollectionMock.get()).thenReturn(accountsTask);

        Toast toastInstance = mock(Toast.class);
        try (MockedStatic<Toast> toastStatic = mockStatic(Toast.class);
             MockedStatic<android.util.Log> logStatic = mockStatic(android.util.Log.class)) {
            toastStatic.when(() ->
                    Toast.makeText(
                            ArgumentMatchers.eq(activity),
                            ArgumentMatchers.contains("Error loading user images"),
                            ArgumentMatchers.eq(Toast.LENGTH_SHORT)))
                    .thenReturn(toastInstance);

            invokeLoadImages();

            toastStatic.verify(() ->
                    Toast.makeText(
                            ArgumentMatchers.eq(activity),
                            ArgumentMatchers.contains("Error loading user images"),
                            ArgumentMatchers.eq(Toast.LENGTH_SHORT)));
            verify(toastInstance).show();
        }

        verify(progressBarMock).setVisibility(View.VISIBLE);
        verify(progressBarMock, atLeastOnce()).setVisibility(View.GONE);
    }

    @Test
    public void deleteImage_forEventPoster_updatesImageFields() throws Exception {
        when(firestoreMock.collection("events")).thenReturn(eventsCollectionMock);
        when(eventsCollectionMock.document("event-1")).thenReturn(documentReferenceMock);

        Task<Void> updateTask = silentVoidTask();
        when(documentReferenceMock.update(ArgumentMatchers.<Map<String, Object>>any())).thenReturn(updateTask);

        AdminImageAdapter.ImageItem item =
                new AdminImageAdapter.ImageItem("event-1", "url", "Event Poster", "event-1", "Poster");

        invokeDeleteImage(item);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(documentReferenceMock).update(captor.capture());
        Map<String, Object> updates = captor.getValue();
        assertNull(updates.get("imageUrl"));
        assertNull(updates.get("image_base64"));

        verify(updateTask).addOnSuccessListener(ArgumentMatchers.<OnSuccessListener<Void>>any());
        verify(updateTask).addOnFailureListener(ArgumentMatchers.<OnFailureListener>any());
    }

    @Test
    public void deleteImage_forProfilePicture_clearsProfileField() throws Exception {
        when(firestoreMock.collection("accounts")).thenReturn(accountsCollectionMock);
        when(accountsCollectionMock.document("user-22")).thenReturn(documentReferenceMock);

        Task<Void> updateTask = silentVoidTask();
        when(documentReferenceMock.update(ArgumentMatchers.eq("profile_image_url"), ArgumentMatchers.isNull()))
                .thenReturn(updateTask);

        AdminImageAdapter.ImageItem item =
                new AdminImageAdapter.ImageItem("user-22", "url", "Profile Picture", "user-22", "Jamie");

        invokeDeleteImage(item);

        verify(documentReferenceMock).update("profile_image_url", null);
        verify(updateTask).addOnSuccessListener(ArgumentMatchers.<OnSuccessListener<Void>>any());
        verify(updateTask).addOnFailureListener(ArgumentMatchers.<OnFailureListener>any());
    }

    private Editable editableWithText(String value) {
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn(value);
        return editable;
    }

    private void invokeFilter(String query) throws Exception {
        Method method = AdminImageListActivity.class.getDeclaredMethod("filterImages", String.class);
        method.setAccessible(true);
        method.invoke(activity, query);
    }

    private void invokeApplyCurrentFilter() throws Exception {
        Method method = AdminImageListActivity.class.getDeclaredMethod("applyCurrentFilter");
        method.setAccessible(true);
        method.invoke(activity);
    }

    private void invokeUpdateImages(List<AdminImageAdapter.ImageItem> images) throws Exception {
        Method method = AdminImageListActivity.class.getDeclaredMethod("updateImages", List.class);
        method.setAccessible(true);
        method.invoke(activity, images);
    }

    private void invokeLoadImages() throws Exception {
        Method method = AdminImageListActivity.class.getDeclaredMethod("loadImages");
        method.setAccessible(true);
        method.invoke(activity);
    }

    private void invokeDeleteImage(AdminImageAdapter.ImageItem image) throws Exception {
        Method method = AdminImageListActivity.class.getDeclaredMethod("deleteImage", AdminImageAdapter.ImageItem.class);
        method.setAccessible(true);
        method.invoke(activity, image);
    }

    private String invokeNormalize(String value) throws Exception {
        Method method = AdminImageListActivity.class.getDeclaredMethod("normalize", String.class);
        method.setAccessible(true);
        return (String) method.invoke(activity, value);
    }

    private String invokeBuildSearchSource(AdminImageAdapter.ImageItem image) throws Exception {
        Method method = AdminImageListActivity.class.getDeclaredMethod("buildSearchSource", AdminImageAdapter.ImageItem.class);
        method.setAccessible(true);
        return (String) method.invoke(activity, image);
    }

    @SuppressWarnings("unchecked")
    private List<AdminImageAdapter.ImageItem> captureAdapterImages() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(adapterMock).setImages(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<AdminImageAdapter.ImageItem> accessAllImages() throws Exception {
        Field field = AdminImageListActivity.class.getDeclaredField("allImages");
        field.setAccessible(true);
        List<AdminImageAdapter.ImageItem> list =
                (List<AdminImageAdapter.ImageItem>) field.get(activity);
        if (list == null) {
            list = new ArrayList<>();
            field.set(activity, list);
        }
        return list;
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = AdminImageListActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(activity, value);
    }

    private Task<QuerySnapshot> taskThatSucceeds(QuerySnapshot snapshot) {
        return createTask(listener -> listener.onSuccess(snapshot), null);
    }

    private Task<QuerySnapshot> taskThatFails(Exception exception) {
        return createTask(null, failureListener -> failureListener.onFailure(exception));
    }

    private Task<Void> silentVoidTask() {
        return createTask(null, null);
    }

    @SuppressWarnings("unchecked")
    private <T> Task<T> createTask(Consumer<OnSuccessListener<T>> successInvoker,
                                   Consumer<OnFailureListener> failureInvoker) {
        Task<T> task = mock(Task.class);
        when(task.addOnSuccessListener(ArgumentMatchers.<OnSuccessListener<T>>any()))
                .thenAnswer(invocation -> {
                    OnSuccessListener<T> listener = invocation.getArgument(0);
                    if (successInvoker != null) {
                        successInvoker.accept(listener);
                    }
                    return task;
                });
        when(task.addOnFailureListener(ArgumentMatchers.<OnFailureListener>any()))
                .thenAnswer(invocation -> {
                    OnFailureListener listener = invocation.getArgument(0);
                    if (failureInvoker != null) {
                        failureInvoker.accept(listener);
                    }
                    return task;
                });
        return task;
    }
}

