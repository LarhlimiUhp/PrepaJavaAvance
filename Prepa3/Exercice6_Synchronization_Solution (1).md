# Exercice 6 : Multi-threading Synchronization - Solution Complète

## Introduction

Cet exercice couvre les concepts fondamentaux de la synchronisation dans un environnement multithreading en Java.

---

## Question 1 : Importance de la synchronisation en multithreading

### Réponse

La synchronisation est **cruciale** dans un environnement multithreading pour garantir la **cohérence et l'intégrité des données** partagées entre plusieurs threads.

### Importance de la Synchronisation

#### 1. **Garantir la Cohérence des Données**

Sans synchronisation, plusieurs threads peuvent modifier simultanément une même variable, causant des résultats imprévisibles.

**Exemple sans synchronisation** :
```java
public class SansSynchronisation {
    private static int compteur = 0;
    
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                compteur++; // Opération non atomique
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                compteur++; // Opération non atomique
            }
        });
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        System.out.println("Attendu : 2000");
        System.out.println("Réel : " + compteur); // Souvent < 2000 !
    }
}
```

#### 2. **Éviter les Race Conditions**

La synchronisation empêche que deux threads accèdent simultanément à la même ressource critique.

#### 3. **Garantir la Visibilité**

Sans synchronisation, les modifications faites par un thread peuvent ne pas être visibles par d'autres threads (problème de cache CPU).

#### 4. **Maintenir l'Ordre d'Exécution**

La synchronisation garantit que certaines opérations se font dans un ordre spécifique.

#### 5. **Protéger les Sections Critiques**

Les sections de code qui modifient des ressources partagées doivent être protégées.

### Conséquences de l'Absence de Synchronisation

| Problème | Description | Impact |
|----------|-------------|--------|
| **Race Condition** | Résultat dépend de l'ordre d'exécution | Résultats incorrects |
| **Données corrompues** | Modifications simultanées | Incohérence des données |
| **Perte de mises à jour** | Écrasement de valeurs | Calculs erronés |
| **Lecture de valeurs obsolètes** | Cache CPU non synchronisé | Logique incorrecte |

### Exemple Concret : Compte Bancaire

```java
public class CompteBancaire {
    private double solde = 1000.0;
    
    // ❌ SANS synchronisation - DANGEREUX
    public void deposer(double montant) {
        double nouveauSolde = solde + montant;
        // Si un autre thread modifie solde ici, problème !
        solde = nouveauSolde;
    }
    
    // ✅ AVEC synchronisation - SÛR
    public synchronized void deposerSecurise(double montant) {
        solde += montant;
    }
}
```

### Résumé

La synchronisation est importante pour :
- ✅ **Cohérence** : Données toujours valides
- ✅ **Prévisibilité** : Résultats déterministes
- ✅ **Fiabilité** : Pas de corruption de données
- ✅ **Visibilité** : Changements visibles par tous les threads
- ✅ **Sécurité** : Protection contre les accès concurrents

---

## Question 2 : Problème que la synchronisation peut résoudre

### Réponse

La synchronisation résout le problème d'**accès concurrent à des ressources partagées** (shared resources). Elle est nécessaire dans les cas suivants :

### Cas d'Utilisation de la Synchronisation

#### 1. **Modification de Variables Partagées**

Quand plusieurs threads modifient la même variable.

**Exemple** :
```java
public class CompteurPartage {
    private int compteur = 0;
    
    // Problème : plusieurs threads appellent cette méthode
    public synchronized void incrementer() {
        compteur++; // Section critique
    }
    
    public synchronized int getCompteur() {
        return compteur;
    }
}
```

#### 2. **Opérations Non-Atomiques**

Les opérations qui nécessitent plusieurs étapes doivent être synchronisées.

**Exemple** :
```java
public class OperationNonAtomique {
    private int x = 0;
    private int y = 0;
    
    // ❌ PROBLÈME : Opération en plusieurs étapes
    public void update(int valeur) {
        x = valeur;      // Étape 1
        // Si interruption ici, incohérence !
        y = valeur;      // Étape 2
    }
    
    // ✅ SOLUTION : Synchroniser
    public synchronized void updateSecurise(int valeur) {
        x = valeur;
        y = valeur;
    }
}
```

#### 3. **Structures de Données Non Thread-Safe**

Collections comme ArrayList, HashMap ne sont pas thread-safe.

**Exemple** :
```java
public class GestionListe {
    private List<String> liste = new ArrayList<>();
    
    // ✅ Synchroniser l'accès
    public synchronized void ajouter(String element) {
        liste.add(element);
    }
    
    public synchronized String obtenir(int index) {
        return liste.get(index);
    }
    
    // Ou utiliser Collections.synchronizedList()
    // List<String> listeSynchronisee = Collections.synchronizedList(new ArrayList<>());
}
```

#### 4. **Pattern Producer-Consumer**

Quand un thread produit des données et un autre les consomme.

**Exemple** :
```java
public class Buffer {
    private Queue<Integer> queue = new LinkedList<>();
    private final int CAPACITY = 10;
    
    public synchronized void produire(int valeur) throws InterruptedException {
        while (queue.size() == CAPACITY) {
            wait(); // Attendre si le buffer est plein
        }
        queue.add(valeur);
        notifyAll(); // Notifier les consommateurs
    }
    
    public synchronized int consommer() throws InterruptedException {
        while (queue.isEmpty()) {
            wait(); // Attendre si le buffer est vide
        }
        int valeur = queue.poll();
        notifyAll(); // Notifier les producteurs
        return valeur;
    }
}
```

#### 5. **Lecture-Écriture sur Fichiers**

Accès concurrent à un fichier ou une base de données.

**Exemple** :
```java
public class GestionFichier {
    private final Object lock = new Object();
    
    public void ecrire(String contenu) {
        synchronized (lock) {
            // Écrire dans le fichier
            // Aucun autre thread ne peut lire ou écrire pendant ce temps
        }
    }
    
    public String lire() {
        synchronized (lock) {
            // Lire le fichier
            return "contenu";
        }
    }
}
```

#### 6. **Singleton Thread-Safe**

Création d'une instance unique accessible par plusieurs threads.

**Exemple** :
```java
public class Singleton {
    private static Singleton instance;
    
    // ✅ Synchroniser la création
    public static synchronized Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
    
    // Ou utiliser Double-Checked Locking
    public static Singleton getInstanceOptimise() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

#### 7. **Transactions Bancaires**

Transferts entre comptes nécessitent une synchronisation.

**Exemple** :
```java
public class Compte {
    private double solde;
    
    public Compte(double soldeInitial) {
        this.solde = soldeInitial;
    }
    
    public synchronized double getSolde() {
        return solde;
    }
    
