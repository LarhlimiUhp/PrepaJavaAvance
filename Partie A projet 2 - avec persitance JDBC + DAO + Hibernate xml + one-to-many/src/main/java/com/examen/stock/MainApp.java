package com.examen.stock;

import com.examen.stock.dao.IDao;
import com.examen.stock.dao.ProduitDaoImpl;
import com.examen.stock.dao.ProduitDaoHibernate;
import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.model.Catalogue;
import com.examen.stock.service.ProduitService;
import com.examen.stock.service.GestionnaireStock;
import com.examen.stock.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

            // 4. TEST RELATION ONE-TO-MANY (Catalogue)
            System.out.println("\n=== 4. TEST RELATION ONE-TO-MANY (Catalogue) ===");
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();

                Catalogue cat = new Catalogue("Périphériques Gaming");
                cat.ajouterProduit(new Produit("Souris Optique", 45.0));
                cat.ajouterProduit(new Produit("Clavier Mécanique", 85.0));

                session.persist(cat); // Persiste le catalogue et ses produits (Cascade)
                tx.commit();

                System.out.println("Catalogue '" + cat.getNom() + "' enregistré avec succès !");
                System.out.println("Produits dans ce catalogue :");
                cat.getProduits().forEach(p -> System.out.println(" - " + p.getNom() + " (" + p.getPrix() + "€)"));
            }

        } catch (StockException e) {
            System.err.println("Erreur ORM : " + e.getMessage());
        } finally {
            // Fermeture proprement de la SessionFactory Hibernate
            HibernateUtil.shutdown();
        }
    }
}
