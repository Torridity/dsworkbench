/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.mark;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.MarkerSet;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.MinimapPanel;
import de.tor.tribes.ui.models.MarkerTableModel;
import de.tor.tribes.ui.renderer.MapRenderer;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**Manager implementation to handle markers for tribes and allies.<BR> 
 * The global MarkerManager can be accessed using the getSingleton() method.<BR>
 * Markers can either be hold in files as well as in a database (not implemented yet).<BR>
 * The graphical representation can be realized by a table using the getTableModel() method.
 * @author Jejkal
 */
public class MarkerManager {

    private static Logger logger = Logger.getLogger("MarkerManager");
    private static MarkerManager SINGLETON = null;
    private Hashtable<String, MarkerSet> markers = null;
    private List<MarkerManagerListener> mManagerListeners = null;

    public static synchronized MarkerManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MarkerManager();
        }
        return SINGLETON;
    }

    /**Internal constructor*/
    MarkerManager() {
        mManagerListeners = new LinkedList<MarkerManagerListener>();
        markers = new Hashtable<String, MarkerSet>();
        markers.put("default", new MarkerSet("default"));
    }

    public synchronized void addMarkerManagerListener(MarkerManagerListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mManagerListeners.contains(pListener)) {
            mManagerListeners.add(pListener);
        }
    }

    public synchronized void removeMarkerManagerListener(MarkerManagerListener pListener) {
        mManagerListeners.remove(pListener);
    }

    /**Load markers from file
     */
    public void loadMarkersFromFile(String pFile) {
        markers.clear();
        markers.put("default", new MarkerSet("default"));
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        File markerFile = new File(pFile);
        if (markerFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading markers from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(markerFile);
                for (Element setElement : (List<Element>) JaxenUtils.getNodes(d, "//markerSets/markerSet")) {
                    MarkerSet set = MarkerSet.fromXml(setElement);
                    if (set != null) {
                        markers.put(set.getSetName(), set);
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
    }

    public boolean importMarkers(File pFile, String pExtension) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }
        logger.debug("Importing markers");
        try {
            boolean overwriteMarkers = Boolean.parseBoolean(GlobalOptions.getProperty("import.replace.markers"));
            Document d = JaxenUtils.getDocument(pFile);
            for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//markerSets/markerSet")) {
                try {
                    MarkerSet m = MarkerSet.fromXml(e);
                    m.setSetName(m.getSetName() + pExtension);
		    System.out.println(m.getSetName());
                    MarkerSet exist = markers.get(m.getSetName());
                    //replace existing markers
		    System.out.println(exist);
		    System.out.println(overwriteMarkers);
                    if (exist == null || overwriteMarkers) {
                        logger.debug("Adding/Replacing existing marker set '" + m.getSetName() + "'");
                        markers.put(m.getSetName(), m);
                    }
                } catch (Exception inner) {
                    //ignored, marker invalid
                    }
            }
            logger.debug("Markers imported successfully");
            DSWorkbenchMarkerFrame.getSingleton().fireMarkersChangedEvent();
            MinimapPanel.getSingleton().redraw();
            return true;
        } catch (Exception e) {
            logger.error("Failed to import markers", e);
            DSWorkbenchMarkerFrame.getSingleton().fireMarkersChangedEvent();
            MinimapPanel.getSingleton().redraw();
            return false;
        }
    }

    public String getExportData(String[] pSets) {
        logger.debug("Generating marker export data");

        String result = "<markerSets>\n";
        for (String set : pSets) {
            MarkerSet m = markers.get(set);
            try {
                String xml = m.toXml();
                if (xml != null) {
                    result += xml + "\n";
                }
            } catch (Exception e) {
            }
        }
        result += "</markerSets>\n";
        logger.debug("Export data generated successfully");
        return result;
    }

    /**Load markers from database (not implemented yet)
     */
    public void loadMarkersFromDatabase(String pUrl) {
        logger.info("Not implemented yet");
    }

    /**Load markers to file
     */
    public void saveMarkersToFile(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Writing markers to '" + pFile + "'");
        }
        try {

            StringBuffer b = new StringBuffer();
            b.append("<markerSets>\n");
            Enumeration<String> setKeys = markers.keys();
            while (setKeys.hasMoreElements()) {
                MarkerSet set = markers.get(setKeys.nextElement());
                b.append(set.toXml());
            }
            b.append("</markerSets>");
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

    /**Save markers to database (not implemented yet)
     */
    public void saveMarkersToDatabase() {
        logger.info("Not implemented yet");
    }

    public MarkerSet removeSet(String pSet) {
        return markers.remove(pSet);
    }

    public MarkerSet getMarkerSet(String pName) {
        return markers.get(pName);
    }

    public String[] getMarkerSets() {
        Enumeration<String> keys = markers.keys();
        List<String> names = new LinkedList<String>();
        while (keys.hasMoreElements()) {
            names.add(keys.nextElement());
        }
        return names.toArray(new String[]{});
    }

    public Marker[] getMarkerSetMarkers(String pSet) {
        if (!markers.containsKey(pSet)) {
            return new Marker[]{};
        }
        return markers.get(pSet).getMarkers().toArray(new Marker[]{});
    }

    public Marker[] getMarkerSet() {
        MarkerSet set = markers.get(MarkerTableModel.getSingleton().getActiveSet());
        if (set == null) {
            MarkerTableModel.getSingleton().setActiveSet("default");
            set = markers.get("default");
        }
        return set.getMarkers().toArray(new Marker[]{});
    }

    /**Get all markers as array to avaid concurrent modifications*/
    public Marker[] getMarkers() {
        return getMarkerSet();
    }

    public void addMarkerSet(String pName) {
        MarkerSet set = new MarkerSet(pName);
        markers.put(pName, set);
    }

    public void addMarkerSet(MarkerSet pSet) {
        markers.put(pSet.getSetName(), pSet);
    }

    /**Add an ally marker*/
    public void addMarker(Ally pAlly, Color pColor) {
        if (pAlly == null) {
            return;
        }

        addMarkerInternal(Marker.ALLY_MARKER_TYPE, pAlly.getId(), pColor);
        fireMarkerChangedEvents();
    }

    /**Add a tribe marker*/
    public void addMarker(Tribe pTribe, Color pColor) {
        if (pTribe == null) {
            return;
        }
        addMarkerInternal(Marker.TRIBE_MARKER_TYPE, pTribe.getId(), pColor);
        fireMarkerChangedEvents();
    }

    /**Add a marker by value (for internal use only)*/
    private void addMarkerInternal(int pType, int pId, Color pColor) {
        Marker m = null;
        switch (pType) {
            case Marker.TRIBE_MARKER_TYPE: {
                m = getMarker(DataHolder.getSingleton().getTribes().get(pId));
                break;
            }
            default: {
                m = getMarker(DataHolder.getSingleton().getAllies().get(pId));
            }
        }

        //getMarkerByValue(pType, pId);
        if (m != null) {
            m.setMarkerColor(pColor);
        } else {
            m = new Marker();
            m.setMarkerType((pType == 0) ? Marker.TRIBE_MARKER_TYPE : Marker.ALLY_MARKER_TYPE);
            m.setMarkerID(pId);
            m.setMarkerColor(pColor);
            List<Marker> set = markers.get(MarkerTableModel.getSingleton().getActiveSet()).getMarkers();
            set.add(m);
        }
    }

    /**And both, a tribe marker and an ally marker*/
    public void addMarker(Tribe pTribe, Color pTribeColor, Ally pAlly, Color pAllyColor) {
        addMarkerInternal(Marker.TRIBE_MARKER_TYPE, pTribe.getId(), pTribeColor);
        addMarkerInternal(Marker.ALLY_MARKER_TYPE, pAlly.getId(), pAllyColor);
        fireMarkerChangedEvents();
    }

    public void removeMarkers(Marker[] pValues) {
        removeMarkersInternal(pValues);
    }

    /**Remove a marker by its value*/
    public void removeMarker(Marker pValue) {
        if (pValue == null) {
            return;
        }
        removeMarkerInternal(pValue);
    }

    /**Remove an ally marker (internally removeMarker() is used)*/
    public void removeMarker(Ally pAlly) {
        if (pAlly == null) {
            return;
        }
        removeMarkerInternal(getMarker(pAlly));
    }

    /**Remove a tribe marker (internally removeMarker() is used)*/
    public void removeMarker(Tribe pTribe) {
        if (pTribe == null) {
            return;
        }
        removeMarkerInternal(getMarker(pTribe));
    }

    /**Remove a marker by its value (for internal use only)*/
    private void removeMarkerInternal(Marker pValue) {
        if (pValue == null) {
            return;
        }
        markers.get(MarkerTableModel.getSingleton().getActiveSet()).getMarkers().remove(pValue);
        fireMarkerChangedEvents();
    }

    /**Remove a marker by its value (for internal use only)*/
    private void removeMarkersInternal(Marker[] pValues) {
        if (pValues == null) {
            return;
        }
        List<Marker> set = markers.get(MarkerTableModel.getSingleton().getActiveSet()).getMarkers();
        for (Marker v : pValues) {
            set.remove(v);
        }
        fireMarkerChangedEvents();
    }

    public void markerUpdatedExternally() {
        fireMarkerChangedEvents();
    }

    public Marker getMarker(Tribe pTribe) {
        if (pTribe == null) {
            return null;
        }

        Marker[] set = getMarkerSet();

        for (Marker m : set) {
            if ((m.getMarkerType() == Marker.TRIBE_MARKER_TYPE) && (m.getMarkerID() == pTribe.getId())) {
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
        Marker[] set = getMarkerSet();

        for (Marker m : set) {
            if ((m.getMarkerType() == Marker.ALLY_MARKER_TYPE) && (m.getMarkerID() == pAlly.getId())) {
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
        
        Marker[] set = getMarkerSet();

        for (Marker m : set) {
            if ((m.getMarkerType() == Marker.TRIBE_MARKER_TYPE) && (m.getMarkerID() == tribe.getId())) {
                return m;
            }
            if (ally != null && (m.getMarkerType() == Marker.ALLY_MARKER_TYPE) && (m.getMarkerID() == ally.getId())) {
                return m;
            }
        }
        //no marker found
        return null;
    }

    /**Get markers by their type (Tribe = 0, Ally = 1)*/
    public Marker[] getMarkersByType(int pType) {
        List<Marker> markList = new LinkedList<Marker>();
        Marker[] set = getMarkerSet();
        for (Marker m : set) {
            if (m.getMarkerType() == pType) {
                markList.add(m);
            }
        }
        return markList.toArray(new Marker[]{});
    }

    /**Notify all MarkerManagerListeners that the marker data has changed*/
    private void fireMarkerChangedEvents() {
        MarkerManagerListener[] listeners = mManagerListeners.toArray(new MarkerManagerListener[]{});
        for (MarkerManagerListener listener : listeners) {
            listener.fireMarkersChangedEvent();
        }
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.MARKER_LAYER);
    }
}
