package general.useful;

import general.ActionName;
import general.AxisName;
import general.Policy;
import general.State;
import world.Environment;
import world.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class Paint extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 7103983540575706971L;

    private final JPanel panel = new JPanel();
    public JTable table1 = new JTable();
    Color color = Color.green;
    /**
     * Create the frame.
     */
    public Paint(int columns, int rows) {
        initGUI( columns, rows);
    }

    private void initGUI(int columns, int rows) {
        JPanel contentPane;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100 , 100, Integer.parseInt(Main.propertiesFile.getProperties().get(PropertyTypeEnum.WORLD_WIDTH))*55,
                Integer.parseInt(Main.propertiesFile.getProperties().get(PropertyTypeEnum.WORLD_HEIGHT))*35);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{0, 0};
        gbl_contentPane.rowHeights = new int[]{0, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 0;
        contentPane.add(panel, gbc_panel);

        panel.add(table1);
        DefaultTableModel kati = new DefaultTableModel();
        for (int i = 0; i < columns; i++) {
            kati.addColumn(new String[i]);

        }
        for (int i = 0; i < rows; i++) {
            kati.addRow(new String[i]);

        }
        for (int i = 0; i < kati.getColumnCount(); i++)
            for (int j = 0; j < kati.getRowCount(); j++) {
                //"("+j+", "+i+ ")"   Character.toChars(0x2B1B)
                StatusColumnCellRenderer sr = new StatusColumnCellRenderer();
//                JLabel l = new JLabel();
//                l.setBackground(color);
//                l.setText(" O ");
//                sr.setLabelFor(l);
                kati.setValueAt(" O ", /*kati.getRowCount() -*/ i, j);
            }
        table1.setModel(kati);
        table1.setFont(new Font("", Font.PLAIN, 19));
        table1.setRowHeight(23);
        for (int i = 0; i < table1.getColumnCount(); i++)
            table1.getColumn(kati.getColumnName(i)).setPreferredWidth(50);
    }

    public void printPolicyOnResultTable(JTable t, Policy policy) {
        for (State s : policy.getBestActions().keySet()) {
            String print = "";

            if (policy.getBestActions().get(s) != null)
                if (policy.getBestActions().get(s).equals(ActionName.UP)) {
                    print = ActionName.UP.getArrow();
                } else if (policy.getBestActions().get(s).equals(ActionName.DOWN)) {
                    print = ActionName.DOWN.getArrow();
                } else if (policy.getBestActions().get(s).equals(ActionName.RIGHT)) {
                    print = ActionName.RIGHT.getArrow();
                } else if (policy.getBestActions().get(s).equals(ActionName.LEFT)) {
                    print = ActionName.LEFT.getArrow();
                }

//            Component c = this.table1.getCellRenderer((Integer) s.getStateProperties().get(AxisName.Y), (Integer) s.getStateProperties().get(AxisName.X)).getTableCellRendererComponent(table1,null,false,false,(Integer) s.getStateProperties().get(AxisName.Y), (Integer) s.getStateProperties().get(AxisName.X));
//            JLabel l = new JLabel();
//            l.setBackground(c.getBackground().brighter());
//            l.setText(print);
            this.table1.setValueAt(print, /*World.columns - 1 -*/ (Integer) s.getStateProperties().get(AxisName.Y), (Integer) s.getStateProperties().get(AxisName.X));
//            this.table1.getColumnModel().getColumn((Integer) s.getStateProperties().get(AxisName.X)).setCellRenderer(new StatusColumnCellRenderer());
            }
        this.table1.setValueAt("E", /*World.columns -1 - */ (Integer) Environment.getEndState().getStateProperties().get(AxisName.Y), (Integer) Environment.getEndState().getStateProperties().get(AxisName.X));
    }

        public class StatusColumnCellRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                //Cells are by default rendered as a JLabel.
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                //Get the status for the current row.
                TableModel tableModel = table.getModel();
                l.setBackground(Color.getHSBColor(l.getBackground().getRed(), l.getBackground().getGreen() + 0.001f, l.getBackground().getBlue()));

                //Return the JLabel which renders the cell.
                return l;

            }
        }
}
