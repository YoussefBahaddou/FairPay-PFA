package com.emsi.fairpay_maroc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.Commentaire;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Commentaire> comments;
    private OnCommentDeleteListener onCommentDeleteListener;
    private int currentUserId;

    public CommentAdapter(List<Commentaire> comments, int currentUserId) {
        this.comments = comments;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Commentaire comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Commentaire> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    public void setOnCommentDeleteListener(OnCommentDeleteListener listener) {
        this.onCommentDeleteListener = listener;
    }

    public interface OnCommentDeleteListener {
        void onDeleteComment(Commentaire comment, int position);
    }

    public List<Commentaire> getItems() {
        return comments;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView commentMessage;
        private final TextView commentDate;
        private final ImageButton deleteButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentMessage = itemView.findViewById(R.id.comment_message);
            commentDate = itemView.findViewById(R.id.comment_date);
            deleteButton = itemView.findViewById(R.id.delete_button);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onCommentDeleteListener != null) {
                    onCommentDeleteListener.onDeleteComment(comments.get(position), position);
                }
            });
        }

        public void bind(Commentaire comment) {
            commentMessage.setText(comment.getMessage());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            commentDate.setText(dateFormat.format(comment.getDateCreation()));

            // Only show delete button if the comment belongs to the current user
            deleteButton.setVisibility(comment.getUtilisateurId() == currentUserId ? View.VISIBLE : View.GONE);
        }
    }
} 