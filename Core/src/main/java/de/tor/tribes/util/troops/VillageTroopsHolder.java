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
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 * @author Torridity
 */
public class VillageTroopsHolder extends ManageableType implements BBSupport {
    private static Logger logger = LogManager.getLogger("VillageTroopsHolder");

    private final static String[] VARIABLES = new String[]{
        "%VILLAGE%", "%PLAYER%", "%ALLY%", "%PLAYER_NO_BB%", "%ALLY_NO_BB%", "%ALLY_NAME%",
        "%SPEAR_ICON%", "%SWORD_ICON%", "%AXE_ICON%", "%ARCHER_ICON%", "%SPY_ICON%", "%LIGHT_ICON%", "%MARCHER_ICON%",
        "%HEAVY_ICON%", "%RAM_ICON%", "%CATA_ICON%", "%KNIGHT_ICON%", "%SNOB_ICON%", "%MILITIA_ICON%",
        "%SPEAR_AMOUNT%", "%SWORD_AMOUNT%", "%AXE_AMOUNT%", "%ARCHER_AMOUNT%", "%SPY_AMOUNT%", "%LIGHT_AMOUNT%", "%MARCHER_AMOUNT%",
        "%HEAVY_AMOUNT%", "%RAM_AMOUNT%", "%CATA_AMOUNT%", "%KNIGHT_AMOUNT%", "%SNOB_AMOUNT%", "%MILITIA_AMOUNT%",
        "%UPDATE%"
    };
    private final static String STANDARD_TEMPLATE = "[table]\n"
            + "[**]%SPEAR_ICON%[||]%SWORD_ICON%[||]%AXE_ICON%[||]%ARCHER_ICON%[||]%SPY_ICON%[||]%LIGHT_ICON%[||]%MARCHER_ICON%[||]%HEAVY_ICON%[||]%RAM_ICON%[||]%CATA_ICON%[||]%SNOB_ICON%[/**]\n";
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
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    /**
     * All changes here need to be also done in SupportVillageHolder for compability
     */
    @Override
    public String[] getReplacements(boolean pExtended) {
        String villageVal = "-";
        String tribeVal = "-";
        String tribeNoBBVal = "-";
        String allyVal = "-";
        String allyNoBBVal = "-";
        String allyNameVal = "-";
        if (village != null) {
            villageVal = village.toBBCode();
            
            tribeVal = village.getTribe().toBBCode();
            tribeNoBBVal = village.getTribe().getName();
            Ally a = village.getTribe().getAlly();
            if (a == null) {
                a = NoAlly.getSingleton();
            }
            allyVal = a.toBBCode();
            allyNoBBVal = a.getTag();
            allyNameVal = a.getName();
        }
       
        String spearIcon = "[unit]spear[/unit]";
        String spearVal = getValueForUnit("spear");
        String swordIcon = "[unit]sword[/unit]";
        String swordVal = getValueForUnit("sword");
        String axeIcon = "[unit]axe[/unit]";
        String axeVal = getValueForUnit("axe");
        String archerIcon = "[unit]archer[/unit]";
        String archerVal = getValueForUnit("archer");
        String spyIcon = "[unit]spy[/unit]";
        String spyVal = getValueForUnit("spy");
        String lightIcon = "[unit]light[/unit]";
        String lightVal = getValueForUnit("light");
        String marcherIcon = "[unit]marcher[/unit]";
        String marcherVal = getValueForUnit("marcher");
        String heavyIcon = "[unit]heavy[/unit]";
        String heavyVal = getValueForUnit("heavy");
        String ramIcon = "[unit]ram[/unit]";
        String ramVal = getValueForUnit("ram");
        String cataIcon = "[unit]catapult[/unit]";
        String cataVal = getValueForUnit("catapult");
        String snobIcon = "[unit]snob[/unit]";
        String snobVal = getValueForUnit("snob");
        String knightIcon = "[unit]knight[/unit]";
        String knightVal = getValueForUnit("knight");
        String militiaIcon = "[unit]militia[/unit]";
        String militiaVal = getValueForUnit("militia");
        
        String updateVal = "-";
        if(state != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
            updateVal = sdf.format(state);
        }

        return new String[]{
            villageVal, tribeVal, allyVal, tribeNoBBVal, allyNoBBVal, allyNameVal,
            spearIcon, swordIcon, axeIcon, archerIcon, spyIcon, lightIcon, marcherIcon,
            heavyIcon, ramIcon, cataIcon, knightIcon, snobIcon, militiaIcon,
            spearVal, swordVal, axeVal, archerVal, spyVal, lightVal, marcherVal,
            heavyVal, ramVal, cataVal, knightVal, snobVal, militiaVal,
            updateVal
        };
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
