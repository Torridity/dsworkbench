/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.dnd;

import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import javax.swing.JPanel;

public class GhostGlassPane extends JPanel {

    private AlphaComposite composite;
    private Image dragged = null;
    private Point location = new Point(0, 0);

    public GhostGlassPane() {
        setOpaque(false);
        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        this.dragged = GlobalOptions.getSkin().getImage(Skin.ID_V6, 1.0);
    }

   /* public void setImage(BufferedImage dragged) {
        this.dragged = dragged;
    }*/

    public void setPoint(Point location) {
        this.location = location;
    }

    public void paintComponent(Graphics g) {
        if (dragged == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(composite);
        g2.drawImage(dragged, (int) (location.getX() - (dragged.getWidth(this) / 2)), (int) (location.getY() - (dragged.getHeight(this) / 2)), null);
    }
}

