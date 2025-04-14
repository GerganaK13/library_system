import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Register extends JFrame {
    private JTextField textField1; // Name
    private JTextField textField2; // Contact
    private JTextField textField3; // Password
    private JButton registerButton;
    private JPanel panel;
    private JButton loginInsteadButton;

    public Register() {
        setSize(500, 500);
        setContentPane(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = textField1.getText();
                String contact = textField2.getText();
                String password = textField3.getText();

                if (name.isEmpty() || contact.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Fill all the fields!");
                    return;
                }

                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                int userID = (int) (Math.random() * 100000);
                String role = "Student";
                int borrowLimit = 3;

                // Вече не изпращаме InputStream, защото няма снимка
                connect.addUser(userID, name, role, contact, borrowLimit, hashedPassword, null);
                JOptionPane.showMessageDialog(null, "Registered successfully! Your ID is: " + userID);
                new login();
                setVisible(false);
            }
        });

        loginInsteadButton.addActionListener(e -> {
            new login();
            setVisible(false);
        });
    }
}
