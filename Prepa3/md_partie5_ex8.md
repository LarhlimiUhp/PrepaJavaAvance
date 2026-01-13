# Partie 5 - Exercice 8 : Hibernate ORM

## 8.1 Question 1 : Explication des Annotations

### @Entity

**Rôle :** Marque la classe comme une entité JPA/Hibernate.

```java
@Entity
public class Departement {
    // Cette classe sera mappée à une table
}
```

**Signification :**
- Hibernate gérera la persistance de cette classe
- Une table sera créée/utilisée pour cette entité
- Instances peuvent être sauvegardées en BD
- Obligatoire pour toute classe persistante

### @Table(name = "departement")

**Rôle :** Spécifie le nom exact de la table dans la BD.

```java
@Entity
@Table(name = "departement")
public class Departement {
    // Mappée à la table "departement"
}
```

**Sans @Table :**
- Table nommée selon le nom de la classe ("Departement")

**Options avancées :**
```java
@Table(
    name = "departement",
    schema = "public",
    catalog = "myCatalog",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nomDep", "categorie"})
    },
    indexes = {
        @Index(name = "idx_nom", columnList = "nomDep")
    }
)
```

### @Id

**Rôle :** Identifie la clé primaire de l'entité.

```java
@Entity
public class Departement {
    @Id
    private Long idDep;
}
```

**Signification :**
- Champ unique identifiant l'entité
- Obligatoire pour chaque entité
- Correspond à PRIMARY KEY en SQL

### @GeneratedValue(strategy = GenerationType.IDENTITY)

**Rôle :** Génération automatique de l'ID.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idDep;
```

**Stratégies disponibles :**

| Stratégie | Description | BD | Performance |
|-----------|-------------|-----|-------------|
| **IDENTITY** | Auto-incrémentation BD | MySQL, PostgreSQL | Bonne |
| **SEQUENCE** | Utilise séquence | Oracle, PostgreSQL | Excellente |
| **TABLE** | Table dédiée | Toutes | Faible |
| **AUTO** | Hibernate choisit | Toutes | Variable |

**Exemples :**
```java
// IDENTITY (MySQL)
@GeneratedValue(strategy = GenerationType.IDENTITY)

// SEQUENCE (Oracle/PostgreSQL)
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dept_seq")
@SequenceGenerator(name = "dept_seq", sequenceName = "departement_seq")

// TABLE
@GeneratedValue(strategy = GenerationType.TABLE, generator = "dept_gen")
@TableGenerator(name = "dept_gen", table = "id_generator")

// AUTO (par défaut)
@GeneratedValue(strategy = GenerationType.AUTO)
```

### @Column

**Rôle :** Configure le mapping d'un champ vers une colonne.

```java
@Column(name = "idDep")
private Long idDep;
```

**Options complètes :**
```java
@Column(
    name = "nomDep",           // Nom colonne
    nullable = false,          // NOT NULL
    unique = true,             // UNIQUE
    length = 100,              // VARCHAR(100)
    precision = 10,            // Pour DECIMAL
    scale = 2,                 // Décimales
    insertable = true,         // Dans INSERT
    updatable = true,          // Dans UPDATE
    columnDefinition = "TEXT"  // Type SQL personnalisé
)
private String nomDep;
```

**Exemples pratiques :**
```java
// Colonne obligatoire
@Column(nullable = false)
private String nom;  // → nom VARCHAR(255) NOT NULL

// Colonne unique
@Column(unique = true)
private String email;  // → email VARCHAR(255) UNIQUE

// Longueur personnalisée
@Column(length = 500)
private String description;  // → VARCHAR(500)

// Nombre décimal
@Column(precision = 10, scale = 2)
private BigDecimal prix;  // → DECIMAL(10,2)

// Non modifiable
@Column(updatable = false)
private LocalDateTime dateCreation;
```

### @OneToMany

**Rôle :** Définit une relation un-à-plusieurs.

```java
@OneToMany(mappedBy = "departement", 
           fetch = FetchType.LAZY, 
           cascade = CascadeType.ALL)
private List<Livre> livres;
```

**Paramètres détaillés :**

#### mappedBy = "departement"
```java
// Indique que Livre possède la clé étrangère
// "departement" = nom du champ dans Livre

