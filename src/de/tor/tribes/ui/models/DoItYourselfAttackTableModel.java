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
import de.tor.tribes.util.parser.VillageParser;
import java.util.Collections;
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
        Integer.class, UnitHolder.class, Village.class, Village.class, Date.class, Date.class, String.class
    };
    String[] colNames = new String[]{
        "Angriffstyp", "Einheit", "Herkunft", "Ziel", "Abschickzeit", "Ankunftzeit", "Verbleibend"
    };
    private static DoItYourselfAttackTableModel SINGLETON = null;
    //  private List<Attack> mAttacks = null;

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
        AttackManager.getSingleton().removeDoItYourselfAttack(pRow);
        fireTableDataChanged();
    }

    public Attack getAttack(int pRow) {
        return AttackManager.getSingleton().getDoItYourselfAttacks().get(pRow);
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
        if (col == 6 || col == 4) {
            return false;
        }
        return true;
    }

    @Override
    public Object getValueAt(int pRow, int pCol) {
        Attack a = null;
        List<Attack> attacks = AttackManager.getSingleton().getDoItYourselfAttacks();
        if (attacks.size() > pRow) {
            a = attacks.get(pRow);
        } else {
            return null;
        }
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
        List<Attack> attacks = AttackManager.getSingleton().getDoItYourselfAttacks();
        if (attacks == null || attacks.size() == 0) {
            return 0;
        }
        return attacks.size();
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        List<Attack> attacks = AttackManager.getSingleton().getDoItYourselfAttacks();
        if (attacks == null || attacks.size() == 0 || attacks.size() < pRow) {
            return;
        }
        switch (pCol) {
            case 0: {
                attacks.get(pRow).setType((Integer) pValue);
                break;
            }
            case 1: {
                attacks.get(pRow).setUnit((UnitHolder) pValue);
                break;
            }
            case 2: {
                try {
                    String v = (String) pValue;
                    List<Village> parsed = VillageParser.parse(v);
                    Village vil = parsed.get(0);
                    if (vil != null) {
                        attacks.get(pRow).setSource(vil);
                    }
                } catch (Exception e) {
                    attacks.get(pRow).setSource(null);
                }
                break;
            }
            case 3: {
                try {
                    String v = (String) pValue;
                    List<Village> parsed = VillageParser.parse(v);
                    Village vil = parsed.get(0);
                    if (vil != null) {
                        attacks.get(pRow).setTarget(vil);
                    }
                } catch (Exception e) {
                    attacks.get(pRow).setTarget(null);
                }
                break;
            }
            case 5: {
                attacks.get(pRow).setArriveTime((Date) pValue);
            }
            default: {
            }
        }
    }
}
