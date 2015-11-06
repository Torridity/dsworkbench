/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.UnitHolder;
import java.util.Hashtable;

/**
 *
 * @author Torridity
 */
public class SupportType {

    public enum DIRECTION {

        INCOMING, OUTGOING
    }
    private Village village = null;
    private Hashtable<UnitHolder, Integer> support = null;
    private DIRECTION direction = null;

    public SupportType(Village pVillage, Hashtable<UnitHolder, Integer> pSupport, DIRECTION pDirection) {
        village = pVillage;
        support = (Hashtable<UnitHolder, Integer>) pSupport.clone();
        direction = pDirection;
    }

    public Village getVillage() {
        return village;
    }

    public Hashtable<UnitHolder, Integer> getSupport() {
        return support;
    }

    public Integer getTroopsOfUnit(UnitHolder pUnit) {
        Integer amount = support.get(pUnit);
        return (amount == null) ? 0 : amount;
    }

    public DIRECTION getDirection() {
        return direction;
    }
}