@Entity
public class Livre {
    @ManyToOne
    private Departement departement;  // ← Ce champ
}

// Sans mappedBy: Hibernate crée une table de jointure
// Avec mappedBy: Utilise FK dans Livre
```

#### fetch = FetchType.LAZY
```java
// LAZY: Chargement à la demande
Departement dept = session.get(Departement.class, 1L);
// Livres PAS encore chargés
List<Livre> livres = dept.getLivres();
// Requête SQL exécutée MAINTENANT

// EAGER: Chargement immédiat
@OneToMany(fetch = FetchType.EAGER)
// Livres chargés avec le département (JOIN)
```

**Comparaison LAZY vs EAGER :**

| Aspect | LAZY | EAGER |
|--------|------|-------|
| Chargement | À la demande | Immédiat |
| Performance | Meilleure si pas utilisé | Peut charger trop |
| N+1 Problem | ⚠️ Risque | ✓ Évité |
| Session | Doit être ouverte | Pas nécessaire |

#### cascade = CascadeType.ALL
```java
// Propage toutes les opérations aux enfants
Departement dept = new Departement();
Livre livre1 = new Livre();
dept.getLivres().add(livre1);

session.save(dept);  // Sauve aussi livre1 automatiquement!
```

**Types de cascade :**
- `PERSIST` : save()
- `MERGE` : merge()
- `REMOVE` : delete()
- `REFRESH` : refresh()
- `DETACH` : detach()
- `ALL` : Tous les types

## 8.2 Question 2 : Classe Livre Complète

### Implémentation

```java
import javax.persistence.*;

@Entity
@Table(name = "livre")
public class Livre {
    
    // ===== CLÉS =====
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLivre")
    private Long idLivre;
    
    // ===== ATTRIBUTS =====
    @Column(name = "titre", nullable = false, length = 200)
    private String titre;
    
    @Column(name = "auteur", length = 150)
    private String auteur;
    
    @Column(name = "prix", precision = 10, scale = 2)
    private Double prix;
    
    @Column(name = "isbn", unique = true, length = 13)
    private String isbn;
    
    @Column(name = "anneePublication")
    private Integer anneePublication;
    
