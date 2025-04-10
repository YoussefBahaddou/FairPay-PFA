package com.emsi.fairpay_maroc;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.utils.LanguageHelper;

import com.emsi.fairpay_maroc.utils.LanguageHelper;

public class FairPayApplication extends Application {
    
    @Override
    protected void attachBaseContext(Context base) {
        // Get the saved language code
        SharedPreferences prefs = base.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE);
        String languageCode = prefs.getString("language_code", "en"); // Default to French
        
        // Apply the language
        super.attachBaseContext(LanguageHelper.updateLocale(base, languageCode));
    }
          @Override
          public void onCreate() {
              super.onCreate();
        
              // Initialize language at app startup
              String languageCode = LanguageHelper.getLanguage(this);
              LanguageHelper.setLanguage(this, languageCode);
        
              // Initialize OkHttp client for Supabase
              try {
                  // Pre-initialize the HTTP client to avoid delay on first API call
                  SupabaseClient.getHttpClient();
              } catch (Exception e) {
                  Log.e("FairPayApplication", "Error initializing HTTP client: " + e.getMessage());
              }
        
              // Set default uncaught exception handler for crash reporting
              Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                  Log.e("FairPayApplication", "Uncaught exception", throwable);
                  // In a production app, you might want to send this to a crash reporting service
              });
        
              // Initialize SharedPreferences with default values if needed
              SharedPreferences prefs = getSharedPreferences("FairPayPrefs", MODE_PRIVATE);
              if (!prefs.contains("first_launch")) {
                  SharedPreferences.Editor editor = prefs.edit();
                  editor.putBoolean("first_launch", false);
                  editor.putBoolean("notifications_enabled", true);
                  editor.apply();
              }
        
              // Register activity lifecycle callbacks for app-wide monitoring if needed
              registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                  @Override
                  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                      // Apply app theme or other configurations to all activities
                  }

                  @Override
                  public void onActivityStarted(Activity activity) {}

                  @Override
                  public void onActivityResumed(Activity activity) {}

                  @Override
                  public void onActivityPaused(Activity activity) {}

                  @Override
                  public void onActivityStopped(Activity activity) {}

                  @Override
                  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

                  @Override
                  public void onActivityDestroyed(Activity activity) {}
              });
          }
      }