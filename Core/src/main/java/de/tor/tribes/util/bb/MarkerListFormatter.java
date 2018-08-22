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

import de.tor.tribes.types.Marker;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Torridity
 */
public class MarkerListFormatter extends BasicFormatter<Marker> {

    private static final String[] VARIABLES = new String[] {LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    public static final String STANDARD_TEMPLATE = "Anzahl der Markierungen: %ELEMENT_COUNT%\n\n"
            + "%LIST_START% %NAME% [color=%MARKER_COLOR%]▓▓▓[/color] %LIST_END%\n";
    private static final String TEMPLATE_PROPERTY = "marker.list.bbexport.template";

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
        Collections.addAll(vars, new Marker().getBBVariables());
        return vars.toArray(new String[vars.size()]);
    }
}
