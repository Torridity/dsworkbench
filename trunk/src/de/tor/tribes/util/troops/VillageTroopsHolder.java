/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.xml.JaxenUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Element;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import java.util.LinkedList;

/**
 * @author Jejkal
 */
public class VillageTroopsHolder {

    private Village village = null;
    private Hashtable<UnitHolder, Integer> ownTroops = null;
    private Hashtable<UnitHolder, Integer> troopsInVillage = null;
    private Hashtable<UnitHolder, Integer> troopsOutside = null;
    private Hashtable<UnitHolder, Integer> troopsOnTheWay = null;
    private List<Village> supportTargets = null;
    private Hashtable<Village, Hashtable<UnitHolder, Integer>> supports = null;
    private Date state = null;

    public static VillageTroopsHolder fromXml(Element e) throws Exception {
        Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(e.getChild("id").getText()));
        long state = Long.parseLong(e.getChild("state").getText());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(state);
        VillageTroopsHolder holder = new VillageTroopsHolder(v, c.getTime());

        Element own = (Element) JaxenUtils.getNodes(e, "troops/own").get(0);
        Element inVillage = (Element) JaxenUtils.getNodes(e, "troops/inVillage").get(0);
        Element outside = (Element) JaxenUtils.getNodes(e, "troops/outside").get(0);
        Element onTheWay = (Element) JaxenUtils.getNodes(e, "troops/onTheWay").get(0);

