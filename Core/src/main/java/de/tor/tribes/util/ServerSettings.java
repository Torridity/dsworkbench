/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util;

import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;

/**
 *
 * @author Charon
 * @author extremeCrazyCoder
 */
public class ServerSettings {

    private static Logger logger = Logger.getLogger("ServerSettings");
    private String SERVER_ID = "de26";
    private Rectangle mapSize = null;
    private int BONUS_NEW = 0;
    private int SNOB_RANGE = 70;
    private boolean church = false;
    private boolean watchtower = false;
    private int fakeLimit = 1;
    private boolean millisArrival = true;
    private double speed = 1.0;
    private int resourceConstant = 30;

    private boolean nightBonusActive = true;
    private int nightBonusStartHour = 0;
    private int nightBonusEndHour = 8;
    
    public static final int NO_MORAL = 0;
    public static final int POINTBASED_MORAL = 1;
    public static final int TIMEBASED_MORAL = 2;
    public static final int TIME_LIMITED_POINTBASED_MORAL = 3;
    private int moral = 0;
    
    public static final int NOBLESYSTEM_PACKETS = 0;
    public static final int NOBLESYSTEM_GOLD_COINS = 1;
    private int nobleSystem = 0;
    
    private static ServerSettings SINGLETON = null;
    private List<ServerSettingsListener> listeners = new ArrayList<>();

