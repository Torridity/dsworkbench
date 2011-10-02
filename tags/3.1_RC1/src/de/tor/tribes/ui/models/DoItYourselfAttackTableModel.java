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
import java.util.Date;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang.time.DurationFormatUtils;

/**
 *
 * @author Torridity
 */
public class DoItYourselfAttackTableModel extends AbstractTableModel {

    private static Logger logger = Logger.getLogger("DoItYourselfAttackTable");
    protected static Class[] types = new Class[]{Integer.class, UnitHolder.class, Village.class, Village.class, Date.class, Date.class, String.class};
    protected static String[] colNames = new String[]{"Angriffstyp", "Einheit", "Herkunft", "Ziel", "Abschickzeit", "Ankunftzeit", "Verbleibend"};
    protected static boolean[] editableColumns = new boolean[]{true, true, false, false, true, true, false};

    public DoItYourselfAttackTableModel() {
    }

    @Override
    public int getColumnCount() {
        return colNames.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editableColumns[columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return colNames[column];
    }

    public void addAttack(Village pSource, Village pTarget, Date pArrive, UnitHolder pUnit, Integer pType) {
        AttackManager.getSingleton().addDoItYourselfAttack(pSource, pTarget, pUnit, pArrive, pType);
        fireTableDataChanged();
    }

    public void removeRow(int pRow) {
        Attack a = (Attack) AttackManager.getSingleton().getDoItYourselfAttacks().get(pRow);
        AttackManager.getSingleton().removeElement(a);
        fireTableDataChanged();
    }

    public Attack getAttack(int pRow) {
        return (Attack) AttackManager.getSingleton().getDoItYourselfAttacks().get(pRow);
    }

    @Override
    public Object getValueAt(int pRow, int pCol) {
        try {
            Attack a = (Attack) AttackManager.getSingleton().getAllElements(AttackManager.MANUAL_ATTACK_PLAN).get(pRow);

            switch (pCol) {
                case 0:
                    return a.getType();
                case 1:
                    return a.getUnit();
                case 2:
                    return a.getSource();
                case 3:
                    return a.getTarget();
                case 4: {
                    try {
                        long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);

                        return new Date(sendTime);
                    } catch (Exception e) {
                        return null;
                    }
                }
                case 5:
                    if (a.getArriveTime() != null) {
                        return a.getArriveTime();
                    } else {
                        return null;
                    }
                default: {
                    try {
                        long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                        long t = sendTime - System.currentTimeMillis();
                        t = (t <= 0) ? 0 : t;
                        return DurationFormatUtils.formatDuration(t, "HHH:mm:ss.SSS", true);
                        /* t = (t <= 0) ? 0 : t;
                        if (t != 0) {
                        long h = (int) Math.floor((double) t / (double) (1000 * 60 * 60));
                        t -= (h * 1000 * 60 * 60);
                        long min = (int) Math.floor((double) t / (double) (1000 * 60));
                        t -= (min * 1000 * 60);
                        long s = (int) Math.floor((double) t / (double) 1000);
                        t -= (s * 1000);
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
                        }*/
                    } catch (Exception e) {
                    }
                    return "00:00:00.000";
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getRowCount() {
        return AttackManager.getSingleton().getAllElements(AttackManager.MANUAL_ATTACK_PLAN).size();
    }

    @Override
    public void setValueAt(Object pValue,
            int pRow,
            int pCol) {
        try {
            Attack a = (Attack) AttackManager.getSingleton().getAllElements(AttackManager.MANUAL_ATTACK_PLAN).get(pRow);
            switch (pCol) {
                case 0: {
                    a.setType((Integer) pValue);
                    break;
                }
                case 1: {
                    a.setUnit((UnitHolder) pValue);
                    break;
                }
                case 4: {
                    if (pValue == null) {
                        a.setArriveTime(null);
                    } else {
                        Date sendTime = (Date) pValue;
                        long arriveTime = sendTime.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                        a.setArriveTime(new Date(arriveTime));
                    }
                    break;
                }
                case 5: {
                    if (pValue == null) {
                        a.setArriveTime(null);
                    } else {
                        a.setArriveTime((Date) pValue);
                    }
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
        }
        AttackManager.getSingleton().revalidate(AttackManager.MANUAL_ATTACK_PLAN, true);
    }
}
