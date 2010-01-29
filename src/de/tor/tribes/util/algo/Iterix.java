/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Off;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.algo.AlgorithmLogPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import java.awt.Color;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Torridity
 */
public class Iterix extends AbstractAttackAlgorithm {

    @Override
    public List<Village> getNotAssignedSources() {
        return new LinkedList<Village>();
    }
    private JLabel[][] labels;
    private JLabel[][] labels2;
    private double[][] mappings;
    private double[][] result;
    //  private List<Village> ramSources;
    private List<Village> targets;
    private Integer[] sourceAmounts;
    JFrame f;
    int selectedSource = 0;
    int selectedTarget = 0;
    private AlgorithmLogPanel mLogPanel = null;

    @Override
    public List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets) {
        mLogPanel = new AlgorithmLogPanel();

        mLogPanel.addText("Starte systematische Berechnung");
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        UnitHolder cata = DataHolder.getSingleton().getUnitByPlainName("catapult");
        List<Village> ramAndCataSources = pSources.get(ram);
        List<Village> cataSources = pSources.get(cata);
        if (cataSources != null && !cataSources.isEmpty()) {
            ramAndCataSources.addAll(cataSources);
        }
        if (!pTimeFrame.isVariableArriveTime()) {
            mLogPanel.addText(" - Entferne Herkunftsdörfer, die keins der Ziel erreichen können");
            //remove non-working sources if we use a fixed arrive time
            removeImpossibleSources(ramAndCataSources, pTargets, pTimeFrame);
        }
        if (ramAndCataSources.isEmpty()) {
            mLogPanel.addError("Keine Dörfer übrig, Berechnung wird abgebrochen.");
            return new LinkedList<AbstractTroopMovement>();
        }
        //build array of attack amount of each source
        sourceAmounts = resolveDuplicates(ramAndCataSources);
        //build mappings of possible source-target combinations
        mLogPanel.addText(" - Erstelle mögliche Herkunft-Ziel Kombinationen");
        mappings = buildMappings(ramAndCataSources, pTargets, pTimeFrame, pMaxAttacksPerVillage);
        //initialize result array
        result = new double[mappings.length][mappings[0].length];
        // <editor-fold defaultstate="collapsed" desc="Old stuff">

        // int[] sourceMappings = buildSourceMappings(mappings);
        // int[] targetMappings = buildTargetMappings(mappings);


        // mappingsToCSV(mappings, sourceMappings, targetMappings, "mappings.csv");
   /*     labels = new JLabel[mappings.length + 1][mappings[0].length + 1];
        labels2 = new JLabel[mappings.length][mappings[0].length];
        f = new JFrame();
        JPanel p = new JPanel();
        JPanel p2 = new JPanel();
        p.setLayout(new GridLayout(mappings.length + 1, mappings[0].length + 1));
        p2.setLayout(new GridLayout(mappings.length, mappings[0].length));
        for (int i = 0; i <= mappings.length; i++) {
        for (int j = 0; j <= mappings[0].length; j++) {
        JLabel l = new JLabel("0");
        l.setOpaque(true);
        labels[i][j] = l;
        p.add(l);
        try {
        JLabel l2 = new JLabel("0");
        l2.setOpaque(true);
        labels2[i][j] = l2;
        p2.add(l2);
        } catch (Exception e) {
        }

        }
        }
        f.setLayout(new GridLayout(2, 2));
        f.add(p);
        f.add(p2);

        JButton b = new JButton("Calc");

        b.addMouseListener(new MouseListener() {

        @Override
        public void mouseClicked(MouseEvent e) {*/

        // </editor-fold>

        try {
            //long s = System.currentTimeMillis();
            mLogPanel.addText(" - Suche optimale Herkunft-Ziel Kombinationen");
            while (!solve(ramAndCataSources, pTargets, mappings, result)) {
                Thread.sleep(10);
                //System.out.println(" Loop: " + (System.currentTimeMillis() - s));
            }
            //System.out.println("solved: " + (System.currentTimeMillis() - s));
            /* int[] sourceMappings = buildSourceMappings(mappings);
            int[] targetMappings = buildTargetMappings(mappings);
            drawResults(sourceMappings, targetMappings);
            colorSelectedValues(selectedSource, selectedTarget);*/
        } catch (Exception ewe) {
            ewe.printStackTrace();
            mLogPanel.addError("Unerwarteter Fehler bei der Berechnung!");
            return new LinkedList<AbstractTroopMovement>();
        }

// <editor-fold defaultstate="collapsed" desc="Old stuff">
        //solve2(ramSources, targets, mappings, result);
/*              solve(ramSources, targets, mappings, result);
        int[] sourceMappings = buildSourceMappings(mappings);
        int[] targetMappings = buildTargetMappings(mappings);
        drawResults(sourceMappings, targetMappings);
        colorSelectedValues(selectedSource, selectedTarget);
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
        });
        f.add(b);
        f.pack();
        f.setVisible(true);

        //solve(ramSources, pTargets, mappings, result);
        for (int i = 0; i < mappings.length; i++) {
        for (int j = 0; j < mappings[0].length; j++) {
        labels[i][j].setText("" + mappings[i][j]);
        labels2[i][j].setText("" + result[i][j]);
        }
        }
        //init view
        sourceMappings = buildSourceMappings(mappings);
        targetMappings = buildTargetMappings(mappings);
        drawResults(sourceMappings, targetMappings);
         */
// </editor-fold>

        mLogPanel.addText(" - Erstelle Ergebnisliste");
        //store results
        Hashtable<Village, Off> movements = new Hashtable<Village, Off>();
        for (int i = 0; i < ramAndCataSources.size(); i++) {
            for (int j = 0; j < pTargets.size(); j++) {
                if (result[i][j] != 0) {
                    Village source = ramAndCataSources.get(i);
                    Village target = pTargets.get(j);
                    Off movementForTarget = movements.get(target);
                    if (movementForTarget == null) {
                        movementForTarget = new Off(target, pMaxAttacksPerVillage);
                        movements.put(target, movementForTarget);
                    }
                    movementForTarget.addOff(ram, source);
                }
            }
        }

        //set result movements and remove used targets if needed
        List<AbstractTroopMovement> movementList = new LinkedList<AbstractTroopMovement>();
        Enumeration<Village> targetKeys = movements.keys();
        while (targetKeys.hasMoreElements()) {
            Village target = targetKeys.nextElement();
            if (!pFakeOffTargets) {
                pTargets.remove(target);
            }
            movementList.add(movements.get(target));
        }
        //assign fakes
        //@TODO Check if faking existing source-target could happen/if checking for it makes sense
        List<Village> ramAndCataFakes = pFakes.get(ram);
        List<Village> cataFakes = pFakes.get(cata);
        if (cataFakes != null && !cataFakes.isEmpty()) {
            ramAndCataFakes.addAll(cataFakes);
        }

        if (ramAndCataFakes == null || ramAndCataFakes.isEmpty()) {
            mLogPanel.addText("Berechnung abgeschlossen.");
            JFrame jf = new JFrame();
            jf.add(mLogPanel);
            jf.pack();
            jf.setVisible(true);
            return movementList;
        }
        if (!pTimeFrame.isVariableArriveTime()) {
            //remove non-working sources if we use a fixed arrive time
            removeImpossibleSources(ramAndCataFakes, pTargets, pTimeFrame);
        }
        sourceAmounts = resolveDuplicates(ramAndCataFakes);
        mappings = buildMappings(ramAndCataFakes, pTargets, pTimeFrame, pMaxAttacksPerVillage);
        result = new double[mappings.length][mappings[0].length];
        try {
//            long s = System.currentTimeMillis();
            while (!solve(ramAndCataFakes, pTargets, mappings, result)) {
                Thread.sleep(10);
                //              System.out.println(" Loop: " + (System.currentTimeMillis() - s));
            }
            //        System.out.println("solved: " + (System.currentTimeMillis() - s));
            /* int[] sourceMappings = buildSourceMappings(mappings);
            int[] targetMappings = buildTargetMappings(mappings);
            drawResults(sourceMappings, targetMappings);
            colorSelectedValues(selectedSource, selectedTarget);*/
        } catch (Exception ewe) {
            ewe.printStackTrace();
        }
        Hashtable<Village, Fake> fakeMovements = new Hashtable<Village, Fake>();
        for (int i = 0; i < ramAndCataFakes.size(); i++) {
            for (int j = 0; j < pTargets.size(); j++) {
                if (result[i][j] != 0) {
                    Village source = ramAndCataFakes.get(i);
                    Village target = pTargets.get(j);
                    Fake movementForTarget = fakeMovements.get(target);
                    if (movementForTarget == null) {
                        movementForTarget = new Fake(target, pMaxAttacksPerVillage);
                        fakeMovements.put(target, movementForTarget);
                    }
                    movementForTarget.addOff(ram, source);
                }
            }
        }
        targetKeys = fakeMovements.keys();
        while (targetKeys.hasMoreElements()) {
            Village target = targetKeys.nextElement();
            movementList.add(movements.get(target));
        }

        JFrame jf = new JFrame();
        jf.add(f);
        jf.pack();
        jf.setVisible(true);

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
                double dist = DSCalculator.calculateDistance(s, t);
                double runtime = dist * ram.getSpeed() * 60000;
                long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                if (pTimeFrame.inside(new Date(send), null)) {
                    fail = false;
                    break;
                }
            }
            if (fail) {
                cnt++;
                //removing village that does not reach any target
                mLogPanel.addInfo("   * Entferne Herkunftsdorf '" + s + "'");
                pSources.remove(s);
            }
        }
        mLogPanel.addInfo("   * " + cnt + " von " + cntBefore + " Herkunftsdörfern entfernt");
    }

    /**Build possible source-target mappings*/
    public double[][] buildMappings(List<Village> pSources, List<Village> pTargets, TimeFrame pTimeFrame, int pMaxAttacksPerVillage) {
        double[][] tMappings = new double[pSources.size()][pTargets.size()];
        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        int cnt = 0;
        List<Long> usedDates = new LinkedList<Long>();
        for (int i = 0; i < pSources.size(); i++) {
            for (int j = 0; j < pTargets.size(); j++) {
                double dist = DSCalculator.calculateDistance(pSources.get(i), pTargets.get(j));
                double runtime = dist * ram.getSpeed() * 60000;
                if (pTimeFrame.isVariableArriveTime()) {
                    Date arriveTime = pTimeFrame.getRandomArriveTime(Math.round(runtime), pSources.get(i).getTribe(), usedDates);
                    if (arriveTime != null) {
                        tMappings[i][j] = pMaxAttacksPerVillage;
                        usedDates.add(arriveTime.getTime());
                        cnt++;
                    } else {
                        tMappings[i][j] = 0;
                    }
                } else {
                    long send = pTimeFrame.getEnd() - (long) Math.round(runtime);
                    if (pTimeFrame.inside(new Date(send), null)) {
                        tMappings[i][j] = pMaxAttacksPerVillage;
                        cnt++;
                    } else {
                        tMappings[i][j] = 0;
                    }
                }
            }
        }


        int[] s = buildSourceMappings(tMappings);
        int c = 0;
        for (Integer i : s) {
            if (i != 0) {
                c++;
            }
        }

        int maxCount = pSources.size() * pTargets.size();
        mLogPanel.addInfo("   * " + cnt + " von " + maxCount + " Herkunft-Ziel Kombinationen möglich");
        mLogPanel.addInfo("   * " + c + " von " + pSources.size() + " Herkunftsdörfer werden verwendet");
        return tMappings;
    }

    public boolean solve(List<Village> pSources, List<Village> pTargets, double[][] pMappings, double[][] pResults) {
        //get next sources
        List<Integer> sourcesToTest = selectSources(pMappings, pResults);

        if (sourcesToTest.isEmpty()) {
            //no more sources available, matrix solved
            mLogPanel.addInfo("   * Keine weiteren Angriffe möglich");
            return true;
        }

        //build temporary matrices
        double[][] temp = new double[mappings.length][mappings[0].length];
        double[][] tempResult = new double[mappings.length][mappings[0].length];

        int mostAttacks = 0;
        int mostAttacksAtIndex = 0;
        int influenceOfSelection = Integer.MAX_VALUE;

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
            //solve full matrix for
            int possibleAttacks = solveRecursive(pSources, pTargets, temp, tempResult, i, true);
            //reset temporary data to current state
            for (int j = 0; j < mappings.length; j++) {
                for (int k = 0; k < mappings[0].length; k++) {
                    temp[j][k] = mappings[j][k];
                    tempResult[j][k] = pResults[j][k];
                }
            }
            //solve only current step again
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
        mostAttacks = solveRecursive(pSources, pTargets, mappings, pResults, mostAttacksAtIndex, false);
        mLogPanel.addInfo("   * " + mostAttacks + " bisher gefundene Angriffe");
        //System.out.println("CurrentResults: " + mostAttacks);
        return false;
    }

    public int solveRecursive(List<Village> pSources, List<Village> pTargets, double[][] pMappings, double[][] pResults, int pIndex, boolean recurse) {
        //get source list for solving
        List<Integer> idxs = selectSources(pMappings, pResults);
        if (idxs.size() < 1) {
            return countResults(pResults);
        }
        //get target mappings
        int[] targetMappings = buildTargetMappings(pMappings);
        //choose target
        Integer sourceIdx = idxs.get(pIndex);
        //decision 2
        int targetID = -1;
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

        int[] resultSourceMappings = buildSourceMappings(result);
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
                        pResults[i][j] = pResults[i][j] + 1;
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

        selectedSource = sourceIdx;
        selectedTarget = targetID;
        if (recurse) {
            //solve until all attacks are assigned
            return solveRecursive(pSources, pTargets, pMappings, pResults, 0, recurse);
        } else {
            //do not recurse, finish here
            return countResults(pResults);
        }
    }

    //enable for one village at one target, probably not all targets are used
  /*  private List<Integer> selectSources(double[][] pMappings, double[][] pResults) {
    int[] sourceMappings = buildSourceMappings(pMappings);
    int[] sourceResultMappings = buildSourceMappings(pResults);
    //find single mappings
    int smallesAmount = Integer.MAX_VALUE;
    //set to 0 to avoid more than one attacks per village
    int smallesResultCount = Integer.MAX_VALUE;
    List<Integer> idxs = new LinkedList<Integer>();
    for (int i = 0; i < sourceMappings.length; i++) {
    if (sourceMappings[i] > 0) {
    //valid source
    if (sourceMappings[i] < smallesAmount) {
    //lesser targets than worst source
    if (sourceResultMappings[i] < smallesResultCount) {
    //lesser targets and lesser results
    smallesAmount = sourceMappings[i];
    smallesResultCount = sourceResultMappings[i];
    idxs.clear();
    idxs.add(i);
    } else if (sourceResultMappings[i] == smallesResultCount) {
    //lesser targets and same results
    idxs.add(i);
    } else {
    //lesser targets but at least one result...ignore
    }
    } else {
    //more targets than worst village
    if (sourceResultMappings[i] < smallesResultCount) {
    //more targets but lesser results
    smallesAmount = sourceMappings[i];
    smallesResultCount = sourceResultMappings[i];
    idxs.clear();
    idxs.add(i);
    } else if (sourceResultMappings[i] == smallesResultCount) {
    idxs.add(i);
    } else {
    //more targets and more results
    }
    }
    }
    }
    if (idxs.isEmpty()) {
    System.out.println("EMPTY");
    }
    return idxs;
    }*/
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

    /**Build array of source (row) element sums*/
    private int[] buildSourceMappings(double[][] pMappings) {
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

        return sourceMappings;
    }

    /**Build array of target (col) element sums*/
    private int[] buildTargetMappings(double[][] pMappings) {
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
        for (int i = 0; i <
                mappings.length; i++) {
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
}


