/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.util.parser.GroupParser;
import de.tor.tribes.util.parser.ReportParser;
import de.tor.tribes.util.parser.TroopsParser;
import de.tor.tribes.util.parser.TroopsParser50;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class ClipboardWatch extends Thread {

    private static Logger logger = Logger.getLogger("ClipboardMonitor");
    private static ClipboardWatch SINGLETON = null;
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

    @Override
    public void run() {
        logger.info("Starting ClipboardMonitor");
        while (true) {
            try {
                Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                String data = (String) t.getTransferData(DataFlavor.stringFlavor);

                if ((data.length() > 10) && (data.length() != lastDataLength)) {
                    if (ReportParser.parse(data)) {
                        //report parsed, clean clipboard
                        logger.info("Report successfully parsed. Cleaning up clipboard");
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                    } else if (TroopsParser.parse(data)) {
                        logger.info("Troops successfully parsed. Cleaning up clipboard");
                        //at least one village was found, so clean the clipboard
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                  //  }else if (TroopsParser50.parse(data)) {
                      //  logger.info("Troops successfully parsed. Cleaning up clipboard");
                        //at least one village was found, so clean the clipboard
                     //   Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                    } else if (GroupParser.parse(data)) {
                        logger.info("Groups successfully parsed. Cleaning up clipboard");
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                    } else {
                        //store last length to avoid parsing the same data more than once
                        lastDataLength = data.length();
                    }
                }
            } catch (Exception e) {
                //no usable data
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }
}
