package com.emsi.fairpay_maroc.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.utils.LanguageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "ProfileActivity";
    
    // UI components
    private TextInputEditText etFirstName, etLastName, etUsername, etEmail, etPhone;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private MaterialButton btnSaveChanges;
    private ProgressBar progressBar;
    
    // User data
    private int userId;
    private JSONObject userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Apply layout direction based on language
        applyLayoutDirection();
        
        // Initialize UI components
        initializeViews();
        
        // Set up toolbar and back button
        setupToolbar();
        
        // Load user data
        loadUserData();
        
        // Set up save button click listener
        btnSaveChanges.setOnClickListener(v -> validateAndSaveChanges());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    
    private void applyLayoutDirection() {
        String languageCode = LanguageHelper.getLanguage(this);
        if ("ar".equals(languageCode)) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    private void initializeViews() {
        // Personal information fields
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        
        // Password fields
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        
        // Button and progress bar
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());
    }
    
    private void loadUserData() {
        // Get user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("FairPayPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        
        if (userId == -1) {
            Toast.makeText(this, R.string.error_loading_profile, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        
        // Fetch user data from database
        new Thread(() -> {
            try {
                JSONArray results = SupabaseClient.queryTable(
                        "utilisateur", 
                        "id", 
                        String.valueOf(userId), 
                        "*");
                
                if (results.length() > 0) {
                    userData = results.getJSONObject(0);
                    
                    // Update UI on main thread
                    runOnUiThread(() -> {
                        try {
                            // Split full name into first and last name
                            String nom = userData.optString("nom", "");
                            String prenom = userData.optString("prenom", "");
                            
                            etFirstName.setText(prenom);
                            etLastName.setText(nom);
                            etUsername.setText(userData.optString("username", ""));
                            etEmail.setText(userData.optString("email", ""));
                            etPhone.setText(userData.optString("telephone", ""));
                            
                            progressBar.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting user data to UI: " + e.getMessage());
                            showError(R.string.error_loading_profile);
                        }
                    });
                } else {
                    showError(R.string.user_not_found);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading user data: " + e.getMessage());
                showError(R.string.error_loading_profile);
            }
        }).start();
    }
    
    private void validateAndSaveChanges() {
        // Validate personal information fields
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        // Validate password fields
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmNewPassword = etConfirmNewPassword.getText().toString();
        
        // Basic validation
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || 
            email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, R.string.all_fields_required, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Email validation
        if (!isValidEmail(email)) {
            Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Phone validation
        if (!isValidPhone(phone)) {
            Toast.makeText(this, R.string.invalid_phone, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Password validation (only if user wants to change password)
        boolean changingPassword = !currentPassword.isEmpty() || 
                                  !newPassword.isEmpty() || 
                                  !confirmNewPassword.isEmpty();
        
        if (changingPassword) {
            if (currentPassword.isEmpty()) {
                Toast.makeText(this, R.string.current_password_required, Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (newPassword.isEmpty()) {
                Toast.makeText(this, R.string.new_password_required, Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (newPassword.length() < 6) {
                Toast.makeText(this, R.string.password_too_short, Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);
        
        // Save changes in a background thread
        new Thread(() -> {
            try {
                // First, verify current password if changing password
                if (changingPassword) {
                    boolean passwordVerified = verifyCurrentPassword(currentPassword);
                    if (!passwordVerified) {
                        showError(R.string.incorrect_current_password);
                        return;
                    }
                }
                
                // Check if username is already taken (if changed)
                String currentUsername = userData.optString("username", "");
                if (!username.equals(currentUsername)) {
                    boolean usernameExists = SupabaseClient.valueExists("utilisateur", "username", username);
                    if (usernameExists) {
                        showError(R.string.username_already_taken);
                        return;
                    }
                }
                
                // Check if email is already taken (if changed)
                String currentEmail = userData.optString("email", "");
                if (!email.equals(currentEmail)) {
                    boolean emailExists = SupabaseClient.valueExists("utilisateur", "email", email);
                    if (emailExists) {
                        showError(R.string.email_already_taken);
                        return;
                    }
                }
                
                // Create JSON object with updated user data
                JSONObject updatedData = new JSONObject();
                updatedData.put("nom", lastName);
                updatedData.put("prenom", firstName);
                updatedData.put("username", username);
                updatedData.put("email", email);
                updatedData.put("telephone", phone);
                
                // Add new password if changing password
                if (changingPassword) {
                    updatedData.put("password", newPassword);
                }
                
                // Update user data in database
                updateUserData(updatedData);
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving changes: " + e.getMessage());
                showError(R.string.error_saving_changes);
            }
        }).start();
    }
    
    private boolean verifyCurrentPassword(String currentPassword) throws JSONException, IOException {
        // In a real app, you would hash the password and compare with the stored hash
        // For simplicity, we're comparing plain text passwords here
        String storedPassword = userData.optString("password", "");
        return storedPassword.equals(currentPassword);
    }
    
    private void updateUserData(JSONObject updatedData) {
        try {
            // Create the URL for the PATCH request
            String url = "utilisateur?id=eq." + userId;
            
            // Make the update request
            JSONObject response = SupabaseClient.updateRecord(url, updatedData);
            
            // Update successful
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnSaveChanges.setEnabled(true);
                Toast.makeText(this, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show();
                
                // Reload user data to reflect changes
                loadUserData();
                
                // Clear password fields
                etCurrentPassword.setText("");
                etNewPassword.setText("");
                etConfirmNewPassword.setText("");
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating user data: " + e.getMessage());
            showError(R.string.error_saving_changes);
        }
    }
    
    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.compile(emailPattern).matcher(email).matches();
    }
    
    private boolean isValidPhone(String phone) {
        // Simple validation for phone number (at least 8 digits)
        return phone.replaceAll("[^0-9]", "").length() >= 8;
    }
    
    private void showError(int messageResId) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            btnSaveChanges.setEnabled(true);
            Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
        });
    }
}
