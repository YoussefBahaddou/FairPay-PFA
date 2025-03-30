package com.emsi.fairpay_maroc.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emsi.fairpay_maroc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends BaseActivity {

    private TextInputLayout tilFullName, tilEmail, tilUsername, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etUsername, etPassword, etConfirmPassword;
    private MaterialButton btnSignup;

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
                if (validateInputs()) {
                    // In a real app, you would register the user with a server
                    // For now, just show a success message and navigate to login
                    Toast.makeText(SignupActivity.this,
                            getString(R.string.signup_success),
                            Toast.LENGTH_SHORT).show();

                    // Navigate to login screen
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
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

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnSignup = findViewById(R.id.btn_signup);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

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

        return isValid;
    }
}
