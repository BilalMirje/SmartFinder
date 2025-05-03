package com.example.aipoweredsmartfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.api.ApiClient;
import com.example.aipoweredsmartfinder.models.User;
import com.example.aipoweredsmartfinder.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        usernameLayout = findViewById(R.id.usernameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener for register button
        registerButton.setOnClickListener(v -> attemptRegister());

        // Set click listener for login link
        loginLink.setOnClickListener(v -> {
            finish(); // Go back to login activity
        });
    }

    private void attemptRegister() {
        Log.d(TAG, "Attempting registration");
        
        // Reset errors
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        // Get values
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        Log.d(TAG, "Registration values - Username: " + username + ", Email: " + email);

        // Validate input
        boolean cancel = false;
        View focusView = null;

        // Check for all fields
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.error_required_field));
            focusView = usernameInput;
            cancel = true;
            Log.d(TAG, "Username validation failed");
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.error_required_field));
            focusView = emailInput;
            cancel = true;
            Log.d(TAG, "Email validation failed");
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_required_field));
            focusView = passwordInput;
            cancel = true;
            Log.d(TAG, "Password validation failed");
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.error_required_field));
            focusView = confirmPasswordInput;
            cancel = true;
            Log.d(TAG, "Confirm password validation failed");
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            focusView = confirmPasswordInput;
            cancel = true;
            Log.d(TAG, "Passwords do not match");
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
            Log.d(TAG, "Registration validation failed");
        } else {
            // Show a progress spinner, and perform the registration
            Log.d(TAG, "Registration validation passed, proceeding with API call");
            showProgress(true);
            
            User user = new User(username, email, password);
            
            ApiClient.registerUser(user, new ApiClient.ApiCallback<User>() {
                @Override
                public void onSuccess(User registeredUser) {
                    Log.d(TAG, "Registration successful");
                    showProgress(false);
                    
                    // Show success message
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
                    
                    // Go to login activity
                    Log.d(TAG, "Starting LoginActivity");
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Registration error: " + error);
                    showProgress(false);
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
    }
} 