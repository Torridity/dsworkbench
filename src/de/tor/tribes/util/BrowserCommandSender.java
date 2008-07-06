/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.types.Village;
import java.awt.Desktop;
import java.net.URI;
import javax.swing.JOptionPane;

/**
 *
 * @author Charon
 */
public class BrowserCommandSender {

    private static final String errMsg = "Fehler beim starten des Web-Browsers";
    private static final Desktop DESKTOP = Desktop.getDesktop();

    public static void sendAttack(Village pSource, Village pTarget) {
        String url = "http://" + GlobalOptions.getSelectedServer() + ".die-staemme.de/game.php?village=";
        url += pSource.getId() + "&screen=place&mode=command&target=" + pTarget.getId();
        try {
            DESKTOP.browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Öffnen des Browsers", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void centerVillageInGame(Village pSource) {
        String url = "http://" + GlobalOptions.getSelectedServer() + ".die-staemme.de/game.php?village=";
        url += pSource.getId() + "&screen=map&x=" + pSource.getX() + "&y=" + pSource.getY();
        try {
            DESKTOP.browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Öffnen des Browsers", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

     public static void centerPointInGame(int pX, int pY) {
        String url = "http://" + GlobalOptions.getSelectedServer() + ".die-staemme.de/game.php?village=";
        url += "&screen=map&x=" + pX + "&y=" + pY;
        try {
            DESKTOP.browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Öffnen des Browsers", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }
     
    /*private static void openURL(String url) {
    String osName = System.getProperty("os.name");
    try {
    if (osName.startsWith("Mac OS")) {
    Class fileMgr = Class.forName("com.apple.eio.FileManager");
    Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
    openURL.invoke(null, new Object[]{url});
    } else if (osName.startsWith("Windows")) {
    
    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
    } else { //assume Unix or Linux
    String[] browsers = {
    "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
    };
    String browser = null;
    for (int count = 0; count < browsers.length && browser == null; count++) {
    if (Runtime.getRuntime().exec(
    new String[]{"which", browsers[count]}).waitFor() == 0) {
    browser = browsers[count];
    }
    }
    if (browser == null) {
    throw new Exception("Konnte Web-Browser nicht finden");
    } else {
    Runtime.getRuntime().exec(new String[]{browser, url});
    }
    }
    } catch (Exception e) {
    JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
    }
    }*/
}
