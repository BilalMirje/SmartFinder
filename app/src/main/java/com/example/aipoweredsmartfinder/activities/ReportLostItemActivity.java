package com.example.aipoweredsmartfinder.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.api.ApiClient;
import com.example.aipoweredsmartfinder.models.Item;
import com.example.aipoweredsmartfinder.models.User;
import com.example.aipoweredsmartfinder.utils.ImageUtils;
import com.example.aipoweredsmartfinder.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReportLostItemActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY = 2;

    private TextInputLayout titleLayout, descriptionLayout, locationLayout, dateLayout;
    private TextInputEditText titleInput, descriptionInput, locationInput, dateInput;
    private Spinner categorySpinner;
    private ImageView itemPhoto;
    private Button btnAddPhoto, btnSubmit;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private User currentUser;
    private Uri photoUri;
    private String currentPhotoPath;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_item);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Report Lost Item");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize session manager and get current user
        sessionManager = SessionManager.getInstance(getApplicationContext());
        currentUser = sessionManager.getUser();

        // Check if user is logged in
        if (currentUser == null) {
            // User is not logged in, redirect to login activity
            Intent intent = new Intent(ReportLostItemActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        titleLayout = findViewById(R.id.titleLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        locationLayout = findViewById(R.id.locationLayout);
        dateLayout = findViewById(R.id.dateLayout);
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        locationInput = findViewById(R.id.locationInput);
        dateInput = findViewById(R.id.dateInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        itemPhoto = findViewById(R.id.itemPhoto);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // Set up category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.item_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set up date input
        calendar = Calendar.getInstance();
        dateInput.setOnClickListener(v -> showDatePicker());
        
        // Set current date
        updateDateDisplay();

        // Set up photo button
        btnAddPhoto.setOnClickListener(v -> showImagePickerDialog());

        // Set up submit button
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        dateInput.setText(sdf.format(calendar.getTime()));
    }

    private void showImagePickerDialog() {
        // Create an intent to open the gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    private void validateAndSubmit() {
        // Reset errors
        titleLayout.setError(null);
        descriptionLayout.setError(null);
        locationLayout.setError(null);
        dateLayout.setError(null);

        // Get values
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        // Validate input
        boolean cancel = false;
        View focusView = null;

        // Check for required fields
        if (TextUtils.isEmpty(title)) {
            titleLayout.setError(getString(R.string.error_required_field));
            focusView = titleInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionLayout.setError(getString(R.string.error_required_field));
            focusView = descriptionInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(location)) {
            locationLayout.setError(getString(R.string.error_required_field));
            focusView = locationInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(date)) {
            dateLayout.setError(getString(R.string.error_required_field));
            focusView = dateInput;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and perform the submission
            showProgress(true);

            // Create a new item
            Item item = new Item(
                    title,
                    description,
                    location,
                    date,
                    category,
                    "", // Photo URL will be set by server
                    "lost", // This is a lost item
                    currentUser.getId()
            );

            // Convert the selected image to base64 if it exists
            String imageBase64 = "";
            if (photoUri != null) {
                imageBase64 = ImageUtils.uriToBase64(this, photoUri);
            }

            // Submit the item to the server
            ApiClient.addItem(item, sessionManager.getToken(), imageBase64, new ApiClient.ApiCallback<Item>() {
                @Override
                public void onSuccess(Item result) {
                    showProgress(false);
                    Toast.makeText(ReportLostItemActivity.this, getString(R.string.success_report), Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    showProgress(false);
                    Toast.makeText(ReportLostItemActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null) {
                photoUri = data.getData();
                itemPhoto.setImageURI(photoUri);
                itemPhoto.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                itemPhoto.setImageURI(photoUri);
                itemPhoto.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 