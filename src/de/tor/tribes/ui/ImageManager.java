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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
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
    public final static int CURSOR_RADAR = 23;
    public final static int CURSOR_CHURCH_1 = 24;
    public final static int CURSOR_CHURCH_2 = 25;
    public final static int CURSOR_CHURCH_3 = 26;
    public final static int CURSOR_REMOVE_CHURCH = 27;
    private static final int ID_NOTE_ICON_0 = 0;
    private static final int ID_NOTE_ICON_1 = 1;
    private static final int ID_NOTE_ICON_2 = 2;
    private static final int ID_NOTE_ICON_3 = 3;
    private static final int ID_NOTE_ICON_4 = 4;
    private static final List<Cursor> CURSORS = new LinkedList<Cursor>();
    private static final List<ImageIcon> CURSOR_IMAGES = new LinkedList<ImageIcon>();
    private static final List<BufferedImage> UNIT_IMAGES = new LinkedList<BufferedImage>();
    private static final List<ImageIcon> UNIT_ICONS = new LinkedList<ImageIcon>();
    private static final List<BufferedImage> NOTE_ICONS = new LinkedList<BufferedImage>();
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
            //misc cursors
            loadCursor("graphics/cursors/camera.png", "camera");
            loadCursor("graphics/cursors/support.png", "support");
            //draw cursors
            loadCursor("graphics/cursors/draw_line.png", "draw_line");
            loadCursor("graphics/cursors/draw_rect.png", "draw_rect");
            loadCursor("graphics/cursors/draw_circle.png", "draw_circle");
            loadCursor("graphics/cursors/draw_text.png", "draw_text");
            //new tools cursors
            loadCursor("graphics/cursors/selection.png", "selection");
            loadCursor("graphics/cursors/draw_freeform.png", "draw_freeform");
            loadCursor("graphics/cursors/radar.png", "radar");
            //church cursors
            loadCursor("graphics/cursors/church1.png", "church1");
            loadCursor("graphics/cursors/church2.png", "church2");
            loadCursor("graphics/cursors/church3.png", "church3");
            loadCursor("graphics/cursors/no_church.png", "church0");

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

    public static void loadNoteIcons() throws Exception {
        try {
            NOTE_ICONS.add(ImageIO.read(new File("src/res/marker.png")));//0
            NOTE_ICONS.add(ImageIO.read(new File("src/res/marker.png")));//1
            NOTE_ICONS.add(ImageIO.read(new File("src/res/marker.png")));//2
            NOTE_ICONS.add(ImageIO.read(new File("src/res/marker.png")));//3
            NOTE_ICONS.add(ImageIO.read(new File("src/res/marker.png")));//4
        } catch (Exception e) {
            logger.error("Failed to load note icons", e);
            throw new Exception("Failed to load note icons");
        }
    }

    public static BufferedImage getNoteIcon(int v) {
        return NOTE_ICONS.get(v);
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
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/spear.png")));//0
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/sword.png")));//1
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/axe.png")));//2
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/archer.png")));//3
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/spy.png")));//4
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/light.png")));//5
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/marcher.png")));//6
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/heavy.png")));//7
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/ram.png")));//8
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/cata.png")));//9
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/knight.png")));//10
            UNIT_IMAGES.add(ImageIO.read(new File("graphics/icons/snob.png")));//11

            for (BufferedImage i : UNIT_IMAGES) {
                UNIT_ICONS.add(new ImageIcon(i.getScaledInstance(i.getWidth(), i.getHeight(), BufferedImage.SCALE_DEFAULT)));
            }
        } catch (Exception e) {
            logger.error("Failed to load unit icons", e);
            throw new Exception("Failed to load unit icons");
        }
    }
    /*
     *  public final static int ICON_SPEAR = 0;
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
     */

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

    public static BufferedImage getUnitImage(int pId) {
        return getUnitImage(pId, true);
    }

    /**Get thr unit icon for the provided ID*/
    public static BufferedImage getUnitImage(int pId, boolean pChecked) {

        if (DataHolder.getSingleton().getUnits().size() == 9 && pChecked) {
            //old style
            switch (pId) {
                case 0:
                    return UNIT_IMAGES.get(0);
                case 1:
                    return UNIT_IMAGES.get(1);
                case 2:
                    return UNIT_IMAGES.get(2);
                case 3:
                    return UNIT_IMAGES.get(4);
                case 4:
                    return UNIT_IMAGES.get(5);
                case 5:
                    return UNIT_IMAGES.get(7);
                case 6:
                    return UNIT_IMAGES.get(8);
                case 7:
                    return UNIT_IMAGES.get(9);
                case 8:
                    return UNIT_IMAGES.get(11);
                default:
                    return null;
            }
        } else {
            return UNIT_IMAGES.get(pId);
        }
    }

    public static ImageIcon getUnitIcon(UnitHolder pUnit) {
        if (pUnit == null) {
            return null;
        }
        if (pUnit.getPlainName().equals("spear")) {
            return UNIT_ICONS.get(ICON_SPEAR);
        } else if (pUnit.getPlainName().equals("sword")) {
            return UNIT_ICONS.get(ICON_SWORD);
        } else if (pUnit.getPlainName().equals("axe")) {
            return UNIT_ICONS.get(ICON_AXE);
        } else if (pUnit.getPlainName().equals("archer")) {
            return UNIT_ICONS.get(ICON_ARCHER);
        } else if (pUnit.getPlainName().equals("spy")) {
            return UNIT_ICONS.get(ICON_SPY);
        } else if (pUnit.getPlainName().equals("light")) {
            return UNIT_ICONS.get(ICON_LKAV);
        } else if (pUnit.getPlainName().equals("marcher")) {
            return UNIT_ICONS.get(ICON_MARCHER);
        } else if (pUnit.getPlainName().equals("heavy")) {
            return UNIT_ICONS.get(ICON_HEAVY);
        } else if (pUnit.getPlainName().equals("ram")) {
            return UNIT_ICONS.get(ICON_RAM);
        } else if (pUnit.getPlainName().equals("catapult")) {
            return UNIT_ICONS.get(ICON_CATA);
        } else if (pUnit.getPlainName().equals("snob")) {
            return UNIT_ICONS.get(ICON_SNOB);
        } else if (pUnit.getPlainName().equals("knight")) {
            return UNIT_ICONS.get(ICON_KNIGHT);
        }
        //unknown unit
        return null;
    }

    public static BufferedImage getUnitImage(UnitHolder pUnit) {
        if (pUnit == null) {
            return null;
        }
        if (pUnit.getPlainName().equals("spear")) {
            return UNIT_IMAGES.get(ICON_SPEAR);
        } else if (pUnit.getPlainName().equals("sword")) {
            return UNIT_IMAGES.get(ICON_SWORD);
        } else if (pUnit.getPlainName().equals("axe")) {
            return UNIT_IMAGES.get(ICON_AXE);
        } else if (pUnit.getPlainName().equals("archer")) {
            return UNIT_IMAGES.get(ICON_ARCHER);
        } else if (pUnit.getPlainName().equals("spy")) {
            return UNIT_IMAGES.get(ICON_SPY);
        } else if (pUnit.getPlainName().equals("light")) {
            return UNIT_IMAGES.get(ICON_LKAV);
        } else if (pUnit.getPlainName().equals("marcher")) {
            return UNIT_IMAGES.get(ICON_MARCHER);
        } else if (pUnit.getPlainName().equals("heavy")) {
            return UNIT_IMAGES.get(ICON_HEAVY);
        } else if (pUnit.getPlainName().equals("ram")) {
            return UNIT_IMAGES.get(ICON_RAM);
        } else if (pUnit.getPlainName().equals("catapult")) {
            return UNIT_IMAGES.get(ICON_CATA);
        } else if (pUnit.getPlainName().equals("snob")) {
            return UNIT_IMAGES.get(ICON_SNOB);
        } else if (pUnit.getPlainName().equals("knight")) {
            return UNIT_IMAGES.get(ICON_KNIGHT);
        }
        //unknown unit
        return null;
    }
}
