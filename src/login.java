import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class login extends JFrame {
    private JTextField textField2; // Парола
    private JTextField textField1; // UserID
    private JButton loginButton;
    private JPanel panel;

    public login() {
        setSize(500, 500);
        setContentPane(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userIdInput = textField1.getText();
                String passwordInput = textField2.getText();

                User user = connect.login(userIdInput, passwordInput);
                if (user != null) {
                    JOptionPane.showMessageDialog(null,
                            "Successfully logged in as " + user.getName(),
                            "Login Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    new Welcome(user);
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Login failed. Invalid credentials.",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
