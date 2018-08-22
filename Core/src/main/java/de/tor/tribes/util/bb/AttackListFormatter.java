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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Torridity
 */
public class AttackListFormatter extends BasicFormatter<Attack> {

    private static final String[] VARIABLES = new String[] {LIST_START, LIST_END, ELEMENT_COUNT, ELEMENT_ID};
    public static final String STANDARD_TEMPLATE = "[b]Angriffsplan[/b]\nAnzahl der Angriffe: %ELEMENT_COUNT%\n[table]\n"
            + "[**]ID[||]Art[||]Einheit[||]Herkunft[||]Ziel[||]Abschickzeit[||]Versammlungsplatz[/**]\n"
            + "%LIST_START%[*]%ELEMENT_ID%[|]%TYPE%[|]%UNIT%[|]%SOURCE%[|]%TARGET%[|]%SEND%[|]%PLACE%[/*]%LIST_END%\n"
            + "[/table]";
    private static final String TEMPLATE_PROPERTY = "attack.list.bbexport.template";

    @Override
    public String getPropertyKey() {
        return TEMPLATE_PROPERTY;
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }
    /*
    [table]
    [**]Typ[||]Angreifer[||]VP Link[/**]
    %LIST_START%
    [*]%TYPE%[|]%ATTACKER%[|]%PLACE%[/*]
    %LIST_END%
    [/table]
    
    [table]
    [**]head1[||]head2[/**]
    [*]test1[|]test2
    [/table]
     */

    @Override
    public String[] getTemplateVariables() {
        List<String> vars = new LinkedList<>();
        Collections.addAll(vars, VARIABLES);
        Collections.addAll(vars, new Attack().getBBVariables());
        return vars.toArray(new String[vars.size()]);
    }
}
