# Compte Rendu Détaillé : Préparation Examen Java & Hibernate (Partie A)

## 1. Objectifs et Structure du Projet

### Objectif Global
L'objectif de ce projet est de construire une application de gestion de stock robuste, en partant d'une logique simple en mémoire pour aboutir à une architecture industrielle utilisant un framework de mapping objet-relationnel (ORM) comme **Hibernate**.

### Structure du Projet (Arborescence Maven)
Le projet suit la structure standard Maven pour assurer la séparation des responsabilités :

- `src/main/java` : Code source Java.
    - `com.examen.stock.model` : Les **Entités** (objets métiers).
    - `com.examen.stock.dao` : Couche d'**Accès aux Données** (Data Access Object).
    - `com.examen.stock.service` : Couche **Métier** (logique de gestion).
    - `com.examen.stock.util` : Classes utilitaires (connexion base de données, Hibernate).
    - `com.examen.stock.exception` : Gestion des erreurs spécifiques au domaine.
- `src/main/resources` : Fichiers de configuration et de mapping XML.
- `pom.xml` : Gestion des dépendances (Hibernate, MySQL Connector).

---
### fonction main principale
```java
package com.examen.stock;

import com.examen.stock.dao.ProduitDaoImpl;
import com.examen.stock.dao.ProduitDaoHibernate;
import com.examen.stock.dao.CatalogueDaoHibernate;
import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.model.Catalogue;
import com.examen.stock.service.ProduitService;
import com.examen.stock.service.CatalogueService;
import com.examen.stock.service.GestionnaireStock;
import com.examen.stock.service.StockSimulationService;
import com.examen.stock.util.HibernateUtil;

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

            // 4. TEST RELATION ONE-TO-MANY (Catalogue via DAO)
            System.out.println("\n=== 4. TEST RELATION ONE-TO-MANY (Catalogue via DAO) ===");
            CatalogueService catService = new CatalogueService(new CatalogueDaoHibernate());

            Catalogue cat = new Catalogue("Matériel Bureau 2026");
            cat.ajouterProduit(new Produit("Chaise Ergonomique", 250.0));
            cat.ajouterProduit(new Produit("Bureau Assis-Debout", 450.0));

            catService.creerCatalogue(cat); // Persiste via le service
            catService.afficherTousLesCatalogues();

            // 5. TEST PROGRAMMATION CONCURRENTE (Threads)
            System.out.println("\n=== 5. TEST PROGRAMMATION CONCURRENTE (Threads) ===");
            StockSimulationService simulation = new StockSimulationService(serviceHibernate);
            Thread t = new Thread(simulation);
            t.start(); // Lance le processus en arrière-plan

            System.out.println("[Main] Le thread de simulation tourne en parallèle...");

            // On attend un peu pour voir le thread travailler avant de fermer l'app
            Thread.sleep(10000);
            simulation.stopSimulation();
            t.join(); // Attend la fin du thread proprement

        } catch (StockException e) {
            System.err.println("Erreur ORM : " + e.getMessage());
        } catch (InterruptedException e) { // Added catch for InterruptedException
            System.err.println("Erreur de thread : " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        } finally {
            // Fermeture proprement de la SessionFactory Hibernate
            HibernateUtil.shutdown();
        }
    }
}

```
## 2. Étape 1 : Fondamentaux Java & Gestion en Mémoire

### Objectif
Mettre en place la structure de base sans base de données, en utilisant les **Génériques** et les **Streams**.

### Détails sur les Collections Utilisées
- **`ArrayList<T>`** : Utilisée pour stocker les produits en mémoire.
    - *Pourquoi ?* Elle offre un accès rapide par index et une facilité d'ajout dynamique.

### Code Source Complet : Étape 1

#### 1. Entité Produit (`Produit.java`)
```java
package com.examen.stock.model;

public class Produit {
    private String nom;
    private double prix;
    private Catalogue catalogue;

    public Produit() {}

    public Produit(String nom, double prix) {
        this.nom = nom;
        this.prix = prix;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public Catalogue getCatalogue() { return catalogue; }
    public void setCatalogue(Catalogue catalogue) { this.catalogue = catalogue; }

    @Override
    public String toString() { return nom + " (" + prix + "€)"; }
}
```

#### 2. Exception Personnalisée (`StockException.java`)
```java
package com.examen.stock.exception;

public class StockException extends Exception {
    public StockException(String message) { super(message); }
}
```

#### 3. Interface Repository Générique (`Repository.java`)
```java
package com.examen.stock.repository;

import com.examen.stock.exception.StockException;
import java.util.List;

public interface Repository<T> {
    void ajouter(T element);
    List<T> listerTout();
    T trouverParNom(String nom) throws StockException;
    void mettreAJour(T element) throws StockException;
    void supprimer(String nom) throws StockException;
}
```

#### 4. Gestionnaire de Stock (`GestionnaireStock.java`)
```java
package com.examen.stock.service;

import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.repository.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GestionnaireStock<T extends Produit> implements Repository<T> {
    private List<T> inventaire = new ArrayList<>();

    @Override
    public void ajouter(T element) {
        inventaire.add(element);
        System.out.println("Ajout de : " + element.getNom());
    }

    @Override
    public List<T> listerTout() {
        return new ArrayList<>(inventaire);
    }

    @Override
    public T trouverParNom(String nom) throws StockException {
        return inventaire.stream()
                .filter(p -> p.getNom().equalsIgnoreCase(nom))
                .findFirst()
                .orElseThrow(() -> new StockException("Produit '" + nom + "' introuvable !"));
    }

    @Override
    public void mettreAJour(T element) throws StockException {
        T existant = trouverParNom(element.getNom());
        int index = inventaire.indexOf(existant);
        inventaire.set(index, element);
    }

    @Override
    public void supprimer(String nom) throws StockException {
        T existant = trouverParNom(nom);
        inventaire.remove(existant);
    }

    public List<T> filtrerParPrixMin(double seuil) {
        return inventaire.stream()
                .filter(p -> p.getPrix() >= seuil)
                .collect(Collectors.toList());
    }
}
```
### Analyse ligne par ligne du Stream (`trouverParNom`)
1. **`inventaire.stream()`** : Transforme la collection en un flux d'éléments pour permettre un traitement fonctionnel.
2. **`.filter(p -> ...)`** : Applique un filtre. On compare le nom du produit actuel avec le nom recherché (`equalsIgnoreCase` ignore la casse).
3. **`.findFirst()`** : Récupère le premier élément correspondant sous forme d'un `Optional<T>`.
4. **`.orElseThrow(...)`** : Si l'Optional est vide (produit non trouvé), lance une `StockException` personnalisée avec un message clair.

