package com.example.connect.activities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.connect.network.CreateAccountRepo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

/**
 * Unit testing to verify Account creation logic
 * Using a mock datasbase to check
 * <p>
 * Tests:
 * 1. Successful registration saves data to database
 * 2. Correctly handles failed authentication
 * 3. Correctly handles a failed firestore write
 * 4. Registration without a mobile number works --> Full filling user stoy
 * <p>
 * Working as of 07/11/2025
 * @author Aakansh Chatterjee
 */

// Using mockito to avoid messing with actual database --> Automatically intializes the mocks
@RunWith(MockitoJUnitRunner.class)
public class AccountCreationFirebaseTest {

    @Mock
    FirebaseAuth mockAuth;
    @Mock
    FirebaseFirestore mockDb;
    @Mock
    Task<AuthResult> mockAuthTask;
    @Mock
    Task<Void> mockFirestoreTask;
    @Mock
    AuthResult mockAuthResult;
    @Mock
    FirebaseUser mockUser;
    @Mock
    CollectionReference mockCollection;
    @Mock
    DocumentReference mockDocument;

    // Call from network
    private CreateAccountRepo repo;
    private CreateAccountRepo.RegistrationCallback mockCallback;

    // Create a mock repo --> Fake firebase
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repo = new CreateAccountRepo(mockAuth, mockDb);
        mockCallback = mock(CreateAccountRepo.RegistrationCallback.class);

        // Setup common mocks --> runs before each test, so there a fresh mock and repo being tested on
        when(mockDb.collection("accounts")).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.set(any())).thenReturn(mockFirestoreTask);
    }


    // Test 1 - Successful registration saves data to database
    @Test
    public void testSuccessfulRegistrationWritesToFirestore() {
        // Arrange
        when(mockUser.getUid()).thenReturn("uid123");
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        // Mock successful auth --> need to create our own responses for the mock firebase
        when(mockAuthTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<AuthResult> listener = invocation.getArgument(0);
            listener.onSuccess(mockAuthResult);
            return mockAuthTask;
        });
        when(mockAuthTask.addOnFailureListener(any())).thenReturn(mockAuthTask);

        // Mock successful Firestore write
        when(mockFirestoreTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockFirestoreTask;
        });
        when(mockFirestoreTask.addOnFailureListener(any())).thenReturn(mockFirestoreTask);

        // Action of registering user
        repo.registerUser("test@example.com", "password123", "John Doe",
                "Johnny", "1234567890", mockCallback);

        // Verification
        verify(mockAuth).createUserWithEmailAndPassword("test@example.com", "password123");
        verify(mockDb).collection("accounts");
        verify(mockCollection).document("uid123");

        // Verify the data being saved
        ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockDocument).set(dataCaptor.capture());

        // Asseting values of the different fields
        Map<String, Object> savedData = dataCaptor.getValue();
        assert(savedData.get("full_name").equals("John Doe"));
        assert(savedData.get("display_name").equals("Johnny"));
        assert(savedData.get("email").equals("test@example.com"));
        assert(savedData.get("mobile_num").equals("1234567890"));

        // Verify callback
        verify(mockCallback).onSuccess();
    }

    // Test 2 - Failed authentication calls failure callback
    @Test
    public void testFailedAuthenticationCallsFailureCallback() {
        // Arrange
        Exception testException = new Exception("Authentication failed");
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        when(mockAuthTask.addOnSuccessListener(any())).thenReturn(mockAuthTask);
        when(mockAuthTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(testException);
            return mockAuthTask;
        });

        // Action of registering user
        repo.registerUser("fail@example.com", "badpass", "Jane Doe",
                "Jane", "", mockCallback);

        // Verifying callback
        verify(mockAuth).createUserWithEmailAndPassword("fail@example.com", "badpass");
        verify(mockCallback).onFailure("Account creation failed: Authentication failed");
    }


    // Test 3 - Failed firestore write calls failure callback
    @Test
    public void testFailedFirestoreWriteCallsFailureCallback() {
        // Arrange
        Exception firestoreException = new Exception("Firestore write failed");
        when(mockUser.getUid()).thenReturn("uid456");
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        // Mock successful auth
        when(mockAuthTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<AuthResult> listener = invocation.getArgument(0);
            listener.onSuccess(mockAuthResult);
            return mockAuthTask;
        });
        when(mockAuthTask.addOnFailureListener(any())).thenReturn(mockAuthTask);

        // Mock failed Firestore write
        when(mockFirestoreTask.addOnSuccessListener(any())).thenReturn(mockFirestoreTask);
        when(mockFirestoreTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(firestoreException);
            return mockFirestoreTask;
        });

        // Register use
        repo.registerUser("test@example.com", "password123", "Bob Smith",
                "Bob", "9876543210", mockCallback);

        // Verify the response
        verify(mockCallback).onFailure("Failed to save user data: Firestore write failed");
    }


    // Test 4 - Registration without a mobile number works
    @Test
    public void testRegistrationWithoutMobileNumber() {
        // Arrange
        when(mockUser.getUid()).thenReturn("uid789");
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        when(mockAuthTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<AuthResult> listener = invocation.getArgument(0);
            listener.onSuccess(mockAuthResult);
            return mockAuthTask;
        });
        when(mockAuthTask.addOnFailureListener(any())).thenReturn(mockAuthTask);

        when(mockFirestoreTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockFirestoreTask;
        });
        when(mockFirestoreTask.addOnFailureListener(any())).thenReturn(mockFirestoreTask);

        // Register User
        repo.registerUser("test@example.com", "password123", "Alice Wonder",
                "Alice", "", mockCallback);

        // Verify the document, post, creation
        ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockDocument).set(dataCaptor.capture());

        Map<String, Object> savedData = dataCaptor.getValue();
        assert(!savedData.containsKey("mobile_num")); // Should not include empty mobile number

        verify(mockCallback).onSuccess();
    }
}