package com.examen.stock;

import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.repository.ProduitJDBCRepository;
import com.examen.stock.repository.Repository;
import com.examen.stock.service.GestionnaireStock;
import java.util.List;
import java.util.stream.Collectors;

public class MainApp {
    public static void main(String[] args) {
        // --- TEST VERSION MÉMOIRE ---
        System.out.println("=== TEST VERSION MÉMOIRE ===");
        GestionnaireStock<Produit> monStock = new GestionnaireStock<>();
        monStock.ajouter(new Produit("Ordinateur", 1200.0));
        monStock.ajouter(new Produit("Souris", 25.0));
        monStock.ajouter(new Produit("Clavier", 45.0));

        try {
            System.out.println("\nRecherche d'un produit... Clavier");
            Produit p = monStock.trouverParNom("Clavier");
            System.out.println(p);
        } catch (StockException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        System.out.println("\nProduits de plus de 40€ :");
        monStock.filtrerParPrixMin(40).forEach(System.out::println);

        // Test Update/Delete Mémoire
        try {
            System.out.println("\n--- Test Update/Delete Mémoire ---");
            monStock.mettreAJour(new Produit("Souris", 30.0));
            monStock.supprimer("Ordinateur");
            monStock.listerTout().forEach(System.out::println);
        } catch (StockException e) {
            System.err.println("Erreur Mémoire : " + e.getMessage());
        }

        // --- TEST VERSION JDBC (MySQL) ---
        System.out.println("\n=== TEST VERSION JDBC (MySQL) ===");
        Repository<Produit> jdbcRepo = new ProduitJDBCRepository();

        // Ajout dans la DB
        jdbcRepo.ajouter(new Produit("Ecran", 150.0));
        jdbcRepo.ajouter(new Produit("Casque", 80.0));
        jdbcRepo.ajouter(new Produit("Tour", 80.0));
        jdbcRepo.ajouter(new Produit("Casque", 180.0));

        // Test Update/Delete JDBC
        try {
            System.out.println("\n--- Test Update/Delete JDBC ---");
            jdbcRepo.mettreAJour(new Produit("Ecran", 140.0));
            jdbcRepo.supprimer("Tour");
        } catch (StockException e) {
            System.err.println("Erreur JDBC : " + e.getMessage());
        }

        // Lister le contenu de la DB
        System.out.println("\nContenu de la base de données :");
        jdbcRepo.listerTout().forEach(System.out::println);

        // Recherche dans la DB
        try {
            System.out.println("\nRecherche JDBC... Ecran");
            Produit p = jdbcRepo.trouverParNom("Ecran");
            System.out.println("Trouvé : " + p);
        } catch (StockException e) {
            System.err.println("Erreur JDBC : " + e.getMessage());
        }

        // --- ANALYSE RICHE AVEC STREAMS (Sur données JDBC) ---
        System.out.println("\n--- ANALYSE TECHNIQUE DU STOCK (JDBC + STREAMS) ---");
        List<Produit> dbStock = jdbcRepo.listerTout();

        if (!dbStock.isEmpty()) {
            // 1. Calcul de la valeur totale du stock
            double valeurTotale = dbStock.stream()
                    .mapToDouble(Produit::getPrix)
                    .sum();
            System.out.println("Valeur totale du stock : " + valeurTotale + "€");

            // 2. Prix moyen
            dbStock.stream()
                    .mapToDouble(Produit::getPrix)
                    .average()
                    .ifPresent(avg -> System.out.printf("Prix moyen des produits : %.2f€\n", avg));

            // 3. Le produit le plus cher
            dbStock.stream()
                    .max((p1, p2) -> Double.compare(p1.getPrix(), p2.getPrix()))
                    .ifPresent(p -> System.out.println("Produit le plus cher : " + p));

            // 4. Liste des noms de produits (Triés et formatés)
            String catalogue = dbStock.stream()
                    .map(Produit::getNom)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(", "));
            System.out.println("Catalogue unique (trié) : " + catalogue);
        }
    }
}
