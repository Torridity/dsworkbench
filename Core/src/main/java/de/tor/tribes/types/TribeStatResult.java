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

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.util.BBSupport;
import java.text.NumberFormat;

/**
 *
 * @author Torridity
 */
public class TribeStatResult implements BBSupport {

    private final static String[] VARIABLES = new String[]{"%TRIBE_NAME%", "%ATTACKS%", "%OFF_ATTACKS%", "%SNOB_ATTACKS%", "%FAKE_ATTACKS%", "%ENOBLEMENTS%",
        "%KILLS%", "%KILLS_FARM%", "%KILLS_PERCENT_ALLY%", "%KILLS_PERCENT_ALL%", "%LOSSES%", "%LOSSES_FARM%", "%LOSSES_PERCENT_ALLY%", "%LOSSES_PERCENT_ALL%", "%WALL_DESTRUCTION%", "%BUILDING_DESTRUCTION%"};
    private String STANDARD_TEMPLATE = null;

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        String valName = (tribe != null) ? tribe.getName() : "unbekannt";
        String valAttacks = nf.format(attacks);
        String valOffs = nf.format(offs);
        String valSnobs = nf.format(snobs);
        String valFakes = nf.format(fakes);
        String valEnoblements = nf.format(enoblements);
        String valKills = nf.format(kills);
        String valKillsAsFarm = nf.format(killsAsFarm);
        String valLosses = nf.format(losses);
        String valLossesAsFarm = nf.format(lossesAsFarm);
        String valWallDestruction = nf.format(wallDestruction);
        String valBuildingDestruction = nf.format(buildingDestruction);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String valKillsPercentAlly = (allyKills == 0) ? "0.0 %" : nf.format(100 * kills / (double) allyKills) + " %";
        String valLossesPercentAlly = (allyLosses == 0) ? "0.0 %" : nf.format(100 * losses / (double) allyLosses) + " %";
        String valKillsPercentAll = (overallKills == 0) ? "0.0 %" : nf.format(100 * kills / (double) overallKills) + " %";
        String valLossesPercentAll = (overallLosses == 0) ? "0.0 %" : nf.format(100 * losses / (double) overallLosses) + " %";

