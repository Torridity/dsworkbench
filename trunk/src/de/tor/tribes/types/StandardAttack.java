/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class StandardAttack extends ManageableType {

    private static Logger logger = Logger.getLogger("StandardAttack");
    private String name = null;
    private List<StandardAttackElement> elements = null;
    private int icon = -1;

    public StandardAttack() {
        this(null);
    }

    public StandardAttack(String pName, int pIcon) {
        setName(pName);
        setIcon(pIcon);
        elements = new ArrayList<StandardAttackElement>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            elements.add(new StandardAttackElement(unit, 0));
        }
    }

    public StandardAttack(String pName) {
        this(pName, -1);
    }

    @Override
    public String getElementIdentifier() {
        return "stdAttack";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "stdAttacks";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }

    @Override
    public String toXml() {
        StringBuilder b = new StringBuilder();
        try {
            b.append("<").append(getElementIdentifier()).append(" name=\"").append(URLEncoder.encode(name, "UTF-8")).append("\">");
            b.append("<icon>").append(getIcon()).append("</icon>\n");
            for (StandardAttackElement elem : elements) {
                b.append(elem.toXml());
            }
            b.append("</").append(getElementIdentifier()).append(">\n");
        } catch (IOException ioe) {
            return "\n";
        }
        return b.toString();
    }

    @Override
    public void loadFromXml(Element e) {
        try {
            setName(URLDecoder.decode(e.getAttribute("name").getValue(), "UTF-8"));
        } catch (IOException ioe) {
            setName("");
        }

        try {
            setIcon(Integer.parseInt(e.getChild("icon").getText()));
        } catch (Exception ex) {
            setIcon(-1);
        }

        elements.clear();

        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            Element unitElement = e.getChild(u.getPlainName());
            StandardAttackElement elem = null;
            try {
               // elem = StandardAttackElement.fromXml(u, unitElement);
            } catch (Exception ex) {
                elem = new StandardAttackElement(u, 0);
            }
            elements.add(elem);
        }
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getName() {
        return name;
    }

    public void setIcon(int pIcon) {
        icon = pIcon;
    }

    public int getIcon() {
        return icon;
    }

    public StandardAttackElement getElementForUnit(UnitHolder pUnit) {
        for (StandardAttackElement elem : elements) {
            if (elem.affectsUnit(pUnit)) {
                return elem;
            }
        }
        return null;
    }
}
