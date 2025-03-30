package com.emsi.fairpay_maroc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.PriceUpdate;

import java.util.List;

public class PriceUpdateAdapter extends RecyclerView.Adapter<PriceUpdateAdapter.PriceUpdateViewHolder> {

    private List<PriceUpdate> updates;

    public PriceUpdateAdapter(List<PriceUpdate> updates) {
        this.updates = updates;
    }

    @NonNull
    @Override
    public PriceUpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_price_update, parent, false);
        return new PriceUpdateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PriceUpdateViewHolder holder, int position) {
        PriceUpdate update = updates.get(position);
        holder.ivProductImage.setImageResource(update.getProductImageResId());
        holder.tvProductName.setText(update.getProductName());
        holder.tvLocation.setText(update.getLocation());
        holder.tvPrice.setText(update.getPrice());
        holder.tvDate.setText(update.getDate());
        holder.tvUpdatedBy.setText(update.getUpdatedBy());
    }

    @Override
    public int getItemCount() {
        return updates.size();
    }

    static class PriceUpdateViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvLocation;
        TextView tvPrice;
        TextView tvDate;
        TextView tvUpdatedBy;

        PriceUpdateViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvUpdatedBy = itemView.findViewById(R.id.tv_updated_by);
        }
    }
}
