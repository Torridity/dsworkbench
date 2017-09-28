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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.io.TroopAmountElement;
import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.attack.StandardAttackManager;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class AttackTypeTableModel extends AbstractTableModel {
    private static final Logger logger = Logger.getLogger("AttackTypeTableModel");

    private List<String> columnNames = new LinkedList<>();
    private List<Class> columnTypes = new LinkedList<>();

    public AttackTypeTableModel() {
        columnNames.add("Name");
        columnTypes.add(String.class);
        columnNames.add("Symbol");
        columnTypes.add(Integer.class);
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            columnNames.add(unit.getPlainName());
            columnTypes.add(TroopAmountElement.class);
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes.get(columnIndex);
    }

    @Override
    public int getRowCount() {
        return StandardAttackManager.getSingleton().getElementCount();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0 && !StandardAttackManager.getSingleton().isAllowedName((String) getValueAt(rowIndex, columnIndex))) {
            return false;
        } else if (columnIndex == 1 && !StandardAttackManager.getSingleton().isAllowedIcon((Integer) getValueAt(rowIndex, columnIndex))) {
            return false;
        }
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        //check if value is valid
        StandardAttack a = StandardAttackManager.getSingleton().getManagedElement(rowIndex);
        if (columnIndex == 0) {
            if (StandardAttackManager.getSingleton().isAllowedName((String) aValue) && !StandardAttackManager.getSingleton().containsElementByName((String) aValue)) {
                a.setName((String) aValue);
            }
        } else if (columnIndex == 1) {
            if (StandardAttackManager.getSingleton().isAllowedIcon((Integer) aValue) && !StandardAttackManager.getSingleton().containsElementByIcon((Integer) aValue)) {
                a.setIcon((Integer) aValue);
            }
        } else {
            UnitHolder unit = DataHolder.getSingleton().getUnits().get(columnIndex - 2);
            try {
                a.getTroops().setAmount(new TroopAmountElement(unit, (String) aValue));
            } catch(IllegalArgumentException e) {
                logger.info("cannot set Amount", e);
                JOptionPaneHelper.showWarningBox(null, "Konnte den Wert nicht setzen:\n"
                        + e.getCause().getMessage(), "Konnte den Wert nicht setzen");
            }
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        StandardAttack a = StandardAttackManager.getSingleton().getManagedElement(rowIndex);

        switch (columnIndex) {
            case 0:
                return a.getName();
            case 1:
                return a.getIcon();
            default:
                UnitHolder unit = DataHolder.getSingleton().getUnits().get(columnIndex - 2);
                return a.getTroops().getElementForUnit(unit);
        }
    }
}
