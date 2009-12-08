/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.BarbarianAlly;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.conquer.ConquerManagerListener;
import java.awt.Desktop;
import java.text.SimpleDateFormat;
import javax.swing.table.AbstractTableModel;

/**
 * @author Charon
 */
public class ConquersTableModel extends AbstractTableModel {

    Class[] types = null;
    String[] colNames = null;
    private static ConquersTableModel SINGLETON = null;
    // boolean[] columnsVisible = new boolean[9];

    public static synchronized ConquersTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ConquersTableModel();
        }
        return SINGLETON;
    }

    public void setup() {
        types = new Class[]{
                    Village.class, String.class, String.class, Tribe.class, Ally.class, Tribe.class, Ally.class, Integer.class, Double.class
                };
        colNames = new String[]{
                    "Dorf", "Kontinent", "Geadelt am", "Verlierer", "Stamm", "Gewinner", "Stamm", "Zustimmung", "Entfernung"
                };
    }

    ConquersTableModel() {
        /*     columnsVisible[0] = true;
        columnsVisible[1] = false;
        columnsVisible[2] = true;
        columnsVisible[3] = true;
        columnsVisible[4] = true;
        columnsVisible[5] = false;
        columnsVisible[6] = true;
        columnsVisible[7] = true;
        columnsVisible[8] = true;
         */
        ConquerManager.getSingleton().addConquerManagerListener(new ConquerManagerListener() {

            @Override
            public void fireConquersChangedEvent() {
                fireTableDataChanged();
            }
        });
    }

    /*    protected int getNumber(int col) {
    int n = col;    // right number to return
    int i = 0;
    do {
    if (!(columnsVisible[i])) {
    n++;
    }
    i++;
    } while (i < n);
    // If we are on an invisible column,
    // we have to go one step further
    while (!(columnsVisible[n])) {
    n++;
    }
    return n;
    }*/
    @Override
    public int getRowCount() {
        int cnt = ConquerManager.getSingleton().getConquerCount();
        return cnt;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }
    /* @Override
    public Class getColumnClass(int columnIndex) {
    return types[getNumber(columnIndex)];
    }*/

    @Override
    public int getColumnCount() {
        if (types == null) {
            return 0;
        }
        return types.length;
    }
    /* public int getColumnCount() {
    int n = 0;
    for (int i = 0; i < 9; i++) {
    if (columnsVisible[i]) {
    n++;
    }
    }
    return n;
    }*/

    /*public String getColumnName(int col) {
    return colNames[getNumber(col)];
    }*/
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
       // columnIndex = getNumber(columnIndex);
        Conquer c = ConquerManager.getSingleton().getConquer(rowIndex);
        switch (columnIndex) {
            case 0:
                return DataHolder.getSingleton().getVillagesById().get(c.getVillageID());
            case 1: {
                Village v = DataHolder.getSingleton().getVillagesById().get(c.getVillageID());
                return "K" + DSCalculator.getContinent(v.getX(), v.getY());
            }
            case 2: {
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return f.format((long) c.getTimestamp() * 1000);
            }
            case 3: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getLoser());
                if (t == null) {
                    return Barbarians.getSingleton();
                } else {
                    return t;
                }
            }
            case 4: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getLoser());
                if (t == null) {
                    return BarbarianAlly.getSingleton();
                } else if (t.getAlly() == null) {
                    return NoAlly.getSingleton();
                } else {
                    return t.getAlly();
                }
            }
            case 5: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getWinner());
                if (t == null) {
                    return Barbarians.getSingleton();
                } else {
                    return t;
                }
            }
            case 6: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getWinner());
                if (t == null) {
                    return BarbarianAlly.getSingleton();
                } else if (t.getAlly() == null) {
                    return NoAlly.getSingleton();
                } else {
                    return t.getAlly();
                }
            }
            case 7: {
                return c.getCurrentAcceptance();
            }
            default: {
                Village v = DataHolder.getSingleton().getVillagesById().get(c.getVillageID());
                Village vUser = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                if (vUser != null) {
                    return DSCalculator.calculateDistance(v, vUser);
                } else {
                    return 0;
                }


            }
        }
    }
}
