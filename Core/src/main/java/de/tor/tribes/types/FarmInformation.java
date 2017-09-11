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
package de.tor.tribes.types;

import com.thoughtworks.xstream.XStream;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchFarmManager;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.TroopHelper;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import de.tor.tribes.util.village.KnownVillage;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author Torridity
 */
public class FarmInformation extends ManageableType {

    private static Logger logger = Logger.getLogger("FarmInformation");

    public enum FARM_RESULT {

        UNKNOWN,
        OK,
        IMPOSSIBLE,
        FAILED,
        FARM_INACTIVE
    }

    public enum FARM_STATUS {

        READY,
        FARMING,
        REPORT_EXPECTED,
        NOT_SPYED,
        TROOPS_FOUND,
        CONQUERED,
        LOCKED
    }
    private FARM_STATUS status = FARM_STATUS.NOT_SPYED;
    private boolean justCreated = false;
    private boolean spyed = false;
    private boolean inactive = false;
    private int villageId = 0;
    private transient Village village = null;
    private transient FARM_RESULT lastResult = FARM_RESULT.UNKNOWN;
    private int ownerId = -1;
    private int attackCount = 0;
    private int woodLevel = 1;
    private int clayLevel = 1;
    private int ironLevel = 1;
    private int storageLevel = 1;
    private int hideLevel = 0;
    private int wallLevel = 0;
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
    private transient long lastRuntimeUpdate = -1;
    private boolean resourcesFoundInLastReport = false;
    private transient String lastSendInformation = null;
    private transient DSWorkbenchFarmManager.FARM_CONFIGURATION usedConfig = null;

    public FarmInformation() {
        //needed for proper XStream integration
    }

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
     * Revalidate the farm information (check owner, check returning/running
     * troops) This method is called after initializing the farm manager and on
     * user request
     */
    public void revalidate() {
        checkOwner();
    }

    /**
     * Get the time when the farm troops reach the farm or return
     */
    public long getRuntimeInformation() {
        lastRuntimeUpdate = System.currentTimeMillis();
        if (farmTroopArrive == -1 || farmTroop == null) {
            return -1;
        }
        // Hashtable<UnitHolder, Integer> units = TroopHelper.unitTableFromSerializableFormat(farmTroop);
        //  double speed = TroopHelper.getTroopSpeed(units);
        long arriveTimeRelativeToNow = farmTroopArrive - System.currentTimeMillis();//farmTroopArrive - DSCalculator.calculateMoveTimeInMillis(getVillage(), DataHolder.getSingleton().getVillagesById().get(farmSourceId), speed) - lastRuntimeUpdate;
        if (arriveTimeRelativeToNow <= 0) {//farm was reached...return time until return
            if (status.equals(FARM_STATUS.FARMING)) {
                setStatus(FARM_STATUS.REPORT_EXPECTED);
            }
            arriveTimeRelativeToNow = 0;
            farmTroopArrive = -1;
            farmSourceId = -1;
            farmTroop = null;
        }
        return arriveTimeRelativeToNow;
    }

    public void refreshRuntime() {
        if (lastRuntimeUpdate < System.currentTimeMillis() - DateUtils.MILLIS_PER_SECOND * 5) {
            getRuntimeInformation();
        }
    }

    public boolean isSpyed() {
        return spyed;
    }

    public void setSpyed(boolean spyed) {
        this.spyed = spyed;
    }

    public void setArrived() {
        farmTroopArrive = System.currentTimeMillis();
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
        return (int) (Math.round(getGeneratedResources(woodInStorage, woodLevel, pTimestamp)));
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
        return (int) (Math.round(getGeneratedResources(clayInStorage, clayLevel, pTimestamp)));
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
        return (int) (Math.round(getGeneratedResources(ironInStorage, ironLevel, pTimestamp)));
    }

    /**
     * Get all resources in storage
     */
    public int getResourcesInStorage(long pTimestamp) {
        return Math.max(0, getWoodInStorage(pTimestamp) + getClayInStorage(pTimestamp) + getIronInStorage(pTimestamp));
    }

