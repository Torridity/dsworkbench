/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Enoblement;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class AttackCalculator {

    public static Hashtable<Village, Hashtable<Village, UnitHolder>> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pMaxCleanPerSnob,
            Date pStartTime,
            Date pArriveTime,
            int pMinTimeBetweenAttacks,
            int pTimeFrameStartHour,
            int pTimeFrameEndHour,
            boolean pNightBlock,
            boolean pRandomize) {

        //get snob villages
        UnitHolder snobUnit = DataHolder.getSingleton().getUnitByPlainName("snob");
        List<Village> snobVillages = pSources.get(snobUnit);
        //get max possible number of enoblements
        int snobs = 0;
        for (Village snobVillage : snobVillages) {
            snobs += TroopsManager.getSingleton().getTroopsForVillage(snobVillage).getTroopsOfUnit(snobUnit);
        }
        int maxEnoblements = (int) Math.floor(snobs / 4);
        //build timeframe
        TimeFrame timeFrame = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
        Hashtable<Village, Enoblement> enoblements = new Hashtable<Village, Enoblement>();

        //assign snobs
        for (Village source : snobVillages) {
            //build target mappings ordered by distance
            List<DistanceMapping> snobMappings = buildSourceTargetsMapping(source, pTargets);

            for (DistanceMapping map : snobMappings) {
                long dur = (long) DSCalculator.calculateMoveTimeInSeconds(map.getSource(), map.getTarget(), snobUnit.getSpeed()) * 1000;
                Date send = new Date(pArriveTime.getTime() - dur);
                //check if needed snob can arrive village in time frame
                if (timeFrame.inside(send)) {
                    //ok, check enoblement
                    Enoblement e = enoblements.get(map.getTarget());
                    if (e != null) {
                        //enoblement for village available
                        if (e.snobDone()) {
                            //all snobs available
                            break;
                        } else {
                            e.addSnob(map.getSource());
                        }
                    } else {
                        if (enoblements.size() < maxEnoblements) {
                            e = new Enoblement(map.getTarget(), pMaxCleanPerSnob);
                            e.addSnob(map.getSource());
                            enoblements.put(map.getTarget(), e);
                            break;
                        }
                    }
                }
            }
        }
        //Result: List of Enoblements -> check them  if they are valid!
        //TODO: Try to assign Clean offs

        return null;
    }

    private static List<DistanceMapping> buildSourceTargetsMapping(Village pSource, List<Village> pTargets) {
        List<DistanceMapping> mappings = new LinkedList<DistanceMapping>();

        for (Village target : pTargets) {
            DistanceMapping mapping = new DistanceMapping(pSource, target);
            mappings.add(mapping);
        }
        Collections.sort(mappings);
        return mappings;
    }
}

class TimeFrame {

    private long start = 0;
    private long end = 0;
    private int minHour = 0;
    private int maxHour = 0;

    public TimeFrame(Date pStart, Date pEnd, int pMinHour, int pMaxHour) {
        start = pStart.getTime();
        end = pEnd.getTime();
        minHour = pMinHour;
        maxHour = pMaxHour;
    }

    public boolean inside(Date pDate) {
        long t = pDate.getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(pDate);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        if ((t > start) && (t < end)) {
            return ((hour >= minHour) && ((hour <= maxHour) && (minute <= 59) && (second <= 59)));
        }
        return false;
    }
}

class DistanceMapping implements Comparable<DistanceMapping> {

    private Village source = null;
    private Village target = null;
    private double distance = 0.0;

    public DistanceMapping(Village pSource, Village pTarget) {
        source = pSource;
        target = pTarget;
        distance = DSCalculator.calculateDistance(pSource, pTarget);
    }

    public Village getSource() {
        return source;
    }

    public Village getTarget() {
        return target;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(DistanceMapping o) {
        if (getDistance() < o.getDistance()) {
            return -1;
        } else if (getDistance() > o.getDistance()) {
            return 1;
        }
        return 0;
    }
}
