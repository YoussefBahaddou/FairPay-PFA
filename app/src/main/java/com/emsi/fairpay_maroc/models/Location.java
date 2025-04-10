package com.emsi.fairpay_maroc.models;

public class Location {
    private String name;
    private String villeName;
    private int villeId;

    public Location(String name, String villeName) {
        this.name = name;
        this.villeName = villeName;
        this.villeId = -1;
    }

    public Location(String name, String villeName, int villeId) {
        this.name = name;
        this.villeName = villeName;
        this.villeId = villeId;
    }

    public String getName() {
        return name;
    }

    public String getVilleName() {
        return villeName;
    }
    
    public int getVilleId() {
        return villeId;
    }
}
