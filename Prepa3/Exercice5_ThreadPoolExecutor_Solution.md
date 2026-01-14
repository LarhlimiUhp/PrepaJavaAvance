# Exercice 5 : ThreadPoolExecutor / ExecutorService - Solution Complète

## Introduction

Cet exercice couvre les concepts fondamentaux du multithreading en Java avec l'architecture ThreadPoolExecutor/ExecutorService.

---

## Question 1 : Critères avant d'adopter ThreadPoolExecutor/ExecutorService

### Réponse

Avant d'adopter une solution basée sur ThreadPoolExecutor/ExecutorService, il faut prendre en considération les critères suivants :

#### 1. **Nature de la Tâche**
- Les tâches sont-elles **indépendantes** les unes des autres ?
- Les tâches sont-elles **CPU-intensive** (calculs lourds) ou **I/O-intensive** (lecture/écriture) ?
- Quelle est la **durée d'exécution** estimée de chaque tâche ?

#### 2. **Volume de Travail**
- **Nombre de tâches** : Ai-je beaucoup de tâches à exécuter ?
- **Fréquence** : Les tâches arrivent-elles en continu ou par lots ?
- Est-ce que le volume justifie l'overhead du multithreading ?

#### 3. **Ressources Système**
- **Nombre de cœurs CPU** disponibles
- **Mémoire disponible** (chaque thread consomme de la mémoire)
- **Contraintes système** (limites du système d'exploitation)

#### 4. **Performance et Scalabilité**
- Le parallélisme améliore-t-il réellement les performances ?
- Quel est le **nombre optimal de threads** pour mon cas d'usage ?
- Y a-t-il un risque de **contention** (accès concurrent aux ressources partagées) ?

#### 5. **Complexité de Gestion**
- Ai-je besoin de **contrôler le cycle de vie** des threads ?
- Dois-je gérer une **file d'attente** de tâches ?
- Ai-je besoin de récupérer des **résultats** des tâches ?

#### 6. **Coût de Création de Threads**
- La création répétée de threads est coûteuse
- Le pool réutilise les threads, ce qui améliore les performances

### Quand utiliser ThreadPoolExecutor/ExecutorService ?

✅ **OUI, utiliser quand :**
- Nombreuses tâches courtes à exécuter
- Tâches indépendantes et parallélisables
- Besoin de limiter le nombre de threads simultanés
- Besoin de gérer une file d'attente de tâches
- Application serveur traitant de multiples requêtes

❌ **NON, ne pas utiliser quand :**
- Une seule tâche ou très peu de tâches
- Tâches très longues et peu nombreuses
- Application simple sans besoin de concurrence
- Overhead supérieur au gain de performance

---

## Question 2 : Déclarer un ThreadPoolExecutor/ExecutorService pour 7 threads

### Réponse

Il existe plusieurs façons de créer un ExecutorService permettant d'exécuter 7 threads en parallèle :

### Solution 1 : Utiliser `Executors.newFixedThreadPool()`

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolExample {
    public static void main(String[] args) {
        // Créer un pool de 7 threads
        ExecutorService executor = Executors.newFixedThreadPool(7);
        
        // Utiliser l'executor...
        
        // Arrêter l'executor
        executor.shutdown();
    }
}
```

### Solution 2 : Utiliser `ThreadPoolExecutor` directement (plus de contrôle)

```java
import java.util.concurrent.*;

public class ThreadPoolExecutorExample {
    public static void main(String[] args) {
        // Paramètres du ThreadPoolExecutor
        int corePoolSize = 7;        // Nombre de threads minimum
        int maximumPoolSize = 7;     // Nombre de threads maximum
        long keepAliveTime = 60L;    // Durée de vie des threads inactifs
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        
        // Créer le ThreadPoolExecutor
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            unit,
            workQueue
        );
        
        // Utiliser l'executor...
        
        // Arrêter l'executor
        executor.shutdown();
    }
}
```

### Solution 3 : Configuration complète avec nom de threads

```java
import java.util.concurrent.*;

