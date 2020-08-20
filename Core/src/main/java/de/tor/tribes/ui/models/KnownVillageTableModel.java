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
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.ui.editors.BuildingLevelCellEditor.BuildingLevelModel;
import de.tor.tribes.util.BuildingSettings;
import de.tor.tribes.util.village.KnownVillage;
import de.tor.tribes.util.village.KnownVillageManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Torridity
 * @author extremeCrazyCoder
 */
public class KnownVillageTableModel extends AbstractTableModel implements BuildingLevelModel {

    private static Logger logger = LogManager.getLogger("KnownVillageTableModel");

    private final List<String> columnNames = new ArrayList<>();
    private final List<Class> columnTypes = new ArrayList<>();
    private final List<Boolean> editable = new ArrayList<>();
    private final List<String> buildingNames = new ArrayList<>();
    
    public KnownVillageTableModel() {
        generateColumns();
        
        DataHolder.getSingleton().addDataHolderListener(new DataHolderListener() {
            @Override
            public void fireDataHolderEvent(String arg0) {
            }

            @Override
            public void fireDataLoadedEvent(boolean arg0) {
                generateColumns();
            }
        });
    }
    
    private void generateColumns() {
        columnNames.clear();
        columnTypes.clear();
        editable.clear();
        buildingNames.clear();
        
        columnNames.add("Spieler"); columnTypes.add(Tribe.class); editable.add(false);
        columnNames.add("Dorf"); columnTypes.add(KnownVillage.class); editable.add(false);
        columnNames.add("Stand"); columnTypes.add(Date.class); editable.add(true);
        for (String buildingName : BuildingSettings.BUILDING_NAMES) {
            if(BuildingSettings.getMaxBuildingLevel(buildingName) == -1) continue;
            
            buildingNames.add(buildingName);
            columnNames.add(buildingName);
            columnTypes.add(Integer.class);
            editable.add(true);
        }
        
        fireTableStructureChanged();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable.get(columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    @Override
    public int getRowCount() {
        return KnownVillageManager.getSingleton().getElementCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            KnownVillage v = (KnownVillage) KnownVillageManager.getSingleton().getAllElements().get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return v.getVillage().getTribe();
                case 1:
                    return v;
                case 2:
                    return new Date(v.getLastUpdate());
                default:
                return v.getBuildingLevelByName(getBuildingNameForColumn(columnIndex));
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        if(getValueAt(rowIndex, columnIndex).equals(o)) return;
        if(columnIndex < 3) {
            logger.error("Invalid Columnindex " + columnIndex);
            return;
        }
        KnownVillage v = (KnownVillage) KnownVillageManager.getSingleton().getAllElements().get(rowIndex);
        v.setBuildingLevelByName(getBuildingNameForColumn(columnIndex), (Integer) o);
        v.updateTime();
        KnownVillageManager.getSingleton().revalidate(true);
    }

    @Override
    public String getBuildingNameForColumn(int column) {
        if(column < 3) {
            return null;
        }
        return buildingNames.get(column - 3);
    }
}
