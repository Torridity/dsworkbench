/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import java.awt.Component;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Torridity
 */
public class SortableTableHeaderRenderer extends DefaultTableCellRenderer {

    private final Icon ascIcon = UIManager.getIcon("Table.ascendingSortIcon");
    private final Icon descIcon = UIManager.getIcon("Table.descendingSortIcon");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setBackground(Constants.DS_BACK);
        DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
        r.setText("<html><b>" + r.getText() + "</b></html>");
        try {
            List<? extends SortKey> sortKeys = table.getRowSorter().getSortKeys();
            SortKey key = sortKeys.get(0);
            if (column == key.getColumn()) {
                r.setIcon(key.getSortOrder() == SortOrder.ASCENDING ? ascIcon : descIcon);
            } else {
                r.setIcon(null);
            }
        } catch (Exception e) {
            r.setIcon(null);
        }
        return r;
    }
}
