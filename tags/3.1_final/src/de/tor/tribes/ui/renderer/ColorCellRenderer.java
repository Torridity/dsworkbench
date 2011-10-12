/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Charon
 */
public class ColorCellRenderer extends DefaultTableRenderer {

    private boolean bMarkSelection = true;

    public ColorCellRenderer() {
    }

    public ColorCellRenderer(boolean pMarkSelection) {
        bMarkSelection = pMarkSelection;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel l = (JLabel) c;

        Color selectColor = (Color) value;
        if (isSelected && bMarkSelection && selectColor != null) {
            selectColor = selectColor.darker();
        }
        l.setBackground(selectColor);
        l.setForeground(selectColor);
        l.setText("");
        return l;
    }
}
