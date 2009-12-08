/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.dnd;

/**
 *
 * @author Jejkal
 */
public interface GhostDropListener {

    /**
     * Reimplement this method with what you want to be done on a drop event
     * @param e Event
     */
    public void ghostDropped(GhostDropEvent e);
}
