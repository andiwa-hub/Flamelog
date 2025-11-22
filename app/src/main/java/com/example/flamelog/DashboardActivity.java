package com.example.flamelog;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvAppName, tvTemperature, tvSmoke, tvFlame;
    private Button btnRegisteredUsers, btnLogs, btnSettings, btnContacts;
    private Animation blinkAnim;
    private boolean emergencyScreenShown = false;

    private String previousAlertLevel = "SAFE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvAppName = findViewById(R.id.tvAppName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvSmoke = findViewById(R.id.tvSmoke);
        tvFlame = findViewById(R.id.tvFlame);

        btnRegisteredUsers = findViewById(R.id.btnRegisteredUsers);
        btnLogs = findViewById(R.id.btnLogs);
        btnSettings = findViewById(R.id.btnSettings);
        btnContacts = findViewById(R.id.btnContacts);

        blinkAnim = AnimationUtils.loadAnimation(this, R.anim.blink);
        tvAppName.startAnimation(blinkAnim);

        btnLogs.setOnClickListener(v -> openLogs());
        btnSettings.setOnClickListener(v -> openSettings());
        btnContacts.setOnClickListener(v -> openContacts());
        btnRegisteredUsers.setOnClickListener(v -> openRegisteredUsers());

        updateSensorValues(27, "Normal", "Safe");

        DatabaseReference sensorRef = FirebaseDatabase.getInstance().getReference("SensorData");
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Double temperature = snapshot.child("Temperature").getValue(Double.class);
                int temp = temperature != null ? temperature.intValue() : 0;

                Integer smokeDigital = snapshot.child("SmokeDigital").getValue(Integer.class);
                String smokeStatus = (smokeDigital != null && smokeDigital == 0) ? "Detected" : "Normal";

                Integer flameDigital = snapshot.child("FlameDigital").getValue(Integer.class);
                String flameStatus = (flameDigital != null && flameDigital == 0) ? "Detected" : "Safe";

                String alertLevel = snapshot.child("AlertLevel").getValue(String.class);
                if (alertLevel == null) alertLevel = "SAFE";

                updateSensorValues(temp, smokeStatus, flameStatus);

                // ✅ Only react when alert level changes
                if (!alertLevel.equalsIgnoreCase(previousAlertLevel)) {
                    previousAlertLevel = alertLevel;

                    if ("MEDIUM".equalsIgnoreCase(alertLevel) && !emergencyScreenShown) {
                        emergencyScreenShown = true;
                        Intent intent = new Intent(DashboardActivity.this, HazardAlert.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if ("HIGH".equalsIgnoreCase(alertLevel) && !emergencyScreenShown) {
                        emergencyScreenShown = true;
                        Intent intent = new Intent(DashboardActivity.this, EmergencyMapActivity.class);
                        intent.putExtra("AlertLevel", alertLevel);
                        intent.putExtra("Address", snapshot.child("Address").getValue(String.class));
                        intent.putExtra("Latitude", snapshot.child("Latitude").getValue(String.class));
                        intent.putExtra("Longitude", snapshot.child("Longitude").getValue(String.class));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if ("SAFE".equalsIgnoreCase(alertLevel) || "LOW".equalsIgnoreCase(alertLevel)) {
                        emergencyScreenShown = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load sensor data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateSensorValues(int temperature, String smokeStatus, String flameStatus) {
        tvTemperature.setText(temperature + "°C");
        tvSmoke.setText(smokeStatus);
        tvFlame.setText(flameStatus);

        if (temperature >= 35) {
            tvTemperature.setTextColor(0xFFFF0000);
        } else {
            tvTemperature.setTextColor(0xFF00FF00);
        }

        tvSmoke.setTextColor(smokeStatus.equalsIgnoreCase("Normal") ? 0xFF00FF00 : 0xFFFF0000);
        tvFlame.setTextColor(flameStatus.equalsIgnoreCase("Safe") ? 0xFF00FF00 : 0xFFFF0000);
    }

    private void openLogs() {
        startActivity(new Intent(DashboardActivity.this, LogActivity.class));
    }

    private void openSettings() {
        startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
    }

    private void openContacts() {
        startActivity(new Intent(DashboardActivity.this, ContactsActivity.class));
    }

    private void openRegisteredUsers() {
        startActivity(new Intent(DashboardActivity.this, RegisteredUsersActivity.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        tvAppName.clearAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvAppName.startAnimation(blinkAnim);
        emergencyScreenShown = false;
    }
}




