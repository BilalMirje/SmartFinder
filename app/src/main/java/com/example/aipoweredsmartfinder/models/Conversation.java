package com.example.aipoweredsmartfinder.models;

public class Conversation {
    private int itemId;
    private String itemTitle;
    private String itemType;
    private String lastMessage;
    private String lastMessageTime;
    private String otherUserName;
    private boolean hasUnreadMessages;

    public Conversation(int itemId, String itemTitle, String itemType, String lastMessage, 
                       String lastMessageTime, String otherUserName, boolean hasUnreadMessages) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.itemType = itemType;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.otherUserName = otherUserName;
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemType() {
        return itemType;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }
} 