package com.emsi.fairpay_maroc.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private TextInputLayout tilFullName, tilEmail, tilUsername, tilPassword, tilConfirmPassword, tilPhone;
    private TextInputEditText etFullName, etEmail, etUsername, etPassword, etConfirmPassword, etPhone;
    private MaterialButton btnSignup;
    private boolean isLoading = false;

    // Default role ID for regular users (adjust according to your database)
    private static final int DEFAULT_USER_ROLE_ID = 2; // Assuming 1 is admin, 2 is regular user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        initViews();

        // Set up signup button click listener
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs() && !isLoading) {
                    registerUser();
                }
            }
        });

        // Set up login text click listener
        findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to login screen
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initViews() {
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilPhone = findViewById(R.id.til_phone);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhone = findViewById(R.id.et_phone);

        btnSignup = findViewById(R.id.btn_signup);
    }

    private void registerUser() {
        isLoading = true;
        btnSignup.setEnabled(false);
        btnSignup.setText(R.string.registering);

        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        final String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Split full name into first and last name
        String[] nameParts = fullName.split(" ", 2);
        final String firstName = nameParts[0];
        final String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Create user data JSON object
        JSONObject userData = new JSONObject();
        try {
            // Generate a random UUID for the ID
            String uuid = UUID.randomUUID().toString();
            userData.put("id", uuid);
            userData.put("nom", lastName);
            userData.put("prenom", firstName);
            userData.put("username", username);
            userData.put("password", password); // In a real app, hash this password
            userData.put("telephone", phone);
            userData.put("email", email);
            userData.put("role_id", DEFAULT_USER_ROLE_ID);

            // Log the data we're sending
            Log.d(TAG, "User data to insert: " + userData.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user data: " + e.getMessage(), e);
            showError("Error creating user data: " + e.getMessage());
            return;
        }

        // Check if username or email already exists
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // First check if the role exists
                    boolean roleExists = SupabaseClient.roleExists(DEFAULT_USER_ROLE_ID);
                    if (!roleExists) {
                        showError("Error: User role does not exist in the database. Please contact support.");
                        return;
                    }

                    // Check if username exists
                    boolean usernameExists = SupabaseClient.valueExists("utilisateur", "username", username);
                    if (usernameExists) {
                        runOnUiThread(() -> {
                            tilUsername.setError(getString(R.string.error_username_taken));
                            isLoading = false;
                            btnSignup.setEnabled(true);
                            btnSignup.setText(R.string.sign_up);
                        });
                        return;
                    }

                    // Check if email exists
                    boolean emailExists = SupabaseClient.valueExists("utilisateur", "email", email);
                    if (emailExists) {
                        runOnUiThread(() -> {
                            tilEmail.setError(getString(R.string.error_email_taken));
                            isLoading = false;
                            btnSignup.setEnabled(true);
                            btnSignup.setText(R.string.sign_up);
                        });
                        return;
                    }

                    // Get the next available ID
                    final int nextId = SupabaseClient.getNextId("utilisateur");
                    userData.put("id", nextId);

                    // Insert the new user
                    JSONObject result = SupabaseClient.insertIntoTable("utilisateur", userData);
                    Log.d(TAG, "User registered successfully: " + result.toString());

                    runOnUiThread(() -> {
                        Toast.makeText(SignupActivity.this,
                                getString(R.string.signup_success),
                                Toast.LENGTH_SHORT).show();

                        try {
                            // Save user session
                            getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putInt("user_id", nextId)
                                    .putString("username", username)
                                    .putString("nom", lastName)
                                    .putString("prenom", firstName)
                                    .putInt("role_id", DEFAULT_USER_ROLE_ID)
                                    .putBoolean("is_logged_in", true)
                                    .apply();

                            // Navigate to MainActivity instead of LoginActivity
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving user session: " + e.getMessage(), e);
                            // If there's an error, fall back to LoginActivity
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error during registration: " + e.getMessage(), e);
                    showError("Error during registration: " + e.getMessage());
                }
            }
        }).start();
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
            isLoading = false;
            btnSignup.setEnabled(true);
            btnSignup.setText(R.string.sign_up);
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate full name
        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_name_required));
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        // Validate email
        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_email_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Validate username
        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_username_required));
            isValid = false;
        } else if (username.length() < 4) {
            tilUsername.setError(getString(R.string.error_username_too_short));
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        // Validate password
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.error_confirm_password_required));
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        // Validate phone number
        if (phone.isEmpty()) {
            tilPhone.setError(getString(R.string.error_phone_required));
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        return isValid;
    }
}