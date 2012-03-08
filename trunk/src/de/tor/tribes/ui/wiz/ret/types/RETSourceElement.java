/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.ret.types;

import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class RETSourceElement {

    private Village village = null;
    private boolean ignored = false;

    public RETSourceElement(Village pVillage) {
        village = pVillage;
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public Village getVillage() {
        return village;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public boolean isIgnored() {
        return ignored;
    }
}