public class ConfiguredThreadPool {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            7,                                    // corePoolSize
            7,                                    // maximumPoolSize
            0L,                                   // keepAliveTime
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),  // workQueue
            new ThreadFactory() {                 // threadFactory personnalisé
                private int counter = 0;
                
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "Worker-" + counter++);
                    return thread;
                }
            },
            new ThreadPoolExecutor.AbortPolicy()  // RejectedExecutionHandler
        );
        
        System.out.println("Pool de 7 threads créé");
        
        executor.shutdown();
    }
}
```

### Comparaison des Approches

| Approche | Avantages | Inconvénients |
|----------|-----------|---------------|
| `newFixedThreadPool(7)` | Simple, rapide | Moins de contrôle |
| `ThreadPoolExecutor` direct | Contrôle total | Plus verbeux |
| Configuration complète | Maximum de flexibilité | Complexe |

---

## Question 3 : Classe héritant de Thread dans ThreadPoolExecutor

### Réponse

Une classe qui hérite de la classe `Thread` dans une architecture ThreadPoolExecutor/ExecutorService représente **une tâche (Task)** à exécuter.

### Explication Détaillée

```java
// Cette classe représente UNE TÂCHE à exécuter
public class MaTache extends Thread {
    private int id;
    
    public MaTache(int id) {
        this.id = id;
    }
    
    @Override
    public void run() {
        // Le code de la tâche à exécuter
        System.out.println("Tâche " + id + " exécutée par " + 
                          Thread.currentThread().getName());
        
        // Simulation de travail
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### Points Clés

1. **Tâche = Unité de travail** : Chaque instance représente un travail indépendant
2. **Pas un thread du pool** : Ce n'est PAS un thread du pool, mais une tâche soumise au pool
3. **Exécution par le pool** : Le pool va assigner cette tâche à un de ses threads disponibles
4. **Réutilisation** : Le même thread du pool peut exécuter plusieurs tâches différentes

### Exemple Complet

```java
public class ExempleTaskThread {
    public static void main(String[] args) {
        // Créer un pool de 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Créer et soumettre 10 tâches
        for (int i = 0; i < 10; i++) {
            MaTache tache = new MaTache(i);
            executor.submit(tache);
        }
        
        // Arrêter le pool
        executor.shutdown();
    }
}

class MaTache extends Thread {
    private int id;
    
    public MaTache(int id) {
        this.id = id;
    }
    
    @Override
    public void run() {
        System.out.println("Tâche " + id + " exécutée par " + 
                          Thread.currentThread().getName());
    }
}
```

**Note** : Il est généralement préférable d'implémenter `Runnable` ou `Callable` plutôt que d'hériter de `Thread`.

---

## Question 4 : Utilité de la méthode `run()`

### Réponse

La méthode `run()` contient **le code qui sera exécuté** par le thread.

### Caractéristiques de `run()`

#### 1. **Définition du Travail**
```java
@Override
public void run() {
    // Code de la tâche à exécuter
    System.out.println("Exécution de la tâche");
    // Traitement...
}
```

#### 2. **Point d'Entrée**
- C'est le **point d'entrée** de l'exécution du thread
- Équivalent du `main()` pour un thread

#### 3. **Exécution Automatique**
- Appelée automatiquement quand le thread démarre
- Ne doit **JAMAIS** être appelée directement

### Exemple Complet

```java
public class TaskExample extends Thread {
    private String taskName;
    
    public TaskExample(String taskName) {
        this.taskName = taskName;
    }
    
    @Override
    public void run() {
        System.out.println(taskName + " - Début");
        
        try {
            // Simulation de travail
            for (int i = 1; i <= 5; i++) {
                System.out.println(taskName + " - Étape " + i);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            System.out.println(taskName + " - Interrompu");
        }
        
        System.out.println(taskName + " - Fin");
    }
}
```

### ⚠️ Erreur Courante

```java
// ❌ MAUVAIS : Appel direct de run()
MaTache tache = new MaTache();
tache.run(); // Exécute dans le thread courant, pas un nouveau thread

// ✅ CORRECT : Utiliser submit() ou start()
ExecutorService executor = Executors.newFixedThreadPool(2);
executor.submit(tache); // Exécute dans un thread du pool
```

---

## Question 5 : Utilité de la méthode `submit()`

### Réponse

La méthode `submit()` permet de **soumettre une tâche** au ThreadPoolExecutor/ExecutorService pour qu'elle soit exécutée par un thread du pool.

### Caractéristiques de `submit()`

#### 1. **Soumission de Tâche**
```java
ExecutorService executor = Executors.newFixedThreadPool(5);
MaTache tache = new MaTache();
executor.submit(tache); // Soumet la tâche au pool
```

#### 2. **Retourne un Future**
```java
Future<?> future = executor.submit(tache);
// Permet de suivre l'état de la tâche et récupérer le résultat
```

#### 3. **Trois Variantes**

**a) `submit(Runnable task)`** - Sans résultat
```java
Future<?> future = executor.submit(new Runnable() {
    @Override
    public void run() {
        System.out.println("Tâche exécutée");
    }
});
```

**b) `submit(Callable<T> task)`** - Avec résultat
```java
Future<Integer> future = executor.submit(new Callable<Integer>() {
    @Override
    public Integer call() {
        return 42;
    }
});

Integer resultat = future.get(); // Bloquant jusqu'à ce que la tâche se termine
```

