package com.example.flamelog;

import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class EmergencyMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private View firePulse;
    private TextView tvLocation, tvNearestStation;
    private Button btnCall911, btnMarkResolved, btnShareLocation;
    private ImageButton btnBack, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_map);

        // Initialize UI components
        firePulse = findViewById(R.id.firePulse);
        tvLocation = findViewById(R.id.tvLocation);
        tvNearestStation = findViewById(R.id.tvNearestStation);
        btnCall911 = findViewById(R.id.btnCall911);
        btnMarkResolved = findViewById(R.id.btnMarkResolved);
        btnShareLocation = findViewById(R.id.btnShareLocation);
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);

        // Fire pulse glow animation
        Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.fire_pulse);
        firePulse.startAnimation(pulseAnim);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map fragment not found!", Toast.LENGTH_SHORT).show();
        }

        // Button actions
        btnBack.setOnClickListener(v -> finish()); // Go back
        btnSettings.setOnClickListener(v -> Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show());

        btnCall911.setOnClickListener(v -> callEmergency());
        btnMarkResolved.setOnClickListener(v -> markResolved());
        btnShareLocation.setOnClickListener(v -> shareLocation());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Apply dark custom map style
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_map_style));
            if (!success) {
                Toast.makeText(this, "⚠️ Map style failed to load.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error applying map style.", Toast.LENGTH_SHORT).show();
        }

        // Example hazard marker (replace with Firebase coordinates)
        LatLng hazardLocation = new LatLng(14.5995, 120.9842);
        mMap.addMarker(new MarkerOptions().position(hazardLocation).title("🔥 Fire Incident"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hazardLocation, 14f));

        // Fire pulse visible
        firePulse.setVisibility(View.VISIBLE);

        // Update location info
        tvLocation.setText("Manila, Philippines");
        tvNearestStation.setText("2.8 km • Quezon City Fire Dept.");
    }

    private void callEmergency() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:911")); // Replace with actual emergency number
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }
        startActivity(callIntent);
    }

    private void markResolved() {
        Toast.makeText(this, "Incident marked as resolved", Toast.LENGTH_SHORT).show();
        finish(); // Close activity
    }

    private void shareLocation() {
        LatLng hazardLocation = new LatLng(14.5995, 120.9842); // Replace with real-time GPS
        String locationLink = "https://maps.google.com/?q=" + hazardLocation.latitude + "," + hazardLocation.longitude;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Fire Incident Location: " + locationLink);
        startActivity(Intent.createChooser(shareIntent, "Share Location via"));
    }
}
