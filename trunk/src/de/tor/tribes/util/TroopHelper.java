/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import javax.swing.DefaultListModel;
import javax.swing.JList;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class TroopHelper {

    private static Logger logger = Logger.getLogger("TroopHelper");

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

    private static class CapacityInfo {

        private Village village = null;
        private Integer carriage = 0;

        public CapacityInfo(Village pVillage, int pCarriage) {
            village = pVillage;
            carriage = pCarriage;
        }

        public Village getVillage() {
            return village;
        }

        public Integer getCarriage() {
            return carriage;
        }
    }

    public static Village[] getOwnVillagesByCarryCapacity(int pMinCapacity) {
        Village[] villages = GlobalOptions.getSelectedProfile().getTribe().getVillageList();
        List<CapacityInfo> result = new LinkedList<CapacityInfo>();
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
                    result.add(new CapacityInfo(v, capacity));
                }
            }
        }

        Collections.sort(result, new Comparator<CapacityInfo>() {

            @Override
            public int compare(CapacityInfo o1, CapacityInfo o2) {
                return o1.getCarriage().compareTo(o2.getCarriage());
            }
        });


        List<Village> finalList = new LinkedList<Village>();
        for (CapacityInfo v : result) {
            finalList.add(v.getVillage());
        }

        return finalList.toArray(new Village[finalList.size()]);
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
        int minUnits = DSWorkbenchFarmManager.getSingleton().getMinUnits();
        int minHaul = DSWorkbenchFarmManager.getSingleton().getMinHaul();

        if (holder != null) {
            List<UnitHolder> neededUnits = new LinkedList<UnitHolder>();
            UnitHolder[] allowed = DSWorkbenchFarmManager.getSingleton().getAllowedFarmUnits();
            Arrays.sort(allowed, UnitHolder.RUNTIME_COMPARATOR);
            for (UnitHolder unit : allowed) {
                int amount = holder.getTroopsOfUnitInVillage(unit);
                if (amount > 0) {
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
            if (resources < minHaul) {
                logger.debug("Min haul not reached (" + resources + " < " + minHaul + ")");
                return units;
            }
            int unitCount = 0;
            for (UnitHolder unit : neededUnits) {
                int amount = holder.getTroopsOfUnitInVillage(unit);
                if (amount * unit.getCarry() > resources) {
                    int neededUnitsOfType = (int) Math.rint(resources / unit.getCarry());
                    unitCount += neededUnitsOfType;
                    units.put(unit, neededUnitsOfType);
                    resources = 0;
                } else {
                    unitCount += amount;
                    units.put(unit, amount);
                    resources -= (int) (amount * unit.getCarry());
                }
                if (resources <= 0) {
                    break;
                }
            }

            if (unitCount < minUnits) {
                logger.debug("Min unit count not met (" + unitCount + " < " + minUnits + ")");
                units.clear();
                return units;
            }

            UnitHolder spy = DataHolder.getSingleton().getUnitByPlainName("spy");
            if (holder.getTroopsOfUnitInVillage(spy) > 0) {
                units.put(spy, 1);
            }
        } else {
            logger.debug("No troops found for village " + pSource);
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
            UnitHolder unit = keys.nextElement();
            Integer amount = pTroops.get(unit);
            if (amount != null && amount != 0) {
                speed = Math.max(speed, unit.getSpeed());
            }
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
