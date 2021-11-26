package general.useful;

import general.ActionName;
import general.AxisName;
import general.Policy;
import general.State;
import world.Environment;
import world.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.HashMap;

public class PaintWorldNew extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 7103983540575706971L;

    private int columns;
    private int rows;
    public JTable mainTable = new JTable();
    private final JPanel panel = new JPanel();
    public HashMap<JTable, CellInfo[][]> tables = new HashMap<>();
    public int nextTablePositionX;
    public int nextTablePositionY;
    public String[] columnNames;

    /**
     * Create the frame.
     */
    public PaintWorldNew(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        nextTablePositionX = 3;
        nextTablePositionY = 0;
        columnNames = new String[columns];
        for (int i = 0 ;i<columns ;i++){
            columnNames[0]= i + "";
        }
        mainTable = addTable();
    }

    private void initGUI(JTable table) {
        JPanel contentPane;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(1000, 100,
                Integer.parseInt(Main.propertiesFile.getProperties().get(PropertyTypeEnum.WORLD_WIDTH))*100,
                Integer.parseInt(Main.propertiesFile.getProperties().get(PropertyTypeEnum.WORLD_WIDTH))*100);
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
        gbc_panel.gridx = nextTablePositionX;
        gbc_panel.gridy = nextTablePositionY;
        contentPane.add(panel, gbc_panel);

        panel.add(table);
        DefaultTableModel kati = new DefaultTableModel();
        for (int i = 0; i < columns; i++) {
            kati.addColumn(new String[i]);

        }
        TableModel model = new DefaultTableModel(tables.get(table), columnNames){
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        table.setDefaultRenderer(Object.class, new Renderer());
        table.setVisible(true);
        for (int i = 0; i < rows; i++) {
            kati.addRow(new String[i]);

        }
        for (int i = 0; i < kati.getRowCount(); i++)
            for (int j = 0; j < kati.getColumnCount(); j++) {
                //"("+j+", "+i+ ")"   Character.toChars(0x2B1B)
                kati.setValueAt(" ", /*kati.getRowCount() - 1-*/ i , j);
            }
        table.setModel(kati);
        table.setFont(new Font("", Font.PLAIN, 19));
        table.setRowHeight(23);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumn(kati.getColumnName(i)).setPreferredWidth(50);
        if(tables.size()==1) nextTablePositionX += columns * table.getColumn(kati.getColumnName(0)).getPreferredWidth();
        nextTablePositionY += columns * table.getRowHeight();

    }

    public void changeCellColor(JTable table, int row, int column, Color color, String display){
        tables.get(table)[row][column].color = color;
        tables.get(table)[row][column].display = display;
    }

    public JTable addTable() {
        JTable table= new JTable();
        CellInfo[][] data = new CellInfo[rows][columns];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < columns; j++) {
                //"("+j+", "+i+ ")"   Character.toChars(0x2B1B)
                data[i][j]= new CellInfo(" ");
            }
        tables.put(table, data);
        initGUI(table);
        return table;
    }

    protected void printPolicyOnResultTable(JTable table, Policy policy) {
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
            table.setValueAt(print, /*World.columns -1 - */ (Integer) s.getStateProperties().get(AxisName.Y), (Integer) s.getStateProperties().get(AxisName.X));
        }
        table.setValueAt("E", /*World.columns -1 - */ (Integer) Environment.getEndState().getStateProperties().get(AxisName.Y), (Integer) Environment.getEndState().getStateProperties().get(AxisName.X));
    }

}

// taken from https://stackoverflow.com/questions/47598510/how-to-single-out-a-cell-in-a-jtable-and-change-its-properties-without-resetting
class Renderer implements TableCellRenderer{
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JTextField editor = new JTextField();
        if (value instanceof CellInfo)
        {
            CellInfo info = (CellInfo) value;
            editor.setText(info.display);

            if (info.bOrF == 'b'){
                editor.setBackground(info.color);
            }
            else if(info.bOrF == 'f'){
                editor.setForeground(info.color);
            }
        }
        return editor;
    }
}

class CellInfo
{
    String display;
    char bOrF = ' ';
    Color color = Color.black;

    public CellInfo(String display)
    {
        this.display = display;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public void setBorF(char bOrF)
    {
        this.bOrF = bOrF;
    }
}
