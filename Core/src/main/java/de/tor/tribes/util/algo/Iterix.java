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

import de.tor.tribes.util.algo.types.TimeFrame;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.TroopMovement;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class Iterix extends AbstractAttackAlgorithm {
    //Log the Times needed to execute the algorithm to console
    private static final boolean LOG_TIMES = false; 
    
    private static Logger logger = Logger.getLogger("SystematicAlgorithm");
    
    //[unit type id * source.size() + source][target]
    private int[][] mappings;
    private int[][] result;
    
    //[unit type id][source]
    private int[][] sourceAmounts;
    
    //list where the unit type ids are obtained from
    private List<UnitHolder> troops;
    
    int selectedSource = 0;
    int selectedTarget = 0;
    int round = 0;

    @Override
    public List<TroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            List<Village> pFakeTargets,
            Hashtable<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets) {
        long s = System.currentTimeMillis();
        logText("Starte systematische Berechnung");
        logText("Berechne Offs");
        
        troops = new LinkedList<>(pSources.keySet());
        for(UnitHolder unit: new LinkedList<>(pFakes.keySet())) {
            if(!troops.contains(unit)) troops.add(unit);
        }
        
        Hashtable<Village, TroopMovement> movements = new Hashtable<>();
        List<TroopMovement> movementList = new LinkedList<>();
        Enumeration<UnitHolder> unitKeys = pSources.keys();
        sourceAmounts = new int[troops.size()][];
        
        // <editor-fold defaultstate="collapsed" desc=" Assign Offs">
        //list where the source an target ids are obtained from
        List<Village> offSources = new LinkedList<>();
        List<Village> allOffSources = new LinkedList<>();
        
        //generate a list with all source Villages
        while(unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> unitSources = pSources.get(unit);
            logInfo(" - Starte Vorbereiten für Einheit '" + unit.getName() + "' (1/2)");
 
            if(unitSources != null && !unitSources.isEmpty()) {
                //off sources are available
                for(Village v: unitSources) {
                    if(!allOffSources.contains(v)) allOffSources.add(v);
                }
                
                //remove non-working sources if we use a fixed arrive time
                logText(" - Entferne Herkunftsdörfer, die keins der Ziel erreichen können");
                removeImpossibleSources(unitSources, pTargets, pTimeFrame, unit);

                if (unitSources.isEmpty()) {
                    logText("Keine Dörfer für diese Einheit übrig");
                    continue;
                }
                
                for(Village v: unitSources) {
                    if(!offSources.contains(v)) offSources.add(v);
                }
            }
        }
        unitKeys = pSources.keys();
        
        //generate sourceAmounts Array
        while(unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> unitSources = pSources.get(unit);
            logInfo(" - Starte Vorbereiten für Einheit '" + unit.getName() + "' (2/2)");
           
            if (unitSources != null && !unitSources.isEmpty()) {
                //build array of attack amount of each source
                sourceAmounts[troops.indexOf(unit)] = resolveDuplicates(unitSources, offSources);
            }
        }
        
        if (!offSources.isEmpty()) {
            //build mappings of possible source-target combinations
            logText(" - Erstelle mögliche Herkunft-Ziel Kombinationen");
            mappings = buildMappings(offSources, pTargets, pTimeFrame, pMaxAttacksTable);

            //initialize result array
            result = new int[mappings.length][mappings[0].length];
            try {
                long s1 = System.currentTimeMillis();
                logText(" - Suche optimale Herkunft-Ziel Kombinationen");
                round = 1;
                int maxMappings = countMappings(mappings);
                while (!solve(offSources, pTargets, mappings, result)) {
                    Thread.sleep(10);
                    int currentMappings = countMappings(mappings);
                    updateStatus(maxMappings - currentMappings, maxMappings);
                    logInfo("   * " + currentMappings + " von " + maxMappings + " verbleibende Kombinationen");
                    if (isAborted()) {
                        break;
                    }
                    if(LOG_TIMES) {
                        System.out.println(" Loop: " + (System.currentTimeMillis() - s1));
                    }
                }
                if(LOG_TIMES) {
                    System.out.println("solved: " + (System.currentTimeMillis() - s1));
                }
            } catch (Exception ewe) {
                logger.error("Unexpexted error during off calculation", ewe);
                logError("Unerwarteter Fehler bei der Off-Berechnung!");
                return new LinkedList<>();
            }
            
            logText(" - Erstelle Ergebnisliste");
            //store results
            int cnt = 0;
            for (int i = 0; i < mappings.length; i++) {
                for (int j = 0; j < pTargets.size(); j++) {
                    Village target = pTargets.get(j);
                    TroopMovement movementForTarget = movements.get(target);
                    if (movementForTarget == null) {
                        movementForTarget = new TroopMovement(target, pMaxAttacksTable.get(target), Attack.CLEAN_TYPE);
                        movements.put(target, movementForTarget);
                    }
                    
                    if (result[i][j] != 0) {
                        cnt++;
                        Village source = offSources.get(i % offSources.size());
                        movementForTarget.addOff(troops.get((int) (i / offSources.size())), source);
                    }
                }
            }
            logText(" - " + cnt + " Angriffe gefunden");
        }//end of off assignment
        // </editor-fold>

        //set result movements and remove used targets if needed
        Enumeration<Village> targetKeys = movements.keys();
        while (targetKeys.hasMoreElements()) {
            Village target = targetKeys.nextElement();
            
            if(movements.get(target).getOffCount() > 0 || !pFakeOffTargets) {
                //add all Off Targets with assigned offs
                movementList.add(movements.get(target));
            }
        }

        if (isAborted()) {
            return movementList;
        }

        if (!pFakeOffTargets) {
            //remove all off targets
            pTargets.clear();
        }

        if(LOG_TIMES) {
            System.out.println("Copy " + copy);
            System.out.println("Map " + map);
            System.out.println("SMap " + sMap);
            System.out.println("TMap " + tMap);
            System.out.println("Swap " + swap);
            System.out.println("Solve " + solve);
        }
        
        logText(pFakeTargets.size() + " Fake Targets");
        //add fake targets
        for (Village fakeTarget : pFakeTargets) {
            pTargets.add(fakeTarget);
        }
        logText(pTargets.size() + " Fake Targets");
        
        // <editor-fold defaultstate="collapsed" desc=" Assign Fakes">
        logText("Berechne Fakes");
        unitKeys = pFakes.keys();
        sourceAmounts = new int[troops.size()][];
        
        //list where the source an target ids are obtained from
        List<Village> fakeSources = new LinkedList<>();
        
        //generate a list with all source Villages
        while(unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> unitSources = pFakes.get(unit);
            logInfo(" - Starte Vorbereiten für Einheit '" + unit.getName() + "' (1/2)");
 
            if(unitSources != null && !unitSources.isEmpty()) {
                //off sources are available
                
                //remove non-working sources if we use a fixed arrive time
                logText(" - Entferne Herkunftsdörfer, die keins der Ziel erreichen können");
                removeImpossibleSources(unitSources, pTargets, pTimeFrame, unit);

                if (unitSources.isEmpty()) {
                    logText("Keine Dörfer für diese Einheit übrig");
                    continue;
                }

                for(Village v: unitSources) {
                    if(!fakeSources.contains(v)) fakeSources.add(v);
                }
            }
        }
        
        if (fakeSources.isEmpty()) {
            logText("Keine gültigen Fakes gefunden. Berechnung abgeschlossen.");
            if(LOG_TIMES) {
                System.out.println("Overall " + (System.currentTimeMillis() - s));
            }
            return movementList;
        }
        unitKeys = pFakes.keys();
        
        //generate sourceAmounts Array
        while(unitKeys.hasMoreElements()) {
            UnitHolder unit = unitKeys.nextElement();
            List<Village> unitSources = pFakes.get(unit);
            logInfo(" - Starte Vorbereiten für Einheit '" + unit.getName() + "' (2/2)");
           
            if (unitSources != null && !unitSources.isEmpty()) {
                //build array of attack amount of each source
                sourceAmounts[troops.indexOf(unit)] = resolveDuplicates(unitSources, fakeSources);
            }
        }
        
        //build mappings of possible source-target combinations
        logText(" - Erstelle mögliche Herkunft-Ziel Kombinationen");
        mappings = buildMappings(fakeSources, pTargets, pTimeFrame, pMaxAttacksTable);
        for (int i = 0; i < mappings.length; i++) {
            for (int j = 0; j < mappings[0].length; j++) {
                Village target = pTargets.get(j);
                if (!movements.isEmpty()) {
                    TroopMovement movement = movements.get(target);
                    if (movement != null && mappings[i][j] != 0) {
                        int remainingCount = pMaxAttacksTable.get(target) - movement.getOffCount();
                        //  System.out.println("Remain for " + target + ": " + remainingCount);
                        mappings[i][j] = (remainingCount < 0) ? 0 : remainingCount;
                    }
                }
            }
        }
        
        if (!fakeSources.isEmpty()) {
            //initialize result array
            result = new int[mappings.length][mappings[0].length];
            try {
                long s1 = System.currentTimeMillis();
                logText(" - Suche optimale Herkunft-Ziel Kombinationen");
                round = 1;
                int maxMappings = countMappings(mappings);
                while (!solve(fakeSources, pTargets, mappings, result)) {
                    Thread.sleep(10);
                    int currentMappings = countMappings(mappings);
                    updateStatus(maxMappings - currentMappings, maxMappings);
                    logInfo("   * " + currentMappings + " von " + maxMappings + " verbleibende Kombinationen");
                    if (isAborted()) {
                        break;
                    }
                    if(LOG_TIMES) {
                        System.out.println(" Loop: " + (System.currentTimeMillis() - s1));
                    }
                }
                if(LOG_TIMES) {
                    System.out.println("solved: " + (System.currentTimeMillis() - s1));
                }
            } catch (Exception ewe) {
                logger.error("Unexpexted error during off calculation", ewe);
                logError("Unerwarteter Fehler bei der Off-Berechnung!");
                return new LinkedList<>();
            }
        }//end of fake assignment
        // </editor-fold>
        
        Hashtable<Village, TroopMovement> fakeMovements = new Hashtable<>();
        for (int i = 0; i < mappings.length; i++) {
            for (int j = 0; j < pTargets.size(); j++) {
                Village target = pTargets.get(j);
                TroopMovement movementForTarget = fakeMovements.get(target);
                if (movementForTarget == null) {
                    movementForTarget = new TroopMovement(target, pMaxAttacksTable.get(target), Attack.FAKE_TYPE);
                    fakeMovements.put(target, movementForTarget);
                }
                
                if (result[i][j] != 0) {
                    Village source = fakeSources.get(i % fakeSources.size());
                    movementForTarget.addOff(troops.get((int) i / fakeSources.size()), source);
                }
            }
        }
        targetKeys = fakeMovements.keys();
        while (targetKeys.hasMoreElements()) {
            Village target = targetKeys.nextElement();
            if(fakeMovements.get(target).getOffCount() > 0 || !movements.containsKey(target)) {
                movementList.add(fakeMovements.get(target));
            }
        }
        
        if(LOG_TIMES) {
            System.out.println("Overall " + (System.currentTimeMillis() - s));
        }

        return movementList;
    }

    /**Remove source-target mappings that are invalid in respect to the provided timeframe*/
    public void removeImpossibleSources(List<Village> pSources, List<Village> pTargets, TimeFrame pTimeFrame, UnitHolder pUnit) {
        int cnt = 0;
        int cntBefore = pSources.size();
        for (Village s : pSources.toArray(new Village[]{})) {
            boolean fail = true;
            for (Village t : pTargets) {
                // double dist = DSCalculator.calculateDistance(s, t);
                if (pUnit.getPlainName().equals("snob")) {
                    if (DSCalculator.calculateDistance(s, t) > ServerSettings.getSingleton().getSnobRange()) {
                        //continue with the next destination Village
                        continue;
                    }
                }
                
                double runtime = DSCalculator.calculateMoveTimeInSeconds(s, t, pUnit.getSpeed());
                // long send = pTimeFrame.getEnd() - (long) runtime * 1000;
                //if (pTimeFrame.inside(new Date(send), s.getTribe())) {
                long lRuntime = (long) runtime * 1000;
                if (pTimeFrame.isMovementPossible(lRuntime)) {
                    fail = false;
                    break;
                }
            }
            if (fail) {
                cnt++;
                //removing village that does not reach any target
                logInfo("   * Entferne Herkunftsdorf '" + s + "'");
                pSources.remove(s);
            }
        }
        logInfo("   * " + cnt + " von " + cntBefore + " Herkunftsdörfern entfernt");
    }

    /**Build possible source-target mappings*/
    public int[][] buildMappings(List<Village> pSources, List<Village> pTargets, TimeFrame pTimeFrame, Hashtable<Village, Integer> pMaxAttacksTable) {
        int[][] tMappings = new int[troops.size() * pSources.size()][pTargets.size()];
        
        int cnt = 0;
        for(int z = 0; z < troops.size(); z++) {
            for (int i = 0; i < pSources.size(); i++) {
                if (isAborted()) {
                    break;
                }
                for (int j = 0; j < pTargets.size(); j++) {
                    if (isAborted()) {
                        break;
                    }
                    
                    // double dist = DSCalculator.calculateDistance(s, t);
                    if (troops.get(z).getPlainName().equals("snob")) {
                        if (DSCalculator.calculateDistance(pSources.get(i),
                            pTargets.get(j)) > ServerSettings.getSingleton().getSnobRange()) {
                            tMappings[z * pSources.size() + i][j] = 0;
                            //continue with the next destination Village
                            continue;
                        }
                    }
                    
                    long runtime = Math.round(DSCalculator.calculateMoveTimeInSeconds(pSources.get(i),
                            pTargets.get(j), troops.get(z).getSpeed())  * 1000);
                    
                    if (pTimeFrame.isMovementPossible(runtime)) {
                        tMappings[z * pSources.size() + i][j] = pMaxAttacksTable.get(pTargets.get(j));
                        cnt++;
                    } else {
                        tMappings[z * pSources.size() + i][j] = 0;
                    }
                }
            }
        }
        
        int maxCount = troops.size() * pSources.size() * pTargets.size();
        logInfo("   * " + cnt + " von " + maxCount + " Herkunft-Ziel Kombinationen möglich");
        return tMappings;
    }

    public boolean solve(List<Village> pSources, List<Village> pTargets, int[][] pMappings, int[][] pResults) {
        //get next sources
        List<Integer> sourcesToTest = selectSources(pMappings);

        if (sourcesToTest.isEmpty()) {
            //no more sources available, matrix solved
            logInfo("   * Keine weiteren Angriffe möglich");
            return true;
        } else {
            logInfo("   * " + sourcesToTest.size() + " mögliche Bewegung(en) in Durchlauf " + round);
        }
        round++;
        //build temporary matrices
        int[][] temp = new int[mappings.length][mappings[0].length];
        int[][] tempResult = new int[mappings.length][mappings[0].length];

        int mostAttacks = 0;
        int mostAttacksAtIndex = 0;
        int influenceOfSelection = Integer.MAX_VALUE;
        long s = System.currentTimeMillis();
        
        //only try out wich path is best if there is more than one Possibility
        if(sourcesToTest.size() > 1) {
            //solve matrix for all current sources
            for (int i = 0; i < sourcesToTest.size(); i++) {
                //reset temporary data to current state
                for (int j = 0; j < mappings.length; j++) {
                    for (int k = 0; k < mappings[0].length; k++) {
                        temp[j][k] = mappings[j][k];
                        tempResult[j][k] = pResults[j][k];
                    }
                }
                //get amount of possible attacks for all sources
                int cntBefore = 0;
                int[] oldSources = buildSourceMappings(pMappings);
                for (Integer source : oldSources) {
                    cntBefore += source;
                }
                //solve full matrix for current source selection to get possible attacks at the end
                int possibleAttacks = solveRecursive(pSources, pTargets, temp, tempResult, i, true);
                //reset temporary data to current state
                for (int j = 0; j < mappings.length; j++) {
                    for (int k = 0; k < mappings[0].length; k++) {
                        temp[j][k] = mappings[j][k];
                        tempResult[j][k] = pResults[j][k];
                    }
                }
                //solve only first step again to get influence
                solveRecursive(pSources, pTargets, temp, tempResult, i, false);
                //get amount of possible attacks for all sources after current step
                int[] newSources = buildSourceMappings(temp);
                int cntAfter = 0;
                for (Integer source : newSources) {
                    cntAfter += source;
                }
                //calculate influence on possible attacks
                int currentInfluenceOfSelection = cntBefore - cntAfter;
                if (currentInfluenceOfSelection < 0) {
                    currentInfluenceOfSelection = 0;
                }
                //check if result is better than currently best result
                if (possibleAttacks > mostAttacks || currentInfluenceOfSelection < influenceOfSelection) {
                    mostAttacks = possibleAttacks;
                    influenceOfSelection = currentInfluenceOfSelection;
                    mostAttacksAtIndex = i;
                }
            }
        }
        solve += (System.currentTimeMillis() - s);
        //solve first step for best parameters
        mostAttacks = solveRecursive(pSources, pTargets, mappings, pResults, mostAttacksAtIndex, false);
        logInfo("   * Neuer Angriff: " + pSources.get(selectedSource) + " -> " + pTargets.get(selectedTarget));
        logInfo("   * " + mostAttacks + " bisher gefundene Angriffe");
        return false;
    }
    long solve = 0;
    long copy = 0;
    long map = 0;
    long swap = 0;

    public int solveRecursive(List<Village> pSources, List<Village> pTargets, int[][] pMappings, int[][] pResults, int pIndex, boolean recurse) {
        //get source list for solving
        List<Integer> idxs = selectSources(pMappings);
        if (idxs.size() < 1 || isAborted()) {
            return countResults(pResults);
        }
        //get target mappings
        int[] targetMappings = buildTargetMappings(pMappings);
        //choose target
        Integer sourceIdx = idxs.get(pIndex);
        //decision 2
        int targetID = -1;
        long s = System.currentTimeMillis();
        int lowestInfluence = Integer.MAX_VALUE;
        for (int i = 0; i < pMappings.length; i++) {
            for (int j = 0; j < pMappings[0].length; j++) {
                if (i == sourceIdx && pMappings[i][j] > 0) {
                    if (targetMappings[j] < lowestInfluence) {
                        targetID = j;
                        lowestInfluence = targetMappings[j];
                    }
                }
            }
        }
        copy += (System.currentTimeMillis() - s);
        s = System.currentTimeMillis();
        int[] resultSourceMappings = buildSourceMappings(result);
        map += (System.currentTimeMillis() - s);
        s = System.currentTimeMillis();
        for (int i = 0; i < pMappings.length; i++) {
            //update target col for current source row (decrement attacks to this target)
            int newValue = pMappings[i][targetID] - 1;
            pMappings[i][targetID] = (newValue > 0) ? newValue : 0;
            
            if(i == sourceIdx) {
                //block source-target combination for additional attacks
                pResults[sourceIdx][targetID]++;
                pMappings[sourceIdx][targetID] = 0;
                
                for (int j = 0; j < pMappings[i].length; j++) {
                    //last selected source positions
                    if (j != targetID) {
                        //update entire source row
                        if (resultSourceMappings[i] == sourceAmounts[(int) i / pSources.size()][i % pSources.size()] - 1) {
                            //all attacks from this village are planned, block all remaining source-target combinations
                            pMappings[i][j] = 0;
                        }
                    }
                }
            }
        }
        swap += (System.currentTimeMillis() - s);
        selectedSource = sourceIdx;
        selectedTarget = targetID;

        if (recurse && !isAborted()) {
            //solve until all attacks are assigned
            return solveRecursive(pSources, pTargets, pMappings, pResults, 0, recurse);
        } else {
            //do not recurse, finish here
            return countResults(pResults);
        }
    }

    //enable for one village to more targets (all targets can be filled)
    /**Select a list of next sources*/
    private List<Integer> selectSources(int[][] pMappings) {
        int[] sourceMappings = buildSourceMappings(pMappings);
        //find single mappings
        int smallesAmount = Integer.MAX_VALUE;
        List<Integer> idxs = new LinkedList<>();
        for (int i = 0; i < sourceMappings.length; i++) {
            if (sourceMappings[i] > 0) {
                //valid source
                if (sourceMappings[i] < smallesAmount) {
                    //lesser targets and lesser results
                    smallesAmount = sourceMappings[i];
                    //smallesResultCount = sourceResultMappings[i];
                    idxs.clear();
                    idxs.add(i);
                } else if (sourceMappings[i] == smallesAmount) {
                    //lesser targets and same results
                    idxs.add(i);
                }
            }
        }
        return idxs;
    }

    /**Remove duplicated villages from source list and return amount of each village*/
    private int[] resolveDuplicates(List<Village> pVillages, List<Village> all) {
        int[] amounts = new int[all.size()];
        for (Village v : pVillages) {
            amounts[all.indexOf(v)]++;
        }
        return amounts;
    }

    /**Count all elements inside one array*/
    private int countResults(int[][] pResult) {
        int count = 0;
        for (int[] aPResult : pResult) {
            for (int j = 0; j < pResult[0].length; j++) {
                count += aPResult[j];
            }
        }
        return count;
    }

    /**Count all elements inside one array*/
    private int countMappings(int[][] pMappings) {
        int count = 0;
        for (int[] pMapping : pMappings) {
            for (int j = 0; j < pMappings[0].length; j++) {
                count += (pMapping[j] != 0) ? 1 : 0;
            }
        }
        return count;
    }
    long sMap = 0;
    long tMap = 0;

    /**Build array of source (row) element sums*/
    private int[] buildSourceMappings(int[][] pMappings) {
        long s = System.currentTimeMillis();
        int[] sourceMappings = new int[pMappings.length];
        for (int i = 0; i < pMappings.length; i++) {
            //source rows
            int amount = 0;
            for (int j = 0; j < pMappings[0].length; j++) {
                //target cols
                amount += pMappings[i][j];
            }
            sourceMappings[i] = amount;
        }
        sMap += (System.currentTimeMillis() - s);
        return sourceMappings;
    }

    /**Build array of target (col) element sums*/
    private int[] buildTargetMappings(int[][] pMappings) {
        long s = System.currentTimeMillis();
        int[] targetMappings = new int[pMappings[0].length];
        for (int j = 0; j < pMappings[0].length; j++) {
            //target cols
            int amount = 0;
            for (int i = 0; i < pMappings.length; i++) {
                //source rows
                amount += pMappings[i][j];
            }
            targetMappings[j] = amount;
        }
        tMap += (System.currentTimeMillis() - s);
        return targetMappings;
    }
}
