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
package de.tor.tribes.util.troops;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.interfaces.BBFormatterInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 * @author Torridity
 */
public class VillageTroopsHolder extends ManageableType implements BBSupport {
    private static Logger logger = LogManager.getLogger("VillageTroopsHolder");

    public final static String[] UNIT_NAMES = new String[] {
        "spear", "sword", "axe", "archer", "spy", "light", "marcher",
        "heavy", "ram", "catapult", "knight", "snob", "militia",
    };
    
    public final static String[] BB_VARIABLES;
    public final static String STANDARD_TEMPLATE;
    static {
        List<String> bbTemp = new ArrayList<>();
        bbTemp.addAll(Arrays.asList(new Village().getBBVariables()));
        bbTemp.add("%UPDATE%");
        for (String unitName : UNIT_NAMES) {
            bbTemp.add("%" + unitName.toUpperCase() + "_ICON%");
        }
        for (String unitName : UNIT_NAMES) {
            bbTemp.add("%" + unitName.toUpperCase() + "_AMOUNT%");
        }
        BB_VARIABLES = bbTemp.toArray(new String[bbTemp.size()]);
        
        StringBuilder stdTemplate = new StringBuilder();
        stdTemplate.append("[b]Truppenübersicht[/b]\n");
        stdTemplate.append("[table]\n");
        stdTemplate.append("[**]Dorf");
        for (String unitName : UNIT_NAMES) {
            stdTemplate.append("[||]%").append(unitName.toUpperCase()).append("_ICON%");
        }
        stdTemplate.append("[/**]\n");
        stdTemplate.append(BBFormatterInterface.LIST_START);
        stdTemplate.append("[*]%VILLAGE%");
        for (String unitName : UNIT_NAMES) {
            stdTemplate.append("[|]").append("%").append(unitName.toUpperCase()).append("_AMOUNT%");
        }
        stdTemplate.append("[/*]\n");
        stdTemplate.append(BBFormatterInterface.LIST_END);
        stdTemplate.append("[/table]");
        STANDARD_TEMPLATE = stdTemplate.toString();
    }
    
    private Village village = null;
    private TroopAmountFixed troops = new TroopAmountFixed();
    private Date state = null;

    @Override
    public void loadFromXml(Element e) {
        this.village = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(e.getChild("id").getText()));

        TroopAmountFixed hTroops = new TroopAmountFixed(e.getChild("troops"));
        setTroops(hTroops);
        this.state = new Date(Long.parseLong(e.getChild("state").getText()));
    }

    @Override
    public Element toXml(String elementName) {
        if(state == null) {
            state = new Date(0);
            logger.error("Found empty state: {}", village);
        }
        
        Element troopInfo = new Element(elementName);
        troopInfo.setAttribute("type", "normal");
        troopInfo.addContent(new Element("id").setText(Integer.toString(village.getId())));
        troopInfo.addContent(new Element("state").setText(Long.toString(state.getTime())));
        troopInfo.addContent(troops.toXml("troops"));
        return troopInfo;
    }

    public VillageTroopsHolder() {
        this(null, null);
    }

    public VillageTroopsHolder(Village pVillage, Date pState) {
        troops = new TroopAmountFixed(0);
        this.village = pVillage;
        this.state = pState;
    }

    public void clear() {
        troops.fill(-1);
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village mVillage) {
        this.village = mVillage;
    }

    public void setTroops(TroopAmountFixed pTroops) {
        setState(new Date());
        troops = pTroops.clone();
    }

    public TroopAmountFixed getTroops() {
        return troops;
    }

    public Date getState() {
        return state;
    }

    public void setState(Date mState) {
        this.state = mState;
    }

    @Override
    public String toString() {
        String result = "";
        result += "Village: " + village + "\n";
        result += "Truppen\n";
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            result += unit.getName() + " " + troops.getAmountForUnit(unit) + "\n";
        }
        return result;
    }

    @Override
    public String[] getBBVariables() {
        return BB_VARIABLES;
    }

    /**
     * All changes here need to be also done in SupportVillageHolder for compability
     */
    @Override
    public String[] getReplacements(boolean pExtended) {
        List<String> replacements = new ArrayList<>();
        if(getVillage() != null) {
            replacements.addAll(Arrays.asList(getVillage().getReplacements(pExtended)));
        } else {
            int size = new Village().getBBVariables().length;
            for(int i = 0; i < size; i++) {
                replacements.add("-");
            }
        }
        
        String updateVal = "-";
        if(state != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
            updateVal = sdf.format(state);
        }
        replacements.add(updateVal);
        
        for (String unitName : UNIT_NAMES) {
            replacements.add("[unit]" + unitName + "[/unit]");
        }
        for (String unitName : UNIT_NAMES) {
            replacements.add(getValueForUnit(unitName));
        }
        return replacements.toArray(new String[replacements.size()]);
    }

    private String getValueForUnit(String pName) {
        UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(pName);
        if (u == null) {
            return "-";
        }
        Integer i = troops.getAmountForUnit(u);
        return i.toString();
    }
}
