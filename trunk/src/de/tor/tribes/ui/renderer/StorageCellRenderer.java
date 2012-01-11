/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.ui.components.FillingLabel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class StorageCellRenderer extends DefaultTableRenderer {

    private DefaultTableCellRenderer renderer = null;
    private FillingLabel label = null;

    public StorageCellRenderer() {
        super();
        renderer = new DefaultTableCellRenderer();
        label = new FillingLabel();
        label.setOpaque(true);
        label.setColors(new Color[]{new Color(187, 148, 70), new Color(242, 131, 30), new Color(224, 211, 209)});
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel defaultLabel = ((JLabel) c);
        label.setBackground(defaultLabel.getBackground());
        try {
            StorageStatus status = (StorageStatus) value;
            label.setData(new double[]{status.getWoodStatus(), status.getClayStatus(), status.getIronStatus()});
        } catch (Exception e) {
        }
        return label;
    }
}
