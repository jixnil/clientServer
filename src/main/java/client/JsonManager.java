package client;

import com.google.gson.Gson;

import java.io.*;
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
    public static void saveRequestAsJson(Request request) {
        try {
            File dir = new File("pending");
            if (!dir.exists()) dir.mkdirs();

            String filename = request.getAction() + "_" + request.getClient().getnClient() + "_" + System.currentTimeMillis() + ".json";
            File file = new File(dir, filename);

            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new Gson();
                gson.toJson(request, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Request readRequestFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, Request.class);
        }
    }


    public static String readJsonFromFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }

    public static void deleteJsonFile(String filename) throws IOException {
        Files.deleteIfExists(Paths.get(filename));
    }
}
