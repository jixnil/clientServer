package server;

import com.google.gson.Gson;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerApp {

    // Map pour suivre la fréquence des erreurs
    private static Map<String, Long> errorTimestamps = new HashMap<>();

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
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            Gson gson = new Gson();

            // Lire la requête JSON
            String json = reader.readLine();

            if (json == null || json.trim().isEmpty()) {
                handleError("Erreur : JSON vide ou null reçu.", writer);
                return;
            }

            System.out.println("JSON reçu : " + json);
            Request req = gson.fromJson(json, Request.class);

            if (req == null || req.getAction() == null ||
                    (!"list".equals(req.getAction()) && req.getClient() == null)) {
                handleError("Erreur : Requête mal formée.", writer);
                return;
            }

            // Processus principal pour les actions
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

    private static void handleError(String errorMessage, PrintWriter writer) {
        long currentTime = System.currentTimeMillis();
        String key = errorMessage; // Clé basée sur le message d'erreur

        // Vérifier si l'erreur a été enregistrée récemment
        if (!errorTimestamps.containsKey(key) || currentTime - errorTimestamps.get(key) > 300000) { // 5 minutes
            System.out.println(errorMessage);
            errorTimestamps.put(key, currentTime);
        }
        writer.println("{\"status\":\"error\",\"message\":\"" + errorMessage + "\"}");
    }

    public static void main(String[] args) throws Exception {
        startServer(); // Démarre le serveur
    }
}


