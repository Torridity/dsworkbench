/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/**Class for loading and holding all cursors needed for DS Workbench
 * @author Jejkal
 */
public class ImageManager {

    private static Logger logger = Logger.getLogger(ImageManager.class);

    //mappanel default
    public final static int CURSOR_DEFAULT = 0;
    public final static int CURSOR_MARK = 1;
    public final static int CURSOR_MEASURE = 2;
    public final static int CURSOR_TAG = 3;
    public final static int CURSOR_ATTACK_INGAME = 4;
    public final static int CURSOR_SEND_RES_INGAME = 5;
    //mappanel attack
    public final static int CURSOR_ATTACK_RAM = 6;
    public final static int CURSOR_ATTACK_AXE = 7;
    public final static int CURSOR_ATTACK_SNOB = 8;
    public final static int CURSOR_ATTACK_SPY = 9;
    public final static int CURSOR_ATTACK_SWORD = 10;
    public final static int CURSOR_ATTACK_LIGHT = 11;
    public final static int CURSOR_ATTACK_HEAVY = 12;
    //unit icons
    public final static int ICON_SPEAR = 0;
    public final static int ICON_SWORD = 1;
    public final static int ICON_AXE = 2;
    public final static int ICON_ARCHER = 3;
    public final static int ICON_SPY = 4;
    public final static int ICON_LKAV = 5;
    public final static int ICON_MARCHER = 6;
    public final static int ICON_HEAVY = 7;
    public final static int ICON_RAM = 8;
    public final static int ICON_CATA = 9;
    public final static int ICON_KNIGHT = 10;
    public final static int ICON_SNOB = 11;
    //minimap
    public final static int CURSOR_MOVE = 13;
    public final static int CURSOR_ZOOM = 14;
    public final static int CURSOR_SHOT = 15;
    private static final List<Cursor> CURSORS = new LinkedList<Cursor>();
    private static final List<ImageIcon> UNIT_ICONS = new LinkedList<ImageIcon>();

    /**Load the list of cursors*/
    public static void loadCursors() throws Exception {
        try {
            //default map panel cursors 
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/default.png"), new Point(0, 0), "default"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/mark.gif"), new Point(0, 0), "mark"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/measure.png"), new Point(0, 0), "measure"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/tag.png"), new Point(0, 0), "tag"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_ingame.png"), new Point(0, 0), "attack_ingame"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/res_ingame.png"), new Point(0, 0), "res_ingame"));
            //map panel cursors for attack purposes
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_ram.png"), new Point(0, 0), "attack_ram"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_axe.png"), new Point(0, 0), "attack_axe"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_snob.png"), new Point(0, 0), "attack_snob"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_spy.png"), new Point(0, 0), "attack_spy"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_sword.png"), new Point(0, 0), "attack_sword"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_light.png"), new Point(0, 0), "attack_light"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/attack_heavy.png"), new Point(0, 0), "attack_heavy"));
            //minimap cursors
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/move.png"), new Point(0, 0), "move"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/zoom.png"), new Point(0, 0), "zoom"));
            CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("graphics/cursors/camera.png"), new Point(0, 0), "shot"));
        } catch (Exception e) {
            logger.error("Failed to load cursor images", e);
            throw new Exception("Failed to load cursors");
        }
    }

    /**Get the cursor for the provided ID*/
    public static Cursor getCursor(int pID) {
        return CURSORS.get(pID);
    }

    /**Load the icons of the units used for the animated unit movement on the MapPanel*/
    public static void loadUnitIcons() throws Exception {
        try {
            UNIT_ICONS.add(new ImageIcon("graphics/icons/spear.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/sword.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/axe.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/archer.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/spy.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/light.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/marcher.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/heavy.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/ram.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/cata.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/knight.png"));
            UNIT_ICONS.add(new ImageIcon("graphics/icons/snob.png"));
        } catch (Exception e) {
            logger.error("Failed to load unit icons", e);
            throw new Exception("Failed to load unit icons");
        }
    }

    /**Get thr unit icon for the provided ID*/
    public static ImageIcon getUnitIcon(int pId) {
        return UNIT_ICONS.get(pId);
    }
}
