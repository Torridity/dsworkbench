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
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.BBSupport;
import de.tor.tribes.util.GlobalOptions;
import java.util.Date;
import org.jdom.Element;

/**
 * @author Torridity
 */
public class VillageTroopsHolder extends ManageableType implements BBSupport {

    private final static String[] VARIABLES = new String[]{"%VILLAGE%", "%SPEAR_ICON%", "%SWORD_ICON%", "%AXE_ICON%", "%ARCHER_ICON%", "%SPY_ICON%", "%LIGHT_ICON%", "%MARCHER_ICON%", "%HEAVY_ICON%", "%RAM_ICON%", "%CATA_ICON%", "%KNIGHT_ICON%", "%SNOB_ICON%", "%MILITIA_ICON%", "%SPEAR_AMOUNT%", "%SWORD_AMOUNT%", "%AXE_AMOUNT%", "%ARCHER_AMOUNT%", "%SPY_AMOUNT%", "%LIGHT_AMOUNT%", "%MARCHER_AMOUNT%", "%HEAVY_AMOUNT%", "%RAM_AMOUNT%", "%CATA_AMOUNT%", "%KNIGHT_AMOUNT%", "%SNOB_AMOUNT%", "%MILITIA_AMOUNT%"};
    private final static String STANDARD_TEMPLATE = "[table]\n"
            + "[**]%SPEAR_ICON%[||]%SWORD_ICON%[||]%AXE_ICON%[||]%ARCHER_ICON%[||]%SPY_ICON%[||]%LIGHT_ICON%[||]%MARCHER_ICON%[||]%HEAVY_ICON%[||]%RAM_ICON%[||]%CATA_ICON%[||]%SNOB_ICON%[/**]\n";
    private Village village = null;
    private TroopAmountFixed troops = new TroopAmountFixed();
    private Date state = null;

    @Override
    public void loadFromXml(Element e) {
        this.village = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(e.getChild("id").getText()));
        this.state = new Date(Long.parseLong(e.getChild("state").getText()));

        TroopAmountFixed hTroops = new TroopAmountFixed(e.getChild("troops"));
        setTroops(hTroops);
    }

    public VillageTroopsHolder() {
        this(null, null);
    }

    public VillageTroopsHolder(Village pVillage, Date pState) {
        troops = new TroopAmountFixed(0);
        this.village = pVillage;
        this.state = pState;
    }

    @Override
    public String toXml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<troopInfo>\n");
        builder.append("<id>").append(village.getId()).append("</id>\n");
        builder.append("<state>").append(state.getTime()).append("</state>\n");
        builder.append("<troops ");
        builder.append(troops.toXml());
        builder.append(" />\n");
        builder.append("</troopInfo>");
        return builder.toString();
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
        troops = pTroops.clone();
    }

    public TroopAmountFixed getTroops() {
        return troops;
    }

    public float getFarmSpace() {
        double farmSpace = troops.getTroopPopCount();
        int max = GlobalOptions.getProperties().getInt("max.farm.space");

        //calculate farm space depending on pop bonus
        float res = (float) (farmSpace / (double) max);

        return (res > 1.0f) ? 1.0f : res;
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
    public String getElementIdentifier() {
        return "troopInfo";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "troopInfos";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "name";
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        Village v = village;
        String villageVal = "-";
        if (v != null) {
            villageVal = village.toBBCode();
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

        return new String[]{villageVal, spearIcon, swordIcon, axeIcon, archerIcon, spyIcon, lightIcon, marcherIcon, heavyIcon, ramIcon, cataIcon, knightIcon, snobIcon, militiaIcon,
                    spearVal, swordVal, axeVal, archerVal, spyVal, lightVal, marcherVal, heavyVal, ramVal, cataVal, knightVal, snobVal, militiaVal};
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
