/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.sim;

import de.tor.tribes.io.UnitHolder;

/**
 *
 * @author Charon
 */
public class FighterPart {

    private UnitHolder unit = null;
    private int unitCount = 0;
    private int unitTech = 1;

    public FighterPart(UnitHolder pUnit, int pUnitCount, int pTechLevel) {
        unit = pUnit;
        unitCount = pUnitCount;
        unitTech = pTechLevel;
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
     * @return the unitCount
     */
    public int getUnitCount() {
        return unitCount;
    }

    /**
     * @param unitCount the unitCount to set
     */
    public void setUnitCount(int unitCount) {
        this.unitCount = unitCount;
    }

    /**
     * @return the unitTech
     */
    public int getUnitTech() {
        return unitTech;
    }

    /**
     * @param unitTech the unitTech to set
     */
    public void setUnitTech(int unitTech) {
        this.unitTech = unitTech;
    }
}
