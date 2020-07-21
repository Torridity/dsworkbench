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

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.BuildingSettings;
import de.tor.tribes.util.ServerSettings;
import java.awt.Color;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 * @author extremeCrazyCoder
 * 
 * Class to hold extra information of a Village like
 *  spy information
 *  watchtower / church Range
 */
public class KnownVillage extends ManageableType {
    private static Logger logger = LogManager.getLogger("KnownVillage");
    
    private int[] buildings;
    private Village village;
    private long lastUpdate;
    
    public KnownVillage(Village pVillage) {
        buildings = new int[BuildingSettings.BUILDING_NAMES.length];
        Arrays.fill(buildings, -1);
        this.village = pVillage;
        updateTime();
    }
    
    public KnownVillage(Element e) {
        buildings = new int[BuildingSettings.BUILDING_NAMES.length];
        Arrays.fill(buildings, -1);
        loadFromXml(e);
    }

    @Override
    public final void loadFromXml(Element pElement) {
        this.village = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("id").getText()));
        this.lastUpdate = Long.parseLong(pElement.getChild("update").getText());
        
        Element buildingElm = pElement.getChild("buildings");
        for(int i = 0; i < BuildingSettings.BUILDING_NAMES.length; i++) {
            String val = buildingElm.getAttributeValue(BuildingSettings.BUILDING_NAMES[i]);
            if(val != null) {
                try {
                    this.buildings[i] = Integer.parseInt(val);
                } catch(NumberFormatException e) {
                    this.buildings[i] = -1;
                    logger.debug("unable to decode property: {} with {}", BuildingSettings.BUILDING_NAMES[i], val, e);
                }
            }
            else
                logger.debug("property null: {}", BuildingSettings.BUILDING_NAMES[i]);
        }
    }

    @Override
    public Element toXml(String elementName) {
        Element kVillage = new Element(elementName);
        try {
            kVillage.addContent(new Element("id").setText(Integer.toString(village.getId())));
            kVillage.addContent(new Element("update").setText(Long.toString(lastUpdate)));
            
            Element buildingsE = new Element("buildings");
            for(int i = 0; i < BuildingSettings.BUILDING_NAMES.length; i++) {
                buildingsE.setAttribute(BuildingSettings.BUILDING_NAMES[i], Integer.toString(buildings[i]));
            }
            kVillage.addContent(buildingsE);
        } catch (Exception e) {
            logger.error("Exception during generating XML", e);
            return null;
        }
        
        return kVillage;
    }

    public Village getVillage() {
        return village;
    }

    public void updateInformation(KnownVillage other) {
        if(lastUpdate > other.getLastUpdate()) {
            //This is newer.... Just get Information that has not been discovered here
            for(int i = 0; i < BuildingSettings.BUILDING_NAMES.length; i++) {
                if(buildings[i] == -1) {
                    buildings[i] = other.getBuildingLevelByName(BuildingSettings.BUILDING_NAMES[i]);
                }
            }
        }
        else {
            //Other is newer ... Copy everything that is valid
            for(int i = 0; i < BuildingSettings.BUILDING_NAMES.length; i++) {
                int level = other.getBuildingLevelByName(BuildingSettings.BUILDING_NAMES[i]);
                if(level != -1) {
                    buildings[i] = level;
                }
            }
            lastUpdate = other.getLastUpdate();
        }
    }

    /**
     * @return the range
     */
    public int getChurchRange() {
        int level = getBuildingLevelByName("church");
        if(level == -1) return -1;
        return BuildingSettings.CHURCH_RANGE[level];
    }

    /**
     * @return the range
     */
    public double getWatchtowerRange() {
        int level = getBuildingLevelByName("watchtower");
        if(level == -1) return -1;
        return BuildingSettings.WATCHTOWER_RANGE[level];
    }

    /**
     * @return the rangeColor
     */
    public Color getRangeColor() {
        return village.getTribe().getMarkerColor();
    }

    public boolean hasChurch() {
        return getBuildingLevelByName("church") > 0;
    }

    public boolean hasWatchtower() {
        return getBuildingLevelByName("watchtower") > 0;
    }

    public void setChurchLevel(int pLevel) {
        if(!ServerSettings.getSingleton().isChurch()) {
            logger.info("Tried to set Church level " + pLevel + "on server without");
            return;
        }
        setBuildingLevelByName("church", pLevel);
        updateTime();
    }

    public void removeChurchInfo() {
        if(!ServerSettings.getSingleton().isChurch()) {
            logger.info("Tried to remove Church on server without");
            return;
        }
        setBuildingLevelByName("church", -1);
        updateTime();
    }

    public void setWatchtowerLevel(int pLevel) {
        if(!ServerSettings.getSingleton().isWatchtower()) {
            logger.info("Tried to set Watchtower level " + pLevel + "on server without");
            return;
        }
        setBuildingLevelByName("watchtower", pLevel);
        updateTime();
    }

    public void removeWatchtowerInfo() {
        if(!ServerSettings.getSingleton().isWatchtower()) {
            logger.info("Tried to remove Watchtower on server without");
            return;
        }
        setBuildingLevelByName("watchtower", -1);
        updateTime();
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    private void updateTime() {
        lastUpdate = System.currentTimeMillis() / 1000L;
    }
    
    /**
     * This Function returns the Level of a given Building
     * will return -1 if there is no Informaion about that Building stored
     * will return -2 if the building has no been found (logged as info)
     * @param building: Name of the Building
     * @return: The Level
     */
    public int getBuildingLevelByName(String pBuilding) {
        int id = BuildingSettings.getBuildingIdByName(pBuilding);
        if(id == -2) {
            logger.info("Building " + pBuilding + " not found");
        return -2;
        }

        return buildings[id];
    }

    public void setBuildingLevelByName(String pBuilding, int pLevel) {
        int id = BuildingSettings.getBuildingIdByName(pBuilding);
        logger.debug("Setting building {} ({}) to level {}", pBuilding, id, pLevel);
        if(id == -2) {
            logger.info("Building {} not found", pBuilding);
            return;
        }
        
        if(!BuildingSettings.isBuildingLevelValid(pBuilding, pLevel)) {
            logger.error("Building level invalid {}: {}", pBuilding, pLevel);
            return;
        }

        buildings[id] = pLevel;
    }
    
    public void setBuildingLevelById(int pBuildingId, int pLevel) {
        logger.debug("Setting building {} to level {}", pBuildingId, pLevel);
        if(!BuildingSettings.isBuildingLevelValid(pBuildingId, pLevel)) {
            logger.error("Building level invalid " + pBuildingId + ": " + pLevel);
            return;
        }

        buildings[pBuildingId] = pLevel;
    }

    @Override
    public String toString() {
        return village.getFullName();
    }

    void updateInformation(FightReport pReport) {
        if (pReport.getSpyLevel() >= pReport.SPY_LEVEL_BUILDINGS) {
            for(int i = 0; i < buildings.length; i++) {
                if(pReport.getBuilding(i) != -1) {
                    //Building was spyed
                    if(BuildingSettings.getMaxBuildingLevel(BuildingSettings.BUILDING_NAMES[i]) > 0) {
                        //Building can be build
                        setBuildingLevelById(i, pReport.getBuilding(i));
                        updateTime();
                    }
                }
            }
        } else if (pReport.getWallAfter() != -1) {
            // set wall destruction (works also without spying)
            setBuildingLevelByName("wall", pReport.getWallAfter());
        }
    }
    
    public int getFarmSpace() {
        if(getBuildingLevelByName("farm") < 0) return -1; //building not yet read
        int maxFarmSpace = BuildingSettings.getMaxFarmSpace(getBuildingLevelByName("farm"));
        
        int buildingPop = 0;
        for(int i = 0; i < BuildingSettings.BUILDING_NAMES.length; i++) {
            logger.trace("Building Farm {} / {} / {}", i, buildings[i], BuildingSettings.getPopUsageById(i, buildings[i]));
            buildingPop += BuildingSettings.getPopUsageById(i, buildings[i]);
        } 
        
        logger.debug("Getting Farm Space {} / {} / {}", village.getCoordAsString(), maxFarmSpace, buildingPop);
        return maxFarmSpace - buildingPop;
    }
}
