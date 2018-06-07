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
import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.util.xml.JDomUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * @author Charon
 */
public class StandardAttackManager extends GenericManager<StandardAttack> {

    private static final Logger logger = LogManager.getLogger("StandardAttackManager");
    public static final String NO_TYPE_NAME = "Keine Auswahl";
    public static final String FAKE_TYPE_NAME = "Fake";
    public static final String OFF_TYPE_NAME = "Off";
    public static final String SNOB_TYPE_NAME = "AG";
    public static final String SUPPORT_TYPE_NAME = "Unterstützung";
    public static final String FAKE_SUPPORT_TYPE_NAME = "Unterstützung (Fake)";
    private static StandardAttackManager SINGLETON = null;

    public static synchronized StandardAttackManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new StandardAttackManager();
        }
        return SINGLETON;
    }

    StandardAttackManager() {
        super(false);
    }

    private void checkValues() {
        if (getElementByName(NO_TYPE_NAME) == null) {
            addManagedElement(new StandardAttack(NO_TYPE_NAME, StandardAttack.NO_ICON));
        }
        if (getElementByName(OFF_TYPE_NAME) == null) {
            addManagedElement(new StandardAttack(OFF_TYPE_NAME, StandardAttack.OFF_ICON));
        }
        if (getElementByName(FAKE_TYPE_NAME) == null) {
            addManagedElement(new StandardAttack(FAKE_TYPE_NAME, StandardAttack.FAKE_ICON));
        }
        if (getElementByName(SNOB_TYPE_NAME) == null) {
            addManagedElement(new StandardAttack(SNOB_TYPE_NAME, StandardAttack.SNOB_ICON));
        }
        if (getElementByName(SUPPORT_TYPE_NAME) == null) {
            addManagedElement(new StandardAttack(SUPPORT_TYPE_NAME, StandardAttack.SUPPORT_ICON));
        }
        if (getElementByName(FAKE_SUPPORT_TYPE_NAME) == null) {
            addManagedElement(new StandardAttack(FAKE_SUPPORT_TYPE_NAME, StandardAttack.FAKE_SUPPORT_ICON));
        }
    }

    public StandardAttack getElementByName(final String pName) {
        Object result = CollectionUtils.find(getAllElements(), new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((StandardAttack) o).getName().equals(pName);
            }
        });

        return (StandardAttack) result;
    }

    public StandardAttack getElementByIcon(final int pIcon) {
        Object result = CollectionUtils.find(getAllElements(), new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((StandardAttack) o).getIcon() == pIcon;
            }
        });

        return (StandardAttack) result;
    }

    public boolean containsElementByName(final String pName) {
        Object result = CollectionUtils.find(getAllElements(), new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((StandardAttack) o).getName().equals(pName);
            }
        });

        return result != null;
    }

    public boolean containsElementByIcon(final int pIcon) {
        Object result = CollectionUtils.find(getAllElements(), new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((StandardAttack) o).getIcon() == pIcon;
            }
        });

        return result != null;
    }

    public boolean addStandardAttack(String pName, int pIcon) {
        if (isAllowedName(pName) && !containsElementByName(pName) && isAllowedIcon(pIcon) && !containsElementByIcon(pIcon)) {
            addManagedElement(new StandardAttack(pName, pIcon));
            return true;
        }
        return false;
    }

    public boolean isAllowedName(String pName) {
        return !(pName == null
                || pName.equals(StandardAttackManager.NO_TYPE_NAME)
                || pName.equals(StandardAttackManager.OFF_TYPE_NAME)
                || pName.equals(StandardAttackManager.FAKE_TYPE_NAME)
                || pName.equals(StandardAttackManager.SNOB_TYPE_NAME)
                || pName.equals(StandardAttackManager.SUPPORT_TYPE_NAME)
                || pName.equals(StandardAttackManager.FAKE_SUPPORT_TYPE_NAME));
    }

    public boolean isAllowedIcon(int pIcon) {
        return pIcon != StandardAttack.NO_ICON
                && pIcon != StandardAttack.OFF_ICON
                && pIcon != StandardAttack.FAKE_ICON
                && pIcon != StandardAttack.SNOB_ICON
                && pIcon != StandardAttack.SUPPORT_ICON
                && pIcon != StandardAttack.FAKE_SUPPORT_ICON;
    }

    public boolean removeStandardAttack(StandardAttack pElement) {
        if (!isAllowedName(pElement.getName()) || !isAllowedIcon(pElement.getIcon())) {
            return false;
        }
        super.removeElement(pElement);
        return true;
    }

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
            logger.info("Loading standard attacks from '" + pFile + "'");
            try {
                Document d = JDomUtils.getDocument(attackFile);
                for (Element e : (List<Element>) JDomUtils.getNodes(d, "stdAttacks/stdAttack")) {
                    StandardAttack element = new StandardAttack();
                    element.loadFromXml(e);
                    addManagedElement(element);
                }
                checkValues();
                logger.debug("Standard attacks loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load standard attacks", e);
                checkValues();
            }
        } else {
            logger.info("No standard attacks found under '" + pFile + "'");
            checkValues();
        }
        revalidate();
    }

    @Override
    public void saveElements(String pFile) {
        try (FileWriter w = new FileWriter(pFile)) {
            w.write("<data><stdAttacks>\n");

            for (ManageableType element : getAllElements()) {
                w.write(element.toXml());
            }
            w.write("</stdAttacks></data>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store standard attacks", e);
        }
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        return "";
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        return false;
    }
}
