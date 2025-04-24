package com.emsi.fairpay_maroc.models;

public class Submission {
    private String id;
    private String nom;
    private String price;
    private String dateMiseAJour;
    private String status;
    private String commentaire;

    public Submission(String id, String nom, String price, String dateMiseAJour, String status, String commentaire) {
        this.id = id;
        this.nom = nom;
        this.price = price;
        this.dateMiseAJour = dateMiseAJour;
        this.status = status;
        this.commentaire = commentaire;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDateMiseAJour() {
        return dateMiseAJour;
    }

    public void setDateMiseAJour(String dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
