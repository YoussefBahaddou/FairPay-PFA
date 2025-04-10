package com.emsi.fairpay_maroc.models;

public class Item {
    private String image;
    private String nom;
    private String prix;
    private String conseil;
    private String datemiseajour;
    private String categorieName; 
    private String regionName;    
    private String typeName;      

    public Item(String image, String nom, String prix, String conseil, String datemiseajour, String categorieName, String regionName, String typeName) {
        this.image = image;
        this.nom = nom;
        this.prix = prix;
        this.conseil = conseil;
        this.datemiseajour = datemiseajour;
        this.categorieName = categorieName;
        this.regionName = regionName;
        this.typeName = typeName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrix() {
        return prix;
    }

    public void setPrix(String prix) {
        this.prix = prix;
    }

    public String getConseil() {
        return conseil;
    }

    public void setConseil(String conseil) {
        this.conseil = conseil;
    }

    public String getDatemiseajour() {
        return datemiseajour;
    }

    public void setDatemiseajour(String datemiseajour) {
        this.datemiseajour = datemiseajour;
    }

    public String getCategorieName() {
        return categorieName;
    }

    public void setCategorieName(String categorieName) {
        this.categorieName = categorieName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
