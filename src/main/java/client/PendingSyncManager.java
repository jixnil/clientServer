package client;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PendingSyncManager {

    public static void synchronizePendingRequests() {
        System.out.println("Tentative de synchronisation des fichiers JSON...");
        File dir = new File("pending/");
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;


        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Request req = gson.fromJson(reader, Request.class);
                String json = gson.toJson(req);

                boolean success = Boolean.parseBoolean(SocketClient.sendJsonToServer(json));
                if (success) {
                    JsonManager.deleteJsonFile(String.valueOf(file));  // supprime après envoi réussi
                    System.out.println("Synchronisé et supprimé : " + file.getName());
                } else {
                    System.out.println("Échec de synchronisation : " + file.getName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
