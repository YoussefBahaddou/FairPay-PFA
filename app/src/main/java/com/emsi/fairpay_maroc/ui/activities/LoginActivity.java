package com.emsi.fairpay_maroc.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        // Set up login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs() && !isLoading) {
                    loginUser();
                }
            }
        });

        // Set up register text click listener
        findViewById(R.id.tv_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignupActivity
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // Set up forgot password text click listener
        findViewById(R.id.tv_forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Navigate to forgot password screen
                Toast.makeText(LoginActivity.this, "Forgot password functionality coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser() {
        isLoading = true;
        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.logging_in);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Query the database for the user
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Query user by username
                    JSONArray users = SupabaseClient.queryTable(
                            "utilisateur", 
                            "username", 
                            username, 
                            "id,nom,prenom,username,password,email,telephone,role_id"
                    );
                    
                    if (users.length() > 0) {
                        JSONObject user = users.getJSONObject(0);
                        String storedPassword = user.getString("password");
                        
                        // In a real app, you should use proper password hashing
                        if (password.equals(storedPassword)) {
                            // Login successful
                            // Save user info to shared preferences
                            saveUserSession(user);
                            
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, 
                                        "Login successful!", 
                                        Toast.LENGTH_SHORT).show();
                                
                                // Navigate to MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            // Password doesn't match
                            runOnUiThread(() -> {
                                tilPassword.setError(getString(R.string.error_invalid_credentials));
                                isLoading = false;
                                btnLogin.setEnabled(true);
                                btnLogin.setText(R.string.login);
                            });
                        }
                    } else {
                        // User not found
                        runOnUiThread(() -> {
                            tilUsername.setError(getString(R.string.error_invalid_credentials));
                            isLoading = false;
                            btnLogin.setEnabled(true);
                            btnLogin.setText(R.string.login);
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error during login: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, 
                                "Error: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        isLoading = false;
                        btnLogin.setEnabled(true);
                        btnLogin.setText(R.string.login);
                    });
                }
            }
        }).start();
    }

    private void saveUserSession(JSONObject user) {
        try {
            // Save user data to SharedPreferences
            getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                    .edit()
                    .putInt("user_id", user.getInt("id"))
                    .putString("username", user.getString("username"))
                    .putString("nom", user.getString("nom"))
                    .putString("prenom", user.getString("prenom"))
                    .putInt("role_id", user.getInt("role_id"))
                    .putBoolean("is_logged_in", true)
                    .apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving user session: " + e.getMessage(), e);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate username
        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_username_required));
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        // Validate password
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }
}
