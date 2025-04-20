import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.sql.*;

public class BookForm extends JFrame {
    private JPanel panel;
    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton backButton;  // Button to go back to Welcome page
    private JTable bookTable;
    private final DefaultTableModel model;

    private JComboBox<String> categoryFilter;


    public BookForm() {
        setTitle("Browse Books");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        searchLabel = new JLabel("Search Books:");
        searchPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchPanel.add(searchField);

        searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        // Add combo boxes for filtering by Category, Publisher, and Availability
        categoryFilter = new JComboBox<>(new String[] { "All Categories", "Fiction", "Science", "History" });


        searchPanel.add(categoryFilter);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Adding the back button to navigate to the Welcome form
        backButton = new JButton("Back to Welcome");
        backButton.addActionListener(e -> goBackToWelcome());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(backButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        model = new DefaultTableModel();
        model.addColumn("ISBN");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Publisher");
        model.addColumn("Category");
        model.addColumn("Condition");
        model.addColumn("Availability");
        bookTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bookTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);
        setVisible(true);

        searchButton.addActionListener(e -> searchBooks());
        loadBooks();
    }

    private void loadBooks() {
        model.setRowCount(0);
        String query = "SELECT ISBN, Title, Author, Name AS Publisher, CategoryName, Cond, Availability FROM Books "
                + "JOIN Publishers ON Books.PublisherID = Publishers.PublisherID "
                + "JOIN Categories ON Books.CategoryID = Categories.CategoryID";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getString("ISBN"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getString("Publisher"),
                        rs.getString("CategoryName"),
                        rs.getString("Cond"),  // Now escaped correctly
                        rs.getString("Availability")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    private void searchBooks() {
        String searchQuery = searchField.getText();
        String categoryFilterSelected = categoryFilter.getSelectedItem().toString();

        StringBuilder queryBuilder = new StringBuilder("SELECT ISBN, Title, Author, Name AS Publisher, CategoryName, Cond, Availability FROM Books "
                + "JOIN Publishers ON Books.PublisherID = Publishers.PublisherID "
                + "JOIN Categories ON Books.CategoryID = Categories.CategoryID WHERE");

        boolean firstCondition = true;

        if (!searchQuery.isEmpty()) {
            queryBuilder.append(" (Title LIKE ? OR Author LIKE ?)");
            firstCondition = false;
        }

        if (!categoryFilterSelected.equals("All Categories")) {
            if (!firstCondition) queryBuilder.append(" AND");
            queryBuilder.append(" CategoryName = ?");
            firstCondition = false;
        }


        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "0000");
             PreparedStatement pstmt = connection.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;
            if (!searchQuery.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + searchQuery + "%");
                pstmt.setString(paramIndex++, "%" + searchQuery + "%");
            }

            if (!categoryFilterSelected.equals("All Categories")) {
                pstmt.setString(paramIndex++, categoryFilterSelected);
            }

            ResultSet rs = pstmt.executeQuery();

            model.setRowCount(0);  // Clear the existing data

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getString("ISBN"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getString("Publisher"),
                        rs.getString("CategoryName"),
                        rs.getString("Cond"),
                        rs.getString("Availability")
                };
                model.addRow(row);
            }

        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    private void goBackToWelcome() {
        // Close the current BookForm window
        this.dispose();

        // Create a new instance of the Welcome form (pass the user details if needed)
        // Assuming you have a User object to pass, if not, you can pass a default object
    }

    public static void main(String[] args) {
        new BookForm();
    }
}
