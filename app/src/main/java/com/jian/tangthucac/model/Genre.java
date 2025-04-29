package com.jian.tangthucac.model;

import java.io.Serializable;

public class Genre implements Serializable {
    private String name;
    private int imageResource;

    public Genre(String name, int imageResource) {
        this.name = name;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
