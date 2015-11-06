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

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.Village;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class TAPResultDetailsTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Herkunft", "Ziel", "Einheit", "Start", "Ankunft", "Typ"
    };
    private Class[] types = new Class[]{
        Village.class, Village.class, UnitHolder.class, Date.class, Date.class, Integer.class
    };
    private final List<Attack> elements = new LinkedList<Attack>();

    public TAPResultDetailsTableModel() {
        super();
    }

    public Attack getRow(int pRow) {
        return elements.get(pRow);
    }

    public void addRow(Attack pMovement) {
        elements.add(pMovement);
        fireTableDataChanged();
    }

    public void clear() {
        elements.clear();
        fireTableDataChanged();
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

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        Attack element = elements.get(row);
        switch (column) {
            case 0:
                return element.getSource();
            case 1:
                return element.getTarget();
            case 2:
                return element.getUnit();
            case 3:
                return element.getSendTime();
            case 4:
                return element.getArriveTime();
            default:
                return element.getType();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
