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
    private JTextField userIdField;
    private JTextField isbnField;
    private JTextField issueDateField;
    private JTextField dueDateField;

    public BorrowBookForm() {
        setTitle("Borrow Book");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 10, 10));

        userIdLabel = new JLabel("User ID:");
        isbnLabel = new JLabel("ISBN:");
        issueDateLabel = new JLabel("Issue Date (Auto-generated):");
        dueDateLabel = new JLabel("Due Date (Auto-generated):");

        userIdField = new JTextField(20);
        isbnField = new JTextField(20);
        issueDateField = new JTextField(20);
        dueDateField = new JTextField(20);

        issueDateField.setEditable(false);  // Make issue date field uneditable
        dueDateField.setEditable(false);    // Make due date field uneditable

        borrowButton = new JButton("Borrow Book");

        borrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                borrowBookToDatabase();
            }
        });

        panel.add(userIdLabel);
        panel.add(userIdField);
        panel.add(isbnLabel);
        panel.add(isbnField);
        panel.add(issueDateLabel);
        panel.add(issueDateField);
        panel.add(dueDateLabel);
        panel.add(dueDateField);
        panel.add(new JLabel());
        panel.add(borrowButton);

        add(panel);

        setVisible(true);
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

            String query = "INSERT INTO Borrowing (UserID, ISBN, IssueDate, DueDate) VALUES (?, ?, ?, ?)";
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
                 PreparedStatement pstmt = connection.prepareStatement(query)) {

                pstmt.setInt(1, Integer.parseInt(userId));
                pstmt.setString(2, isbn);
                pstmt.setDate(3, sqlIssueDate);
                pstmt.setDate(4, sqlDueDate);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(null, "Book borrowed successfully!");

                    // After borrowing the book, open the Welcome form
                    new Welcome(new User(Integer.parseInt(userId), "", "", "", 0, null));  // Pass User object with ID
                    dispose();  // Close the BorrowBookForm window
                } else {
                    JOptionPane.showMessageDialog(null, "Error borrowing the book.");
                }
            } catch (SQLException ex) {
                System.out.println("SQL Error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error in date processing: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new BorrowBookForm();
    }
}