package com.example.connect.activities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.connect.adapters.AdminProfileAdapter;
import com.example.connect.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link AdminOrganizerListActivity}.
 * Exercises local filtering logic, organizer loading, and guard clauses on destructive actions.
 */
public class AdminOrganizerListActivityTest {

    private AdminOrganizerListActivity activity;
    private AdminProfileAdapter adapterMock;
    private TextView emptyStateMock;
    private EditText searchInputMock;
    private ProgressBar progressBarMock;
    private FirebaseFirestore firestoreMock;

    @Before
    public void setUp() throws Exception {
        activity = mock(AdminOrganizerListActivity.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        adapterMock = mock(AdminProfileAdapter.class);
        emptyStateMock = mock(TextView.class);
        searchInputMock = mock(EditText.class);
        progressBarMock = mock(ProgressBar.class);
        firestoreMock = mock(FirebaseFirestore.class);

        setField("adapter", adapterMock);
        setField("tvEmptyState", emptyStateMock);
        setField("etSearch", searchInputMock);
        setField("progressBar", progressBarMock);
        setField("db", firestoreMock);

        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn("");
        when(searchInputMock.getText()).thenReturn(editable);

        List<User> organizers = accessAllOrganizers();
        organizers.clear();
        organizers.add(createUser("alpha01", "Alpha"));
        organizers.add(createUser("beta02", "Beta"));
    }

    @Test
    public void filterList_withEmptyQuery_showsAllOrganizers() throws Exception {
        invokeFilterList("");

        List<User> displayed = captureAdapterUsers();
        assertEquals(2, displayed.size());
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void filterList_withNameMatch_isCaseInsensitive() throws Exception {
        invokeFilterList("aLpHa");

        List<User> displayed = captureAdapterUsers();
        assertEquals(1, displayed.size());
        assertEquals("Alpha", displayed.get(0).getName());
        verify(emptyStateMock).setVisibility(View.GONE);
    }

    @Test
    public void filterList_withNoMatch_showsContextualEmptyMessage() throws Exception {
        invokeFilterList("missing");

        List<User> displayed = captureAdapterUsers();
        assertEquals(0, displayed.size());
        verify(emptyStateMock).setText("No organizers found matching \"missing\".");
        verify(emptyStateMock).setVisibility(View.VISIBLE);
    }

    @Test
    public void deleteOrganizer_withNullUserId_skipsFirestoreAndProgress() throws Exception {
        User user = new User();
        user.setUserId(null);

        invokeDeleteOrganizer(user);

        verify(progressBarMock, never()).setVisibility(View.VISIBLE);
        verifyNoInteractions(firestoreMock);
    }

    private User createUser(String userId, String name) {
        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        return user;
    }

    @SuppressWarnings("unchecked")
    private List<User> accessAllOrganizers() throws Exception {
        Field field = AdminOrganizerListActivity.class.getDeclaredField("allOrganizers");
        field.setAccessible(true);
        List<User> list = (List<User>) field.get(activity);
        if (list == null) {
            list = new ArrayList<>();
            field.set(activity, list);
        }
        return list;
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = AdminOrganizerListActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(activity, value);
    }

    @SuppressWarnings("unchecked")
    private List<User> captureAdapterUsers() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(adapterMock).setUsers(captor.capture());
        return captor.getValue();
    }

    private void invokeFilterList(String query) throws Exception {
        Method method = AdminOrganizerListActivity.class.getDeclaredMethod("filterList", String.class);
        method.setAccessible(true);
        method.invoke(activity, query);
    }

    private void invokeDeleteOrganizer(User user) throws Exception {
        Method method = AdminOrganizerListActivity.class.getDeclaredMethod("deleteOrganizer", User.class);
        method.setAccessible(true);
        method.invoke(activity, user);
    }
}