    // ===== RELATIONS =====
    /**
     * Relation Many-to-One vers Departement
     * Plusieurs livres appartiennent à un département
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "idDep",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_livre_departement")
    )
    private Departement departement;
    
    // ===== CONSTRUCTEURS =====
    public Livre() {
        // Constructeur par défaut requis par JPA
    }
    
    public Livre(String titre, String auteur, Double prix) {
        this.titre = titre;
        this.auteur = auteur;
        this.prix = prix;
    }
    
    public Livre(String titre, String auteur, Double prix, 
                 String isbn, Integer anneePublication, Departement departement) {
        this.titre = titre;
        this.auteur = auteur;
        this.prix = prix;
        this.isbn = isbn;
        this.anneePublication = anneePublication;
        this.departement = departement;
    }
    
    // ===== GETTERS & SETTERS =====
    public Long getIdLivre() { return idLivre; }
    public void setIdLivre(Long idLivre) { this.idLivre = idLivre; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }
    
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public Integer getAnneePublication() { return anneePublication; }
    public void setAnneePublication(Integer anneePublication) { 
        this.anneePublication = anneePublication; 
    }
    
    public Departement getDepartement() { return departement; }
    
    public void setDepartement(Departement departement) {
        // Maintient la bidirectionnalité
        if (this.departement != null) {
            this.departement.getLivres().remove(this);
        }
        this.departement = departement;
        if (departement != null && !departement.getLivres().contains(this)) {
            departement.getLivres().add(this);
        }
    }
    
    // ===== MÉTHODES UTILITAIRES =====
    @Override
    public String toString() {
        return String.format("Livre{id=%d, titre='%s', auteur='%s', prix=%.2f€}",
                           idLivre, titre, auteur, prix);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Livre livre = (Livre) o;
        
        if (idLivre != null && livre.idLivre != null) {
            return idLivre.equals(livre.idLivre);
        }
        return isbn != null && isbn.equals(livre.isbn);
    }
    
    @Override
    public int hashCode() {
        return isbn != null ? isbn.hashCode() : 0;
    }
}
```

### Script SQL Correspondant

```sql
CREATE TABLE livre (
    idLivre BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    auteur VARCHAR(150),
    prix DECIMAL(10,2),
    isbn VARCHAR(13) UNIQUE,
    anneePublication INT,
    idDep BIGINT NOT NULL,
    CONSTRAINT FK_livre_departement 
        FOREIGN KEY (idDep) REFERENCES departement(idDep)
);
```

## 8.3 Question 3 : cascade = CascadeType.ALL

### Définition

Propage automatiquement **toutes** les opérations du parent vers les enfants.

### Types de Cascade

**CascadeType.PERSIST**
```java
@OneToMany(cascade = CascadeType.PERSIST)
private List<Livre> livres;

Departement dept = new Departement("IT");
Livre livre = new Livre("Java", "Expert", 45.0);
dept.getLivres().add(livre);

session.persist(dept);  // Persiste aussi livre!
```

**CascadeType.MERGE**
```java
@OneToMany(cascade = CascadeType.MERGE)
private List<Livre> livres;

dept.setNomDep("IT Moderne");
dept.getLivres().get(0).setPrix(40.0);

session.merge(dept);  // Merge aussi les livres!
```

**CascadeType.REMOVE**
```java
@OneToMany(cascade = CascadeType.REMOVE)
private List<Livre> livres;

session.delete(dept);  // Supprime aussi TOUS les livres!
// ⚠️ ATTENTION: Peut supprimer beaucoup de données
```

**CascadeType.ALL**
```java
@OneToMany(cascade = CascadeType.ALL)
// Équivalent à tous les types combinés
```

### Exemples Pratiques

**Création avec CASCADE :**
```java
Departement dept = new Departement("Sciences");
Livre livre1 = new Livre("Physique", "Einstein", 60.0);
Livre livre2 = new Livre("Chimie", "Curie", 55.0);

livre1.setDepartement(dept);
livre2.setDepartement(dept);

session.persist(dept);  // Une ligne sauve tout!
```

**Recommandations :**

| Cascade | Composition | Association | Risques |
|---------|-------------|-------------|---------|
| PERSIST | ✓ Utile | ⚠️ Possible | Faible |
| MERGE | ✓ Utile | ⚠️ Attention | Moyen |
| REMOVE | ✓ Logique | ✗ Dangereux | ⚠️ Élevé |
| ALL | ⚠️ Précaution | ✗ Déconseillé | ⚠️ Très élevé |

## 8.4 Question 4 : Concepts Hibernate

### Transaction

**Unité de travail atomique ACID :**
```java
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();

// Opérations
Departement dept = new Departement("IT");
session.save(dept);

tx.commit();  // Valide tout ou rien
```

**Propriétés ACID :**
- **A**tomicité : Tout ou rien
- **C**ohérence : État valide
- **I**solation : Indépendance
- **D**urabilité : Changements permanents

### beginTransaction()

**Démarre une transaction :**
```java
Transaction tx = session.beginTransaction();

try {
    // Opérations
    tx.commit();
} catch (Exception e) {
    tx.rollback();  // Annule tout
    throw e;
}
```

### createQuery()

**Crée une requête HQL :**
```java
// HQL (orienté objet)
List<Departement> list = session
    .createQuery("FROM Departement WHERE categorie = :cat", 
                 Departement.class)
    .setParameter("cat", "IT")
    .list();

// Avec jointure
String hql = "FROM Departement d JOIN FETCH d.livres";
```

**HQL vs SQL :**
```java
// SQL
"SELECT * FROM departement WHERE nomDep = 'IT'"

// HQL
"FROM Departement WHERE nomDep = 'IT'"
// Utilise noms de classes et propriétés Java
```

### commit()

**Valide et rend permanent :**
```java
tx.commit();

// Hibernate fait:
// 1. Flush: Synchronise entités avec BD
// 2. Commit: Valide transaction JDBC
// 3. Clear (optionnel): Vide cache
```

### SessionFactory

**Factory thread-safe pour Sessions :**
```java
// Création (une fois au démarrage)
SessionFactory factory = new Configuration()
    .configure()
    .buildSessionFactory();

// Utilisation (par requête)
Session session = factory.openSession();
// ...
session.close();

// Fermeture (à l'arrêt)
factory.close();
```

**Caractéristiques :**
- Thread-safe (partageable)
- Coûteux à créer (créer une fois)
- Contient métadonnées et cache
- Configuration figée après création

## 8.5 Question 5 : updateDepartement

### Avec session.update()

```java
public void updateDepartement(Departement departement) {
    Session session = null;
    Transaction tx = null;
    
    try {
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        
        session.update(departement);
        
        tx.commit();
        
    } catch (Exception e) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        throw new RuntimeException("Échec mise à jour", e);
        
    } finally {
        if (session != null) {
            session.close();
        }
    }
}
```

### Avec session.merge() (Recommandé)

```java
public Departement updateDepartement(Departement departement) {
    Session session = null;
    Transaction tx = null;
    Departement merged = null;
    
    try {
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        
        merged = (Departement) session.merge(departement);
        
        tx.commit();
        
        return merged;
        
    } catch (Exception e) {
        if (tx != null) {
            tx.rollback();
        }
        throw new RuntimeException("Échec mise à jour", e);
        
    } finally {
        if (session != null) {
            session.close();
        }
    }
}
```

### Comparaison update() vs merge()

| Aspect | update() | merge() |
|--------|----------|---------|
| Entité détachée | ✓ OK | ✓ OK |
| Entité transient | ✗ Exception | ✓ INSERT |
| Entité attachée | ✗ Exception | ✓ OK |
| Retour | void | Entité gérée |
| Objet original | Devient attaché | Reste détaché |

## 8.6 Question 6 : Jakarta, Persistance, ORM

### Jakarta EE

**Plateforme de spécifications Java entreprise :**
- JPA (Persistance)
- Servlets (HTTP)
- EJB (Composants métier)
- CDI (Injection dépendances)
- JAX-RS (Services REST)

**Évolution :**
```
J2EE (1999) → Java EE (2006) → Jakarta EE (2019)
javax.*     → javax.*         → jakarta.*
```

### Persistance des Données

**Sauvegarde durable au-delà de l'exécution :**
```java
// Sans persistance
Produit p = new Produit("Laptop", 1000);
// Perdu à la fin du programme

// Avec persistance
session.save(p);
// Survit au-delà de l'exécution
```

**Cycle de vie :**
```
Transient → Persistent → Detached → Removed
   ↓            ↓           ↓          ↓
Nouveau    En session   Hors      Supprimé
 objet                 session     de BD
```

### ORM (Object-Relational Mapping)

**Conversion automatique objet ↔ relationnel :**

**Objet :**
```java
@Entity
public class Auteur {
    @Id private Long id;
    private String nom;
    
    @OneToMany
    private List<Livre> livres;
}
```

**Relationnel :**
```sql
CREATE TABLE auteur (
    id BIGINT PRIMARY KEY,
    nom VARCHAR(255)
);

CREATE TABLE livre (
    id BIGINT PRIMARY KEY,
    auteur_id BIGINT,
    FOREIGN KEY (auteur_id) REFERENCES auteur(id)
);
```

**Avantages ORM :**
✅ Productivité (moins de SQL)  
✅ Portabilité (indépendant BD)  
✅ Maintenabilité (code objet)  
✅ Cache automatique  
✅ Lazy loading  

**Inconvénients :**
❌ Courbe d'apprentissage  
❌ Performance (requêtes complexes)  
❌ N+1 queries problem  

---

## Points Clés à Retenir

✅ **@Entity** : Marque classe comme entité JPA  
✅ **@OneToMany/@ManyToOne** : Relations avec mappedBy  
✅ **FetchType.LAZY** : Chargement à la demande  
✅ **CascadeType.ALL** : Propage toutes opérations  
✅ **Transaction** : Unité atomique ACID  
✅ **SessionFactory** : Thread-safe, créée une fois  
✅ **merge()** : Plus flexible que update()  

---

**Document créé le :** 13 Janvier 2026  
**Exercice :** 8 - Hibernate ORM  
**Niveau :** Avancé
