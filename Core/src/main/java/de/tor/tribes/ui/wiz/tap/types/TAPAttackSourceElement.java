/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.wiz.tap.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Torridity
 */
public class TAPAttackSourceElement {

    private Village village = null;
    private UnitHolder unit = null;
    private boolean fake = false;
    private boolean ignored = false;

    public TAPAttackSourceElement(Village pVillage, UnitHolder pUnit) {
        village = pVillage;
        unit = pUnit;
    }

    public TAPAttackSourceElement(Village pVillage, UnitHolder pUnit, boolean pFake) {
        this(pVillage, pUnit);
        fake = pFake;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TAPAttackSourceElement) {
            return ((TAPAttackSourceElement) obj).getVillage().equals(getVillage());
        }
        return false;
    }

    public Village getVillage() {
        return village;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public void setUnit(UnitHolder pUnit) {
        unit = pUnit;
    }

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean pValue) {
        fake = pValue;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean pValue) {
        ignored = pValue;
    }
}
