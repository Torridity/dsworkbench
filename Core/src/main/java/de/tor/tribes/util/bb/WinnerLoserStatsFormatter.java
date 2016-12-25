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
package de.tor.tribes.util.bb;

import de.tor.tribes.types.TribeStatsElement.Stats;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Torridity
 */
public class WinnerLoserStatsFormatter extends BasicFormatter<Stats> {

    private static final String WINNER_BY_POINTS = "%WINNER_BY_POINTS%";
    private static final String WINNER_BY_EXPANSION = "%WINNER_BY_EXPANSION%";
    private static final String WINNER_BY_CONQUERS = "%WINNER_BY_CONQUERS%";
    private static final String WINNER_BY_DEFENSE = "%WINNER_BY_DEFENSE%";
    private static final String WINNER_BY_OFFENSE = "%WINNER_BY_OFFENSE%";
    private static final String WINNER_BY_KILLS_PER_POINT = "%WINNER_BY_KILLS_PER_POINT%";
    private static final String LOSER_BY_POINTS = "%LOSER_BY_POINTS%";
    private static final String LOSER_BY_EXPANSION = "%LOSER_BY_EXPANSION%";
    private static final String LOSER_BY_CONQUERS = "%LOSER_BY_CONQUERS%";
    private static final String LOSER_BY_OFFENSE = "%LOSER_BY_OFFENSE%";
    private static final String LOSER_BY_DEFENSE = "%LOSER_BY_DEFENSE%";
    private final String[] STAT_SPECIFIC_VARIABLES = new String[]{WINNER_BY_POINTS, WINNER_BY_EXPANSION, WINNER_BY_CONQUERS, WINNER_BY_OFFENSE, WINNER_BY_DEFENSE, WINNER_BY_KILLS_PER_POINT, LOSER_BY_POINTS, LOSER_BY_EXPANSION, LOSER_BY_CONQUERS, LOSER_BY_OFFENSE, LOSER_BY_DEFENSE};
    private final String[] VARIABLES = new String[]{};
    private static final String STANDARD_TEMPLATE = "[b]Gewinner und Verlierer[/b]\n\n"
            + "[table]\n[**]Titel[||]Spieler[/**]\n"
            + "[*][u][b]Gewinner[/b][/u][|][/*]\n"
            + "[*][b]Punktesammler[/b][|]%WINNER_BY_POINTS%[/*]\n"
            + "[*][b]Überflieger(in)[/b][|]%WINNER_BY_EXPANSION%[/*]\n"
            + "[*][b]Adelkönig(in)[/b][|]%WINNER_BY_CONQUERS%[/*]\n"
            + "[*][b]'My Home is my Castle'[/b][|]%WINNER_BY_DEFENSE%[/*]\n"
            + "[*][b]'Angriff ist die beste Verteidigung'[/b][|]%WINNER_BY_OFFENSE%[/*]\n"
            + "[*][b]'Ein hart erarbeiteter Sieg'[/b][|]%WINNER_BY_KILLS_PER_POINT%[/*]\n"
            + "[*][|][/*]\n"
            + "[*][u][b]Verlierer[/b][/u][|][/*]\n"
            + "[*][b]Punktespender[/b][|]%LOSER_BY_POINTS%[/*]\n"
            + "[*][b]'Geier Sturzflug'[/b][|]%LOSER_BY_EXPANSION%[/*]\n"
            + "[*][b]Dorfspender[/b][|]%LOSER_BY_CONQUERS%[/*]\n"
            + "[*][b]Friedensaktivist[/b][|]%LOSER_BY_OFFENSE%[/*]\n"
            + "[*][b]Liebling des Feindes[/b][|]%LOSER_BY_DEFENSE%[/*]\n"
            + "[/table]";
    private static final String TEMPLATE_PROPERTY = "winner.loser.stats.bbexport.template";

    @Override
    public String formatElements(List<Stats> pElements, boolean pExtended) {
        StringBuilder b = new StringBuilder();
        String template = getTemplate();
        String[] replacements = getStatSpecificReplacements(pElements, pExtended);
        template = StringUtils.replaceEach(template, getTemplateVariables(), replacements);
        b.append(template);
        return b.toString();
    }

