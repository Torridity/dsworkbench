/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.interfaces;

import java.awt.Point;

/**
 *
 * @author Jejkal
 */
public interface VillageSelectionListener {

    public void fireSelectionFinishedEvent(Point vStart, Point vEnd);
}
