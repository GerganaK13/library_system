import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.sql.*;

public class PublisherForm extends JFrame {
    private JPanel panel;
    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchButton;
    private JTable publisherTable;
    private DefaultTableModel model;

    public PublisherForm() {
        setTitle("Publisher Information");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        searchLabel = new JLabel("Search Publisher:");
        searchPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchPanel.add(searchField);

        searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        model = new DefaultTableModel();
        model.addColumn("Publisher Name");
        model.addColumn("Contact");
        model.addColumn("Email");
        publisherTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(publisherTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);
        setVisible(true);

        searchButton.addActionListener(e -> searchPublishers());
        loadPublishers();
    }

    private void loadPublishers() {
        model.setRowCount(0);
        String query = "SELECT PublisherID, Name, Contact, Email FROM Publishers";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getString("Name"),
                        rs.getString("Contact"),
                        rs.getString("Email")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    private void searchPublishers() {
        String searchQuery = searchField.getText();
        if (searchQuery.isEmpty()) {
            loadPublishers();
            return;
        }

        model.setRowCount(0);
        String query = "SELECT PublisherID, Name, Contact, Email FROM Publishers WHERE Name LIKE ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getString("Name"),
                        rs.getString("Contact"),
                        rs.getString("Email")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new PublisherForm();
    }
}
