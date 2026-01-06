package com.examen.stock.view;

import com.examen.stock.dao.CatalogueDaoHibernate;
import com.examen.stock.model.Catalogue;
import com.examen.stock.service.CatalogueService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CatalogueController {
    @FXML
    private TextField catalogueNomField;
    @FXML
    private TableView<Catalogue> catalogueTable;
    @FXML
    private TableColumn<Catalogue, Integer> colId;
    @FXML
    private TableColumn<Catalogue, String> colNom;

    private CatalogueService catalogueService = new CatalogueService(new CatalogueDaoHibernate());
    private ObservableList<Catalogue> catalogueList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        handleRefresh();
    }

    @FXML
    public void handleAjouterCatalogue() {
        try {
            String nom = catalogueNomField.getText();
            catalogueService.creerCatalogue(new Catalogue(nom));
            handleRefresh();
            catalogueNomField.clear();
        } catch (Exception e) {
            System.err.println("Erreur ajout catalogue: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        catalogueList.setAll(catalogueService.listerCatalogues());
        catalogueTable.setItems(catalogueList);
    }

    @FXML
    public void handleVoirDetails() {
        Catalogue selected = catalogueTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println(
                    "Détails du catalogue: " + selected.getNom() + " (" + selected.getProduits().size() + " produits)");
        }
    }
}