### Questions types : Étape 1
1. **Q : Pourquoi utiliser `<T extends Produit>` ?**
   - **R :** Pour garantir que le gestionnaire ne manipule que des objets héritant de `Produit`.
2. **Q : Quel est l'avantage du `.stream().filter(...)` ?**
   - **R :** Plus lisible et facilite le filtrage complexe sur les collections.
3. **Q : Pourquoi créer une `StockException` ?**
   - **R :** Pour distinguer les erreurs métier des erreurs système.
4. **Q : Différence entre opération "intermédiaire" (`filter`) et "terminale" (`findFirst`) ?**
   - **R :** L'opération intermédiaire est "paresseuse" (ne fait rien tant que la terminale n'est pas appelée). La terminale déclenche le parcours et produit le résultat.
5. **Q : Pourquoi utiliser `Optional<T>` avec `findFirst` ?**
   - **R :** Pour éviter les `NullPointerException` en fournissant un conteneur sécurisé qui indique si un résultat existe ou non.
6. **Q : Qu'est-ce qu'une expression lambda dans ce contexte ?**
   - **R :** C'est une fonction anonyme permettant de définir un comportement de filtrage de manière concise.

---
### rappel
La syntaxe d'une expression lambda en Java se compose de trois parties principales : les paramètres, la flèche (->) et le corps.

Voici la structure générale :

```java
(parametres) -> { corps_de_la_fonction }
```
1. Les différentes variations de syntaxe
A. Sans paramètres
On utilise des parenthèses vides.

```java
() -> System.out.println("Bonjour !")
```
B. Un seul paramètre (le plus courant)
Les parenthèses sont facultatives si on ne précise pas le type. C'est ce que vous avez dans votre code :

```java
// Avec p comme Produit
p -> p.getNom()
```
C. Plusieurs paramètres
Les parenthèses sont obligatoires.

```java
(a, b) -> a + b
```
D. Préciser le type (optionnel)
On peut forcer le type pour plus de clarté.

```java
(Produit p) -> p.getPrix()
``` 
2. Le Corps de la lambda
Il existe deux types de corps :

Expression simple (une seule ligne) : Pas besoin d'accolades {} ni de mot-clé return. Le résultat est renvoyé automatiquement.
Exemple : p -> p.getPrix() > 10
Bloc de code (plusieurs lignes) : Les accolades {} et le mot-clé return deviennent obligatoires.
```java
p -> {
    double tva = 0.20;
    return p.getPrix() * (1 + tva);
}
```
Rapport avec notre code
Dans notre cas :

```java
p -> p.getNom().equalsIgnoreCase(nom)
```
Paramètre : p (représente un objet de notre liste inventaire).
Flèche : -> sépare la donnée du traitement.
Corps : p.getNom().equalsIgnoreCase(nom) est l'action de comparaison qui renvoie true ou false.

## 3. Étape 2 : Architecture DAO & JDBC

### Objectif :Séparer la logique de persistance du reste du code.
L'utilité de l'Étape 2 (DAO & JDBC) est fondamentale dans le développement d'applications professionnelles. Elle marque le passage d'un prototype "jouet" (en mémoire) à une application réelle.

Voici les 4 avantages principaux du passage à cette architecture :

1. La Persistance des Données (JDBC)
Avant (Étape 1) : Si vous fermiez l'application, tous les produits étaient supprimés (car stockés en RAM).
Maintenant (Étape 2) : Les données sont sauvegardées dans une base de données MySQL. Même si le programme s'arrête ou que l'ordinateur redémarre, le stock reste intact.

2. La Séparation des Responsabilités (Pattern DAO)
Le DAO (Data Access Object) crée une "cloison étanche" entre le code métier et le code technique :
-Le Service ne connaît que les méthodes (ajouter, lister). Il ne sait pas qu'il y a du SQL derrière.
- Le DAO s'occupe uniquement de la "plomberie" vers la base de données.
- Résultat : Si demain vous voulez changer de base de données (passer de MySQL à PostgreSQL), vous ne touchez qu'au DAO, pas une seule ligne du code métier ne change.

3. La Sécurité et Robustesse
Comme nous l'avons vu dans le code du ProduitDaoImpl:

- L'utilisation de PreparedStatement protège contre les injections SQL (piratage).
- Le Try-with-resources évite les plantages du serveur à cause de connexions restées ouvertes par erreur.

4. Transition vers Hibernate
L'étape 2 est une étape d'apprentissage indispensable :

- On comprend comment Java communique avec une base de données.
- On réalise que le code JDBC est répétitif et "verbeux" (beaucoup de lignes pour une simple insertion).
- Cela permet de comprendre pourquoi on utilise Hibernate à l'étape 3 : pour supprimer tout ce code répétitif et laisser le framework s'en occuper.

En résumé pour la suite de l'architecture DAO & JDBC apporte la persistance (sauvegarde réelle), la maintenabilité (code organisé en couches) et une meilleure évolutivité.


### Code Source Complet : Étape 2

#### 1. Script SQL (`schema.sql`)
```sql
CREATE DATABASE IF NOT EXISTS gestion_stock;
USE gestion_stock;

CREATE TABLE IF NOT EXISTS produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prix DOUBLE NOT NULL
);
```

#### 2. Connexion Database (`DatabaseConnection.java`)
```java
package com.examen.stock.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String URL = "jdbc:mysql://" + HOST + ":3306/gestion_stock";
    private static final String USER = "root";
    private static final String PASSWORD = ""; 
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Pilote MySQL non trouvé", e);
            }
        }
        return connection;
    }
}
```
Ce code implémente le Pattern Singleton pour la connexion à la base de données. Son but est de garantir qu'une seule et unique connexion est ouverte et réutilisée par toute l'application, ce qui économise les ressources du système.

Analyse détaillée
1. La condition de vérification
```java
if (connection == null || connection.isClosed()) {
```
connection == null : Vérifie si c'est la toute première fois qu'on demande une connexion.
connection.isClosed() : Vérifie si la connexion existante a été coupée (par exemple après un timeout ou une erreur réseau).
Si l'une de ces conditions est vraie, on doit (re)créer la connexion. Sinon, on saute directement au return connection à la fin (réutilisation).
2. Chargement du Pilote (Driver)
```java
Class.forName("com.mysql.cj.jdbc.Driver");
```
Cette ligne charge dynamiquement en mémoire la classe du pilote MySQL. C'est elle qui permet à Java de "parler" avec MySQL.

Cette ligne de code est historique et cruciale dans le monde du JDBC. Voici son utilité décomposée en 3 points :

