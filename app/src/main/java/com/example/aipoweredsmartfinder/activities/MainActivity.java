package com.example.aipoweredsmartfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.models.User;
import com.example.aipoweredsmartfinder.api.ApiClient;
import com.example.aipoweredsmartfinder.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_TYPE = "item_type";
    private Button btnReportLost, btnReportFound, btnViewItems;
    private MaterialButton btnMessages;
    private TextView welcomeText;
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize session manager and get current user
        sessionManager = SessionManager.getInstance(getApplicationContext());
        currentUser = sessionManager.getUser();

        // Check if user is logged in
        if (currentUser == null) {
            // User is not logged in, redirect to login activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        btnReportLost = findViewById(R.id.btnReportLost);
        btnReportFound = findViewById(R.id.btnReportFound);
        btnViewItems = findViewById(R.id.btnViewItems);
        btnMessages = findViewById(R.id.btnMessages);

        // Set welcome message
        String welcomeMessage = "Welcome, " + currentUser.getUsername() + "!";
        welcomeText.setText(welcomeMessage);

        // Set click listeners for buttons
        btnReportLost.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ReportLostItemActivity.class)));
        btnReportFound.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ReportFoundItemActivity.class)));
        btnViewItems.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewItemsActivity.class);
            intent.putExtra(EXTRA_ITEM_TYPE, "all");
            startActivity(intent);
        });
        btnMessages.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ConversationsActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            // Log out the user
            sessionManager.logout();
            
            // Redirect to login activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_change_password) {
            showChangePasswordDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.currentPasswordInput);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String currentPassword = currentPasswordInput.getText().toString();
                String newPassword = newPasswordInput.getText().toString();
                String confirmPassword = confirmPasswordInput.getText().toString();

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(MainActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                ApiClient.changePassword(currentPassword, newPassword, sessionManager.getToken(), new ApiClient.ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });
        });

        dialog.show();
    }
} 