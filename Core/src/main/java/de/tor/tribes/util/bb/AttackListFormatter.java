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

import de.tor.tribes.types.Attack;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.JOptionPaneHelper;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Torridity
 */
public class AttackListFormatter extends BasicFormatter<Attack> {

    private static final String[] VARIABLES = new String[] {LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    private static final String STANDARD_TEMPLATE = "Anzahl der Angriffe: %ELEMENT_COUNT%\n[table]\n"
            + "[**]ID[||]Art[||]Einheit[||]Herkunft[||]Ziel[||]Abschickzeit[||]Versammlungsplatz[/**]\n"
            + "%LIST_START%[*]%ELEMENT_ID%[|]%TYPE%[|]%UNIT%[|]%SOURCE%[|]%TARGET%[|]%SEND%[|]%PLACE%[/*]%LIST_END%\n"
            + "[/table]";
    private static final String TEMPLATE_PROPERTY = "attack.list.bbexport.template";
    
    
    private static final String IGM_STANDARD_TEMPLATE = "Anzahl der Angriffe: %ELEMENT_COUNT%\n\n%LIST_START%\n"
            + "%TYPE% von %ATTACKER% aus %SOURCE% mit %UNIT% auf %DEFENDER% in %TARGET% startet am "
            + "[color=#ff0e0e]%SEND%[/color] und kommt am [color=#2eb92e]%ARRIVE%[/color] an (%PLACE%)"
            + "%LIST_END%\n";
    private static final String IGM_TEMPLATE_PROPERTY = "attack.list.bbexport.igmtemplate";
    
    private static Logger logger = LogManager.getLogger("AttackListFormatter");
    
    private boolean igmExport;
    public AttackListFormatter(boolean forIGM) {
        this.igmExport = forIGM;
    }
    
    @Override
    public String getPropertyKey() {
        if(igmExport) return IGM_TEMPLATE_PROPERTY;
        return TEMPLATE_PROPERTY;
    }

    @Override
    public String getStandardTemplate() {
        if(igmExport) return IGM_STANDARD_TEMPLATE;
        return STANDARD_TEMPLATE;
    }

    @Override
    public String[] getTemplateVariables() {
        List<String> vars = new LinkedList<>();
        Collections.addAll(vars, VARIABLES);
        Collections.addAll(vars, new Attack().getBBVariables());
        return vars.toArray(new String[vars.size()]);
    }
    
    public static String AttackListToBBCodes(Component parent, List<Attack> attacks, String planType) {
        int answer = JOptionPaneHelper.showQuestionThreeChoicesBox(parent, "Welcher BB-Codes Typ soll verwendet werden?\n(Erweiterte BB-Codes sind nur für das Forum und die Notizen geeignet)", "Erweiterter BB-Code", "Normal", "IGM", "Erweitert");

        StringBuilder buffer = new StringBuilder();
        //JOptionPane.NO_OPTION means normal
        boolean forIGM = answer == JOptionPane.YES_OPTION;
        boolean extended = answer == JOptionPane.CANCEL_OPTION;

        if (extended) {
            buffer.append("[u][size=12]").append(planType).append("[/size][/u]\n\n");
        } else {
            buffer.append("[u]").append(planType).append("[/u]\n\n");
        }

        buffer.append(new AttackListFormatter(forIGM).formatElements(attacks, extended));

        if (extended) {
            buffer.append("\n[size=8]Erstellt am ");
            buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
            buffer.append(" mit DS Workbench ");
            buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/size]\n");
        } else {
            buffer.append("\nErstellt am ");
            buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
            buffer.append(" mit DS Workbench ");
            buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "\n");
        }

        return buffer.toString();
    }
    
    @Override
    public Class<Attack> getConvertableType() {
        return Attack.class;
    }
}
