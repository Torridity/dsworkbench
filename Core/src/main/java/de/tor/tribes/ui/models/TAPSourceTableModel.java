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
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackSourceElement;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class TAPSourceTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Spieler", "Dorf", "Einheit", "Fake"
    };
    private Class[] types = new Class[]{
        Tribe.class, Village.class, UnitHolder.class, Boolean.class
    };
    private final List<TAPAttackSourceElement> elements = new LinkedList<>();

    public TAPSourceTableModel() {
        super();
    }

    public void clear(){
        elements.clear();
        fireTableDataChanged();
    }
    
    public void addRow(final Village pVillage, UnitHolder pUnit, boolean pFake) {
        elements.add(new TAPAttackSourceElement(pVillage, pUnit, pFake));
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

    public void removeRow(int row) {
        elements.remove(row);
        fireTableDataChanged();
    }

    public TAPAttackSourceElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        TAPAttackSourceElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage().getTribe();
            case 1:
                return element.getVillage();
            case 2:
                return element.getUnit();
            default:
                return element.isFake();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
