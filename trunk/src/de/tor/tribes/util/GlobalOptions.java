/*
 * GlobalOptions.java
 *
 * Created on 29.09.2007, 16:29:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**Global settings used by almost all components. e.g. WorldData or UI specific objects
 * @author Charon
 */
public class GlobalOptions {

    private static Logger logger = Logger.getLogger(GlobalOptions.class);
    public final static double VERSION = 0.9;
    private static boolean INITIALIZED = false;
    /**Active skin used by the MapPanel*/
    private static Skin mSkin;
    /**DataHolder which holds and manages the WorldData*/
    private static DataHolder mDataHolder = null;
    private static List<Marker> mMarkers = null;
    private static WorldDecorationHolder mDecorationHolder = null;
    private static String SELECTED_SERVER = "de26";
    private static Properties GLOBAL_PROPERTIES = null;
    private static List<Attack> mAttacks = null;
    private static Hashtable<Village, List<String>> mTags = null;
    //flag which is set if the user is logged in with hin account name
    private static String loggedInAs = null;
    private static boolean isOfflineMode = false;

    /**Init all managed objects
     * @param pDownloadData TRUE=download the WorldData from the tribes server
     * @throws Exception If an Error occurs while initializing the objects
     */
    public static void initialize() throws Exception {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;
        logger.debug("Loading properties");
        loadProperties();
        logger.debug("Loading cursors");
        ImageManager.loadCursors();
        logger.debug("Loading unit icons");
        ImageManager.loadUnitIcons();
        logger.debug("Loading graphic pack");
        loadSkin();
        logger.debug("Loading world.dat");
        WorldDecorationHolder.initialize();
        setSelectedServer(getProperty("default.server"));
        UIManager.put("OptionPane.background", Constants.DS_BACK);
        UIManager.put("Panel.background", Constants.DS_BACK);
        UIManager.put("Button.background", Constants.DS_BACK_LIGHT);
    }

    public static void addDataHolderListener(DataHolderListener pListener) {
        if (mDataHolder == null) {
            mDataHolder = new DataHolder();
        }
        mDataHolder.addListener(pListener);
    }

    /**Tells if a network connection is established or not*/
    public static boolean isOfflineMode() {
        return isOfflineMode;
    }

    /**Set the network status*/
    public static void setOfflineMode(boolean pValue) {
        isOfflineMode = pValue;
    }

    /**Get the list of available skins*/
    public static String[] getAvailableSkins() {
        return new File("graphics/skins").list();
    }

    /**Load the global properties*/
    private static void loadProperties() throws Exception {
        GLOBAL_PROPERTIES = new Properties();
        if (new File("global.properties").exists()) {
            logger.debug("Loading existing properties file");
            GLOBAL_PROPERTIES.load(new FileInputStream("global.properties"));
        } else {
            logger.debug("Creating empty properties file");
            saveProperties();
        }
    }

    /**Store the global properties*/
    public static void saveProperties() {
        try {
            GLOBAL_PROPERTIES.store(new FileOutputStream("global.properties"), "Automatically generated. Please do not modify!");
        } catch (Exception e) {
        }
    }

    /**Add a property*/
    public static void addProperty(String pKey, String pValue) {
        GLOBAL_PROPERTIES.put(pKey, pValue);
    }

    /**Get the value of a property*/
    public static String getProperty(String pKey) {
        return GLOBAL_PROPERTIES.getProperty(pKey);
    }

    /**Load the default skin
     * @throws Exception If there was an error while loading the default skin
     */
    public static void loadSkin() throws Exception {
        mSkin = new Skin(GLOBAL_PROPERTIES.getProperty("default.skin"));
    }

    /**Load the WorldData either from the server or from the local harddisk
     * @param pDownload TRUE = download the data from the server
     * @throws Exception If an exception occurs while loading the data
     */
    public static void loadData(boolean pDownload) throws Exception {
        if (mDataHolder == null) {
            mDataHolder = new DataHolder();
            mDataHolder.initialize();
            if (!mDataHolder.serverSupported()) {
                mDataHolder.fireDataLoadedEvents();
                throw new Exception("Daten konnten nicht geladen werden");
            } else if (!mDataHolder.loadData(pDownload)) {
                throw new Exception("Daten konnten nicht geladen werden");
            }
        } else {
            if (!mDataHolder.loadData(pDownload)) {
                if (!mDataHolder.serverSupported()) {
                    mDataHolder.fireDataLoadedEvents();
                    throw new Exception("Failed to validate server settings");
                } else {
                    logger.error("Failed to obtain data from server. Loading local backup");
                    if (pDownload) {
                        mDataHolder.fireDataHolderEvents("Download fehlgeschlagen. Suche nach lokaler Kopie.");
                    } else {
                        mDataHolder.fireDataHolderEvents("Lokale Daten unvollständig. Starte Korrekturversuch.");
                    }
                    if (!mDataHolder.loadData(false)) {
                        logger.error("Failed to load server data from local backup");
                        mDataHolder.fireDataHolderEvents("Lokale Kopie fehlerhaft oder nicht vorhanden.");
                        mDataHolder.fireDataLoadedEvents();
                        throw new Exception("Daten konnten nicht geladen werden");
                    }
                }
            }
        }
    }

