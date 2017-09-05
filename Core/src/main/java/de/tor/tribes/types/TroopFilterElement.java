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

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;

/**
 *
 * @author Torridity
 */
public class TroopFilterElement {
    
    private UnitHolder unit = null;
    private int minAmount = 0;
    private int maxAmount = 0;
    
    public TroopFilterElement(UnitHolder pUnit, int pMin, int pMax) {
        unit = pUnit;
        setMin(pMin);
        setMax(pMax);
    }
    
    public UnitHolder getUnit() {
        return unit;
    }
    
    public int getMin() {
        return minAmount;
    }
    
    public int getMax() {
        return maxAmount;
    }
    
    public void setMin(int pMin) {
        if (pMin < 0) {
            minAmount = Integer.MIN_VALUE;
        } else {
            minAmount = pMin;
        }
    }
    
    public void setMax(int pMax) {
        if (pMax < 0) {
            //no max
            maxAmount = Integer.MAX_VALUE;
        } else {
            maxAmount = pMax;
        }
    }
    
    public boolean allowsVillage(Village pVillage) {
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OWN);
        if (holder == null) {
            return false;
        }
        int amount = holder.getTroopsOfUnitInVillage(unit);
        return (amount >= minAmount && amount <= maxAmount);
    }
    
    @Override
    public String toString() {
        String res = "";
        res += unit.toString();
        if (minAmount == Integer.MIN_VALUE && maxAmount == Integer.MAX_VALUE) {
            res += " (keine Einschränkung)";
        } else if (minAmount == Integer.MIN_VALUE && maxAmount != Integer.MAX_VALUE) {
            res += " (max. " + maxAmount + ")";
        } else if (minAmount != Integer.MIN_VALUE && maxAmount == Integer.MAX_VALUE) {
            res += " (min. " + minAmount + ")";
        } else {
            res += " (" + minAmount + " bis " + maxAmount + ")";
        }
        return res;
    }
}
