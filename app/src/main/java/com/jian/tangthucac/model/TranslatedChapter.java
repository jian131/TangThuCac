package com.jian.tangthucac.model;

import java.io.Serializable;

/**
 * Model đại diện cho một chương đã được dịch sang tiếng Việt
 */
public class TranslatedChapter implements Serializable {
    private String id;
    private String storyId;
    private String title;
    private String content;
    private String originalContent;
    private String translatorId;
    private String translatorName;
    private long translationDate;
    private int index;
    private int views;
    private boolean isPublished;

    // Thêm các trường cho bản dịch
    private String titleVi;
    private String contentVi;
    private String translationEngine;
    private double translationQuality;
    private long translatedTime;
    private int chapterNumber;
    private String sourceUrl;

    public TranslatedChapter() {
        // Constructor mặc định cần thiết cho Firebase
        this.translationDate = System.currentTimeMillis();
        this.views = 0;
        this.isPublished = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getTranslatorId() {
        return translatorId;
    }

    public void setTranslatorId(String translatorId) {
        this.translatorId = translatorId;
    }

    public String getTranslatorName() {
        return translatorName;
    }

    public void setTranslatorName(String translatorName) {
        this.translatorName = translatorName;
    }

    public long getTranslationDate() {
        return translationDate;
    }

    public void setTranslationDate(long translationDate) {
        this.translationDate = translationDate;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    // Các phương thức getter/setter cho các trường mới

    public String getTitleVi() {
        return titleVi;
    }

    public void setTitleVi(String titleVi) {
        this.titleVi = titleVi;
    }

    public String getContentVi() {
        return contentVi;
    }

    public void setContentVi(String contentVi) {
        this.contentVi = contentVi;
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

    public long getTranslatedTime() {
        return translatedTime;
    }

    public void setTranslatedTime(long translatedTime) {
        this.translatedTime = translatedTime;
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

    @Override
    public String toString() {
        return "TranslatedChapter{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", index=" + index +
                '}';
    }
}
