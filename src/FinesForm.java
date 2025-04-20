import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.sql.*;

public class FinesForm extends JFrame {
    private JPanel panel;
    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton backButton;  // Button to go back to Welcome page
    private JButton markPaidButton;  // Button to mark a fine as paid
    private JTable finesTable;
    private final DefaultTableModel model;

    public FinesForm() {
        setTitle("Search Fines");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        searchLabel = new JLabel("Search Fines (by UserID, ISBN, or Status):");
        searchPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchPanel.add(searchField);

        searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        // Adding the back button to navigate to the Welcome form
        backButton = new JButton("Back to Welcome");
        backButton.addActionListener(e -> goBackToWelcome());

        // Adding the markPaidButton to mark fines as paid
        markPaidButton = new JButton("Mark Paid");
        markPaidButton.addActionListener(e -> markFineAsPaid());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(backButton);
        bottomPanel.add(markPaidButton);  // Add markPaidButton to bottom panel
        panel.add(bottomPanel, BorderLayout.SOUTH);

        model = new DefaultTableModel();
        model.addColumn("FineID");
        model.addColumn("Amount Due");
        model.addColumn("Status");
        model.addColumn("ISBN");
        model.addColumn("User Name");
        model.addColumn("Issue Date");
        model.addColumn("Due Date");

        finesTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(finesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);
        setVisible(true);

        searchButton.addActionListener(e -> searchFines());
        loadFines();  // Load all fines initially
    }

    private void loadFines() {
        model.setRowCount(0);
        String query = "SELECT F.FineID, F.AmountDue, F.Status, B.ISBN, U.Name, B.IssueDate, B.DueDate "
                + "FROM Fines F "
                + "JOIN Borrowing B ON F.BorrowID = B.BorrowID "
                + "JOIN Users U ON F.UserID = U.UserID";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getInt("FineID"),
                        rs.getDouble("AmountDue"),
                        rs.getString("Status"),
                        rs.getString("ISBN"),
                        rs.getString("Name"),
                        rs.getDate("IssueDate"),   // Get Issue Date from Borrowing table
                        rs.getDate("DueDate")      // Get Due Date from Borrowing table
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    private void searchFines() {
        String searchQuery = searchField.getText();
        if (searchQuery.isEmpty()) {
            loadFines();
            return;
        }

        model.setRowCount(0);
        String query = "SELECT F.FineID, F.AmountDue, F.Status, B.ISBN, U.Name, B.IssueDate, B.DueDate "
                + "FROM Fines F "
                + "JOIN Borrowing B ON F.BorrowID = B.BorrowID "
                + "JOIN Users U ON F.UserID = U.UserID "
                + "WHERE U.UserID LIKE ? OR B.ISBN LIKE ? OR F.Status LIKE ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchQuery + "%");
            pstmt.setString(2, "%" + searchQuery + "%");
            pstmt.setString(3, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getInt("FineID"),
                        rs.getDouble("AmountDue"),
                        rs.getString("Status"),
                        rs.getString("ISBN"),
                        rs.getString("Name"),
                        rs.getDate("IssueDate"),
                        rs.getDate("DueDate")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    private void markFineAsPaid() {
        // Get the selected fine from the table
        int selectedRow = finesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int fineID = (int) model.getValueAt(selectedRow, 0); // Get FineID

            // SQL to update the status of the fine to "Paid"
            String updateQuery = "UPDATE Fines SET Status = 'Paid' WHERE FineID = ?";
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
                 PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {

                pstmt.setInt(1, fineID);
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(null, "Fine marked as Paid!");
                    loadFines();  // Reload fines after updating the status
                } else {
                    JOptionPane.showMessageDialog(null, "Error marking fine as paid.");
                }
            } catch (SQLException ex) {
                System.out.println("SQL Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a fine to mark as paid.");
        }
    }

    private void goBackToWelcome() {
        // Close the current FinesForm window
        this.dispose();

        // Open the Welcome page
        new Welcome(new User(1, "John Doe", "Student", "johndoe@example.com", 5, null));  // Example user, replace with actual user object
    }

    public static void main(String[] args) {
        new FinesForm();
    }
}
