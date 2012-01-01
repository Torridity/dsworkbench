/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Village;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class VillageCellRenderer extends DefaultTableRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        Village vil = (Village) value;
        label.setText(vil.toString());
        label.setToolTipText(vil.getToolTipText());
        return label;
    }
}
