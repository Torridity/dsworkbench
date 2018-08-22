/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util.attack;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.xml.JDomUtils;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author Torridity
 */
public class AttackManager extends GenericManager<Attack> {

    private static Logger logger = LogManager.getLogger("AttackManager");
    public final static String MANUAL_ATTACK_PLAN = "Manuelle Planung";
    private static AttackManager SINGLETON = null;

    public static synchronized AttackManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new AttackManager();
        }
        return SINGLETON;
    }

    AttackManager() {
        super(true);
        addGroup(MANUAL_ATTACK_PLAN);
    }

    @Override
    public void initialize() {
        super.initialize();
        addGroup(MANUAL_ATTACK_PLAN);
    }

    @Override
    public String[] getGroups() {
        String[] groups = super.getGroups();
        Arrays.sort(groups, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                if (o1.equals(DEFAULT_GROUP) || o1.equals(MANUAL_ATTACK_PLAN)) {
                    return -1;
                } else if (o2.equals(DEFAULT_GROUP) || o2.equals(MANUAL_ATTACK_PLAN)) {
                    return 1;
                } else {
                    return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
                }
            }
        });
        return groups;
    }

    @Override
    public int importData(Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        invalidate();
        logger.info("Loading troop movements");

        try {
            for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "plans/plan")) {
                String planKey = e.getAttributeValue("key");
                planKey = URLDecoder.decode(planKey, "UTF-8");
                if (pExtension != null) {
                    planKey += "_" + pExtension;
                }
                logger.debug("Loading plan '{}'", planKey);
                addGroup(planKey);
                for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "attacks/attack")) {
                    Attack a = new Attack();
                    a.loadFromXml(e1);

                    if (a.getSource() != null && a.getTarget() != null) {
                        addManagedElement(planKey, a);
                        result++;
                    }
                }
            }
            logger.debug("Troop movements loaded successfully");
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to load troop movements", e);
        }
        revalidate(true);
        return result;
    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element plans = new Element("plans");
        if (pGroupsToExport == null || pGroupsToExport.isEmpty()) {
            return plans;
        }
        logger.debug("Generating attacks data");

        for (String plan : pGroupsToExport) {
            try {
                Element planE = new Element("plan");
                planE.setAttribute("key", URLEncoder.encode(plan, "UTF-8"));

                Element attacks = new Element("attacks");
                for (ManageableType elem : getAllElements(plan)) {
                    attacks.addContent(elem.toXml("attack"));
                }
                planE.addContent(attacks);
                plans.addContent(planE);
            } catch (Exception e) {
                logger.warn("Failed to generate plan '" + plan + "'", e);
            }
        }
        logger.debug("Data generated successfully");
        return plans;
    }

    /**
     * Add an attack to a plan
     *
     * type is automatically determined through unit
     */
    public void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime, String pPlan) {
        boolean showOnMap = GlobalOptions.getProperties().getBoolean("draw.attacks.by.default");
        addAttack(pSource, pTarget, pUnit, pArriveTime, showOnMap, pPlan, Attack.NO_TYPE, false);
    }

    /**
     * Add an attack to a plan
     *
     * type is automatically determined through unit
     */
    public void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime, boolean pShowOnMap, String pPlan, Integer pType, boolean pTransferredToBrowser) {
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
        a.setTransferredToBrowser(pTransferredToBrowser);
        if (pType == -1) {
            switch (pUnit.getPlainName()) {
                case "catapult":
                case "ram":
                case "axe":
                    a.setType(Attack.CLEAN_TYPE);
                    break;
                case "snob":
                    a.setType(Attack.SNOB_TYPE);
                    break;
                case "spear":
                case "sword":
                case "heavy":
                    a.setType(Attack.SUPPORT_TYPE);
                    break;
                default:
                    a.setType(Attack.NO_TYPE);
                    break;
            }
        } else {
            a.setType(pType);
        }
        a.setTroopsByType();
        
        addManagedElement(pPlan, a);
    }

    public void clearDoItYourselfAttacks() {
        removeAllElementsFromGroup(MANUAL_ATTACK_PLAN);
    }

    public List<ManageableType> getDoItYourselfAttacks() {
        return getAllElements(MANUAL_ATTACK_PLAN);
    }
}
