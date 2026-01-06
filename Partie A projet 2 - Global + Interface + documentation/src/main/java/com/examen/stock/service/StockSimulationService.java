package com.examen.stock.service;

import com.examen.stock.model.Produit;
import java.util.Random;

/**
 * Service simulant un réapprovisionnement automatique du stock en arrière-plan.
 * Cette classe implémente Runnable pour être exécutée dans un Thread séparé.
 */
public class StockSimulationService implements Runnable {
    private ProduitService produitService;
    private volatile boolean running = true;
    private Random random = new Random();

    public StockSimulationService(ProduitService produitService) {
        this.produitService = produitService;
    }

    public void stopSimulation() {
        this.running = false;
    }

    @Override
    public void run() {
        System.out.println("[Thread Simulation] Démarrage du réapprovisionnement automatique...");

        int count = 0;
        while (running && count < 3) { // On limite à 3 produits pour la démo
            try {
                // Simulation d'un délai d'attente (ex: réception d'une livraison)
                Thread.sleep(3000);

                String nomProduit = "Produit_Auto_" + (count + 1);
                double prix = 10 + (100 - 10) * random.nextDouble();

                Produit p = new Produit(nomProduit, Math.round(prix * 100.0) / 100.0);

                // Opération de persistance via le service
                produitService.enregistrerProduit(p);

                System.out.println("[Thread Simulation] Nouveau produit livré et enregistré : " + p);
                count++;

            } catch (InterruptedException e) {
                System.err.println("[Thread Simulation] Interruption du thread.");
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("[Thread Simulation] Fin de la simulation.");
    }
}