    /**
     * Get the amount of resources of a type, generated since the last update
     */
    private double getGeneratedResources(int pResourcesBefore, int pBuildingLevel, long pAtTimestamp) {
        long timeSinceLastFarmInfo = pAtTimestamp - lastReport;
        if (lastReport < 0) {
            //no report read yet...reset time difference
            timeSinceLastFarmInfo = 0;
        }
        double timeFactor = (double) timeSinceLastFarmInfo / (double) DateUtils.MILLIS_PER_HOUR;
        double resourcesPerHour = DSCalculator.calculateResourcesPerHour(pBuildingLevel);
        double generatedResources = pResourcesBefore + resourcesPerHour * timeFactor;
        generatedResources *= (DSWorkbenchFarmManager.getSingleton().isConsiderSuccessRate()) ? getCorrectionFactor() : 1.0f;
        return Math.min(getStorageCapacity(), generatedResources);
    }

    public void guessBuildings() {
        List<FightReport> reports = ReportManager.getSingleton().findAllReportsForTarget(getVillage());
        Collections.sort(reports, new Comparator<FightReport>() {
            @Override
            public int compare(FightReport o1, FightReport o2) {
                return Long.valueOf(o1.getTimestamp()).compareTo(o2.getTimestamp());
            }
        });

        if (!reports.isEmpty()) {//at least one report exists
            if (reports.size() > 1) {
                //resource guess possible...do so!
                Iterator<FightReport> reportIterator = reports.iterator();
                //get first report from iterator
                FightReport report1 = reportIterator.next();
                guessStorage(report1);
                while (reportIterator.hasNext()) {
                    //get second report
                    FightReport report2 = reportIterator.next();
                    //check if haul information is available
                    if (report1.getHaul() != null && report2.getHaul() != null) {//haul information available, perform guess
                        guessResourceBuildings(report1, report2);
                        //guess storage from report2
                        guessStorage(report2);
                    }

                    //set last report to report2 and continue
                    report1 = report2;
                }
            } else {
                //guess only storage with one report
                guessStorage(reports.get(0));
            }
        }
    }

    private void guessResourceBuildings(FightReport pReport1, FightReport pReport2) {
        double dt = (double) (pReport2.getTimestamp() - pReport1.getTimestamp()) / (double) DateUtils.MILLIS_PER_HOUR;

        for (int i = 0; i < 3; i++) {
            //get resources in village at time of arrival
            int resourceInVillage1 = pReport1.getHaul()[i] + ((pReport1.getSpyedResources() != null) ? pReport1.getSpyedResources()[i] : 0);
            int resourceInVillage2 = pReport2.getHaul()[i] + ((pReport2.getSpyedResources() != null) ? pReport2.getSpyedResources()[i] : 0);
            int dResource = resourceInVillage2 - resourceInVillage1;

            int resourceBuildingLevel = DSCalculator.calculateEstimatedResourceBuildingLevel(dResource, dt);
            switch (i) {
                case 0:
                    setWoodLevel(Math.max(woodLevel, resourceBuildingLevel));
                    break;
                case 1:
                    setClayLevel(Math.max(clayLevel, resourceBuildingLevel));
                    break;
                case 2:
                    setIronLevel(Math.max(ironLevel, resourceBuildingLevel));
                    break;
            }
        }
    }

    private void guessResourceBuildings(FightReport pReport) {
        if (pReport == null || pReport.getHaul() == null) {
            //no info
            return;
        }
        //only use if last report is not too old....!! -> send time - 30min !?
        //and if last attack returned empty
        long send = pReport.getTimestamp() - DSCalculator.calculateMoveTimeInMillis(pReport.getSourceVillage(), pReport.getTargetVillage(), TroopHelper.getSlowestUnit(pReport.getAttackers()).getSpeed());

        if (resourcesFoundInLastReport
                || lastReport == -1
                || lastReport < send - 200 * DateUtils.MILLIS_PER_MINUTE
                || lastReport == pReport.getTimestamp()) {
            //ignore this report 
            return;
        }

        int wood = pReport.getHaul()[0];
        int clay = pReport.getHaul()[1];
        int iron = pReport.getHaul()[2];


        double dt = (pReport.getTimestamp() - lastReport) / (double) DateUtils.MILLIS_PER_HOUR;//DSCalculator.calculateMoveTimeInMillis(pReport.getSourceVillage(), pReport.getTargetVillage(), TroopHelper.getSlowestUnit(pReport.getAttackers()).getSpeed()) / (double) DateUtils.MILLIS_PER_HOUR;
        int woodBuildingLevel = DSCalculator.calculateEstimatedResourceBuildingLevel(wood, dt);
        int clayBuildingLevel = DSCalculator.calculateEstimatedResourceBuildingLevel(clay, dt);
        int ironBuildingLevel = DSCalculator.calculateEstimatedResourceBuildingLevel(iron, dt);
        setWoodLevel(Math.max(woodLevel, woodBuildingLevel));
        setClayLevel(Math.max(clayLevel, clayBuildingLevel));
        setIronLevel(Math.max(ironLevel, ironBuildingLevel));
    }

