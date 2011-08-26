/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Off;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.DSCalculator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class Recurrection extends AbstractAttackAlgorithm {

    @Override
    public List<AbstractTroopMovement> calculateAttacks(Hashtable<UnitHolder, List<Village>> pSources, Hashtable<UnitHolder, List<Village>> pFakes, List<Village> pTargets, List<Village> pFakeTargets, Hashtable<Village, Integer> pMaxAttacksTable, TimeFrame pTimeFrame, boolean pFakeOffTargets) {

        UnitHolder ram = DataHolder.getSingleton().getUnitByPlainName("ram");
        List<Village> sources = pSources.get(ram);
        List<Village> targets = pTargets;
        int[][] combinations = new int[sources.size()][targets.size()];
        int[][] result = new int[sources.size()][targets.size()];

        for (int i = 0; i < sources.size(); i++) {
            Village source = sources.get(i);
            for (int j = 0; j < targets.size(); j++) {
                Village target = targets.get(j);
                long run = (long) DSCalculator.calculateMoveTimeInSeconds(source, target, ram.getSpeed()) * 1000;
                if (pTimeFrame.isMovementPossible(run, null)) {
                    combinations[i][j] = 1;
                } else {
                    combinations[i][j] = 0;
                }
                result[i][j] = 0;
            }
        }
        int[] targetUsages = new int[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            targetUsages[i] = 1;

        }

        solve(combinations, result, targetUsages, 0, 0);
        List<AbstractTroopMovement> movements = new LinkedList<AbstractTroopMovement>();

        for (int j = 0; j < targets.size(); j++) {
            Off o = new Off(targets.get(j), targetUsages[j]);
            for (int i = 0; i < sources.size(); i++) {
                if (result[i][j] == 1) {
                    o.addOff(ram, sources.get(i));
                }
            }
            if (o.offComplete()) {
                movements.add(o);
            }
        }
        return movements;

    }
    long count = 0;

    private int solve(int[][] combinations, int[][] result, int[] maxTargets, int startX, int startY) {
        for (int row = startX; row < combinations.length; row++) {
            for (int col = startY; col < combinations[0].length;) {
                //use field
              //  System.out.println("Inside " + row + "/" + col);
                if (combinations[row][col] != 0) {
                    //check if we can set it
                    if (canSet(row, result) && canSet(col, maxTargets[col], result)) {
                        result[row][col] = 1;
                        System.out.println("New:");
                        print(result);
                        System.out.println("..........");
                        int cnt = solve(combinations, result, maxTargets, row, col);
                        System.out.println("Current count: " + count);
                        System.out.println("New: " + cnt);

                        if (cnt < count) {
                            //cnt is smaller, so go back one step
                            System.out.println("Smaller:");
                            print(result);
                            System.out.println("======");
                            result[row][col] = 0;
                            combinations[row][col] = 1;
                            // if (colreset) {
                            col--;
                            // }
                        } else {
                            System.out.println("Set new max " + cnt);
                            count = cnt;
                            //col++;
                        }
                    } else {
                        //goto next due to unusable field
                       // System.out.println("Can't");
                        col++;
                    }
                } else {
                   // System.out.println("Zero");
                    //goto next due to zero field
                    col++;
                }
            }
        }

        //     print(result);
        //       System.out.println("-----");
        System.out.println("Leaving");
        System.out.println("-----");
        return countMovements(result);
    }

    private boolean canSet(int row, int[][] data) {
        for (int i = 0; i < data[0].length; i++) {
            if (data[row][i] != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean canSet(int col, int max, int[][] data) {
        for (int i = 0; i < data.length; i++) {
            if (data[i][col] == max) {
                return false;
            }
        }
        return true;
    }

    private int countMovements(int[][] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                sum += data[i][j];
            }
        }
        return sum;
    }

    private static void print(int[][] data) {
        for (int i = 0; i < data.length; i++) {
            String row = "";
            for (int j = 0; j < data[0].length; j++) {
                row += data[i][j] + " ";
            }
         //   System.out.println(row);
        }
    }

    public static void main(String[] args) {
        int x = 10;
        int y = 10;

        int[][] data = new int[x][y];
        int[][] data2 = new int[x][y];
        int[][] result = new int[x][y];
        int[][] result2 = new int[x][y];
        int vs = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int v = (int) (10.0 * Math.random());
                if (v >= 9) {
                    data[i][j] = 1;
                    vs++;
                } else {
                    data[i][j] = 0;
                }
                data2[i][j] = data[i][j];
                result[i][j] = 0;
                result2[i][j] = 0;
            }
        }

        System.out.println("Vs: " + vs);
        int[] maxTarget = new int[y];
        String max = "";
        int maxVal = 0;
        for (int i = 0; i < y; i++) {
            maxTarget[i] = (int) Math.rint(Math.random());
            if (maxTarget[i] == 1) {
                maxVal++;
            }
            max += maxTarget[i] + " ";
        }
        System.out.println("Max: " + maxVal);
        // System.out.println(max);
        System.out.println("-----------------");
        print(data);
        // System.out.println("*****");
        new Recurrection().solve(data, result, maxTarget, 0, 0);
        System.out.println("=========================");
        new Recurrection().solve(data2, result2, maxTarget, 0, 0);
    }
}
