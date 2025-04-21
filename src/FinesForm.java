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
    private JButton backButton;
    private JButton markPaidButton;
    private JTable finesTable;
    private final DefaultTableModel model;
    private User currentUser;

    public FinesForm(User user) {
        this.currentUser = user;
        setTitle("Search Fines");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245)); 
        //Search panel 
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchLabel = new JLabel("Search Fines:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(300, 30));
        searchPanel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.setBackground(new Color(0, 102, 204));  
        searchButton.setForeground(Color.WHITE);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Table setup 
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

        // Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backButton = new JButton("Back to Menu");
        backButton.setPreferredSize(new Dimension(150, 30));
        backButton.setBackground(new Color(255, 69, 0));  // Red
        backButton.setForeground(Color.WHITE);
        bottomPanel.add(backButton);

        markPaidButton = new JButton("Mark Paid");
        markPaidButton.setPreferredSize(new Dimension(150, 30));
        markPaidButton.setBackground(new Color(0, 153, 51)); 
        markPaidButton.setForeground(Color.WHITE);
        bottomPanel.add(markPaidButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);

        searchButton.addActionListener(e -> searchFines());
        backButton.addActionListener(e -> goBackToMenu());
        markPaidButton.addActionListener(e -> markFineAsPaid());

        loadFines();
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
                        rs.getDate("IssueDate"),
                        rs.getDate("DueDate")
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
        int selectedRow = finesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int fineID = (int) model.getValueAt(selectedRow, 0);

            String updateQuery = "UPDATE Fines SET Status = 'Paid' WHERE FineID = ?";
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
                 PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {

                pstmt.setInt(1, fineID);
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(null, "Fine marked as Paid!");
                    loadFines();
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

    private void goBackToMenu() {
        dispose();
        new Welcome(currentUser);
    }
}
