/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.util.*;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Enoblement;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Village;
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
public class AllInOne extends AbstractAttackAlgorithm {

    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pCleanPerSnob,
            Date pStartTime,
            Date pArriveTime,
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

        //System.out.println("===GENERATING ENOBLEMENTS===");
        generateEnoblements(snobVillages, pTargets, timeFrame, finalEnoblements);
        /* System.out.println("===GENERATING ENOBLEMENTS FINISHED===");
        System.out.println("| RemainingSnobs: " + snobVillages.size());
        System.out.println("| Found Enoblements: " + finalEnoblements.size());
        System.out.println("|--------------------");
         */
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
            e.setMinOffs(pCleanPerSnob);
            e.setMaxOffs(pMaxAttacksPerVillage);
        }
        // System.out.println("===ASSIGNING OFFS TO ENOBLEMENTS===");
        assignOffsToEnoblements(finalEnoblements, offSources, timeFrame);
        /* System.out.println("===ASSIGNING OFFS TO ENOBLEMENTS FINISHED===");
        System.out.println("| Remaining Offs: " + offSources.size());
         */
        int fullyValid = 0;
        for (Enoblement e : finalEnoblements) {
            //System.out.println("| Enoblement:");
            if (e.offDone() && e.snobDone()) {
                fullyValid++;
                double maxDist = 0;
                for (Village v : e.getSnobSources()) {
                    double dist = DSCalculator.calculateDistance(v, e.getTarget());
                    if (dist > maxDist) {
                        maxDist = dist;
                    }
                }
                List<Village> ramVillages = e.getOffs().get(ramUnit);
                if (ramVillages != null) {
                    for (Village v : ramVillages) {
                        double dist = DSCalculator.calculateDistance(v, e.getTarget());
                        if (dist > maxDist) {
                            maxDist = dist;
                        }
                    }
                    //  System.out.println("|  * MaxDist: " + maxDist);

                    double minDist = Double.MAX_VALUE;
                    for (Village v : e.getSnobSources()) {
                        double dist = DSCalculator.calculateDistance(v, e.getTarget());
                        if (dist < minDist) {
                            minDist = dist;
                        }
                    }
                    for (Village v : ramVillages) {
                        double dist = DSCalculator.calculateDistance(v, e.getTarget());
                        if (dist < minDist) {
                            minDist = dist;
                        }
                    }
                /*   System.out.println("|  * MinDist: " + minDist);
                System.out.println("|  * Delta: " + (maxDist - minDist));*/
                } /*else {
            System.out.println("| No ram sources found");
            }*/
            }
        }

        setValidEnoblements(fullyValid);
        /*System.out.println("| Fully Valid: " + fullyValid);
        System.out.println("|----------------");
        System.out.println("===ASSIGNING REMAINING OFFS===");*/
        List<Fake> pFinalFakes = new LinkedList<Fake>();
        assignOffs(pFinalFakes, offSources, pTargets, timeFrame, pMaxAttacksPerVillage);
        //System.out.println("===ASSIGNING REMAINING OFFS FINISHED===");
        //System.out.println("| Fakes: " + pFinalFakes.size());
        int fullFakes = 0;
        for (Fake f : pFinalFakes) {
            if (f.offComplete()) {
                fullFakes++;
            }
        }
        setFullOffs(fullFakes);

        List<AbstractTroopMovement> movements = new LinkedList<AbstractTroopMovement>();

        for (Enoblement e : finalEnoblements) {
            movements.add(e);
        }

        for (Fake f : pFinalFakes) {
            movements.add(f);
        }

        return movements;
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
                Enoblement e = new Enoblement(target, 0, 0);
                for (int j = 0; j < 4; j++) {
                    Village snobVillage = snobMappings.get(j).getTarget();
                    e.addSnob(snobVillage);
                }
                tmpEno.add(e);
            }
        }

        if (tmpEno.size() == 0) {
            return;
        }

        Collections.sort(tmpEno, Enoblement.DISTANCE_SORTER);
        //remove first element and recalculate
        Enoblement e = tmpEno.get(0);
        pFinalEnoblements.add(e);
        pTargets.remove(e.getTarget());
        for (Village source : e.getSnobSources()) {
            pSnobSources.remove(source);
        }
        generateEnoblements(pSnobSources, pTargets, pTimeFrame, pFinalEnoblements);
    }

    private static void assignOffsToEnoblements(List<Enoblement> pInOutEnoblements, List<Village> pOffSources, TimeFrame pTimeFrame) {

        Hashtable<Enoblement, List<DistanceMapping>> tmpMappings = new Hashtable<Enoblement, List<DistanceMapping>>();
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        for (Enoblement enoblement : pInOutEnoblements) {
            //use enoblement if offs where not assigned yet
            if (!enoblement.offDone()) {
                //calculate snob distances for current enoblement target village
                List<DistanceMapping> offMappings = buildSourceTargetsMapping(enoblement.getTarget(), pOffSources);

                //expect target to be valid
                boolean valid = true;
                //check distances for first (fastest) possible off

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

        if (tmpMappings.size() == 0) {
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
        List<DistanceMapping> offMappings = tmpMappings.get(best);

        for (DistanceMapping offMapping : offMappings) {
            //use target due to the distance was calculated based on the enoblements target
            Village off = offMapping.getTarget();
            best.addCleanOff(ram, off);
            pOffSources.remove(off);
        }

        assignOffsToEnoblements(pInOutEnoblements, pOffSources, pTimeFrame);
    }

    private static void assignOffs(List<Fake> pFakes, List<Village> pOffSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacks) {
        //table which holds for every target the distance of each source
        Hashtable<Village, List<DistanceMapping>> tmpMappings = new Hashtable<Village, List<DistanceMapping>>();
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        for (Village target : pTargets) {
            //calculate snob distances for current enoblement target village
            List<DistanceMapping> offMappings = buildSourceTargetsMapping(target, pOffSources);

            //check distances for first (fastest) possible off

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

        if (tmpMappings.size() == 0) {
            //no off could be assigned in time frame
            return;
        }

        double minDist = 0;
        Village best = null;
        Enumeration<Village> keys = tmpMappings.keys();
        //find the enoblement for which the worst off has the smallest runtime
        while (keys.hasMoreElements()) {
            //get next off source
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
        List<DistanceMapping> offMappings = tmpMappings.get(best);
        Fake f = null;


        //try to find existing fake
        for (Fake fake : pFakes) {
            if (fake.getTarget().equals(best)) {
                f = fake;
                break;
            }
        }

        //no existing fake found
        if (f == null) {
            f = new Fake(best, pMaxAttacks);
            pFakes.add(f);
        }
        for (DistanceMapping offMapping : offMappings) {
            //use target due to the distance was calculated based on the enoblements target
            if (!f.offComplete()) {
                f.addOff(ram, offMapping.getTarget());
                pOffSources.remove(offMapping.getTarget());
            }
        }
        if (f.offComplete()) {
            //remove target only if max number was reached
            pTargets.remove(best);
        }

        assignOffs(pFakes, pOffSources, pTargets, pTimeFrame, pMaxAttacks);
    }
}

