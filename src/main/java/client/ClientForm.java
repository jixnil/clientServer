package client;

import com.google.gson.Gson;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientForm extends JFrame {

    private JTextField txtId, txtNom, txtAdresse, txtSolde;
    private JLabel lblStatut;
    private JTable table;
    private ClientTableModel tableModel;
    private final List<Client> clients = new ArrayList<>();

    public ClientForm() {
        setTitle("Gestion des Clients");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        initUI();
        chargerClients();
        initTimer();

        setVisible(true);
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // Partie haute : formulaire + boutons
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(initFormPanel(), BorderLayout.CENTER);
        topPanel.add(initButtonPanel(), BorderLayout.SOUTH);

        contentPane.add(topPanel, BorderLayout.NORTH);

        // Table des clients
        tableModel = new ClientTableModel(clients);
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setAutoCreateRowSorter(true);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Client c = tableModel.getClientAt(table.convertRowIndexToModel(row));
                remplirChamps(c);
            }
        });

        contentPane.add(new JScrollPane(table), BorderLayout.CENTER);

        // Statut
        lblStatut = new JLabel("Statut : ");
        lblStatut.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentPane.add(lblStatut, BorderLayout.SOUTH);
    }

    private JPanel initFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Informations Client"));

        txtId = new JTextField();
        txtNom = new JTextField();
        txtAdresse = new JTextField();
        txtSolde = new JTextField();

        formPanel.add(new JLabel("Numéro du client:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Nom :"));
        formPanel.add(txtNom);
        formPanel.add(new JLabel("Adresse :"));
        formPanel.add(txtAdresse);
        formPanel.add(new JLabel("Solde :"));
        formPanel.add(txtSolde);

        return formPanel;
    }

    private JPanel initButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Actions"));

        panel.add(createButton("Ajouter", "add"));
        panel.add(createButton("Modifier", "update"));
        panel.add(createButton("Supprimer", "delete"));
        panel.add(createButton("Rafraîchir", "list"));

        return panel;
    }

    private JButton createButton(String text, String action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.addActionListener(e -> {
            if ("list".equals(action)) {
                chargerClients();
            } else {
                envoyerClient(action);
            }
        });
        return btn;
    }

    private void remplirChamps(Client c) {
        txtId.setText(String.valueOf(c.getnClient()));
        txtNom.setText(c.getNom());
        txtAdresse.setText(c.getAdresse());
        txtSolde.setText(String.valueOf(c.getSolde()));
    }

    private void clearForm() {
        txtId.setText("");
        txtNom.setText("");
        txtAdresse.setText("");
        txtSolde.setText("");
    }

    private void showStatus(String message, Color color) {
        lblStatut.setText("Statut : " + message);
        lblStatut.setForeground(color);
    }

    private void envoyerClient(String action) {
        String idText = txtId.getText().trim();
        String nom = txtNom.getText().trim();
        String adresse = txtAdresse.getText().trim();
        String soldeText = txtSolde.getText().trim();

        if (idText.isEmpty() || nom.isEmpty() || adresse.isEmpty() || soldeText.isEmpty()) {
            showStatus("Erreur : Tous les champs doivent être remplis.", Color.RED);
            return;
        }

        if ("delete".equals(action)) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Êtes-vous sûr de vouloir supprimer ce client ?",
                    "Confirmation de suppression",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                showStatus("Suppression annulée par l'utilisateur.", Color.GRAY);
                return;
            }
        }

        try {
            int id = Integer.parseInt(idText);
            double solde = Double.parseDouble(soldeText);
            Client c = new Client(id, nom, adresse, solde);
            String json = new Gson().toJson(new Request(action, c));

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    if (NetworkChecker.isServerAvailable()) {
                        String response = SocketClient.sendJsonToServer(json);
                        handleServerResponse(response, action, c);
                    } else {
                        handleOfflineMode(action, c);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    clearForm();
                }
            };
            worker.execute();
        } catch (NumberFormatException e) {
            showStatus("Erreur : Numéro ou solde invalide.", Color.RED);
        }
    }

    private void handleServerResponse(String response, String action, Client client) {
        if (response != null) {
            try {
                Map<String, String> map = new Gson().fromJson(response, Map.class);
                String status = map.get("status");
                String message = map.get("message");

                if ("success".equalsIgnoreCase(status)) {
                    showStatus(message, new Color(0, 128, 0));
                    chargerClients();

                    if ("delete".equals(action)) {
                        JsonManager.deleteDuplicateJsonFiles(String.valueOf(client.getnClient()));
                    }

                } else {
                    showStatus("Erreur : " + message, Color.RED);
                }

                showPopup("Message du serveur", message,
                        "success".equalsIgnoreCase(status) ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                showStatus("Réponse du serveur invalide.", Color.RED);
                showPopup("Erreur", "Réponse du serveur invalide.", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            handleOfflineMode(action, client);
        }
    }


    private void handleOfflineMode(String action, Client client) {
        JsonManager.saveRequestAsJson(new Request(action, client));
        showStatus("Mode hors ligne : données enregistrées localement.", Color.ORANGE);
        showPopup("Mode Hors Ligne", "Connexion échouée.\nDonnées sauvegardées localement.", JOptionPane.WARNING_MESSAGE);
    }

    private void showPopup(String title, String message, int type) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, type));
    }

    private void chargerClients() {
        try {
            String json = new Gson().toJson(new Request("list", null));
            List<Client> list = SocketClient.requestClientList(json);
            tableModel.setClients(list);
            showStatus("Liste des clients mise à jour.", new Color(0, 128, 0));
        } catch (Exception e) {
            showStatus("Impossible de charger les clients.", Color.RED);
        }
    }

    private void initTimer() {
        Timer timer = new Timer(5000, e -> {
            boolean isConnected = NetworkChecker.isServerAvailable();
            updateStatus(isConnected);
            if (isConnected) {
                PendingSyncManager.synchronizePendingRequests();
            }
        });
        timer.start();
    }

    private void updateStatus(boolean isConnected) {
        if (isConnected) {
            showStatus("🟢 Connecté", Color.GREEN.darker());
        } else {
            showStatus("🟠 Hors ligne : données stockées localement.", Color.ORANGE.darker());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(ClientForm::new);
    }
}
