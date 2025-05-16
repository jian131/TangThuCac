package com.jian.tangthucac.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class Chapter implements Serializable {
    @DocumentId
    private String id;
    private String novelId;
    private int chapterNumber;
    private String title;
    private String originalContent;
    private String translatedContent;
    private boolean isTranslated;
    private String sourceUrl;
    private long createdAt;
    private long translatedAt;

    // Constructor mặc định cần thiết cho Firestore
    public Chapter() {
    }

    public Chapter(String id, String novelId, int chapterNumber, String title, String originalContent) {
        this.id = id;
        this.novelId = novelId;
        this.chapterNumber = chapterNumber;
        this.title = title;
        this.originalContent = originalContent;
        this.isTranslated = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor đầy đủ
    public Chapter(String id, String novelId, int chapterNumber, String title,
                  String originalContent, String translatedContent, boolean isTranslated,
                  String sourceUrl, long createdAt, long translatedAt) {
        this.id = id;
        this.novelId = novelId;
        this.chapterNumber = chapterNumber;
        this.title = title;
        this.originalContent = originalContent;
        this.translatedContent = translatedContent;
        this.isTranslated = isTranslated;
        this.sourceUrl = sourceUrl;
        this.createdAt = createdAt;
        this.translatedAt = translatedAt;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNovelId() {
        return novelId;
    }

    public void setNovelId(String novelId) {
        this.novelId = novelId;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
        if (translatedContent != null && !translatedContent.isEmpty()) {
            this.isTranslated = true;
            this.translatedAt = System.currentTimeMillis();
        }
    }

    public boolean isTranslated() {
        return isTranslated;
    }

    public void setTranslated(boolean translated) {
        isTranslated = translated;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getTranslatedAt() {
        return translatedAt;
    }

    public void setTranslatedAt(long translatedAt) {
        this.translatedAt = translatedAt;
    }

    /**
     * Lấy nội dung hiển thị của chương
     * Nếu chương đã được dịch, trả về bản dịch tiếng Việt
     * Nếu chưa, trả về bản gốc tiếng Trung
     */
    public String getDisplayContent() {
        if (isTranslated && translatedContent != null && !translatedContent.isEmpty()) {
            return translatedContent;
        }
        return originalContent;
    }

    /**
     * Tạo tên hiển thị của chương
     */
    public String getDisplayTitle() {
        return "Chương " + chapterNumber + ": " + title;
    }
}
