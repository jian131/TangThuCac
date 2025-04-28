
package com.jian.tangthucac.model;

public class Message {
    private String content;
    private String sender; // "user" hoặc "bot"

    public Message(String content, String sender) {
        this.content = content;
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    public boolean isUser() {
        return "user".equals(sender);
    }
}
