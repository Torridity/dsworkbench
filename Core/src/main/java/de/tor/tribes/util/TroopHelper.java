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
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountDynamic;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchFarmManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import org.apache.log4j.Logger;

import java.util.*;

/**
 *
 * @author Torridity
 */
public class TroopHelper {

    private final static Logger logger = Logger.getLogger("TroopHelper");
    private final static int[] ramsNeeded = new int[]{0, 2, 4, 7, 10, 14, 19, 24, 30, 37, 46, 55, 65, 77, 91, 106, 124, 143, 166, 191, 219};

    public static TroopAmountFixed getTroopsForCarriage(DSWorkbenchFarmManager.FARM_CONFIGURATION pConfig, VillageTroopsHolder pTroops, FarmInformation pInfo) {
        TroopAmountFixed units = new TroopAmountFixed();
        Village source = pTroops.getVillage();
        TroopAmountFixed backupUnits = DSWorkbenchFarmManager.getSingleton().getBackupUnits(source);
        TroopAmountFixed minUnits = DSWorkbenchFarmManager.getSingleton().getMinUnits(pConfig, source);
        
        UnitHolder[] allowed = DSWorkbenchFarmManager.getSingleton().getAllowedFarmUnits(pConfig, source);
        Arrays.sort(allowed, UnitHolder.RUNTIME_COMPARATOR);
        boolean minUnitsMetOnce = false;
        if (logger.isDebugEnabled()) {
            logger.debug("Getting farm units from " + source);
        }

        if (DSWorkbenchFarmManager.getSingleton().isUseRams(pConfig)) {
            if (pInfo.getWallLevel() > 0) {
                UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
                int rams = pTroops.getTroops().getAmountForUnit(ram) - backupUnits.getAmountForUnit(ram);
                if (rams > 1) {
                    int needed = ramsNeeded[pInfo.getWallLevel()];
                    int using = Math.min(needed, rams);
                    units.setAmountForUnit(ram, using);
                }
            }
        }

        for (UnitHolder unit : allowed) {
            int amount = pTroops.getTroops().getAmountForUnit(unit) - backupUnits.getAmountForUnit(unit);
            if (amount > 0) {
                //get current unit speed
                double speed = unit.getSpeed();
                int currentCarryCapacity = units.getFarmCapacity();
                //correct speed by used units (not necessary as they are sorted by runtime!? ... but won't hurt anyway)
                speed = Math.max(speed, units.getSpeed());
                
                if (logger.isDebugEnabled()) {
                    logger.debug(" - Current max. speed: " + speed);
                    logger.debug(" - Current capacity: " + currentCarryCapacity);
                }
                
                //get resources for current max speed excluding carry capacity
                int resources = pInfo.getResourcesInStorage(System.currentTimeMillis() + DSCalculator.calculateMoveTimeInMillis(source, pInfo.getVillage(), speed));
                resources -= currentCarryCapacity;
                if (logger.isDebugEnabled()) {
                    logger.debug(" - Remaining resources: " + resources);
                }
                //get needed amount of units to carry remaining resources
                int neededAmountOfUnit = (int) Math.ceil((double) resources / unit.getCarry());
                logger.debug(" - Needing " + neededAmountOfUnit + " units of type " + unit);
                if (neededAmountOfUnit <= amount
                        && (minUnitsMetOnce || neededAmountOfUnit > minUnits.getAmountForUnit(unit))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding " + neededAmountOfUnit + " units of type " + unit);
                    }
                    //unit can carry all and more units than min are needed
                    units.setAmountForUnit(unit, neededAmountOfUnit);
                    resources -= unit.getCarry() * neededAmountOfUnit;
                    minUnitsMetOnce = true;
                } else if (neededAmountOfUnit > amount && (minUnitsMetOnce || amount > minUnits.getAmountForUnit(unit))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding all units (" + amount + ") of type " + unit);
                    }
                    //unit can not carry all and but more units than min are available
                    units.setAmountForUnit(unit, amount);
                    resources -= unit.getCarry() * amount;
                    minUnitsMetOnce = true;
                }//otherwise don't use unit

                if (resources <= 0) {
                    logger.debug("Got carriage for all resources");
                    //farm will be empty
                    break;
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No units of type " + unit + " in village or amount is smaller than backup");
                }
            }
        }

        UnitHolder spy = DataHolder.getSingleton().getUnitByPlainName("spy");
        Integer neededSpies = minUnits.getAmountForUnit(spy);
        int availableSpies = pTroops.getTroops().getAmountForUnit(spy) - backupUnits.getAmountForUnit(spy);
        units.setAmountForUnit(spy, (neededSpies > availableSpies) ? availableSpies : neededSpies);

        //check result
        double speed = units.getSpeed();
        int carryCapacity = units.getFarmCapacity();
        int resources = pInfo.getResourcesInStorage(System.currentTimeMillis() + DSCalculator.calculateMoveTimeInMillis(source, pInfo.getVillage(), speed));
        if (resources < carryCapacity && pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.C)) {
            if (!DSWorkbenchFarmManager.getSingleton().allowPartlyFarming()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to get enough units to carry '" + resources + "' resources. Max. carriage was '" + carryCapacity + "'");
                }
                //not enough units found
                units.fill(0);
            }
        }
        return units;
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillages(TroopAmountDynamic pMinAmounts) {
        Hashtable<Village, VillageTroopsHolder> result = new Hashtable<>();
        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null && hasMinTroopAmounts(holder, pMinAmounts)) {
                result.put(holder.getVillage(), holder);
            }
        }
        return result;
    }

    public static boolean hasMinTroopAmounts(VillageTroopsHolder pHolder, TroopAmountDynamic pMinAmounts) {
        if (pMinAmounts == null) {
            return true;
        }

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            //check for all units amount and backup
            int amount = pHolder.getTroops().getAmountForUnit(unit);
            if (amount < pMinAmounts.getAmountForUnit(unit, pHolder.getVillage())
                    || amount < DSWorkbenchFarmManager.getSingleton()
                    .getBackupUnits(pHolder.getVillage()).getAmountForUnit(unit)) {
                //no troops of type or not enough units or backup met
                return false;
            }
        }
        return true;
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillagesByCapacity(FarmInformation pInfo) {
        int currentResources = pInfo.getResourcesInStorage(System.currentTimeMillis());

        return getOwnTroopsForAllVillagesFilteredBy(currentResources, Integer.MAX_VALUE);
    }

    private static Hashtable<Village,VillageTroopsHolder> getOwnTroopsForAllVillagesFilteredBy(int minResourceThreshold, int maxResourceThreshold) {
        return getOwnTroopsForVillagesFilteredBy(GlobalOptions.getSelectedProfile().getTribe().getVillageList(), minResourceThreshold, maxResourceThreshold);
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForVillagesFilteredBy(Village[] villages, int minResourceThreshold, int maxResourceThreshold) {
        Hashtable<Village, VillageTroopsHolder> result = new Hashtable<>();

        for (Village v : villages) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null) {
                if (holder.getTroops().getFarmCapacity() >= minResourceThreshold && holder.getTroops().getFarmCapacity() <= maxResourceThreshold) {
                    //village is valid
                    result.put(holder.getVillage(), holder);
                }
            }
        }

        return result;
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillagesByMinHaul(int pMinHaul) {
        return getOwnTroopsForAllVillagesFilteredBy(pMinHaul, Integer.MAX_VALUE);
    }

    public static List<Village> fillSourcesWithAttacksForUnit(Village source, Hashtable<UnitHolder, List<Village>> villagesForUnitHolder, List<Village> existingSources, UnitHolder unitHolder) {
        List<Village> sourcesForUnit = existingSources != null ? existingSources : villagesForUnitHolder.get(unitHolder);
        if (sourcesForUnit == null) {
            sourcesForUnit = new LinkedList<>();
            sourcesForUnit.add(source);
            villagesForUnitHolder.put(unitHolder, sourcesForUnit);
        } else {
            sourcesForUnit.add(source);
        }

        return sourcesForUnit;
    }

    public static void sendTroops(Village pVillage, TroopAmountFixed pTroops) {
        VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
        VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE);
        VillageTroopsHolder onTheWay = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.ON_THE_WAY);
        
        if (own != null) {//check for case that no troops are available at all
            own.getTroops().removeAmount(pTroops);
        }
        if (inVillage != null) {//check for case that troops are from place
            inVillage.getTroops().removeAmount(pTroops);
        }
        if (onTheWay != null) {//check for case that troops are from place
            onTheWay.getTroops().addAmount(pTroops);
        }
    }

    public static VillageTroopsHolder getRandomOffVillageTroops(Village pVillage) {
        TroopAmountFixed units = new TroopAmountFixed(0);
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (unit.isOffense()) {
                units.setAmountForUnit(unit, (int) Math.rint(Math.random() * 7000.0 / unit.getPop()));
            }
        }
        VillageTroopsHolder holder = new VillageTroopsHolder(pVillage, new Date(System.currentTimeMillis()));
        holder.setTroops(units);
        return holder;
    }

    public static int getAttackForce(Village pVillage, UnitHolder pSlowestUnit) {
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
        if (holder == null) {
            return 0;
        }

        TroopAmountFixed troops = holder.getTroops();

        int force = 0;
        for (UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            int value = troops.getAmountForUnit(unit);
            if(value > 0 && unit.getSpeed() <= pSlowestUnit.getSpeed()) {
                force += unit.getAttack() * value;
            }
        }
        return force;
    }

    public static int getNeededSupports(Village pVillage, TroopAmountFixed pTargetAmount, TroopAmountFixed pSplitAmount, boolean pAllowSimilar) {
        boolean useArcher = !DataHolder.getSingleton().getUnitByPlainName("archer").equals(UnknownUnit.getSingleton());
        
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE);
        TroopAmountFixed troops;
        if (holder == null) {
            troops = new TroopAmountFixed(0);
        } else {
            troops = holder.getTroops();
        }

        if (pAllowSimilar) {
            int defSplit = pSplitAmount.getDefValue();
            int defCavSplit = pSplitAmount.getDefCavalryValue();
            int defArchSplit = pSplitAmount.getDefArcherValue();
            
            int defDiff = pTargetAmount.getDefValue() - troops.getDefValue();
            int defCavDiff = pTargetAmount.getDefCavalryValue() - troops.getDefCavalryValue();
            int defArchDiff = pTargetAmount.getDefArcherValue() - troops.getDefArcherValue();
            
            int defSupport = (defDiff == 0) ? 0 : (int) (Math.ceil((double) defDiff / (double) defSplit));
            int defCavSupport = (defCavDiff == 0) ? 0 : (int) (Math.ceil((double) defCavDiff / (double) defCavSplit));
            int defArchSupport = (defArchDiff == 0) ? 0 : (int) (Math.ceil((double) defArchDiff / (double) defArchSplit));
            
            int supportsNeeded = Math.max(defSupport, defCavSupport);
            if(useArcher) supportsNeeded = Math.max(supportsNeeded, defArchSupport);
            return supportsNeeded;
        } else {
            int supportsNeeded = 0;
            for (UnitHolder unit: DataHolder.getSingleton().getUnits()) {
                if(unit.isDefense() && !unit.getPlainName().equals("knight")) {
                    int diff = pTargetAmount.getAmountForUnit(unit) - troops.getAmountForUnit(unit);
                    int unitSupports = (pSplitAmount.getAmountForUnit(unit) == 0) ? 0 :
                            (int) (Math.ceil((double) diff / (double) pSplitAmount.getAmountForUnit(unit)));
                    
                    supportsNeeded = Math.max(supportsNeeded, unitSupports);
                }
            }

            return supportsNeeded;
        }
    }

    public static TroopAmountFixed getRequiredTroops(Village pVillage, TroopAmountFixed pTargetAmounts) {
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE);
        TroopAmountFixed result = pTargetAmounts.clone();
        result.removeAmount(holder.getTroops());
        return result;
    }
}
