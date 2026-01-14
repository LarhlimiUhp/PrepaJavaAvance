# Exercice 3 : Java Streams - Solution Complète

## Énoncé

### Classe Produit

```java
public class Produit {
    private String id;        // Composé de chiffres
    private String nom;
    private double prix;
    private String categorie; // Electronique, meuble, librairie
    private int anneFabric;
    
    // Constructeurs, getters et setters
}
```

### Catégories existantes
- Electronique
- meuble
- librairie

### Objectif

Créer un **seul Stream** qui effectue les opérations suivantes :

1. **Appliquer une réduction de 15%** sur les produits de catégorie "Electronique" dont le prix est inférieur à 2000
2. **Modifier les IDs** :
   - Ne **PAS** changer les IDs des produits de catégorie "Librairie" et "Jardin"
   - Pour les **autres catégories**, changer l'ID pour : `[3 premières lettres]-[numéro]`
   - Exemple : ID "12" de catégorie "Electronique" → "ELE-12"
3. **Stocker les résultats** dans une collection accessible par indices (List)

---

## Solution

### 1. Classe Produit Complète

```java
public class Produit {
    private String id;
    private String nom;
    private double prix;
    private String categorie;
    private int anneFabric;
    
    // Constructeur
    public Produit(String id, String nom, double prix, String categorie, int anneFabric) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.categorie = categorie;
        this.anneFabric = anneFabric;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public double getPrix() {
        return prix;
    }
    
    public String getCategorie() {
        return categorie;
    }
    
    public int getAnneFabric() {
        return anneFabric;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public void setPrix(double prix) {
        this.prix = prix;
    }
    
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
    
    public void setAnneFabric(int anneFabric) {
        this.anneFabric = anneFabric;
    }
    
    @Override
    public String toString() {
        return "Produit{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", categorie='" + categorie + '\'' +
                ", anneFabric=" + anneFabric +
                '}';
    }
}
```

---

### 2. Solution avec un seul Stream

```java
import java.util.*;
import java.util.stream.*;

public class ExerciceStreams {
    
    public static List<Produit> traiterProduits(Set<Produit> produits) {
        return produits.stream()
            // Étape 1 : Appliquer la réduction de 15% sur les produits Electronique < 2000
            .map(p -> {
                if (p.getCategorie().equalsIgnoreCase("Electronique") && p.getPrix() < 2000) {
                    p.setPrix(p.getPrix() * 0.85); // Réduction de 15%
                }
                return p;
            })
            // Étape 2 : Modifier les IDs selon les règles
            .map(p -> {
                String categorie = p.getCategorie();
                
                // Ne pas changer les IDs pour Librairie et Jardin
                if (categorie.equalsIgnoreCase("Librairie") || 
                    categorie.equalsIgnoreCase("Jardin")) {
                    return p;
                }
                
                // Pour les autres catégories, changer le format de l'ID
                String prefixe = categorie.substring(0, Math.min(3, categorie.length()))
                                         .toUpperCase();
                String nouveauId = prefixe + "-" + p.getId();
                p.setId(nouveauId);
                
                return p;
            })
            // Étape 3 : Collecter dans une List (accessible par indices)
            .collect(Collectors.toList());
    }
}
```

---

### 3. Solution Alternative (Sans Modifier les Objets Originaux)

Si on veut éviter de modifier les objets originaux, on peut créer de nouveaux objets :

```java
public static List<Produit> traiterProduitsImmutable(Set<Produit> produits) {
    return produits.stream()
        .map(p -> {
            // Créer une copie du produit
            String id = p.getId();
            String nom = p.getNom();
            double prix = p.getPrix();
            String categorie = p.getCategorie();
            int anneFabric = p.getAnneFabric();
            
            // Appliquer la réduction de 15% si nécessaire
            if (categorie.equalsIgnoreCase("Electronique") && prix < 2000) {
                prix = prix * 0.85;
            }
            
            // Modifier l'ID si nécessaire
            if (!categorie.equalsIgnoreCase("Librairie") && 
                !categorie.equalsIgnoreCase("Jardin")) {
                String prefixe = categorie.substring(0, Math.min(3, categorie.length()))
                                         .toUpperCase();
                id = prefixe + "-" + id;
            }
            
            // Retourner un nouveau produit avec les modifications
            return new Produit(id, nom, prix, categorie, anneFabric);
        })
        .collect(Collectors.toList());
}
```

