/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.attack.AttackManagerListener;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jejkal
 */
public class AttackManagerTableModel extends AbstractTableModel {

    Class[] types = new Class[]{
        Village.class, Village.class, UnitHolder.class, Date.class, Date.class, Boolean.class
    };
    String[] colNames = new String[]{
        "Herkunft", "Ziel", "Einheit", "Abschickzeit", "Ankunftzeit", "Einzeichnen"
    };
    private static AttackManagerTableModel SINGLETON = null;

    public static synchronized AttackManagerTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new AttackManagerTableModel();
        }
        return SINGLETON;
    }

    AttackManagerTableModel() {

        AttackManager.getSingleton().addAttackManagerListener(new AttackManagerListener() {

            @Override
            public void fireAttacksChangedEvent(String pPlan) {
                fireTableDataChanged();
            }
        });
    }

    public void addRow(Object[] row) {
        AttackManager.getSingleton().addAttack((Village) row[0], (Village) row[1], (UnitHolder) row[2], (Date) row[3]);
    }

    public void removeRow(int pRow) {
        AttackManager.getSingleton().removeAttack(null, pRow);
    }

    @Override
    public String getColumnName(int col) {
        return colNames[col];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public Object getValueAt(int pRow, int pCol) {
        Attack a = AttackManager.getSingleton().getAttackPlan(null).get(pRow);
        switch (pCol) {
            case 0:
                return a.getSource();
            case 1:
                return a.getTarget();
            case 2:
                return a.getUnit();
            case 3: {
                long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);

                return new Date(sendTime);
            }
            case 4:
                return a.getArriveTime();
            default:
                return a.isShowOnMap();
        }
    }

    @Override
    public int getRowCount() {
        return AttackManager.getSingleton().getAttackPlan(null).size();
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        Attack a = AttackManager.getSingleton().getAttackPlan(null).get(pRow);
        switch (pCol) {
            case 0: {
                a.setSource((Village) pValue);
                break;
            }
            case 1: {
                a.setTarget((Village) pValue);
                break;
            }
            case 2: {
                a.setUnit((UnitHolder) pValue);
                break;
            }
            case 3: {
                long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                a.setArriveTime(new Date(sendTime));
                break;
            }
            case 4: {
                a.setArriveTime((Date) pValue);
                break;
            }
            default:
                a.setShowOnMap((Boolean) pValue);
        }
    }

    @Override
    public int getColumnCount() {
        return types.length;
    }
}