1. Chargement dynamique (La Réflexion)
La méthode Class.forName() est une fonctionnalité de Java appelée "Réflexion". Elle permet de charger une classe en mémoire uniquement en connaissant son nom sous forme de texte (
String
).

Ici, on charge la classe du connecteur MySQL (Driver.class) qui se trouve dans le fichier .jar (la dépendance) de votre projet.

2. Auto-enregistrement (Le rôle du Driver)
L'utilité principale n'est pas juste de "charger" la classe, mais de déclencher ce qu'on appelle un bloc statique à l'intérieur du pilote MySQL. Dès que cette classe est chargée en mémoire par Class.forName(), elle s'exécute d'elle-même et dit au compagnon de Java (le DriverManager) :

"Hé ! Je suis le pilote MySQL. Si quelqu'un demande une connexion commençant par jdbc:mysql://, c'est moi qu'il faut utiliser !"

3. Est-ce toujours obligatoire ?
Historiquement : Oui, c'était obligatoire jusqu'à Java 6.
Aujourd'hui : Avec les versions modernes de JDBC (4.0+), Java est capable de scanner automatiquement les fichiers .jar pour trouver les pilotes. La ligne est donc devenue facultative.
Pourquoi on la garde ? : On continue de l'écrire par sécurité et pour la compatibilité, car elle garantit que le pilote est bien présent avant de tenter d'ouvrir la connexion (si le pilote est absent, le code s'arrête immédiatement avec une erreur claire).
### Résumé
Class.forName("...") sert à charger le pilote de la base de données et à l'enregistrer auprès de Java pour qu'il puisse traduire vos commandes Java en langage MySQL.





3. Établissement de la connexion
```java
connection = DriverManager.getConnection(URL, USER, PASSWORD);
```
DriverManager est le service Java qui gère les pilotes.
On lui passe l'URL (qui contient l'adresse du serveur et le nom de la base), ainsi que l'utilisateur et le mot de passe.
4. Gestion des Exceptions
```java
} catch (ClassNotFoundException e) {
    throw new SQLException("Pilote MySQL non trouvé", e);
}
```
Si le fichier .jar du connecteur MySQL est absent du projet, Java lève une ClassNotFoundException.
Ici, on la capture et on la transforme en SQLException pour que le reste de l'application n'ait qu'un seul type d'erreur de base de données à gérer.
Résumé des points clés pour l'examen
Performance : On ne crée pas une nouvelle connexion à chaque requête (Singleton).
Robustesse : La méthode vérifie si la connexion est toujours active avant de la renvoyer.
Sécurité : Les paramètres (URL, USER, PASS) sont centralisés dans une seule classe.
Good

#### 3. Interface DAO (`IDao.java`)
```java
package com.examen.stock.dao;

import com.examen.stock.exception.StockException;
import java.util.List;

public interface IDao<T> {
    void create(T element);
    List<T> readAll();
    T readByName(String nom) throws StockException;
    void update(T element) throws StockException;
    void delete(String nom) throws StockException;
}
```

Cette interface est le contrat de persistance de cette application. Elle définit les règles du jeu pour toute classe qui voudrait gérer le stockage de données (que ce soit via JDBC, Hibernate, ou même un simple fichier).

Voici l'analyse détaillée des concepts clés :

1. L'Abstraction par l'Interface
En déclarant une interface, on dit au reste de l'application : "Peu importe comment vous enregistrez les données (SQL, XML, Mémoire), vous devez obligatoirement fournir ces 5 méthodes".

Utilité : Cela permet de changer d'implémentation (passer de JDBC à Hibernate) sans jamais modifier le code qui appelle ces méthodes.

2. La Généricité (<T>)
L'utilisation de <T> est cruciale. Elle transforme l'interface en un modèle réutilisable :

IDao<Produit> gérera des produits.
IDao<Client> gérera des clients.
On évite ainsi de créer une interface différente pour chaque type d'objet métier.

3. Les Opérations CRUD
L'interface définit le cycle de vie complet d'une donnée (souvent résumé par l'acronyme CRUD):

Create : void create(T element) -> Insérer une nouvelle donnée.
Read :
List<T> readAll() -> Récupérer la liste complète.
T readByName(String nom) -> Rechercher une donnée précise par son identifiant unique.
Update : void update(T element) -> Modifier une donnée existante.
Delete : void delete(String nom) -> Supprimer une donnée.

4. La Gestion des Erreurs (throws StockException)
Remarquez que presque toutes les méthodes déclarent throws StockException.

Pourquoi ? Parce qu'une opération de base de données peut échouer (nom introuvable, mise à jour impossible, suppression d'un objet inexistant).
En l'ajoutant ici, vous forcez les développeurs à gérer ces cas d'erreurs (avec un bloc try/catch) lorsqu'ils utilisent le DAO.
Résumé pour l'examen
L'interface IDao<T> est le pilier de la séparation des couches. Elle garantit la flexibilité du projet (on peut changer de techno de stockage) et la réutilisabilité (on peut l'utiliser pour n'importe quel objet métier grâce aux génériques).

#### 4. Implémentation JDBC (`ProduitDaoImpl.java`)
```java
package com.examen.stock.dao;

import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDaoImpl implements IDao<Produit> {
    @Override
    public void create(Produit p) {
        String sql = "INSERT INTO produits (nom, prix) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPrix());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    @Override
    public List<Produit> readAll() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produits";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Produit(rs.getString("nom"), rs.getDouble("prix")));
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }
    // ... autres méthodes implémentées
}
```
Cette méthode (create) est le cœur de l'insertion de données dans la base via JDBC. Elle illustre trois concepts fondamentaux pour l'examen : le SQL paramétré, le Try-with-resources et la pré-compilation.

Analyse détaillée
1. La requête SQL avec "Placeholders"
```
String sql = "INSERT INTO produits (nom, prix) VALUES (?, ?)";
Les ? sont des points d'interrogation appelés "placeholders".
On ne met jamais les valeurs directement dans la chaîne de caractères (pour éviter les injections SQL). On laisse Java les injecter proprement plus tard.
2. Le Try-with-resources
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {

C'est une structure introduite en Java 7.
Avantages : Elle garantit que la connexion (conn) et la requête (ps) seront automatiquement fermées à la fin du bloc { }, même si une erreur survient. Cela évite les fuites de mémoire (Memory Leaks).

3. Le PreparedStatement
Contrairement à un Statement classique, le PreparedStatement pré-compile la requête SQL sur le serveur de base de données.
C'est plus rapide si on exécute la même requête plusieurs fois et c'est beaucoup plus sûr.

4. Injection des valeurs
```java
ps.setString(1, p.getNom());
ps.setDouble(2, p.getPrix());

