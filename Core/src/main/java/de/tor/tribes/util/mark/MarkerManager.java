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
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Manager implementation to handle markers for tribes and allies.<BR> The global MarkerManager can be accessed using the getSingleton()
 * method.<BR> Markers can either be hold in files as well as in a database (not implemented yet).<BR> The graphical representation can be
 * realized by a table using the getTableModel() method.
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
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        invalidate();
        initialize();
        File markerFile = new File(pFile);
        if (markerFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading markers from '" + pFile + "'");
            }
            try {
                Document d = JDomUtils.getDocument(markerFile);
                for (Element e : (List<Element>) JDomUtils.getNodes(d, "markerSets/markerSet")) {
                    String setKey = e.getAttributeValue("name");
                    setKey = URLDecoder.decode(setKey, "UTF-8");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading marker set '" + setKey + "'");
                    }
                    addGroup(setKey);
                    for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "markers/marker")) {
                        Marker m = new Marker();
                        m.loadFromXml(e1);
                        if (!groupExists(setKey)) {
                            addGroup(setKey);
                        }
                        addManagedElement(setKey, m);
                    }
                }
                logger.debug("Markers successfully loaded");
            } catch (Exception e) {
                logger.error("Failed to load markers", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Marker file not found under '" + pFile + "'");
            }
        }
        revalidate();
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }
        logger.debug("Importing markers");
        boolean result = false;
        invalidate();
        try {
            Document d = JDomUtils.getDocument(pFile);
            for (Element e : (List<Element>) JDomUtils.getNodes(d, "markerSets/markerSet")) {
                String setKey = e.getAttributeValue("name");
                setKey = URLDecoder.decode(setKey, "UTF-8");
                if (pExtension != null) {
                    setKey += "_" + pExtension;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Loading marker set '" + setKey + "'");
                }
                for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "markers/marker")) {
                    Marker m = new Marker();
                    m.loadFromXml(e1);
                    if (!groupExists(setKey)) {
                        addGroup(setKey);
                    }
                    addManagedElement(setKey, m);
                }
            }
            logger.debug("Markers imported successfully");
            result = true;
        } catch (Exception e) {
            logger.error("Failed to import markers", e);
            MinimapPanel.getSingleton().redraw();

        }
        revalidate(true);
        return result;
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        logger.debug("Generating marker export data");

        StringBuilder b = new StringBuilder();
        b.append("<markerSets>\n");
        for (String set : pGroupsToExport) {
            try {
                b.append("<markerSet name=\"").append(URLEncoder.encode(set, "UTF-8")).append("\">\n");
                b.append("<markers>\n");
                ManageableType[] elements = getAllElements(set).toArray(new ManageableType[getAllElements(set).size()]);

                for (ManageableType t : elements) {
                    Marker m = (Marker) t;
                    b.append(m.toXml()).append("\n");
                }
                b.append("</markers>\n");
                b.append("</markerSet>\n");
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed to export marker set '" + set + "'", e);
            }
        }
        b.append("</markerSets>\n");
        logger.debug("Export data generated successfully");
        return b.toString();
    }

    @Override
    public void saveElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Writing markers to '" + pFile + "'");
        }
        try {
            StringBuilder b = new StringBuilder();
            b.append("<data><markerSets>\n");
            Iterator<String> setKeys = getGroupIterator();
            while (setKeys.hasNext()) {
                String group = setKeys.next();

                b.append("<markerSet name=\"").append(URLEncoder.encode(group, "UTF-8")).append("\">\n");
                b.append("<markers>\n");
                for (ManageableType t : getAllElements(group)) {
                    Marker m = (Marker) t;
                    b.append(m.toXml()).append("\n");
                }
                b.append("</markers>\n");
                b.append("</markerSet>\n");

            }
            b.append("</markerSets></data>");
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
            logger.debug("Markers successfully saved");
        } catch (Exception e) {
            if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
                //this should only happen at the first start
                logger.info("Ignoring error, server directory does not exists yet");
            } else {
                logger.error("Failed to save markers", e);
            }
        }
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
