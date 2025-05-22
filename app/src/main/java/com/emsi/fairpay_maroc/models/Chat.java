package com.emsi.fairpay_maroc.models;

import java.util.Date;

public class Chat {
    private int id;
    private String sujet;
    private Date dateCreation;
    private int utilisateurId;

    public Chat(int id, String sujet, Date dateCreation, int utilisateurId) {
        this.id = id;
        this.sujet = sujet;
        this.dateCreation = dateCreation;
        this.utilisateurId = utilisateurId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
} 