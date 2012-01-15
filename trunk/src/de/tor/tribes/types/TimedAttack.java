/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import java.util.Date;

/**
 *
 * @author Torridity
 */
public class TimedAttack {

    private Village mSource = null;
    private UnitHolder unit = null;
    private long lArriveTime = 0;
    private boolean possibleFake = false;
    private boolean possibleSnob = false;

    public TimedAttack(Village pSource, Date pArriveTime) {
        mSource = pSource;
        lArriveTime = pArriveTime.getTime();
    }

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    /**
     * @return the mSource
     */
    public Village getSource() {
        return mSource;
    }

    /**
     * @param mSource the mSource to set
     */
    public void setSource(Village mSource) {
        this.mSource = mSource;
    }

    /**
     * @return the lArriveTime
     */
    public Long getlArriveTime() {
        return lArriveTime;
    }

    /**
     * @param lArriveTime the lArriveTime to set
     */
    public void setlArriveTime(long lArriveTime) {
        this.lArriveTime = lArriveTime;
    }

    /**
     * @return the possibleFake
     */
    public boolean isPossibleFake() {
        return possibleFake;
    }

    /**
     * @param possibleFake the possibleFake to set
     */
    public void setPossibleFake(boolean possibleFake) {
        this.possibleFake = possibleFake;
    }

    /**
     * @return the possibleSnob
     */
    public boolean isPossibleSnob() {
        return possibleSnob;
    }

    /**
     * @param possibleSnob the possibleSnob to set
     */
    public void setPossibleSnob(boolean possibleSnob) {
        this.possibleSnob = possibleSnob;
    }
}
