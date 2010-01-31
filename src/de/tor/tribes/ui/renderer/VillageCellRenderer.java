/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
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
        Village vil = (Village) value;

        try {
            label.setText(vil.toString());
            label.setToolTipText(vil.getToolTipText());
        } catch (Exception e) {
            label.setText("Ungültig");
        }
        return label;
    }
}