**c) `submit(Runnable task, T result)`** - Avec résultat prédéfini
```java
String resultat = "Terminé";
Future<String> future = executor.submit(new Runnable() {
    @Override
    public void run() {
        System.out.println("Tâche");
    }
}, resultat);
```

### Exemple Complet

```java
import java.util.concurrent.*;

public class SubmitExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Exemple 1 : Soumettre un Runnable
        Future<?> future1 = executor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Tâche Runnable exécutée");
            }
        });
        
        // Exemple 2 : Soumettre un Callable
        Future<String> future2 = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                return "Résultat de la tâche Callable";
            }
        });
        
        try {
            // Attendre le résultat
            String resultat = future2.get();
            System.out.println("Résultat reçu : " + resultat);
            
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
    }
}
```

### Différence entre `submit()` et `execute()`

| Méthode | Retour | Usage |
|---------|--------|-------|
| `submit()` | `Future<?>` | Quand on veut récupérer un résultat ou suivre l'état |
| `execute()` | `void` | Fire-and-forget, pas besoin de résultat |

```java
// execute() - Pas de retour
executor.execute(new Runnable() {
    public void run() {
        System.out.println("Execute");
    }
});

// submit() - Retourne un Future
Future<?> future = executor.submit(new Runnable() {
    public void run() {
        System.out.println("Submit");
    }
});
```

---

## Question 6 : Qu'est-ce qu'une file d'attente ?

### Réponse

Une **file d'attente (Queue)** est une structure de données qui stocke les tâches en attente d'être exécutées par les threads du pool.

### Principe FIFO (First In, First Out)

```
┌─────────────────────────────────────────────────┐
│         FILE D'ATTENTE (WorkQueue)              │
├─────────────────────────────────────────────────┤
│ Entrée → [Tâche3] [Tâche2] [Tâche1] → Sortie   │
│                                                 │
│         Plus ancienne ←──────── Plus récente    │
└─────────────────────────────────────────────────┘
         ↓                    ↑
    Exécution             Soumission
```

### Fonctionnement

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

// Si on soumet 5 tâches mais qu'on a seulement 2 threads :
executor.submit(tache1); // Thread 1 exécute
executor.submit(tache2); // Thread 2 exécute
executor.submit(tache3); // → File d'attente
executor.submit(tache4); // → File d'attente
executor.submit(tache5); // → File d'attente

