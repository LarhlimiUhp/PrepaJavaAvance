package com.examen.stock.model;

import java.util.HashSet;
import java.util.Set;

public class Catalogue {
    private int id;
    private String nom;
    private Set<Produit> produits = new HashSet<>();

    // Constructeur par défaut pour Hibernate
    public Catalogue() {
    }

    public Catalogue(String nom) {
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Set<Produit> getProduits() {
        return produits;
    }

    public void setProduits(Set<Produit> produits) {
        this.produits = produits;
    }

    /**
     * Méthode utilitaire pour ajouter un produit au catalogue
     * en maintenant la cohérence bidirectionnelle.
     */
    public void ajouterProduit(Produit p) {
        this.produits.add(p);
        p.setCatalogue(this);
    }

    @Override
    public String toString() {
        return "Catalogue{" + "id=" + id + ", nom='" + nom + '\'' + ", nbProduits=" + produits.size() + '}';
    }
}
