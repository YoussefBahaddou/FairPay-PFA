package com.emsi.fairpay_maroc.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.emsi.fairpay_maroc.R;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds
    private ImageView logoImageView;
    private TextView appNameTextView;
    private TextView sloganTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        logoImageView = findViewById(R.id.splash_logo);
        appNameTextView = findViewById(R.id.splash_app_name);
        sloganTextView = findViewById(R.id.splash_slogan);

        // Set initial alpha to 0 (invisible)
        logoImageView.setAlpha(0f);
        appNameTextView.setAlpha(0f);
        sloganTextView.setAlpha(0f);

        // Start animations after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(this::startAnimations, 100);
    }

    private void startAnimations() {
        // Logo animation: scale up and fade in
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 0.5f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 0.5f, 1f);
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 0f, 1f);

        AnimatorSet logoAnimSet = new AnimatorSet();
        logoAnimSet.playTogether(logoScaleX, logoScaleY, logoFadeIn);
        logoAnimSet.setDuration(800);
        logoAnimSet.setInterpolator(new DecelerateInterpolator());

        // App name animation: slide up and fade in
        ObjectAnimator nameSlideUp = ObjectAnimator.ofFloat(appNameTextView, View.TRANSLATION_Y, 50f, 0f);
        ObjectAnimator nameFadeIn = ObjectAnimator.ofFloat(appNameTextView, View.ALPHA, 0f, 1f);

        AnimatorSet nameAnimSet = new AnimatorSet();
        nameAnimSet.playTogether(nameSlideUp, nameFadeIn);
        nameAnimSet.setDuration(600);
        nameAnimSet.setStartDelay(400);
        nameAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());

        // Slogan animation: slide up and fade in
        ObjectAnimator sloganSlideUp = ObjectAnimator.ofFloat(sloganTextView, View.TRANSLATION_Y, 50f, 0f);
        ObjectAnimator sloganFadeIn = ObjectAnimator.ofFloat(sloganTextView, View.ALPHA, 0f, 1f);

        AnimatorSet sloganAnimSet = new AnimatorSet();
        sloganAnimSet.playTogether(sloganSlideUp, sloganFadeIn);
        sloganAnimSet.setDuration(600);
        sloganAnimSet.setStartDelay(600);
        sloganAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());

        // Play all animations together
        AnimatorSet fullAnimSet = new AnimatorSet();
        fullAnimSet.playTogether(logoAnimSet, nameAnimSet, sloganAnimSet);

        // Add listener to navigate to login screen when animations complete
        fullAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Wait a moment before transitioning to login screen
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Create fade out animation for the entire layout
                    View rootView = findViewById(R.id.splash_root);
                    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(rootView, View.ALPHA, 1f, 0f);
                    fadeOut.setDuration(400);
                    fadeOut.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // Navigate to login screen
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);

                            // Apply custom transition animation
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                            // Finish splash activity
                            finish();
                        }
                    });
                    fadeOut.start();
                }, 800); // Short delay before transitioning
            }
        });

        // Start the animations
        fullAnimSet.start();
    }
}
