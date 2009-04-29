/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

/**
 *
 * @author Charon
 */
public class ShortcutHandler implements AWTEventListener {

    public static final int ID_SCROLL_LEFT = 0;
    public static final int ID_SCROLL_UP = 1;
    public static final int ID_SCROLL_RIGHT = 2;
    public static final int ID_SCROLL_DOWN = 3;
    private static ShortcutHandler SINGLETON = null;

    public static synchronized ShortcutHandler getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ShortcutHandler();
        }
        return SINGLETON;
    }

    ShortcutHandler() {
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (((KeyEvent) event).getID() == KeyEvent.KEY_PRESSED) {
            KeyEvent e = (KeyEvent) event;
            System.out.println("Mods: " + e.getKeyModifiersText(e.getModifiers()));
            System.out.println("Keys: " + e.getKeyText(e.getKeyCode()));
        }
    }

    public static void main(String[] args) {
        //Toolkit.getDefaultToolkit().addAWTEventListener(ShortcutHandler.getSingleton(), AWTEvent.KEY_EVENT_MASK);
      
        while (true) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }
}
