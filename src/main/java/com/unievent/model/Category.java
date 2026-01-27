package com.unievent.model;

public class Category {
    private int catId;
    private String catName;

    // Constructors
    public Category() {}

    public Category(String catName) {
        this.catName = catName;
    }

    // Getters and Setters
    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }
}
