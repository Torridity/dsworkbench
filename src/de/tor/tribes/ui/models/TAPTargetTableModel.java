/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackTargetElement;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author Torridity
 */
public class TAPTargetTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Spieler", "Dorf", "Fake", "Angriffe"
    };
    private Class[] types = new Class[]{
        Tribe.class, Village.class, Boolean.class, Integer.class
    };
    private final List<TAPAttackTargetElement> elements = new LinkedList<TAPAttackTargetElement>();

    public TAPTargetTableModel() {
        super();
    }

    public void addRow(final Village pVillage, boolean pFake) {
        Object result = CollectionUtils.find(elements, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((TAPAttackTargetElement) o).getVillage().equals(pVillage);
            }
        });

        if (result == null) {
            elements.add(new TAPAttackTargetElement(pVillage, pFake));
        } else {
            TAPAttackTargetElement resultElem = (TAPAttackTargetElement) result;
            resultElem.addAttack();
            resultElem.setFake(pFake);
        }
        fireTableDataChanged();
    }

    public void removeTargets(List<Village> pToRemove) {
        for (TAPAttackTargetElement elem : elements.toArray(new TAPAttackTargetElement[elements.size()])) {
            if (pToRemove.contains(elem.getVillage())) {
                elements.remove(elem);
            }
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

    public void removeRow(int row, int viewRow) {
        TAPAttackTargetElement elem = elements.get(row);
        if (!elem.removeAttack()) {
            elements.remove(row);
            fireTableDataChanged();
        } else {
            fireTableRowsUpdated(row, row);
        }
    }

    public TAPAttackTargetElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        TAPAttackTargetElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage().getTribe();
            case 1:
                return element.getVillage();
            case 2:
                return element.isFake();
            default:
                return element.getAttacks();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}
