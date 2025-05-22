package com.emsi.fairpay_maroc.ui.activities;

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
import com.emsi.fairpay_maroc.adapters.MessageAdapter;
import com.emsi.fairpay_maroc.models.Message;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIPricingActivity extends AppCompatActivity {
    private static final String TAG = "AIPricingActivity";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String API_KEY = "AIzaSyAKxsguQPheD6QMFMl-T_I0YLnhJ1Af2TM";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private RecyclerView messagesRecyclerView;
    private TextInputLayout messageInputLayout;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private MaterialButton clearButton;
    private ProgressBar progressBar;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private OkHttpClient client;
    private String userLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_pricing);

        // Get user's language
        userLanguage = Locale.getDefault().getLanguage();

        // Initialize OkHttpClient
        client = new OkHttpClient();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.ai_pricing);
        }

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInputLayout = findViewById(R.id.message_input_layout);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        clearButton = findViewById(R.id.clear_button);
        progressBar = findViewById(R.id.progress_bar);

        // Setup RecyclerView
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Set click listeners
        sendButton.setOnClickListener(v -> sendMessage());
        clearButton.setOnClickListener(v -> clearMessages());
    }

    private void sendMessage() {
        String userMessage = messageInput.getText().toString().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        // Add user message to the list
        messages.add(new Message(userMessage, true));
        messageAdapter.notifyItemInserted(messages.size() - 1);
        messagesRecyclerView.scrollToPosition(messages.size() - 1);
        messageInput.setText("");

        // Show loading
        showLoading(true);

        // Construct the prompt
        String prompt = "You are a pricing assistant for Morocco. " +
                "IMPORTANT: Always respond in the same language that the user used to ask the question. " +
                "Only answer questions about prices in Morocco. " +
                "For each price, provide ONE definitive number that represents the most common/average price. " +
                "Do not give price ranges or approximations. " +
                "If you don't know the exact price, provide the most likely price based on current market data. " +
                "Keep your answers brief and to the point. " +
                "If the question is not about prices in Morocco, respond with: " +
                getString(R.string.ai_pricing_error) + "\n\n" +
                "User question: " + userMessage;

        // Make API request in background
        new Thread(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                JSONObject contents = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", prompt);
                parts.put(part);
                contents.put("parts", parts);
                requestBody.put("contents", contents);

                Request request = new Request.Builder()
                    .url(GEMINI_API_URL + "?key=" + API_KEY)
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject candidate = candidates.getJSONObject(0);
                        JSONObject content = candidate.getJSONObject("content");
                        JSONArray responseParts = content.getJSONArray("parts");
                        if (responseParts.length() > 0) {
                            String aiResponse = responseParts.getJSONObject(0).getString("text");
                            runOnUiThread(() -> {
                                messages.add(new Message(aiResponse, false));
                                messageAdapter.notifyItemInserted(messages.size() - 1);
                                messagesRecyclerView.scrollToPosition(messages.size() - 1);
                                showLoading(false);
                            });
                            return;
                        }
                    }
                    throw new IOException("Invalid response format");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting AI response", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void clearMessages() {
        messages.clear();
        messageAdapter.notifyDataSetChanged();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        sendButton.setEnabled(!show);
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