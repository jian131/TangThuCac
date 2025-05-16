package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp đại diện cho truyện gốc từ nguồn Trung Quốc
 * Đóng vai trò như một adapter chuyển tiếp đến ChineseNovel
 */
public class OriginalStory implements Serializable {
    private String id;
    private String title;        // Tiêu đề gốc (tiếng Trung)
    private String translatedTitle; // Tiêu đề đã dịch (tiếng Việt)
    private String author;
    private String description;
    private String coverUrl;
    private String source;
    private String sourceUrl;
    private String status;
    private List<String> genres;
    private List<String> tags;
    private Map<String, String> chapterLinks;
    private int totalChapters;
    private int translatedChapters;
    private boolean isDownloaded;
    private boolean isTranslating;

    // Constructor mặc định cần thiết cho Firebase
    public OriginalStory() {
        this.genres = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.chapterLinks = new HashMap<>();
    }

    // Constructor từ ChineseNovel
    public OriginalStory(ChineseNovel novel) {
        this.id = novel.getId();
        this.title = novel.getTitleZh();
        this.translatedTitle = novel.getTitleVi();
        this.author = novel.getAuthor();
        this.description = novel.getDescription();
        this.coverUrl = novel.getCoverUrl();
        this.source = novel.getSource();
        this.sourceUrl = novel.getSourceUrl();
        this.status = novel.getStatus();
        this.genres = novel.getGenres();
        this.tags = novel.getTags() != null ? novel.getTags() : new ArrayList<>();
        this.chapterLinks = novel.getChapterLinks() != null ? novel.getChapterLinks() : new HashMap<>();
        this.totalChapters = novel.getChapterIds() != null ? novel.getChapterIds().size() : 0;
        this.translatedChapters = novel.getTranslatedChapterCount();
        this.isDownloaded = novel.isDownloaded();
        this.isTranslating = novel.isHasTranslation();
    }

    // Chuyển đổi sang ChineseNovel
    public ChineseNovel toChineseNovel() {
        ChineseNovel novel = new ChineseNovel();
        novel.setId(this.id);
        novel.setTitleZh(this.title);
        novel.setTitleVi(this.translatedTitle);
        novel.setAuthor(this.author);
        novel.setDescription(this.description);
        novel.setCoverUrl(this.coverUrl);
        novel.setSource(this.source);
        novel.setSourceUrl(this.sourceUrl);
        novel.setStatus(this.status);
        novel.setGenres(this.genres);
        novel.setTags(this.tags);
        novel.setChapterLinks(this.chapterLinks);
        novel.setDownloaded(this.isDownloaded);
        novel.setHasTranslation(this.isTranslating);
        novel.setTranslatedChapterCount(this.translatedChapters);
        return novel;
    }

    // Getters và setters
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

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getChapterLinks() {
        return chapterLinks;
    }

    public void setChapterLinks(Map<String, String> chapterLinks) {
        this.chapterLinks = chapterLinks;
    }

    public int getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(int totalChapters) {
        this.totalChapters = totalChapters;
    }

    public int getTranslatedChapters() {
        return translatedChapters;
    }

    public void setTranslatedChapters(int translatedChapters) {
        this.translatedChapters = translatedChapters;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public boolean isTranslating() {
        return isTranslating;
    }

    public void setTranslating(boolean translating) {
        isTranslating = translating;
    }
}
