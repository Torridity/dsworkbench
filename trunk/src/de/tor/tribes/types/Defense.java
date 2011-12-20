/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.DSCalculator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class Defense {

    private DefenseElement defense = null;
    private List<Village> supporters = null;
    private UnitHolder unit = null;

    public Defense(DefenseElement pElement, UnitHolder pUnit) {
        defense = pElement;
        unit = pUnit;
        supporters = new LinkedList<Village>();
    }

    public boolean addSupport(Village pSource, boolean pPrimary, boolean pMultiUse) {
        long runtime = Math.round(DSCalculator.calculateDistance(pSource, defense.getTarget()) * unit.getSpeed()) * 1000l;
        if (defense.getFirstAttack().getTime() - runtime > System.currentTimeMillis()) {
            //high priority
            if (pMultiUse || !supporters.contains(pSource)) {
                supporters.add(pSource);
                return true;
            }
        } else if (defense.getLastAttack().getTime() - runtime > System.currentTimeMillis()) {
            //low priority
            if (!pPrimary) {
                if (pMultiUse || !supporters.contains(pSource)) {
                    supporters.add(pSource);
                    return true;
                }
            }
        } else if (defense.getLastAttack().getTime() - runtime < System.currentTimeMillis()) {
            //impossible
        }
        return false;
    }

    public boolean isFinished() {
        return supporters.size() == defense.getNeededSupports();
    }

    public Village getTarget() {
        return defense.getTarget();
    }

    public int getSupports() {
        return supporters.size();
    }

    public int getNeededSupports() {
        return defense.getNeededSupports();
    }
}
