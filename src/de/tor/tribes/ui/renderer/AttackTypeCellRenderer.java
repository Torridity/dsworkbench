/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import java.awt.Component;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;

/**
 * @author Jejkal
 */
public class AttackTypeCellRenderer extends JComboBox implements TableCellRenderer {

    private static Logger logger = Logger.getLogger("AttackDialog (TypeRenderer)");
   // private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    //private List<ImageIcon> icons = null;

    public AttackTypeCellRenderer() {
        /*try {
        icons = new LinkedList<ImageIcon>();
        icons.add(new ImageIcon("./graphics/icons/axe.png"));
        icons.add(new ImageIcon("./graphics/icons/snob.png"));
        icons.add(new ImageIcon("./graphics/icons/def.png"));
        icons.add(new ImageIcon("./graphics/icons/fake.png"));
        icons.add(new ImageIcon("./graphics/icons/def_fake.png"));
        icons.add(new ImageIcon("./graphics/icons/spy.png"));
        } catch (Exception e) {
        logger.warn("Failed to load attack type icons");
        icons = null;
        }*/
        super();
        setRenderer(new AttackTypeListCellRenderer());
        setBackground(Constants.DS_BACK);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        /*Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {
        Integer type = (Integer) value;
        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        if (type == 0) {
        //no icon!?
        ((JLabel) c).setText("-");
        ((JLabel) c).setIcon(null);
        } else {
        int pos = type - 1;
        if (pos >= 0) {
        ((JLabel) c).setText("");
        ((JLabel) c).setIcon(icons.get(pos));
        } else {
        ((JLabel) c).setText("-");
        ((JLabel) c).setIcon(null);
        }
        }
        } catch (Exception e) {
        //cast problem
        }
        return c;*/

        setModel(new DefaultComboBoxModel(new Object[]{value}));
        setBorder(BorderFactory.createEmptyBorder());
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        setSelectedItem(value);
        return this;
    }
}
