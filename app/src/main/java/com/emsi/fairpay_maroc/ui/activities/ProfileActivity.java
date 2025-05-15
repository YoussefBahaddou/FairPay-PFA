package com.emsi.fairpay_maroc.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.utils.SharedPreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private CircleImageView profileImage;
    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private Button btnSave;

    private Uri selectedImageUri;
    private boolean imageChanged = false;
    private int userId;
    private String currentImageUrl;

    // Activity result launcher for image selection
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        profileImage.setImageURI(selectedImageUri);
                        imageChanged = true;
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        initViews();

        // Get user ID from SharedPreferences
        userId = SharedPreferencesManager.getInstance(this).getUserId();

        // Load user data
        loadUserData();
    }

    private void initViews() {
        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Profile image
        profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> openImagePicker());

        // Form fields
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);

        // Save button
        btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        // Show loading state
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                JSONArray result = SupabaseClient.queryTable(
                        "users",
                        "id",
                        String.valueOf(userId),
                        "id,name,email,phone,profile_image"
                );

                if (result.length() > 0) {
                    JSONObject user = result.getJSONObject(0);

                    runOnUiThread(() -> {
                        try {
                            etName.setText(user.optString("name", ""));
                            etEmail.setText(user.optString("email", ""));
                            etPhone.setText(user.optString("phone", ""));

                            // Load profile image if available
                            currentImageUrl = user.optString("profile_image", null);
                            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                                // Load image using your preferred image loading library
                                // For example with Glide:
                                // Glide.with(this).load(currentImageUrl).into(profileImage);
                            }

                            findViewById(R.id.progress_bar).setVisibility(View.GONE);
                            findViewById(R.id.content_layout).setVisibility(View.VISIBLE);

                        } catch (Exception e) {
                            Log.e(TAG, "Error setting user data to UI", e);
                            showError(getString(R.string.error_loading_profile));
                        }
                    });
                } else {
                    runOnUiThread(() -> showError(getString(R.string.user_not_found)));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading user data", e);
                runOnUiThread(() -> showError(getString(R.string.error_loading_profile)));
            }
        }).start();
    }

    private void saveUserProfile() {
        // Validate inputs
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        btnSave.setEnabled(false);
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

        new Thread(() -> {
            // Declare success variable at this scope level so it's accessible in the runOnUiThread block
            boolean success = false;

            try {
                // Create JSON object with user data
                JSONObject userData = new JSONObject();
                userData.put("name", name);
                userData.put("email", email);
                userData.put("phone", phone);

                // Upload image if changed
                if (imageChanged && selectedImageUri != null) {
                    String imageUrl = uploadImageToStorage(selectedImageUri);
                    if (imageUrl != null) {
                        userData.put("profile_image", imageUrl);
                    }
                }

                // Update user data in Supabase
                JSONObject fullData = new JSONObject(userData.toString());
                try {
                    // Add the user ID to the JSON object if needed
                    fullData.put("id", userId);

                    // Call updateRecord and handle the result
                    JSONObject result = SupabaseClient.updateRecord("users", fullData);
                    success = (result != null); // Or use a more specific check based on your API response

                } catch (JSONException e) {
                    Log.e(TAG, "Error adding ID to JSON object", e);
                }

                // Final success value is used in the UI update
                final boolean finalSuccess = success;

                runOnUiThread(() -> {
                    findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    if (finalSuccess) {
                        Toast.makeText(this, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show();
                        // Update shared preferences if needed
                        SharedPreferencesManager.getInstance(this).saveUserName(name);

                        // Set result and finish
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, R.string.error_updating_profile, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error saving user profile", e);
                runOnUiThread(() -> {
                    findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, R.string.error_updating_profile, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String uploadImageToStorage(Uri imageUri) throws IOException, JSONException {
        // Get the file extension
        String fileExtension = getFileExtension(imageUri);
        if (fileExtension == null) {
            fileExtension = "jpg"; // Default to jpg if we can't determine the extension
        }

        // Generate a unique filename using UUID
        String fileName = "profile_" + UUID.randomUUID().toString() + "." + fileExtension;

        // Convert the image to bytes
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        inputStream.close();
        byte[] imageBytes = byteBuffer.toByteArray();

        // Determine content type
        String contentType = "image/jpeg"; // Default
        if (fileExtension.equalsIgnoreCase("png")) {
            contentType = "image/png";
        } else if (fileExtension.equalsIgnoreCase("gif")) {
            contentType = "image/gif";
        }

        // Upload to Supabase Storage
        return SupabaseClient.uploadFile("profiles", fileName, imageBytes, contentType);
    }

    private String getFileExtension(Uri uri) {
        String extension = null;

        try {
            String mimeType = getContentResolver().getType(uri);
            if (mimeType != null) {
                extension = mimeType.substring(mimeType.lastIndexOf("/") + 1);
            } else {
                String path = uri.getPath();
                if (path != null && path.contains(".")) {
                    extension = path.substring(path.lastIndexOf(".") + 1);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file extension", e);
        }

        return extension;
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
            findViewById(R.id.error_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.content_layout).setVisibility(View.GONE);

            // Set error message
            if (findViewById(R.id.tv_error_message) != null) {
                ((android.widget.TextView) findViewById(R.id.tv_error_message)).setText(message);
            }

            // Retry button
            if (findViewById(R.id.btn_retry) != null) {
                findViewById(R.id.btn_retry).setOnClickListener(v -> {
                    findViewById(R.id.error_layout).setVisibility(View.GONE);
                    loadUserData();
                });
            }
        });
    }
}