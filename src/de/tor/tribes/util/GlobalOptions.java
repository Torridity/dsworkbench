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
import de.tor.tribes.io.WorldDataHolder;
import de.tor.tribes.io.WorldDecorationHolder;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Properties;

/**Global settings used by almost all components. e.g. WorldData or UI specific objects
 * @author Charon
 */
public class GlobalOptions {

    private static boolean INITIALIZED = false;
    /**Active skin used by the MapPanel*/
    private static Skin mSkin;
    /**DataHolder which holds and manages the WorldData*/
    private static DataHolder mDataHolder;
    private static WorldDataHolder mWorldData = null;
    private static Hashtable<String, Color> mMarkers = null;
    private static WorldDecorationHolder mDecorationHolder = null;
    private static String SELECTED_SERVER = "de12";
    private static DataHolderListener mListener;
    private static Properties GLOBAL_PROPERTIES = null;

    /**Init all managed objects
     * @param pDownloadData TRUE=download the WorldData from the tribes server
     * @throws Exception If an Error occurs while initializing the objects
     */
    public static void initialize(boolean pDownloadData, DataHolderListener pListener) throws Exception {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;
        mListener = pListener;
        loadProperties();
        loadSkin();//"/res/skins/symbol"
        loadMarkers();
        loadDecoration();
    }

    public static String[] getAvailableSkins() {
        return new File("graphics/skins").list();
    }

    private static void loadProperties() throws Exception {
        GLOBAL_PROPERTIES = new Properties();
        if (new File("global.properties").exists()) {
            GLOBAL_PROPERTIES.load(new FileInputStream("global.properties"));
        } else {
            saveProperties();
        }
    }

    public static void saveProperties() {
        try {
            GLOBAL_PROPERTIES.store(new FileOutputStream("global.properties"), "Automatically generated. Please do not modify!");
        } catch (Exception e) {
        }
    }

    public static void addProperty(String pKey, String pValue) {
        GLOBAL_PROPERTIES.put(pKey, pValue);
    }

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
        mDataHolder = new DataHolder(mListener);
        if (!mDataHolder.loadData(pDownload)) {
            throw new Exception("Daten konnten nicht geladen werden");
        }
    }

    /**Load the PlayerMarkers from the harddisk
     */
    public static void loadMarkers() {
        mMarkers = new Hashtable<String, Color>();
    }

    /**Load the WorldData from the harddisk
     */
    public static void loadWorldData() throws Exception {
        mWorldData = new WorldDataHolder();
        mWorldData.loadUnits();
    }

    public static void loadDecoration() throws Exception {
        mDecorationHolder = new WorldDecorationHolder();
        mDecorationHolder.loadWorld();
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

    /**Get the list of markers
     * @return Hashtable<String, Color> List of MapMarkers
     */
    public static Hashtable<String, Color> getMarkers() {
        return mMarkers;
    }

    /**Get the WorldData
     * @return WorldDataHolder WorldDataHolder
     */
    public static WorldDataHolder getWorldData() {
        return mWorldData;
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
                SELECTED_SERVER = pServer;
            }
        } else {
            SELECTED_SERVER = pServer;
        }
    }
}
