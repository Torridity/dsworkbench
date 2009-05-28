/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.conquer.ConquerManagerListener;
import java.text.SimpleDateFormat;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Charon
 */
public class ConquersTableModel extends AbstractTableModel {

    Class[] types = null;
    String[] colNames = null;
    private static ConquersTableModel SINGLETON = null;

    public static synchronized ConquersTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ConquersTableModel();
        }
        return SINGLETON;
    }

    public void setup() {
        types = new Class[]{
                    Village.class, String.class, Tribe.class, Tribe.class, Integer.class
                };
        colNames = new String[]{
                    "Dorf", "Geadelt am", "Verlierer", "Gewinner", "Zustimmung"
                };
    }

    ConquersTableModel() {
        ConquerManager.getSingleton().addConquerManagerListener(new ConquerManagerListener() {

            @Override
            public void fireConquersChangedEvent() {
                fireTableDataChanged();
            }
        });
    }

    @Override
    public int getRowCount() {
        return ConquerManager.getSingleton().getConquerCount();
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

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        //Conquer c = ConquerManager.getSingleton().getConquers().get(rowIndex);
        Conquer c = ConquerManager.getSingleton().getConquer(rowIndex);
        switch (columnIndex) {
            case 0:
                return DataHolder.getSingleton().getVillagesById().get(c.getVillageID());
            case 1: {
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return f.format((long) c.getTimestamp() * 1000);
            }
            case 2: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getLoser());
                if (t == null) {
                    return Barbarians.getSingleton();
                } else {
                    return t;
                }
            }
            case 3: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getWinner());
                if (t == null) {
                    return Barbarians.getSingleton();
                } else {
                    return t;
                }
            }
            default:
                return c.getCurrentAcceptance();
        }
    }
}