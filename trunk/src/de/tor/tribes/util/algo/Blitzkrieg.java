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
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class Blitzkrieg extends AbstractAttackAlgorithm {

    private static Logger logger = Logger.getLogger("Algorithm_Blitzkrieg");

    @Override
    public List<AbstractTroopMovement> calculateAttacks(Hashtable<UnitHolder, List<Village>> pSources, List<Village> pTargets, int pMaxAttacksPerVillage, int pMaxCleanPerSnob, Date pStartTime, Date pArriveTime, int pTimeFrameStartHour, int pTimeFrameEndHour, boolean pNightBlock, boolean pRandomize) {
        //get snob villages

        //build timeframe
        TimeFrame timeFrame = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
        //generate enoblements with minimum runtime

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
        //</editor-fold>

        logger.debug("Assigning offs");
        List<Fake> pFinalFakes = new LinkedList<Fake>();
        assignOffs(pFinalFakes, offSources, pTargets, timeFrame, pMaxAttacksPerVillage);
        int fullFakes = 0;

        logger.debug("Checking for full offs");
        for (Fake f : pFinalFakes) {
            if (f.offComplete()) {
                fullFakes++;
            }
        }
        setFullOffs(fullFakes);

        logger.debug("Building result list");
        List<AbstractTroopMovement> movements = new LinkedList<AbstractTroopMovement>();

        for (Fake f : pFinalFakes) {
            movements.add(f);
        }

        return movements;
    }

    private static void assignOffs(List<Fake> pFakes, List<Village> pOffSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacks) {
        //table which holds for every target the distance of each source
        Hashtable<Village, List<DistanceMapping>> tmpMappings = new Hashtable<Village, List<DistanceMapping>>();
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        for (Village target : pTargets) {
            //calculate snob distances for current enoblement target village
            List<DistanceMapping> offMappings = AbstractAttackAlgorithm.buildSourceTargetsMapping(target, pOffSources);

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
