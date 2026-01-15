# Exercice 1 Partie 1 : Solution Complète
![Uploading image.png…]()

## Analyse du Diagramme

D'après le diagramme de classes fourni, nous avons :
- Une classe mère **Personnel** avec les attributs : idE (String), salaire (double)
- Deux classes filles : **Gerant** et **Assistant**
- Une classe **Departement** liée à Gerant

**Relation identifiée** : Un Gérant peut gérer plusieurs départements (one-to-many), mais un département ne peut être géré que par un seul gérant (many-to-one du point de vue du département).

---

## 1. Implémentation des Classes

### Classe Personnel (Classe Mère)

```java
public class Personnel {
    private String idE;
    private double salaire;
    
    // Constructeur avec tous les attributs de la classe mère
    public Personnel(String idE, double salaire) {
        this.idE = idE;
        this.salaire = salaire;
    }
    
    // Getters et Setters
    public String getIdE() {
        return idE;
    }
    
    public void setIdE(String idE) {
        this.idE = idE;
    }
    
    public double getSalaire() {
        return salaire;
    }
    
    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }
    
    @Override
    public String toString() {
        return "Personnel [idE=" + idE + ", salaire=" + salaire + "]";
    }
}
```

---

### Classe Gerant (Hérite de Personnel)

```java
import java.util.ArrayList;
import java.util.List;

public class Gerant extends Personnel {
    private String specialite;
    
    // Relation One-to-Many : Un gérant peut gérer plusieurs départements
    private List<Departement> departements;
    
    // Constructeur
    public Gerant(String idE, double salaire, String specialite) {
        super(idE, salaire); // Appel du constructeur de la classe mère
        this.specialite = specialite;
        this.departements = new ArrayList<>();
    }
    
    // Getters et Setters
    public String getSpecialite() {
        return specialite;
    }
    
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
    
    public List<Departement> getDepartements() {
        return departements;
    }
    
    public void setDepartements(List<Departement> departements) {
        this.departements = departements;
    }
    
    // Méthode pour ajouter un département à la liste
    public void ajouterDepartement(Departement dep) {
        if (!this.departements.contains(dep)) {
            this.departements.add(dep);
            dep.setGerant(this); // Maintenir la cohérence bidirectionnelle
        }
    }
    
    // Méthode pour retirer un département de la liste
    public void retirerDepartement(Departement dep) {
        if (this.departements.contains(dep)) {
            this.departements.remove(dep);
            dep.setGerant(null); // Maintenir la cohérence bidirectionnelle
        }
    }
    
    @Override
    public String toString() {
        return "Gerant [idE=" + getIdE() + ", salaire=" + getSalaire() + 
               ", specialite=" + specialite + ", nbDepartements=" + departements.size() + "]";
    }
}
```

---

### Classe Assistant (Hérite de Personnel)

```java
public class Assistant extends Personnel {
    private int anneeDebu;
    
    // Constructeur
    public Assistant(String idE, double salaire, int anneeDebu) {
        super(idE, salaire); // Appel du constructeur de la classe mère
        this.anneeDebu = anneeDebu;
    }
    
    // Getters et Setters
    public int getAnneeDebu() {
        return anneeDebu;
    }
    
    public void setAnneeDebu(int anneeDebu) {
        this.anneeDebu = anneeDebu;
    }
    
    @Override
    public String toString() {
        return "Assistant [idE=" + getIdE() + ", salaire=" + getSalaire() + 
               ", anneeDebu=" + anneeDebu + "]";
    }
}
```

---

### Classe Departement

```java
public class Departement {
    private String nomD;
    
    // Relation Many-to-One : Un département est géré par un seul gérant
    private Gerant gerant;
    
    // Constructeur
    public Departement(String nomD) {
        this.nomD = nomD;
    }
    
    // Constructeur avec gérant
    public Departement(String nomD, Gerant gerant) {
        this.nomD = nomD;
        this.setGerant(gerant);
    }
    
    // Getters et Setters
    public String getNomD() {
        return nomD;
    }
    
    public void setNomD(String nomD) {
        this.nomD = nomD;
    }
    
    public Gerant getGerant() {
        return gerant;
    }
    
    public void setGerant(Gerant gerant) {
        // Retirer ce département de l'ancien gérant si nécessaire
        if (this.gerant != null && this.gerant != gerant) {
            this.gerant.getDepartements().remove(this);
        }
        
        this.gerant = gerant;
        
        // Ajouter ce département au nouveau gérant
        if (gerant != null && !gerant.getDepartements().contains(this)) {
            gerant.getDepartements().add(this);
        }
    }
    
    @Override
    public String toString() {
        String gerantInfo = (gerant != null) ? gerant.getIdE() : "Aucun";
        return "Departement [nomD=" + nomD + ", gerant=" + gerantInfo + "]";
    }
    
    // Override equals et hashCode pour utilisation dans Set
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Departement that = (Departement) obj;
        return nomD != null ? nomD.equals(that.nomD) : that.nomD == null;
    }
    
    @Override
    public int hashCode() {
        return nomD != null ? nomD.hashCode() : 0;
    }
}
```

