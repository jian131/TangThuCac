package com.jian.tangthucac.model;

import java.io.Serializable;

/**
 * Model đại diện cho một chương trong truyện
 */
public class Chapter implements Serializable {
    private String title;
    private String content;
    private int index;
    private String id;
    private String url;

    public Chapter() {
        // Constructor mặc định cần thiết cho Firebase
    }

    public Chapter(String title, String content, int index) {
        this.title = title;
        this.content = content;
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "title='" + title + '\'' +
                ", index=" + index +
                '}';
    }
}
