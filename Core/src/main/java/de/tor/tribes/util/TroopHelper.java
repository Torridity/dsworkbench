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
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchFarmManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class TroopHelper {

    private static Logger logger = Logger.getLogger("TroopHelper");
    private static int[] ramsNeeded = new int[]{0, 2, 4, 7, 10, 14, 19, 24, 30, 37, 46, 55, 65, 77, 91, 106, 124, 143, 166, 191, 219};

    public static Hashtable<UnitHolder, Integer> getTroopsForCarriage(DSWorkbenchFarmManager.FARM_CONFIGURATION pConfig, VillageTroopsHolder pTroops, FarmInformation pInfo) {
        Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
        Village source = pTroops.getVillage();

        UnitHolder[] allowed = DSWorkbenchFarmManager.getSingleton().getAllowedFarmUnits(pConfig);
        Arrays.sort(allowed, UnitHolder.RUNTIME_COMPARATOR);
        boolean minUnitsMetOnce = false;
        if (logger.isDebugEnabled()) {
            logger.debug("Getting farm units from " + source);
        }

        if (DSWorkbenchFarmManager.getSingleton().isUseRams(pConfig)) {
            if (pInfo.getWallLevel() > 0) {
                UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
                int rams = pTroops.getTroopsOfUnitInVillage(ram) - DSWorkbenchFarmManager.getSingleton().getBackupUnits(ram);
                if (rams > 1) {
                    int needed = ramsNeeded[pInfo.getWallLevel()];
                    int using = Math.min(needed, rams);
                    units.put(ram, using);
                }
            }
        }

        for (UnitHolder unit : allowed) {
            int amount = pTroops.getTroopsOfUnitInVillage(unit) - DSWorkbenchFarmManager.getSingleton().getBackupUnits(unit);
            if (amount > 0) {
                //get current unit speed
                double speed = unit.getSpeed();
                Set<Entry<UnitHolder, Integer>> entries = units.entrySet();
                int currentCarryCapacity = 0;
                //correct speed by used units (not necessary as they are sorted by runtime!? ... but won't hurt anyway)
                for (Entry<UnitHolder, Integer> entry : entries) {
                    //get max speed and...
                    speed = Math.max(speed, entry.getKey().getSpeed());
                    //...current carry capacity of existing entries
                    currentCarryCapacity += entry.getKey().getCarry() * entry.getValue();
                }//in the first loop, speed will be the unit speed and carry capacity will be 0

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
                        && (minUnitsMetOnce || neededAmountOfUnit > DSWorkbenchFarmManager.getSingleton().getMinUnits(pConfig, unit))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding " + neededAmountOfUnit + " units of type " + unit);
                    }
                    //unit can carry all and more units than min are needed
                    units.put(unit, neededAmountOfUnit);
                    resources -= unit.getCarry() * neededAmountOfUnit;
                    minUnitsMetOnce = true;
                } else if (neededAmountOfUnit > amount && (minUnitsMetOnce || amount > DSWorkbenchFarmManager.getSingleton().getMinUnits(pConfig, unit))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding all units (" + amount + ") of type " + unit);
                    }
                    //unit can not carry all and but more units than min are available
                    units.put(unit, amount);
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
        Integer neededSpies = DSWorkbenchFarmManager.getSingleton().getMinUnits(pConfig, spy);
        int availableSpies = pTroops.getTroopsOfUnitInVillage(spy) - DSWorkbenchFarmManager.getSingleton().getBackupUnits(spy);
        if (neededSpies != null) {
            units.put(spy, (neededSpies > availableSpies) ? availableSpies : neededSpies);
        }

        //check result
        double speed = 0;
        int carryCapacity = 0;
        Set<Entry<UnitHolder, Integer>> entries = units.entrySet();
        for (Entry<UnitHolder, Integer> entry : entries) {
            speed = Math.max(speed, entry.getKey().getSpeed());
            carryCapacity += entry.getKey().getCarry() * entry.getValue();
        }
        int resources = pInfo.getResourcesInStorage(System.currentTimeMillis() + DSCalculator.calculateMoveTimeInMillis(source, pInfo.getVillage(), speed));
        if (resources < carryCapacity && pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.C)) {
            if (!DSWorkbenchFarmManager.getSingleton().allowPartlyFarming()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to get enough units to carry '" + resources + "' resources. Max. carriage was '" + carryCapacity + "'");
                }
                //not enough units found
                units.clear();
            }
        }
        return units;
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillages(Hashtable<UnitHolder, Integer> pMinAmounts) {
        Hashtable<Village, VillageTroopsHolder> result = new Hashtable<Village, VillageTroopsHolder>();
        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null && hasMinTroopAmounts(holder, pMinAmounts)) {
                result.put(holder.getVillage(), holder);
            }
        }
        return result;
    }

    public static boolean hasMinTroopAmounts(VillageTroopsHolder pHolder, Hashtable<UnitHolder, Integer> pMinAmounts) {
        if (pMinAmounts == null || pMinAmounts.isEmpty()) {
            return true;
        }

        Set<Entry<UnitHolder, Integer>> entries = pMinAmounts.entrySet();
        for (Entry<UnitHolder, Integer> entry : entries) {//check for all units amount and backup
            Integer amount = pHolder.getAmountForUnit(entry.getKey());
            if (amount == null || amount < entry.getValue() || amount < DSWorkbenchFarmManager.getSingleton().getBackupUnits(entry.getKey())) {
                //no troops of type or not enough units or backup met
                return false;
            }
        }
        return true;
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillages() {
        return getOwnTroopsForAllVillages(null);
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillagesByCapacity(FarmInformation pInfo) {
        Hashtable<Village, VillageTroopsHolder> result = new Hashtable<Village, VillageTroopsHolder>();
        int currentResources = pInfo.getResourcesInStorage(System.currentTimeMillis());
        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null) {
                if (getCapacity(holder.getTroops()) >= currentResources) {
                    //village is valid
                    result.put(holder.getVillage(), holder);
                }
            }
        }
        return result;
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillagesByMinHaul(int pMinHaul) {
        Hashtable<Village, VillageTroopsHolder> result = new Hashtable<Village, VillageTroopsHolder>();

        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null) {
                if (getCapacity(holder.getTroops()) >= pMinHaul) {
                    //village is valid
                    result.put(holder.getVillage(), holder);
                }
            }
        }
        return result;
    }

    public static void sendTroops(Village pVillage, Hashtable<UnitHolder, Integer> pTroops) {
        VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
        VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE);
        VillageTroopsHolder onTheWay = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.ON_THE_WAY);

        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            if (own != null) {//check for case that no troops are available at all
                own.setAmountForUnit(unit, own.getAmountForUnit(unit) - pTroops.get(unit));
            }
            if (inVillage != null) {//check for case that troops are from place
                inVillage.setAmountForUnit(unit, inVillage.getAmountForUnit(unit) - pTroops.get(unit));
            }
            if (onTheWay != null) {//check for case that troops are from place
                onTheWay.setAmountForUnit(unit, onTheWay.getAmountForUnit(unit) + pTroops.get(unit));
            }
        }
    }

    public static double getTroopSpeed(Hashtable<UnitHolder, Integer> pTroops) {
        double speed = 0;
        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            Integer amount = pTroops.get(unit);
            if (amount != null && amount != 0) {
                speed = Math.max(speed, unit.getSpeed());
            }
        }
        return speed;
    }

    public static UnitHolder getSlowestUnit(Hashtable<UnitHolder, Integer> pTroops) {
        UnitHolder slowest = null;

        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            Integer amount = pTroops.get(unit);
            if (amount != null && amount > 0) {
                if (slowest == null) {
                    slowest = unit;
                } else {
                    if (unit.getSpeed() > slowest.getSpeed()) {
                        slowest = unit;
                    }
                }
            }
        }
        return slowest;
    }

    public static List<UnitHolder> getContainedUnits(Hashtable<UnitHolder, Integer> pTroops) {
        List<UnitHolder> units = new LinkedList<UnitHolder>();

        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            Integer amount = pTroops.get(unit);
            if (amount != null && amount > 0) {
                units.add(unit);
            }
        }
        return units;
    }

    public static double getAttackForce(Hashtable<UnitHolder, Integer> pTroops) {
        double result = 0;

        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            Integer amount = pTroops.get(unit);
            if (amount != null && amount > 0) {
                result += amount * unit.getAttack();
            }
        }
        return result;
    }

    public static int getPopulation(Hashtable<UnitHolder, Integer> pTroops) {
        int pop = 0;
        if (pTroops != null) {
            Enumeration<UnitHolder> keys = pTroops.keys();
            while (keys.hasMoreElements()) {
                UnitHolder unit = keys.nextElement();
                pop += unit.getPop() * pTroops.get(unit);
            }
        }
        return pop;
    }

    public static int getCapacity(Hashtable<UnitHolder, Integer> pTroops) {
        int capacity = 0;
        if (pTroops != null && !pTroops.isEmpty()) {
            Enumeration<UnitHolder> units = pTroops.keys();
            while (units.hasMoreElements()) {
                UnitHolder unit = units.nextElement();
                Integer amount = pTroops.get(unit);
                if (amount != null && amount != 0) {
                    capacity += unit.getCarry() * amount;
                }
            }
        }
        return capacity;
    }

    public static boolean isEmpty(Hashtable<UnitHolder, Integer> pTroops) {
        int pop = 0;
        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            pop += unit.getPop() * pTroops.get(unit);
        }
        return pop == 0;
    }

    public static String unitTableToProperty(Hashtable<UnitHolder, Integer> pTroops) {
        StringBuilder result = new StringBuilder();
        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder key = keys.nextElement();
            result.append(key.getPlainName()).append("=").append(pTroops.get(key)).append("/");
        }
        return result.toString().substring(0, result.length() - 1);
    }

    public static String stringUnitTableToProperty(Hashtable<String, Integer> pTroops) {
        StringBuilder result = new StringBuilder();
        Enumeration<String> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            result.append(key).append("=").append(pTroops.get(key)).append("/");
        }
        return result.toString().substring(0, result.length() - 1);
    }

    public static Hashtable<UnitHolder, Integer> propertyToUnitTable(String pProperty) {
        Hashtable<UnitHolder, Integer> result = new Hashtable<UnitHolder, Integer>();
        if (pProperty == null) {
            return result;
        }
        try {
            String[] troops = pProperty.split("/");
            for (String unit : troops) {
                String[] split = unit.split("=");
                result.put(DataHolder.getSingleton().getUnitByPlainName(split[0].trim()), Integer.parseInt(split[1]));
            }
        } catch (Exception e) {
            logger.warn("Failed to read troops from property '" + pProperty + "'", e);
            result.clear();
        }
        return result;
    }

    public static Hashtable<String, Integer> stringPropertyToUnitTable(String pProperty) {
        Hashtable<String, Integer> result = new Hashtable<String, Integer>();
        if (pProperty == null) {
            return result;
        }
        try {
            String[] troops = pProperty.split("/");
            for (String unit : troops) {
                String[] split = unit.split("=");
                result.put(split[0].trim(), Integer.parseInt(split[1]));
            }
        } catch (Exception e) {
            logger.warn("Failed to read troops from property '" + pProperty + "'", e);
            result.clear();
        }
        return result;
    }

    public static Hashtable<String, Integer> unitTableToSerializableFormat(Hashtable<UnitHolder, Integer> pTroops) {
        Hashtable<String, Integer> result = new Hashtable<String, Integer>();
        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder key = keys.nextElement();
            result.put(key.getPlainName(), pTroops.get(key));
        }
        return result;
    }

    public static Hashtable<UnitHolder, Integer> unitTableFromSerializableFormat(Hashtable<String, Integer> pTroops) {
        Hashtable<UnitHolder, Integer> result = new Hashtable<UnitHolder, Integer>();
        Enumeration<String> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            result.put(DataHolder.getSingleton().getUnitByPlainName(key), pTroops.get(key));
        }
        return result;
    }

    public static String unitListToProperty(JList pList) {
        if (pList.getModel() instanceof DefaultListModel) {
            DefaultListModel model = (DefaultListModel) pList.getModel();
            String result = "";
            for (int i = 0; i < model.getSize(); i++) {
                Object elem = model.getElementAt(i);
                if (elem instanceof UnitHolder) {
                    result += ((UnitHolder) model.getElementAt(i)).getPlainName();
                    if (i != model.getSize() - 1) {
                        result += ";";
                    }
                } else {
                    logger.warn("Element " + i + " is not an instance of UnitHolder");
                }
            }
            return result;
        } else {
            logger.warn("List model not an instance of DefaultListModel");
            return "";
        }
    }

    public static DefaultListModel unitListPropertyToModel(String pProperty, UnitHolder[] pDefault) {
        DefaultListModel model = new DefaultListModel();
        String[] elems = (pProperty == null) ? null : pProperty.split(";");
        if (elems == null || elems.length == 0) {
            for (UnitHolder unit : pDefault) {
                model.addElement(unit);
            }
        } else {
            for (String elem : elems) {
                UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(elem);
                if (unit.equals(UnknownUnit.getSingleton())) {
                    logger.warn("Read unknown unit from string " + elem);
                } else {
                    model.addElement(unit);
                }
            }
        }

        return model;
    }

    public static VillageTroopsHolder getRandomOffVillageTroops(Village pVillage) {
        Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (unit.isOffense()) {
                units.put(unit, (int) Math.rint(Math.random() * 7000.0 / unit.getPop()));
            } else {
                units.put(unit, 0);
            }
        }
        VillageTroopsHolder holder = new VillageTroopsHolder(pVillage, new Date(System.currentTimeMillis()));
        holder.setTroops(units);
        return holder;
    }

    public static int getAttackForce(Village pVillage, UnitHolder pSlowedUnit) {
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
        if (holder == null) {
            return 0;
        }

        Hashtable<UnitHolder, Integer> units = holder.getTroops();

        Set<Entry<UnitHolder, Integer>> keys = units.entrySet();
        int force = 0;
        for (Entry<UnitHolder, Integer> key : keys) {
            UnitHolder unit = key.getKey();
            Integer value = key.getValue();
            if (value > 0 && unit.getSpeed() <= pSlowedUnit.getSpeed()) {
                force += unit.getAttack() * value;
            }
        }
        return force;
    }

    public static int getNeededSupports(Village pVillage, Hashtable<UnitHolder, Integer> pTargetAmount, Hashtable<UnitHolder, Integer> pSplitAmount, boolean pAllowSimilar) {
        boolean useArcher = !DataHolder.getSingleton().getUnitByPlainName("archer").equals(UnknownUnit.getSingleton());

        double defGoal = 0;
        double defCavGoal = 0;
        double defArchGoal = 0;
        int defSplit = 0;
        int defCavSplit = 0;
        int defArchSplit = 0;

        //
        UnitHolder current = DataHolder.getSingleton().getUnitByPlainName("spear");
        Integer spearGoal = pTargetAmount.get(current);
        defGoal += spearGoal * current.getDefense();
        defGoal += spearGoal * current.getDefenseCavalry();
        defArchGoal += (useArcher) ? spearGoal * current.getDefenseArcher() : 0;
        Integer spearSplit = pSplitAmount.get(current);
        defSplit += spearSplit * current.getDefense();
        defCavSplit += spearSplit * current.getDefense();
        defArchSplit += (useArcher) ? spearSplit * current.getDefenseArcher() : 0;
        //
        current = DataHolder.getSingleton().getUnitByPlainName("sword");
        Integer swordGoal = pTargetAmount.get(current);
        defGoal += swordGoal * current.getDefense();
        defCavGoal += swordGoal * current.getDefenseCavalry();
        defArchGoal += (useArcher) ? swordGoal * current.getDefenseArcher() : 0;
        Integer swordSplit = pSplitAmount.get(current);
        defSplit += swordSplit * current.getDefense();
        defCavSplit += swordSplit * current.getDefense();
        defArchSplit += (useArcher) ? swordSplit * current.getDefenseArcher() : 0;
        //
        Integer archerGoal = 0;
        Integer archerSplit = 0;
        if (useArcher) {
            current = DataHolder.getSingleton().getUnitByPlainName("archer");
            archerGoal = pTargetAmount.get(current);
            defGoal += archerGoal * current.getDefense();
            defCavGoal += archerGoal * current.getDefenseCavalry();
            defArchGoal += (useArcher) ? archerGoal * current.getDefenseArcher() : 0;
            archerSplit = pSplitAmount.get(current);
            defSplit += archerSplit * current.getDefense();
            defCavSplit += archerSplit * current.getDefense();
            defArchSplit += (useArcher) ? archerSplit * current.getDefenseArcher() : 0;
        }
        //
        current = DataHolder.getSingleton().getUnitByPlainName("spy");
        Integer spyGoal = pTargetAmount.get(current);
        defGoal += spyGoal * current.getDefense();
        defCavGoal += spyGoal * current.getDefenseCavalry();
        defArchGoal += (useArcher) ? spyGoal * current.getDefenseArcher() : 0;
        Integer spySplit = pSplitAmount.get(current);
        defSplit += spySplit * current.getDefense();
        defCavSplit += spySplit * current.getDefense();
        defArchSplit += (useArcher) ? spySplit * current.getDefenseArcher() : 0;
        //
        current = DataHolder.getSingleton().getUnitByPlainName("heavy");
        Integer heavyGoal = pTargetAmount.get(DataHolder.getSingleton().getUnitByPlainName("heavy"));
        defGoal += heavyGoal * current.getDefense();
        defCavGoal += heavyGoal * current.getDefenseCavalry();
        defArchGoal += (useArcher) ? heavyGoal * current.getDefenseArcher() : 0;
        Integer heavySplit = pSplitAmount.get(current);
        defSplit += heavySplit * current.getDefense();
        defCavSplit += heavySplit * current.getDefense();
        defArchSplit += (useArcher) ? heavySplit * current.getDefenseArcher() : 0;
        //

        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE);
        Hashtable<UnitHolder, Integer> troops;
        if (holder == null) {
            troops = new Hashtable<UnitHolder, Integer>();
            troops.put(DataHolder.getSingleton().getUnitByPlainName("spear"), 0);
            troops.put(DataHolder.getSingleton().getUnitByPlainName("sword"), 0);
            if (useArcher) {
                troops.put(DataHolder.getSingleton().getUnitByPlainName("archer"), 0);
            }
            troops.put(DataHolder.getSingleton().getUnitByPlainName("spy"), 0);
            troops.put(DataHolder.getSingleton().getUnitByPlainName("heavy"), 0);
        } else {
            troops = holder.getTroops();
        }

        if (pAllowSimilar) {
            int def = 0;
            int defCav = 0;
            int defArch = 0;

            Set<Entry<UnitHolder, Integer>> entries = troops.entrySet();

            for (Entry<UnitHolder, Integer> entry : entries) {
                def += entry.getKey().getDefense() * entry.getValue();
                defCav += entry.getKey().getDefenseCavalry() * entry.getValue();
                if (useArcher) {
                    defArch += entry.getKey().getDefenseArcher() * entry.getValue();
                }
            }

            int defDiff = (int) Math.rint(defGoal - def);
            int defCavDiff = (int) Math.rint(defCavGoal - defCav);
            int defArchDiff = (useArcher) ? (int) Math.rint(defArchGoal - defArch) : 0;

            int defSupport = (defDiff == 0) ? 0 : (int) (Math.ceil((double) defDiff / (double) defSplit));
            int defCavSupport = (defCavDiff == 0) ? 0 : (int) (Math.ceil((double) defCavDiff / (double) defCavSplit));
            int defArchSupport = (defArchDiff == 0) ? 0 : (int) (Math.ceil((double) defArchDiff / (double) defArchSplit));

            return Math.max(Math.max(defSupport, defCavSupport), defArchSupport);
        } else {
            int spearDiff = spearGoal - troops.get(DataHolder.getSingleton().getUnitByPlainName("spear"));
            int swordDiff = swordGoal - troops.get(DataHolder.getSingleton().getUnitByPlainName("sword"));
            int archerDiff = 0;
            if (useArcher) {
                archerDiff = archerGoal - troops.get(DataHolder.getSingleton().getUnitByPlainName("archer"));
            }
            int spyDiff = spyGoal - troops.get(DataHolder.getSingleton().getUnitByPlainName("spy"));
            int heavyDiff = heavyGoal - troops.get(DataHolder.getSingleton().getUnitByPlainName("heavy"));

            int spearSupports = (spearSplit == 0) ? 0 : (int) (Math.ceil((double) spearDiff / (double) spearSplit));
            int swordSupports = (swordSplit == 0) ? 0 : (int) (Math.ceil((double) swordDiff / (double) swordSplit));
            int archerSupports = (archerSplit == 0) ? 0 : (int) (Math.ceil((double) archerDiff / (double) archerSplit));
            int spySupports = (spySplit == 0) ? 0 : (int) (Math.ceil((double) spyDiff / (double) spySplit));
            int heavySupports = (heavySplit == 0) ? 0 : (int) (Math.ceil((double) heavyDiff / (double) heavySplit));

            return Math.max(Math.max(Math.max(Math.max(spearSupports, swordSupports), archerSupports), spySupports), heavySupports);
        }
    }

    public static Hashtable<UnitHolder, Integer> getRequiredTroops(Village pVillage, Hashtable<UnitHolder, Integer> pTargetAmounts) {
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE);
        Hashtable<UnitHolder, Integer> result = new Hashtable<UnitHolder, Integer>();
        Set<Entry<UnitHolder, Integer>> entries = pTargetAmounts.entrySet();
        for (Entry<UnitHolder, Integer> entry : entries) {
            UnitHolder targetUnit = entry.getKey();
            Integer targetAmount = entry.getValue();
            int amountInVillage = holder.getAmountForUnit(targetUnit);
            int required = targetAmount - amountInVillage;
            result.put(targetUnit, (required > 0) ? required : 0);
        }
        return result;
    }
}
