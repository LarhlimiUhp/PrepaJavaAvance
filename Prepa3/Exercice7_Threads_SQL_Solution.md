# Exercice 7 : Threads avec SQL - Solution Complète

## Code Fourni

### Classe TaskExeQuery

```java
public class TaskExeQuery extends Thread {
    public Connection connection; 
    public String query;
    
    public TaskExeQuery(Connection connection, String query) {
        this.connection = connection;
        this.query = query;
    }
    
    public void run() {
        Operations.execQuery(query, connection);
    }
}
```

### Classe Operations

```java
public class Operations {
    public static void execQuery(String query, Connection con) {
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery(query);
            
            while (resultSet.next()) {
                String valCol = resultSet.getString("hometown");
                System.out.println(valCol);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

### Classe MainEx

```java
public class MainEx {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1/ourDataBase";
        String user = "root";
        String password = "";
        Connection connection = null;
        
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        TaskExeQuery task1 = new TaskExeQuery(connection, "SELECT * FROM membre");
        TaskExeQuery task2 = new TaskExeQuery(connection, "SELECT hm FROM membre");
        
        Thread th1 = new Thread(task1);
        Thread th2 = new Thread(task2);
        
        // Réponse à la question 2
        // ...
    }
}
```

---

## Question 1 : Modifier execQuery pour la rendre thread-safe

### a) En utilisant synchronized

### Solution avec synchronized

```java
public class Operations {
    
