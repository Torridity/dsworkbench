/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class Fake extends AbstractTroopMovement {

    public Fake(Village pTarget, int pMaxAttacks) {
        super(pTarget, 0, pMaxAttacks);
    }

    @Override
    public List<Attack> getAttacks(TimeFrame pTimeFrame, List<Long> pUsedSendTimes) {
        List<Attack> result = new LinkedList<Attack>();
        Enumeration<UnitHolder> unitKeys = getOffs().keys();
        Village target = getTarget();
        int type = Attack.FAKE_TYPE;
        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> sources = getOffs().get(unit);
            for (Village offSource : sources) {
                Attack a = new Attack();
                a.setTarget(target);
                a.setSource(offSource);
                long runtime = Math.round(DSCalculator.calculateMoveTimeInSeconds(offSource, target, unit.getSpeed()) * 1000);
                Date fittedTime = pTimeFrame.getFittedArriveTime(runtime, offSource, pUsedSendTimes);
                if (fittedTime != null) {
                    a.setArriveTime(fittedTime);
                    a.setUnit(unit);
                    a.setType(type);
                    result.add(a);
                }
            }
        }

        return result;
    }
}
