/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.dep.types;

import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class SupportSourceElement {

    private Village village = null;
    private int supports = 0;
    private boolean ignored = false;

    public SupportSourceElement(Village pVillage, int pSupports) {
        village = pVillage;
        supports = pSupports;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SupportSourceElement) {
            return ((SupportSourceElement) obj).getVillage().equals(getVillage());
        }
        return false;
    }

    public Village getVillage() {
        return village;
    }

    public int getSupports() {
        return supports;
    }

    public void setSupports(int pSupports) {
        supports = pSupports;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean pValue) {
        ignored = pValue;
    }
}
