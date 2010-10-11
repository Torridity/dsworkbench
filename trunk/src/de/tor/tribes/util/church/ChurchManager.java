/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.church;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Church;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.MinimapPanel;
import de.tor.tribes.ui.renderer.MapRenderer;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.mark.MarkerManager;
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

/**
 * @author Charon
 */
public class ChurchManager {

    private static Logger logger = Logger.getLogger("ChurchManager");
    private static ChurchManager SINGLETON = null;
    private List<Church> churches = null;
    private DefaultTableModel model = null;
    private List<ChurchManagerListener> mManagerListeners = null;

    public static synchronized ChurchManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ChurchManager();
        }
        return SINGLETON;
    }

    ChurchManager() {
        churches = new LinkedList<Church>();
        mManagerListeners = new LinkedList<ChurchManagerListener>();
        model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf", "Reichweite", "Farbe"
                }) {

            Class[] types = new Class[]{
                String.class, Village.class, Integer.class, Color.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false, false
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
        churches.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        File churchFile = new File(pFile);
        if (churchFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading churches from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(churchFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//churches/church")) {
                    try {
                        Church c = new Church(e);
                        churches.add(c);
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
                    Church c = new Church(e);
                    churches.add(c);
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
        for (Church c : churches) {
            result += c.toXml() + "\n";
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
            StringBuffer b = new StringBuffer();

            b.append("<churches>\n");
            for (Church c : churches) {
                b.append(c.toXml() + "\n");
            }
            b.append("</churches>");
            //write data to file
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
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

    public Church getChurch(Village v) {
        if (v == null) {
            return null;
        }
        Church[] churchesArray = churches.toArray(new Church[]{});
        for (Church c : churchesArray) {
            if (c.getVillageId() == v.getId()) {
                return c;
            }
        }
        return null;
    }

    public List<Village> getChurchVillages() {
        List<Village> villages = new LinkedList<Village>();
        Church[] churchesArray = churches.toArray(new Church[]{});
        for (Church c : churchesArray) {
            villages.add(DataHolder.getSingleton().getVillagesById().get(c.getVillageId()));
        }
        return villages;
    }

    public void addChurch(Village pVillage, int pRange) {
        if (pVillage != null) {
            Church c = new Church();
            c.setVillageId(pVillage.getId());
            c.setRange(pRange);
            churches.add(c);
        }
        fireChurchesChangedEvents();
    }

    public void removeChurch(Village pVillage) {
        if (pVillage != null) {
            Church[] churchesArray = churches.toArray(new Church[]{});
            for (Church c : churchesArray) {
                if (c.getVillageId() == pVillage.getId()) {
                    churches.remove(c);
                    break;
                }
            }
            fireChurchesChangedEvents();
        }
    }

    public void removeChurches(Village[] pVillages) {
        if (pVillages != null) {
            Church[] churchesArray = churches.toArray(new Church[]{});
            for (Village v : pVillages) {
                if (v != null) {
                    //village is valid
                    for (Church c : churchesArray) {
                        //iterate through all churches
                        if (c.getVillageId() == v.getId()) {
                            //remove current church
                            churches.remove(c);
                        }
                    }
                }
            }
            fireChurchesChangedEvents();
        }
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

        Church[] churchesArray = churches.toArray(new Church[]{});
        for (Church c : churchesArray) {
            Integer range = c.getRange();
            Village v = DataHolder.getSingleton().getVillagesById().get(c.getVillageId());
            Color col = v.getTribe().getMarkerColor();
            String tribe = (v.getTribe() == Barbarians.getSingleton()) ? "Barbaren" : v.getTribe().getName();
            model.addRow(new Object[]{tribe, v, range, col});
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
