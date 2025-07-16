package com.distributed.chat;

import org.json.JSONObject;

public class ChatMessage {
    private String sender;
    private String content;
    private long timestamp;
    private String originServerId;
    
    public ChatMessage(String sender, String content, String originServerId) {
        this.sender = sender;
        this.content = content;
        this.originServerId = originServerId;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Constructor for deserialization
    public ChatMessage(String sender, String content, long timestamp, String originServerId) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.originServerId = originServerId;
    }
    
    // Convert to JSON string for transmission
    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("sender", sender);
        json.put("content", content);
        json.put("timestamp", timestamp);
        json.put("originServerId", originServerId);
        return json.toString();
    }
    
    // Create from JSON string
    public static ChatMessage fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        return new ChatMessage(
            json.getString("sender"),
            json.getString("content"),
            json.getLong("timestamp"),
            json.getString("originServerId")
        );
    }
    
    // Getters
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public String getOriginServerId() { return originServerId; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", originServerId, sender, content);
    }
}