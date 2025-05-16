package com.jian.tangthucac.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Novel implements Serializable {
    @DocumentId
    private String id;
    private String title;
    private String author;
    private String coverUrl;
    private String genre;
    private float rating;
    private int chapterCount;
    private String description;
    private boolean isCompleted;
    private List<String> tags;
    private String originalLanguage;
    private String sourceUrl;
    private long viewCount;
    private long favoriteCount;
    private long updatedAt;

    // Constructor mặc định cần thiết cho Firestore
    public Novel() {
        tags = new ArrayList<>();
    }

    public Novel(String id, String title, String author, String coverUrl, String genre, float rating, int chapterCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.genre = genre;
        this.rating = rating;
        this.chapterCount = chapterCount;
        this.isCompleted = false;
        this.tags = new ArrayList<>();
        this.originalLanguage = "zh"; // Mặc định là tiếng Trung
        this.viewCount = 0;
        this.favoriteCount = 0;
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor đầy đủ
    public Novel(String id, String title, String author, String coverUrl, String genre,
                float rating, int chapterCount, String description, boolean isCompleted,
                List<String> tags, String originalLanguage, String sourceUrl,
                long viewCount, long favoriteCount, long updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.genre = genre;
        this.rating = rating;
        this.chapterCount = chapterCount;
        this.description = description;
        this.isCompleted = isCompleted;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.originalLanguage = originalLanguage;
        this.sourceUrl = sourceUrl;
        this.viewCount = viewCount;
        this.favoriteCount = favoriteCount;
        this.updatedAt = updatedAt;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(int chapterCount) {
        this.chapterCount = chapterCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    // Phương thức để tính % hoàn thành
    public String getCompletionStatus() {
        return isCompleted ? "Hoàn thành" : "Đang cập nhật";
    }
}
