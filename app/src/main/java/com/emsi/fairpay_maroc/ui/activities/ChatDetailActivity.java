package com.emsi.fairpay_maroc.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.adapters.CommentAdapter;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Commentaire;
import com.emsi.fairpay_maroc.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatDetailActivity extends AppCompatActivity {
    private static final String TAG = "ChatDetailActivity Log";
    private RecyclerView commentsRecyclerView;
    private ProgressBar progressBar;
    private CommentAdapter commentAdapter;
    private TextInputLayout messageInputLayout;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private TextView toolbarTitle;
    private int chatId;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Get current user ID from SharedPreferences
        currentUserId = SharedPreferencesManager.getInstance(this).getUserId();

        // Get chat details from intent
        chatId = getIntent().getIntExtra("chat_id", -1);
        String chatSubject = getIntent().getStringExtra("chat_subject");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Initialize views
        toolbarTitle = findViewById(R.id.toolbar_title);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        messageInputLayout = findViewById(R.id.message_input_layout);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        // Set chat subject in toolbar
        toolbarTitle.setText(chatSubject);

        // Setup RecyclerView
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(new ArrayList<>(), currentUserId);
        commentsRecyclerView.setAdapter(commentAdapter);

        // Set click listeners
        sendButton.setOnClickListener(v -> sendComment());
        commentAdapter.setOnCommentDeleteListener(this::deleteComment);

        // Load comments
        loadComments();
    }

    private void loadComments() {
        showLoading(true);
        Log.d(TAG, "Starting to load comments for chat ID: " + chatId);
        new Thread(() -> {
            try {
                String filter = "chat_id=eq." + chatId;
                Log.d(TAG, "Querying comments with filter: " + filter);
                JSONArray commentsArray = SupabaseClient.queryTableWithFilter("commentaire", filter, "*");
                Log.d(TAG, "Received comments array: " + commentsArray.toString());
                
                List<Commentaire> comments = new ArrayList<>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());

                for (int i = 0; i < commentsArray.length(); i++) {
                    JSONObject commentObj = commentsArray.getJSONObject(i);
                    Log.d(TAG, "Processing comment: " + commentObj.toString());
                    
                    String dateStr = commentObj.getString("date_creation");
                    Log.d(TAG, "Parsing date: " + dateStr);
                    Date dateCreation = dateFormat.parse(dateStr);
                    
                    Commentaire comment = new Commentaire(
                            commentObj.getInt("id"),
                            commentObj.getString("message"),
                            dateCreation,
                            commentObj.getInt("utilisateur_id"),
                            commentObj.getInt("chat_id")
                    );
                    comments.add(comment);
                    Log.d(TAG, "Added comment to list: " + comment.getMessage());
                }

                Log.d(TAG, "Total comments loaded: " + comments.size());
                runOnUiThread(() -> {
                    commentAdapter.updateComments(comments);
                    showLoading(false);
                    Log.d(TAG, "Comments loaded successfully");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading comments: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void sendComment() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            messageInputLayout.setError(getString(R.string.error_empty_message));
            return;
        }

        showLoading(true);
        new Thread(() -> {
            try {
                int nextId = SupabaseClient.getNextId("commentaire");
                JSONObject commentData = new JSONObject();
                commentData.put("id", nextId);
                commentData.put("message", message);
                commentData.put("date_creation", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                commentData.put("utilisateur_id", currentUserId);
                commentData.put("chat_id", chatId);

                JSONObject result = SupabaseClient.insertIntoTable("commentaire", commentData);
                runOnUiThread(() -> {
                    messageInput.setText("");
                    messageInputLayout.setError(null);
                    Toast.makeText(this, R.string.comment_added, Toast.LENGTH_SHORT).show();
                    loadComments();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void deleteComment(Commentaire comment, int position) {
        showLoading(true);
        new Thread(() -> {
            try {
                String path = "commentaire?id=eq." + comment.getId();
                boolean success = SupabaseClient
                        .deleteFromTable(path);
                runOnUiThread(() -> {
                    if (success) {
                        List<Commentaire> currentComments = commentAdapter.getItems();
                        currentComments.remove(position);
                        commentAdapter.updateComments(currentComments);
                        Toast.makeText(this, R.string.comment_deleted, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 