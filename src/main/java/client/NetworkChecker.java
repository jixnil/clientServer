package client;

import java.net.Socket;

public class NetworkChecker {
    public static boolean isServerAvailable() {
        try (Socket socket = new Socket("127.0.0.1", 12345)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
