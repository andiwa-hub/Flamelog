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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private boolean locationReady = false;

    private GeoPoint homePoint = null;
    private Marker homeMarker = null;

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

        //  fetch location
        DatabaseReference homeRef = FirebaseDatabase.getInstance().getReference("Location");
        homeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Double homeLat = snapshot.child("homeLat").getValue(Double.class);
                Double homeLng = snapshot.child("homeLng").getValue(Double.class);
                String homeAddress = snapshot.child("homeAddress").getValue(String.class);

                if (homeLat != null && homeLng != null) {
                    homePoint = new GeoPoint(homeLat, homeLng);
                    locationReady = true;

                    if (homeMarker != null) map.getOverlays().remove(homeMarker);
                    homeMarker = new Marker(map);
                    homeMarker.setPosition(homePoint);
                    homeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    homeMarker.setTitle("🏠 Home");
                    map.getOverlays().add(homeMarker);

                    tvLocation.setText(homeAddress != null ? homeAddress : "Home location set");

                    map.post(() -> {
                        float zoomLevel = getDynamicZoomLevel();
                        mapController.setZoom(zoomLevel);
                        mapController.setCenter(homePoint);
                        map.invalidate();
                    });
                } else {
                    locationReady = false;
                    tvLocation.setText("Home location not set");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EmergencyMapActivity.this, "Failed to load home location", Toast.LENGTH_SHORT).show();
            }
        });

        // sensor listener
        DatabaseReference sensorRef = FirebaseDatabase.getInstance().getReference("SensorData");
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long smokeDigital = snapshot.child("SmokeDigital").getValue(Long.class);
                Long flameDigital = snapshot.child("FlameDigital").getValue(Long.class);
                String alertLevel = snapshot.child("AlertLevel").getValue(String.class);

                boolean fireDetected = (smokeDigital != null && smokeDigital == 0)
                        || (flameDigital != null && flameDigital == 0);

                if (fireDetected && locationReady) {
                    tvAlertMessage.setVisibility(View.VISIBLE);
                    firePulse.setVisibility(View.VISIBLE);
                    bottomPanel.setVisibility(View.VISIBLE);

                    incidentActive = true;

                    if (!incidentLogged) {
                        logIncident(tvLocation.getText().toString(), "active", alertLevel != null ? alertLevel : "Unknown");
                        incidentLogged = true;
                    }
                } else {
                    if (!incidentActive) {
                        tvAlertMessage.setVisibility(View.GONE);
                        firePulse.setVisibility(View.GONE);
                        bottomPanel.setVisibility(View.GONE);
                        incidentLogged = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EmergencyMapActivity.this, "Sensor data error", Toast.LENGTH_SHORT).show();
            }
        });

        btnMarkResolved.setOnClickListener(v -> markResolved());

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
        log.put("alertLevel", alertLevel);
        log.put("timestamp", timestamp);

        logsRef.push().setValue(log);
    }

    private void markResolved() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("incidents/current/status");
        ref.setValue("resolved");
        Toast.makeText(this, "Incident marked as resolved", Toast.LENGTH_SHORT).show();

        logIncident(tvLocation.getText().toString(), "resolved", "None");

        incidentLogged = false;
        incidentActive = false;

        // Redirect to Dashboard
        Intent intent = new Intent(EmergencyMapActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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








