package com.example.flamelog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotifications, switchVibration;
    private SeekBar seekBarSensitivity;
    private Button btnEditProfile, btnLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize UI elements
        switchNotifications = findViewById(R.id.switchNotifications);
        switchVibration = findViewById(R.id.switchVibration);
        seekBarSensitivity = findViewById(R.id.seekBarSensitivity);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogOut = findViewById(R.id.btnLogOut);

        // Example: Switch listeners
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Notifications " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Vibration/Sound " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        // Example: SeekBar listener
        seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // You can save this sensitivity value to preferences
                Toast.makeText(SettingsActivity.this, "Sensitivity: " + progress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Edit Profile button
        btnEditProfile.setOnClickListener(v -> {
            // Replace with your EditProfileActivity
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        // Log Out button
        btnLogOut.setOnClickListener(v -> {
            // Example: go back to login
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
