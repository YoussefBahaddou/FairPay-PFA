package com.emsi.fairpay_maroc.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.Item;
import com.emsi.fairpay_maroc.utils.ImageHelper;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;
    private OnItemClickListener listener;
    private OnItemActionListener actionListener;
    
    // Interface for item clicks
    public interface OnItemClickListener {
        void onItemClick(Item item, int position);
    }
    
    // Interface for item actions (edit, delete)
    public interface OnItemActionListener {
        void onEditItem(Item item, int position);
        void onDeleteItem(Item item, int position);
    }
    
    public ItemAdapter(List<Item> items) {
        this.items = items;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnItemActionListener(OnItemActionListener listener) {
        this.actionListener = listener;
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
        
        // Set item data
        holder.tvProductName.setText(item.getNom());
        holder.tvPrice.setText(item.getPrix() + " DH");
        holder.tvCategory.setText(holder.itemView.getContext().getString(R.string.category_colon) + " " + item.getCategorieName());
        holder.tvType.setText(holder.itemView.getContext().getString(R.string.type_colon) + " " + item.getTypeName());
        holder.tvLocation.setText(item.getRegionName());
        holder.tvDate.setText(holder.itemView.getContext().getString(R.string.last_updated) + " " + item.getTimeDifference());
        
        // Load the item image
        ImageHelper.loadImage(holder.itemView.getContext(), item.getImage(), holder.ivProductImage);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
        
        // Check user role to show/hide options menu
        SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences("FairPayPrefs", MODE_PRIVATE);
        int userRoleId = prefs.getInt("user_role_id", -1);
        
        // Show options menu for producers (role_id = 2) and contributors (role_id = 3)
        if (userRoleId == 2 || userRoleId == 3) {
            holder.ivOptions.setVisibility(View.VISIBLE);
            holder.ivOptions.setOnClickListener(v -> showOptionsMenu(v, item, position));
        } else {
            holder.ivOptions.setVisibility(View.GONE);
        }
    }
    
    private void showOptionsMenu(View view, Item item, int position) {
        Context context = view.getContext();
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.item_options_menu);
        
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_edit) {
                if (actionListener != null) {
                    actionListener.onEditItem(item, position);
                }
                return true;
            } else if (id == R.id.action_delete) {
                if (actionListener != null) {
                    actionListener.onDeleteItem(item, position);
                }
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateItems(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvPrice;
        TextView tvCategory;
        TextView tvType;
        TextView tvLocation;
        TextView tvDate;
        ImageView ivOptions;
        
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvType = itemView.findViewById(R.id.tv_type);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDate = itemView.findViewById(R.id.tv_date);
            ivOptions = itemView.findViewById(R.id.iv_options);
        }
    }
}
