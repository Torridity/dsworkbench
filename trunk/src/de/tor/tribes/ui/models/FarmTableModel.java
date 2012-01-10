/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.farm.FarmManager;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class FarmTableModel extends AbstractTableModel {

    private Class[] types = new Class[]{FarmInformation.FARM_STATUS.class, Village.class, new double[3].getClass(), Date.class, Boolean.class, Float.class};
    private String[] colNames = new String[]{"Status", "Dorf", "Rohstoffe", "Letzter Bericht", "Ergebnisbericht erwartet", "Erfolgsquote"};

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
                return elem.getVillage();
            case 2:               
                return new double[]{(double) elem.getWoodInStorage() / (double) elem.getStorageCapacity(),
                            (double) elem.getClayInStorage() / (double) elem.getStorageCapacity(),
                            (double) elem.getIronInStorage() / (double) elem.getStorageCapacity()};
            case 3:
                return new Date(elem.getLastReport());
            case 4:
                return elem.isReportExpected();
            default:
                return elem.getCorrectionFactor();
        }
    }
}
