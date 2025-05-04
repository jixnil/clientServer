package client;

public class Client {
    private int nClient;
    private String nom;
    private String adresse;
    private double solde;

    public Client(int nClient, String nom, String adresse, double solde) {
        this.nClient = nClient;
        this.nom = nom;
        this.adresse = adresse;
        this.solde = solde;
    }

    // Getters et setters
    public int getnClient() { return nClient; }
    public String getNom() { return nom; }
    public String getAdresse() { return adresse; }
    public double getSolde() { return solde; }
}
