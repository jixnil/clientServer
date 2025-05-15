package server;

import client.JsonManager;
import com.google.gson.Gson;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import java.util.HashMap;
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
        PrintWriter writer = null;
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            writer = new PrintWriter(socket.getOutputStream(), true);
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

            String action = req.getAction();
            Client client = req.getClient();
            String responseJson;

            switch (action) {
                case "add":
                    try {
                        DatabaseManager.insertClient(client);
                        responseJson = "{\"status\":\"success\",\"message\":\"Client ajouté dans la base de donnees\"}";

                        // Supprimer le fichier JSON s’il existe après insertion
                        String jsonFile = "pending/client_" + client.getnClient() + ".json";
                        JsonManager.deleteJsonFile(jsonFile);
                    } catch (Exception ex) {
                        if (ex.getMessage().contains("duplicate")) {
                            responseJson = "{\"status\":\"error\",\"message\":\"Clé dupliquée détectée pour le client n°" + client.getnClient() + "\"}";
                            System.out.println("[ALERTE] Duplicate key détectée pour le client : " + client.getnClient());

                            // Supprimer tous les fichiers JSON avec ce client
                            File folder = new File(JsonManager.JSON_DIR);
                            for (File file : folder.listFiles()) {
                                if (file.getName().contains("client_" + client.getnClient())) {
                                    JsonManager.deleteJsonFile(file.getAbsolutePath());
                                    System.out.println("Fichier dupliqué supprimé : " + file.getName());
                                }
                            }
                        } else {
                            responseJson = "{\"status\":\"error\",\"message\":\"Erreur lors de l'ajout du client numero  client dupliqer\"}";
                        }
                    }

                    break;
                case "update":
                    DatabaseManager.updateClient(client);
                    responseJson = "{\"status\":\"success\",\"message\":\"Client modifié dans la base de donnees\"}";
                    break;
                case "delete":
                    DatabaseManager.deleteClient(client.getnClient());
                    responseJson = "{\"status\":\"success\",\"message\":\"Client supprimé de la base de donnees\"}";
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
            if (writer != null) {
                writer.println("{\"status\":\"error\",\"message\":\"Erreur interne serveur\"}");
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


