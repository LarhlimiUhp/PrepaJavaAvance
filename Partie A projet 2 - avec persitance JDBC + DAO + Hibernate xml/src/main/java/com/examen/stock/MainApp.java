package com.examen.stock;

import com.examen.stock.dao.IDao;
import com.examen.stock.dao.ProduitDaoImpl;
import com.examen.stock.dao.ProduitDaoHibernate;
import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.service.ProduitService;
import com.examen.stock.service.GestionnaireStock;
import com.examen.stock.util.HibernateUtil;

public class MainApp {
    public static void main(String[] args) {
        // 1. ANCIENNE APPROCHE (Mémoire)
        System.out.println("=== 1. MODE MÉMOIRE (Ancien) ===");
        GestionnaireStock<Produit> monStock = new GestionnaireStock<>();
        monStock.ajouter(new Produit("Ordinateur", 1000.0));
        monStock.ajouter(new Produit("Souris", 20.0));

        // 2. ARCHITECTURE JDBC (DAO Classique)
        System.out.println("\n=== 2. ARCHITECTURE JDBC (DAO Classique) ===");
        ProduitService serviceJdbc = new ProduitService(new ProduitDaoImpl());
        serviceJdbc.enregistrerProduit(new Produit("Micro-Casque", 45.0));
        serviceJdbc.recupererTout().forEach(System.out::println);

        // 3. MAPPING OBJET RELATIONNEL (Hibernate XML)
        System.out.println("\n=== 3. MAPPING OBJET RELATIONNEL (Hibernate XML) ===");
        try {
            ProduitService serviceHibernate = new ProduitService(new ProduitDaoHibernate());

            // Test CRUD via Hibernate
            serviceHibernate.enregistrerProduit(new Produit("Clavier RGB 2025", 120.0));
            serviceHibernate.modifierPrix("Clavier RGB 2025", 115.0);

            System.out.println("Données via Hibernate :");
            serviceHibernate.recupererTout().forEach(p -> System.out.println(p));

            // Statistiques riches (Streams)
            serviceHibernate.afficherStatistiques();

        } catch (StockException e) {
            System.err.println("Erreur ORM : " + e.getMessage());
        } finally {
            // Fermeture proprement de la SessionFactory Hibernate
            HibernateUtil.shutdown();
        }
    }
}