---

### 4. Solution Optimisée avec peek() pour le débogage

```java
public static List<Produit> traiterProduitsAvecDebug(Set<Produit> produits) {
    return produits.stream()
        .peek(p -> System.out.println("Avant traitement: " + p))
        
        // Réduction de 15% pour Electronique < 2000
        .map(p -> {
            if (p.getCategorie().equalsIgnoreCase("Electronique") && p.getPrix() < 2000) {
                p.setPrix(p.getPrix() * 0.85);
            }
            return p;
        })
        .peek(p -> System.out.println("Après réduction: " + p))
        
        // Modification des IDs
        .map(p -> {
            String categorie = p.getCategorie();
            
            if (!categorie.equalsIgnoreCase("Librairie") && 
                !categorie.equalsIgnoreCase("Jardin")) {
                String prefixe = categorie.substring(0, Math.min(3, categorie.length()))
                                         .toUpperCase();
                p.setId(prefixe + "-" + p.getId());
            }
            return p;
        })
        .peek(p -> System.out.println("Après modification ID: " + p))
        
        .collect(Collectors.toList());
}
```

---

## Code de Test Complet

```java
import java.util.*;
import java.util.stream.*;

public class TestExercice3 {
    
    public static void main(String[] args) {
        // Créer un Set de produits
        Set<Produit> produits = new HashSet<>();
        
        // Produits Electronique (certains < 2000, d'autres >= 2000)
        produits.add(new Produit("1", "Smartphone", 1500.0, "Electronique", 2023));
        produits.add(new Produit("2", "Laptop", 2500.0, "Electronique", 2023));
        produits.add(new Produit("3", "Tablette", 800.0, "Electronique", 2022));
        produits.add(new Produit("4", "Écouteurs", 150.0, "Electronique", 2024));
        
        // Produits Meuble
        produits.add(new Produit("5", "Chaise", 250.0, "meuble", 2023));
        produits.add(new Produit("6", "Table", 600.0, "meuble", 2022));
        
        // Produits Librairie (IDs ne doivent PAS changer)
        produits.add(new Produit("7", "Livre Java", 45.0, "Librairie", 2023));
        produits.add(new Produit("8", "Cahier", 15.0, "Librairie", 2024));
        
        // Produit Jardin (IDs ne doivent PAS changer)
        produits.add(new Produit("9", "Tondeuse", 350.0, "Jardin", 2023));
        
        System.out.println("========================================");
        System.out.println("PRODUITS AVANT TRAITEMENT");
        System.out.println("========================================");
        produits.forEach(System.out::println);
        
        // Traiter les produits avec le Stream
        List<Produit> produitsTraites = traiterProduits(produits);
        
        System.out.println("\n========================================");
        System.out.println("PRODUITS APRÈS TRAITEMENT");
        System.out.println("========================================");
        produitsTraites.forEach(System.out::println);
        
        System.out.println("\n========================================");
        System.out.println("VÉRIFICATIONS");
        System.out.println("========================================");
        
        // Vérifier les réductions
        System.out.println("\n--- Produits Electronique < 2000 (réduction appliquée) ---");
        produitsTraites.stream()
            .filter(p -> p.getCategorie().equalsIgnoreCase("Electronique"))
            .filter(p -> p.getId().startsWith("ELE-"))
            .forEach(p -> {
                System.out.println(p.getNom() + " : " + p.getPrix() + " DH (ID: " + p.getId() + ")");
            });
        
        // Vérifier les IDs non modifiés
        System.out.println("\n--- Produits Librairie et Jardin (IDs non modifiés) ---");
        produitsTraites.stream()
            .filter(p -> p.getCategorie().equalsIgnoreCase("Librairie") || 
                        p.getCategorie().equalsIgnoreCase("Jardin"))
            .forEach(p -> {
                System.out.println(p.getNom() + " : ID = " + p.getId());
            });
        
        // Vérifier les IDs modifiés
        System.out.println("\n--- Produits Meuble (IDs modifiés) ---");
        produitsTraites.stream()
            .filter(p -> p.getCategorie().equalsIgnoreCase("meuble"))
            .forEach(p -> {
                System.out.println(p.getNom() + " : ID = " + p.getId());
            });
        
        // Vérifier que c'est une List (accessible par indices)
        System.out.println("\n--- Accès par index (preuve que c'est une List) ---");
        System.out.println("Produit à l'index 0 : " + produitsTraites.get(0).getNom());
        System.out.println("Produit à l'index 2 : " + produitsTraites.get(2).getNom());
        System.out.println("Taille de la liste : " + produitsTraites.size());
    }
    
    // Méthode de traitement (définie précédemment)
    public static List<Produit> traiterProduits(Set<Produit> produits) {
        return produits.stream()
            .map(p -> {
                if (p.getCategorie().equalsIgnoreCase("Electronique") && p.getPrix() < 2000) {
                    p.setPrix(p.getPrix() * 0.85);
                }
                return p;
            })
            .map(p -> {
                String categorie = p.getCategorie();
                
                if (!categorie.equalsIgnoreCase("Librairie") && 
                    !categorie.equalsIgnoreCase("Jardin")) {
                    String prefixe = categorie.substring(0, Math.min(3, categorie.length()))
                                             .toUpperCase();
                    String nouveauId = prefixe + "-" + p.getId();
                    p.setId(nouveauId);
                }
                return p;
            })
            .collect(Collectors.toList());
    }
}
```