Important : Les index commencent à 1 (pas 0).
Le 1er ? reçoit le nom (String).
Le 2ème ? reçoit le prix (Double).
5. Exécution de la mise à jour
```java
ps.executeUpdate();
On utilise executeUpdate() pour les requêtes qui modifient la base (INSERT, UPDATE, DELETE).
Elle renvoie le nombre de lignes modifiées (ici on ne stocke pas le résultat, mais on pourrait).

### Résumé
Sécurité : Utilisation des ? contre les injections SQL.
Gestion des ressources : Utilisation du try(...) pour la fermeture automatique.
Architecture : Cette classe implémente une interface (IDao), ce qui permet de découpler la logique métier de la base de données.

#### 5. Couche Service (`ProduitService.java`)
```java
package com.examen.stock.service;

import com.examen.stock.dao.IDao;
import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import java.util.List;
import java.util.stream.Collectors;

public class ProduitService {
    private IDao<Produit> dao;
    public ProduitService(IDao<Produit> dao) { this.dao = dao; }

    public void enregistrerProduit(Produit p) { dao.create(p); }
    public List<Produit> recupererTout() { return dao.readAll(); }
    
    public void afficherStatistiques() {
        List<Produit> produits = dao.readAll();
        double total = produits.stream().mapToDouble(Produit::getPrix).sum();
        System.out.println("Total Stock : " + total + "€");
    }
}
```


## 4. Étape 3 : Mapping ORM avec Hibernate (XML)

### Objectif
Automatiser la persistance avec Hibernate.

### Mapping Complet (XML & Java)

#### 1. Configuration (`hibernate.cfg.xml`)
```xml
<hibernate-configuration>
    <session-factory>
        <property name="connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="connection.url">jdbc:mysql://localhost:3306/gestion_stock</property>
        <property name="connection.username">root</property>
        <property name="dialect">org.hibernate.dialect.MySQLDialect</property>
        <property name="hbm2ddl.auto">update</property>
        <mapping resource="com/examen/stock/model/Produit.hbm.xml"/>
        <mapping resource="com/examen/stock/model/Catalogue.hbm.xml"/>
    </session-factory>
</hibernate-configuration>
```

#### 2. Mapping Produit (`Produit.hbm.xml`)
```xml
<hibernate-mapping package="com.examen.stock.model">
    <class name="Produit" table="produits">
        <id name="nom" column="nom">
            <generator class="assigned"/>
        </id>
        <property name="prix" column="prix" type="double"/>
        <many-to-one name="catalogue" column="catalogue_id" class="com.examen.stock.model.Catalogue" />
    </class>
</hibernate-mapping>
```

#### 3. Utilitaire Hibernate (`HibernateUtil.java`)
```java
package com.examen.stock.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Détection dynamique de l'hôte (Local vs Docker)
            String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
            String url = "jdbc:mysql://" + host + ":3306/gestion_stock";

            Configuration configuration = new Configuration().configure();
            configuration.setProperty("hibernate.connection.url", url);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() { return sessionFactory; }
    public static void shutdown() { getSessionFactory().close(); }
}
```

#### 4. DAO Hibernate (`ProduitDaoHibernate.java`)
```java
// Contenu de ProduitDaoHibernate...
```

#### 5. DAO Catalogue (`CatalogueDaoHibernate.java`) [NEW]
```java
package com.examen.stock.dao;

import com.examen.stock.exception.StockException;
import com.examen.stock.model.Catalogue;
import com.examen.stock.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class CatalogueDaoHibernate implements IDao<Catalogue> {
    @Override
    public void create(Catalogue catalogue) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(catalogue);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
        }
    }
    @Override
    public List<Catalogue> readAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Catalogue", Catalogue.class).list();
        } catch (Exception e) { return List.of(); }
    }
    @Override
    public Catalogue readByName(String nom) throws StockException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Catalogue where nom = :nom", Catalogue.class)
                    .setParameter("nom", nom).uniqueResult();
        }
    }
    // ... autres méthodes implémentées
}
```

---

## 5. Étape 4 : Relations & Orchestration Globale

### Objectif
Gérer les associations complexes et lancer l'application.

#### 1. Entité Catalogue (`Catalogue.java`)
```java
package com.examen.stock.model;

import java.util.HashSet;
import java.util.Set;

public class Catalogue {
    private int id;
    private String nom;
    private Set<Produit> produits = new HashSet<>();

