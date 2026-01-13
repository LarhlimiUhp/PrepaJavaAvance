# Compte Rendu Détaillé : Révision Développement Java Avancé 2025-2026

**Étudiant :** [Votre Nom]  
**Date :** 09 Janvier 2026  
**Module :** Développement Java Avancé  
**Document source :** RevisionGlobale_JavaAvancee2526.pdf

---

## Table des Matières

1. [Rappels Fondamentaux Java](#rappels)
2. [Exercice 1 - Programmation Orientée Objet et Collections](#exercice-1)
3. [Exercice 3 - Java Streams API](#exercice-3)
4. [Exercice 4 - Gestion des Exceptions](#exercice-4)
5. [Exercice 5 - ThreadPoolExecutor / ExecutorService](#exercice-5)
6. [Exercice 6 - Synchronisation Multi-threading](#exercice-6)
7. [Exercice 7 - Threads et Bases de Données](#exercice-7)
8. [Exercice 8 - Hibernate ORM](#exercice-8)
9. [Synthèse et Conclusions](#synthese)

---

## <a name="rappels"></a>1. Rappels Fondamentaux Java

### 1.1 Héritage et Polymorphisme

#### Concept de Base

**Héritage** : Mécanisme permettant à une classe (enfant) d'hériter des propriétés et méthodes d'une autre classe (parent).

```java
// Classe parent
public class Animal {
    protected String nom;
    
    public void manger() {
        System.out.println(nom + " mange");
    }
}

// Classe enfant
public class Chien extends Animal {
    // Hérite de 'nom' et 'manger()'
    
    public void aboyer() {
        System.out.println(nom + " aboie");
    }
    
    // Redéfinition (Override)
    @Override
    public void manger() {
        System.out.println(nom + " mange des croquettes");
    }
}
```

**Points clés :**
- Mot-clé `extends` pour hériter
- `super()` appelle le constructeur parent
- `super.methode()` appelle la méthode parent
- `@Override` indique une redéfinition
- Une classe ne peut hériter que d'une seule classe (pas d'héritage multiple)

**Polymorphisme** : Capacité d'un objet à prendre plusieurs formes.

```java
Animal animal1 = new Chien();  // Polymorphisme
animal1.manger();  // Appelle la version de Chien (liaison dynamique)

// Casting
if (animal1 instanceof Chien) {
    Chien chien = (Chien) animal1;
    chien.aboyer();
}
```

#### Modificateurs d'Accès

| Modificateur | Classe | Package | Sous-classe | Monde |
|--------------|--------|---------|-------------|-------|
| `public` | ✓ | ✓ | ✓ | ✓ |
| `protected` | ✓ | ✓ | ✓ | ✗ |
| *default* | ✓ | ✓ | ✗ | ✗ |
| `private` | ✓ | ✗ | ✗ | ✗ |

**Règles importantes :**
- `private` : Accessible uniquement dans la classe
- `protected` : Accessible dans le package ET les sous-classes
- `public` : Accessible partout
- Sans modificateur (default) : Accessible dans le package uniquement

### 1.2 Collections Java - Rappel Complet

#### Hiérarchie des Collections

```
Collection (interface)
├── List (interface)
│   ├── ArrayList (classe)
│   ├── LinkedList (classe)
│   └── Vector (classe)
├── Set (interface)
│   ├── HashSet (classe)
│   ├── LinkedHashSet (classe)
│   └── TreeSet (classe)
└── Queue (interface)
    ├── PriorityQueue (classe)
    └── LinkedList (classe)

Map (interface - séparée)
├── HashMap (classe)
├── LinkedHashMap (classe)
├── TreeMap (classe)
└── Hashtable (classe)
```

#### List - Collection Ordonnée

**Caractéristiques :**
- Maintient l'ordre d'insertion
- Autorise les doublons
- Accès par index

**ArrayList** : Tableau dynamique
```java
List<String> liste = new ArrayList<>();
liste.add("A");           // Ajout
liste.add(0, "B");        // Insertion à l'index 0
liste.get(0);             // Récupération : "B"
liste.set(0, "C");        // Modification
liste.remove(0);          // Suppression
liste.size();             // Taille
liste.contains("A");      // Contient ?
liste.indexOf("A");       // Index de "A"
```

**Performance ArrayList :**
- get(index) : O(1) - très rapide
- add(element) : O(1) amortisé
- add(index, element) : O(n) - lent
- remove(index) : O(n) - lent

**LinkedList** : Liste chaînée
```java
LinkedList<String> liste = new LinkedList<>();
liste.addFirst("A");      // Ajout au début
liste.addLast("B");       // Ajout à la fin
liste.removeFirst();      // Suppression début
liste.removeLast();       // Suppression fin
```

**Performance LinkedList :**
- get(index) : O(n) - lent
- add(element) : O(1)
- add(index, element) : O(n)
- addFirst/addLast : O(1) - rapide

**Quand utiliser quoi ?**
- **ArrayList** : Accès fréquent par index, peu d'insertions/suppressions au milieu
- **LinkedList** : Insertions/suppressions fréquentes au début/fin, peu d'accès par index

#### Set - Collection Sans Doublons

**Caractéristiques :**
- Pas de doublons (unicité garantie)
- Ordre non garanti (sauf LinkedHashSet et TreeSet)

**HashSet** : Basé sur table de hachage
```java
Set<String> set = new HashSet<>();
set.add("A");             // Ajout
set.add("A");             // Ignoré (doublon)
set.remove("A");          // Suppression
set.contains("A");        // Contient ?
set.size();               // Taille

// Parcours
for (String s : set) {
    System.out.println(s);
}
```

**Performance HashSet :**
- add, remove, contains : O(1) - très rapide
- Ordre imprévisible

**LinkedHashSet** : Maintient l'ordre d'insertion
```java
Set<String> set = new LinkedHashSet<>();
set.add("B");
set.add("A");
set.add("C");
// Ordre préservé : B, A, C
```

**TreeSet** : Ensemble trié
```java
Set<Integer> set = new TreeSet<>();
set.add(5);
set.add(1);
set.add(3);
// Ordre naturel : 1, 3, 5
```

**Performance TreeSet :**
- add, remove, contains : O(log n)
- Toujours trié

#### Map - Associations Clé-Valeur

**Caractéristiques :**
- Paires clé-valeur
- Clés uniques (pas de doublons)
- Valeurs peuvent être dupliquées

**HashMap** : Table de hachage
```java
Map<String, Integer> map = new HashMap<>();
map.put("Alice", 25);     // Ajout/Modification
map.get("Alice");         // Récupération : 25
map.remove("Alice");      // Suppression
map.containsKey("Alice"); // Contient la clé ?
map.containsValue(25);    // Contient la valeur ?
map.size();               // Taille

// Parcours des clés
for (String key : map.keySet()) {
    System.out.println(key);
}

// Parcours des valeurs
for (Integer value : map.values()) {
    System.out.println(value);
}

// Parcours des paires
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " : " + entry.getValue());
}
```

**Performance HashMap :**
- put, get, remove : O(1) - très rapide
- Ordre non garanti

**TreeMap** : Map triée par clés
```java
Map<String, Integer> map = new TreeMap<>();
map.put("Charlie", 30);
map.put("Alice", 25);
map.put("Bob", 28);
// Ordre alphabétique des clés : Alice, Bob, Charlie
```

#### Comparaison des Collections

| Collection | Ordre | Doublons | Performance recherche | Usage |
|------------|-------|----------|----------------------|-------|
| **ArrayList** | Oui | Oui | O(n) | Accès indexé fréquent |
| **LinkedList** | Oui | Oui | O(n) | Insert/delete début/fin |
| **HashSet** | Non | Non | O(1) | Unicité, recherche rapide |
| **TreeSet** | Oui (trié) | Non | O(log n) | Ensemble trié |
| **HashMap** | Non | Clés: Non, Valeurs: Oui | O(1) | Associations rapides |
| **TreeMap** | Oui (trié) | Clés: Non, Valeurs: Oui | O(log n) | Map triée |

### 1.3 Generics - Rappel

**Définition** : Permettent de créer des classes/méthodes avec des types paramétrés.

```java
// Sans generics (ancien style - à éviter)
List liste = new ArrayList();
liste.add("texte");
liste.add(123);  // Autorisé mais dangereux
String s = (String) liste.get(0);  // Cast nécessaire

// Avec generics (moderne)
List<String> liste = new ArrayList<>();
liste.add("texte");
// liste.add(123);  // Erreur de compilation - sécurité du type
String s = liste.get(0);  // Pas de cast nécessaire
```

**Avantages :**
- Sécurité du type à la compilation
- Pas de cast nécessaire
- Code plus lisible et maintenable

**Méthode générique :**
```java
public <T> T getPremierElement(List<T> liste) {
    return liste.isEmpty() ? null : liste.get(0);
}

// Utilisation
List<String> mots = Arrays.asList("a", "b");
String premier = getPremierElement(mots);  // Type inféré
```

**Wildcards :**
```java
// ? extends T : accepte T et ses sous-classes (upper bound)
public void traiter(List<? extends Number> nombres) {
    // Peut lire, ne peut pas modifier
}

// ? super T : accepte T et ses super-classes (lower bound)
public void ajouter(List<? super Integer> liste) {
    liste.add(42);  // OK
}

// ? : n'importe quel type (unbounded)
public int compter(List<?> liste) {
    return liste.size();
}
```

### 1.4 Exceptions - Hiérarchie Complète

```
Throwable
├── Error (erreurs système - ne pas capturer)
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── VirtualMachineError
└── Exception
    ├── RuntimeException (non vérifiées)
    │   ├── NullPointerException
    │   ├── IndexOutOfBoundsException
    │   ├── IllegalArgumentException
    │   ├── ArithmeticException
    │   └── ClassCastException
    └── Exceptions vérifiées (checked)
        ├── IOException
        ├── SQLException
        ├── ParseException
        └── ClassNotFoundException
```

**Exceptions vérifiées vs non vérifiées :**

| Type | Vérification | Gestion obligatoire | Exemples |
|------|--------------|---------------------|----------|
| **Checked** | À la compilation | Oui (try-catch ou throws) | IOException, SQLException |
| **Unchecked** | À l'exécution | Non (optionnelle) | NullPointerException, IndexOutOfBounds |

**Try-catch-finally complet :**
```java
try {
    // Code pouvant lever une exception
    FileReader fr = new FileReader("fichier.txt");
    // ...
} catch (FileNotFoundException e) {
    // Gestion spécifique
    System.err.println("Fichier non trouvé: " + e.getMessage());
} catch (IOException e) {
    // Gestion plus générale
    System.err.println("Erreur I/O: " + e.getMessage());
} catch (Exception e) {
    // Catch-all (à éviter généralement)
    e.printStackTrace();
} finally {
    // Exécuté TOUJOURS (même si return dans try)
    // Nettoyage des ressources
}
```

**Try-with-resources (Java 7+) :**
```java
// Fermeture automatique des ressources
try (FileReader fr = new FileReader("fichier.txt");
     BufferedReader br = new BufferedReader(fr)) {
    
    String ligne = br.readLine();
    
} catch (IOException e) {
    e.printStackTrace();
}
// fr et br sont automatiquement fermés
```

**Créer ses propres exceptions :**
```java
public class SoldeInsuffisantException extends Exception {
    public SoldeInsuffisantException(String message) {
        super(message);
    }
}

// Utilisation
public void retirer(double montant) throws SoldeInsuffisantException {
    if (solde < montant) {
        throw new SoldeInsuffisantException("Solde insuffisant");
    }
    solde -= montant;
}
```

### 1.5 Interfaces et Classes Abstraites

#### Classes Abstraites

```java
public abstract class Forme {
    protected String couleur;
    
    // Méthode concrète
    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }
    
    // Méthode abstraite (pas d'implémentation)
    public abstract double calculerAire();
    
    public abstract double calculerPerimetre();
}

// Classe concrète
public class Cercle extends Forme {
    private double rayon;
    
    @Override
    public double calculerAire() {
        return Math.PI * rayon * rayon;
    }
    
    @Override
    public double calculerPerimetre() {
        return 2 * Math.PI * rayon;
    }
}
```

**Caractéristiques :**
- Peut avoir méthodes abstraites ET concrètes
- Peut avoir attributs
- Peut avoir constructeurs
- Ne peut pas être instanciée directement
- Une classe ne peut hériter que d'une seule classe abstraite

#### Interfaces

```java
public interface Dessinable {
    // Constantes (public static final implicite)
    String COULEUR_DEFAUT = "noir";
    
    // Méthodes abstraites (public abstract implicite)
    void dessiner();
    
    // Méthodes par défaut (Java 8+)
    default void afficher() {
        System.out.println("Affichage par défaut");
    }
    
    // Méthodes statiques (Java 8+)
    static void info() {
        System.out.println("Interface Dessinable");
    }
}

// Implémentation
public class Rectangle implements Dessinable {
    @Override
    public void dessiner() {
        System.out.println("Dessin d'un rectangle");
    }
    // afficher() est hérité (méthode par défaut)
}

// Implémentation multiple
public class Carre implements Dessinable, Comparable<Carre> {
    @Override
    public void dessiner() { }
    
    @Override
    public int compareTo(Carre autre) {
        return 0;
    }
}
```

**Comparaison Classe Abstraite vs Interface :**

| Aspect | Classe Abstraite | Interface |
|--------|------------------|-----------|
| Héritage | Simple (extends 1) | Multiple (implements N) |
| Méthodes concrètes | Oui | Oui (default depuis Java 8) |
| Attributs | Oui (tous types) | Oui (constantes uniquement) |
| Constructeurs | Oui | Non |
| Usage | Relation "est un" | Relation "peut faire" |

**Quand utiliser quoi ?**
- **Classe abstraite** : Quand les classes partagent du code commun et une relation forte
- **Interface** : Quand on définit un contrat/comportement, surtout si besoin d'héritage multiple

---

## <a name="exercice-1"></a>2. Exercice 1 : Programmation Orientée Objet et Collections

### 2.1 Partie 1 : Conception des Classes avec Relations

#### 2.1.1 Analyse du Problème

**Énoncé :** Implémenter une hiérarchie de classes Personnel → (Gérant, Assistant) avec relation Gérant ↔ Département.

**Contraintes :**
1. Un gérant peut gérer plusieurs départements (one-to-many)
2. Un département ne peut être géré que par un seul gérant (many-to-one)
3. Relation bidirectionnelle (navigation dans les deux sens)
4. Le type de relation doit être déductible du code

#### 2.1.2 Design Pattern : Relation Bidirectionnelle

**Principe** : Chaque côté de la relation maintient une référence vers l'autre.

```
Gerant                    Departement
+departements   ←→        +gerant
List<Departement>         Gerant
```

**Responsabilités :**
- **Gérant** : Maintient la liste de ses départements
- **Département** : Connaît son gérant
- **Méthodes** : Doivent synchroniser les deux côtés

#### 2.1.3 Implémentation Détaillée

**Classe Personnel (Mère) :**
```java
public class Personnel {
    // Attributs privés pour encapsulation
    private String nom;
    private String prenom;
    private double salaire;
    
    /**
     * Constructeur avec tous les attributs (requis par l'énoncé)
     * @param nom Le nom du personnel
     * @param prenom Le prénom du personnel
     * @param salaire Le salaire du personnel
     */
    public Personnel(String nom, String prenom, double salaire) {
        // Validation possible
        if (salaire < 0) {
            throw new IllegalArgumentException("Salaire ne peut pas être négatif");
        }
        this.nom = nom;
        this.prenom = prenom;
        this.salaire = salaire;
    }
    
    // Getters et Setters avec validation
    public String getNom() { 
        return nom; 
    }
    
    public void setNom(String nom) { 
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Nom ne peut pas être vide");
        }
        this.nom = nom; 
    }
    
    public String getPrenom() { 
        return prenom; 
    }
    
    public void setPrenom(String prenom) { 
        this.prenom = prenom; 
    }
    
    public double getSalaire() { 
        return salaire; 
    }
    
    public void setSalaire(double salaire) {
        if (salaire < 0) {
            throw new IllegalArgumentException("Salaire ne peut pas être négatif");
        }
        this.salaire = salaire; 
    }
    
    @Override
    public String toString() {
        return String.format("%s %s (Salaire: %.2f)", prenom, nom, salaire);
    }
}
```

**Classe Assistant :**
```java
public class Assistant extends Personnel {
    private int anneeDebut;
    
    /**
     * Constructeur Assistant
     * Appelle super() pour initialiser les attributs hérités
     */
    public Assistant(String nom, String prenom, double salaire, int anneeDebut) {
        super(nom, prenom, salaire);  // Appel OBLIGATOIRE au constructeur parent
        
        // Validation de l'année
        int anneeActuelle = java.time.Year.now().getValue();
        if (anneeDebut > anneeActuelle) {
            throw new IllegalArgumentException("Année de début invalide");
        }
        this.anneeDebut = anneeDebut;
    }
    
    public int getAnneeDebut() { 
        return anneeDebut; 
    }
    
    public void setAnneeDebut(int anneeDebut) { 
        this.anneeDebut = anneeDebut; 
    }
    
    /**
     * Calcule l'ancienneté de l'assistant
     */
    public int getAnciennete() {
        return java.time.Year.now().getValue() - anneeDebut;
    }
    
    @Override
    public String toString() {
        return String.format("Assistant: %s (Début: %d, Ancienneté: %d ans)", 
                           super.toString(), anneeDebut, getAnciennete());
    }
}
```

**Classe Gérant avec Gestion de Relation :**
```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Gerant extends Personnel {
    private String specialite;
    
    // Relation One-to-Many vers Departement
    // List indique qu'un gérant peut avoir PLUSIEURS départements
    private List<Departement> departements;
    
    public Gerant(String nom, String prenom, double salaire, String specialite) {
        super(nom, prenom, salaire);
        this.specialite = specialite;
        // Initialisation de la collection
        this.departements = new ArrayList<>();
    }
    
    public String getSpecialite() { 
        return specialite; 
    }
    
    public void setSpecialite(String specialite) { 
        this.specialite = specialite; 
    }
    
    /**
     * Retourne une copie non modifiable pour protéger l'encapsulation
     */
    public List<Departement> getDepartements() { 
        return Collections.unmodifiableList(departements); 
    }
    
    /**
     * Ajoute un département à la liste ET établit la relation inverse
     * Pattern: Gestion bidirectionnelle de la relation
     */
    public void ajouterDepartement(Departement dept) {
        // Vérification null
        if (dept == null) {
            throw new IllegalArgumentException("Département ne peut pas être null");
        }
        
        // Éviter les doublons
        if (!this.departements.contains(dept)) {
            this.departements.add(dept);
            
            // Établir la relation inverse (bidirectionnalité)
            // Vérifier que la relation n'est pas déjà établie pour éviter récursion infinie
            if (dept.getGerant() != this) {
                dept.setGerant(this);
            }
        }
    }
    
    /**
     * Retire un département de la liste ET rompt la relation inverse
     */
    public void retirerDepartement(Departement dept) {
        if (dept == null) {
            return;
        }
        
        if (this.departements.remove(dept)) {
            // Rompre la relation inverse
            if (dept.getGerant() == this) {
                dept.setGerant(null);
            }
        }
    }
    
    /**
     * Compte le nombre de départements gérés
     */
    public int getNombreDepartements() {
        return departements.size();
    }
    
    @Override
    public String toString() {
        return String.format("Gérant: %s (Spécialité: %s, Dép.: %d)", 
                           super.toString(), specialite, getNombreDepartements());
    }
}
```

**Classe Département avec Relation Inverse :**
```java
public class Departement {
    private String nomDep;
    
    // Relation Many-to-One vers Gerant
    // Référence unique indique qu'un département a UN SEUL gérant
    private Gerant gerant;
    
    public Departement(String nomDep) {
        if (nomDep == null || nomDep.trim().isEmpty()) {
            throw new IllegalArgumentException("Nom département ne peut pas être vide");
        }
        this.nomDep = nomDep;
    }
    
    public String getNomDep() { 
        return nomDep; 
    }
    
    public void setNomDep(String nomDep) { 
        this.nomDep = nomDep; 
    }
    
    public Gerant getGerant() { 
        return gerant; 
    }
    
    /**
     * Définit le gérant ET maintient la cohérence bidirectionnelle
     * C'est la méthode clé pour gérer la relation
     */
    public void setGerant(Gerant nouveauGerant) {
        // Si un gérant existe déjà, retirer ce département de son ancienne liste
        if (this.gerant != null && this.gerant != nouveauGerant) {
            Gerant ancienGerant = this.gerant;
            this.gerant = null;  // Éviter récursion infinie
            ancienGerant.retirerDepartement(this);
        }
        
        // Établir la nouvelle relation
        this.gerant = nouveauGerant;
        
        // Ajouter ce département à la liste du nouveau gérant
        if (nouveauGerant != null && !nouveauGerant.getDepartements().contains(this)) {
            nouveauGerant.ajouterDepartement(this);
        }
    }
    
    @Override
    public String toString() {
        String gerantNom = (gerant != null) ? gerant.getNom() : "Aucun";
        return String.format("Département: %s (Gérant: %s)", nomDep, gerantNom);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Departement autre = (Departement) obj;
        return nomDep.equals(autre.nomDep);
    }
    
    @Override
    public int hashCode() {
        return nomDep.hashCode();
    }
}
```

#### 2.1.4 Création des Objets - Test Complet

```java
public class TestRelations {
    public static void main(String[] args) {
        System.out.println("=== CRÉATION DES ASSISTANTS ===");
        
        // 2 Assistants
        Assistant asst1 = new Assistant("Dupont", "Marie", 3000.0, 2020);
        Assistant asst2 = new Assistant("Martin", "Pierre", 3200.0, 2021);
        
        System.out.println(asst1);
        System.out.println(asst2);
        System.out.println("Ancienneté Marie: " + asst1.getAnciennete() + " ans");
        
        System.out.println("\n=== CRÉATION DES GÉRANTS ===");
        
        // 2 Gérants
        Gerant ger1 = new Gerant("Bernard", "Jean", 8000.0, "Informatique");
        Gerant ger2 = new Gerant("Durand", "Sophie", 9500.0, "Finance");
        
        System.out.println(ger1);
        System.out.println(ger2);
        
        System.out.println("\n=== CRÉATION DES DÉPARTEMENTS ===");
        
        // 2 Départements
        Departement dept1 = new Departement("IT");
        Departement dept2 = new Departement("Comptabilité");
        Departement dept3 = new Departement("Support IT");
        
        System.out.println(dept1);
        System.out.println(dept2);
        
        System.out.println("\n=== ÉTABLISSEMENT DES RELATIONS ===");
        
        // Méthode 1: Via le département
        dept1.setGerant(ger1);
        System.out.println("Relation établie: " + dept1);
        System.out.println("Gérant " + ger1.getNom() + " gère: " + ger1.getNombreDepartements());
        
        // Méthode 2: Via le gérant
        ger1.ajouterDepartement(dept3);
        System.out.println("Relation établie: " + dept3);
        System.out.println("Gérant " + ger1.getNom() + " gère: " + ger1.getNombreDepartements());
        
        // Test avec le second gérant
        dept2.setGerant(ger2);
        
        System.out.println("\n=== VÉRIFICATION BIDIRECTIONNALITÉ ===");
        
        // Vérifier côté Gérant
        System.out.println("Départements de " + ger1.getNom() + ":");
        for (Departement d : ger1.getDepartements()) {
            System.out.println("  - " + d.getNomDep());
        }
        
        // Vérifier côté Département
        System.out.println("Gérant de " + dept1.getNomDep() + ": " + 
                         dept1.getGerant().getNom());
        
        System.out.println("\n=== TEST CHANGEMENT DE GÉRANT ===");
        
        // Déplacer un département d'un gérant à un autre
        System.out.println("Avant: " + dept1 + " géré par " + ger1.getNom());
        dept1.setGerant(ger2);  // Changement de gérant
        System.out.println("Après: " + dept1 + " géré par " + ger2.getNom());
        
        System.out.println("Départements de " + ger1.getNom() + ": " + 
                         ger1.getNombreDepartements());
        System.out.println("Départements de " + ger2.getNom