package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/clients_db";
    private static final String USER = "root";
    private static final String PASSWORD = "123456Az";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Insertion d'un client
    public static void insertClient(Client client) throws SQLException {
        String sql = "INSERT INTO client(nclient, nom, adresse, solde) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, client.getnClient());
            ps.setString(2, client.getNom());
            ps.setString(3, client.getAdresse());
            ps.setDouble(4, client.getSolde());
            ps.executeUpdate();
        }
    }

    // Mise à jour d'un client
    public static void updateClient(Client client) throws SQLException {
        String sql = "UPDATE client SET nom=?, adresse=?, solde=? WHERE nclient=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getNom());
            ps.setString(2, client.getAdresse());
            ps.setDouble(3, client.getSolde());
            ps.setInt(4, client.getnClient());
            ps.executeUpdate();
        }
    }

    // Suppression d'un client
    public static void deleteClient(int id) throws SQLException {
        String sql = "DELETE FROM client WHERE nclient=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Récupérer tous les clients
    public static List<Client> getAllClients() throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Client(
                        rs.getInt("nclient"),
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        rs.getDouble("solde")
                ));
            }
        }
        return list;
    }
}
