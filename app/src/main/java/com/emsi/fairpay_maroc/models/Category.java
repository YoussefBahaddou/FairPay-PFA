package com.emsi.fairpay_maroc.models;

public class Category {
    private String iconUrl;
    private String name;    

    public Category(String iconUrl, String name) {
        this.iconUrl = iconUrl;
        this.name = name;
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
