package com.example.flamelog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profilePic;
    private Button btnChangePic, btnSaveProfile, btnCancelProfile;
    private EditText editFullName, editEmail, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI elements
        profilePic = findViewById(R.id.profilePic);
        btnChangePic = findViewById(R.id.btnChangePic);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelProfile = findViewById(R.id.btnCancelProfile);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        // Change picture (placeholder)
        btnChangePic.setOnClickListener(v -> {
            Toast.makeText(this, "Change Picture clicked", Toast.LENGTH_SHORT).show();
            // TODO: Open gallery or camera
        });

        // Save changes
        btnSaveProfile.setOnClickListener(v -> {
            String fullName = editFullName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // Simple validation
            if (fullName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Save to database or SharedPreferences
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Return to SettingsActivity
        });

        // Cancel changes
        btnCancelProfile.setOnClickListener(v -> finish());
    }
}
