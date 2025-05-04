package server;

import com.google.gson.Gson;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerApp {

    public static void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Serveur en écoute sur le port 12345...");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> handleClient(socket)).start(); // Un thread par client
        }
    }

    private static void handleClient(Socket socket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true) // auto-flush
        ) {
            Gson gson = new Gson();

            // Lire la requête JSON
            String json = reader.readLine();

            if (json == null || json.trim().isEmpty()) {
                System.out.println("Erreur : JSON vide ou null reçu.");
                writer.println("{\"status\":\"error\",\"message\":\"Requête vide\"}");
                return;
            }

            System.out.println("JSON reçu : " + json);
            Request req = gson.fromJson(json, Request.class);

            if (req == null || req.getAction() == null ||
                    (!"list".equals(req.getAction()) && req.getClient() == null)) {
                System.out.println("Erreur : Requête mal formée.");
                writer.println("{\"status\":\"error\",\"message\":\"Requête mal formée\"}");
                return;
            }

            String action = req.getAction();
            Client client = req.getClient();
            String responseJson;

            switch (action) {
                case "add":
                    DatabaseManager.insertClient(client);
                    responseJson = "{\"status\":\"success\",\"message\":\"Client ajouté\"}";
                    break;
                case "update":
                    DatabaseManager.updateClient(client);
                    responseJson = "{\"status\":\"success\",\"message\":\"Client modifié\"}";
                    break;
                case "delete":
                    DatabaseManager.deleteClient(client.getnClient());
                    responseJson = "{\"status\":\"success\",\"message\":\"Client supprimé\"}";
                    break;
                case "list":
                    List<Client> clients = DatabaseManager.getAllClients();
                    responseJson = gson.toJson(clients);
                    break;
                default:
                    responseJson = "{\"status\":\"error\",\"message\":\"Action inconnue\"}";
            }

            writer.println(responseJson);

        } catch (Exception e) {
            System.out.println("Erreur serveur : " + e.getMessage());
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("{\"status\":\"error\",\"message\":\"Erreur interne serveur\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        startServer();
    }
}
