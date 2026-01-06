package com.examen.stock.dao;

import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDaoImpl implements IDao<Produit> {

    @Override
    public void create(Produit produit) {
        String sql = "INSERT INTO produits (nom, prix) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, produit.getNom());
            pstmt.setDouble(2, produit.getPrix());
            pstmt.executeUpdate();
            System.out.println("DAO: Produit créé -> " + produit.getNom());
        } catch (SQLException e) {
            System.err.println("Erreur DAO (insertion) : " + e.getMessage());
        }
    }

    @Override
    public List<Produit> readAll() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT nom, prix FROM produits";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                produits.add(new Produit(rs.getString("nom"), rs.getDouble("prix")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur DAO (lecture) : " + e.getMessage());
        }
        return produits;
    }

    @Override
    public Produit readByName(String nom) throws StockException {
        String sql = "SELECT nom, prix FROM produits WHERE nom = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Produit(rs.getString("nom"), rs.getDouble("prix"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur DAO (recherche) : " + e.getMessage());
        }
        throw new StockException("Produit '" + nom + "' introuvable via DAO.");
    }

    @Override
    public void update(Produit produit) throws StockException {
        String sql = "UPDATE produits SET prix = ? WHERE nom = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, produit.getPrix());
            pstmt.setString(2, produit.getNom());
            int rows = pstmt.executeUpdate();
            if (rows == 0)
                throw new StockException("Mise à jour DAO impossible : " + produit.getNom() + " inconnu.");
            System.out.println("DAO: Produit mis à jour -> " + produit.getNom());
        } catch (SQLException e) {
            System.err.println("Erreur DAO (maj) : " + e.getMessage());
        }
    }

    @Override
    public void delete(String nom) throws StockException {
        String sql = "DELETE FROM produits WHERE nom = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
                throw new StockException("Suppression DAO impossible : " + nom + " inconnu.");
            System.out.println("DAO: Produit supprimé -> " + nom);
        } catch (SQLException e) {
            System.err.println("Erreur DAO (suppression) : " + e.getMessage());
        }
    }
}
