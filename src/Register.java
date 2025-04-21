import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import org.mindrot.jbcrypt.BCrypt;

public class Register extends JFrame {
    private JTextField textField1; // Name
    private JTextField textField2; // Contact
    private JPasswordField textField3; // Password
    private JButton registerButton;
    private JPanel panel;
    private JButton loginInsteadButton;
    private JLabel img;

    public Register() {
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245)); // Light gray background

        // Add fields
        textField1 = new JTextField(20);
        textField2 = new JTextField(20);
        textField3 = new JPasswordField(20);
        textField1.setFont(new Font("Arial", Font.PLAIN, 14));
        textField2.setFont(new Font("Arial", Font.PLAIN, 14));
        textField3.setFont(new Font("Arial", Font.PLAIN, 14));
        textField1.setPreferredSize(new Dimension(250, 40));
        textField2.setPreferredSize(new Dimension(250, 40));
        textField3.setPreferredSize(new Dimension(250, 40));
        panel.add(createFieldPanel("Name", textField1));
        panel.add(createFieldPanel("Contact", textField2));
        panel.add(createFieldPanel("Password", textField3));

        // Register Button
        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBackground(new Color(0, 102, 204));  // Blue background
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(200, 40));
        panel.add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = textField1.getText();
                String contact = textField2.getText();
                String password = textField3.getText();

                if (name.isEmpty() || contact.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields.");
                    return;
                }

                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                int userID = (int) (Math.random() * 100000);
                String role = "Student";
                int borrowLimit = 3;

                // Add user to database
                connect.addUser(userID, name, role, contact, borrowLimit, hashedPassword, null);
                JOptionPane.showMessageDialog(null, "Registered successfully! Your ID is: " + userID);
                new login();
                setVisible(false);
            }
        });

        loginInsteadButton = new JButton("Login Instead");
        loginInsteadButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginInsteadButton.setBackground(new Color(0, 102, 204));  // Blue background
        loginInsteadButton.setForeground(Color.WHITE);
        loginInsteadButton.setPreferredSize(new Dimension(200, 40));
        panel.add(loginInsteadButton);

        loginInsteadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new login();
                setVisible(false);
            }
        });

        // Image (Logo)
        ImageIcon registerIcon = new ImageIcon("src/photos/register.png");  // Path to the register image
        Image imgResized = registerIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(imgResized));  // Set the resized image to JLabel
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(imageLabel);

        add(panel);
        setVisible(true);
    }

    private JPanel createFieldPanel(String label, JTextField textField) {
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel(label));
        panel.add(textField);
        return panel;
    }

    public static void main(String[] args) {
        new Register();
    }
}
