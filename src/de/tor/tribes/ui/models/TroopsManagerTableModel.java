/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.TroopsManagerListener;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jejkal
 */
public class TroopsManagerTableModel extends AbstractTableModel {

    Class[] types = null;
    String[] colNames = null;
    private static TroopsManagerTableModel SINGLETON = null;

    public static synchronized TroopsManagerTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TroopsManagerTableModel();
        }
        return SINGLETON;
    }

    /**Setup the table depending on the number of troops of the current server*/
    public void setup() {
        List<Class> typesList = new LinkedList<Class>();
        List<String> namesList = new LinkedList<String>();
        typesList.add(Village.class);
        namesList.add("Dorf");
        typesList.add(Date.class);
        namesList.add("Stand");
        for (int i = 0; i < DataHolder.getSingleton().getUnits().size(); i++) {
            typesList.add(Integer.class);
            namesList.add("");
        }
        types = typesList.toArray(new Class[]{});
        colNames = namesList.toArray(new String[]{});
    }

    TroopsManagerTableModel() {
        TroopsManager.getSingleton().addTroopsManagerListener(new TroopsManagerListener() {

            @Override
            public void fireTroopsChangedEvent() {
                fireTableDataChanged();
            }
        });
    }

    @Override
    public int getRowCount() {
        return TroopsManager.getSingleton().getEntryCount();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public int getColumnCount() {
        if (types == null) {
            return 0;
        }
        return types.length;
    }

    @Override
    public String getColumnName(int col) {
        return colNames[col];
    }

    public void addRow(Object[] row) {
        TroopsManager.getSingleton().addTroopsForVillage((Village) row[0], (Date) row[1], (List<Integer>) row[2]);
    }

    public void removeRow(int pRow) {
        Village v = TroopsManager.getSingleton().getVillages()[pRow];
        if (v != null) {
            TroopsManager.getSingleton().removeTroopsForVillage(v);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col > 1) {
            return true;
        }
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Village row = TroopsManager.getSingleton().getVillages()[rowIndex];
        switch (columnIndex) {
            case 0: {
                return row;
            }
            case 1: {
                return TroopsManager.getSingleton().getTroopsForVillage(row).getState();
            }
            default: {
                int troopIndex = columnIndex - 2;
                return TroopsManager.getSingleton().getTroopsForVillage(row).getTroops().get(troopIndex);
            }
        }
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        switch (pCol) {
            case 0: {
                //not allowed
            }
            case 1: {
                //not allowed
            }
            default: {
                int troopIndex = pCol - 2;
                Village row = TroopsManager.getSingleton().getVillages()[pRow];
                //set current troops
                TroopsManager.getSingleton().getTroopsForVillage(row).getTroops().set(troopIndex, (Integer) pValue);
                //refresh time
                TroopsManager.getSingleton().getTroopsForVillage(row).setState(Calendar.getInstance().getTime());
            }
        }
    }
}
