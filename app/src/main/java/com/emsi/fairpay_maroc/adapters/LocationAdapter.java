package com.emsi.fairpay_maroc.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.Location;
import com.emsi.fairpay_maroc.ui.activities.MapActivity;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locations;
    private OnLocationSelectedListener listener;

    public interface OnLocationSelectedListener {
        void onLocationSelected(Location location);
    }

    public LocationAdapter(List<Location> locations) {
        this.locations = locations;
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locations.get(position);

        // Set the region name
        holder.tvName.setText(location.getName());

        // Set the associated ville name
        holder.tvVilleName.setText("Ville : " + location.getVilleName());
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocationSelected(location);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvVilleName;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_location_name);
            tvVilleName = itemView.findViewById(R.id.tv_location_ville_name);
        }
    }
    
    public void updateLocations(List<Location> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }
}
