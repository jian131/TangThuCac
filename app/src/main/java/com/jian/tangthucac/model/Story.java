package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.Map;
import java.util.List;

public class Story implements Serializable {
    private String id;
    private String title;
    private String author;
    private int views;
    private String image;
    private Map<String, Chapter> chapters;
    private String genre;
    private boolean hot;

    // Thêm các trường còn thiếu từ log lỗi
    private String description;
    private String imageUrl;
    private List<String> genres;
    private String updateDate;
    private int totalChapters;

    public Story() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Map<String, Chapter> getChapters() { return chapters; }
    public void setChapters(Map<String, Chapter> chapters) { this.chapters = chapters; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public boolean isHot() { return hot; }
    public void setHot(boolean hot) { this.hot = hot; }

    // Getter và setter cho các trường mới thêm
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public String getUpdateDate() { return updateDate; }
    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    public int getTotalChapters() { return totalChapters; }
    public void setTotalChapters(int totalChapters) { this.totalChapters = totalChapters; }
}
