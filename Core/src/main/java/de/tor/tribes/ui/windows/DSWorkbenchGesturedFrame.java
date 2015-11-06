/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.windows;

import javax.swing.JFrame;

/**
 *
 * @author Torridity
 */
public abstract class DSWorkbenchGesturedFrame extends JFrame {

    public final static int ID_UNKNOWN = -1;
    public final static int ID_CLOSE = 0;
    public final static int ID_TO_BACKGROUND = 1;
    public final static int ID_NEXT_PAGE = 2;
    public final static int ID_PREVIOUS_PAGE = 3;
    public final static int ID_RENAME = 4;
    public final static int ID_EXPORT_BB = 5;
    public final static int ID_EXPORT_PLAIN = 6;

    public boolean handleGesture(String pGesture) {
        if (pGesture == null) {
            return false;
        }

        if (pGesture.equals("DR")) {
            fireCloseGestureEvent();
        } else if (pGesture.equals("DL")) {
            fireToBackgroundGestureEvent();
        } else if (pGesture.equals("R")) {
            fireNextPageGestureEvent();
        } else if (pGesture.equals("L")) {
            firePreviousPageGestureEvent();
        } else if (pGesture.equals("UR")) {
            fireExportAsBBGestureEvent();
        } else if (pGesture.equals("UL")) {
            firePlainExportGestureEvent();
        } else if (pGesture.equals("RDLUR")) {
            fireRenameGestureEvent();
        } else {
            return false;
        }

        return true;
    }

    /**v ->*/
    public abstract void fireCloseGestureEvent();

    /**v <-*/
    public abstract void fireToBackgroundGestureEvent();

    /**->*/
    public abstract void fireNextPageGestureEvent();

    /**<-*/
    public abstract void firePreviousPageGestureEvent();

    /**-> v <- ^*/
    public abstract void fireRenameGestureEvent();

    /**^ ->*/
    public abstract void fireExportAsBBGestureEvent();

    /**^ <-*/
    public abstract void firePlainExportGestureEvent();
}
