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
import de.tor.tribes.io.TroopAmountDynamic;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.ui.ImageManager;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class StandardAttack extends ManageableType {

    private static Logger logger = Logger.getLogger("StandardAttack");
    private String name = null;
    private TroopAmountDynamic troops = null;
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
        name = pName;
        icon = pIcon;
        troops = new TroopAmountDynamic(0);
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
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.name.hashCode();
        hash = 97 * hash + this.troops.hashCode();
        hash = 97 * hash + this.icon;
        return hash;
    }

    @Override
    public boolean equals(Object pOther) {
        if(pOther instanceof TroopAmountDynamic) {
            return troops.equals(pOther);
        }
        if(pOther instanceof  TroopAmountFixed) {
            if(!troops.isFixed()) return false;
            return troops.transformToFixed(null).equals(pOther);
        }
        if(pOther instanceof StandardAttack) {
            return pOther.hashCode() == this.hashCode();
        }
        return false;
    }

    @Override
    public String toXml() {
        StringBuilder b = new StringBuilder();
        try {
            b.append("<").append(getElementIdentifier()).append(" name=\"").append(URLEncoder.encode(name, "UTF-8")).append("\" icon=\"").append(icon).append("\">\n");
            b.append("<attackElements ");
            b.append(troops.toXml());
            b.append(" />\n");
            b.append("</").append(getElementIdentifier()).append(">\n");
        } catch (IOException ioe) {
            return "\n";
        }
        return b.toString();
    }

    @Override
    public void loadFromXml(Element e) {
        try {
            name = URLDecoder.decode(e.getAttribute("name").getValue(), "UTF-8");
        } catch (IOException ioe) {
            name = "";
        }

        try {
            icon = Integer.parseInt(e.getAttribute("icon").getValue());
        } catch (Exception ex) {
            icon = -1;
        }

        troops = new TroopAmountDynamic(e.getChild("attackElements"));
    }

    public final void setName(String pName) {
        name = pName;
    }

    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public final void setIcon(int pIcon) {
        icon = pIcon;
    }

    public final int getIcon() {
        return icon;
    }

    public TroopAmountDynamic getTroops() {
        return troops;
    }
}
