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
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.dist.DistanceManager;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Charon
 */
public class DistanceTableModel extends AbstractTableModel {


    public DistanceTableModel() {
    }

    public void clear() {
        DistanceManager.getSingleton().clear();
    }

    @Override
    public int getRowCount() {
        Tribe currentUser = GlobalOptions.getSelectedProfile().getTribe();
        if (currentUser == null) {
            return 0;
        }
        return currentUser.getVillages();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Village.class;
        }
        return Double.class;
    }

    @Override
    public int getColumnCount() {
        return DistanceManager.getSingleton().getVillages().length + 1;
    }

    @Override
    public String getColumnName(int col) {
        if (col == 0) {
            return "Eigene";
        }
        return DistanceManager.getSingleton().getVillages()[col - 1].toString();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        Village v1 = GlobalOptions.getSelectedProfile().getTribe().getVillageList()[rowIndex];
        if (columnIndex == 0) {
            result = v1;
        } else {
            Village v2 = DistanceManager.getSingleton().getVillages()[columnIndex - 1];
            result = DSCalculator.calculateDistance(v1, v2);
        }
        return result;
    }
}
