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
    private EditText etFirstName; // Changed from etName to etFirstName
    private EditText etLastName;  // Added for last name (nom)
    private EditText etUsername;  // Added for username
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
        etFirstName = findViewById(R.id.et_first_name);  // Update ID to match layout
        etLastName = findViewById(R.id.et_last_name);    // Update ID to match layout
        etUsername = findViewById(R.id.et_username);     // Update ID to match layout
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
                // Use "utilisateur" table instead of "users"
                JSONArray result = SupabaseClient.queryTable(
                        "utilisateur",
                        "id",
                        String.valueOf(userId),
                        "id,nom,prenom,username,email,telephone,image"  // Include username
                );

                if (result.length() > 0) {
                    JSONObject user = result.getJSONObject(0);

                    runOnUiThread(() -> {
                        try {
                            // Set individual fields
                            etFirstName.setText(user.optString("prenom", ""));
                            etLastName.setText(user.optString("nom", ""));
                            etUsername.setText(user.optString("username", ""));
                            etEmail.setText(user.optString("email", ""));
                            etPhone.setText(user.optString("telephone", ""));

                            // Load profile image if available
                            currentImageUrl = user.optString("image", null);
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
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        btnSave.setEnabled(false);
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

        new Thread(() -> {
            boolean success = false;

            try {
                // Create JSON object with user data
                JSONObject userData = new JSONObject();
                userData.put("prenom", firstName);
                userData.put("nom", lastName);
                userData.put("username", username);
                userData.put("email", email);
                userData.put("telephone", phone);

                // Upload image if changed
                if (imageChanged && selectedImageUri != null) {
                    try {
                        String imageUrl = uploadImageToStorage(selectedImageUri);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            userData.put("image", imageUrl);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error uploading image: " + e.getMessage(), e);
                        // Continue with the update even if image upload fails
                    }
                }

                // Update user data in Supabase
                try {
                    // Use the updateRecord method
                    success = SupabaseClient.updateRecord("utilisateur", userId, userData);

                    Log.d(TAG, "Update result: " + success);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating user data: " + e.getMessage(), e);
                }

                final boolean finalSuccess = success;

                runOnUiThread(() -> {
                    findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    if (finalSuccess) {
                        Toast.makeText(this, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show();
                        // Update shared preferences with the full name
                        String fullName = firstName + " " + lastName;
                        SharedPreferencesManager.getInstance(this).saveUserName(fullName);

                        // Set result and finish
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, R.string.error_updating_profile, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error saving user profile: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, R.string.error_updating_profile, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String uploadImageToStorage(Uri imageUri) throws IOException, JSONException {
        if (imageUri == null) {
            Log.e(TAG, "Image URI is null");
            return null;
        }

        // Get the file extension
        String fileExtension = getFileExtension(imageUri);
        if (fileExtension == null) {
            fileExtension = "jpg"; // Default to jpg if we can't determine the extension
        }

        // Generate a unique filename using UUID
        String fileName = "profile_" + userId + "_" + UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Convert the image to bytes
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for image");
                return null;
            }

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();
            byte[] imageBytes = byteBuffer.toByteArray();

            if (imageBytes.length == 0) {
                Log.e(TAG, "Image bytes array is empty");
                return null;
            }

            // Determine content type
            String contentType = "image/jpeg"; // Default
            if (fileExtension.equalsIgnoreCase("png")) {
                contentType = "image/png";
            } else if (fileExtension.equalsIgnoreCase("gif")) {
                contentType = "image/gif";
            }

            // Use the existing uploadFile method
            String uploadedUrl = SupabaseClient.uploadFile(
                "utilisateur-images", // Use the correct bucket name with hyphen
                fileName,
                imageBytes,
                contentType
            );

            Log.d(TAG, "Uploaded image URL: " + uploadedUrl);
            return uploadedUrl;
        } catch (Exception e) {
            Log.e(TAG, "Error uploading image to storage: " + e.getMessage(), e);
            throw e;
        }
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