    public static synchronized ServerSettings getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ServerSettings();
        }
        return SINGLETON;
    }

    public boolean loadSettings(String pServerID) {
        try {
            logger.debug("Loading server settings");
            SERVER_ID = pServerID;
            String serverPath = Constants.SERVER_DIR + "/" + SERVER_ID + "/settings.xml";
            
            logger.debug("Parse server settings from '" + serverPath + "'");
            Document d = JaxenUtils.getDocument(new File(serverPath));
            
            logger.debug(" - reading map system");
            try {
                setMapSize(Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/map_size")));
            } catch (Exception inner) {
                logger.warn("Unable to read map Size", inner);
                setMapSize(1000);
            }
            
            logger.debug(" - reading bonus type");
            try {
                BONUS_NEW = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/bonus_new"));
            } catch (Exception inner) {
                logger.warn("Unable to read bonus type", inner);
                BONUS_NEW = 0;
            }
            
            logger.debug(" - reading snob distance");
            try {
                SNOB_RANGE = Integer.parseInt(JaxenUtils.getNodeValue(d, "//snob/max_dist"));
            } catch (Exception inner) {
                logger.warn("Unable to read snob range", inner);
                SNOB_RANGE = 70;
            }
            
            logger.debug(" - reading church setting");
            try {
                church = Integer.parseInt(JaxenUtils.getNodeValue(d, "//game/church")) == 1;
            } catch (Exception inner) {
                logger.warn("Unable to read church setting", inner);
                church = false;
            }
            
            logger.debug(" - reading watchtower setting");
            try {
                watchtower = Integer.parseInt(JaxenUtils.getNodeValue(d, "//game/watchtower")) == 1;
            } catch (Exception inner) {
                logger.warn("Unable to read watchtower setting", inner);
                watchtower = false;
            }

            logger.debug(" - reading fake limit settings");
            try {
                fakeLimit = Integer.parseInt(JaxenUtils.getNodeValue(d, "//game/fake_limit"));
            } catch (Exception inner) {
                logger.warn("Unable to read fake limit settings", inner);
                fakeLimit = 1;
            }
            
            logger.debug(" - reading millis setting");
            try {
                millisArrival = Integer.parseInt(JaxenUtils.getNodeValue(d, "//misc/millis_arrival")) == 1;
            } catch (Exception inner) {
                try {//new settings is under "commands"
                    millisArrival = Integer.parseInt(JaxenUtils.getNodeValue(d, "//commands/millis_arrival")) == 1;
                } catch (Exception inner2) {
                    logger.warn("Unable to read millis settings (Exception 1/2)", inner);
                    logger.warn("Unable to read millis settings (Exception 2/2)", inner2);
                    //empty or invalid value...use no millis
                    millisArrival = false;
                }
            }

            logger.debug(" - reading server speed");
            try {
                this.speed = Double.parseDouble(JaxenUtils.getNodeValue(d, "//speed"));
            } catch (Exception inner) {
                logger.warn("Unable to read server speed", inner);
                this.speed = 1.0;
            }

            logger.debug(" - reading noble system");
            try {
                this.nobleSystem = Integer.parseInt(JaxenUtils.getNodeValue(d, "//snob/gold"));
            } catch (Exception inner) {
                logger.warn("Unable to read noble system", inner);
                this.nobleSystem = 1;
            }

            logger.debug(" - reading night bonus");
            try {
                this.nightBonusActive = Integer.parseInt(JaxenUtils.getNodeValue(d, "//night/active")) == 1;
            } catch (Exception inner) {
                logger.warn("Unable to read night bonus", inner);
                this.nightBonusActive = true;
            }
            
            logger.debug(" - reading night bonus start hour");
            try {
                this.nightBonusStartHour = Integer.parseInt(JaxenUtils.getNodeValue(d, "//night/start_hour"));
            } catch (Exception inner) {
                logger.warn("Unable to read night bonus start hour", inner);
                this.nightBonusStartHour = 0;
            }
            
            logger.debug(" - reading night bonus end hour");
            try {
                this.nightBonusStartHour = Integer.parseInt(JaxenUtils.getNodeValue(d, "//night/end_hour"));
            } catch (Exception inner) {
                logger.warn("Unable to read night bonus end hour", inner);
                this.nightBonusEndHour = 8;
            }
            
            logger.debug(" - reading moral type");
            try {
                setMoralType(Integer.parseInt(JaxenUtils.getNodeValue(d, "//moral")));
            } catch (Exception inner) {
                logger.warn("Unable to read moral type", inner);
                this.moral = 0;
            }
            
            logger.debug(" - reading resource Production base");
            try {
                setResourceConstant(Integer.parseInt(JaxenUtils.getNodeValue(d, "//game/base_production")));
            } catch (Exception inner) {
                logger.warn("Unable to resource Production base", inner);
                this.moral = 0;
            }
            
        } catch (Exception e) {
            logger.error("Failed to load server settings", e);
            fireServerSettingsChanged();
            return false;
        }
        logger.debug("Successfully read settings for server '" + SERVER_ID + "'");
        fireServerSettingsChanged();
        return true;
    }

    public void setServerID(String pServerID) {
        SERVER_ID = pServerID;
    }

    public String getServerID() {
        return SERVER_ID;
    }
    
    public void setMapSize(int pMapSize) {
        if (pMapSize < 50 || pMapSize > 1000) {
            logger.warn("Invalid map size (" + pMapSize + "). Falling back to 1000x1000");
            pMapSize = 1000;
        }
        pMapSize = (int) Math.floor(pMapSize / 2.0);
        mapSize = new Rectangle(500 - pMapSize, 500 - pMapSize, pMapSize * 2, pMapSize * 2);
    }

    /**
     * @return returns the part of the map where villages can be placed
     */
    public Rectangle getMapDimension() {
        if (mapSize == null) {
            return new Rectangle(0, 0, 1000, 1000);
        }
        return mapSize;
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

    public boolean isWatchtower() {
        return watchtower;
    }
    
    public int getFakeLimitPercent() {
        return fakeLimit;
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

    public void setNightBonusActive(boolean nightBonusActive) {
        this.nightBonusActive = nightBonusActive;
    }

    public boolean isNightBonusActive() {
        return nightBonusActive;
    }

    public void setNightBonusStartHour(int nightBonusStartHour) {
        this.nightBonusStartHour = nightBonusStartHour;
    }

    public int getNightBonusStartHour() {
        return nightBonusStartHour;
    }

    public void setNightBonusEndHour(int nightBonusEndHour) {
        this.nightBonusEndHour = nightBonusEndHour;
    }

    public int getNightBonusEndHour() {
        return nightBonusEndHour;
    }
    
    public int getMoralType() {
        return moral;
    }

    public void setMoralType(int pMoral) {
        if(pMoral < 0 || pMoral > 3) {
            throw new IllegalArgumentException("Invalid moral type (" + pMoral + ")");
        }
        this.moral = pMoral;
    }
    private void setResourceConstant(int resourceConstant) {
        this.resourceConstant = resourceConstant;
    }

    public int getResourceConstant() {
        return resourceConstant;
    }
    
    public int getNobleSystem() {
        return nobleSystem;
    }

    /**
     * Add a manager listener
     *
     * @param pListener
     */
    public void addListener(ServerSettingsListener pListener) {
        if (!listeners.contains(pListener)) {
            listeners.add(pListener);
        }
    }

    /**
     * Remove a manager listener
     *
     * @param pListener
     */
    public void removeListener(ServerSettingsListener pListener) {
        listeners.remove(pListener);
    }
    
    void fireServerSettingsChanged() {
        for (ServerSettingsListener listener : listeners.toArray(new ServerSettingsListener[listeners.size()])) {
            listener.fireServerSettingsChanged();
        }
    }
    
    public interface ServerSettingsListener {
        void fireServerSettingsChanged();
    }
}
