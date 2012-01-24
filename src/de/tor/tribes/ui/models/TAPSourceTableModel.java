/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackSourceElement;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author Torridity
 */
public class TAPSourceTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Spieler", "Dorf", "Einheit", "Fake"
    };
    private Class[] types = new Class[]{
        Tribe.class, Village.class, UnitHolder.class, Boolean.class
    };
    private final List<TAPAttackSourceElement> elements = new LinkedList<TAPAttackSourceElement>();

    public TAPSourceTableModel() {
        super();
    }

    public void addRow(final Village pVillage, UnitHolder pUnit, boolean pFake) {
        Object result = CollectionUtils.find(elements, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((TAPAttackSourceElement) o).getVillage().equals(pVillage);
            }
        });

        if (result == null) {
            elements.add(new TAPAttackSourceElement(pVillage, pUnit, pFake));
        } else {
            TAPAttackSourceElement resultElem = (TAPAttackSourceElement) result;
            resultElem.setUnit(pUnit);
            resultElem.setFake(pFake);
        }
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

    public TAPAttackSourceElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        TAPAttackSourceElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage().getTribe();
            case 1:
                return element.getVillage();
            case 2:
                return element.getUnit();
            default:
                return element.isFake();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
