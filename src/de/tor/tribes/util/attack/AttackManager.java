/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.attack;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.GlobalOptions;
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.Logger;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.models.AttackManagerTableModel;
import de.tor.tribes.ui.renderer.MapRenderer;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Jejkal
 */
public class AttackManager {

    private static Logger logger = Logger.getLogger("AttackManager");
    private static AttackManager SINGLETON = null;
    private Hashtable<String, List<Attack>> mAttackPlans = null;
    public static final String DEFAULT_PLAN_ID = "default";
    private final List<AttackManagerListener> mManagerListeners = new LinkedList<AttackManagerListener>();

    public static synchronized AttackManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new AttackManager();
        }
        return SINGLETON;
    }

    AttackManager() {
        mAttackPlans = new Hashtable<String, List<Attack>>();
        mAttackPlans.put(DEFAULT_PLAN_ID, new LinkedList<Attack>());
    }

    public synchronized void addAttackManagerListener(AttackManagerListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mManagerListeners.contains(pListener)) {
            mManagerListeners.add(pListener);
        }
    }

    public synchronized void removeAttackManagerListener(AttackManagerListener pListener) {
        mManagerListeners.remove(pListener);
    }

    public void loadAttacksFromDatabase(String pUrl) {
        //not yet implemented
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
                    mAttackPlans.put(planKey, planAttacks);
                    for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "attacks/attack")) {
                        Attack a = new Attack(e1);
                        if (a != null) {
                            Village source = DataHolder.getSingleton().getVillages()[a.getSource().getX()][a.getSource().getY()];
                            Village target = DataHolder.getSingleton().getVillages()[a.getTarget().getX()][a.getTarget().getY()];
                            Date sendTime = new Date(a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(source, target, a.getUnit().getSpeed()) * 1000));
                            addAttack(source, target, a.getUnit(), a.getArriveTime(), a.isShowOnMap(), planKey);
                        //AttackManagerTableModel.getSingleton().addRow(new Object[]{source, target, a.getUnit(), sendTime, a.getArriveTime(), a.isShowOnMap()});
                        }
                    }
                }
                logger.debug("Attacks loaded successfully");
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

    public synchronized void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime) {
        boolean showOnMap = false;
        try {
            showOnMap = Boolean.parseBoolean(GlobalOptions.getProperty("draw.attacks.by.default"));
        } catch (Exception e) {
        }
        addAttack(pSource, pTarget, pUnit, pArriveTime, showOnMap, null);
    }

    public synchronized void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime, String pPlan) {
        boolean showOnMap = false;
        try {
            showOnMap = Boolean.parseBoolean(GlobalOptions.getProperty("draw.attacks.by.default"));
        } catch (Exception e) {
        }
        addAttack(pSource, pTarget, pUnit, pArriveTime, showOnMap, pPlan);
    }

    /**Add an attack to the default plan*/
    public synchronized void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime, boolean pShowOnMap) {
        //System.out.println("Attack " + pSource + ", " + pTarget+ ", " + de.tor.tribes.util.Constants.DATE_FORMAT.format(pArriveTime));
        addAttack(pSource, pTarget, pUnit, pArriveTime, pShowOnMap, null);
    }

    /**Add an attack to a plan*/
    public synchronized void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime, boolean pShowOnMap, String pPlan) {
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
        a.setShowOnMap(pShowOnMap);
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

    public synchronized void addEmptyPlan(String pPlan) {
        if (pPlan == null) {
            return;
        }
        List<Attack> attackPlan = new LinkedList<Attack>();
        mAttackPlans.put(pPlan, attackPlan);
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
        fireAttacksChangedEvents(plan);
    }

    public synchronized void renamePlan(String pPlan, String pNewName) {
        List<Attack> attacks = mAttackPlans.remove(pPlan);
        mAttackPlans.put(pNewName, attacks);
        AttackManagerTableModel.getSingleton().setActiveAttackPlan(pNewName);
        fireAttacksChangedEvents(pNewName);
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

    public String[] getPlansAsArray() {
        return mAttackPlans.keySet().toArray(new String[]{});
    }

    public void attacksUpdatedExternally(String pPlan) {
        fireAttacksChangedEvents(pPlan);
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
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ATTACK_LAYER);
    }
}
