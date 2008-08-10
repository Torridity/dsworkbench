/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

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
    private String markerValue = null;
    private Color markerColor = null;

    public Marker() {
    }

    public Marker(Element pElement) {
        setMarkerType(Integer.parseInt(pElement.getChild("type").getText()));
        setMarkerValue(pElement.getChild("value").getText());
        setMarkerColor(Color.decode(pElement.getChild("color").getText()));
    }

    public int getMarkerType() {
        return markerType;
    }

    public void setMarkerType(int markerType) {
        this.markerType = markerType;
    }

    public String getMarkerValue() {
        return markerValue;
    }

    public void setMarkerValue(String markerValue) {
        this.markerValue = markerValue;
    }

    public Color getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }

    public static Marker fromXml(Element pElement) {
        return new Marker(pElement);
    }

    public String toXml() {
        String xml = "<marker>\n";
        xml += "<type>" + getMarkerType() + "</type>\n";
        xml += "<value>" + getMarkerValue() + "</value>\n";
        String hexCol = Integer.toHexString(getMarkerColor().getRGB());
        hexCol = "#" + hexCol.substring(2, hexCol.length());
        xml += "<color>" + hexCol + "</color>\n";
        xml += "</marker>";
        return xml;
    }
}
