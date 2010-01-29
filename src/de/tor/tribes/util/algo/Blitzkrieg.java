/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Village;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Off;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class Blitzkrieg{// extends AbstractAttackAlgorithm {

    private static Logger logger = Logger.getLogger("Algorithm_Blitzkrieg");
    private List<Village> notAssignedSources = null;
    private List<Attack> miscAttacks = null;

  /*  public List<Village> getNotAssignedSources() {
        return notAssignedSources;
    }

    public List<Attack> getMiscAttacks() {
        return miscAttacks;
    }

    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pMaxCleanPerSnob,
            TimeFrame pTimeFrame,
            boolean pRandomize,
            boolean pUse5Snobs) {
        //get snob villages

        //build timeframe
        //TimeFrame timeFrame = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
        //generate enoblements with minimum runtime
        notAssignedSources = new LinkedList<Village>();
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

        logger.debug("Assigning offs");
        List<Off> pOffs = new LinkedList<Off>();
        List<Long> sends = new LinkedList<Long>();
        assignOffs(pOffs,
                sends,
                offSources,
                pTargets,
                pTimeFrame,
                pMaxAttacksPerVillage);
        int fullOffs = 0;

        logger.debug("Checking for full offs and removing off tagets from faked list");
        for (Off f : pOffs) {
            if (f.offComplete()) {
                fullOffs++;
            }
            //remove target to disallow faking of village a off is running at
            pTargets.remove(f.getTarget());
        }
        setFullOffs(fullOffs);
        logger.debug("Assigning fakes");
        List<Fake> pFinalFakes = new LinkedList<Fake>();
        assignFakes(pFinalFakes, sends,
                fakeSources,
                pTargets,
                pTimeFrame,
                pMaxAttacksPerVillage);

        logger.debug("Building result list");
        List<AbstractTroopMovement> movements = new LinkedList<AbstractTroopMovement>();
        logger.debug(" - adding offs");
        for (Off f : pOffs) {
            movements.add(f);
        }
        logger.debug(" - adding fakes");
        for (Fake f : pFinalFakes) {
            movements.add(f);
        }

        Hashtable<Village, Integer> attsPerTarget = new Hashtable<Village, Integer>();
        for (AbstractTroopMovement movement : movements) {
            Village target = movement.getTarget();
            if (movement.getOffCount() < pMaxAttacksPerVillage) {
                attsPerTarget.put(target, movement.getOffCount());
            }
        }

        logger.debug("Assigning misc attacks");
        miscAttacks = new LinkedList<Attack>();
        assignMiscAttacks(miscAttacks,
                sends,
                attsPerTarget,
                offSources,
                fakeSources,
                pTargets,
                pTimeFrame,
                pMaxAttacksPerVillage);

        logger.debug("Getting list of not assigned sources");
        for (Village offSource : offSources) {
            notAssignedSources.add(offSource);
        }
        for (Village fakeSource : fakeSources) {
            notAssignedSources.add(fakeSource);
        }
        return movements;
    }

    private void assignOffs(List<Off> pOffs, List<Long> sends, List<Village> pOffSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacks) {
        //table, which holds for every target the distance of each source
        Hashtable<Village, List<DistanceMapping>> tmpMappings = new Hashtable<Village, List<DistanceMapping>>();
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        for (Village target : pTargets) {
            //calculate off distances for current enoblement target village
            List<DistanceMapping> offMappings = AbstractAttackAlgorithm.buildSourceTargetsMapping(target, pOffSources, false);
            //temp map for valid distances
            List<DistanceMapping> tmpMap = new LinkedList<DistanceMapping>();
            for (DistanceMapping mapping : offMappings) {
                long dur = (long) (mapping.getDistance() * ram.getSpeed() * 60000l);
                Date send = new Date(pTimeFrame.getEnd() - dur);

                //check if needed off can arrive village in time frame
                if ((tmpMap.size() == pMaxAttacks)) {
                    //break if at least one is not in time or max number of attacks was reached
                    break;
                } else {
                    //add valid distance to map of send time was not used yet
                    if (pTimeFrame.inside(send, mapping.getTarget().getTribe()) && !sends.contains(send.getTime())) {
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
            List<DistanceMapping> mappings = tmpMappings.get(e);

            //check mapping if valid
            if (best == null) {
                //no best set yet, so take the first element to initialize
                best = e;
                //use the slowest off to calculate the worst case
                minDist = mappings.get(mappings.size() - 1).getDistance();
            } else {
                //use the slowest off to calculate the worst case
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
            long dur = (long) (offMapping.getDistance() * ram.getSpeed() * 60000l);
            Date send = new Date(pTimeFrame.getEnd() - dur);
            if (!f.offComplete() && !sends.contains(send.getTime())) {
                f.addOff(ram, offMapping.getTarget());
                pOffSources.remove(offMapping.getTarget());
                sends.add(send.getTime());
            }
        }
        if (f.offComplete()) {
            //remove target only if max number was reached
            pTargets.remove(best);
        }

        //next round
        assignOffs(pOffs, sends, pOffSources, pTargets, pTimeFrame, pMaxAttacks);
    }

    private void assignFakes(List<Fake> pFakes, List<Long> sends, List<Village> pFakeSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacks) {
        //table which holds for every target the distance of each source
        Hashtable<Village, List<DistanceMapping>> tmpMappings = new Hashtable<Village, List<DistanceMapping>>();
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        for (Village target : pTargets) {
            //calculate snob distances for current enoblement target village
            List<DistanceMapping> offMappings = AbstractAttackAlgorithm.buildSourceTargetsMapping(target, pFakeSources, false);

            //temp map for valid distances
            List<DistanceMapping> tmpMap = new LinkedList<DistanceMapping>();
            for (DistanceMapping mapping : offMappings) {
                long dur = (long) (mapping.getDistance() * ram.getSpeed() * 60000l);
                Date send = new Date(pTimeFrame.getEnd() - dur);
                //check if needed off can arrive village in time frame
                if (tmpMap.size() == pMaxAttacks) {
                    //break if at least one is not in time or max number of attacks was reached
                    break;
                } else {
                    //add valid distance to map
                    if (pTimeFrame.inside(send, mapping.getTarget().getTribe()) && !sends.contains(send.getTime())) {
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
            long dur = (long) (offMapping.getDistance() * ram.getSpeed() * 60000l);
            Date send = new Date(pTimeFrame.getEnd() - dur);
            if (!f.offComplete() && !sends.contains(send.getTime())) {
                f.addOff(ram, offMapping.getTarget());
                pFakeSources.remove(offMapping.getTarget());
                sends.add(send.getTime());
            }
        }
        if (f.offComplete()) {
            //remove target only if max number was reached
            pTargets.remove(best);
        }

        //add used send times to list to avoid unsendable attacks
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(pTimeFrame.getEnd());
     /*   for (Attack a : f.getAttacks(c.getTime())) {
            long send = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
            sends.add(send);
        }*/

/*       assignFakes(pFakes, sends, pFakeSources, pTargets, pTimeFrame, pMaxAttacks);
    }

    private void assignMiscAttacks(List<Attack> pAttacks, List<Long> sends, Hashtable<Village, Integer> attsPerTarget, List<Village> pOffSources, List<Village> pFakeSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacks) {
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        int sizeBefore = pAttacks.size();
        Enumeration<Village> keys = attsPerTarget.keys();
        while (keys.hasMoreElements()) {
            //go through all targets
            Village target = keys.nextElement();
            Integer cnt = attsPerTarget.get(target);
            long bestDist = Long.MAX_VALUE;
            long bestTime = 0;
            Village best = null;
            if (cnt <= pMaxAttacks) {
                //only use target if no attacks < max
                for (Village offSource : pOffSources) {
                    //go through all sources to find best source with least distance
                    long movetime = (long) (DSCalculator.calculateMoveTimeInSeconds(offSource, target, ram.getSpeed()) * 1000);
                    Date d = pTimeFrame.getArriveDate(movetime);
                    if (d != null && !sends.contains(d.getTime() - movetime)) {
                        //add if arrive is valid and send time was not used yet
                        long delta = Math.abs(pTimeFrame.getEnd() - d.getTime());
                        if (delta < bestDist) {
                            bestDist = delta;
                            best = offSource;
                            bestTime = d.getTime();
                        }
                    }
                }
            }

            if (best != null) {
                pOffSources.remove(best);
                Attack a = new Attack();
                a.setSource(best);
                a.setTarget(target);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(bestTime);
                a.setArriveTime(c.getTime());
                a.setUnit(ram);
                a.setType(Attack.CLEAN_TYPE);
                pAttacks.add(a);
                attsPerTarget.put(target, cnt + 1);
                long send = c.getTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                sends.add(send);
            }
        }

        //fake round
        keys = attsPerTarget.keys();
        while (keys.hasMoreElements()) {
            //go through all targets
            Village target = keys.nextElement();
            Integer cnt = attsPerTarget.get(target);
            long bestDist = Long.MAX_VALUE;
            long bestTime = 0;
            Village best = null;
            if (cnt <= pMaxAttacks) {
                //only use target if no attacks < max
                for (Village fakeSource : pFakeSources) {
                    //go through all sources to find best source with least distance
                    long movetime = (long) (DSCalculator.calculateMoveTimeInSeconds(fakeSource, target, ram.getSpeed()) * 1000);
                    Date d = pTimeFrame.getArriveDate(movetime);
                    if (d != null && !sends.contains(d.getTime() - movetime)) {
                        //add if arrive is valid and send time was not used yet
                        long delta = Math.abs(pTimeFrame.getEnd() - d.getTime());
                        if (delta < bestDist) {
                            bestDist = delta;
                            best = fakeSource;
                            bestTime = d.getTime();
                        }
                    }
                }
            }

            if (best != null) {
                pFakeSources.remove(best);
                Attack a = new Attack();
                a.setSource(best);
                a.setTarget(target);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(bestTime);
                a.setArriveTime(c.getTime());
                a.setUnit(ram);
                a.setType(Attack.FAKE_TYPE);
                pAttacks.add(a);
                attsPerTarget.put(target, cnt + 1);
                long send = c.getTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                sends.add(send);
            }
        }

        if (sizeBefore == pAttacks.size()) {
            //nothing changed, erturn
            return;
        }
        assignMiscAttacks(pAttacks, sends, attsPerTarget, pOffSources, pFakeSources, pTargets, pTimeFrame, pMaxAttacks);
    }*/
}
