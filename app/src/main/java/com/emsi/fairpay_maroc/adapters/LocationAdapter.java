package com.emsi.fairpay_maroc.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.Location;
import com.emsi.fairpay_maroc.ui.activities.MapActivity;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locations;
    private OnLocationClickListener onLocationClickListener;

    public interface OnLocationClickListener {
        void onLocationClick(Location location, int position);
    }

    public LocationAdapter(List<Location> locations) {
        this.locations = locations;
    }

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        this.onLocationClickListener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locations.get(position);
        holder.bind(location);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        private ImageView locationImage;
        private TextView locationName;
        private TextView locationCity;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationImage = itemView.findViewById(R.id.location_image);
            locationName = itemView.findViewById(R.id.location_name);
            locationCity = itemView.findViewById(R.id.location_city);

            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && LocationAdapter.this.onLocationClickListener != null) {
                    LocationAdapter.this.onLocationClickListener.onLocationClick(locations.get(position), position);
                }
            });
        }

        public void bind(Location location) {
            locationName.setText(location.getName());
            locationCity.setText(location.getVilleName());

            // Since Location doesn't have an imageUrl field, use a placeholder image
            locationImage.setImageResource(R.drawable.placeholder_image);

        }
    }
    
    public void updateLocations(List<Location> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }
}
