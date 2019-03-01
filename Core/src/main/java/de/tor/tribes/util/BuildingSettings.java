/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.util.xml.JDomUtils;
import java.io.File;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author extremeCrazyCoder
 */
public class BuildingSettings {
    private static Logger logger = LogManager.getLogger("ServerSettings");
    
    public static final String[] BUILDING_NAMES = {"main", "barracks", "stable", "garage",
        "church", "watchtower", "snob", "smith", "place", "statue", "market", "wood",
        "stone", "iron", "farm", "storage", "hide", "wall"};

    public static final int[] CHURCH_RANGE = {0, 4, 6, 8};
    public static final double[] WATCHTOWER_RANGE = {0, 1.1, 1.3, 1.5, 1.7, 2.0, 2.3,
            2.6, 3.0, 3.4, 3.9, 4.4, 5.1, 5.8, 6.7, 7.6, 8.7, 10.0, 11.5, 13.1, 15.0};
    
    /**
     * -1 Building not Available
     */
    private static final int[] MAX_LEVEL = new int[BUILDING_NAMES.length];
    private static final int[] MIN_LEVEL = new int[BUILDING_NAMES.length];
    
    private static final int[] BUILD_WOOD = new int[BUILDING_NAMES.length];
    private static final int[] BUILD_STONE = new int[BUILDING_NAMES.length];
    private static final int[] BUILD_IRON = new int[BUILDING_NAMES.length];
    private static final int[] BUILD_POP = new int[BUILDING_NAMES.length];
    private static final int[] BUILD_TIME = new int[BUILDING_NAMES.length];
    private static final double[] BUILD_WOOD_FACTOR = new double[BUILDING_NAMES.length];
    private static final double[] BUILD_STONE_FACTOR = new double[BUILDING_NAMES.length];
    private static final double[] BUILD_IRON_FACTOR = new double[BUILDING_NAMES.length];
    private static final double[] BUILD_POP_FACTOR = new double[BUILDING_NAMES.length];
    private static final double[] BUILD_TIME_FACTOR = new double[BUILDING_NAMES.length];
    
    private static final int FARM_POP = 240;
    private static final double FARM_POP_FACTOR = 1.1721022975335;

    public static final double RESOURCE_PRODUCTION_FACTOR = 1.163118;
    public static final int STORAGE_CAPACITY = 1000;
    public static final double STORAGE_CAPACITY_FACTOR = 1.2294934;
    public static final int HIDE_CAPACITY = 150;
    public static final double HIDE_CAPACITY_FACTOR = 1.3335;
    
