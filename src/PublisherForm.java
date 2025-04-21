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
    private JButton backButton;  // Back to menu button
    private User currentUser;    // Store the current user

    public PublisherForm(User user) {
        this.currentUser = user;  // Store the current user
        setTitle("Publisher Information");
        setSize(800, 500);  // Increased size to fit better in full screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBackground(new Color(240, 240, 240)); // Light background color

        // --- Search panel ---
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));  // Align elements to the left with padding
        searchPanel.setBackground(new Color(240, 240, 240)); // Match the background color

        searchLabel = new JLabel("Search Publisher:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchLabel.setForeground(new Color(0, 102, 204));  // Blue color for labels
        searchPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204)));  // Blue border around the text field
        searchPanel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(0, 102, 204));  // Blue background
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        // --- Table setup ---
        model = new DefaultTableModel();
        model.addColumn("Publisher Name");
        model.addColumn("Contact");
        model.addColumn("Email");
        publisherTable = new JTable(model);
        publisherTable.setFont(new Font("Arial", Font.PLAIN, 12));
        publisherTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(publisherTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- Back to menu button ---
        backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(255, 69, 0));  // Red background for back button
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> goBackToMenu());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(backButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

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

    // Method to go back to the Welcome screen
    private void goBackToMenu() {
        this.dispose();
        new Welcome(currentUser);  // Open the Welcome form and pass the current user to it
    }

    public static void main(String[] args) {
        // Example user for testing, replace with actual login system
    }
}
