/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.FarmInformation;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class ResourcesInStorageCellRenderer extends DefaultTableRenderer {

    private ImageIcon availableIcon = null;
    private ImageIcon emptyIcon = null;

    public ResourcesInStorageCellRenderer() {
        super();
        try {
            availableIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/res.png"));
            emptyIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/nores.png"));

        } catch (Exception e) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);
        try {
            label.setText("");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            Boolean result = (Boolean) value;
            if (result) {
                label.setIcon(availableIcon);
            } else {
                label.setIcon(emptyIcon);
            }
        } catch (Exception e) {
            label.setText("?");
            label.setIcon(null);
        }
        return label;
    }
}
