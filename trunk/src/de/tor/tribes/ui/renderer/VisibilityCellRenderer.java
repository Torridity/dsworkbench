/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 *
 * @author Torridity
 */
public class VisibilityCellRenderer extends SubstanceDefaultTableCellRenderer {

    private ImageIcon visible = null;
    private ImageIcon invisible = null;

    public VisibilityCellRenderer() {
        visible = new ImageIcon(VisibilityCellRenderer.class.getResource("/res/ui/eye.png"));
        invisible = new ImageIcon(VisibilityCellRenderer.class.getResource("/res/ui/eye_forbidden.png"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        label.setText("");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        if ((Boolean) value) {
            label.setIcon(visible);
        } else {
            label.setIcon(invisible);
        }
        return label;
    }
}
