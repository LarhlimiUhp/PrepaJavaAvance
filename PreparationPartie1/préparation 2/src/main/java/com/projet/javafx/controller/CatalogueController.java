package com.projet.javafx.controller;

import com.projet.javafx.model.Catalogue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class CatalogueController {

    @FXML
    private TextField txtNom;
    @FXML
    private ListView<Catalogue> listCatalogues;

    private ObservableList<Catalogue> catalogues = FXCollections.observableArrayList();
    private int currentId = 1;

    @FXML
    public void initialize() {
        listCatalogues.setItems(catalogues);
    }

    @FXML
    private void addCatalogue() {
        String nom = txtNom.getText();
        if (!nom.isEmpty()) {
            catalogues.add(new Catalogue(currentId++, nom));
            txtNom.clear();
        }
    }
}
