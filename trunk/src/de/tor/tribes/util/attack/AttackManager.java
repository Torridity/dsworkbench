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
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Enumeration;
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
        mAttackPlans.put(DEFAULT_PLAN_ID, new LinkedList<Attack>());
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
        mAttackPlans.put(DEFAULT_PLAN_ID, new LinkedList<Attack>());
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
                Document d = JaxenUtils.getDocument(attackFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//plans/plan")) {
                    String planKey = e.getAttributeValue("key");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading plan '" + planKey + "'");
                    }
                    List<Attack> planAttacks = new LinkedList<Attack>();
                    for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "attacks/attack")) {
                        Attack a = new Attack(e1);
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

    public void saveAttacksToDatabase(String pUrl) {
        //not implemented yet
    }

    public void saveAttacksToFile(String pFile) {
        try {
            FileWriter w = new FileWriter(pFile);
            w.write("<plans>\n");
            Enumeration<String> plans = mAttackPlans.keys();
            while (plans.hasMoreElements()) {
                String key = plans.nextElement();
                w.write("<plan key=\"" + key + "\">\n");
                List<Attack> attacks = mAttackPlans.get(key);
                w.write("<attacks>\n");
                for (Attack a : attacks) {
                    w.write(a.toXml() + "\n");
                }
                w.write("</attacks>\n");
                w.write("</plan>\n");
            }
            w.write("</plans>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store attacks", e);
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
        List<Attack> attackPlan = mAttackPlans.get(plan);
        if (attackPlan == null) {
            attackPlan = new LinkedList<Attack>();
            attackPlan.add(a);
            mAttackPlans.put(plan, attackPlan);
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

        List<Attack> planAttacks = mAttackPlans.get(plan);
        Attack[] attacks = planAttacks.toArray(new Attack[]{});
        if (planAttacks != null) {
            for (int i : pIDs) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removing attack " + i + " from plan '" + plan + "'");
                }
                planAttacks.remove(attacks[i]);
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

    public Enumeration<String> getPlans() {
        return mAttackPlans.keys();
    }

    public void attacksUpdatedExternally(String pPlan) {
        fireAttacksChangedEvents(pPlan);
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
                    "Herkunft", "Ziel", "Einheit", "Abschickzeit", "Ankunftzeit", "Einzeichnen"
                }) {

            Class[] types = new Class[]{
                Village.class, Village.class, UnitHolder.class, Date.class, Date.class, Boolean.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        List<Attack> planAttacks = mAttackPlans.get(plan);

        if (planAttacks != null) {
            for (int i = 0; i < planAttacks.size(); i++) {
                UnitHolder unit = planAttacks.get(i).getUnit();
                Date arriveTime = planAttacks.get(i).getArriveTime();

                Village source = planAttacks.get(i).getSource();
                Village target = planAttacks.get(i).getTarget();
                Date sendTime = new Date(arriveTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(source, target, unit.getSpeed()) * 1000));
                model.addRow(new Object[]{source, target, unit, sendTime, arriveTime, planAttacks.get(i).isShowOnMap()});
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
        String xml = "H:/Software/DSWorkbench/servers/de26/a.xml";
        AttackManager.getSingleton().loadAttacksFromFile(xml);
        System.out.println(AttackManager.getSingleton().getPlans().nextElement());

    }
}
