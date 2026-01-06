package com.examen.stock.view;

import com.examen.stock.dao.ProduitDaoHibernate;
import com.examen.stock.model.Produit;
import com.examen.stock.service.ProduitService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProduitController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField prixField;
    @FXML
    private TableView<Produit> produitTable;
    @FXML
    private TableColumn<Produit, String> colNom;
    @FXML
    private TableColumn<Produit, Double> colPrix;

    private ProduitService produitService = new ProduitService(new ProduitDaoHibernate());
    private ObservableList<Produit> produitList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        handleRefresh();
    }

    @FXML
    public void handleAjouterProduit() {
        try {
            String nom = nomField.getText();
            double prix = Double.parseDouble(prixField.getText());
            produitService.enregistrerProduit(new Produit(nom, prix));
            handleRefresh();
            nomField.clear();
            prixField.clear();
        } catch (Exception e) {
            System.err.println("Erreur ajout produit: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        produitList.setAll(produitService.recupererTout());
        produitTable.setItems(produitList);
    }

    @FXML
    public void handleSupprimerProduit() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Logique de suppression à implémenter si besoin dans le DAO
            System.out.println("Suppression demandée pour: " + selected.getNom());
        }
    }
}
