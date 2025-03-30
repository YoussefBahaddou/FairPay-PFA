package com.emsi.fairpay_maroc.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.utils.LanguageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends BaseActivity {

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private ImageButton btnLanguage;

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
        btnLanguage = findViewById(R.id.btn_language);

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
                if (validateInputs()) {
                    // For now, just navigate to MainActivity
                    // In a real app, you would authenticate with a server
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
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

    private void showLanguageSelectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_language_selection, null);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_language);
        RadioButton radioFrench = dialogView.findViewById(R.id.radio_french);
        RadioButton radioArabic = dialogView.findViewById(R.id.radio_arabic);

        // Set the current language selection
        String currentLanguage = LanguageHelper.getLanguage(this);
        if ("ar".equals(currentLanguage)) {
            radioArabic.setChecked(true);
        } else {
            radioFrench.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    // Get selected language
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    String languageCode;

                    if (selectedId == R.id.radio_arabic) {
                        languageCode = "ar";
                    } else {
                        languageCode = "fr";
                    }

                    // If language changed, update it and restart activity
                    if (!languageCode.equals(currentLanguage)) {
                        LanguageHelper.setLanguage(this, languageCode);

                        // Show toast message
                        Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show();

                        // Restart activity to apply language change
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
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }
}
