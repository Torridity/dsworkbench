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

import de.tor.tribes.types.Marker;
import de.tor.tribes.ui.MarkerCell;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.Color;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Charon
 */
public class MarkerTableModel extends AbstractTableModel {
    
    private String sMarkerSet = null;
    private final Class[] types = new Class[]{MarkerCell.class, Color.class, Boolean.class};
    private final String colNames[] = new String[]{"Name", "Markierung", "Sichtbar"};
    
    public MarkerTableModel(String pMarkerSet) {
        sMarkerSet = pMarkerSet;
    }
    
    public void setMarkerSet(String pMarkerSet) {
        sMarkerSet = pMarkerSet;
        fireTableDataChanged();
    }
    
    public String getMarkerSet() {
        return sMarkerSet;
    }
    
    @Override
    public int getColumnCount() {
        return colNames.length;
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return colNames[columnIndex];
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }
    
    @Override
    public int getRowCount() {
        if (sMarkerSet == null) {
            return 0;
        }
        return MarkerManager.getSingleton().getAllElements(sMarkerSet).size();
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (sMarkerSet == null) {
            return null;
        }
        
        Marker m = ((Marker) MarkerManager.getSingleton().getAllElements(sMarkerSet).get(rowIndex));
        switch (columnIndex) {
            case 0: {
                return m.getView();
            }
            case 1: {
                return m.getMarkerColor();
            }
            case 2: {
                return m.isShownOnMap();
            }
        }
        return null;
    }
    
    @Override
    public void setValueAt(Object pValue, int rowIndex, int columnIndex) {
        Marker m = ((Marker) MarkerManager.getSingleton().getAllElements(sMarkerSet).get(rowIndex));
        switch (columnIndex) {
            case 0: {
                // view can't be set
                break;
            }
            case 1: {
                //set color
                m.setMarkerColor((Color) pValue);
                break;
            }
            case 2: {
                m.setShownOnMap((Boolean) pValue);
                break;
            }
        }
        MarkerManager.getSingleton().revalidate(sMarkerSet, true);
    }
}
