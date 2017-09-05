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

import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.ext.Village;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author Torridity
 */
public class DefenseToolModel extends DefaultTableModel {

    private List<DefenseInformation> entries = null;
    private Class[] types = new Class[]{Integer.class, Village.class, Integer.class, Integer.class, Date.class, Date.class, DefenseInformation.DEFENSE_STATUS.class, Double.class, String.class};
    private String[] colNames = new String[]{"Tendenz", "Ziel", "Angriffe", "Fakes", "Erster Angriff", "Letzter Angriff", "Status", "Verlustrate", "Unterstützungen"};

    public DefenseToolModel() {
        super();
        entries = new ArrayList<>();
    }

    public void clear(boolean pNotify) {
        entries.clear();
        if (pNotify) {
            fireTableDataChanged();
        }
    }

    public void clear() {
        clear(true);
    }

    @Override
    public void removeRow(int pRow) {
        entries.remove(pRow);
        fireTableDataChanged();
    }

    public void removeRow(DefenseInformation pToRemove) {
        entries.remove(pToRemove);
    }

    @Override
    public int getRowCount() {
        if (entries == null) {
            return 0;
        }
        return entries.size();
    }

    public DefenseInformation[] getRows() {
        return entries.toArray(new DefenseInformation[entries.size()]);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    public DefenseInformation findElement(final Village pTarget) {
        return (DefenseInformation) CollectionUtils.find(entries, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((DefenseInformation) o).getTarget().equals(pTarget);
            }
        });
    }

    public void addRow(DefenseInformation pElement) {
        entries.add(pElement);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DefenseInformation info = entries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return info.getDelta();
            case 1:
                return info.getTarget();
            case 2:
                return info.getAttackCount();
            case 3:
                return info.getFakeCount();
            case 4:
                return info.getFirstAttack();
            case 5:
                return info.getLastAttack();
            case 6:
                return info.getStatus();
            case 7:
                return info.getLossRatio();
             default:
                return info.getSupports().length + "/" + info.getNeededSupports();
        }
    }
}
