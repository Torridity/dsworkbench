/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.DefenseInformation.DEFENSE_STATUS;
import de.tor.tribes.types.ext.Village;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class DEPResultTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Ziel", "Unterstützungen", "Status"
    };
    private Class[] types = new Class[]{
        Village.class, String.class, DEFENSE_STATUS.class
    };
    private final List<DefenseInformation> elements = new LinkedList<DefenseInformation>();

    public DEPResultTableModel() {
        super();
    }

    public void clear() {
        elements.clear();
        fireTableDataChanged();
    }

    public void addRow(DefenseInformation pDefense) {
        elements.add(pDefense);
    }

    @Override
    public int getRowCount() {
        if (elements == null) {
            return 0;
        }
        return elements.size();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void removeRow(int row, int viewRow) {
        elements.remove(row);
        fireTableDataChanged();
    }

    public DefenseInformation getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        DefenseInformation element = elements.get(row);
        switch (column) {
            case 0:
                return element.getTarget();
            case 1:
                return element.getSupports().length + "/" + element.getNeededSupports();
            default:
                return element.getStatus();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
