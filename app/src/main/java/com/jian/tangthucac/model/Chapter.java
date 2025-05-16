package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Mô hình dữ liệu Chapter đại diện cho một chương truyện
 * Đóng vai trò như một adapter chuyển tiếp đến ChineseChapter
 */
public class Chapter implements Serializable {
    private String id;
    private String storyId;
    private int chapterNumber;
    private String title;
    private String content;
    private String translatedContent;
    private boolean hasTranslation;
    private Date lastUpdated;
    private boolean isDownloaded;

    // Constructor mặc định cần thiết cho Firebase
    public Chapter() {
        // Cần thiết cho Firebase
    }

    // Constructor từ ChineseChapter
    public Chapter(ChineseChapter chineseChapter) {
        this.id = chineseChapter.getId();
        this.storyId = chineseChapter.getNovelId();
        this.chapterNumber = chineseChapter.getChapterNumber();
        this.title = chineseChapter.getTitleZh();
        this.content = chineseChapter.getContentZh();
        this.translatedContent = chineseChapter.getContentVi();
        this.hasTranslation = chineseChapter.isHasTranslation();
        this.lastUpdated = chineseChapter.getLastUpdated();
        this.isDownloaded = chineseChapter.isDownloaded();
    }

    // Chuyển đổi sang ChineseChapter
    public ChineseChapter toChineseChapter() {
        ChineseChapter chapter = new ChineseChapter();
        chapter.setId(this.id);
        chapter.setNovelId(this.storyId);
        chapter.setChapterNumber(this.chapterNumber);
        chapter.setTitleZh(this.title);
        chapter.setContentZh(this.content);
        chapter.setContentVi(this.translatedContent);
        chapter.setHasTranslation(this.hasTranslation);
        chapter.setLastUpdated(this.lastUpdated);
        chapter.setDownloaded(this.isDownloaded);
        return chapter;
    }

    // Getters và setters
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    public boolean isHasTranslation() {
        return hasTranslation;
    }

    public void setHasTranslation(boolean hasTranslation) {
        this.hasTranslation = hasTranslation;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }
}
