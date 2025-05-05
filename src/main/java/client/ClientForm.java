package client;

import com.google.gson.Gson;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientForm extends JFrame {

    private JTextField txtId, txtNom, txtAdresse, txtSolde;
    private JLabel lblStatut;
    private JTable table;
    private ClientTableModel tableModel;
    private List<Client> clients = new ArrayList<>();

    public ClientForm() {
        setTitle("Gestion des Clients");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        // Panel principal avec padding
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // Panel formulaire
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Informations Client"));

        txtId = new JTextField();
        txtNom = new JTextField();
        txtAdresse = new JTextField();
        txtSolde = new JTextField();

        formPanel.add(new JLabel("ID :"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Nom :"));
        formPanel.add(txtNom);
        formPanel.add(new JLabel("Adresse :"));
        formPanel.add(txtAdresse);
        formPanel.add(new JLabel("Solde :"));
        formPanel.add(txtSolde);

        // Panel boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        buttonPanel.add(createButton("Ajouter", "add"));
        buttonPanel.add(createButton("Modifier", "update"));
        buttonPanel.add(createButton("Supprimer", "delete"));
        buttonPanel.add(createButton("Rafraîchir", "list"));

        // Panel du haut : formulaire + boutons
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPane.add(topPanel, BorderLayout.NORTH);

        // Label statut
        lblStatut = new JLabel("Statut : ");
        lblStatut.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatut.setForeground(Color.DARK_GRAY);
        contentPane.add(lblStatut, BorderLayout.SOUTH);

        // Table des clients
        tableModel = new ClientTableModel(clients);
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);

        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Sélection dans la table
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Client c = tableModel.getClientAt(table.convertRowIndexToModel(row));
                txtId.setText(String.valueOf(c.getnClient()));
                txtNom.setText(c.getNom());
                txtAdresse.setText(c.getAdresse());
                txtSolde.setText(String.valueOf(c.getSolde()));
            }
        });

        setVisible(true);
        chargerClients(); // chargement initial
    }

    private JButton createButton(String text, String action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.addActionListener(e -> {
            if (action.equals("list")) {
                chargerClients();
            } else {
                envoyerClient(action);
            }
        });
        return btn;
    }

    private void envoyerClient(String action) {
        try {
            String idText = txtId.getText().trim();
            String nom = txtNom.getText().trim();
            String adresse = txtAdresse.getText().trim();
            String soldeText = txtSolde.getText().trim();

            if (idText.isEmpty() || nom.isEmpty() || adresse.isEmpty() || soldeText.isEmpty()) {
                showStatus("Erreur : Tous les champs doivent être remplis.", Color.RED);
                return;
            }
            if (action.equals("delete")) {
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


            int id = Integer.parseInt(idText);
            double solde = Double.parseDouble(soldeText);

            Client c = new Client(id, nom, adresse, solde);
            String json = new Gson().toJson(new Request(action, c));
            System.out.println("JSON envoyé : " + json);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (NetworkChecker.isServerAvailable()) {
                        JsonManager.saveRequestAsJson(new Request("add", c));
                        boolean success = SocketClient.sendJsonToServer(json);
                        if (success) {
                            showStatus("Opération " + action + " réussie.", new Color(0, 128, 0));
                            chargerClients();
                        } else {
                            showStatus("Échec de l'envoi.", Color.RED);
                        }
                    } else {
                        JsonManager.saveClientAsJson(c);
                        showStatus("Mode hors ligne : données enregistrées localement.", Color.ORANGE);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    clearForm();
                }
            };
            worker.execute();
        } catch (NumberFormatException ex) {
            showStatus("Erreur de format : champs numériques invalides.", Color.RED);
        } catch (Exception ex) {
            showStatus("Erreur : " + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        }
    }

    private void showStatus(String message, Color color) {
        lblStatut.setText("Statut : " + message);
        lblStatut.setForeground(color);
    }

    private void clearForm() {
        txtId.setText("");
        txtNom.setText("");
        txtAdresse.setText("");
        txtSolde.setText("");
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
    private void updateStatus(boolean isConnected) {
        if (isConnected) {
            lblStatut.setText("🟢 Connecté");
            lblStatut.setForeground(Color.GREEN.darker());
        } else {
            lblStatut.setText("🟠 Hors ligne : données stockées localement.");
            lblStatut.setForeground(Color.ORANGE.darker());
        }
    }

    public void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(ClientForm::new);
        Timer timer = new Timer(5000, e -> {
            boolean isConnected = NetworkChecker.isServerAvailable();
            updateStatus(isConnected);
            if (isConnected) {
                PendingSyncManager.synchronizePendingRequests();
            }
        });
        timer.start();

    }
}
