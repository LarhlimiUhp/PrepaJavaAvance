# Compte Rendu Global : Projet de Gestion de Stock

## 1. Introduction
Ce projet consiste en la mise en place d'une application Java modulaire pour la gestion d'un inventaire de produits. L'objectif était de structurer un code monolithique en un projet Maven organisé, appliquant les meilleures pratiques de programmation orientée objet et les fonctionnalités modernes de Java (Java 17+).

## 2. Architecture du Projet
Le projet suit une structure Maven standard, facilitant la gestion des dépendances et le cycle de vie du développement.

### Arborescence des fichiers
```text
Partie A projet 2/
├── pom.xml                           # Configuration Maven
└── src/main/java/com/examen/stock/
    ├── MainApp.java                  # Point d'entrée
    ├── exception/
    │   └── StockException.java       # Gestion des erreurs métier
    ├── model/
    │   └── Produit.java              # Modèle de données
    ├── repository/
    │   └── Repository.java           # Interface de persistance
    └── service/
        └── GestionnaireStock.java    # Logique métier et Streams
```

## 3. Analyse Détaillée des Composants

### A. Gestion des Erreurs (`StockException.java`)
Placée dans le package `exception`, cette classe personnalisée permet de distinguer les erreurs liées au stock des erreurs système générales.

```java
package com.examen.stock.exception;

public class StockException extends Exception {
    public StockException(String message) { super(message); }
}
```

### B. Modèle de Données (`Produit.java`)
Cette classe implémente l'**encapsulation**. Elle contient un constructeur par défaut (requis par Hibernate) et des getters/setters.

```java
package com.examen.stock.model;

public class Produit {
    private String nom;
    private double prix;

    public Produit() {}

    public Produit(String nom, double prix) {
        this.nom = nom;
        this.prix = prix;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    @Override
    public String toString() { return nom + " (" + prix + "€)"; }
}
```

### C. Abstraction et Généricité (`Repository.java`)
L'interface `Repository<T>` définit un contrat générique pour les opérations de base.

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

### D. Logique Métier et Streams (`GestionnaireStock.java`)
Ce composant implémente le repository en mémoire et utilise les Streams pour le filtrage.

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

    public List<T> filtrerParPrixMin(double seuil) {
        return inventaire.stream()
                .filter(p -> p.getPrix() >= seuil)
                .collect(Collectors.toList());
    }

    @Override
    public void mettreAJour(T element) throws StockException {
        T existant = trouverParNom(element.getNom());
        inventaire.set(inventaire.indexOf(existant), element);
    }

    @Override
    public void supprimer(String nom) throws StockException {
        inventaire.remove(trouverParNom(nom));
    }
}
```

## 4. Guide d'Exécution

### Compilation
Pour compiler le projet et générer les fichiers `.class` :
```bash
mvn clean compile
```

### Exécution
Pour lancer l'application et voir les tests en action :
```bash
java -cp "target\classes" com.examen.stock.MainApp
```

## 5. Résultats Obtenus
L'exécution valide les points suivants :
1. **Ajout dynamique** de produits dans le stock.
2. **Gestion robuste des exceptions** : Une tentative de recherche d'un produit inexistant ("Ecran") déclenche une `StockException` capturée et affichée proprement.
3. **Analyse de données** : Le filtrage par prix minimal (ex: > 40€) via les Streams fonctionne correctement, retournant uniquement les articles concernés.

## 6. Note sur la Sécurité de Type
### Question : Le gestionnaire peut-il manipuler des objets n'ayant pas les propriétés d'un produit ?

**Réponse : Non.** Le code utilise une **borne générique** (`T extends Produit`) dans la classe `GestionnaireStock.java`. Ce mécanisme garantit deux choses :
- **Protection à la compilation** : Si vous essayez d'utiliser le gestionnaire avec une classe qui n'hérite pas de `Produit` (ex: `Client`), le compilateur Java générera une erreur immédiate.
- **Accès garanti aux méthodes** : Puisque Java sait que `T` est obligatoirement un `Produit`, il autorise l'utilisation sécurisée des méthodes `.getNom()` et `.getPrix()` à l'intérieur des Streams sans risque d'erreur de cast ou de méthode inexistante.

## 7. Couche de Persistance JDBC (MySQL)
Une nouvelle couche de persistance a été ajoutée pour permettre le stockage permanent des produits.

### Composants JDBC :
- **`schema.sql`** : Script de création de la base de données.

```sql
CREATE DATABASE IF NOT EXISTS gestion_stock;
USE gestion_stock;

CREATE TABLE IF NOT EXISTS produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prix DOUBLE NOT NULL
);
```

- **`DatabaseConnection.java`** : Gère la connexion via le pilote JDBC (Pattern Singleton).

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

### Configuration requise :
1.  **MySQL Server** : Doit être actif sur `localhost:3306`.
2.  **Base de données** : Exécuter `schema.sql` pour initialiser la structure.
3.  **Identifiants** : Par défaut `root` sans mot de passe (modifiable dans `DatabaseConnection.java`).

## 8. Utilisation avec Docker
L'intégration de Docker permet de lancer tout l'environnement sans aucune installation locale (hormis Docker Desktop).

### Fichiers de configuration :
- **`Dockerfile`** :

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/gestion-stock-1.0-SNAPSHOT.jar app.jar
COPY target/lib /app/target/lib
COPY target/classes /app/target/classes
ENTRYPOINT ["java", "-cp", "app.jar:target/lib/*", "com.examen.stock.MainApp"]
```

