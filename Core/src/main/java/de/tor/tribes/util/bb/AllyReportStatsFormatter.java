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

import de.tor.tribes.types.AllyStatResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Torridity
 */
public class AllyReportStatsFormatter extends BasicFormatter<AllyStatResult> {

    private static final String[] VARIABLES = new String[] {LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private static final String STANDARD_TEMPLATE = new AllyStatResult().getStandardTemplate();
    private static final String TEMPLATE_PROPERTY = "ally.report.stats.bbexport.template";

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
        vars.addAll(Arrays.asList(VARIABLES));
        vars.addAll(Arrays.asList(new AllyStatResult().getBBVariables()));
        return vars.toArray(new String[vars.size()]);
    }
}
