/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Village;
import de.tor.tribes.util.Constants;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Torridity
 */
public class VillageCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        try {
            Village vil = (Village) value;

            if (!isSelected) {
                if (row % 2 == 0) {
                    label.setBackground(Constants.DS_ROW_B);
                } else {
                    label.setBackground(Constants.DS_ROW_A);
                }
            }

            label.setText(vil.toString());
            label.setToolTipText(vil.getToolTipText());
        } catch (Exception e) {
            label.setText("Ungültig");
        }
        return c;
    }
}
