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
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 * @author Charon
 */
public class KnownVillageManager extends GenericManager<KnownVillage> {

    private static Logger logger = LogManager.getLogger("KnownVillageManager");
    private static KnownVillageManager SINGLETON = null;

    private final List<KnownVillage> churchVillages;
    private final List<KnownVillage> watchtowerVillages;

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
    public int importData(Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        invalidate();
        logger.debug("Reading KnownVillages");
        try {
            for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "villages/village")) {
                try {
                    KnownVillage v = new KnownVillage(e);
                    if (getKnownVillage(v.getVillage()) == null) {
                        addManagedElement(v);
                    } else {
                        //Village ist already Known
                        KnownVillage merge = getKnownVillage(v.getVillage());
                        merge.updateInformation(v);
                    }
                    result++;
                } catch (Exception inner) {
                    //ignored, marker invalid
                }
            }
            logger.debug("KnownVillages successfully loaded");
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to load KnownVillages", e);
            MinimapPanel.getSingleton().redraw();
        }
        rebuildWatchtowerChurchCache();
        revalidate(true);
        return result;
    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element villages = new Element("villages");
        
        logger.debug("Generating KnownVillages data");
        for (ManageableType t : getAllElements()) {
            villages.addContent(t.toXml("village"));
        }
        logger.debug("Data generated successfully");
        return villages;
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

    public KnownVillage getKnownVillage(Village pVillage) {
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
