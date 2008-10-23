/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.ui.DSWorkbenchAttackFrame;
import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class SystrayManager {

    private static Logger logger = Logger.getLogger(SystrayManager.class);
    private static TrayIcon ATTACK = null;
    private static TrayIcon UPDATE = null;
    private static TrayIcon CURRENT = null;
    private static Timer HIDE_TIMER = null;
    

    static {
        if (SystemTray.isSupported()) {
            try {
                ATTACK = new TrayIcon(Toolkit.getDefaultToolkit().getImage("graphics/icons/att.png"), "Tooltip OK");
                ATTACK.addMouseListener(new MouseListener() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
                            DSWorkbenchAttackFrame.getSingleton().setVisible(true);
                            DSWorkbenchAttackFrame.getSingleton().toFront();
                            SystemTray.getSystemTray().remove(CURRENT);
                            if (HIDE_TIMER != null) {
                                HIDE_TIMER.cancel();
                                HIDE_TIMER = null;
                            }
                            CURRENT = null;
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });

                UPDATE = new TrayIcon(ImageIO.read(new File("graphics/icons/update.png")));
                UPDATE.addMouseListener(new MouseListener() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
                            BrowserCommandSender.openPage("http://www.dsworkbench.de");
                            SystemTray.getSystemTray().remove(CURRENT);
                            if (HIDE_TIMER != null) {
                                HIDE_TIMER.cancel();
                                HIDE_TIMER = null;
                            }
                            CURRENT = null;
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to initialize systray manager");
                e.printStackTrace();
            }
        }
    }

    private static boolean showIcon(TrayIcon pIcon) {
        if (CURRENT != null) {
            //if there is already an icon visible, hide it and abort hide timer
            SystemTray.getSystemTray().remove(CURRENT);
            if (HIDE_TIMER != null) {
                HIDE_TIMER.cancel();
                HIDE_TIMER = null;
            }
        }

        try {
            SystemTray.getSystemTray().add(pIcon);
            //set current icon and start hide timer
            CURRENT = pIcon;
            HIDE_TIMER = new Timer();
            int duration = 10000;
            try {
                duration = Integer.parseInt(GlobalOptions.getProperty("info.duration")) * 1000;
            } catch (Exception e) {
            }
            HIDE_TIMER.schedule(new TimerTask() {

                @Override
                public void run() {
                    SystemTray.getSystemTray().remove(getCurrentIcon());
                }
            }, duration);
            return true;
        } catch (IllegalArgumentException iae) {
            //icon already available
            iae.printStackTrace();
            return true;
        } catch (AWTException awte) {
            logger.error("Failed to add systray icon. Tray is not visible?");
            awte.printStackTrace();
        } catch (Exception e) {
            logger.error("Failed to add systray icon", e);
            e.printStackTrace();
        }
        return false;
    }

    public static void notifyOnAttacks(int pCount) {
        if (showIcon(ATTACK)) {
            //ATTACK.displayMessage("Anstehende Angriffe", "In den kommenden 10 Minuten " + ((pCount == 1) ? "muss 1 Angriff " : "müssen " + pCount + " Angriffe ") + "abgeschickt werden.\n" +
            //      "Klicke doppelt auf dieses Icon, um die Angriffsübersicht zu öffnen.", TrayIcon.MessageType.WARNING);
            ATTACK.displayMessage("Test", "test123", TrayIcon.MessageType.NONE);
        }

    }

    public static void notifyOnUpdate(double pVersion) {
        if (showIcon(UPDATE)) {
            try {
                UPDATE.displayMessage("Update verfügbar", "Eine neue Version (" + pVersion + ") von DS Workbench ist verfügbar.\nKlicke doppelt auf dieses Icon, um die Webseite zu öffnen.", TrayIcon.MessageType.INFO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static TrayIcon getCurrentIcon() {
        return CURRENT;
    }
}

class HideTask extends TimerTask {

    public void run() {
    }
}
