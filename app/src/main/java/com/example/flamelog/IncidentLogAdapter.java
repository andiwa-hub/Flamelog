package com.example.flamelog;

import android.content.Context;
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
                .inflate(R.layout.item_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> incident = incidentList.get(position);

        holder.tvLocation.setText(incident.getOrDefault("location", "Unknown Location"));
        holder.tvStatus.setText(incident.getOrDefault("status", "Unknown Status"));
        holder.tvTimestamp.setText(incident.getOrDefault("timestamp", "No Timestamp"));
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    //  update data
    public void updateData(List<Map<String, String>> newData) {
        this.incidentList.clear();
        if (newData != null) {
            this.incidentList.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation, tvStatus, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
