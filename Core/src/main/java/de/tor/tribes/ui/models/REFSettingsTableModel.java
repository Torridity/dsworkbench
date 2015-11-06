/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.ref.types.REFTargetElement;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author Torridity
 */
public class REFSettingsTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Dorf", "Notwendige Unterstützungen"
    };
    Class[] types = new Class[]{
        Village.class, Integer.class
    };
    private final List<REFTargetElement> elements = new LinkedList<REFTargetElement>();

    public void clear() {
        elements.clear();
        fireTableDataChanged();
    }

    public void addRow(Village pVillage) {
        elements.add(new REFTargetElement(pVillage));
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

    public REFTargetElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        REFTargetElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage();
            default:
                return element.getNeededSupports();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
