package server;

import java.sql.*;

public class DatabaseManagerTest {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/clients_db", "root", "123456Az"
            );
            System.out.println("Connexion réussie !");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