// Quand Thread 1 termine tache1 :
// → Il prend tache3 de la file d'attente
```

### Types de Files d'Attente

#### 1. **LinkedBlockingQueue** (illimitée)
```java
BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2, 4, 60L, TimeUnit.SECONDS, queue
);
// Accepte un nombre illimité de tâches
```

#### 2. **ArrayBlockingQueue** (limitée)
```java
BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2, 4, 60L, TimeUnit.SECONDS, queue
);
// Maximum 10 tâches en attente
```

#### 3. **SynchronousQueue** (pas de stockage)
```java
BlockingQueue<Runnable> queue = new SynchronousQueue<>();
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2, 4, 60L, TimeUnit.SECONDS, queue
);
// Tâche rejetée si aucun thread disponible
```

### Exemple Visuel

```java
public class QueueExample {
    public static void main(String[] args) {
        // Pool de 2 threads
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Soumettre 6 tâches
        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Début Tâche " + taskId + " - " + 
                                  Thread.currentThread().getName());
                try {
                    Thread.sleep(2000); // Simule 2 secondes de travail
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Fin Tâche " + taskId);
            });
            System.out.println("Tâche " + taskId + " soumise");
        }
        
        executor.shutdown();
    }
}
```

**Output possible** :
```
Tâche 1 soumise
Tâche 2 soumise
Tâche 3 soumise (→ file d'attente)
Tâche 4 soumise (→ file d'attente)
Tâche 5 soumise (→ file d'attente)
Tâche 6 soumise (→ file d'attente)
Début Tâche 1 - pool-1-thread-1
Début Tâche 2 - pool-1-thread-2
Fin Tâche 1
Début Tâche 3 - pool-1-thread-1
Fin Tâche 2
Début Tâche 4 - pool-1-thread-2
...
```

---

## Question 7 : Problèmes d'exécution avec ThreadPoolExecutor

### Réponse

L'utilisation d'une architecture ThreadPoolExecutor/ExecutorService peut causer plusieurs problèmes :

### 1. **Deadlock (Blocage Mutuel)**

#### Problème
Deux threads ou plus attendent indéfiniment des ressources détenues par l'autre.

#### Exemple
```java
public class DeadlockExample {
    private static Object lock1 = new Object();
    private static Object lock2 = new Object();
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Thread 1 : verrouille lock1 puis lock2
        executor.submit(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1 : lock1 acquis");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                
                synchronized (lock2) {
                    System.out.println("Thread 1 : lock2 acquis");
                }
            }
        });
        
        // Thread 2 : verrouille lock2 puis lock1
        executor.submit(() -> {
            synchronized (lock2) {
                System.out.println("Thread 2 : lock2 acquis");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                
                synchronized (lock1) {
                    System.out.println("Thread 2 : lock1 acquis");
                }
            }
        });
        
        executor.shutdown();
    }
}
```

### 2. **Race Condition (Condition de Course)**

#### Problème
Plusieurs threads accèdent et modifient une ressource partagée sans synchronisation.

#### Exemple
```java
public class RaceConditionExample {
    private static int compteur = 0;
    
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // 100 threads incrémentent le compteur 1000 fois
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    compteur++; // Opération non atomique !
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("Compteur attendu : 100000");
        System.out.println("Compteur réel : " + compteur); // Souvent < 100000
    }
}
```

### 3. **Thread Starvation (Famine de Thread)**

#### Problème
Certaines tâches ne sont jamais exécutées car d'autres monopolisent les threads.

#### Exemple
```java
public class StarvationExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Tâche longue qui monopolise les threads
        executor.submit(() -> {
            while (true) {
                // Travail infini
                System.out.println("Tâche longue en cours...");
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
        });
        
        executor.submit(() -> {
            while (true) {
                System.out.println("Autre tâche longue...");
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
        });
        
        // Cette tâche ne sera JAMAIS exécutée
        executor.submit(() -> {
            System.out.println("Tâche courte - Je ne serai jamais exécutée !");
        });
    }
}
```

### 4. **Memory Leak (Fuite Mémoire)**

#### Problème
Oublier de fermer l'ExecutorService ou accumulation de tâches dans la queue.

#### Exemple
```java
public class MemoryLeakExample {
    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            
            executor.submit(() -> {
                System.out.println("Tâche exécutée");
            });
            
            // ❌ OUBLI : executor.shutdown();
            // Les threads restent en vie et consomment de la mémoire !
        }
    }
}
```

### 5. **Thread Pool Saturation**

#### Problème
Trop de tâches soumises, la file d'attente déborde.

#### Exemple
```java
public class SaturationExample {
    public static void main(String[] args) {
        // Pool avec file d'attente limitée
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 2, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(5), // Seulement 5 tâches en attente
            new ThreadPoolExecutor.AbortPolicy() // Rejette si pleine
        );
        
