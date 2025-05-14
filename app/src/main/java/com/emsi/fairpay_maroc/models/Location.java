package com.emsi.fairpay_maroc.models;

public class Location {
    private int villeId;
    private String name;
    private String villeName;

    public Location(int villeId, String name, String villeName) {
        this.villeId = villeId;
        this.name = name;
        this.villeName = villeName;
    }

    // Constructor without ID for backward compatibility
    public Location(String name, String villeName) {
        this(-1, name, villeName);
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
}
