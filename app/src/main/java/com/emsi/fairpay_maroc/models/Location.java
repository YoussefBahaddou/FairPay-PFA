package com.emsi.fairpay_maroc.models;

public class Location {
    private int villeId;
    private String name;
    private String villeName;
    private String imageUrl;

    public Location(int villeId, String name, String villeName) {
        this.villeId = villeId;
        this.name = name;
        this.villeName = villeName;
        this.imageUrl = "";
    }

    public Location(int villeId, String name, String villeName, String imageUrl) {
        this.villeId = villeId;
        this.name = name;
        this.villeName = villeName;
        this.imageUrl = imageUrl;
    }

    public int getVilleId() {
        return villeId;
    }

    public String getName() {
        return name;
    }

    public String getVilleName() {
        return villeName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