        try {
            // Soumettre 20 tâches
            for (int i = 0; i < 20; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("Tâche " + taskId);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (RejectedExecutionException e) {
            System.out.println("❌ Tâche rejetée : Pool saturé !");
        }
        
        executor.shutdown();
    }
}
```

### 6. **Improper Exception Handling**

#### Problème
Exceptions non catchées dans les threads peuvent causer des problèmes silencieux.

#### Exemple
```java
public class ExceptionHandlingExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        executor.submit(() -> {
            System.out.println("Tâche 1 - Début");
            int resultat = 10 / 0; // ArithmeticException !
            System.out.println("Tâche 1 - Fin"); // Jamais atteint
        });
        
        // L'exception est "avalée", le programme continue
        System.out.println("Programme continue...");
        
        executor.shutdown();
    }
}
```

### 7. **Context Switching Overhead**

#### Problème
Trop de threads cause trop de changements de contexte, ralentissant le système.

#### Exemple
```java
public class ContextSwitchingExample {
    public static void main(String[] args) {
        // ❌ MAUVAIS : Trop de threads pour 4 cœurs CPU
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        
        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                // Calculs CPU-intensive
                double result = 0;
                for (int j = 0; j < 1000000; j++) {
                    result += Math.sqrt(j);
                }
            });
        }
        
        executor.shutdown();
        
        // ✅ MIEUX : Nombre de threads = nombre de cœurs
        // ExecutorService executor = Executors.newFixedThreadPool(
        //     Runtime.getRuntime().availableProcessors()
        // );
    }
}
```

### Résumé des Problèmes

| Problème | Cause | Solution |
|----------|-------|----------|
| Deadlock | Verrouillage circulaire | Ordre de verrouillage cohérent |
| Race Condition | Accès concurrent non synchronisé | Synchronisation, AtomicXXX |
| Starvation | Tâches monopolisent les threads | Priorités, timeout |
| Memory Leak | Oubli de shutdown() | Toujours appeler shutdown() |
| Saturation | File d'attente pleine | Augmenter capacité ou gérer rejets |
| Exceptions | Pas de try-catch | Gérer exceptions dans run() |
| Context Switching | Trop de threads | Ajuster nombre de threads |

---

## Question 8 : Utilisation de `Collection<Future<?>>`

### Réponse

`Collection<Future<?>>` est utilisée pour **stocker et gérer les résultats** de plusieurs tâches soumises à un ExecutorService.

### Principe

```java
Collection<Future<?>> futures = new ArrayList<>();

// Soumettre plusieurs tâches et stocker leurs Future
for (int i = 0; i < 10; i++) {
    Future<?> future = executor.submit(new MaTache(i));
    futures.add(future);
}

// Attendre que toutes les tâches se terminent
for (Future<?> future : futures) {
    future.get(); // Bloquant
}
```

### Exemple Complet avec Callable

```java
import java.util.*;
import java.util.concurrent.*;

public class FutureCollectionExample {
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        // Collection pour stocker les Future
        Collection<Future<Integer>> futures = new ArrayList<>();
        
        // Soumettre 10 tâches qui calculent des carrés
        for (int i = 1; i <= 10; i++) {
            final int nombre = i;
            
            Callable<Integer> tache = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    Thread.sleep(1000); // Simule un calcul
                    return nombre * nombre;
                }
            };
            
            Future<Integer> future = executor.submit(tache);
            futures.add(future);
        }
        
        // Récupérer tous les résultats
        System.out.println("=== Récupération des résultats ===");
        int somme = 0;
        
        for (Future<Integer> future : futures) {
            try {
                Integer resultat = future.get(); // Attend le résultat
                System.out.println("Résultat reçu : " + resultat);
                somme += resultat;
                
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Erreur : " + e.getMessage());
            }
        }
        
        System.out.println("Somme totale : " + somme);
        
        executor.shutdown();
    }
}
```

### Méthodes Utiles sur Future

#### 1. **`get()`** - Récupérer le résultat
```java
Integer resultat = future.get(); // Bloquant
```

#### 2. **`get(timeout, unit)`** - Avec timeout
```java
try {
    Integer resultat = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    System.out.println("Timeout dépassé");
}
```

#### 3. **`isDone()`** - Vérifier si terminé
```java
if (future.isDone()) {
    Integer resultat = future.get();
}
```

#### 4. **`cancel()`** - Annuler la tâche
```java
boolean cancelled = future.cancel(true);
```

#### 5. **`isCancelled()`** - Vérifier si annulée
```java
if (future.isCancelled()) {
    System.out.println("Tâche annulée");
}
```

### Exemple avec Gestion d'État

```java
public class FutureManagementExample {
    
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Collection<Future<String>> futures = new ArrayList<>();
        
