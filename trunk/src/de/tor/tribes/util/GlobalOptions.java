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
import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.map.FormManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.UIManager;
import org.apache.log4j.Logger;

/**Global settings used by almost all components. e.g. WorldData or UI specific objects
 * @author Charon
 */
public class GlobalOptions {

    private static Logger logger = Logger.getLogger("GlobalSettings");
    private static boolean INITIALIZED = false;
    /**Active skin used by the MapPanel*/
    private static Skin mSkin;
    /**DataHolder which holds and manages the WorldData*/
    private static WorldDecorationHolder mDecorationHolder = null;
    private static String SELECTED_SERVER = "de26";
    private static Properties GLOBAL_PROPERTIES = null;
    //flag for online/offline mode
    private static boolean isOfflineMode = false;
    //used to store last attack time of AttackAddFrame
    private static Date lastArriveTime = null;
    private static HelpBroker mainHelpBroker = null;
    private static CSH.DisplayHelpFromSource csh = null;
    private static final String mainHelpSetName = "DS Workbench Dokumentation.hs";
    private static int iUVID = -1;

    /**Init all managed objects
     * @param pDownloadData TRUE=download the WorldData from the tribes server
     * @throws Exception If an Error occurs while initializing the objects
     */
    public static void initialize() throws Exception {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;
        logger.debug("Loading help system");
        loadHelpSystem();
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

    /**Tells if a network connection is established or not*/
    public static boolean isOfflineMode() {
        return isOfflineMode;
    }

    /**Set the network status*/
    public static void setOfflineMode(boolean pValue) {
        isOfflineMode = pValue;
    }

    public static void setUVMode(int pUVID) {
        iUVID = pUVID;
    }

    public static void unsetUVMode() {
        iUVID = -1;
    }

    public static int getUVID() {
        return iUVID;
    }

    /**Get the list of available skins*/
    public static String[] getAvailableSkins() {
        List<String> skins = new LinkedList<String>();
        skins.add(Skin.MINIMAP_SKIN_ID);
        for (String s : new File("graphics/skins").list()) {
            skins.add(s);
        }
        Collections.sort(skins);
        return skins.toArray(new String[]{});
    }

    private static void loadHelpSystem() {
        if (mainHelpBroker == null) {
            HelpSet mainHelpSet = null;
            try {
                URL hsURL = HelpSet.findHelpSet(null, mainHelpSetName);
                if (hsURL == null) {
                    logger.error("HelpSet " + mainHelpSetName + " not found.");
                } else {
                    logger.debug("HelpSet found");
                    mainHelpSet = new HelpSet(null, hsURL);
                }

            } catch (HelpSetException ee) {
                logger.error("HelpSet " + mainHelpSetName + " could not be opened.", ee);
                return;
            }
            logger.debug("HelpSet opened");

            if (mainHelpSet != null) {
                logger.debug("Creating HelpBroker");
                mainHelpBroker = mainHelpSet.createHelpBroker();
            }

            if (mainHelpBroker != null) {
                logger.debug("Creating DisplayHelpFromSource");
                csh = new CSH.DisplayHelpFromSource(mainHelpBroker);

            }
        }
        logger.debug("HelpSystem initialized");
    }

    public static HelpBroker getHelpBroker() {
        return mainHelpBroker;
    }

    public static CSH.DisplayHelpFromSource getHelpDisplay() {
        return csh;
    }

    /**Load the global properties*/
    private static void loadProperties() throws Exception {
        GLOBAL_PROPERTIES = new Properties();
        if (new File("global.properties").exists()) {
            logger.debug("Loading existing properties file");
            FileInputStream fin = new FileInputStream("global.properties");
            GLOBAL_PROPERTIES.load(fin);
            fin.close();
        } else {
            logger.debug("Creating empty properties file");
            saveProperties();
        }
    }

    /**Store the global properties*/
    public static void saveProperties() {
        try {
            FileOutputStream fout = new FileOutputStream("global.properties");
            GLOBAL_PROPERTIES.store(fout, "Automatically generated. Please do not modify!");
            fout.flush();
            fout.close();
        } catch (Exception e) {
            logger.error("Failed to write properties", e);
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

    /**Load user data (attacks, markers...)*/
    public static void loadUserData() {
        if (getSelectedServer() != null) {

            logger.debug("Loading markers");
            MarkerManager.getSingleton().loadMarkersFromFile(DataHolder.getSingleton().getDataDirectory() + "/markers.xml");
            logger.debug("Loading attacks");
            AttackManager.getSingleton().loadTroopMovementsFromDisk(DataHolder.getSingleton().getDataDirectory() + "/attacks.xml");
            logger.debug("Loading tags");
            TagManager.getSingleton().loadTagsFromFile(DataHolder.getSingleton().getDataDirectory() + "/tags.xml");
            logger.debug("Loading troops");
            TroopsManager.getSingleton().loadTroopsFromFile(DataHolder.getSingleton().getDataDirectory() + "/troops.xml");
            logger.debug("Loading forms");
            FormManager.getSingleton().loadFormsFromFile(DataHolder.getSingleton().getDataDirectory() + "/forms.xml");
            logger.debug("Loading churches");
            ChurchManager.getSingleton().loadChurchesFromFile(DataHolder.getSingleton().getDataDirectory() + "/churches.xml");
            logger.debug("Removing temporary data");
            DataHolder.getSingleton().removeTempData();
        }
    }

    /**Load user data (attacks, markers...)*/
    public static void saveUserData() {
        if (getSelectedServer() != null) {

            logger.debug("Saving markers");
            MarkerManager.getSingleton().saveMarkersToFile(DataHolder.getSingleton().getDataDirectory() + "/markers.xml");
            logger.debug("Saving attacks");
            AttackManager.getSingleton().saveTroopMovementsToDisk(DataHolder.getSingleton().getDataDirectory() + "/attacks.xml");
            logger.debug("Saving tags");
            TagManager.getSingleton().saveTagsToFile(DataHolder.getSingleton().getDataDirectory() + "/tags.xml");
            logger.debug("Saving troops");
            TroopsManager.getSingleton().saveTroopsToFile(DataHolder.getSingleton().getDataDirectory() + "/troops.xml");
            logger.debug("Saving forms");
            FormManager.getSingleton().saveFormsToFile(DataHolder.getSingleton().getDataDirectory() + "/forms.xml");
            logger.debug("Saving churches");
            ChurchManager.getSingleton().saveChurchesToFile(DataHolder.getSingleton().getDataDirectory() + "/churches.xml");
            logger.debug("User data saved");
        }
    }

    public static Skin getSkin() {
        return mSkin;
    }

    /**Get the DecorationHolder
     * @return WorldDecorationHolder Object which contains the WorldData
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

    public static void setLastArriveTime(Date pTime) {
        lastArriveTime = pTime;
    }

    public static Date getLastArriveTime() {
        return lastArriveTime;
    }
}
