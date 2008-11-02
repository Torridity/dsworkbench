/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Charon
 */
public class ToolsRenderer {

    private final List<BufferedImage> mToolsImages = new LinkedList<BufferedImage>();

    public ToolsRenderer() {
        try {
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/tools.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/notool.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/measure.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/mark.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/def.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/booty.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_axe.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_ram.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_snob.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_spy.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_light.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_heavy.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_sword.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/move.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/zoom.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/camera.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/search.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/settings.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/clock.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/tag.png")));
        } catch (Exception e) {
        }
    }

    public void render(Graphics2D g2d) {
        int w = MapPanel.getSingleton().getWidth();
        int h = MapPanel.getSingleton().getHeight();
        BufferedImage fade = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) fade.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);
        Composite c = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.drawImage(fade, 0, 0, null);
        g2d.setComposite(c);
        double deg = 360 / mToolsImages.size();
        int cnt = 0;
        for (BufferedImage image : mToolsImages) {
            int xv = (int) Math.rint(200 + 150 * Math.cos(2 * Math.PI * cnt * deg / 360));
            int yv = (int) Math.rint(200 + 150 * Math.sin(2 * Math.PI * cnt * deg / 360));
            g2d.drawImage(image.getScaledInstance(50, 50, BufferedImage.SCALE_SMOOTH), xv, yv, null);
            cnt++;
        }
    }
}
