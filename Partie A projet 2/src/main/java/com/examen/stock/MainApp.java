package com.examen.stock;

import com.examen.stock.dao.ProduitDaoImpl;
import com.examen.stock.dao.ProduitDaoHibernate;
import com.examen.stock.dao.CatalogueDaoHibernate;
import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.model.Catalogue;
import com.examen.stock.service.ProduitService;
import com.examen.stock.service.CatalogueService;
import com.examen.stock.service.GestionnaireStock;
import com.examen.stock.service.StockSimulationService;
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

            // 4. TEST RELATION ONE-TO-MANY (Catalogue via DAO)
            System.out.println("\n=== 4. TEST RELATION ONE-TO-MANY (Catalogue via DAO) ===");
            CatalogueService catService = new CatalogueService(new CatalogueDaoHibernate());

            Catalogue cat = new Catalogue("Matériel Bureau 2026");
            cat.ajouterProduit(new Produit("Chaise Ergonomique", 250.0));
            cat.ajouterProduit(new Produit("Bureau Assis-Debout", 450.0));

            catService.creerCatalogue(cat); // Persiste via le service
            catService.afficherTousLesCatalogues();

            // 5. TEST PROGRAMMATION CONCURRENTE (Threads)
            System.out.println("\n=== 5. TEST PROGRAMMATION CONCURRENTE (Threads) ===");
            StockSimulationService simulation = new StockSimulationService(serviceHibernate);
            Thread t = new Thread(simulation);
            t.start(); // Lance le processus en arrière-plan

            System.out.println("[Main] Le thread de simulation tourne en parallèle...");

            // On attend un peu pour voir le thread travailler avant de fermer l'app
            Thread.sleep(10000);
            simulation.stopSimulation();
            t.join(); // Attend la fin du thread proprement

        } catch (StockException e) {
            System.err.println("Erreur ORM : " + e.getMessage());
        } catch (InterruptedException e) { // Added catch for InterruptedException
            System.err.println("Erreur de thread : " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        } finally {
            // Fermeture proprement de la SessionFactory Hibernate
            HibernateUtil.shutdown();
        }
    }
}
