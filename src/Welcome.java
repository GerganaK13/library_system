import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Welcome extends JFrame {
    private JLabel img; // To display the logo image
    private JLabel first_name;
    private JPanel panel;
    private JButton borrowBooksButton;
    private JButton viewBooksButton;
    private JButton viewFinesButton;
    private JButton logOutButton;

    public Welcome(User user) {
        // Set up the main window
        setTitle("Welcome - Library Management System");
        setSize(600, 400);  // Increased size for better layout
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false); // Prevent resizing

        // Create and customize the panel
        panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 10, 10)); // Layout with vertical alignment
        panel.setBackground(new Color(255, 228, 225)); // Light pink background

        // Load and resize the logo image
        ImageIcon logoIcon = new ImageIcon("src/LetsMoveIcon.jpg"); // Path to the image file (update the path)
        if (logoIcon.getIconWidth() == -1) {
            System.out.println("Error loading image.");
        } else {
            System.out.println("Image loaded successfully.");
            Image imgResized = logoIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Resize image to 150x150 pixels
            img = new JLabel(new ImageIcon(imgResized)); // Create a JLabel with the resized image
            panel.add(img); // Add the image label to the panel
        }

        // Add the welcome message
        first_name = new JLabel("Welcome to the Library Management System, " + user.getName() + "!");
        first_name.setFont(new Font("Arial", Font.BOLD, 18));
        first_name.setForeground(new Color(0, 102, 204));  // Blue text
        panel.add(first_name);

        // Set up and style buttons
        viewBooksButton = new JButton("View Books");
        viewBooksButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewBooksButton.setBackground(new Color(0, 102, 204));  // Blue background
        viewBooksButton.setForeground(Color.WHITE);
        viewBooksButton.setFocusPainted(false);
        panel.add(viewBooksButton);

        borrowBooksButton = new JButton("Borrow Books");
        borrowBooksButton.setFont(new Font("Arial", Font.BOLD, 14));
        borrowBooksButton.setBackground(new Color(0, 153, 51));  // Green background
        borrowBooksButton.setForeground(Color.WHITE);
        borrowBooksButton.setFocusPainted(false);
        panel.add(borrowBooksButton);

        viewFinesButton = new JButton("View Fines");
        viewFinesButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewFinesButton.setBackground(new Color(255, 140, 0));  // Orange background
        viewFinesButton.setForeground(Color.WHITE);
        viewFinesButton.setFocusPainted(false);
        panel.add(viewFinesButton);

        logOutButton = new JButton("Log Out");
        logOutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logOutButton.setBackground(new Color(255, 69, 0));  // Red background
        logOutButton.setForeground(Color.WHITE);
        logOutButton.setFocusPainted(false);
        panel.add(logOutButton);

        // Add ActionListeners for buttons
        addActionListeners(user);

        // Add panel to the frame and make it visible
        setContentPane(panel);
        setVisible(true);
    }

    // Method to add ActionListeners to buttons
    private void addActionListeners(User user) {
    viewBooksButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Action for View Books button
            JOptionPane.showMessageDialog(null, "Navigate to View Books Form.");
            new BookForm();  // Assuming you have a BookForm for displaying books
            setVisible(false);  // Hide Welcome Form
        }
    });

        borrowBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Action for Borrow Books button
                JOptionPane.showMessageDialog(null, "Navigate to Borrow Books Form.");
                new BorrowBookForm();  // Assuming you have a BorrowBookForm
                setVisible(false);  // Hide Welcome Form
            }
        });



        viewFinesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Action for View Fines button
                JOptionPane.showMessageDialog(null, "Navigate to View Fines Form.");
                new FinesForm();  // Assuming you have a FinesForm
                setVisible(false);  // Hide Welcome Form
            }
        });

        logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Action for Log Out button
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to log out?", "Log Out", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    setVisible(false);  // Hide Welcome Form
                    new login();  // Open the Login Form again
                }
            }
        });
    }

    public static void main(String[] args) {
    }
}