---

## 2. Description des Relations dans le Code

Les relations sont clairement identifiables grâce aux attributs :

- **Gerant → Departement (One-to-Many)** : 
  - Dans la classe `Gerant`, l'attribut `List<Departement> departements` indique qu'un gérant peut gérer plusieurs départements
  
- **Departement → Gerant (Many-to-One)** : 
  - Dans la classe `Departement`, l'attribut `Gerant gerant` indique qu'un département est géré par un seul gérant

Les méthodes `ajouterDepartement()`, `retirerDepartement()` et `setGerant()` maintiennent la **cohérence bidirectionnelle** de la relation.

---

## 3. Création de 2 Objets de Chaque Classe

### Classe de Test

```java
public class TestExercice1 {
    public static void main(String[] args) {
        System.out.println("=== Création des objets ===\n");
        
        // Création de 2 Gérants
        Gerant gerant1 = new Gerant("G001", 15000.0, "Informatique");
        Gerant gerant2 = new Gerant("G002", 18000.0, "Finance");
        
        System.out.println(gerant1);
        System.out.println(gerant2);
        System.out.println();
        
        // Création de 2 Assistants
        Assistant assistant1 = new Assistant("A001", 8000.0, 2020);
        Assistant assistant2 = new Assistant("A002", 9500.0, 2019);
        
        System.out.println(assistant1);
        System.out.println(assistant2);
        System.out.println();
        
        // Création de 2 Départements
        Departement dept1 = new Departement("Développement", gerant1);
        Departement dept2 = new Departement("Comptabilité", gerant2);
        
        System.out.println(dept1);
        System.out.println(dept2);
        System.out.println();
        
        // Ajout d'un autre département au gérant1
        Departement dept3 = new Departement("Sécurité");
        gerant1.ajouterDepartement(dept3);
        
        System.out.println("=== Après ajout d'un département à gerant1 ===\n");
        System.out.println(gerant1);
        System.out.println(dept3);
        System.out.println();
        
        // Affichage des départements du gérant1
        System.out.println("=== Départements gérés par " + gerant1.getIdE() + " ===");
        for (Departement d : gerant1.getDepartements()) {
            System.out.println("  - " + d.getNomD());
        }
    }
}
```

---

## Output Attendu

```
=== Création des objets ===

Gerant [idE=G001, salaire=15000.0, specialite=Informatique, nbDepartements=1]
Gerant [idE=G002, salaire=18000.0, specialite=Finance, nbDepartements=1]

Assistant [idE=A001, salaire=8000.0, anneeDebu=2020]
Assistant [idE=A002, salaire=9500.0, anneeDebu=2019]

Departement [nomD=Développement, gerant=G001]
Departement [nomD=Comptabilité, gerant=G002]

=== Après ajout d'un département à gerant1 ===

Gerant [idE=G001, salaire=15000.0, specialite=Informatique, nbDepartements=2]
Departement [nomD=Sécurité, gerant=G001]

=== Départements gérés par G001 ===
  - Développement
  - Sécurité
```

---

## Points Clés de l'Implémentation

1. **Héritage** : Les classes `Gerant` et `Assistant` héritent de `Personnel` et utilisent `super()` pour appeler le constructeur de la classe mère

2. **Relation One-to-Many** : Implémentée avec `List<Departement>` dans `Gerant`

3. **Relation Many-to-One** : Implémentée avec `Gerant gerant` dans `Departement`

4. **Cohérence bidirectionnelle** : Les méthodes `setGerant()` et `ajouterDepartement()` maintiennent la cohérence des deux côtés de la relation

5. **equals() et hashCode()** : Redéfinis dans `Departement` pour permettre l'utilisation dans des collections comme `Set` (nécessaire pour la partie 2)
