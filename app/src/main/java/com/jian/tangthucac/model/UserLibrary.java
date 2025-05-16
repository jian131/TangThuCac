package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model đại diện cho thư viện của người dùng
 */
public class UserLibrary implements Serializable {
    private String userId;                  // ID của người dùng
    private Map<String, Long> novels;       // Map lưu ID truyện và thời gian thêm vào
    private Map<String, Integer> lastRead;  // Map lưu ID truyện và chương đọc cuối
    private List<String> recentlyRead;      // Danh sách ID truyện đọc gần đây
    private Map<String, Boolean> favorites; // Map lưu ID truyện và trạng thái yêu thích

    public UserLibrary() {
        // Constructor mặc định cho Firebase
        this.novels = new HashMap<>();
        this.lastRead = new HashMap<>();
        this.recentlyRead = new ArrayList<>();
        this.favorites = new HashMap<>();
    }

    // Constructor với userId
    public UserLibrary(String userId) {
        this();
        this.userId = userId;
    }

    // Getters và Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Long> getNovels() {
        return novels;
    }

    public void setNovels(Map<String, Long> novels) {
        this.novels = novels;
    }

    public Map<String, Integer> getLastRead() {
        return lastRead;
    }

    public void setLastRead(Map<String, Integer> lastRead) {
        this.lastRead = lastRead;
    }

    public List<String> getRecentlyRead() {
        return recentlyRead;
    }

    public void setRecentlyRead(List<String> recentlyRead) {
        this.recentlyRead = recentlyRead;
    }

    public Map<String, Boolean> getFavorites() {
        return favorites;
    }

    public void setFavorites(Map<String, Boolean> favorites) {
        this.favorites = favorites;
    }

    // Phương thức tiện ích
    /**
     * Thêm truyện vào thư viện
     * @param novelId ID của truyện
     */
    public void addNovel(String novelId) {
        novels.put(novelId, System.currentTimeMillis());
    }

    /**
     * Xóa truyện khỏi thư viện
     * @param novelId ID của truyện
     */
    public void removeNovel(String novelId) {
        novels.remove(novelId);
        lastRead.remove(novelId);
        recentlyRead.remove(novelId);
        favorites.remove(novelId);
    }

    /**
     * Cập nhật chương đọc cuối cùng của truyện
     * @param novelId ID của truyện
     * @param chapterNumber Số chương
     */
    public void updateLastRead(String novelId, int chapterNumber) {
        lastRead.put(novelId, chapterNumber);

        // Cập nhật danh sách đọc gần đây
        recentlyRead.remove(novelId);
        recentlyRead.add(0, novelId);

        // Giới hạn danh sách đọc gần đây tối đa 20 truyện
        if (recentlyRead.size() > 20) {
            recentlyRead = recentlyRead.subList(0, 20);
        }
    }

    /**
     * Đánh dấu/bỏ đánh dấu truyện yêu thích
     * @param novelId ID của truyện
     * @param isFavorite true nếu yêu thích, false nếu bỏ yêu thích
     */
    public void toggleFavorite(String novelId, boolean isFavorite) {
        if (isFavorite) {
            favorites.put(novelId, true);
        } else {
            favorites.remove(novelId);
        }
    }

    /**
     * Kiểm tra xem truyện có trong thư viện không
     * @param novelId ID của truyện
     * @return true nếu truyện đã có trong thư viện
     */
    public boolean containsNovel(String novelId) {
        return novels.containsKey(novelId);
    }

    /**
     * Kiểm tra xem truyện có được đánh dấu yêu thích không
     * @param novelId ID của truyện
     * @return true nếu truyện được đánh dấu yêu thích
     */
    public boolean isFavorite(String novelId) {
        return favorites.containsKey(novelId) && favorites.get(novelId);
    }

    /**
     * Lấy chương đọc cuối cùng của truyện
     * @param novelId ID của truyện
     * @return Số chương đọc cuối cùng, 0 nếu chưa đọc
     */
    public int getLastReadChapter(String novelId) {
        return lastRead.containsKey(novelId) ? lastRead.get(novelId) : 0;
    }
}
