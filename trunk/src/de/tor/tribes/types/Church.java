/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Color;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Church {

    public static final int NO_CHURCH = 0;
    public static final int RANGE1 = 4;
    public static final int RANGE2 = 6;
    public static final int RANGE3 = 8;
    private int villageId = 0;
    private int range = NO_CHURCH;
    private transient Color rangeColor = Color.WHITE;

    public Church() {
    }

    public Church(Element pElement) throws Exception {
        setVillageId(Integer.parseInt(pElement.getChild("village").getText()));
        setRange(Integer.parseInt(pElement.getChild("range").getText()));
        try {
            Element e = pElement.getChild("color");
            int red = e.getAttribute("r").getIntValue();
            int green = e.getAttribute("g").getIntValue();
            int blue = e.getAttribute("b").getIntValue();
            int alpha = e.getAttribute("a").getIntValue();
            setRangeColor(new Color(red, green, blue, alpha));
        } catch (Exception e) {
            //try to read old color value
            Color DEFAULT = Color.LIGHT_GRAY;
            try {
                if (Integer.parseInt(GlobalOptions.getProperty("default.mark")) == 1) {
                    DEFAULT = Color.RED;
                }
                try {
                    Village v = DataHolder.getSingleton().getVillagesById().get(getVillageId());
                    if (v != null) {
                        Tribe t = v.getTribe();
                        if (t != null && t.equals(DSWorkbenchMainFrame.getSingleton().getCurrentUser())) {
                            DEFAULT = Color.YELLOW;
                        }
                    }
                } catch (Exception ignore) {
                }
            } catch (Exception inner) {
                DEFAULT = Color.LIGHT_GRAY;
            }
            setRangeColor(DEFAULT);
        }
    }

    public String toXml() {
        try {
            String xml = "<church>\n";
            xml +=
                    "<village>" + getVillageId() + "</village>\n";
            xml +=
                    "<range>" + getRange() + "</range>\n";
            /*String hexCol = Integer.toHexString(getMarkerColor().getRGB());
            hexCol = "#" + hexCol.substring(2, hexCol.length());*/
            int red = getRangeColor().getRed();
            int green = getRangeColor().getGreen();
            int blue = getRangeColor().getBlue();
            int alpha = getRangeColor().getAlpha();
            xml +=
                    "<color r=\"" + red + "\" g=\"" + green + "\" b=\"" + blue + "\" a=\"" + alpha + "\"/>\n";
            xml +=
                    "</church>";
            return xml;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * @return the villageId
     */
    public int getVillageId() {
        return villageId;
    }

    /**
     * @param villageId the villageId to set
     */
    public void setVillageId(int villageId) {
        this.villageId = villageId;
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
        return rangeColor;
    }

    /**
     * @param rangeColor the rangeColor to set
     */
    public void setRangeColor(Color rangeColor) {
        this.rangeColor = rangeColor;
    }
}
