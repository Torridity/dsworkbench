/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

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
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage);
        if (holder == null) {
            return false;
        }
        int amount = holder.getTroopsOfUnitInVillage(getUnit());
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
