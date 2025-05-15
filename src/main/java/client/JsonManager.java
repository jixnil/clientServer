package client;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.*;

public class JsonManager {

    public static final String JSON_DIR = "pending/";

    // Sauvegarde JSON simple
    public static void saveClientAsJson(Client client) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(client);

        Files.createDirectories(Paths.get(JSON_DIR));
        String filename = JSON_DIR + "client_" + client.getnClient() + ".json";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(json);
        }
    }

    // Sauvegarde une requête avec timestamp pour éviter les doublons
    public static void saveRequestAsJson(Request req) {
        try {
            File dir = new File(JSON_DIR);
            if (!dir.exists()) dir.mkdirs();

            String filename = JSON_DIR + req.getAction() + "_" + req.getClient().getnClient() + "_" + System.currentTimeMillis() + ".json";
            Gson gson = new Gson();
            try (FileWriter writer = new FileWriter(filename)) {
                gson.toJson(req, writer);
            }
            System.out.println("Requête enregistrée localement : " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lire un fichier JSON Request
    public static Request readRequestFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, Request.class);
        }
    }

    // Lire un fichier JSON brut
    public static String readJsonFromFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }

    // Supprimer un fichier spécifique
    public static void deleteJsonFile(String filename) {
        try {
            Files.deleteIfExists(Paths.get(filename));
            System.out.println("Fichier supprimé : " + filename);
        } catch (IOException e) {
            System.err.println("Erreur suppression fichier : " + filename);
            e.printStackTrace();
        }
    }

    // Supprimer les fichiers en doublon par client
    public static void deleteDuplicateJsonFiles(String nClient) {
        File dir = new File(JSON_DIR);
        if (!dir.exists()) return;

        File[] files = dir.listFiles((d, name) -> name.contains("client_" + nClient));
        if (files != null) {
            for (File file : files) {
                try {
                    Files.deleteIfExists(file.toPath());
                    System.out.println("Fichier dupliqué supprimé : " + file.getName());
                } catch (IOException e) {
                    System.err.println("Erreur suppression fichier : " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void cleanAllPendingJsonFiles() {
        File dir = new File(JSON_DIR);
        if (!dir.exists()) return;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    Files.deleteIfExists(file.toPath());
                    System.out.println("Fichier nettoyé : " + file.getName());
                } catch (IOException e) {
                    System.err.println("Erreur suppression fichier : " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}
