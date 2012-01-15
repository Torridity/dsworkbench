/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.bb;

import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Circle;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Torridity
 */
public class FormListFormatter extends BasicFormatter<AbstractForm> {

    private final String[] VARIABLES = new String[]{LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private final String STANDARD_TEMPLATE = "[b]Zeichnungen[/b]\nAnzahl der Zeichnungen: %ELEMENT_COUNT%\n"
            + "%LIST_START%\n" + new Circle().getStandardTemplate() + "\n%LIST_END%";
    private final String TEMPLATE_PROPERTY = "form.list.bbexport.template";

    @Override
    public String formatElements(List<AbstractForm> pElements, boolean pExtended) {
        StringBuilder b = new StringBuilder();
        int cnt = 1;

        List<AbstractForm> allowedElements = new LinkedList<AbstractForm>();
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
        String replacedStart = StringUtils.replaceEach(beforeList, new String[]{ELEMENT_COUNT}, new String[]{f.format(allowedElements.size())});
        b.append(replacedStart);
        for (AbstractForm form : allowedElements) {
            String[] replacements = form.getReplacements(pExtended);
            if (replacements != null) {
                String itemLine = StringUtils.replaceEach(listItemTemplate, form.getBBVariables(), replacements);
                itemLine = StringUtils.replaceEach(itemLine, new String[]{ELEMENT_ID, ELEMENT_COUNT}, new String[]{f.format(cnt), f.format(allowedElements.size())});
                b.append(itemLine).append("\n");
                cnt++;
            }
        }
        String replacedEnd = StringUtils.replaceEach(afterList, new String[]{ELEMENT_COUNT}, new String[]{f.format(allowedElements.size())});
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
        for (String var : VARIABLES) {
            vars.add(var);
        }
        for (String var : new Circle().getBBVariables()) {
            vars.add(var);
        }
        return vars.toArray(new String[vars.size()]);
    }
}