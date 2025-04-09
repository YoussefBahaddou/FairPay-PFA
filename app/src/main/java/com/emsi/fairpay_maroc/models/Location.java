package com.emsi.fairpay_maroc.models;

public class Location {
    private String name; 
    private String villeName; 

    public Location(String name, String villeName) {
        this.name = name;
        this.villeName = villeName;
    }

    public String getName() {
        return name;
    }

    public String getVilleName() {
        return villeName;
    }
}
