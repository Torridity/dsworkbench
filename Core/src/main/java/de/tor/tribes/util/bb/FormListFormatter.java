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

import de.tor.tribes.types.drawing.AbstractForm;
import de.tor.tribes.types.drawing.Circle;
import org.apache.commons.lang.StringUtils;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Torridity
 */
public class FormListFormatter extends BasicFormatter<AbstractForm> {

    private static final String[] VARIABLES = new String[] {LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private static final String STANDARD_TEMPLATE = "[b]Zeichnungen[/b]\nAnzahl der Zeichnungen: %ELEMENT_COUNT%\n"
            + "%LIST_START%\n" + new Circle().getStandardTemplate() + "\n%LIST_END%";
    private static final String TEMPLATE_PROPERTY = "form.list.bbexport.template";

    @Override
    public String formatElements(List<AbstractForm> pElements, boolean pExtended) {
        StringBuilder b = new StringBuilder();
        int cnt = 1;

        List<AbstractForm> allowedElements = new LinkedList<>();
        if (pElements != null) {
            for (AbstractForm f : pElements.toArray(new AbstractForm[pElements.size()])) {
                if (f.allowsBBExport()) {
                    allowedElements.add(f);
                }
            }
        }
        NumberFormat f = getNumberFormatter(allowedElements.size());
        String beforeList = getHeader();
        String listItemTemplate = getLineTemplate();
        String afterList = getFooter();
        String replacedStart = StringUtils.replaceEach(beforeList, new String[] {ELEMENT_COUNT}, new String[] {f.format(allowedElements.size())});
        b.append(replacedStart);
        for (AbstractForm form : allowedElements) {
            String[] replacements = form.getReplacements(pExtended);
            if (replacements != null) {
                String itemLine = StringUtils.replaceEach(listItemTemplate, form.getBBVariables(), replacements);
                itemLine = StringUtils.replaceEach(itemLine, new String[] {ELEMENT_ID, ELEMENT_COUNT}, new String[] {f.format(cnt), f.format(allowedElements.size())});
                b.append(itemLine).append("\n");
                cnt++;
            }
        }
        String replacedEnd = StringUtils.replaceEach(afterList, new String[] {ELEMENT_COUNT}, new String[] {f.format(allowedElements.size())});
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
        List<String> vars = new LinkedList<>();
        Collections.addAll(vars, VARIABLES);
        Collections.addAll(vars, new Circle().getBBVariables());
        return vars.toArray(new String[vars.size()]);
    }
}
