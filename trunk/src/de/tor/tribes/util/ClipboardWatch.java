/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.util.parser.ReportParser;
import de.tor.tribes.util.parser.TroopsParser;
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

    private static Logger logger = Logger.getLogger(ClipboardWatch.class);
    private static ClipboardWatch SINGLETON = null;

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
        logger.info("Starting ClipboardWatch");
        while (true) {
            try {
                Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                String data = (String) t.getTransferData(DataFlavor.stringFlavor);
                if (data.length() > 10) {
                    if (ReportParser.parse(data)) {
                        //report parsed, clean clipboard
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                    } else if (TroopsParser.parse(data)) {
                        //at least one village was found, so clean the clipboard
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                //no usable data
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }
    }
}
