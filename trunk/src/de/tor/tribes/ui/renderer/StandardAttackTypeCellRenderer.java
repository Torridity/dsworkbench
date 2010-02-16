/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
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
 *
 * @author Torridity
 */
public class StandardAttackTypeCellRenderer implements TableCellRenderer {

    private static Logger logger = Logger.getLogger("AttackDialog (StandardAttackTypeRenderer)");
    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    private List<ImageIcon> icons = null;

    public StandardAttackTypeCellRenderer() {
        try {
            icons = new LinkedList<ImageIcon>();
            icons.add(new ImageIcon("./graphics/icons/axe.png"));
            icons.add(new ImageIcon("./graphics/icons/snob.png"));
            icons.add(new ImageIcon("./graphics/icons/def.png"));
            icons.add(new ImageIcon("./graphics/icons/fake.png"));
            icons.add(new ImageIcon("./graphics/icons/def_fake.png"));
        } catch (Exception e) {
            logger.warn("Failed to load attack type icons");
            icons = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {
            String type = (String) value;
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            if (type.equals("Keiner")) {
                //no icon!?
                ((JLabel) c).setText("-");
                ((JLabel) c).setIcon(null);
            } else if (type.equals("Off")) {
                ((JLabel) c).setText("");
                ((JLabel) c).setIcon(icons.get(0));
            } else if (type.equals("AG")) {
                ((JLabel) c).setText("");
                ((JLabel) c).setIcon(icons.get(1));
            } else if (type.equals("Unterstützung")) {
                ((JLabel) c).setText("");
                ((JLabel) c).setIcon(icons.get(2));
            } else if (type.equals("Fake")) {
                ((JLabel) c).setText("");
                ((JLabel) c).setIcon(icons.get(3));
            } else if (type.equals("Fake (Deff)")) {
                ((JLabel) c).setText("");
                ((JLabel) c).setIcon(icons.get(4));
            }
        } catch (Exception e) {
            //cast problem
        }
        return c;
    }
}
