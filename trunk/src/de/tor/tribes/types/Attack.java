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

    private static final long serialVersionUID = 10L;
    private Village source = null;
    private Village target = null;
    private UnitHolder unit = null;
    private Date arriveTime = null;
    private boolean showOnMap = false;

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
        xml += "</extensions>\n";
        xml += "</attack>";
        return xml;
    }
    /*
    <attack>
    <source>VillageID</source>
    <target>VillageID</target>
    <arrive>Timestamp</arrive>
    <unit>Name</unit>
    <extensions>
    <showOnMap>true</showOnMap>
    </extensions>
    </attack>
     */
}
