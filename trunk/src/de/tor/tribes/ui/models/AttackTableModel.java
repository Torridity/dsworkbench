/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.BarbarianAlly;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.attack.AttackManager;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang.time.DurationFormatUtils;

/**
 *
 * @author Torridity
 */
public class AttackTableModel extends AbstractTableModel {

    private String sPlan = null;
    private Class[] types = new Class[]{Tribe.class, Ally.class, Village.class, Tribe.class, Ally.class, Village.class, UnitHolder.class, Integer.class, Date.class, Date.class, Long.class, Boolean.class, Boolean.class};
    private String[] colNames = new String[]{"Angreifer", "Stamm (Angreifer)", "Herkunft", "Verteidiger", "Stamm (Verteidiger)", "Ziel", "Einheit", "Typ", "Abschickzeit", "Ankunftzeit", "Verbleibend", "Einzeichnen", "Übertragen"};
    private boolean[] editableColumns = new boolean[]{false, false, false, false, false, false, true, true, true, true, false, true, true};

    public AttackTableModel(String pPlan) {
        sPlan = pPlan;
    }

    public void setPlan(String pPlan) {
        sPlan = pPlan;
        fireTableDataChanged();
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

    @Override
    public int getRowCount() {
        if (sPlan == null) {
            return 0;
        }
        return AttackManager.getSingleton().getAllElements(sPlan).size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (sPlan == null) {
            return null;
        }
        try {
            Attack a = (Attack) AttackManager.getSingleton().getAllElements(sPlan).get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    Tribe attacker = a.getSource().getTribe();
                    if (attacker == null) {
                        return Barbarians.getSingleton();
                    }
                    return attacker;
                }
                case 1: {
                    Tribe attacker = a.getSource().getTribe();
                    if (attacker == null) {
                        return BarbarianAlly.getSingleton();
                    }
                    Ally ally = attacker.getAlly();
                    if (ally == null) {
                        return NoAlly.getSingleton();
                    }
                    return ally;
                }
                case 2:
                    return a.getSource();
                case 3: {
                    Tribe defender = a.getTarget().getTribe();
                    if (defender == null) {
                        return Barbarians.getSingleton();
                    }
                    return defender;
                }
                case 4: {
                    Tribe defender = a.getTarget().getTribe();
                    if (defender == null) {
                        return Barbarians.getSingleton();
                    }
                    Ally ally = defender.getAlly();
                    if (ally == null) {
                        return NoAlly.getSingleton();
                    }
                    return ally;
                }
                case 5:
                    return a.getTarget();
                case 6:
                    return a.getUnit();
                case 7:
                    return a.getType();
                case 8: {
                    long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                    return new Date(sendTime);
                }
                case 9:
                    return a.getArriveTime();
                case 10: {
                    long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                    long t = sendTime - System.currentTimeMillis();
                    return (t <= 0) ? 0 : t;
                }
                case 11: {
                    return a.isShowOnMap();
                }
                default: {
                    return a.isTransferredToBrowser();
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        if (sPlan == null) {
            return;
        }
        try {
            Attack a = (Attack) AttackManager.getSingleton().getAllElements(sPlan).get(pRow);
            switch (pCol) {
                case 6: {
                    if (pValue == null) {
                        a.setUnit(null);
                    } else {
                        a.setUnit((UnitHolder) pValue);
                    }
                    break;
                }
                case 7: {
                    if (pValue == null) {
                        a.setType(Attack.NO_TYPE);
                    } else {
                        a.setType((Integer) pValue);
                    }
                    break;
                }
                case 8: {
                    if (pValue == null) {
                        a.setArriveTime(null);
                    } else {
                        Date sendTime = (Date) pValue;
                        long arriveTime = sendTime.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                        a.setArriveTime(new Date(arriveTime));
                    }
                    break;
                }
                case 9: {
                    if (pValue == null) {
                        a.setArriveTime(null);
                    } else {
                        a.setArriveTime((Date) pValue);
                    }
                    break;
                }
                case 11: {
                    a.setShowOnMap((Boolean) pValue);
                    break;
                }
                case 12: {
                    a.setTransferredToBrowser((Boolean) pValue);
                    break;
                }

                default: {
                    //not editable
                    break;
                }
            }
        } catch (Exception e) {
        }
        AttackManager.getSingleton().revalidate(sPlan, true);
    }
}