/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.jdesktop.swingx.JXLabel;

/**
 *
 * @author Torridity
 */
public class CollapseExpandTrigger extends JXLabel {

    BufferedImage back = null;

    public CollapseExpandTrigger() {
        try {
            back = ImageIO.read(CollapseExpandTrigger.class.getResource("/res/ui/knob.png"));
        } catch (IOException ioe) {
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int pos = w / 2;

        Graphics2D g2d = (Graphics2D) g;
        for (int y = 0; y < getHeight(); y += 20) {
            g2d.drawImage(back, null, pos - 2, y);
        }
        g2d.dispose();

    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.getContentPane().setLayout(new BorderLayout());
        f.add(new CollapseExpandTrigger(), BorderLayout.CENTER);
        f.setSize(100, 100);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
