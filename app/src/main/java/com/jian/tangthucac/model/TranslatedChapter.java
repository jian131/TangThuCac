package com.jian.tangthucac.model;

import java.io.Serializable;

/**
 * Lớp model chứa thông tin về chương đã dịch
 */
public class TranslatedChapter implements Serializable {
    private String id;
    private String storyId;
    private String title;
    private String originalTitle;
    private String originalContent;
    private String translatedContent;
    private String chapterNumber;
    private long translationTimestamp;
    private String translationEngine;
    private boolean isBilingualMode;
    private boolean isDownloaded;
    private int wordCount;
    private String cacheKey;

    /**
     * Constructor rỗng cần thiết cho Serializable
     */
    public TranslatedChapter() {
        this.translationTimestamp = System.currentTimeMillis();
        this.isBilingualMode = false;
        this.isDownloaded = false;
    }

    /**
     * Constructor với thông tin cơ bản
     * @param storyId ID của truyện
     * @param id ID của chương
     * @param title Tiêu đề của chương
     */
    public TranslatedChapter(String storyId, String id, String title) {
        this.storyId = storyId;
        this.id = id;
        this.title = title;
        this.translationTimestamp = System.currentTimeMillis();
        this.isBilingualMode = false;
        this.isDownloaded = false;
    }

    /**
     * Constructor đầy đủ
     * @param storyId ID của truyện
     * @param id ID của chương
     * @param title Tiêu đề đã dịch
     * @param originalTitle Tiêu đề gốc
     * @param translatedContent Nội dung đã dịch
     * @param originalContent Nội dung gốc
     */
    public TranslatedChapter(String storyId, String id, String title, String originalTitle,
                             String translatedContent, String originalContent) {
        this(storyId, id, title);
        this.originalTitle = originalTitle;
        this.translatedContent = translatedContent;
        this.originalContent = originalContent;
    }

    // Getters và Setters

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
        updateWordCount();
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    public String getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(String chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public long getTranslationTimestamp() {
        return translationTimestamp;
    }

    public void setTranslationTimestamp(long translationTimestamp) {
        this.translationTimestamp = translationTimestamp;
    }

    public String getTranslationEngine() {
        return translationEngine;
    }

    public void setTranslationEngine(String translationEngine) {
        this.translationEngine = translationEngine;
    }

    public boolean isBilingualMode() {
        return isBilingualMode;
    }

    public void setBilingualMode(boolean bilingualMode) {
        isBilingualMode = bilingualMode;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getCacheKey() {
        if (cacheKey == null) {
            cacheKey = storyId + "_" + id;
        }
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    /**
     * Cập nhật số từ trong nội dung gốc
     */
    private void updateWordCount() {
        if (originalContent != null) {
            // Đối với tiếng Trung, mỗi ký tự được xem như một từ
            this.wordCount = originalContent.replaceAll("[\\s\\n\\r\\t<>]*", "").length();
        }
    }

    /**
     * Tạo nội dung song ngữ dựa trên nội dung gốc và nội dung đã dịch
     * @param separator Phân cách giữa các đoạn văn
     * @return Nội dung song ngữ
     */
    public String createBilingualContent(String separator) {
        if (originalContent == null || translatedContent == null) {
            return translatedContent != null ? translatedContent :
                   originalContent != null ? originalContent : "";
        }

        StringBuilder bilingual = new StringBuilder();
        String[] originalParagraphs = originalContent.split("<br\\s*/?>");
        String[] translatedParagraphs = translatedContent.split("<br\\s*/?>");

        int maxParagraphs = Math.min(originalParagraphs.length, translatedParagraphs.length);
        for (int i = 0; i < maxParagraphs; i++) {
            // Thêm đoạn gốc
            bilingual.append(originalParagraphs[i].trim());
            bilingual.append(separator != null ? separator : "<br/>");

            // Thêm đoạn dịch
            bilingual.append(translatedParagraphs[i].trim());
            bilingual.append("<br/><br/>");
        }

        return bilingual.toString();
    }

    /**
     * Cập nhật timestamp dịch
     */
    public void updateTranslationTimestamp() {
        this.translationTimestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslatedChapter that = (TranslatedChapter) o;

        if (storyId != null ? !storyId.equals(that.storyId) : that.storyId != null) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        int result = storyId != null ? storyId.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TranslatedChapter{" +
                "storyId='" + storyId + '\'' +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", wordCount=" + wordCount +
                ", isDownloaded=" + isDownloaded +
                '}';
    }

    // ==== Phương thức tương thích ngược với mã cũ ====

    /**
     * Phương thức tương thích ngược - lấy tiêu đề tiếng Việt
     */
    public String getTitleVi() {
        return title;
    }

    /**
     * Phương thức tương thích ngược - đặt tiêu đề tiếng Việt
     */
    public void setTitleVi(String titleVi) {
        this.title = titleVi;
    }

    /**
     * Phương thức tương thích ngược - lấy nội dung gốc
     */
    public String getContent() {
        return originalContent;
    }

    /**
     * Phương thức tương thích ngược - đặt nội dung gốc
     */
    public void setContent(String content) {
        this.originalContent = content;
        updateWordCount();
    }

    /**
     * Phương thức tương thích ngược - lấy nội dung đã dịch
     */
    public String getContentVi() {
        return translatedContent;
    }

    /**
     * Phương thức tương thích ngược - đặt nội dung đã dịch
     */
    public void setContentVi(String contentVi) {
        this.translatedContent = contentVi;
    }

    /**
     * Phương thức tương thích ngược - đặt URL nguồn
     */
    public void setSourceUrl(String sourceUrl) {
        // TranslatedChapter không có trường sourceUrl,
        // nhưng giữ lại phương thức này để tương thích
    }

    /**
     * Phương thức tương thích ngược - đặt thời gian dịch
     */
    public void setTranslatedTime(long translatedTime) {
        this.translationTimestamp = translatedTime;
    }

    /**
     * Phương thức tương thích ngược - lấy thời gian dịch
     */
    public long getTranslatedTime() {
        return translationTimestamp;
    }

    /**
     * Phương thức tương thích ngược - đặt chất lượng dịch
     */
    public void setTranslationQuality(double translationQuality) {
        // Lưu vào cache key để tương thích
        this.cacheKey = String.format("%s_%s_%.1f", storyId, id, translationQuality);
    }

    /**
     * Phương thức tương thích ngược - lấy chất lượng dịch
     */
    public double getTranslationQuality() {
        return 0.9; // Giá trị mặc định
    }

    /**
     * Phương thức tương thích ngược - đặt số chương với tham số int
     */
    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = String.valueOf(chapterNumber);
    }

    /**
     * Phương thức tương thích ngược - lấy số chương dưới dạng int
     */
    public int getChapterNumberInt() {
        if (chapterNumber != null) {
            try {
                return Integer.parseInt(chapterNumber);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return 0;
    }
}
