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
    private JButton publishersButton;
    private final User currentUser;

    public Welcome(User user) {
        // Set up the main window
        this.currentUser = user;  // Set the current user
        setTitle("Welcome - Library Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create and customize the panel
        panel = new JPanel();
        panel.setLayout(new GridLayout(7, 1, 10, 10));
        panel.setBackground(new Color(255, 228, 225));

        // Load and resize the logo image
        ImageIcon logoIcon = new ImageIcon("src/photos/LetsMoveIcon.jpg"); // Path to the image file
        if (logoIcon.getIconWidth() == -1) {
            System.out.println("Error loading image.");
        } else {
            Image imgResized = logoIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Resize image to 50x50 pixels
            img = new JLabel(new ImageIcon(imgResized));
            panel.add(img);
        }

        // Add the welcome message
        first_name = new JLabel("Welcome to the Library Management System, " + user.getName() + "!");
        first_name.setFont(new Font("Arial", Font.BOLD, 18));
        first_name.setForeground(new Color(0, 102, 204));  
        panel.add(first_name);

        // Set up and style buttons
        viewBooksButton = new JButton("View Books");
        viewBooksButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewBooksButton.setBackground(new Color(0, 102, 204));
        viewBooksButton.setForeground(Color.WHITE);
        viewBooksButton.setFocusPainted(false);
        panel.add(viewBooksButton);

        borrowBooksButton = new JButton("Borrow Books");
        borrowBooksButton.setFont(new Font("Arial", Font.BOLD, 14));
        borrowBooksButton.setBackground(new Color(0, 153, 51)); 
        borrowBooksButton.setForeground(Color.WHITE);
        borrowBooksButton.setFocusPainted(false);
        panel.add(borrowBooksButton);

        viewFinesButton = new JButton("View Fines");
        viewFinesButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewFinesButton.setBackground(new Color(255, 140, 0));
        viewFinesButton.setForeground(Color.WHITE);
        viewFinesButton.setFocusPainted(false);
        panel.add(viewFinesButton);

        publishersButton = new JButton("View Publishers");
        publishersButton.setFont(new Font("Arial", Font.BOLD, 14));
        publishersButton.setBackground(new Color(102, 51, 255)); 
        publishersButton.setForeground(Color.WHITE);
        publishersButton.setFocusPainted(false);
        panel.add(publishersButton);  

        logOutButton = new JButton("Log Out");
        logOutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logOutButton.setBackground(new Color(255, 69, 0));  
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
                new BookForm(user); 
                setVisible(false);  // Hide Welcome Form
            }
        });

        borrowBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BorrowBookForm(user); 
                setVisible(false);  // Hide Welcome Form
            }
        });

        viewFinesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new FinesForm(user);  
                setVisible(false);  // Hide Welcome Form
            }
        });

        publishersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PublisherForm(user);  // Open PublisherForm when button is clicked
                setVisible(false);  // Hide Welcome Form
            }
        });

        logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
