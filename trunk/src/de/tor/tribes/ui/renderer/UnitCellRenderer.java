/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.ui.ImageManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Torridity
 */
public class UnitCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        try {
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            if (value == null) {
                //no icon!?
                ((JLabel) c).setText("-");
                ((JLabel) c).setIcon(null);
            } else {
                ((JLabel) c).setText("");
                ((JLabel) c).setIcon(ImageManager.getUnitIcon((UnitHolder) value));
            }
        } catch (Exception e) {
            //cast problem
        }

        return c;
    }
}
