/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import org.apache.commons.lang.math.LongRange;

/**
 *
 * @author Torridity
 */
public class DefenseTask {

    private Village target = null;
    private int necessaryDefenses = 0;
    private LongRange arriveTimeFrame = null;

    public DefenseTask(Village pTarget, int pDefenses, LongRange pTimeFrame) {
        target = pTarget;
        necessaryDefenses = pDefenses;
        arriveTimeFrame = pTimeFrame;
    }

    /**
     * @return the target
     */
    public Village getTarget() {
        return target;
    }

    /**
     * @return the necessaryDefenses
     */
    public int getNecessaryDefenses() {
        return necessaryDefenses;
    }

    /**
     * @return the arriveTimeFrame
     */
    public LongRange getArriveTimeFrame() {
        return arriveTimeFrame;
    }
}
