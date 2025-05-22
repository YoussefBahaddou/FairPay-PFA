package com.emsi.fairpay_maroc.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.adapters.ChatAdapter;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Chat;
import com.emsi.fairpay_maroc.utils.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity Log";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ChatAdapter chatAdapter;
    private FloatingActionButton fabAddChat;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Log.d(TAG, "ChatListActivity created");

        // Get current user ID
        currentUserId = SharedPreferencesManager.getInstance(this).getUserId();

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        fabAddChat = findViewById(R.id.fab_add_chat);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(new ArrayList<>(), currentUserId);
        recyclerView.setAdapter(chatAdapter);

        // Set up click listeners
        fabAddChat.setOnClickListener(v -> {
            Log.d(TAG, "Add chat FAB clicked");
            Intent intent = new Intent(ChatListActivity.this, AddChatActivity.class);
            startActivityForResult(intent, 1001); // Request code for chat addition
        });

        chatAdapter.setOnChatClickListener((chat, position) -> {
            Log.d(TAG, "Chat clicked: " + chat.getSujet());
            Intent intent = new Intent(ChatListActivity.this, ChatDetailActivity.class);
            intent.putExtra("chat_id", chat.getId());
            intent.putExtra("chat_subject", chat.getSujet());
            startActivity(intent);
        });

        chatAdapter.setOnChatDeleteListener((chat, position) -> {
            Log.d(TAG, "Delete chat requested for: " + chat.getSujet());
            deleteChat(chat.getId(), position);
        });

        // Load chats
        loadChats();
    }

    private void loadChats() {
        Log.d(TAG, "Starting to load chats");
        showLoading(true);

        new Thread(() -> {
            try {
                Log.d(TAG, "Fetching chats from database");
                JSONArray chatsArray = SupabaseClient.queryTable("chat", null, null, "*");
                
                if (chatsArray == null) {
                    Log.e(TAG, "Chats array is null from database");
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    });
                    return;
                }

                Log.d(TAG, "Successfully fetched " + chatsArray.length() + " chats from database");
                List<Chat> chats = new ArrayList<>();

                for (int i = 0; i < chatsArray.length(); i++) {
                    JSONObject chatData = chatsArray.getJSONObject(i);
                    Log.d(TAG, "Processing chat data: " + chatData.toString());

                    int id = chatData.optInt("id", -1);
                    String sujet = chatData.optString("sujet", "");
                    String dateCreation = chatData.optString("date_creation", "");
                    int userId = chatData.optInt("utilisateur_id", -1);

                    if (id != -1 && !sujet.isEmpty()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date date = sdf.parse(dateCreation);
                            Chat chat = new Chat(id, sujet, date, userId);
                            chats.add(chat);
                            Log.d(TAG, "Added chat to list: ID=" + id + ", Subject=" + sujet);
                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing date: " + dateCreation, e);
                        }
                    } else {
                        Log.w(TAG, "Skipping invalid chat data: ID=" + id + ", Subject=" + sujet);
                    }
                }

                runOnUiThread(() -> {
                    Log.d(TAG, "Updating UI with " + chats.size() + " chats");
                    chatAdapter.updateChats(chats);
                    showLoading(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading chats: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void deleteChat(int chatId, int position) {
        Log.d(TAG, "Starting chat deletion for ID: " + chatId);
        showLoading(true);

        new Thread(() -> {
            try {
                Log.d(TAG, "Deleting comments first for chat ID: " + chatId);
                String commentsPath = "commentaire?chat_id=eq." + chatId;
                boolean commentsDeleted = SupabaseClient.deleteFromTable(commentsPath);
                
                if (commentsDeleted) {
                    Log.d(TAG, "Comments deleted successfully, now deleting chat");
                    String chatPath = "chat?id=eq." + chatId;
                    boolean chatDeleted = SupabaseClient.deleteFromTable(chatPath);

                    if (chatDeleted) {
                        Log.d(TAG, "Chat deleted successfully");
                        runOnUiThread(() -> {
                            List<Chat> currentChats = chatAdapter.getItems();
                            currentChats.remove(position);
                            chatAdapter.updateChats(currentChats);
                            showLoading(false);
                            Toast.makeText(this, R.string.chat_deleted, Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e(TAG, "Failed to delete chat after comments");
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.e(TAG, "Failed to delete comments");
                    runOnUiThread(() -> {
                        showLoading(false);

                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting chat and comments: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showLoading(boolean isLoading) {
        Log.d(TAG, "Setting loading state: " + isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            Log.d(TAG, "Chat added successfully, refreshing list");
            loadChats();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Back button pressed");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 