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
import java.util.Collections;
import org.apache.log4j.Logger;

/**
 * @author Charon
 */
public class BruteForce extends AbstractAttackAlgorithm {

    private static Logger logger = Logger.getLogger("Algorithm_BruteForce");
    private List<Village> notAssignedSources = null;

    @Override
    public List<Village> getNotAssignedSources() {
        return notAssignedSources;
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
        Enumeration<UnitHolder> unitKeys = pSources.keys();
        Hashtable<Village, Hashtable<UnitHolder, List<Village>>> attacks = new Hashtable<Village, Hashtable<UnitHolder, List<Village>>>();
        notAssignedSources = new LinkedList<Village>();
        Hashtable<Tribe, Integer> attacksPerTribe = new Hashtable<Tribe, Integer>();
        logger.debug("Assigning offs");

        // <editor-fold defaultstate="collapsed" desc=" Assign Offs">
        if (pRandomize) {
            Collections.shuffle(pTargets);
        }
        List<Long> sendTimes = new LinkedList<Long>();
        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> sources = pSources.get(unit);
            if (sources != null) {
                for (Village source : sources) {
                    //time when the attacks should arrive
                    long arrive = pTimeFrame.getEnd();
                    //max. number of attacks per target village
                    int maxAttacksPerVillage = pMaxAttacksPerVillage;
                    Village vTarget = null;
                    //TimeFrame t = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
                    //search all tribes and villages for targets
                    for (Village v : pTargets) {
                        double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());

                        Date sendTime = new Date(arrive - (long) time * 1000);
                        //check if attack is somehow possible
                        if (pTimeFrame.inside(sendTime, source.getTribe()) && !sendTimes.contains(sendTime.getTime())) {
                            //only calculate if time is in time frame
                            //get list of source villages for current target
                            Hashtable<UnitHolder, List<Village>> attacksForVillage = attacks.get(v);
                            if (attacksForVillage == null) {
                                //no attack found for this village
                                //get number of attacks on this tribe
                                Integer cnt = pMaxAttacksPerVillage;
                                if (cnt == null) {
                                    //no attacks on this tribe yet
                                    cnt = 0;
                                }
                                //create new table of attacks
                                attacksForVillage = new Hashtable<UnitHolder, List<Village>>();
                                List<Village> sourceList = new LinkedList<Village>();
                                sourceList.add(source);
                                attacksForVillage.put(unit, sourceList);
                                attacks.put(v, attacksForVillage);
                                attacksPerTribe.put(v.getTribe(), cnt + 1);
                                vTarget = v;
                            } else {
                                Enumeration<UnitHolder> units = attacksForVillage.keys();
                                int currentAttacks = 0;
                                while (units.hasMoreElements()) {
                                    currentAttacks += attacksForVillage.get(units.nextElement()).size();
                                }
                                //there are already attacks on this village
                                if (currentAttacks < maxAttacksPerVillage) {
                                    //more attacks on this village are allowed
                                    Integer cnt = attacksPerTribe.get(v.getTribe());
                                    if (cnt == null) {
                                        cnt = 0;
                                    }
                                    //max number of attacks neither for villages nor for player reached
                                    List<Village> attsPerUnit = attacksForVillage.get(unit);
                                    if (attsPerUnit != null) {
                                        attsPerUnit.add(source);
                                    } else {
                                        attsPerUnit = new LinkedList<Village>();
                                        attsPerUnit.add(source);
                                        attacksForVillage.put(unit, attsPerUnit);
                                    }
                                    attacksPerTribe.put(v.getTribe(), cnt + 1);
                                    vTarget = v;
                                } else {
                                    //max number of attacks per village reached, continue search
                                }
                            }

                        }

                        if (vTarget != null) {
                            sendTimes.add(sendTime.getTime());
                            break;
                        }
                    }

                    if (vTarget == null) {
                        notAssignedSources.add(source);
                    }
                }
            }
        }
        // </editor-fold>

        logger.debug("Removing off targets from fake list");
        Enumeration<Village> targets = attacks.keys();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            pTargets.remove(target);
        }

        logger.debug("Assigning fakes");

        // <editor-fold defaultstate="collapsed" desc=" Assign fakes">
        unitKeys = pFakes.keys();
        Hashtable<Village, Hashtable<Village, UnitHolder>> fakes = new Hashtable<Village, Hashtable<Village, UnitHolder>>();
        //notAssigned = new LinkedList<Village>();
        attacksPerTribe = new Hashtable<Tribe, Integer>();

        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> sources = pFakes.get(unit);
            if (sources != null) {
                for (Village source : sources) {
                    //time when the attacks should arrive
                    long arrive = pTimeFrame.getEnd();
                    //max. number of attacks per target village
                    int maxAttacksPerVillage = pMaxAttacksPerVillage;
                    Village vTarget = null;
                    //TimeFrame t = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
                    //search all tribes and villages for targets
                    for (Village v : pTargets) {
                        if (!attacks.containsKey(v)) {
                            double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());
                            Date sendTime = new Date(arrive - (long) time * 1000);
                            //check if attack is somehow possible
                            if (pTimeFrame.inside(sendTime, source.getTribe())) {
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
                            notAssignedSources.add(source);
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
            Enumeration<UnitHolder> sourceKeys = attacks.get(target).keys();
            Off f = new Off(target, pMaxAttacksPerVillage);
            while (sourceKeys.hasMoreElements()) {
                UnitHolder sourceUnit = sourceKeys.nextElement();
                List<Village> unitVillages = attacks.get(target).get(sourceUnit);
                for (Village source : unitVillages) {
                    f.addOff(sourceUnit, source);
                }
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
