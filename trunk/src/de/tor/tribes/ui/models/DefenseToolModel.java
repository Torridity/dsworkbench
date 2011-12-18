/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.DefenseElement;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author Torridity
 */
public class DefenseToolModel extends AbstractTableModel {

    private List<DefenseElement> entries = null;
    private Class[] types = new Class[]{Integer.class, Village.class, Integer.class, Integer.class, Date.class, Date.class, Integer.class, DefenseElement.DEFENSE_STATUS.class, Double.class, Integer.class};
    private String[] colNames = new String[]{"Tendenz", "Ziel", "Angriffe", "Fakes", "Erster Angriff", "Letzter Angriff", "Wall", "Status", "Verlustrate", "Unterstützungen"};
    private boolean[] editableColumns = new boolean[]{false, false, false, false, false, false, false, false, false, false};

    public DefenseToolModel() {
        entries = new ArrayList<DefenseElement>();
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    public DefenseElement[] getRows() {
        return entries.toArray(new DefenseElement[entries.size()]);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editableColumns[columnIndex];
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    public DefenseElement findElement(final Village pTarget) {
        return (DefenseElement) CollectionUtils.find(entries, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((DefenseElement) o).getTarget().equals(pTarget);
            }
        });
    }

    public void addRow(DefenseElement pElement) {
        entries.add(pElement);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return entries.get(rowIndex).getDelta();
            case 1:
                return entries.get(rowIndex).getTarget();
            case 2:
                return entries.get(rowIndex).getAttackCount();
            case 3:
                return entries.get(rowIndex).getFakeCount();
            case 4:
                return entries.get(rowIndex).getFirstAttack();
            case 5:
                return entries.get(rowIndex).getLastAttack();
            case 6:
                return entries.get(rowIndex).getWallLevel();
            case 7:
                return entries.get(rowIndex).getStatus();
            case 8:
                return entries.get(rowIndex).getLossRatio();
            case 9:
                return entries.get(rowIndex).getNeededSupports();

        }
        return null;
    }
}