        return new String[]{valName, valAttacks, valOffs, valSnobs, valFakes, valEnoblements, valKills, valKillsAsFarm, valKillsPercentAlly, valKillsPercentAll, valLosses, valLossesAsFarm, valLossesPercentAlly, valLossesPercentAll, valWallDestruction, valBuildingDestruction};
    }

    @Override
    public String getStandardTemplate() {
        if (STANDARD_TEMPLATE == null) {
            StringBuilder b = new StringBuilder();
            b.append("[b]Auswertung für [player]%TRIBE_NAME%[/player][/b]\n\n");
            b.append("Adelungen: %ENOBLEMENTS%\n");
            b.append("Zerstörte Wallstufen: %WALL_DESTRUCTION%\n");
            b.append("Zerstörte Gebäudestufen: %BUILDING_DESTRUCTION%\n\n");
            b.append("Angriffe:\n");
            b.append("[table][**]Gesamt[||]Off[||]Fake[||]AG[/**]\n");
            b.append("[*]%ATTACKS%[|]%OFF_ATTACKS%[|]%FAKE_ATTACKS%[|]%SNOB_ATTACKS%[/*]\n");
            b.append("[/table]\n\n");
            b.append("Besiegte Einheiten:\n");
            b.append("[table][**]Art[||]Anzahl[||]Bauernhofplätze[||]Anteil (Stamm)[||]Anteil (Gesamt)[/**]\n");
            b.append("[*]Getötet[|]%KILLS%[|]%KILLS_FARM%[|]%KILLS_PERCENT_ALLY%[|]%KILLS_PERCENT_ALL%[/*]\n");
            b.append("[*]Verloren[|]%LOSSES%[|]%LOSSES_FARM%[|]%LOSSES_PERCENT_ALLY%[|]%LOSSES_PERCENT_ALL%[/*]\n");
            b.append("[/table]\n");

            STANDARD_TEMPLATE = b.toString();
        }
        return STANDARD_TEMPLATE;
    }
    Tribe tribe = null;
    private int attacks = 0;
    private int offs = 0;
    private int snobs = 0;
    private int fakes = 0;
    private int enoblements = 0;
    private int losses = 0;
    private int lossesAsFarm = 0;
    private int kills = 0;
    private int killsAsFarm = 0;
    private int overallKills = 0;
    private int overallLosses = 0;
    private int allyKills = 0;
    private int allyLosses = 0;
    private int wallDestruction = 0;
    private int buildingDestruction = 0;

    public TribeStatResult() {
    }

    public void setTribeStats(SingleAttackerStat pStat, boolean pUseApproxValues) {
        tribe = pStat.getAttacker();
        attacks = pStat.getOffCount() + pStat.getFakeCount() + pStat.getSnobAttackCount() + pStat.getSimpleSnobAttackCount();
        offs = pStat.getOffCount();
        snobs = pStat.getSimpleSnobAttackCount() + pStat.getSnobAttackCount();
        fakes = pStat.getFakeCount();
        enoblements = pStat.getEnoblementCount();
        losses = pStat.getSummedLosses();
        kills = pStat.getSummedKills();
        lossesAsFarm = pStat.getSummedLossesAsFarmSpace();
        killsAsFarm = pStat.getSummedKillsAsFarmSpace();
        if (pUseApproxValues) {
            kills += pStat.getAtLeast2KDamageCount() * 2000;
            kills += pStat.getAtLeast4KDamageCount() * 4000;
            kills += pStat.getAtLeast6KDamageCount() * 6000;
            kills += pStat.getAtLeast8KDamageCount() * 8000;
            killsAsFarm += pStat.getAtLeast2KDamageCount() * 2000 * 1.5;
            killsAsFarm += pStat.getAtLeast4KDamageCount() * 4000 * 1.5;
            killsAsFarm += pStat.getAtLeast6KDamageCount() * 6000 * 1.5;
            killsAsFarm += pStat.getAtLeast8KDamageCount() * 8000 * 1.5;
        }
        wallDestruction = pStat.getDestroyedWallLevels();
        buildingDestruction = pStat.getSummedDestroyedBuildings();
    }

    /**
     * @return the tribeStats
     */
    public Tribe getTribe() {
        return tribe;
    }

    /**
     * @return the attacks
     */
    public int getAttacks() {
        return attacks;
    }

    /**
     * @return the offs
     */
    public int getOffs() {
        return offs;
    }

    /**
     * @return the snobs
     */
    public int getSnobs() {
        return snobs;
    }

    /**
     * @return the fakes
     */
    public int getFakes() {
        return fakes;
    }

    /**
     * @return the enoblements
     */
    public int getEnoblements() {
        return enoblements;
    }

    /**
     * @return the losses
     */
    public int getLosses() {
        return losses;
    }

    /**
     * @return the lossesAsFarm
     */
    public int getLossesAsFarm() {
        return lossesAsFarm;
    }

    /**
     * @return the kills
     */
    public int getKills() {
        return kills;
    }

    /**
     * @return the killsAsFarm
     */
    public int getKillsAsFarm() {
        return killsAsFarm;
    }

    /**
     * @return the wallDestruction
     */
    public int getWallDestruction() {
        return wallDestruction;
    }

    /**
     * @return the buildingDestruction
     */
    public int getBuildingDestruction() {
        return buildingDestruction;
    }

    /**
     * @return the overallKills
     */
    public int getOverallKills() {
        return overallKills;
    }

    /**
     * @param overallKills the overallKills to set
     */
    public void setOverallKills(int overallKills) {
        this.overallKills = overallKills;
    }

    /**
     * @return the overallLosses
     */
    public int getOverallLosses() {
        return overallLosses;
    }

    /**
     * @param overallLosses the overallLosses to set
     */
    public void setOverallLosses(int overallLosses) {
        this.overallLosses = overallLosses;
    }

    /**
     * @return the allyKills
     */
    public int getAllyKills() {
        return allyKills;
    }

    /**
     * @param allyKills the allyKills to set
     */
    public void setAllyKills(int allyKills) {
        this.allyKills = allyKills;
    }

    /**
     * @return the allyLosses
     */
    public int getAllyLosses() {
        return allyLosses;
    }

    /**
     * @param allyLosses the allyLosses to set
     */
    public void setAllyLosses(int allyLosses) {
        this.allyLosses = allyLosses;
    }
}
