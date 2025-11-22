package com.example.flamelog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HazardAlert extends AppCompatActivity {

    private LinearLayout mediumAlertPanel;
    private TextView tvMediumAlertTitle;
    private TextView tvMediumAlertLocation;
    private TextView tvMediumAlertMessage;
    private Button btnMediumAlertAcknowledge;

    private String currentAddress = "Unknown";
    private boolean incidentLogged = false;
    private boolean incidentActive = false;

    private DatabaseReference sensorRef;

    // Track previous state to avoid repeated triggers
    private String previousAlertLevel = "SAFE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hazard_alert);

        mediumAlertPanel = findViewById(R.id.mediumAlertPanel);
        tvMediumAlertTitle = findViewById(R.id.tvMediumAlertTitle);
        tvMediumAlertLocation = findViewById(R.id.tvMediumAlertLocation);
        tvMediumAlertMessage = findViewById(R.id.tvMediumAlertMessage);
        btnMediumAlertAcknowledge = findViewById(R.id.btnMediumAlertAcknowledge);

        if (mediumAlertPanel != null) {
            mediumAlertPanel.setVisibility(View.GONE);
        }

        // Load location asynchronously
        DatabaseReference homeRef = FirebaseDatabase.getInstance().getReference("Location");
        homeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Double homeLat = snapshot.child("homeLat").getValue(Double.class);
                Double homeLng = snapshot.child("homeLng").getValue(Double.class);
                String homeAddress = snapshot.child("homeAddress").getValue(String.class);

                if (homeLat != null && homeLng != null) {
                    currentAddress = (homeAddress != null) ? homeAddress
                            : "Lat: " + homeLat + ", Lng: " + homeLng;
                } else {
                    currentAddress = "Home location not set";
                }

                // Update location text if panel is already visible
                if (mediumAlertPanel.getVisibility() == View.VISIBLE) {
                    tvMediumAlertLocation.setText("Location: " + currentAddress);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HazardAlert.this, "Failed to load home location", Toast.LENGTH_SHORT).show();
            }
        });

        sensorRef = FirebaseDatabase.getInstance().getReference("SensorData");
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String alertLevel = snapshot.child("AlertLevel").getValue(String.class);
                if (alertLevel == null) return;

                previousAlertLevel = alertLevel;

                if ("MEDIUM".equalsIgnoreCase(alertLevel)) {
                    mediumAlertPanel.setVisibility(View.VISIBLE);
                    tvMediumAlertTitle.setText("⚠️ Medium Alert Detected!");
                    tvMediumAlertLocation.setText("Location: " + currentAddress);
                    tvMediumAlertMessage.setText("Smoke/Gas has been detected. Please check the area.");

                    incidentActive = true;

                    if (!incidentLogged) {
                        addLog(currentAddress, "active", "MEDIUM");
                        incidentLogged = true;
                    }
                }
                // Do not auto-hide when SAFE — panel stays until user acknowledges
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HazardAlert.this, "Sensor data error", Toast.LENGTH_SHORT).show();
            }
        });

        btnMediumAlertAcknowledge.setOnClickListener(v -> {
            Toast.makeText(HazardAlert.this, "Alert acknowledged", Toast.LENGTH_SHORT).show();
            mediumAlertPanel.setVisibility(View.GONE);

            if (sensorRef != null) {
                sensorRef.child("AlertLevel").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String alertLevel = snapshot.getValue(String.class);
                        addLog(currentAddress, "acknowledged", alertLevel);

                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        if (auth.getCurrentUser() != null) {
                            String uid = auth.getCurrentUser().getUid();
                            DatabaseReference roleRef = FirebaseDatabase.getInstance()
                                    .getReference("UserProfile")
                                    .child(uid)
                                    .child("role");

                            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    String role = snapshot.getValue(String.class);
                                    Intent intent;
                                    if ("admin".equalsIgnoreCase(role)) {
                                        intent = new Intent(HazardAlert.this, DashboardActivity.class);
                                    } else {
                                        intent = new Intent(HazardAlert.this, UserDashboardActivity.class);
                                    }
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Intent intent = new Intent(HazardAlert.this, UserDashboardActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            Intent intent = new Intent(HazardAlert.this, UserDashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(HazardAlert.this, "Failed to fetch alert level", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            incidentLogged = false;
            incidentActive = false;
        });
    }

    private void addLog(String location, String status, String alertLevel) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("incidents")
                .child("logs");

        String timestamp = DateFormat.getDateTimeInstance().format(new Date());

        Map<String, Object> log = new HashMap<>();
        log.put("location", location);
        log.put("status", status);
        log.put("alertLevel", alertLevel != null ? alertLevel : "Unknown");
        log.put("timestamp", timestamp);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            log.put("userId", auth.getCurrentUser().getUid());
        } else {
            log.put("userId", "anonymous");
        }

        logsRef.push().setValue(log);
    }
}



