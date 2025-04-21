import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class BorrowBookForm extends JFrame {
    private JPanel panel;
    private JLabel userIdLabel;
    private JLabel isbnLabel;
    private JLabel issueDateLabel;
    private JLabel dueDateLabel;
    private JButton borrowButton;
    private JButton returnButton;
    private JTextField userIdField;
    private JTextField isbnField;
    private JTextField issueDateField;
    private JTextField dueDateField;
    private JButton backButton;
    private User currentUser;

    public BorrowBookForm(User user) {
        this.currentUser = user;
        setTitle("Borrow Book");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBackground(new Color(245, 245, 245)); // light gray background

        userIdLabel = new JLabel("User ID:");
        userIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        isbnLabel = new JLabel("ISBN:");
        isbnLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        issueDateLabel = new JLabel("Issue Date:");
        issueDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dueDateLabel = new JLabel("Due Date:");
        dueDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        userIdField = new JTextField();
        userIdField.setPreferredSize(new Dimension(150, 30));
        isbnField = new JTextField();
        isbnField.setPreferredSize(new Dimension(150, 30));
        issueDateField = new JTextField();
        issueDateField.setPreferredSize(new Dimension(150, 30));
        dueDateField = new JTextField();
        dueDateField.setPreferredSize(new Dimension(150, 30));

        issueDateField.setEditable(false);  // Make issue date field uneditable
        dueDateField.setEditable(false);    // Make due date field uneditable

        borrowButton = new JButton("Borrow Book");
        borrowButton.setPreferredSize(new Dimension(150, 30));
        borrowButton.setBackground(new Color(0, 102, 204));  // Blue background
        borrowButton.setForeground(Color.WHITE);

        returnButton = new JButton("Return Book");
        returnButton.setPreferredSize(new Dimension(150, 30));
        returnButton.setBackground(new Color(0, 153, 51));  // Green background
        returnButton.setForeground(Color.WHITE);

        backButton = new JButton("Back to Menu");
        backButton.setPreferredSize(new Dimension(150, 30));
        backButton.setBackground(new Color(255, 69, 0));  // Red background
        backButton.setForeground(Color.WHITE);

        panel.add(userIdLabel);
        panel.add(userIdField);
        panel.add(isbnLabel);
        panel.add(isbnField);
        panel.add(issueDateLabel);
        panel.add(issueDateField);
        panel.add(dueDateLabel);
        panel.add(dueDateField);
        panel.add(borrowButton);
        panel.add(returnButton);
        panel.add(backButton);

        add(panel);
        setVisible(true);

        borrowButton.addActionListener(e -> borrowBookToDatabase());
        returnButton.addActionListener(e -> returnBookToDatabase());
        backButton.addActionListener(e -> goBackToMenu());
    }

    private void borrowBookToDatabase() {
        String userId = userIdField.getText();
        String isbn = isbnField.getText();

        // Check if the fields are not empty
        if (userId.isEmpty() || isbn.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all required fields!");
            return;
        }

        try {
            // Automatically generate the issue date (today) and due date (30 days from now)
            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(30); // 30 days from issue date

            // Convert LocalDate to SQL Date
            java.sql.Date sqlIssueDate = java.sql.Date.valueOf(issueDate);
            java.sql.Date sqlDueDate = java.sql.Date.valueOf(dueDate);

            // Set the automatically generated dates to the text fields
            issueDateField.setText(issueDate.toString());
            dueDateField.setText(dueDate.toString());

            // Check if there is an available copy of the book
            String checkAvailabilityQuery = "SELECT InventoryID FROM Inventory WHERE ISBN = ? AND Status = 'Available' LIMIT 1";
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/library", "root", "0000");
                 PreparedStatement pstmt = connection.prepareStatement(checkAvailabilityQuery)) {

                pstmt.setString(1, isbn);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    // Found an available copy
                    int inventoryId = rs.getInt("InventoryID");

                    // Update the book copy status to "Borrowed"
                    String updateCopyQuery = "UPDATE Inventory SET Status = 'Borrowed' WHERE InventoryID = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateCopyQuery)) {
                        updateStmt.setInt(1, inventoryId);
                        updateStmt.executeUpdate();
                    }

                    // Insert into the Borrowing table
                    String borrowQuery = "INSERT INTO Borrowing (UserID, ISBN, InventoryID, IssueDate, DueDate) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement borrowStmt = connection.prepareStatement(borrowQuery)) {
                        borrowStmt.setInt(1, Integer.parseInt(userId));
                        borrowStmt.setString(2, isbn);
                        borrowStmt.setInt(3, inventoryId);  // Use InventoryID here
                        borrowStmt.setDate(4, sqlIssueDate);
                        borrowStmt.setDate(5, sqlDueDate);
                        borrowStmt.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(null, "Book borrowed successfully!");
                    goBackToMenu();  // Close the BorrowBookForm
                } else {
                    JOptionPane.showMessageDialog(null, "No available copies of this book.");
                }
            } catch (SQLException ex) {
                System.out.println("SQL Error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error in date processing: " + ex.getMessage());
        }
    }


    private void returnBookToDatabase() {
        String userId = userIdField.getText();
        String isbn = isbnField.getText();

        // Check if the fields are not empty
        if (userId.isEmpty() || isbn.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all required fields!");
            return;
        }

        try {
            // Retrieve the InventoryID from the Borrowing table for the given user and ISBN
            String query = "SELECT br.InventoryID FROM Borrowing br "
                    + "JOIN Inventory i ON br.InventoryID = i.InventoryID "
                    + "WHERE br.UserID = ? AND br.ISBN = ? AND br.ReturnDate IS NULL LIMIT 1";

            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/library", "root", "0000");
                 PreparedStatement pstmt = connection.prepareStatement(query)) {

                pstmt.setInt(1, Integer.parseInt(userId));
                pstmt.setString(2, isbn);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    // Found the borrowed copy
                    int inventoryId = rs.getInt("InventoryID");

                    // Update the book copy status to "Available"
                    String updateCopyQuery = "UPDATE Inventory SET Status = 'Available' WHERE InventoryID = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateCopyQuery)) {
                        updateStmt.setInt(1, inventoryId);
                        updateStmt.executeUpdate();
                    }

                    // Update the Borrowing table with the return date
                    String returnQuery = "UPDATE Borrowing SET ReturnDate = ? WHERE UserID = ? AND ISBN = ? AND InventoryID = ?";
                    try (PreparedStatement returnStmt = connection.prepareStatement(returnQuery)) {
                        returnStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                        returnStmt.setInt(2, Integer.parseInt(userId));
                        returnStmt.setString(3, isbn);
                        returnStmt.setInt(4, inventoryId);
                        returnStmt.executeUpdate();
                    }

                    // Show success message and go back to menu
                    JOptionPane.showMessageDialog(null, "Book returned successfully!");
                    goBackToMenu();  // Go back to Welcome screen after successful return
                } else {
                    JOptionPane.showMessageDialog(null, "No borrowed copy found for this book.");
                }
            } catch (SQLException ex) {
                System.out.println("SQL Error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error in processing return: " + ex.getMessage());
        }
    }


    private void goBackToMenu() {
        dispose();
        new Welcome(currentUser);
    }

    public static void main(String[] args) {
        // Test case can be added if needed
    }
}
