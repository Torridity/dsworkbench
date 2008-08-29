/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.attack;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.Logger;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.table.DefaultTableModel;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Jejkal
 */
public class AttackManager {

    private static Logger logger = Logger.getLogger(AttackManager.class);
    private static AttackManager SINGLETON = null;
    private Hashtable<String, List<Attack>> mAttackPlans = null;
    private static final String DEFAULT_PLAN_ID = "default";
    private List<AttackManagerListener> mManagerListeners = null;

    public static synchronized AttackManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new AttackManager();
        }
        return SINGLETON;
    }

    AttackManager() {
        mAttackPlans = new Hashtable<String, List<Attack>>();
        mManagerListeners = new LinkedList<AttackManagerListener>();
    }

    public synchronized void addAttackManagerListener(AttackManagerListener pListener) {
        mManagerListeners.add(pListener);
    }

    public synchronized void removeAttackManagerListener(AttackManagerListener pListener) {
        mManagerListeners.remove(pListener);
    }

    public void loadAttacksFromFile(String pFile) {
        mAttackPlans.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        File attackFile = new File(pFile);
        if (attackFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.info("Loading attacks from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(pFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//plans/plan")) {
                    String planKey = e.getAttributeValue("key");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading plan '" + planKey + "'");
                    }
                    List<Attack> planAttacks = new LinkedList<Attack>();
                    for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "attacks/attack")) {
                        Attack a = new Attack(e);
                        if (a != null) {
                            planAttacks.add(a);
                        }
                    }
                    for (Attack a : planAttacks) {
                        a.setSource(DataHolder.getSingleton().getVillages()[a.getSource().getX()][a.getSource().getY()]);
                        a.setTarget(DataHolder.getSingleton().getVillages()[a.getTarget().getX()][a.getTarget().getY()]);
                    }
                    mAttackPlans.put(planKey, planAttacks);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Attacks loaded successfully");
                }
            } catch (Exception e) {
                logger.error("Failed to load attacks", e);
            }
        } else {
            logger.info("No attacks found under '" + pFile + "'");
        }
    }

    /**Add an attack to the default plan*/
    public synchronized void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime) {
        addAttack(pSource, pTarget, pUnit, pArriveTime, null);
    }

    /**Add an attack to a plan*/
    public synchronized void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime, String pPlan) {
        String plan = pPlan;
        if (plan == null) {
            plan = DEFAULT_PLAN_ID;
        }

        if (pSource == null || pTarget == null || pUnit == null || pArriveTime == null) {
            logger.error("Invalid attack");
            return;
        }

        Attack a = new Attack();
        a.setSource(pSource);
        a.setTarget(pTarget);
        a.setUnit(pUnit);
        a.setArriveTime(pArriveTime);
        List<Attack> attackPlan = mAttackPlans.get(pPlan);
        if (attackPlan == null) {
            attackPlan = new LinkedList<Attack>();
            attackPlan.add(a);
            mAttackPlans.put(pPlan, attackPlan);
        } else {
            attackPlan.add(a);
        }
        fireAttacksChangedEvents(plan);
    }

    /**Remove a complete attack plan*/
    public synchronized void removePlan(String pPlan) {
        String plan = pPlan;
        if (pPlan == null) {
            plan = DEFAULT_PLAN_ID;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Removing attack plan '" + plan + "'");
        }
        mAttackPlans.remove(plan);
    }

    /**Remove one attack from the default plan*/
    public synchronized void removeAttack(int pID) {
        removeAttacks(null, new int[]{pID});
    }

    /**Remove a number of attacks from the default plan*/
    public synchronized void removeAttack(int[] pIDs) {
        removeAttacks(null, pIDs);
    }

    /**Remove one attack from any plan*/
    public synchronized void removeAttack(String pPlan, int pID) {
        removeAttacks(pPlan, new int[]{pID});
    }

    /**Remove a number of attacks from any plan*/
    public synchronized void removeAttacks(String pPlan, int[] pIDs) {
        String plan = pPlan;
        if (plan == null) {
            plan = DEFAULT_PLAN_ID;
        }

        List<Attack> planAttacks = mAttackPlans.get(pPlan);
        if (planAttacks != null) {
            for (int i : pIDs) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removing attack " + i + " from plan '" + plan + "'");
                }
                planAttacks.remove(i);
            }
        }
        fireAttacksChangedEvents(plan);
    }

    public List<Attack> getAttackPlan(String pPlan) {
        String plan = pPlan;
        if (plan == null) {
            plan = DEFAULT_PLAN_ID;
        }
        return mAttackPlans.get(plan);
    }

    /**Get the table model for the default plan*/
    public synchronized DefaultTableModel getTableModel() {
        return getTableModel(null);
    }

    /**Get the table model for any plan*/
    public synchronized DefaultTableModel getTableModel(String pPlan) {
        String plan = pPlan;
        if (plan == null) {
            plan = DEFAULT_PLAN_ID;
        }

        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Ziel", "Einheit", "Ankunftszeit", "Einzeichnen"
                }) {

            Class[] types = new Class[]{
                Village.class, Village.class, UnitHolder.class, Date.class, Boolean.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        List<Attack> planAttacks = mAttackPlans.get(pPlan);
        if (planAttacks != null) {
            for (int i = 0; i < planAttacks.size(); i++) {
                model.setValueAt(planAttacks.get(i).getSource(), i, 0);
                model.setValueAt(planAttacks.get(i).getTarget(), i, 0);
                model.setValueAt(planAttacks.get(i).getUnit(), i, 0);
                model.setValueAt(planAttacks.get(i).getArriveTime(), i, 0);
                model.setValueAt(Boolean.TRUE, i, 0);
            }
        }

        return model;
    }

    /**Notify attack manager listeners about changes*/
    private void fireAttacksChangedEvents(String pPlan) {
        String plan = pPlan;
        if (plan == null) {
            plan = DEFAULT_PLAN_ID;
        }
        AttackManagerListener[] listeners = mManagerListeners.toArray(new AttackManagerListener[]{});
        for (AttackManagerListener listener : listeners) {
            listener.fireAttacksChangedEvent(plan);
        }
    }

    public static void main(String[] args) {
        String xml = "<plans><plan key=\"default\"><attacks><attack></attack></attacks></plan><plan key=\"master\"><attacks><attack></attack></attacks></plan></plans>";
        AttackManager.getSingleton().loadAttacksFromFile(xml);
    }
}
