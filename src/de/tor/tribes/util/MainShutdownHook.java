/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.LayerOrderConfigurationFrame;
import java.io.File;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;

/**
 * @author Charon
 */
public class MainShutdownHook extends Thread {

    private static Logger logger = Logger.getLogger("ShutdownHook");

    public MainShutdownHook() {
        setName("ShutdownHook");
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
            GlobalOptions.addProperty("layer.order", LayerOrderConfigurationFrame.getSingleton().getLayerOrder());
            DSWorkbenchMainFrame.getSingleton().storeProperties();
            GlobalOptions.saveProperties();
            GlobalOptions.storeViewStates();
            if (!FileUtils.deleteQuietly(new File(".running"))) {
                logger.warn("Failed to remove file '.running'");
            }
            logger.debug("Shutdown finished");
        } catch (Throwable t) {
            logger.error("Shutdown failed", t);
        }
    }
}
