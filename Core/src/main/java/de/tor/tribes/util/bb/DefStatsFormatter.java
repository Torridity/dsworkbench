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
public class DefStatsFormatter extends BasicFormatter<Stats> {

    private static final String TRIBE = "%TRIBE%";
    private static final String KILLS_BEFORE = "%KILLS_START%";
    private static final String KILLS_AFTER = "%KILLS_END%";
    private static final String KILLS_DIFFERENCE = "%KILLS_DIFFERENCE%";
    private static final String PERCENT_DIFFERENCE = "%PERCENT_DIFFERENCE%";
    private final String[] STAT_SPECIFIC_VARIABLES = new String[]{TRIBE, KILLS_BEFORE, KILLS_AFTER, KILLS_DIFFERENCE, PERCENT_DIFFERENCE};
    private final String[] VARIABLES = new String[]{LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private static final String STANDARD_TEMPLATE = "[b]Verteidigungsstatistik[/b]\nBerücksichtigte Spieler: %ELEMENT_COUNT%\n[table]\n"
            + "[**]Platz[||]Spieler[||]Besiegte Angreifer (Anfang)[||]Zuwachs[||]Besiegte Angreifer (Ende)[/**]\n"
            + "%LIST_START%[*]%ELEMENT_ID%[|]%TRIBE%[|]%KILLS_START%[|]%KILLS_DIFFERENCE%[|]%KILLS_END%[/*]%LIST_END%\n"
            + "[/table]";
    private static final String TEMPLATE_PROPERTY = "def.stats.bbexport.template";

    @Override
    public String formatElements(List<Stats> pElements, boolean pShowAll) {
        StringBuilder b = new StringBuilder();
        int cnt = 1;
        NumberFormat f = getNumberFormatter(pElements.size());
        String beforeList = getHeader();
        String listItemTemplate = getLineTemplate();
        String afterList = getFooter();
        String replacedStart = StringUtils.replaceEach(beforeList, new String[]{ELEMENT_COUNT}, new String[]{f.format(pElements.size())});
        b.append(replacedStart);
        Collections.sort(pElements, Stats.BASH_DEF_COMPARATOR);
        int idx = 0;
        for (Stats s : pElements) {
            String[] replacements = getStatSpecificReplacements(s);
            String itemLine = StringUtils.replaceEach(listItemTemplate, STAT_SPECIFIC_VARIABLES, replacements);
            itemLine = StringUtils.replaceEach(itemLine, new String[]{ELEMENT_ID, ELEMENT_COUNT}, new String[]{f.format(cnt), f.format(pElements.size())});
            b.append(itemLine).append("\n");
            cnt++;
            idx++;
            if (idx == 10 && !pShowAll) {
                //show only top10
                break;
            }
        }
        String replacedEnd = StringUtils.replaceEach(afterList, new String[]{ELEMENT_COUNT}, new String[]{f.format(pElements.size())});
        b.append(replacedEnd);
        return b.toString();
    }

    private String[] getStatSpecificReplacements(Stats pStats) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String tribe = pStats.getParent().getTribe().toBBCode();
        String killsBefore = nf.format(pStats.getBashDefStart());
        String killsAfter = nf.format(pStats.getBashDefEnd());
        String killsDiff = nf.format(pStats.getBashDefDiff());

        if (pStats.getBashDefDiff() > 0) {
            killsDiff = "[color=green]" + killsDiff + "[/color]";
        } else {
            killsDiff = "[color=red]" + killsDiff + "[/color]";
        }

        long pBefore = pStats.getBashDefStart();
        double perc = 0;
        if (pBefore > 0) {
            perc = (double) 100 * (double) pStats.getBashDefDiff() / (double) pBefore;
        }

        String percentDiff = ((perc >= 0) ? "+" : "") + nf.format(perc) + "%";

        if (perc > 0) {
            percentDiff = "[color=green]" + percentDiff + "[/color]";
        } else {
            percentDiff = "[color=red]" + percentDiff + "[/color]";
        }

        return new String[]{tribe, killsBefore, killsAfter, killsDiff, percentDiff};
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
        Collections.addAll(vars, STAT_SPECIFIC_VARIABLES);
        return vars.toArray(new String[vars.size()]);
    }
}
