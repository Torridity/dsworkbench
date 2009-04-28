/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.church;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.MinimapPanel;
import de.tor.tribes.ui.renderer.MapRenderer;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class ChurchManager {

    public static final int NO_CHURCH = 0;
    public static final int RANGE1 = 4;
    public static final int RANGE2 = 6;
    public static final int RANGE3 = 8;
    private static Logger logger = Logger.getLogger("ChurchManager");
    private static ChurchManager SINGLETON = null;
    private Hashtable<Integer, Integer> lChurches = null;
    private DefaultTableModel model = null;
    private List<ChurchManagerListener> mManagerListeners = null;

    public static synchronized ChurchManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ChurchManager();
        }
        return SINGLETON;
    }

    ChurchManager() {
        lChurches = new Hashtable<Integer, Integer>();
        mManagerListeners = new LinkedList<ChurchManagerListener>();
        model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf", "Reichweite"
                }) {

            Class[] types = new Class[]{
                String.class, Village.class, Integer.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, true
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
    }

    public synchronized void addChurchManagerListener(ChurchManagerListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mManagerListeners.contains(pListener)) {
            mManagerListeners.add(pListener);
        }
    }

    public synchronized void removeChurchManagerListener(ChurchManagerListener pListener) {
        mManagerListeners.remove(pListener);
    }

    /**Load markers from file
     */
    public void loadChurchesFromFile(String pFile) {
        lChurches.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        File churchFile = new File(pFile);
        if (churchFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading markers from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(churchFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//churches/church")) {
                    try {
                        Integer id = Integer.parseInt(e.getChild("village").getText());
                        Integer range = Integer.parseInt(e.getChild("range").getText());
                        lChurches.put(id, range);
                    } catch (Exception inner) {
                        //ignored, marker invalid
                    }
                }
                logger.debug("Churches successfully loaded");
            } catch (Exception e) {
                logger.error("Failed to load churches", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Churches file not found under '" + pFile + "'");
            }
        }
    }

    public boolean importChurches(File pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }
        logger.debug("Importing churches");
        try {
            Document d = JaxenUtils.getDocument(pFile);
            for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//churches/church")) {
                try {
                    Integer id = Integer.parseInt(e.getChild("village").getText());
                    Integer range = Integer.parseInt(e.getChild("range").getText());
                    lChurches.put(id, range);
                } catch (Exception inner) {
                    //ignored, marker invalid
                    }
            }
            logger.debug("Churches imported successfully");

            fireChurchesChangedEvents();
            return true;
        } catch (Exception e) {
            logger.error("Failed to import churches", e);
            DSWorkbenchMarkerFrame.getSingleton().fireMarkersChangedEvent();
            MinimapPanel.getSingleton().redraw();
            return false;
        }
    }

    public String getExportData() {
        logger.debug("Generating churches export data");

        String result = "<churches>\n";
        Enumeration<Integer> ids = lChurches.keys();
        while (ids.hasMoreElements()) {
            Integer id = ids.nextElement();
            Integer range = lChurches.get(id);
            String xml = "<church>\n";
            xml += "<village>" + id + "</village>\n";
            xml += "<range>" + range + "</range>\n";
            xml += "</church>\n";
            result += xml;
        }
        result += "</churches>\n";
        logger.debug("Export data generated successfully");
        return result;
    }

    /**Load markers from database (not implemented yet)
     */
    public void loadChurchesFromDatabase(String pUrl) {
        logger.info("Not implemented yet");
    }

    /**Load markers to file
     */
    public void saveChurchesToFile(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Writing churches to '" + pFile + "'");
        }
        try {
            FileWriter w = new FileWriter(pFile);
            w.write("<churches>\n");
            Enumeration<Integer> ids = lChurches.keys();
            while (ids.hasMoreElements()) {
                Integer id = ids.nextElement();
                Integer range = lChurches.get(id);
                String xml = "<church>\n";
                xml += "<village>" + id + "</village>\n";
                xml += "<range>" + range + "</range>\n";
                xml += "</church>\n";
                w.write(xml);
            }
            w.write("</churches>");
            w.flush();
            w.close();
            logger.debug("Churches successfully saved");
        } catch (Exception e) {
            if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
                //this should only happen at the first start
                logger.info("Ignoring error, server directory does not exists yet");
            } else {
                logger.error("Failed to save churches", e);
            }
        }
    }

    /**Save markers to database (not implemented yet)
     */
    public void saveChurchesToDatabase() {
        logger.info("Not implemented yet");
    }

    public int getChurchRange(Village v) {
        if (v == null) {
            return NO_CHURCH;
        }
        Integer range = lChurches.get(v.getId());
        if (range == null) {
            return NO_CHURCH;
        }

        return range;
    }

    public List<Village> getChurchVillages() {
        List<Village> villages = new LinkedList<Village>();
        Enumeration<Integer> ids = lChurches.keys();
        while (ids.hasMoreElements()) {
            villages.add(DataHolder.getSingleton().getVillagesById().get(ids.nextElement()));
        }
        return villages;
    }

    public void addChurch(Village pVillage, int pRange) {
        if (pVillage != null) {
            lChurches.put(pVillage.getId(), pRange);
        }
        fireChurchesChangedEvents();
    }

    public void removeChurch(Village pVillage) {
        if (pVillage != null) {
            lChurches.remove(pVillage.getId());
        }
        fireChurchesChangedEvents();
    }

    public void removeChurches(Village[] pVillages) {
        if (pVillages != null) {
            for (Village v : pVillages) {
                if (v != null) {
                    lChurches.remove(v.getId());
                }
            }
        }
        fireChurchesChangedEvents();
    }

    public void churchesUpdatedExternally() {
        fireChurchesChangedEvents();
    }

    /**Get the table model which contains all markers*/
    public DefaultTableModel getTableModel() {

        //remove former rows
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        Enumeration<Integer> ids = lChurches.keys();
        while (ids.hasMoreElements()) {
            Integer id = ids.nextElement();
            Integer range = lChurches.get(id);
            Village v = DataHolder.getSingleton().getVillagesById().get(id);
            String tribe = (v.getTribe() == null) ? "Barbaren" : v.getTribe().getName();
            model.addRow(new Object[]{tribe, v, range});
        }
        return model;
    }

    /**Notify all MarkerManagerListeners that the marker data has changed*/
    private void fireChurchesChangedEvents() {
        ChurchManagerListener[] listeners = mManagerListeners.toArray(new ChurchManagerListener[]{});
        for (ChurchManagerListener listener : listeners) {
            listener.fireChurchesChangedEvent();
        }
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.MARKER_LAYER);
    }
}
