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
package de.tor.tribes.io;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.ext.Village;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class holds many functions for calculations with troops
 * 
 * @author extremeCrazyCoder
 */

public abstract class TroopAmount extends ManageableType implements Cloneable {
    private static final Logger logger = LogManager.getLogger("TroopAmount");

    @Override
    public String getElementIdentifier() {
        return "trpAmount";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "trpAmount";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }
    
    public abstract void addAmount(TroopAmount pTroops);
    public abstract void removeAmount(TroopAmount pRemove);
    public abstract void multiplyWith(double factor);
    protected abstract int getInternalAmountForUnit(UnitHolder pUnit, Village pVillage);

    public int getOffValue(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getAttack() * amount;
            }
        }

        return result;
    }

    public int getRealOffValue(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if ((unit.getPlainName().equals("axe")
                    || unit.getPlainName().equals("light")
                    || unit.getPlainName().equals("marcher")
                    || unit.getPlainName().equals("heavy")
                    || unit.getPlainName().equals("ram")
                    || unit.getPlainName().equals("catapult")) && amount > 0) {
                result += unit.getAttack() * amount;
            }
        }

        return result;
    }

    public int getDefValue(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getDefense() * amount;
            }
        }

        return result;
    }

    public int getDefArcherValue(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getDefenseArcher() * amount;
            }
        }

        return result;
    }

    public int getDefCavalryValue(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getDefenseCavalry() * amount;
            }
        }

        return result;
    }

    public int getTroopPopCount(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getPop() * amount;
            }
        }

        return result;
    }

    /**
     * Just the sum of all Units
     * ignores entries with -1
     * @return 
     */
    public int getTroopSum(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += amount;
            }
        }

        return result;
    }
    
    public int getFarmCapacity(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getCarry() * amount;
            }
        }

        return result;
    }
    
    /**
     * @param pVillage to send the troops from (only for dynamic)
     * @return Speed of slowest Unit
     */
    public double getSpeed(Village pVillage) {
        return getSlowestUnit(pVillage).getSpeed();
    }

    public UnitHolder getSlowestUnit(Village pVillage) {
        UnitHolder slowest = null;
        for(UnitHolder unit: this.getContainedUnits(pVillage)) {
            if(slowest == null || slowest.getSpeed() < unit.getSpeed()) {
                slowest = unit;
            }
        }
        return slowest;
    }
    
    public abstract List<UnitHolder> getContainedUnits();
    public abstract List<UnitHolder> getContainedUnits(Village pVillage);
}
