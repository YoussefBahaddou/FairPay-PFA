package com.emsi.fairpay_maroc.models;

import java.util.Date;

public class Commentaire {
    private int id;
    private String message;
    private Date dateCreation;
    private int utilisateurId;
    private int chatId;

    public Commentaire(int id, String message, Date dateCreation, int utilisateurId, int chatId) {
        this.id = id;
        this.message = message;
        this.dateCreation = dateCreation;
        this.utilisateurId = utilisateurId;
        this.chatId = chatId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }
} 