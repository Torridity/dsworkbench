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
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.util.algo.types.TimeFrame;

/**
 *
 * @author Torridity
 */
public class Enoblement extends AbstractTroopMovement {

    private List<Village> snobSources = null;
    public static final Comparator<Enoblement> DISTANCE_SORTER = new SnobDistanceSort();

    public Enoblement(Village pTarget, int pCleanOffs, int pMaxOffs) {
        super(pTarget, pCleanOffs, pMaxOffs);
        snobSources = new LinkedList<Village>();
    }

    public int getNumberOfCleanOffs() {
        return getMinOffs();
    }

    public void addCleanOff(UnitHolder pUnit, Village pSource) {
        addOff(pUnit, pSource);
    }

    public void addSnob(Village pSource) {
        snobSources.add(pSource);
    }

    public boolean snobDone(boolean pUser5Snobs) {
        if (pUser5Snobs) {
            return (snobSources.size() == 5);
        }
        return (snobSources.size() == 4);

    }

    public boolean offDone() {
        return offValid();
    }

    public List<Village> getSnobSources() {
        return snobSources;
    }

    @Override
    public List<Attack> getAttacks(TimeFrame pTimeFrame, List<Long> pUsedSendTimes) {
        List<Attack> result = new LinkedList<Attack>();
        Enumeration<UnitHolder> unitKeys = getOffs().keys();
        Village target = getTarget();
        int type = Attack.CLEAN_TYPE;
        UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
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
                /*

                if (!pTimeFrame.isVariableArriveTime()) {
                a.setArriveTime(new Date(pTimeFrame.getEnd()));
                } else {
                long runtime = Math.round(DSCalculator.calculateMoveTimeInSeconds(offSource, target, unit.getSpeed()) * 1000);
                a.setArriveTime(pTimeFrame.getRandomArriveTime(runtime, offSource.getTribe(), new LinkedList<Long>()));
                }*/

            }
        }
        type = Attack.SNOB_TYPE;
        for (Village snobSource : getSnobSources()) {
            Attack a = new Attack();
            a.setTarget(target);
            a.setSource(snobSource);
            long runtime = Math.round(DSCalculator.calculateMoveTimeInSeconds(snobSource, target, snob.getSpeed()) * 1000);

            Date fittedTime = pTimeFrame.getFittedArriveTime(runtime, snobSource, pUsedSendTimes);
            if (fittedTime != null) {
                a.setArriveTime(fittedTime);
                a.setUnit(snob);
                a.setType(type);
                result.add(a);
            }

            /*if (!pTimeFrame.isVariableArriveTime()) {
            a.setArriveTime(new Date(pTimeFrame.getEnd()));
            } else {
            long runtime = Math.round(DSCalculator.calculateMoveTimeInSeconds(snobSource, target, snob.getSpeed()) * 1000);
            a.setArriveTime(pTimeFrame.getRandomArriveTime(runtime, snobSource.getTribe(), new LinkedList<Long>()));
            }*/
        }
        return result;
    }

    private static class SnobDistanceSort implements Comparator<Enoblement>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Enoblement e1, Enoblement e2) {
            int lastIndex1 = e1.getSnobSources().size() - 1;
            int lastIndex2 = e1.getSnobSources().size() - 1;

            double d1 = DSCalculator.calculateDistance(e1.getSnobSources().get(lastIndex1), e1.getTarget());
            double d2 = DSCalculator.calculateDistance(e2.getSnobSources().get(lastIndex2), e2.getTarget());
            return Double.compare(d1, d2);
        }
    }
}
