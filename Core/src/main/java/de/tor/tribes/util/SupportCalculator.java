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
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * @author Charon
 */
public class SupportCalculator {

    private static Logger logger = Logger.getLogger("SupportCalculator");

    public static List<SupportMovement> calculateSupport(Village pVillage, Date pArrive, boolean pRealDefOnly, List<Tag> pTags, int pMinNumber) {
        Hashtable<UnitHolder, Integer> unitTable = new Hashtable<>();
        if (logger.isDebugEnabled()) {
            logger.debug("Try to find support for village " + pVillage + " at arrival time " + new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(pArrive));
            if (pTags == null || pTags.isEmpty()) {
                logger.debug(" - using all villages of current user");
            } else {
                logger.debug(" - valid tags: " + pTags);
            }
            logger.debug(" - need at least " + pMinNumber + " units");
        }

        int cnt = 0;
        if (pRealDefOnly) {
            logger.debug("Using only def units");
            //use only "real" def units
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                switch (unit.getPlainName()) {
                    case "spear":
                        unitTable.put(unit, cnt);
                        break;
                    case "sword":
                        unitTable.put(unit, cnt);
                        break;
                    case "archer":
                        unitTable.put(unit, cnt);
                        break;
                    case "heavy":
                        unitTable.put(unit, cnt);
                        break;
                    case "knight":
                        unitTable.put(unit, cnt);
                        break;
                }
                cnt++;
            }
        } else {
            logger.debug("Using all units but spy, ram and snob");
            //use all units for def
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                if (!unit.getPlainName().equals("spy") && !unit.getPlainName().equals("snob") && !unit.getPlainName().equals("ram")) {
                    unitTable.put(unit, cnt);
                }
                cnt++;
            }
        }

        List<SupportMovement> movements = new LinkedList<>();

        //get tagged villages
        List<Village> villages = new LinkedList<>();
        if (pTags == null || pTags.isEmpty()) {
            Tribe own = GlobalOptions.getSelectedProfile().getTribe();
            if (own == null) {
                logger.error("Current tribe is 'null'");
                return movements;
            }
            Collections.addAll(villages, own.getVillageList());
        } else {
            for (Tag t : pTags) {
                for (Integer id : t.getVillageIDs()) {
                    Village v = DataHolder.getSingleton().getVillagesById().get(id);
                    if (!villages.contains(v)) {
                        villages.add(v);
                    }
                }
            }
        }
        /* Village[] villageList = tmpVillageList.toArray(new Village[]{});
        List<Village> villages = new LinkedList<Village>();
        //buid list of allowed villages
        for (Village v : villageList) {
        if (pTags != null && !pTags.isEmpty()) {
        for (Tag t : pTags) {
        if (t.tagsVillage(v.getId())) {
        if (!villages.contains(v)) {
        //add village if not already included
        villages.add(v);
        }
        }
        }
        } else {
        //add all villages
        villages.add(v);
        }
        }*/
        //move village itself
        villages.remove(pVillage);
        for (Village v : villages) {
            //use all villages
            UnitHolder slowestUnit = calculateAvailableUnits(pVillage, v, unitTable, pArrive, pMinNumber);
            if (slowestUnit != null) {
                //unit found
                movements.add(new SupportMovement(v, slowestUnit, new Date(pArrive.getTime() - ((long) (DSCalculator.calculateMoveTimeInSeconds(pVillage, v, slowestUnit.getSpeed()) * 1000)))));
            }
        }

        return movements;
    }

    private static UnitHolder calculateAvailableUnits(Village pTarget, Village pSource, Hashtable<UnitHolder, Integer> pUnitTable, Date pArrive, int pMinNumber) {
        Enumeration<UnitHolder> allowedKeys = pUnitTable.keys();
        VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(pSource, TroopsManager.TROOP_TYPE.OWN);
        if (troops == null) {
            return null;
        }
        // List<Integer> availableTroops = troops.getTroops();

        UnitHolder slowestPossible = null;
        while (allowedKeys.hasMoreElements()) {
            UnitHolder unit = allowedKeys.nextElement();
            int availCount = troops.getTroops().getAmountForUnit(unit);
            if (availCount > pMinNumber) {
                long ms = (long) (DSCalculator.calculateMoveTimeInSeconds(pSource, pTarget, unit.getSpeed()) * 1000);
                if (pArrive.getTime() - ms > System.currentTimeMillis()) {
                    if (slowestPossible == null) {
                        slowestPossible = unit;
                    } else {
                        if (unit.getSpeed() > slowestPossible.getSpeed()) {
                            //if current unit is slower use this unit
                            slowestPossible = unit;
                        }
                    }
                }
            }
        }
        return slowestPossible;
    }

    public static class SupportMovement {

        private Village source = null;
        private UnitHolder unit = null;
        private Date sendTime = null;

        public SupportMovement(Village pSource, UnitHolder pUnit, Date pStartDate) {
            this.source = pSource;
            this.unit = pUnit;
            this.sendTime = pStartDate;
        }

        /**
         * @return the source
         */
        public Village getSource() {
            return source;
        }

        /**
         * @param source the source to set
         */
        public void setSource(Village source) {
            this.source = source;
        }

        /**
         * @return the unit
         */
        public UnitHolder getUnit() {
            return unit;
        }

        /**
         * @param unit the unit to set
         */
        public void setUnit(UnitHolder unit) {
            this.unit = unit;
        }

        /**
         * @return the sendTime
         */
        public Date getSendTime() {
            return sendTime;
        }

        /**
         * @param sendTime the sendTime to set
         */
        public void setSendTime(Date sendTime) {
            this.sendTime = sendTime;
        }

        @Override
        public String toString() {
            return "Von " + source + " am " + new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss.SSS").format(sendTime) + " mit " + unit;
        }
    }
}


