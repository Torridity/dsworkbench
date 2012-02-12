/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.control.ManageableType;
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

    public static Hashtable<UnitHolder, Integer> getTroopsForCarriage(DSWorkbenchFarmManager.FARM_CONFIGURATION pConfig, VillageTroopsHolder pTroops, FarmInformation pInfo, boolean pAcceptPartlyFarming) {
        Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
        Village source = pTroops.getVillage();

        UnitHolder[] allowed = DSWorkbenchFarmManager.getSingleton().getAllowedFarmUnits(pConfig);
        Arrays.sort(allowed, UnitHolder.RUNTIME_COMPARATOR);
        boolean minUnitsMetOnce = false;
        if (logger.isDebugEnabled()) {
            logger.debug("Getting farm units from " + source);
        }
        for (UnitHolder unit : allowed) {
            int amount = pTroops.getTroopsOfUnitInVillage(unit);
            if (amount > 0 && amount > DSWorkbenchFarmManager.getSingleton().getBackupUnits(unit)) {
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
                if (neededAmountOfUnit <= amount && (minUnitsMetOnce || neededAmountOfUnit > DSWorkbenchFarmManager.getSingleton().getMinUnits(pConfig, unit))) {
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
        int availableSpies = pTroops.getTroopsOfUnitInVillage(spy);
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
        if (resources > carryCapacity && !pAcceptPartlyFarming) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to get enough units to carry '" + resources + "' resources. Max. carriage was '" + carryCapacity + "'");
            }
            //not enough units found
            units.clear();
        }
        return units;
    }

    public static Hashtable<Village, VillageTroopsHolder> getOwnTroopsForAllVillages(Hashtable<UnitHolder, Integer> pMinAmounts) {
        Hashtable<Village, VillageTroopsHolder> result = new Hashtable<Village, VillageTroopsHolder>();
        for (ManageableType t : TroopsManager.getSingleton().getAllElements(TroopsManager.OWN_GROUP)) {
            VillageTroopsHolder holder = (VillageTroopsHolder) t;
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
        for (ManageableType t : TroopsManager.getSingleton().getAllElements(TroopsManager.OWN_GROUP)) {
            VillageTroopsHolder holder = (VillageTroopsHolder) t;
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
        for (ManageableType t : TroopsManager.getSingleton().getAllElements(TroopsManager.OWN_GROUP)) {
            VillageTroopsHolder holder = (VillageTroopsHolder) t;
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

        if (own == null || inVillage == null || onTheWay == null) {
            return;
        }

        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            own.setAmountForUnit(unit, own.getAmountForUnit(unit) - pTroops.get(unit));
            inVillage.setAmountForUnit(unit, inVillage.getAmountForUnit(unit) - pTroops.get(unit));
            onTheWay.setAmountForUnit(unit, onTheWay.getAmountForUnit(unit) + pTroops.get(unit));
        }
    }

    public static void returnTroops(Village pVillage, Hashtable<UnitHolder, Integer> pTroops) {
        VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
        VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE);
        VillageTroopsHolder onTheWay = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.ON_THE_WAY);

        if (own == null || inVillage == null || onTheWay == null) {
            return;
        }

        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            own.setAmountForUnit(unit, own.getAmountForUnit(unit) + pTroops.get(unit));
            inVillage.setAmountForUnit(unit, inVillage.getAmountForUnit(unit) + pTroops.get(unit));
            onTheWay.setAmountForUnit(unit, onTheWay.getAmountForUnit(unit) - pTroops.get(unit));
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
}
