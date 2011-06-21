/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.views.DSWorkbenchDoItYourselfAttackPlaner;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.attack.AttackManager;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Torridity
 */
public class DoItYourselfAttackTableModel extends AbstractDSWorkbenchTableModel {

    private final String PROPERTY_BASE_ID = "manual.attack.planer.table.model";
    private static Logger logger = Logger.getLogger("DoItYourselfAttackTable");
    protected static Class[] types;
    protected static String[] colNames;
    protected static List<String> internalNames;
    protected static boolean[] editableColumns = null;

    static {
        types = new Class[]{Integer.class, UnitHolder.class, Village.class, Village.class, Date.class, Date.class, String.class};
        colNames = new String[]{"Angriffstyp", "Einheit", "Herkunft", "Ziel", "Abschickzeit", "Ankunftzeit", "Verbleibend"};
        internalNames = Arrays.asList(new String[]{"Angriffstyp", "Einheit", "Herkunft", "Ziel", "Abschickzeit", "Ankunftzeit", "Countdown"});
        editableColumns = new boolean[]{true, true, true, true, false, true, false, true, true, false};
    }
    private static DoItYourselfAttackTableModel SINGLETON = null;

    public static synchronized DoItYourselfAttackTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DoItYourselfAttackTableModel();
        }
        return SINGLETON;
    }

    DoItYourselfAttackTableModel() {
        //  mAttacks = new LinkedList<Attack>();
    }

    public void clear() {
        AttackManager.getSingleton().clearDoItYourselfAttacks();
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
        Attack a = null;
        List<ManageableType> elements = AttackManager.getSingleton().getDoItYourselfAttacks();
        if (elements.size() > pRow) {
            a = (Attack) elements.get(pRow);
        } else {
            return null;
        }
        pCol = convertViewColumnToModel(pCol);
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
                } catch (Exception e) {
                }
                return "00:00:00.000";
            }
        }
    }

    @Override
    public int getRowCount() {
        List<ManageableType> elements = AttackManager.getSingleton().getDoItYourselfAttacks();
        if (elements == null || elements.isEmpty()) {
            return 0;
        }
        return elements.size();
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        List<ManageableType> attacks = AttackManager.getSingleton().getAllElements(AttackManager.MANUAL_ATTACK_PLAN);
        if (attacks == null || attacks.isEmpty() || attacks.size() < pRow) {
            return;
        }
        pCol = convertViewColumnToModel(pCol);
        Attack a = (Attack)attacks.get(pRow);
        switch (pCol) {
            case 0: {
               a.setType((Integer) pValue);
                break;
            }
            case 1: {
                a.setUnit((UnitHolder) pValue);
                break;
            }
            case 2: {
                try {
                    String v = (String) pValue;
                    List<Village> parsed = PluginManager.getSingleton().executeVillageParser(v);
                    Village vil = parsed.get(0);
                    if (vil != null) {
                        a.setSource(vil);
                    }
                } catch (Exception e) {
                    a.setSource(null);
                }
                break;
            }
            case 3: {
                try {
                    String v = (String) pValue;
                    List<Village> parsed = PluginManager.getSingleton().executeVillageParser(v);
                    Village vil = parsed.get(0);
                    if (vil != null) {
                        a.setTarget(vil);
                    }
                } catch (Exception e) {
                    a.setTarget(null);
                }
                break;
            }
            case 5: {
                a.setArriveTime((Date) pValue);
                break;
            }
            default:
                break;
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
        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().fireRebuildTableEvent();
    }
}
