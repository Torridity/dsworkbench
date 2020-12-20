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
import de.tor.tribes.control.GenericManagerListener;
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

    private boolean cacheValid = false;
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
        
        addManagerListener(new GenericManagerListener() {
            @Override
            public void dataChangedEvent() {
                logger.debug("invalidating cache");
                cacheValid = false;
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                logger.debug("invalidating cache grouped");
                cacheValid = false;
            }
        });
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
                    if(v.getVillage() == null) {
                        logger.warn("Known Village without Village {}", e.getChild("id").getText());
                        //ignore this entry maybe village got deleted
                        continue;
                    }
                    if(! v.containsInformation()) {
                        continue;
                    }
                    if (getKnownVillage(v.getVillage()) == null) {
                        addManagedElement(v);
                    } else {
                        //Village ist already Known
                        KnownVillage merge = getKnownVillage(v.getVillage());
                        merge.updateInformation(v);
                    }
                    result++;
                } catch (Exception inner) {
                    logger.debug("Got invalid knownvillage:", inner);
                    //ignored, village invalid
                }
            }
            logger.debug("KnownVillages successfully loaded");
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to load KnownVillages", e);
            MinimapPanel.getSingleton().redraw();
        }
        revalidate(true);
        return result;
    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element villages = new Element("villages");
        
        logger.debug("Generating KnownVillages data");
        for (ManageableType t : getAllElements()) {
            Element e = t.toXml("village");
            if(e != null) {
                villages.addContent(e);
            }
        }
        logger.debug("Data generated successfully");
        return villages;
    }

    public List<KnownVillage> getChurchVillages() {
        if(!cacheValid) {
            rebuildWatchtowerChurchCache();
        }
        return churchVillages;
    }

    public void addChurchLevel(Village pVillage, int pLevel, boolean pTimeUpdate) {
        if (pVillage != null) {
            KnownVillage v = getKnownVillage(pVillage);
            if (v == null) {
                v = new KnownVillage(pVillage);
                v.setChurchLevel(pLevel);
                if(pTimeUpdate) {
                    v.updateTime();
                }
                addManagedElement(v);
            } else {
                v.setChurchLevel(pLevel);
                if(pTimeUpdate) {
                    v.updateTime();
                }
                fireDataChangedEvents();
            }
        }
    }

    public void removeChurch(Village pVillage, boolean pTimeUpdate) {
        if (pVillage != null) {
            KnownVillage knowV = getKnownVillage(pVillage);
            knowV.removeChurchInfo();
            if(pTimeUpdate) {
                knowV.updateTime();
            }
            cacheValid = false;
        }
    }

    public void removeChurches(Village[] pVillages, boolean pTimeUpdate) {
        if (pVillages != null) {
            invalidate();
            for (Village v : pVillages) {
                removeChurch(v, pTimeUpdate);
            }
            revalidate(true);
        }
    }

    public List<KnownVillage> getWatchtowerVillages() {
        if(!cacheValid) {
            rebuildWatchtowerChurchCache();
        }
        return watchtowerVillages;
    }

    public void addWatchtowerLevel(Village pVillage, int pLevel, boolean pTimeUpdate) {
        if (pVillage != null) {
            KnownVillage v = getKnownVillage(pVillage);
            if (v == null) {
                v = new KnownVillage(pVillage);
                v.setWatchtowerLevel(pLevel);
                if(pTimeUpdate) {
                    v.updateTime();
                }
                addManagedElement(v);
            } else {
                v.setWatchtowerLevel(pLevel);
                if(pTimeUpdate) {
                    v.updateTime();
                }
                fireDataChangedEvents();
            }
        }
    }

    public void removeWatchtower(Village pVillage, boolean pTimeUpdate) {
        if (pVillage != null) {
            KnownVillage knowV = getKnownVillage(pVillage);
            knowV.removeWatchtowerInfo();
            if(pTimeUpdate) {
                knowV.updateTime();
            }
            cacheValid = false;
        }
    }

    public void removeWatchtowers(Village[] pVillages, boolean pTimeUpdate) {
        if (pVillages != null) {
            invalidate();
            for (Village v : pVillages) {
                removeWatchtower(v, pTimeUpdate);
            }
            revalidate(true);
        }
    }
    
    public void removeVillage(Village pVillage) {
        if (pVillage != null) {
            removeElement(getKnownVillage(pVillage));
            cacheValid = false;
        }
    }
    
    public void removeVillages(Village[] pVillages) {
        if (pVillages != null) {
            invalidate();
            for (Village v : pVillages) {
                removeVillage(v);
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
        
        //none Found create new one
        KnownVillage k = new KnownVillage(pVillage);
        addManagedElement(k);
        return k;
    }

    public void updateInformation(FightReport pReport) {
        if(pReport.getSpyLevel() >= pReport.SPY_LEVEL_BUILDINGS) {
            //update Building info if Buildings were spyed
            KnownVillage v = getKnownVillage(pReport.getTargetVillage());
            
            if (v != null) {
                v.updateInformation(pReport);
                fireDataChangedEvents();
            } else {
                v = new KnownVillage(pReport.getTargetVillage());
                v.updateInformation(pReport);
                addManagedElement(v);
            }
        }
    }
    
    private void rebuildWatchtowerChurchCache() {
        logger.debug("rebuilding cache");
        churchVillages.clear();
        watchtowerVillages.clear();
        
        for(ManageableType e :getAllElements()) {
            KnownVillage v = (KnownVillage) e;
            if(v.getVillage() == null) continue; //maybe a bug during loading??
            if (v.hasChurch()) {
                churchVillages.add(v);
            }
            if (v.hasWatchtower()) {
                watchtowerVillages.add(v);
            }
        }
        
        logger.debug("rebuilded cache");
        cacheValid = true;
    }
    
    public void cleanEmptyVillages() {
        List<KnownVillage> toRemove = new ArrayList<>();
        for(ManageableType m : getAllElements()) {
            if(! ((KnownVillage) m).containsInformation()) {
                logger.debug("Removing village {}", m);
                toRemove.add((KnownVillage) m);
            }
        }
        removeElements(toRemove);
    }
}
