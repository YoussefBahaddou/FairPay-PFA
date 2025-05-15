package com.emsi.fairpay_maroc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;
    private OnItemClickListener onItemClickListener;
    private OnItemActionListener onItemActionListener;

    public ItemAdapter(List<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item, position);
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.onItemActionListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Item item, int position);
    }

    public interface OnItemActionListener {
        void onEditItem(Item item, int position);
        void onDeleteItem(Item item, int position);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView productLocation;
        private final TextView productTime;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productLocation = itemView.findViewById(R.id.product_location);
            productTime = itemView.findViewById(R.id.product_time);

            // Set click listener for the whole item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(items.get(position), position);
                }
            });
        }

        public void bind(Item item, int position) {
            productName.setText(item.getNom());
            productPrice.setText(item.getPrix());
            productLocation.setText(item.getRegionName());
            productTime.setText(item.getTimeDifference());

            // Load image using Glide
            if (item.getImage() != null && !item.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImage())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
