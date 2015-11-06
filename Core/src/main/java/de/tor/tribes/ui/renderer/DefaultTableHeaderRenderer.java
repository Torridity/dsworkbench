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

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Torridity
 */
public class DefaultTableHeaderRenderer extends DefaultTableCellRenderer implements UIResource {

    private boolean horizontalTextPositionSet;

    public DefaultTableHeaderRenderer() {
        setHorizontalAlignment(JLabel.CENTER);
    }

    public void setHorizontalTextPosition(int textPosition) {
        horizontalTextPositionSet = true;
        super.setHorizontalTextPosition(textPosition);
    }

    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Icon sortIcon = null;
       // setBackground(Constants.DS_BACK);
        boolean isPaintingForPrint = false;

        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                Color fgColor = null;
                Color bgColor = null;
                if (hasFocus) {
                    fgColor = UIManager.getColor("TableHeader.focusCellForeground");
                    bgColor = UIManager.getColor("TableHeader.focusCellBackground");
                }
                if (fgColor == null) {
                    fgColor = header.getForeground();
                }
                if (bgColor == null) {
                    bgColor = header.getBackground();
                }

                setFont(header.getFont());
                isPaintingForPrint = header.isPaintingForPrint();
            }

            if (!isPaintingForPrint && table.getRowSorter() != null) {
                if (!horizontalTextPositionSet) {
                    // There is a row sorter, and the developer hasn't
                    // set a text position, change to leading.
                    setHorizontalTextPosition(JLabel.LEADING);
                }
                java.util.List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
                if (sortKeys.size() > 0
                        && sortKeys.get(0).getColumn() == table.convertColumnIndexToModel(column)) {
                    switch (sortKeys.get(0).getSortOrder()) {
                        case ASCENDING:
                            sortIcon = UIManager.getIcon("Table.ascendingSortIcon");
                            break;
                        case DESCENDING:
                            sortIcon = UIManager.getIcon("Table.descendingSortIcon");
                            break;
                        case UNSORTED:
                            sortIcon = UIManager.getIcon("Table.naturalSortIcon");
                            break;
                    }
                }
            }
        }

        setText(value == null ? "" : value.toString());
        setIcon(sortIcon);

        Border border = null;
        if (hasFocus) {
            border = UIManager.getBorder("TableHeader.focusCellBorder");
        }
        if (border == null) {
            border = UIManager.getBorder("TableHeader.cellBorder");
        }
        setBorder(border);

        return this;
    }
}
