package com.examen.stock.repository;

import com.examen.stock.exception.StockException;
import com.examen.stock.model.Produit;
import com.examen.stock.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitJDBCRepository implements Repository<Produit> {

    @Override
    public void ajouter(Produit produit) {
        String sql = "INSERT INTO produits (nom, prix) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, produit.getNom());
            pstmt.setDouble(2, produit.getPrix());
            pstmt.executeUpdate();
            System.out.println("JDBC: Ajout de " + produit.getNom() + " dans la base de données.");
        } catch (SQLException e) {
            System.err.println("Erreur JDBC lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public List<Produit> listerTout() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT nom, prix FROM produits";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                produits.add(new Produit(rs.getString("nom"), rs.getDouble("prix")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur JDBC lors du listing : " + e.getMessage());
        }
        return produits;
    }

    @Override
    public Produit trouverParNom(String nom) throws StockException {
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
            System.err.println("Erreur JDBC lors de la recherche : " + e.getMessage());
        }
        throw new StockException("Produit '" + nom + "' introuvable dans la base de données !");
    }

    @Override
    public void mettreAJour(Produit produit) throws StockException {
        String sql = "UPDATE produits SET prix = ? WHERE nom = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, produit.getPrix());
            pstmt.setString(2, produit.getNom());
            int rows = pstmt.executeUpdate();
            if (rows == 0)
                throw new StockException("Mise à jour impossible : " + produit.getNom() + " introuvable.");
            System.out.println("JDBC: Mise à jour de " + produit.getNom());
        } catch (SQLException e) {
            System.err.println("Erreur JDBC lors de la maj : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(String nom) throws StockException {
        String sql = "DELETE FROM produits WHERE nom = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            int rows = pstmt.executeUpdate();
            if (rows == 0)
                throw new StockException("Suppression impossible : " + nom + " introuvable.");
            System.out.println("JDBC: Suppression de " + nom);
        } catch (SQLException e) {
            System.err.println("Erreur JDBC lors de la suppression : " + e.getMessage());
        }
    }
}