---

## Output Attendu

```
========================================
PRODUITS AVANT TRAITEMENT
========================================
Produit{id='1', nom='Smartphone', prix=1500.0, categorie='Electronique', anneFabric=2023}
Produit{id='2', nom='Laptop', prix=2500.0, categorie='Electronique', anneFabric=2023}
Produit{id='3', nom='Tablette', prix=800.0, categorie='Electronique', anneFabric=2022}
Produit{id='4', nom='Écouteurs', prix=150.0, categorie='Electronique', anneFabric=2024}
Produit{id='5', nom='Chaise', prix=250.0, categorie='meuble', anneFabric=2023}
Produit{id='6', nom='Table', prix=600.0, categorie='meuble', anneFabric=2022}
Produit{id='7', nom='Livre Java', prix=45.0, categorie='Librairie', anneFabric=2023}
Produit{id='8', nom='Cahier', prix=15.0, categorie='Librairie', anneFabric=2024}
Produit{id='9', nom='Tondeuse', prix=350.0, categorie='Jardin', anneFabric=2023}

========================================
PRODUITS APRÈS TRAITEMENT
========================================
Produit{id='ELE-1', nom='Smartphone', prix=1275.0, categorie='Electronique', anneFabric=2023}
Produit{id='ELE-2', nom='Laptop', prix=2500.0, categorie='Electronique', anneFabric=2023}
Produit{id='ELE-3', nom='Tablette', prix=680.0, categorie='Electronique', anneFabric=2022}
Produit{id='ELE-4', nom='Écouteurs', prix=127.5, categorie='Electronique', anneFabric=2024}
Produit{id='MEU-5', nom='Chaise', prix=250.0, categorie='meuble', anneFabric=2023}
Produit{id='MEU-6', nom='Table', prix=600.0, categorie='meuble', anneFabric=2022}
Produit{id='7', nom='Livre Java', prix=45.0, categorie='Librairie', anneFabric=2023}
Produit{id='8', nom='Cahier', prix=15.0, categorie='Librairie', anneFabric=2024}
Produit{id='9', nom='Tondeuse', prix=350.0, categorie='Jardin', anneFabric=2023}

========================================
VÉRIFICATIONS
========================================

--- Produits Electronique < 2000 (réduction appliquée) ---
Smartphone : 1275.0 DH (ID: ELE-1)
Tablette : 680.0 DH (ID: ELE-3)
Écouteurs : 127.5 DH (ID: ELE-4)

--- Produits Librairie et Jardin (IDs non modifiés) ---
Livre Java : ID = 7
Cahier : ID = 8
Tondeuse : ID = 9

--- Produits Meuble (IDs modifiés) ---
Chaise : ID = MEU-5
Table : ID = MEU-6

--- Accès par index (preuve que c'est une List) ---
Produit à l'index 0 : Smartphone
Produit à l'index 2 : Tablette
Taille de la liste : 9
```

