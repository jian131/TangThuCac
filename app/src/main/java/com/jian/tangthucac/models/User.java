package com.jian.tangthucac.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    @DocumentId
    private String userId;
    private String username;
    private String email;
    private String avatarUrl;
    private List<String> favoriteNovels;
    private List<String> readingHistory;

    // Constructor mặc định cần thiết cho Firestore
    public User() {
        favoriteNovels = new ArrayList<>();
        readingHistory = new ArrayList<>();
    }

    public User(String userId, String username, String email, String avatarUrl) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.favoriteNovels = new ArrayList<>();
        this.readingHistory = new ArrayList<>();
    }

    // Getters và Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<String> getFavoriteNovels() {
        return favoriteNovels;
    }

    public void setFavoriteNovels(List<String> favoriteNovels) {
        this.favoriteNovels = favoriteNovels;
    }

    public List<String> getReadingHistory() {
        return readingHistory;
    }

    public void setReadingHistory(List<String> readingHistory) {
        this.readingHistory = readingHistory;
    }

    // Phương thức để thêm truyện vào danh sách yêu thích
    public void addFavoriteNovel(String novelId) {
        if (favoriteNovels == null) {
            favoriteNovels = new ArrayList<>();
        }
        if (!favoriteNovels.contains(novelId)) {
            favoriteNovels.add(novelId);
        }
    }

    // Phương thức để xóa truyện khỏi danh sách yêu thích
    public void removeFavoriteNovel(String novelId) {
        if (favoriteNovels != null) {
            favoriteNovels.remove(novelId);
        }
    }

    // Phương thức để thêm truyện vào lịch sử đọc
    public void addToReadingHistory(String novelId) {
        if (readingHistory == null) {
            readingHistory = new ArrayList<>();
        }
        // Nếu đã tồn tại, xóa đi và thêm lại vào đầu danh sách
        if (readingHistory.contains(novelId)) {
            readingHistory.remove(novelId);
        }
        readingHistory.add(0, novelId); // Thêm vào đầu danh sách
    }
}
