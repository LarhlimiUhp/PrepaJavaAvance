# Compte Rendu : Projet JavaFX - Gestion de Stock (Version Complète)

Ce document présente une synthèse théorique sur JavaFX ainsi qu'une explication détaillée de l'implémentation du projet "Produit & Catalogue", incluant l'intégralité du code source.

---

## 1. Partie Cours : Présentation de JavaFX

JavaFX est une bibliothèque Java moderne utilisée pour créer des interfaces graphiques (GUI) riches et performantes.

### 1.1 Architecture de JavaFX
- **Prism** : Moteur de rendu graphique.
- **Glass Windowing Toolkit** : Gestion des fenêtres et événements système.
- **Scene Graph** : Structure hiérarchique des éléments visuels.

---

## 2. Structure et Code Source du Projet

### 2.1 Modèles de Données (Concepts & Code)

Les modèles sont des classes Java simples (POJO) qui encapsulent les données de notre application.

#### Produit.java
Ce fichier définit ce qu'est un produit dans notre système.
```java
package com.projet.javafx.model;

public class Produit {
    private int id;
    private String designation;
    private double prix;
    private int quantite;

    public Produit() {}

    public Produit(int id, String designation, double prix, int quantite) {
        this.id = id;
        this.designation = designation;
        this.prix = prix;
        this.quantite = quantite;
    }

    // Getters et Setters nécessaires pour que la TableView JavaFX puisse lire les données
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    @Override
    public String toString() {
        return designation + " (" + prix + " €)";
    }
}
```

#### Catalogue.java
Permet de regrouper des produits.
```java
package com.projet.javafx.model;

import java.util.ArrayList;
import java.util.List;

public class Catalogue {
    private int id;
    private String nom;
    private List<Produit> produits; // Relation : Un catalogue contient plusieurs produits

    public Catalogue() {
        this.produits = new ArrayList<>();
    }

    public Catalogue(int id, String nom) {
        this.id = id;
        this.nom = nom;
        this.produits = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public List<Produit> getProduits() { return produits; }

    @Override
    public String toString() {
        return nom; // Utilisé par la ListView pour l'affichage par défaut
    }
}
```

---

### 2.2 Vues (FXML)

Les fichiers FXML séparent le design du code logique. Ils utilisent une syntaxe proche du HTML/XML.

#### MainView.fxml (Navigation)
Définit la page maîtresse avec un menu à gauche.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.text.Font?>

<BorderPane fx:id="mainPane" prefHeight="600.0" prefWidth="800.0" xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.projet.javafx.controller.MainController">
   <left>
      <VBox prefHeight="600.0" prefWidth="200.0" spacing="10.0" style="-fx-background-color: #2c3e50;" BorderPane.alignment="CENTER">
         <children>
            <Label text="Gestion Stock" textFill="WHITE">
               <font><Font name="System Bold" size="18.0" /></font>
               <VBox.margin><Insets bottom="20.0" left="10.0" top="20.0" /></VBox.margin>
            </Label>
            <!-- Boutons de navigation appelant les méthodes du contrôleur -->
            <Button fx:id="btnProduit" maxWidth="1.7976931348623157E308" onAction="#handleShowProduit" style="-fx-background-color: #34495e; -fx-text-fill: white;" text="Gestion Produits" />
            <Button fx:id="btnCatalogue" maxWidth="1.7976931348623157E308" onAction="#handleShowCatalogue" style="-fx-background-color: #34495e; -fx-text-fill: white;" text="Gestion Catalogues" />
         </children>
      </VBox>
   </left>
</BorderPane>
```

#### ProduitView.fxml (Gestion de Produits)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.text.Font?>

<VBox prefHeight="600.0" prefWidth="600.0" spacing="20.0" xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.projet.javafx.controller.ProduitController">
   <children>
      <Label text="Gestion des Produits">
         <font><Font name="System Bold" size="24.0" /></font>
      </Label>
      <!-- Formulaire de saisie -->
      <HBox spacing="10.0">
         <children>
            <VBox spacing="5.0"><Label text="Désignation" /><TextField fx:id="txtDesignation" /></VBox>
            <VBox spacing="5.0"><Label text="Prix" /><TextField fx:id="txtPrix" /></VBox>
            <VBox spacing="5.0"><Label text="Quantité" /><TextField fx:id="txtQuantite" /></VBox>
         </children>
      </HBox>
      <HBox spacing="10.0">
         <Button onAction="#addProduit" style="-fx-background-color: #27ae60; -fx-text-fill: white;" text="Ajouter" />
         <Button onAction="#deleteProduit" style="-fx-background-color: #e74c3c; -fx-text-fill: white;" text="Supprimer" />
      </HBox>
      <!-- Tableau d'affichage -->
      <TableView fx:id="tableProduits" VBox.vgrow="ALWAYS">
        <columns>
          <TableColumn fx:id="colId" text="ID" />
          <TableColumn fx:id="colDesignation" prefWidth="250.0" text="Désignation" />
          <TableColumn fx:id="colPrix" text="Prix" />
          <TableColumn fx:id="colQuantite" text="Quantité" />
        </columns>
      </TableView>
   </children>
   <padding><Insets bottom="20.0" left="20.0" right="20.0" top="20.0" /></padding>
</VBox>
```

