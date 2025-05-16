package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model đại diện cho một truyện Trung Quốc
 */
public class ChineseNovel implements Serializable {
    private String id;
    private String title;           // Tiêu đề tiếng Trung
    private String titleVi;         // Tiêu đề tiếng Việt (đã dịch)
    private String author;          // Tác giả
    private String description;     // Mô tả tiếng Trung
    private String descriptionVi;   // Mô tả tiếng Việt (đã dịch)
    private String coverImageUrl;   // URL ảnh bìa
    private List<String> genres;    // Thể loại
    private List<String> tags;      // Các tag
    private String status;          // Trạng thái: ongoing, completed
    private String source;          // Nguồn: qidian, uukanshu, 69shu, ...
    private String sourceUrl;       // URL nguồn
    private int chapterCount;       // Tổng số chương
    private boolean completed;      // Đã hoàn thành hay chưa
    private long updateTime;        // Thời gian cập nhật
    private int views;              // Lượt xem
    private int translatedChaptersCount; // Số chương đã dịch
    private List<String> chapterIds;     // Danh sách ID các chương
    private Map<String, ChineseChapter> chapters; // Map lưu trữ các chương
    private boolean isInLibrary;    // Đã thêm vào thư viện chưa
    private int lastReadChapter;    // Chương đọc cuối cùng

    public ChineseNovel() {
        // Constructor mặc định cho Firebase
        this.genres = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.chapterIds = new ArrayList<>();
        this.chapters = new HashMap<>();
        this.updateTime = System.currentTimeMillis();
        this.views = 0;
        this.translatedChaptersCount = 0;
        this.isInLibrary = false;
        this.lastReadChapter = 0;
    }

    // Constructor đầy đủ
    public ChineseNovel(String id, String title, String author, String description) {
        this();
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
    }

    // Getters và Setters
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

    public String getTitleVi() {
        return titleVi;
    }

    public void setTitleVi(String titleVi) {
        this.titleVi = titleVi;
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

    public String getDescriptionVi() {
        return descriptionVi;
    }

    public void setDescriptionVi(String descriptionVi) {
        this.descriptionVi = descriptionVi;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(int chapterCount) {
        this.chapterCount = chapterCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getTranslatedChaptersCount() {
        return translatedChaptersCount;
    }

    public void setTranslatedChaptersCount(int translatedChaptersCount) {
        this.translatedChaptersCount = translatedChaptersCount;
    }

    public List<String> getChapterIds() {
        return chapterIds;
    }

    public void setChapterIds(List<String> chapterIds) {
        this.chapterIds = chapterIds;
    }

    public Map<String, ChineseChapter> getChapters() {
        return chapters;
    }

    public void setChapters(Map<String, ChineseChapter> chapters) {
        this.chapters = chapters;
    }

    public boolean isInLibrary() {
        return isInLibrary;
    }

    public void setInLibrary(boolean inLibrary) {
        isInLibrary = inLibrary;
    }

    public int getLastReadChapter() {
        return lastReadChapter;
    }

    public void setLastReadChapter(int lastReadChapter) {
        this.lastReadChapter = lastReadChapter;
    }

    // Phương thức tiện ích
    public void addChapter(ChineseChapter chapter) {
        if (chapters == null) {
            chapters = new HashMap<>();
        }
        chapters.put(chapter.getId(), chapter);

        if (chapterIds == null) {
            chapterIds = new ArrayList<>();
        }
        if (!chapterIds.contains(chapter.getId())) {
            chapterIds.add(chapter.getId());
        }

        chapterCount = chapterIds.size();
    }

    public void incrementViews() {
        this.views++;
    }

    public void updateTimestamp() {
        this.updateTime = System.currentTimeMillis();
    }

    public double getTranslationProgress() {
        if (chapterCount == 0) return 0;
        return (double) translatedChaptersCount / chapterCount * 100;
    }

    @Override
    public String toString() {
        return "ChineseNovel{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", titleVi='" + titleVi + '\'' +
                ", author='" + author + '\'' +
                ", chapterCount=" + chapterCount +
                ", translatedChaptersCount=" + translatedChaptersCount +
                '}';
    }
}
