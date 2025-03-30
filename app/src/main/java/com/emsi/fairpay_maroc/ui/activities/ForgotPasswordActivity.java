package com.emsi.fairpay_maroc.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.emsi.fairpay_maroc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends BaseActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnSendResetEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        tilEmail = findViewById(R.id.til_email);
        etEmail = findViewById(R.id.et_email);
        btnSendResetEmail = findViewById(R.id.btn_send_reset_email);

        // Set up send reset email button click listener
        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEmail()) {
                    // In a real app, you would send a reset email
                    // For now, just show a success dialog
                    showResetEmailSentDialog();
                }
            }
        });

        // Set up login text click listener
        findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just finish this activity to go back to login
                finish();
            }
        });
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();

        // Validate email
        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_email_required));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            return false;
        } else {
            tilEmail.setError(null);
            return true;
        }
    }

    private void showResetEmailSentDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_email_sent)
                .setMessage(R.string.reset_email_sent_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // Go back to login screen
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
