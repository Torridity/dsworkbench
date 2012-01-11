/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.ui.windows.ClockFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.apache.log4j.Logger;

/**
 * @TODO DIFF Added notification alert
 *
 * @author Charon
 */
public class ClipboardWatch extends Thread {

    private static Logger logger = Logger.getLogger("ClipboardMonitor");
    private static ClipboardWatch SINGLETON = null;
    private static String recentlyParsedData = null;
    private int lastDataLength = 0;

    public static synchronized ClipboardWatch getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ClipboardWatch();
            SINGLETON.start();
        }
        return SINGLETON;
    }

    ClipboardWatch() {
        setDaemon(true);
        setPriority(MIN_PRIORITY);
    }

    private synchronized void playNotification() {
        if (!Boolean.parseBoolean(GlobalOptions.getProperty("clipboard.notification"))) {
            return;
        }

        Timer t = new Timer("ClipboardNotification", true);
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                Clip clip = null;
                AudioClip ac = null;
                try {
                    if (org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS) {
                        clip = AudioSystem.getClip();
                        AudioInputStream inputStream = AudioSystem.getAudioInputStream(ClockFrame.class.getResourceAsStream("/res/Ding.wav"));
                        clip.open(inputStream);
                        clip.start();
                    } else {
                        ac = Applet.newAudioClip(ClockFrame.class.getResource("/res/Ding.wav"));
                        ac.play();
                    }
                } catch (Exception e) {
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                try {
                    if (clip != null) {
                        clip.stop();
                        clip.flush();
                        clip = null;
                    }

                    if (ac != null) {
                        ac.stop();
                        ac = null;
                    }
                } catch (Exception e) {
                }
            }
        }, 0);
    }

    @Override
    public void run() {
        logger.info("Starting ClipboardMonitor");
        while (true) {
            if (DSWorkbenchMainFrame.getSingleton().isWatchClipboard()) {
                try {
                    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                    String data = (String) t.getTransferData(DataFlavor.stringFlavor);

                    if ((data.length() > 10) && (data.length() != lastDataLength)) {
                        if (recentlyParsedData == null || !data.equals(recentlyParsedData)) {
                            if (PluginManager.getSingleton().executeReportParser(data)) {
                                //report parsed, clean clipboard
                                logger.info("Report successfully parsed.");
                                playNotification();
                                recentlyParsedData = data;
                            } else if (PluginManager.getSingleton().executeTroopsParser(data)) {
                                logger.info("Troops successfully parsed.");
                                playNotification();
                                //at least one village was found, so clean the clipboard
                                recentlyParsedData = data;
                            } else if (PluginManager.getSingleton().executeGroupParser(data)) {
                                logger.info("Groups successfully parsed.");
                                playNotification();
                                recentlyParsedData = data;
                            } else if (PluginManager.getSingleton().executeSupportParser(data)) {
                                logger.info("Support successfully parsed.");
                                playNotification();
                                recentlyParsedData = data;
                            } else if (PluginManager.getSingleton().executeNonPAPlaceParser(data)) {
                                logger.info("Place info successfully parsed.");
                                playNotification();
                                recentlyParsedData = data;
                            } else {
                                //store last length to avoid parsing the same data more than once
                                lastDataLength = data.length();
                            }
                        }
                    }
                } catch (Exception e) {
                    //no usable data
                    //  e.printStackTrace();
                }
            } else {
                //clipboard watch is disabled, sleep 9 + 1 seconds
                try {
                    Thread.sleep(9000);
                } catch (Exception e) {
                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }
}
