package client;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PendingSyncManager {

    public static void synchronizePendingRequests() {
        System.out.println("Tentative de synchronisation des fichiers JSON...");

        File dir = new File(JsonManager.JSON_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Dossier 'pending/' introuvable.");
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.out.println("Aucun fichier JSON à synchroniser.");
            return;
        }

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Request req = gson.fromJson(reader, Request.class);
                String json = gson.toJson(req);

                String response = SocketClient.sendJsonToServer(json);

                if (response != null) {
                    try {
                        Client.ResponseWrapper wrapper = gson.fromJson(response, Client.ResponseWrapper.class);
                        if ("success".equalsIgnoreCase(wrapper.status)) {
                            JsonManager.deleteJsonFile(file.getPath());
                            System.out.println("Synchronisé et supprimé : " + file.getName());
                        } else {
                            System.err.println("Échec serveur : " + wrapper.message + " - " + file.getName());
                        }
                    } catch (Exception parseError) {
                        System.err.println("Erreur de parsing JSON pour : " + file.getName());
                        parseError.printStackTrace();
                    }
                } else {
                    System.err.println("Aucune réponse du serveur pour : " + file.getName());
                }

            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture de : " + file.getName());
                e.printStackTrace();
            } catch (Exception ex) {
                System.err.println("Fichier invalide : " + file.getName());
                ex.printStackTrace();
            }
        }
    }
}
