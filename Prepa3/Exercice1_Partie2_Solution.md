# Exercice 1 Partie 2 : Solution Complète

## Contexte

Nous travaillons avec les classes définies dans la partie 1 :
- `Personnel` (classe mère)
- `Gerant` extends Personnel
- `Assistant` extends Personnel
- `Departement`

Collections disponibles :
```java
List<Personnel> lesPrs;  // Contient des Gérants et des Assistants
Set<Departement> lesDeps; // Contient des départements
```

---

## Question 1 : Méthode pour ajouter un département

**Contraintes** :
- Chaque département doit avoir un nom unique
- Pour ajouter un département, il faut l'affecter à un gérant

### Solution

```java
/**
 * Méthode pour ajouter un département au Set lesDeps
 * @param lesDeps - Le Set de départements
 * @param nomDepartement - Le nom du département à ajouter
 * @param gerant - Le gérant qui gérera ce département
 * @return true si l'ajout est réussi, false sinon
 */
public static boolean ajouterDepartement(Set<Departement> lesDeps, 
                                          String nomDepartement, 
                                          Gerant gerant) {
    // Vérifier que le gérant n'est pas null
    if (gerant == null) {
        System.out.println("Erreur : Un département doit être affecté à un gérant.");
        return false;
    }
    
    // Vérifier l'unicité du nom du département
    for (Departement dep : lesDeps) {
        if (dep.getNomD().equals(nomDepartement)) {
            System.out.println("Erreur : Un département avec le nom '" + 
                             nomDepartement + "' existe déjà.");
            return false;
        }
    }
    
    // Créer le nouveau département et l'affecter au gérant
    Departement nouveauDep = new Departement(nomDepartement, gerant);
    
    // Ajouter au Set
    boolean ajoutReussi = lesDeps.add(nouveauDep);
    
    if (ajoutReussi) {
        System.out.println("Département '" + nomDepartement + 
                         "' ajouté avec succès et affecté au gérant " + gerant.getIdE());
    }
    
    return ajoutReussi;
}
```

### Exemple d'utilisation

```java
// Supposons que lesPrs et lesDeps sont déjà remplis
Set<Departement> lesDeps = new HashSet<>();
List<Personnel> lesPrs = new ArrayList<>();

// Ajouter des gérants à lesPrs
Gerant g1 = new Gerant("G001", 15000, "Informatique");
lesPrs.add(g1);

// Ajouter un département
ajouterDepartement(lesDeps, "Développement", g1);
```

---

## Question 2 : Méthode pour supprimer un département

**Contrainte** : Ne pas oublier de traiter la relation entre département et gérant lors de la suppression.

### Solution

```java
/**
 * Méthode statique pour supprimer un département du Set
 * @param lesDeps - Le Set de départements
 * @param nomDepartement - Le nom du département à supprimer
 * @return true si la suppression est réussie, false sinon
 */
public static boolean supprimerDepartement(Set<Departement> lesDeps, 
                                            String nomDepartement) {
    // Rechercher le département par son nom
    Departement depASupprimer = null;
    
    for (Departement dep : lesDeps) {
        if (dep.getNomD().equals(nomDepartement)) {
            depASupprimer = dep;
            break;
        }
    }
    
    // Vérifier si le département existe
    if (depASupprimer == null) {
        System.out.println("Erreur : Aucun département avec le nom '" + 
                         nomDepartement + "' n'a été trouvé.");
        return false;
    }
    
    // Traiter la relation avec le gérant
    Gerant gerant = depASupprimer.getGerant();
    if (gerant != null) {
        // Retirer le département de la liste du gérant
        gerant.retirerDepartement(depASupprimer);
        System.out.println("Relation avec le gérant " + gerant.getIdE() + " supprimée.");
    }
    
    // Supprimer du Set
    boolean suppressionReussie = lesDeps.remove(depASupprimer);
    
    if (suppressionReussie) {
        System.out.println("Département '" + nomDepartement + "' supprimé avec succès.");
    }
    
    return suppressionReussie;
}
```

### Alternative : Supprimer par objet Departement

```java
/**
 * Méthode statique pour supprimer un département du Set (version avec objet)
 * @param lesDeps - Le Set de départements
 * @param departement - Le département à supprimer
 * @return true si la suppression est réussie, false sinon
 */
public static boolean supprimerDepartement(Set<Departement> lesDeps, 
                                            Departement departement) {
    if (departement == null) {
        System.out.println("Erreur : Le département est null.");
        return false;
    }
    
    // Vérifier si le département existe dans le Set
    if (!lesDeps.contains(departement)) {
        System.out.println("Erreur : Le département n'existe pas dans le Set.");
        return false;
    }
    
    // Traiter la relation avec le gérant
    Gerant gerant = departement.getGerant();
    if (gerant != null) {
        gerant.retirerDepartement(departement);
        System.out.println("Relation avec le gérant " + gerant.getIdE() + " supprimée.");
    }
    
    // Supprimer du Set
    boolean suppressionReussie = lesDeps.remove(departement);
    
    if (suppressionReussie) {
        System.out.println("Département '" + departement.getNomD() + 
                         "' supprimé avec succès.");
    }
    
    return suppressionReussie;
}
```

