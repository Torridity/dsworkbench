/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.BarbarianAlly;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchAttackFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.attack.AttackManagerListener;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class AttackManagerTableModel extends AbstractDSWorkbenchTableModel {

    private final String PROPERTY_BASE_ID = "attack.table.model";
    private static Logger logger = Logger.getLogger("AttackTable");
    protected static Class[] types;
    protected static String[] colNames;
    protected static List<String> internalNames;
    protected static boolean[] editableColumns = null;
    public final static int ATTACKER_COL = 0;
    public final static int ATTACKER_ALLY_COL = 1;
    public final static int SOURCE_COL = 2;
    public final static int DEFENDER_COL = 3;
    public final static int DEFENDER_ALLY_COL = 4;
    public final static int TARGET_COL = 5;
    public final static int UNIT_COL = 6;
    public final static int SEND_TIME_COL = 7;
    public final static int ARRIVE_TIME_COL = 8;
    public final static int DRAW_COL = 9;
    public final static int TYPE_COL = 10;
    public final static int COUNTDOWN_COL = 11;

    static {
        types = new Class[]{Tribe.class, Ally.class, Village.class, Tribe.class, Ally.class, Village.class, UnitHolder.class, Date.class, Date.class, Boolean.class, Integer.class, String.class};
        colNames = new String[]{"Angreifer", "Stamm (Angreifer)", "Herkunft", "Verteidiger", "Stamm (Verteidiger)", "Ziel", "Einheit", "Abschickzeit", "Ankunftzeit", "Einzeichnen", "Typ", "Verbleibend"};
        internalNames = Arrays.asList(new String[]{"Angreifer", "Stamm (Angreifer)", "Herkunft", "Verteidiger", "Stamm (Verteidiger)", "Ziel", "Einheit", "Abschickzeit", "Ankunftzeit", "Einzeichnen", "Typ", "Countdown"});
        editableColumns = new boolean[]{false, false, true, false, false, true, true, true, true, true, true, false};
    }
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
    public boolean isCellEditable(int row, int col) {
        col = getRealColumnId(col);
        if (col == 0 || col == 1 || col == 3 || col == 4 || col == 11) {
            //attacker, defender and countdown are not editable
            return false;
        }
        return true;
    }

    public Attack getAttackAtRow(int pRow) {
        List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(getActiveAttackPlan());
        if (attacks.size() > pRow) {
            return attacks.get(pRow);
        } else {
            return null;
        }
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
            pCol = getRealColumnId(pCol);
            switch (pCol) {
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
                case 7: {
                    long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                    return new Date(sendTime);
                }
                case 8:
                    return a.getArriveTime();
                case 9:
                    return a.isShowOnMap();
                case 10:
                    return a.getType();
                default:
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
        try {
            String activePlan = getActiveAttackPlan();
            Attack a = AttackManager.getSingleton().getAttackPlan(activePlan).get(pRow);
            pCol = getRealColumnId(pCol);
            switch (pCol) {
                case 2: {
                    if (pValue == null) {
                        a.setSource(null);
                    } else {
                        a.setSource((Village) pValue);
                    }
                    break;
                }
                case 5: {
                    if (pValue == null) {
                        a.setTarget(null);
                    } else {
                        a.setTarget((Village) pValue);
                    }
                    break;
                }
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
                        a.setArriveTime(null);
                    } else {
                        Date sendTime = (Date) pValue;
                        long arriveTime = sendTime.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                        a.setArriveTime(new Date(arriveTime));
                    }
                    break;
                }
                case 8: {
                    if (pValue == null) {
                        a.setArriveTime(null);
                    } else {
                        a.setArriveTime((Date) pValue);
                    }
                    break;
                }
                case 9: {
                    if (pValue == null) {
                        a.setShowOnMap(false);
                    } else {
                        a.setShowOnMap((Boolean) pValue);
                    }
                    break;
                }
                case 10: {
                    if (pValue == null) {
                        a.setType(Attack.NO_TYPE);
                    } else {
                        a.setType((Integer) pValue);
                    }
                    break;
                }
                default: {
                    //not editable
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public String getPropertyBaseID() {
        return PROPERTY_BASE_ID;
    }

    @Override
    public Class[] getColumnClasses() {
        return types;
    }

    @Override
    public String[] getColumnNames() {
        return colNames;
    }

    @Override
    public List<String> getInternalColumnNames() {
        return internalNames;
    }

    @Override
    public boolean[] getEditableColumns() {
        return editableColumns;
    }

    @Override
    public void doNotifyOnColumnChange() {
        DSWorkbenchAttackFrame.getSingleton().fireAttacksChangedEvent(null);
    }
}
