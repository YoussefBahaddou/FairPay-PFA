package com.emsi.fairpay_maroc.models;

public class Submission {
    private String id;
    private String name;
    private float price;
    private String status;
    private String date;
    private String imageUrl;

    public Submission(String id, String name, float price, String status, String date, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.status = status;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
