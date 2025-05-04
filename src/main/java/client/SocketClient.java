package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

public class SocketClient {

    public static boolean sendJsonToServer(String json) {
        try (Socket socket = new Socket("127.0.0.1", 12345);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Envoyer le JSON avec println (ajoute \n automatiquement)
            writer.println(json);

            // Lire la réponse du serveur
            String response = reader.readLine();
            System.out.println("Réponse serveur : " + response);
            System.out.println("JSON envoyé : " + json);

            // Vérification de succès dans la réponse
            return response != null && (response.contains("succès") || response.contains("success") || response.contains("ok") || response.contains("reçu"));
        } catch (IOException e) {
            System.err.println("Erreur d'envoi : " + e.getMessage());
            return false;
        }
    }

    public static List<Client> requestClientList(String json) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", 12345);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Envoyer la requête JSON
            writer.println(json);

            // Lire la réponse JSON (la liste de clients)
            String response = reader.readLine(); // assume one-line JSON response
            System.out.println("Réponse du serveur : " + response);

            Type listType = new TypeToken<List<Client>>(){}.getType();
            return new Gson().fromJson(response, listType);
        }
    }
}
