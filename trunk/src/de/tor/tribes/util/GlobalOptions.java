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
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
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
    //mappanel default
    public final static int CURSOR_DEFAULT = 0;
    public final static int CURSOR_MARK = 1;
    public final static int CURSOR_MEASURE = 2;
    public final static int CURSOR_TAG = 3;
    public final static int CURSOR_ATTACK_INGAME = 4;
    public final static int CURSOR_SEND_RES_INGAME = 5;
    //mappanel attack
    public final static int CURSOR_ATTACK_RAM = 6;
    public final static int CURSOR_ATTACK_AXE = 7;
    public final static int CURSOR_ATTACK_SNOB = 8;
    public final static int CURSOR_ATTACK_SPY = 9;
    public final static int CURSOR_ATTACK_SWORD = 10;
    public final static int CURSOR_ATTACK_LIGHT = 11;
    public final static int CURSOR_ATTACK_HEAVY = 12;
    //unit icons
    public final static int ICON_SPEAR = 0;
    public final static int ICON_SWORD = 1;
    public final static int ICON_AXE = 2;
    public final static int ICON_ARCHER = 3;
    public final static int ICON_SPY = 4;
    public final static int ICON_LKAV = 5;
    public final static int ICON_MARCHER = 6;
    public final static int ICON_HEAVY = 7;
    public final static int ICON_RAM = 8;
    public final static int ICON_CATA = 9;
    public final static int ICON_KNIGHT = 10;
    public final static int ICON_SNOB = 11;
    //auto-update frequence
    public final static int AUTO_UPDATE_NEVER = 0;
    public final static int AUTO_UPDATE_HOURLY = 1;
    public final static int AUTO_UPDATE_2_HOURS = 2;
    public final static int AUTO_UPDATE_4_HOURS = 3;
    public final static int AUTO_UPDATE_12_HOURS = 4;
    public final static int AUTO_UPDATE_DAILY = 5;
    //minimap
    public final static int CURSOR_MOVE = 12;
    public final static int CURSOR_ZOOM = 13;
    private static boolean INITIALIZED = false;
    /**Active skin used by the MapPanel*/
    private static Skin mSkin;
    /**DataHolder which holds and manages the WorldData*/
    private static DataHolder mDataHolder = null;
    private static List<Marker> mMarkers = null;
    private static WorldDecorationHolder mDecorationHolder = null;
    private static String SELECTED_SERVER = "de26";
    private static DataHolderListener mListener;
    private static Properties GLOBAL_PROPERTIES = null;
    private static List<Cursor> CURSORS = null;
    private static List<Attack> mAttacks = null;
    private static Hashtable<Village, List<String>> mTags = null;
    //flag which is set if the user is logged in with hin account name
    private static String loggedInAs = null;
    private static boolean isOfflineMode = false;
    public final static Color DS_BACK = new Color(225, 213, 190);
    public final static Color DS_BACK_LIGHT = new Color(239, 235, 223);
    private final static List<ImageIcon> UNIT_ICONS = new LinkedList<ImageIcon>();

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
        loadCursors();
        logger.debug("Loading unit icons");
        loadUnitIcons();
        logger.debug("Loading graphic pack");
        loadSkin();
        logger.debug("Loading world.dat");
        loadDecoration();
        setSelectedServer(getProperty("default.server"));
        UIManager.put("OptionPane.background", DS_BACK);
        UIManager.put("Panel.background", DS_BACK);
        UIManager.put("Button.background", DS_BACK_LIGHT);
    }

    public static void addDataHolderListener(DataHolderListener pListener) {
        if (mDataHolder == null) {
            mDataHolder = new DataHolder();
        }
        mDataHolder.addListener(pListener);
    }

    public static boolean isOfflineMode() {
        return isOfflineMode;
    }

    public static void setOfflineMode(boolean pValue) {
        isOfflineMode = pValue;
    }

    public static String[] getAvailableSkins() {
        return new File("graphics/skins").list();
    }

    public static void loadUnitIcons() {
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/spear.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/sword.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/axe.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/archer.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/spy.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/light.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/marcher.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/heavy.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/ram.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/cata.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/knight.png"));
        UNIT_ICONS.add(new ImageIcon("./graphics/icons/snob.png"));
    }

    public static ImageIcon getUnitIcon(int pId) {
        return UNIT_ICONS.get(pId);
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
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/tag.png"), new Point(0, 0), "tag"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_ingame.png"), new Point(0, 0), "attack_ingame"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/res_ingame.png"), new Point(0, 0), "res_ingame"));
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
            mDataHolder.initialize();
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
            loadMarkers();
            logger.debug("Loading attacks");
            loadAttacks();
            logger.debug("Loading tags");
            loadTags();
        }
    }

    /**Load the PlayerMarkers from the harddisk
     */
    public static void loadMarkers() {
        mMarkers = new LinkedList<Marker>();
        String path = mDataHolder.getDataDirectory() + "/markers.xml";
        if (new File(path).exists()) {
            try {
                Document d = JaxenUtils.getDocument(new File(path));
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//markers/marker")) {
                    mMarkers.add(new Marker(e));
                }
            } catch (Exception e) {
                logger.error("Failed to read markers");
                e.printStackTrace();
            }
        } else {
            logger.info("No markers available for selected server");
        }
    }

    /**Get the list of markers
     * @return Hashtable<String, Color> List of MapMarkers
     */
    public static List<Marker> getMarkers() {
        return mMarkers;
    }

    public static Marker getMarkerByValue(String pValue) {
        for (Marker m : mMarkers) {
            if (m.getMarkerValue().equals(pValue)) {
                return m;
            }
        }
        return null;
    }

    public static void storeMarkers() {
        String path = mDataHolder.getDataDirectory() + "/markers.xml";
        try {
            FileWriter w = new FileWriter(path);
            w.write("<markers>\n");
            for (Marker m : mMarkers) {
                w.write(m.toXml() + "\n");
            }
            w.write("</markers>");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store markers");
        }
    }

    public static void loadAttacks() {
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

    public static void loadTags() {
        mTags = new Hashtable<Village, List<String>>();
        String path = mDataHolder.getDataDirectory() + "/tags.bin";
        if (new File(path).exists()) {
            try {
                ObjectInputStream oin = new ObjectInputStream(new FileInputStream(path));
                mTags = (Hashtable<Village, List<String>>) oin.readObject();
                oin.close();
            } catch (Exception e) {
                logger.error("Failed to read tags", e);
            }
        } else {
            logger.info("No tags available for selected server");
        }

        Enumeration<Village> keys = mTags.keys();
        while (keys.hasMoreElements()) {
            Village next = keys.nextElement();
            List<String> tags = mTags.remove(next);
            mTags.put(mDataHolder.getVillages()[next.getX()][next.getY()], tags);
        }
    }

    public static List<String> getTags(Village v) {
        return mTags.get(v);
    }

    public static void addTag(Village v, String tag) {
        List<String> tags = mTags.get(v);
        if (tags == null) {
            tags = new LinkedList<String>();
            tags.add(tag);
            mTags.put(v, tags);
        } else {
            tags.add(tag);
        }
    }

    public static void removeTags(Village v) {
        mTags.remove(v);
    }

    public static void storeTags() {
        String path = mDataHolder.getDataDirectory() + "/tags.bin";
        try {
            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(path));
            oout.writeObject(mTags);
            oout.close();
        } catch (Exception e) {
            logger.error("Failed to store tags", e);
        }
    }

    /**Load the world decoration file*/
    public static void loadDecoration() throws Exception {
        mDecorationHolder = new WorldDecorationHolder();
        mDecorationHolder.loadWorld();
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
                SELECTED_SERVER = pServer;
            }
        } else {
            SELECTED_SERVER = pServer;
        }
    }
}
