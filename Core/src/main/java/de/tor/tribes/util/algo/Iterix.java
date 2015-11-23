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
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Off;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import java.awt.Color;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class Iterix extends AbstractAttackAlgorithm {

    private static Logger logger = Logger.getLogger("SystematicAlgorithm");
    private JLabel[][] labels;
    private JLabel[][] labels2;
    private double[][] mappings;
    private double[][] result;
    //  private List<Village> ramSources;
    // private List<Village> targets;
    private Integer[] sourceAmounts;
    JFrame f;
    int selectedSource = 0;
    int selectedTarget = 0;
    int round = 0;

    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            List<Village> pFakeTargets,
            Hashtable<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets) {
        logText("Starte systematische Berechnung");
        logText("Berechne Offs");
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        UnitHolder cata = DataHolder.getSingleton().getUnitByPlainName("catapult");
        List<Village> ramAndCataSources = pSources.get(ram);
        if (ramAndCataSources == null || ramAndCataSources.isEmpty()) {
            ramAndCataSources = new LinkedList<Village>();
        }
        List<Village> cataSources = pSources.get(cata);
        if (cataSources != null && !cataSources.isEmpty()) {
            ramAndCataSources.addAll(cataSources);
        }
        Hashtable<Village, Off> movements = new Hashtable<Village, Off>();
        List<AbstractTroopMovement> movementList = new LinkedList<AbstractTroopMovement>();
        if (!ramAndCataSources.isEmpty()) {
            //off sources are available
            logText(" - Entferne Herkunftsdörfer, die keins der Ziel erreichen können");
            //remove non-working sources if we use a fixed arrive time
            removeImpossibleSources(ramAndCataSources, pTargets, pTimeFrame);
            if (ramAndCataSources.isEmpty()) {
                logError("Keine Dörfer übrig, Berechnung wird abgebrochen.");
                return new LinkedList<AbstractTroopMovement>();
            }
            //build array of attack amount of each source
            sourceAmounts = resolveDuplicates(ramAndCataSources);
            //build mappings of possible source-target combinations
            logText(" - Erstelle mögliche Herkunft-Ziel Kombinationen");
            mappings = buildMappings(ramAndCataSources, pTargets, pTimeFrame, pMaxAttacksTable);
            //initialize result array
            result = new double[mappings.length][mappings[0].length];
            try {
                //long s = System.currentTimeMillis();
                logText(" - Suche optimale Herkunft-Ziel Kombinationen");
                round = 1;
                int maxMappings = countMappings(mappings);
                while (!solve(ramAndCataSources, pTargets, mappings, result)) {
                    Thread.sleep(10);
                    int currentMappings = countMappings(mappings);
                    updateStatus(maxMappings - currentMappings, maxMappings);
                    logInfo("   * " + currentMappings + " von " + maxMappings + " verbleibende Kombinationen");
                    if (isAborted()) {
                        break;
                    }
                    //System.out.println(" Loop: " + (System.currentTimeMillis() - s));
                }
                //System.out.println("solved: " + (System.currentTimeMillis() - s));
            /* int[] sourceMappings = buildSourceMappings(mappings);
                int[] targetMappings = buildTargetMappings(mappings);
                drawResults(sourceMappings, targetMappings);
                colorSelectedValues(selectedSource, selectedTarget);*/
            } catch (Exception ewe) {
                logger.error("Unexpexted error during off calculation", ewe);
                logError("Unerwarteter Fehler bei der Off-Berechnung!");
                return new LinkedList<AbstractTroopMovement>();
            }

            logText(" - Erstelle Ergebnisliste");
            //store results
            for (int i = 0; i < ramAndCataSources.size(); i++) {
                for (int j = 0; j < pTargets.size(); j++) {
                    if (result[i][j] != 0) {
                        Village source = ramAndCataSources.get(i);
                        Village target = pTargets.get(j);
                        Off movementForTarget = movements.get(target);
                        if (movementForTarget == null) {
                            movementForTarget = new Off(target, pMaxAttacksTable.get(target));
                            movements.put(target, movementForTarget);
                        }
                        movementForTarget.addOff(ram, source);
                    } else {
                        Village target = pTargets.get(j);
                        Off movementForTarget = movements.get(target);
                        if (movementForTarget == null) {
                            movementForTarget = new Off(target, pMaxAttacksTable.get(target));
                            movements.put(target, movementForTarget);
                        }
                    }
                }
            }

            //set result movements and remove used targets if needed

            Enumeration<Village> targetKeys = movements.keys();
            while (targetKeys.hasMoreElements()) {
                Village target = targetKeys.nextElement();
                if (!pFakeOffTargets) {
                    pTargets.remove(target);
                }
                movementList.add(movements.get(target));
            }
        }//end of off assignment

        if (isAborted()) {
            return movementList;
        }

        if (!pFakeOffTargets) {
            //remove all off targets
            pTargets.clear();
        }

        //add fake targets
        for (Village fakeTarget : pFakeTargets) {
            pTargets.add(fakeTarget);
        }

        //assign fakes
        logText("Berechne Fakes");
        List<Village> ramAndCataFakes = pFakes.get(ram);
        if (ramAndCataFakes == null || ramAndCataFakes.isEmpty()) {
            ramAndCataFakes = new LinkedList<Village>();
        }
        List<Village> cataFakes = pFakes.get(cata);
        if (cataFakes != null && !cataFakes.isEmpty()) {
            ramAndCataFakes.addAll(cataFakes);
        }
        /* System.out.println("Copy " + copy);
        System.out.println("Map " + map);
        System.out.println("SMap " + sMap);
        System.out.println("TMap " + tMap);
        System.out.println("Swap " + swap);
        System.out.println("Solve " + solve);*/
        if (ramAndCataFakes == null || ramAndCataFakes.isEmpty()) {
            logText("Keine gültigen Fakes gefunden. Berechnung abgeschlossen.");
            return movementList;
        }
        //      if (!pTimeFrame.isVariableArriveTime()) {
        //remove non-working sources if we use a fixed arrive time
        removeImpossibleSources(ramAndCataFakes, pTargets, pTimeFrame);
        //    }
        sourceAmounts = resolveDuplicates(ramAndCataFakes);
        mappings = buildMappings(ramAndCataFakes, pTargets, pTimeFrame, pMaxAttacksTable);
        for (int i = 0; i < mappings.length; i++) {
            for (int j = 0; j < mappings[0].length; j++) {
                Village target = pTargets.get(j);
                if (!movements.isEmpty()) {
                    Off movement = movements.get(target);
                    if (movement != null && mappings[i][j] != 0) {
                        int remainingCount = pMaxAttacksTable.get(target) - movement.getOffCount();
                        //  System.out.println("Remain for " + target + ": " + remainingCount);
                        mappings[i][j] = (remainingCount < 0) ? 0 : remainingCount;
                    }
                }
            }
        }

        result = new double[mappings.length][mappings[0].length];
        try {
//            long s = System.currentTimeMillis();
            logText(" - Suche optimale Herkunft-Ziel Kombinationen");
            int maxMappings = countMappings(mappings);
            while (!solve(ramAndCataFakes, pTargets, mappings, result)) {
                Thread.sleep(10);
                int currentMappings = countMappings(mappings);
                updateStatus(maxMappings - currentMappings, maxMappings);
                logInfo("   * " + currentMappings + " von " + maxMappings + " verbleibende Kombinationen");
                if (isAborted()) {
                    break;
                    //              System.out.println(" Loop: " + (System.currentTimeMillis() - s));
                }
            }
            //        System.out.println("solved: " + (System.currentTimeMillis() - s));
            /* int[] sourceMappings = buildSourceMappings(mappings);
            int[] targetMappings = buildTargetMappings(mappings);
            drawResults(sourceMappings, targetMappings);
            colorSelectedValues(selectedSource, selectedTarget);*/
        } catch (Exception ewe) {
            logger.error("Unexpexted error during fake calculation", ewe);
            logError("Unerwarteter Fehler bei der Fake-Berechnung!");
            return new LinkedList<AbstractTroopMovement>();
        }
        Hashtable<Village, Fake> fakeMovements = new Hashtable<Village, Fake>();
        for (int i = 0; i < ramAndCataFakes.size(); i++) {
            for (int j = 0; j < pTargets.size(); j++) {
                if (result[i][j] != 0) {
                    Village source = ramAndCataFakes.get(i);
                    Village target = pTargets.get(j);
                    Fake movementForTarget = fakeMovements.get(target);
                    if (movementForTarget == null) {
                        movementForTarget = new Fake(target, pMaxAttacksTable.get(target));
                        fakeMovements.put(target, movementForTarget);
                    }
                    movementForTarget.addOff(ram, source);
                } else {
                    Village target = pTargets.get(j);
                    Fake movementForTarget = fakeMovements.get(target);
                    if (movementForTarget == null) {
                        movementForTarget = new Fake(target, pMaxAttacksTable.get(target));
                        fakeMovements.put(target, movementForTarget);
                    }
                }
            }
        }
        Enumeration<Village> targetKeys = fakeMovements.keys();
        while (targetKeys.hasMoreElements()) {
            Village target = targetKeys.nextElement();
            movementList.add(fakeMovements.get(target));
        }

        /*JFrame jf = new JFrame();
        jf.add(f);
        jf.pack();
        jf.setVisible(true);*/

        return movementList;
    }

    /**Remove source-target mappings that are invalid in respect to the provided timeframe*/
    public void removeImpossibleSources(List<Village> pSources, List<Village> pTargets, TimeFrame pTimeFrame) {
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        int cnt = 0;
        int cntBefore = pSources.size();
        for (Village s : pSources.toArray(new Village[]{})) {
            boolean fail = true;
            for (Village t : pTargets) {
                // double dist = DSCalculator.calculateDistance(s, t);
                double runtime = DSCalculator.calculateMoveTimeInSeconds(s, t, ram.getSpeed());
                // long send = pTimeFrame.getEnd() - (long) runtime * 1000;
                //if (pTimeFrame.inside(new Date(send), s.getTribe())) {
                long lRuntime = (long) runtime * 1000;
                if (pTimeFrame.isMovementPossible(lRuntime, s)) {
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
    public double[][] buildMappings(List<Village> pSources, List<Village> pTargets, TimeFrame pTimeFrame, Hashtable<Village, Integer> pMaxAttacksTable) {
        double[][] tMappings = new double[pSources.size()][pTargets.size()];
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        int cnt = 0;
        for (int i = 0; i < pSources.size(); i++) {
            if (isAborted()) {
                break;
            }
            for (int j = 0; j < pTargets.size(); j++) {
                if (isAborted()) {
                    break;
                }
                double dist = DSCalculator.calculateDistance(pSources.get(i), pTargets.get(j));
                long runtime = Math.round(dist * ram.getSpeed() * 60000);

                if (pTimeFrame.isMovementPossible(runtime, pSources.get(i))) {
                    tMappings[i][j] = pMaxAttacksTable.get(pTargets.get(j));
                    cnt++;
                } else {
                    tMappings[i][j] = 0;
                }
            }
        }


        /*int[] s = buildSourceMappings(tMappings);
        int c = 0;
        for (Integer i : s) {
        if (i != 0) {
        c++;
        }
        }*/

        int maxCount = pSources.size() * pTargets.size();
        logInfo("   * " + cnt + " von " + maxCount + " Herkunft-Ziel Kombinationen möglich");
        // logInfo("   * " + c + " von " + pSources.size() + " Herkunftsdörfer werden verwendet");
        return tMappings;
    }

    public boolean solve(List<Village> pSources, List<Village> pTargets, double[][] pMappings, double[][] pResults) {
        //get next sources
        List<Integer> sourcesToTest = selectSources(pMappings, pResults);

        if (sourcesToTest.isEmpty()) {
            //no more sources available, matrix solved
            logInfo("   * Keine weiteren Angriffe möglich");
            return true;
        } else {
            logInfo("   * " + sourcesToTest.size() + " mögliche Ziele in Durchlauf " + round);
        }
        round++;
        //build temporary matrices
        double[][] temp = new double[mappings.length][mappings[0].length];
        double[][] tempResult = new double[mappings.length][mappings[0].length];

        int mostAttacks = 0;
        int mostAttacksAtIndex = 0;
        int influenceOfSelection = Integer.MAX_VALUE;
        long s = System.currentTimeMillis();
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

    public int solveRecursive(List<Village> pSources, List<Village> pTargets, double[][] pMappings, double[][] pResults, int pIndex, boolean recurse) {
        //get source list for solving
        List<Integer> idxs = selectSources(pMappings, pResults);
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
                    if (targetMappings[j] < lowestInfluence && lowestInfluence > 0) {
                        targetID = j;
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
            double newValue = pMappings[i][targetID] - 1.0;
            pMappings[i][targetID] = (newValue > 0.0) ? newValue : 0.0;
            for (int j = 0; j < pMappings[i].length; j++) {
                //update target cols
                if (i == sourceIdx || j == targetID) {
                    //last selected source positions
                    if (i == sourceIdx && j == targetID) {
                        //block source-target combination for additional attacks
                        pResults[i][j]++;
                        pMappings[i][j] = 0.0;
                    } else if (i == sourceIdx && j != targetID) {
                        //update entire source row
                        if (resultSourceMappings[i] == sourceAmounts[i] - 1) {
                            //all attacks from this village are planned, block all remaining source-target combinations
                            pMappings[i][j] = 0.0;
                        } else {
                            //attacks from source are still available, decrement amount
                            newValue = pMappings[i][j] - 1.0;
                            pMappings[i][j] = (newValue > 0.0) ? newValue : 0.0;
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
    private List<Integer> selectSources(double[][] pMappings, double[][] pResults) {
        int[] sourceMappings = buildSourceMappings(pMappings);
        //find single mappings
        int smallesAmount = Integer.MAX_VALUE;
        List<Integer> idxs = new LinkedList<Integer>();
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
                idxs.add(i);
            }
        }
        return idxs;
    }

    /**Remove duplicated villages from source list and return amount of each village*/
    private Integer[] resolveDuplicates(List<Village> pVillages) {
        List<Village> processed = new LinkedList<Village>();
        List<Integer> amounts = new LinkedList<Integer>();
        for (Village v : pVillages) {
            if (!processed.contains(v)) {
                //add new village
                processed.add(v);
                amounts.add(1);
            } else {
                //increment amount
                int idx = processed.indexOf(v);
                amounts.set(idx, amounts.get(idx) + 1);
            }
        }
        pVillages.clear();
        for (Village v : processed) {
            pVillages.add(v);
        }
        return amounts.toArray(new Integer[]{});
    }

    /**Count all elements inside one array*/
    private int countResults(double[][] pResult) {
        int count = 0;
        for (int i = 0; i < pResult.length; i++) {
            for (int j = 0; j < pResult[0].length; j++) {
                count += pResult[i][j];
            }
        }
        return count;
    }

    /**Count all elements inside one array*/
    private int countMappings(double[][] pMappings) {
        int count = 0;
        for (int i = 0; i < pMappings.length; i++) {
            for (int j = 0; j < pMappings[0].length; j++) {
                count += (pMappings[i][j] != 0) ? 1 : 0;
            }
        }
        return count;
    }
    long sMap = 0;
    long tMap = 0;

    /**Build array of source (row) element sums*/
    private int[] buildSourceMappings(double[][] pMappings) {
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
    private int[] buildTargetMappings(double[][] pMappings) {
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

    private void drawResults(int[] sourceMappings, int[] targetMappings) {
        for (int i = 0; i < mappings.length; i++) {
            for (int j = 0; j < mappings[0].length; j++) {
                labels[i][j].setText("" + mappings[i][j]);
                labels2[i][j].setText("" + result[i][j]);
            }

        }

        for (int i = 0; i < sourceMappings.length; i++) {
            labels[i][mappings[0].length].setBackground(Color.GREEN);
            labels[i][mappings[0].length].setText("" + sourceMappings[i]);
        }

        for (int i = 0; i < targetMappings.length; i++) {
            labels[mappings.length][i].setBackground(Color.GREEN);
            labels[mappings.length][i].setText("" + targetMappings[i]);
        }

    }

    private void colorSelectedValues(int pSourceIdx, int pTargetIdx) {
        for (int i = 0; i < mappings.length; i++) {
            for (int j = 0; j < mappings[0].length; j++) {
                if (i == pSourceIdx || j == pTargetIdx) {
                    labels[i][j].setBackground(Color.BLUE);
                    labels2[i][j].setBackground(Color.BLUE);
                } else {
                    labels[i][j].setBackground(Constants.DS_BACK);
                    labels2[i][j].setBackground(Constants.DS_BACK);
                }

                if (i == pSourceIdx && j == pTargetIdx) {
                    labels[i][j].setBackground(Color.RED);
                    labels2[i][j].setBackground(Color.RED);
                }

                if (result[i][j] >= 1) {
                    labels2[i][j].setBackground(Color.MAGENTA);
                }

            }
        }
    }

    public void mappingsToCSV(double[][] pMappings, int[] pSourceMappings, int[] pTargetMappings, String pFile) {
        try {
            FileWriter w = new FileWriter(pFile);
            for (int i = 0; i < pMappings.length; i++) {
                String line = "";
                for (int j = 0; j < pMappings[0].length; j++) {
                    line += pMappings[i][j] + ";";
                }

                line = line + pSourceMappings[i] + "\n";
                w.write(line);
            }

            String line = "";
            for (int mapping : pTargetMappings) {
                line += mapping + ";";
            }

            line = line.substring(0, line.length() - 1) + "\n";
            w.write(line);
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean test(int[][] data) {
        int x = (int) (9.0 * Math.random());
        int y = (int) (9.0 * Math.random());

        data[x][y] = (int) Math.rint(10.0 * Math.random());
        int sum = sum(data);
        System.out.println("Added " + data[x][y]);
        System.out.println("Sum: " + sum);
        //  print(data);
        System.out.println("===============");

        if (sum < 100 && sum % 2 == 0) {
            System.out.println("Taking sum " + sum + " and going to next level...");
            boolean res = test(data);
            if (res) {
                System.out.println("Finished in Iteration");
                return true;
            }
        } else if (sum == 100) {
            System.out.println("Finished");
            return true;
        }
        System.out.println("Reset " + x + "/" + y + " at sum " + sum);
        data[x][y] = 0;
        return test(data);
    }

    private int sum(int[][] data) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                sum += data[i][j];
            }
        }
        return sum;
    }

    private void print(int[][] data) {
        for (int i = 0; i < 10; i++) {
            String row = "";
            for (int j = 0; j < 10; j++) {
                row += data[i][j] + " ";
            }
            System.out.println(row);
        }
    }

    public static void main(String[] args) {
        int[][] data = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                data[i][j] = 0;
            }
        }
        new Iterix().test(data);
    }
}
