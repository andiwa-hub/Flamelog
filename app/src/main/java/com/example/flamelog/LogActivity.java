package com.example.flamelog;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;
import java.util.Map;

public class LogActivity extends AppCompatActivity {

    private RecyclerView rvIncidentLogs;
    private IncidentLogAdapter adapter;
    private ArrayList<Map<String, String>> incidentList;      // filtered list
    private ArrayList<Map<String, String>> allIncidentList;   // master list
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
        allIncidentList = new ArrayList<>();
        adapter = new IncidentLogAdapter(this, incidentList);
        rvIncidentLogs.setLayoutManager(new LinearLayoutManager(this));
        rvIncidentLogs.setAdapter(adapter);

        logsRef = FirebaseDatabase.getInstance()
                .getReference("incidents")
                .child("logs");

        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allIncidentList.clear();
                GenericTypeIndicator<Map<String, String>> typeIndicator =
                        new GenericTypeIndicator<Map<String, String>>() {};
                for (DataSnapshot logSnap : snapshot.getChildren()) {
                    Map<String, String> log = logSnap.getValue(typeIndicator);
                    if (log != null) {
                        log.put("key", logSnap.getKey());
                        if (!log.containsKey("alertLevel")) {
                            log.put("alertLevel", "Unknown");
                        }
                        allIncidentList.add(log);
                    }
                }
                if (spinnerFilter.getSelectedItem() != null) {
                    applyFilter(spinnerFilter.getSelectedItem().toString());
                }

                if (allIncidentList.isEmpty()) {
                    Toast.makeText(LogActivity.this, "No logs found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LogActivity.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
            }
        });

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.alert_levels, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                applyFilter(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilter("All");
            }
        });

        fabExport.setOnClickListener(v ->
                Toast.makeText(this, "Exporting logs...", Toast.LENGTH_SHORT).show()
        );

        btnClearLogs.setOnClickListener(v -> {
            boolean isAdmin = true;
            if (!isAdmin) {
                Toast.makeText(LogActivity.this, "Only admins can clear logs", Toast.LENGTH_SHORT).show();
                return;
            }

            logsRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    allIncidentList.clear();
                    incidentList.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(LogActivity.this, "All incident logs cleared", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LogActivity.this, "Failed to clear logs", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void applyFilter(String filter) {
        incidentList.clear();
        if (filter.equalsIgnoreCase("All")) {
            incidentList.addAll(allIncidentList);
        } else {
            for (Map<String, String> log : allIncidentList) {
                String level = log.get("alertLevel");
                if (level != null && level.equalsIgnoreCase(filter)) {
                    incidentList.add(log);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}

