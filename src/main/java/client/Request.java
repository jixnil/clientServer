package client;

public class Request {
    private String action;
    private Client client;

    public Request(String action, Client client) {
        this.action = action;
        this.client = client;
    }

    public String getAction() {
        return action;
    }

    public Client getClient() {
        return client;
    }
}
