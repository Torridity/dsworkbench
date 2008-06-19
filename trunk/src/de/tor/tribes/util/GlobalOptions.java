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
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**Global settings used by almost all components. e.g. WorldData or UI specific objects
 * @author Charon
 */
public class GlobalOptions {

    private static Logger logger = Logger.getLogger(GlobalOptions.class);
    //mappanel default
    public final static int CURSOR_DEFAULT = 0;
    public final static int CURSOR_MARK = 1;
    public final static int CURSOR_MEASURE = 2;
    //mappanel attack
    public final static int CURSOR_ATTACK_RAM = 3;
    public final static int CURSOR_ATTACK_AXE = 4;
    public final static int CURSOR_ATTACK_SNOB = 5;
    public final static int CURSOR_ATTACK_SPY = 6;
    public final static int CURSOR_ATTACK_SWORD = 7;
    public final static int CURSOR_ATTACK_LIGHT = 8;
    public final static int CURSOR_ATTACK_HEAVY = 9;
    //minimap
    public final static int CURSOR_MOVE = 10;
    public final static int CURSOR_ZOOM = 11;
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
    private static List<Cursor> CURSORS = null;

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
        logger.debug("Loading properties");
        loadProperties();
        logger.debug("Loading cursors");
        loadCursors();
        logger.debug("Loading graphic pack");
        loadSkin();
        logger.debug("Loading markers");
        loadMarkers();
        logger.debug("Loading world.dat");
        loadDecoration();
    }

    public static String[] getAvailableSkins() {
        return new File("graphics/skins").list();
    }

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

    private static void loadCursors() throws Exception {
        CURSORS = new LinkedList<Cursor>();
        try {
            //default map panel cursors 
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/default.png"), new Point(0, 0), "default"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/mark.png"), new Point(0, 0), "mark"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/measure.png"), new Point(0, 0), "measure"));
            //map panel cursors for attack purposes
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_ram.png"), new Point(0, 0), "attack_ram"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_axe.png"), new Point(0, 0), "attack_axe"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_snob.png"), new Point(0, 0), "attack_snob"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_spy.png"), new Point(0, 0), "attack_spy"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_sword.png"), new Point(0, 0), "attack_sword"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_light.png"), new Point(0, 0), "attack_light"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_heavy.png"), new Point(0, 0), "attack_heavy"));
            //minimap cursors
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/move.png"), new Point(0, 0), "move"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/zoom.png"), new Point(0, 0), "zoom"));

        } catch (Throwable t) {
            logger.error("Failed to load cursor images", t);
            throw new Exception("Cursor konnten nicht geladen werden");
        }
    }

    public static Cursor getCursor(int pID) {
        return CURSORS.get(pID);
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
        String path = mDataHolder.getDataDirectory() + "/markers.bin";
        if (new File(path).exists()) {
            try {
                ObjectInputStream oin = new ObjectInputStream(new FileInputStream(path));
                mMarkers = (Hashtable<String, Color>) oin.readObject();
                oin.close();
            } catch (Exception e) {
                logger.error("Failed to read markers");
            }
        } else {
            logger.info("No markers available for selected server");
        }
    }

    public static void storeMarkers() {
        String path = mDataHolder.getDataDirectory() + "/markers.bin";
        try {
            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(path));
            oout.writeObject(mMarkers);
            oout.close();
        } catch (Exception e) {
            logger.error("Failed to store markers");
        }
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
