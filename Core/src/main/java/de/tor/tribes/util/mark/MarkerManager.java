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
package de.tor.tribes.util.mark;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Marker.MarkerType;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.MinimapPanel;
import de.tor.tribes.util.xml.JDomUtils;
import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 * Manager implementation to handle markers for tribes and allies.<BR>
 * The global MarkerManager can be accessed using the getSingleton() method.<BR>
 * The graphical representation can be realized by a table using the getTableModel() method.
 *
 * @author Torridity
 */
public class MarkerManager extends GenericManager<Marker> {

    private static Logger logger = LogManager.getLogger("MarkerManager");
    private static MarkerManager SINGLETON = null;

    public static synchronized MarkerManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MarkerManager();
        }
        return SINGLETON;
    }

    /**
     * Internal constructor
     */
    MarkerManager() {
        super(true);
    }

    @Override
    public int importData(Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        logger.debug("Reading marker");
        invalidate();
        try {
            for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "markerSets/markerSet")) {
                String setKey = e.getAttributeValue("name");
                setKey = URLDecoder.decode(setKey, "UTF-8");
                if (pExtension != null) {
                    setKey += "_" + pExtension;
                }
                
                logger.debug("Loading marker set '{}'", setKey);
                for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "markers/marker")) {
                    Marker m = new Marker();
                    m.loadFromXml(e1);
                    if (!groupExists(setKey)) {
                        addGroup(setKey);
                    }
                    addManagedElement(setKey, m);
                    result++;
                }
            }
            logger.debug("Markers successfully loaded");
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to load markers", e);
            MinimapPanel.getSingleton().redraw();
        }
        revalidate(true);
        return result;
    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element markerSets = new Element("markerSets");
        logger.debug("Generating marker data");
        
        for (String set : pGroupsToExport) {
            try {
                Element markerSet = new Element("markerSet");
                markerSet.setAttribute("name", URLEncoder.encode(set, "UTF-8"));
                
                Element markers = new Element("markers");
                for (ManageableType t : getAllElements(set)) {
                    markers.addContent(t.toXml("marker"));
                }
                markerSet.addContent(markers);
                markerSets.addContent(markerSet);
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed to generate marker set '" + set + "'", e);
            }
        }
        
        logger.debug("Data generated successfully");
        return markerSets;
    }

    @Override
    public String[] getGroups() {
        String[] groups = super.getGroups();
        Arrays.sort(groups, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                if (o1.equals(DEFAULT_GROUP)) {
                    return -1;
                } else if (o2.equals(DEFAULT_GROUP)) {
                    return 1;
                } else {
                    return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
                }
            }
        });
        return groups;
    }

    /**
     * Add an ally marker
     *
     * @param pAlly
     * @param pColor
     */
    public void addMarker(Ally pAlly, Color pColor) {
        if (pAlly == null) {
            return;
        }

        addMarkerInternal(Marker.MarkerType.ALLY, pAlly.getId(), pColor);
    }

    /**
     * Add a tribe marker
     *
     * @param pTribe
     * @param pColor
     */
    public void addMarker(Tribe pTribe, Color pColor) {
        if (pTribe == null) {
            return;
        }
        addMarkerInternal(Marker.MarkerType.TRIBE, pTribe.getId(), pColor);
    }

    /**
     * Add a marker by value (for internal use only)
     */
    private void addMarkerInternal(MarkerType pType, int pId, Color pColor) {
        Marker m;
        switch (pType) {
            case TRIBE:
                m = getMarker(DataHolder.getSingleton().getTribes().get(pId));
                break;
            case ALLY:
                m = getMarker(DataHolder.getSingleton().getAllies().get(pId));
                break;
            default:
                m = null;
        }

        if (m != null) {
            m.setMarkerColor(pColor);
        } else {
            m = new Marker();
            m.setMarkerType(pType);
            m.setMarkerID(pId);
            m.setMarkerColor(pColor);
            addManagedElement(m);
        }
    }

    /**
     * And both, a tribe marker and an ally marker
     *
     * @param pTribe
     * @param pTribeColor
     * @param pAlly
     * @param pAllyColor
     */
    public void addMarker(Tribe pTribe, Color pTribeColor, Ally pAlly, Color pAllyColor) {
        invalidate();
        addMarkerInternal(Marker.MarkerType.TRIBE, pTribe.getId(), pTribeColor);
        addMarkerInternal(Marker.MarkerType.ALLY, pAlly.getId(), pAllyColor);
        revalidate(true);
    }

    public Marker getMarker(Tribe pTribe) {
        if (pTribe == null) {
            return null;
        }


        for (ManageableType t : getAllElementsFromAllGroups()) {
            Marker m = (Marker) t;
            if ((m.getMarkerType() == Marker.MarkerType.TRIBE) && (m.getMarkerID() == pTribe.getId())) {
                return m;
            }
        }

        //no marker found
        return null;
    }

    public Marker getMarker(Ally pAlly) {
        if (pAlly == null) {
            return null;
        }
        for (ManageableType t : getAllElementsFromAllGroups()) {
            Marker m = (Marker) t;
            if ((m.getMarkerType() == Marker.MarkerType.ALLY) && (m.getMarkerID() == pAlly.getId())) {
                return m;
            }
        }

        //no marker found
        return null;
    }

    public Marker getMarker(Village pVillage) {
        if (pVillage == null) {
            return null;
        }

        Tribe tribe = pVillage.getTribe();

        if (tribe.equals(Barbarians.getSingleton())) {
            //barbarians cannot be marked
            return null;
        }

        Ally ally = tribe.getAlly();

        for (String group : getGroups()) {
            for (ManageableType t : getAllElements(group)) {
                Marker m = (Marker) t;
                if (((m.getMarkerType() == Marker.MarkerType.TRIBE) && (m.getMarkerID() == tribe.getId()))
                        || (ally != null && (m.getMarkerType() == Marker.MarkerType.ALLY) && (m.getMarkerID() == ally.getId()))) {
                    return m;
                }
            }
        }
        //no marker found
        return null;
    }
}
