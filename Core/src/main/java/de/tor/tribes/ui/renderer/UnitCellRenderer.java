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
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class UnitCellRenderer extends DefaultTableRenderer {

    public UnitCellRenderer() {
        super();

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        UnitHolder unit = (UnitHolder) value;
        JLabel label = (JLabel) c;
        label.setText("");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setIcon(ImageManager.getUnitIcon(unit));
        return label;
    }
}
