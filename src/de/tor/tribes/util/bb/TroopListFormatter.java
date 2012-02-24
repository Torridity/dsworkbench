/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.bb;

import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * @TODO switch to php version!?
 * @author Torridity
 */
public class TroopListFormatter extends BasicFormatter<VillageTroopsHolder> {

    private final String[] VARIABLES = new String[]{LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private final String TEMPLATE_PROPERTY = "troops.list.bbexport.template";
    private final String STANDARD_TEMPLATE = "[b]Truppenübersicht[/b]\n"
            + "[table]\n"
            + "[**]Dorf[||]%SPEAR_ICON%[||]%SWORD_ICON%[||]%AXE_ICON%[||]%ARCHER_ICON%[||]%SPY_ICON%[||]%LIGHT_ICON%[||]%MARCHER_ICON%[||]%HEAVY_ICON%[||]%RAM_ICON%[||]%CATA_ICON%[||]%KNIGHT_ICON%[||]%SNOB_ICON%[/**]\n"
            + LIST_START
            + "[*]%VILLAGE%[|]%SPEAR_AMOUNT%[|]%SWORD_AMOUNT%[|]%AXE_AMOUNT%[|]%ARCHER_AMOUNT%[|]%SPY_AMOUNT%[|]%LIGHT_AMOUNT%[|]%MARCHER_AMOUNT%[|]%HEAVY_AMOUNT%[|]%RAM_AMOUNT%[|]%CATA_AMOUNT%[|]%KNIGHT_AMOUNT%[|]%SNOB_AMOUNT%[/*]"
            + LIST_END
            + "[/table]";

    @Override
    public String formatElements(List<VillageTroopsHolder> pElements, boolean pExtended) {
        StringBuilder b = new StringBuilder();
        int cnt = 1;
        NumberFormat f = getNumberFormatter(pElements.size());
        String beforeList = getHeader();
        String listItemTemplate = getLineTemplate();
        String afterList = getFooter();
        String replacedStart = StringUtils.replaceEach(beforeList, new String[]{ELEMENT_COUNT}, new String[]{f.format(pElements.size())});

        VillageTroopsHolder dummyHolder = new VillageTroopsHolder();
        //replace unit icons
        replacedStart = StringUtils.replaceEach(replacedStart, dummyHolder.getBBVariables(), dummyHolder.getReplacements(pExtended));
        b.append(replacedStart).append("\n");
        for (VillageTroopsHolder t : pElements) {
            String[] replacements = t.getReplacements(pExtended);
            String itemLine = StringUtils.replaceEach(listItemTemplate, t.getBBVariables(), replacements);
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
        for (String var : VARIABLES) {
            vars.add(var);
        }
        for (String var : new VillageTroopsHolder().getBBVariables()) {
            vars.add(var);
        }
        return vars.toArray(new String[vars.size()]);
    }
}
