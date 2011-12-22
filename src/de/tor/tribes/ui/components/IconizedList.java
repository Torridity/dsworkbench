/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.components;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.jdesktop.swingx.JXList;

/**
 *
 * @author jejkal
 */
public class IconizedList extends JXList {

    private Image iconImage = null;

    public IconizedList(String pResourcePath) {
        try {
            BufferedImage b = ImageIO.read(IconizedList.class.getResource(pResourcePath));
            iconImage = b.getScaledInstance(80, 80, BufferedImage.SCALE_SMOOTH);
        } catch (Exception e) {
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (iconImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            Composite c = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f));
            g2d.drawImage(iconImage, getWidth() - 80, 0, null);
            g2d.setComposite(c);
        }
    }
}