---

## Question 3 : Regrouper les assistants par salaire

**Objectif** : Créer une Map associant à chaque salaire le nombre d'assistants correspondant.

**Type de Map** : `Map<Double, Integer>` (salaire → nombre d'assistants)

### Solution

```java
/**
 * Méthode pour regrouper le nombre d'assistants par salaire
 * @param lesPrs - Liste contenant du Personnel (Gérants et Assistants)
 * @return Map associant chaque salaire au nombre d'assistants
 */
public static Map<Double, Integer> regrouperAssistantsParSalaire(List<Personnel> lesPrs) {
    Map<Double, Integer> assistantsParSalaire = new HashMap<>();
    
    // Parcourir la liste du personnel
    for (Personnel p : lesPrs) {
        // Vérifier si c'est un Assistant
        if (p instanceof Assistant) {
            double salaire = p.getSalaire();
            
            // Incrémenter le compteur pour ce salaire
            assistantsParSalaire.put(salaire, 
                                    assistantsParSalaire.getOrDefault(salaire, 0) + 1);
        }
    }
    
    return assistantsParSalaire;
}
```

### Solution avec Java 8 Streams (Alternative)

```java
/**
 * Version avec Streams Java 8
 */
public static Map<Double, Long> regrouperAssistantsParSalaireStream(List<Personnel> lesPrs) {
    return lesPrs.stream()
                 .filter(p -> p instanceof Assistant)  // Filtrer uniquement les Assistants
                 .collect(Collectors.groupingBy(
                     Personnel::getSalaire,             // Grouper par salaire
                     Collectors.counting()              // Compter les occurrences
                 ));
}
```

### Exemple d'utilisation

```java
// Créer et remplir la liste
List<Personnel> lesPrs = new ArrayList<>();
lesPrs.add(new Assistant("A001", 8000.0, 2020));
lesPrs.add(new Assistant("A002", 8000.0, 2019));
lesPrs.add(new Assistant("A003", 9500.0, 2021));
lesPrs.add(new Gerant("G001", 15000.0, "IT"));
lesPrs.add(new Assistant("A004", 8000.0, 2022));

// Regrouper les assistants par salaire
Map<Double, Integer> resultat = regrouperAssistantsParSalaire(lesPrs);

// Afficher les résultats
System.out.println("=== Nombre d'assistants par salaire ===");
for (Map.Entry<Double, Integer> entry : resultat.entrySet()) {
    System.out.println("Salaire: " + entry.getKey() + " DH -> " + 
                      entry.getValue() + " assistant(s)");
}
```

**Output attendu** :
```
=== Nombre d'assistants par salaire ===
Salaire: 8000.0 DH -> 3 assistant(s)
Salaire: 9500.0 DH -> 1 assistant(s)
```

---

## Question 4 : Regrouper les Gérants par spécialité

**Objectif** : Regrouper les Gérants ayant un salaire < 10000 par spécialité.

**Type de Map** : `Map<String, List<Gerant>>` (spécialité → liste de gérants)

### Solution

```java
/**
 * Méthode pour regrouper les Gérants par spécialité (salaire < 10000)
 * @param lesPrs - Liste contenant du Personnel
 * @return Map associant chaque spécialité aux gérants correspondants
 */
public static Map<String, List<Gerant>> regrouperGerantsParSpecialite(List<Personnel> lesPrs) {
    Map<String, List<Gerant>> gerantsParSpecialite = new HashMap<>();
    
    // Parcourir la liste du personnel
    for (Personnel p : lesPrs) {
        // Vérifier si c'est un Gérant avec salaire < 10000
        if (p instanceof Gerant && p.getSalaire() < 10000) {
            Gerant gerant = (Gerant) p;
            String specialite = gerant.getSpecialite();
            
            // Si la spécialité n'existe pas encore dans la Map, créer une nouvelle liste
            if (!gerantsParSpecialite.containsKey(specialite)) {
                gerantsParSpecialite.put(specialite, new ArrayList<>());
            }
            
            // Ajouter le gérant à la liste de sa spécialité
            gerantsParSpecialite.get(specialite).add(gerant);
        }
    }
    
    return gerantsParSpecialite;
}
```

### Solution avec Java 8 Streams (Alternative)

```java
/**
 * Version avec Streams Java 8
 */
public static Map<String, List<Gerant>> regrouperGerantsParSpecialiteStream(List<Personnel> lesPrs) {
    return lesPrs.stream()
                 .filter(p -> p instanceof Gerant)           // Filtrer les Gérants
                 .map(p -> (Gerant) p)                       // Cast en Gerant
                 .filter(g -> g.getSalaire() < 10000)        // Salaire < 10000
                 .collect(Collectors.groupingBy(
                     Gerant::getSpecialite                   // Grouper par spécialité
                 ));
}
```

### Exemple d'utilisation

```java
// Créer et remplir la liste
List<Personnel> lesPrs = new ArrayList<>();
lesPrs.add(new Gerant("G001", 8000.0, "Informatique"));
lesPrs.add(new Gerant("G002", 9500.0, "Informatique"));
lesPrs.add(new Gerant("G003", 12000.0, "Finance"));      // Sera exclu (salaire >= 10000)
lesPrs.add(new Gerant("G004", 7500.0, "RH"));
lesPrs.add(new Gerant("G005", 9000.0, "Finance"));
lesPrs.add(new Assistant("A001", 8000.0, 2020));

// Regrouper les gérants par spécialité
Map<String, List<Gerant>> resultat = regrouperGerantsParSpecialite(lesPrs);

// Afficher les résultats
System.out.println("=== Gérants (salaire < 10000) par spécialité ===");
for (Map.Entry<String, List<Gerant>> entry : resultat.entrySet()) {
    System.out.println("\nSpécialité: " + entry.getKey());
    for (Gerant g : entry.getValue()) {
        System.out.println("  - " + g.getIdE() + " (Salaire: " + g.getSalaire() + " DH)");
    }
}
```

**Output attendu** :
```
=== Gérants (salaire < 10000) par spécialité ===

Spécialité: Informatique
  - G001 (Salaire: 8000.0 DH)
  - G002 (Salaire: 9500.0 DH)

Spécialité: RH
  - G004 (Salaire: 7500.0 DH)

Spécialité: Finance
  - G005 (Salaire: 9000.0 DH)
```

---

## Code Complet de Test

```java
import java.util.*;

public class TestExercice1Partie2 {
    
    public static void main(String[] args) {
        // Initialiser les collections
        List<Personnel> lesPrs = new ArrayList<>();
        Set<Departement> lesDeps = new HashSet<>();
        
        // Créer des gérants
        Gerant g1 = new Gerant("G001", 8000.0, "Informatique");
        Gerant g2 = new Gerant("G002", 9500.0, "Informatique");
        Gerant g3 = new Gerant("G003", 12000.0, "Finance");
        Gerant g4 = new Gerant("G004", 7500.0, "RH");
        
        // Créer des assistants
        Assistant a1 = new Assistant("A001", 8000.0, 2020);
        Assistant a2 = new Assistant("A002", 8000.0, 2019);
        Assistant a3 = new Assistant("A003", 9500.0, 2021);
        Assistant a4 = new Assistant("A004", 8000.0, 2022);
        
        // Ajouter à la liste
        lesPrs.add(g1);
        lesPrs.add(g2);
        lesPrs.add(g3);
        lesPrs.add(g4);
        lesPrs.add(a1);
        lesPrs.add(a2);
        lesPrs.add(a3);
        lesPrs.add(a4);
        
        // Test Question 1 : Ajouter des départements
        System.out.println("=== Test Question 1 : Ajout de départements ===");
        ajouterDepartement(lesDeps, "Développement", g1);
        ajouterDepartement(lesDeps, "Comptabilité", g3);
        ajouterDepartement(lesDeps, "RH", g4);
        ajouterDepartement(lesDeps, "Développement", g2); // Doit échouer (nom existe)
        System.out.println();
        
        // Test Question 2 : Supprimer un département
        System.out.println("=== Test Question 2 : Suppression de département ===");
        supprimerDepartement(lesDeps, "RH");
        supprimerDepartement(lesDeps, "Marketing"); // Doit échouer (n'existe pas)
        System.out.println();
        
        // Test Question 3 : Regrouper assistants par salaire
        System.out.println("=== Test Question 3 : Assistants par salaire ===");
        Map<Double, Integer> assistantsParSalaire = regrouperAssistantsParSalaire(lesPrs);
        for (Map.Entry<Double, Integer> entry : assistantsParSalaire.entrySet()) {
            System.out.println("Salaire: " + entry.getKey() + " DH -> " + 
                             entry.getValue() + " assistant(s)");
        }
        System.out.println();
        
        // Test Question 4 : Regrouper gérants par spécialité
        System.out.println("=== Test Question 4 : Gérants par spécialité (salaire < 10000) ===");
        Map<String, List<Gerant>> gerantsParSpecialite = regrouperGerantsParSpecialite(lesPrs);
        for (Map.Entry<String, List<Gerant>> entry : gerantsParSpecialite.entrySet()) {
            System.out.println("\nSpécialité: " + entry.getKey());
            for (Gerant g : entry.getValue()) {
                System.out.println("  - " + g.getIdE() + " (Salaire: " + g.getSalaire() + " DH)");
            }
        }
    }
    
    // Méthodes définies précédemment...
    // (ajouterDepartement, supprimerDepartement, regrouperAssistantsParSalaire, 
    //  regrouperGerantsParSpecialite)
}
```

---

## Points Clés

1. **Question 1** : Vérification de l'unicité du nom + affectation obligatoire à un gérant
2. **Question 2** : Gestion bidirectionnelle de la relation lors de la suppression
3. **Question 3** : Utilisation de `instanceof` pour filtrer les Assistants + `getOrDefault()` pour compter
4. **Question 4** : Double filtrage (type Gerant + salaire < 10000) + regroupement par spécialité

Les solutions proposent à la fois des approches impératives classiques et des alternatives avec Java 8 Streams pour une meilleure lisibilité et concision du code.
