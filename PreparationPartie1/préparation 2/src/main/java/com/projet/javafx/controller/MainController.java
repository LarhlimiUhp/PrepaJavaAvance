package com.projet.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainPane;

    @FXML
    public void initialize() {
        // Optionnel : charger la vue produit par défaut
        handleShowProduit();
    }

    @FXML
    private void handleShowProduit() {
        loadView("/fxml/ProduitView.fxml");
    }
    @FXML
    private void message() {
        loadView("/fxml/ProduitView.fxml");
    }

    @FXML
    private void handleShowCatalogue() {
        loadView("/fxml/CatalogueView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
