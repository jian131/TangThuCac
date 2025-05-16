package com.jian.tangthucac.model;

import java.io.Serializable;

/**
 * Lớp đại diện cho thể loại truyện
 */
public class Genre implements Serializable {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private int storyCount;

    // Constructor mặc định cần thiết cho Firebase
    public Genre() {
        // Cần thiết cho Firebase
    }

    // Constructor đầy đủ
    public Genre(String id, String name, String description, String imageUrl, int storyCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.storyCount = storyCount;
    }

    // Tạo từ ChineseGenre
    public Genre(ChineseGenre chineseGenre) {
        this.id = chineseGenre.getId();
        this.name = chineseGenre.getName();
        this.description = chineseGenre.getDescription();
        this.imageUrl = chineseGenre.getImageUrl();
        this.storyCount = chineseGenre.getNovelCount();
    }

    // Chuyển sang ChineseGenre
    public ChineseGenre toChineseGenre() {
        ChineseGenre genre = new ChineseGenre();
        genre.setId(this.id);
        genre.setName(this.name);
        genre.setDescription(this.description);
        genre.setImageUrl(this.imageUrl);
        genre.setNovelCount(this.storyCount);
        return genre;
    }

    // Getters và setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getStoryCount() {
        return storyCount;
    }

    public void setStoryCount(int storyCount) {
        this.storyCount = storyCount;
    }
}
