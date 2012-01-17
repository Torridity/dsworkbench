/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import com.thoughtworks.xstream.XStream;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.ui.views.DSWorkbenchFarmManager;
import de.tor.tribes.util.*;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.troops.TroopsManager;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class FarmInformation extends ManageableType {

    private static Logger logger = Logger.getLogger("FarmInformation");
    private static double RESOURCE_PRODUCTION_CONTANT = 1.163118;
    private static double STORAGE_CAPACITY_CONTANT = 1.2294934;
    private static double HIDE_CAPACITY_CONTANT = 1.3335;

    public enum FARM_RESULT {

        OK,
        NO_ADEQUATE_SOURCE_BY_NEEDED_TROOPS,
        NO_ADEQUATE_SOURCE_BY_RANGE,
        NO_ADEQUATE_SOURCE_BY_MIN_HAUL,
        NO_TROOPS,
        FAILED_OPEN_BROWSER,
        FARM_INACTIVE
    }

    public enum FARM_STATUS {

        READY,
        FARMING,
        REPORT_EXPECTED,
        NOT_SPYED,
        TROOPS_FOUND,
        CONQUERED
    }

    public enum SPY_LEVEL {

        NONE,
        RESOURCES,
        BUILDINGS
    }
    private FARM_STATUS status = FARM_STATUS.NOT_SPYED;
    private boolean justCreated = false;
    private boolean spyed = false;
    private boolean inactive = false;
    private int villageId = 0;
    private transient Village village = null;
    private transient FARM_RESULT lastResult = FARM_RESULT.OK;
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
    private long farmTroopArrive = -1;
    private int farmSourceId = -1;
    private Hashtable<String, Integer> farmTroop = null;
    private transient StorageStatus storageStatus = null;

    /**
     * Default constructor
     */
    public FarmInformation(Village pVillage) {
        villageId = pVillage.getId();
        ownerId = pVillage.getTribe().getId();
    }

    /**
     * Get the village for this farm
     */
    public Village getVillage() {
        if (village == null) {
            village = DataHolder.getSingleton().getVillagesById().get(villageId);
        }
        return village;
    }

    /**
     * Set a flag that indicates, that this info was just created.
     */
    public void setJustCreated(boolean justCreated) {
        this.justCreated = justCreated;
    }

    /**
     * Returns currently attacking farm troops
     */
    public Hashtable<String, Integer> getFarmTroop() {
        return farmTroop;
    }

    /**
     * Returns the current storage status for table cell rendering
     */
    public StorageStatus getStorageStatus() {
        if (storageStatus == null) {
            storageStatus = new StorageStatus(getWoodInStorage(), getClayInStorage(), getIronInStorage(), getStorageCapacity());
        } else {
            storageStatus.update(getWoodInStorage(), getClayInStorage(), getIronInStorage(), getStorageCapacity());
        }
        return storageStatus;
    }

    /**
     * Used to check if this farm info is new
     */
    public boolean isJustCreated() {
        if (justCreated) {
            justCreated = false;
            return true;
        }
        return false;
    }

    /**
     * Set initial resources
     */
    public void setInitialResources() {
        woodInStorage = 1000;
        clayInStorage = 1000;
        ironInStorage = 1000;
    }

    /**
     * Revalidate the farm information (check owner, check returning/running troops) This method is called after initializing the farm
     * manager and on user request
     */
    public void revalidate() {
        checkOwner();
    }

    /**
     * Get the time when the farm troops reach the farm or return
     */
    public long getRuntimeInformation() {
        if (farmTroopArrive == -1 || farmTroop == null) {
            return -1;
        }
        Hashtable<UnitHolder, Integer> units = TroopHelper.unitTableFromSerializableFormat(farmTroop);
        double speed = TroopHelper.getTroopSpeed(units);
        long arriveTimeRelativeToNow = farmTroopArrive - DSCalculator.calculateMoveTimeInMillis(getVillage(), DataHolder.getSingleton().getVillagesById().get(farmSourceId), speed) - System.currentTimeMillis();
        if (arriveTimeRelativeToNow < 0) {//farm was reached...return time until return
            setStatus(FARM_STATUS.REPORT_EXPECTED);
            arriveTimeRelativeToNow = 0;
            farmTroopArrive = -1;
            farmTroop = null;
        }
        return arriveTimeRelativeToNow;
    }

    /**
     * Returns the last result of farmFarm()
     */
    public FARM_RESULT getLastResult() {
        if (lastResult == null) {//used for lazy loading of XStream input
            lastResult = FARM_RESULT.OK;
        }
        return lastResult;
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
        return getWoodInStorage(pTimestamp) + getClayInStorage(pTimestamp) + getIronInStorage(pTimestamp);
    }

    /**
     * Get the amount of resources of a type, generated since the last update
     */
    private double getGeneratedResources(int pResourcesBefore, int pBuildingLevel, long pAtTimestamp) {
        int usedBuildingLevel = pBuildingLevel;
        if (getStatus().equals(FARM_STATUS.NOT_SPYED)) {
            //return pResourcesBefore;
            usedBuildingLevel = 1;
        }
        long timeSinceLastFarmInfo = pAtTimestamp - lastReport;
        double timeFactor = (double) timeSinceLastFarmInfo / (double) DateUtils.MILLIS_PER_HOUR;
        double resourcesPerHour = 30 * ServerSettings.getSingleton().getSpeed() * Math.pow(RESOURCE_PRODUCTION_CONTANT, (usedBuildingLevel - 1));
        double generatedResources = pResourcesBefore + resourcesPerHour * timeFactor;
        generatedResources *= (DSWorkbenchFarmManager.getSingleton().isConsiderSuccessRate()) ? getCorrectionFactor() : 1.0f;
        return Math.min(getStorageCapacity(), generatedResources);
    }

    /**
     * Check if the owner of this farm has changed
     */
    public void checkOwner() {
        try {
            Village v = getVillage();
            if (v.getTribe().getId() != ownerId || ConquerManager.getSingleton().getConquer(v) != null) {
                setStatus(FARM_STATUS.CONQUERED);
            }
        } catch (ConcurrentModificationException cme) {
            //ignore and keep status
        }
    }

    /**
     * Reset farming troops and status (READY or NOT_SPYED)
     */
    public void resetFarmStatus() {
        farmSourceId = -1;
        farmTroop = null;
        farmTroopArrive = -1;
        if (spyed) {
            setStatus(FARM_STATUS.READY);
        } else {
            setStatus(FARM_STATUS.NOT_SPYED);
        }
    }

    /**
     * Get the correction factor depending on overall expected haul and overall actual haul. Correction is started beginning with the fifth
     * attack
     */
    public float getCorrectionFactor() {
        if (expectedHaul == 0) {
            return 1f;
        }
        return Math.min(1.0f, (float) actualHaul / (float) expectedHaul);
    }

    /**
     * Update farm info from report
     */
    public void updateFromReport(FightReport pReport) {

        if (pReport == null || pReport.getTimestamp() < lastReport) { //old report 
            logger.debug("Skipping farm update from report for "
                    + getVillage() + " due to an old report (" + lastReport + " > " + pReport.getTimestamp() + ")");
            return;
        }

        if (pReport.wasLostEverything()) {
            logger.debug("Changing farm status to due to total loss");
            setStatus(FARM_STATUS.TROOPS_FOUND);
        } else {
            //get the carry capacity of the survived farmers
            Hashtable<UnitHolder, Integer> survived = pReport.getSurvivingAttackers();
            Set<Entry<UnitHolder, Integer>> entries = survived.entrySet();
            int farmTroopsCapacity = 0;
            for (Entry<UnitHolder, Integer> entry : entries) {
                farmTroopsCapacity += (int) (entry.getKey().getCarry() * entry.getValue());
            }

            logger.debug("Surviving capacity is " + farmTroopsCapacity);
            //update spy information
            SPY_LEVEL spyLevel = updateSpyInformation(pReport);
            ///update max haul depending on spy information and the time the troops arrived at the target village
            int maxHaul = farmTroopsCapacity;
            logger.debug("Setting default max haul: " + maxHaul);
            if (spyLevel.equals(SPY_LEVEL.BUILDINGS)) {
                logger.debug("Setting farm status to SPYED");
                spyed = true;
            }

            if (spyed || !spyLevel.equals(SPY_LEVEL.NONE)) {
                //set max haul to expected resources in storage if this information was spyed...otherwise use the carry capabilities of all sent troops
                maxHaul = getWoodInStorage(pReport.getTimestamp()) + getClayInStorage(pReport.getTimestamp()) + getIronInStorage(pReport.getTimestamp());
                logger.debug("Getting max haul from storage capacity: " + maxHaul);
            }

            if (pReport.getHaul() != null) {
                //resources were hauled
                int haulDelta = updateHaulInformation(pReport, maxHaul, !spyLevel.equals(SPY_LEVEL.NONE));
                logger.debug("Haul delta: " + haulDelta);
                float correctionBefore = getCorrectionFactor();
                //expected haul maximum
                expectedHaul += maxHaul;
                //expected haul maximum including "disappeared" resources (negative delta) or "additional" resources (positive delta)
                actualHaul += maxHaul - haulDelta;

                logger.debug("Correction terms: " + expectedHaul + " / " + actualHaul);
                float correctionAfter = getCorrectionFactor();

                logger.debug("Correction factor delta: " + Math.abs(correctionAfter - correctionBefore));

                if (Math.abs(correctionAfter - correctionBefore) > .1) {
                    logger.debug("Correction factor delta larget than 0.1");
                    //limit correction influence by one report to +/- 10 percent
                    actualHaul = (int) Math.rint((correctionBefore + ((correctionAfter < correctionBefore) ? -.1 : .1)) * expectedHaul);
                    logger.debug("New correction factor: " + getCorrectionFactor());
                } else {
                    logger.debug("No correction necessary. New correction factor: " + getCorrectionFactor());
                }
            }

            //reset status

            if (getRuntimeInformation() <= 0) {
                resetFarmStatus();
            } else {
                setStatus(FARM_STATUS.FARMING);
            }
        }
        lastReport = pReport.getTimestamp();
    }

    /**
     * Update spy'ed buildings and resources
     */
    private SPY_LEVEL updateSpyInformation(FightReport pReport) {
        SPY_LEVEL spyLevel = SPY_LEVEL.NONE;
        if (pReport.getSpyedResources() != null) {
            woodInStorage = pReport.getSpyedResources()[0];
            clayInStorage = pReport.getSpyedResources()[1];
            ironInStorage = pReport.getSpyedResources()[2];
            spyLevel = SPY_LEVEL.RESOURCES;
        }

        if (pReport.getStorageLevel() != -1) {
            storageLevel = pReport.getStorageLevel();
            spyLevel = SPY_LEVEL.BUILDINGS;
        }
        if (pReport.getWoodLevel() != -1) {
            woodLevel = pReport.getWoodLevel();
            spyLevel = SPY_LEVEL.BUILDINGS;
        }
        if (pReport.getClayLevel() != -1) {
            clayLevel = pReport.getClayLevel();
            spyLevel = SPY_LEVEL.BUILDINGS;
        }
        if (pReport.getIronLevel() != -1) {
            ironLevel = pReport.getIronLevel();
            spyLevel = SPY_LEVEL.BUILDINGS;
        }
        if (pReport.getHideLevel() != -1) {
            hideLevel = pReport.getHideLevel();
            spyLevel = SPY_LEVEL.BUILDINGS;
        }

        return spyLevel;
    }

    /**
     * Read haul information from report, correct storage amounts and return difference to max haul
     */
    private int updateHaulInformation(FightReport pReport, int pMaxHaul, boolean pWasResourcesSpyed) {
        //get haul and update hauled resources
        int hauledResourcesSum = pReport.getHaul()[0] + pReport.getHaul()[1] + pReport.getHaul()[2];
        hauledWood += pReport.getHaul()[0];
        hauledClay += pReport.getHaul()[1];
        hauledIron += pReport.getHaul()[2];

        if (pMaxHaul > hauledResourcesSum) {
            logger.debug("Hauled resources (" + hauledResourcesSum + ") larger than max haul (" + pMaxHaul + ")");
            //storage is now empty
            woodInStorage = 0;
            clayInStorage = 0;
            ironInStorage = 0;
        } else {//haul is smaller or equal capacity
            if (!pWasResourcesSpyed) {//correct resources limited to 0 if this was not done by spy information
                woodInStorage -= pReport.getHaul()[0];
                woodInStorage = (woodInStorage > 0) ? woodInStorage : 0;
                clayInStorage -= pReport.getHaul()[1];
                clayInStorage = (clayInStorage > 0) ? clayInStorage : 0;
                ironInStorage -= pReport.getHaul()[2];
                ironInStorage = (ironInStorage > 0) ? ironInStorage : 0;
            }
        }

        return pMaxHaul - hauledResourcesSum;
    }

    /**
     * Get the storage capacity of this farm excluding hidden resources
     */
    public int getStorageCapacity() {
        int storageCapacity = (int) Math.round(1000 * Math.pow(STORAGE_CAPACITY_CONTANT, (storageLevel - 1)));
        int hiddenResources = 0;
        if (hideLevel > 0) {
            hiddenResources = (int) Math.round(150 * Math.pow(HIDE_CAPACITY_CONTANT, hideLevel - 1));
        }
        return storageCapacity - hiddenResources;
    }

    /**
     * Farm this farm
     *
     * @param The troops used for farming or 'null' if the needed amount of troops should be calculated
     */
    public FARM_RESULT farmFarm(final Hashtable<UnitHolder, Integer> pFarmUnits) {
        if (inactive) {
            lastResult = FARM_RESULT.FARM_INACTIVE;
            return lastResult;
        }

        if (pFarmUnits == null && !TroopsManager.getSingleton().hasInformation(TroopsManager.TROOP_TYPE.OWN)) {
            logger.info("No own troops imported to DS Workbench");
            lastResult = FARM_RESULT.NO_TROOPS;
            return lastResult;
        }

        Village[] villages;
        boolean cByMinHaul = false;

        if (pFarmUnits == null) {//get villages for farm type C, depending on current resources
            villages = TroopHelper.getOwnVillagesByCarryCapacity(this);

            if (villages.length == 0 && DSWorkbenchFarmManager.getSingleton().allowPartlyFarming()) {
                //no village can carry all...get villages that can carry more than min haul (ordered by capacity and distance) if partly farming is allowed
                villages = TroopHelper.getOwnVillagesByMinHaul(this, DSWorkbenchFarmManager.getSingleton().getMinHaul());
                cByMinHaul = villages.length >= 0;
            }
        } else {//get villages for farm type A or B, depending on static troop count
            villages = TroopHelper.getOwnVillagesByOwnTroops(pFarmUnits);
        }

        if (villages.length == 0) {
            //no farm villages available
            lastResult = FARM_RESULT.NO_ADEQUATE_SOURCE_BY_NEEDED_TROOPS;
            return lastResult;
        }

        if (!cByMinHaul) {//sort valid villages by speed if we are not in the case that we are using farm type C without sufficient troops
            Arrays.sort(villages, new Comparator<Village>() {

                @Override
                public int compare(Village o1, Village o2) {
                    //get speed of defined troops (A and B) or by troops for carriage (C)...
                    //...as this ordering is not performed in case of cByMinHaul, pAllowMaxCarriage is set to 'false'
                    double speed1 = TroopHelper.getTroopSpeed((pFarmUnits == null)
                            ? TroopHelper.getTroopsForCarriage(o1, FarmInformation.this, false)
                            : pFarmUnits);
                    double speed2 = TroopHelper.getTroopSpeed((pFarmUnits == null)
                            ? TroopHelper.getTroopsForCarriage(o2, FarmInformation.this, false)
                            : pFarmUnits);

                    return new Double(
                            DSCalculator.calculateMoveTimeInMinutes(o1, getVillage(), speed1)).compareTo(
                            new Double(DSCalculator.calculateMoveTimeInMinutes(o2, getVillage(), speed2)));
                }
            });
        }

        //now select the "best" village for farming
        Village selection = null;
        Hashtable<UnitHolder, Integer> farmers = null;
        IntRange r = DSWorkbenchFarmManager.getSingleton().getFarmRange();
        boolean allEmpty = true;
        boolean minHaulMetAtLeastOnce = false;
        int minHaul = DSWorkbenchFarmManager.getSingleton().getMinHaul();

        //search feasible village
        for (Village v : villages) {
            //either use defined farm units (A and B) or calculate needed troops (C)...optionally by accepting max. carriage
            Hashtable<UnitHolder, Integer> troops = (pFarmUnits == null) ? TroopHelper.getTroopsForCarriage(v, FarmInformation.this, cByMinHaul) : pFarmUnits;
            double speed = TroopHelper.getTroopSpeed(troops);
            int resources = getResourcesInStorage(System.currentTimeMillis() + DSCalculator.calculateMoveTimeInMillis(v, getVillage(), speed));
            double dist = (int) Math.rint(DSCalculator.calculateMoveTimeInMinutes(v, getVillage(), speed));
            boolean troopsEmpty = troops.isEmpty();
            //troops are empty if they are not met the minimum troop amount
            if (!troopsEmpty) {
                allEmpty = false;
            }
            if (dist > 0 && !troopsEmpty && r.containsInteger(dist)) {
                if (resources < minHaul) {
                    minHaulMetAtLeastOnce = true;
                } else {
                    selection = v;
                    farmers = troops;
                }
            }
        }

        //check if feasible village was found
        if (selection == null || farmers == null) {
            if (!allEmpty) {//not all results were empty, so probably 
                if (!minHaulMetAtLeastOnce) {
                    lastResult = FARM_RESULT.NO_ADEQUATE_SOURCE_BY_RANGE;
                    return lastResult;
                } else {
                    lastResult = FARM_RESULT.NO_ADEQUATE_SOURCE_BY_MIN_HAUL;
                    return lastResult;
                }
            } else {
                lastResult = FARM_RESULT.NO_ADEQUATE_SOURCE_BY_NEEDED_TROOPS;
                return lastResult;
            }
        }

        //send troops and update
        if (BrowserCommandSender.sendTroops(selection, getVillage(), farmers)) {
            TroopHelper.sendTroops(selection, farmers);
            double speed = TroopHelper.getTroopSpeed(farmers);
            farmTroop = TroopHelper.unitTableToSerializableFormat(farmers);
            farmTroopArrive = System.currentTimeMillis() + 2 * DSCalculator.calculateMoveTimeInMillis(selection, getVillage(), speed);
            farmSourceId = selection.getId();
            setStatus(FARM_STATUS.FARMING);
        } else {
            farmTroop = null;
            lastResult = FARM_RESULT.FAILED_OPEN_BROWSER;
            return lastResult;
        }
        attackCount++;
        lastResult = FARM_RESULT.OK;
        return lastResult;
    }

    /**
     * Set the current farm status
     */
    public void setStatus(FARM_STATUS status) {
        logger.debug("Changing farm status for " + getVillage() + " from " + this.status + " to " + status);
        this.status = status;
        switch (this.status) {
            case CONQUERED:
            case TROOPS_FOUND:
                inactive = true;
                break;
        }
    }

    /**
     * Get the current farm status
     */
    public FARM_STATUS getStatus() {
        return status;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getWoodLevel() {
        return woodLevel;
    }

    public int getClayLevel() {
        return clayLevel;
    }

    public int getIronLevel() {
        return ironLevel;
    }

    public int getStorageLevel() {
        return storageLevel;
    }

    public int getHideLevel() {
        return hideLevel;
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

    public boolean isInactive() {
        return inactive;
    }

    public void activateFarm() {
        inactive = false;
        resetFarmStatus();
    }

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
        XStream xstream = new XStream();
        xstream.alias("farmInfo", FarmInformation.class);
        return xstream.toXML(this);
    }

    @Override
    public void loadFromXml(Element e) {
    }

    public static void main(String[] args) {
        FarmInformation info = new FarmInformation(new DummyVillage());
        System.out.println(info.toXml());
    }
}
