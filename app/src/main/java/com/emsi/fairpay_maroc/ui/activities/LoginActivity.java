package com.emsi.fairpay_maroc.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.utils.LanguageHelper;
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
    private ImageButton btnLanguage;
    private boolean isLoading = false;
    private CheckBox cbRememberMe;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
        
          // Check if user is already logged in
          if (checkUserLoggedIn()) {
              // User is already logged in, navigate to MainActivity
              navigateToMainActivity();
              return;
          }
        
          setContentView(R.layout.activity_login);

          // Initialize views
          tilUsername = findViewById(R.id.til_username);
          tilPassword = findViewById(R.id.til_password);
          etUsername = findViewById(R.id.et_username);
          etPassword = findViewById(R.id.et_password);
          btnLogin = findViewById(R.id.btn_login);
          btnLanguage = findViewById(R.id.btn_language);
          cbRememberMe = findViewById(R.id.cb_remember_me);

          // Set up language button click listener
          btnLanguage.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  showLanguageSelectionDialog();
              }
          });

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
                  Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                  startActivity(intent);
              }
          });

          // Set up forgot password text click listener
          findViewById(R.id.tv_forgot_password).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                  startActivity(intent);
              }
          });
      }

    private void saveUserSession(JSONObject user) {
        try {
            boolean rememberMe = cbRememberMe.isChecked();
            
            getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                    .edit()
                    .putInt("user_id", user.getInt("id"))
                    .putString("username", user.getString("username"))
                    .putString("nom", user.getString("nom"))
                    .putString("prenom", user.getString("prenom"))
                    .putInt("role_id", user.getInt("role_id"))
                    .putBoolean("is_logged_in", true)
                    .putBoolean("remember_me", rememberMe)
                    .apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving user session: " + e.getMessage(), e);
        }
    }
      /**
     * Checks if a user is already logged in by checking SharedPreferences
     */
      private boolean checkUserLoggedIn() {
          return getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                  .getBoolean("is_logged_in", false);
      }

      /**
     * Navigates to the MainActivity
     */
      private void navigateToMainActivity() {
          Intent intent = new Intent(LoginActivity.this, MainActivity.class);
          startActivity(intent);
          finish();
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
                            runOnUiThread(() -> {
                                tilPassword.setError(getString(R.string.error_invalid_credentials));
                                isLoading = false;
                                btnLogin.setEnabled(true);
                                btnLogin.setText(R.string.login);
                            });
                        }
                    } else {
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


    private void showLanguageSelectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_language_selection, null);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_language);
        RadioButton radioFrench = dialogView.findViewById(R.id.radio_french);
        RadioButton radioEnglish = dialogView.findViewById(R.id.radio_english);
        RadioButton radioArabic = dialogView.findViewById(R.id.radio_arabic);

        // Set the current language selection
        String currentLanguage = LanguageHelper.getLanguage(this);
        if ("ar".equals(currentLanguage)) {
            radioArabic.setChecked(true);
        } else if ("en".equals(currentLanguage)) {
            radioEnglish.setChecked(true);
        } else {
            radioFrench.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    String languageCode;

                    if (selectedId == R.id.radio_arabic) {
                        languageCode = "ar";
                    } else if (selectedId == R.id.radio_english) {
                        languageCode = "en";
                    } else {
                        languageCode = "fr";
                    }

                    if (!languageCode.equals(currentLanguage)) {
                        LanguageHelper.setLanguage(this, languageCode);
                        Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_username_required));
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }

    private void handleLoginSuccess(JSONObject user) {
        try {
            // Get the user ID
            int userId = user.getInt("id");
            
            // Save login state and user ID
            SharedPreferences.Editor editor = getSharedPreferences("FairPayPrefs", MODE_PRIVATE).edit();
            editor.putBoolean("is_logged_in", true);
            editor.putInt("user_id", userId);
            editor.apply();
            
            // Navigate to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing user data: " + e.getMessage());
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }
}