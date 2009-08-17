/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.attack.StandardAttackManager;
import java.awt.Desktop;
import java.net.URI;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 * http://de8.die-staemme.de/game.php?t=743256&village=269739&screen=place
 * http://de8.die-staemme.de/game.php?t=743256&village=273090&screen=place&mode=command&target=285904
 * @TODO (DIFF) Troops can be added to browser
 * @author Charon
 */
public class BrowserCommandSender {

    private static Logger logger = Logger.getLogger("BrowserInterface");

    public static void sendTroops(Village pSource, Village pTarget, int pType) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());

            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getUVID();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=place&mode=command&target=" + pTarget.getId();
            url +="&type=0";
            System.out.println("Type is " + pType);
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                url += "&" + unit.getPlainName() + "=" + StandardAttackManager.getSingleton().getAmountForVillage(pType, unit, pSource);
            }
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        } catch (Throwable t) {
            JOptionPaneHelper.showErrorBox(null, "Fehler beim Öffnen des Browsers", "Fehler");
            logger.error("Failed to open browser window", t);
        }
    }

    public static void sendTroops(Village pSource, Village pTarget) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());

            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getUVID();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=place&mode=command&target=" + pTarget.getId();
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        } catch (Throwable t) {
            JOptionPaneHelper.showErrorBox(null, "Fehler beim Öffnen des Browsers", "Fehler");
            logger.error("Failed to open browser window", t);
        }
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
                Runtime.getRuntime().exec(new String[]{browser, pUrl});
            } catch (Throwable t) {
                logger.error("Failed opening URL " + pUrl);
            }
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
                Runtime.getRuntime().exec(new String[]{browser, pUrl});
            } catch (Throwable t) {
                logger.error("Failed opening URL " + pUrl);
                return false;
            }
        }
        return true;
    }

    public static void centerVillage(Village pSource) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getUVID();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=map&x=" + pSource.getX() + "&y=" + pSource.getY();
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        } catch (Throwable t) {
            JOptionPaneHelper.showErrorBox(null, "Fehler beim Öffnen des Browsers", "Fehler");
            logger.error("Failed to open browser window", t);
        }
    }

    public static void centerCoordinate(int pX, int pY) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getUVID();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += "&screen=map&x=" + pX + "&y=" + pY;
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        } catch (Throwable t) {
            JOptionPaneHelper.showErrorBox(null, "Fehler beim Öffnen des Browsers", "Fehler");
            logger.error("Failed to open browser window", t);
        }
    }

    public static void sendRes(Village pSource, Village pTarget) {
        try {
            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String url = baseURL + "/game.php?village=";
            int uvID = GlobalOptions.getUVID();
            if (uvID >= 0) {
                url = baseURL + "/game.php?t=" + uvID + "&village=";
            }
            url += pSource.getId() + "&screen=market&mode=send&target=" + pTarget.getId();
            String browser = GlobalOptions.getProperty("default.browser");
            if (browser == null || browser.length() < 1) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        } catch (Throwable t) {
            JOptionPaneHelper.showErrorBox(null, "Fehler beim Öffnen des Browsers", "Fehler");
            logger.error("Failed to open browser window", t);
        }
    }
}
