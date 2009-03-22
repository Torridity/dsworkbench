/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Off;
import org.apache.log4j.Logger;

/**
 * @author Charon
 */
public class BruteForce extends AbstractAttackAlgorithm {

    private static Logger logger = Logger.getLogger("Algorithm_BruteForce");

    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pMaxCleanPerSnob,
            Date pStartTime,
            Date pArriveTime,
            int pTimeFrameStartHour,
            int pTimeFrameEndHour,
            boolean pNightBlock,
            boolean pRandomize) {
        Enumeration<UnitHolder> unitKeys = pSources.keys();
        Hashtable<Village, Hashtable<Village, UnitHolder>> attacks = new Hashtable<Village, Hashtable<Village, UnitHolder>>();
        List<Village> notAssigned = new LinkedList<Village>();
        Hashtable<Tribe, Integer> attacksPerTribe = new Hashtable<Tribe, Integer>();
        logger.debug("Assigning offs");
        // <editor-fold defaultstate="collapsed" desc=" Assign Offs">

        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> sources = pSources.get(unit);
            if (sources != null) {
                for (Village source : sources) {
                    //time when the attacks should arrive
                    long arrive = pArriveTime.getTime();
                    //max. number of attacks per target village
                    int maxAttacksPerVillage = pMaxAttacksPerVillage;
                    Village vTarget = null;
                    TimeFrame t = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
                    //search all tribes and villages for targets
                    for (Village v : pTargets) {
                        double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());
                        Date sendTime = new Date(arrive - (long) time * 1000);
                        //check if attack is somehow possible
                        if (t.inside(sendTime)) {
                            //only calculate if time is in time frame
                            //get list of source villages for current target
                            Hashtable<Village, UnitHolder> attacksForVillage = attacks.get(v);
                            if (attacksForVillage == null) {
                                //no attack found for this village
                                //get number of attacks on this tribe
                                Integer cnt = pMaxAttacksPerVillage;
                                if (cnt == null) {
                                    //no attacks on this tribe yet
                                    cnt = 0;
                                }
                                //create new table of attacks
                                attacksForVillage = new Hashtable<Village, UnitHolder>();
                                attacksForVillage.put(source, unit);
                                attacks.put(v, attacksForVillage);
                                attacksPerTribe.put(v.getTribe(), cnt + 1);
                                vTarget = v;
                            } else {
                                //there are already attacks on this village
                                if (attacksForVillage.keySet().size() < maxAttacksPerVillage) {
                                    //more attacks on this village are allowed
                                    Integer cnt = attacksPerTribe.get(v.getTribe());
                                    if (cnt == null) {
                                        cnt = 0;
                                    }
                                    //max number of attacks neither for villages nor for player reached
                                    attacksForVillage.put(source, unit);
                                    attacksPerTribe.put(v.getTribe(), cnt + 1);
                                    vTarget = v;
                                } else {
                                    //max number of attacks per village reached, continue search
                                }
                            }
                        }
                        if (vTarget != null) {
                            break;
                        }
                    }

                    if (vTarget == null) {
                        notAssigned.add(source);
                    }
                }
            }
        }
// </editor-fold>

        logger.debug("Assigning fakes");
        // <editor-fold defaultstate="collapsed" desc=" Assign fakes">
        unitKeys = pFakes.keys();
        Hashtable<Village, Hashtable<Village, UnitHolder>> fakes = new Hashtable<Village, Hashtable<Village, UnitHolder>>();
        notAssigned = new LinkedList<Village>();
        attacksPerTribe = new Hashtable<Tribe, Integer>();

        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> sources = pFakes.get(unit);
            if (sources != null) {
                for (Village source : sources) {
                    //time when the attacks should arrive
                    long arrive = pArriveTime.getTime();
                    //max. number of attacks per target village
                    int maxAttacksPerVillage = pMaxAttacksPerVillage;
                    Village vTarget = null;
                    TimeFrame t = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
                    //search all tribes and villages for targets
                    for (Village v : pTargets) {
                        if (!attacks.contains(v)) {
                            double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());
                            Date sendTime = new Date(arrive - (long) time * 1000);
                            //check if attack is somehow possible
                            if (t.inside(sendTime)) {
                                //only calculate if time is in time frame
                                //get list of source villages for current target
                                Hashtable<Village, UnitHolder> attacksForVillage = fakes.get(v);
                                if (attacksForVillage == null) {
                                    //no attack found for this village
                                    //get number of attacks on this tribe
                                    Integer cnt = pMaxAttacksPerVillage;
                                    if (cnt == null) {
                                        //no attacks on this tribe yet
                                        cnt = 0;
                                    }
                                    //create new table of attacks
                                    attacksForVillage = new Hashtable<Village, UnitHolder>();
                                    attacksForVillage.put(source, unit);
                                    fakes.put(v, attacksForVillage);
                                    attacksPerTribe.put(v.getTribe(), cnt + 1);
                                    vTarget = v;
                                } else {
                                    //there are already attacks on this village
                                    if (attacksForVillage.keySet().size() < maxAttacksPerVillage) {
                                        //more attacks on this village are allowed
                                        Integer cnt = attacksPerTribe.get(v.getTribe());
                                        if (cnt == null) {
                                            cnt = 0;
                                        }
                                        //max number of attacks neither for villages nor for player reached
                                        attacksForVillage.put(source, unit);
                                        attacksPerTribe.put(v.getTribe(), cnt + 1);
                                        vTarget = v;
                                    } else {
                                        //max number of attacks per village reached, continue search
                                    }
                                }
                            }
                            if (vTarget != null) {
                                break;
                            }
                        }

                        if (vTarget == null) {
                            notAssigned.add(source);
                        }
                    }
                }
            }
        }

        // </editor-fold>

        //convert to result list
        List<AbstractTroopMovement> movements = new LinkedList<AbstractTroopMovement>();
        Enumeration<Village> targetKeys = attacks.keys();
        int fullMovements = 0;
        logger.debug(" - adding offs");
        while (targetKeys.hasMoreElements()) {
            Village target = targetKeys.nextElement();
            Enumeration<Village> sourceKeys = attacks.get(target).keys();
            Off f = new Off(target, pMaxAttacksPerVillage);
            while (sourceKeys.hasMoreElements()) {
                Village source = sourceKeys.nextElement();
                UnitHolder unit = attacks.get(target).get(source);
                f.addOff(unit, source);
            }
            if (f.offComplete()) {
                fullMovements++;
            }
            movements.add(f);
        }
        logger.debug(" - adding fakes");
        Enumeration<Village> fakeKeys = fakes.keys();
        while (fakeKeys.hasMoreElements()) {
            Village target = fakeKeys.nextElement();
            Enumeration<Village> sourceKeys = fakes.get(target).keys();
            Fake f = new Fake(target, pMaxAttacksPerVillage);
            while (sourceKeys.hasMoreElements()) {
                Village source = sourceKeys.nextElement();
                UnitHolder unit = fakes.get(target).get(source);
                f.addOff(unit, source);
            }
            if (f.offComplete()) {
                fullMovements++;
            }
            movements.add(f);
        }

        setValidEnoblements(0);
        setFullOffs(fullMovements);
        return movements;
    }
}
