/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.ui.DSWorkbenchAttackFrame;
import de.tor.tribes.ui.DSWorkbenchChurchFrame;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.DSWorkbenchRankFrame;
import de.tor.tribes.ui.DSWorkbenchSearchFrame;
import de.tor.tribes.ui.DSWorkbenchTroopsFrame;
import de.tor.tribes.ui.FormConfigFrame;
import org.apache.log4j.Logger;
import de.tor.tribes.ui.DSWorkbenchFormFrame;

/**
 *
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
            GlobalOptions.saveUserData();
            GlobalOptions.addProperty("attack.frame.visible", Boolean.toString(DSWorkbenchAttackFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("marker.frame.visible", Boolean.toString(DSWorkbenchMarkerFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("church.frame.visible", Boolean.toString(DSWorkbenchChurchFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("troops.frame.visible", Boolean.toString(DSWorkbenchTroopsFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("rank.frame.visible", Boolean.toString(DSWorkbenchRankFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("form.frame.visible", Boolean.toString(DSWorkbenchFormFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("search.frame.visible", Boolean.toString(DSWorkbenchSearchFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("attack.frame.alwaysOnTop", Boolean.toString(DSWorkbenchAttackFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("marker.frame.alwaysOnTop", Boolean.toString(DSWorkbenchMarkerFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("church.frame.alwaysOnTop", Boolean.toString(DSWorkbenchChurchFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("troops.frame.alwaysOnTop", Boolean.toString(DSWorkbenchTroopsFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("rank.frame.alwaysOnTop", Boolean.toString(DSWorkbenchRankFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("form.frame.alwaysOnTop", Boolean.toString(DSWorkbenchFormFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("form.config.frame.alwaysOnTop", Boolean.toString(FormConfigFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("search.frame.alwaysOnTop", Boolean.toString(DSWorkbenchSearchFrame.getSingleton().isAlwaysOnTop()));
            logger.debug("Saving global properties");
            GlobalOptions.saveProperties();
            logger.debug("Shutdown finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
