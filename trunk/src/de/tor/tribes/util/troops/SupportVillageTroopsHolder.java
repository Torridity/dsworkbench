/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.util.xml.JaxenUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Element;
import de.tor.tribes.ui.models.TroopsTableModel;
import de.tor.tribes.util.GlobalOptions;
import java.util.LinkedList;
import org.jdom.DataConversionException;

/**
 * @author Jejkal
 */
public class SupportVillageTroopsHolder extends VillageTroopsHolder {

    private List<Village> supportTargets = null;
    private Hashtable<Village, Hashtable<UnitHolder, Integer>> supports = null;

    @Override
    public void loadFromXml(Element e) {
        super.loadFromXml(e);
        try {
            List<Element> supportElements = (List<Element>) JaxenUtils.getNodes(e, "troops/supportTargets/supportTarget");
            for (Element target : supportElements) {
                int id = Integer.parseInt(target.getText());
                addSupportTarget(DataHolder.getSingleton().getVillagesById().get(id));
            }

            supportElements = (List<Element>) JaxenUtils.getNodes(e, "troops/supportSources/supportSource");
            for (Element source : supportElements) {
                int id = source.getAttribute("village").getIntValue();
                Village village = DataHolder.getSingleton().getVillagesById().get(id);
                Hashtable<UnitHolder, Integer> supportAmount = new Hashtable<UnitHolder, Integer>();
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    supportAmount.put(unit, source.getAttribute(unit.getPlainName()).getIntValue());
                }
                addSupport(village, supportAmount);
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
        supports = new Hashtable<Village, Hashtable<UnitHolder, Integer>>();
        supportTargets = new LinkedList<Village>();
    }

    @Override
    public String toXml() {
        String result = "<troopInfo type=\"support\">\n";
        result += "<id>" + getVillage().getId() + "</id>\n";
        result += "<state>" + getState().getTime() + "</state>\n";
        result += "<troops";

        List<UnitHolder> units = DataHolder.getSingleton().getUnits();
        for (UnitHolder unit : units) {
            result += unit.getPlainName() + "=\"" + getTroops().get(unit) + "\" ";
        }
        result += "/>\n";
        String targets = "<supportTargets>\n";
        for (Village supportTarget : supportTargets) {
            targets += "<supportTarget>" + supportTarget.getId() + "</supportTarget>\n";
        }
        targets += "</supportTargets>\n";

        Enumeration<Village> keys = supports.keys();
        String supportSrc = "<supportSources>\n";
        while (keys.hasMoreElements()) {
            Village key = keys.nextElement();
            String support = "<supportSource village=\"" + key.getId() + "\" ";
            for (UnitHolder unit : units) {
                support += unit.getPlainName() + "=\"" + supports.get(key).get(unit) + "\" ";
            }
            support += "/>\n";
            supportSrc += support;
        }
        supportSrc += "</supportSources>\n";
        result += targets + supportSrc;
        result += "</troopInfo>";
        return result;
    }

    public void clear() {
        super.clear();
        clearSupportTargets();
        clearSupports();
    }

    /*public Hashtable<UnitHolder, Integer> getForeignTroops() {
    Hashtable<UnitHolder, Integer> foreign = new Hashtable<UnitHolder, Integer>();
    for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
    Integer own = getOwnTroops().get(u);
    Integer inVillage = getTroopsInVillage().get(u);
    int result = inVillage - own;
    result = (result < 0) ? 0 : result;
    foreign.put(u, result);
    }
    return foreign;
    }*/
    public float getFarmSpace() {
        /*  Hashtable<UnitHolder, Integer> own = getOwnTroops();
        Hashtable<UnitHolder, Integer> outside = getTroopsOutside();
        Hashtable<UnitHolder, Integer> onTheWay = getTroopsOnTheWay();
        double farmSpace = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
        farmSpace += own.get(unit) * unit.getPop();
        farmSpace += outside.get(unit) * unit.getPop();
        farmSpace += onTheWay.get(unit) * unit.getPop();
        }
        
        int max = 20000;
        try {
        max = Integer.parseInt(GlobalOptions.getProperty("max.farm.space"));
        } catch (Exception e) {
        max = 20000;
        }
        
        //calculate farm space depending on pop bonus
        float res = (float) (farmSpace / (double) max);
        
        return (res > 1.0f) ? 1.0f : res;*/
        return 0f;
    }

    public void clearSupports() {
        //remove supports to this village
    /*    Enumeration<Village> supportKeys = supports.keys();
        while (supportKeys.hasMoreElements()) {
        Village v = supportKeys.nextElement();
        SupportVillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
        if (holder != null) {
        holder.getSupportTargets().remove(v);
        }
        }*/
        supports.clear();
    }

    public void clearSupportTargets() {
        //remove this village as troop source
       /* for (Village v : supportTargets) {
        SupportVillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
        if (holder != null) {
        //remove support from this village
        holder.getSupports().remove(getVillage());
        holder.updateSupportValues();
        }
        }*/
        //clear targets list
        supportTargets.clear();
    }

    public boolean addSupportTarget(Village pTarget) {
        if (!supportTargets.contains(pTarget)) {
            return supportTargets.add(pTarget);
        }
        return false;
    }

    public List<Village> getSupportTargets() {
        return supportTargets;
    }

    public void addSupport(Village pTarget, Hashtable<UnitHolder, Integer> pUnits) {
        supports.put(pTarget, (Hashtable<UnitHolder, Integer>) pUnits.clone());
    }

    /**Merge own troops with supports if no in-village information is available
     */
    public void updateSupportValues() {
        /* try {
        //set own to invillage if no in-village information available
        Hashtable<UnitHolder, Integer> own = getOwnTroops();
        Enumeration<UnitHolder> keys = own.keys();
        while (keys.hasMoreElements()) {
        UnitHolder unit = keys.nextElement();
        int ownValue = own.get(unit);
        int cntInVillage = ownValue;
        //add all known supports
        Enumeration<Village> supportKeys = getSupports().keys();
        while (supportKeys.hasMoreElements()) {
        Village source = supportKeys.nextElement();
        Integer amount = getSupports().get(source).get(unit);
        cntInVillage += amount;
        }
        getTroopsInVillage().put(unit, cntInVillage);
        }
        } catch (Exception e) {
        }*/
    }

    public Hashtable<Village, Hashtable<UnitHolder, Integer>> getSupports() {
        return supports;
    }

    public double getOffValue(int type) {
        /* Hashtable<UnitHolder, Integer> active = null;
        switch (type) {
        case TroopsManagerTableModel.SHOW_OWN_TROOPS:
        active = ownTroops;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_OUTSIDE:
        active = troopsOutside;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_ON_THE_WAY:
        active = troopsOnTheWay;
        break;
        case TroopsManagerTableModel.SHOW_FORGEIGN_TROOPS:
        double own = getOffValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
        double inVillage = getOffValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        double res = inVillage - own;
        return (res >= 0) ? res : 0;
        default:
        active = getTroopsInVillage();
        }
        Enumeration<UnitHolder> units = active.keys();
        int result = 0;
        while (units.hasMoreElements()) {
        UnitHolder unit = units.nextElement();
        result += unit.getAttack() * active.get(unit);
        }
        
        return result;*/
        return 0;
    }

    public double getRealOffValue(int type) {
        /* Hashtable<UnitHolder, Integer> active = null;
        switch (type) {
        case TroopsManagerTableModel.SHOW_OWN_TROOPS:
        active = ownTroops;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_OUTSIDE:
        active = troopsOutside;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_ON_THE_WAY:
        active = troopsOnTheWay;
        break;
        case TroopsManagerTableModel.SHOW_FORGEIGN_TROOPS:
        double own = getOffValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
        double inVillage = getOffValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        double res = inVillage - own;
        return (res >= 0) ? res : 0;
        default:
        active = getTroopsInVillage();
        }
        Enumeration<UnitHolder> units = active.keys();
        int result = 0;
        while (units.hasMoreElements()) {
        UnitHolder unit = units.nextElement();
        if (unit.getPlainName().equals("axe")
        || unit.getPlainName().equals("light")
        || unit.getPlainName().equals("marcher")
        || unit.getPlainName().equals("heavy")
        || unit.getPlainName().equals("ram")
        || unit.getPlainName().equals("catapult")) {
        result += unit.getAttack() * active.get(unit);
        }
        }
        
        return result;*/
        return 0;
    }

    public double getDefValue(int type) {
        /* Hashtable<UnitHolder, Integer> active = null;
        switch (type) {
        case TroopsManagerTableModel.SHOW_OWN_TROOPS:
        active = ownTroops;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_OUTSIDE:
        active = troopsOutside;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_ON_THE_WAY:
        active = troopsOnTheWay;
        break;
        case TroopsManagerTableModel.SHOW_FORGEIGN_TROOPS:
        double own = getDefValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
        double inVillage = getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        double res = inVillage - own;
        return (res >= 0) ? res : 0;
        default:
        active = getTroopsInVillage();
        }
        Enumeration<UnitHolder> units = active.keys();
        int result = 0;
        while (units.hasMoreElements()) {
        UnitHolder unit = units.nextElement();
        result += unit.getDefense() * active.get(unit);
        }
        
        return result;*/
        return 0;
    }

    public double getDefArcherValue(int type) {
        /* Hashtable<UnitHolder, Integer> active = null;
        switch (type) {
        case TroopsManagerTableModel.SHOW_OWN_TROOPS:
        active = ownTroops;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_OUTSIDE:
        active = troopsOutside;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_ON_THE_WAY:
        active = troopsOnTheWay;
        break;
        case TroopsManagerTableModel.SHOW_FORGEIGN_TROOPS:
        double own = getDefArcherValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
        double inVillage = getDefArcherValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        double res = inVillage - own;
        return (res >= 0) ? res : 0;
        default:
        active = getTroopsInVillage();
        }
        Enumeration<UnitHolder> units = active.keys();
        int result = 0;
        while (units.hasMoreElements()) {
        UnitHolder unit = units.nextElement();
        result += unit.getDefenseArcher() * active.get(unit);
        }
        
        return result;*/
        return 0;
    }

    public double getDefCavalryValue(int type) {
        /* Hashtable<UnitHolder, Integer> active = null;
        switch (type) {
        case TroopsManagerTableModel.SHOW_OWN_TROOPS:
        active = ownTroops;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_OUTSIDE:
        active = troopsOutside;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_ON_THE_WAY:
        active = troopsOnTheWay;
        break;
        case TroopsManagerTableModel.SHOW_FORGEIGN_TROOPS:
        double own = getDefCavalryValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
        double inVillage = getDefCavalryValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        double res = inVillage - own;
        return (res >= 0) ? res : 0;
        default:
        active = getTroopsInVillage();
        }
        Enumeration<UnitHolder> units = active.keys();
        int result = 0;
        while (units.hasMoreElements()) {
        UnitHolder unit = units.nextElement();
        result += unit.getDefenseCavalry() * active.get(unit);
        }
        
        return result;*/
        return 0;
    }

    public int getTroopPopCount(int type) {
        /*  Hashtable<UnitHolder, Integer> active = null;
        switch (type) {
        case TroopsManagerTableModel.SHOW_OWN_TROOPS:
        active = ownTroops;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_OUTSIDE:
        active = troopsOutside;
        break;
        case TroopsManagerTableModel.SHOW_TROOPS_ON_THE_WAY:
        active = troopsOnTheWay;
        break;
        case TroopsManagerTableModel.SHOW_FORGEIGN_TROOPS:
        int own = getTroopPopCount(TroopsManagerTableModel.SHOW_OWN_TROOPS);
        int inVillage = getTroopPopCount(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        int res = inVillage - own;
        return (res >= 0) ? res : 0;
        default:
        active = getTroopsInVillage();
        }
        
        Enumeration<UnitHolder> units = active.keys();
        int result = 0;
        while (units.hasMoreElements()) {
        UnitHolder unit = units.nextElement();
        result += unit.getPop() * active.get(unit);
        }
        
        return result;*/
        return 0;
    }

    public String toString() {
        String result = "";
        result += "Village: " + getVillage() + "\n";
        Enumeration<UnitHolder> keys = getTroops().keys();
        result += "Truppen\n";
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            result += unit.getName() + " " + getTroops().get(unit) + "\n";
        }
        return result;
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
}
