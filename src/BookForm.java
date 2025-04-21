import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class BookForm extends JFrame {
    private JPanel panel;
    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton backButton;
    private JTable bookTable;
    private final DefaultTableModel model;
    private JComboBox<String> categoryFilter;
    private final User currentUser;

    public BookForm(User user) {
        this.currentUser = user;

        setTitle("Browse Books");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245)); 

        //Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchLabel = new JLabel("Search Books:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        searchLabel.setPreferredSize(new Dimension(100, 30));
        searchPanel.add(searchLabel);

        searchField = new JTextField(30);
        searchField.setPreferredSize(new Dimension(300, 30));
        searchPanel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setBackground(new Color(0, 102, 204));  // Blue
        searchButton.setForeground(Color.WHITE);
        searchPanel.add(searchButton);

        categoryFilter = new JComboBox<>(new String[] {
                "All Categories", "Fiction", "Science", "History"
        });
        categoryFilter.setFont(new Font("Arial", Font.PLAIN, 14));
        categoryFilter.setPreferredSize(new Dimension(150, 30));
        searchPanel.add(categoryFilter);

        panel.add(searchPanel, BorderLayout.NORTH);

        //Table setup
        model = new DefaultTableModel();
        model.addColumn("ISBN");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Publisher");
        model.addColumn("Category");
        model.addColumn("Condition");
        model.addColumn("Available Copies");

        bookTable = new JTable(model);
        bookTable.setFont(new Font("Arial", Font.PLAIN, 14));
        bookTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(bookTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        //Back button
        backButton = new JButton("Back to Welcome");
        backButton.addActionListener(e -> goBackToWelcome());
        backButton.setPreferredSize(new Dimension(150, 30));
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(255, 69, 0));  // Red
        backButton.setForeground(Color.WHITE);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(backButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);

        // Wire up actions
        searchButton.addActionListener(e -> searchBooks());
        loadBooks();
    }

    private void loadBooks() {
        model.setRowCount(0); 
        String sql =
                "SELECT b.ISBN, b.Title, b.Author, p.Name AS Publisher, c.CategoryName, b.Cond, "
                        + "COALESCE(SUM(CASE WHEN i.Status = 'Available' THEN 1 ELSE 0 END), 0) AS AvailableCopies "
                        + "FROM Books b "
                        + "JOIN Publishers p ON b.PublisherID = p.PublisherID "
                        + "JOIN Categories c ON b.CategoryID = c.CategoryID "
                        + "LEFT JOIN Inventory i ON b.ISBN = i.ISBN "
                        + "GROUP BY b.ISBN, b.Title, b.Author, p.Name, c.CategoryName, b.Cond";

        try (Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library", "root", "0000");
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("ISBN"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getString("Publisher"),
                        rs.getString("CategoryName"),
                        rs.getString("Cond"),
                        rs.getInt("AvailableCopies")  
                });
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    private void searchBooks() {
        model.setRowCount(0);

        String text = searchField.getText().trim();
        String cat  = (String) categoryFilter.getSelectedItem();

        StringBuilder sb = new StringBuilder(
                "SELECT b.ISBN, b.Title, b.Author, p.Name AS Publisher, c.CategoryName, b.Cond, "
                        + "COALESCE(SUM(CASE WHEN i.Status = 'Available' THEN 1 ELSE 0 END), 0) AS AvailableCopies "
                        + "FROM Books b "
                        + "JOIN Publishers p ON b.PublisherID = p.PublisherID "
                        + "JOIN Categories c ON b.CategoryID = c.CategoryID "
                        + "LEFT JOIN Inventory i ON b.ISBN = i.ISBN "
        );

        boolean hasWhere = false;
        if (!text.isEmpty()) {
            sb.append(" WHERE (Title LIKE ? OR Author LIKE ?)");
            hasWhere = true;
        }
        if (!"All Categories".equals(cat)) {
            sb.append(hasWhere ? " AND " : " WHERE ")
                    .append("CategoryName = ?");
        }

        sb.append(" GROUP BY b.ISBN, b.Title, b.Author, p.Name, c.CategoryName, b.Cond");

        try (Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library", "root", "0000");
             PreparedStatement ps = c.prepareStatement(sb.toString())) {

            int idx = 1;
            if (!text.isEmpty()) {
                ps.setString(idx++, "%" + text + "%");
                ps.setString(idx++, "%" + text + "%");
            }
            if (!"All Categories".equals(cat)) {
                ps.setString(idx, cat);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("ISBN"),
                            rs.getString("Title"),
                            rs.getString("Author"),
                            rs.getString("Publisher"),
                            rs.getString("CategoryName"),
                            rs.getString("Cond"),
                            rs.getInt("AvailableCopies")  
                    });
                }
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    private void goBackToWelcome() {
        // close BookForm
        dispose();

        // and re-open Welcome with the same user
        new Welcome(currentUser);
    }

    public static void main(String[] args) {
    }
}
