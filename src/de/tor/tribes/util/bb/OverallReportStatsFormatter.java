/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.bb;

import de.tor.tribes.types.OverallStatResult;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jejkal
 */
public class OverallReportStatsFormatter extends BasicFormatter<OverallStatResult> {

    private final String STANDARD_TEMPLATE = new OverallStatResult().getStandardTemplate();
    private final String TEMPLATE_PROPERTY = "overall.report.stats.bbexport.template";

    @Override
    public String formatElements(List<OverallStatResult> pElements, boolean pExtended) {
        OverallStatResult res = pElements.get(0);
        String[] replacements = res.getReplacements(pExtended);
        String template = getStandardTemplate();
        template = StringUtils.replaceEach(template, res.getBBVariables(), replacements);
        return template;
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
        for (String var : new OverallStatResult().getBBVariables()) {
            vars.add(var);
        }
        return vars.toArray(new String[vars.size()]);
    }
}
