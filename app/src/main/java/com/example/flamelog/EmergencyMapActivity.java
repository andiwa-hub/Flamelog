package com.example.flamelog;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EmergencyMapActivity extends AppCompatActivity {

    private MapView map;
    private MapController mapController;
    private View firePulse;
    private TextView tvLocation, tvAlertMessage;
    private Button btnMarkResolved;
    private LinearLayout bottomPanel;

    private boolean incidentLogged = false;
    private boolean incidentActive = false;

    private GeoPoint homePoint = null;
    private Marker homeMarker = null;

    // Track previous state to avoid repeated triggers
    private String previousAlertLevel = "SAFE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setOsmdroidBasePath(getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(getCacheDir());
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_emergency_map);

        firePulse = findViewById(R.id.firePulse);
        tvLocation = findViewById(R.id.tvLocation);
        tvAlertMessage = findViewById(R.id.tvAlertMessage);
        bottomPanel = findViewById(R.id.bottomPanel);
        btnMarkResolved = findViewById(R.id.btnMarkResolved);

        Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.fire_pulse);
        firePulse.startAnimation(pulseAnim);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        mapController = (MapController) map.getController();

        ScaleBarOverlay scaleBar = new ScaleBarOverlay(map);
        scaleBar.setAlignBottom(true);
        map.getOverlays().add(scaleBar);

        // Fetch home location asynchronously
        DatabaseReference homeRef = FirebaseDatabase.getInstance().getReference("Location");
        homeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Double homeLat = snapshot.child("homeLat").getValue(Double.class);
                Double homeLng = snapshot.child("homeLng").getValue(Double.class);
                String homeAddress = snapshot.child("homeAddress").getValue(String.class);

                if (homeLat != null && homeLng != null) {
                    homePoint = new GeoPoint(homeLat, homeLng);

                    if (homeMarker != null) map.getOverlays().remove(homeMarker);
                    homeMarker = new Marker(map);
                    homeMarker.setPosition(homePoint);
                    homeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    homeMarker.setTitle("🏠 Home");
                    map.getOverlays().add(homeMarker);

                    tvLocation.setText(homeAddress != null ? homeAddress : "Lat: " + homeLat + ", Lng: " + homeLng);

                    map.post(() -> {
                        float zoomLevel = getDynamicZoomLevel();
                        mapController.setZoom(zoomLevel);
                        mapController.setCenter(homePoint);
                        map.invalidate();
                    });
                } else {
                    tvLocation.setText("Home location not set");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EmergencyMapActivity.this, "Failed to load home location", Toast.LENGTH_SHORT).show();
            }
        });

        // Sensor listener
        DatabaseReference sensorRef = FirebaseDatabase.getInstance().getReference("SensorData");
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long smokeDigital = snapshot.child("SmokeDigital").getValue(Long.class);
                Long flameDigital = snapshot.child("FlameDigital").getValue(Long.class);
                String alertLevel = snapshot.child("AlertLevel").getValue(String.class);

                boolean fireDetected = (smokeDigital != null && smokeDigital == 0)
                        || (flameDigital != null && flameDigital == 0);

                if (alertLevel == null) return;

                previousAlertLevel = alertLevel;

                if (fireDetected && "HIGH".equalsIgnoreCase(alertLevel)) {
                    tvAlertMessage.setVisibility(View.VISIBLE);
                    firePulse.setVisibility(View.VISIBLE);
                    bottomPanel.setVisibility(View.VISIBLE);

                    incidentActive = true;

                    if (!incidentLogged) {
                        logIncident(tvLocation.getText().toString(), "active", "HIGH");
                        incidentLogged = true;
                    }
                }
                // IMPORTANT CHANGE: Do not auto-hide when SAFE.
                // Panel stays visible until user presses "Mark Resolved".
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EmergencyMapActivity.this, "Sensor data error", Toast.LENGTH_SHORT).show();
            }
        });

        btnMarkResolved.setOnClickListener(v -> markResolved());

        // Initially hidden until HIGH alert
        tvAlertMessage.setVisibility(View.GONE);
        firePulse.setVisibility(View.GONE);
        bottomPanel.setVisibility(View.GONE);
    }

    private float getDynamicZoomLevel() {
        float density = getResources().getDisplayMetrics().density;
        if (density >= 3.0) return 19.0f;
        if (density >= 2.0) return 18.5f;
        return 18.0f;
    }

    private void logIncident(String location, String status, String alertLevel) {
        DatabaseReference logsRef = FirebaseDatabase.getInstance().getReference("incidents/logs");
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

    private void markResolved() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("incidents/current/status");
        ref.setValue("resolved");
        Toast.makeText(this, "Incident marked as resolved", Toast.LENGTH_SHORT).show();

        DatabaseReference sensorRef = FirebaseDatabase.getInstance().getReference("SensorData");
        sensorRef.child("AlertLevel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String alertLevel = snapshot.getValue(String.class);
                logIncident(tvLocation.getText().toString(), "resolved", alertLevel != null ? alertLevel : "Unknown");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logIncident(tvLocation.getText().toString(), "resolved", "Unknown");
            }
        });

        incidentLogged = false;
        incidentActive = false;

        tvAlertMessage.setVisibility(View.GONE);
        firePulse.setVisibility(View.GONE);
        bottomPanel.setVisibility(View.GONE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            DatabaseReference roleRef = FirebaseDatabase.getInstance().getReference("UserProfile").child(uid).child("role");

            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);
                    Intent intent;
                    if ("admin".equalsIgnoreCase(role)) {
                        intent = new Intent(EmergencyMapActivity.this, DashboardActivity.class);
                    } else {
                        intent = new Intent(EmergencyMapActivity.this, UserDashboardActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Intent intent = new Intent(EmergencyMapActivity.this, UserDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}
