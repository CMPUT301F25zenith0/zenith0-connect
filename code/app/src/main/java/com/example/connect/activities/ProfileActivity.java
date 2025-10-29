package com.example.connect.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.models.User;

/**
 * Activity for creating/updating a User profile.
 * Shows input fields for Name, Email, Phone, and a Submit button.
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone;
    private Button btnSubmit;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = new User("", "", "");

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnSubmit = findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(v -> submitProfile());
    }

    /**
     * Reads input fields and updates the User object.
     * Shows a simple confirmation toast.
     */
    private void submitProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);

        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
    }

    public User getUser() {
        return user;
    }
}