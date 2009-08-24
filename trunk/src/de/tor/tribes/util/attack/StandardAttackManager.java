/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.attack;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.StandardAttackElement;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class StandardAttackManager {

    private static Logger logger = Logger.getLogger("StandardAttackManager");
    public static final int NO_TYPE_ROW = 0;
    public static final int OFF_TYPE_ROW = 1;
    public static final int SNOB_TYPE_ROW = 2;
    public static final int SUPPORT_TYPE_ROW = 3;
    public static final int FAKE_TYPE_ROW = 4;
    private static StandardAttackManager SINGLETON = null;
    private Hashtable<String, List<StandardAttackElement>> standardAttacks = null;

    public static synchronized StandardAttackManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new StandardAttackManager();
        }
        return SINGLETON;
    }

    StandardAttackManager() {
        standardAttacks = new Hashtable<String, List<StandardAttackElement>>();
    }

    public Hashtable<String, List<StandardAttackElement>> getStandardAttacks() {
        return standardAttacks;
    }

    public void loadStandardAttacksFromDatabase(String pUrl) {
        //not yet implemented
    }

    public void loadStandardAttacksFromDisk(String pFile) {
        standardAttacks.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        File attackFile = new File(pFile);
        if (attackFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.info("Loading standard attacks from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(attackFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//stdAttacks/type")) {
                    String type = URLDecoder.decode(e.getAttributeValue("name"), "UTF-8");
                    List<StandardAttackElement> elements = new LinkedList<StandardAttackElement>();
                    standardAttacks.put(type, elements);
                    for (Element elem : (List<Element>) JaxenUtils.getNodes(e, "attackElement")) {
                        StandardAttackElement element = null;
                        try {
                            element = StandardAttackElement.fromXml(elem);
                        } catch (Exception invalid) {
                        }

                        if (element != null) {
                            elements.add(element);
                        }
                    }
                    for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                        int typeID = NO_TYPE_ROW;
                        if (type.equals("Keiner")) {
                            typeID = NO_TYPE_ROW;
                        } else if (type.equals("Fake")) {
                            typeID = FAKE_TYPE_ROW;
                        } else if (type.equals("Off")) {
                            typeID = OFF_TYPE_ROW;
                        } else if (type.equals("AG")) {
                            typeID = SNOB_TYPE_ROW;
                        } else if (type.equals("Unterstützung")) {
                            typeID = SUPPORT_TYPE_ROW;
                        }
                        if (!containsElementForUnit(typeID, unit)) {
                            standardAttacks.get(type).add(new StandardAttackElement(unit, 0));
                        }
                    }
                }

                logger.debug("Standard attacks loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load standard attacks", e);
            }
        } else {
            logger.info("No standard attacks found under '" + pFile + "'");
            standardAttacks.put("Keiner", new LinkedList<StandardAttackElement>());
            standardAttacks.put("Fake", new LinkedList<StandardAttackElement>());
            standardAttacks.put("Off", new LinkedList<StandardAttackElement>());
            standardAttacks.put("AG", new LinkedList<StandardAttackElement>());
            standardAttacks.put("Unterstützung", new LinkedList<StandardAttackElement>());
            for (int type = NO_TYPE_ROW; type <= SUPPORT_TYPE_ROW; type++) {
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    String typeName = "";
                    if (type == NO_TYPE_ROW) {
                        typeName = "Keiner";
                    } else if (type == FAKE_TYPE_ROW) {
                        typeName = "Fake";
                    } else if (type == OFF_TYPE_ROW) {
                        typeName = "Off";
                    } else if (type == SNOB_TYPE_ROW) {
                        typeName = "AG";
                    } else if (type == SUPPORT_TYPE_ROW) {
                        typeName = "Unterstützung";
                    }

                    if (!containsElementForUnit(type, unit)) {
                        standardAttacks.get(typeName).add(new StandardAttackElement(unit, 0));
                    }
                }
            }
        }
    }

    public void saveStandardAttacksToDatabase(String pUrl) {
        //not implemented yet
    }

    public void saveStandardAttacksToDisk(String pFile) {
        try {
            FileWriter w = new FileWriter(pFile);
            w.write("<stdAttacks>\n");
            Enumeration<String> keys = standardAttacks.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                List<StandardAttackElement> elements = standardAttacks.get(key);
                w.write("<type name=\"" + URLEncoder.encode(key, "UTF-8") + "\">\n");
                for (StandardAttackElement elem : elements) {
                    w.write(elem.toXml());
                }
                w.write("</type>\n");
            }

            w.write("</stdAttacks>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store standard attacks", e);
        }

    }

    public int getAmountForVillage(int pType, UnitHolder pUnit, Village pVillage) {
        List<StandardAttackElement> activeList = null;
        switch (pType) {
            case Attack.CLEAN_TYPE: {
                activeList = standardAttacks.get("Off");
                break;
            }
            case Attack.FAKE_TYPE: {
                activeList = standardAttacks.get("Fake");
                break;
            }
            case Attack.SNOB_TYPE: {
                activeList = standardAttacks.get("AG");
                break;
            }
            case Attack.SUPPORT_TYPE: {
                activeList = standardAttacks.get("Unterstützung");
                break;
            }
            default: {
                activeList = standardAttacks.get("Keiner");
                break;
            }
        }

        for (StandardAttackElement elem : activeList) {
            if (elem.affectsUnit(pUnit)) {
                return elem.getTroopsAmount(pVillage);
            }
        }

        return 0;
    }

    public StandardAttackElement getElementForUnit(int pType, UnitHolder pUnit) {

        List<StandardAttackElement> activeList = null;
        switch (pType) {
            case OFF_TYPE_ROW: {
                activeList = standardAttacks.get("Off");
                break;
            }
            case FAKE_TYPE_ROW: {
                activeList = standardAttacks.get("Fake");
                break;
            }
            case SNOB_TYPE_ROW: {
                activeList = standardAttacks.get("AG");
                break;
            }
            case SUPPORT_TYPE_ROW: {
                activeList = standardAttacks.get("Unterstützung");
                break;
            }
            default: {
                activeList = standardAttacks.get("Keiner");
                break;
            }
        }

        for (StandardAttackElement elem : activeList) {
            if (elem.affectsUnit(pUnit)) {
                return elem;
            }
        }
        return null;
    }

    private boolean containsElementForUnit(int pType, UnitHolder pUnit) {
        List<StandardAttackElement> activeList = null;
        switch (pType) {
            case OFF_TYPE_ROW: {
                activeList = standardAttacks.get("Off");
                break;
            }
            case FAKE_TYPE_ROW: {
                activeList = standardAttacks.get("Fake");
                break;
            }
            case SNOB_TYPE_ROW: {
                activeList = standardAttacks.get("AG");
                break;
            }
            case SUPPORT_TYPE_ROW: {
                activeList = standardAttacks.get("Unterstützung");
                break;
            }
            default: {
                activeList = standardAttacks.get("Keiner");
                break;
            }
        }

        for (StandardAttackElement elem : activeList) {
            if (elem.affectsUnit(pUnit)) {
                return true;
            }
        }
        return false;
    }
}
