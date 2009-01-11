/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.types.Village;
import java.awt.Desktop;
import java.net.URI;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class BrowserCommandSender {

    private static Logger logger = Logger.getLogger("BrowserInterface");

    public static void sendTroops(Village pSource, Village pTarget) {
        try {

            String url = "http://" + GlobalOptions.getSelectedServer() + ".die-staemme.de/game.php?village=";
            url += pSource.getId() + "&screen=place&mode=command&target=" + pTarget.getId();
            /*            
            javascript:
            var A;
            if (frames.length>=1){
            A=main
            }else{
            A=this;
            };
            A.insertUnit(A.document.forms['units'].elements['spy'],100);
            A.insertUnit(A.document.forms['units'].elements['x'],456);
            A.insertUnit(A.document.forms['units'].elements['y'],472);
            A.insertUnit(A.document.forms['units'].elements['attack'].click());    
             */
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Öffnen des Browsers", "Fehler", JOptionPane.ERROR_MESSAGE);
            logger.error("Failed to open browser window", e);
        }
    }

    public static void openPage(String pUrl) {
        try {
            Desktop.getDesktop().browse(new URI(pUrl));
        } catch (Exception e) {
            logger.error("Failed opening URL " + pUrl);
        }
    }

    public static void centerVillage(Village pSource) {
        try {
            String url = "http://" + GlobalOptions.getSelectedServer() + ".die-staemme.de/game.php?village=";
            url += pSource.getId() + "&screen=map&x=" + pSource.getX() + "&y=" + pSource.getY();
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Öffnen des Browsers", "Fehler", JOptionPane.ERROR_MESSAGE);
            logger.error("Failed to open browser window", e);
        }
    }

    public static void centerCoordinate(int pX, int pY) {
        try {
            String url = "http://" + GlobalOptions.getSelectedServer() + ".die-staemme.de/game.php?village=";
            url += "&screen=map&x=" + pX + "&y=" + pY;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Öffnen des Browsers", "Fehler", JOptionPane.ERROR_MESSAGE);
            logger.error("Failed to open browser window", e);
        }
    }

    public static void sendRes(Village pSource, Village pTarget) {
        try {
            String url = "http://" + GlobalOptions.getSelectedServer() + ".die-staemme.de/game.php?village=";
            url += pSource.getId() + "&screen=market&mode=send&target=" + pTarget.getId();
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim Öffnen des Browsers", "Fehler", JOptionPane.ERROR_MESSAGE);
            logger.error("Failed to open browser window", e);
        }
    }
}
