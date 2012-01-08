/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.xml.JaxenUtils;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Element;
import de.tor.tribes.util.GlobalOptions;

/**
 * @author Jejkal
 */
public class SupportVillageTroopsHolder extends VillageTroopsHolder {

    private Hashtable<Village, Hashtable<UnitHolder, Integer>> outgoingSupports = null;
    private Hashtable<Village, Hashtable<UnitHolder, Integer>> incomingSupports = null;

    @Override
    public void loadFromXml(Element e) {
        super.loadFromXml(e);
        try {
            List<Element> supportElements = (List<Element>) JaxenUtils.getNodes(e, "supportTargets/supportTarget");
            for (Element source : supportElements) {
                int id = source.getAttribute("village").getIntValue();
                Village village = DataHolder.getSingleton().getVillagesById().get(id);
                Hashtable<UnitHolder, Integer> supportAmount = new Hashtable<UnitHolder, Integer>();
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    supportAmount.put(unit, source.getAttribute(unit.getPlainName()).getIntValue());
                }
                addOutgoingSupport(village, supportAmount);
            }

            supportElements = (List<Element>) JaxenUtils.getNodes(e, "supportSources/supportSource");
            for (Element source : supportElements) {
                int id = source.getAttribute("village").getIntValue();
                Village village = DataHolder.getSingleton().getVillagesById().get(id);
                Hashtable<UnitHolder, Integer> supportAmount = new Hashtable<UnitHolder, Integer>();
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    supportAmount.put(unit, source.getAttribute(unit.getPlainName()).getIntValue());
                }
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
        incomingSupports = new Hashtable<Village, Hashtable<UnitHolder, Integer>>();
        outgoingSupports = new Hashtable<Village, Hashtable<UnitHolder, Integer>>();
    }

    @Override
    public String toXml() {
        String result = "<troopInfo type=\"support\">\n";
        result += "<id>" + getVillage().getId() + "</id>\n";
        result += "<state>" + getState().getTime() + "</state>\n";
        result += "<troops ";

        List<UnitHolder> units = DataHolder.getSingleton().getUnits();
        for (UnitHolder unit : units) {
            result += unit.getPlainName() + "=\"" + getTroops().get(unit) + "\" ";
        }
        result += "/>\n";
        Enumeration<Village> keys = outgoingSupports.keys();
        String supportTrg = "<supportTargets>\n";
        while (keys.hasMoreElements()) {
            Village key = keys.nextElement();
            String support = "<supportTarget village=\"" + key.getId() + "\" ";
            for (UnitHolder unit : units) {
                support += unit.getPlainName() + "=\"" + outgoingSupports.get(key).get(unit) + "\" ";
            }
            support += "/>\n";
            supportTrg += support;
        }
        supportTrg += "</supportTargets>\n";

        keys = incomingSupports.keys();
        String supportSrc = "<supportSources>\n";
        while (keys.hasMoreElements()) {
            Village key = keys.nextElement();
            String support = "<supportSource village=\"" + key.getId() + "\" ";
            for (UnitHolder unit : units) {
                support += unit.getPlainName() + "=\"" + incomingSupports.get(key).get(unit) + "\" ";
            }
            support += "/>\n";
            supportSrc += support;
        }
        supportSrc += "</supportSources>\n";
        result += supportTrg + supportSrc;
        result += "</troopInfo>";
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        clearSupports();
    }

    @Override
    public float getFarmSpace() {
        double farmSpace = 0;
        Enumeration<Village> villageKeys = incomingSupports.keys();
        while (villageKeys.hasMoreElements()) {
            Village key = villageKeys.nextElement();
            Hashtable<UnitHolder, Integer> amountForVillage = incomingSupports.get(key);
            Enumeration<UnitHolder> unitKeys = amountForVillage.keys();
            while (unitKeys.hasMoreElements()) {
                UnitHolder unitKey = unitKeys.nextElement();
                farmSpace += amountForVillage.get(unitKey) * unitKey.getPop();
            }
        }

        int max = 20000;
        try {
            max = Integer.parseInt(GlobalOptions.getProperty("max.farm.space"));
        } catch (Exception e) {
            max = 20000;
        }

        //calculate farm space depending on pop bonus
        float res = (float) (farmSpace / (double) max);

        return (res > 1.0f) ? 1.0f : res;
    }

    public void clearSupports() {
        //remove supports to this village
        incomingSupports.clear();
        outgoingSupports.clear();
    }

    public void addOutgoingSupport(Village pTarget, Hashtable<UnitHolder, Integer> pTroops) {
        if (outgoingSupports.get(pTarget) != null) {
            Hashtable<UnitHolder, Integer> existingTroops = outgoingSupports.get(pTarget);
            Enumeration<UnitHolder> unitKeys = pTroops.keys();
            while (unitKeys.hasMoreElements()) {
                UnitHolder key = unitKeys.nextElement();
                if (existingTroops.containsKey(key)) {
                    existingTroops.put(key, existingTroops.get(key) + pTroops.get(key));
                } else {
                    existingTroops.put(key, pTroops.get(key));
                }
            }
        } else {
            outgoingSupports.put(pTarget, (Hashtable<UnitHolder, Integer>) pTroops.clone());
        }

    }

