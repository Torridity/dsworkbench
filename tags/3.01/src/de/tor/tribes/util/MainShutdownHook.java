/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import org.apache.log4j.Logger;
import de.tor.tribes.ui.views.DSWorkbenchFormFrame;
import de.tor.tribes.ui.views.DSWorkbenchMerchantDistibutor;
import de.tor.tribes.ui.views.DSWorkbenchNotepad;
import de.tor.tribes.ui.views.DSWorkbenchReTimerFrame;
import de.tor.tribes.ui.views.DSWorkbenchReportFrame;
import de.tor.tribes.ui.views.DSWorkbenchSOSRequestAnalyzer;
import de.tor.tribes.ui.views.DSWorkbenchSelectionFrame;
import de.tor.tribes.ui.views.DSWorkbenchStatsFrame;
import de.tor.tribes.ui.views.DSWorkbenchTagFrame;

/**
 * @author Charon
 */
public class MainShutdownHook extends Thread {

    private static Logger logger = Logger.getLogger("ShutdownHook");

    public MainShutdownHook() {
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            logger.info("Performing ShutdownHook");
            if (!DataHolder.getSingleton().isDataValid()) {
                logger.error("Server data seems to be invalid. No user data will be stored!");
                return;
            }
            GlobalOptions.saveUserData();
            GlobalOptions.addProperty("layer.order", DSWorkbenchMainFrame.getSingleton().getLayerOrder());
            DSWorkbenchMainFrame.getSingleton().storeProperties();
            GlobalOptions.saveProperties();
            GlobalOptions.storeViewStates();
            logger.debug("Shutdown finished");
        } catch (Throwable t) {
            logger.error("Shutdown failed", t);
        }
    }
}
