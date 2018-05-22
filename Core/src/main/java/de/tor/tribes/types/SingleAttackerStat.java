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
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.village.KnownVillage;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class SingleAttackerStat {

    private Tribe attacker = null;
    private List<Village> usedVillages = null;
    private int fakeAttacks = 0;
    private int offAttacks = 0;
    private int snobAttacks = 0;
    private int simpleSnobAttacks = 0;
    private int enoblements = 0;
    private TroopAmountFixed sentUnits = null;
    private TroopAmountFixed killedUnits = null;
    private TroopAmountFixed lostUnits = null;
    private int unknownDamage = 0;
    private int atLeast2KDamage = 0;
    private int atLeast4KDamage = 0;
    private int atLeast6KDamage = 0;
    private int atLeast8KDamage = 0;
    private int destroyedWallLevels = 0;
    private Hashtable<Integer, Integer> destroyedBuildingLevels = null;
    private TroopAmountFixed silentKills = null;

    public SingleAttackerStat(Tribe pTribe) {
        attacker = pTribe;
        usedVillages = new LinkedList<>();
        sentUnits = new TroopAmountFixed(0);
        killedUnits = new TroopAmountFixed(0);
        lostUnits = new TroopAmountFixed(0);
        destroyedBuildingLevels = new Hashtable<>();
        silentKills = new TroopAmountFixed(0);
    }

    public static SingleAttackerStat createRandomElement(Tribe pTribe) {
        SingleAttackerStat elem = new SingleAttackerStat(pTribe);
        for (int i = 0; i < ((int) Math.rint(Math.random() * 50) + 1); i++) {
            elem.addOff();
        }
        for (int i = 0; i < ((int) Math.rint(Math.random() * 100) + 1); i++) {
            elem.addFake();
        }

        for (int i = 0; i < ((int) Math.rint(Math.random() * 10) + 1); i++) {
            elem.addSnobAttack();
        }

        for (int i = 0; i < ((int) Math.rint(Math.random() * 5)); i++) {
            elem.addEnoblement();
        }

        elem.addDestroyedBuildingLevel(KnownVillage.getBuildingIdByName("Bauernhof"),
                ((int) Math.rint(Math.random() * 30)));
        elem.addDestroyedWallLevels(((int) Math.rint(Math.random() * 60)));
        
        TroopAmountFixed temp = new TroopAmountFixed(0);
        for (int i = 0; i < ((int) Math.rint(Math.random() * 5)); i++) {
            temp.setAmountForUnit(DataHolder.getSingleton().getRandomUnit(), (int) (Math.random() * 500));
        }
        elem.addKilledUnits(temp);

        for (int i = 0; i < ((int) Math.rint(Math.random() * 10)); i++) {
            temp.setAmountForUnit(DataHolder.getSingleton().getRandomUnit(), (int) (Math.random() * 500));
        }
        elem.addSentUnits(temp);
        
        for (int i = 0; i < ((int) Math.rint(Math.random() * 5) + 1); i++) {
            temp.setAmountForUnit(DataHolder.getSingleton().getRandomUnit(), (int) (Math.random() * 100));
        }
        elem.addLostUnits(temp);

        for (int i = 0; i < ((int) Math.rint(Math.random() * 5)); i++) {
            elem.addAtLeast2KDamage();
        }

        return elem;
    }

    public void addSourceVillage(Village pVillage) {
        if (!usedVillages.contains(pVillage)) {
            usedVillages.add(pVillage);
        }
    }

    public Tribe getAttacker() {
        return attacker;
    }

    public List<Village> getSourceVillages() {
        return usedVillages;
    }

    public void addFake() {
        fakeAttacks++;
    }

    public int getFakeCount() {
        return fakeAttacks;
    }

    public void addOff() {
        offAttacks++;
    }

    public int getOffCount() {
        return offAttacks;
    }

    public void addEnoblement() {
        enoblements++;
    }

    public int getEnoblementCount() {
        return enoblements;
    }

    public void addSnobAttack() {
        snobAttacks++;
    }

    public int getSnobAttackCount() {
        return snobAttacks;
    }

    public void addSimpleSnobAttack() {
        simpleSnobAttacks++;
    }

    public int getSimpleSnobAttackCount() {
        return simpleSnobAttacks;
    }

    public void addUnknownDamage() {
        unknownDamage++;
    }

    public int getUnknownDamageCount() {
        return unknownDamage;
    }

    public void addAtLeast2KDamage() {
        atLeast2KDamage++;
    }

    public int getAtLeast2KDamageCount() {
        return atLeast2KDamage;
    }

    public void addAtLeast4KDamage() {
        atLeast4KDamage++;
    }

    public int getAtLeast4KDamageCount() {
        return atLeast4KDamage;
    }

    public void addAtLeast6KDamage() {
        atLeast6KDamage++;
    }

    public int getAtLeast6KDamageCount() {
        return atLeast6KDamage;
    }

    public void addAtLeast8KDamage() {
        atLeast8KDamage++;
    }

    public int getAtLeast8KDamageCount() {
        return atLeast8KDamage;
    }

    public void addDestroyedWallLevels(int pLevels) {
        destroyedWallLevels += pLevels;
    }

    public int getDestroyedWallLevels() {
        return destroyedWallLevels;
    }

    public void addDestroyedBuildingLevel(int pBuildingId, int pLevels) {
        Integer value = destroyedBuildingLevels.get(pBuildingId);
        if (value == null) {
            destroyedBuildingLevels.put(pBuildingId, pLevels);
        } else {
            destroyedBuildingLevels.put(pBuildingId, value + pLevels);
        }
    }

    public Hashtable<Integer, Integer> getDestroyedBuildings() {
        return destroyedBuildingLevels;
    }

    public int getSummedDestroyedBuildings() {
        if (destroyedBuildingLevels == null || destroyedBuildingLevels.isEmpty()) {
            return 0;
        }
        Enumeration<Integer> keys = destroyedBuildingLevels.keys();
        int value = 0;
        while (keys.hasMoreElements()) {
            value += destroyedBuildingLevels.get(keys.nextElement());
        }
        return value;
    }

    public void addSentUnits(TroopAmountFixed pTroops) {
        sentUnits.addAmount(pTroops);
    }

    public void addLostUnits(TroopAmountFixed pTroops) {
        lostUnits.addAmount(pTroops);
    }

    public void addKilledUnits(TroopAmountFixed pTroops) {
        killedUnits.addAmount(pTroops);
    }

    public void addSilentlyKilledUnits(TroopAmountFixed pTroops) {
        silentKills.addAmount(pTroops);
    }

    public int getSummedLosses() {
        return lostUnits.getTroopSum();
    }

    public int getSummedLossesAsFarmSpace() {
        return lostUnits.getTroopPopCount();
    }

    public int getSummedKills() {
        return killedUnits.getTroopSum();
    }

    public int getSummedKillsAsFarmSpace() {
        return killedUnits.getTroopPopCount();
    }

    public int getSummedSilentKills() {
        return silentKills.getTroopSum();
    }

    public int getSummedSilentKillsAsFarmSpace() {
        return silentKills.getTroopPopCount();
    }

    @Override
    public String toString() {
        String res = "";
        res += " Attacker: " + attacker + "\n";
        res += " UsedVillages: " + usedVillages.size() + "\n";
        res += " Fakes: " + fakeAttacks + "\n";
        res += " Offs: " + offAttacks + "\n";
        res += " Snob: " + snobAttacks + "\n";
        res += " SimpleSnob: " + simpleSnobAttacks + "\n";
        res += " Enoblements: " + enoblements + "\n";
        res += " UnitStats\n";
        res += " .........\n";
        res += "  Sent: " + sentUnits + "\n";
        res += "  Lost: " + lostUnits + "\n";
        res += "  Killed: " + killedUnits + "\n";
        res += "  SilentKills: " + silentKills + "\n";
        res += "  Killed: " + getSummedKills() + "\n";
        res += "  Lost: " + getSummedLosses() + "\n";
        res += "  UnknownDamage: " + unknownDamage + "\n";
        res += "  AtLeast2KDamage: " + atLeast2KDamage + "\n";
        res += "  AtLeast4KDamage: " + atLeast4KDamage + "\n";
        res += "  AtLeast6KDamage: " + atLeast6KDamage + "\n";
        res += "  AtLeast8KDamage: " + atLeast8KDamage + "\n";
        res += " DestructionStats\n";
        res += " ................\n";
        res += "  Wall: " + destroyedWallLevels + "\n";
        res += "  Buildings: " + destroyedBuildingLevels + "\n";
        return res;
    }
}
