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
package de.tor.tribes.types;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
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
    public static final int NO_ICON = ImageManager.NOTE_SYMBOL_NONE;
    public static final int OFF_ICON = ImageManager.NOTE_SYMBOL_AXE;
    public static final int FAKE_ICON = ImageManager.NOTE_SYMBOL_FAKE;
    public static final int SNOB_ICON = ImageManager.NOTE_SYMBOL_SNOB;
    public static final int SUPPORT_ICON = ImageManager.NOTE_SYMBOL_SPEAR;
    public static final int FAKE_SUPPORT_ICON = ImageManager.NOTE_SYMBOL_FAKE_DEF;
    private int icon = NO_ICON;

    public StandardAttack() {
        this(null);
    }

    public StandardAttack(String pName, int pIcon) {
        setName(pName);
        setIcon(pIcon);
        elements = new ArrayList<>();
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

    public boolean equals(Hashtable<UnitHolder, Integer> pUnits) {
        if (containsDynamicAmount()) {
            return false;
        }

        Set<Entry<UnitHolder, Integer>> entries = pUnits.entrySet();
        for (Entry<UnitHolder, Integer> entry : entries) {
            if (getFixedAmountForUnit(entry.getKey()) != entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public boolean containsDynamicAmount() {
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (!getElementForUnit(unit).isFixed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toXml() {
        StringBuilder b = new StringBuilder();
        try {
            b.append("<").append(getElementIdentifier()).append(" name=\"").append(URLEncoder.encode(name, "UTF-8")).append("\" icon=\"").append(getIcon()).append("\">\n");
            b.append("<attackElements>\n");
            for (StandardAttackElement elem : elements) {
                b.append(elem.toXml());
            }
            b.append("</attackElements>\n");
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
            setIcon(Integer.parseInt(e.getAttribute("icon").getValue()));
        } catch (Exception ex) {
            setIcon(-1);
        }

        elements.clear();
        for (Element aElem : (List<Element>) JaxenUtils.getNodes(e, "attackElements/attackElement")) {
            try {
                StandardAttackElement elem = StandardAttackElement.fromXml(aElem);
                elements.add(elem);
            } catch (Exception ex) {
                //ignore
            }
        }
    }

    public final void setName(String pName) {
        name = pName;
    }

    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public final void setIcon(int pIcon) {
        icon = pIcon;
    }

    public final int getIcon() {
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

    public int getAmountForUnit(UnitHolder pUnit, Village pVillage) {
        StandardAttackElement element = getElementForUnit(pUnit);
        return (element == null) ? 0 : element.getTroopsAmount(pVillage);
    }

    public int getFixedAmountForUnit(UnitHolder pUnit) {
        StandardAttackElement element = getElementForUnit(pUnit);
        return (element == null) ? 0 : getElementForUnit(pUnit).getTroopsAmount();
    }
}
