package com.emsi.fairpay_maroc.models;

public class Item {
    private String image;
    private String nom;
    private String prix;
    private String conseil;
    private String datemiseajour;
    private int categorieId;
    private int villeId;
    private int typeId;

    public Item(String image, String nom, String prix, String conseil, String datemiseajour, int categorieId, int villeId, int typeId) {
        this.image = image;
        this.nom = nom;
        this.prix = prix;
        this.conseil = conseil;
        this.datemiseajour = datemiseajour;
        this.categorieId = categorieId;
        this.villeId = villeId;
        this.typeId = typeId;
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

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public int getVilleId() {
        return villeId;
    }

    public void setVilleId(int villeId) {
        this.villeId = villeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