- **`docker-compose.yml`** :

```yaml
version: '3.8'
services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ""
      MYSQL_ALLOW_EMPTY_PASSWORD: "yes"
      MYSQL_DATABASE: gestion_stock
    volumes:
      - ./schema.sql:/docker-entrypoint-initdb.d/schema.sql
    ports:
      - "3306:3306"
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost" ]
      timeout: 20s
      retries: 10

  app:
    build: .
    environment:
      DB_HOST: db
    depends_on:
      db:
        condition: service_healthy
```

### Commande unique pour démarrer :
```bash
docker compose up --build
```
Cette commande va :
1. Construire l'image Java.
2. Lancer MySQL et attendre qu'il soit "Healthy".
3. Créer la base et les tables.
4. Lancer l'application qui se connectera automatiquement au service `db`.

## 9. Architecture DAO & Service
Le projet a évolué vers une architecture en couches pour une meilleure maintenabilité.

### A. Interface DAO (`IDao.java`)
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

### B. Implémentation JDBC (`ProduitDaoImpl.java`)
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
    // ... autres méthodes implémentées (readByName, update, delete)
}
```

### C. Couche Service (`ProduitService.java`)
```java
package com.examen.stock.service;

import com.examen.stock.dao.IDao;
import com.examen.stock.model.Produit;
import java.util.List;

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

## 10. Avantages de l'Architecture DAO/Service
Le passage d'un Repository unique à une architecture DAO + Service offre plusieurs bénéfices :

| Caractéristique | Aproche Repository Unique | Architecture DAO + Service |
| :--- | :--- | :--- |
| **Responsabilité** | Mixte (Persistance + Logique) | Séparée (DAO=Données, Service=Métier) |
| **Couplage** | Fort (Main dépend de la technique) | Faible (Main ne connaît que le Service) |
| **Evolutivité** | Difficile (tout est lié) | Facile (on peut changer le DAO sans toucher au Service) |
| **Standard** | Académique / Prototype | Professionnel / Entreprise (Spring/JEE) |

En résumé, cette architecture permet de changer de base de données ou d'ajouter des règles de gestion complexes sans jamais impacter le code de démarrage (`MainApp`).

## 11. Mapping Objet-Relationnel (ORM) avec Hibernate

### A. Configuration (`hibernate.cfg.xml`)
```xml
<hibernate-configuration>
    <session-factory>
        <property name="connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="connection.url">jdbc:mysql://localhost:3306/gestion_stock</property>
        <property name="connection.username">root</property>
        <property name="dialect">org.hibernate.dialect.MySQLDialect</property>
        <property name="hbm2ddl.auto">update</property>
        <mapping resource="com/examen/stock/model/Produit.hbm.xml"/>
    </session-factory>
</hibernate-configuration>
```

### B. Mapping XML (`Produit.hbm.xml`)
```xml
<hibernate-mapping package="com.examen.stock.model">
    <class name="Produit" table="produits">
        <id name="nom" column="nom">
            <generator class="assigned"/>
        </id>
        <property name="prix" column="prix" type="double"/>
    </class>
</hibernate-mapping>
```

### C. Utilitaire (`HibernateUtil.java`)
```java
public class HibernateUtil {
    private static final SessionFactory sf = new Configuration().configure().buildSessionFactory();
    public static SessionFactory getSessionFactory() { return sf; }
    public static void shutdown() { getSessionFactory().close(); }
}
```

### D. DAO Hibernate (`ProduitDaoHibernate.java`)
```java
public class ProduitDaoHibernate implements IDao<Produit> {
    @Override
    public void create(Produit p) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction t = s.beginTransaction();
            s.persist(p);
            t.commit();
        }
    }
    @Override
    public List<Produit> readAll() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Produit", Produit.class).list();
        }
    }
    // ... autres méthodes CRUD via Session API
}
```

## 12. Orchestration Globale (`MainApp.java`)
Ce fichier sert de point d'entrée et démontre les trois modes de fonctionnement de l'application.

```java
package com.examen.stock;

import com.examen.stock.dao.IDao;
import com.examen.stock.dao.ProduitDaoImpl;
import com.examen.stock.dao.ProduitDaoHibernate;
import com.examen.stock.model.Produit;
import com.examen.stock.service.ProduitService;
import com.examen.stock.service.GestionnaireStock;
import com.examen.stock.util.HibernateUtil;

public class MainApp {
    public static void main(String[] args) {
        // 1. MODE MÉMOIRE (Ancien)
        GestionnaireStock<Produit> monStock = new GestionnaireStock<>();
        monStock.ajouter(new Produit("Ordinateur", 1000.0));

        // 2. ARCHITECTURE JDBC (DAO Classique)
        ProduitService serviceJdbc = new ProduitService(new ProduitDaoImpl());
        serviceJdbc.enregistrerProduit(new Produit("Micro-Casque", 45.0));

        // 3. MAPPING OBJET RELATIONNEL (Hibernate XML)
        try {
            ProduitService serviceH = new ProduitService(new ProduitDaoHibernate());
            serviceH.enregistrerProduit(new Produit("Clavier RGB 2025", 120.0));
            serviceH.afficherStatistiques();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
```

---
**Conclusion** : Ce projet est désormais une application "Cloud Ready", offrant une flexibilité totale d'exécution : en mémoire, via JDBC local, ou via des conteneurs isolés avec Docker.
