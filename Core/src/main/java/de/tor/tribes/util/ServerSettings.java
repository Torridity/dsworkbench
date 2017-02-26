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
import java.awt.Dimension;
import java.io.File;
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
    private int BONUS_NEW = 0;
    private int SNOB_RANGE = 70;
    private boolean church = false;
    private boolean millisArrival = true;
    private double speed = 1.0;
    private double riseSpeed = 1.0;

    private boolean nightBonusActive = true;
    private int nightBonusStartHour = 0;
    private int nightBonusEndHour = 8;
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
            SERVER_ID = pServerID;
            String serverPath = Constants.SERVER_DIR + "/" + SERVER_ID + "/settings.xml";
            logger.debug("Parse server settings from '" + serverPath + "'");
            Document d = JaxenUtils.getDocument(new File(serverPath));
            logger.debug(" - reading map system");
            try {
                setCoordType(1000);
            } catch (Exception inner) {
                setCoordType(1000);
            }
            logger.debug(" - reading bonus type");
            try {
                BONUS_NEW = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/bonus_new"));
            } catch (Exception inner) {
                BONUS_NEW = 0;
            }
            logger.debug(" - reading snob distance");
            try {
                SNOB_RANGE = Integer.parseInt(JaxenUtils.getNodeValue(d, "//snob/max_dist"));
            } catch (Exception inner) {
                SNOB_RANGE = 70;
            }
            logger.debug(" - reading church setting");
            try {
                church = Integer.parseInt(JaxenUtils.getNodeValue(d, "//game/church")) == 1;
            } catch (Exception inner) {
                church = false;
            }
            logger.debug(" - reading millis setting");
            try {
                millisArrival = Integer.parseInt(JaxenUtils.getNodeValue(d, "//misc/millis_arrival")) == 1;
            } catch (Exception inner) {
                try {//new settings is under "commands"
                    millisArrival = Integer.parseInt(JaxenUtils.getNodeValue(d, "//commands/millis_arrival")) == 1;
                } catch (Exception inner2) {
                    //empty or invalid value...use no millis
                    millisArrival = false;
                }
            }

            logger.debug(" - reading server speed");
            try {
                this.speed = Double.parseDouble(JaxenUtils.getNodeValue(d, "//speed"));
            } catch (Exception inner) {
                this.speed = 1.0;
            }

            logger.debug(" - reading rise speed");
            try {
                this.riseSpeed = Double.parseDouble(JaxenUtils.getNodeValue(d, "//snob/rise"));
            } catch (Exception inner) {
                this.riseSpeed = 1.0;
            }

            logger.debug(" - reading night bonus");
            try {
                this.nightBonusActive = Integer.parseInt(JaxenUtils.getNodeValue(d, "//night/active")) == 1;
            } catch (Exception inner) {
                this.nightBonusActive = true;
            }
            logger.debug(" - reading night bonus start hour");
            try {
                this.nightBonusStartHour = Integer.parseInt(JaxenUtils.getNodeValue(d, "//night/start_hour"));
            } catch (Exception inner) {
                this.nightBonusStartHour = 0;
            }
            logger.debug(" - reading night bonus end hour");
            try {
                this.nightBonusStartHour = Integer.parseInt(JaxenUtils.getNodeValue(d, "//night/end_hour"));
            } catch (Exception inner) {
                this.nightBonusEndHour = 8;
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
        if (mapSize == null) {
            return new Dimension(1000, 1000);
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

    public void setRiseSpeed(double speed) {
        this.riseSpeed = speed;
    }

    public double getRiseSpeed() {
        return riseSpeed;
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

}