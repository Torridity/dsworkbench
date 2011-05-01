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
import org.apache.log4j.Logger;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 * @author Jejkal
 */
public class AttackTypeCellRenderer extends DefaultTableRenderer {

    private static Logger logger = Logger.getLogger("AttackDialog (TypeRenderer)");
    private List<ImageIcon> icons = null;

    public AttackTypeCellRenderer() {
        try {
            icons = new LinkedList<ImageIcon>();
            icons.add(new ImageIcon("./graphics/icons/axe.png"));
            icons.add(new ImageIcon("./graphics/icons/snob.png"));
            icons.add(new ImageIcon("./graphics/icons/def.png"));
            icons.add(new ImageIcon("./graphics/icons/fake.png"));
            icons.add(new ImageIcon("./graphics/icons/def_fake.png"));
            icons.add(new ImageIcon("./graphics/icons/spy.png"));
        } catch (Exception e) {
            icons = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Integer type = (Integer) value;
        if (type == null || type == 0) {
            //no icon!?
            label.setText("-");
            label.setIcon(null);
        } else {
            int pos = type - 1;
            if (pos >= 0) {
                label.setText("");
                label.setIcon(icons.get(pos));
            } else {
                label.setText("-");
                label.setIcon(null);
            }
        }
        return c;
    }
}
