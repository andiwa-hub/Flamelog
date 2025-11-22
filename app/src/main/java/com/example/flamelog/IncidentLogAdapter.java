package com.example.flamelog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncidentLogAdapter extends RecyclerView.Adapter<IncidentLogAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, String>> incidentList;

    public IncidentLogAdapter(Context context, List<Map<String, String>> incidentList) {
        this.context = context;
        this.incidentList = incidentList != null ? incidentList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_log, parent, false); // ✅ row layout
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> incident = incidentList.get(position);

        String alertLevel = incident.getOrDefault("alertLevel", "Unknown");

        holder.tvLevel.setText("Level: " + alertLevel);
        holder.tvAddress.setText("Address: " + incident.getOrDefault("location", "Unknown Location"));
        holder.tvStatus.setText("Status: " + incident.getOrDefault("status", "Unknown Status"));
        holder.tvTimestamp.setText("Time: " + incident.getOrDefault("timestamp", "No Timestamp"));

        // ✅ Color‑coding for alert levels
        switch (alertLevel.toUpperCase()) {
            case "HIGH":
                holder.tvLevel.setTextColor(Color.parseColor("#D32F2F")); // Red
                break;
            case "MEDIUM":
                holder.tvLevel.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "LOW":
                holder.tvLevel.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            default:
                holder.tvLevel.setTextColor(Color.parseColor("#AAAAAA")); // Gray for Unknown
                break;
        }
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Map<String, String>> newData) {
        this.incidentList.clear();
        if (newData != null) {
            this.incidentList.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel, tvAddress, tvStatus, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
