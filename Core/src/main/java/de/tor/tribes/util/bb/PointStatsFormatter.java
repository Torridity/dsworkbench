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
import org.apache.commons.lang3.StringUtils;

/**
 * @author Torridity
 */
public class PointStatsFormatter extends BasicFormatter<Stats> {

    private static final String TRIBE = "%TRIBE%";
    private static final String POINTS_BEFORE = "%POINTS_START%";
    private static final String POINTS_AFTER = "%POINTS_END%";
    private static final String POINTS_DIFFERENCE = "%POINTS_DIFFERENCE%";
    private static final String PERCENT_DIFFERENCE = "%PERCENT_DIFFERENCE%";
    private static final String KILLS_PER_POINT = "%KILLS_PER_POINT%";
    private static final String[] VARIABLES = new String[] {LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    public static final String STANDARD_TEMPLATE = "[b]Punktestatistik[/b]\nBerücksichtigte Spieler: %ELEMENT_COUNT%\n[table]\n"
            + "[**]Platz[||]Spieler[||]Punkte (Anfang)[||]Wachstum[||]Punkte (Ende)[||]Kills/Punkt[/**]\n"
            + "%LIST_START%[*]%ELEMENT_ID%[|]%TRIBE%[|]%POINTS_START%[|]%PERCENT_DIFFERENCE%[|]%POINTS_END%[|]%KILLS_PER_POINT%[/*]%LIST_END%\n"
            + "[/table]";
    private static final String TEMPLATE_PROPERTY = "point.stats.bbexport.template";
    private final String[] STAT_SPECIFIC_VARIABLES = new String[] {TRIBE, POINTS_BEFORE, POINTS_AFTER, POINTS_DIFFERENCE, PERCENT_DIFFERENCE, KILLS_PER_POINT};

    /*
    01. [player]-Atheris-[/player]
    [quote]101,752 (Vorher)
    [color=red]0[/color] (+0.00%)
    [color=red]0.00 Kills/Punkt[/color]
    101,752 (Nachher)[/quote]
     */
    @Override
    public String formatElements(List<Stats> pElements, boolean pShowAll) {
        StringBuilder b = new StringBuilder();
        int cnt = 1;
        NumberFormat f = getNumberFormatter(pElements.size());
        String beforeList = getHeader();
        String listItemTemplate = getLineTemplate();
        String afterList = getFooter();
        String replacedStart = StringUtils.replaceEach(beforeList, new String[] {ELEMENT_COUNT}, new String[] {f.format(pElements.size())});
        b.append(replacedStart);
        Collections.sort(pElements, Stats.POINTS_COMPARATOR);
        int idx = 0;
        for (Stats s : pElements) {
            String[] replacements = getStatSpecificReplacements(s);
            String itemLine = StringUtils.replaceEach(listItemTemplate, STAT_SPECIFIC_VARIABLES, replacements);
            itemLine = StringUtils.replaceEach(itemLine, new String[] {ELEMENT_ID, ELEMENT_COUNT}, new String[] {f.format(cnt), f.format(pElements.size())});
            b.append(itemLine).append("\n");
            cnt++;
            idx++;
            if (idx == 10 && !pShowAll) {
                //show only top10
                break;
            }
        }
        String replacedEnd = StringUtils.replaceEach(afterList, new String[] {ELEMENT_COUNT}, new String[] {f.format(pElements.size())});
        b.append(replacedEnd);
        return b.toString();
    }

    private String[] getStatSpecificReplacements(Stats pStats) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String tribe = pStats.getParent().getTribe().toBBCode();
        String pointsBefore = nf.format(pStats.getPointStart());
        String pointsAfter = nf.format(pStats.getPointEnd());
        String pointsDiff = nf.format(pStats.getPointDiff());

        if (pStats.getPointDiff() > 0) {
            pointsDiff = "[color=green]" + pointsDiff + "[/color]";
        } else {
            pointsDiff = "[color=red]" + pointsDiff + "[/color]";
        }

        double perc = pStats.getExpansion();
        String percentDiff = ((perc >= 0) ? "+" : "") + nf.format(perc) + "%";

        if (perc > 0) {
            percentDiff = "[color=green]" + percentDiff + "[/color]";
        } else {
            percentDiff = "[color=red]" + percentDiff + "[/color]";
        }

        String killsPerPoints = nf.format(pStats.getKillPerPoint());

        return new String[] {tribe, pointsBefore, pointsAfter, pointsDiff, percentDiff, killsPerPoints};
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
        Collections.addAll(vars, VARIABLES);
        vars.add(TRIBE);
        vars.add(POINTS_BEFORE);
        vars.add(POINTS_AFTER);
        vars.add(POINTS_DIFFERENCE);
        vars.add(PERCENT_DIFFERENCE);
        vars.add(KILLS_PER_POINT);
        return vars.toArray(new String[vars.size()]);
    }
}
