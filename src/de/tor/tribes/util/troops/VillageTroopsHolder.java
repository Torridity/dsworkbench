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

/**
 *
 * @author Jejkal
 */
public class VillageTroopsHolder {

    private Village village = null;
    //  private List<Integer> troops = null;
    private Hashtable<UnitHolder, Integer> ownTroops = null;
    private Hashtable<UnitHolder, Integer> troopsInVillage = null;
    private Hashtable<UnitHolder, Integer> troopsOutside = null;
    private Hashtable<UnitHolder, Integer> troopsOnTheWay = null;
    private Date state = null;
    /* private int iOffPower = -1;
    private int iDefPower = -1;
    private int iArchDefPower = -1;
    private int iCavDefPower = -1;
     */

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

        return holder;
    }

    public VillageTroopsHolder(Village pVillage, Date pState) {
        ownTroops = new Hashtable<UnitHolder, Integer>();
        troopsInVillage = new Hashtable<UnitHolder, Integer>();
        troopsOutside = new Hashtable<UnitHolder, Integer>();
        troopsOnTheWay = new Hashtable<UnitHolder, Integer>();
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
        result += own + inVillage + outside + onTheWay;
        result += "</troops>\n";
        result += "</village>";
        return result;
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

    public Hashtable<UnitHolder, Integer> getTroopsInVillage() {
        return troopsInVillage;
    }

    public Hashtable<UnitHolder, Integer> getTroopsOutside() {
        return troopsOutside;
    }

    public Hashtable<UnitHolder, Integer> getTroopsOnTheWay() {
        return troopsOnTheWay;
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
                active = troopsInVillage;
        }
        Enumeration<UnitHolder> units = active.keys();
        int result = 0;
        while (units.hasMoreElements()) {
            UnitHolder unit = units.nextElement();
            result += unit.getAttack() * active.get(unit);
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
                active = troopsInVillage;
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
                active = troopsInVillage;
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
                active = troopsInVillage;
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
                active = troopsInVillage;
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
