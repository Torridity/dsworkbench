/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Torridity
 */
public class MultiColorCellRenderer implements TableCellRenderer {

    public MultiColorCellRenderer() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        MultiColorLabel c = new MultiColorLabel();
        c.setText(value.toString());
        return c;
    }
}
