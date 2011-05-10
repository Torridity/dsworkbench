/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.control.ManageableType;
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
import de.tor.tribes.ui.models.TroopsTableModel;
import de.tor.tribes.util.GlobalOptions;
import java.util.LinkedList;
import org.jdom.DataConversionException;

/**
 * @author Jejkal
 */
public class VillageTroopsHolder extends ManageableType {

    private Village village = null;
    private Hashtable<UnitHolder, Integer> troops = null;
    private Date state = null;

    @Override
    public void loadFromXml(Element e) {
        setVillage(DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(e.getChild("id").getText())));
        setState(new Date(Long.parseLong(e.getChild("state").getText())));

        Element troopsElement = (Element) JaxenUtils.getNodes(e, "troops").get(0);

        Hashtable<UnitHolder, Integer> hTroops = new Hashtable<UnitHolder, Integer>();

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            try {
                hTroops.put(unit, troopsElement.getAttribute(unit.getPlainName()).getIntValue());
            } catch (DataConversionException dce) {
            }
        }
        setTroops(hTroops);
    }

    public VillageTroopsHolder() {
        this(null, null);
    }

    public VillageTroopsHolder(Village pVillage, Date pState) {
        troops = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            troops.put(u, 0);
        }
        setVillage(pVillage);
        setState(pState);
    }

    @Override
    public String toXml() {
        String result = "<troopInfo>\n";
        result += "<id>" + getVillage().getId() + "</id>\n";
        result += "<state>" + getState().getTime() + "</state>\n";
        result += "<troops ";

        List<UnitHolder> units = DataHolder.getSingleton().getUnits();
        for (UnitHolder unit : units) {
            result += unit.getPlainName() + "=\"" + troops.get(unit) + "\" ";
        }
        result += "/>\n";
        result += "</troopInfo>";
        return result;
    }

    public void clear() {
        troops.clear();
    }

    public Village getVillage() {
        return village;
    }

    public void setVillage(Village mVillage) {
        this.village = mVillage;
    }

    public void setTroops(Hashtable<UnitHolder, Integer> pTroops) {
        troops = (Hashtable<UnitHolder, Integer>)pTroops.clone();
    }

    public Hashtable<UnitHolder, Integer> getTroops() {
        return troops;
    }

    public int getTroopsOfUnitInVillage(UnitHolder pUnit) {
        return troops.get(pUnit);
    }

    public float getFarmSpace() {
        double farmSpace = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            farmSpace += troops.get(unit) * unit.getPop();
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

    public Date getState() {
        return state;
    }

    public void setState(Date mState) {
        this.state = mState;
    }

    public double getOffValue() {
        Enumeration<UnitHolder> units = troops.keys();
        int result = 0;
        while (units.hasMoreElements()) {
            UnitHolder unit = units.nextElement();
            result += unit.getAttack() * troops.get(unit);
        }

        return result;
    }

    public double getRealOffValue() {
        Enumeration<UnitHolder> units = troops.keys();
        int result = 0;
        while (units.hasMoreElements()) {
            UnitHolder unit = units.nextElement();
            if (unit.getPlainName().equals("axe")
                    || unit.getPlainName().equals("light")
                    || unit.getPlainName().equals("marcher")
                    || unit.getPlainName().equals("heavy")
                    || unit.getPlainName().equals("ram")
                    || unit.getPlainName().equals("catapult")) {
                result += unit.getAttack() * troops.get(unit);
            }
        }

        return result;
    }

    public double getDefValue() {
        Enumeration<UnitHolder> units = troops.keys();
        int result = 0;
        while (units.hasMoreElements()) {
            UnitHolder unit = units.nextElement();
            result += unit.getDefense() * troops.get(unit);
        }

        return result;
    }

    public double getDefArcherValue() {
        Enumeration<UnitHolder> units = troops.keys();
        int result = 0;
        while (units.hasMoreElements()) {
            UnitHolder unit = units.nextElement();
            result += unit.getDefenseArcher() * troops.get(unit);
        }

        return result;
    }

    public double getDefCavalryValue() {
        Enumeration<UnitHolder> units = troops.keys();
        int result = 0;
        while (units.hasMoreElements()) {
            UnitHolder unit = units.nextElement();
            result += unit.getDefenseCavalry() * troops.get(unit);
        }

        return result;
    }

    public int getTroopPopCount() {
        Enumeration<UnitHolder> units = troops.keys();
        int result = 0;
        while (units.hasMoreElements()) {
            UnitHolder unit = units.nextElement();
            result += unit.getPop() * troops.get(unit);
        }

        return result;
    }

    @Override
    public String toString() {
        String result = "";
        result += "Village: " + getVillage() + "\n";
        Enumeration<UnitHolder> keys = troops.keys();
        result += "Truppen\n";
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            result += unit.getName() + " " + troops.get(unit) + "\n";
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
}
