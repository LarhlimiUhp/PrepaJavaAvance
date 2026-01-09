package com.projet.javafx.model;

import java.util.ArrayList;
import java.util.List;

public class Catalogue {
    private int id;
    private String nom;
    private List<Produit> produits;

    public Catalogue() {
        this.produits = new ArrayList<>();
    }

    public Catalogue(int id, String nom) {
        this.id = id;
        this.nom = nom;
        this.produits = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public List<Produit> getProduits() { return produits; }
    public void setProduits(List<Produit> produits) { this.produits = produits; }

    public void addProduit(Produit p) {
        this.produits.add(p);
    }

    @Override
    public String toString() {
        return nom;
    }
}