    private String[] getStatSpecificReplacements(List<Stats> pStats, boolean pExtended) {

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        //point titles
        Collections.sort(pStats, Stats.POINTS_COMPARATOR);
        String winnerByPoints = pStats.get(0).getParent().getTribe().toBBCode();
        winnerByPoints += " (" + formatValue(pStats.get(0).getPointDiff(), nf) + " Punkte)";
        long pointsLost = pStats.get(pStats.size() - 1).getPointDiff();
        String loserByPoints = "";
        if (pointsLost < 0) {
            loserByPoints = pStats.get(pStats.size() - 1).getParent().getTribe().toBBCode();
            loserByPoints += " (" + formatValue(pStats.get(pStats.size() - 1).getPointDiff(), nf) + " Punkte)";
        } else {
            loserByPoints = "-Keine Punkteverluste vorhanden-";
        }
        //conquer titles
        Collections.sort(pStats, Stats.VILLAGE_COMPARATOR);
        String winnerByConquers = pStats.get(0).getParent().getTribe().toBBCode();
        winnerByConquers += " (" + formatValue(pStats.get(0).getVillageDiff(), nf) + " Dörfer)";
        long conquerLost = pStats.get(pStats.size() - 1).getVillageDiff();
        String loserByConquers = "";
        if (conquerLost < 0) {
            pStats.get(pStats.size() - 1).getParent().getTribe().toBBCode();
            loserByConquers += " (" + formatValue(pStats.get(pStats.size() - 1).getVillageDiff(), nf) + " Dörfer)";
        } else {
            loserByConquers = "-Keine Dorfverluste vorhanden-";
        }
        //off bash titles
        Collections.sort(pStats, Stats.BASH_OFF_COMPARATOR);
        String winnerByOffense = pStats.get(0).getParent().getTribe().toBBCode();
        winnerByOffense += " (" + formatValue(pStats.get(0).getBashOffDiff(), nf) + " besiegte Gegner)";
        String loserByOffense = pStats.get(pStats.size() - 1).getParent().getTribe().toBBCode();
        loserByOffense += " (" + formatValue(pStats.get(pStats.size() - 1).getBashOffDiff(), nf) + " besiegte Gegner)";
        //def bash titles
        Collections.sort(pStats, Stats.BASH_DEF_COMPARATOR);
        String winnerByDefense = pStats.get(0).getParent().getTribe().toBBCode();
        winnerByDefense += " (" + formatValue(pStats.get(0).getBashDefDiff(), nf) + " besiegte Gegner)";
        String loserByDefense = pStats.get(pStats.size() - 1).getParent().getTribe().toBBCode();
        loserByDefense += " (" + formatValue(pStats.get(pStats.size() - 1).getBashDefDiff(), nf) + " besiegte Gegner)";
        //kpp titles
        Collections.sort(pStats, Stats.KILLS_PER_POINT_COMPARATOR);
        String winnerByKillsPerPoint = pStats.get(0).getParent().getTribe().toBBCode();
        winnerByKillsPerPoint += " (" + formatValue(pStats.get(0).getKillPerPoint(), nf) + " Kills pro Punkt)";
        //expansion title
        Collections.sort(pStats, Stats.EXPANSION_COMPARATOR);
        String winnerByExpansion = pStats.get(0).getParent().getTribe().toBBCode();
        winnerByExpansion += " (" + formatValue(pStats.get(0).getExpansion(), nf) + "% Punktezuwachs)";
        double expansionLost = pStats.get(pStats.size() - 1).getExpansion();
        String loserByExpansion = "";
        if (expansionLost < 0) {
            loserByExpansion = " (" + formatValue(pStats.get(pStats.size() - 1).getExpansion(), nf) + "% Punktezuwachs)";
        } else {
            loserByExpansion = "-Keine negatives Wachstum vorhanden-";
        }


        return new String[]{winnerByPoints, winnerByExpansion, winnerByConquers, winnerByOffense, winnerByDefense, winnerByKillsPerPoint, loserByPoints, loserByExpansion, loserByConquers, loserByOffense, loserByDefense};
    }

    private String formatValue(Number pValue, NumberFormat pFormatter) {
        return (pValue.longValue() >= 0) ? "+" + pFormatter.format(pValue) : pFormatter.format(pValue);
    }

    @Override
    public String getPropertyKey() {
        return TEMPLATE_PROPERTY;
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    @Override
    public String[] getTemplateVariables() {
        List<String> vars = new LinkedList<>();
        for (String var : VARIABLES) {
            vars.add(var);
        }
        for (String var : STAT_SPECIFIC_VARIABLES) {
            vars.add(var);
        }
        return vars.toArray(new String[vars.size()]);
    }
}