    // Transfert entre comptes
    public static void transferer(Compte source, Compte destination, double montant) {
        // Synchroniser sur les deux comptes (attention au deadlock !)
        synchronized (source) {
            synchronized (destination) {
                if (source.solde >= montant) {
                    source.solde -= montant;
                    destination.solde += montant;
                    System.out.println("Transfert réussi : " + montant);
                }
            }
        }
    }
}
```

### Règle Générale

**Utilisez la synchronisation quand** :
- ✅ Plusieurs threads accèdent à la même variable
- ✅ Au moins un thread **modifie** la variable
- ✅ Les opérations ne sont pas atomiques
- ✅ Vous utilisez des structures de données non thread-safe
- ✅ Vous devez garantir l'ordre d'exécution

**Ne synchronisez PAS quand** :
- ❌ Les variables sont en lecture seule (immutable)
- ❌ Les variables sont locales aux threads (ThreadLocal)
- ❌ Vous utilisez des classes thread-safe (AtomicInteger, ConcurrentHashMap)

---

## Question 3 : Utilité de Lock et comment l'utiliser

### Réponse

**Lock** est une interface Java qui offre un mécanisme de verrouillage plus flexible et puissant que `synchronized`.

### Avantages de Lock par rapport à synchronized

| Caractéristique | synchronized | Lock |
|----------------|--------------|------|
| **Flexibilité** | Limitée | Élevée |
| **Timeout** | Non | Oui (tryLock) |
| **Interruptibilité** | Non | Oui (lockInterruptibly) |
| **Équité** | Non garanti | Configurable |
| **Conditions multiples** | wait/notify uniquement | Conditions multiples |
| **Lisibilité** | Automatique | Manuel (finally) |

### Types de Lock

#### 1. **ReentrantLock** (Le plus utilisé)

```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExempleLock {
    private final Lock lock = new ReentrantLock();
    private int compteur = 0;
    
    public void incrementer() {
        lock.lock(); // Acquérir le verrou
        try {
            compteur++; // Section critique
        } finally {
            lock.unlock(); // Toujours libérer dans finally
        }
    }
    
    public int getCompteur() {
        lock.lock();
        try {
            return compteur;
        } finally {
            lock.unlock();
        }
    }
}
```

#### 2. **ReadWriteLock** (Lecture/Écriture)

```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheAvecRWLock {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Map<String, String> cache = new HashMap<>();
    
    // Plusieurs threads peuvent lire simultanément
    public String lire(String cle) {
        rwLock.readLock().lock();
        try {
            return cache.get(cle);
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    // Un seul thread peut écrire à la fois
    public void ecrire(String cle, String valeur) {
        rwLock.writeLock().lock();
        try {
            cache.put(cle, valeur);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

### Méthodes Principales de Lock

#### 1. **lock()** - Verrouillage simple

```java
Lock lock = new ReentrantLock();

lock.lock();
try {
    // Section critique
} finally {
    lock.unlock();
}
```

#### 2. **tryLock()** - Tentative non-bloquante

```java
Lock lock = new ReentrantLock();

if (lock.tryLock()) {
    try {
        // Section critique
        System.out.println("Verrou acquis");
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("Verrou non disponible, abandon");
}
```

#### 3. **tryLock(timeout)** - Tentative avec timeout

```java
Lock lock = new ReentrantLock();

try {
    if (lock.tryLock(5, TimeUnit.SECONDS)) {
        try {
            // Section critique
            System.out.println("Verrou acquis");
        } finally {
            lock.unlock();
        }
    } else {
        System.out.println("Timeout : verrou non obtenu");
    }
} catch (InterruptedException e) {
    System.out.println("Interrompu en attendant le verrou");
}
```

#### 4. **lockInterruptibly()** - Verrouillage interruptible

```java
Lock lock = new ReentrantLock();

try {
    lock.lockInterruptibly(); // Peut être interrompu
    try {
        // Section critique
    } finally {
        lock.unlock();
    }
} catch (InterruptedException e) {
    System.out.println("Thread interrompu pendant l'attente du verrou");
}
```

### Exemple Complet : Compte Bancaire avec Lock

```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CompteBancaireAvecLock {
    private double solde;
    private final Lock lock = new ReentrantLock();
    
    public CompteBancaireAvecLock(double soldeInitial) {
        this.solde = soldeInitial;
    }
    
    public void deposer(double montant) {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + 
                             " - Dépôt de " + montant);
            solde += montant;
            System.out.println("Nouveau solde : " + solde);
        } finally {
            lock.unlock();
        }
    }
    
    public boolean retirer(double montant) {
        lock.lock();
        try {
            if (solde >= montant) {
                System.out.println(Thread.currentThread().getName() + 
                                 " - Retrait de " + montant);
                solde -= montant;
                System.out.println("Nouveau solde : " + solde);
                return true;
            } else {
                System.out.println("Solde insuffisant");
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
    
    public double getSolde() {
        lock.lock();
        try {
            return solde;
        } finally {
            lock.unlock();
        }
    }
}
```

### Conditions avec Lock

```java
import java.util.concurrent.locks.*;

public class BufferAvecCondition {
    private final Lock lock = new ReentrantLock();
    private final Condition nonVide = lock.newCondition();
    private final Condition nonPlein = lock.newCondition();
    
    private final int[] buffer = new int[10];
    private int count = 0;
    
    public void produire(int valeur) throws InterruptedException {
        lock.lock();
        try {
            while (count == buffer.length) {
                nonPlein.await(); // Attendre que le buffer ne soit pas plein
            }
            buffer[count++] = valeur;
            System.out.println("Produit : " + valeur);
            nonVide.signal(); // Notifier qu'un élément est disponible
        } finally {
            lock.unlock();
        }
    }
    
    public int consommer() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                nonVide.await(); // Attendre qu'un élément soit disponible
            }
            int valeur = buffer[--count];
            System.out.println("Consommé : " + valeur);
            nonPlein.signal(); // Notifier qu'il y a de la place
            return valeur;
        } finally {
            lock.unlock();
        }
    }
}
```

### Lock Équitable (Fair Lock)

```java
// Fair Lock : Les threads obtiennent le verrou dans l'ordre FIFO
Lock fairLock = new ReentrantLock(true);

public void methodeFair() {
    fairLock.lock();
    try {
        // Section critique
        // Les threads attendent leur tour
    } finally {
        fairLock.unlock();
    }
}
```

### Bonnes Pratiques avec Lock

```java
public class BonnesPratiquesLock {
    private final Lock lock = new ReentrantLock();
    
    // ✅ BON : Toujours unlock() dans finally
    public void bonneMethode() {
        lock.lock();
        try {
            // Section critique
        } finally {
            lock.unlock(); // Garantit la libération même en cas d'exception
        }
    }
    
    // ❌ MAUVAIS : unlock() pas dans finally
    public void mauvaiseMethode() {
        lock.lock();
        // Section critique
        lock.unlock(); // Si exception, le verrou reste acquis !
    }
    
    // ✅ BON : Utiliser tryLock pour éviter les deadlocks
    public void methodeSafe(Lock autreLock) {
        if (lock.tryLock()) {
            try {
                if (autreLock.tryLock()) {
                    try {
                        // Section critique avec deux verrous
                    } finally {
                        autreLock.unlock();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
```

---

## Question 4 : Utilité de « synchronized »

### Réponse

Le mot-clé `synchronized` est utilisé pour **contrôler l'accès concurrent** à une méthode ou un bloc de code, garantissant qu'un seul thread à la fois peut exécuter cette section.

### Deux Formes de synchronized

#### 1. **Méthode Synchronisée**

```java
public class CompteurSynchronise {
    private int compteur = 0;
    
    // Synchronise sur l'instance courante (this)
    public synchronized void incrementer() {
        compteur++;
    }
    
    public synchronized int getCompteur() {
        return compteur;
    }
}
```

**Équivalent à** :
```java
public void incrementer() {
    synchronized(this) {
        compteur++;
    }
}
```

#### 2. **Bloc Synchronisé**

```java
public class BlocSynchronise {
    private int compteur = 0;
    private final Object lock = new Object();
    
    public void incrementer() {
        // Code non synchronisé
        System.out.println("Avant section critique");
        
        // Section critique
        synchronized(lock) {
            compteur++;
        }
        
        // Code non synchronisé
        System.out.println("Après section critique");
    }
}
```

### Méthode Statique Synchronisée

```java
public class CompteurStatique {
    private static int compteur = 0;
    
    // Synchronise sur la classe (CompteurStatique.class)
    public static synchronized void incrementer() {
        compteur++;
    }
}
```

**Équivalent à** :
```java
public static void incrementer() {
    synchronized(CompteurStatique.class) {
        compteur++;
    }
}
```

### Utilités de synchronized

#### 1. **Protection des Sections Critiques**

```java
public class BanqueSimple {
    private double solde = 1000.0;
    
    // Section critique : doit être atomique
    public synchronized void transferer(BanqueSimple destination, double montant) {
        if (this.solde >= montant) {
            this.solde -= montant;
            // Sans synchronized, un autre thread pourrait interrompre ici
            destination.deposer(montant);
        }
    }
    
    public synchronized void deposer(double montant) {
        this.solde += montant;
    }
}
```

#### 2. **Garantir la Visibilité**

```java
public class Drapeau {
    private boolean arret = false;
    
    // Sans synchronized, les changements peuvent ne pas être visibles
    public synchronized void arreter() {
        arret = true;
    }
    
    public synchronized boolean estArrete() {
        return arret;
    }
}
```

#### 3. **Coordination entre Threads**

```java
public class ProducerConsumer {
    private List<Integer> buffer = new ArrayList<>();
    private final int CAPACITY = 5;
    
    public synchronized void produire(int valeur) throws InterruptedException {
        while (buffer.size() == CAPACITY) {
            wait(); // Libère le verrou et attend
        }
        buffer.add(valeur);
        System.out.println("Produit : " + valeur);
        notifyAll(); // Réveille les threads en attente
    }
    
    public synchronized int consommer() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait();
        }
        int valeur = buffer.remove(0);
        System.out.println("Consommé : " + valeur);
        notifyAll();
        return valeur;
    }
}
```

### Sur Quel Objet Synchroniser ?

```java
public class ChoixObjetVerrou {
    private int donneeA = 0;
    private int donneeB = 0;
    
    private final Object lockA = new Object();
    private final Object lockB = new Object();
    
    // Verrous séparés : meilleure concurrence
    public void modifierA() {
        synchronized(lockA) {
            donneeA++;
        }
    }
    
    public void modifierB() {
        synchronized(lockB) {
            donneeB++;
        }
    }
    
    // Verrou unique : plus simple mais moins concurrent
    public synchronized void modifierAB() {
        donneeA++;
        donneeB++;
    }
}
```

### Granularité de la Synchronisation

```java
public class GranulariteSynchronisation {
    private List<String> liste = new ArrayList<>();
    
    // ❌ MAUVAIS : Synchronise trop (toute la méthode)
    public synchronized void ajouterMauvais(String element) {
        System.out.println("Début traitement");
        // Traitement long non critique
        dormir(1000);
        
        liste.add(element); // Seule cette ligne nécessite la synchronisation
        System.out.println("Fin traitement");
    }
    
    // ✅ BON : Synchronise uniquement la section critique
    public void ajouterBon(String element) {
        System.out.println("Début traitement");
        dormir(1000);
        
        synchronized(this) {
            liste.add(element); // Section critique minimale
        }
        System.out.println("Fin traitement");
    }
    
    private void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

### Exemple Complet : Gestion de Stock

```java
public class GestionStock {
    private Map<String, Integer> stock = new HashMap<>();
    
    public synchronized void ajouterProduit(String produit, int quantite) {
        stock.put(produit, stock.getOrDefault(produit, 0) + quantite);
        System.out.println("Ajouté " + quantite + " " + produit + 
                         " (Total: " + stock.get(produit) + ")");
    }
    
    public synchronized boolean retirerProduit(String produit, int quantite) {
        int quantiteActuelle = stock.getOrDefault(produit, 0);
        
        if (quantiteActuelle >= quantite) {
            stock.put(produit, quantiteActuelle - quantite);
            System.out.println("Retiré " + quantite + " " + produit + 
                             " (Reste: " + stock.get(produit) + ")");
            return true;
        } else {
            System.out.println("Stock insuffisant pour " + produit);
            return false;
        }
    }
    
    public synchronized int getQuantite(String produit) {
        return stock.getOrDefault(produit, 0);
    }
}

// Test
public class TestGestionStock {
    public static void main(String[] args) {
        GestionStock stock = new GestionStock();
        
        // Thread 1 : Ajoute des produits
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                stock.ajouterProduit("Pomme", 10);
                try { Thread.sleep(100); } catch (InterruptedException e) {}
            }
        });
        
        // Thread 2 : Retire des produits
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                stock.retirerProduit("Pomme", 8);
                try { Thread.sleep(150); } catch (InterruptedException e) {}
            }
        });
        
        t1.start();
        t2.start();
    }
}
```

### Résumé : synchronized

| Aspect | Description |
|--------|-------------|
| **But** | Contrôler l'accès concurrent |
| **Portée** | Méthode ou bloc de code |
| **Verrou** | Implicite (objet ou classe) |
| **Libération** | Automatique à la fin du bloc |
| **Flexibilité** | Limitée par rapport à Lock |
| **Simplicité** | Plus simple que Lock |

---

## Question 5 : S'assurer d'un problème de manque de synchronisation

### Réponse

Pour s'assurer que votre code cause un problème de manque de synchronisation, vous pouvez utiliser plusieurs techniques de test et d'observation.

### Techniques pour Détecter les Problèmes

#### 1. **Tests avec Haute Concurrence**

Augmenter le nombre de threads et d'itérations pour amplifier les problèmes.

```java
public class TestConcurrence {
    private int compteur = 0; // Non synchronisé !
    
