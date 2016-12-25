/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class Fake extends AbstractTroopMovement {

    public Fake(Village pTarget, int pMaxAttacks) {
        super(pTarget, 0, pMaxAttacks);
    }

    @Override
    public List<Attack> getAttacks(TimeFrame pTimeFrame, List<Long> pUsedSendTimes) {
        List<Attack> result = new LinkedList<>();
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
