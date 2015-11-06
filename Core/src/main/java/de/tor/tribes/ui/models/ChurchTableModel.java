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
import de.tor.tribes.util.church.ChurchManager;
import java.awt.Color;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class ChurchTableModel extends AbstractTableModel {

    private Class[] types = new Class[]{Tribe.class, Village.class, Integer.class, Color.class};
    private String[] colNames = new String[]{"Spieler", "Dorf", "Radius", "Farbe"};
    private boolean[] editableColumns = new boolean[]{false, false, true, false};

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
        return ChurchManager.getSingleton().getChurchVillages().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            Village v = ChurchManager.getSingleton().getChurchVillages().get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    return v.getTribe();
                }
                case 1: {
                    return v;
                }
                case 2: {
                    return ChurchManager.getSingleton().getChurch(v).getRange();
                }
                default: {
                    return ChurchManager.getSingleton().getChurch(v).getRangeColor();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        Village v = ChurchManager.getSingleton().getChurchVillages().get(rowIndex);
        switch (columnIndex) {
            default: {
                ChurchManager.getSingleton().getChurch(v).setRange((Integer) o);
            }
        }
        ChurchManager.getSingleton().revalidate(true);
    }
}
