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
package de.tor.tribes.util.xml;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class XMLHelper {

    public static String troopsToXML(Hashtable<UnitHolder, Integer> pTroops) {
        StringBuilder b = new StringBuilder();
        b.append("<troops ");
        List<UnitHolder> units = DataHolder.getSingleton().getUnits();
        for (UnitHolder unit : units) {
            Integer am = pTroops.get(unit);
            if (am != null && am != 0) {
                b.append(unit.getPlainName()).append("=\"").append(am).append("\" ");
            }
        }
        b.append("/>\n");
        return b.toString();
    }

    public static Hashtable<UnitHolder, Integer> xmlToTroops(Element pElement) {
        Hashtable<UnitHolder, Integer> troops = new Hashtable<UnitHolder, Integer>();

        Element troopsElement = (Element) JaxenUtils.getNodes(pElement, "troops").get(0);
        if (troopsElement == null) {
            return troops;
        }
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            try {
                Attribute attrib = troopsElement.getAttribute(unit.getPlainName());
                if (attrib != null) {
                    troops.put(unit, attrib.getIntValue());
                } else {
                    troops.put(unit, Integer.valueOf(0));
                }
            } catch (Exception ex) {
                troops.put(unit, Integer.valueOf(0));
            }
        }
        return troops;
    }
}
