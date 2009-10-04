/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.util.DSCalculator;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Charon
 */
public class DistanceTableModel extends AbstractTableModel {

    private List<Village> columns = null;
    private static DistanceTableModel SINGLETON = null;

    public static synchronized DistanceTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DistanceTableModel();
        }
        return SINGLETON;
    }

    DistanceTableModel() {
        columns = new LinkedList<Village>();
        columns.add(DSWorkbenchMainFrame.getSingleton().getCurrentUser().getVillageList()[0]);
        columns.add(DSWorkbenchMainFrame.getSingleton().getCurrentUser().getVillageList()[1]);
    }

    @Override
    public int getRowCount() {
        return DSWorkbenchMainFrame.getSingleton().getCurrentUser().getVillages();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Village.class;
        }
        return Double.class;
    }

    @Override
    public int getColumnCount() {
        return columns.size() + 1;
    }

    @Override
    public String getColumnName(int col) {
        if (col == 0) {
            return "Eigene";
        }
        return columns.get(col - 1).toString();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void addVillage(Village pVillage) {
        if (!columns.contains(pVillage)) {
            columns.add(pVillage);
        }
    }

    public void removeVillages(int[] pColumns) {
        List<Village> tmp = new LinkedList<Village>();
        for (int col : pColumns) {
            col = col - 1;
            if (col >= 0) {
                tmp.add(columns.get(col));
            }
        }
        for (Village v : tmp) {
            columns.remove(v);
        }
        fireTableStructureChanged();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Village v1 = DSWorkbenchMainFrame.getSingleton().getCurrentUser().getVillageList()[rowIndex];
        if (columnIndex == 0) {
            return v1;
        }
        Village v2 = columns.get(columnIndex - 1);
        return DSCalculator.calculateDistance(v1, v2);
    }
}
