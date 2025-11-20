package com.example.flamelog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity {

    private MapView map;
    private Marker selectedMarker;
    private GeoPoint tappedPoint;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_map_picker);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController controller = map.getController();
        controller.setZoom(15.0);
        controller.setCenter(new GeoPoint(14.178, 121.243));

        map.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                tappedPoint = (GeoPoint) map.getProjection()
                        .fromPixels((int) event.getX(), (int) event.getY());

                if (selectedMarker != null) {
                    map.getOverlays().remove(selectedMarker);
                }

                selectedMarker = new Marker(map);
                selectedMarker.setPosition(tappedPoint);
                selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                selectedMarker.setTitle("Home");
                map.getOverlays().add(selectedMarker);
                map.invalidate();
            }
            return false;
        });

        Button btnConfirm = findViewById(R.id.btnConfirmHome);
        btnConfirm.setOnClickListener(v -> {
            if (tappedPoint != null) {
                saveHomeLocationToFirebase(tappedPoint.getLatitude(), tappedPoint.getLongitude());
            } else {
                Toast.makeText(this, "Please tap on the map first", Toast.LENGTH_SHORT).show();
            }
        });

        EditText etAddress = findViewById(R.id.etAddress);
        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            String addressStr = etAddress.getText().toString();
            if (!addressStr.isEmpty()) {
                Geocoder geocoder = new Geocoder(MapPickerActivity.this, Locale.getDefault());
                try {
                    List<Address> results = geocoder.getFromLocationName(addressStr, 1);
                    if (results != null && !results.isEmpty()) {
                        Address addr = results.get(0);
                        GeoPoint point = new GeoPoint(addr.getLatitude(), addr.getLongitude());

                        map.getController().setCenter(point);
                        map.getController().setZoom(17.0);

                        if (selectedMarker != null) {
                            map.getOverlays().remove(selectedMarker);
                        }
                        selectedMarker = new Marker(map);
                        selectedMarker.setPosition(point);
                        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        selectedMarker.setTitle("Home (from search)");
                        map.getOverlays().add(selectedMarker);
                        map.invalidate();

                        tappedPoint = point;
                    } else {
                        Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Geocoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnZoomIn = findViewById(R.id.btnZoomIn);
        Button btnZoomOut = findViewById(R.id.btnZoomOut);

        btnZoomIn.setOnClickListener(v -> map.getController().zoomIn());
        btnZoomOut.setOnClickListener(v -> map.getController().zoomOut());
    }

    private void saveHomeLocationToFirebase(double lat, double lng) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // saves location in firebase
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Location");

        ref.child("homeLat").setValue(lat);
        ref.child("homeLng").setValue(lng);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                String fullAddress = addr.getAddressLine(0);
                ref.child("homeAddress").setValue(fullAddress);
            } else {
                ref.child("homeAddress").setValue("Unknown address");
            }
        } catch (IOException e) {
            ref.child("homeAddress").setValue("Geocoding failed");
        }

        Toast.makeText(this, "Home location and address saved!", Toast.LENGTH_SHORT).show();
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



