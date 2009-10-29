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
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class AttackManagerTableModel extends AbstractTableModel {

    private static Logger logger = Logger.getLogger("AttackTable");
    Class[] types = new Class[]{
        Village.class, Village.class, UnitHolder.class, Date.class, Date.class, Boolean.class, Integer.class
    };
    String[] colNames = new String[]{
        "Herkunft", "Ziel", "Einheit", "Abschickzeit", "Ankunftzeit", "Einzeichnen", "Typ"
    };
    private String sActiveAttackPlan = AttackManager.DEFAULT_PLAN_ID;
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

    public synchronized void setActiveAttackPlan(String pPlan) {
        logger.debug("Setting active attack plan to '" + pPlan + "'");
        sActiveAttackPlan = pPlan;
    }

    public synchronized String getActiveAttackPlan() {
        return sActiveAttackPlan;
    }

    public void addRow(Object[] row) {
        AttackManager.getSingleton().addAttack((Village) row[0], (Village) row[1], (UnitHolder) row[2], (Date) row[4], sActiveAttackPlan);
    }

    public void removeRow(int pRow) {
        AttackManager.getSingleton().removeAttack(sActiveAttackPlan, pRow);
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
        try {
            List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(getActiveAttackPlan());
            Attack a = null;
            if (attacks.size() > pRow) {
                a = attacks.get(pRow);
            } else {
                return null;
            }

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
                case 5:
                    return a.isShowOnMap();
                default:
                    return a.getType();
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getRowCount() {
        String activePlan = getActiveAttackPlan();
        List<Attack> active = AttackManager.getSingleton().getAttackPlan(activePlan);
        if (active != null) {
            return active.size();
        } else {
            sActiveAttackPlan = AttackManager.DEFAULT_PLAN_ID;
            return AttackManager.getSingleton().getAttackPlan(activePlan).size();
        }
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        try{
        String activePlan = getActiveAttackPlan();
        Attack a = AttackManager.getSingleton().getAttackPlan(activePlan).get(pRow);
        switch (pCol) {
            case 0: {
                if (pValue == null) {
                    a.setSource(null);
                } else {
                    a.setSource((Village) pValue);
                }
                break;
            }
            case 1: {
                if (pValue == null) {
                    a.setTarget(null);
                } else {
                    a.setTarget((Village) pValue);
                }
                break;
            }
            case 2: {
                if (pValue == null) {
                    a.setUnit(null);
                } else {
                    a.setUnit((UnitHolder) pValue);
                }
                break;
            }
            case 3: {
                if (pValue == null) {
                    a.setArriveTime(null);
                } else {
                    Date sendTime = (Date) pValue;
                    long arriveTime = sendTime.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                    a.setArriveTime(new Date(arriveTime));
                }
                break;
            }
            case 4: {
                if (pValue == null) {
                    a.setArriveTime(null);
                } else {
                    a.setArriveTime((Date) pValue);
                }
                break;
            }
            case 5: {
                if (pValue == null) {
                    a.setShowOnMap(false);
                } else {
                    a.setShowOnMap((Boolean) pValue);
                }
                break;
            }
            default: {
                if (pValue == null) {
                    a.setType(Attack.NO_TYPE);
                } else {
                    a.setType((Integer) pValue);
                }
                break;
            }
        }
        //DSWorkbenchAttackFrame.getSingleton().updateTableUI();
        }catch(Exception e){
        }
    }

    @Override
    public int getColumnCount() {
        return types.length;
    }
}
