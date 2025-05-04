package client;

import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;

public class JsonManager {

    public static final String JSON_DIR = "pending/";

    public static void saveClientAsJson(Client client) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(client);

        Files.createDirectories(Paths.get(JSON_DIR));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(JSON_DIR + "client_" + client.getnClient() + ".json"))) {
            writer.write(json);
        }
    }

    public static String readJsonFromFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }

    public static void deleteJsonFile(String filename) throws IOException {
        Files.deleteIfExists(Paths.get(filename));
    }
}
