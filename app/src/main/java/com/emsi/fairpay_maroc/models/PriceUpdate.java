package com.emsi.fairpay_maroc.models;

public class PriceUpdate {
    private int productImageResId;
    private String productName;
    private String location;
    private String price;
    private String date;
    private String updatedBy;

    public PriceUpdate(int productImageResId, String productName, String location,
                       String price, String date, String updatedBy) {
        this.productImageResId = productImageResId;
        this.productName = productName;
        this.location = location;
        this.price = price;
        this.date = date;
        this.updatedBy = updatedBy;
    }

    public int getProductImageResId() {
        return productImageResId;
    }

    public String getProductName() {
        return productName;
    }

    public String getLocation() {
        return location;
    }

    public String getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
