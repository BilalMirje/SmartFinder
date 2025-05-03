package com.example.aipoweredsmartfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_TIME = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Handler to delay the start of the next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if user is already logged in
            SessionManager sessionManager = SessionManager.getInstance(getApplicationContext());
            
            Intent intent;
            if (sessionManager.isLoggedIn()) {
                // User is already logged in, go to main activity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // User is not logged in, go to login activity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            
            startActivity(intent);
            finish();
        }, SPLASH_DISPLAY_TIME);
    }
} 