package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Lớp đại diện cho một mục trong thư viện của người dùng
 */
public class Library implements Serializable {
    private String id;
    private String userId;
    private String storyId;
    private Date addedDate;
    private int lastReadChapter;
    private Date lastReadDate;
    private boolean isFavorite;

    // Constructor mặc định cần thiết cho Firebase
    public Library() {
        // Cần thiết cho Firebase
    }

    // Constructor đầy đủ
    public Library(String id, String userId, String storyId, Date addedDate,
                  int lastReadChapter, Date lastReadDate, boolean isFavorite) {
        this.id = id;
        this.userId = userId;
        this.storyId = storyId;
        this.addedDate = addedDate;
        this.lastReadChapter = lastReadChapter;
        this.lastReadDate = lastReadDate;
        this.isFavorite = isFavorite;
    }

    // Khởi tạo một mục thư viện mới
    public static Library createNew(String userId, String storyId) {
        Library library = new Library();
        library.setId(userId + "_" + storyId);
        library.setUserId(userId);
        library.setStoryId(storyId);
        library.setAddedDate(new Date());
        library.setLastReadChapter(0);
        library.setLastReadDate(new Date());
        library.setFavorite(false);
        return library;
    }

    // Getters và setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public int getLastReadChapter() {
        return lastReadChapter;
    }

    public void setLastReadChapter(int lastReadChapter) {
        this.lastReadChapter = lastReadChapter;
    }

    public Date getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(Date lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // Cập nhật thông tin khi đọc chương
    public void updateReadingProgress(int chapterNumber) {
        this.lastReadChapter = chapterNumber;
        this.lastReadDate = new Date();
    }
}
