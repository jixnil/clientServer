package client;

import com.google.gson.Gson;

import java.io.File;

public class PendingSyncManager {

    public static void synchronizePendingRequests() {
        File dir = new File("pending");
        if (!dir.exists()) return;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try {
                Request req = JsonManager.readRequestFromFile(file);
                boolean success = SocketClient.sendJsonToServer(new Gson().toJson(req));
                if (success) {
                    file.delete();
                    System.out.println("✅ Synchro OK : " + file.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
