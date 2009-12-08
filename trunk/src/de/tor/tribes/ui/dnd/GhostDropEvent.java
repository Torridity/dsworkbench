/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.ui.dnd;

import java.awt.Point;

/**
 *
 * @author Jejkal
 */
public class GhostDropEvent {

    private Point point;
    private String action;

    /**
     * Create a drop event
     * @param action String that represents the action
     * @param point Coordinates of where the drop occured
     */
    public GhostDropEvent(String action, Point point) {
        this.action = action;
        this.point = point;
    }

    /**
     * Get the name of the action of this event
     * @return action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Gives you the coordinates where the mouse was released (drop)
     * @return Point
     */
    public Point getDropLocation() {
        return point;
    }
}
