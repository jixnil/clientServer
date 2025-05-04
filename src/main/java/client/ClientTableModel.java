package client;

import javax.swing.table.AbstractTableModel;
import java.util.List;

class ClientTableModel extends AbstractTableModel {

    private List<Client> clients;
    private final String[] colonnes = {"ID", "Nom", "Adresse", "Solde"};

    public ClientTableModel(List<Client> clients) {
        this.clients = clients;
    }

    @Override
    public int getRowCount() {
        return clients.size();
    }

    @Override
    public int getColumnCount() {
        return colonnes.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Client c = clients.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> c.getnClient();
            case 1 -> c.getNom();
            case 2 -> c.getAdresse();
            case 3 -> c.getSolde();
            default -> null;
        };
    }

    @Override
    public String getColumnName(int column) {
        return colonnes[column];
    }

    public Client getClientAt(int rowIndex) {
        return clients.get(rowIndex);
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
        fireTableDataChanged(); // Notifie le JTable
    }

}
