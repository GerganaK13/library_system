import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
public class form1 extends JFrame { private JPanel panel1;
    private JTable table1;
    private JComboBox comboBox1;
    private JButton addColumnButton;
    private JComboBox comboBox2;
    public static DefaultTableModel model;
    private ArrayList<String[]> users;
    private JButton backButton;

    public form1() {
        setSize(700, 400);
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        model = new DefaultTableModel();
        table1.setModel(model);

        users = connect.executeQuery("SELECT * FROM Users");
        updateTable();

        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable();
            }
        });

        table1.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()) {
            private String oldValue;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                oldValue = (value != null) ? value.toString() : "";
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            @Override
            public boolean stopCellEditing() {
                String newValue = getCellEditorValue().toString();
                int row = table1.getEditingRow();
                int column = table1.getEditingColumn();
                String columnName = table1.getColumnName(column);
                String userIDStr = (String) table1.getValueAt(row, 0); 
                if (!newValue.equals(oldValue)) {
                    String[] columns = {columnName};
                    String[] newValues = {newValue};
                    try {
                        int userID = Integer.parseInt(userIDStr);
                        connect.updateUser(userID, columns, newValues);
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid UserID format.");
                    }
                }
                return super.stopCellEditing();
            }
        });

        addColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!comboBox2.getSelectedItem().toString().equals("select")) {
                    String newColumn = JOptionPane.showInputDialog("Enter the name of the new column");
                    String dataType = comboBox2.getSelectedItem().toString();
                    if (newColumn != null && !newColumn.trim().isEmpty()) {
                        connect.addColumn(newColumn, dataType);
                        model.addColumn(newColumn);
                        users = connect.executeQuery("SELECT * FROM Users");
                        updateTable();
                    }
                }
            }
        });
    }

    private void updateTable() {
        model.setRowCount(0);
        for (String[] user : users) {
            model.addRow(user);
        }
    }
}
