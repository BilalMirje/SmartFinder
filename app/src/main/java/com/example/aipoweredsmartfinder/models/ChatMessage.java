package com.example.aipoweredsmartfinder.models;

public class ChatMessage {
    private int id;
    private int itemId;
    private int senderId;
    private int receiverId;
    private String message;
    private boolean isRead;
    private String createdAt;
    private String senderName;
    private String receiverName;
    private String itemTitle;
    private String itemType;

    public ChatMessage(int id, int itemId, int senderId, int receiverId, String message, 
                      boolean isRead, String createdAt, String senderName, String receiverName,
                      String itemTitle, String itemType) {
        this.id = id;
        this.itemId = itemId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.itemTitle = itemTitle;
        this.itemType = itemType;
    }

    public int getId() {
        return id;
    }

    public int getItemId() {
        return itemId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemType() {
        return itemType;
    }
} 