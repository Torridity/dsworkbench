/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;

/**
 * @author Jejkal
 */
public class AttackTypeCellRenderer implements TableCellRenderer {

    private static Logger logger = Logger.getLogger("AttackDialog (TypeRenderer)");
    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    private List<ImageIcon> icons = null;

    public AttackTypeCellRenderer() {
        try {
            icons = new LinkedList<ImageIcon>();
            icons.add(new ImageIcon("./graphics/icons/axe.png"));
            icons.add(new ImageIcon("./graphics/icons/snob.png"));
            icons.add(new ImageIcon("./graphics/icons/def.png"));
            icons.add(new ImageIcon("./graphics/icons/fake.png"));
        } catch (Exception e) {
            logger.warn("Failed to load attack type icons");
            icons = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
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
        return c;
    }
}
