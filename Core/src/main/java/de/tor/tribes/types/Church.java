/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import java.awt.Color;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Church extends ManageableType {

    public static final int NO_CHURCH = 0;
    public static final int RANGE1 = 4;
    public static final int RANGE2 = 6;
    public static final int RANGE3 = 8;
    private Village village = null;
    private int range = NO_CHURCH;
    // private transient Color rangeColor = Color.WHITE;

    public Church() {
    }

    @Override
    public String toXml() {
        try {
            String xml = "<church>\n";
            xml += "<village>" + getVillage().getId() + "</village>\n";
            xml += "<range>" + getRange() + "</range>\n";
            xml += "</church>";
            return xml;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * @return the villageId
     */
    public Village getVillage() {
        return village;
    }

    /**
     * @param pVillage 
     * @param villageId the villageId to set
     */
    public void setVillage(Village pVillage) {
        this.village = pVillage;
    }

    /**
     * @return the range
     */
    public int getRange() {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * @return the rangeColor
     */
    public Color getRangeColor() {
        return getVillage().getTribe().getMarkerColor();
    }

    @Override
    public String getElementIdentifier() {
        return "church";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "churches";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }

    @Override
    public void loadFromXml(Element pElement) {
        setVillage(DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(pElement.getChild("village").getText())));
        setRange(Integer.parseInt(pElement.getChild("range").getText()));
    }
}
