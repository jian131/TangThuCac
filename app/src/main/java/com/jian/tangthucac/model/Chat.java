package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mô hình cuộc trò chuyện AI chat
 */
public class Chat implements Serializable {
    private String id;
    private String title;
    private long createdAt;
    private List<Message> messages;

    public Chat() {
        // Constructor rỗng cho Firebase
        this.createdAt = System.currentTimeMillis();
        this.messages = new ArrayList<>();
    }

    public Chat(String id, String title) {
        this.id = id;
        this.title = title;
        this.createdAt = System.currentTimeMillis();
        this.messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Thêm một tin nhắn vào cuộc trò chuyện
     * @param message Tin nhắn cần thêm
     */
    public void addMessage(Message message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
    }

    /**
     * Tạo tiêu đề cho cuộc trò chuyện dựa trên tin nhắn đầu tiên
     * @return true nếu đã cập nhật tiêu đề, false nếu không có tin nhắn
     */
    public boolean generateTitleFromFirstMessage() {
        if (messages != null && !messages.isEmpty()) {
            Message firstMessage = messages.get(0);
            String content = firstMessage.getContent();
            if (content.length() > 30) {
                content = content.substring(0, 30) + "...";
            }
            this.title = content;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", messages=" + (messages != null ? messages.size() : 0) +
                '}';
    }
}
