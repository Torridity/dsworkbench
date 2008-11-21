/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Charon
 */
public class ToolsRenderer {

    private final List<BufferedImage> mToolsImages = new LinkedList<BufferedImage>();
    //private final Hashtable<BufferedImage, List<BufferedImage>> mMenu = new Hashtable<BufferedImage, List<BufferedImage>>();
    private final BufferedImage[] mMenuImages = new BufferedImage[3];

    public ToolsRenderer() {
        try {
            mMenuImages[0] = ImageIO.read(new File("./graphics/menu_left.png"));
            mMenuImages[1] = ImageIO.read(new File("./graphics/menu_center.png"));
            mMenuImages[2] = ImageIO.read(new File("./graphics/menu_right.png"));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/map_tools.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/minimap_tools.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_tools.png")));
            mToolsImages.add(ImageIO.read(new File("./graphics/icons/general_tools.png")));

            mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_axe.png")));
        mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_ram.png")));
        mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_snob.png")));
        mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_spy.png")));
        mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_light.png")));
        mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_heavy.png")));
        mToolsImages.add(ImageIO.read(new File("./graphics/icons/attack_sword.png")));
            
            
        /*
        List<BufferedImage> mapSubMenu = new LinkedList<BufferedImage>();
        mMenu.put(ImageIO.read(new File("./graphics/icons/map_tools.png")), mapSubMenu);
        mapSubMenu.add(ImageIO.read(new File("./graphics/icons/measure.png")));
        mapSubMenu.add(ImageIO.read(new File("./graphics/icons/mark.png")));
        mapSubMenu.add(ImageIO.read(new File("./graphics/icons/tag.png")));
        mapSubMenu.add(ImageIO.read(new File("./graphics/icons/def.png")));
        mapSubMenu.add(ImageIO.read(new File("./graphics/icons/booty.png")));
        
        List<BufferedImage> miniMapSubMenu = new LinkedList<BufferedImage>();
        mMenu.put(ImageIO.read(new File("./graphics/icons/minimap_tools.png")), miniMapSubMenu);
        miniMapSubMenu.add(ImageIO.read(new File("./graphics/icons/move.png")));
        miniMapSubMenu.add(ImageIO.read(new File("./graphics/icons/zoom.png")));
        miniMapSubMenu.add(ImageIO.read(new File("./graphics/icons/camera.png")));
        List<BufferedImage> attackSubMenu = new LinkedList<BufferedImage>();
        mMenu.put(ImageIO.read(new File("./graphics/icons/attack_tools.png")), attackSubMenu);
        attackSubMenu.add(ImageIO.read(new File("./graphics/icons/attack_axe.png")));
        attackSubMenu.add(ImageIO.read(new File("./graphics/icons/attack_ram.png")));
        attackSubMenu.add(ImageIO.read(new File("./graphics/icons/attack_snob.png")));
        attackSubMenu.add(ImageIO.read(new File("./graphics/icons/attack_spy.png")));
        attackSubMenu.add(ImageIO.read(new File("./graphics/icons/attack_light.png")));
        attackSubMenu.add(ImageIO.read(new File("./graphics/icons/attack_heavy.png")));
        attackSubMenu.add(ImageIO.read(new File("./graphics/icons/attack_sword.png")));
        List<BufferedImage> miscSubMenu = new LinkedList<BufferedImage>();
        mMenu.put(ImageIO.read(new File("./graphics/icons/general_tools.png")), miscSubMenu);
        miscSubMenu.add(ImageIO.read(new File("./graphics/icons/search.png")));
        miscSubMenu.add(ImageIO.read(new File("./graphics/icons/settings.png")));
        miscSubMenu.add(ImageIO.read(new File("./graphics/icons/clock.png")));
         */
        } catch (Exception e) {
        }
    }

    public void render(Graphics2D g2d) {
        int w = MapPanel.getSingleton().getWidth();
        int h = MapPanel.getSingleton().getHeight();
        /*        BufferedImage fade = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) fade.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);
        Composite c = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.drawImage(fade, 0, 0, null);
        g2d.setComposite(c);
         */
        int cnt = 0;
        g2d.drawImage(mMenuImages[0], 0, h - 40, null);
        for(BufferedImage i : mToolsImages){
            g2d.drawImage(mMenuImages[1], 20 + cnt * 40, h - 40, null);
            g2d.drawImage(i, 20 + cnt * 40 + 5, h - 40 + 5, null);
            cnt++;
        }
        g2d.drawImage(mMenuImages[2], 20 + cnt * 40, h - 40, null);
    }
}
