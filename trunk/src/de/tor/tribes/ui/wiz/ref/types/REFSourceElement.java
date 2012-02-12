/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.ref.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.TroopSplit;
import de.tor.tribes.types.ext.Village;
import java.util.Hashtable;

/**
 *
 * @author Torridity
 */
public class REFSourceElement {

    private Village village = null;
    private TroopSplit split = null;

    public REFSourceElement(Village pVillage) {
        setVillage(pVillage);
        split = new TroopSplit(pVillage);
    }

    public void updateAvailableSupports(Hashtable<UnitHolder, Integer> pUnits, int pTolerance) {
        split.update(pUnits, pTolerance);
    }

    public final void setVillage(Village village) {
        this.village = village;
    }

    public Village getVillage() {
        return village;
    }

    public int getAvailableSupports() {
        return split.getSplitCount();
    }
}
