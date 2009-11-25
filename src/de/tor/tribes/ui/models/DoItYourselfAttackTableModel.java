/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Torridity
 */
public class DoItYourselfAttackTableModel extends AbstractTableModel {

    private static Logger logger = Logger.getLogger("DoItYourselfAttackTable");
    Class[] types = new Class[]{
        UnitHolder.class, Village.class, Date.class, Date.class, String.class
    };
    String[] colNames = new String[]{
        "Einheit", "Herkunft", "Ziel", "Abschickzeit", "Ankunftzeit", "Verbleibend"
    };
    private static DoItYourselfAttackTableModel SINGLETON = null;
    private List<Attack> mAttacks = null;

    public static synchronized DoItYourselfAttackTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DoItYourselfAttackTableModel();
        }
        return SINGLETON;
    }

    public DoItYourselfAttackTableModel() {
        mAttacks = new LinkedList<Attack>();
    }

    public void clear() {
        mAttacks.clear();
    }

    public void addAttack(Village pSource, Village pTarget, Date pArrive, UnitHolder pUnit) {
        Attack a = new Attack();
        a.setSource(pSource);
        a.setTarget(pTarget);
        a.setArriveTime(pArrive);
        a.setUnit(pUnit);
    }

    @Override
    public int getColumnCount() {
        return types.length;
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
        if (col == 0 || col == 5) {
            return false;
        }
        return true;
    }

    @Override
    public Object getValueAt(int pRow, int pCol) {
        Attack a = mAttacks.get(pRow);

        switch (pCol) {
            case 0:
                return a.getUnit();
            case 1:
                return a.getSource();
            case 2:
                return a.getTarget();
            case 3: {
                long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                return new Date(sendTime);
            }
            case 4:
                return a.getArriveTime();
            default: {
                long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                long t = sendTime - System.currentTimeMillis();
                t = (t <= 0) ? 0 : t;
                if (t != 0) {
                    long h = (int) Math.floor((double) t / (double) (1000 * 60 * 60));
                    t = t - (h * 1000 * 60 * 60);
                    long min = (int) Math.floor((double) t / (double) (1000 * 60));
                    t = t - (min * 1000 * 60);
                    long s = (int) Math.floor((double) t / (double) 1000);
                    t = t - (s * 1000);
                    long ms = t;
                    String res = ((h < 10) ? ("0" + h) : "" + h);
                    res += ":";
                    res += ((min < 10) ? "0" + min : "" + min);
                    res += ":";
                    res += ((s < 10) ? "0" + s : "" + s);
                    res += ".";
                    if (ms < 100) {
                        if (ms < 10) {
                            res += "00" + ms;
                        } else {
                            res += "0" + ms;
                        }
                    } else {
                        res += "" + ms;
                    }
                    return res;
                }

                return "00:00:00.000";
            }
        }
    }

    @Override
    public int getRowCount() {
        return mAttacks.size();
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
    }
}
