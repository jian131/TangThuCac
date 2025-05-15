package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Lớp model cho truyện gốc tiếng Trung
 */
public class OriginalStory implements Serializable {
    private String id;
    private String title;
    private String author;
    private String description;
    private String coverImageUrl;
    private String genre;
    private String status;
    private String sourceName;
    private String sourceBaseUrl;
    private List<String> chapterIds;
    private List<String> chapterTitles;
    private int totalChapters;
    private long lastUpdated;
    private int viewCount;
    private double rating;
    private int downloadCount;
    private boolean isDownloaded;
    private int lastReadChapter;
    private int totalDownloadedChapters;
    private Map<String, TranslatedChapter> translatedChapters;

    /**
     * Constructor với các thông tin cơ bản
     */
    public OriginalStory(String id, String title, String author, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.chapterIds = new ArrayList<>();
        this.chapterTitles = new ArrayList<>();
        this.lastUpdated = System.currentTimeMillis();
        this.isDownloaded = false;
        this.lastReadChapter = 0;
        this.totalDownloadedChapters = 0;
    }

    /**
     * Constructor mặc định cần thiết cho Serializable
     */
    public OriginalStory() {
        this.chapterIds = new ArrayList<>();
        this.chapterTitles = new ArrayList<>();
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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceBaseUrl() {
        return sourceBaseUrl;
    }

    public void setSourceBaseUrl(String sourceBaseUrl) {
        this.sourceBaseUrl = sourceBaseUrl;
    }

    public List<String> getChapterIds() {
        return chapterIds;
    }

    public void setChapterIds(List<String> chapterIds) {
        this.chapterIds = chapterIds;
        this.totalChapters = chapterIds != null ? chapterIds.size() : 0;
    }

    public List<String> getChapterTitles() {
        return chapterTitles;
    }

    public void setChapterTitles(List<String> chapterTitles) {
        this.chapterTitles = chapterTitles;
    }

    public int getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(int totalChapters) {
        this.totalChapters = totalChapters;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public int getLastReadChapter() {
        return lastReadChapter;
    }

    public void setLastReadChapter(int lastReadChapter) {
        this.lastReadChapter = lastReadChapter;
    }

    public int getTotalDownloadedChapters() {
        return totalDownloadedChapters;
    }

    public void setTotalDownloadedChapters(int totalDownloadedChapters) {
        this.totalDownloadedChapters = totalDownloadedChapters;
    }

    /**
     * Thêm một chương vào danh sách
     * @param chapterId ID của chương
     * @param chapterTitle Tiêu đề của chương
     */
    public void addChapter(String chapterId, String chapterTitle) {
        if (this.chapterIds == null) {
            this.chapterIds = new ArrayList<>();
        }
        if (this.chapterTitles == null) {
            this.chapterTitles = new ArrayList<>();
        }

        this.chapterIds.add(chapterId);
        this.chapterTitles.add(chapterTitle);
        this.totalChapters = this.chapterIds.size();
    }

    /**
     * Lấy URL của một chương dựa trên ID
     * @param chapterId ID của chương
     * @return URL đầy đủ của chương
     */
    public String getChapterUrl(String chapterId) {
        return sourceBaseUrl + "/novel/" + id + "/chapter/" + chapterId;
    }

    /**
     * Cập nhật thời gian cập nhật của truyện
     */
    public void updateTimestamp() {
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Tính tỉ lệ tiến độ đọc truyện (%)
     * @return Tỉ lệ hoàn thành
     */
    public float getReadingProgress() {
        if (totalChapters <= 0) {
            return 0f;
        }
        return (float) lastReadChapter / totalChapters * 100f;
    }

    /**
     * Lấy tiêu đề của chương theo vị trí
     * @param position Vị trí của chương
     * @return Tiêu đề của chương, hoặc null nếu không tìm thấy
     */
    public String getChapterTitle(int position) {
        if (chapterTitles != null && position >= 0 && position < chapterTitles.size()) {
            return chapterTitles.get(position);
        }
        return null;
    }

    /**
     * Lấy ID của chương theo vị trí
     * @param position Vị trí của chương
     * @return ID của chương, hoặc null nếu không tìm thấy
     */
    public String getChapterId(int position) {
        if (chapterIds != null && position >= 0 && position < chapterIds.size()) {
            return chapterIds.get(position);
        }
        return null;
    }

    /**
     * Lấy vị trí của chương dựa trên ID
     * @param chapterId ID của chương cần tìm
     * @return Vị trí của chương, hoặc -1 nếu không tìm thấy
     */
    public int getChapterPosition(String chapterId) {
        if (chapterIds != null) {
            return chapterIds.indexOf(chapterId);
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OriginalStory that = (OriginalStory) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (sourceName != null ? !sourceName.equals(that.sourceName) : that.sourceName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (sourceName != null ? sourceName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OriginalStory{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", totalChapters=" + totalChapters +
                ", isDownloaded=" + isDownloaded +
                '}';
    }

    // ==== Phương thức tương thích ngược với mã cũ ====

    /**
     * Phương thức tương thích ngược - lấy tiêu đề tiếng Việt
     */
    public String getTitleVi() {
        return title; // Phương thức cũ chuyển sang phương thức mới
    }

    /**
     * Phương thức tương thích ngược - đặt tiêu đề tiếng Việt
     */
    public void setTitleVi(String titleVi) {
        this.title = titleVi;
    }

    /**
     * Phương thức tương thích ngược - lấy URL ảnh
     */
    public String getImageUrl() {
        return coverImageUrl;
    }

    /**
     * Phương thức tương thích ngược - đặt URL ảnh
     */
    public void setImageUrl(String imageUrl) {
        this.coverImageUrl = imageUrl;
    }

    /**
     * Phương thức tương thích ngược - lấy nguồn
     */
    public String getSource() {
        return sourceName;
    }

    /**
     * Phương thức tương thích ngược - đặt nguồn
     */
    public void setSource(String source) {
        this.sourceName = source;
    }

    /**
     * Phương thức tương thích ngược - lấy thể loại dưới dạng danh sách
     */
    public List<String> getGenres() {
        List<String> genres = new ArrayList<>();
        if (genre != null) {
            // Nếu genre chứa nhiều thể loại phân cách bằng dấu phẩy
            String[] genreArray = genre.split(",");
            for (String g : genreArray) {
                genres.add(g.trim());
            }
        }
        return genres;
    }

    /**
     * Phương thức tương thích ngược - đặt thể loại từ danh sách
     */
    public void setGenres(List<String> genres) {
        if (genres != null && !genres.isEmpty()) {
            this.genre = String.join(", ", genres);
        }
    }

    /**
     * Phương thức tương thích ngược - lấy URL ảnh bìa
     */
    public String getCoverImage() {
        return coverImageUrl;
    }

    /**
     * Phương thức tương thích ngược - đặt URL ảnh bìa
     */
    public void setCoverImage(String coverImage) {
        this.coverImageUrl = coverImage;
    }

    /**
     * Phương thức tương thích ngược - lấy số lượng chương
     */
    public int getChapterCount() {
        return totalChapters;
    }

    /**
     * Phương thức tương thích ngược - đặt số lượng chương
     */
    public void setChapterCount(int chapterCount) {
        this.totalChapters = chapterCount;
    }

    /**
     * Phương thức tương thích ngược - kiểm tra đã hoàn thành
     */
    public boolean isCompleted() {
        return "Hoàn thành".equals(status);
    }

    /**
     * Phương thức tương thích ngược - đặt trạng thái hoàn thành
     */
    public void setCompleted(boolean completed) {
        this.status = completed ? "Hoàn thành" : "Đang tiến hành";
    }

    /**
     * Phương thức tương thích ngược - lấy số chương đã dịch
     */
    public int getTranslatedChaptersCount() {
        return totalDownloadedChapters;
    }

    /**
     * Phương thức tương thích ngược - đặt số chương đã dịch
     */
    public void setTranslatedChaptersCount(int count) {
        this.totalDownloadedChapters = count;
    }

    /**
     * Phương thức tương thích ngược - lấy mô tả tiếng Việt
     */
    public String getDescriptionVi() {
        return description;
    }

    /**
     * Phương thức tương thích ngược - đặt mô tả tiếng Việt
     */
    public void setDescriptionVi(String descriptionVi) {
        this.description = descriptionVi;
    }

    /**
     * Phương thức tương thích ngược - đặt URL nguồn
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceBaseUrl = sourceUrl;
    }

    /**
     * Phương thức tương thích ngược - lấy URL nguồn
     */
    public String getSourceUrl() {
        return sourceBaseUrl;
    }

    /**
     * Phương thức tương thích ngược - đặt ngôn ngữ
     */
    public void setLanguage(String language) {
        // Phương thức này không có trong lớp mới
        // Chỉ để tương thích ngược
    }

    /**
     * Phương thức tương thích ngược - lấy ngôn ngữ
     */
    public String getLanguage() {
        return "zh"; // Mặc định là tiếng Trung
    }

    /**
     * Phương thức tương thích ngược - đặt thẻ
     */
    public void setTags(List<String> tags) {
        // Chỉ để tương thích ngược
    }

    /**
     * Phương thức tương thích ngược - lấy thẻ
     */
    public List<String> getTags() {
        return new ArrayList<>(); // Trả về danh sách rỗng
    }

    /**
     * Phương thức tương thích ngược - đặt thời gian cập nhật
     */
    public void setUpdateTime(long updateTime) {
        this.lastUpdated = updateTime;
    }

    /**
     * Phương thức tương thích ngược - lấy thời gian cập nhật
     */
    public long getUpdateTime() {
        return lastUpdated;
    }

    /**
     * Phương thức tương thích ngược - lấy các chương đã dịch
     */
    public Map<String, TranslatedChapter> getTranslatedChapters() {
        if (translatedChapters == null) {
            translatedChapters = new HashMap<>();
        }
        return translatedChapters;
    }

    /**
     * Phương thức tương thích ngược - đặt các chương đã dịch
     */
    public void setTranslatedChapters(Map<String, TranslatedChapter> translatedChapters) {
        this.translatedChapters = translatedChapters;
        if (translatedChapters != null) {
            this.totalDownloadedChapters = translatedChapters.size();
        }
    }
}
