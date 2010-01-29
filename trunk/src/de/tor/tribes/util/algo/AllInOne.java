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
import de.tor.tribes.types.Off;
import de.tor.tribes.types.Village;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class AllInOne{// extends AbstractAttackAlgorithm {

    private static Logger logger = Logger.getLogger("Algorithm_AllInOne");
    private List<Village> notAssignedSources = null;

    public List<Village> getNotAssignedSources() {
        return notAssignedSources;
    }
/*
    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pCleanPerSnob,
            TimeFrame pTimeFrame,
            boolean pRandomize,
            boolean pUse5Snobs) {

        //get snob villages
        notAssignedSources = new LinkedList<Village>();
        logger.debug("Getting snob sources");
        UnitHolder snobUnit = DataHolder.getSingleton().getUnitByPlainName("snob");
        List<Village> snobVillages = pSources.get(snobUnit);
        if (snobVillages == null) {
            snobVillages = new LinkedList<Village>();
        }

        //build timeframe
        // TimeFrame timeFrame = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
        List<Enoblement> finalEnoblements = new LinkedList<Enoblement>();

        //generate enoblements with minimum runtime
        logger.debug("Generating enoblements");
        generateEnoblements(snobVillages, pTargets, pTimeFrame, finalEnoblements, pUse5Snobs);
        logger.debug("Getting off sources");
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
        List<Village> fakeSources = new LinkedList<Village>();
        ramSources = pFakes.get(ramUnit);
        if (ramSources != null) {
            for (Village ramSource : ramSources) {
                fakeSources.add(ramSource);
            }
        }
        cataSources = pFakes.get(cataUnit);
        if (cataSources != null) {
            for (Village cataSource : cataSources) {
                fakeSources.add(cataSource);
            }
        }
        //</editor-fold>

        //set desired number of clean offs
        for (Enoblement e : finalEnoblements) {
            e.setMinOffs(pCleanPerSnob);
            e.setMaxOffs(pMaxAttacksPerVillage);
        }
        logger.debug("Assigning offs to enoblements");
        assignOffsToEnoblements(finalEnoblements, offSources, pTimeFrame);

        int fullyValid = 0;
        Enoblement[] aEnoblements = finalEnoblements.toArray(new Enoblement[]{});
        logger.debug("Checking for fully valid enoblements");
        for (Enoblement e : aEnoblements) {
            if (e.offDone() && e.snobDone(pUse5Snobs)) {
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
                }
            } else {
                if (e.getOffCount() == 0) {
                    //remove enoblements without any off
                    logger.debug("Removing enoblement without any assigned off");
                    finalEnoblements.remove(e);
                }
            }
        }

        setValidEnoblements(fullyValid);
        List<Off> pOffs = new LinkedList<Off>();
        logger.debug("Generating attacks");
        assignOffs(pOffs, offSources, pTargets, pTimeFrame, pMaxAttacksPerVillage);

        int fullOffs = 0;
        logger.debug("Checking for full attacks and removing off tagets from faked list");
        for (Off f : pOffs) {
            if (f.offComplete()) {
                fullOffs++;
            }
            //remove off target from list of fakes villages
            pTargets.remove(f.getTarget());
        }

        //remove enoblement targets from faked list
        for (Enoblement e : aEnoblements) {
            pTargets.remove(e.getTarget());
        }

        setFullOffs(fullOffs);
        List<Fake> pFinalFakes = new LinkedList<Fake>();
        logger.debug("Generating fakes");
        assignFakes(pFinalFakes, fakeSources, pTargets, pTimeFrame, pMaxAttacksPerVillage);

        List<AbstractTroopMovement> movements = new LinkedList<AbstractTroopMovement>();
        logger.debug("Building result list");
        logger.debug(" - adding enoblements");
        for (Enoblement e : finalEnoblements) {
            movements.add(e);
        }
        logger.debug(" - adding offs");
        for (Off f : pOffs) {
            movements.add(f);
        }

        logger.debug(" - adding fakes");
        for (Fake f : pFinalFakes) {
            movements.add(f);
        }

        for (Village snobSource : snobVillages) {
            notAssignedSources.add(snobSource);
        }

        for (Village offSource : offSources) {
            notAssignedSources.add(offSource);
        }
        for (Village fakeSource : fakeSources) {
            notAssignedSources.add(fakeSource);
        }

        return movements;
    }

    private static void generateEnoblements(List<Village> pSnobSources, List<Village> pTargets, TimeFrame pTimeFrame, List<Enoblement> pFinalEnoblements, boolean pUse5SNobs) {
        List<Enoblement> tmpEno = new LinkedList<Enoblement>();

        for (Village target : pTargets) {
            //calculate snob distances for current target village
            List<DistanceMapping> snobMappings = buildSourceTargetsMapping(target, pSnobSources, true);

            //expect target to be valid
            boolean valid = true;
            //check distances for first (fastest) possible snobs
            UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
            if (snobMappings.size() > 3) {
                //at least 4 snobs left
                int snobCount = (pUse5SNobs) ? 5 : 4;
                for (int i = 0; i < snobCount; i++) {
                    long dur = (long) (snobMappings.get(i).getDistance() * snob.getSpeed() * 60000.0);
                    Date send = new Date(pTimeFrame.getEnd() - dur);
                    //check if needed snob can arrive village in time frame
                    if (!pTimeFrame.inside(send, snobMappings.get(i).getTarget().getTribe())) {
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
                int snobCount = (pUse5SNobs) ? 5 : 4;
                for (int j = 0; j < snobCount; j++) {
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
        generateEnoblements(pSnobSources, pTargets, pTimeFrame, pFinalEnoblements, pUse5SNobs);
    }

    private static void assignOffsToEnoblements(List<Enoblement> pInOutEnoblements, List<Village> pOffSources, TimeFrame pTimeFrame) {

        Hashtable<Enoblement, List<DistanceMapping>> tmpMappings = new Hashtable<Enoblement, List<DistanceMapping>>();
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        for (Enoblement enoblement : pInOutEnoblements) {
            //use enoblement if offs where not assigned yet
            if (!enoblement.offDone()) {
                //calculate snob distances for current enoblement target village
                List<DistanceMapping> offMappings = buildSourceTargetsMapping(enoblement.getTarget(), pOffSources, false);

                //expect target to be valid
                boolean valid = true;
                //check distances for first (fastest) possible off
                List<DistanceMapping> validMappings = new LinkedList<DistanceMapping>();
                if (pOffSources.size() > enoblement.getNumberOfCleanOffs()) {
                    //at least 4 snobs left
                    //                   for (int i = 0; i <= enoblement.getNumberOfCleanOffs(); i++) {
                    for (DistanceMapping mapping : offMappings) {
                        //                       long dur = (long) (offMappings.get(i).getDistance() * ram.getSpeed() * 60000.0);
                        long dur = (long) (mapping.getDistance() * ram.getSpeed() * 60000.0);
                        Date send = new Date(pTimeFrame.getEnd() - dur);
                        //check if needed off can arrive village in time frame
                        //                       if (!pTimeFrame.inside(send)) {
                        if (pTimeFrame.inside(send, mapping.getTarget().getTribe())) {
                            //break if at least one of the fastest is not in time
                            //                           valid = false;
                            //                           break;
                            validMappings.add(mapping);
                            if (validMappings.size() == enoblement.getNumberOfCleanOffs()) {
                                valid = true;
                                break;
                            }
                        }
                    }
                } else {
                    //not enough offs left
                    valid = false;
                }
                //if all off villages are in time, create enoblement
                if (valid) {
                    //add new temp off mapping list
//                    List<DistanceMapping> tmpDistances = new LinkedList<DistanceMapping>();
//                    for (int j = 0; j < enoblement.getNumberOfCleanOffs(); j++) {
//                        tmpDistances.add(offMappings.get(j));
//                    }
//                    tmpMappings.put(enoblement, tmpDistances);
                    tmpMappings.put(enoblement, validMappings);
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

    private static void assignOffs(List<Off> pOffs, List<Village> pOffSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacks) {
        //table which holds for every target the distance of each source
        Hashtable<Village, List<DistanceMapping>> tmpMappings = new Hashtable<Village, List<DistanceMapping>>();
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        for (Village target : pTargets) {
            //calculate snob distances for current enoblement target village
            List<DistanceMapping> offMappings = buildSourceTargetsMapping(target, pOffSources, false);

            //check distances for first (fastest) possible off

            //temp map for valid distances
            List<DistanceMapping> tmpMap = new LinkedList<DistanceMapping>();
            for (DistanceMapping mapping : offMappings) {
                long dur = (long) (mapping.getDistance() * ram.getSpeed() * 60000.0);
                Date send = new Date(pTimeFrame.getEnd() - dur);
                //check if needed off can arrive village in time frame
                if (tmpMap.size() == pMaxAttacks) {
                    //break if at least one is not in time or max number of attacks was reached
                    break;
                } else {
                    //add valid distance to map
                    if (pTimeFrame.inside(send, mapping.getTarget().getTribe())) {
                        tmpMap.add(mapping);
                    }
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
        Off f = null;
        //try to find existing fake
        for (Off off : pOffs) {
            if (off.getTarget().equals(best)) {
                f = off;
                break;
            }
        }

        //no existing off found
        if (f == null) {
            f = new Off(best, pMaxAttacks);
            pOffs.add(f);
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

        assignOffs(pOffs, pOffSources, pTargets, pTimeFrame, pMaxAttacks);
    }

    private static void assignFakes(List<Fake> pFakes, List<Village> pFakeSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacks) {
        //table which holds for every target the distance of each source
        Hashtable<Village, List<DistanceMapping>> tmpMappings = new Hashtable<Village, List<DistanceMapping>>();
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        for (Village target : pTargets) {
            //calculate snob distances for current enoblement target village
            List<DistanceMapping> offMappings = buildSourceTargetsMapping(target, pFakeSources, false);

            //check distances for first (fastest) possible off

            //temp map for valid distances
            List<DistanceMapping> tmpMap = new LinkedList<DistanceMapping>();
            for (DistanceMapping mapping : offMappings) {
                long dur = (long) (mapping.getDistance() * ram.getSpeed() * 60000.0);
                Date send = new Date(pTimeFrame.getEnd() - dur);
                //check if needed off can arrive village in time frame
                if ((tmpMap.size() == pMaxAttacks)) {
                    //break if at least one is not in time or max number of attacks was reached
                    break;
                } else {
                    //add valid distance to map
                    if (pTimeFrame.inside(send, mapping.getTarget().getTribe())) {
                        tmpMap.add(mapping);
                    }
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
                pFakeSources.remove(offMapping.getTarget());
            }
        }
        if (f.offComplete()) {
            //remove target only if max number was reached
            pTargets.remove(best);
        }

        assignFakes(pFakes, pFakeSources, pTargets, pTimeFrame, pMaxAttacks);
    }*/
}
