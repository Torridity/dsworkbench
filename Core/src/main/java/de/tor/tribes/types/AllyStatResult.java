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

import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.util.BBSupport;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class AllyStatResult implements BBSupport {

    private final static String[] VARIABLES = new String[]{"%ALLY_NAME%", "%ALLY_TAG%", "%ATTACKERS%", "%ATTACKS%", "%OFF_ATTACKS%", "%SNOB_ATTACKS%", "%FAKE_ATTACKS%", "%ENOBLEMENTS%",
        "%KILLS%", "%KILLS_FARM%", "%KILLS_PERCENT%", "%LOSSES%", "%LOSSES_FARM%", "%LOSSES_PERCENT%", "%WALL_DESTRUCTION%", "%BUILDING_DESTRUCTION%"};
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
        String valAllyName = (ally != null) ? ally.getName() : "unbekannt";
        String valAllyTag = (ally != null) ? ally.getTag() : "unbekannt";
        String valAttackers = nf.format(getAttackers());
        String valAttacks = nf.format(attacks);
        String valOffs = nf.format(offs);
        String valSnobs = nf.format(snobs);
        String valFakes = nf.format(fakes);
        String valEnoblements = nf.format(enoblements);
        String valKills = nf.format(kills);
        String valKillsFarm = nf.format(killsAsFarm);
        String valLosses = nf.format(losses);
        String valLossesFarm = nf.format(lossesAsFarm);
        String valWallDestruction = nf.format(wallDestruction);
        String valBuildingDestruction = nf.format(buildingDestruction);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String valKillsPercent = (overallKills == 0) ? "0.0 %" : nf.format(100 * kills / (double) overallKills) + " %";
        String valLossesPercent = (overallLosses == 0) ? "0.0 %" : nf.format(100 * losses / (double) overallLosses) + " %";

        return new String[]{valAllyName, valAllyTag, valAttackers, valAttacks, valOffs, valSnobs, valFakes, valEnoblements, valKills, valKillsFarm,
                    valKillsPercent, valLosses, valLossesFarm, valLossesPercent, valWallDestruction, valBuildingDestruction};
    }

    @Override
    public String getStandardTemplate() {
        if (STANDARD_TEMPLATE == null) {
            StringBuilder b = new StringBuilder();
            b.append("[b]Auswertung für %ALLY_NAME% ([ally]%ALLY_TAG%[/ally])[/b]\n\n");
            b.append("Angreifer: %ATTACKERS%\n");
            b.append("Adelungen: %ENOBLEMENTS%\n");
            b.append("Zerstörte Wallstufen: %WALL_DESTRUCTION%\n");
            b.append("Zerstörte Gebäudestufen: %BUILDING_DESTRUCTION%\n\n");
            b.append("Angriffe:\n");
            b.append("[table][**]Gesamt[||]Off[||]Fake[||]AG[/**]\n");
            b.append("[*]%ATTACKS%[|]%OFF_ATTACKS%[|]%FAKE_ATTACKS%[|]%SNOB_ATTACKS%[/*]\n");
            b.append("[/table]\n\n");
            b.append("Besiegte Einheiten:\n");
            b.append("[table][**]Art[||]Anzahl[||]Bauernhofplätze[||]Anteil[/**]\n");
            b.append("[*]Getötet[|]%KILLS%[|]%KILLS_FARM%[|]%KILLS_PERCENT%[/*]\n");
            b.append("[*]Verloren[|]%LOSSES%[|]%LOSSES_FARM%[|]%LOSSES_PERCENT%[/*]\n");
            b.append("[/table]\n");


            STANDARD_TEMPLATE = b.toString();
        }
        return STANDARD_TEMPLATE;
    }
    private Ally ally = null;
    private List<TribeStatResult> tribeStats = null;
    private int attacks = 0;
    private int offs = 0;
    private int snobs = 0;
    private int fakes = 0;
    private int enoblements = 0;
    private int losses = 0;
    private int lossesAsFarm = 0;
    private int overallLosses = 0;
    private int kills = 0;
    private int killsAsFarm = 0;
    private int overallKills = 0;
    private int wallDestruction = 0;
    private int buildingDestruction = 0;

    public AllyStatResult() {
        tribeStats = new LinkedList<>();
    }

    public void addTribeStatResult(TribeStatResult pStat) {
        tribeStats.add(pStat);
        attacks += pStat.getOffs() + pStat.getFakes() + pStat.getSnobs();
        offs += pStat.getOffs();
        snobs += pStat.getSnobs();
        fakes += pStat.getFakes();
        enoblements += pStat.getEnoblements();
        losses += pStat.getLosses();
        kills += pStat.getKills();
        lossesAsFarm += pStat.getLossesAsFarm();
        killsAsFarm += pStat.getKillsAsFarm();

        wallDestruction += pStat.getWallDestruction();
        buildingDestruction += pStat.getBuildingDestruction();
    }

    public int getAttackers() {
        return tribeStats.size();
    }

    /**
     * @return the tribeStats
     */
    public List<TribeStatResult> getTribeStats() {
        return tribeStats;
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
     * @return the ally
     */
    public Ally getAlly() {
        return ally;
    }

    /**
     * @param ally the ally to set
     */
    public void setAlly(Ally ally) {
        this.ally = ally;
    }
}
