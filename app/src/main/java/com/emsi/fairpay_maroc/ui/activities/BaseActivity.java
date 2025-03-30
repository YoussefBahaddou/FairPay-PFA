package com.emsi.fairpay_maroc.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.emsi.fairpay_maroc.utils.LanguageHelper;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply the saved language to the context
        String languageCode = LanguageHelper.getLanguage(newBase);
        super.attachBaseContext(LanguageHelper.updateLocale(newBase, languageCode));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Apply RTL layout for Arabic
        String languageCode = LanguageHelper.getLanguage(this);
        if ("ar".equals(languageCode)) {
            getWindow().getDecorView().setLayoutDirection(android.view.View.LAYOUT_DIRECTION_RTL);
        } else {
            getWindow().getDecorView().setLayoutDirection(android.view.View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Apply the saved language when configuration changes
        String languageCode = LanguageHelper.getLanguage(this);
        LanguageHelper.updateLocale(this, languageCode);
    }

    protected void logout() {
        // Clear user session data
        // For example: SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
        // editor.clear();
        // editor.apply();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
