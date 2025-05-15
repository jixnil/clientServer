package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

public class SocketClient {

    public static String sendJsonToServer(String json) {
        try (Socket socket = new Socket("127.0.0.1", 12345);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println(json);

            String response = reader.readLine();
            System.out.println("JSON envoyé : " + json);
            System.out.println("Réponse serveur : " + response);

            return response; // Peut être null si pas de réponse
        } catch (IOException e) {
            System.err.println("Erreur d'envoi : " + e.getMessage());
            return null;
        }
    }

    public static List<Client> requestClientList(String json) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", 12345);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println(json);

            String response = reader.readLine();
            System.out.println("Réponse du serveur : " + response);

            Type listType = new TypeToken<List<Client>>(){}.getType();
            return new Gson().fromJson(response, listType);
        }
    }
}
