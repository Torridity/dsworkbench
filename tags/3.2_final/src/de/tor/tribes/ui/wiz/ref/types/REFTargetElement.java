/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.ref.types;

import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class REFTargetElement {

    private Village village = null;
    private int neededSupports = 0;

    public REFTargetElement(Village pVillage) {
        setVillage(pVillage);
    }

    public final void setVillage(Village village) {
        this.village = village;
    }

    public Village getVillage() {
        return village;
    }

    public void setNeededSupports(int neededSupports) {
        this.neededSupports = neededSupports;
    }

    public int getNeededSupports() {
        return neededSupports;
    }
}
