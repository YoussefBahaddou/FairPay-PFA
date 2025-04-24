package com.emsi.fairpay_maroc.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Item;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;
    private int userId; // Pass the logged-in user's ID to the adapter
    private Set<Integer> favoriteIds = new HashSet<>();
    private boolean isFavoritesView = false; // Flag to indicate if this is the favorites view

    public ItemAdapter(List<Item> items) {
        this.items = items;
        this.userId = -1; // Default value if userId is not provided
    }

    public ItemAdapter(List<Item> items, int userId) {
        this.items = items;
        this.userId = userId;
    }

    public ItemAdapter(List<Item> items, int userId, boolean isFavoritesView) {
        this.items = items;
        this.userId = userId;
        this.isFavoritesView = isFavoritesView; // Set the flag
    }

    public void setFavoriteIds(Set<Integer> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    public void updateItems(List<Item> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setFavoritesView(boolean isFavoritesView) {
        this.isFavoritesView = isFavoritesView;
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
             .load(item.getImage())
             .placeholder(R.drawable.placeholder_image)
             .error(R.drawable.null_img)
             .into(holder.ivProductImage);

        // Set the product details
        holder.tvProductName.setText(item.getNom());
        holder.tvPrice.setText(item.getPrix() + " DH");
        holder.tvType.setText("Type : " + item.getTypeName());
        holder.tvCategory.setText("Catégorie : " + item.getCategorieName());
        holder.tvLocation.setText("Ville : " + item.getRegionName());
        holder.tvDate.setText("Dernier mise à jour il y a : " + item.getDatemiseajour());

        // Check if the item is in the user's favorites
        boolean isFavorite = favoriteIds.contains(item.getId());
        holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_empty);

        // Handle favorite icon click
        holder.ivFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                // Remove from favorites
                removeFromFavorites(item.getId(), holder);

                if (isFavoritesView) {
                    // Only remove the item from the list if this is the favorites view
                    items.remove(position);
                    notifyItemRemoved(position);
                } else {
                    // Update the heart icon dynamically
                    holder.ivFavorite.setImageResource(R.drawable.ic_heart_empty);
                }
            } else {
                // Add to favorites
                addToFavorites(item.getId(), holder);
                holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled);
            }
        });
    }

    private boolean checkIfFavorite(int itemId) {
        try {
            // Query the "favoris" table to check if the item is already a favorite
            return SupabaseClient.queryTable("favoris", "produit_serv_id", String.valueOf(itemId), "*").length() > 0;
        } catch (Exception e) {
            Log.e("ItemAdapter", "Error checking if item is favorite: " + e.getMessage(), e);
            return false;
        }
    }

    private void addToFavorites(int itemId, ItemViewHolder holder) {
        if (favoriteIds.contains(itemId)) {
            // Item is already a favorite, no need to insert again
            return;
        }

        new Thread(() -> {
            try {
                JSONObject favorite = new JSONObject();
                favorite.put("utilisateur_id", userId); // Use the logged-in user's ID
                favorite.put("produit_serv_id", itemId);

                SupabaseClient.insertIntoTable("favoris", favorite);

                // Update the UI and local state on the main thread
                holder.itemView.post(() -> {
                    favoriteIds.add(itemId); // Add to the local state
                    holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled);
                    notifyDataSetChanged(); // Refresh the RecyclerView
                    Toast.makeText(holder.itemView.getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                });

                Log.d("ItemAdapter", "Item added to favorites: " + itemId);
            } catch (Exception e) {
                Log.e("ItemAdapter", "Error adding item to favorites: " + e.getMessage(), e);

                // Show error message on the main thread
                holder.itemView.post(() -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void removeFromFavorites(int itemId, ItemViewHolder holder) {
        if (!favoriteIds.contains(itemId)) {
            // Item is not a favorite, no need to delete
            return;
        }

        new Thread(() -> {
            try {
                SupabaseClient.deleteFromTable("favoris", "produit_serv_id", String.valueOf(itemId));

                // Update the UI and local state on the main thread
                holder.itemView.post(() -> {
                    favoriteIds.remove(itemId); // Remove from the local state
                    holder.ivFavorite.setImageResource(R.drawable.ic_heart_empty);
                    notifyDataSetChanged(); // Refresh the RecyclerView
                    Toast.makeText(holder.itemView.getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                });

                Log.d("ItemAdapter", "Item removed from favorites: " + itemId);
            } catch (Exception e) {
                Log.e("ItemAdapter", "Error removing item from favorites: " + e.getMessage(), e);

                // Show error message on the main thread
                holder.itemView.post(() -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        ImageView ivFavorite;
        TextView tvProductName;
        TextView tvLocation;
        TextView tvPrice;
        TextView tvDate;
        TextView tvCategory;
        TextView tvType;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivFavorite = itemView.findViewById(R.id.iv_favorite); // Heart icon
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvType = itemView.findViewById(R.id.tv_type);
        }
    }
}