    public Catalogue() {}
    public Catalogue(String nom) { this.nom = nom; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Set<Produit> getProduits() { return produits; }
    public void setProduits(Set<Produit> produits) { this.produits = produits; }

    public void ajouterProduit(Produit p) {
        this.produits.add(p);
        p.setCatalogue(this);
    }
}
```

#### 2. Mapping Catalogue (`Catalogue.hbm.xml`)
```xml
<hibernate-mapping package="com.examen.stock.model">
    <class name="Catalogue" table="catalogues">
        <id name="id" column="id">
            <generator class="native"/>
        </id>
        <property name="nom" column="nom" type="string" length="100"/>
        <set name="produits" cascade="all-delete-orphan" inverse="true" lazy="false">
            <key column="catalogue_id"/>
            <one-to-many class="com.examen.stock.model.Produit"/>
        </set>
    </class>
</hibernate-mapping>
```

#### 3. Application Principale (`MainApp.java`)
```java
package com.examen.stock;

import com.examen.stock.dao.*;
import com.examen.stock.model.*;
import com.examen.stock.service.*;
import com.examen.stock.util.HibernateUtil;

public class MainApp {
    public static void main(String[] args) {
        try {
            // 1. MODE MÉMOIRE (Fondamentaux)
            System.out.println("=== 1. MODE MÉMOIRE ===");
            GestionnaireStock<Produit> monStock = new GestionnaireStock<>();
            monStock.ajouter(new Produit("Ordinateur", 1000.0));
            monStock.ajouter(new Produit("Souris", 25.0));

            // 2. MODE JDBC (DAO Classique)
            System.out.println("\n=== 2. MODE JDBC ===");
            ProduitService serviceJdbc = new ProduitService(new ProduitDaoImpl());
            serviceJdbc.enregistrerProduit(new Produit("Clavier Mécanique", 85.0));
            serviceJdbc.recupererTout().forEach(System.out::println);

            // 3. MODE HIBERNATE (ORM)
            System.out.println("\n=== 3. MODE HIBERNATE ===");
            ProduitService serviceH = new ProduitService(new ProduitDaoHibernate());
            serviceH.enregistrerProduit(new Produit("Écran 4K", 350.0));
            serviceH.afficherStatistiques();

            // 4. TEST RELATION ONE-TO-MANY (Catalogue via DAO)
            System.out.println("\n=== 4. TEST RELATION ONE-TO-MANY ===");
            CatalogueService catService = new CatalogueService(new CatalogueDaoHibernate());
            
            Catalogue cat = new Catalogue("Matériel Bureau 2026");
            cat.ajouterProduit(new Produit("Chaise Ergonomique", 250.0));
            cat.ajouterProduit(new Produit("Bureau Assis-Debout", 450.0));

            catService.creerCatalogue(cat);
            catService.afficherTousLesCatalogues();

        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
```

---
## Gestion des threads
Cette partie du code montre comment gérer le multitâche (ou programmation concurrente) en Java. L'idée est de lancer une mission qui s'exécute "en tâche de fond" pendant que votre programme principal continue de faire autre chose.

```java
// 5. TEST PROGRAMMATION CONCURRENTE (Threads)
            System.out.println("\n=== 5. TEST PROGRAMMATION CONCURRENTE (Threads) ===");
            StockSimulationService simulation = new StockSimulationService(serviceHibernate);
            Thread t = new Thread(simulation);
            t.start(); // Lance le processus en arrière-plan

            System.out.println("[Main] Le thread de simulation tourne en parallèle...");

            // On attend un peu pour voir le thread travailler avant de fermer l'app
            Thread.sleep(10000);
            simulation.stopSimulation();
            t.join(); // Attend la fin du thread proprement

        } catch (StockException e) {
            System.err.println("Erreur ORM : " + e.getMessage());
        } catch (InterruptedException e) { // Added catch for InterruptedException
            System.err.println("Erreur de thread : " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore the interrupted status
        } finally {
            // Fermeture proprement de la SessionFactory Hibernate
            HibernateUtil.shutdown();
        }   


Voici l'analyse détaillée étape par étape :

1. Préparation de la mission
```java
StockSimulationService simulation = new StockSimulationService(serviceHibernate);
Thread t = new Thread(simulation);
```
StockSimulationService : C'est la classe qui contient le travail à faire (elle implémente l'interface Runnable). Ici, elle simule l'arrivée de nouveaux produits.
new Thread(t) : On crée un "moteur" (le Thread) et on lui donne la "mission" à accomplir.

2. Le lancement asynchrone
```java
t.start(); // Lance le processus en arrière-plan
System.out.println("[Main] Le thread de simulation tourne en parallèle...");
```
t.start() : C'est l'instruction cruciale. Elle demande au système d'ouvrir une nouvelle voie d'exécution. Le code dans simulation.run() commence à s'exécuter, mais le programme principal n'attend pas : il passe immédiatement à la ligne suivante (System.out.println).
3. La synchronisation temporelle
```java
Thread.sleep(10000);
```
Le fil principal (Main) s'arrête pendant 10 secondes. Cela permet de laisser au thread de simulation le temps de travailler (puisqu'il ajoute un produit toutes les 3 secondes dans son propre code).

4. L'arrêt propre
```java
simulation.stopSimulation();
t.join(); // Attend la fin du thread proprement
stopSimulation()
 : On envoie un signal au thread (via un booléen) pour lui dire : "C'est fini, arrête-toi après ton action actuelle".
t.join() : C'est une mesure de sécurité. Le programme principal s'arrête ici et attend que le thread t ait réellement fini tout ce qu'il faisait avant de continuer.
5. La gestion des interruptions
java
} catch (InterruptedException e) {
    System.err.println("Erreur de thread : " + e.getMessage());
    Thread.currentThread().interrupt(); // Restaure le statut d'interruption
}
En Java, si un thread est forcé de s'arrêter pendant qu'il dort (via sleep), il lève une InterruptedException. On la capture pour éviter un plantage et on "signale" que le thread a bien été interrompu.
6. Le nettoyage final
java
finally {
    HibernateUtil.shutdown();
}
Le bloc finally s'exécute quoi qu'il arrive (succès ou erreur). On s'en sert pour fermer proprement la connexion à la base de données (le "Cleaning up connection pool" que vous avez vu dans les logs).
### En résumé:

start() : Lance le parallélisme.
sleep() : Fait une pause.
join() : Synchronise/Attend la fin d'un autre thread.
Runnable : Représente la tâche à faire.

---
## 7. Étape 6 : Interface Graphique (JavaFX) & Modèle MVC

### Objectif
Cette étape marque la transition vers une application logicielle complète. Jusqu'ici, nous utilisions la console. **JavaFX** permet de créer une véritable fenêtre Windows/Linux/Mac pour interagir visuellement avec le stock. 

Nous appliquons ici le motif d'architecture **MVC (Modèle-Vue-Contrôleur)**, qui est le standard industriel pour séparer le design de la logique :
1.  **Modèle** : Nos classes `Produit` et `Catalogue` (déjà créées).
2.  **Vue** : Les fichiers **.fxml** (design en XML).
3.  **Contrôleur** : Les classes Java qui font le lien entre la fenêtre et nos services.

---

### A. La Vue : Le langage FXML
Le FXML est un langage basé sur XML qui permet de "dessiner" l'interface sans écrire de code Java complexe pour le placement des boutons.

#### Exemple : La Table de Produits (`ProduitView.fxml`)
```xml
<TableView fx:id="produitTable" VBox.vgrow="ALWAYS">
    <columns>
        <TableColumn fx:id="colNom" text="Nom" prefWidth="150"/>
        <TableColumn fx:id="colPrix" text="Prix" prefWidth="100"/>
    </columns>
</TableView>
```
*Explication technique :*
- **`fx:id`** : C'est le nom de la variable que nous utiliserons dans le code Java pour manipuler ce composant.
- **`VBox.vgrow="ALWAYS"`** : Indique que le tableau doit s'étirer pour prendre toute la place disponible.

---

### B. Le Contrôleur : La Logique UI
C'est ici que l'on définit ce qui se passe quand on clique sur un bouton.

#### Analyse du `ProduitController.java`
```java
public class ProduitController {
    @FXML private TextField nomField; // Injecté depuis le FXML
    @FXML private TableView<Produit> produitTable;
    
    // On utilise notre service pour les données
    private ProduitService service = new ProduitService(new ProduitDaoHibernate());
    
    // Liste "observable" : si elle change, le tableau se met à jour tout seul
    private ObservableList<Produit> produitList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Lien entre les colonnes du tableau et les attributs de l'objet Produit
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        handleRefresh();
    }

