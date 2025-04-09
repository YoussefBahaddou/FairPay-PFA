package com.emsi.fairpay_maroc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;

    public ItemAdapter(List<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ItemViewHolder(view);
    }

      @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
    
        // Load the image from the URL using Glide
        Glide.with(holder.itemView.getContext())
             .load(item.getImage()) // Load the image URL
             .placeholder(R.drawable.placeholder_image) // Optional placeholder image
             .error(R.drawable.null_img) // Optional error image
             .into(holder.ivProductImage);
    
        // Set the product name
        holder.tvProductName.setText(item.getNom());
    
        // Format the price to always end with "DH"
        holder.tvPrice.setText(item.getPrix() + " DH");
    
        // Format the date to include "Dernier mise à jour le :"
        holder.tvDate.setText("Dernier mise à jour le : " + item.getDatemiseajour());
    
        // Set the location text
        holder.tvLocation.setText(item.getVilleId() == -1 ? "Unknown Location" : "Location ID: " + item.getVilleId());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvLocation;
        TextView tvPrice;
        TextView tvDate;
        TextView tvUpdatedBy;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
