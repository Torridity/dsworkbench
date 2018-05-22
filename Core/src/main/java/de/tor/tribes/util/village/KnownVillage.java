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
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ServerSettings;
import java.awt.Color;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * @author extremeCrazyCoder
 * 
 * Class to hold extra information of a Village like
 *  spy information
 *  watchtower / church Range
 */
public class KnownVillage extends ManageableType {
    
    private static Logger logger = Logger.getLogger("KnownVillage");
    
    private int[] buildings;

    /**
     * -1 Building not Available
     * -2 Special Building depends on World Settings
     */
    private static final int[] buildingMaxLevel = {30, 25, 20, 15, -2, -2, -2,
            20, 1, 1, 30, 30, 30, 30, 30, 30, 10, 20};
    
    private Village village;
    
    private long lastUpdate;

    public static final int[] CHURCH_RANGE = {0, 4, 6, 8};
    public static final double[] WATCHTOWER_RANGE = {0, 1.1, 1.3, 1.5, 1.7, 2.0, 2.3, 2.6, 3.0, 3.4, 3.9, 4.4, 5.1, 5.8, 6.7, 7.6, 8.7, 10.0, 11.5, 13.1, 15.0};
    
    public KnownVillage(Village pVillage) {
        buildings = new int[Constants.buildingNames.length];
        Arrays.fill(buildings, -1);
        this.village = pVillage;
        updateTime();
    }
    
    public KnownVillage(Element e) {
        buildings = new int[Constants.buildingNames.length];
        Arrays.fill(buildings, -1);
        loadFromXml(e);
    }

    @Override
    public String toXml() {
        try {
            StringBuilder xml = new StringBuilder();
            xml.append("<village>");
            xml.append("<id>").append(village.getId()).append("</id>");
            xml.append("<update>").append(lastUpdate).append("</update>");
            
            for(int i = 0; i < Constants.buildingNames.length; i++) {
                xml.append("<").append(Constants.buildingNames[i]).append(">")
                        .append(buildings[i])
                        .append("</").append(Constants.buildingNames[i]).append(">");
            }
            
            xml.append("</village>");
            return xml.toString();
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public String getElementIdentifier() {
        return "knownVillage";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "knownVillages";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }

    @Override
    public void loadFromXml(Element pElement) {
        this.village = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("id").getText()));
        this.lastUpdate = Long.parseLong(pElement.getChild("update").getText());
        
        for(int i = 0; i < Constants.buildingNames.length; i++) {
            this.buildings[i] = Integer.parseInt(pElement.getChild(Constants.buildingNames[i]).getText());
        }
    }

    public Village getVillage() {
        return village;
    }

    public void updateInformation(KnownVillage other) {
        if(lastUpdate > other.getLastUpdate()) {
            //This is newer.... Just get Information that has not been discovered here
            for(int i = 0; i < Constants.buildingNames.length; i++) {
                if(buildings[i] == -1) {
                    buildings[i] = other.getBuildingLevelByName(Constants.buildingNames[i]);
                }
            }
        }
        else {
            //Other is newer ... Copy everything that is valid
            for(int i = 0; i < Constants.buildingNames.length; i++) {
                int level = other.getBuildingLevelByName(Constants.buildingNames[i]);
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
        return CHURCH_RANGE[level];
    }

    /**
     * @return the range
     */
    public double getWatchtowerRange() {
        int level = getBuildingLevelByName("watchtower");
        if(level == -1) return -1;
        return WATCHTOWER_RANGE[level];
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
        if(!ServerSettings.getSingleton().isChurch()) {
            logger.info("Tried to set Watchtower level " + pLevel + "on server without");
            return;
        }
        setBuildingLevelByName("watchtower", pLevel);
        updateTime();
    }

    public void removeWatchtowerInfo() {
        if(!ServerSettings.getSingleton().isChurch()) {
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
        int id = getBuildingIdByName(pBuilding);
        if(id == -2) {
            logger.info("Building " + pBuilding + " not found");
        return -2;
        }

        return buildings[id];
    }

    private void setBuildingLevelByName(String pBuilding, int pLevel) {
        int id = getBuildingIdByName(pBuilding);
        if(id == -2) {
            logger.info("Building " + pBuilding + " not found");
            return;
        }
        
        if(pLevel > getMaxBuildingLevel(pBuilding)) {
            logger.error("Building cannot be constructed that far " + pBuilding + ": " + pLevel);
            return;
        }

        buildings[id] = pLevel;
    }
    
    private void setBuildingLevelById(int pBuildingId, int pLevel) {
        if(pLevel > getMaxBuildingLevel(Constants.buildingNames[pBuildingId])) {
            logger.error("Building cannot be constructed that far " + pBuildingId + ": " + pLevel);
            return;
        }

        buildings[pBuildingId] = pLevel;
    }

    @Override
    public String toString() {
        return village.getFullName();
    }
    
    /**
     * 
     * @param pBuilding Name of Building
     * @return maximum level will be returned
     * returns -1 if a building cannot be built
     * returns -2 if the building was not found
     */
    public static int getMaxBuildingLevel(String pBuilding) {
        int id = getBuildingIdByName(pBuilding);
        if(id == -2) {
            logger.info("Building " + pBuilding + " not found");
            return -2;
        }
        if(buildingMaxLevel[id] != -2) {
            return buildingMaxLevel[id];
        }
        //Buildings that depend on world settings
        switch(pBuilding) {
            case "church":
                if(ServerSettings.getSingleton().isChurch())
                    return 3;
                return -1;
            case "watchtower":
                if(ServerSettings.getSingleton().isWatchtower())
                    return 20;
                return -1;
            case "academy":
                if(ServerSettings.getSingleton().getNobleSystem() ==
                        ServerSettings.NOBLESYSTEM_PACKETS)
                    return 3;
                return 1;
        }
        
        logger.error("came to position in code that should never be reached: "
                + pBuilding);
        return -2;
    }
    
    /**
     * 
     * @param pBuilding Name of Building
     * @return id of building will be returned
     * returns -2 if the building was not found
     */
    public static int getBuildingIdByName(String pName) {
        for(int i = 0; i < Constants.buildingNames.length; i++) {
            if(Constants.buildingNames[i].equals(pName)) {
                return i;
            }   
        }
        return -2;
    }

    void updateInformation(FightReport pReport) {
        for(int i = 0; i < buildings.length; i++) {
            if(pReport.getBuilding(i) != -1) {
                //Building was spyed
                if(getMaxBuildingLevel(Constants.buildingNames[i]) > 0) {
                    //Building can be build
                    setBuildingLevelById(i, pReport.getBuilding(i));
                    updateTime();
                }
            }
        }
    }
}
