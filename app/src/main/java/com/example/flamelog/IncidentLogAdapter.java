package com.example.flamelog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IncidentLogAdapter extends RecyclerView.Adapter<IncidentLogAdapter.ViewHolder> {

    private Context context;
    private List<Incident> incidentList;

    public IncidentLogAdapter(Context context, List<Incident> incidentList) {
        this.context = context;
        this.incidentList = incidentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_incident_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Incident incident = incidentList.get(position);
        holder.title.setText(incident.getTitle());
        holder.description.setText(incident.getDescription());
        holder.icon.setImageResource(incident.getIconResId());
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvIncidentTitle);
            description = itemView.findViewById(R.id.tvIncidentDescription);
            icon = itemView.findViewById(R.id.ivIncidentIcon);
        }
    }
}
