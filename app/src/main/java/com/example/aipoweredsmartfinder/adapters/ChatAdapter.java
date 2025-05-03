package com.example.aipoweredsmartfinder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.models.ChatMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<ChatMessage> messages;
    private int currentUserId;
    private SimpleDateFormat serverFormat;
    private SimpleDateFormat displayFormat;

    public ChatAdapter(Context context, List<ChatMessage> messages, int currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.displayFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageText.setText(message.getMessage());
        
        try {
            Date date = serverFormat.parse(message.getCreatedAt());
            String formattedDate = displayFormat.format(date);
            if (holder.viewType == VIEW_TYPE_SENT) {
                holder.messageTime.setText(formattedDate);
            } else {
                holder.timeText.setText(formattedDate);
            }
        } catch (ParseException e) {
            if (holder.viewType == VIEW_TYPE_SENT) {
                holder.messageTime.setText(message.getCreatedAt());
            } else {
                holder.timeText.setText(message.getCreatedAt());
            }
        }

        if (holder.viewType == VIEW_TYPE_RECEIVED) {
            holder.senderName.setText(message.getSenderName());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.getSenderId() == currentUserId) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;
        TextView messageTime;
        TextView senderName;
        int viewType;

        MessageViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            messageText = itemView.findViewById(R.id.messageText);
            
            if (viewType == VIEW_TYPE_SENT) {
                messageTime = itemView.findViewById(R.id.messageTime);
            } else {
                timeText = itemView.findViewById(R.id.timeText);
                senderName = itemView.findViewById(R.id.senderName);
            }
        }
    }
} 