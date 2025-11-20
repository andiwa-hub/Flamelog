package com.example.flamelog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginButton;
    TextView registerLink, forgotPasswordLink;
    FirebaseAuth mAuth;
    DatabaseReference userRef;

    private static final int NOTIF_PERMISSION_CODE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("UserProfile");

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        // notification permission for higher android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIF_PERMISSION_CODE
                );
            }
        }

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            handleLoginSuccess();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // registration
        registerLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        // forgot pass
        forgotPasswordLink.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Reset link sent to your email",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // login checker
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            // Already logged in → skip login screen
            handleLoginSuccess();
        }
    }

    // login success handler
    private void handleLoginSuccess() {
        String uid = mAuth.getCurrentUser().getUid();

        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean approved = snapshot.child("approved").getValue(Boolean.class);
                String role = snapshot.child("role").getValue(String.class);

                if (approved != null && approved) {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    FirebaseMessaging.getInstance()
                            .subscribeToTopic("alerts")
                            .addOnCompleteListener(t -> {
                                if (t.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this,
                                            "Notifications Enabled",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                    if ("admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, UserDashboardActivity.class));
                    }
                    finish();
                } else {
                    mAuth.signOut();
                    Toast.makeText(LoginActivity.this,
                            "Your account is pending admin approval.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoginActivity.this,
                        "Failed to check approval status.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}


