import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class login extends JFrame {
    private JTextField textField1; // UserID
    private JPasswordField textField2; // Password
    private JButton loginButton;
    private JPanel panel;
    private JLabel img;

    public login() {
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245)); 

        // Logo Image
        ImageIcon logoIcon = new ImageIcon("src/photos/login.jpg");  
        JLabel imageLabel = new JLabel(logoIcon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(imageLabel);

        // Input fields
        textField1 = new JTextField(20);
        textField2 = new JPasswordField(20);
        textField1.setFont(new Font("Arial", Font.PLAIN, 14));
        textField2.setFont(new Font("Arial", Font.PLAIN, 14));
        textField1.setPreferredSize(new Dimension(250, 40));
        textField2.setPreferredSize(new Dimension(250, 40));
        panel.add(createFieldPanel("User ID", textField1));
        panel.add(createFieldPanel("Password", textField2));

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 102, 204));  
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(200, 40));
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement login functionality
                String userIdInput = textField1.getText();
                String passwordInput = textField2.getText();
                User user = connect.login(userIdInput, passwordInput);
                if (user != null) {
                    JOptionPane.showMessageDialog(null, "Successfully logged in as " + user.getName());
                    new Welcome(user);
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials.");
                }
            }
        });

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
        new login();
    }
}
