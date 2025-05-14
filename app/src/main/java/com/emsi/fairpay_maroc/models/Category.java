package com.emsi.fairpay_maroc.models;

public class Category {
    private int id;
    private String iconUrl;
    private String name;

    public Category(int id, String iconUrl, String name) {
        this.id = id;
        this.iconUrl = iconUrl;
        this.name = name;
    }

    // Constructor without ID for backward compatibility
    public Category(String iconUrl, String name) {
        this(-1, iconUrl, name);
    }

    public int getId() {
        return id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
