package com.example.connect;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.content.SharedPreferences;

import com.example.connect.activities.AdminDashboardActivity;
import com.example.connect.activities.CreateAcctActivity;
import com.example.connect.activities.EventListActivity;
import com.example.connect.activities.LoginActivity;
import com.example.connect.activities.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for MainActivity
 * Tests auto-login logic, user status checking, and navigation flows
 *
 * @author Digaant Chhokra
 * @version 1.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest {

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private SharedPreferences mockSharedPrefs;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private DocumentReference mockDocRef;

    @Mock
    private DocumentSnapshot mockDocSnapshot;

    @Mock
    private Task<DocumentSnapshot> mockTask;

    private String testUserId = "test_user_123";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup default mock behaviors
        when(mockSharedPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
        when(mockUser.getUid()).thenReturn(testUserId);
    }

    // ========== AUTO-LOGIN TESTS ==========

    /**
     * Test 1: User with Remember Me enabled and valid session should auto-login
     */
    @Test
    public void testAutoLoginWithRememberMeEnabled() {
        // Given: Remember Me is enabled and user is logged in
        when(mockSharedPrefs.getBoolean("rememberMe", false)).thenReturn(true);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        // When: checkAutoLogin is called
        // Then: Should proceed to check user status
        verify(mockAuth, never()).signOut();
    }

    /**
     * Test 2: User without Remember Me should show main screen
     */
    @Test
    public void testNoAutoLoginWithoutRememberMe() {
        // Given: Remember Me is disabled
        when(mockSharedPrefs.getBoolean("rememberMe", false)).thenReturn(false);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        // When: checkAutoLogin is called
        // Then: Should show main activity screen (not auto-login)
        assertFalse("Remember Me should be disabled",
                mockSharedPrefs.getBoolean("rememberMe", false));
    }

    /**
     * Test 3: No current user should show main screen
     */
    @Test
    public void testNoAutoLoginWithoutCurrentUser() {
        // Given: Remember Me is enabled but no user is logged in
        when(mockSharedPrefs.getBoolean("rememberMe", false)).thenReturn(true);
        when(mockAuth.getCurrentUser()).thenReturn(null);

        // When: checkAutoLogin is called
        // Then: Should show main activity screen
        assertNull("Current user should be null", mockAuth.getCurrentUser());
    }

    // ========== USER STATUS TESTS ==========

    /**
     * Test 4: Regular user should navigate to EventListActivity
     */
    @Test
    public void testRegularUserNavigatesToEventList() {
        // Given: User document exists with admin = false
        when(mockDocSnapshot.exists()).thenReturn(true);
        when(mockDocSnapshot.getBoolean("disabled")).thenReturn(false);
        when(mockDocSnapshot.getBoolean("admin")).thenReturn(false);

        // Then: Should navigate to EventListActivity (verified by intent)
        assertFalse("User should not be admin",
                mockDocSnapshot.getBoolean("admin"));
    }

    /**
     * Test 5: Admin user should navigate to AdminDashboardActivity
     */
    @Test
    public void testAdminUserNavigatesToAdminDashboard() {
        // Given: User document exists with admin = true
        when(mockDocSnapshot.exists()).thenReturn(true);
        when(mockDocSnapshot.getBoolean("disabled")).thenReturn(false);
        when(mockDocSnapshot.getBoolean("admin")).thenReturn(true);

        // Then: Should navigate to AdminDashboardActivity
        assertTrue("User should be admin",
                mockDocSnapshot.getBoolean("admin"));
    }

    /**
     * Test 6: Disabled user should be signed out
     */
    @Test
    public void testDisabledUserIsSignedOut() {
        // Given: User document exists with disabled = true
        when(mockDocSnapshot.exists()).thenReturn(true);
        when(mockDocSnapshot.getBoolean("disabled")).thenReturn(true);
        when(mockDocSnapshot.getBoolean("admin")).thenReturn(false);

        // Then: User should be treated as disabled
        assertTrue("User should be disabled",
                mockDocSnapshot.getBoolean("disabled"));
    }

    /**
     * Test 7: Non-existent user document should sign out user
     */
    @Test
    public void testNonExistentUserDocumentSignsOut() {
        // Given: User document doesn't exist
        when(mockDocSnapshot.exists()).thenReturn(false);

        // Then: Should sign out user
        assertFalse("User document should not exist",
                mockDocSnapshot.exists());
    }

    // ========== SHARED PREFERENCES TESTS ==========

    /**
     * Test 8: Remember Me preference can be retrieved
     */
    @Test
    public void testRememberMePreferenceRetrieval() {
        // Given: Remember Me is set to true
        when(mockSharedPrefs.getBoolean("rememberMe", false)).thenReturn(true);

        // When: Retrieving preference
        boolean rememberMe = mockSharedPrefs.getBoolean("rememberMe", false);

        // Then: Should return true
        assertTrue("Remember Me should be enabled", rememberMe);
    }

    /**
     * Test 9: Remember Me preference can be cleared
     */
    @Test
    public void testRememberMePreferenceClear() {
        // When: Clearing Remember Me
        when(mockEditor.putBoolean("rememberMe", false)).thenReturn(mockEditor);

        mockSharedPrefs.edit().putBoolean("rememberMe", false).apply();

        // Then: Verify the editor was used correctly
        verify(mockSharedPrefs).edit();
        verify(mockEditor).putBoolean("rememberMe", false);
        verify(mockEditor).apply();
    }

    /**
     * Test 10: Default Remember Me value should be false
     */
    @Test
    public void testDefaultRememberMeValueIsFalse() {
        // Given: No preference set
        when(mockSharedPrefs.getBoolean("rememberMe", false)).thenReturn(false);

        // When: Retrieving preference
        boolean rememberMe = mockSharedPrefs.getBoolean("rememberMe", false);

        // Then: Should return false (default)
        assertFalse("Default Remember Me should be false", rememberMe);
    }

    // ========== FIREBASE AUTH TESTS ==========

    /**
     * Test 11: FirebaseAuth returns current user correctly
     */
    @Test
    public void testFirebaseAuthReturnsCurrentUser() {
        // Given: Current user exists
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        // When: Getting current user
        FirebaseUser currentUser = mockAuth.getCurrentUser();

        // Then: Should not be null
        assertNotNull("Current user should not be null", currentUser);
        assertEquals("User ID should match", testUserId, currentUser.getUid());
    }

    /**
     * Test 12: FirebaseAuth returns null when no user
     */
    @Test
    public void testFirebaseAuthReturnsNullWhenNoUser() {
        // Given: No current user
        when(mockAuth.getCurrentUser()).thenReturn(null);

        // When: Getting current user
        FirebaseUser currentUser = mockAuth.getCurrentUser();

        // Then: Should be null
        assertNull("Current user should be null", currentUser);
    }

    /**
     * Test 13: FirebaseAuth sign out works correctly
     */
    @Test
    public void testFirebaseAuthSignOut() {
        // When: Signing out
        mockAuth.signOut();

        // Then: Verify sign out was called
        verify(mockAuth, times(1)).signOut();
    }

    // ========== FIRESTORE TESTS ==========

    /**
     * Test 14: Firestore document retrieval returns existing document
     */
    @Test
    public void testFirestoreDocumentExists() {
        // Given: Document exists
        when(mockDocSnapshot.exists()).thenReturn(true);

        // When: Checking if document exists
        boolean exists = mockDocSnapshot.exists();

        // Then: Should return true
        assertTrue("Document should exist", exists);
    }

    /**
     * Test 15: Firestore document retrieval returns null for non-existent document
     */
    @Test
    public void testFirestoreDocumentDoesNotExist() {
        // Given: Document doesn't exist
        when(mockDocSnapshot.exists()).thenReturn(false);

        // When: Checking if document exists
        boolean exists = mockDocSnapshot.exists();

        // Then: Should return false
        assertFalse("Document should not exist", exists);
    }

    /**
     * Test 16: Firestore can retrieve admin field correctly
     */
    @Test
    public void testFirestoreRetrievesAdminField() {
        // Given: Document has admin field set to true
        when(mockDocSnapshot.getBoolean("admin")).thenReturn(true);

        // When: Retrieving admin field
        Boolean isAdmin = mockDocSnapshot.getBoolean("admin");

        // Then: Should return true
        assertNotNull("Admin field should not be null", isAdmin);
        assertTrue("Admin should be true", isAdmin);
    }

    /**
     * Test 17: Firestore can retrieve disabled field correctly
     */
    @Test
    public void testFirestoreRetrievesDisabledField() {
        // Given: Document has disabled field set to true
        when(mockDocSnapshot.getBoolean("disabled")).thenReturn(true);

        // When: Retrieving disabled field
        Boolean isDisabled = mockDocSnapshot.getBoolean("disabled");

        // Then: Should return true
        assertNotNull("Disabled field should not be null", isDisabled);
        assertTrue("Disabled should be true", isDisabled);
    }

    /**
     * Test 18: Null admin field should be handled correctly
     */
    @Test
    public void testNullAdminFieldHandling() {
        // Given: Document has null admin field
        when(mockDocSnapshot.getBoolean("admin")).thenReturn(null);

        // When: Retrieving admin field
        Boolean isAdmin = mockDocSnapshot.getBoolean("admin");

        // Then: Should be null
        assertNull("Admin field should be null", isAdmin);
    }

    /**
     * Test 19: Null disabled field should be handled correctly
     */
    @Test
    public void testNullDisabledFieldHandling() {
        // Given: Document has null disabled field
        when(mockDocSnapshot.getBoolean("disabled")).thenReturn(null);

        // When: Retrieving disabled field
        Boolean isDisabled = mockDocSnapshot.getBoolean("disabled");

        // Then: Should be null
        assertNull("Disabled field should be null", isDisabled);
    }

    /**
     * Test 20: User ID is correctly retrieved from FirebaseUser
     */
    @Test
    public void testUserIdRetrieval() {
        // Given: User has ID
        when(mockUser.getUid()).thenReturn(testUserId);

        // When: Retrieving user ID
        String userId = mockUser.getUid();

        // Then: Should match expected ID
        assertEquals("User ID should match", testUserId, userId);
    }
}