    // Méthode 1 : Synchroniser toute la méthode
    public static synchronized void execQuery(String query, Connection con) {
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery(query);
            
            while (resultSet.next()) {
                String valCol = resultSet.getString("hometown");
                System.out.println(valCol);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Fermer les ressources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

**Note** : Synchroniser une méthode statique verrouille sur la **classe** (Operations.class).

### Alternative : Synchroniser sur un objet spécifique

```java
public class Operations {
    // Objet de verrouillage partagé
    private static final Object lock = new Object();
    
    public static void execQuery(String query, Connection con) {
        Statement statement = null;
        ResultSet resultSet = null;
        
        // Synchroniser sur l'objet lock
        synchronized (lock) {
            try {
                statement = con.createStatement();
                resultSet = statement.executeQuery(query);
                
                while (resultSet.next()) {
                    String valCol = resultSet.getString("hometown");
                    System.out.println(valCol);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

### Alternative : Synchroniser sur la connexion

```java
public class Operations {
    
    public static void execQuery(String query, Connection con) {
        Statement statement = null;
        ResultSet resultSet = null;
        
        // Synchroniser sur la connexion
        synchronized (con) {
            try {
                statement = con.createStatement();
                resultSet = statement.executeQuery(query);
                
                while (resultSet.next()) {
                    String valCol = resultSet.getString("hometown");
                    System.out.println(valCol);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

---

### b) En utilisant Lock

### Solution avec ReentrantLock

```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Operations {
    // Lock partagé pour toutes les opérations
    private static final Lock lock = new ReentrantLock();
    
    public static void execQuery(String query, Connection con) {
        Statement statement = null;
        ResultSet resultSet = null;
        
        lock.lock(); // Acquérir le verrou
        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery(query);
            
            while (resultSet.next()) {
                String valCol = resultSet.getString("hometown");
                System.out.println(valCol);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Fermer les ressources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            lock.unlock(); // TOUJOURS libérer le verrou dans finally
        }
    }
}
```

### Solution avec ReadWriteLock (Optimisée)

Si on a des requêtes de lecture et d'écriture, on peut optimiser avec ReadWriteLock :

```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Operations {
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    // Méthode pour les requêtes SELECT (lecture)
    public static void execQueryRead(String query, Connection con) {
        Statement statement = null;
        ResultSet resultSet = null;
        
        rwLock.readLock().lock(); // Plusieurs threads peuvent lire en même temps
        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery(query);
            
            while (resultSet.next()) {
                String valCol = resultSet.getString("hometown");
                System.out.println(valCol);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            rwLock.readLock().unlock();
        }
    }
    
    // Méthode pour les requêtes INSERT/UPDATE/DELETE (écriture)
    public static void execQueryWrite(String query, Connection con) {
        Statement statement = null;
        
        rwLock.writeLock().lock(); // Un seul thread peut écrire
        try {
            statement = con.createStatement();
            int rowsAffected = statement.executeUpdate(query);
            System.out.println("Lignes affectées : " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            rwLock.writeLock().unlock();
        }
    }
    
    // Méthode générique qui détecte le type de requête
    public static void execQuery(String query, Connection con) {
        String queryUpper = query.trim().toUpperCase();
        
        if (queryUpper.startsWith("SELECT")) {
            execQueryRead(query, con);
        } else {
            execQueryWrite(query, con);
        }
    }
}
```

### Comparaison synchronized vs Lock

| Aspect | synchronized | Lock |
|--------|--------------|------|
| **Simplicité** | ✅ Plus simple | ❌ Plus verbeux |
| **Libération automatique** | ✅ Oui | ❌ Non (finally obligatoire) |
| **Timeout** | ❌ Non | ✅ tryLock(timeout) |
| **Interruptibilité** | ❌ Non | ✅ lockInterruptibly() |
| **Équité** | ❌ Non garanti | ✅ Configurable |
| **Performance** | Similaire | Similaire |
| **Flexibilité** | ❌ Limitée | ✅ Élevée |

---

## Question 2 : Exécution des threads avec th2 attendant th1

### Réponse

Dans la classe MainEx, voici les instructions pour exécuter th1 puis th2, avec th2 attendant la fin de th1 :

```java
public class MainEx {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1/ourDataBase";
        String user = "root";
        String password = "";
        Connection connection = null;
        
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        TaskExeQuery task1 = new TaskExeQuery(connection, "SELECT * FROM membre");
        TaskExeQuery task2 = new TaskExeQuery(connection, "SELECT hm FROM membre");
        
        Thread th1 = new Thread(task1);
        Thread th2 = new Thread(task2);
        
        // ========== Réponse à la question 2 ==========
        
        try {
            // Démarrer th1
            th1.start();
            System.out.println("Thread 1 démarré");
            
            // Attendre que th1 se termine
            th1.join();
            System.out.println("Thread 1 terminé");
            
            // Démarrer th2 seulement après la fin de th1
            th2.start();
            System.out.println("Thread 2 démarré");
            
            // Attendre que th2 se termine
            th2.join();
            System.out.println("Thread 2 terminé");
            
            System.out.println("Tous les threads sont terminés");
            
        } catch (InterruptedException e) {
            System.err.println("Thread interrompu : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermer la connexion
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Connexion fermée");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

### Explication Détaillée

```java
// 1. Démarrer th1
th1.start();
// → th1 commence à s'exécuter dans un thread séparé
// → Le thread principal continue immédiatement

// 2. Attendre que th1 se termine
th1.join();
// → Le thread principal ATTEND (bloquant)
// → Continue seulement quand th1 est terminé

// 3. Démarrer th2
th2.start();
// → th2 commence maintenant que th1 est terminé
// → Garantit l'ordre d'exécution

// 4. Attendre que th2 se termine
th2.join();
// → Le thread principal attend la fin de th2
// → Garantit que tout est terminé avant de fermer la connexion
```

### Chronologie d'Exécution

```
Timeline:
─────────────────────────────────────────────────────────────►

Main Thread:
  │
  ├── th1.start() ───────────────────────────────►
  │                                               │
  ├── th1.join() ────────────────────────────────┤
  │                   (ATTEND)                    │
  │                                               │
  │◄──────────────────────────────────────────────┘
  │
  ├── th2.start() ───────────────────────────────►
  │                                               │
  ├── th2.join() ────────────────────────────────┤
  │                   (ATTEND)                    │
  │                                               │
  │◄──────────────────────────────────────────────┘
  │
  └── Fin


Thread 1:
          │
          ├── Exécute requête 1
          │
          └── Termine


Thread 2:
                                                  │
                                                  ├── Exécute requête 2
                                                  │
                                                  └── Termine
```

### Alternative : Avec Messages de Debug

```java
public class MainEx {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1/ourDataBase";
        String user = "root";
        String password = "";
        Connection connection = null;
        
        try {
            System.out.println("=== Connexion à la base de données ===");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion établie avec succès\n");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        TaskExeQuery task1 = new TaskExeQuery(connection, "SELECT * FROM membre");
        TaskExeQuery task2 = new TaskExeQuery(connection, "SELECT hometown FROM membre");
        
        Thread th1 = new Thread(task1, "Thread-Query-1");
        Thread th2 = new Thread(task2, "Thread-Query-2");
        
        try {
            // Exécution séquentielle : th1 puis th2
            System.out.println("=== Exécution de la requête 1 ===");
            th1.start();
            
            th1.join(); // Attendre th1
            System.out.println("Requête 1 terminée\n");
            
            System.out.println("=== Exécution de la requête 2 ===");
            th2.start();
            
            th2.join(); // Attendre th2
            System.out.println("Requête 2 terminée\n");
            
            System.out.println("=== Toutes les requêtes sont terminées ===");
            
        } catch (InterruptedException e) {
            System.err.println("Thread interrompu : " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Connexion fermée");
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture : " + e.getMessage());
                }
            }
        }
    }
}
```

### Alternative : Avec Gestion d'Erreurs Avancée

```java
public class MainEx {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1/ourDataBase";
        String user = "root";
        String password = "";
        Connection connection = null;
        
        try {
            connection = DriverManager.getConnection(url, user, password);
            
            TaskExeQuery task1 = new TaskExeQuery(connection, "SELECT * FROM membre");
            TaskExeQuery task2 = new TaskExeQuery(connection, "SELECT hometown FROM membre");
            
            Thread th1 = new Thread(task1, "Thread-1");
            Thread th2 = new Thread(task2, "Thread-2");
            
            // Démarrer et attendre th1
            th1.start();
            th1.join();
            
            // Vérifier si th1 s'est terminé normalement
            if (th1.isAlive()) {
                System.err.println("Erreur : Thread 1 encore actif");
                return;
            }
            
            // Démarrer et attendre th2
            th2.start();
            th2.join();
            
            // Vérifier si th2 s'est terminé normalement
            if (th2.isAlive()) {
                System.err.println("Erreur : Thread 2 encore actif");
                return;
            }
            
            System.out.println("Succès : Tous les threads terminés");
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrompu : " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

---

## Question 3 : Utilité de DriverManager et jdbc:mysql://

### a) DriverManager

#### Définition

`DriverManager` est une classe Java qui **gère les drivers JDBC** et établit les **connexions aux bases de données**.

#### Rôle de DriverManager

1. **Enregistrer les drivers JDBC**
2. **Établir les connexions** avec les bases de données
3. **Gérer plusieurs drivers** simultanément
4. **Sélectionner le driver approprié** selon l'URL

#### Méthodes Principales

##### 1. `getConnection()` - Établir une connexion

```java
// Signature
public static Connection getConnection(String url, String user, String password) 
    throws SQLException

// Utilisation
Connection conn = DriverManager.getConnection(
    "jdbc:mysql://127.0.0.1:3306/ourDataBase",
    "root",
    ""
);
```

##### 2. `registerDriver()` - Enregistrer un driver

```java
// Enregistrement manuel (rarement nécessaire avec JDBC 4.0+)
Driver driver = new com.mysql.cj.jdbc.Driver();
DriverManager.registerDriver(driver);
```

##### 3. `deregisterDriver()` - Désenregistrer un driver

```java
DriverManager.deregisterDriver(driver);
```

##### 4. `getDrivers()` - Lister les drivers

```java
Enumeration<Driver> drivers = DriverManager.getDrivers();
while (drivers.hasMoreElements()) {
    Driver driver = drivers.nextElement();
    System.out.println(driver.getClass().getName());
}
```

#### Exemple Complet d'Utilisation

```java
import java.sql.*;

public class ExempleDriverManager {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/mabase";
        String user = "root";
        String password = "monmotdepasse";
        
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            // 1. DriverManager établit la connexion
            System.out.println("Connexion à la base de données...");
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion réussie !");
            
            // 2. Créer un statement
            statement = connection.createStatement();
            
            // 3. Exécuter une requête
            resultSet = statement.executeQuery("SELECT * FROM users");
            
            // 4. Traiter les résultats
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println("ID: " + id + ", Name: " + name);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. Fermer les ressources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
                System.out.println("Ressources fermées");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

#### Fonctionnement Interne

```
┌─────────────────────────────────────────────────┐
│            DriverManager                        │
├─────────────────────────────────────────────────┤
│  Drivers enregistrés:                           │
│  - com.mysql.cj.jdbc.Driver                     │
│  - org.postgresql.Driver                        │
│  - oracle.jdbc.OracleDriver                     │
│  - ...                                          │
└─────────────────────────────────────────────────┘
           │
           │ getConnection(url, user, pwd)
           ▼
┌─────────────────────────────────────────────────┐
│  1. Parse l'URL (jdbc:mysql://...)              │
│  2. Trouve le driver MySQL                      │
│  3. Appelle driver.connect(url, props)          │
│  4. Retourne la Connection                      │
└─────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────┐
│            Connection établie                   │
│         (vers la base MySQL)                    │
└─────────────────────────────────────────────────┘
```

#### Avantages de DriverManager

✅ **Simplicité** : Une seule méthode pour se connecter  
✅ **Indépendance** : Changez de base en changeant l'URL  
✅ **Gestion automatique** : Détecte automatiquement les drivers (JDBC 4.0+)  
✅ **Multi-drivers** : Supporte plusieurs types de bases simultanément

#### Limitations

❌ **Connection Pool** : Ne gère pas de pool de connexions  
❌ **Performance** : Crée une nouvelle connexion à chaque appel  
❌ **Enterprise** : Préférer DataSource pour applications d'entreprise

#### Alternative : DataSource (Production)

```java
// Pour applications d'entreprise
import javax.sql.DataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

MysqlDataSource dataSource = new MysqlDataSource();
dataSource.setURL("jdbc:mysql://localhost:3306/mabase");
dataSource.setUser("root");
dataSource.setPassword("motdepasse");

Connection conn = dataSource.getConnection();
```

---

### b) jdbc:mysql://

#### Définition

`jdbc:mysql://` est une **URL JDBC** qui spécifie le protocole et les paramètres de connexion à une base de données MySQL.

#### Structure d'une URL JDBC

```
jdbc:<sgbd>://<hôte>:<port>/<base_de_données>?<paramètres>
│    │       │       │      │    │              │
│    │       │       │      │    │              └─ Paramètres optionnels
│    │       │       │      │    └──────────────── Nom de la base
│    │       │       │      └───────────────────── Port (optionnel)
│    │       │       └──────────────────────────── Hôte/IP
│    │       └──────────────────────────────────── Protocole de connexion
│    └──────────────────────────────────────────── Type de SGBD
└───────────────────────────────────────────────── Protocole JDBC
```

#### Exemples d'URLs JDBC

##### MySQL

```java
// Connexion locale
"jdbc:mysql://localhost:3306/mabase"
"jdbc:mysql://127.0.0.1:3306/mabase"

// Connexion distante
"jdbc:mysql://192.168.1.100:3306/mabase"
"jdbc:mysql://db.monsite.com:3306/mabase"

// Avec paramètres
"jdbc:mysql://localhost:3306/mabase?useSSL=false&serverTimezone=UTC"
"jdbc:mysql://localhost:3306/mabase?useUnicode=true&characterEncoding=UTF-8"
```

##### PostgreSQL

```java
"jdbc:postgresql://localhost:5432/mabase"
```

##### Oracle

```java
"jdbc:oracle:thin:@localhost:1521:orcl"
```

##### SQL Server

```java
"jdbc:sqlserver://localhost:1433;databaseName=mabase"
```

##### SQLite

```java
"jdbc:sqlite:C:/data/mabase.db"
```

#### Composants de l'URL MySQL

```java
String url = "jdbc:mysql://127.0.0.1:3306/ourDataBase";
//            │    │       │          │     │
//            │    │       │          │     └─ Nom de la base
//            │    │       │          └─────── Port MySQL (3306 par défaut)
//            │    │       └────────────────── Adresse IP ou nom d'hôte
//            │    └────────────────────────── Type de base (mysql)
//            └─────────────────────────────── Protocole JDBC
```

#### Décomposition Détaillée

| Partie | Valeur | Signification |
|--------|--------|---------------|
| **jdbc:** | Fixe | Protocole JDBC |
| **mysql:** | Nom du SGBD | Type de base de données |
| **//127.0.0.1** | Hôte | Adresse du serveur (localhost) |
| **:3306** | Port | Port MySQL (optionnel, 3306 par défaut) |
| **/ourDataBase** | Base | Nom de la base de données |

#### Paramètres Courants

```java
// Sans SSL (développement)
"jdbc:mysql://localhost:3306/mabase?useSSL=false"

// Timezone
"jdbc:mysql://localhost:3306/mabase?serverTimezone=UTC"

// Encodage
"jdbc:mysql://localhost:3306/mabase?characterEncoding=UTF-8"

// Plusieurs paramètres (séparés par &)
"jdbc:mysql://localhost:3306/mabase?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8"

// Avec timeout de connexion
"jdbc:mysql://localhost:3306/mabase?connectTimeout=10000"

// Mode de reconnexion automatique
"jdbc:mysql://localhost:3306/mabase?autoReconnect=true"
```

#### Exemples Complets avec Explications

##### Exemple 1 : Connexion Simple

```java
public class ConnexionSimple {
    public static void main(String[] args) {
        // URL JDBC complète
        String url = "jdbc:mysql://localhost:3306/entreprise";
        String user = "admin";
        String password = "secret123";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connecté à : " + url);
            System.out.println("Base de données : entreprise");
            System.out.println("Port : 3306");
            
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }
}
```

##### Exemple 2 : Connexion avec Paramètres

```java
public class ConnexionParametree {
    public static void main(String[] args) {
        // Construire l'URL avec paramètres
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:mysql://");
        urlBuilder.append("localhost:3306/");
        urlBuilder.append("mabase");
        urlBuilder.append("?useSSL=false");
        urlBuilder.append("&serverTimezone=UTC");
        urlBuilder.append("&allowPublicKeyRetrieval=true");
        
        String url = urlBuilder.toString();
        System.out.println("URL complète : " + url);
        
        try (Connection conn = DriverManager.getConnection(url, "root", "")) {
            System.out.println("Connexion établie avec succès");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

##### Exemple 3 : Connexion Dynamique

```java
public class ConnexionDynamique {
    public static Connection creerConnexion(String hote, int port, 
                                           String base, String user, String pwd) {
        String url = String.format("jdbc:mysql://%s:%d/%s", hote, port, base);
        
        try {
            Connection conn = DriverManager.getConnection(url, user, pwd);
            System.out.println("Connecté à : " + url);
            return conn;
            
        } catch (SQLException e) {
            System.err.println("Échec de connexion à : " + url);
            e.printStackTrace();
            return null;
        }
    }
    
    public static void main(String[] args) {
        // Connexion locale
        Connection conn1 = creerConnexion("localhost", 3306, "db1", "root", "");
        
        // Connexion distante
        Connection conn2 = creerConnexion("192.168.1.50", 3306, "db2", "admin", "pass");
        
        // Fermer les connexions
        try {
            if (conn1 != null) conn1.close();
            if (conn2 != null) conn2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

#### Variations d'Adresses

```java
// Localhost
"jdbc:mysql://localhost:3306/mabase"
"jdbc:mysql://127.0.0.1:3306/mabase"
"jdbc:mysql://[::1]:3306/mabase"  // IPv6

// Réseau local
"jdbc:mysql://192.168.1.100:3306/mabase"
"jdbc:mysql://pc-serveur:3306/mabase"

// Internet
"jdbc:mysql://db.monsite.com:3306/mabase"
"jdbc:mysql://123.45.67.89:3306/mabase"

// Port personnalisé
"jdbc:mysql://localhost:3307/mabase"  // MySQL sur port 3307

// Sans port (utilise 3306 par défaut)
"jdbc:mysql://localhost/mabase"
```

#### Erreurs Courantes

##### 1. Port Incorrect

```java
// ❌ ERREUR : Port 3360 au lieu de 3306
"jdbc:mysql://localhost:3360/mabase"
// Exception : Communications link failure
```

##### 2. Base de Données Inexistante

```java
// ❌ ERREUR : Base "mabas" n'existe pas
"jdbc:mysql://localhost:3306/mabas"
// SQLException: Unknown database 'mabas'
```

##### 3. Hôte Inaccessible

```java
// ❌ ERREUR : Serveur inaccessible
"jdbc:mysql://192.168.1.999:3306/mabase"
// SQLException: Communications link failure
```

##### 4. Protocole Incorrect

```java
// ❌ ERREUR : jdbc au lieu de jdbc:mysql
"jdbc://localhost:3306/mabase"
// SQLException: No suitable driver found
```

#### Résumé des Protocoles JDBC

| SGBD | Protocole JDBC | Port par défaut |
|------|----------------|-----------------|
| MySQL | jdbc:mysql:// | 3306 |
| PostgreSQL | jdbc:postgresql:// | 5432 |
| Oracle | jdbc:oracle:thin:@ | 1521 |
| SQL Server | jdbc:sqlserver:// | 1433 |
| SQLite | jdbc:sqlite: | N/A (fichier) |
| H2 | jdbc:h2: | N/A (embedded) |
| MariaDB | jdbc:mariadb:// | 3306 |

---

## Code Complet Final - Toutes les Questions

### Classe TaskExeQuery (Améliorée)

```java
import java.sql.*;

public class TaskExeQuery extends Thread {
    private Connection connection;
    private String query;
    
    public TaskExeQuery(Connection connection, String query) {
        this.connection = connection;
        this.query = query;
    }
    
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " - Début exécution");
        Operations.execQuery(query, connection);
        System.out.println(Thread.currentThread().getName() + " - Fin exécution");
    }
    
    public String getQuery() {
        return query;
    }
}
```

### Classe Operations (Version synchronized)

```java
import java.sql.*;

public class Operations {
    
    // Version avec synchronized (Question 1.a)
    public static synchronized void execQuery(String query, Connection con) {
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            System.out.println("Exécution de la requête : " + query);
            
            statement = con.createStatement();
            resultSet = statement.executeQuery(query);
            
            // Afficher les résultats
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = resultSet.getString(i);
                    System.out.println(columnName + ": " + value);
                }
                System.out.println("---");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermer les ressources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }
}
```

### Classe Operations (Version Lock)

```java
import java.sql.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Operations {
    private static final Lock lock = new ReentrantLock();
    
    // Version avec Lock (Question 1.b)
    public static void execQuery(String query, Connection con) {
        Statement statement = null;
        ResultSet resultSet = null;
        
        lock.lock(); // Acquérir le verrou
        try {
            System.out.println("Exécution de la requête : " + query);
            
            statement = con.createStatement();
            resultSet = statement.executeQuery(query);
            
            // Afficher les résultats
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = resultSet.getString(i);
                    System.out.println(columnName + ": " + value);
                }
                System.out.println("---");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermer les ressources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture : " + e.getMessage());
            }
            
            lock.unlock(); // Libérer le verrou
        }
    }
}
```

### Classe MainEx (Version Complète)

```java
import java.sql.*;

public class MainEx {
    public static void main(String[] args) {
        // Paramètres de connexion
        String url = "jdbc:mysql://127.0.0.1:3306/ourDataBase";
        String user = "root";
        String password = "";
        Connection connection = null;
        
        try {
            // Établir la connexion
            System.out.println("=== Connexion à la base de données ===");
            System.out.println("URL : " + url);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion établie avec succès\n");
            
            // Créer les tâches
            TaskExeQuery task1 = new TaskExeQuery(
                connection, 
                "SELECT * FROM membre"
            );
            TaskExeQuery task2 = new TaskExeQuery(
                connection, 
                "SELECT hometown FROM membre"
            );
            
            // Créer les threads
            Thread th1 = new Thread(task1, "Thread-Query-1");
            Thread th2 = new Thread(task2, "Thread-Query-2");
            
            // ========== Réponse à la question 2 ==========
            
            System.out.println("=== Démarrage de l'exécution séquentielle ===\n");
            
            // Exécuter th1
            System.out.println("Démarrage du Thread 1...");
            th1.start();
            
            // Attendre que th1 se termine
            th1.join();
            System.out.println("Thread 1 terminé\n");
            
            // Exécuter th2 après th1
            System.out.println("Démarrage du Thread 2...");
            th2.start();
            
            // Attendre que th2 se termine
            th2.join();
            System.out.println("Thread 2 terminé\n");
            
            System.out.println("=== Toutes les requêtes sont terminées ===");
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrompu : " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            // Fermer la connexion
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("\nConnexion fermée");
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture : " + e.getMessage());
                }
            }
        }
    }
}
```

### Programme de Test Complet

```java
import java.sql.*;
import java.util.concurrent.locks.*;

public class TestCompletExercice7 {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   Test Complet Exercice 7 - Threads SQL   ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        // Test 1 : Version synchronized
        System.out.println("=== Test 1 : Version synchronized ===");
        testerVersionSynchronized();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test 2 : Version Lock
        System.out.println("=== Test 2 : Version Lock ===");
        testerVersionLock();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test 3 : Exécution séquentielle
        System.out.println("=== Test 3 : Exécution séquentielle ===");
        testerExecutionSequentielle();
    }
    
    private static void testerVersionSynchronized() {
        // Implémenter le test avec synchronized
        System.out.println("Test de la version synchronized...");
        // Code du test...
    }
    
    private static void testerVersionLock() {
        // Implémenter le test avec Lock
        System.out.println("Test de la version Lock...");
        // Code du test...
    }
    
    private static void testerExecutionSequentielle() {
        // Implémenter le test d'exécution séquentielle
        System.out.println("Test de l'exécution séquentielle avec join()...");
        // Code du test...
    }
}
```

---

## Résumé des Réponses

### Question 1 : Rendre execQuery thread-safe

**a) synchronized** :
```java
public static synchronized void execQuery(String query, Connection con)
```

**b) Lock** :
```java
private static final Lock lock = new ReentrantLock();
public static void execQuery(...) {
    lock.lock();
    try { /* ... */ }
    finally { lock.unlock(); }
}
```

### Question 2 : Exécution séquentielle

```java
th1.start();
th1.join();  // Attendre th1

th2.start();
th2.join();  // Attendre th2
```

### Question 3 : Utilité

**DriverManager** : Gère les drivers JDBC et établit les connexions

**jdbc:mysql://** : URL spécifiant le protocole et les paramètres de connexion MySQL

---

## Points Clés à Retenir

1. **Synchronisation** : Nécessaire pour éviter les conflits d'accès concurrent
2. **join()** : Permet d'attendre la fin d'un thread avant de continuer
3. **DriverManager** : Point d'entrée pour toutes les connexions JDBC
4. **URL JDBC** : Format standardisé pour spécifier les paramètres de connexion
5. **Fermeture des ressources** : Toujours utiliser try-finally ou try-with-resources