    /**Load user data (attacks, markers...)*/
    public static void loadUserData() {
        if (getSelectedServer() != null) {
            logger.debug("Loading markers");
            MarkerManager.getSingleton().loadMarkersFromFile(DataHolder.getSingleton().getDataDirectory() + "/markers.xml");
            logger.debug("Loading attacks");
            loadAttacks();
            logger.debug("Loading tags");
            TagManager.getSingleton().loadTagsFromFile(DataHolder.getSingleton().getDataDirectory() + "/tags.xml");
        }
    }

    /**Load the PlayerMarkers from the harddisk
     */
 /*   private static void loadMarkers() {
        mMarkers = new LinkedList<Marker>();
        String path = mDataHolder.getDataDirectory() + "/markers.xml";
        if (new File(path).exists()) {
            try {
                Document d = JaxenUtils.getDocument(new File(path));
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//markers/marker")) {
                    try {
                        mMarkers.add(new Marker(e));
                    } catch (Exception inner) {
                        //ignored, marker invalid
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to read markers");
                e.printStackTrace();
            }
        } else {
            logger.info("No markers available for selected server");
        }
    }
*/
    /**Get the list of markers
     * @return Hashtable<String, Color> List of MapMarkers
     */
/*    public static List<Marker> getMarkers() {
        return mMarkers;
    }
*/
 /*   public static Marker getMarkerByValue(String pValue) {
        for (Marker m : mMarkers) {
            if (m.getMarkerValue().equals(pValue)) {
                return m;
            }
        }
        return null;
    }
*/
/*    public static void storeMarkers() {
        String path = mDataHolder.getDataDirectory() + "/markers.xml";
        try {
            FileWriter w = new FileWriter(path);
            w.write("<markers>\n");
            for (Marker m : mMarkers) {
                String xml = m.toXml();
                if (xml != null) {
                    w.write(m.toXml() + "\n");
                }
            }
            w.write("</markers>");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store markers");
        }
    }
*/
    private static void loadAttacks() {
        mAttacks = new LinkedList<Attack>();
        String path = mDataHolder.getDataDirectory() + "/attacks.xml";

        if (new File(path).exists()) {
            try {
                Document d = JaxenUtils.getDocument(new File(path));
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//attacks/attack")) {
                    mAttacks.add(new Attack(e));
                }
            } catch (Exception e) {
                logger.error("Failed to read attacks", e);
            }
        } else {
            logger.info("No attacks available for selected server");
        }

        for (Attack a : mAttacks) {
            a.setSource(mDataHolder.getVillages()[a.getSource().getX()][a.getSource().getY()]);
            a.setTarget(mDataHolder.getVillages()[a.getTarget().getX()][a.getTarget().getY()]);
        }

    }

    public static synchronized List<Attack> getAttacks() {
        return mAttacks;
    }

    public static void storeAttacks() {
        String path = mDataHolder.getDataDirectory() + "/attacks.xml";
        try {
            FileWriter w = new FileWriter(path);
            w.write("<attacks>\n");
            for (Attack a : mAttacks) {
                w.write(a.toXml() + "\n");
            }
            w.write("</attacks>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store attacks", e);
        }
    }

   
    public static void setLoggedInAs(String pAccount) {
        loggedInAs = pAccount;
    }

    public static String isLoggedInAs() {
        return loggedInAs;
    }

    /**Get the DataHolder
     * @return DataHolder Object which contains the WorldData
     */
    public static DataHolder getDataHolder() {
        return mDataHolder;
    }

    /**Get the skin
     * @return Skin Object which contains the skin
     */
    public static Skin getSkin() {
        return mSkin;
    }

    /**Get the DataHolder
     * @return DataHolder Object which contains the WorldData
     */
    public static WorldDecorationHolder getWorldDecorationHolder() {
        return mDecorationHolder;
    }

    public static String getSelectedServer() {
        return SELECTED_SERVER;
    }

    public static void setSelectedServer(String pServer) {
        if (pServer == null) {
            return;
        }
        if (SELECTED_SERVER != null) {
            if (SELECTED_SERVER.equals(pServer)) {
                return;
            } else {
                logger.info("Setting selected server to " + pServer);
                SELECTED_SERVER = pServer;
            }
        } else {
            logger.info("Setting selected server to " + pServer);
            SELECTED_SERVER = pServer;
        }
    }
}
