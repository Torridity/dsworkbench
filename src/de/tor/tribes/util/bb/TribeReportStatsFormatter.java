/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.bb;

import de.tor.tribes.types.TribeStatResult;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jejkal
 */
public class TribeReportStatsFormatter extends BasicFormatter<TribeStatResult> {

     private final String[] VARIABLES = new String[]{LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private final String STANDARD_TEMPLATE = new TribeStatResult().getStandardTemplate();
    private final String TEMPLATE_PROPERTY = "tribe.report.stats.bbexport.template";
    
    @Override
    public String formatElements(List<TribeStatResult> pElements, boolean pExtended) {
        StringBuilder b = new StringBuilder();
        int cnt = 1;
        NumberFormat f = getNumberFormatter(pElements.size());
        String beforeList = getHeader();
        String listItemTemplate = getLineTemplate();
        String afterList = getFooter();
        String replacedStart = StringUtils.replaceEach(beforeList, new String[]{ELEMENT_COUNT}, new String[]{f.format(pElements.size())});
        b.append(replacedStart);
        for (TribeStatResult n : pElements) {
            String[] replacements = n.getReplacements(pExtended);
            String itemLine = StringUtils.replaceEach(listItemTemplate, n.getBBVariables(), replacements);
            itemLine = StringUtils.replaceEach(itemLine, new String[]{ELEMENT_ID, ELEMENT_COUNT}, new String[]{f.format(cnt), f.format(pElements.size())});
            b.append(itemLine).append("\n");
            cnt++;
        }
        String replacedEnd = StringUtils.replaceEach(afterList, new String[]{ELEMENT_COUNT}, new String[]{f.format(pElements.size())});
        b.append(replacedEnd);
        return b.toString();
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
        List<String> vars = new LinkedList<String>();
        vars.addAll(Arrays.asList(VARIABLES));
        vars.addAll(Arrays.asList(new TribeStatResult().getBBVariables()));
        return vars.toArray(new String[vars.size()]);
    }
}
