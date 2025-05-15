package com.jian.tangthucac.model;

import java.io.Serializable;

/**
 * Model lưu trữ thông tin chương đã dịch
 */
public class TranslatedChapter implements Serializable {
    private String id;                // ID chương
    private int chapterNumber;        // Số thứ tự chương
    private String title;             // Tiêu đề gốc
    private String titleVi;           // Tiêu đề đã dịch
    private String content;           // Nội dung gốc
    private String contentVi;         // Nội dung đã dịch
    private String sourceUrl;         // URL chương gốc
    private long translatedTime;      // Thời gian dịch
    private String translationEngine; // Công cụ dịch sử dụng (Claude, DeepL, etc.)
    private double translationQuality;// Chất lượng dịch (0.0-1.0)
    private int viewCount;            // Số lượt xem
    private long lastReadTime;        // Thời gian đọc gần nhất

    public TranslatedChapter() {
        // Required empty constructor for Firebase
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTitleVi() {
        return titleVi;
    }

    public void setTitleVi(String titleVi) {
        this.titleVi = titleVi;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentVi() {
        return contentVi;
    }

    public void setContentVi(String contentVi) {
        this.contentVi = contentVi;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public long getTranslatedTime() {
        return translatedTime;
    }

    public void setTranslatedTime(long translatedTime) {
        this.translatedTime = translatedTime;
    }

    public String getTranslationEngine() {
        return translationEngine;
    }

    public void setTranslationEngine(String translationEngine) {
        this.translationEngine = translationEngine;
    }

    public double getTranslationQuality() {
        return translationQuality;
    }

    public void setTranslationQuality(double translationQuality) {
        this.translationQuality = translationQuality;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }
}
