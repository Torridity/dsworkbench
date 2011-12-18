/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.algo.TimeFrame;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class Off extends AbstractTroopMovement {

    public Off(Village pTarget, int pMaxAttacks) {
        super(pTarget, 0, pMaxAttacks);
    }

    @Override
    public List<Attack> getAttacks(TimeFrame pTimeFrame, List<Long> pUsedSendTimes) {
        List<Attack> result = new LinkedList<Attack>();
        Enumeration<UnitHolder> unitKeys = getOffs().keys();
        Village target = getTarget();
        int type = Attack.NO_TYPE;
        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            if (unit.getPlainName().equals("snob")) {
                type = Attack.SNOB_TYPE;
            } else if (unit.getPlainName().equals("ram") || (unit.getPlainName().equals("catapult"))) {
                type = Attack.CLEAN_TYPE;
            } else if (unit.getPlainName().equals("spear") || (unit.getPlainName().equals("sword")) || (unit.getPlainName().equals("archer")) || (unit.getPlainName().equals("heavy"))) {
                type = Attack.SUPPORT_TYPE;
            }

            List<Village> sources = getOffs().get(unit);
            for (Village offSource : sources) {
                Attack a = new Attack();
                a.setTarget(target);
                a.setSource(offSource);
                long runtime = Math.round(DSCalculator.calculateMoveTimeInSeconds(offSource, target, unit.getSpeed()) * 1000);
                Date fittedTime = pTimeFrame.getFittedArriveTime(runtime, getTarget(), pUsedSendTimes);
                if (fittedTime != null) {
                    a.setArriveTime(fittedTime);
                    a.setUnit(unit);
                    a.setType(type);
                    result.add(a);
                }
            }
        }

        /*    //sort result by start time
        Collections.sort(result, RUNTIME_SORT);
        //apply min distance calculation
        long last = 0;
        for (Attack a : result) {
        long startTime = a.getArriveTime().getTime() - (long) DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000;
        if (last != 0) {
        double diff = pTimeBetweenAttacks - Math.abs(startTime - last);
        if (diff < 0) {
        //diff is smaller than zero, so also smaller than min difference
        //so move the attack to future by this value
        a.setArriveTime(new Date(a.getArriveTime().getTime() - (long) diff));
        //correct the start time
        startTime = startTime - (long) diff;
        }
        }
        last = startTime;
        }*/
        return result;
    }
}
