package com.examen.stock.service;

import com.examen.stock.dao.IDao;
import com.examen.stock.exception.StockException;
import com.examen.stock.model.Catalogue;
import java.util.List;

public class CatalogueService {
    private IDao<Catalogue> dao;

    public CatalogueService(IDao<Catalogue> dao) {
        this.dao = dao;
    }

    public void creerCatalogue(Catalogue c) {
        dao.create(c);
    }

    public List<Catalogue> listerCatalogues() {
        return dao.readAll();
    }

    public Catalogue chercherParNom(String nom) throws StockException {
        return dao.readByName(nom);
    }

    public void supprimerCatalogue(String nom) throws StockException {
        dao.delete(nom);
    }

    public void afficherTousLesCatalogues() {
        List<Catalogue> catalogues = dao.readAll();
        if (catalogues.isEmpty()) {
            System.out.println("Service: Aucun catalogue disponible.");
            return;
        }
        System.out.println("\n--- LISTE DES CATALOGUES ---");
        catalogues.forEach(System.out::println);
    }
}
