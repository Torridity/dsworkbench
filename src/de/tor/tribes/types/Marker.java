/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.MarkerCell;
import java.awt.Color;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Marker {

    public final static int TRIBE_MARKER_TYPE = 0;
    public final static int ALLY_MARKER_TYPE = 1;
    private int markerType = 0;
    private int markerID = 0;
    private Color markerColor = null;
    private transient MarkerCell mView = null;

    public Marker() {
    }

    public Marker(Element pElement) throws Exception {
        setMarkerType(Integer.parseInt(pElement.getChild("type").getText()));
        try {
            setMarkerID(Integer.parseInt(pElement.getChild("id").getText()));
        } catch (Exception e) {
            //try to read old marker version with plain text value
            String value = pElement.getChild("value").getText();
            if (getMarkerType() == Marker.TRIBE_MARKER_TYPE) {
                setMarkerID(DataHolder.getSingleton().getTribeByName(value).getId());
            } else {
                setMarkerID(DataHolder.getSingleton().getAllyByName(value).getId());
            }
        }
        try {
            Element e = pElement.getChild("color");
            int red = e.getAttribute("r").getIntValue();
            int green = e.getAttribute("g").getIntValue();
            int blue = e.getAttribute("b").getIntValue();
            int alpha = e.getAttribute("a").getIntValue();
            setMarkerColor(new Color(red, green, blue, alpha));
        } catch (Exception e) {
            //try to read old color value
            setMarkerColor(Color.decode(pElement.getChild("color").getText()));
        }
    }

    public int getMarkerType() {
        return markerType;
    }

    public void setMarkerType(int markerType) {
        this.markerType = markerType;
        mView = null;
    }

    public int getMarkerID() {
        return markerID;
    }

    public void setMarkerID(int pMarkerID) {
        this.markerID = pMarkerID;
        mView = null;
    }

    public Color getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }

    public MarkerCell getView() {
        if (mView == null) {
            try {
                mView = MarkerCell.factoryMarkerCell(this);
            } catch (Exception e) {
                mView = null;
            }
        }
        return mView;
    }

    public static Marker fromXml(Element pElement) throws Exception {
        return new Marker(pElement);
    }

    public String toXml() {
        try {
            String xml = "<marker>\n";
            xml += "<type>" + getMarkerType() + "</type>\n";
            xml += "<id>" + getMarkerID() + "</id>\n";
            /*String hexCol = Integer.toHexString(getMarkerColor().getRGB());
            hexCol = "#" + hexCol.substring(2, hexCol.length());*/
            int red = getMarkerColor().getRed();
            int green = getMarkerColor().getGreen();
            int blue = getMarkerColor().getBlue();
            int alpha = getMarkerColor().getAlpha();
            xml += "<color r=\"" + red + "\" g=\"" + green + "\" b=\"" + blue + "\" a=\"" + alpha + "\"/>\n";
            xml += "</marker>";
            return xml;
        } catch (Exception e) {
            return null;
        }
    }
}
