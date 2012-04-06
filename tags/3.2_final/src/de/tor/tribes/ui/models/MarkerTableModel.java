/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        if (columnIndex > 0) {
            return true;
        }
        return false;
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
