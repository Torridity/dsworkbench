/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.MarkerSet;
import de.tor.tribes.ui.MarkerCell;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.Color;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Charon
 */
public class MarkerTableModel extends AbstractTableModel {

    private final Class[] types = new Class[]{
        MarkerCell.class, Color.class
    };
    private final String names[] =
            new String[]{
        "Name", "Markierung"
    };
    private static MarkerTableModel SINGLETON = null;
    private String activeSet = "default";

    public static synchronized MarkerTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MarkerTableModel();
        }
        return SINGLETON;
    }

    public String getActiveSet() {
        if (activeSet == null || MarkerManager.getSingleton().getMarkerSet(activeSet) == null) {
            activeSet = "default";
        }
        return activeSet;
    }

    public void setActiveSet(String pSet) {
        if (activeSet == null || MarkerManager.getSingleton().getMarkerSet(activeSet) == null) {
            activeSet = "default";
        }
        activeSet = pSet;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public String getColumnName(int columnIndex) {
        return names[columnIndex];
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
        MarkerSet ms = MarkerManager.getSingleton().getMarkerSet(activeSet);
        if (ms != null) {
            return ms.getMarkers().size();
        } else {
            return 0;
        }
    }

    @Override
    public int getColumnCount() {
        return types.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        switch (columnIndex) {
            case 0: {
                return MarkerManager.getSingleton().getMarkerSetMarkers(activeSet)[rowIndex].getView();
            }
            case 1: {
                return MarkerManager.getSingleton().getMarkerSetMarkers(activeSet)[rowIndex].getMarkerColor();
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object pValue, int rowIndex, int columnIndex) {

        switch (columnIndex) {
            case 0: {
                // view can't be set
                break;
            }
            case 1: {
                //set color
                MarkerManager.getSingleton().getMarkerSetMarkers(activeSet)[rowIndex].setMarkerColor((Color) pValue);
            }
        }
    }
}
