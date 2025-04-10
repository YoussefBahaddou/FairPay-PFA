package com.emsi.fairpay_maroc.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.widget.Toolbar;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.utils.LanguageHelper;
import com.google.android.material.button.MaterialButton;

public class LanguageSettingsActivity extends BaseActivity {

    private RadioGroup radioGroupLanguage;
    private RadioButton radioFrench, radioArabic, radioEnglish;
    private MaterialButton btnApplyLanguage;
    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        radioGroupLanguage = findViewById(R.id.radio_group_language);
        radioFrench = findViewById(R.id.radio_french);
        radioArabic = findViewById(R.id.radio_arabic);
        radioEnglish = findViewById(R.id.radio_english);
        btnApplyLanguage = findViewById(R.id.btn_apply_language);

        // Get current language
        currentLanguage = LanguageHelper.getLanguage(this);

        // Set the current language selection
        if ("ar".equals(currentLanguage)) {
            radioArabic.setChecked(true);
        } else if ("en".equals(currentLanguage)) {
            radioEnglish.setChecked(true);
        } else {
            radioFrench.setChecked(true);
        }

        // Set up apply button click listener
        btnApplyLanguage.setOnClickListener(v -> {
            int selectedId = radioGroupLanguage.getCheckedRadioButtonId();
            String languageCode;

            if (selectedId == R.id.radio_arabic) {
                languageCode = "ar";
            } else if (selectedId == R.id.radio_english) {
                languageCode = "en";
            } else {
                languageCode = "fr";
            }

            if (!languageCode.equals(currentLanguage)) {
                showLanguageChangeConfirmation(languageCode);
            } else {
                finish();
            }
        });
    }

    private void showLanguageChangeConfirmation(String languageCode) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.language_settings)
                .setMessage(R.string.language_change_confirmation)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    LanguageHelper.setLanguage(this, languageCode);
                    restartActivity();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}