    public void addIncomingSupport(Village pSource, Hashtable<UnitHolder, Integer> pTroops) {
        if (incomingSupports.get(pSource) != null) {
            Hashtable<UnitHolder, Integer> existingTroops = incomingSupports.get(pSource);
            Enumeration<UnitHolder> unitKeys = pTroops.keys();
            while (unitKeys.hasMoreElements()) {
                UnitHolder key = unitKeys.nextElement();
                if (existingTroops.containsKey(key)) {
                    existingTroops.put(key, existingTroops.get(key) + pTroops.get(key));
                } else {
                    existingTroops.put(key, pTroops.get(key));
                }
            }
        } else {
            incomingSupports.put(pSource, (Hashtable<UnitHolder, Integer>) pTroops.clone());
        }
    }

    public Hashtable<Village, Hashtable<UnitHolder, Integer>> getIncomingSupports() {
        return incomingSupports;
    }

    public Hashtable<Village, Hashtable<UnitHolder, Integer>> getOutgoingSupports() {
        return outgoingSupports;
    }

    public double getOffValue() {
        return 0;
    }

    public double getRealOffValue() {
        return 0;
    }

    @Override
    public double getDefValue() {
        int result = 0;
        Hashtable<UnitHolder, Integer> troops = getTroops();
        Enumeration<UnitHolder> unitKeys = troops.keys();
        while (unitKeys.hasMoreElements()) {
            UnitHolder unitKey = unitKeys.nextElement();
            result += unitKey.getDefenseCavalry() * troops.get(unitKey);
        }

        return result;
    }

    public double getDefArcherValue() {
        int result = 0;
        Hashtable<UnitHolder, Integer> troops = getTroops();
        Enumeration<UnitHolder> unitKeys = troops.keys();
        while (unitKeys.hasMoreElements()) {
            UnitHolder unitKey = unitKeys.nextElement();
            result += unitKey.getDefenseCavalry() * troops.get(unitKey);
        }

        return result;
    }

    public double getDefCavalryValue() {
        int result = 0;
        Hashtable<UnitHolder, Integer> troops = getTroops();
        Enumeration<UnitHolder> unitKeys = troops.keys();
        while (unitKeys.hasMoreElements()) {
            UnitHolder unitKey = unitKeys.nextElement();
            result += unitKey.getDefenseCavalry() * troops.get(unitKey);
        }

        return result;
    }

    @Override
    public int getTroopsOfUnitInVillage(UnitHolder pUnit) {
        return getTroops().get(pUnit);
    }

    @Override
    public Hashtable<UnitHolder, Integer> getTroops() {
        Hashtable<UnitHolder, Integer> troopsInVillage = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            troopsInVillage.put(unit, 0);
        }
        Enumeration<Village> villageKeys = incomingSupports.keys();
        while (villageKeys.hasMoreElements()) {
            Village key = villageKeys.nextElement();
            Hashtable<UnitHolder, Integer> amounts = incomingSupports.get(key);
            Enumeration<UnitHolder> unitKeys = amounts.keys();
            while (unitKeys.hasMoreElements()) {
                UnitHolder unitKey = unitKeys.nextElement();
                troopsInVillage.put(unitKey, troopsInVillage.get(unitKey) + amounts.get(unitKey));
            }
        }
        return troopsInVillage;
    }

    @Override
    public String toString() {
        /* String result = "";
        result += "Village: " + getVillage() + "\n";
        Enumeration<UnitHolder> keys = getTroops().keys();
        result += "Truppen\n";
        while (keys.hasMoreElements()) {
        UnitHolder unit = keys.nextElement();
        result += unit.getName() + " " + getTroops().get(unit) + "\n";
        }
        return result;*/
        if (getVillage() != null) {
            return getVillage().toString();
        }
        return "Ungültiges Dorf";
    }

    @Override
    public String getElementIdentifier() {
        return "village";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "villages";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        Village v = getVillage();
        String villageVal = "-";
        if (v != null) {
            villageVal = getVillage().toBBCode();
        }
        Hashtable<UnitHolder, Integer> troops = getTroops();
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

    private String getValueForUnit(Hashtable<UnitHolder, Integer> pTroops, String pName) {
        UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(pName);
        if (u == null) {
            return "-";
        }
        Integer i = null;
        if (pTroops != null) {
            i = pTroops.get(u);
            if (i == null) {
                i = 0;
            }
        } else {
            i = 0;
        }

        return i.toString();
    }
}