    @FXML
    public void handleAjouterProduit() {
        String nom = nomField.getText();
        double prix = Double.parseDouble(prixField.getText());
        service.enregistrerProduit(new Produit(nom, prix)); // Persistance BD
        handleRefresh(); // Mise à jour Visuelle
    }
}
```
*Points clés du code :*
1.  **`@FXML`** : Cette annotation dit à Java : "Va chercher l'élément qui a cet ID dans le fichier XML".
2.  **`initialize()`** : C'est la méthode qui s'exécute automatiquement au chargement de la fenêtre.
3.  **`PropertyValueFactory`** : Un automatisme génial de JavaFX qui va chercher tout seul les `getNom()` et `getPrix()` de vos produits pour remplir le tableau.

---

### C. Le "Trick" du Lanceur (Java modules)
Depuis Java 11, JavaFX est séparé du JDK. Cela cause souvent des erreurs comme *"des composants d'exécution obligatoires sont manquants"*.

**La Solution : `JavaFXLauncher.java`**
Pour contourner les restrictions de modules, on crée une classe "normale" qui appelle l'application JavaFX :
```java
public class JavaFXLauncher {
    public static void main(String[] args) {
        JavaFXApp.main(args); // Appelle le vrai point d'entrée
    }
}
```
*Pourquoi ça marche ?* Parce que si la classe de démarrage n'hérite pas de `Application`, Java ne déclenche pas les vérifications strictes des modules au lancement.

---

### Code Source Complet : Étape 6

#### 1. Point d'entrée et Lanceur
**JavaFXLauncher.java** (Contournement des modules)
```java
package com.examen.stock;
public class JavaFXLauncher {
    public static void main(String[] args) {
        JavaFXApp.main(args);
    }
}
```

**JavaFXApp.java**
```java
package com.examen.stock;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/examen/stock/view/MainView.fxml"));
        primaryStage.setTitle("Système de Gestion de Stock - Examen 2026");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
    public static void main(String[] args) { launch(args); }
}
```

#### 2. Vues (FXML)
**MainView.fxml**
```xml
<BorderPane xmlns:fx="http://javafx.com/fxml" fx:controller="com.examen.stock.view.MainController">
    <top>
        <MenuBar>
            <Menu text="Fichier"><MenuItem text="Quitter" onAction="#handleExit"/></Menu>
        </MenuBar>
    </top>
    <center>
        <TabPane>
            <Tab text="Produits" closable="false"><fx:include source="ProduitView.fxml"/></Tab>
            <Tab text="Catalogues" closable="false"><fx:include source="CatalogueView.fxml"/></Tab>
        </TabPane>
    </center>
</BorderPane>
```

#### 3. Contrôleurs (Logique)
**ProduitController.java**
```java
public class ProduitController {
    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TableView<Produit> produitTable;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, Double> colPrix;

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
            produitService.enregistrerProduit(new Produit(nomField.getText(), Double.parseDouble(prixField.getText())));
            handleRefresh();
            nomField.clear(); prixField.clear();
        } catch (Exception e) { System.err.println(e.getMessage()); }
    }

    @FXML
    public void handleRefresh() {
        produitList.setAll(produitService.recupererTout());
        produitTable.setItems(produitList);
    }
}
```

**CatalogueView.fxml**
```xml
<VBox spacing="10" xmlns:fx="http://javafx.com/fxml" fx:controller="com.examen.stock.view.CatalogueController">
    <HBox spacing="10">
        <TextField fx:id="catalogueNomField" promptText="Nom du catalogue"/>
        <Button text="Ajouter" onAction="#handleAjouterCatalogue"/>
    </HBox>
    <TableView fx:id="catalogueTable" VBox.vgrow="ALWAYS">
        <columns>
            <TableColumn fx:id="colId" text="ID" prefWidth="50"/>
            <TableColumn fx:id="colNom" text="Nom" prefWidth="200"/>
        </columns>
    </TableView>
</VBox>
```

**CatalogueController.java**
```java
public class CatalogueController {
    @FXML private TextField catalogueNomField;
    @FXML private TableView<Catalogue> catalogueTable;
    @FXML private TableColumn<Catalogue, Integer> colId;
    @FXML private TableColumn<Catalogue, String> colNom;

    private CatalogueService service = new CatalogueService(new CatalogueDaoHibernate());
    private ObservableList<Catalogue> catalogueList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        handleRefresh();
    }

    @FXML
    public void handleAjouterCatalogue() {
        service.creerCatalogue(new Catalogue(catalogueNomField.getText()));
        handleRefresh();
        catalogueNomField.clear();
    }

    @FXML
    public void handleRefresh() {
        catalogueList.setAll(service.listerCatalogues());
        catalogueTable.setItems(catalogueList);
    }
}
```

---

### Questions types : Étape 6 (Examen)

1.  **Q : Qu'est-ce qu'une `ObservableList` et pourquoi est-elle indispensable ?**
    - **R :** C'est une liste qui implémente le pattern **Observer**. Dès qu'un produit est ajouté ou supprimé de cette liste, JavaFX est immédiatement prévenu et met à jour l'affichage à l'écran sans que l'on ait à redessiner la table manuellement.

2.  **Q : Comment JavaFX fait-il le lien entre une colonne et un attribut d'objet ?**
    - **R :** On utilise un `PropertyValueFactory`. Il utilise la **Réflexion Java** pour trouver dynamiquement le "getter" correspondant au nom fourni (ex: "prix" -> `getPrix()`).

3.  **Q : Quel est le rôle du `FXMLLoader` ?**
    - **R :** Son rôle est de lire le fichier `.fxml`, de créer les objets graphiques correspondants, d'instancier le contrôleur associé et d'injecter les composants `@FXML` dans ce contrôleur.

4.  **Q : Pourquoi séparer la Vue du Contrôleur ?**
    - **R :** Pour la **Maintenabilité**. On peut changer tout le design (couleurs, polices, placements) dans le XML sans jamais risquer de casser la logique Java de calculs ou de base de données.

---

## Annexe : Déploiement avec Docker

Cette section détaille la configuration nécessaire pour exécuter l'application dans un environnement conteneurisé, garantissant que la base de données et l'application Java communiquent correctement.

### 1. Configuration Docker

#### **Dockerfile**
Ce fichier définit comment construire l'image de l'application Java.
```dockerfile
# Utilisation d'une image légère Java 17
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copie de l'artefact JAR et des dépendances
COPY target/gestion-stock-1.0-SNAPSHOT.jar app.jar
COPY target/lib /app/target/lib
COPY target/classes /app/target/classes

