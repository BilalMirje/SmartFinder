package com.example.aipoweredsmartfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.api.ApiClient;
import com.example.aipoweredsmartfinder.models.Item;
import com.example.aipoweredsmartfinder.models.User;
import com.example.aipoweredsmartfinder.utils.SessionManager;
import com.squareup.picasso.Picasso;

public class ItemDetailActivity extends AppCompatActivity {
    private static final String TAG = "ItemDetailActivity";
    public static final String EXTRA_ITEM_ID = "item_id";

    private ImageView itemImage;
    private TextView itemTitle, itemCategory, itemLocation, itemDate, itemDescription;
    private Button btnContact, btnDelete;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private User currentUser;
    private int itemId;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Item Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize session manager and get current user
        sessionManager = SessionManager.getInstance(getApplicationContext());
        currentUser = sessionManager.getUser();

        // Check if user is logged in
        if (currentUser == null) {
            // User is not logged in, redirect to login activity
            Intent intent = new Intent(ItemDetailActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get the item ID from the intent
        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId == -1) {
            // Invalid item ID, go back to the previous activity
            Toast.makeText(this, "Invalid item ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        itemImage = findViewById(R.id.itemImage);
        itemTitle = findViewById(R.id.itemTitle);
        itemCategory = findViewById(R.id.itemCategory);
        itemLocation = findViewById(R.id.itemLocation);
        itemDate = findViewById(R.id.itemDate);
        itemDescription = findViewById(R.id.itemDescription);
        btnContact = findViewById(R.id.btnContact);
        btnDelete = findViewById(R.id.btnDelete);
        progressBar = findViewById(R.id.progressBar);

        // Load the item details
        loadItemDetails();

        // Set click listener for the contact button
        btnContact.setOnClickListener(v -> {
            // Check if this is the user's own item
            if (item != null && item.getUserId() == currentUser.getId()) {
                Toast.makeText(this, "You cannot message yourself for your own item", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Start chat activity
            Intent intent = new Intent(ItemDetailActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_ITEM_ID, itemId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Set click listener for the delete button
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteItem())
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void deleteItem() {
        showProgress(true);
        ApiClient.deleteItem(itemId, sessionManager.getToken(), new ApiClient.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showProgress(false);
                Toast.makeText(ItemDetailActivity.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                showProgress(false);
                Toast.makeText(ItemDetailActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadItemDetails() {
        showProgress(true);

        ApiClient.getItemDetails(itemId, sessionManager.getToken(), new ApiClient.ApiCallback<Item>() {
            @Override
            public void onSuccess(Item item) {
                showProgress(false);
                displayItemDetails(item);
            }

            @Override
            public void onError(String error) {
                showProgress(false);
                Toast.makeText(ItemDetailActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayItemDetails(Item item) {
        this.item = item;
        // Set the item details
        itemTitle.setText(item.getTitle());
        itemCategory.setText("Category: " + item.getCategory());
        itemLocation.setText("Location: " + item.getLocation());
        itemDate.setText("Date: " + item.getDate());
        itemDescription.setText(item.getDescription());

        // Load the item image using Picasso
        if (item.getPhotoUrl() != null && !item.getPhotoUrl().isEmpty()) {
            Picasso.get()
                    .load(item.getPhotoUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(itemImage);
        }

        // Set the toolbar title based on the item type
        if ("lost".equals(item.getType())) {
            getSupportActionBar().setTitle("Lost Item Details");
        } else {
            getSupportActionBar().setTitle("Found Item Details");
        }

        // Show/hide buttons based on ownership
        boolean isOwner = item.getUserId() == currentUser.getId();
        btnContact.setVisibility(isOwner ? View.GONE : View.VISIBLE);
        btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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