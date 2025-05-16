package com.jian.tangthucac.model;

import java.io.Serializable;

/**
 * Mô hình tin nhắn cho AI chat
 */
public class Message implements Serializable {
    private String id;
    private String sender;  // "user" hoặc "ai"
    private String content;
    private long timestamp;

    public Message() {
        // Constructor rỗng cho Firebase
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String id, String sender, String content) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
