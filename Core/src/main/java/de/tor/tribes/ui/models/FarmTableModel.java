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

import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.util.farm.FarmManager;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang.time.DurationFormatUtils;

/**
 *
 * @author Torridity
 */
public class FarmTableModel extends AbstractTableModel {

    private Class[] types = new Class[]{FarmInformation.FARM_STATUS.class, Boolean.class, Date.class, String.class, FarmInformation.SIEGE_STATUS.class, Integer.class, StorageStatus.class, String.class, FarmInformation.FARM_RESULT.class, Float.class};
    private String[] colNames = new String[]{"Status", "Resourcen gefunden", "Letzter Bericht", "Dorf", "Kata Status", "Wall", "Rohstoffe", "Ankunft", "Übertragen", "Erfolgsquote"};

    public FarmTableModel() {
    }

    @Override
    public int getRowCount() {
        return FarmManager.getSingleton().getElementCount();
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

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FarmInformation elem = (FarmInformation) FarmManager.getSingleton().getAllElements().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return elem.getStatus();
            case 1:
                return elem.isResourcesFoundInLastReport();
            case 2:
                return new Date(elem.getLastReport());
            case 3:
                return elem.getVillage().getShortName();
            case 4:
                return elem.getSiegeStatus();
            case 5:
                return elem.getWallLevel();
            case 6:
                return elem.getStorageStatus();
            case 7:
                long t = elem.getRuntimeInformation();
                t = (t <= 0) ? 0 : t;
                if (t == 0) {
                    return "Keine Truppen unterwegs";
                }
                return DurationFormatUtils.formatDuration(t, "HH:mm:ss", true);
            case 8:
                return elem.getLastResult();
            default:
                return elem.getCorrectionFactor();
        }
    }
}
