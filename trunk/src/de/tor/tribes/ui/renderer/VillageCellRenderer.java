/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.VillageRenderPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, 2));

        Village vil = (Village) value;
        JLabel label = (JLabel) c;
        try {
            label.setText(vil.toString());
            label.setToolTipText(vil.getToolTipText());
        } catch (Exception e) {
            label.setText("Ungültig");
        }
        return c;
    }
}
