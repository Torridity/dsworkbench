/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.DSCalculator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class Defense {

    private DefenseInformation parent = null;
    private Village supporter = null;
    private UnitHolder unit = null;
    private boolean transferredToBrowser = false;

    public Defense(DefenseInformation pParent, Village pSupporter, UnitHolder pUnit) {
        parent = pParent;
        supporter = pSupporter;
        unit = pUnit;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public Village getSupporter() {
        return supporter;
    }

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    public void setTransferredToBrowser(boolean transferredToBrowser) {
        this.transferredToBrowser = transferredToBrowser;
    }

    public boolean isTransferredToBrowser() {
        return transferredToBrowser;
    }

    public int getSupports() {
        return parent.getSupports().length;
    }

    public int getNeededSupports() {
        return parent.getNeededSupports();
    }

    public Village getTarget() {
        return parent.getTarget();
    }

    public long getBestSendTime() {
        long first = parent.getFirstAttack().getTime();
        long moveTime = DSCalculator.calculateMoveTimeInMillis(supporter, parent.getTarget(), unit.getSpeed());
        return first - moveTime;

    }

    public long getWorstSendTime() {
        long first = parent.getLastAttack().getTime();
        long moveTime = DSCalculator.calculateMoveTimeInMillis(supporter, parent.getTarget(), unit.getSpeed());
        return first - moveTime;

    }
}
