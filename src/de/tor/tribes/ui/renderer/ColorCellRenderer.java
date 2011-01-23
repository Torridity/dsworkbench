/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Charon
 */
public class ColorCellRenderer implements TableCellRenderer {

    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    private boolean bMarkSelection = true;

    public ColorCellRenderer() {
    }

    public ColorCellRenderer(boolean pMarkSelection) {
        bMarkSelection = pMarkSelection;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color selectColor = (Color) value;
        if (isSelected && bMarkSelection) {
            selectColor = selectColor.darker();
        }
        c.setBackground(selectColor);
        ((JLabel) c).setText("");
        return c;
    }
}