    public void incrementer() {
        compteur++; // Opération non atomique
    }
    
    public int getCompteur() {
        return compteur;
    }
    
    public static void main(String[] args) throws InterruptedException {
        TestConcurrence test = new TestConcurrence();
        
        int nombreThreads = 100;
        int iterations = 1000;
        Thread[] threads = new Thread[nombreThreads];
        
        // Créer et démarrer les threads
        for (int i = 0; i < nombreThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    test.incrementer();
                }
            });
            threads[i].start();
        }
        
        // Attendre tous les threads
        for (Thread t : threads) {
            t.join();
        }
        
        int attendu = nombreThreads * iterations;
        int reel = test.getCompteur();
        
        System.out.println("Attendu : " + attendu);
        System.out.println("Réel : " + reel);
        
        if (attendu != reel) {
            System.out.println("❌ PROBLÈME DE SYNCHRONISATION DÉTECTÉ !");
            System.out.println("Différence : " + (attendu - reel));
        } else {
            System.out.println("✅ Aucun problème détecté (peut être de la chance)");
        }
    }
}
```

#### 2. **Exécutions Répétées**

Exécuter le même test plusieurs fois pour observer l'instabilité.

```java
public class TestRepete {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Test répété 10 fois ===");
        
        for (int execution = 1; execution <= 10; execution++) {
            TestConcurrence test = new TestConcurrence();
            
            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 10000; i++) {
                    test.incrementer();
                }
            });
            
            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 10000; i++) {
                    test.incrementer();
                }
            });
            
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            
            System.out.println("Exécution " + execution + " : " + test.getCompteur());
        }
    }
}
```

**Output typique** :
```
=== Test répété 10 fois ===
Exécution 1 : 19847
Exécution 2 : 20000
Exécution 3 : 19523
Exécution 4 : 19891
Exécution 5 : 20000
Exécution 6 : 19245
Exécution 7 : 19678
Exécution 8 : 19934
Exécution 9 : 20000
Exécution 10 : 19512
```
➡️ Résultats **inconsistants** = problème de synchronisation

#### 3. **Débogage avec Affichages**

Ajouter des affichages pour voir les valeurs intermédiaires.

```java
public class DebugSynchronisation {
    private int valeur = 0;
    
