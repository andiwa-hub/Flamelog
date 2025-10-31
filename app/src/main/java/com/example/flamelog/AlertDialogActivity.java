package com.example.flamelog;  // ← change to your actual package name

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlertDialogActivity extends AppCompatActivity {

    private ImageView imgWarningIcon;
    private TextView tvFireDetected, tvAlertLevel, tvRecommendedActions;
    private Button btnCallEmergency, btnViewLocation, btnViewMap, btnMarkResolved;

    private Animation neonPulseAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_alert);

        // 🔗 Link UI elements
        imgWarningIcon = findViewById(R.id.imgWarningIcon);
        tvFireDetected = findViewById(R.id.tvFireDetected);
        tvAlertLevel = findViewById(R.id.tvAlertLevel);
        tvRecommendedActions = findViewById(R.id.tvRecommendedActions);
        btnCallEmergency = findViewById(R.id.btnCallEmergency);
        btnViewLocation = findViewById(R.id.btnViewLocation);
        btnViewMap = findViewById(R.id.btnViewMap);
        btnMarkResolved = findViewById(R.id.btnMarkResolved);

        // ⚡ Load neon pulse animation
        neonPulseAnim = AnimationUtils.loadAnimation(this, R.anim.neon_pulse);

        // ⚡ Start neon animation
        imgWarningIcon.startAnimation(neonPulseAnim);
        tvFireDetected.startAnimation(neonPulseAnim);

        // ⚠️ Example dynamic alert level (could be passed from Firebase or sensors)
        String alertLevel = "HIGH";  // "LOW", "MEDIUM", or "HIGH"
        updateAlertLevel(alertLevel);

        // 📞 CALL EMERGENCY
        btnCallEmergency.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:911")); // change to your local emergency number
            startActivity(callIntent);
        });

        // 📍 VIEW LOCATION (example: open Google Maps with coordinates)
        btnViewLocation.setOnClickListener(v -> {
            String mapUri = "geo:0,0?q=Current+Fire+Location";
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        // 🗺️ VIEW MAP (launches your custom MapActivity)
        btnViewMap.setOnClickListener(v -> {
            Intent intent = new Intent(AlertDialogActivity.this, EmergencyMapActivity.class);
            startActivity(intent);
        });

        // ✅ MARK AS RESOLVED
        btnMarkResolved.setOnClickListener(v -> finish());
    }

    // 🌈 Change glow color + alert text dynamically
    private void updateAlertLevel(String level) {
        switch (level) {
            case "LOW":
                tvAlertLevel.setText("ALERT LEVEL: LOW");
                tvAlertLevel.setTextColor(0xFF00FF00); // Green
                imgWarningIcon.setColorFilter(0xFF00FF00);
                break;
            case "MEDIUM":
                tvAlertLevel.setText("ALERT LEVEL: MEDIUM");
                tvAlertLevel.setTextColor(0xFFFFD600); // Yellow
                imgWarningIcon.setColorFilter(0xFFFFD600);
                break;
            case "HIGH":
            default:
                tvAlertLevel.setText("ALERT LEVEL: HIGH");
                tvAlertLevel.setTextColor(0xFFFF1744); // Red
                imgWarningIcon.setColorFilter(0xFFFF1744);
                break;
        }
    }
}
