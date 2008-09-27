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
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import javax.swing.UIManager;
import org.apache.log4j.Logger;

/**Global settings used by almost all components. e.g. WorldData or UI specific objects
 * @author Charon
 */
public class GlobalOptions {

    private static Logger logger = Logger.getLogger(GlobalOptions.class);
    public final static double VERSION = 0.91;
    private static boolean INITIALIZED = false;
    /**Active skin used by the MapPanel*/
    private static Skin mSkin;
    /**DataHolder which holds and manages the WorldData*/
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

        DataHolder.getSingleton().addListener(pListener);
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
            AttackManager.getSingleton().loadAttacksFromFile(DataHolder.getSingleton().getDataDirectory() + "/attacks.xml");
            logger.debug("Loading tags");
            TagManager.getSingleton().loadTagsFromFile(DataHolder.getSingleton().getDataDirectory() + "/tags.xml");
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
    /*   public static DataHolder getDataHolder() {
    return mDataHolder;
    }*/
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
