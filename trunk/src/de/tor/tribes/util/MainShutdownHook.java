/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.views.DSWorkbenchAttackFrame;
import de.tor.tribes.ui.views.DSWorkbenchChurchFrame;
import de.tor.tribes.ui.views.DSWorkbenchConquersFrame;
import de.tor.tribes.ui.DSWorkbenchDoItYourselfAttackPlaner;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.views.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.views.DSWorkbenchRankFrame;
import de.tor.tribes.ui.views.DSWorkbenchSearchFrame;
import de.tor.tribes.ui.views.DSWorkbenchTroopsFrame;
import de.tor.tribes.ui.FormConfigFrame;
import org.apache.log4j.Logger;
import de.tor.tribes.ui.views.DSWorkbenchFormFrame;
import de.tor.tribes.ui.views.DSWorkbenchNotepad;
import de.tor.tribes.ui.views.DSWorkbenchReportFrame;
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
            GlobalOptions.addProperty("attack.frame.visible", Boolean.toString(DSWorkbenchAttackFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("marker.frame.visible", Boolean.toString(DSWorkbenchMarkerFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("church.frame.visible", Boolean.toString(DSWorkbenchChurchFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("conquers.frame.visible", Boolean.toString(DSWorkbenchConquersFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("notepad.frame.visible", Boolean.toString(DSWorkbenchNotepad.getSingleton().isVisible()));
            GlobalOptions.addProperty("tag.frame.visible", Boolean.toString(DSWorkbenchTagFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("troops.frame.visible", Boolean.toString(DSWorkbenchTroopsFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("rank.frame.visible", Boolean.toString(DSWorkbenchRankFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("form.frame.visible", Boolean.toString(DSWorkbenchFormFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("search.frame.visible", Boolean.toString(DSWorkbenchSearchFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("stats.frame.visible", Boolean.toString(DSWorkbenchStatsFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("attack.frame.alwaysOnTop", Boolean.toString(DSWorkbenchAttackFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("doityourself.attack.frame.alwaysOnTop", Boolean.toString(DSWorkbenchDoItYourselfAttackPlaner.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("marker.frame.alwaysOnTop", Boolean.toString(DSWorkbenchMarkerFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("church.frame.alwaysOnTop", Boolean.toString(DSWorkbenchChurchFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("conquers.frame.alwaysOnTop", Boolean.toString(DSWorkbenchConquersFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("notepad.frame.alwaysOnTop", Boolean.toString(DSWorkbenchNotepad.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("tag.frame.alwaysOnTop", Boolean.toString(DSWorkbenchTagFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("troops.frame.alwaysOnTop", Boolean.toString(DSWorkbenchTroopsFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("rank.frame.alwaysOnTop", Boolean.toString(DSWorkbenchRankFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("form.frame.alwaysOnTop", Boolean.toString(DSWorkbenchFormFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("stats.frame.alwaysOnTop", Boolean.toString(DSWorkbenchStatsFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("form.config.frame.alwaysOnTop", Boolean.toString(FormConfigFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("search.frame.alwaysOnTop", Boolean.toString(DSWorkbenchSearchFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("selection.frame.alwaysOnTop", Boolean.toString(DSWorkbenchSelectionFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("report.frame.alwaysOnTop", Boolean.toString(DSWorkbenchReportFrame.getSingleton().isAlwaysOnTop()));
            GlobalOptions.addProperty("report.frame.visible", Boolean.toString(DSWorkbenchReportFrame.getSingleton().isVisible()));
            GlobalOptions.addProperty("layer.order", DSWorkbenchMainFrame.getSingleton().getLayerOrder());
            DSWorkbenchMainFrame.getSingleton().storeProperties();
            logger.debug("Saving global properties");
            GlobalOptions.saveProperties();
            logger.debug("Shutdown finished");
        } catch (Throwable t) {
            logger.error("Shutdown failed", t);
        }
    }
}
