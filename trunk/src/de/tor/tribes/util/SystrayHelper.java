/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.awt.*;

/**
 *
 * @author Torridity
 */
public class SystrayHelper {

    private static TrayIcon trayIcon;
    private static boolean installed = false;

    public static boolean isSystraySupported() {
        return SystemTray.isSupported();
    }

    public static void installSystrayIcon() {
        if (!installed && SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(SystrayHelper.class.getResource("/res/ui/axe.png"));
            trayIcon = new TrayIcon(image, "DS Workbench", null);

            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
                installed = true;
            } catch (AWTException e) {
                trayIcon = null;
            }
        }
    }

    public static boolean isSystrayEnabled() {
        String systrayEnabled = GlobalOptions.getProperty("systray.enabled");
        if (systrayEnabled == null) {//developer mode
            return true;
        }
        return Boolean.parseBoolean(GlobalOptions.getProperty("systray.enabled"));
    }

    public static void showInfoMessage(String pMessage) {
        if (trayIcon != null && isSystrayEnabled()) {
            trayIcon.displayMessage("Information", pMessage, TrayIcon.MessageType.INFO);
        }
    }

    public static void showWarningMessage(String pMessage) {
        if (trayIcon != null && isSystrayEnabled()) {
            trayIcon.displayMessage("Warnung", pMessage, TrayIcon.MessageType.WARNING);
        }
    }

    public static void showErrorMessage(String pMessage) {
        if (trayIcon != null && isSystrayEnabled()) {
            trayIcon.displayMessage("Fehler", pMessage, TrayIcon.MessageType.ERROR);
        }
    }
}
