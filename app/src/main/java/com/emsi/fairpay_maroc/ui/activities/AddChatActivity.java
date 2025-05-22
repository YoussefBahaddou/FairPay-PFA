package com.emsi.fairpay_maroc.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity logs";
    private TextInputLayout subjectInputLayout;
    private TextInputEditText subjectEditText;
    private MaterialButton createButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);
        Log.d(TAG, "AddChatActivity created");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Log.d(TAG, "Toolbar setup completed");
        }

        // Initialize views
        subjectInputLayout = findViewById(R.id.subject_input_layout);
        subjectEditText = findViewById(R.id.subject_edit_text);
        createButton = findViewById(R.id.create_button);
        progressBar = findViewById(R.id.progress_bar);

        // Set up create button click listener
        createButton.setOnClickListener(v -> {
            Log.d(TAG, "Create button clicked");
            createChat();
        });
    }

    private void createChat() {
        String subject = subjectEditText.getText().toString().trim();
        Log.d(TAG, "Creating chat with subject: " + subject);

        if (subject.isEmpty()) {
            Log.w(TAG, "Subject is empty");
            subjectInputLayout.setError(getString(R.string.error_empty_subject));
            return;
        }

        showLoading(true);
        Log.d(TAG, "Starting chat creation process");

        new Thread(() -> {
            try {
                // Get current user ID
                int userId = SharedPreferencesManager.getInstance(this).getUserId();
                Log.d(TAG, "Current user ID: " + userId);

                // Get next available ID
                int nextId = SupabaseClient.getNextId("chat");
                Log.d(TAG, "Next available ID: " + nextId);

                // Create JSON object for chat data
                JSONObject chatData = new JSONObject();
                chatData.put("id", nextId);
                chatData.put("sujet", subject);
                chatData.put("utilisateur_id", userId);
                chatData.put("date_creation", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                
                Log.d(TAG, "Prepared chat data: " + chatData.toString());

                // Insert chat into database
                JSONObject result = SupabaseClient.insertIntoTable("chat", chatData);
                boolean success = result != null;
                Log.d(TAG, "Chat insertion result: " + success);

                runOnUiThread(() -> {
                    showLoading(false);
                    if (success) {
                        Log.d(TAG, "Chat created successfully");
                        Toast.makeText(this, R.string.chat_created, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e(TAG, "Failed to create chat");
                        Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error creating chat: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, R.string.error_creating_chat, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showLoading(boolean isLoading) {
        Log.d(TAG, "Setting loading state: " + isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        createButton.setEnabled(!isLoading);
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