        // Soumettre des tâches de durées différentes
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            final int sleepTime = i * 1000; // 1s, 2s, 3s, 4s, 5s
            
            Future<String> future = executor.submit(() -> {
                Thread.sleep(sleepTime);
                return "Tâche " + taskId + " terminée";
            });
            
            futures.add(future);
        }
        
        // Surveiller l'état des tâches
        while (!allDone(futures)) {
            System.out.println("En attente... Tâches terminées : " + 
                             countDone(futures) + "/" + futures.size());
            Thread.sleep(500);
        }
        
        // Récupérer tous les résultats
        System.out.println("\n=== Tous les résultats ===");
        for (Future<String> future : futures) {
            try {
                System.out.println(future.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
    }
    
    private static boolean allDone(Collection<Future<String>> futures) {
        for (Future<String> future : futures) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }
    
    private static int countDone(Collection<Future<String>> futures) {
        int count = 0;
        for (Future<String> future : futures) {
            if (future.isDone()) {
                count++;
            }
        }
        return count;
    }
}
```

### Utilisation avec `invokeAll()`

```java
public class InvokeAllExample {
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Créer une liste de Callable
        List<Callable<Integer>> taches = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            final int nombre = i;
            taches.add(() -> {
                Thread.sleep(1000);
                return nombre * nombre;
            });
        }
        
        try {
            // invokeAll() attend que TOUTES les tâches soient terminées
            List<Future<Integer>> futures = executor.invokeAll(taches);
            
            // Tous les Future sont déjà done
            System.out.println("Toutes les tâches sont terminées !");
            
            for (Future<Integer> future : futures) {
                System.out.println("Résultat : " + future.get());
            }
            
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
    }
}
```

### Points Clés

1. **`Collection<Future<?>>`** permet de gérer plusieurs tâches en parallèle
2. **`future.get()`** est bloquant jusqu'à ce que la tâche se termine
3. **`future.isDone()`** permet de vérifier l'état sans bloquer
4. **`invokeAll()`** attend automatiquement toutes les tâches
5. Utile pour des **opérations batch** (traiter plusieurs éléments en parallèle)

---

## Résumé Général

### Architecture ThreadPoolExecutor/ExecutorService

```
┌──────────────────────────────────────────────────────────┐
│                   ExecutorService                        │
├──────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │  Thread 1   │  │  Thread 2   │  │  Thread N   │     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
│         │                 │                 │             │
│         └─────────────────┴─────────────────┘             │
│                           │                               │
│                ┌──────────▼─────────┐                    │
│                │   WorkQueue        │                    │
│                │  [T3][T4][T5][T6]  │                    │
│                └────────────────────┘                    │
└──────────────────────────────────────────────────────────┘
```

### Flux d'Exécution

1. **Créer** l'ExecutorService
2. **Soumettre** des tâches avec `submit()` ou `execute()`
3. Les tâches sont placées dans la **file d'attente**
4. Les **threads du pool** prennent les tâches et les exécutent
5. Les **résultats** sont retournés via `Future`
6. **Fermer** l'ExecutorService avec `shutdown()`

### Bonnes Pratiques

✅ Toujours appeler `shutdown()` ou `shutdownNow()`
✅ Gérer les exceptions dans `run()` ou `call()`
✅ Utiliser le bon nombre de threads (CPU cores pour CPU-intensive)
✅ Utiliser `Future` pour récupérer les résultats
✅ Éviter les ressources partagées sans synchronisation

❌ Ne pas créer trop de threads
❌ Ne pas oublier de fermer l'executor
❌ Ne pas ignorer les exceptions
❌ Ne pas appeler `run()` directement
