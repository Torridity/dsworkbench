/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.ui.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jejkal
 */
public class GhostMotionAdapter extends MouseMotionAdapter {

    private GhostGlassPane glassPane;

    /**
     * Public constructor
     * @param glassPane Glasspane on which you want the adapter to tell it where to paint the drag effect
     */
    public GhostMotionAdapter(GhostGlassPane glassPane) {
        this.glassPane = glassPane;
    }

    public void mouseDragged(MouseEvent e) {
        Component c = e.getComponent();
        Point p = (Point) e.getPoint().clone();
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, glassPane);
        glassPane.setPoint(p);
        glassPane.repaint();
    }
}
