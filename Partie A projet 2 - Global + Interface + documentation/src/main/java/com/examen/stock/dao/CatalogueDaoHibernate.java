package com.examen.stock.dao;

import com.examen.stock.exception.StockException;
import com.examen.stock.model.Catalogue;
import com.examen.stock.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class CatalogueDaoHibernate implements IDao<Catalogue> {

    @Override
    public void create(Catalogue catalogue) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(catalogue);
            transaction.commit();
            System.out.println("Hibernate: Catalogue persisté -> " + catalogue.getNom());
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Erreur Hibernate Catalogue (insertion) : " + e.getMessage());
        }
    }

    @Override
    public List<Catalogue> readAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Catalogue", Catalogue.class).list();
        } catch (Exception e) {
            System.err.println("Erreur Hibernate Catalogue (lecture) : " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public Catalogue readByName(String nom) throws StockException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Ici, on cherche par le nom. Dans le mapping XML, l'ID est 'id' (native).
            // On utilise donc une requête HQL.
            Catalogue c = session.createQuery("from Catalogue where nom = :nom", Catalogue.class)
                    .setParameter("nom", nom)
                    .uniqueResult();
            if (c != null)
                return c;
        } catch (Exception e) {
            System.err.println("Erreur Hibernate Catalogue (recherche) : " + e.getMessage());
        }
        throw new StockException("Catalogue '" + nom + "' introuvable.");
    }

    @Override
    public void update(Catalogue catalogue) throws StockException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(catalogue);
            transaction.commit();
            System.out.println("Hibernate: Catalogue mis à jour -> " + catalogue.getNom());
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw new StockException("Erreur lors de la mise à jour du catalogue.");
        }
    }

    @Override
    public void delete(String nom) throws StockException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Catalogue c = readByName(nom);
            if (c != null) {
                session.remove(c);
                System.out.println("Hibernate: Catalogue supprimé -> " + nom);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw new StockException("Erreur lors de la suppression du catalogue.");
        }
    }
}
