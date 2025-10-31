package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private EditText et_Pass, et_Email;
    private Button btn_login;
    private ImageButton btn_back;

    private boolean result;

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        et_Email = findViewById(R.id.et_email);
        et_Pass = findViewById(R.id.et_Password);

        btn_login = findViewById(R.id.login_btn);
        btn_back= findViewById(R.id.back_btn);

        btn_login.setOnClickListener(v -> {
            loginCheck(et_Email, et_Pass);
        });

        // The animation slides the screen to the left, even though going back wards
        // This looks weird, to fix we need to do animation stuff --> If we got time
        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

// TO BE TESTED --> NEED AND UP AND RUNNING DATABASE First
    protected void loginCheck(EditText et_Email, EditText et_Pass){
        // need to write code that checks the database for the account
            // Figure if firebase have their own search parameters, and if there methods I can import here
        // Or build a search for the email first, and check if the account details match the password.

        String email = et_Email.getText().toString().trim();
        String password = et_Pass.getText().toString().trim();

        // Redundant Checks

        // Validate inputs
        if (email.isEmpty()) {
            et_Email.setError("Email is required");
            et_Email.requestFocus();
        }

        if (password.isEmpty()) {
            et_Pass.setError("Password is required");
            et_Pass.requestFocus();
        }


        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to next activity
                        Intent intent = new Intent(LoginActivity.this, EventListActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        // Login failed
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
