package com.example.flamelog;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogActivity extends AppCompatActivity {

    private RecyclerView rvIncidentLogs;
    private IncidentLogAdapter adapter;
    private ArrayList<Map<String, String>> incidentList;
    private Spinner spinnerFilter;
    private DatabaseReference logsRef;
    private Button btnClearLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_log);

        rvIncidentLogs = findViewById(R.id.rvIncidentLogs);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        FloatingActionButton fabExport = findViewById(R.id.fabExportLogs);
        btnClearLogs = findViewById(R.id.btnClearLogs);

        incidentList = new ArrayList<>();
        adapter = new IncidentLogAdapter(this, incidentList);
        rvIncidentLogs.setLayoutManager(new LinearLayoutManager(this));
        rvIncidentLogs.setAdapter(adapter);

        //  firebase reference
        logsRef = FirebaseDatabase.getInstance()
                .getReference("incidents")
                .child("logs");

        // fetches the logs
        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                incidentList.clear();
                GenericTypeIndicator<Map<String, String>> typeIndicator =
                        new GenericTypeIndicator<Map<String, String>>() {};
                for (DataSnapshot logSnap : snapshot.getChildren()) {
                    Map<String, String> log = logSnap.getValue(typeIndicator);
                    if (log != null) {
                        log.put("key", logSnap.getKey());
                        if (!log.containsKey("alertLevel")) {
                            log.put("alertLevel", "Unknown");
                        }
                        incidentList.add(log);
                    }
                }
                adapter.notifyDataSetChanged();

                if (incidentList.isEmpty()) {
                    Toast.makeText(LogActivity.this, "No logs found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LogActivity.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
            }
        });

        // spinner setup
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.alert_levels, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        fabExport.setOnClickListener(v ->
                Toast.makeText(this, "Exporting logs...", Toast.LENGTH_SHORT).show()
        );

        // clear Logs button for admin
        btnClearLogs.setOnClickListener(v -> {
            boolean isAdmin = true; // Replace with actual role check if needed
            if (!isAdmin) {
                Toast.makeText(LogActivity.this, "Only admins can clear logs", Toast.LENGTH_SHORT).show();
                return;
            }

            logsRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    incidentList.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(LogActivity.this, "All incident logs cleared", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LogActivity.this, "Failed to clear logs", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void addLog(String location, String status, String alertLevel) {
        String timestamp = DateFormat.getDateTimeInstance().format(new Date());

        Map<String, String> log = new HashMap<>();
        log.put("location", location);
        log.put("status", status);
        log.put("alertLevel", alertLevel);   // 🔧 fixed to match Firebase field
        log.put("timestamp", timestamp);

        logsRef.push().setValue(log);
    }
}