---

## Explications Détaillées

### 1. Premier `map()` : Réduction de 15%

```java
.map(p -> {
    if (p.getCategorie().equalsIgnoreCase("Electronique") && p.getPrix() < 2000) {
        p.setPrix(p.getPrix() * 0.85); // 100% - 15% = 85%
    }
    return p;
})
```

- Vérifie si le produit est de catégorie "Electronique" **ET** prix < 2000
- Applique la réduction : `prix * 0.85` (équivaut à une réduction de 15%)
- Exemples :
  - Smartphone (1500 DH) → 1275 DH
  - Tablette (800 DH) → 680 DH
  - Laptop (2500 DH) → **pas de réduction** (prix >= 2000)

### 2. Deuxième `map()` : Modification des IDs

```java
.map(p -> {
    String categorie = p.getCategorie();
    
    // Ne pas modifier pour Librairie et Jardin
    if (!categorie.equalsIgnoreCase("Librairie") && 
        !categorie.equalsIgnoreCase("Jardin")) {
        
        // Extraire les 3 premières lettres
        String prefixe = categorie.substring(0, Math.min(3, categorie.length()))
                                 .toUpperCase();
        
        // Créer le nouvel ID
        String nouveauId = prefixe + "-" + p.getId();
        p.setId(nouveauId);
    }
    return p;
})
```

- **Librairie et Jardin** : IDs conservés tels quels
- **Autres catégories** :
  - Electronique → ELE
  - meuble → MEU
  - Format : `PREFIXE-ID_ORIGINAL`

### 3. `collect(Collectors.toList())` : Collection Indexable

```java
.collect(Collectors.toList())
```

- Collecte tous les éléments dans une **ArrayList**
- Permet l'accès par index : `list.get(0)`, `list.get(2)`, etc.
- Maintient l'ordre des éléments

---

## Points Clés

1. **Un seul Stream** : Toutes les opérations sont enchaînées dans un pipeline unique
2. **Deux `map()` successifs** : Séparation claire des responsabilités (réduction puis modification d'ID)
3. **Conditions imbriquées** : Gestion des différents cas (catégories, prix)
4. **`Math.min()`** : Protection contre les catégories ayant moins de 3 lettres
5. **`equalsIgnoreCase()`** : Comparaison insensible à la casse
6. **`collect(Collectors.toList())`** : Résultat dans une List accessible par indices

---

## Variantes Possibles

### Avec filter() + map() séparés

```java
return produits.stream()
    .map(p -> {
        // Appliquer réduction
        if (p.getCategorie().equalsIgnoreCase("Electronique") && p.getPrix() < 2000) {
            p.setPrix(p.getPrix() * 0.85);
        }
        
        // Modifier ID
        String categorie = p.getCategorie();
        if (!categorie.equalsIgnoreCase("Librairie") && 
            !categorie.equalsIgnoreCase("Jardin")) {
            String prefixe = categorie.substring(0, Math.min(3, categorie.length()))
                                     .toUpperCase();
            p.setId(prefixe + "-" + p.getId());
        }
        
        return p;
    })
    .collect(Collectors.toList());
```

Cette solution est également valide et plus concise (un seul `map()`).
