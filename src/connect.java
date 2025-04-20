import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;

public class connect {
    private static final String URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root";
    private static final String PASSWORD = "0000";

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
}

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