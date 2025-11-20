package com.example.flamelog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchNotifications, switchVibration;
    private Button btnLogOut, btnSetHomeLocation, btnEditProfile;
    private SeekBar seekBarSensitivity;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        switchNotifications = findViewById(R.id.switchNotifications);
        switchVibration = findViewById(R.id.switchVibration);
        btnLogOut = findViewById(R.id.btnLogOut);
        seekBarSensitivity = findViewById(R.id.seekBarSensitivity);
        btnSetHomeLocation = findViewById(R.id.btnSetHomeLocation);
        btnEditProfile = findViewById(R.id.btnEditProfile); // 🔧 new button

        // restores saved states
        switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", false));
        switchVibration.setChecked(prefs.getBoolean("vibration_enabled", false));
        seekBarSensitivity.setProgress(prefs.getInt("sensitivity_level", 50));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(this, "Notifications " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply();
            Toast.makeText(this, "Vibration/Sound " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("sensitivity_level", progress).apply();
                Toast.makeText(SettingsActivity.this, "Sensitivity: " + progress, Toast.LENGTH_SHORT).show();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnLogOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnSetHomeLocation.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MapPickerActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }
}



