/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.MarkerCell;
import de.tor.tribes.util.BBSupport;
import java.awt.Color;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Marker extends ManageableType implements BBSupport {

    private final static String[] VARIABLES = new String[]{"%NAME%", "%BB_CODE%", "%MARKER_COLOR%"};
    private final static String STANDARD_TEMPLATE = "Anzahl der Markierungen: %ELEMENT_COUNT%\n\n"
            + "%LIST_START% [color=%MARKER_COLOR%]▓▓▓[/color] %NAME% %LIST_END%\n";

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String nameVal = null;
        String bbCodeVal = null;
        if (getMarkerType() == Marker.ALLY_MARKER_TYPE) {
            Ally a = DataHolder.getSingleton().getAllies().get(getMarkerID());
            if (a != null) {
                bbCodeVal = a.toBBCode();
                nameVal = a.getName();
            } else {
                bbCodeVal = "Ungültiger Stamm";
                nameVal = "Ungültiger Stamm";
            }
        } else {
            Tribe t = DataHolder.getSingleton().getTribes().get(getMarkerID());
            if (t != null) {
                bbCodeVal = t.toBBCode();
                nameVal = t.getName();
            } else {
                nameVal = "Ungültiger Spieler";
                bbCodeVal = "Ungültiger Spieler";
            }
        }
        String colorVal = Integer.toHexString(getMarkerColor().getRGB());
        colorVal = "#" + colorVal.substring(2, colorVal.length());

        return new String[]{nameVal, bbCodeVal, colorVal};
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }
    public final static int TRIBE_MARKER_TYPE = 0;
    public final static int ALLY_MARKER_TYPE = 1;
    private int markerType = 0;
    private int markerID = 0;
    private Color markerColor = null;
    private boolean shownOnMap = true;
    private transient MarkerCell mView = null;

    public Marker() {
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

    public static String toInternalRepresentation(Marker pMarker) {
        return pMarker.getMarkerID() + "&" + pMarker.getMarkerType() + "&" + pMarker.getMarkerColor().getRed() + "&" + pMarker.getMarkerColor().getGreen() + "&" + pMarker.getMarkerColor().getBlue() + "&" + pMarker.isShownOnMap();

    }

    public static Marker fromInternalRepresentation(String pLine) {
        Marker m = new Marker();
        try {
            String[] split = pLine.trim().split("&");
            m.setMarkerID(Integer.parseInt(split[0]));
            m.setMarkerType(Integer.parseInt(split[1]));
            m.setMarkerColor(new Color(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4])));
            m.setShownOnMap(Boolean.parseBoolean(split[5]));
        } catch (NumberFormatException nfe) {
            m = null;
        } catch (IllegalArgumentException iae) {
            m = null;
        }
        return m;
    }

    @Override
    public String toXml() {
        try {
            StringBuilder b = new StringBuilder();
            b.append("<marker>\n");
            b.append("<type>").append(getMarkerType()).append("</type>\n");
            b.append("<id>").append(getMarkerID()).append("</id>\n");
            int red = getMarkerColor().getRed();
            int green = getMarkerColor().getGreen();
            int blue = getMarkerColor().getBlue();
            int alpha = getMarkerColor().getAlpha();
            b.append("<color r=\"").append(red).append("\" g=\"").append(green).append("\" b=\"").append(blue).append("\" a=\"").append(alpha).append("\"/>\n");
            b.append("<shownOnMap>").append(isShownOnMap()).append("</shownOnMap>\n");
            b.append("</marker>");
            return b.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getElementIdentifier() {
        return "marker";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "markers";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "name";
    }

    @Override
    public void loadFromXml(Element pElement) {
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
        try {
            String value = pElement.getChild("shownOnMap").getText();
            setShownOnMap(Boolean.parseBoolean(value));
        } catch (Exception e) {
            //try to read old format
            setShownOnMap(true);
        }
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
