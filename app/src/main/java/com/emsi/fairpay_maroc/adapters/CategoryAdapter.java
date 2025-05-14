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

        // Load the icon from the URL using Glide
        Glide.with(holder.itemView.getContext())
             .load(category.getIconUrl()) // Load the icon URL
             .placeholder(R.drawable.placeholder_image) // Optional placeholder image
             .error(R.drawable.error_image) // Optional error image
             .into(holder.ivIcon);

        // Set the category name
        holder.tvName.setText(category.getName());
        
        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, position);
            }
        });
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
