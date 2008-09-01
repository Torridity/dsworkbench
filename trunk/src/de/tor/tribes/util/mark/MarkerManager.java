/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.mark;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.ui.MarkerCell;
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
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

    private static Logger logger = Logger.getLogger(MarkerManager.class);
    private static MarkerManager SINGLETON = null;
    private List<Marker> lMarkers = null;
    private List<MarkerManagerListener> mManagerListeners = null;

    public static synchronized MarkerManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MarkerManager();
        }
        return SINGLETON;
    }

    /**Internal constructor*/
    MarkerManager() {
        lMarkers = new LinkedList<Marker>();
        mManagerListeners = new LinkedList<MarkerManagerListener>();
    }

    public synchronized void addMarkerManagerListener(MarkerManagerListener pListener) {
        mManagerListeners.add(pListener);
    }

    public synchronized void removeMarkerManagerListener(MarkerManagerListener pListener) {
        mManagerListeners.remove(pListener);
    }

    /**Load markers from file
     */
    public void loadMarkersFromFile(String pFile) {
        lMarkers.clear();
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
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//markers/marker")) {
                    try {
                        lMarkers.add(new Marker(e));
                    } catch (Exception inner) {
                        //ignored, marker invalid
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
            FileWriter w = new FileWriter(pFile);
            w.write("<markers>\n");
            Marker[] markers = getMarkers();
            for (Marker m : markers) {
                String xml = m.toXml();
                if (xml != null) {
                    w.write(m.toXml() + "\n");
                }
            }
            w.write("</markers>");
            w.flush();
            w.close();
            logger.debug("Markers successfully saved");
        } catch (Exception e) {
            logger.error("Failed to save markers", e);
        }
    }

    /**Save markers to database (not implemented yet)
     */
    public void saveMarkersToDatabase() {
        logger.info("Not implemented yet");
    }

    /**Get all markers as array to avaid concurrent modifications*/
    public Marker[] getMarkers() {
        return lMarkers.toArray(new Marker[]{});
    }

    /**Add an ally marker*/
    public void addMarker(Ally pAlly, Color pColor) {
        if (pAlly == null) {
            return;
        }

        addMarkerInternal(Marker.ALLY_MARKER_TYPE, pAlly.getName(), pColor);
        fireMarkerChangedEvents();
    }

    /**Add a tribe marker*/
    public void addMarker(Tribe pTribe, Color pColor) {
        if (pTribe == null) {
            return;
        }
        addMarkerInternal(Marker.TRIBE_MARKER_TYPE, pTribe.getName(), pColor);
        fireMarkerChangedEvents();
    }

    /**Add a marker by value (for internal use only)*/
    private void addMarkerInternal(int pType, String pValue, Color pColor) {
        Marker m = getMarkerByValue(pValue);
        if (m != null) {
            m.setMarkerColor(pColor);
        } else {
            m = new Marker();
            m.setMarkerType((pType == 0) ? Marker.TRIBE_MARKER_TYPE : Marker.ALLY_MARKER_TYPE);
            m.setMarkerValue(pValue);
            m.setMarkerColor(pColor);
            lMarkers.add(m);
        }

    }

    /**And both, a tribe marker and an ally marker*/
    public void addMarker(Tribe pTribe, Color pTribeColor, Ally pAlly, Color pAllyColor) {
        addMarkerInternal(Marker.TRIBE_MARKER_TYPE, pTribe.getName(), pTribeColor);
        addMarkerInternal(Marker.ALLY_MARKER_TYPE, pAlly.getName(), pAllyColor);
        fireMarkerChangedEvents();
    }

    public void removeMarkers(String[] pValues) {
        removeMarkersInternal(pValues);
    }

    /**Remove a marker by its value*/
    public void removeMarker(String pValue) {
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
        removeMarker(pAlly.getName());
    }

    /**Remove a tribe marker (internally removeMarker() is used)*/
    public void removeMarker(Tribe pTribe) {
        if (pTribe == null) {
            return;
        }
        removeMarker(pTribe.getName());
    }

    /**Remove a marker by its value (for internal use only)*/
    private void removeMarkerInternal(String pValue) {
        if (pValue == null) {
            return;
        }
        Marker toRemove = getMarkerByValue(pValue);
        lMarkers.remove(toRemove);
        fireMarkerChangedEvents();
    }

    /**Remove a marker by its value (for internal use only)*/
    private void removeMarkersInternal(String[] pValues) {
        if (pValues == null) {
            return;
        }
        for (String v : pValues) {
            Marker toRemove = getMarkerByValue(v);
            lMarkers.remove(toRemove);
        }
        fireMarkerChangedEvents();
    }

    public void markerUpdatedExternally() {
        fireMarkerChangedEvents();
    }

    /**Get the table model which contains all markers*/
    public DefaultTableModel getTableModel() {
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Name", "Markierung"
                }) {

            Class[] types = new Class[]{
                MarkerCell.class, Color.class
            };
            boolean[] canEdit = new boolean[]{
                false, true
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
        Marker[] markers = getMarkers();
        if (markers.length > 0) {
            for (int i = 0; i < markers.length; i++) {
                model.addRow(new Object[]{markers[i].getView(), markers[i].getMarkerColor()});
            }
        }
        return model;
    }

    /**Get markers by their value*/
    public Marker getMarkerByValue(String pValue) {
        Marker[] markers = getMarkers();
        for (Marker m : markers) {
            if (m.getMarkerValue().equals(pValue)) {
                return m;
            }
        }
        return null;
    }

    /**Get markers by their type (Tribe = 0, Ally = 1)*/
    public Marker[] getMarkersByType(int pType) {
        List<Marker> markList = new LinkedList<Marker>();
        Marker[] markers = getMarkers();
        for (Marker m : markers) {
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
    }
}
