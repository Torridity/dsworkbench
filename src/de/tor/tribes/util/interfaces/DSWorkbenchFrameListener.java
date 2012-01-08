/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.interfaces;

import javax.swing.JFrame;

/**
 *
 * @author Charon
 */
public interface DSWorkbenchFrameListener {

    public void fireVisibilityChangedEvent(JFrame pFrame, boolean v);
}
