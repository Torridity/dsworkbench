/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.xml;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import java.util.Hashtable;
import java.util.List;
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
            if (am != null) {
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
                troops.put(unit, troopsElement.getAttribute(unit.getPlainName()).getIntValue());
            } catch (Exception ex) {
            }
        }
        return troops;
    }
}
