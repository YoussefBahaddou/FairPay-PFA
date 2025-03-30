package com.emsi.fairpay_maroc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LanguageHelper {
    private static final String SELECTED_LANGUAGE = "selected_language";

    /**
     * Get the currently selected language from SharedPreferences
     */
    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        // Default to French if no language is selected
        return preferences.getString(SELECTED_LANGUAGE, "fr");
    }

    /**
     * Set the selected language in SharedPreferences
     */
    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, languageCode);
        editor.apply();
    }

    /**
     * Update the locale of the given context
     */
    public static Context updateLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // For API 24+
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            return context.createConfigurationContext(configuration);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // For API 17-23
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);
        } else {
            // For API 16 and below
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }
}
