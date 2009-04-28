/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

/**
 *
 * @author Charon
 */
public class ShortcutHandler implements AWTEventListener {

//public static final int ID_SCROLL
    private static ShortcutHandler SINGLETON = null;

    public static synchronized ShortcutHandler getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ShortcutHandler();
        }
        return SINGLETON;
    }

    public ShortcutHandler() {
    }

    @Override
    public void eventDispatched(AWTEvent event) {

        if (((KeyEvent) event).getID() == KeyEvent.KEY_PRESSED) {
            KeyEvent e = (KeyEvent) event;

            


        }
    }
}
