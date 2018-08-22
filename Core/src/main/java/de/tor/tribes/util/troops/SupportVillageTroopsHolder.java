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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.xml.JDomUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.jdom2.Element;

/**
 * @author Torridity
 */
public class SupportVillageTroopsHolder extends VillageTroopsHolder {

    private HashMap<Village, TroopAmountFixed> outgoingSupports = null;
    private HashMap<Village, TroopAmountFixed> incomingSupports = null;

    @Override
    public void loadFromXml(Element e) {
        super.loadFromXml(e);
        try {
            List<Element> supportElements = (List<Element>) JDomUtils.getNodes(e, "supportTargets/supportTarget");
            for (Element source : supportElements) {
                int id = Integer.parseInt(source.getChildText("village"));
                Village village = DataHolder.getSingleton().getVillagesById().get(id);
                TroopAmountFixed supportAmount = new TroopAmountFixed(source);
                addOutgoingSupport(village, supportAmount);
            }

            supportElements = (List<Element>) JDomUtils.getNodes(e, "supportSources/supportSource");
            for (Element source : supportElements) {
                int id = source.getAttribute("village").getIntValue();
                Village village = DataHolder.getSingleton().getVillagesById().get(id);
                TroopAmountFixed supportAmount = new TroopAmountFixed(source);
                addIncomingSupport(village, supportAmount);
            }
        } catch (Exception newFeature) {
            //no support data yet
        }
    }

    public SupportVillageTroopsHolder() {
        this(null, null);
    }

    public SupportVillageTroopsHolder(Village pVillage, Date pState) {
        super(pVillage, pState);
        incomingSupports = new HashMap<>();
        outgoingSupports = new HashMap<>();
    }

    @Override
    public Element toXml(String elementName) {
        Element support = super.toXml(elementName);
        
        Element supportTargets = new Element("supportTargets");
        for (Village key: outgoingSupports.keySet()) {
            Element target = outgoingSupports.get(key).toXml("supportTarget");
            target.setAttribute("village", Integer.toString(key.getId()));
            supportTargets.addContent(target);
        }
        support.addContent(supportTargets);

        Element supportSources = new Element("supportSources");
        for (Village key: incomingSupports.keySet()) {
            Element target = incomingSupports.get(key).toXml("supportSource");
            target.setAttribute("village", Integer.toString(key.getId()));
            supportSources.addContent(target);
        }
        support.addContent(supportSources);
        return support;
    }

    @Override
    public void clear() {
        super.clear();
        clearSupports();
    }

    @Override
    public float getFarmSpace() {
        double farmSpace = 0;
        Set<Village> villageKeys = incomingSupports.keySet();
        for (Village key: villageKeys) {
            farmSpace += incomingSupports.get(key).getTroopPopCount();
        }

        int max = GlobalOptions.getProperties().getInt("max.farm.space");
        //calculate farm space depending on pop bonus
        float res = (float) (farmSpace / (double) max);

        return (res > 1.0f) ? 1.0f : res;
    }

    public void clearSupports() {
        //remove supports to this village
        incomingSupports.clear();
        outgoingSupports.clear();
    }

    public void addOutgoingSupport(Village pTarget, TroopAmountFixed pTroops) {
        if (outgoingSupports.get(pTarget) != null) {
            TroopAmountFixed existingTroops = outgoingSupports.get(pTarget);
            existingTroops.addAmount(pTroops);
        } else {
            outgoingSupports.put(pTarget, (TroopAmountFixed) pTroops.clone());
        }

    }

    public void addIncomingSupport(Village pSource, TroopAmountFixed pTroops) {
        if (incomingSupports.get(pSource) != null) {
            TroopAmountFixed existingTroops = incomingSupports.get(pSource);
            existingTroops.addAmount(pTroops);
        } else {
            incomingSupports.put(pSource, (TroopAmountFixed) pTroops.clone());
        }
    }

    public HashMap<Village, TroopAmountFixed> getIncomingSupports() {
        return incomingSupports;
    }

    public HashMap<Village, TroopAmountFixed> getOutgoingSupports() {
        return outgoingSupports;
    }

    @Override
    public TroopAmountFixed getTroops() {
        TroopAmountFixed troopsInVillage = new TroopAmountFixed();
        Set<Village> villageKeys = incomingSupports.keySet();
        for (Village key: villageKeys) {
            troopsInVillage.addAmount(incomingSupports.get(key));
        }
        return troopsInVillage;
    }

    @Override
    public String toString() {
        /*
         * String result = ""; result += "Village: " + getVillage() + "\n"; Enumeration<UnitHolder> keys = getTroops().keys(); result +=
         * "Truppen\n"; while (keys.hasMoreElements()) { UnitHolder unit = keys.nextElement(); result += unit.getName() + " " +
         * getTroops().get(unit) + "\n"; } return result;
         */
        if (getVillage() != null) {
            return getVillage().toString();
        }
        return "Ungültiges Dorf";
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        Village v = getVillage();
        String villageVal = "-";
        if (v != null) {
            villageVal = getVillage().toBBCode();
        }
        
        TroopAmountFixed troops = getTroops();
        String spearIcon = "[unit]spear[/unit]";
        String spearVal = getValueForUnit(troops, "spear");
        String swordIcon = "[unit]sword[/unit]";
        String swordVal = getValueForUnit(troops, "sword");
        String axeIcon = "[unit]axe[/unit]";
        String axeVal = getValueForUnit(troops, "axe");
        String archerIcon = "[unit]archer[/unit]";
        String archerVal = getValueForUnit(troops, "archer");
        String spyIcon = "[unit]spy[/unit]";
        String spyVal = getValueForUnit(troops, "spy");
        String lightIcon = "[unit]light[/unit]";
        String lightVal = getValueForUnit(troops, "light");
        String marcherIcon = "[unit]marcher[/unit]";
        String marcherVal = getValueForUnit(troops, "marcher");
        String heavyIcon = "[unit]heavy[/unit]";
        String heavyVal = getValueForUnit(troops, "heavy");
        String ramIcon = "[unit]ram[/unit]";
        String ramVal = getValueForUnit(troops, "ram");
        String cataIcon = "[unit]catapult[/unit]";
        String cataVal = getValueForUnit(troops, "catapult");
        String snobIcon = "[unit]snob[/unit]";
        String snobVal = getValueForUnit(troops, "snob");
        String knightIcon = "[unit]knight[/unit]";
        String knightVal = getValueForUnit(troops, "knight");
        String militiaIcon = "[unit]militia[/unit]";
        String militiaVal = getValueForUnit(troops, "militia");

        return new String[]{villageVal, spearIcon, swordIcon, axeIcon, archerIcon, spyIcon, lightIcon, marcherIcon, heavyIcon, ramIcon, cataIcon, knightIcon, snobIcon, militiaIcon,
                    spearVal, swordVal, axeVal, archerVal, spyVal, lightVal, marcherVal, heavyVal, ramVal, cataVal, knightVal, snobVal, militiaVal};
    }

    private String getValueForUnit(TroopAmountFixed pTroops, String pName) {
        UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(pName);
        if (u == null) {
            return "-";
        }
        Integer i = null;
        if (pTroops != null) {
            i = pTroops.getAmountForUnit(u);
        } else {
            i = 0;
        }

        return i.toString();
    }
}
