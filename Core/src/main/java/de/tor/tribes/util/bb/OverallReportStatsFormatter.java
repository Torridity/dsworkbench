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

import de.tor.tribes.types.OverallStatResult;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * @author Torridity
 */
public class OverallReportStatsFormatter extends BasicFormatter<OverallStatResult> {

    public static final String STANDARD_TEMPLATE = new OverallStatResult().getStandardTemplate();
    private static final String TEMPLATE_PROPERTY = "overall.report.stats.bbexport.template";

    @Override
    public String formatElements(List<OverallStatResult> pElements, boolean pExtended) {
        OverallStatResult res = pElements.get(0);
        String[] replacements = res.getReplacements(pExtended);
        String template = STANDARD_TEMPLATE;
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
        List<String> vars = new LinkedList<>();
        Collections.addAll(vars, new OverallStatResult().getBBVariables());
        return vars.toArray(new String[vars.size()]);
    }
}