        Hashtable<UnitHolder, Integer> ownTroops = new Hashtable<UnitHolder, Integer>();
        Hashtable<UnitHolder, Integer> troopsInVillage = new Hashtable<UnitHolder, Integer>();
        Hashtable<UnitHolder, Integer> troopsOutside = new Hashtable<UnitHolder, Integer>();
        Hashtable<UnitHolder, Integer> troopsOnTheWay = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            ownTroops.put(unit, own.getAttribute(unit.getPlainName()).getIntValue());
            troopsInVillage.put(unit, inVillage.getAttribute(unit.getPlainName()).getIntValue());
            troopsOutside.put(unit, outside.getAttribute(unit.getPlainName()).getIntValue());
            troopsOnTheWay.put(unit, onTheWay.getAttribute(unit.getPlainName()).getIntValue());
        }
        holder.setOwnTroops(ownTroops);
        holder.setTroopsInVillage(troopsInVillage);
        holder.setTroopsOutside(troopsOutside);
        holder.setTroopsOnTheWay(troopsOnTheWay);
        try {
            List<Element> supportElements = (List<Element>) JaxenUtils.getNodes(e, "troops/supportTargets/supportTarget");
            for (Element target : supportElements) {
                int id = Integer.parseInt(target.getText());
                holder.addSupportTarget(DataHolder.getSingleton().getVillagesById().get(id));
            }

            supportElements = (List<Element>) JaxenUtils.getNodes(e, "troops/supportSources/supportSource");
            for (Element source : supportElements) {
                int id = source.getAttribute("village").getIntValue();
                Village village = DataHolder.getSingleton().getVillagesById().get(id);
                Hashtable<UnitHolder, Integer> supportAmount = new Hashtable<UnitHolder, Integer>();
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    supportAmount.put(unit, source.getAttribute(unit.getPlainName()).getIntValue());
                }
                holder.addSupport(village, supportAmount);
            }
        } catch (Exception newFeature) {
            //no support data yet
        }
        return holder;
    }

    public VillageTroopsHolder(Village pVillage, Date pState) {
        ownTroops = new Hashtable<UnitHolder, Integer>();
        troopsInVillage = new Hashtable<UnitHolder, Integer>();
        troopsOutside = new Hashtable<UnitHolder, Integer>();
        troopsOnTheWay = new Hashtable<UnitHolder, Integer>();
        supports = new Hashtable<Village, Hashtable<UnitHolder, Integer>>();
        supportTargets = new LinkedList<Village>();
        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            ownTroops.put(u, 0);
            troopsInVillage.put(u, 0);
            troopsOutside.put(u, 0);
            troopsOnTheWay.put(u, 0);
        }
        setVillage(pVillage);
        setState(pState);
    }

    public String toXml() {
        String result = "<village>\n";
        result += "<id>" + getVillage().getId() + "</id>\n";
        result += "<state>" + getState().getTime() + "</state>\n";
        result += "<troops>\n";

        String own = "<own ";
        String inVillage = "<inVillage ";
        String outside = "<outside ";
        String onTheWay = "<onTheWay ";
        List<UnitHolder> units = DataHolder.getSingleton().getUnits();
        for (UnitHolder unit : units) {
            own += unit.getPlainName() + "=\"" + ownTroops.get(unit) + "\" ";
            inVillage += unit.getPlainName() + "=\"" + troopsInVillage.get(unit) + "\" ";
            outside += unit.getPlainName() + "=\"" + troopsOutside.get(unit) + "\" ";
            onTheWay += unit.getPlainName() + "=\"" + troopsOnTheWay.get(unit) + "\" ";
        }
        own += "/>\n";
        inVillage += "/>\n";
        outside += "/>\n";
        onTheWay += "/>\n";
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
        result += own + inVillage + outside + onTheWay + targets + supportSrc;
        result += "</troops>\n";
        result += "</village>";
        return result;
    }

    public void clear() {
        ownTroops.clear();
        troopsInVillage.clear();
        troopsOutside.clear();
        troopsOnTheWay.clear();
        clearSupportTargets();
        clearSupports();
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village mVillage) {
        this.village = mVillage;
    }

    public Hashtable<UnitHolder, Integer> getOwnTroops() {
        return ownTroops;
    }

    /**Returns own troops in village plus supports
     */
    public Hashtable<UnitHolder, Integer> getTroopsInVillage() {
        return troopsInVillage;
    }

    public Hashtable<UnitHolder, Integer> getTroopsOutside() {
        return troopsOutside;
    }

    public Hashtable<UnitHolder, Integer> getTroopsOnTheWay() {
        return troopsOnTheWay;
    }

    public Hashtable<UnitHolder, Integer> getForeignTroops() {
        Hashtable<UnitHolder, Integer> foreign = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            Integer own = getOwnTroops().get(u);
            Integer inVillage = getTroopsInVillage().get(u);
            int result = inVillage - own;
            result = (result < 0) ? 0 : result;
            foreign.put(u, result);
        }
        return foreign;
    }

    public int getTroopsOfUnitInVillage(UnitHolder pUnit) {
        return troopsInVillage.get(pUnit);
    }

    public void setOwnTroops(Hashtable<UnitHolder, Integer> mTroops) {
        ownTroops = (Hashtable<UnitHolder, Integer>) mTroops.clone();
    }

    public void setTroopsInVillage(Hashtable<UnitHolder, Integer> mTroops) {
        troopsInVillage = (Hashtable<UnitHolder, Integer>) mTroops.clone();
    }

    public void setTroopsOutside(Hashtable<UnitHolder, Integer> mTroops) {
        troopsOutside = (Hashtable<UnitHolder, Integer>) mTroops.clone();
    }

    public float getFarmSpace() {
        Hashtable<UnitHolder, Integer> own = getOwnTroops();
        Hashtable<UnitHolder, Integer> outside = getTroopsOutside();
        Hashtable<UnitHolder, Integer> onTheWay = getTroopsOnTheWay();
        double farmSpace = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            farmSpace += own.get(unit) * unit.getPop();
            farmSpace += outside.get(unit) * unit.getPop();
            farmSpace += onTheWay.get(unit) * unit.getPop();
        }

        boolean isPopBonus = (getVillage().getType() == 4);

        //calculate farm space depending on pop bonus
        float res = (float) (farmSpace / ((!isPopBonus) ? 24000 : 26400));
        return (res > 1.0f) ? 1.0f : res;
    }

    public void clearSupports() {
        //remove supports to this village
        Enumeration<Village> supportKeys = supports.keys();
        while (supportKeys.hasMoreElements()) {
            Village v = supportKeys.nextElement();
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
            if (holder != null) {
                holder.getSupportTargets().remove(v);
            }
        }
        supports.clear();
    }

    public void clearSupportTargets() {
        //remove this village as troop source
        for (Village v : supportTargets) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
            if (holder != null) {
                //remove support from this village
                holder.getSupports().remove(this);
                holder.updateSupportValues();
            }
        }
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
        try {
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
        }
    }

    public Hashtable<Village, Hashtable<UnitHolder, Integer>> getSupports() {
        return supports;
    }

    public void setTroopsOnTheWay(Hashtable<UnitHolder, Integer> mTroops) {
        troopsOnTheWay = (Hashtable<UnitHolder, Integer>) mTroops.clone();
    }

    public Date getState() {
        return state;
    }

    public void setState(Date mState) {
        this.state = mState;
    }

    public double getOffValue(int type) {
        Hashtable<UnitHolder, Integer> active = null;
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

        return result;
    }

    public double getRealOffValue(int type) {
        Hashtable<UnitHolder, Integer> active = null;
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
            if (unit.getPlainName().equals("axe") ||
                    unit.getPlainName().equals("light") ||
                    unit.getPlainName().equals("marcher") ||
                    unit.getPlainName().equals("heavy") ||
                    unit.getPlainName().equals("ram") ||
                    unit.getPlainName().equals("catapult")) {
                result += unit.getAttack() * active.get(unit);
            }
        }

        return result;
    }

    public double getDefValue(int type) {
        Hashtable<UnitHolder, Integer> active = null;
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

        return result;
    }

    public double getDefArcherValue(int type) {
        Hashtable<UnitHolder, Integer> active = null;
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

        return result;
    }

    public double getDefCavalryValue(int type) {
        Hashtable<UnitHolder, Integer> active = null;
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

        return result;
    }

    public int getTroopPopCount(int type) {
        Hashtable<UnitHolder, Integer> active = null;
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

        return result;
    }

    /*   public void calculateTroopsPowers() {
    Enumeration<UnitHolder> keys = troopsInVillage.keys();
    try {
    while (keys.hasMoreElements()) {
    UnitHolder unit = keys.nextElement();
    iOffPower += troopsInVillage.get(unit) * unit.getAttack();
    iDefPower += troopsInVillage.get(unit) * unit.getDefense();
    iArchDefPower += troopsInVillage.get(unit) * unit.getDefenseArcher();
    iCavDefPower += troopsInVillage.get(unit) * unit.getDefenseCavalry();
    }
    iOffPower = (iOffPower < 0) ? 0 : iOffPower;
    iDefPower = (iDefPower < 0) ? 0 : iDefPower;
    iArchDefPower = (iArchDefPower < 0) ? 0 : iArchDefPower;
    iCavDefPower = (iCavDefPower < 0) ? 0 : iCavDefPower;
    } catch (Exception e) {
    }
    }*/
    /* public void calculateTroopsPowers() {
    // if ((iOffPower < 0) || (iDefPower < 0) || (iArchDefPower < 0) || (iCavDefPower < 0)) {
    try {
    for (int i = 0; i < troops.size(); i++) {
    iOffPower += troops.get(i) * DataHolder.getSingleton().getUnits().get(i).getAttack();
    iDefPower += troops.get(i) * DataHolder.getSingleton().getUnits().get(i).getDefense();
    iArchDefPower += troops.get(i) * DataHolder.getSingleton().getUnits().get(i).getDefenseArcher();
    iCavDefPower += troops.get(i) * DataHolder.getSingleton().getUnits().get(i).getDefenseCavalry();
    }
    iOffPower = (iOffPower < 0) ? 0 : iOffPower;
    iDefPower = (iDefPower < 0) ? 0 : iDefPower;
    iArchDefPower = (iArchDefPower < 0) ? 0 : iArchDefPower;
    iCavDefPower = (iCavDefPower < 0) ? 0 : iCavDefPower;
    } catch (Exception e) {
    //units not loaded yet
    }
    // }
    }*/

    /*   public void recalculateTroopsPower() {
    iOffPower = 0;
    iDefPower = 0;
    iArchDefPower = 0;
    iCavDefPower = 0;
    calculateTroopsPowers();
    }
     */
    public String toString() {
        String result = "";
        result += "Village: " + getVillage() + "\n";
        Enumeration<UnitHolder> keys = ownTroops.keys();
        result += "Eigene\n";
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            result += unit.getName() + " " + ownTroops.get(unit) + "\n";
        }
        keys = troopsInVillage.keys();
        result += "Im Dorf\n";
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            result += unit.getName() + " " + troopsInVillage.get(unit) + "\n";
        }
        keys = troopsOutside.keys();
        result += "Außerhalb\n";
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            result += unit.getName() + " " + troopsOutside.get(unit) + "\n";
        }
        keys = troopsOnTheWay.keys();
        result += "Unterwegs\n";
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            result += unit.getName() + " " + troopsOnTheWay.get(unit) + "\n";
        }
        return result;
    }
}
