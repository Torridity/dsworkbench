/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
