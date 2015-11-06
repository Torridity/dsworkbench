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
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.GlobalOptions;
import java.util.List;
import org.apache.log4j.Logger;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class AttackManager extends GenericManager<Attack> {

    private static Logger logger = Logger.getLogger("AttackManager");
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

    /**
     *
     * @param pFile
     */
    @Override
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        invalidate();
        initialize();
        File attackFile = new File(pFile);
        if (attackFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.info("Loading troop movements from '" + pFile + "'");
            }

            try {
                Document d = JaxenUtils.getDocument(attackFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//plans/plan")) {
                    String planKey = e.getAttributeValue("key");
                    planKey = URLDecoder.decode(planKey, "UTF-8");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading plan '" + planKey + "'");
                    }
                    addGroup(planKey);
                    for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "attacks/attack")) {
                        Attack a = new Attack();
                        a.loadFromXml(e1);
                        if (a != null && a.getSource() != null && a.getTarget() != null) {
                            Village source = DataHolder.getSingleton().getVillages()[a.getSource().getX()][a.getSource().getY()];
                            Village target = DataHolder.getSingleton().getVillages()[a.getTarget().getX()][a.getTarget().getY()];
                            addAttack(source, target, a.getUnit(), a.getArriveTime(), a.isShowOnMap(), planKey, a.getType(), a.isTransferredToBrowser());
                        }
                    }
                }
                logger.debug("Troop movements loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load troop movements", e);
            }
        } else {
            logger.info("No troop movements found under '" + pFile + "'");
        }
        revalidate();
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        invalidate();
        boolean result = false;
        if (pFile == null) {
            logger.error("File argument is 'null'");
        } else {
            try {
                logger.info("Importing attacks");
                Document d = JaxenUtils.getDocument(pFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//plans/plan")) {
                    String planKey = e.getAttributeValue("key");
                    planKey = URLDecoder.decode(planKey, "UTF-8");
                    if (pExtension != null) {
                        planKey += "_" + pExtension;
                    }
                    addGroup(planKey);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading plan '" + planKey + "'");
                    }

                    for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "attacks/attack")) {
                        Attack a = new Attack();
                        a.loadFromXml(e1);

                        if (a != null) {
                            Village source = DataHolder.getSingleton().getVillages()[a.getSource().getX()][a.getSource().getY()];
                            Village target = DataHolder.getSingleton().getVillages()[a.getTarget().getX()][a.getTarget().getY()];
                            addAttack(source, target, a.getUnit(), a.getArriveTime(), a.isShowOnMap(), planKey, a.getType(), false);
                        }
                    }
                }

                logger.debug("Troop movements imported successfully");
                result = true;
            } catch (Exception e) {
                logger.error("Failed to import troop movements", e);
            }
        }
        revalidate(true);
        return result;
    }

    /**
     * @param plansToExport
     * @return
     */
    @Override
    public String getExportData(final List<String> plansToExport) {
        if (plansToExport.isEmpty()) {
            return "";
        }
        logger.debug("Generating attacks export data");
        StringBuilder b = new StringBuilder();
        b.append("<plans>\n");

        for (String plan : plansToExport) {
            try {
                b.append("<plan key=\"").append(URLEncoder.encode(plan, "UTF-8")).append("\">\n");

                ManageableType[] elements = getAllElements(plan).toArray(new ManageableType[getAllElements(plan).size()]);
                b.append("<attacks>\n");

                for (ManageableType elem : elements) {
                    b.append(elem.toXml()).append("\n");
                }
                b.append("</attacks>\n");
                b.append("</plan>\n");
            } catch (Exception e) {
                logger.warn("Failed to export plan '" + plan + "'", e);
            }
        }
        b.append("</plans>\n");
        logger.debug("Export data generated successfully");
        return b.toString();
    }

    @Override
    public void saveElements(String pFile) {

        try {
            StringBuilder b = new StringBuilder();
            b.append("<plans>\n");
            Iterator<String> plans = getGroupIterator();

            while (plans.hasNext()) {
                String key = plans.next();
                b.append("<plan key=\"").append(URLEncoder.encode(key, "UTF-8")).append("\">\n");
                List<ManageableType> elems = getAllElements(key);
                b.append("<attacks>\n");
                for (ManageableType elem : elems) {
                    b.append(elem.toXml()).append("\n");
                }

                b.append("</attacks>\n");
                b.append("</plan>\n");
            }

            b.append("</plans>\n");
            //write data to file
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store attacks", e);
        }

    }

    public void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime, String pPlan) {
        boolean showOnMap = GlobalOptions.getProperties().getBoolean("draw.attacks.by.default", false);
        addAttack(pSource, pTarget, pUnit, pArriveTime, showOnMap, pPlan, -1, false);
    }

    /**
     * Add an attack to a plan
     *
     * @param pSource
     * @param pTarget
     * @param pUnit
     * @param pShowOnMap
     * @param pArriveTime
     * @param pPlan
     * @param pTransferredToBrowser
     * @param pType
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
            if (pUnit.getPlainName().equals("ram")) {
                a.setType(Attack.CLEAN_TYPE);
            } else if (pUnit.getPlainName().equals("snob")) {
                a.setType(Attack.SNOB_TYPE);
            } else if (pUnit.getPlainName().equals("sword") || pUnit.getPlainName().equals("heavy")) {
                a.setType(Attack.SUPPORT_TYPE);
            } else {
                a.setType(Attack.NO_TYPE);
            }

        } else {
            a.setType(pType);
        }

        addManagedElement(pPlan, a);
    }

    public void addDoItYourselfAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime, int pType) {
        Attack a = new Attack();
        a.setSource(pSource);
        a.setTarget(pTarget);
        a.setArriveTime(pArriveTime);
        a.setUnit(pUnit);
        a.setType(pType);
        addManagedElement(MANUAL_ATTACK_PLAN, a);
    }

    public void clearDoItYourselfAttacks() {
        removeAllElementsFromGroup(MANUAL_ATTACK_PLAN);
    }

    public List<ManageableType> getDoItYourselfAttacks() {
        return getAllElements(MANUAL_ATTACK_PLAN);
    }
}
