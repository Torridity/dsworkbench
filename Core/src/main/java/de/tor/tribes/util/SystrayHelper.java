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
            Image image = Toolkit.getDefaultToolkit().getImage(SystrayHelper.class.getResource("/res/ui/axe_systray.png"));
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
