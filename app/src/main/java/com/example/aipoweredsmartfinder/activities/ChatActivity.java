package com.example.aipoweredsmartfinder.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.adapters.ChatAdapter;
import com.example.aipoweredsmartfinder.api.ApiClient;
import com.example.aipoweredsmartfinder.models.ChatMessage;
import com.example.aipoweredsmartfinder.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_ID = "item_id";
    private static final long REFRESH_INTERVAL = 5000; // 5 seconds

    private RecyclerView messagesRecyclerView;
    private TextInputLayout messageInputLayout;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private TextView itemTitle;
    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private int itemId;
    private Timer refreshTimer;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Get item ID from intent
        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId == -1) {
            Toast.makeText(this, "Invalid item ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInputLayout = findViewById(R.id.messageInputLayout);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        itemTitle = findViewById(R.id.itemTitle);

        // Set up RecyclerView
        messages = new ArrayList<>();
        adapter = new ChatAdapter(this, messages, sessionManager.getUser().getId());
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(adapter);

        // Set up click listener for send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Load initial messages
        loadMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start periodic refresh
        startMessageRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop periodic refresh
        stopMessageRefresh();
    }

    private void startMessageRefresh() {
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> loadMessages());
            }
        }, REFRESH_INTERVAL, REFRESH_INTERVAL);
    }

    private void stopMessageRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private void loadMessages() {
        ApiClient.getMessages(itemId, sessionManager.getToken(), new ApiClient.ApiCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                messages.clear();
                messages.addAll(result);
                adapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messages.size() - 1);
                
                // Update item title if we have messages
                if (!messages.isEmpty()) {
                    ChatMessage firstMessage = messages.get(0);
                    itemTitle.setText(firstMessage.getItemTitle());
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(message)) {
            messageInputLayout.setError("Please enter a message");
            return;
        }
        
        messageInputLayout.setError(null);
        messageInput.setEnabled(false);
        sendButton.setEnabled(false);

        ApiClient.sendMessage(itemId, message, sessionManager.getToken(), new ApiClient.ApiCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage result) {
                messageInput.setText("");
                messageInput.setEnabled(true);
                sendButton.setEnabled(true);
                loadMessages(); // Reload all messages
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();
                messageInput.setEnabled(true);
                sendButton.setEnabled(true);
            }
        });
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