package com.emsi.fairpay_maroc.models;

public class Item {
    private int id;
    private String image;
    private String nom;
    private String prix;
    private String conseil;
    private String timeDifference;
    private String categorieName;
    private String regionName;
    private String typeName;

    public Item(int id, String image, String nom, String prix, String conseil, String timeDifference, 
                String categorieName, String regionName, String typeName) {
        this.id = id;
        this.image = image;
        this.nom = nom;
        this.prix = prix;
        this.conseil = conseil;
        this.timeDifference = timeDifference;
        this.categorieName = categorieName;
        this.regionName = regionName;
        this.typeName = typeName;
    }

    // Constructor without ID for backward compatibility
    public Item(String image, String nom, String prix, String conseil, String timeDifference, 
                String categorieName, String regionName, String typeName) {
        this(-1, image, nom, prix, conseil, timeDifference, categorieName, regionName, typeName);
    }

    public int getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getNom() {
        return nom;
    }

    public String getPrix() {
        return prix;
    }

    public String getConseil() {
        return conseil;
    }

    public String getTimeDifference() {
        return timeDifference;
    }

    public String getCategorieName() {
        return categorieName;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getTypeName() {
        return typeName;
    }
}
