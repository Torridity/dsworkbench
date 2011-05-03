/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.bb;

import de.tor.tribes.types.Tag;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class TagListFormatter extends BasicFormatter<Tag> {

    private final String[] VARIABLES = new String[]{LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private final String STANDARD_TEMPLATE = "[table]\n[**]ID[||]Dorf[||]Besitzer[||]Punkte[/**]\n%LIST_START%\n"
            + "[*]%ELEMENT_ID%[|][coord]%X%|%Y%[/coord][|]%TRIBE%[|]%POINTS%[/*]\n"
            + "%LIST_END%\n[/table]";
    private final String TEMPLATE_PROPERTY = "tag.list.bbexport.template";

    @Override
    public String formatElements(List<Tag> pElements, boolean pExtended) {
      
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
        for (String var : new Tag().getBBVariables()) {
            vars.add(var);
        }
        return vars.toArray(new String[vars.size()]);
    }
    
}
