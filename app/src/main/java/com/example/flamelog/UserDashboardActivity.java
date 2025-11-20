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

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvAppName, tvTemperature, tvSmoke, tvFlame;
    private Button btnLogs, btnSettings;
    private Animation blinkAnim;
    private boolean emergencyScreenShown = false; // ✅ prevent multiple launches

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_dashboard); // use the user XML layout

        tvAppName = findViewById(R.id.tvAppName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvSmoke = findViewById(R.id.tvSmoke);
        tvFlame = findViewById(R.id.tvFlame);

        btnLogs = findViewById(R.id.btnLogs);
        btnSettings = findViewById(R.id.btnSettings);

        blinkAnim = AnimationUtils.loadAnimation(this, R.anim.blink);

        tvAppName.startAnimation(blinkAnim);

        btnLogs.setOnClickListener(v -> openLogs());
        btnSettings.setOnClickListener(v -> openSettings());

        // initializes sensor readings
        updateSensorValues(27, "Normal", "Safe");

        // fetch all sensor data from firebase
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

                // update UI
                updateSensorValues(temp, smokeStatus, flameStatus);

                // launches EmergencyMapActivity when a ahzard is detected
                boolean fireDetected = (smokeDigital != null && smokeDigital == 0)
                        || (flameDigital != null && flameDigital == 0);

                if (fireDetected && !emergencyScreenShown) {
                    emergencyScreenShown = true;
                    Intent intent = new Intent(UserDashboardActivity.this, EmergencyMapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                //  reset flag when hazard clears
                if (!fireDetected) {
                    emergencyScreenShown = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserDashboardActivity.this, "Failed to load sensor data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // update sensor readings
    public void updateSensorValues(int temperature, String smokeStatus, String flameStatus) {
        tvTemperature.setText(temperature + "°C");
        tvSmoke.setText(smokeStatus);
        tvFlame.setText(flameStatus);

        if (temperature >= 50) {
            tvTemperature.setTextColor(0xFFFF4D4D);
        } else if (temperature >= 35) {
            tvTemperature.setTextColor(0xFFFF884D);
        } else {
            tvTemperature.setTextColor(0xFF00FF00);
        }

        tvSmoke.setTextColor(smokeStatus.equalsIgnoreCase("Normal") ? 0xFF00FF00 : 0xFFFF4D4D);
        tvFlame.setTextColor(flameStatus.equalsIgnoreCase("Safe") ? 0xFF00FF00 : 0xFFFF4D4D);
    }

    private void openLogs() {
        Intent intent = new Intent(UserDashboardActivity.this, LogActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(UserDashboardActivity.this, SettingsActivity.class);
        startActivity(intent);
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
    }
}

