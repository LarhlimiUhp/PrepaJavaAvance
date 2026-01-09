package com.projet.javafx.controller;

import com.projet.javafx.model.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProduitController {

    @FXML
    private TextField txtDesignation;
    @FXML
    private TextField txtPrix;
    @FXML
    private TextField txtQuantite;
    @FXML
    private TableView<Produit> tableProduits;
    @FXML
    private TableColumn<Produit, Integer> colId;
    @FXML
    private TableColumn<Produit, String> colDesignation;
    @FXML
    private TableColumn<Produit, Double> colPrix;
    @FXML
    private TableColumn<Produit, Integer> colQuantite;

    private ObservableList<Produit> produits = FXCollections.observableArrayList();
    private int currentId = 1;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        tableProduits.setItems(produits);
    }

    @FXML
    private void addProduit() {
        String designation = txtDesignation.getText();
        double prix = Double.parseDouble(txtPrix.getText());
        int quantite = Integer.parseInt(txtQuantite.getText());

        Produit p = new Produit(currentId++, designation, prix, quantite);
        produits.add(p);
        clearFields();
    }

    @FXML
    private void deleteProduit() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected != null) {
            produits.remove(selected);
        }
    }

    private void clearFields() {
        txtDesignation.clear();
        txtPrix.clear();
        txtQuantite.clear();
    }
}
