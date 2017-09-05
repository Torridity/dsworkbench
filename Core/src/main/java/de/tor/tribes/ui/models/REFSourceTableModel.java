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
package de.tor.tribes.ui.models;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.ref.types.REFSourceElement;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author Torridity
 */
public class REFSourceTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Dorf", "Verfügbare Unterstützungen"
    };
    Class[] types = new Class[]{
        Village.class, Integer.class
    };
    private final List<REFSourceElement> elements = new LinkedList<>();

    public void clear() {
        elements.clear();
        fireTableDataChanged();
    }

    public void addRow(Village pVillage) {
        elements.add(new REFSourceElement(pVillage));
        fireTableDataChanged();
    }

    public boolean removeRow(final Village pVillage, boolean pNotify) {

        REFSourceElement elem = (REFSourceElement) CollectionUtils.find(elements, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((REFSourceElement) o).getVillage().equals(pVillage);
            }
        });

        if (elem != null) {
            elements.remove(elem);
        }

        if (pNotify) {
            fireTableDataChanged();
        }
        return elem != null;
    }

    @Override
    public int getRowCount() {
        if (elements == null) {
            return 0;
        }
        return elements.size();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void removeRow(int row) {
        elements.remove(row);
        fireTableDataChanged();
    }

    public REFSourceElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        REFSourceElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage();
            default:
                return element.getAvailableSupports();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
