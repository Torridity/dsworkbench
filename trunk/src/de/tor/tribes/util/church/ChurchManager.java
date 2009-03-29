/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.church;

import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.MinimapPanel;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
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

    public static final int RANGE1 = 4;
    public static final int RANGE2 = 6;
    public static final int RANGE3 = 8;
    private static Logger logger = Logger.getLogger("ChurchManager");
    private static ChurchManager SINGLETON = null;
    private Hashtable<Integer, Integer> lChurches = null;
  private DefaultTableModel model = null;

    public static synchronized ChurchManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ChurchManager();
        }
        return SINGLETON;
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

            //@TODO UPDATE CHURCH VIEW
            MinimapPanel.getSingleton().redraw();
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

    public int getChurchLevel(Village v) {
        Integer level = lChurches.get(v.getId());
        if (level == null) {
            return 0;
        }

        return level;
    }

     /**Get the table model which contains all markers*/
    public DefaultTableModel getTableModel() {

        //remove former rows
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        Marker[] markers = getMarkers();
        if (markers.length > 0) {
            for (int i = 0; i < markers.length; i++) {
                model.addRow(new Object[]{markers[i].getView(), markers[i].getMarkerColor()});
            }
        }
        return model;
    }
}
