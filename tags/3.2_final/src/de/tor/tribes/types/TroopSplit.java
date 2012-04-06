/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author Torridity
 */
public class TroopSplit {

    private Village mVillage = null;
    private int iSplitCount = 1;

    public TroopSplit(Village pVillage) {
        mVillage = pVillage;
    }

    public void update(Hashtable<UnitHolder, Integer> pSplitValues, int pTolerance) {
        if (pSplitValues.isEmpty()) {
            iSplitCount = 1;
            return;
        }
        Enumeration<UnitHolder> unitKeys = pSplitValues.keys();
        int maxSplitCount = -1;
        while (unitKeys.hasMoreElements()) {
            UnitHolder unitKey = unitKeys.nextElement();
            Integer splitAmount = pSplitValues.get(unitKey);
            if (splitAmount > 0) {
                VillageTroopsHolder ownTroops = TroopsManager.getSingleton().getTroopsForVillage(mVillage, TroopsManager.TROOP_TYPE.OWN);

                if (ownTroops == null) {
                    //do nothing if there are no own troops in the village
                    iSplitCount = 0;
                    return;
                }

                int amountInVillage = ownTroops.getTroopsOfUnitInVillage(unitKey);
                int split = amountInVillage / splitAmount;
                int currentSplitCount = split;
                int rest = amountInVillage - split * splitAmount;
                if (100.0 * (double) rest / (double) splitAmount >= 100.0 - (double) pTolerance) {
                    currentSplitCount++;
                }
                if (maxSplitCount == -1 || (currentSplitCount < maxSplitCount)) {
                    maxSplitCount = currentSplitCount;
                }
            }
        }

        iSplitCount = Math.max(0, maxSplitCount);
    }

    public Village getVillage() {
        return mVillage;
    }

    public int getSplitCount() {
        return iSplitCount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(mVillage.toString()).append(" (").append(iSplitCount).append("x)");
        return builder.toString();
    }
}
