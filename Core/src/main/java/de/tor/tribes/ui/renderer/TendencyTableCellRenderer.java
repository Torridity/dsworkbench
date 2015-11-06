/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class TendencyTableCellRenderer extends DefaultTableRenderer {

    private ImageIcon yellow = null;
    private ImageIcon red = null;
    private ImageIcon green = null;

    public TendencyTableCellRenderer() {
        yellow = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/yellow_arrow_horizontal.png"));
        red = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/red_arrow_up.png"));
        green = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/green_arrow_down.png"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        Integer val = (Integer) value;
        String text = "";
        if (val == 0) {
            label.setIcon(yellow);
        } else if (val > 0) {
            label.setIcon(red);
            text = "(+ " + val + ")";
        } else if (val < 0) {
            label.setIcon(green);
            text = "(" + val + ")";
        }
        label.setText(StringUtils.center(text, 9));
        return label;
    }
}
