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
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.types.Transport;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.attack.StandardAttackManager;
import java.awt.Desktop;
import java.net.URI;
import java.util.Date;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 * http://de8.die-staemme.de/game.php?t=743256&village=269739&screen=place
 * http://de8.die-staemme.de/game.php?t=743256&village=273090&screen=place&mode=command&target=285904
 *
 * @author Charon
 */
public class BrowserCommandSender {

    private static Logger logger = Logger.getLogger("BrowserInterface");

    public static boolean sendAttack(Attack pAttack, UserProfile pProfile) {
        boolean result = false;
        if (pAttack != null) {
            result = sendTroops(pAttack.getSource(), pAttack.getTarget(), pAttack.getSendTime(), pAttack.getType(), pProfile);
            pAttack.setTransferredToBrowser(result);
        }
        return result;
    }

    public static boolean sendAttack(Attack pAttack) {
        return sendAttack(pAttack, null);
    }

    private static boolean sendTroops(Village pSource, Village pTarget, Date pSendTime, int pTypeIcon, UserProfile pProfile) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            logger.debug("Transfer troops to browser for village '" + pSource + "' to '" + pTarget + "' with type '" + pTypeIcon + "'");
            String url = baseURL + "/game.php?village=";
            int uvID = -1;
            if (pProfile != null) {
                uvID = pProfile.getUVId();
            } else {
                uvID = GlobalOptions.getSelectedProfile().getUVId();
            }
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=place&mode=command&target=" + pTarget.getId();
            url += "&type=0";

            StandardAttack a = StandardAttackManager.getSingleton().getElementByIcon(pTypeIcon);
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                int amount = 0;
                if (a != null) {
                    amount = a.getAmountForUnit(unit, pSource);
                }
                url += "&" + unit.getPlainName() + "=" + amount;
            }
            if (pSendTime != null) {
                url += "&ts=" + pSendTime.getTime();
            } else {
                url += "&ts=" + System.currentTimeMillis();
            }

            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                p.waitFor();
            }
            int sleep = 150;
            String prop = GlobalOptions.getProperty("command.sleep.time");
            try {
                if (prop != null) {
                    sleep = Integer.parseInt(GlobalOptions.getProperty("command.sleep.time"));
                    if (sleep < 100) {
                        logger.warn("command.sleep.time must not be smaller than 100. Setting value to minimum.");
                        sleep = 100;
                    }
                }
            } catch (Exception e) {
                logger.error("Property command.sleep.time is no int value (" + prop + ")");
            }
            try {
                Thread.sleep(sleep);
            } catch (Exception ignored) {
            }
        } catch (Throwable t) {
            JOptionPaneHelper.showErrorBox(null, "Fehler beim Öffnen des Browsers", "Fehler");
            logger.error("Failed to open browser window", t);
            return false;
        }
        return true;
    }

    public static boolean sendRes(Village pSource, Village pTarget, Transport pTrans, UserProfile pProfile) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            logger.debug("Transfer resources to browser for village '" + pSource + "' to '" + pTarget);
            String url = baseURL + "/game.php?village=";
            int uvID = -1;
            if (pProfile != null) {
                uvID = pProfile.getUVId();
            } else {
                uvID = GlobalOptions.getSelectedProfile().getUVId();
            }
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=market&mode=send&target=" + pTarget.getId();
            url += "&type=1";
            url += "&wood=" + pTrans.getSingleTransports().get(0).getAmount();
            url += "&stone=" + pTrans.getSingleTransports().get(1).getAmount();
            url += "&iron=" + pTrans.getSingleTransports().get(2).getAmount();
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                p.waitFor();
            }
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
            return true;
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean sendTroops(Village pSource, Village pTarget) {
        return sendTroops(pSource, pTarget, new Hashtable<UnitHolder, Integer>());
    }

    public static boolean sendTroops(Village pSource, Village pTarget, Hashtable<UnitHolder, Integer> pTroops) {
        return sendTroops(pSource, pTarget, pTroops, null);
    }

    public static boolean sendTroops(Village pSource, Village pTarget, Hashtable<UnitHolder, Integer> pTroops, UserProfile pProfile) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            logger.debug("Transfer troops to browser for village '" + pSource + "' to '" + pTarget + "'");
            String url = baseURL + "/game.php?village=";
            int uvID = -1;
            if (pProfile != null) {
                uvID = pProfile.getUVId();
            } else {
                uvID = GlobalOptions.getSelectedProfile().getUVId();
            }
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }

            url += pSource.getId() + "&screen=place&mode=command&target=" + pTarget.getId();
            url += "&type=0";
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                Integer amount = pTroops.get(unit);
                if (amount == null) {
                    amount = 0;
                }
                url += "&" + unit.getPlainName() + "=" + amount;
            }
            url += "&ts=" + System.currentTimeMillis();
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                p.waitFor();
            }

            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
        return true;
    }

    public static void openPage(String pUrl) {
        String browser = GlobalOptions.getProperty("default.browser");
        if (browser == null || browser.length() < 1) {
            try {
                Desktop.getDesktop().browse(new URI(pUrl));
            } catch (Throwable t) {
                logger.error("Failed opening URL " + pUrl);
            }
        } else {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{browser, pUrl});
                p.waitFor();
            } catch (Throwable t) {
                logger.error("Failed opening URL " + pUrl);
            }
        }
        try {
            Thread.sleep(100);
        } catch (Exception ignored) {
        }
    }

    public static boolean openTestPage(String pUrl) {
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
        try {
            Thread.sleep(100);
        } catch (Exception ignored) {
        }
        return true;
    }

    public static boolean openPlaceTroopsView(Village pSource) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getSelectedProfile().getUVId();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=place&mode=units";
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                p.waitFor();
            }
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
            return true;
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean centerVillage(Village pSource) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getSelectedProfile().getUVId();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=map&x=" + pSource.getX() + "&y=" + pSource.getY();
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                p.waitFor();
            }
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
            return true;
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean showVillageInfoInGame(Village pSource) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getSelectedProfile().getUVId();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=info_village&id=" + pSource.getId();
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                p.waitFor();
            }
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
            return true;
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean centerCoordinate(int pX, int pY) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getSelectedProfile().getUVId();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += "&screen=map&x=" + pX + "&y=" + pY;
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                p.waitFor();
            }

            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
            return true;
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }

    public static boolean sendRes(Village pSource, Village pTarget) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getSelectedProfile().getUVId();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=market&mode=send&target=" + pTarget.getId();
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Process p = Runtime.getRuntime().exec(new String[]{browser, url});
                p.waitFor();
            }

            try {
                Thread.sleep(100);
            } catch (Exception ignored) {
            }
            return true;
        } catch (Throwable t) {
            logger.error("Failed to open browser window", t);
            return false;
        }
    }
}
