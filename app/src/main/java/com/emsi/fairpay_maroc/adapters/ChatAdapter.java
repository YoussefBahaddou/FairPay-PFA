package com.emsi.fairpay_maroc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.models.Chat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chats;
    private OnChatClickListener onChatClickListener;
    private OnChatDeleteListener onChatDeleteListener;
    private int currentUserId;

    public ChatAdapter(List<Chat> chats, int currentUserId) {
        this.chats = chats;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void updateChats(List<Chat> newChats) {
        this.chats = newChats;
        notifyDataSetChanged();
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.onChatClickListener = listener;
    }

    public void setOnChatDeleteListener(OnChatDeleteListener listener) {
        this.onChatDeleteListener = listener;
    }

    public interface OnChatClickListener {
        void onChatClick(Chat chat, int position);
    }

    public interface OnChatDeleteListener {
        void onDeleteChat(Chat chat, int position);
    }

    public List<Chat> getItems() {
        return chats;
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView chatSubject;
        private final TextView chatDate;
        private final ImageButton deleteButton;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatSubject = itemView.findViewById(R.id.chat_subject);
            chatDate = itemView.findViewById(R.id.chat_date);
            deleteButton = itemView.findViewById(R.id.delete_button);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onChatClickListener != null) {
                    onChatClickListener.onChatClick(chats.get(position), position);
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onChatDeleteListener != null) {
                    onChatDeleteListener.onDeleteChat(chats.get(position), position);
                }
            });
        }

        public void bind(Chat chat) {
            chatSubject.setText(chat.getSujet());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            chatDate.setText(dateFormat.format(chat.getDateCreation()));

            // Only show delete button if the chat belongs to the current user
            deleteButton.setVisibility(chat.getUtilisateurId() == currentUserId ? View.VISIBLE : View.GONE);
        }
    }
} 