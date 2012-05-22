/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchMerchantDistibutor;
import de.tor.tribes.ui.wiz.red.types.ExtendedTransport;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class REDFinalTransportsTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Herkunft", "Ziel", "Holz", "Lehm", "Eisen", "Händler", "Übertragen"
    };
    Class[] types = new Class[]{
        Village.class, Village.class, Integer.class, Integer.class, Integer.class, Integer.class, Boolean.class
    };
    private final List<ExtendedTransport> elements = new LinkedList<ExtendedTransport>();

    public void clear() {
        elements.clear();
        fireTableDataChanged();
    }

    public boolean addRow(final Village pSource, Village pTarget, List<DSWorkbenchMerchantDistibutor.Resource> pResources, boolean pSubmitted) {
        ExtendedTransport t = new ExtendedTransport(pSource, pResources, pTarget);
        t.setTransferredToBrowser(pSubmitted);
        if (t.hasGoods()) {
            elements.add(t);
            return true;
        }
        return false;
    }

    public boolean addRow(final Village pSource, Village pTarget, List<DSWorkbenchMerchantDistibutor.Resource> pResources) {
        ExtendedTransport t = new ExtendedTransport(pSource, pResources, pTarget);
        if (t.hasGoods()) {
            elements.add(t);
            return true;
        }
        return false;
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

    public ExtendedTransport getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        ExtendedTransport element = elements.get(row);
        switch (column) {
            case 0:
                return element.getSource();
            case 1:
                return element.getTarget();
            case 2:
                return element.getWood();
            case 3:
                return element.getClay();
            case 4:
                return element.getIron();
            case 5:
                return element.getMerchants();
            default:
                return element.isTransferredToBrowser();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
