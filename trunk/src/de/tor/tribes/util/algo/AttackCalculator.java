/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.util.*;
import de.tor.tribes.io.DataHolder;
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
import java.util.TreeSet;

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

        //get max possible number of enoblements
        int maxEnoblements = (int) Math.floor(snobVillages.size() / 4);
        //build timeframe
        TimeFrame timeFrame = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
        Hashtable<Village, Enoblement> enoblements = new Hashtable<Village, Enoblement>();


        List<DistanceMapping> mappingList = new LinkedList<DistanceMapping>();
        List<Enoblement> tmpEno = new LinkedList<Enoblement>();

        for (Village target : pTargets) {
            List<DistanceMapping> snobMappings = buildSourceTargetsMapping(target, snobVillages);
            int availSnobs = 0;
            for (DistanceMapping mapping : snobMappings) {
                long dur = (long) (mapping.getDistance() * snobUnit.getSpeed() * 60000.0);
                Date send = new Date(pArriveTime.getTime() - dur);
                //check if needed snob can arrive village in time frame
                if (timeFrame.inside(send)) {
                    availSnobs++;
                    if (availSnobs == 4) {
                        //add new enoblement
                        Enoblement e = new Enoblement(target, pMaxCleanPerSnob);
                        for (int i = 0; i < 4; i++) {
                            e.addSnob(snobMappings.get(i).getTarget());
                        }
                        tmpEno.add(e);
                        break;
                    }
                }
            }
        }


        Collections.sort(tmpEno, Enoblement.DISTANCE_SORTER);
        List<Enoblement> taken = new LinkedList<Enoblement>();
        for (Enoblement e : tmpEno) {
            boolean take = true;
            List<Village> enoSnobVillages = e.getSnobSources();
            for (Enoblement t : taken) {
                List<Village> tSnobVillages = t.getSnobSources();
                for (Village v : tSnobVillages) {
                    if (enoSnobVillages.contains(v)) {
                        //dont take
                        take = false;
                        break;
                    }
                }
                if (!take) {
                    break;
                }
            }
            if (take) {
                taken.add(e);
            }
            if (taken.size() == maxEnoblements) {
                break;
            }
        }

        Collections.sort(taken, Enoblement.DISTANCE_SORTER);
        for (Enoblement e : taken) {
            System.out.println(DSCalculator.calculateDistance(e.getSnobSources().get(0), e.getSnobSources().get(3)));
        }

        /*

        // <editor-fold defaultstate="collapsed" desc="Assign possible enoblements">
        //assign snobs
        for (Village source : snobVillages) {
        //build target mappings ordered by distance
        List<DistanceMapping> snobMappings = buildSourceTargetsMapping(source, pTargets);
        boolean targetFound = false;
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
        System.out.println(" *SnobDone");
        //all snobs availablet
        break;
        } else {
        System.out.println(" *Add Snob");
        e.addSnob(source);
        targetFound = true;
        break;
        }
        }
        }
        }
        //only add new enoblement if absolutely no target was found
        if (!targetFound) {
        if (enoblements.size() < maxEnoblements) {
        System.out.println(" *New enoblement");
        Village target = snobMappings.get(0).getTarget();
        Enoblement e = new Enoblement(target, pMaxCleanPerSnob);
        e.addSnob(source);
        enoblements.put(target, e);
        }
        }
        System.out.println(" * OutsideFor");
        }

        //</editor-fold>

        //Result: List of Enoblements -> check them  if they are valid!


        // <editor-fold defaultstate="collapsed" desc="Get possible Off sources (ram + cata)">
        UnitHolder ramUnit = DataHolder.getSingleton().getUnitByPlainName("ram");
        UnitHolder cataUnit = DataHolder.getSingleton().getUnitByPlainName("catapult");

        List<Village> ramSources = pSources.get(ramUnit);
        if (ramSources == null) {
        ramSources = new LinkedList<Village>();
        }
        List<Village> cataSources = pSources.get(cataUnit);
        if (cataSources == null) {
        cataSources = new LinkedList<Village>();
        }
        //source villages for offs
        List<Village> offSources = new LinkedList<Village>();
        for (Village v : ramSources) {
        offSources.add(v);
        }
        for (Village v : cataSources) {
        offSources.add(v);
        }
        System.out.println("PossibleOffs: " + offSources.size());
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Get fake villages (not enobled targets)">
        List<Village> fakeVillages = pTargets;
        Enumeration<Village> toEnoble = enoblements.keys();
        System.out.println("Planned enoblements: " + enoblements.size());
        //remove enoble villages from fake list
        while (toEnoble.hasMoreElements()) {
        fakeVillages.remove(toEnoble.nextElement());
        }
        System.out.println("Planed fakes: " + fakeVillages.size());
        //</editor-fold>

        Hashtable<Village, Fake> fakes = new Hashtable<Village, Fake>();
        for (Village v : fakeVillages) {
        fakes.put(v, new Fake(v, pMaxAttacksPerVillage));
        }

        assignOffs(enoblements, offSources, fakes, pArriveTime, timeFrame, ramUnit.getSpeed());

        List<Village> attacked = new LinkedList<Village>();
        toEnoble = enoblements.keys();
        int validEnoblements = 0;
        while (toEnoble.hasMoreElements()) {
        System.out.println("Enoblement: ");
        Enoblement e = enoblements.get(toEnoble.nextElement());
        if (!attacked.contains(e.getTarget())) {
        attacked.add(e.getTarget());
        }

        for (Village v : e.getSnobSources()) {
        System.out.println(" - " + v);
        }

        if (e.snobDone() && e.offDone()) {
        //not enough snobs
        validEnoblements++;
        }
        }

        Enumeration<Village> fakeKeys = fakes.keys();
        int validFakes = 0;
        while (fakeKeys.hasMoreElements()) {
        Fake f = fakes.get(fakeKeys.nextElement());
        if (!attacked.contains(f.getTarget())) {
        attacked.add(f.getTarget());
        }
        int offs = f.getOffSources().size();
        //not enough snobs
        validFakes += offs;
        }


        System.out.println("Attacked Villages: " + attacked.size());
        System.out.println("Valid Enoblements: " + validEnoblements);
        System.out.println("Fakes: " + validFakes);
        System.out.println("Not assigned: " + offSources.size());
         */ return null;
    }

    private static void assignOffs(Hashtable<Village, Enoblement> pEnoblements, List<Village> pOffSources, Hashtable<Village, Fake> pFakeTargets, Date pArriveTime, TimeFrame pTimeFrame, double pSpeed) {
        /*if (pOffSources.isEmpty()) {
        return;
        }*/
        System.out.println("*ASSIGN ITERATION");
        Enumeration<Village> enobleKeys = pEnoblements.keys();
        UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        boolean assigned = false;
        while (enobleKeys.hasMoreElements()) {
            Village enobleKey = enobleKeys.nextElement();

            Enoblement enoblement = pEnoblements.get(enobleKey);
            System.out.println(" * Check Enoblement: " + enobleKey + ", AGs " + enoblement.getSnobSources().size());
            if (!enoblement.offDone()) {
                //build mapping between enobled village and off list
                List<DistanceMapping> mappings = buildSourceTargetsMapping(enobleKey, pOffSources);
                for (DistanceMapping mapping : mappings) {
                    //check if source reaches target in time
                    long dur = (long) DSCalculator.calculateMoveTimeInSeconds(mapping.getSource(), mapping.getTarget(), snob.getSpeed()) * 1000;
                    Date send = new Date(pArriveTime.getTime() - dur);
                    if (pTimeFrame.inside(send)) {
                        System.out.println(" - Enobelment assigned");
                        //send time is in time frame
                        enoblement.addCleanOff(mapping.getTarget());
                        //remove off from list of available offs
                        pOffSources.remove(mapping.getTarget());
                        assigned =
                                true;
                        break;

                    } else {
                        System.out.println("Not in time frame");
                    }

                }
            }
            if (assigned) {
                break;
            }

        }
        System.out.println("*Enoblements checked");
        if (!assigned) {
            // System.out.println("No enoblement assigned");
            //no off source could be used for enoblement, try to use one as fake
            Enumeration<Village> fakeKeys = pFakeTargets.keys();
            while (fakeKeys.hasMoreElements()) {
                Village fakeKey = fakeKeys.nextElement();
                Fake fake = pFakeTargets.get(fakeKey);
                //check if max number of offs was reached
                if (!fake.offDone()) {
                    //build distance map between fake source and target
                    List<DistanceMapping> mappings = buildSourceTargetsMapping(fakeKey, pOffSources);
                    //check all villages if fake could reach it
                    for (DistanceMapping mapping : mappings) {
                        long dur = (long) DSCalculator.calculateMoveTimeInSeconds(mapping.getSource(), mapping.getTarget(), ram.getSpeed()) * 1000;
                        Date send = new Date(pArriveTime.getTime() - dur);
                        //check if time is in time frame
                        if (pTimeFrame.inside(send)) {
                            //System.out.println(" - Fake assigned");
                            fake.addOff(mapping.getTarget());
                            pOffSources.remove(mapping.getTarget());
                            assigned =
                                    true;
                            break;

                        }


                    }
                }
                if (assigned) {
                    break;
                }

            }
        }
        //check if next iteration is possible
        if (assigned) {
            //offSources still change, so iterate again
            assignOffs(pEnoblements, pOffSources, pFakeTargets, pArriveTime, pTimeFrame, pSpeed);
        }

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
