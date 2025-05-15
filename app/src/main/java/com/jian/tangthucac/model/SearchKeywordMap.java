package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp model chứa ánh xạ từ khóa tìm kiếm giữa tiếng Việt và tiếng Trung
 */
public class SearchKeywordMap implements Serializable {
    private String id;
    private String vietnameseKeyword;
    private String chineseKeyword;
    private String englishKeyword;
    private long timestamp;
    private int useCount;

    /**
     * Constructor rỗng cần thiết cho serialization
     */
    public SearchKeywordMap() {
        this.timestamp = System.currentTimeMillis();
        this.useCount = 0;
    }

    /**
     * Constructor với từ khóa tiếng Việt và tiếng Trung
     * @param vietnameseKeyword Từ khóa tiếng Việt
     * @param chineseKeyword Từ khóa tiếng Trung tương ứng
     */
    public SearchKeywordMap(String vietnameseKeyword, String chineseKeyword) {
        this.vietnameseKeyword = vietnameseKeyword;
        this.chineseKeyword = chineseKeyword;
        this.timestamp = System.currentTimeMillis();
        this.useCount = 0;
        // Tạo ID dựa trên từ khóa tiếng Việt
        this.id = vietnameseKeyword.toLowerCase().replace(" ", "_");
    }

    /**
     * Constructor đầy đủ
     * @param vietnameseKeyword Từ khóa tiếng Việt
     * @param chineseKeyword Từ khóa tiếng Trung
     * @param englishKeyword Từ khóa tiếng Anh
     */
    public SearchKeywordMap(String vietnameseKeyword, String chineseKeyword, String englishKeyword) {
        this(vietnameseKeyword, chineseKeyword);
        this.englishKeyword = englishKeyword;
    }

    // Getters và setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVietnameseKeyword() {
        return vietnameseKeyword;
    }

    public void setVietnameseKeyword(String vietnameseKeyword) {
        this.vietnameseKeyword = vietnameseKeyword;
    }

    public String getChineseKeyword() {
        return chineseKeyword;
    }

    public void setChineseKeyword(String chineseKeyword) {
        this.chineseKeyword = chineseKeyword;
    }

    public String getEnglishKeyword() {
        return englishKeyword;
    }

    public void setEnglishKeyword(String englishKeyword) {
        this.englishKeyword = englishKeyword;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    /**
     * Tăng số lần sử dụng từ khóa
     */
    public void incrementUseCount() {
        this.useCount++;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Cập nhật timestamp
     */
    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchKeywordMap that = (SearchKeywordMap) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (vietnameseKeyword != null ? !vietnameseKeyword.equals(that.vietnameseKeyword) : that.vietnameseKeyword != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (vietnameseKeyword != null ? vietnameseKeyword.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SearchKeywordMap{" +
                "vietnameseKeyword='" + vietnameseKeyword + '\'' +
                ", chineseKeyword='" + chineseKeyword + '\'' +
                ", useCount=" + useCount +
                '}';
    }

    // ==== Phương thức tương thích ngược với mã cũ ====

    /**
     * Phương thức tương thích ngược - lấy thời gian sử dụng gần nhất
     */
    public long getLastUsedTime() {
        return timestamp;
    }

    /**
     * Phương thức tương thích ngược - đặt thời gian sử dụng gần nhất
     */
    public void setLastUsedTime(long lastUsedTime) {
        this.timestamp = lastUsedTime;
    }

    /**
     * Phương thức tương thích ngược - đặt công cụ dịch
     */
    public void setTranslationEngine(String translationEngine) {
        // SearchKeywordMap không còn lưu trữ thông tin này
        // nhưng giữ lại phương thức để tương thích với mã cũ
    }

    /**
     * Phương thức tương thích ngược - lấy công cụ dịch
     */
    public String getTranslationEngine() {
        // Giá trị mặc định
        return "Claude";
    }

    /**
     * Phương thức tương thích ngược - đặt các ngôn ngữ khác
     */
    public void setOtherLanguages(Map<String, String> otherLanguages) {
        // SearchKeywordMap không còn lưu trữ thông tin này
        // nhưng giữ lại phương thức để tương thích với mã cũ
    }

    /**
     * Phương thức tương thích ngược - lấy các ngôn ngữ khác
     */
    public Map<String, String> getOtherLanguages() {
        Map<String, String> otherLangs = new HashMap<>();
        if (englishKeyword != null) {
            otherLangs.put("en", englishKeyword);
        }
        return otherLangs;
    }
}
