/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class FightStats {

    private List<Ally> attackingAllies = null;
    private List<Tribe> attackingTribes = null;
    private List<Village> attackingVillages = null;
    private List<Ally> defendingAllies = null;
    private List<Tribe> defendingTribes = null;
    private List<Village> defendingVillages = null;
    private long startTime = Long.MAX_VALUE;
    private long endTime = Long.MIN_VALUE;
    private int reportCount = 0;
    private int fakeCount = 0;
    private int offCount = 0;
    private int snobCount = 0;
    private int simpleSnobCount = 0;
    private int enobelements = 0;
    private int destroyedWallLevels = 0;
    private Hashtable<String, Integer> destroyedBuildingLevels = null;
    private Hashtable<UnitHolder, Integer> diedAttackers = null;
    private Hashtable<UnitHolder, Integer> diedDefenders = null;
    private Hashtable<UnitHolder, Integer> defendersOnTheWay = null;
    private Hashtable<UnitHolder, Integer> defendersOutside = null;

    public FightStats() {
        attackingAllies = new LinkedList<Ally>();
        attackingTribes = new LinkedList<Tribe>();
        attackingVillages = new LinkedList<Village>();
        defendingAllies = new LinkedList<Ally>();
        defendingTribes = new LinkedList<Tribe>();
        defendingVillages = new LinkedList<Village>();
        destroyedBuildingLevels = new Hashtable<String, Integer>();
        diedAttackers = new Hashtable<UnitHolder, Integer>();
        diedDefenders = new Hashtable<UnitHolder, Integer>();
        defendersOnTheWay = new Hashtable<UnitHolder, Integer>();
        defendersOutside = new Hashtable<UnitHolder, Integer>();

    }

    public void includeReport(FightReport pReport) {
        Tribe attacker = pReport.getAttacker();
        Tribe defender = pReport.getDefender();
        Village sourceVillage = pReport.getSourceVillage();
        Village targetVillage = pReport.getTargetVillage();
        if (attacker == null || defender == null || sourceVillage == null || targetVillage == null) {
            return;
        }

        reportCount++;
        if (pReport.getTimestamp() < startTime) {
            startTime = pReport.getTimestamp();
        }
        if (pReport.getTimestamp() > endTime) {
            endTime = pReport.getTimestamp();
        }
        Ally attackerAlly = attacker.getAlly();
        Ally defenderAlly = defender.getAlly();
        if (attackerAlly == null) {
            attackerAlly = NoAlly.getSingleton();
        }
        if (defenderAlly == null) {
            defenderAlly = NoAlly.getSingleton();
        }

        if (!attackingTribes.contains(attacker)) {
            attackingTribes.add(attacker);
        }

        if (!defendingTribes.contains(defender)) {
            defendingTribes.add(defender);
        }

        if (!attackingVillages.contains(sourceVillage)) {
            attackingVillages.add(sourceVillage);
        }

        if (!defendingVillages.contains(targetVillage)) {
            defendingVillages.add(targetVillage);
        }

        if (!attackingAllies.contains(attackerAlly)) {
            attackingAllies.add(attackerAlly);
        }

        if (!defendingAllies.contains(defenderAlly)) {
            defendingAllies.add(defenderAlly);
        }

        switch (pReport.guessType()) {
            case Attack.FAKE_TYPE:
                fakeCount++;
                break;
            case Attack.SPY_TYPE:
                fakeCount++;
                break;
            case Attack.CLEAN_TYPE:
                offCount++;
                break;
            case Attack.SNOB_TYPE:
                if (pReport.isSimpleSnobAttack()) {
                    simpleSnobCount++;
                } else {
                    snobCount++;
                }
                break;
        }

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (!pReport.areAttackersHidden()) {
                Integer value = diedAttackers.get(unit);
                if (value == null) {
                    value = 0;
                }
                diedAttackers.put(unit, value + pReport.getDiedAttackers().get(unit));
            }
            if (!pReport.wasLostEverything()) {
                Integer value = diedDefenders.get(unit);
                if (value == null) {
                    value = 0;
                }
                diedDefenders.put(unit, value + pReport.getDiedDefenders().get(unit));
            }

            if (pReport.wasConquered() && pReport.whereDefendersOnTheWay()) {
                Integer value = defendersOnTheWay.get(unit);
                if (value == null) {
                    value = 0;
                }
                defendersOnTheWay.put(unit, value + pReport.getDefendersOnTheWay().get(unit));
            }
        }

        if (pReport.wasConquered()) {
            enobelements++;
        }

        if (pReport.wasWallDamaged()) {
            destroyedWallLevels += (pReport.getWallBefore() - pReport.getWallAfter());
        }

        if (pReport.wasBuildingDamaged()) {
            Integer value = destroyedBuildingLevels.get(pReport.getAimedBuilding());
            if (value == null) {
                value = 0;
            }
            destroyedBuildingLevels.put(pReport.getAimedBuilding(), value + (pReport.getBuildingBefore() - pReport.getBuildingAfter()));
        }
    }

    public String toString() {
        String res = "";
        res += attackingTribes + "\n";
        res += attackingAllies + "\n";
        res += attackingVillages + "\n";
        res += "----\n";
        res += defendingTribes + "\n";
        res += defendingAllies + "\n";
        res += defendingVillages + "\n";
        res += "----\n";
        res += "" + offCount + "\n";
        res += "" + fakeCount + "\n";
        res += "" + simpleSnobCount + "\n";
        res += "" + snobCount + "\n";
        res += "----\n";
        res += diedAttackers + "\n";
        res += diedDefenders + "\n";
        res += defendersOnTheWay + "\n";
        res += "-----\n";
        res += destroyedBuildingLevels + "\n";
        res += "" + destroyedWallLevels + "\n";
        res += "" + enobelements + "\n";
        res += "=====\n";
        return res;

    }
}