# Point d'entrée avec inclusion du classpath pour les dépendances
ENTRYPOINT ["java", "-cp", "app.jar:target/lib/*", "com.examen.stock.MainApp"]
```

#### **docker-compose.yml**
Ce fichier orchestre les deux services : la base de données **MySQL** et l'application **Java**.
```yaml
version: '3.8'
services:
  db:
    image: mysql:8.0
    container_name: mysql_db
    environment:
      MYSQL_ROOT_PASSWORD: ""
      MYSQL_ALLOW_EMPTY_PASSWORD: "yes"
      MYSQL_DATABASE: gestion_stock
    volumes:
      - ./schema.sql:/docker-entrypoint-initdb.d/schema.sql
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  app:
    build: .
    container_name: java_app
    environment:
      DB_HOST: db
    depends_on:
      db:
        condition: service_healthy
```

### 2. Guide d'Exécution (Terminal)

Pour tester le projet de zéro, exécutez les commandes suivantes dans l'ordre :

1. **Compilation et Packaging** : Génère le fichier JAR et récupère les dépendances.
   ```powershell
   mvn clean package
   ```

2. **Lancement de l'Infrastructure** : Construit l'image et démarre les conteneurs.
   ```powershell
   docker compose up --build
   ```

3. **Arrêt des Services** :
   ```powershell
   docker compose down
   ```

> [!IMPORTANT]
> La variable d'environnement `DB_HOST: db` dans le `docker-compose` est cruciale. Elle permet à Hibernate de trouver le conteneur MySQL par son nom de service plutôt que par `localhost`.

### 3. Configuration Alternative (WAMP / XAMPP)

Si vous n'utilisez pas Docker, voici la marche à suivre pour exécuter le projet localement avec un serveur WAMP ou XAMPP :

#### **Prérequis**
*   Démarrer les services **Apache** et **MySQL** depuis le panneau de contrôle (WAMP/XAMPP).
*   Créer la base de données via **phpMyAdmin** ou une console SQL :
    ```sql
    CREATE DATABASE gestion_stock;
    ```

#### **Ajustements du Code**
L'application est conçue pour être "Docker-ready", mais elle s'adapte automatiquement à un environnement local grâce à la détection de l'hôte dans `HibernateUtil.java`.

1. **Hôte** : Comme aucune variable `DB_HOST` n'est définie sur votre machine Windows, le code utilisera par défaut `localhost`.
2. **Identifiants** :
    *   **WAMP** : Utilisateur `root`, mot de passe vide (par défaut).
    *   **XAMPP** : Utilisateur `root`, mot de passe vide (par défaut).
    *   *Note : Si vous avez défini un mot de passe pour MySQL, vous devrez mettre à jour `hibernate.cfg.xml` ou `DatabaseConnection.java`.*

#### **Lancement depuis l'IDE (IntelliJ / Eclipse)**
1. Faites un clic droit sur `MainApp.java`.
2. Choisissez **Run 'MainApp.main()'**.
3. Vérifiez la console pour voir les logs Hibernate.

---





### Ecran de sortie
java_app  | === 1. MODE MÉMOIRE (Ancien) ===
java_app  | Ajout de : Ordinateur
java_app  | Ajout de : Souris                                                                         
java_app  |                                                                                           
java_app  | === 2. ARCHITECTURE JDBC (DAO Classique) ===
java_app  | DAO: Produit créé -> Micro-Casque                                                         
java_app  | Micro-Casque (45.0€)
java_app  | 
java_app  | === 3. MAPPING OBJET RELATIONNEL (Hibernate XML) ===                                      
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.Version logVersion                                  
java_app  | INFO: HHH000412: Hibernate ORM core version 6.2.7.Final
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.cfg.Environment <clinit>                            
java_app  | INFO: HHH000406: Using bytecode reflection optimizer
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.boot.jaxb.internal.MappingBinder doBind             
java_app  | WARN: HHH90000028: Support for `<hibernate-mappings/>` is deprecated [RESOURCE : com/examen/stock/model/Produit.hbm.xml]; migrate to orm.xml or mapping.xml, or enable `hibernate.transform_hbm_xml.enabled` for on the fly transformation
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.boot.jaxb.internal.MappingBinder doBind
java_app  | WARN: HHH90000028: Support for `<hibernate-mappings/>` is deprecated [RESOURCE : com/examen/stock/model/Catalogue.hbm.xml]; migrate to orm.xml or mapping.xml, or enable `hibernate.transform_hbm_xml.enabled` for on the fly transformation
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl configure                                                                            
java_app  | WARN: HHH10001002: Using built-in connection pool (not intended for production use)
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator                                                                         
java_app  | INFO: HHH10001005: Loaded JDBC driver class: com.mysql.cj.jdbc.Driver
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator                                                                         
java_app  | INFO: HHH10001012: Connecting with JDBC URL [jdbc:mysql://db:3306/gestion_stock]
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator                                                                         
java_app  | INFO: HHH10001001: Connection properties: {password=****, user=root}
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl buildCreator                                                                         
java_app  | INFO: HHH10001003: Autocommit mode: false
java_app  | Jan 06, 2026 1:14:41 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PooledConnections <init>                                                             
java_app  | INFO: HHH10001115: Connection pool size: 20 (min=1)
java_app  | Jan 06, 2026 1:14:42 AM org.hibernate.bytecode.internal.BytecodeProviderInitiator buildBytecodeProvider
java_app  | INFO: HHH000021: Bytecode provider name : bytebuddy
java_app  | Jan 06, 2026 1:14:42 AM org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator initiateService
java_app  | INFO: HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]                                                                       
java_app  | Jan 06, 2026 1:14:42 AM org.hibernate.resource.transaction.backend.jdbc.internal.DdlTransactionIsolatorNonJtaImpl getIsolatedConnection
java_app  | INFO: HHH10001501: Connection obtained from JdbcConnectionAccess [org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator$ConnectionProviderJdbcConnectionAccess@2478b629] for (non-JTA) DDL execution was not in auto-commit mode; the Connection 'local transaction' will be committed and the Connection will be set into auto-commit mode.
java_app  | Hibernate: 
java_app  |     create table catalogues (                                                             
java_app  |         id integer not null auto_increment,
java_app  |         nom varchar(100),                                                                 
java_app  |         primary key (id)                                                                  
java_app  |     ) engine=InnoDB
java_app  | Hibernate:                                                                                
java_app  |     alter table produits 
java_app  |        modify column nom  varchar(255) not null                                           
java_app  | Hibernate:                                                                                
java_app  |     alter table produits                                                                  
java_app  |        modify column prix  float(53)                                                      
java_app  | Hibernate: 
java_app  |     alter table produits 
java_app  |        add column catalogue_id integer                                                    
java_app  | Hibernate:                                                                                
java_app  |     alter table produits 
java_app  |        add constraint FKa011t7xi62i35pp1hk8qgmyvs                                         
java_app  |        foreign key (catalogue_id)                                                         
java_app  |        references catalogues (id)
java_app  | Hibernate:                                                                                
java_app  |     insert 
java_app  |     into                                                                                  
java_app  |         produits                                                                          
java_app  |         (catalogue_id,prix,nom)                                                           
java_app  |     values
java_app  |         (?,?,?)                                                                           
java_app  | Hibernate: Produit persisté -> Clavier RGB 2025
java_app  | Hibernate:                                                                                
java_app  |     select                                                                                
java_app  |         p1_0.nom,
java_app  |         p1_0.catalogue_id,                                                                
java_app  |         p1_0.prix                                                                         
java_app  |     from                                                                                  
java_app  |         produits p1_0 
java_app  |     where                                                                                 
java_app  |         p1_0.nom=?                                                                        
java_app  | Hibernate: 
java_app  |     select                                                                                
java_app  |         p1_0.nom,                                                                         
java_app  |         p1_0.catalogue_id,
java_app  |         p1_0.prix                                                                         
java_app  |     from                                                                                  
java_app  |         produits p1_0                                                                     
java_app  |     where
java_app  |         p1_0.nom=?                                                                        
java_app  | Hibernate: 
java_app  |     update                                                                                
java_app  |         produits                                                                          
java_app  |     set                                                                                   
java_app  |         catalogue_id=?,
java_app  |         prix=?                                                                            
java_app  |     where                                                                                 
java_app  |         nom=?                                                                             
java_app  | Hibernate: Produit mis à jour -> Clavier RGB 2025
java_app  | Données via Hibernate :                                                                   
java_app  | Hibernate:                                                                                
java_app  |     select
java_app  |         p1_0.nom,
java_app  |         p1_0.catalogue_id,                                                                
java_app  |         p1_0.prix                                                                         
java_app  |     from
java_app  |         produits p1_0                                                                     
java_app  | Micro-Casque (45.0€)                                                                      
java_app  | Clavier RGB 2025 (115.0€)
java_app  | Hibernate:                                                                                
java_app  |     select                                                                                
java_app  |         p1_0.nom,
java_app  |         p1_0.catalogue_id,                                                                
java_app  |         p1_0.prix                                                                         
java_app  |     from
java_app  |         produits p1_0                                                                     
java_app  |                                                                                           
java_app  | --- STATISTIQUES SERVICE (VIA DAO) ---                                                    
java_app  | Valeur Valeur Stock : 160.0€
java_app  | Prix Moyen : 80.00€                                                                       
java_app  | Catalogue Service : Clavier RGB 2025 | Micro-Casque                                       
java_app  |                                                                                           
java_app  | === 4. TEST RELATION ONE-TO-MANY (Catalogue via DAO) ===
java_app  | Hibernate:                                                                                
java_app  |     insert                                                                                
java_app  |     into                                                                                  
java_app  |         catalogues
java_app  |         (nom)                                                                             
java_app  |     values                                                                                
java_app  |         (?)                                                                               
java_app  | Hibernate:                                                                                
java_app  |     insert 
java_app  |     into                                                                                  
java_app  |         produits                                                                          
java_app  |         (catalogue_id,prix,nom)                                                           
java_app  |     values
java_app  |         (?,?,?)                                                                           
java_app  | Hibernate:                                                                                
java_app  |     insert                                                                                
java_app  |     into
java_app  |         produits                                                                          
java_app  |         (catalogue_id,prix,nom)                                                           
java_app  |     values                                                                                
java_app  |         (?,?,?)                                                                           
java_app  | Hibernate: Catalogue persisté -> Matériel Bureau 2026
java_app  | Hibernate:                                                                                
java_app  |     select                                                                                
java_app  |         c1_0.id,                                                                          
java_app  |         c1_0.nom 
java_app  |     from                                                                                  
java_app  |         catalogues c1_0                                                                   
java_app  | Hibernate:                                                                                
java_app  |     select                                                                                
java_app  |         p1_0.catalogue_id,                                                                
java_app  |         p1_0.nom,                                                                         
java_app  |         p1_0.prix 
java_app  |     from                                                                                  
java_app  |         produits p1_0                                                                     
java_app  |     where                                                                                 
java_app  |         p1_0.catalogue_id=?                                                               
java_app  |                                                                                           
java_app  | --- LISTE DES CATALOGUES ---
java_app  | Catalogue{id=1, nom='Matériel Bureau 2026', nbProduits=2}                                 
java_app  |                                                                                           
java_app  | === 5. TEST PROGRAMMATION CONCURRENTE (Threads) ===                                       
java_app  | [Main] Le thread de simulation tourne en parallèle...                                     
java_app  | [Thread Simulation] Démarrage du réapprovisionnement automatique...                       
java_app  | Hibernate: 
java_app  |     insert 
java_app  |     into                                                                                  
java_app  |         produits                                                                          
java_app  |         (catalogue_id,prix,nom)                                                           
java_app  |     values                                                                                
java_app  |         (?,?,?)                                                                           
java_app  | Hibernate: Produit persisté -> Produit_Auto_1                                             
java_app  | [Thread Simulation] Nouveau produit livré et enregistré : Produit_Auto_1 (80.37€)
java_app  | Hibernate:                                                                                
java_app  |     insert 
java_app  |     into                                                                                  
java_app  |         produits                                                                          
java_app  |         (catalogue_id,prix,nom)                                                           
java_app  |     values                                                                                
java_app  |         (?,?,?)                                                                           
java_app  | Hibernate: Produit persisté -> Produit_Auto_2                                             
java_app  | [Thread Simulation] Nouveau produit livré et enregistré : Produit_Auto_2 (13.67€)         
java_app  | Hibernate:                                                                                
java_app  |     insert 
java_app  |     into                                                                                  
java_app  |         produits                                                                          
java_app  |         (catalogue_id,prix,nom) 
java_app  |     values                                                                                
java_app  |         (?,?,?)                                                                           
java_app  | Hibernate: Produit persisté -> Produit_Auto_3                                             
java_app  | [Thread Simulation] Nouveau produit livré et enregistré : Produit_Auto_3 (53.78€)         
java_app  | [Thread Simulation] Fin de la simulation.
java_app  | Jan 06, 2026 1:14:53 AM org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl$PoolState stop
java_app  | INFO: HHH10001008: Cleaning up connection pool [jdbc:mysql://db:3306/gestion_stock]
java_app exited with code 0

**Rapport complet pour la préparation à l'examen 2026.**
