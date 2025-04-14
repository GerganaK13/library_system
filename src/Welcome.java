import javax.swing.*;

public class Welcome extends JFrame {

    private JLabel img;
    private JLabel first_name;
    private JPanel panel;

    public Welcome(User user) {
        setSize(500, 500);
        setContentPane(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        first_name.setText("Welcome to the library management system, " + user.getName() + "!");

        }
    }

