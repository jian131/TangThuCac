package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Model lưu trữ thông tin truyện gốc (chủ yếu là tiếng Trung)
 */
public class OriginalStory implements Serializable {
    private String id;
    private String title;           // Tiêu đề gốc
    private String titleVi;         // Tiêu đề đã dịch sang tiếng Việt
    private String author;          // Tác giả
    private String description;     // Mô tả gốc
    private String descriptionVi;   // Mô tả đã dịch
    private String imageUrl;        // Đường dẫn hình ảnh
    private String coverImage;      // Đường dẫn hình ảnh bìa
    private String source;          // Nguồn (ví dụ: webnovel, novelfull, etc.)
    private String sourceUrl;       // URL của truyện gốc
    private String language;        // Ngôn ngữ gốc (zh, en, etc.)
    private List<String> genres;    // Thể loại
    private List<String> tags;      // Các thẻ
    private int chapterCount;       // Số chương
    private int translatedChaptersCount; // Số chương đã dịch
    private Map<String, TranslatedChapter> translatedChapters; // Map lưu trữ các chương đã dịch
    private boolean completed;      // Trạng thái hoàn thành
    private long updateTime;        // Thời gian cập nhật

    public OriginalStory() {
        // Required empty constructor for Firebase
    }

    // Getters & Setters
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCoverImage() {
        return coverImage != null ? coverImage : imageUrl;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public int getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(int chapterCount) {
        this.chapterCount = chapterCount;
    }

    public int getTranslatedChaptersCount() {
        if (translatedChapters != null) {
            return translatedChapters.size();
        }
        return translatedChaptersCount;
    }

    public void setTranslatedChaptersCount(int translatedChaptersCount) {
        this.translatedChaptersCount = translatedChaptersCount;
    }

    public Map<String, TranslatedChapter> getTranslatedChapters() {
        return translatedChapters;
    }

    public void setTranslatedChapters(Map<String, TranslatedChapter> translatedChapters) {
        this.translatedChapters = translatedChapters;
        // Cập nhật số lượng chương đã dịch nếu có dữ liệu
        if (translatedChapters != null) {
            this.translatedChaptersCount = translatedChapters.size();
        }
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

    /**
     * Lấy tổng số chương của truyện (nếu chưa có thông tin thì lấy từ số chương đã dịch)
     */
    public int getTotalChapters() {
        if (chapterCount > 0) {
            return chapterCount;
        }
        if (translatedChapters != null) {
            return translatedChapters.size();
        }
        return 0;
    }
}
