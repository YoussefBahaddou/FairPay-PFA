package com.emsi.fairpay_maroc.models;

public class Category {
    private int iconResId;
    private String name;

    public Category(int iconResId, String name) {
        this.iconResId = iconResId;
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getName() {
        return name;
    }
}
