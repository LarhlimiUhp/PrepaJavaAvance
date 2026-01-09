
## Support de Cours : Introduction à JavaFX avec Projet "Produit & Catalogue"

### Objectifs du cours
- Comprendre l'architecture de JavaFX.
- Apprendre à créer une interface graphique en Java.
- Appliquer le modèle MVC à un projet concret.
- Savoir manipuler les fichiers FXML, les contrôleurs et les modèles de données.

---

## 1. Introduction à JavaFX

JavaFX est une bibliothèque Java moderne destinée à la création d'interfaces graphiques (GUI) riches, réactives et maintenables. Elle succède à Swing.

### 1.1 Architecture de JavaFX
- **Prism** : moteur de rendu graphique performant. Il se charge de convertir les nœuds du Scene Graph en pixels affichables à l’écran.
- **Glass Windowing Toolkit** : couche d’abstraction entre le système d’exploitation et JavaFX, qui gère les fenêtres, les événements clavier et souris.
- **Scene Graph** : modèle hiérarchique d’objets graphiques. Chaque élément de l’interface est un nœud du graphe (ex : bouton, champ de texte, image).

### 1.2 Points forts
- Séparation claire entre la logique métier et l’interface graphique grâce à l’usage de FXML.
- Compatibilité avec CSS pour styliser l’interface.
- Programmation orientée objet, modulaire et moderne.

---

## 2. Projet : Gestion de Stock "Produit & Catalogue"

Le projet permet de gérer des produits (nom, prix, quantité) organisés en catalogues. Il respecte le modèle d’architecture MVC : Modèle - Vue - Contrôleur.

### 2.1 Modèles de Données

#### Produit.java
Classe représentant un produit avec les propriétés suivantes :
- `id` : identifiant unique du produit.
- `designation` : nom ou description.
- `prix` : prix unitaire.
- `quantite` : nombre d’unités disponibles.
```java
public class Produit {
    private int id;
    private String designation;
    private double prix;
    private int quantite;
    // constructeurs, getters, setters, toString()
}
```

#### Catalogue.java
Classe contenant un groupe de produits.
- Chaque catalogue a un `id`, un `nom`, et une liste de `Produit`.
```java
public class Catalogue {
    private int id;
    private String nom;
    private List<Produit> produits = new ArrayList<>();
    // constructeurs, getters, setters
}
```

### 2.2 Interface Utilisateur (FXML)
FXML est un langage XML qui permet de décrire l’interface utilisateur sans écrire de code Java.

#### MainView.fxml
- Utilise un `BorderPane` pour structurer la page principale.
- La partie gauche contient une `VBox` avec deux boutons de navigation : "Gestion Produits" et "Gestion Catalogues".

#### ProduitView.fxml
- Affiche une interface avec :
  - Trois champs `TextField` pour désignation, prix et quantité.
  - Deux boutons : Ajouter / Supprimer un produit.
  - Un tableau `TableView` pour afficher les produits enregistrés.

### 2.3 Contrôleurs Java

Les contrôleurs sont des classes Java qui contiennent la logique associée aux vues FXML.

#### ProduitController.java
Contient les méthodes d’interaction avec la vue Produit :
- `initialize()` : initialise les colonnes du tableau avec les données du modèle.
- `addProduit()` : ajoute un produit à la liste observable.
- `deleteProduit()` : supprime le produit sélectionné dans la table.
```java
@FXML
private void addProduit() {
    String des = txtDesignation.getText();
    double prix = Double.parseDouble(txtPrix.getText());
    int qte = Integer.parseInt(txtQuantite.getText());
    produits.add(new Produit(currentId++, des, prix, qte));
}
```

#### MainController.java
Gère la navigation entre les vues grâce à la méthode `loadView()`.
```java
@FXML
private void handleShowProduit() {
    loadView("/fxml/ProduitView.fxml");
}
```

### 2.4 Point d'Entrée : MainApp.java
Classe principale qui lance l'application JavaFX.
- Utilise `FXMLLoader` pour charger le fichier MainView.fxml.
```java
public class MainApp extends Application {
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    public static void main(String[] args) { launch(args); }
}
```

---

## 3. Architecture MVC (Modèle - Vue - Contrôleur)

| Composant      | Rôle                                       | Exemple                        |
|----------------|---------------------------------------------|--------------------------------|
| Modèle         | Gère les données                         | Produit.java, Catalogue.java   |
| Vue            | Interface utilisateur (design)             | ProduitView.fxml               |
| Contrôleur     | Logique, traitement des actions utilisateur| ProduitController.java         |

---

## 4. Démarche Complète de Réalisation sur IntelliJ IDEA

Voici une procédure détaillée pour créer ce projet dans l’environnement IntelliJ IDEA :

### Étape 1 : Préparer IntelliJ IDEA
1. Télécharger et installer IntelliJ IDEA (Community ou Ultimate).
2. S’assurer que le **JDK 17+** est installé.
3. Installer JavaFX SDK depuis : https://openjfx.io

### Étape 2 : Créer un nouveau projet JavaFX
1. Ouvrir IntelliJ > `New Project`.
2. Choisir `Java` > `JavaFX` dans la liste (ou Java si JavaFX n'apparaît pas).
3. Donner un nom au projet (ex : `GestionStockJavaFX`).
4. Cliquer sur `Next` > `Finish`.

### Étape 3 : Configurer le projet
1. Aller dans `File > Project Structure > Libraries`.
2. Cliquer sur `+` > `Java` et sélectionner le dossier `lib` du JavaFX SDK.
3. Valider l’ajout.
4. Aller dans `Run > Edit Configurations`, puis ajouter les **VM options** suivantes :
```bash
--module-path "chemin/vers/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
```

### Étape 4 : Organiser le projet
Créer les dossiers suivants :
```
/src
  /com/projet/javafx
    MainApp.java
  /com/projet/javafx/model
    Produit.java
    Catalogue.java
  /com/projet/javafx/controller
    MainController.java
    ProduitController.java
/resources/fxml
  MainView.fxml
  ProduitView.fxml
```

### Étape 5 : Écrire les fichiers
- Copier les fichiers `.java` et `.fxml` donnés dans ce cours dans les bons dossiers.
- Vérifier que chaque fichier FXML a bien un attribut `fx:controller` pointant vers la bonne classe Java.

### Étape 6 : Lancer l’application
- Cliquer sur `Run > Run MainApp`.
- Vérifier que la fenêtre principale s'affiche.
- Tester les fonctionnalités : ajout et suppression de produits.

---

## 5. Conclusion

JavaFX permet de créer des interfaces modernes et maintenables grâce à la séparation logique/vue. Le projet présenté illustre parfaitement l'application du modèle MVC.

### Pour aller plus loin
- Documentation JavaFX : https://openjfx.io
- Tutoriels officiels : https://docs.oracle.com/javafx/
- Cours complets sur OpenClassrooms, Udemy, etc.
