package com.example.aipoweredsmartfinder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipoweredsmartfinder.R;
import com.example.aipoweredsmartfinder.models.Conversation;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {
    private Context context;
    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationsAdapter(Context context, List<Conversation> conversations, OnConversationClickListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        
        holder.itemTitle.setText(conversation.getItemTitle());
        holder.itemType.setText(conversation.getItemType());
        holder.lastMessage.setText(conversation.getLastMessage());
        holder.lastMessageTime.setText(conversation.getLastMessageTime());
        holder.otherUserName.setText(conversation.getOtherUserName());
        
        // Show unread indicator if there are unread messages
        holder.unreadIndicator.setVisibility(conversation.hasUnreadMessages() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle;
        TextView itemType;
        TextView lastMessage;
        TextView lastMessageTime;
        TextView otherUserName;
        View unreadIndicator;

        ConversationViewHolder(View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemType = itemView.findViewById(R.id.itemType);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageTime = itemView.findViewById(R.id.lastMessageTime);
            otherUserName = itemView.findViewById(R.id.otherUserName);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
    }
} 