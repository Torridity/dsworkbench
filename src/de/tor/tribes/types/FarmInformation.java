/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.*;
import de.tor.tribes.util.bb.TroopListFormatter;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.commons.lang.time.DateUtils;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class FarmInformation extends ManageableType {

    private static double RESOURCE_PRODUCTION_CONTANT = 1.163118;
    private static double STORAGE_CAPACITY_CONTANT = 1.2294934;
    private static double HIDE_CAPACITY_CONTANT = 1.3335;

    @Override
    public String getElementIdentifier() {
        return "farmInfo";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "farmInfos";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }

    @Override
    public String toXml() {
        StringBuilder b = new StringBuilder();
        return b.toString();
    }

    @Override
    public void loadFromXml(Element e) {
    }

    public enum FARM_STATUS {

        OK, IGNORED, NOT_SPYED, TROOPS_FOUND, CONQUERED
    }
    private FARM_STATUS status = FARM_STATUS.NOT_SPYED;
    private int villageId = 0;
    private Village village = null;
    private int ownerId = -1;
    private int attackCount = 0;
    private int woodLevel = 1;
    private int clayLevel = 1;
    private int ironLevel = 1;
    private int storageLevel = 1;
    private int hideLevel = 0;
    private int woodInStorage = 0;
    private int clayInStorage = 0;
    private int ironInStorage = 0;
    private int hauledWood = 0;
    private int hauledClay = 0;
    private int hauledIron = 0;
    private int expectedHaul = 0;
    private int actualHaul = 0;
    private long lastReport = -1;
    private Hashtable<UnitHolder, Integer> farmTroop = null;

    /**
     * Default constructor
     */
    public FarmInformation(Village pVillage) {
        villageId = pVillage.getId();
        ownerId = pVillage.getTribe().getId();
    }

    public Village getVillage() {
        if (village == null) {
            village = DataHolder.getSingleton().getVillagesById().get(villageId);
        }
        return village;
    }

    /**
     * Get the current wood amount in storage
     */
    public int getWoodInStorage() {
        return getWoodInStorage(System.currentTimeMillis());
    }

    /**
     * Get the wood amount in storage at a specific timestamp
     */
    public int getWoodInStorage(long pTimestamp) {
        return (int) (Math.rint(getGeneratedResources(woodInStorage, woodLevel, pTimestamp)));
    }

    /**
     * Get the current clay amount in storage
     */
    public int getClayInStorage() {
        return getClayInStorage(System.currentTimeMillis());
    }

    /**
     * Get the clay amount in storage at a specific timestamp
     */
    public int getClayInStorage(long pTimestamp) {
        return (int) (Math.rint(getGeneratedResources(clayInStorage, clayLevel, pTimestamp)));
    }

    /**
     * Get the current iron amount in storage
     */
    public int getIronInStorage() {
        return getIronInStorage(System.currentTimeMillis());
    }

    /**
     * Get the iron amount in storage at a specific timestamp
     */
    public int getIronInStorage(long pTimestamp) {
        return (int) (Math.rint(getGeneratedResources(ironInStorage, ironLevel, pTimestamp)));
    }

    /**
     * Get all resources in storage
     */
    public int getResourcesInStorage(long pTimestamp) {
        return getWoodInStorage() + getClayInStorage() + getIronInStorage();
    }

    /**
     * Get the amount of resources of a type, generated since the last update
     */
    private double getGeneratedResources(int pResourcesBefore, int pBuildingLevel, long pAtTimestamp) {
        if (getStatus().equals(FARM_STATUS.NOT_SPYED)) {
            return pResourcesBefore;
        }
        long timeSinceLastFarmInfo = pAtTimestamp - lastReport;
        double timeFactor = (double) timeSinceLastFarmInfo / (double) DateUtils.MILLIS_PER_HOUR;
        double generatedResources = pResourcesBefore + 30 * ServerSettings.getSingleton().getSpeed() * Math.pow(RESOURCE_PRODUCTION_CONTANT, (pBuildingLevel - 1));
        generatedResources *= timeFactor;
        generatedResources *= getCorrectionFactor();

        return Math.max(getStorageCapacity(), generatedResources);
    }

    /**
     * Check if the owner of this farm has changed
     */
    public void checkOwner() {
        Village v = getVillage();
        if (v.getTribe().getId() != ownerId) {
            setStatus(FARM_STATUS.CONQUERED);
        }
    }

    /**
     * Get the correction factor depending on overall expected haul and overall actual haul. Correction is started beginning with the fifth
     * attack
     */
    private double getCorrectionFactor() {
        if (attackCount < 5) {//wait a while until "correcting" 
            return 1;
        }
        return (double) actualHaul / (double) expectedHaul;
    }

    /**
     * Update farm info from report
     */
    public void updateFromReport(FightReport pReport) {
        if (pReport == null || pReport.getTimestamp() < lastReport) {
            //old report
            return;
        }
        if (pReport.wasLostEverything()) {
            setStatus(FARM_STATUS.TROOPS_FOUND);
        } else {
            int maxHaul = woodInStorage + clayInStorage + ironInStorage;
            boolean wasSpyed = updateSpyInformation(pReport);
            if (pReport.getHaul() != null) {
                int haulDelta = updateHaulInformation(pReport, maxHaul, wasSpyed);
                if (haulDelta < 0) {
                    //less resources than expected
                    expectedHaul += maxHaul;
                    actualHaul += maxHaul + haulDelta;
                }
                attackCount++;
            }

            //set initial status
            if (getStatus().equals(FARM_STATUS.NOT_SPYED) && wasSpyed) {
                setStatus(FARM_STATUS.OK);
            }
        }
        lastReport = pReport.getTimestamp();
    }

    /**
     * Update spy'ed buildings and resources
     */
    private boolean updateSpyInformation(FightReport pReport) {
        boolean gotInformation = false;
        boolean spyed = false;
        if (pReport.getSpyedResources() != null) {
            woodInStorage = pReport.getSpyedResources()[0];
            clayInStorage = pReport.getSpyedResources()[1];
            ironInStorage = pReport.getSpyedResources()[2];
            spyed = true;
        }

        if (pReport.getStorageLevel() != -1) {
            storageLevel = pReport.getStorageLevel();
        }
        if (pReport.getWoodLevel() != -1) {
            woodLevel = pReport.getWoodLevel();
        }
        if (pReport.getClayLevel() != -1) {
            clayLevel = pReport.getClayLevel();
        }
        if (pReport.getIronLevel() != -1) {
            ironLevel = pReport.getIronLevel();
        }
        if (pReport.getHideLevel() != -1) {
            hideLevel = pReport.getHideLevel();
        }
        if (gotInformation) {
            lastReport = System.currentTimeMillis();
        }
        return spyed;
    }

    /**
     * Read haul information from report, correct storage amounts and return difference to max haul
     */
    private int updateHaulInformation(FightReport pReport, int pMaxHaul, boolean pWasSpyed) {
        hauledWood += pReport.getHaul()[0];
        hauledClay += pReport.getHaul()[1];
        hauledIron += pReport.getHaul()[2];
        int haul = pReport.getHaul()[0] + pReport.getHaul()[1] + pReport.getHaul()[2];
        Hashtable<UnitHolder, Integer> survived = pReport.getSurvivingAttackers();
        Enumeration<UnitHolder> keys = survived.keys();
        int carryCapacity = 0;
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            carryCapacity += (int) (unit.getCarry() * survived.get(unit));
        }

        if (carryCapacity > haul) {
            //storage is empty
            woodInStorage = 0;
            clayInStorage = 0;
            ironInStorage = 0;
        } else {//haul is smaller or equal capacity
            if (!pWasSpyed) {//correct resources limited to 0
                woodInStorage -= pReport.getHaul()[0];
                woodInStorage = (woodInStorage > 0) ? woodInStorage : 0;
                clayInStorage -= pReport.getHaul()[1];
                clayInStorage = (clayInStorage > 0) ? clayInStorage : 0;
                ironInStorage -= pReport.getHaul()[2];
                ironInStorage = (ironInStorage > 0) ? ironInStorage : 0;
            }
        }
        return pMaxHaul - haul;
    }

    public int getStorageCapacity() {
        int storageCapacity = (int) Math.round(1000 * Math.pow(STORAGE_CAPACITY_CONTANT, (storageLevel - 1)));
        int hiddenResources = 0;
        if (hideLevel > 0) {
            hiddenResources = (int) Math.round(150 * Math.pow(HIDE_CAPACITY_CONTANT, hideLevel - 1));
        }
        return storageCapacity - hiddenResources;

    }

    public boolean farmFarm() {
        final int resources = getResourcesInStorage(System.currentTimeMillis());
        Village[] villages = TroopHelper.getOwnVillagesByCarryCapacity(resources);

        if (villages.length == 0) {
            //no village can carry all...get max.
            villages = TroopHelper.getOwnVillagesWithMaxCarryCapacity();
        }

        if (villages.length == 0) {
            //no farm villages available
            return false;
        }

        //sort by speed
        Arrays.sort(villages, new Comparator<Village>() {

            @Override
            public int compare(Village o1, Village o2) {
                double speed1 = TroopHelper.getTroopSpeed(TroopHelper.getTroopsForCarriage(o1, getVillage(), FarmInformation.this));
                double speed2 = TroopHelper.getTroopSpeed(TroopHelper.getTroopsForCarriage(o2, getVillage(), FarmInformation.this));

                return new Double(
                        DSCalculator.calculateMoveTimeInMinutes(o1, getVillage(), speed1)).compareTo(
                        new Double(DSCalculator.calculateMoveTimeInMinutes(o2, getVillage(), speed2)));
            }
        });

        Village selection = villages[0];
        farmTroop = TroopHelper.getTroopsForCarriage(selection, getVillage(), this);
        boolean result = false;
        //@TODO integrate to browser
        //  if (BrowserCommandSender.sendTroops(villages[0], getVillage(), farmTroop)) {
        if (true) {
            TroopHelper.sendTroops(selection, farmTroop);
            result = true;
        } else {
            farmTroop = null;
        }
        return result;
    }

    /**
     * Set the current farm status
     */
    public void setStatus(FARM_STATUS status) {
        this.status = status;
    }

    /**
     * Get the current farm status
     */
    public FARM_STATUS getStatus() {
        return status;
    }

    /**
     * Get the overall hauled wood
     */
    public int getHauledWood() {
        return hauledWood;
    }

    /**
     * Get the overall hauled clay
     */
    public int getHauledClay() {
        return hauledClay;
    }

    /**
     * Get the overall hauled iron
     */
    public int getHauledIron() {
        return hauledIron;
    }

    /**
     * Timestamp of last report with farm relevant information
     */
    public long getLastReport() {
        return lastReport;
    }
}