    public static boolean loadSettings(String pServerID) {
        Arrays.fill(MAX_LEVEL, -1);
        Arrays.fill(MIN_LEVEL, -1);
        
        Arrays.fill(BUILD_WOOD, -1);
        Arrays.fill(BUILD_STONE, -1);
        Arrays.fill(BUILD_IRON, -1);
        Arrays.fill(BUILD_POP, -1);
        Arrays.fill(BUILD_TIME, -1);
        Arrays.fill(BUILD_WOOD_FACTOR, Double.NaN);
        Arrays.fill(BUILD_STONE_FACTOR, Double.NaN);
        Arrays.fill(BUILD_IRON_FACTOR, Double.NaN);
        Arrays.fill(BUILD_POP_FACTOR, Double.NaN);
        Arrays.fill(BUILD_TIME_FACTOR, Double.NaN);
        
        try {
            logger.debug("Loading server buildings");
            String buildingsPath = Constants.SERVER_DIR + "/" + pServerID + "/buildings.xml";
            
            logger.debug("Parse buildings from '" + buildingsPath + "'");
            Document d = JDomUtils.getDocument(new File(buildingsPath));
            
            for(Element b: d.getRootElement().getChildren()) {
                String name = b.getName();
                int index = ArrayUtils.indexOf(BUILDING_NAMES, name);
                
                if(index < 0) {
                    logger.warn("Found unknown Building {}", name);
                    continue;
                }
                
                try {
                    MAX_LEVEL[index] = Integer.parseInt(b.getChildTextTrim("max_level"));
                    MIN_LEVEL[index] = Integer.parseInt(b.getChildTextTrim("min_level"));

                    BUILD_WOOD[index] = Integer.parseInt(b.getChildTextTrim("wood"));
                    BUILD_STONE[index] = Integer.parseInt(b.getChildTextTrim("stone"));
                    BUILD_IRON[index] = Integer.parseInt(b.getChildTextTrim("iron"));
                    BUILD_POP[index] = Integer.parseInt(b.getChildTextTrim("pop"));
                    BUILD_TIME[index] = Integer.parseInt(b.getChildTextTrim("build_time"));
                    BUILD_WOOD_FACTOR[index] = Double.parseDouble(b.getChildTextTrim("wood_factor"));
                    BUILD_STONE_FACTOR[index] = Double.parseDouble(b.getChildTextTrim("stone_factor"));
                    BUILD_IRON_FACTOR[index] = Double.parseDouble(b.getChildTextTrim("iron_factor"));
                    BUILD_POP_FACTOR[index] = Double.parseDouble(b.getChildTextTrim("pop_factor"));
                    BUILD_TIME_FACTOR[index] = Double.parseDouble(b.getChildTextTrim("build_time_factor"));
                } catch (Exception e) {
                    logger.error("Got an excetion during reading of buildings", e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load buildings", e);
            return false;
        }
        logger.info(Arrays.toString(MAX_LEVEL));
        logger.info(Arrays.toString(MIN_LEVEL));
        logger.info(Arrays.toString(BUILD_WOOD));
        logger.info(Arrays.toString(BUILD_STONE));
        logger.info(Arrays.toString(BUILD_IRON));
        logger.info(Arrays.toString(BUILD_POP));
        logger.info(Arrays.toString(BUILD_TIME));
        logger.info(Arrays.toString(BUILD_WOOD_FACTOR));
        logger.info(Arrays.toString(BUILD_STONE_FACTOR));
        logger.info(Arrays.toString(BUILD_IRON_FACTOR));
        logger.info(Arrays.toString(BUILD_POP_FACTOR));
        logger.info(Arrays.toString(BUILD_TIME_FACTOR));
        logger.debug("Successfully read buildings for server '" + pServerID + "'");
        return true;
    }
    
    /**
     * 
     * @param pBuilding Name of Building
     * @return id of building will be returned
     * returns -2 if the building was not found
     */
    public static int getBuildingIdByName(String pName) {
        int index = ArrayUtils.indexOf(BuildingSettings.BUILDING_NAMES, pName);
        if(index == -1) {
            logger.debug("Invalid building name got {}", pName);
            return -2;
        }
        return index;
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
        return MAX_LEVEL[id];
    }
    
    /**
     * 
     * @param pBuilding Name of Building
     * @return minimum level will be returned
     * returns -1 if a building cannot be built
     * returns -2 if the building was not found
     */
    public static int getMinBuildingLevel(String pBuilding) {
        int id = getBuildingIdByName(pBuilding);
        if(id == -2) {
            logger.info("Building " + pBuilding + " not found");
            return -2;
        }
        return MIN_LEVEL[id];
    }
    
    /**
     * 
     * @param pBuilding Name of Building
     * @param pLevel Level to check
     * @return wether the level is between max and min
     * returns false if building was not found
     */
    public static boolean isBuildingLevelValid(String pBuilding, int pLevel) {
        int id = getBuildingIdByName(pBuilding);
        if(id == -2) {
            return false;
        }
        return isBuildingLevelValid(id, pLevel);
    }
    
    /**
     * 
     * @param pBuildingID ID of Building
     * @param pLevel Level to check
     * @return wether the level is between max and min
     * returns false if building was not found
     */
    public static boolean isBuildingLevelValid(int pBuildingID, int pLevel) {
        if(pLevel > MAX_LEVEL[pBuildingID]) {
            logger.debug("Level too hight {} / {} / {}", pBuildingID, pLevel, MAX_LEVEL[pBuildingID]);
            return false;
        }
        if(pLevel < MIN_LEVEL[pBuildingID]) {
            logger.debug("Level too low {} / {} / {}", pBuildingID, pLevel, MIN_LEVEL[pBuildingID]);
            return false;
        }
        return true;
    }
    
    public static int getMaxFarmSpace(int pLevel) {
        return calculateValue(FARM_POP, FARM_POP_FACTOR, pLevel);
    }
    
    public static int getPopUsageById(int id, int pLevel) {
        return calculateValue(BUILD_POP[id], BUILD_POP_FACTOR[id], pLevel);
    }

    public static double calculateResourcesPerHour(int pBuildingLevel) {
        return ServerSettings.getSingleton().getSpeed() *
                calculateValue(ServerSettings.getSingleton().getResourceConstant(),
                        RESOURCE_PRODUCTION_FACTOR, pBuildingLevel);
    }

    public static int calculateStorageCapacity(int pStorageLevel) {
        return calculateValue(STORAGE_CAPACITY, STORAGE_CAPACITY_FACTOR, pStorageLevel);
    }

    public static int calculateHideCapacity(int pHideLevel) {
        return calculateValue(HIDE_CAPACITY, HIDE_CAPACITY_FACTOR, pHideLevel);
    }
    
    private static int calculateValue(int pBase, double pFactor, int pLevel) {
        if(pLevel <= 0) return 0;
        return (int) Math.round(pBase * Math.pow(pFactor, (pLevel - 1)));
    }
}
