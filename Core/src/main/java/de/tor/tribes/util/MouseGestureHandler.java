/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import com.smardec.mousegestures.MouseGesturesListener;
import de.tor.tribes.ui.views.DSWorkbenchAttackFrame;
import de.tor.tribes.ui.views.DSWorkbenchConquersFrame;
import de.tor.tribes.ui.views.DSWorkbenchFormFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.views.DSWorkbenchMarkerFrame;
import de.tor.tribes.ui.views.DSWorkbenchNotepad;
import de.tor.tribes.ui.views.DSWorkbenchRankFrame;
import de.tor.tribes.ui.views.DSWorkbenchReportFrame;
import de.tor.tribes.ui.views.DSWorkbenchStatsFrame;
import de.tor.tribes.ui.views.DSWorkbenchTagFrame;
import de.tor.tribes.ui.views.DSWorkbenchTroopsFrame;

/**
 *
 * @author Torridity
 */
public class MouseGestureHandler implements MouseGesturesListener {

    @Override
    public void processGesture(String string) {
        // System.out.println("Gesture: " + string);
        if (DSWorkbenchAttackFrame.getSingleton().isActive()) {
            DSWorkbenchAttackFrame.getSingleton().handleGesture(string);
        } else if (DSWorkbenchMarkerFrame.getSingleton().isActive()) {
            DSWorkbenchMarkerFrame.getSingleton().handleGesture(string);
        } else if (DSWorkbenchNotepad.getSingleton().isActive()) {
            DSWorkbenchNotepad.getSingleton().handleGesture(string);
        } else if (DSWorkbenchTroopsFrame.getSingleton().isActive()) {
            DSWorkbenchTroopsFrame.getSingleton().handleGesture(string);
        } else if (DSWorkbenchFormFrame.getSingleton().isActive()) {
            DSWorkbenchFormFrame.getSingleton().handleGesture(string);
        } else if (DSWorkbenchConquersFrame.getSingleton().isActive()) {
            DSWorkbenchConquersFrame.getSingleton().handleGesture(string);
        } else if (DSWorkbenchTagFrame.getSingleton().isActive()) {
            DSWorkbenchTagFrame.getSingleton().handleGesture(string);
        } else if (DSWorkbenchStatsFrame.getSingleton().isActive()) {
            DSWorkbenchStatsFrame.getSingleton().handleGesture(string);
        } else if (DSWorkbenchReportFrame.getSingleton().isActive()) {
            DSWorkbenchReportFrame.getSingleton().handleGesture(string);
        } /*else if (DSWorkbenchMainFrame.getSingleton().getAttackPlaner().isActive()) {
            DSWorkbenchMainFrame.getSingleton().getAttackPlaner().handleGesture(string);
        }*/ else if (DSWorkbenchRankFrame.getSingleton().isActive()) {
            DSWorkbenchRankFrame.getSingleton().handleGesture(string);
        }
    }

    @Override
    public void gestureMovementRecognized(String string) {
    }
}