---

### 2.3 Contrôleurs (Logique Applicative)

Les contrôleurs contiennent le code Java qui s'exécute en réponse aux actions dans l'interface FXML.

#### MainController.java (Gestion de la Navigation)
```java
package com.projet.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MainController {
    @FXML private BorderPane mainPane; // Accès au conteneur défini dans MainView.fxml

    @FXML
    public void initialize() {
        handleShowProduit(); // Charger la vue produit au démarrage
    }

    @FXML
    private void handleShowProduit() {
        loadView("/fxml/ProduitView.fxml");
    }

    @FXML
    private void handleShowCatalogue() {
        loadView("/fxml/CatalogueView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            // FXMLLoader est l'outil qui transforme le XML en objets JavaFX
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainPane.setCenter(view); // On place la nouvelle vue au centre du BorderPane
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### ProduitController.java (Interaction avec les Données)
```java
package com.projet.javafx.controller;

import com.projet.javafx.model.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProduitController {
    @FXML private TextField txtDesignation, txtPrix, txtQuantite;
    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, Integer> colId, colQuantite;
    @FXML private TableColumn<Produit, String> colDesignation;
    @FXML private TableColumn<Produit, Double> colPrix;

    // ObservableList : Liste spéciale qui notifie l'interface lors d'un changement
    private ObservableList<Produit> produits = FXCollections.observableArrayList();
    private int currentId = 1;

    @FXML
    public void initialize() {
        // Lier les colonnes du tableau aux attributs de la classe Produit
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        tableProduits.setItems(produits); // Attacher la liste au tableau
    }

    @FXML
    private void addProduit() {
        try {
            String des = txtDesignation.getText();
            double prix = Double.parseDouble(txtPrix.getText());
            int qte = Integer.parseInt(txtQuantite.getText());

            produits.add(new Produit(currentId++, des, prix, qte));
            txtDesignation.clear(); txtPrix.clear(); txtQuantite.clear();
        } catch (NumberFormatException e) {
            // Gestion d'erreur simplifiée
            System.err.println("Entrée invalide !");
        }
    }

    @FXML
    private void deleteProduit() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected != null) {
            produits.remove(selected);
        }
    }
}
```

---

### 2.4 Point d'Entrée : MainApp.java

C'est ici que l'application démarre.
```java
package com.projet.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargement du layout principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        
        primaryStage.setTitle("Système de Gestion de Stock");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Appel à la méthode interne de JavaFX
    }
}
```

### 2.5 Résolution du Problème de Runtime (Launcher.java)

Depuis Java 11, JavaFX n'est plus inclus dans le JDK. Si l'on lance directement la classe qui hérite de `Application`, la JVM vérifie la présence des modules JavaFX avant même de charger les dépendances Maven, ce qui provoque l'erreur "Composants d'exécution manquants".

La solution est de créer une classe `Launcher` simple qui ne dépend pas de JavaFX pour appeler la classe principale.

```java
package com.projet.javafx;

public class Launcher {
    public static void main(String[] args) {
        // Appelle le main de MainApp sans hériter d'Application
        MainApp.main(args);
    }
}
```

---

## 3. Comment exécuter le projet ?

Pour lancer l'application correctement, il existe deux méthodes :

### Méthode A : Via Maven (Recommandé)
Ouvrez un terminal dans le dossier du projet et tapez :
```bash
mvn javafx:run
```

### Méthode B : Via l'IDE (Eclipse / VS Code / IntelliJ)
Au lieu de lancer `MainApp.java`, lancez la classe **`Launcher.java`**. Cela permettra de charger correctement les bibliothèques JavaFX incluses dans le fichier `pom.xml`.

---

## Conclusion
Ce projet montre comment structurer une application JavaFX moderne avec Maven, en utilisant le pattern MVC et en contournant les contraintes de déploiement liées aux versions récentes de Java.
