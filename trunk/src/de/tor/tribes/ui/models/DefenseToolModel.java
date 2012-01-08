/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
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

    private List<DefenseInformation> entries = null;
    private Class[] types = new Class[]{Integer.class, Village.class, Integer.class, Integer.class, Date.class, Date.class, DefenseInformation.DEFENSE_STATUS.class, Double.class, Integer.class, Boolean.class, Boolean.class};
    private String[] colNames = new String[]{"Tendenz", "Ziel", "Angriffe", "Fakes", "Erster Angriff", "Letzter Angriff", "Status", "Verlustrate", "Unterstützungen", "Analysiert", "Verteidigt"};

    public DefenseToolModel() {
        entries = new ArrayList<DefenseInformation>();
    }

    public void clear() {
        entries.clear();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    public DefenseInformation[] getRows() {
        return entries.toArray(new DefenseInformation[entries.size()]);
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
        return false;
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    public DefenseInformation findElement(final Village pTarget) {
        return (DefenseInformation) CollectionUtils.find(entries, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((DefenseInformation) o).getTarget().equals(pTarget);
            }
        });
    }

    public void addRow(DefenseInformation pElement) {
        entries.add(pElement);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DefenseInformation info = entries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return info.getDelta();
            case 1:
                return info.getTarget();
            case 2:
                return info.getAttackCount();
            case 3:
                return info.getFakeCount();
            case 4:
                return info.getFirstAttack();
            case 5:
                return info.getLastAttack();
            case 6:
                return info.getStatus();
            case 7:
                return info.getLossRatio();
            case 8:
                return info.getNeededSupports();
            case 9:
                return info.isAnalyzed();
            default:
                return info.isSave();
        }
    }
}
