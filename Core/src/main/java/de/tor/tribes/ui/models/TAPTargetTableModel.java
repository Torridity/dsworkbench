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

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackTargetElement;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

/**
 *
 * @author Torridity
 */
public class TAPTargetTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Spieler", "Dorf", "Fake", "Angriffe"
    };
    private Class[] types = new Class[]{
        Tribe.class, Village.class, Boolean.class, Integer.class
    };
    private final List<TAPAttackTargetElement> elements = new LinkedList<>();

    public TAPTargetTableModel() {
        super();
    }

    public void clear() {
        elements.clear();
        fireTableDataChanged();
    }

    public void addRow(final Village pVillage, boolean pFake, int pAmount) {
        Object result = CollectionUtils.find(elements, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((TAPAttackTargetElement) o).getVillage().equals(pVillage);
            }
        });

        if (result == null) {
            elements.add(new TAPAttackTargetElement(pVillage, pFake, pAmount));
        } else {
            TAPAttackTargetElement resultElem = (TAPAttackTargetElement) result;
            resultElem.setAttacks(pAmount);
            resultElem.setFake(pFake);
        }
        fireTableDataChanged();
    }

    public void addRow(final Village pVillage, boolean pFake) {
        addRow(pVillage, pFake, 1);
    }

    public void increaseRowCount(final Village pVillage) {
        Object result = CollectionUtils.find(elements, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((TAPAttackTargetElement) o).getVillage().equals(pVillage);
            }
        });

        if (result != null) {
            TAPAttackTargetElement resultElem = (TAPAttackTargetElement) result;
            resultElem.addAttack();
            fireTableDataChanged();
        }
    }

    public void decreaseRowCount(final Village pVillage) {
        Object result = CollectionUtils.find(elements, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((TAPAttackTargetElement) o).getVillage().equals(pVillage);
            }
        });

        if (result != null) {
            TAPAttackTargetElement resultElem = (TAPAttackTargetElement) result;
            resultElem.removeAttack();
            fireTableDataChanged();
        }
    }

    public void removeTargets(List<Village> pToRemove) {
        for (TAPAttackTargetElement elem : elements.toArray(new TAPAttackTargetElement[elements.size()])) {
            if (pToRemove.contains(elem.getVillage())) {
                elements.remove(elem);
            }
        }
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
        return column == 3;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void removeRow(int row) {
        elements.remove(row);
        fireTableDataChanged();
    }

    public TAPAttackTargetElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (elements == null || elements.size() - 1 < rowIndex) {
            return;
        }
        TAPAttackTargetElement element = elements.get(rowIndex);

        if (aValue instanceof Integer) {
            element.setAttacks((Integer) aValue);
        }
        fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        TAPAttackTargetElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage().getTribe();
            case 1:
                return element.getVillage();
            case 2:
                return element.isFake();
            default:
                return element.getAttacks();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
