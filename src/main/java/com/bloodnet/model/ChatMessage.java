package com.bloodnet.model;

import java.time.LocalDateTime;

/**
 * Chat Message Model Class for BloodNet Application
 * Represents a message in the donor-hospital communication system
 */
public class ChatMessage {
    
    private int messageId;
    private int requestId;
    private int senderId;
    private String senderType; // "donor" or "hospital"
    private String senderName;
    private String message;
    private String messageType; // "text", "image", "file"
    private boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    
    // Default constructor
    public ChatMessage() {}
    
    // Constructor for new message
    public ChatMessage(int requestId, int senderId, String senderType, 
                      String senderName, String message) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.message = message;
        this.messageType = "text";
        this.isRead = false;
        this.sentAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }
    
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
    
    public int getRequestId() {
        return requestId;
    }
    
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
    
    public int getSenderId() {
        return senderId;
    }
    
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderType() {
        return senderType;
    }
    
    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
        if (read && readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    /**
     * Get formatted time string
     * @return formatted time string
     */
    public String getFormattedTime() {
        if (sentAt == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (sentAt.toLocalDate().equals(now.toLocalDate())) {
            return sentAt.toLocalTime().toString().substring(0, 5); // HH:MM
        } else {
            return sentAt.toLocalDate().toString();
        }
    }
    
    /**
     * Check if message is from donor
     * @return true if sender is donor
     */
    public boolean isFromDonor() {
        return "donor".equals(senderType);
    }
    
    /**
     * Check if message is from hospital
     * @return true if sender is hospital
     */
    public boolean isFromHospital() {
        return "hospital".equals(senderType);
    }
    
    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId=" + messageId +
                ", requestId=" + requestId +
                ", senderType='" + senderType + '\'' +
                ", senderName='" + senderName + '\'' +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", sentAt=" + sentAt +
                '}';
    }
}
