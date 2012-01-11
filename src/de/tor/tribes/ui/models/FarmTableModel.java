/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.farm.FarmManager;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang.time.DurationFormatUtils;

/**
 *
 * @author Torridity
 */
public class FarmTableModel extends AbstractTableModel {

    private Class[] types = new Class[]{FarmInformation.FARM_STATUS.class, FarmInformation.FARM_RESULT.class, Village.class, StorageStatus.class, Date.class, String.class, Float.class};
    private String[] colNames = new String[]{"Status", "Letztes Ergebnis", "Dorf", "Rohstoffe", "Letzter Bericht", "Ankunft", "Erfolgsquote"};

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
                return elem.getLastResult();
            case 2:
                return elem.getVillage();
            case 3:
                //@TODO check storage status (200 resources each, capacity = 1000, view shows small amount!
                return elem.getStorageStatus();
            case 4:
                return new Date(elem.getLastReport());
            case 5:
                long t = elem.getRuntimeInformation();
                t = (t <= 0) ? 0 : t;
                if (t == 0) {
                    return "Keine Truppen unterwegs";
                }
                return DurationFormatUtils.formatDuration(t, "HH:mm:ss", true);
            default:
                return elem.getCorrectionFactor();
        }
    }
}
