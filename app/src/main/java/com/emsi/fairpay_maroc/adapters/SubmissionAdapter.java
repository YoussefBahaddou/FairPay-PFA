package com.emsi.fairpay_maroc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.Submission;

import java.util.List;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {

    private List<Submission> submissions;
    private OnItemClickListener listener;
    private OnStatusUpdateListener statusListener;
    private boolean isAdmin = false;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(String productId);
    }

    public interface OnStatusUpdateListener {
        void onStatusUpdate(String productId, String newStatus);
    }

    // Constructor with click listener
    public SubmissionAdapter(List<Submission> submissions, OnItemClickListener listener) {
        this.submissions = submissions;
        this.listener = listener;
    }

    public SubmissionAdapter(List<Submission> submissions, OnItemClickListener listener, 
                            OnStatusUpdateListener statusListener, boolean isAdmin) {
        this.submissions = submissions;
        this.listener = listener;
        this.statusListener = statusListener;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Submission submission = submissions.get(position);
        Context context = holder.itemView.getContext();

        // Set product name
        holder.productNameTextView.setText(submission.getNom());

        // Set price
        try {
            // Try to parse the price as a float
            float priceValue = Float.parseFloat(submission.getPrice());
            holder.priceTextView.setText(context.getString(R.string.price_format, priceValue));
        } catch (NumberFormatException e) {
            // If parsing fails, just display the price as is
            holder.priceTextView.setText(submission.getPrice());
        }

        // Set date
        holder.dateTextView.setText(submission.getDateMiseAJour());

        // Set status with appropriate color
        String status = submission.getStatus();
        holder.statusTextView.setText(status);
        int statusColor;
        switch (status.toLowerCase()) {
            case "approved":
                statusColor = ContextCompat.getColor(context, R.color.colorPrimary);
                holder.approveButton.setVisibility(View.GONE);
                break;
            case "rejected":
                statusColor = ContextCompat.getColor(context, R.color.colorAccent);
                holder.approveButton.setVisibility(View.GONE);
                break;
            default:
                statusColor = ContextCompat.getColor(context, R.color.gray);
                if (isAdmin && statusListener != null) {
                    holder.approveButton.setVisibility(View.VISIBLE);
                    holder.approveButton.setOnClickListener(v -> {
                        statusListener.onStatusUpdate(submission.getId(), "approved");
                    });
                } else {
                    holder.approveButton.setVisibility(View.GONE);
                }
                break;
        }
        holder.statusTextView.setTextColor(statusColor);

        // Show admin notes if available
        if (!TextUtils.isEmpty(submission.getCommentaire())) {
            holder.commentaireTextView.setText(submission.getCommentaire());
            holder.commentaireTextView.setVisibility(View.VISIBLE);
        } else {
            holder.commentaireTextView.setVisibility(View.GONE);
        }

        // Set click listener if provided
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(submission.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView priceTextView;
        TextView dateTextView;
        TextView statusTextView;
        TextView commentaireTextView;
        Button approveButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.tv_product_name);
            priceTextView = itemView.findViewById(R.id.tv_price);
            dateTextView = itemView.findViewById(R.id.tv_date);
            statusTextView = itemView.findViewById(R.id.tv_status);
            commentaireTextView = itemView.findViewById(R.id.tv_commentaire);
            approveButton = itemView.findViewById(R.id.btn_approve);
        }
    }
}