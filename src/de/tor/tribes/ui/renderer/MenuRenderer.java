/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Line;
import de.tor.tribes.ui.ClockFrame;
import de.tor.tribes.ui.DSWorkbenchSettingsDialog;
import de.tor.tribes.ui.FormConfigFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.MinimapPanel;
import de.tor.tribes.ui.SearchFrame;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
public class MenuRenderer implements MouseListener, MouseMotionListener {

    private static MenuRenderer SINGLETON = null;
    private List<BufferedImage> mIcons = null;
    private Point menuLocation = null;
    private Point mouseLocation = null;
    private Hashtable<Integer, Rectangle> menuRegions = null;
    private boolean isVisible = false;

    public static synchronized MenuRenderer getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MenuRenderer();
        }
        return SINGLETON;
    }

    MenuRenderer() {
        try {
            mIcons = new LinkedList<BufferedImage>();
            //0-6 map related
            mIcons.add(ImageIO.read(new File("./graphics/icons/notool.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/measure.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/mark.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/tag.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/support_tool.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/def.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/booty.png")));
            //7-13 attack related
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_axe.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_ram.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_snob.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_spy.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_light.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_heavy.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_sword.png")));
            //14-16 minimap related
            mIcons.add(ImageIO.read(new File("./graphics/icons/move.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/zoom.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/camera.png")));
            //17-20 minimap related
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_line.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_rect.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_circle.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_text.png")));
            //21-23 misc
            mIcons.add(ImageIO.read(new File("./graphics/icons/search.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/settings.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/clock.png")));
        } catch (Exception e) {
        }
        menuRegions = new Hashtable<Integer, Rectangle>();
        mouseLocation = new Point(0, 0);
        menuLocation = new Point(0, 0);
    }

    public void switchVisibility() {
        isVisible = !isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setMenuLocation(int x, int y) {
        menuLocation = new Point(x, y);
    }

    public void renderMenu(Graphics2D g2d) {
        if (!isVisible) {
            return;
        }
        //size of each icon
        int iconh = 30;
        int iconw = iconh;
        int space = 5;
        g2d.setColor(Constants.DS_BACK);
        g2d.fill3DRect(menuLocation.x, menuLocation.y - 20, 7 * iconw + 8 * space, 5 * iconh + 5 * space + 25, true);

        for (int i = 0; i < 7; i++) {
            menuRegions.put(i, new Rectangle(menuLocation.x + space + i * iconw + i * space, menuLocation.y + 5, iconw, iconh));
        }

        for (int i = 7; i < 14; i++) {
            menuRegions.put(i, new Rectangle(menuLocation.x + space + (i - 7) * iconw + (i - 7) * space, menuLocation.y + space + iconh + space, iconw, iconh));
        }

        for (int i = 14; i < 17; i++) {
            menuRegions.put(i, new Rectangle(menuLocation.x + space + (i - 14) * iconw + (i - 14) * space, menuLocation.y + space + iconh + space + iconh + space, iconw, iconh));
        }
        for (int i = 17; i < 21; i++) {
            menuRegions.put(i, new Rectangle(menuLocation.x + space + (i - 17) * iconw + (i - 17) * space, menuLocation.y + space + iconh + space + iconh + space + iconh + space, iconw, iconh));
        }

        for (int i = 21; i < 24; i++) {
            menuRegions.put(i, new Rectangle(menuLocation.x + space + (i - 21) * iconw + (i - 21) * space, menuLocation.y + space + iconh + space + iconh + space + iconh + space + iconh + space, iconw, iconh));
        }

        Enumeration<Integer> regions = menuRegions.keys();
        while (regions.hasMoreElements()) {
            Integer region = regions.nextElement();
            Rectangle rect = menuRegions.get(region);

            if (rect.contains(mouseLocation)) {
                g2d.setColor(Constants.DS_BACK);
                g2d.fill3DRect(rect.x, rect.y, rect.width, rect.height, false);
                g2d.setColor(Color.BLACK);
                String name = getToolName(region);
                g2d.drawString(name, menuLocation.x + space, menuLocation.y - 5);
            } else {
                g2d.setColor(Constants.DS_BACK_LIGHT);
                g2d.fill3DRect(rect.x, rect.y, rect.width, rect.height, true);
            }

            g2d.drawImage(mIcons.get(region).getScaledInstance(rect.width, rect.height, BufferedImage.SCALE_SMOOTH), rect.x, rect.y, null);
        }
    }

    private String getToolName(int id) {
        switch (id) {
            case 0: {
                return "Kein Werkzeug";
            }
            case 1: {
                return "Entfernungen messen";
            }
            case 2: {
                return "Dörfer markieren";
            }
            case 3: {
                return "Dörfer mit Tags versehen";
            }
            case 4: {
                return "Unterstützungsrechner";
            }
            case 5: {
                return "Versammlungsplatz InGame öffnen";
            }
            case 6: {
                return "Marktplatz InGame öffnen";
            }
            case 7: {
                return "Angriff mit Axtkämpfer";
            }
            case 8: {
                return "Angriff mit Rammen";
            }
            case 9: {
                return "Angriff mit AG";
            }
            case 10: {
                return "Angriff mit Spähern";
            }
            case 11: {
                return "Angriff mit LKav";
            }
            case 12: {
                return "Angriff mit SKav";
            }
            case 13: {
                return "Angriff mit Schwertkämpfern";
            }
            case 14: {
                return "Ausschnitt der Minimap bewegen";
            }
            case 15: {
                return "Ausschnitt der Minimap zoomen";
            }
            case 16: {
                return "Foto der Minimap erstellen";
            }
            case 17: {
                return "Linie zeichnen";
            }
            case 18: {
                return "Rechteck zeichnen";
            }
            case 19: {
                return "Kreis zeichnen";
            }
            case 20: {
                return "Text zeichnen";
            }
            case 21: {
                return "Suche öffnen";
            }
            case 22: {
                return "Einstellungen öffnen";
            }
            case 23: {
                return "Uhr öffnen";
            }
        }
        return "";
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isVisible()) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Enumeration<Integer> regions = menuRegions.keys();
                while (regions.hasMoreElements()) {
                    Integer region = regions.nextElement();
                    Rectangle rect = menuRegions.get(region);
                    if (rect.contains(new Point(e.getX(), e.getY()))) {
                        switch (region) {
                            case 0: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
                                break;
                            }
                            case 1: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MEASURE);
                                break;
                            }
                            case 2: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MARK);
                                break;
                            }
                            case 3: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_TAG);
                                break;
                            }
                            case 4: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SUPPORT);
                                break;
                            }
                            case 5: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_INGAME);
                                break;
                            }
                            case 6: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SEND_RES_INGAME);
                                break;
                            }
                            case 7: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_AXE);
                                break;
                            }
                            case 8: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_RAM);
                                break;
                            }
                            case 9: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SNOB);
                                break;
                            }
                            case 10: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SPY);
                                break;
                            }
                            case 11: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
                                break;
                            }
                            case 12: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
                                break;
                            }
                            case 13: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
                                break;
                            }
                            case 14: {
                                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MOVE);
                                break;
                            }
                            case 15: {
                                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ZOOM);
                                break;
                            }
                            case 16: {
                                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SHOT);
                                break;
                            }
                            case 17: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_LINE);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Line.class);
                                break;
                            }
                            case 18: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_RECT);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Rectangle.class);
                                break;
                            }
                            case 19: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_CIRCLE);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Circle.class);
                                break;
                            }
                            case 20: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_TEXT);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Text.class);
                                break;
                            }
                            case 21: {
                                SearchFrame.getSingleton().setVisible(true);
                                break;
                            }
                            case 22: {
                                DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
                                break;
                            }
                            case 23: {
                                ClockFrame.getSingleton().setVisible(true);
                                break;
                            }
                        }
                        //hide menu
                        isVisible = false;
                    }
                }
            }
        }//else ignore mouse
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseLocation = new Point(e.getX(), e.getY());
    }
}
