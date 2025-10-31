package com.example.flamelog;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvAppName, tvTemperature, tvSmoke, tvFlame;
    private Button btnEmergency, btnLogs, btnSettings, btnSensorHealth;
    private Animation blinkAnim, pulseAnim;
    private ValueAnimator emergencyColorAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Views
        tvAppName = findViewById(R.id.tvAppName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvSmoke = findViewById(R.id.tvSmoke);
        tvFlame = findViewById(R.id.tvFlame);

        btnEmergency = findViewById(R.id.btnEmergency);
        btnLogs = findViewById(R.id.btnLogs);
        btnSettings = findViewById(R.id.btnSettings);
        btnSensorHealth = findViewById(R.id.btnSensorHealth);

        // Load Animations
        blinkAnim = AnimationUtils.loadAnimation(this, R.anim.blink);
        pulseAnim = AnimationUtils.loadAnimation(this, R.anim.button_neon_pulse);

        // Start Animations
        tvAppName.startAnimation(blinkAnim);
        btnEmergency.startAnimation(pulseAnim);

        // Start emergency button color pulse
        startEmergencyColorPulse();

        // Quick action buttons click listeners
        btnLogs.setOnClickListener(v -> openLogs());
        btnSettings.setOnClickListener(v -> openSettings());
        btnSensorHealth.setOnClickListener(v -> openSensorHealth());

        // Emergency button listener
        btnEmergency.setOnClickListener(v -> triggerEmergency());

        // Initialize sensor readings
        updateSensorValues(27, "Normal", "Safe");
    }

    // Animate emergency button color
    private void startEmergencyColorPulse() {
        int colorFrom = 0xFFFF7043; // soft orange
        int colorTo = 0xFFFF3D00;   // bright red
        emergencyColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        emergencyColorAnimator.setDuration(700);
        emergencyColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        emergencyColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        emergencyColorAnimator.addUpdateListener(anim -> {
            int color = (int) anim.getAnimatedValue();
            Drawable bg = DrawableCompat.wrap(btnEmergency.getBackground());
            DrawableCompat.setTint(bg, color);
            btnEmergency.setBackground(bg);
        });
        emergencyColorAnimator.start();
    }

    // Update sensor readings dynamically
    public void updateSensorValues(int temperature, String smokeStatus, String flameStatus) {
        tvTemperature.setText(temperature + "°C");
        tvSmoke.setText(smokeStatus);
        tvFlame.setText(flameStatus);

        // Dynamic colors
        if (temperature >= 50) {
            tvTemperature.setTextColor(0xFFFF4D4D); // Red
        } else if (temperature >= 35) {
            tvTemperature.setTextColor(0xFFFF884D); // Orange
        } else {
            tvTemperature.setTextColor(0xFF00FF00); // Green
        }

        tvSmoke.setTextColor(smokeStatus.equalsIgnoreCase("Normal") ? 0xFF00FF00 : 0xFFFF4D4D);
        tvFlame.setTextColor(flameStatus.equalsIgnoreCase("Safe") ? 0xFF00FF00 : 0xFFFF4D4D);
    }

    // Placeholder methods for quick action buttons
    private void openLogs() {
        // TODO: Implement Logs screen
    }

    private void openSettings() {
        // TODO: Implement Settings screen
    }

    private void openSensorHealth() {
        // TODO: Implement Sensor Health screen
    }

    private void triggerEmergency() {
        // TODO: Implement emergency alert trigger
    }

    @Override
    protected void onPause() {
        super.onPause();
        tvAppName.clearAnimation();
        btnEmergency.clearAnimation();
        if (emergencyColorAnimator != null) emergencyColorAnimator.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvAppName.startAnimation(blinkAnim);
        btnEmergency.startAnimation(pulseAnim);
        startEmergencyColorPulse();
    }
}
