package com.jian.tangthucac.model;

import java.io.Serializable;

/**
 * Model đại diện cho một chương truyện Trung Quốc
 */
public class ChineseChapter implements Serializable {
    private String id;              // ID của chương
    private String novelId;         // ID của truyện
    private String title;           // Tiêu đề gốc tiếng Trung
    private String titleVi;         // Tiêu đề đã dịch sang tiếng Việt
    private String content;         // Nội dung gốc tiếng Trung
    private String contentVi;       // Nội dung đã dịch sang tiếng Việt
    private int chapterNumber;      // Số thứ tự chương
    private String sourceUrl;       // URL nguồn của chương
    private long updateTime;        // Thời gian cập nhật
    private boolean translated;     // Đã được dịch chưa
    private String translatorId;    // ID của người dịch (nếu có)
    private String translationEngine; // Công cụ dịch được sử dụng
    private int views;              // Số lượt xem
    private double translationQuality; // Chất lượng bản dịch (0-5)

    public ChineseChapter() {
        // Constructor mặc định cho Firebase
        this.updateTime = System.currentTimeMillis();
        this.views = 0;
        this.translated = false;
        this.translationQuality = 0;
    }

    // Constructor đầy đủ
    public ChineseChapter(String id, String novelId, String title, int chapterNumber) {
        this();
        this.id = id;
        this.novelId = novelId;
        this.title = title;
        this.chapterNumber = chapterNumber;
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

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isTranslated() {
        return translated;
    }

    public void setTranslated(boolean translated) {
        this.translated = translated;
    }

    public String getTranslatorId() {
        return translatorId;
    }

    public void setTranslatorId(String translatorId) {
        this.translatorId = translatorId;
    }

    public String getTranslationEngine() {
        return translationEngine;
    }

    public void setTranslationEngine(String translationEngine) {
        this.translationEngine = translationEngine;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public double getTranslationQuality() {
        return translationQuality;
    }

    public void setTranslationQuality(double translationQuality) {
        this.translationQuality = translationQuality;
    }

    // Phương thức tiện ích
    public void incrementViews() {
        this.views++;
    }

    public void translate(String contentVi, String titleVi, String translationEngine) {
        this.contentVi = contentVi;
        this.titleVi = titleVi;
        this.translationEngine = translationEngine;
        this.translated = true;
        this.updateTime = System.currentTimeMillis();
    }

    public void updateTimestamp() {
        this.updateTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "ChineseChapter{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", chapterNumber=" + chapterNumber +
                ", translated=" + translated +
                '}';
    }
}