    public void modifier() {
        int temp = valeur;
        System.out.println(Thread.currentThread().getName() + 
                         " - Lit : " + temp);
        
        // Pause pour amplifier le problème
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        
        temp++;
        System.out.println(Thread.currentThread().getName() + 
                         " - Écrit : " + temp);
        valeur = temp;
    }
    
    public static void main(String[] args) throws InterruptedException {
        DebugSynchronisation debug = new DebugSynchronisation();
        
        Thread t1 = new Thread(() -> debug.modifier(), "Thread-1");
        Thread t2 = new Thread(() -> debug.modifier(), "Thread-2");
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        System.out.println("Valeur finale : " + debug.valeur);
    }
}
```

**Output révélant le problème** :
```
Thread-1 - Lit : 0
Thread-2 - Lit : 0
Thread-1 - Écrit : 1
Thread-2 - Écrit : 1
Valeur finale : 1  ← Devrait être 2 !
```

#### 4. **Utilisation de CountDownLatch**

Synchroniser le départ des threads pour maximiser la concurrence.

```java
import java.util.concurrent.CountDownLatch;

public class TestAvecCountDownLatch {
    private int compteur = 0;
    
    public void incrementer() {
        compteur++;
    }
    
    public static void main(String[] args) throws InterruptedException {
        TestAvecCountDownLatch test = new TestAvecCountDownLatch();
        int nombreThreads = 50;
        
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(nombreThreads);
        
        // Créer les threads
        for (int i = 0; i < nombreThreads; i++) {
            new Thread(() -> {
                try {
                    startSignal.await(); // Attendre le signal de départ
                    
                    for (int j = 0; j < 1000; j++) {
                        test.incrementer();
                    }
                    
                    doneSignal.countDown(); // Signaler la fin
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        
        System.out.println("Tous les threads sont prêts. GO !");
        startSignal.countDown(); // Démarrer tous les threads en même temps
        
        doneSignal.await(); // Attendre que tous finissent
        
        int attendu = nombreThreads * 1000;
        int reel = test.compteur;
        
        System.out.println("Attendu : " + attendu);
        System.out.println("Réel : " + reel);
        System.out.println("Perte : " + (attendu - reel));
    }
}
```

#### 5. **Outils d'Analyse**

##### a) **Thread Sanitizer / FindBugs / SpotBugs**
Détectent automatiquement les problèmes de concurrence.

##### b) **Java Flight Recorder & Mission Control**
Profilent l'application et détectent les contentions.

##### c) **VisualVM**
Monitore les threads et identifie les deadlocks.

#### 6. **Assertions et Invariants**

```java
public class TestAvecAssertions {
    private int x = 0;
    private int y = 0;
    
    // Invariant : x devrait toujours être égal à y
    public void incrementer() {
        x++;
        // Sans synchronisation, un autre thread peut lire ici
        y++;
        
        // Vérifier l'invariant
        assert x == y : "Invariant violé : x=" + x + ", y=" + y;
    }
    
    public static void main(String[] args) {
        // Exécuter avec : java -ea TestAvecAssertions
        TestAvecAssertions test = new TestAvecAssertions();
        
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    test.incrementer();
                }
            }).start();
        }
    }
}
```

### Programme de Test Complet

```java
public class DiagnosticSynchronisation {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== DIAGNOSTIC DE SYNCHRONISATION ===\n");
        
        // Test 1 : Compteur simple
        System.out.println("Test 1 : Compteur non synchronisé");
        testCompteur(false);
        
        System.out.println("\nTest 2 : Compteur synchronisé");
        testCompteur(true);
        
        // Test 2 : Liste
        System.out.println("\nTest 3 : Liste non synchronisée");
        testListe(false);
        
