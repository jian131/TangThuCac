package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Model lưu trữ ánh xạ từ khóa tìm kiếm giữa các ngôn ngữ
 */
public class SearchKeywordMap implements Serializable {
    private String id;                      // ID ánh xạ
    private String vietnameseKeyword;       // Từ khóa tiếng Việt gốc
    private String chineseKeyword;          // Từ khóa tiếng Trung đã dịch
    private String englishKeyword;          // Từ khóa tiếng Anh đã dịch
    private Map<String, String> otherLanguages; // Các ngôn ngữ khác (code: keyword)
    private int useCount;                   // Số lần sử dụng từ khóa
    private long lastUsedTime;              // Thời gian sử dụng gần nhất
    private String translationEngine;       // Công cụ dịch sử dụng

    public SearchKeywordMap() {
        // Required empty constructor for Firebase
    }

    // Tạo constructor thuận tiện
    public SearchKeywordMap(String vietnameseKeyword, String chineseKeyword) {
        this.vietnameseKeyword = vietnameseKeyword;
        this.chineseKeyword = chineseKeyword;
        this.useCount = 0;
        this.lastUsedTime = System.currentTimeMillis();
    }

    // Getters & Setters
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

    public Map<String, String> getOtherLanguages() {
        return otherLanguages;
    }

    public void setOtherLanguages(Map<String, String> otherLanguages) {
        this.otherLanguages = otherLanguages;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public void incrementUseCount() {
        this.useCount++;
        this.lastUsedTime = System.currentTimeMillis();
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getTranslationEngine() {
        return translationEngine;
    }

    public void setTranslationEngine(String translationEngine) {
        this.translationEngine = translationEngine;
    }
}
