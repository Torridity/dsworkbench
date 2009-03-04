/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.UnitHolder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public abstract class AbstractTroopMovement {

    private Village mTarget = null;
    private Hashtable<UnitHolder, List<Village>> mOffs = null;
    private int iMinOffs = 0;
    private int iMaxOffs = 0;

    public AbstractTroopMovement(Village pTarget, int pMinOffs, int pMaxOffs) {
        setTarget(pTarget);
        mOffs = new Hashtable<UnitHolder, List<Village>>();
        iMinOffs = pMinOffs;
        iMaxOffs = pMaxOffs;
    }

    public void setTarget(Village pTarget) {
        mTarget = pTarget;
    }

    public Village getTarget() {
        return mTarget;
    }

    public void addOff(UnitHolder pUnit, Village mSource) {
        List<Village> sourcesForUnit = mOffs.get(pUnit);
        if (sourcesForUnit == null) {
            sourcesForUnit = new LinkedList<Village>();
            sourcesForUnit.add(mSource);
            mOffs.put(pUnit, sourcesForUnit);
        } else {
            sourcesForUnit.add(mSource);
        }
    }

    public Hashtable<UnitHolder, List<Village>> getOffs() {
        return mOffs;
    }

    public boolean offValid() {
        Enumeration<UnitHolder> unitKeys = mOffs.keys();
        int offs = 0;
        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            offs += mOffs.get(unit).size();
        }

        return (offs >= iMinOffs);
    }

    public boolean offComplete() {
        Enumeration<UnitHolder> unitKeys = mOffs.keys();
        int offs = 0;
        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            offs += mOffs.get(unit).size();
        }
        return (offs == iMaxOffs);
    }

    public void setMaxOffs(int pValue) {
        iMaxOffs = pValue;
    }

    public void setMinOffs(int pValue) {
        iMinOffs = pValue;
    }

    public int getMinOffs() {
        return iMinOffs;
    }
}
