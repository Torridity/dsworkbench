/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Dimension;
import java.io.File;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import org.jdom.Document;

/**
 *
 * @author Charon
 */
public class ServerSettings {

    private static Logger logger = Logger.getLogger("ServerSettings");
    private String SERVER_ID = "de26";
    private int COORD = 2;
    private Dimension mapSize = null;
    private boolean MAP_NEW = true;
    private int BONUS_NEW = 0;
    private int SNOB_RANGE = 70;
    private boolean church = false;
    private boolean millisArrival = true;
    private double speed = 1.0;
    private static ServerSettings SINGLETON = null;

    public static synchronized ServerSettings getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ServerSettings();
        }
        return SINGLETON;
    }

    public boolean loadSettings(String pServerID) {
        try {
            logger.debug("Loading server settings");
            setServerID(pServerID);
            String serverPath = Constants.SERVER_DIR + "/" + SERVER_ID + "/settings.xml";
            logger.debug("Parse server settings from '" + serverPath + "'");
            Document d = JaxenUtils.getDocument(new File(serverPath));
            logger.debug(" - reading map system");
            try {
                setCoordType(Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/map_size")));
            } catch (Exception inner) {
                setCoordType(1000);
            }
            logger.debug(" - reading map type");
            try {
                setNewMap((Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/map_new")) == 1));
            } catch (Exception inner) {
                setNewMap(true);
            }
            logger.debug(" - reading bonus type");
            try {
                setNewBonus(Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/bonus_new")));
            } catch (Exception inner) {
                setNewBonus(0);
            }
            logger.debug(" - reading snob distance");
            try {
                setSnobRange(Integer.parseInt(JaxenUtils.getNodeValue(d, "//snob/max_dist")));
            } catch (Exception inner) {
                setSnobRange(70);
            }
            logger.debug(" - reading church setting");
            try {
                setChurch(Integer.parseInt(JaxenUtils.getNodeValue(d, "//game/church")) == 1);
            } catch (Exception inner) {
                setChurch(false);
            }
            logger.debug(" - reading millis setting");
            try {
                setMillisArrival(Integer.parseInt(JaxenUtils.getNodeValue(d, "//misc/millis_arrival")) == 1);
            } catch (Exception inner) {
                //empty or invalid value...use no millis
                setMillisArrival(false);
            }
            logger.debug(" - reading server speed");
            try {
                setSpeed(Double.parseDouble(JaxenUtils.getNodeValue(d, "//speed")));
            } catch (Exception inner) {
                setSpeed(1.0);
            }

        } catch (Exception e) {
            logger.error("Failed to load server settings", e);
            return false;
        }
        logger.debug("Successfully read settings for server '" + SERVER_ID + "'");
        return true;
    }

    public void setServerID(String pServerID) {
        SERVER_ID = pServerID;
    }

    public String getServerID() {
        return SERVER_ID;
    }

    public void setCoordType(int pMapSize) {
        if (pMapSize == 1000) {
            COORD = 2;
        } else if (pMapSize == 500) {
            COORD = 1;
        } else {
            throw new IllegalArgumentException("Invalid map size (" + pMapSize + "). Falling back to 1000x1000.");
        }
        switch (COORD) {
            case 1: {
                mapSize = new Dimension(pMapSize, pMapSize);
                break;
            }
            default: {
                mapSize = new Dimension(pMapSize, pMapSize);
            }
        }
    }

    public int getCoordType() {
        return COORD;
    }

    public Dimension getMapDimension() {
        return mapSize;
    }

    public void setNewMap(boolean pNewMap) {
        MAP_NEW = pNewMap;
    }

    public boolean isNewMap() {
        return MAP_NEW;
    }

    public void setNewBonus(int pNewBonus) {
        BONUS_NEW = pNewBonus;
    }

    public int getNewBonus() {
        return BONUS_NEW;
    }

    public void setSnobRange(int pSnobRange) {
        SNOB_RANGE = pSnobRange;
    }

    public int getSnobRange() {
        return SNOB_RANGE;
    }

    public void setChurch(boolean v) {
        church = v;
    }

    public boolean isChurch() {
        return church;
    }

    public void setMillisArrival(boolean v) {
        millisArrival = v;
    }

    public boolean isMillisArrival() {
        return millisArrival;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }
}
