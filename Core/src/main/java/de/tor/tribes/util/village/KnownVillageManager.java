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
package de.tor.tribes.util.village;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.MinimapPanel;
import de.tor.tribes.util.xml.JDomUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * @author Charon
 */
public class KnownVillageManager extends GenericManager<KnownVillage> {

    private static Logger logger = LogManager.getLogger("ChurchManager");
    private static KnownVillageManager SINGLETON = null;

    private List<KnownVillage> churchVillages;
    private List<KnownVillage> watchtowerVillages;

    public static synchronized KnownVillageManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new KnownVillageManager();
        }
        return SINGLETON;
    }

    KnownVillageManager() {
        super(false);
        churchVillages = new ArrayList<>();
        watchtowerVillages = new ArrayList<>();
    }

    @Override
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        invalidate();
        initialize();
        File villageFile = new File(pFile);
        if (villageFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading KnownVillages from '" + pFile + "'");
            }
            try {
                Document d = JDomUtils.getDocument(villageFile);
                for (Element e : (List<Element>) JDomUtils.getNodes(d, "villages/village")) {
                    try {
                        KnownVillage v = new KnownVillage(e);
                        if (getKnownVillage(v.getVillage()) == null) {
                            addManagedElement(v);
                        } else {
                            //somehow this village appears twice in saved data
                            //this should never happen
                            KnownVillage merge = getKnownVillage(v.getVillage());
                            merge.updateInformation(v);
                        }
                    } catch (Exception inner) {
                        logger.debug("invaid line", inner);
                    }
                }
                logger.debug("KnownVillages successfully loaded");
            } catch (Exception e) {
                logger.error("Failed to load KnownVillages", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("KnownVillages file not found under '" + pFile + "'");
            }
        }
        rebuildWatchtowerChurchCache();
        revalidate(true);
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }
        boolean result = false;
        invalidate();
        logger.debug("Importing KnownVillages");
        try {
            Document d = JDomUtils.getDocument(pFile);
            for (Element e : (List<Element>) JDomUtils.getNodes(d, "villages/village")) {
                try {
                    KnownVillage v = new KnownVillage(e);
                    if (getKnownVillage(v.getVillage()) == null) {
                        addManagedElement(v);
                    } else {
                        //Village ist already Known
                        KnownVillage merge = getKnownVillage(v.getVillage());
                        merge.updateInformation(v);
                    }
                } catch (Exception inner) {
                    //ignored, marker invalid
                }
            }
            logger.debug("KnownVillages imported successfully");
            result = true;
        } catch (Exception e) {
            logger.error("Failed to import KnownVillages", e);
            MinimapPanel.getSingleton().redraw();
            result = false;
        }
        rebuildWatchtowerChurchCache();
        revalidate(true);
        return result;
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        logger.debug("Generating KnownVillages export data");

        String result = "<villages>\n";
        ManageableType[] elements = getAllElements().toArray(new ManageableType[getAllElements().size()]);

        for (ManageableType t : elements) {
            KnownVillage v = (KnownVillage) t;
            result += v.toXml() + "\n";
        }
        result += "</villages>\n";
        logger.debug("Export data generated successfully");
        return result;
    }

    @Override
    public void saveElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Writing KnownVillages to '" + pFile + "'");
        }
        try {
            StringBuilder b = new StringBuilder();

            b.append("<data><villages>\n");
            for (ManageableType t : getAllElements()) {
                KnownVillage c = (KnownVillage) t;
                b.append(c.toXml()).append("\n");
            }
            b.append("</villages></data>");
            //write data to file
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
            logger.debug("KnownVillages successfully saved");
        } catch (Exception e) {
            if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
                //this should only happen at the first start
                logger.info("Ignoring error, server directory does not exists yet");
            } else {
                logger.error("Failed to save churches", e);
            }
        }
    }

    public List<KnownVillage> getChurchVillages() {
        return churchVillages;
    }

    public void addChurchLevel(Village pVillage, int pLevel) {
        if (pVillage != null) {
            KnownVillage v = getKnownVillage(pVillage);
            if (v == null) {
                v = new KnownVillage(pVillage);
                v.setChurchLevel(pLevel);
                if(pLevel > 0) {
                    churchVillages.add(v);
                }
                addManagedElement(v);
            } else {
                v.setChurchLevel(pLevel);
                if (!churchVillages.contains(v) && pLevel > 0) {
                    churchVillages.add(v);
                }
                else if(churchVillages.contains(v) && pLevel == 0) {
                    churchVillages.remove(v);
                }
                fireDataChangedEvents();
            }
        }
    }

    public void removeChurch(Village pVillage) {
        if (pVillage != null) {
            KnownVillage toRemove = getKnownVillage(pVillage);
            if (toRemove != null) {
                toRemove.removeChurchInfo();
                churchVillages.remove(toRemove);
            }
        }
    }

    public void removeChurches(Village[] pVillages) {
        if (pVillages != null) {
            invalidate();
            for (Village v : pVillages) {
                removeChurch(v);
            }
            revalidate(true);
        }
    }

    public List<KnownVillage> getWatchtowerVillages() {
        return watchtowerVillages;
    }

    public void addWatchtowerLevel(Village pVillage, int pLevel) {
        if (pVillage != null) {
            KnownVillage v = getKnownVillage(pVillage);
            if (v == null) {
                v = new KnownVillage(pVillage);
                v.setWatchtowerLevel(pLevel);
                if(pLevel > 0) {
                    watchtowerVillages.add(v);
                }
                addManagedElement(v);
            } else {
                v.setWatchtowerLevel(pLevel);
                if (!watchtowerVillages.contains(v) && pLevel > 0) {
                    watchtowerVillages.add(v);
                }
                else if(watchtowerVillages.contains(v) && pLevel == 0) {
                    watchtowerVillages.remove(v);
                }
                fireDataChangedEvents();
            }
        }
    }

    public void removeWatchtower(Village pVillage) {
        if (pVillage != null) {
            KnownVillage toRemove = getKnownVillage(pVillage);
            if (toRemove != null) {
                toRemove.removeWatchtowerInfo();
                watchtowerVillages.remove(toRemove);
            }
        }
    }

    public void removeWatchtowers(Village[] pVillages) {
        if (pVillages != null) {
            invalidate();
            for (Village v : pVillages) {
                removeWatchtower(v);
            }
            revalidate(true);
        }
    }

    private KnownVillage getKnownVillage(Village pVillage) {
        List<ManageableType> elements = getAllElements();
        for (ManageableType elm : elements) {
            if (((KnownVillage) elm).getVillage().equals(pVillage)) {
                return (KnownVillage) elm;
            }
        }
        return null;
    }

    public void updateInformation(FightReport pReport) {
        if(pReport.getSpyLevel() >= pReport.SPY_LEVEL_BUILDINGS) {
            //update Building info if Buildings were spyed
            KnownVillage v = getKnownVillage(pReport.getTargetVillage());
            
            if (v != null) {
                v.updateInformation(pReport);
            } else {
                v = new KnownVillage(pReport.getTargetVillage());
                v.updateInformation(pReport);
                addManagedElement(v);
            }
            churchVillages.remove(v);
            watchtowerVillages.remove(v);
            if (v.hasChurch()) {
                churchVillages.add(v);
            }
            if (v.hasWatchtower()) {
                watchtowerVillages.add(v);
            }
            fireDataChangedEvents();
        }
    }
    
    private void rebuildWatchtowerChurchCache() {
        churchVillages.clear();
        watchtowerVillages.clear();
        
        for(ManageableType e :getAllElements()) {
            KnownVillage v = (KnownVillage) e;
            if (v.hasChurch()) {
                churchVillages.add(v);
            }
            if (v.hasWatchtower()) {
                watchtowerVillages.add(v);
            }
        }
        fireDataChangedEvents();
    }
}
