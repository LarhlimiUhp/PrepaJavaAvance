package com.examen.stock.dao;

import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class ProduitDaoHibernate implements IDao<Produit> {

    @Override
    public void create(Produit produit) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(produit);
            transaction.commit();
            System.out.println("Hibernate: Produit persisté -> " + produit.getNom());
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Erreur Hibernate (insertion) : " + e.getMessage());
        }
    }

    @Override
    public List<Produit> readAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Produit", Produit.class).list();
        } catch (Exception e) {
            System.err.println("Erreur Hibernate (lecture) : " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public Produit readByName(String nom) throws StockException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Produit p = session.get(Produit.class, nom);
            if (p != null)
                return p;
        } catch (Exception e) {
            System.err.println("Erreur Hibernate (recherche) : " + e.getMessage());
        }
        throw new StockException("Produit '" + nom + "' introuvable via Hibernate.");
    }

    @Override
    public void update(Produit produit) throws StockException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(produit);
            transaction.commit();
            System.out.println("Hibernate: Produit mis à jour -> " + produit.getNom());
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Erreur Hibernate (maj) : " + e.getMessage());
            throw new StockException("Erreur lors de la mise à jour Hibernate.");
        }
    }

    @Override
    public void delete(String nom) throws StockException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Produit p = session.get(Produit.class, nom);
            if (p != null) {
                session.remove(p);
                System.out.println("Hibernate: Produit supprimé -> " + nom);
            } else {
                throw new StockException("Suppression Hibernate impossible : " + nom + " inconnu.");
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Erreur Hibernate (suppression) : " + e.getMessage());
            throw new StockException("Erreur lors de la suppression Hibernate.");
        }
    }
}
