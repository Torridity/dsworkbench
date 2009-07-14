/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.util.xml.JaxenUtils;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class MarkerSet {

    private String setName = null;
    private List<Marker> markers = null;
    private boolean shownOnMap = true;

    public MarkerSet(String pSetName) {
        setSetName(pSetName);
        markers = new LinkedList<Marker>();
    }

    public static MarkerSet fromXml(Element e) throws Exception {
        String name = URLDecoder.decode(e.getAttributeValue("name"), "UTF-8");
        MarkerSet set = new MarkerSet(name);
        set.setShownOnMap(Boolean.parseBoolean(e.getChild("shownOnMap").getValue()));
        for (Element m : (List<Element>) JaxenUtils.getNodes(e, "markers/marker")) {
            try {
                Marker marker = new Marker(m);
                // check if tribe/ally still exists
                if (marker.getMarkerType() == Marker.ALLY_MARKER_TYPE) {
                    if (DataHolder.getSingleton().getAllies().get(marker.getMarkerID()) != null) {
                        set.getMarkers().add(marker);
                    }
                } else if (marker.getMarkerType() == Marker.TRIBE_MARKER_TYPE) {
                    if (DataHolder.getSingleton().getTribes().get(marker.getMarkerID()) != null) {
                        set.getMarkers().add(marker);
                    }
                }
            } catch (Exception inner) {
                //ignored, marker invalid
            }
        }
        return set;
    }

    public String toXml() throws Exception {
        String result = "";

        result += "<markerSet name=\"" + URLEncoder.encode(getSetName(), "UTF-8") + "\">\n";
        result += "<shownOnMap>" + isShownOnMap() + "</shownOnMap>\n";
        result +="<markers>\n";
        for (Marker m : getMarkers()) {
            String xml = m.toXml();
            if (xml != null) {
                result += xml + "\n";
            }
        }
        result +="</markers>\n";
        result += "</markerSet>\n";
        return result;
    }

    /**
     * @return the setName
     */
    public String getSetName() {
        return setName;
    }

    /**
     * @param setName the setName to set
     */
    public void setSetName(String setName) {
        this.setName = setName;
    }

    /**
     * @return the markers
     */
    public List<Marker> getMarkers() {
        return markers;
    }

    /**
     * @param markers the markers to set
     */
    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    /**
     * @return the shownOnMap
     */
    public boolean isShownOnMap() {
        return shownOnMap;
    }

    /**
     * @param shownOnMap the shownOnMap to set
     */
    public void setShownOnMap(boolean shownOnMap) {
        this.shownOnMap = shownOnMap;
    }
}
