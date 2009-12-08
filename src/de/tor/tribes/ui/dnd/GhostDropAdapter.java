/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.ui.dnd;

import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

public class GhostDropAdapter extends MouseAdapter {

    protected GhostGlassPane glassPane;
    protected String action;
    private List<GhostDropListener> listeners;

    /**
     * Instantiate a GhostDropAdapter
     * @param glassPane a glasspane where the transparent drag and drop is drawn
     * @param action TODO !? remove?
     */
    public GhostDropAdapter(GhostGlassPane glassPane, String action) {
        this.glassPane = glassPane;
        this.action = action;
        this.listeners = new ArrayList<GhostDropListener>();
    }

    /**
     * Add event listener to the list of listeners (observer pattern)
     * @param listener to add to listeners list
     */
    public void addGhostDropListener(GhostDropListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Event listener to remove to the list of listeners (observer pattern)
     * @param listener to remove to the listeners list
     */
    public void removeGhostDropListener(GhostDropListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Will notify listeners (observer pattern)
     * @param evt Event
     */
    protected void fireGhostDropEvent(GhostDropEvent evt) {
        for (GhostDropListener listener : listeners) {
            listener.ghostDropped(evt);
        }
    }
}
