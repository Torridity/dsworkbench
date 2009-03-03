/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.util.*;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Enoblement;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Village;
import java.awt.Point;
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
        if (snobVillages == null) {
            snobVillages = new LinkedList<Village>();
        }

        //build timeframe
        TimeFrame timeFrame = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
        List<Enoblement> finalEnoblements = new LinkedList<Enoblement>();
        //generate enoblements with minimum runtime

        generateEnoblements(snobVillages, pTargets, timeFrame, finalEnoblements);

        System.out.println("RemainingSnobs: " + snobVillages.size());
        System.out.println("Found Enoblements: " + finalEnoblements.size());

        // <editor-fold defaultstate="collapsed" desc="Get off villages">
        UnitHolder ramUnit = DataHolder.getSingleton().getUnitByPlainName("ram");
        UnitHolder cataUnit = DataHolder.getSingleton().getUnitByPlainName("catapult");
        List<Village> offSources = new LinkedList<Village>();
        List<Village> ramSources = pSources.get(ramUnit);
        if (ramSources != null) {
            for (Village ramSource : ramSources) {
                offSources.add(ramSource);
            }
        }
        List<Village> cataSources = pSources.get(cataUnit);
        if (cataSources != null) {
            for (Village cataSource : cataSources) {
                offSources.add(cataSource);
            }
        }
        //</editor-fold>

        //set desired number of clean offs
        for (Enoblement e : finalEnoblements) {
            e.setNumberOfCleanOffs(pMaxCleanPerSnob);
        }

        assignOffsToEnoblements(finalEnoblements, offSources, timeFrame);

        System.out.println("Remaining Offs: " + offSources.size());

        int fullyValid = 0;
        for (Enoblement e : finalEnoblements) {
            System.out.println("Enoblement:");
            if (e.offDone() && e.snobDone()) {
                fullyValid++;
                double maxDist = 0;
                for (Village v : e.getSnobSources()) {
                    double dist = DSCalculator.calculateDistance(v, e.getTarget());
                    if (dist > maxDist) {
                        maxDist = dist;
                    }
                }
                for (Village v : e.getCleanSources()) {
                    double dist = DSCalculator.calculateDistance(v, e.getTarget());
                    if (dist > maxDist) {
                        maxDist = dist;
                    }
                }
                System.out.println(" * MaxDist: " + maxDist);

                double minDist = Double.MAX_VALUE;
                for (Village v : e.getSnobSources()) {
                    double dist = DSCalculator.calculateDistance(v, e.getTarget());
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                for (Village v : e.getCleanSources()) {
                    double dist = DSCalculator.calculateDistance(v, e.getTarget());
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                System.out.println(" * MinDist: " + minDist);
                System.out.println(" * Delta: " + (maxDist - minDist));
            }
        }

        System.out.println("Fully Valid: " + fullyValid);

        System.out.println("Assigning remaining offs");
        List<Fake> pFinalFakes = new LinkedList<Fake>();
        assignOffs(pFinalFakes, offSources, pTargets, timeFrame, pMaxAttacksPerVillage);
        System.out.println("Fakes: " + pFinalFakes.size());
        int fullFakes = 0;
        for (Fake f : pFinalFakes) {
            if (f.getOffSources().size() == pMaxAttacksPerVillage) {
                fullFakes++;
            }
        }
        System.out.println("Full Fakes: " + fullFakes);
        /*for (Enoblement e : finalEnoblements) {
        System.out.println("MaxDist: " + DSCalculator.calculateDistance(e.getSnobSources().get(3), e.getTarget()));
        }*/

        return null;
    }

    private static void generateEnoblements(List<Village> pSnobSources, List<Village> pTargets, TimeFrame pTimeFrame, List<Enoblement> pFinalEnoblements) {
        List<Enoblement> tmpEno = new LinkedList<Enoblement>();

        for (Village target : pTargets) {
            //calculate snob distances for current target village
            List<DistanceMapping> snobMappings = buildSourceTargetsMapping(target, pSnobSources);

            //expect target to be valid
            boolean valid = true;
            //check distances for first (fastest) possible snobs
            UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
            if (snobMappings.size() > 3) {
                //at least 4 snobs left
                for (int i = 0; i < 4; i++) {
                    long dur = (long) (snobMappings.get(i).getDistance() * snob.getSpeed() * 60000.0);
                    Date send = new Date(pTimeFrame.getEnd() - dur);
                    //check if needed snob can arrive village in time frame
                    if (!pTimeFrame.inside(send)) {
                        //break if at least one of the fastest is not in time
                        valid = false;
                        break;
                    }
                }
            } else {
                //not enough snobs left
                valid = false;
            }
            //if all snob villages are in time, create enoblement
            if (valid) {
                //add new temp enoblement
                Enoblement e = new Enoblement(target, 0);
                for (int j = 0; j < 4; j++) {
                    Village snobVillage = snobMappings.get(j).getTarget();
                    e.addSnob(snobVillage);
                }
                tmpEno.add(e);
            }
        }

        System.out.println(" * Possible Enoblements: " + tmpEno.size());
        if (tmpEno.size() == 0) {
            System.out.println("---Finished Enoblement---");
            return;
        }

        Collections.sort(tmpEno, Enoblement.DISTANCE_SORTER);
        //remove first element and recalculate
        Enoblement e = tmpEno.get(0);
        System.out.println(" * Dist " + DSCalculator.calculateDistance(e.getSnobSources().get(3), e.getTarget()));
        pFinalEnoblements.add(e);
        pTargets.remove(e.getTarget());
        for (Village source : e.getSnobSources()) {
            pSnobSources.remove(source);
        }
        System.out.println("New Enoblement-Iteration");
        generateEnoblements(pSnobSources, pTargets, pTimeFrame, pFinalEnoblements);
    }

    private static void assignOffsToEnoblements(List<Enoblement> pInOutEnoblements, List<Village> pOffSources, TimeFrame pTimeFrame) {

        Hashtable<Enoblement, List<DistanceMapping>> tmpMappings = new Hashtable<Enoblement, List<DistanceMapping>>();
        for (Enoblement enoblement : pInOutEnoblements) {
            //use enoblement if offs where not assigned yet
            if (!enoblement.offDone()) {
                //calculate snob distances for current enoblement target village
                List<DistanceMapping> offMappings = buildSourceTargetsMapping(enoblement.getTarget(), pOffSources);

                //expect target to be valid
                boolean valid = true;
                //check distances for first (fastest) possible off
                UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
                if (pOffSources.size() > enoblement.getNumberOfCleanOffs()) {
                    //at least 4 snobs left
                    for (int i = 0; i <= enoblement.getNumberOfCleanOffs(); i++) {
                        long dur = (long) (offMappings.get(i).getDistance() * ram.getSpeed() * 60000.0);
                        Date send = new Date(pTimeFrame.getEnd() - dur);
                        //check if needed off can arrive village in time frame
                        if (!pTimeFrame.inside(send)) {
                            //break if at least one of the fastest is not in time
                            valid = false;
                            break;
                        }
                    }
                } else {
                    //not enough offs left
                    valid = false;
                }
                //if all off villages are in time, create enoblement
                if (valid) {
                    //add new temp off mapping list
                    List<DistanceMapping> tmpDistances = new LinkedList<DistanceMapping>();
                    for (int j = 0; j < enoblement.getNumberOfCleanOffs(); j++) {
                        tmpDistances.add(offMappings.get(j));
                    }
                    tmpMappings.put(enoblement, tmpDistances);
                }
            }
        }

        System.out.println(" * Remaining Enoblements: " + tmpMappings.size());
        if (tmpMappings.size() == 0) {
            System.out.println("---Finished OffMapping---");
            return;
        }

        double minDist = 0;
        Enoblement best = null;
        Enumeration<Enoblement> keys = tmpMappings.keys();
        //find the enoblement for which the worst off has the smallest runtime
        while (keys.hasMoreElements()) {
            //get next enoblement
            Enoblement e = keys.nextElement();
            if (best == null) {
                //no best set yet, so take the first element to initialize
                best = e;
                //use the slowest off to calculate the worst case
                minDist = tmpMappings.get(e).get(e.getNumberOfCleanOffs() - 1).getDistance();
            } else {
                //use the slowest off to calculate the worst case
                double dist = tmpMappings.get(e).get(e.getNumberOfCleanOffs() - 1).getDistance();
                if (dist < minDist) {
                    best = e;
                    minDist = dist;
                }
            }
        }
        System.out.println(" *BestDistance: " + minDist);
        List<DistanceMapping> offMappings = tmpMappings.get(best);

        for (DistanceMapping offMapping : offMappings) {
            //use target due to the distance was calculated based on the enoblements target
            Village off = offMapping.getTarget();
            best.addCleanOff(off);
            pOffSources.remove(off);
        }

        System.out.println("New Off-Iteration");
        assignOffsToEnoblements(pInOutEnoblements, pOffSources, pTimeFrame);
    }

    private static void assignOffs(List<Fake> pFakes, List<Village> pOffSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacks) {
        Hashtable<Village, List<DistanceMapping>> tmpMappings = new Hashtable<Village, List<DistanceMapping>>();
        System.out.println(" * Remainging offs: " + pOffSources.size());
        for (Village target : pTargets) {
            //calculate snob distances for current enoblement target village
            List<DistanceMapping> offMappings = buildSourceTargetsMapping(target, pOffSources);

            //check distances for first (fastest) possible off
            UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
            //temp map for valid distances
            List<DistanceMapping> tmpMap = new LinkedList<DistanceMapping>();
            for (DistanceMapping mapping : offMappings) {
                long dur = (long) (mapping.getDistance() * ram.getSpeed() * 60000.0);
                Date send = new Date(pTimeFrame.getEnd() - dur);
                //check if needed off can arrive village in time frame
                if (!pTimeFrame.inside(send) || (tmpMap.size() == pMaxAttacks)) {
                    //break if at least one is not in time or max number of attacks was reached
                    break;
                } else {
                    //add valid distance to map
                    tmpMap.add(mapping);
                }
            }
            //if all off villages are in time, create enoblement
            if (tmpMap.size() > 0) {
                tmpMappings.put(target, tmpMap);
            }

        }

        System.out.println(" * Remaining Mappings: " + tmpMappings.size());
        if (tmpMappings.size() == 0) {
            System.out.println("---Finished FinalOffMapping---");
            return;
        }

        double minDist = 0;
        Village best = null;
        Enumeration<Village> keys = tmpMappings.keys();
        //find the enoblement for which the worst off has the smallest runtime
        while (keys.hasMoreElements()) {
            //get next enoblement
            Village e = keys.nextElement();
            if (best == null) {
                //no best set yet, so take the first element to initialize
                best = e;
                //use the slowest off to calculate the worst case
                List<DistanceMapping> mappings = tmpMappings.get(e);
                minDist = mappings.get(mappings.size() - 1).getDistance();
            } else {
                //use the slowest off to calculate the worst case
                List<DistanceMapping> mappings = tmpMappings.get(e);
                double dist = mappings.get(mappings.size() - 1).getDistance();
                if (dist < minDist) {
                    best = e;
                    minDist = dist;
                }
            }
        }
        System.out.println(" *BestDistance: " + minDist);
        List<DistanceMapping> offMappings = tmpMappings.get(best);
        Fake f = new Fake(best, pMaxAttacks);
        if (offMappings.size() == pMaxAttacks) {
            //remove target only if max number was reached
            pTargets.remove(best);
        }
        for (DistanceMapping offMapping : offMappings) {
            //use target due to the distance was calculated based on the enoblements target
            f.addOff(offMapping.getTarget());
            pOffSources.remove(offMapping.getTarget());
        }
        pFakes.add(f);
        System.out.println("New FinalOff-Iteration");
        assignOffs(pFakes, pOffSources, pTargets, pTimeFrame, pMaxAttacks);
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

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
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
