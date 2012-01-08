/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchFarmManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class TroopHelper {

    public static Village[] getOwnVillagesByOwnTroops(Hashtable<UnitHolder, Integer> pTroops) {
        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
        List<Village> result = new LinkedList<Village>();
        for (Village v : villages) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null) {
                Enumeration<UnitHolder> keys = pTroops.keys();
                while (keys.hasMoreElements()) {
                    UnitHolder key = keys.nextElement();
                    if (holder.getTroopsOfUnitInVillage(key) < pTroops.get(key)) {
                        continue;
                    }
                }
                //village is valid
                result.add(v);
            }
        }

        return result.toArray(new Village[result.size()]);
    }

    public static Village[] getOwnVillagesByCarryCapacity(int pMinCapacity) {
        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
        List<Village> result = new LinkedList<Village>();
        UnitHolder[] allowedUnits = DSWorkbenchFarmManager.getSingleton().getAllowedFarmUnits();
        for (Village v : villages) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null) {
                int capacity = 0;
                for (UnitHolder unit : allowedUnits) {
                    capacity += unit.getCarry() * holder.getTroopsOfUnitInVillage(unit);
                }

                if (capacity >= pMinCapacity) {
                    //village is valid
                    result.add(v);
                }
            }
        }

        return result.toArray(new Village[result.size()]);
    }

    public static Village[] getOwnVillagesWithMaxCarryCapacity() {
        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
        int max = 0;
        List<Village> result = new LinkedList<Village>();
        UnitHolder[] allowedUnits = DSWorkbenchFarmManager.getSingleton().getAllowedFarmUnits();
        for (Village v : villages) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null) {
                int capacity = 0;
                for (UnitHolder unit : allowedUnits) {
                    capacity += unit.getCarry() * holder.getTroopsOfUnitInVillage(unit);
                }

                if (capacity > max) {
                    //new max
                    result.clear();
                    result.add(v);
                    max = capacity;
                } else if (capacity == max && max != 0) {
                    //same capacity as max
                    result.add(v);
                }
            }
        }

        return result.toArray(new Village[result.size()]);
    }

    public static Hashtable<UnitHolder, Integer> getTroopsForCarriage(Village pSource, Village pTarget, FarmInformation pInfo) {
        Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pSource, TroopsManager.TROOP_TYPE.OWN);
        double unitSpeed = 0;

        if (holder != null) {
            List<UnitHolder> neededUnits = new LinkedList<UnitHolder>();
            UnitHolder[] allowed = DSWorkbenchFarmManager.getSingleton().getAllowedFarmUnits();
            Arrays.sort(allowed, UnitHolder.RUNTIME_COMPARATOR);
            for (UnitHolder unit : allowed) {
                int amount = holder.getTroopsOfUnitInVillage(unit);
                if (amount > 30) {
                    int resources = pInfo.getResourcesInStorage(System.currentTimeMillis() + DSCalculator.calculateMoveTimeInMillis(pSource, pTarget, unit.getSpeed()));
                    neededUnits.add(unit);
                    unitSpeed = Math.max(unitSpeed, unit.getSpeed());
                    for (UnitHolder neededUnit : neededUnits) {
                        resources -= (int) (holder.getTroopsOfUnitInVillage(neededUnit) * neededUnit.getCarry());
                    }

                    if (resources <= 0) {
                        //can carry all
                        break;
                    }
                }
            }

            int resources = pInfo.getResourcesInStorage(System.currentTimeMillis() + DSCalculator.calculateMoveTimeInMillis(pSource, pTarget, unitSpeed));
            for (UnitHolder unit : neededUnits) {
                int amount = holder.getTroopsOfUnitInVillage(unit);
                if (amount * unit.getCarry() > resources) {
                    units.put(unit, (int) Math.rint(resources / unit.getCarry()));
                    resources = 0;
                } else {
                    units.put(unit, amount);
                    resources -= (int) (amount * unit.getCarry());
                }
                if (resources <= 0) {
                    break;
                }
            }
            UnitHolder spy = DataHolder.getSingleton().getUnitByPlainName("spy");
            if (holder.getTroopsOfUnitInVillage(spy) > 0) {
                units.put(spy, 1);
            }
        }
        return units;
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
            speed = Math.max(speed, keys.nextElement().getSpeed());
        }
        return speed;
    }

    public static int getPopulation(Hashtable<UnitHolder, Integer> pTroops) {
        int pop = 0;
        Enumeration<UnitHolder> keys = pTroops.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            pop += unit.getPop() * pTroops.get(unit);
        }
        return pop;
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
}
