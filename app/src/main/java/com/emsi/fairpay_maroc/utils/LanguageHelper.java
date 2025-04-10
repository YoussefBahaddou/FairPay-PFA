package com.emsi.fairpay_maroc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LanguageHelper {

    public static void setLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        // Save the selected language
        SharedPreferences.Editor editor = context.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE).edit();
        editor.putString("language_code", languageCode);
        editor.apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE);
        return prefs.getString("language_code", "fr"); // Default to French
    }
    
    /**
     * Updates the locale for the given context and returns a new context with the updated locale
     * This method is used for attachBaseContext in Application class
     * 
     * @param context The context to update
     * @param language The language code to set
     * @return A new context with the updated locale
     */
    public static Context updateLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(configuration);
        } else {
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            return context;
        }
    }
}