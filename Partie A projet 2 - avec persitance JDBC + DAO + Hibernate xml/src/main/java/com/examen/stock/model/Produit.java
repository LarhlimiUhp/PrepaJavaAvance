package com.examen.stock.model;

public class Produit {
    private String nom;
    private double prix;

    // Constructeur par défaut requis par Hibernate
    public Produit() {
    }

    public Produit(String nom, double prix) {
        this.nom = nom;
        this.prix = prix;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    @Override
    public String toString() {
        return nom + " (" + prix + "€)";
    }
}
