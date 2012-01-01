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
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class DrawNotDrawCellRenderer extends DefaultTableRenderer {

    private ImageIcon draw = null;
    private ImageIcon notDraw = null;

    public DrawNotDrawCellRenderer() {
        super();
        try {
            draw = new ImageIcon(SentNotSentCellRenderer.class.getResource("/res/ui/draw_small.gif"));
            notDraw = new ImageIcon(SentNotSentCellRenderer.class.getResource("/res/ui/not_draw_small.gif"));
        } catch (Exception e) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        label.setText(null);
        if ((Boolean) value) {
            label.setIcon(draw);
        } else {
            label.setIcon(notDraw);
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
