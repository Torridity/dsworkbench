/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestClass extends JPanel {

    public TestClass() {
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                repaint();
            }
        });
    }

    private void bigPaint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        Random r = new Random();

        for (int i = 0; i < 1000; i++) {
            g.drawOval(r.nextInt(getWidth() - 100), r.nextInt(getHeight() - 100), 100, 100);
            g.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255), r.nextInt(255)));
        }
    }
    private final GraphicsConfiguration gfxConf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage offImg;

    @Override
    protected void paintComponent(Graphics g) {
        if (offImg == null || offImg.getWidth() != getWidth() || offImg.getHeight() != getHeight()) {
            offImg = gfxConf.createCompatibleImage(getWidth(), getHeight());
            bigPaint(offImg.createGraphics());
        }
        Point p = getMousePosition();
        if (p != null) {
            g.drawImage(offImg, p.x, p.y, this);
        }
        // bigPaint( g );
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 600);
        f.add(new TestClass());
        f.setVisible(true);
    }
}
