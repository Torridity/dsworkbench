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
import de.tor.tribes.util.village.KnownVillageManager;
import de.tor.tribes.util.village.KnownVillage;
import java.awt.Color;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 * @author extremeCrazyCoder
 */
public class WatchtowerTableModel extends AbstractTableModel {

    private static Logger logger = Logger.getLogger("WatchtowerTableModel");

    private final Class[] types = new Class[]{Tribe.class, KnownVillage.class, Integer.class, Color.class};
    private final String[] colNames = new String[]{"Spieler", "Dorf", "Stufe", "Farbe"};
    private final boolean[] editableColumns = new boolean[]{false, false, true, false};

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editableColumns[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    @Override
    public int getRowCount() {
        return KnownVillageManager.getSingleton().getWatchtowerVillages().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            KnownVillage v = KnownVillageManager.getSingleton().getWatchtowerVillages().get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return v.getVillage().getTribe();
                }
                case 1: {
                    return v;
                }
                case 2: {
                    return v.getBuildingLevelByName("watchtower");
                }
                default: {
                    return v.getRangeColor();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        KnownVillage v = KnownVillageManager.getSingleton().getWatchtowerVillages().get(rowIndex);
        switch (columnIndex) {
            case 2:
                v.setWatchtowerLevel((Integer) o);
                break;
            default:
                logger.error("Invalid Columnindex " + columnIndex);
        }
        KnownVillageManager.getSingleton().revalidate(true);
    }
}