        System.out.println("\nTest 4 : Liste synchronisée");
        testListe(true);
    }
    
    private static void testCompteur(boolean synchronise) throws InterruptedException {
        CompteurTest compteur = new CompteurTest(synchronise);
        int nombreThreads = 100;
        int iterations = 1000;
        Thread[] threads = new Thread[nombreThreads];
        
        long debut = System.currentTimeMillis();
        
        for (int i = 0; i < nombreThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    compteur.incrementer();
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        long duree = System.currentTimeMillis() - debut;
        int attendu = nombreThreads * iterations;
        int reel = compteur.getValeur();
        
        System.out.println("Attendu : " + attendu);
        System.out.println("Réel : " + reel);
        System.out.println("Durée : " + duree + " ms");
        
        if (attendu == reel) {
            System.out.println("✅ Résultat correct");
        } else {
            System.out.println("❌ PROBLÈME : Différence de " + (attendu - reel));
        }
    }
    
    private static void testListe(boolean synchronise) throws InterruptedException {
        ListeTest liste = new ListeTest(synchronise);
        int nombreThreads = 50;
        int iterations = 100;
        Thread[] threads = new Thread[nombreThreads];
        
        for (int i = 0; i < nombreThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    liste.ajouter("Element");
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        int attendu = nombreThreads * iterations;
        int reel = liste.getTaille();
        
        System.out.println("Attendu : " + attendu);
        System.out.println("Réel : " + reel);
        
        if (attendu == reel) {
            System.out.println("✅ Résultat correct");
        } else {
            System.out.println("❌ PROBLÈME : Différence de " + (attendu - reel));
        }
    }
}

class CompteurTest {
    private int valeur = 0;
    private boolean synchronise;
    
    public CompteurTest(boolean synchronise) {
        this.synchronise = synchronise;
    }
    
    public void incrementer() {
        if (synchronise) {
            synchronized(this) {
                valeur++;
            }
        } else {
            valeur++;
        }
    }
    
    public int getValeur() {
        return valeur;
    }
}

class ListeTest {
    private List<String> liste = new ArrayList<>();
    private boolean synchronise;
    
    public ListeTest(boolean synchronise) {
        this.synchronise = synchronise;
    }
    
    public void ajouter(String element) {
        if (synchronise) {
            synchronized(this) {
                liste.add(element);
            }
        } else {
            liste.add(element);
        }
    }
    
    public int getTaille() {
        return liste.size();
    }
}
```

### Signes de Problèmes de Synchronisation

✅ **Indicateurs** :
- Résultats **différents** à chaque exécution
- Valeur finale **inférieure** à celle attendue
- **Exceptions** : ConcurrentModificationException, NullPointerException
- **Blocages** (deadlocks) ou **attentes infinies**
- **Inconsistance** des données

---

## Question 6 : Points clés pour la synchronisation de l'exécution concurrente

### Réponse

Pour mettre en place une solution synchronisant l'exécution concurrente, il faut prendre en considération les points clés suivants :

### 1. **Identifier les Ressources Partagées**

```java
public class IdentificationRessources {
    // ✅ Ressources partagées à protéger
    private List<String> listePartagee = new ArrayList<>();
    private int compteurPartage = 0;
    private Map<String, Integer> mapPartagee = new HashMap<>();
    
    // ❌ Pas besoin de protection (variables locales aux threads)
    public void methode() {
        int variableLocale = 0; // Locale, pas partagée
        
        synchronized(this) {
            listePartagee.add("Element"); // Partagée, protégée
        }
    }
}
```

### 2. **Définir les Sections Critiques**

Identifier le code qui accède aux ressources partagées.

```java
public class SectionsCritiques {
    private int compteur = 0;
    
    public void incrementer() {
        // Code non critique
        System.out.println("Début");
        long debut = System.currentTimeMillis();
        
        // ⚠️ SECTION CRITIQUE : Accès à la ressource partagée
        synchronized(this) {
            compteur++; // Doit être protégé
        }
        
        // Code non critique
        long duree = System.currentTimeMillis() - debut;
        System.out.println("Fin : " + duree + " ms");
    }
}
```

### 3. **Choisir le Bon Mécanisme de Synchronisation**

| Mécanisme | Usage | Avantages |
|-----------|-------|-----------|
| **synchronized** | Simple, accès exclusif | Facile, automatique |
| **Lock** | Contrôle fin, timeout | Flexible, interruptible |
| **ReadWriteLock** | Lecture multiple, écriture exclusive | Performance lectures |
| **Atomic classes** | Variables simples | Sans verrou, rapide |
| **Semaphore** | Limiter accès concurrent | Contrôle du nombre |
| **CountDownLatch** | Attendre plusieurs threads | Synchronisation de démarrage |

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

public class ChoixMecanisme {
    // Pour un compteur simple : AtomicInteger
    private AtomicInteger compteur = new AtomicInteger(0);
    
    public void incrementer() {
        compteur.incrementAndGet(); // Atomique, pas besoin de synchronized
    }
    
    // Pour lecture/écriture : ReadWriteLock
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Map<String, String> cache = new HashMap<>();
    
    public String lire(String cle) {
        rwLock.readLock().lock();
        try {
            return cache.get(cle);
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    public void ecrire(String cle, String valeur) {
        rwLock.writeLock().lock();
        try {
            cache.put(cle, valeur);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

### 4. **Minimiser la Portée de la Synchronisation**

```java
public class PorteeSynchronisation {
    private List<String> liste = new ArrayList<>();
    
    // ❌ MAUVAIS : Synchronise trop
    public synchronized void ajouterMauvais(String element) {
        System.out.println("Traitement long...");
        dormir(1000); // Traitement long synchronisé inutilement
        
        liste.add(element);
        System.out.println("Ajouté");
    }
    
    // ✅ BON : Synchronise seulement le nécessaire
    public void ajouterBon(String element) {
        System.out.println("Traitement long...");
        dormir(1000); // Traitement long NON synchronisé
        
        synchronized(this) {
            liste.add(element); // Seule la section critique est synchronisée
        }
        System.out.println("Ajouté");
    }
    
    private void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

### 5. **Éviter les Deadlocks**

#### a) **Ordre de Verrouillage Cohérent**

```java
public class EviterDeadlock {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    // ❌ RISQUE DE DEADLOCK : Ordre différent
    public void methode1() {
        synchronized(lock1) {
            synchronized(lock2) {
                // Traitement
            }
        }
    }
    
    public void methode2() {
        synchronized(lock2) { // Ordre inversé !
            synchronized(lock1) {
                // Traitement
            }
        }
    }
    
    // ✅ ÉVITE DEADLOCK : Ordre cohérent
    public void methode1Corrigee() {
        synchronized(lock1) {
            synchronized(lock2) {
                // Traitement
            }
        }
    }
    
    public void methode2Corrigee() {
        synchronized(lock1) { // Même ordre
            synchronized(lock2) {
                // Traitement
            }
        }
    }
}
```

#### b) **Utiliser tryLock avec Timeout**

```java
import java.util.concurrent.locks.*;

public class DeadlockAvecTimeout {
    private final Lock lock1 = new ReentrantLock();
    private final Lock lock2 = new ReentrantLock();
    
    public void transferer() {
        try {
            if (lock1.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    if (lock2.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            // Traitement
                        } finally {
                            lock2.unlock();
                        }
                    } else {
                        System.out.println("Ne peut pas acquérir lock2, abandon");
                    }
                } finally {
                    lock1.unlock();
                }
            } else {
                System.out.println("Ne peut pas acquérir lock1, abandon");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 6. **Gérer les Exceptions Correctement**

```java
public class GestionExceptions {
    private final Lock lock = new ReentrantLock();
    private int compteur = 0;
    
    // ✅ BON : unlock() dans finally
    public void incrementer() {
        lock.lock();
        try {
            compteur++;
            
            if (compteur > 100) {
                throw new IllegalStateException("Compteur trop grand");
            }
            
        } finally {
            lock.unlock(); // Toujours libéré, même en cas d'exception
        }
    }
    
    // Avec synchronized, automatique
    public synchronized void incrementerSync() {
        compteur++;
        
        if (compteur > 100) {
            throw new IllegalStateException("Compteur trop grand");
        }
        // Verrou libéré automatiquement
    }
}
```

### 7. **Utiliser des Collections Thread-Safe**

```java
import java.util.concurrent.*;

public class CollectionsThreadSafe {
    // ✅ Collections thread-safe
    private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<String> liste = new CopyOnWriteArrayList<>();
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    
    // Ou wrapper synchronisé
    private List<String> listeSynchronisee = Collections.synchronizedList(new ArrayList<>());
    private Map<String, Integer> mapSynchronisee = Collections.synchronizedMap(new HashMap<>());
    
    public void utiliser() {
        // Pas besoin de synchronisation explicite
        map.put("cle", 1);
        liste.add("element");
        
        // Mais attention aux opérations composées
        // ❌ INCORRECT :
        if (!map.containsKey("cle")) {
            map.put("cle", 1); // Race condition entre containsKey et put
        }
        
        // ✅ CORRECT :
        map.putIfAbsent("cle", 1); // Opération atomique
    }
}
```

### 8. **Communication entre Threads**

```java
public class CommunicationThreads {
    private final Object lock = new Object();
    private boolean ready = false;
    
    public void producteur() throws InterruptedException {
        synchronized(lock) {
            // Produire des données
            System.out.println("Production...");
            Thread.sleep(1000);
            
            ready = true;
            lock.notifyAll(); // Notifier les threads en attente
        }
    }
    
    public void consommateur() throws InterruptedException {
        synchronized(lock) {
            while (!ready) {
                lock.wait(); // Attendre que les données soient prêtes
            }
            
            // Consommer les données
            System.out.println("Consommation...");
        }
    }
}
```

### 9. **Tester Intensivement**

```java
public class TestIntensif {
    public static void main(String[] args) throws InterruptedException {
        RessourcePartagee ressource = new RessourcePartagee();
        
        // Créer beaucoup de threads
        int nombreThreads = 100;
        Thread[] threads = new Thread[nombreThreads];
        
        for (int i = 0; i < nombreThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    ressource.incrementer();
                }
            });
            threads[i].start();
        }
        
        // Attendre tous les threads
        for (Thread t : threads) {
            t.join();
        }
        
        // Vérifier le résultat
        int attendu = nombreThreads * 1000;
        int reel = ressource.getValeur();
        
        assert attendu == reel : "Problème de synchronisation détecté !";
        System.out.println("✅ Test réussi : " + reel);
    }
}
```

### 10. **Documenter les Stratégies de Synchronisation**

```java
/**
 * Classe thread-safe pour gérer un cache.
 * 
 * Stratégie de synchronisation :
 * - Utilise ReadWriteLock pour permettre lectures concurrentes
 * - Les écritures sont exclusives
 * - Les opérations composées (get-then-put) sont atomiques
 */
