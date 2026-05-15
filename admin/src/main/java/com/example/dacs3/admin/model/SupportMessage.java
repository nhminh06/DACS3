package com.example.dacs3.admin.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SupportMessage {
    private String id;
    private String userId;
    private String text;
    private String senderRole; // "user" or "admin"
    private Timestamp timestamp;

    public SupportMessage() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
