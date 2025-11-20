package com.example.flamelog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private Button btnSaveProfile, btnCancelProfile;
    private EditText editFullName, editEmail, editPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelProfile = findViewById(R.id.btnCancelProfile);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance()
                    .getReference("UserProfile")
                    .child(currentUser.getUid());
        }

        // save changes
        btnSaveProfile.setOnClickListener(v -> {
            String fullName = editFullName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser != null) {
                // ✅ Update Realtime Database fields to match your schema
                userRef.child("name").setValue(fullName);
                userRef.child("email").setValue(email);

                // Update FirebaseAuth email
                currentUser.updateEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show();
                    }
                });

                // udpate password
                if (!password.isEmpty()) {
                    currentUser.updatePassword(password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Return to SettingsActivity
            }
        });

        btnCancelProfile.setOnClickListener(v -> finish());
    }
}