public class Cache {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, String> data = new HashMap<>();
    
    /**
     * Lit une valeur du cache (opération thread-safe).
     * Plusieurs threads peuvent lire simultanément.
     */
    public String get(String key) {
        lock.readLock().lock();
        try {
            return data.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Écrit une valeur dans le cache (opération thread-safe).
     * L'écriture est exclusive.
     */
    public void put(String key, String value) {
        lock.writeLock().lock();
        try {
            data.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

### Checklist de Synchronisation

✅ **Avant d'implémenter** :
- [ ] Identifier toutes les ressources partagées
- [ ] Déterminer quels threads accèdent à quelles ressources
- [ ] Identifier les opérations qui doivent être atomiques

✅ **Pendant l'implémentation** :
- [ ] Choisir le mécanisme approprié (synchronized, Lock, Atomic, etc.)
- [ ] Minimiser la portée de la synchronisation
- [ ] Établir un ordre de verrouillage cohérent
- [ ] Gérer les exceptions correctement (finally)
- [ ] Documenter la stratégie

✅ **Après l'implémentation** :
- [ ] Tester avec haute concurrence
- [ ] Vérifier l'absence de deadlocks
- [ ] Mesurer les performances
- [ ] Profiler pour détecter les contentions

---

## Question 7 : Section critique d'un code

### Réponse

La **section critique** est une portion de code qui accède à une **ressource partagée** et qui ne doit être exécutée que par **un seul thread à la fois** pour garantir la cohérence des données.

### Caractéristiques d'une Section Critique

#### 1. **Accès à une Ressource Partagée**

```java
public class SectionCritique {
    private int compteur = 0; // Ressource partagée
    
    public void incrementer() {
        // ⚠️ SECTION CRITIQUE
        compteur++; // Accès et modification de la ressource partagée
    }
}
```

#### 2. **Exclusion Mutuelle Requise**

Un seul thread peut exécuter la section critique à la fois.

```java
public class ExclusionMutuelle {
    private List<String> liste = new ArrayList<>();
    
    public void ajouter(String element) {
        // Zone non critique
        System.out.println("Préparation...");
        
        // ⚠️ DÉBUT SECTION CRITIQUE
        synchronized(this) {
            liste.add(element); // Un seul thread à la fois
        }
        // ⚠️ FIN SECTION CRITIQUE
        
        // Zone non critique
        System.out.println("Terminé");
    }
}
```

### Exemples de Sections Critiques

#### Exemple 1 : Opération Read-Modify-Write

```java
public class ReadModifyWrite {
    private int solde = 1000;
    
    public void retirer(int montant) {
        // ⚠️ SECTION CRITIQUE : 3 opérations doivent être atomiques
        synchronized(this) {
            int soldeActuel = solde;     // 1. Read
            soldeActuel -= montant;       // 2. Modify
            solde = soldeActuel;          // 3. Write
        }
    }
}
```

#### Exemple 2 : Check-Then-Act

```java
public class CheckThenAct {
    private Map<String, Integer> stock = new HashMap<>();
    
    public boolean acheter(String produit, int quantite) {
        // ⚠️ SECTION CRITIQUE : vérification et action doivent être atomiques
        synchronized(this) {
            int disponible = stock.getOrDefault(produit, 0);
            
            if (disponible >= quantite) {        // Check
                stock.put(produit, disponible - quantite); // Act
                return true;
            }
            return false;
        }
    }
}
```

#### Exemple 3 : Opérations sur Collections

```java
public class OperationsCollections {
    private List<Integer> liste = new ArrayList<>();
    
    public void traiter() {
        // ⚠️ SECTION CRITIQUE : itération et modification
        synchronized(liste) {
            for (Integer i : liste) {
                // Traitement
            }
            liste.add(42); // Modification pendant l'itération
        }
    }
}
```

### Visualisation d'une Section Critique

```
Thread 1                    Thread 2
  |                           |
  | Préparation              | Préparation
  |                           |
  ▼ ATTEND                    ▼ ENTRE
╔═══════════════════════╗     ╔═══════════════════════╗
║   SECTION CRITIQUE    ║ ←── ║   SECTION CRITIQUE    ║
║   compteur++          ║     ║   compteur++          ║
╚═══════════════════════╝     ╚═══════════════════════╝
  ▲ ENTRE                     ▲ SORT
  |                           |
  | Post-traitement          | Post-traitement
  ▼                           ▼
```

### Sections Critiques Imbriquées

```java
public class SectionsCritiquesImbriquees {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    private int donnee1 = 0;
    private int donnee2 = 0;
    
    public void methode() {
        // Code non critique
        System.out.println("Début");
        
        // ⚠️ PREMIÈRE SECTION CRITIQUE
        synchronized(lock1) {
            donnee1++;
            
            // ⚠️ DEUXIÈME SECTION CRITIQUE (imbriquée)
            synchronized(lock2) {
                donnee2++;
            }
        }
        
        // Code non critique
        System.out.println("Fin");
    }
}
```

### Granularité des Sections Critiques

#### Trop Large (Mauvais)

```java
// ❌ Section critique trop large
public synchronized void traiterMauvais() {
    // Traitement long (1 seconde)
    dormir(1000);
    
    // Seule cette ligne nécessite vraiment la synchronisation
    compteur++;
    
    // Autre traitement long (1 seconde)
    dormir(1000);
}
// Problème : Les threads attendent 2 secondes inutilement
```

#### Optimale (Bon)

```java
// ✅ Section critique minimale
public void traiterBon() {
    // Traitement long (non synchronisé)
    dormir(1000);
    
    // Section critique minimale
    synchronized(this) {
        compteur++;
    }
    
    // Autre traitement long (non synchronisé)
    dormir(1000);
}
// Les threads n'attendent que le temps nécessaire
```

### Identifier les Sections Critiques

#### Questions à se Poser

1. **Accès à une variable partagée ?**
   - Si oui → Section critique potentielle

2. **Lecture ET écriture ?**
   - Si oui → Section critique nécessaire

3. **Opération atomique requise ?**
   - Si oui → Section critique

4. **Structure de données non thread-safe ?**
   - Si oui → Section critique

#### Exemple d'Analyse

```java
public class AnalyseSectionsCritiques {
    private int compteur = 0;          // Ressource partagée
    private final String NOM = "App";  // Constante (pas de section critique)
    
    public void methode1() {
        int local = 0;                 // Variable locale (pas de section critique)
        local++;
        
        compteur++;                    // ⚠️ SECTION CRITIQUE
    }
    
    public void methode2() {
        System.out.println(NOM);       // Lecture seule constante (pas de section critique)
        
        int valeur = compteur;         // ⚠️ SECTION CRITIQUE (lecture variable partagée)
    }
    
    public synchronized void methode3() {
        compteur++;                    // ⚠️ SECTION CRITIQUE (toute la méthode)
    }
}
```

### Sections Critiques et Performance

```java
public class PerformanceSectionsCritiques {
    private int compteur = 0;
    private List<String> logs = new ArrayList<>();
    
    // ❌ MAUVAIS : Une seule grande section critique
    public synchronized void traiterMauvais(String data) {
        // Tout est synchronisé, même les calculs longs
        String resultat = calculerLong(data);  // 1 seconde
        compteur++;
        logs.add(resultat);
    }
    
    // ✅ BON : Section critique minimale
    public void traiterBon(String data) {
        // Calcul long NON synchronisé
        String resultat = calculerLong(data);  // 1 seconde
        
        // Seule la modification des ressources partagées est synchronisée
        synchronized(this) {
            compteur++;
            logs.add(resultat);
        }
    }
    
    private String calculerLong(String data) {
        // Simulation calcul long
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        return data.toUpperCase();
    }
}
```

### Résumé : Section Critique

| Aspect | Description |
|--------|-------------|
| **Définition** | Code accédant à une ressource partagée |
| **Objectif** | Garantir l'exclusion mutuelle |
| **Implémentation** | synchronized, Lock, Atomic, etc. |
| **Taille** | Minimale pour meilleures performances |
| **Protection** | Obligatoire pour cohérence des données |

**Règle d'or** : Une section critique doit être **aussi petite que possible** mais **aussi grande que nécessaire**.

---

## Question 8 : Qu'est-ce que « start() » et « join() » ?

### Réponse

`start()` et `join()` sont deux méthodes fondamentales pour la gestion du cycle de vie des threads en Java.

### start() - Démarrer un Thread

#### Définition

La méthode `start()` démarre l'exécution d'un nouveau thread. Elle :
1. Crée un nouveau thread système
2. Appelle automatiquement la méthode `run()` dans ce nouveau thread
3. Retourne immédiatement (non bloquante)

#### Syntaxe

```java
Thread thread = new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Thread exécuté");
    }
});

thread.start(); // Démarre le thread
```

#### Exemple Complet

```java
public class ExempleStart {
    public static void main(String[] args) {
        System.out.println("Main : Début");
        
        Thread t1 = new Thread(() -> {
            System.out.println("Thread-1 : Exécution");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread-1 : Fin");
        });
        
        System.out.println("Main : Avant start()");
        t1.start(); // Démarre le thread (non bloquant)
        System.out.println("Main : Après start()");
        System.out.println("Main : Fin");
    }
}
```

**Output** :
```
Main : Début
Main : Avant start()
Main : Après start()
Main : Fin
Thread-1 : Exécution
(pause de 2 secondes)
Thread-1 : Fin
```

#### ⚠️ Erreur Courante : Appeler run() au lieu de start()

```java
// ❌ MAUVAIS : Appel direct de run()
Thread t1 = new Thread(() -> {
    System.out.println("Thread-1");
});
t1.run(); // S'exécute dans le thread COURANT, pas un nouveau thread !

// ✅ CORRECT : Utiliser start()
Thread t2 = new Thread(() -> {
    System.out.println("Thread-2");
});
t2.start(); // Crée un NOUVEAU thread
```

#### start() ne peut être appelé qu'une fois

```java
Thread thread = new Thread(() -> {
    System.out.println("Thread");
});

thread.start(); // OK
thread.start(); // ❌ IllegalThreadStateException
```

### join() - Attendre la Fin d'un Thread

#### Définition

La méthode `join()` permet au thread courant d'**attendre** qu'un autre thread se termine. Elle est **bloquante**.

#### Syntaxe

```java
Thread thread = new Thread(() -> {
    // Travail du thread
});

thread.start();
thread.join(); // Attend que thread se termine
```

#### Exemple Complet

```java
public class ExempleJoin {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Main : Début");
        
        Thread t1 = new Thread(() -> {
            System.out.println("Thread-1 : Début");
            try {
                Thread.sleep(3000); // Simule un travail de 3 secondes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread-1 : Fin");
        });
        
        t1.start();
        System.out.println("Main : Thread démarré, attente...");
        
        t1.join(); // ⏳ BLOQUANT : Attend que t1 se termine
        
        System.out.println("Main : Thread terminé");
        System.out.println("Main : Fin");
    }
}
```

**Output** :
```
Main : Début
Thread-1 : Début
Main : Thread démarré, attente...
(pause de 3 secondes)
Thread-1 : Fin
Main : Thread terminé
Main : Fin
```

#### join() avec Timeout

```java
Thread thread = new Thread(() -> {
    try {
        Thread.sleep(5000); // 5 secondes
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
});

thread.start();

// Attendre maximum 2 secondes
thread.join(2000);

if (thread.isAlive()) {
    System.out.println("Thread encore en cours après 2 secondes");
} else {
    System.out.println("Thread terminé");
}
```

### Exemples Combinés start() et join()

#### Exemple 1 : Exécution Séquentielle avec join()

```java
public class ExecutionSequentielle {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            System.out.println("Tâche 1 - Début");
            dormir(2000);
            System.out.println("Tâche 1 - Fin");
        });
        
        Thread t2 = new Thread(() -> {
            System.out.println("Tâche 2 - Début");
            dormir(2000);
            System.out.println("Tâche 2 - Fin");
        });
        
        t1.start();
        t1.join(); // Attendre que t1 se termine
        
        t2.start();
        t2.join(); // Attendre que t2 se termine
        
        System.out.println("Toutes les tâches terminées");
    }
    
    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

**Output** :
```
Tâche 1 - Début
(2 secondes)
Tâche 1 - Fin
Tâche 2 - Début
(2 secondes)
Tâche 2 - Fin
Toutes les tâches terminées
```
**Durée totale** : ~4 secondes

#### Exemple 2 : Exécution Parallèle avec join()

```java
public class ExecutionParallele {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            System.out.println("Tâche 1 - Début");
            dormir(2000);
            System.out.println("Tâche 1 - Fin");
        });
        
        Thread t2 = new Thread(() -> {
            System.out.println("Tâche 2 - Début");
            dormir(2000);
            System.out.println("Tâche 2 - Fin");
        });
        
        t1.start(); // Démarrer t1
        t2.start(); // Démarrer t2 (en parallèle)
        
        t1.join(); // Attendre t1
        t2.join(); // Attendre t2
        
        System.out.println("Toutes les tâches terminées");
    }
    
    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

**Output** :
```
Tâche 1 - Début
Tâche 2 - Début
(2 secondes - en parallèle)
Tâche 1 - Fin
Tâche 2 - Fin
Toutes les tâches terminées
```
**Durée totale** : ~2 secondes (gain de temps grâce au parallélisme)

#### Exemple 3 : Attendre Plusieurs Threads

```java
public class AttendreMultiples {
    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[5];
        
        // Créer et démarrer 5 threads
        for (int i = 0; i < threads.length; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                System.out.println("Thread " + id + " - Début");
                dormir(1000 * (id + 1)); // Durées différentes
                System.out.println("Thread " + id + " - Fin");
            });
            threads[i].start();
        }
        
        // Attendre tous les threads
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Tous les threads terminés");
    }
    
    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

### Cas d'Usage Pratiques

#### Pattern : Download Manager

```java
public class DownloadManager {
    public static void main(String[] args) throws InterruptedException {
        String[] urls = {
            "http://example.com/file1.pdf",
            "http://example.com/file2.pdf",
            "http://example.com/file3.pdf"
        };
        
        Thread[] downloadThreads = new Thread[urls.length];
        
        // Démarrer tous les téléchargements en parallèle
        for (int i = 0; i < urls.length; i++) {
            final String url = urls[i];
            downloadThreads[i] = new Thread(() -> {
                System.out.println("Téléchargement de " + url);
                // Simuler téléchargement
                dormir(2000);
                System.out.println("Terminé : " + url);
            });
            downloadThreads[i].start();
        }
        
        // Attendre que tous les téléchargements soient terminés
        for (Thread thread : downloadThreads) {
            thread.join();
        }
        
        System.out.println("Tous les téléchargements terminés !");
    }
    
    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

#### Pattern : Map-Reduce Simplifié

```java
public class MapReduceSimple {
    public static void main(String[] args) throws InterruptedException {
        int[] nombres = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] resultats = new int[nombres.length];
        Thread[] threads = new Thread[nombres.length];
        
        // MAP : Calculer le carré de chaque nombre en parallèle
        for (int i = 0; i < nombres.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                resultats[index] = nombres[index] * nombres[index];
                System.out.println("Carré de " + nombres[index] + " = " + resultats[index]);
            });
            threads[i].start();
        }
        
        // Attendre tous les threads MAP
        for (Thread thread : threads) {
            thread.join();
        }
        
        // REDUCE : Calculer la somme
        int somme = 0;
        for (int resultat : resultats) {
            somme += resultat;
        }
        
        System.out.println("Somme des carrés : " + somme);
    }
}
```

### Comparaison start() vs join()

| Aspect | start() | join() |
|--------|---------|--------|
| **Action** | Démarre un thread | Attend un thread |
| **Bloquant** | Non | Oui |
| **Retour** | Immédiat | Quand thread termine |
| **Usage** | Lancer l'exécution | Synchroniser la fin |
| **Fréquence** | Une seule fois par thread | Peut être appelé plusieurs fois |

### Cycle de Vie Complet d'un Thread

```
NEW → start() → RUNNABLE → RUNNING → TERMINATED
                                ↑
                                |
                             join()
                        (autre thread attend)
```

### Résumé

**start()** :
- Démarre un nouveau thread
- Appelle automatiquement run()
- Non bloquant
- Ne peut être appelé qu'une fois

**join()** :
- Attend qu'un thread se termine
- Bloquant
- Peut avoir un timeout
- Utilisé pour synchroniser les threads

---

## Conclusion Générale

La synchronisation en multithreading est essentielle pour :
- ✅ Garantir la cohérence des données
- ✅ Éviter les race conditions
- ✅ Coordonner l'exécution de plusieurs threads
- ✅ Protéger les sections critiques

Les outils principaux sont :
- `synchronized` : Simple et automatique
- `Lock` : Flexible et puissant
- `Atomic` : Sans verrou, performant
- `start()` et `join()` : Gestion du cycle de vie des threads
