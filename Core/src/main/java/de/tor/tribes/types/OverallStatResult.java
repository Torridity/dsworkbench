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

import de.tor.tribes.util.BBSupport;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class OverallStatResult implements BBSupport {

    private final static String[] VARIABLES = new String[]{"%START_DATE%", "%END_DATE%", "%REPORT_COUNT%", "%ATTACK_TRIBES%", "%ATTACK_ALLIES%", "%DEFEND_TRIBES%", "%DEFEND_ALLIES%",
        "%KILLS%", "%KILLS_FARM%", "%LOSSES%", "%LOSSES_FARM%", "%LOSSES_PER_ATTACKER%", "%LOSSES_PER_DEFENDER%", "%WALL_DESTRUCTION%", "%BUILDING_DESTRUCTION%"};
    private String STANDARD_TEMPLATE = null;

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        String valStartDate = df.format(getStartDate());
        String valEndData = df.format(getEndDate());
        String valReportCount = nf.format(getReportCount());
        String valAttackTribes = nf.format(getAttackers());
        String valAttackAllies = nf.format(getAttackerAllies());
        String valDefendTribes = nf.format(getDefenders());
        String valDefendAllies = nf.format(getDefenderAllies());
        String valKills = nf.format(getKills());
        String valKillsAsFarm = nf.format(getKillsAsFarm());
        String valLosses = nf.format(getLosses());
        String valLossesAsFarm = nf.format(getLossesAsFarm());
        String valLossesPerAttacker = nf.format(getLosses() / getAttackers());
        String valLossesPerDefender = nf.format(getKills() / getDefenders());
        String valWallDestruction = nf.format(getWallDestruction());
        String valBuildingDestruction = nf.format(getBuildingDestruction());
        return new String[]{valStartDate, valEndData, valReportCount, valAttackTribes, valAttackAllies, valDefendTribes, valDefendAllies, valKills, valKillsAsFarm, valLosses, valLossesAsFarm,
                    valLossesPerAttacker, valLossesPerDefender, valWallDestruction, valBuildingDestruction};
    }

    @Override
    public String getStandardTemplate() {
        if (STANDARD_TEMPLATE == null) {
            StringBuilder b = new StringBuilder();

            b.append("[b][u]Gesamtstatistiken[/u][/b]\n\n");
            b.append("Auswertung vom %START_DATE% bis zum %END_DATE%\n\n");
            b.append("[table]\n");
            b.append("[**]Ausgewertete Berichte[||]%REPORT_COUNT%[/**]\n");
            b.append("[*]Ausgewertete Angreifer (Stämme)[|]%ATTACK_TRIBES% (%ATTACK_ALLIES%)[/*]\n");
            b.append("[**]Verteidiger (Stämme)[||]%DEFEND_TRIBES% (%DEFEND_ALLIES%)[/**]\n");
            b.append("[*]Verluste der Verteidiger (Bauernhofplätze)[|] %KILLS% (%KILLS_FARM%)[/*]\n");
            b.append("[**]Verluste pro Verteidiger[||]%LOSSES_PER_DEFENDER%[/**]\n");
            b.append("[*]Verluste der Angreifer (Bauernhofplätze)[|]%LOSSES% (%LOSSES_FARM%)[/*]\n");
            b.append("[**]Verluste pro Angreifer[||]%LOSSES_PER_ATTACKER%[/**]\n");
            b.append("[*]Zerstörte Wallstufen[|]%WALL_DESTRUCTION%[/*]\n");
            b.append("[**]Zerstörte Gebäudestufen[||]%BUILDING_DESTRUCTION%[/**]\n");

            STANDARD_TEMPLATE = b.toString();
        }
        return STANDARD_TEMPLATE;
    }
    private List<AllyStatResult> allyStats = null;
    private int attackers = 0;
    private int attackerAllies = 0;
    private int defenders = 0;
    private int defenderAllies = 0;
    private int attacks = 0;
    private int offs = 0;
    private int snobs = 0;
    private int fakes = 0;
    private int enoblements = 0;
    private int losses = 0;
    private int lossesAsFarm = 0;
    private int kills = 0;
    private int killsAsFarm = 0;
    private int wallDestruction = 0;
    private int buildingDestruction = 0;
    private int reportCount = 0;
    private Date startDate = null;
    private Date endDate = null;

    public OverallStatResult() {
        allyStats = new LinkedList<AllyStatResult>();
    }

    public void addAllyStatsResult(AllyStatResult pStat) {
        allyStats.add(pStat);
        attackers += pStat.getAttackers();
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

    /**
     * @return the tribeStats
     */
    public List<AllyStatResult> getAllyStats() {
        return allyStats;
    }

    public int getAttackers() {
        return attackers;
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
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the reportCount
     */
    public int getReportCount() {
        return reportCount;
    }

    /**
     * @param reportCount the reportCount to set
     */
    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    /**
     * @return the attackerAllies
     */
    public int getAttackerAllies() {
        return attackerAllies;
    }

    /**
     * @param attackerAllies the attackerAllies to set
     */
    public void setAttackerAllies(int attackerAllies) {
        this.attackerAllies = attackerAllies;
    }

    /**
     * @return the defenders
     */
    public int getDefenders() {
        return defenders;
    }

    /**
     * @param defenders the defenders to set
     */
    public void setDefenders(int defenders) {
        this.defenders = defenders;
    }

    /**
     * @return the defenderAllies
     */
    public int getDefenderAllies() {
        return defenderAllies;
    }

    /**
     * @param defenderAllies the defenderAllies to set
     */
    public void setDefenderAllies(int defenderAllies) {
        this.defenderAllies = defenderAllies;
    }
}
