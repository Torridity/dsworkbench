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

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.util.BBSupport;
import java.awt.Color;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Marker extends ManageableType implements BBSupport {
    private final static Logger logger = Logger.getLogger("Marker");

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
        if (type == MarkerType.ALLY) {
            Ally a = DataHolder.getSingleton().getAllies().get(markerID);
            if (a != null) {
                bbCodeVal = a.toBBCode();
                nameVal = a.getName();
            } else {
                bbCodeVal = "Ungültiger Stamm";
                nameVal = "Ungültiger Stamm";
            }
        } else {
            Tribe t = DataHolder.getSingleton().getTribes().get(markerID);
            if (t != null) {
                bbCodeVal = t.toBBCode();
                nameVal = t.getName();
            } else {
                nameVal = "Ungültiger Spieler";
                bbCodeVal = "Ungültiger Spieler";
            }
        }
        String colorVal = Integer.toHexString(markerColor.getRGB());
        colorVal = "#" + colorVal.substring(2, colorVal.length());

        return new String[]{nameVal, bbCodeVal, colorVal};
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }
    
    public enum MarkerType {
        TRIBE,
        ALLY
    }
    
    private MarkerType type;
    private int markerID;
    private Color markerColor = null;
    private boolean shownOnMap = true;
    private Tribe mTribe = null;
    private Ally mAlly = null;

    public Marker() {
    }

    public MarkerType getMarkerType() {
        return type;
    }

    public void setMarkerType(MarkerType markerType) {
        this.type = markerType;
        checkAllyTribe();
    }

    public int getMarkerID() {
        return markerID;
    }

    public void setMarkerID(int pMarkerID) {
        this.markerID = pMarkerID;
        checkAllyTribe();
    }
    
    private void checkAllyTribe() {
        if(type == MarkerType.ALLY) {
            mAlly = DataHolder.getSingleton().getAllies().get(markerID);
            mTribe = null;
        } else if(type == MarkerType.TRIBE) {
            mTribe = DataHolder.getSingleton().getTribes().get(markerID);
            mAlly = null;
        }
    }
    
    public Ally getAlly() {
        return mAlly;
    }

    public Tribe getTribe() {
        return mTribe;
    }

    public Color getMarkerColor() {
        return markerColor;
    }
    
    public void setMarkerColor(Color markerColor) {
        this.markerColor = markerColor;
    }

    public static String toInternalRepresentation(Marker pMarker) {
        return pMarker.getMarkerID() + "&" + (pMarker.getMarkerType()==MarkerType.ALLY?1:0)
                + "&" + pMarker.getMarkerColor().getRed()
                + "&" + pMarker.getMarkerColor().getGreen()
                + "&" + pMarker.getMarkerColor().getBlue()
                + "&" + pMarker.isShownOnMap();

    }

    public static Marker fromInternalRepresentation(String pLine) {
        try {
            String[] split = pLine.trim().split("&");
            Marker m = new Marker();
            m.setMarkerID(Integer.parseInt(split[0]));
            m.setMarkerType((Integer.parseInt(split[1]) == 1)?(MarkerType.ALLY):(MarkerType.TRIBE));
            m.setMarkerColor(new Color(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4])));
            m.setShownOnMap(Boolean.parseBoolean(split[5]));
            return m;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public String toXml() {
        try {
            StringBuilder b = new StringBuilder();
            b.append("<marker>\n");
            b.append("<type>").append(type==MarkerType.ALLY?1:0).append("</type>\n");
            b.append("<id>").append(markerID).append("</id>\n");
            int red = markerColor.getRed();
            int green = markerColor.getGreen();
            int blue = markerColor.getBlue();
            int alpha = markerColor.getAlpha();
            b.append("<color r=\"").append(red).append("\" g=\"").append(green).append("\" b=\"").append(blue).append("\" a=\"").append(alpha).append("\"/>\n");
            b.append("<shownOnMap>").append(shownOnMap).append("</shownOnMap>\n");
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
        try {
            setMarkerType((Integer.parseInt(pElement.getChild("type").getText()) == 1)
                    ?(MarkerType.ALLY):(MarkerType.TRIBE));
            setMarkerID(Integer.parseInt(pElement.getChild("id").getText()));
            
            Element e = pElement.getChild("color");
            int red = e.getAttribute("r").getIntValue();
            int green = e.getAttribute("g").getIntValue();
            int blue = e.getAttribute("b").getIntValue();
            int alpha = e.getAttribute("a").getIntValue();
            this.markerColor = new Color(red, green, blue, alpha);
            
            String value = pElement.getChild("shownOnMap").getText();
            this.shownOnMap = Boolean.parseBoolean(value);
        } catch (Exception e) {
            logger.warn("Failed to decode XML", e);
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
