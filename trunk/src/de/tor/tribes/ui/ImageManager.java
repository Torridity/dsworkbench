/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import java.awt.Cursor;
import java.awt.Image;
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

    private static Logger logger = Logger.getLogger("TextureManager");    //mappanel default
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
    public final static int CURSOR_SUPPORT = 16;
    public final static int CURSOR_DRAW_LINE = 17;
    public final static int CURSOR_DRAW_RECT = 18;
    public final static int CURSOR_DRAW_CIRCLE = 19;
    public final static int CURSOR_DRAW_TEXT = 20;
    public final static int CURSOR_SELECTION = 21;
    public final static int CURSOR_DRAW_FREEFORM = 22;
    private static final List<Cursor> CURSORS = new LinkedList<Cursor>();
    private static final List<ImageIcon> CURSOR_IMAGES = new LinkedList<ImageIcon>();
    private static final List<ImageIcon> UNIT_ICONS = new LinkedList<ImageIcon>();
    private static boolean cursorSupported = true;

    /**Load the list of cursors*/
    public static void loadCursors() throws Exception {
        try {
            //default map panel cursors 
            loadCursor("graphics/cursors/default.png", "default");
            loadCursor("graphics/cursors/mark.png", "mark");
            loadCursor("graphics/cursors/measure.png", "measure");
            loadCursor("graphics/cursors/tag.png", "tag");
            loadCursor("graphics/cursors/attack_ingame.png", "attack_ingame");
            loadCursor("graphics/cursors/res_ingame.png", "res_ingame");
            //map panel cursors for attack purposes 
            loadCursor("graphics/cursors/attack_ram.png", "attack_ram");
            loadCursor("graphics/cursors/attack_axe.png", "attack_axe");
            loadCursor("graphics/cursors/attack_snob.png", "attack_snob");
            loadCursor("graphics/cursors/attack_spy.png", "attack_spy");
            loadCursor("graphics/cursors/attack_sword.png", "attack_sword");
            loadCursor("graphics/cursors/attack_light.png", "attack_light");
            loadCursor("graphics/cursors/attack_heavy.png", "attack_heavy");
            //minimap cursors
            loadCursor("graphics/cursors/move.png", "move");
            loadCursor("graphics/cursors/zoom.png", "zoom");
            loadCursor("graphics/cursors/camera.png", "camera");
            loadCursor("graphics/cursors/support.png", "support");
            loadCursor("graphics/cursors/draw_line.png", "draw_line");
            loadCursor("graphics/cursors/draw_rect.png", "draw_rect");
            loadCursor("graphics/cursors/draw_circle.png", "draw_circle");
            loadCursor("graphics/cursors/draw_text.png", "draw_text");
            loadCursor("graphics/cursors/selection.png", "selection");
            loadCursor("graphics/cursors/draw_freeform.png", "draw_freeform");
        } catch (Exception e) {
            logger.error("Failed to load cursor images", e);
            throw new Exception("Failed to load cursors");
        }
        if (Toolkit.getDefaultToolkit().getMaximumCursorColors() < 16) {
            logger.warn("Insufficient color depth for custom cursors on current platform. Setting sytem-cursor mode.");
            cursorSupported = false;
            return;
        }
    }

    private static void loadCursor(String pImagePath, String pName) {
        Image im = Toolkit.getDefaultToolkit().getImage(pImagePath);
        CURSORS.add(Toolkit.getDefaultToolkit().createCustomCursor(im, new Point(0, 0), pName));
        CURSOR_IMAGES.add(new ImageIcon(im));
    }

    /**Get the cursor for the provided ID*/
    public static Cursor getCursor(int pID) {
        if (!cursorSupported) {
            return Cursor.getDefaultCursor();
        }
        return CURSORS.get(pID);
    }

    /**Get the cursor for the provided ID*/
    public static ImageIcon getCursorImage(int pID) {
        return CURSOR_IMAGES.get(pID);
    }

    /**Load the icons of the units used for the animated unit movement on the MapPanel*/
    public static void loadUnitIcons() throws Exception {
        try {
            UNIT_ICONS.add(new ImageIcon("graphics/icons/spear.png"));//0
            UNIT_ICONS.add(new ImageIcon("graphics/icons/sword.png"));//1
            UNIT_ICONS.add(new ImageIcon("graphics/icons/axe.png"));//2
            UNIT_ICONS.add(new ImageIcon("graphics/icons/archer.png"));//3
            UNIT_ICONS.add(new ImageIcon("graphics/icons/spy.png"));//4
            UNIT_ICONS.add(new ImageIcon("graphics/icons/light.png"));//5
            UNIT_ICONS.add(new ImageIcon("graphics/icons/marcher.png"));//6
            UNIT_ICONS.add(new ImageIcon("graphics/icons/heavy.png"));//7
            UNIT_ICONS.add(new ImageIcon("graphics/icons/ram.png"));//8
            UNIT_ICONS.add(new ImageIcon("graphics/icons/cata.png"));//9
            UNIT_ICONS.add(new ImageIcon("graphics/icons/knight.png"));//10
            UNIT_ICONS.add(new ImageIcon("graphics/icons/snob.png"));//11
        } catch (Exception e) {
            logger.error("Failed to load unit icons", e);
            throw new Exception("Failed to load unit icons");
        }
    }

    /**Get thr unit icon for the provided ID*/
    public static ImageIcon getUnitIcon(int pId) {
        if (DataHolder.getSingleton().getUnits().size() == 9) {
            //old style
            switch (pId) {
                case 0:
                    return UNIT_ICONS.get(0);
                case 1:
                    return UNIT_ICONS.get(1);
                case 2:
                    return UNIT_ICONS.get(2);
                case 3:
                    return UNIT_ICONS.get(4);
                case 4:
                    return UNIT_ICONS.get(5);
                case 5:
                    return UNIT_ICONS.get(7);
                case 6:
                    return UNIT_ICONS.get(8);
                case 7:
                    return UNIT_ICONS.get(9);
                case 8:
                    return UNIT_ICONS.get(11);
                default:
                    return null;
            }
        } else {
            return UNIT_ICONS.get(pId);
        }
    }

    public static ImageIcon getUnitIcon(UnitHolder pUnit) {
        if (pUnit == null) {
            return null;
        }
        if (pUnit.getName().equals("Speerträger")) {
            return UNIT_ICONS.get(ICON_SPEAR);
        } else if (pUnit.getName().equals("Schwertkämpfer")) {
            return UNIT_ICONS.get(ICON_SWORD);
        } else if (pUnit.getName().equals("Axtkämpfer")) {
            return UNIT_ICONS.get(ICON_AXE);
        } else if (pUnit.getName().equals("Bogenschütze")) {
            return UNIT_ICONS.get(ICON_ARCHER);
        } else if (pUnit.getName().equals("Späher")) {
            return UNIT_ICONS.get(ICON_SPY);
        } else if (pUnit.getName().equals("Leichte Kavallerie")) {
            return UNIT_ICONS.get(ICON_LKAV);
        } else if (pUnit.getName().equals("Berittener Bogenschütze")) {
            return UNIT_ICONS.get(ICON_MARCHER);
        } else if (pUnit.getName().equals("Schwere Kavallerie")) {
            return UNIT_ICONS.get(ICON_HEAVY);
        } else if (pUnit.getName().equals("Ramme")) {
            return UNIT_ICONS.get(ICON_RAM);
        } else if (pUnit.getName().equals("Katapult")) {
            return UNIT_ICONS.get(ICON_CATA);
        } else if (pUnit.getName().equals("Adelsgeschlecht")) {
            return UNIT_ICONS.get(ICON_SNOB);
        } else if (pUnit.getName().equals("Paladin")) {
            return UNIT_ICONS.get(ICON_KNIGHT);
        }
        //unknown unit
        return null;
    }
}
