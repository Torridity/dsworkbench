/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.ref.types.REFResultElement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class REFResultTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Herkunft", "Ziel", "Einheit", "Späteste Abschickzeit", "Ankunftzeit"
    };
    Class[] types = new Class[]{
        Village.class, Village.class, UnitHolder.class, Date.class, Date.class
    };
    private final List<REFResultElement> elements = new LinkedList<REFResultElement>();

    public void clear() {
        elements.clear();
        fireTableDataChanged();
    }

    public void addRow(Village pSource, Village pTarget, UnitHolder pUnit, Date pArrive) {
        elements.add(new REFResultElement(pSource, pTarget, pUnit, pArrive));
        fireTableDataChanged();
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

    public void removeRow(int row) {
        elements.remove(row);
        fireTableDataChanged();
    }

    public REFResultElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        REFResultElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getSource();
            case 1:
                return element.getTarget();
            case 2:
                return element.getUnit();
            case 3:
                return element.getSendTime();
            default:
                return element.getArriveTime();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
