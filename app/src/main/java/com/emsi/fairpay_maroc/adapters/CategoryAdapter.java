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
import com.emsi.fairpay_maroc.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // If there's an icon URL, load it with Glide
        if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(category.getIconUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(getDrawableForCategory(holder.itemView.getContext(), category.getName()))
                    .into(holder.ivIcon);
        } else {
            // Otherwise use a drawable based on the category name
            holder.ivIcon.setImageResource(getDrawableForCategory(holder.itemView.getContext(), category.getName()));
        }

        // Set the category name
        holder.tvName.setText(category.getName());

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, position);
            }
        });
    }

    // Helper method to get the appropriate drawable for each category
    private int getDrawableForCategory(android.content.Context context, String categoryName) {
        if (categoryName == null) return R.drawable.ic_category; // Default icon

        String name = categoryName.toLowerCase();

        if (name.contains("électronique") || name.contains("electronic")) {
            return R.drawable.ic_category_electronics;
        } else if (name.contains("hébérgement") || name.contains("Hébérgement")) {
            return R.drawable.ic_category_accommodation;
        } else if (name.contains("accessoires") || name.contains("accessories")) {
            return R.drawable.ic_category_accessories;
        } else if (name.contains("cosmetic") || name.contains("cosmetique")) {
            return R.drawable.ic_category_cosmetic;
        } else if (name.contains("Automobile") || name.contains("auto") || name.contains("car")) {
            return R.drawable.ic_category_automobile;
        } else if (name.contains("nourriture") || name.contains("food") || name.contains("alimentation")) {
            return R.drawable.ic_category_food;
        } else if (name.contains("chaussures") || name.contains("shoes")) {
            return R.drawable.ic_category_shoes;
        } else {
            return R.drawable.ic_category; // Default icon
        }
    }
    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
