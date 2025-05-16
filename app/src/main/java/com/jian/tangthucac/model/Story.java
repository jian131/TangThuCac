package com.jian.tangthucac.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Mô hình dữ liệu Story đơn giản để giữ tương thích với các thành phần cũ
 * Chuyển tiếp đến ChineseNovel
 */
public class Story implements Serializable {
    private String id;
    private String title;
    private String author;
    private String description;
    private String translator;
    private String coverUrl;
    private String status;
    private String source;
    private List<String> genres;
    private int views;
    private Map<String, Boolean> likes;
    private boolean isTranslated;
    private boolean isCompleted;

    // Constructor mặc định cần thiết cho Firebase
    public Story() {
        // Cần thiết cho Firebase
    }

    // Constructor từ ChineseNovel
    public Story(ChineseNovel chineseNovel) {
        this.id = chineseNovel.getId();
        this.title = chineseNovel.getTitleVi();
        this.author = chineseNovel.getAuthor();
        this.description = chineseNovel.getDescription();
        this.coverUrl = chineseNovel.getCoverUrl();
        this.status = chineseNovel.getStatus();
        this.source = chineseNovel.getSource();
        this.genres = chineseNovel.getGenres();
        this.views = chineseNovel.getViews();
        this.likes = chineseNovel.getLikes();
        this.isTranslated = chineseNovel.isHasTranslation();
        this.isCompleted = chineseNovel.isCompleted();
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

    public String getTranslator() {
        return translator;
    }

    public void setTranslator(String translator) {
        this.translator = translator;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public boolean isTranslated() {
        return isTranslated;
    }

    public void setTranslated(boolean translated) {
        isTranslated = translated;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // Chuyển đổi Story thành ChineseNovel
    public ChineseNovel toChineseNovel() {
        ChineseNovel novel = new ChineseNovel();
        novel.setId(this.id);
        novel.setTitleVi(this.title);
        novel.setAuthor(this.author);
        novel.setDescription(this.description);
        novel.setCoverUrl(this.coverUrl);
        novel.setStatus(this.status);
        novel.setSource(this.source);
        novel.setGenres(this.genres);
        novel.setViews(this.views);
        novel.setLikes(this.likes);
        novel.setHasTranslation(this.isTranslated);
        novel.setCompleted(this.isCompleted);
        return novel;
    }
}
