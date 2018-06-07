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

import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.types.ext.Village;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

/**
 *
 * @author Torridity
 */
public class REDExtendedMerchantTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Dorf", "Rohstoffe", "Speicher", "Händler", "Bauernhof", "Handelsrichtung"
    };
    Class[] types = new Class[]{
        Village.class, StorageStatus.class, Integer.class, String.class, String.class, VillageMerchantInfo.Direction.class
    };
    private final List<VillageMerchantInfo> elements = new LinkedList<>();

    public REDExtendedMerchantTableModel() {
        super();
    }

    public void clear() {
        elements.clear();
        fireTableDataChanged();
    }

    public void addRow(final Village pVillage, int pStash, int pWood, int pClay, int pIron, int pAvailableMerchants, int pMerchants, int pAvailableFarm, int pOverallFarm, VillageMerchantInfo.Direction pDirection, boolean pCheck) {
        Object result = CollectionUtils.find(elements, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((VillageMerchantInfo) o).getVillage().equals(pVillage);
            }
        });

        if (result == null) {
            VillageMerchantInfo vmi = new VillageMerchantInfo(pVillage, pStash, pWood, pClay, pIron, pAvailableMerchants, pMerchants, pAvailableFarm, pOverallFarm);
            vmi.setDirection(pDirection);
            elements.add(vmi);
        } else {
            VillageMerchantInfo resultElem = (VillageMerchantInfo) result;
            resultElem.setWoodStock(pWood);
            resultElem.setClayStock(pClay);
            resultElem.setIronStock(pIron);
            resultElem.setStashCapacity(pStash);
            resultElem.setAvailableMerchants(pAvailableMerchants);
            resultElem.setOverallMerchants(pMerchants);
            resultElem.setAvailableFarm(pAvailableFarm);
            resultElem.setOverallFarm(pOverallFarm);
            resultElem.setDirection(pDirection);
        }
        if (pCheck) {
            fireTableDataChanged();
        }
    }

    public void addRow(final Village pVillage, int pStash, int pWood, int pClay, int pIron, int pAvailableMerchants, int pMerchants, int pAvailableFarm, int pOverallFarm, VillageMerchantInfo.Direction pDirection) {
        addRow(pVillage, pStash, pWood, pClay, pIron, pAvailableMerchants, pMerchants, pAvailableFarm, pOverallFarm, pDirection, true);
    }

    public void removeRow(VillageMerchantInfo pRemove) {
        elements.remove(pRemove);
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

    public VillageMerchantInfo getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        VillageMerchantInfo element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage();
            case 1:
                return new StorageStatus(element.getWoodStock(), element.getClayStock(),
                        element.getIronStock(), element.getStashCapacity());
            case 2:
                return element.getStashCapacity();
            case 3:
                return element.getAvailableMerchants() + "/" + element.getOverallMerchants();
            case 4:
                return element.getAvailableFarm() + "/" + element.getOverallFarm();
            default:
                return element.getDirection();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}