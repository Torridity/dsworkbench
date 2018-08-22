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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Transport;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.awt.Desktop;
import java.net.URI;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Charon
 */
public class BrowserInterface {

    private static Logger logger = LogManager.getLogger("BrowserInterface");

    public static boolean sendAttack(Attack pAttack) {
        return sendAttack(pAttack, null);
    }

    public static boolean sendAttack(Attack pAttack, UserProfile pProfile) {
        boolean result = false;
        if (pAttack != null) {
            result = sendTroops(pAttack.getSource(), pAttack.getTarget(), pAttack.getSendTime(),
                    pAttack.getTroops().transformToFixed(pAttack.getSource()) , pProfile);
            pAttack.setTransferredToBrowser(result);
        }
        return result;
    }

    public static boolean sendTroops(Village pSource, Village pTarget) {
        return sendTroops(pSource, pTarget, new TroopAmountFixed(0));
    }

    public static boolean sendTroops(Village pSource, Village pTarget, TroopAmountFixed pTroops) {
        return sendTroops(pSource, pTarget, pTroops, null);
    }

    public static boolean sendTroops(Village pSource, Village pTarget, TroopAmountFixed pTroops, UserProfile pProfile) {
        return sendTroops(pSource, pTarget, null, pTroops, pProfile);
    }

    public static boolean sendTroops(Village pSource, Village pTarget, Date pSendTime, TroopAmountFixed pTroops, UserProfile pProfile) {
        try {
            logger.debug("Transfer troops to browser for village '" + pSource + "' to '" + pTarget + "'");
            
            StringBuilder url = getBaseUrl(null);
            url.append("village=").append(pSource.getId());
            url.append("&screen=place&mode=command&target=").append(pTarget.getId());
            url.append("&type=0");
            
            for (UnitHolder unit : DataHolder.getSingleton().getSendableUnits()) {
                int amount = pTroops.getAmountForUnit(unit);
                if (amount < 0) {
                    amount = 0;
                }
                url.append("&").append(unit.getPlainName()).append("=").append(amount);
            }
            
            if (pSendTime != null) {
                url.append("&ts=").append(pSendTime.getTime());
            } else {
                url.append("&ts=").append(System.currentTimeMillis());
            }
            
            return openPage(url.toString());
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }
    
    public static boolean sendRes(Village pSource, Village pTarget) {
        return sendRes(pSource, pTarget, null, null);
    }

    public static boolean sendRes(Village pSource, Village pTarget, Transport pTrans, UserProfile pProfile) {
        try {
            StringBuilder url = getBaseUrl(null);
            url.append("village=").append(pSource.getId());
            url.append("&screen=market&mode=send&target=").append(pTarget.getId());
            if(pTrans != null) {
                url.append("&type=1");
                url.append("&wood=").append(pTrans.getSingleTransports().get(0).getAmount());
                url.append("&stone=").append(pTrans.getSingleTransports().get(1).getAmount());
                url.append("&iron=").append(pTrans.getSingleTransports().get(2).getAmount());
            }
            
            return openPage(url.toString());
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean centerVillage(Village pSource) {
        return centerCoordinate(pSource.getX(), pSource.getY());
    }

    public static boolean centerCoordinate(int pX, int pY) {
        try {
            StringBuilder url = getBaseUrl(null);
            url.append("screen=map&x=").append(pX).append("&y=").append(pY);
            
            return openPage(url.toString());
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean openPlaceTroopsView(Village pVillage) {
        try {
            StringBuilder url = getBaseUrl(null);
            url.append("village=").append(pVillage.getId());
            url.append("&screen=place&mode=units");
            
            return openPage(url.toString());
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean showVillageInfoInGame(Village pVillage) {
        if(pVillage == null) {
            return false;
        }
        try {
            StringBuilder url = getBaseUrl(null);
            url.append("village=").append(pVillage.getId());
            url.append("&screen=info_player&id=").append(pVillage.getId());
            
            return openPage(url.toString());
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean showTribeInfoInGame(Tribe pTribe) {
        if(pTribe == null) {
            return false;
        }
        try {
            StringBuilder url = getBaseUrl(null);
            url.append("&screen=info_player&id=").append(pTribe.getId());
            
            return openPage(url.toString());
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean showAllyInfoInGame(Ally pAlly) {
        if(pAlly == null) {
            return false;
        }
        try {
            StringBuilder url = getBaseUrl(null);
            url.append("&screen=info_ally&id=").append(pAlly.getId());
            
            return openPage(url.toString());
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }
    
    private static StringBuilder getBaseUrl(UserProfile pProfile) {
        StringBuilder baseURL = new StringBuilder();
        baseURL.append(ServerManager.getServerURL(GlobalOptions.getSelectedServer()));
        baseURL.append("/game.php?");
        int uvID = -1;
        if (pProfile != null) {
            uvID = pProfile.getUVId();
        } else {
            uvID = GlobalOptions.getSelectedProfile().getUVId();
        }
        if (uvID >= 0) {
            baseURL.append("t=").append(uvID).append("&");
        }
        return baseURL;
    }

    public static boolean openPage(String pUrl) {
        String browser = GlobalOptions.getProperty("default.browser");
        if (browser == null || browser.length() < 1) {
            try {
                Desktop.getDesktop().browse(new URI(pUrl));
            } catch (Throwable t) {
                logger.error("Failed opening URL " + pUrl);
                return false;
            }
        } else {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{browser, pUrl});
                p.waitFor();
            } catch (Throwable t) {
                logger.error("Failed opening URL " + pUrl);
                return false;
            }
        }
        int sleep = GlobalOptions.getProperties().getInt("command.sleep.time");
        if (sleep < 100) {
            logger.warn("command.sleep.time must not be smaller than 100. Setting value to minimum.");
            sleep = 100;
        }
        try {
            Thread.sleep(sleep);
        } catch (Exception ignored) {
        }
        return true;
    }
}
