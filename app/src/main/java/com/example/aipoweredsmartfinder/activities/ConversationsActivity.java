package com.example.aipoweredsmartfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.adapters.ConversationsAdapter;
import com.example.aipoweredsmartfinder.api.ApiClient;
import com.example.aipoweredsmartfinder.models.Conversation;
import com.example.aipoweredsmartfinder.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ConversationsActivity extends AppCompatActivity {
    private RecyclerView conversationsRecyclerView;
    private ProgressBar progressBar;
    private ConversationsAdapter adapter;
    private List<Conversation> conversations;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        conversationsRecyclerView = findViewById(R.id.conversationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize session manager
        sessionManager = SessionManager.getInstance(this);

        // Set up RecyclerView
        conversations = new ArrayList<>();
        adapter = new ConversationsAdapter(this, conversations, conversation -> {
            // Open chat activity when a conversation is clicked
            Intent intent = new Intent(ConversationsActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_ITEM_ID, conversation.getItemId());
            startActivity(intent);
        });
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationsRecyclerView.setAdapter(adapter);

        // Load conversations
        loadConversations();
    }

    private void loadConversations() {
        showProgress(true);
        ApiClient.getConversations(sessionManager.getToken(), new ApiClient.ApiCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> result) {
                showProgress(false);
                conversations.clear();
                conversations.addAll(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                showProgress(false);
                Toast.makeText(ConversationsActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations(); // Refresh conversations when returning to this activity
    }
} 