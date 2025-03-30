package com.emsi.fairpay_maroc.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.utils.LanguageHelper;
import com.google.android.material.button.MaterialButton;

public class LanguageSettingsActivity extends BaseActivity {

    private RadioGroup radioGroupLanguage;
    private RadioButton radioFrench, radioArabic;
    private MaterialButton btnApplyLanguage;
    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        radioGroupLanguage = findViewById(R.id.radio_group_language);
        radioFrench = findViewById(R.id.radio_french);
        radioArabic = findViewById(R.id.radio_arabic);
        btnApplyLanguage = findViewById(R.id.btn_apply_language);

        // Get current language
        currentLanguage = LanguageHelper.getLanguage(this);

        // Set the correct radio button based on current language
        if (currentLanguage.equals("ar")) {
            radioArabic.setChecked(true);
        } else {
            radioFrench.setChecked(true);
        }

        // Set up apply button click listener
        btnApplyLanguage.setOnClickListener(v -> {
            String selectedLanguage;
            int selectedId = radioGroupLanguage.getCheckedRadioButtonId();

            if (selectedId == R.id.radio_arabic) {
                selectedLanguage = "ar";
            } else {
                selectedLanguage = "fr";
            }

            // Only change if the language is different
            if (!selectedLanguage.equals(currentLanguage)) {
                showLanguageChangeConfirmation(selectedLanguage);
            } else {
                finish();
            }
        });
    }

    private void showLanguageChangeConfirmation(String languageCode) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.language_changed)
                .setMessage(R.string.restart_app)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // Save the selected language
                    LanguageHelper.setLanguage(this, languageCode);

                    // Restart the app
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
