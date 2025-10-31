package com.example.flamelog;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class LogActivity extends AppCompatActivity {

    private RecyclerView rvIncidentLogs;
    private IncidentLogAdapter adapter;
    private ArrayList<Incident> incidentList;
    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_log);

        rvIncidentLogs = findViewById(R.id.rvIncidentLogs);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        FloatingActionButton fabExport = findViewById(R.id.fabExportLogs);

        // Sample incidents
        incidentList = new ArrayList<>();
        incidentList.add(new Incident("Fire near warehouse", "Heavy smoke visible", "High", android.R.drawable.ic_dialog_alert));
        incidentList.add(new Incident("Minor kitchen fire", "Contained quickly", "Low", android.R.drawable.ic_menu_info_details));
        incidentList.add(new Incident("Chemical spill", "Handled by response team", "Medium", android.R.drawable.ic_delete));

        // RecyclerView setup
        adapter = new IncidentLogAdapter(this, incidentList);
        rvIncidentLogs.setLayoutManager(new LinearLayoutManager(this));
        rvIncidentLogs.setAdapter(adapter);

        // Spinner setup
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.alert_levels, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        // Export button
        fabExport.setOnClickListener(v -> Toast.makeText(this, "Exporting logs...", Toast.LENGTH_SHORT).show());
    }
}
