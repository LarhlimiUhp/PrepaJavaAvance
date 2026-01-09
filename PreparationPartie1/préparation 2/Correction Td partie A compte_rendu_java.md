# Compte Rendu Détaillé : Révision Développement Java Avancé 2025-2026

**Étudiant :** [Votre Nom]  
**Date :** 09 Janvier 2026  
**Module :** Développement Java Avancé  
**Document source :** RevisionGlobale_JavaAvancee2526.pdf

---

## Table des Matières

1. [Exercice 1 - Programmation Orientée Objet et Collections](#exercice-1)
2. [Exercice 3 - Java Streams API](#exercice-3)
3. [Exercice 4 - Gestion des Exceptions](#exercice-4)
4. [Exercice 5 - ThreadPoolExecutor / ExecutorService](#exercice-5)
5. [Exercice 6 - Synchronisation Multi-threading](#exercice-6)
6. [Exercice 7 - Threads et Bases de Données](#exercice-7)
7. [Exercice 8 - Hibernate ORM](#exercice-8)
8. [Synthèse et Conclusions](#synthese)

---

## <a name="exercice-1"></a>1. Exercice 1 : Programmation Orientée Objet et Collections

### 1.1 Partie 1 : Conception des Classes

#### Objectif
Implémenter une hiérarchie de classes avec des relations one-to-many et one-to-one en respectant les principes de l'orienté objet.

#### Structure Implémentée

**Hiérarchie des classes :**
```
Personnel (classe mère abstraite)
    ├── Assistant
    └── Gerant
```

**Relation entre classes :**
- **Gerant ↔ Departement** : Relation bidirectionnelle one-to-many
  - Un Gérant peut gérer plusieurs Départements (List<Departement>)
  - Un Département est géré par un seul Gérant (référence unique)

#### Points Clés de l'Implémentation

1. **Classe Personnel (Mère)**
   - Attributs : nom, prenom, salaire
   - Constructeur avec tous les attributs (requis par l'énoncé)
   - Getters et Setters standard

2. **Classe Assistant**
   - Hérite de Personnel
   - Attribut additionnel : anneeDebut (int)
   - Appelle super() pour initialiser les attributs hérités

3. **Classe Gerant**
   - Hérite de Personnel
   - Attribut spécifique : specialite (String)
   - Collection : `List<Departement> departements`
   - Méthodes de gestion de la relation :
     - `ajouterDepartement(Departement)` : établit la relation bidirectionnelle
     - `retirerDepartement(Departement)` : rompt la relation

4. **Classe Departement**
   - Attributs : nomDep (String), gerant (Gerant)
   - Méthode `setGerant()` : gère automatiquement la bidirectionnalité
   - Vérifie et met à jour les deux côtés de la relation

#### Création des Objets

Instanciation de 2 objets de chaque type (sauf Personnel qui est la classe mère) :
- 2 Assistants avec années de début différentes
- 2 Gérants avec spécialités différentes
- 2 Départements associés aux gérants

**Résultat :** Les relations sont correctement établies et le type de relation est déductible du code (présence de List vs référence unique).

---

### 1.2 Partie 2 : Manipulation des Collections

#### Question 1 : Ajout d'un Département avec Contraintes

**Contraintes à respecter :**
- Nom de département unique (pas de doublons dans le Set)
- Affectation obligatoire à un gérant

**Implémentation :**
```java
public static boolean ajouterDepartement(
    Set<Departement> lesDeps, 
    Departement nouveauDept, 
    Gerant gerant) {
    
    // Vérification d'unicité
    for (Departement dept : lesDeps) {
        if (dept.getNomDep().equals(nouveauDept.getNomDep())) {
            return false;
        }
    }
    
    // Affectation au gérant
    nouveauDept.setGerant(gerant);
    
    // Ajout au Set
    lesDeps.add(nouveauDept);
    return true;
}
```

**Analyse :**
- Parcours du Set pour vérifier l'unicité
- Utilisation de `setGerant()` qui gère la bidirectionnalité
- Retour booléen pour indiquer le succès/échec

#### Question 2 : Suppression avec Gestion de Relation

**Problématique :** Maintenir la cohérence de la relation bidirectionnelle lors de la suppression.

**Solution implémentée :**
```java
public static boolean supprimerDepartement(
    Set<Departement> lesDeps, 
    Departement deptASupprimer) {
    
    // Traiter la relation avec le gérant
    Gerant gerant = deptASupprimer.getGerant();
    if (gerant != null) {
        gerant.retirerDepartement(deptASupprimer);
        deptASupprimer.setGerant(null);
    }
    
    // Supprimer du Set
    lesDeps.remove(deptASupprimer);
    return true;
}
```

**Points importants :**
- Vérification de l'existence du gérant avant suppression
- Appel à `retirerDepartement()` pour nettoyer la liste du gérant
- Mise à null de la référence pour éviter les liens orphelins
- Respect du principe de cohérence des données

#### Question 3 : Regroupement par Salaire avec Stream API

**Objectif :** Créer une Map associant chaque salaire au nombre d'assistants ayant ce salaire.

**Type de retour :** `Map<Double, Long>`

**Implémentation :**
```java
public static Map<Double, Long> regrouperAssistantsParSalaire(
    List<Personnel> lesPrs) {
    
    return lesPrs.stream()
        .filter(p -> p instanceof Assistant)
        .map(p -> (Assistant) p)
        .collect(Collectors.groupingBy(
            Personnel::getSalaire,
            Collectors.counting()
        ));
}
```

**Étapes du pipeline Stream :**
1. **filter** : Sélectionne uniquement les instances d'Assistant
2. **map** : Cast explicite vers Assistant (pour la clarté du type)
3. **groupingBy** : Groupe par salaire avec comptage automatique

**Exemple de résultat :**
```
{3000.0=2, 3500.0=1, 3200.0=1}
```

#### Question 4 : Filtrage et Regroupement Multiple

**Objectif :** Regrouper les Gérants (salaire < 10000) par spécialité.

**Type de retour :** `Map<String, List<Gerant>>`

**Implémentation :**
```java
public static Map<String, List<Gerant>> regrouperGerantsParSpecialite(
    List<Personnel> lesPrs) {
    
    return lesPrs.stream()
        .filter(p -> p instanceof Gerant)
        .map(p -> (Gerant) p)
        .filter(g -> g.getSalaire() < 10000)
        .collect(Collectors.groupingBy(Gerant::getSpecialite));
}
```

**Analyse du pipeline :**
1. Premier filtre : Type (Gerant)
2. Cast sécurisé
3. Second filtre : Condition métier (salaire)
4. Regroupement par attribut métier (spécialité)

**Avantages de l'approche Stream :**
- Code déclaratif et lisible
- Pas de boucles explicites
- Séparation claire des opérations
- Facilement testable et maintenable

---

## <a name="exercice-3"></a>2. Exercice 3 : Java Streams API

### 2.1 Contexte

**Classe Produit :**
- Attributs : id (String), nom, prix (double), categorie, anneFabric (int)
- Catégories : Electronique, Meuble, Librairie
- ID : Séquence de chiffres

### 2.2 Objectifs du Stream Complexe

1. **Réduction de prix** : -15% sur produits Électronique < 2000€
2. **Transformation d'ID** : Format "CAT-ID" sauf pour Librairie et Jardin
3. **Stockage** : Collection accessible par indices (List)

### 2.3 Implémentation

```java
public static List<Produit> traiterProduits(Set<Produit> produits) {
    return produits.stream()
        .peek(p -> {
            if (p.getCategorie().equalsIgnoreCase("Electronique") 
                && p.getPrix() < 2000) {
                p.setPrix(p.getPrix() * 0.85);
            }
        })
        .peek(p -> {
            String cat = p.getCategorie();
            if (!cat.equalsIgnoreCase("Librairie") 
                && !cat.equalsIgnoreCase("Jardin")) {
                String prefix = cat.substring(0, 3).toUpperCase();
                p.setId(prefix + "-" + p.getId());
            }
        })
        .collect(Collectors.toList());
}
```

### 2.4 Analyse Technique

**Utilisation de peek() :**
- Permet les effets de bord (modifications d'objets)
- Adapté pour les transformations en place
- Alternative à `map()` quand on veut modifier l'objet existant

**Logique conditionnelle :**
- Conditions multiples avec opérateurs logiques
- Vérifications insensibles à la casse (equalsIgnoreCase)
- Extraction de préfixe avec substring

**Transformation d'ID :**
- Concaténation de strings
- Format standardisé : "XXX-number"
- Préservation des IDs pour catégories exclues

**Exemples de transformation :**
| Avant | Après |
|-------|-------|
| id="12", Electronique, 1500€ | id="ELE-12", prix=1275€ |
| id="45", Electronique, 2500€ | id="ELE-45", prix=2500€ |
| id="23", Meuble, 300€ | id="MEU-23", prix=300€ |
| id="89", Librairie, 50€ | id="89", prix=50€ |

**Collecteur final :**
- `toList()` : crée une ArrayList
- Accès par index possible : `list.get(0)`
- Ordre préservé (important pour certains cas d'usage)

### 2.5 Avantages de cette Approche

✅ **Un seul pipeline** : Toutes les opérations enchaînées  
✅ **Performance** : Traversée unique de la collection  
✅ **Lisibilité** : Séparation claire des responsabilités  
✅ **Maintenance** : Facile d'ajouter/modifier des étapes  

---

## <a name="exercice-4"></a>3. Exercice 4 : Gestion des Exceptions

### 3.1 Contexte : NullPointerException

**Trois cas de NullPointerException :**

1. **Mauvaise utilisation de référence**
   ```java
   String str = null;
   str.length(); // NPE
   ```

2. **Objet déclaré mais null**
   ```java
   Etudiant etud = null;
   etud.moy; // NPE
   ```

3. **Tableau créé mais éléments non initialisés**
   ```java
   Etudiant[] tab = new Etudiant[5];
   tab[0].moy; // NPE (tab[0] est null)
   ```

### 3.2 Code Problématique

```java
Etudiant[] students = new Etudiant[5];
students[0] = new Etudiant("Alice", 15.5);
students[1] = new Etudiant("Bob", 14.0);
// students[2], [3], [4] sont null

Etudiant best = students[0];
for(Etudiant e : students) {
    if(e.moy > best.moy) { // NPE ici si e est null
        best = e;
    }
}
```

**Problème :** Accès à `e.moy` sur un élément null du tableau.

### 3.3 Solution avec try-catch

```java
try {
    Etudiant best = students[0];
    
    for(Etudiant e : students) {
        if(e.moy > best.moy) {
            best = e;
        }
    }
    
    System.out.println(" " + best.moy);
    
} catch (NullPointerException ex) {
    System.out.println("Erreur Null pointer trouvée");
}
```

### 3.4 Approche Alternative : Prévention

```java
Etudiant best = students[0];

for(Etudiant e : students) {
    if(e != null && e.moy > best.moy) { // Vérification préventive
        best = e;
    }
}
```

### 3.5 Comparaison des Approches

| Approche | Avantages | Inconvénients |
|----------|-----------|---------------|
| **try-catch** | Capture toute NPE, gestion centralisée | Performance (si exceptions fréquentes) |
| **Vérification if** | Performance optimale, code défensif | Code plus verbeux, à répéter |
| **Optional** | API moderne, expressif | Overhead, courbe d'apprentissage |

### 3.6 Bonnes Pratiques

✅ Préférer la prévention (if) aux exceptions pour les cas prévisibles  
✅ Utiliser try-catch pour les cas vraiment exceptionnels  
✅ Toujours fournir un message d'erreur explicite  
✅ Logger les exceptions pour le débogage  
✅ Considérer Optional<T> pour les retours pouvant être null  

---

## <a name="exercice-5"></a>4. Exercice 5 : ThreadPoolExecutor / ExecutorService

### 4.1 Concepts Fondamentaux

#### Question 1 : Critères d'Adoption

**Quand utiliser ThreadPoolExecutor/ExecutorService ?**

✅ **Nombre élevé de tâches** : Centaines/milliers de tâches à exécuter  
✅ **Tâches courtes et fréquentes** : Optimisation création/destruction  
✅ **Contrôle de concurrence** : Limiter le nombre de threads actifs  
✅ **Gestion de ressources** : CPU, mémoire, connexions limitées  
✅ **Tâches indépendantes** : Pas de dépendances complexes entre tâches  

**Contre-indications :**
- Peu de tâches longues : overhead inutile
- Tâches hautement interdépendantes : risque de deadlock
- Ordre d'exécution strict requis

### 4.2 Déclaration et Configuration

#### Question 2 : Pool de 7 Threads

**Méthode 1 - ExecutorService (simple) :**
```java
ExecutorService executor = Executors.newFixedThreadPool(7);
```

**Méthode 2 - ThreadPoolExecutor (contrôle fin) :**
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    7,                              // corePoolSize
    7,                              // maximumPoolSize
    0L,                             // keepAliveTime
    TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue<Runnable>()
);
```

**Paramètres expliqués :**
- **corePoolSize** : Nombre minimum de threads maintenus
- **maximumPoolSize** : Nombre maximum de threads
- **keepAliveTime** : Durée de vie des threads excédentaires
- **Queue** : File d'attente pour les tâches en surplus

### 4.3 Architecture et Composants

#### Question 3 : Classe héritant de Thread

**Rôle :** Représente une **tâche (Task)** à exécuter.

```java
class MonTask extends Thread {
    public void run() {
        // Code de la tâche
    }
}
```

**Utilisation :**
```java
executor.submit(new MonTask());
```

#### Question 4 : Méthode run()

**Définition :** Point d'entrée de l'exécution du thread.

**Caractéristiques :**
- Contient le code métier de la tâche
- Appelée automatiquement par le framework
- Ne pas appeler directement (utiliser start() ou submit())
- Retour void (utiliser Callable<T> pour retourner une valeur)

### 4.4 Soumission et Suivi des Tâches

#### Question 5 : Méthode submit()

**Signature :**
```java
Future<?> submit(Runnable task)
<T> Future<T> submit(Callable<T> task)
```

**Fonctionnalités :**
- Soumet une tâche pour exécution asynchrone
- Retourne un objet Future pour suivi
- Permet récupération du résultat
- Gestion des exceptions

**Exemple :**
```java
Future<Integer> future = executor.submit(() -> {
    return 42;
});

Integer resultat = future.get(); // Bloquant
```

### 4.5 File d'Attente

#### Question 6 : Queue

**Définition :** Structure de données stockant les tâches en attente.

**Types de queues :**
- **LinkedBlockingQueue** : Capacité illimitée (par défaut)
- **ArrayBlockingQueue** : Capacité fixe
- **SynchronousQueue** : Pas de stockage (transfert direct)
- **PriorityBlockingQueue** : Ordre de priorité

**Fonctionnement :**
1. Tâche soumise → Pool disponible ? → Exécution immédiate
2. Pool plein ? → Ajout à la queue
3. Thread se libère → Prend tâche de la queue

### 4.6 Problèmes Potentiels

#### Question 7 : Problèmes d'Exécution

**1. Deadlock (Interblocage)**
- Threads s'attendent mutuellement
- Blocage définitif du système

**2. Race Conditions**
- Accès concurrent à ressources partagées
- Résultats imprévisibles

**3. Starvation**
- Certaines tâches jamais exécutées
- Problème de priorité ou fairness

**4. Thread Pool Exhaustion**
- Queue qui grossit indéfiniment
- OutOfMemoryError

**5. Exception Handling**
- Exceptions non capturées tuent le thread
- Réduction progressive du pool

### 4.7 Collection de Futures

#### Question 8 : Collection<Future<?>>

**Utilité :**
- Stocker tous les Future retournés par submit()
- Attendre la complétion de toutes les tâches
- Récupérer les résultats
- Annuler des tâches en masse

**Exemple pratique :**
```java
List<Future<Integer>> futures = new ArrayList<>();

// Soumission
for (int i = 0; i < 100; i++) {
    futures.add(executor.submit(new MaTask(i)));
}

// Attente de toutes les tâches
for (Future<Integer> f : futures) {
    try {
        Integer resultat = f.get(); // Bloquant
        System.out.println("Résultat: " + resultat);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

**Méthodes importantes :**
- `get()` : Récupère résultat (bloquant)
- `get(timeout, unit)` : Avec timeout
- `cancel(boolean)` : Annule la tâche
- `isDone()` : Vérifie si terminé
- `isCancelled()` : Vérifie si annulé

---

## <a name="exercice-6"></a>5. Exercice 6 : Synchronisation Multi-threading

### 5.1 Importance de la Synchronisation

#### Question 1 : Pourquoi Synchroniser ?

**Problématique :** En environnement multithreading, plusieurs threads accèdent simultanément aux mêmes ressources partagées.

**Conséquences sans synchronisation :**
- **Incohérence des données** : Valeurs corrompues
- **Race conditions** : Résultats dépendent du timing
- **Perte de mises à jour** : Écrasement de modifications
- **État inconsistant** : Violation des invariants

**Exemple classique - Compte bancaire :**
```java
// Sans synchronisation - PROBLÈME
class CompteBancaire {
    private int solde = 1000;
    
    public void retirer(int montant) {
        if (solde >= montant) {          // Thread 1 lit 1000
            // Thread 2 lit aussi 1000
            solde = solde - montant;     // Les deux retirent 600
            // Solde final: 400 au lieu de -200 (erreur!)
        }
    }
}
```

### 5.2 Cas d'Usage de la Synchronisation

#### Question 2 : Quand Synchroniser ?

**Scénarios nécessitant synchronisation :**

1. **Modification de ressources partagées**
   - Variables communes entre threads
   - Collections non thread-safe

2. **Opérations non-atomiques**
   - Lecture-modification-écriture
   - Check-then-act
   - Séquences multi-étapes

3. **Sections critiques**
   - Code manipulant l'état partagé
   - Mise à jour de structures de données

4. **Garantie de visibilité**
   - Assurer que les modifications sont visibles
   - Problème du cache CPU

**Exemple nécessitant synchronisation :**
```java
// Compteur partagé
private int compteur = 0;

// Sans sync: race condition
public void incrementer() {
    compteur++; // Pas atomique! (lecture + incrémentation + écriture)
}
```

### 5.3 Lock : Contrôle Fin

#### Question 3 : Utilité et Usage de Lock

**Avantages sur synchronized :**
- Tentative d'acquisition non-bloquante (`tryLock()`)
- Timeout possible
- Interruptibilité
- Équité configurable (fairness)
- Conditions multiples (Condition)

**Implémentation standard :**
```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class MaClasse {
    private final Lock lock = new ReentrantLock();
    private int ressource = 0;
    
    public void modifier() {
        lock.lock();
        try {
            // Section critique
            ressource++;
        } finally {
            lock.unlock(); // TOUJOURS dans finally
        }
    }
    
    // Avec tryLock
    public boolean modifierSiDisponible() {
        if (lock.tryLock()) {
            try {
                ressource++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }
}
```

**Patterns avancés :**
```java
// Avec timeout
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try {
        // Traitement
    } finally {
        lock.unlock();
    }
} else {
    // Timeout expiré
}
```

### 5.4 Synchronized : Simplicité

#### Question 4 : Utilité de synchronized

**Deux formes :**

**1. Méthode synchronisée :**
```java
public synchronized void methode() {
    // Tout le corps est protégé
    // Verrou = this
}
```

**2. Bloc synchronisé :**
```java
public void methode() {
    // Code non protégé
    
    synchronized(this) {
        // Section critique
    }
    
    // Code non protégé
}

// Synchronisation sur objet spécifique
private final Object lock = new Object();
synchronized(lock) {
    // Section critique
}
```

**Équivalence :**
```java
// Ces deux sont équivalents
public synchronized void methode() { }

public void methode() {
    synchronized(this) { }
}
```

**Avantages :**
- Syntaxe simple et claire
- Libération automatique du verrou
- Intégré au langage

### 5.5 Détection des Problèmes

#### Question 5 : Comment Détecter un Manque de Synchronisation ?

**Symptômes :**
- ✗ Résultats différents à chaque exécution
- ✗ Valeurs incohérentes ou corrompues
- ✗ Exceptions inattendues (ConcurrentModificationException)
- ✗ Deadlocks ou blocages

**Méthodes de détection :**

**1. Tests répétés :**
```java
for (int i = 0; i < 1000; i++) {
    testConcurrent();
    verifierResultat();
}
```

**2. Augmenter le nombre de threads :**
```java
ExecutorService executor = Executors.newFixedThreadPool(100);
// Plus de threads = plus de chances de race condition
```

**3. Outils de détection :**
- **Thread Sanitizer** (TSan)
- **FindBugs** / **SpotBugs**
- **Java Pathfinder** (JPF)
- **IntelliJ IDEA** : Inspections concurrency

**4. Assertions et invariants :**
```java
private int invariant() {
    assert compteur >= 0 : "Compteur négatif!";
    assert list.size() == compteur : "Taille incohérente!";
}
```

### 5.6 Principes de Synchronisation Efficace

#### Question 6 : Points Clés pour une Bonne Synchronisation

**1. Identifier les ressources partagées**
```java
// Partagé = doit être synchronisé
private List<String> listePartagee = new ArrayList<>();

// Non partagé = pas besoin
public void methode() {
    List<String> listeLocale = new ArrayList<>();
}
```

**2. Minimiser la section critique**
```java
// ✗ Mauvais: section critique trop large
public synchronized void traiter() {
    faireCalculComplexe();    // Pas besoin de sync
    modifierRessource();      // Besoin de sync
    fairePlusDeCalculs();     // Pas besoin de sync
}

// ✓ Bon: section critique minimale
public void traiter() {
    faireCalculComplexe();
    synchronized(this) {
        modifierRessource();  // Seulement ici
    }
    fairePlusDeCalculs();
}
```

**3. Ordre d'acquisition des locks (éviter deadlock)**
```java
// ✓ Bon: ordre constant
public void transfert(Compte source, Compte dest) {
    // Toujours verrouiller dans le même ordre
    Compte premier = source.id < dest.id ? source : dest;
    Compte second = source.id < dest.id ? dest : source;
    
    synchronized(premier) {
        synchronized(second) {
            // Transfert sécurisé
        }
    }
}
```

**4. Utiliser les bonnes structures**
```java
// ✗ Mauvais: ArrayList non thread-safe
List<String> list = new ArrayList<>();

// ✓ Bon: CopyOnWriteArrayList
List<String> list = new CopyOnWriteArrayList<>();

// ✓ Bon: Collections synchronisées
List<String> list = Collections.synchronizedList(new ArrayList<>());
```

**5. Toujours libérer les locks**
```java
lock.lock();
try {
    // Section critique
} finally {
    lock.unlock(); // Même en cas d'exception
}
```

**6. Documenter les stratégies**
```java
/**
 * Thread-safe. Utilise synchronized sur l'instance.
 * Ne pas appeler depuis un autre bloc synchronisé.
 */
public synchronized void methode() { }
```

### 5.7 Section Critique

#### Question 7 : Définition

**Section critique :** Portion de code qui accède à une ressource partagée et qui doit être exécutée en exclusion mutuelle.

**Caractéristiques :**
- Accès à variables/objets partagés
- Non-atomique sans protection
- Doit être protégée par synchronisation
- Doit être la plus courte possible

**Exemple :**
```java
class Compteur {
    private int valeur = 0; // Ressource partagée
    
    public void incrementer() {
        // Début section critique
        int temp = valeur;
        temp = temp + 1;
        valeur = temp;
        // Fin section critique
    }
}
```

### 5.8 Méthodes start() et join()

#### Question 8 : start() et join()

**start() :**
- Démarre l'exécution d'un nouveau thread
- Appelle automatiquement run() dans le nouveau thread
- Non-bloquant pour le thread appelant
- Ne peut être appelé qu'une seule fois

```java
Thread t = new Thread(() -> {
    System.out.println("Dans le thread");
});
t.start(); // Lance le thread
System.out.println("Après start"); // S'exécute immédiatement
```

**join() :**
- Attend la fin de l'exécution d'un thread
- Bloquant pour le thread appelant
- Peut spécifier un timeout

```java
Thread t1 = new Thread(task1);
Thread t2 = new Thread(task2);

t1.start();
t1.join(); // Attend que t1 finisse

t2.start(); // Ne démarre qu'après t1
t2.join();  // Attend que t2 finisse

System.out.println("Tous les threads terminés");
```

**Utilisation typique - Ordre d'exécution :**
```java
// t2 doit s'exécuter après t1
t1.start();