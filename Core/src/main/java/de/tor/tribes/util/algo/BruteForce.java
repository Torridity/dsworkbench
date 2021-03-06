/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.TroopMovement;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Charon
 * @author Patrick
 */
public class BruteForce extends AbstractAttackAlgorithm {

    private static Logger logger = LogManager.getLogger("Algorithm_BruteForce");

    @Override
    public List<TroopMovement> calculateAttacks(
            HashMap<UnitHolder, List<Village>> pSources,
            HashMap<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            List<Village> pFakeTargets,
            HashMap<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets) {

        List<Village> allTargets = Arrays.asList(pTargets.toArray(new Village[pTargets.size()]));
        List<Village> allFakeTargets = Arrays.asList(pFakeTargets.toArray(new Village[pFakeTargets.size()]));

        HashMap<Village, HashMap<UnitHolder, List<Village>>> attacks = new HashMap<>();
        logger.debug("Assigning offs");
        logText("Starte zufällige Berechnung");

        int maxStatus = allTargets.size() + allFakeTargets.size();
        int currentStatus = 0;

        // <editor-fold defaultstate="collapsed" desc=" Assign Offs">
        logInfo(" Starte Berechnung für Offs");
        for(UnitHolder unit: pSources.keySet()) {
            logInfo(" - Starte Berechnung für Einheit '" + unit.getName() + "'");
            List<Village> sources = pSources.get(unit);

            if (sources != null) {
                logInfo(" - Verwende " + sources.size() + " Herkunftsdörfer");
                for (Village source : sources) {

                    //time when the attacks should arrive
                    Village vTarget = null;

                    //distribute targets randomly
                    Collections.shuffle(pTargets);
                    currentStatus = allTargets.size() - pTargets.size();
                    updateStatus(currentStatus, maxStatus);
                    //search all targets
                    logInfo(" - Teste " + pTargets.size() + " mögliche Ziele");
                    for (Village v : pTargets.toArray(new Village[pTargets.size()])) {
                        if (isAborted()) {
                            return new LinkedList<>();
                        }
                        int maxAttacksPerVillage = pMaxAttacksTable.get(v);
                        double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());
                        if (unit.getPlainName().equals("snob")) {
                            if (DSCalculator.calculateDistance(source, v) > ServerSettings.getSingleton().getSnobRange()) {
                                //continue with the next destination Village
                                continue;
                            }
                        }

                        long runtime = (long) time * 1000;
                        //check if attack is somehow possible
                        if (pTimeFrame.isMovementPossible(runtime) && !source.equals(v)) {
                            //only calculate if time is in time frame
                            //get list of source villages for current target
                            HashMap<UnitHolder, List<Village>> attacksForVillage = attacks.get(v);
                            if (attacksForVillage == null) {
                                //create new table of attacks
                                attacksForVillage = new HashMap<>();
                                List<Village> sourceList = new LinkedList<>();
                                logInfo("   * Neue Truppenbewegung: " + source + " -> " + v);
                                sourceList.add(source);
                                attacksForVillage.put(unit, sourceList);
                                attacks.put(v, attacksForVillage);
                                vTarget = v;
                            } else {
                                int currentAttacks = 0;
                                for(List<Village> l: attacksForVillage.values()) {
                                    currentAttacks += l.size();
                                }
                                //there are already attacks on this village
                                if (currentAttacks < maxAttacksPerVillage) {
                                    //more attacks on this village are allowed
                                    boolean added = false;
                                    //max number of attacks neither for villages nor for player reached
                                    List<Village> attsPerUnit = attacksForVillage.get(unit);
                                    if (attsPerUnit != null) {
                                        if (!attsPerUnit.contains(source) || 
                                                (unit.equals(DataHolder.getSingleton().getUnitByPlainName("snob")) && multipleSameSnobsAllowed())) {
                                            //only add source if it does not attack current target yet
                                            added = true;
                                            logInfo("   * Neue Truppenbewegung: " + source + " -> " + v);
                                            attsPerUnit.add(source);
                                        }
                                    } else {
                                        attsPerUnit = new LinkedList<>();
                                        //only add source if it does not attack current target yet
                                        added = true;
                                        logInfo("   * Neue Truppenbewegung: " + source + " -> " + v);
                                        attsPerUnit.add(source);
                                        attacksForVillage.put(unit, attsPerUnit);
                                    }
                                    if (added) {
                                        //only increment attack count if source was added
                                        vTarget = v;
                                        
                                        //check if last missing attack was added. 
                                        if (currentAttacks + 1 == maxAttacksPerVillage){
                                            logInfo("   * Entferne vollständiges Ziel " + v);
                                            pTargets.remove(v);                                        	
                                        }
                                        
                                    } else {
                                        vTarget = null;
                                    }
                                } else {
                                    //max number of attacks per village reached, continue search
                                    logInfo("   * Entferne vollständiges Ziel " + v);
                                    pTargets.remove(v);
                                    vTarget = null;
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
        	/*
        	 *  why would we do this? We should allow one fake for each missing off, so we can simply use pTargets as is?
        	 *  
            logger.debug("Removing assigned off targets from fake list");
            Enumeration<Village> targets = attacks.keys();
            while (targets.hasMoreElements()) {
                Village target = targets.nextElement();
                pTargets.remove(target);
            }*/
        	logger.debug("Keeping remaining Off targets for fake search");
        } else {
            //clear target list
            pTargets.clear();
        }

        //adding fake targets
        for (Village fakeTarget : pFakeTargets) {
            pTargets.add(fakeTarget);
        }
        logger.debug("Assigning fakes");
        logText(" Starte Berechnung für Fakes.");
        // <editor-fold defaultstate="collapsed" desc=" Assign fakes">
        HashMap<Village, HashMap<UnitHolder, List<Village>>> fakes = new HashMap<>();
        for(UnitHolder unit: pFakes.keySet()) {
            logInfo(" - Starte Berechnung für Einheit '" + unit.getName() + "'");
            List<Village> sources = pFakes.get(unit);

            if (sources != null) {
                logInfo(" - Verwende " + sources.size() + " Herkunftsdörfer");
                for (Village source : sources) {

                    //time when the attacks should arrive
                    Village vTarget = null;

                    //distribute targets randomly
                    Collections.shuffle(pTargets);
                    currentStatus = allTargets.size() + allFakeTargets.size() - pTargets.size();
                    updateStatus(currentStatus, maxStatus);
                    //search all targets
                    logInfo(" - Teste " + pTargets.size() + " mögliche Ziele");
                    for (Village v : pTargets.toArray(new Village[pTargets.size()])) {
                        if (isAborted()) {
                            return new LinkedList<>();
                        }
                        int maxAttacksPerVillage = pMaxAttacksTable.get(v);
                        double time = DSCalculator.calculateMoveTimeInSeconds(source, v, unit.getSpeed());
                        if (unit.getPlainName().equals("snob")) {
                            if (DSCalculator.calculateDistance(source, v) > ServerSettings.getSingleton().getSnobRange()) {
                                //continue with the next destination Village
                                continue;
                            }
                        }

                        long runtime = (long) time * 1000;
                        //check if attack is somehow possible
                        if (pTimeFrame.isMovementPossible(runtime)) {
                            //only calculate if time is in time frame
                            //get list of source villages for current target
                            HashMap<UnitHolder, List<Village>> attacksForVillage = attacks.get(v);
                            HashMap<UnitHolder, List<Village>> fakesForVillage = fakes.get(v);
                            if (attacksForVillage == null){
                            	//create empty table of attacks (will stay empty, but is used for maxAttacks calculation)
                                attacksForVillage = new HashMap<>();
                                List<Village> sourceList = new LinkedList<>();
                                attacksForVillage.put(unit, sourceList);
                            }
                            if (fakesForVillage == null) {
                                //create new table of fakes 
                                fakesForVillage = new HashMap<>();
                                List<Village> sourceList = new LinkedList<>();
                                logInfo("   * Neue Truppenbewegung: " + source + " -> " + v);
                                sourceList.add(source);
                                fakesForVillage.put(unit, sourceList);
                                fakes.put(v, fakesForVillage);
                                vTarget = v;
                            } else {
                                int currentAttacks = 0;
                                for(List<Village> listV: attacksForVillage.values()) {
                                    currentAttacks += listV.size();
                                }
                                int currentFakes = 0;
                                for(List<Village> listV: fakesForVillage.values()) {
                                    currentFakes += listV.size();
                                }
                                
                                //there are already attacks or fakes on this village
                                if (currentAttacks + currentFakes < maxAttacksPerVillage) {
                                    //more attacks on this village are allowed
                                    boolean added = false;
                                    //max number of attacks neither for villages nor for player reached
                                    List<Village> attsPerUnit = attacksForVillage.get(unit);
                                    List<Village> fakesPerUnit = fakesForVillage.get(unit);
                                    if (fakesPerUnit != null) {
                                        if (!attsPerUnit.contains(source) && (attsPerUnit == null || !attsPerUnit.contains(source))) {                                        	
                                            //only add source if it does not attack current target yet
                                            added = true;
                                            logInfo("   * Neue Truppenbewegung: " + source + " -> " + v);
                                            fakesPerUnit.add(source);
                                        }
                                    } else {
                                        fakesPerUnit = new LinkedList<>();
                                        //only add source if it does not attack current target yet
                                        added = true;
                                        logInfo("   * Neue Truppenbewegung: " + source + " -> " + v);
                                        fakesPerUnit.add(source);
                                        fakesForVillage.put(unit, attsPerUnit);
                                    }
                                    if (added) {
                                        //only increment attack count if source was added
                                        vTarget = v;
                                        
                                        //check if last missing attack was added. 
                                        if (currentAttacks + currentFakes + 1 == maxAttacksPerVillage){
                                            logInfo("   * Entferne vollständiges Ziel " + v);
                                            pTargets.remove(v);                                        	
                                        }
                                        
                                    } else {
                                        vTarget = null;
                                    }
                                } else {
                                    //max number of attacks per village reached, continue search
                                    logInfo("   * Entferne vollständiges Ziel " + v);
                                    pTargets.remove(v);
                                    vTarget = null;
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

        updateStatus(maxStatus, maxStatus);
        // </editor-fold>

        logText(" - Erstelle Ergebnisliste");
        //convert to result list
        List<TroopMovement> movements = new LinkedList<>();
        logger.debug(" - adding offs");

        logText(String.format(" %d Offs berechnet", attacks.size()));
        for (Village target : allTargets) {
            HashMap<UnitHolder, List<Village>> sourcesForTarget = attacks.get(target);
            TroopMovement f = new TroopMovement(target, pMaxAttacksTable.get(target), Attack.CLEAN_TYPE);
            
            if (sourcesForTarget != null) {
                for(UnitHolder sourceUnit: sourcesForTarget.keySet()) {
                    List<Village> unitVillages = attacks.get(target).get(sourceUnit);
                    for (Village source : unitVillages) {
                        f.addOff(sourceUnit, source);
                    }
                }
            }
            if(sourcesForTarget == null && fakes.get(target) != null) {
                //ignore Off targets, when there are Fakes assigned and no Offs
                continue;
            }
            movements.add(f);
        }

        logger.debug(" - adding fakes");
        logText(String.format(" %d Fakes berechnet", fakes.size()));

        for (Village target : (List<Village>)ListUtils.union(allFakeTargets, allTargets)) {
        	HashMap<UnitHolder, List<Village>> sourcesForTarget = fakes.get(target);
            TroopMovement f = new TroopMovement(target, pMaxAttacksTable.get(target), Attack.FAKE_TYPE);
            if (sourcesForTarget != null) {
                for(UnitHolder sourceUnit: sourcesForTarget.keySet()) {
                    List<Village> unitVillages = fakes.get(target).get(sourceUnit);
                    for (Village source : unitVillages) {
                        f.addOff(sourceUnit, source);
                    }
                }
            }
            if(sourcesForTarget == null && allTargets.contains(target)) {
                //ignore Off targets, where no Fakes were assigned
                continue;
            }
            movements.add(f);
        }

        logText("Berechnung abgeschlossen.");
        return movements;
    }
}
