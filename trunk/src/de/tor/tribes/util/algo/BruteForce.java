/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Off;
import de.tor.tribes.util.ServerSettings;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * @TODO (DIFF) Stop calculation if no more targets are available
 * @author Charon
 */
public class BruteForce extends AbstractAttackAlgorithm {

    private static Logger logger = Logger.getLogger("Algorithm_BruteForce");

    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            List<Village> pFakeTargets,
            Hashtable<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets) {

        Enumeration<UnitHolder> unitKeys = pSources.keys();
        Hashtable<Village, Hashtable<UnitHolder, List<Village>>> attacks = new Hashtable<Village, Hashtable<UnitHolder, List<Village>>>();
        logger.debug("Assigning offs");
        logText("Starte zufällige Berechnung");
        // <editor-fold defaultstate="collapsed" desc=" Assign Offs">
        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            logInfo(" - Starte Berechnung für Einheit '" + unit.getName() + "'");
            List<Village> sources = pSources.get(unit);

            if (sources != null) {
                logInfo(" - Verwende " + sources.size() + " Herkunftsdörfer");
                int maxRuns = sources.size();
                int currentRun = 0;
                for (Village source : sources) {
                    currentRun++;
                    updateStatus(maxRuns - currentRun, maxRuns);
                    //time when the attacks should arrive
                    // long arrive = pTimeFrame.getEnd();
                    Village vTarget = null;

                    //distribute targets randomly
                    Collections.shuffle(pTargets);
                    //search all targets
                    logInfo(" - Teste " + pTargets.size() + " mögliche Ziele");
                    for (Village v : pTargets.toArray(new Village[]{})) {
                        int maxAttacksPerVillage = pMaxAttacksTable.get(v);
                        double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());
                        if (unit.getPlainName().equals("snob")) {
                            if (DSCalculator.calculateDistance(source, v) > ServerSettings.getSingleton().getSnobRange()) {
                                //set move time to "infinite" if distance is too large
                                time = Double.MAX_VALUE;
                            }
                        }

                        long runtime = (long) time * 1000;
                        //check if attack is somehow possible
                        if (pTimeFrame.isMovementPossible(runtime, source.getTribe())) {
                            //only calculate if time is in time frame
                            //get list of source villages for current target
                            Hashtable<UnitHolder, List<Village>> attacksForVillage = attacks.get(v);
                            if (attacksForVillage == null) {
                                //create new table of attacks
                                attacksForVillage = new Hashtable<UnitHolder, List<Village>>();
                                List<Village> sourceList = new LinkedList<Village>();
                                logInfo("   * Neuer Angriffe: " + source + " > " + v);
                                sourceList.add(source);
                                attacksForVillage.put(unit, sourceList);
                                attacks.put(v, attacksForVillage);
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

                                    boolean added = false;
                                    //max number of attacks neither for villages nor for player reached
                                    List<Village> attsPerUnit = attacksForVillage.get(unit);
                                    if (attsPerUnit != null) {
                                        if (!attsPerUnit.contains(source)) {
                                            //only add source if it does not attack current target yet
                                            added = true;
                                            logInfo("   * Neuer Angriffe: " + source + " -> " + v);
                                            attsPerUnit.add(source);
                                        }
                                    } else {
                                        attsPerUnit = new LinkedList<Village>();
                                        //only add source if it does not attack current target yet
                                        added = true;
                                        logInfo("   * Neuer Angriffe: " + source + " -> " + v);
                                        attsPerUnit.add(source);
                                        attacksForVillage.put(unit, attsPerUnit);
                                    }
                                    if (added) {
                                        //only increment attack count if source was added
                                        vTarget = v;
                                    }
                                } else {
                                    //max number of attacks per village reached, continue search
                                    logInfo("   * Entferne vollständiges Ziel " + v);
                                    pTargets.remove(v);
                                }
                            }
                        }

                        if (vTarget != null) {
                            break;
                        }
                    }

                    if (vTarget == null) {
                        logInfo(" - Keine Ziele für Herkunftsdorf " + source + " gefunden");
                    }
                }
            } else {
                logInfo(" - Keine Herkunftsdörfer für aktuelle Einheit");
            }
        }
        // </editor-fold>

        if (pFakeOffTargets) {
            logger.debug("Removing assigned off targets from fake list");
            Enumeration<Village> targets = attacks.keys();
            while (targets.hasMoreElements()) {
                Village target = targets.nextElement();
                pTargets.remove(target);
            }
        } else {
            //clear target list
            pTargets.clear();
        }

        //adding fake targets
        for (Village fakeTarget : pFakeTargets) {
            pTargets.add(fakeTarget);
        }
        logger.debug("Assigning fakes");

        // <editor-fold defaultstate="collapsed" desc=" Assign fakes">
        unitKeys = pFakes.keys();
        Hashtable<Village, Hashtable<Village, UnitHolder>> fakes = new Hashtable<Village, Hashtable<Village, UnitHolder>>();
        //notAssigned = new LinkedList<Village>();

        while (unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> sources = pFakes.get(unit);
            if (sources != null) {
                int maxRuns = sources.size();
                int currentRun = 0;
                for (Village source : sources) {
                    currentRun++;
                    updateStatus(maxRuns - currentRun, maxRuns);
                    //time when the attacks should arrive
                    //  long arrive = pTimeFrame.getEnd();
                    //max. number of attacks per target village

                    Village vTarget = null;
                    //TimeFrame t = new TimeFrame(pStartTime, pArriveTime, pTimeFrameStartHour, pTimeFrameEndHour);
                    //search all tribes and villages for targets
                    Collections.shuffle(pTargets);
                    for (Village v : pTargets) {
                        int maxAttacksPerVillage = pMaxAttacksTable.get(v);
                        if (!attacks.containsKey(v)) {
                            double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());
                            if (unit.getPlainName().equals("snob")) {
                                if (DSCalculator.calculateDistance(source, v) > ServerSettings.getSingleton().getSnobRange()) {
                                    //set move time to "infinite" if distance is too large
                                    time = Double.MAX_VALUE;
                                }
                            }
                            /*Date sendTime = new Date(arrive - (long) time * 1000);
                            if (pTimeFrame.isVariableArriveTime()) {
                            //calculate possible arrive time and store it in sendTime
                            sendTime = pTimeFrame.getRandomArriveTime(Math.round(time * 1000.0), source.getTribe(), new LinkedList<Long>());
                            if (sendTime == null) {
                            //no arrive time found, set send time to 1.1.1970
                            sendTime = new Date(0);
                            } else {
                            //arrive time found, set correct send time
                            sendTime = new Date(sendTime.getTime() - (long) time * 1000);
                            }
                            }*/
                            long runtime = (long) time * 1000;
                            //check if attack is somehow possible
                            //if (pTimeFrame.inside(sendTime, source.getTribe())) {
                            if (pTimeFrame.isMovementPossible(runtime, source.getTribe())) {
                                //only calculate if time is in time frame
                                //get list of source villages for current target
                                Hashtable<Village, UnitHolder> attacksForVillage = fakes.get(v);
                                if (attacksForVillage == null) {
                                    //no attack found for this village
                                    //get number of attacks on this tribe
                                    Integer cnt = maxAttacksPerVillage;
                                    if (cnt == null) {
                                        //no attacks on this tribe yet
                                        cnt = 0;
                                    }
                                    //create new table of attacks
                                    attacksForVillage = new Hashtable<Village, UnitHolder>();
                                    attacksForVillage.put(source, unit);
                                    fakes.put(v, attacksForVillage);
                                    vTarget = v;
                                } else {
                                    //there are already attacks on this village
                                    if (attacksForVillage.keySet().size() < maxAttacksPerVillage) {
                                        //more attacks on this village are allowed

                                        //max number of attacks neither for villages nor for player reached
                                        if (!attacksForVillage.containsKey(source)) {
                                            attacksForVillage.put(source, unit);
                                            vTarget = v;
                                        }
                                    } else {
                                        //max number of attacks per village reached, continue search
                                    }
                                }
                            }
                            if (vTarget != null) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // </editor-fold>
        logText(" - Erstelle Ergebnisliste");
        //convert to result list
        List<AbstractTroopMovement> movements = new LinkedList<AbstractTroopMovement>();
        Enumeration<Village> targetKeys = attacks.keys();
        int fullMovements = 0;
        logger.debug(" - adding offs");
        while (targetKeys.hasMoreElements()) {
            Village target = targetKeys.nextElement();
            Enumeration<UnitHolder> sourceKeys = attacks.get(target).keys();
            Off f = new Off(target, pMaxAttacksTable.get(target));
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
            Fake f = new Fake(target, pMaxAttacksTable.get(target));
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

        logText("Berechnung abgeschlossen.");
        return movements;
    }
}
