/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.Serializable;
import java.util.Date;
import org.jdom.Element;
import de.tor.tribes.util.GlobalOptions;

/**
 *
 * @author Charon
 */
public class Attack implements Serializable {

    public static final int NO_TYPE = 0;
    public static final int CLEAN_TYPE = 1;
    public static final int SNOB_TYPE = 2;
    public static final int SUPPORT_TYPE = 3;
    public static final int FAKE_TYPE = 4;
    private static final long serialVersionUID = 10L;
    private Village source = null;
    private Village target = null;
    private UnitHolder unit = null;
    private Date arriveTime = null;
    private boolean showOnMap = false;
    private int type = 0;

    public Attack() {
        try {
            showOnMap = Boolean.parseBoolean(GlobalOptions.getProperty("draw.attacks.by.default"));
        } catch (Exception e) {
        }
    }

    public Attack(Element pElement) {
        setSource(DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("source").getText())));
        setTarget(DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("target").getText())));
        setArriveTime(new Date(Long.parseLong(pElement.getChild("arrive").getText())));
        setUnit(DataHolder.getSingleton().getUnitByPlainName(pElement.getChild("unit").getText()));
        setShowOnMap(Boolean.parseBoolean(JaxenUtils.getNodeValue(pElement, "extensions/showOnMap")));
        try {
            setType(Integer.parseInt(JaxenUtils.getNodeValue(pElement, "extensions/type")));
        } catch (Exception e) {
            //no type set
            setType(NO_TYPE);
        }
    }

    public boolean isSourceVillage(Village pVillage) {
        return (pVillage == source);
    }

    public boolean isTargetVillage(Village pVillage) {
        return (pVillage == target);
    }

    public Village getSource() {
        return source;
    }

    public void setSource(Village source) {
        this.source = source;
    }

    public Village getTarget() {
        return target;
    }

    public void setTarget(Village target) {
        this.target = target;
    }

    public UnitHolder getUnit() {
        return unit;
    }

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    public Date getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Date arriveTime) {
        this.arriveTime = arriveTime;
    }

    public boolean isShowOnMap() {
        return showOnMap;
    }

    public void setShowOnMap(boolean showOnMap) {
        this.showOnMap = showOnMap;
    }

    public static Attack fromXml(Element pElement) {
        return new Attack(pElement);
    }

    public String toXml() {
        String xml = "<attack>\n";
        xml += "<source>" + getSource().getId() + "</source>\n";
        xml += "<target>" + getTarget().getId() + "</target>\n";
        xml += "<arrive>" + getArriveTime().getTime() + "</arrive>\n";
        xml += "<unit>" + getUnit().getPlainName() + "</unit>\n";
        xml += "<extensions>\n";
        xml += "\t<showOnMap>" + isShowOnMap() + "</showOnMap>\n";
        xml += "\t<type>" + getType() + "</type>\n";
        xml += "</extensions>\n";
        xml += "</attack>";
        return xml;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }
    /*
    <attack>
    <source>VillageID</source>
    <target>VillageID</target>
    <arrive>Timestamp</arrive>
    <unit>Name</unit>
    <extensions>
    <showOnMap>true</showOnMap>
     <type>0</type>
    </extensions>
    </attack>
     */
}