    private void guessStorage(FightReport pReport) {
        if (pReport == null || pReport.getHaul() == null) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            //get resources in village at time of arrival
            double resourceInStorage = (double) pReport.getHaul()[i] + ((pReport.getSpyedResources() != null) ? pReport.getSpyedResources()[i] : 0);
            int guessedStorageLevel = DSCalculator.calculateEstimatedStorageLevel(resourceInStorage);
            switch (i) {
                case 0:
                    setStorageLevel(Math.max(storageLevel, guessedStorageLevel));
                    break;
                case 1:
                    setStorageLevel(Math.max(storageLevel, guessedStorageLevel));
                    break;
                case 2:
                    setStorageLevel(Math.max(storageLevel, guessedStorageLevel));
                    break;
            }
        }
    }

    /**
     * Check if the owner of this farm has changed
     */
    public void checkOwner() {
        try {
            Village v = getVillage();
            if (v == null) {
                return;
            }
            Conquer conquer = ConquerManager.getSingleton().getConquer(v);
            if (v.getTribe().getId() != ownerId || conquer != null) {
                if (conquer != null) {
                    ownerId = conquer.getWinner().getId();
                } else {
                    ownerId = v.getTribe().getId();
                }

                if (ownerId != 0 && !v.getTribe().equals(Barbarians.getSingleton())) {
                    //village was really conquered
                    setStatus(FARM_STATUS.CONQUERED);
                }
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
        lastResult = FARM_RESULT.UNKNOWN;
        lastSendInformation = null;
        if (!inactive) {
            if (spyed) {
                setStatus(FARM_STATUS.READY);
            } else {
                setStatus(FARM_STATUS.NOT_SPYED);
                //now it is time to check updates in building levels
                logger.debug("Checking building updates");
                guessBuildings();
            }
        }
    }

    /**
     * Get the correction factor depending on overall expected haul and overall
     * actual haul. Correction is started beginning with the fifth attack
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
            logger.debug("Skipping farm update from report for " + getVillage() + " as it is an old report (" + lastReport + " > " + pReport.getTimestamp() + ")");
            return;
        }

        if (pReport.wasLostEverything() || pReport.hasSurvivedDefenders()) {
            logger.debug("Changing farm status to due to total loss or found troops");
            setStatus(FARM_STATUS.TROOPS_FOUND);
        } else {

            //at first, update correction factor as spy information update might modifiy farm levels and expected resource calcuclation
            updateCorrectionFactor(pReport);
            //update spy information
            updateSpyInformation(pReport);
            if (!spyed) {
                guessResourceBuildings(pReport);
            }
            //update haul information (hauled resources sums, storage status if no spy information is available)
            updateHaulInformation(pReport);

            //reset status if this was an arrival report
            if (getRuntimeInformation() <= 0) {
                resetFarmStatus();
            } else {
                //set to farming status...probably we've just loaded this report or another report for the same farm was entered
                setStatus(FARM_STATUS.FARMING);
            }
        }
        lastReport = pReport.getTimestamp();
        lastResult = FARM_RESULT.UNKNOWN;
        lastSendInformation = null;
    }

    /**
     * Update spy'ed buildings and resources
     */
    private void updateSpyInformation(FightReport pReport) {
        if (pReport.getSpyLevel() >= pReport.SPY_LEVEL_RESOURCES) {
            int remaining = pReport.getSpyedResources()[0] + pReport.getSpyedResources()[1] + pReport.getSpyedResources()[2];
            if(remaining < 4) remaining = 0; //Fix for a Bug of DS Where there are Resources displayed in Spy but not hauled
            resourcesFoundInLastReport = remaining > DSWorkbenchFarmManager.getSingleton().getMinHaul(usedConfig);
        }
        
        if(pReport.getSpyLevel() >= pReport.SPY_LEVEL_BUILDINGS) {
            storageLevel = pReport.getBuilding(KnownVillage.getBuildingIdByName("storage"));
            woodLevel = pReport.getBuilding(KnownVillage.getBuildingIdByName("timber"));
            clayLevel = pReport.getBuilding(KnownVillage.getBuildingIdByName("clay"));
            ironLevel = pReport.getBuilding(KnownVillage.getBuildingIdByName("iron"));
            hideLevel = pReport.getBuilding(KnownVillage.getBuildingIdByName("hide"));
            wallLevel = pReport.getBuilding(KnownVillage.getBuildingIdByName("wall"));
        }
        else if (pReport.getWallAfter() != -1) {
            //set wall destruction (works also without spying)
            wallLevel = pReport.getWallAfter();
        }
    }

    /**
     * Read haul information from report, correct storage amounts and return
     * difference to max haul
     */
    private void updateHaulInformation(FightReport pReport) {
        if (pReport.getHaul() == null) {
            return;
        }
        //get haul and update hauled resources
        hauledWood += pReport.getHaul()[0];
        hauledClay += pReport.getHaul()[1];
        hauledIron += pReport.getHaul()[2];

        Hashtable<UnitHolder, Integer> survived = pReport.getSurvivingAttackers();
        Set<Entry<UnitHolder, Integer>> entries = survived.entrySet();
        int farmTroopsCapacity = 0;
        for (Entry<UnitHolder, Integer> entry : entries) {
            farmTroopsCapacity += (int) (entry.getKey().getCarry() * entry.getValue());
        }

        int hauledResourcesSum = pReport.getHaul()[0] + pReport.getHaul()[1] + pReport.getHaul()[2];
        if (pReport.getSpyedResources() == null) {
            //if no resource spy information were available, correct them by ourselves
            if (farmTroopsCapacity > hauledResourcesSum) {
                //storage is now empty
                woodInStorage = 0;
                clayInStorage = 0;
                ironInStorage = 0;
                
                //there are no additional resources
                resourcesFoundInLastReport = false;
            } else if (farmTroopsCapacity == hauledResourcesSum) {
                //capacity is equal hauled resources (smaller actually cannot be)
                woodInStorage = getWoodInStorage(pReport.getTimestamp()) - pReport.getHaul()[0];
                woodInStorage = (woodInStorage > 0) ? woodInStorage : 0;
                clayInStorage = getClayInStorage(pReport.getTimestamp()) - pReport.getHaul()[1];
                clayInStorage = (clayInStorage > 0) ? clayInStorage : 0;
                ironInStorage = getIronInStorage(pReport.getTimestamp()) - pReport.getHaul()[2];
                ironInStorage = (ironInStorage > 0) ? ironInStorage : 0;
                
                //there are additional resources
                resourcesFoundInLastReport = true;
            } else {
                //Please what!? Let's ignore this and never talk about it again.
            }
        }
    }

    /**
     * Update this farm's correction factor by calculating the expected haul
     * (estimated storage status) and the actual haul (sum of haul and remaining
     * resources). This call will do nothing if no spy information is available
     * or if no haul information is available. The correction factor delta is
     * limited to +/- 10 percent to reduce the influence of A and B runs and for
     * farms which are relatively new.
     */
    private void updateCorrectionFactor(FightReport pReport) {
        if (pReport.getHaul() != null && pReport.getSpyedResources() != null) {
            logger.debug("Updating correction factor");
            int haulSum = pReport.getHaul()[0] + pReport.getHaul()[1] + pReport.getHaul()[2];
            int storageSum = pReport.getSpyedResources()[0] + pReport.getSpyedResources()[1] + pReport.getSpyedResources()[2];
            int expected = getResourcesInStorage(pReport.getTimestamp());
            //resources were hauled
            logger.debug(" - Resources in farm: " + (haulSum + storageSum));

            float correctionBefore = getCorrectionFactor();
            logger.debug(" - Correction factor before: " + correctionBefore);
            //add resources expected at report's timestamp to expected haul 
            expectedHaul += expected;
            //actual haul contains only resources we've obtained
            actualHaul += haulSum + storageSum;

            float correctionAfter = getCorrectionFactor();
            logger.debug(" - Correction factor after: " + correctionAfter);
            logger.debug(" - Correction factor delta: " + Math.abs(correctionAfter - correctionBefore));

            if (Math.abs(correctionAfter - correctionBefore) > .1) {
                logger.debug(" - Correction factor delta larger than 0.1");
                //limit correction influence by one report to +/- 10 percent
                actualHaul = (int) Math.rint((correctionBefore + ((correctionAfter < correctionBefore) ? -.1 : .1)) * expectedHaul);
                logger.debug(" - New correction factor: " + getCorrectionFactor());
            } else {
                logger.debug(" - No correction necessary. New correction factor: " + getCorrectionFactor());
            }
        } else {
            logger.debug("Skipping correction factor update due to missing spy/farm information");
        }
    }

    /**
     * Get the storage capacity of this farm excluding hidden resources
     */
    public int getStorageCapacity() {
        int storageCapacity = DSCalculator.calculateMaxResourcesInStorage(storageLevel);
        int hiddenResources = 0;
        if (hideLevel > 0) {
            hiddenResources = DSCalculator.calculateMaxHiddenResources(hideLevel);
        }
        //limit capacity to 0
        return Math.max(0, storageCapacity - hiddenResources);
    }

    /**
     * Farm this farm
     *
     * @param pConfig The troops used for farming or 'null' if the needed amount of
     * troops should be calculated
     */
    public FARM_RESULT farmFarm(DSWorkbenchFarmManager.FARM_CONFIGURATION pConfig) {
        StringBuilder info = new StringBuilder();
        if (inactive) {
            lastResult = FARM_RESULT.FARM_INACTIVE;
            info.append("Farm ist inaktiv. Aktiviere die Farm, um sie wieder nutzen zu können.\n");
        } else {//farm is active
            if (!TroopsManager.getSingleton().hasInformation(TroopsManager.TROOP_TYPE.OWN)) {
                //we need troop information to continue....
                logger.info("No own troops imported to DS Workbench");
                lastResult = FARM_RESULT.FAILED;
                info.append("Keine Truppeninformationen aus dem Spiel nach DS Workbench importiert.\n"
                        + "Wechsel in die Truppenübersicht im Spiel, kopiere die Seite per STRG+A und kopiere sie\n"
                        + "per STRG+C in die Zwischenablage, von wo DS Workbench sie dann automatisch einlesen wird.\n");
            } else {
                //////////////troops are imported///////////////
                /////////////////start farming//////////////////
                Hashtable<Village, VillageTroopsHolder> unitsAndVillages;
                boolean pFarmByMinHaul = false;
                if (pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.C)) {//get villages for farm type C, depending on current resources
                    unitsAndVillages = TroopHelper.getOwnTroopsForAllVillagesByCapacity(this);
                    if (unitsAndVillages.isEmpty() && DSWorkbenchFarmManager.getSingleton().allowPartlyFarming()) {
                        //no village can carry all...get villages that can carry more than min haul (ordered by capacity and distance) if partly farming is allowed
                        unitsAndVillages = TroopHelper.getOwnTroopsForAllVillagesByMinHaul(DSWorkbenchFarmManager.getSingleton().getMinHaul(pConfig));
                        pFarmByMinHaul = !unitsAndVillages.isEmpty();
                    }
                } else {//get villages for farm type A or B, depending on static troop count
                    unitsAndVillages = TroopHelper.getOwnTroopsForAllVillages(DSWorkbenchFarmManager.getSingleton().getTroops(pConfig));
                }


                //have possible villages...or not                
                if (unitsAndVillages.isEmpty()) {
                    //no farm villages available
                    lastResult = FARM_RESULT.IMPOSSIBLE;
                    info.append("Keine verwendbaren Dörfer gefunden.\n"
                            + "Dies kann vorkommen, wenn in keinem Dorf genügend (A/B) bzw. die minimale Anzahl Truppen (C) vorhanden sind (abzügliche Truppenreserve),\n"
                            + "oder wenn nicht genügend Truppen vorhanden sind, um die minimale Beute zu tragen (nur C).\n");
                } else {
                    info.append(unitsAndVillages.size()).append(" Dorf/Dörfer mit freien Truppen gefunden.\n");
                    //villages with enough troops found
                    final HashMap<Village, Hashtable<UnitHolder, Integer>> carriageMap = new HashMap<>();
                    Enumeration<Village> villageKeys = unitsAndVillages.keys();
                    List<Village> villages = new LinkedList<>();
                    while (villageKeys.hasMoreElements()) {
                        Village selectedVillage = villageKeys.nextElement();
                        Hashtable<UnitHolder, Integer> units;
                        if (pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.C)) {
                            //calculate needed units
                            units = TroopHelper.getTroopsForCarriage(pConfig, unitsAndVillages.get(selectedVillage), this);
                        } else {//use provided units for A/B-Scenario
                            units = new Hashtable<>();
                            Hashtable<UnitHolder, Integer> configTroops = DSWorkbenchFarmManager.getSingleton().getTroops(pConfig);
                            Enumeration<UnitHolder> unitKeys = configTroops.keys();
                            while (unitKeys.hasMoreElements()) {
                                UnitHolder unitKey = unitKeys.nextElement();
                                VillageTroopsHolder holder = unitsAndVillages.get(selectedVillage);
                                int amount = configTroops.get(unitKey);
                                if (holder.getAmountForUnit(unitKey) - DSWorkbenchFarmManager.getSingleton().getBackupUnits(unitKey) >= amount) {
                                    units.put(unitKey, amount);
                                }
                            }
                        }
                        if (units != null && !units.isEmpty()) {
                            //units from this village can carry all resources
                            carriageMap.put(selectedVillage, units);
                            villages.add(selectedVillage);
                        }
                    }

                    //have village with valid amounts
                    if (villages.isEmpty()) {
                        info.append("Es wurden alle Dörfer aufgrund der Tragekapazität ihrer Truppen, ihrer Entfernung zum Ziel oder der erwarteten Ressourcen gelöscht.\n"
                                + "Möglicherweise könnte ein erneuter Truppenimport aus dem Spiel, eine Vergrößerung des Farmradius oder eine Verkleinerung der minimalen Anzahl an Einheiten\n"
                                + "hilfreich sein. Überprüfe auch die eingestellte Truppenreserve (R), falls vorhanden.\n");
                        lastResult = FARM_RESULT.IMPOSSIBLE;
                    } else {
                        info.append(villages.size()).append(" Dorf/Dörfer verfügen über die benötigte Tragekapazität.\n");
                        //there are villages which can carry all ressources or we use scenario A/B
                        if (!pFarmByMinHaul) {//sort valid villages by speed if we are not in the case that we are using farm type C without sufficient troops
                            Collections.sort(villages, new Comparator<Village>() {
                                @Override
                                public int compare(Village o1, Village o2) {
                                    //get speed of defined troops (A and B) or by troops for carriage (C)...
                                    //...as this ordering is not performed in case of cByMinHaul, pAllowMaxCarriage is set to 'false'
                                    double speed1 = TroopHelper.getTroopSpeed(carriageMap.get(o1));
                                    double speed2 = TroopHelper.getTroopSpeed(carriageMap.get(o2));

                                    return new Double(DSCalculator.calculateMoveTimeInMinutes(o1, getVillage(), speed1)).compareTo(
                                            DSCalculator.calculateMoveTimeInMinutes(o2, getVillage(), speed2));
                                }
                            });
                        }

                        //now select the "best" village for farming
                        Village selection = null;
                        Hashtable<UnitHolder, Integer> farmers = null;
                        IntRange r = DSWorkbenchFarmManager.getSingleton().getFarmRange(pConfig);
                        int noTroops = 0;
                        int distCheckFailed = 0;
                        int minHaulCheckFailed = 0;
                        double minDist = 0;
                        int minHaul = DSWorkbenchFarmManager.getSingleton().getMinHaul(pConfig);
                        //search feasible village
                        for (Village v : villages) {
                            //take troops from carriageMap
                            Hashtable<UnitHolder, Integer> troops = carriageMap.get(v);

                            double speed = TroopHelper.getTroopSpeed(troops);
                            int resources = getResourcesInStorage(System.currentTimeMillis() + DSCalculator.calculateMoveTimeInMillis(v, getVillage(), speed));
                            double dist = DSCalculator.calculateMoveTimeInMinutes(v, getVillage(), speed);
                            //troops are empty if they are not met the minimum troop amount
                            if (troops.isEmpty()
                                    || TroopHelper.getPopulation(troops) == 0
                                    || (pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.C) && TroopHelper.getCapacity(troops) == 0)) {
                                noTroops++;
                            } else {//enough troops
                                if (dist > 0 && r.containsDouble(dist)) {
                                    if (resources < minHaul) {
                                        minHaulCheckFailed++;
                                    } else {
                                        //village and troops found...use them
                                        selection = v;
                                        farmers = troops;
                                        break;
                                    }
                                } else {
                                    distCheckFailed++;
                                    if (dist > 0) {
                                        if (minDist == 0) {
                                            minDist = dist;
                                        } else {
                                            minDist = Math.min(dist, minDist);
                                        }
                                    }
                                }
                            }
                        }

                        //check if feasible village was found
                        if (selection == null || farmers == null) {
                            lastResult = FARM_RESULT.IMPOSSIBLE;
                            info.append("In der abschließenden Prüfung wurden alle Dörfer entfernt.\nDie Gründe waren die Folgenden:\n- ").
                                    append(noTroops).append(" Dorf/Dörfer hatten nicht ausreichend Truppen für die erwarteten Rohstoffe\n- ").
                                    append(distCheckFailed).append(" Dorf/Dörfer lagen außerhalb des eingestellten Farmradius (Min. Laufzeit: ").
                                    append((int) Math.rint(minDist)).append(" Minuten)\n- ").
                                    append(minHaulCheckFailed).append(" Dorf/Dörfer würden nicht genügend Rohstoffe vorfinden, um die minimale Beute zu erzielen");
                        } else {
                            //send troops and update
                            if (BrowserCommandSender.sendTroops(selection, getVillage(), farmers)) {
                                //  if (true) {
                                TroopHelper.sendTroops(selection, farmers);
                                double speed = TroopHelper.getTroopSpeed(farmers);
                                farmTroop = TroopHelper.unitTableToSerializableFormat(farmers);
                                farmTroopArrive = System.currentTimeMillis() + DSCalculator.calculateMoveTimeInMillis(selection, getVillage(), speed);
                                farmSourceId = selection.getId();
                                setStatus(FARM_STATUS.FARMING);
                                attackCount++;
                                lastResult = FARM_RESULT.OK;
                                info.append("Der Farmangriff konnte erfolgreich abgeschickt werden.");
                            } else {
                                farmTroop = null;
                                farmTroopArrive = -1;
                                farmSourceId = -1;
                                lastResult = FARM_RESULT.FAILED;
                                info.append("Der Farmangriff konnte nicht im Browser geöffnet werden.\n"
                                        + "Bitte überprüfe die Browsereinstellungen von DS Workbench.");
                            }
                        }
                    }
                }
            }
        }
        usedConfig = pConfig;
        lastSendInformation = info.toString();
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
            case LOCKED:
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

    public String getLastSendInformation() {
        return lastSendInformation;
    }

    public boolean isResourcesFoundInLastReport() {
        return resourcesFoundInLastReport;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getWoodLevel() {
        return woodLevel;
    }

    public void setWoodLevel(int woodLevel) {
        if (woodLevel >= 1 && woodLevel <= 30) {
            this.woodLevel = woodLevel;
        }
    }

    public int getClayLevel() {
        return clayLevel;
    }

    public void setClayLevel(int clayLevel) {
        if (clayLevel >= 1 && clayLevel <= 30) {
            this.clayLevel = clayLevel;
        }
    }

    public int getIronLevel() {
        return ironLevel;
    }

    public void setIronLevel(int ironLevel) {
        if (ironLevel >= 1 && ironLevel <= 30) {
            this.ironLevel = ironLevel;
        }
    }

    public int getStorageLevel() {
        return storageLevel;
    }

    public void setStorageLevel(int storageLevel) {
        if (storageLevel >= 1 && storageLevel <= 30) {
            this.storageLevel = storageLevel;
        }
    }

    public int getHideLevel() {
        return hideLevel;
    }

    public void setHideLevel(int hideLevel) {
        if (hideLevel >= 1 && hideLevel <= 10) {
            this.hideLevel = hideLevel;
        }
    }

    public void setWallLevel(int wallLevel) {
        this.wallLevel = wallLevel;
    }

    public int getWallLevel() {
        return wallLevel;
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

    public void setLastReport(long lastReport) {
        this.lastReport = lastReport;
    }

    public boolean isInactive() {
        return inactive;
    }

    public void activateFarm() {
        inactive = false;
        resetFarmStatus();
    }

    public void deactivateFarm() {
        inactive = true;
        setStatus(FARM_STATUS.LOCKED);
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
}
