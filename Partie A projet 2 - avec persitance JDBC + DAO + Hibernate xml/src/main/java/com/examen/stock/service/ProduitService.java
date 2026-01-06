package com.examen.stock.service;

import com.examen.stock.dao.IDao;
import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import java.util.List;
import java.util.stream.Collectors;

public class ProduitService {
    private IDao<Produit> dao;

    public ProduitService(IDao<Produit> dao) {
        this.dao = dao;
    }

    public void enregistrerProduit(Produit p) {
        dao.create(p);
    }

    public List<Produit> recupererTout() {
        return dao.readAll();
    }

    public void modifierPrix(String nom, double nouveauPrix) throws StockException {
        Produit p = dao.readByName(nom);
        dao.update(new Produit(p.getNom(), nouveauPrix));
    }

    public void retirerProduit(String nom) throws StockException {
        dao.delete(nom);
    }

    // Analyse avec Streams
    public void afficherStatistiques() {
        List<Produit> produits = dao.readAll();
        if (produits.isEmpty()) {
            System.out.println("Service: Aucune donnée pour les statistiques.");
            return;
        }

        System.out.println("\n--- STATISTIQUES SERVICE (VIA DAO) ---");
        double total = produits.stream().mapToDouble(Produit::getPrix).sum();
        double moyenne = produits.stream().mapToDouble(Produit::getPrix).average().orElse(0);

        System.out.println("Valeur Valeur Stock : " + total + "€");
        System.out.printf("Prix Moyen : %.2f€\n", moyenne);

        String catalogue = produits.stream()
                .map(Produit::getNom)
                .distinct()
                .sorted()
                .collect(Collectors.joining(" | "));
        System.out.println("Catalogue Service : " + catalogue);
    }
}
