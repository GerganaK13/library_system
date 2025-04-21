import org.mindrot.jbcrypt.BCrypt;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDate;

public class connect {
    private static final String URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root";
    private static final String PASSWORD = "0000";

    // Execute SQL query and return results
    public static ArrayList<String[]> executeQuery(String query) {
        ArrayList<String[]> results = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            form1.model.setColumnCount(0);
            for (int i = 1; i <= columnCount; i++) {
                form1.model.addColumn(metaData.getColumnName(i));
            }

            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                results.add(row);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return results;
    }

    // Add a new column to the Users table
    public static void addColumn(String columnName, String dataType) {
        String query = "ALTER TABLE Users ADD COLUMN " + columnName + " " + dataType;

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt.executeUpdate(query);
            connection.commit();
            System.out.println("Column '" + columnName + "' with datatype: " + dataType + " added successfully.");
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    // Update user information in the Users table
    public static void updateUser(int userID, String[] columns, String[] newValues) {
        if (columns.length != newValues.length) {
            System.out.println("Error: Column count does not match value count.");
            return;
        }

        StringBuilder queryBuilder = new StringBuilder("UPDATE Users SET ");
        for (int i = 0; i < columns.length; i++) {
            queryBuilder.append(columns[i]).append(" = ?");
            if (i < columns.length - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(" WHERE UserID = ?");

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < newValues.length; i++) {
                pstmt.setString(i + 1, newValues[i]);
            }
            pstmt.setInt(newValues.length + 1, userID);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                System.out.println("Update committed successfully for UserID: " + userID);
            } else {
                connection.rollback();
                System.out.println("Update failed. Transaction rolled back.");
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    // Add a new user to the Users table
    public static void addUser(int userID, String name, String role, String contact, int borrowLimit, String hashedPassword, InputStream image) {
        String query = "INSERT INTO Users (UserID, Name, Role, Contact, BorrowLimit, Password, Image) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);
            pstmt.setInt(1, userID);
            pstmt.setString(2, name);
            pstmt.setString(3, role);
            pstmt.setString(4, contact);
            pstmt.setInt(5, borrowLimit);
            pstmt.setString(6, hashedPassword);
            pstmt.setBlob(7, image);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                connection.commit();
                System.out.println("User registered successfully: " + name);
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }

    // User login
    public static User login(String userID, String password) {
        User user = null;
        String query = "SELECT * FROM Users WHERE UserID = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("Password");
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        InputStream imgStream = rs.getBinaryStream("Image");
                        user = new User(
                                rs.getInt("UserID"),
                                rs.getString("Name"),
                                rs.getString("Role"),
                                rs.getString("Contact"),
                                rs.getInt("BorrowLimit"),
                                imgStream
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return user;
    }

    // Borrow a book
    public static boolean borrowBook(int userId, String isbn) {
        String query = "SELECT CopyID FROM BookCopies WHERE ISBN = ? AND Status = 'Available' LIMIT 1";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Found an available copy
                int copyId = rs.getInt("CopyID");

                // Update the book copy status to "Borrowed"
                String updateCopyQuery = "UPDATE BookCopies SET Status = 'Borrowed' WHERE CopyID = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateCopyQuery)) {
                    updateStmt.setInt(1, copyId);
                    updateStmt.executeUpdate();
                }

                // Add the borrowing record to the Borrowing table
                String insertBorrowQuery = "INSERT INTO Borrowing (UserID, ISBN, CopyID, IssueDate, DueDate) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement borrowStmt = connection.prepareStatement(insertBorrowQuery)) {
                    borrowStmt.setInt(1, userId);
                    borrowStmt.setString(2, isbn);
                    borrowStmt.setInt(3, copyId);
                    borrowStmt.setDate(4, Date.valueOf(LocalDate.now()));
                    borrowStmt.setDate(5, Date.valueOf(LocalDate.now().plusDays(30)));  // 30 days due date
                    borrowStmt.executeUpdate();
                }

                // Successfully borrowed the book
                return true;
            } else {
                System.out.println("No available copies of this book.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error borrowing book: " + e.getMessage());
            return false;
        }
    }

    // Return a book
    public static boolean returnBook(int userId, String isbn) {
        // Find the borrowed copy of the book
        String query = "SELECT CopyID FROM Borrowing WHERE UserID = ? AND ISBN = ? AND ReturnDate IS NULL LIMIT 1";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, isbn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int copyId = rs.getInt("CopyID");

                // Update the book copy status to "Available"
                String updateCopyQuery = "UPDATE BookCopies SET Status = 'Available' WHERE CopyID = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateCopyQuery)) {
                    updateStmt.setInt(1, copyId);
                    updateStmt.executeUpdate();
                }

                // Update the Borrowing record with the return date
                String returnQuery = "UPDATE Borrowing SET ReturnDate = ? WHERE UserID = ? AND ISBN = ? AND CopyID = ?";
                try (PreparedStatement returnStmt = connection.prepareStatement(returnQuery)) {
                    returnStmt.setDate(1, Date.valueOf(LocalDate.now()));
                    returnStmt.setInt(2, userId);
                    returnStmt.setString(3, isbn);
                    returnStmt.setInt(4, copyId);
                    returnStmt.executeUpdate();
                }

                // Successfully returned the book
                return true;
            } else {
                System.out.println("No borrowed copy found for this book.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error returning book: " + e.getMessage());
            return false;
        }
    }
}
// User class to store user data
class User {
    private int userID;
    private String name;
    private String role;
    private String contact;
    private int borrowLimit;
    private InputStream img;

    public User(int userID, String name, String role, String contact, int borrowLimit, InputStream img) {
        this.userID = userID;
        this.name = name;
        this.role = role;
        this.contact = contact;
        this.borrowLimit = borrowLimit;
        this.img = img;
    }

    public int getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getContact() {
        return contact;
    }

    public int getBorrowLimit() {
        return borrowLimit;
    }

    public ImageIcon getImageIcon() {
        try {
            if (img != null) {
                BufferedImage image = ImageIO.read(img);
                return new ImageIcon(image);
            }
        } catch (IOException e) {
            System.out.println("Error loading image: " + e.getMessage());
        }
        return null;
    }
}

