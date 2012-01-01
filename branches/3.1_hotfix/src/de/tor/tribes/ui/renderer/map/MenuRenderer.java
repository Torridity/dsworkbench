/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.ui.ClockFrame;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.ui.FormConfigFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.MinimapPanel;
import de.tor.tribes.ui.views.DSWorkbenchSearchFrame;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;

/**
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
            //0-8 map related
            mIcons.add(ImageIO.read(new File("./graphics/icons/notool.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/measure.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/mark.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/tag.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/support_tool.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/selection.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/radar.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/def.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/booty.png")));
            //9-15 attack related
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_axe.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_ram.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_snob.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_spy.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_light.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_heavy.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/attack_sword.png")));
            //16-18 minimap related
            mIcons.add(ImageIO.read(new File("./graphics/icons/move.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/zoom.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/camera.png")));
            //19-24 draw related
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_line.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_arrow.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_freeform.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_rect.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_circle.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/draw_text.png")));
            //25-28 church
            mIcons.add(ImageIO.read(new File("./graphics/icons/church1.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/church2.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/church3.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/no_church.png")));
            //29-32 misc
            mIcons.add(ImageIO.read(new File("./graphics/icons/search.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/settings.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/clock.png")));
            mIcons.add(ImageIO.read(new File("./graphics/icons/note.png")));
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
        int maxIconsX = 9;
        int menuW = maxIconsX * iconw + maxIconsX * space + 2 * space;
        int menuH = 6 * iconh + 6 * space + 2 * space;
        Dimension mapSize = MapPanel.getSingleton().getSize();
        if (menuLocation.x + menuW > mapSize.width) {
            menuLocation.move(mapSize.width - menuW, menuLocation.y);
        }
        if (menuLocation.y + menuH > mapSize.height) {
            menuLocation.move(menuLocation.x, mapSize.height - menuH);
        }
        g2d.setColor(Constants.DS_BACK);
        g2d.fill3DRect(menuLocation.x, menuLocation.y - 20, maxIconsX * iconw + (maxIconsX + 1) * space, 6 * iconh + 6 * space + 25, true);
        //map tools
        int pos = 0;
        int lastPos = 0;
        for (; pos < 9; pos++) {
            menuRegions.put(pos, new Rectangle(menuLocation.x + space + pos * iconw + pos * space, menuLocation.y + 5, iconw, iconh));
        }
        //attack tools
        lastPos = pos;
        for (; pos < 16; pos++) {
            menuRegions.put(pos, new Rectangle(menuLocation.x + space + (pos - lastPos) * iconw + (pos - lastPos) * space, menuLocation.y + space + iconh + space, iconw, iconh));
        }
        //minimap tools
        lastPos = pos;
        for (; pos < 19; pos++) {
            menuRegions.put(pos, new Rectangle(menuLocation.x + space + (pos - lastPos) * iconw + (pos - lastPos) * space, menuLocation.y + space + iconh + space + iconh + space, iconw, iconh));
        }
        //draw tools
        lastPos = pos;
        for (; pos < 25; pos++) {
            menuRegions.put(pos, new Rectangle(menuLocation.x + space + (pos - lastPos) * iconw + (pos - lastPos) * space, menuLocation.y + space + iconh + space + iconh + space + iconh + space, iconw, iconh));
        }
        //misc tools
        lastPos = pos;
        for (; pos < 29; pos++) {
            menuRegions.put(pos, new Rectangle(menuLocation.x + space + (pos - lastPos) * iconw + (pos - lastPos) * space, menuLocation.y + space + iconh + space + iconh + space + iconh + space + iconh + space, iconw, iconh));
        }
        lastPos = pos;
        for (; pos < 33; pos++) {
            menuRegions.put(pos, new Rectangle(menuLocation.x + space + (pos - lastPos) * iconw + (pos - lastPos) * space, menuLocation.y + space + iconh + space + iconh + space + iconh + space + iconh + space + iconh + space, iconw, iconh));
        }
        Enumeration<Integer> regions = menuRegions.keys();
        boolean showChurchTools = ServerSettings.getSingleton().isChurch();
        while (regions.hasMoreElements()) {

            Integer region = regions.nextElement();
            Rectangle rect = menuRegions.get(region);

            if (rect.contains(mouseLocation)) {
                g2d.setColor(Constants.DS_BACK);

                if (!showChurchTools && (region == 25 || region == 26 || region == 27 || region == 28)) {
                    g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
                } else {
                    g2d.fill3DRect(rect.x, rect.y, rect.width, rect.height, false);
                }
                g2d.setColor(Color.BLACK);
                String name = getToolName(region);
                g2d.drawString(name, menuLocation.x + space, menuLocation.y - 5);
            } else {
                if (!showChurchTools && (region == 25 || region == 26 || region == 27 || region == 28)) {
                    g2d.setColor(Constants.DS_BACK);
                    g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
                } else {
                    g2d.setColor(Constants.DS_BACK_LIGHT);
                    g2d.fill3DRect(rect.x, rect.y, rect.width, rect.height, true);
                }
            }
            g2d.drawImage(mIcons.get(region).getScaledInstance(rect.width, rect.height, BufferedImage.SCALE_SMOOTH), rect.x, rect.y, null);
        }
    }

    private String getToolName(int id) {
        boolean showChurchTools = ServerSettings.getSingleton().isChurch();
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
                return "Dörfer auf der Karte auswählen";
            }
            case 6: {
                try {
                    String v = GlobalOptions.getProperty("radar.size");
                    int m = Integer.parseInt(v);
                    int h = m / 60;
                    int min = m - h * 60;
                    return "Laufzeitradar (" + h + "h " + min + "min)";
                } catch (Exception e) {
                    return "Laufzeitradar";
                }
            }
            case 7: {
                return "Versammlungsplatz InGame öffnen";
            }
            case 8: {
                return "Marktplatz InGame öffnen";
            }
            case 9: {
                return "Angriff mit Axtkämpfer";
            }
            case 10: {
                return "Angriff mit Rammen";
            }
            case 11: {
                return "Angriff mit AG";
            }
            case 12: {
                return "Angriff mit Spähern";
            }
            case 13: {
                return "Angriff mit LKav";
            }
            case 14: {
                return "Angriff mit SKav";
            }
            case 15: {
                return "Angriff mit Schwertkämpfern";
            }
            case 16: {
                return "Ausschnitt der Minimap bewegen";
            }
            case 17: {
                return "Ausschnitt der Minimap zoomen";
            }
            case 18: {
                return "Foto der Minimap erstellen";
            }
            case 19: {
                return "Linie zeichnen";
            }
            case 20: {
                return "Pfeil zeichnen";
            }
            case 21: {
                return "Freihand zeichnen";
            }
            case 22: {
                return "Rechteck zeichnen";
            }
            case 23: {
                return "Kreis zeichnen";
            }
            case 24: {
                return "Text zeichnen";
            }
            case 25: {
                if (!showChurchTools) {
                    return "(Nicht verfügbar)";
                }
                return "Kirche (Stufe 1) erstellen";
            }
            case 26: {
                if (!showChurchTools) {
                    return "(Nicht verfügbar)";
                }
                return "Kirche (Stufe 2) erstellen";
            }
            case 27: {
                if (!showChurchTools) {
                    return "(Nicht verfügbar)";
                }
                return "Kirche (Stufe 3) erstellen";
            }
            case 28: {
                if (!showChurchTools) {
                    return "(Nicht verfügbar)";
                }
                return "Kirche entfernen";
            }
            case 29: {
                return "Suche öffnen";
            }
            case 30: {
                return "Einstellungen öffnen";
            }
            case 31: {
                return "Uhr öffnen";
            }
            case 32: {
                return "Notiz erstellen";
            }
        }
        return "";
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isVisible()) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                boolean showChurchTools = ServerSettings.getSingleton().isChurch();
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
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
                                break;
                            }
                            case 6: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_RADAR);
                                break;
                            }
                            case 7: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_INGAME);
                                break;
                            }
                            case 8: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SEND_RES_INGAME);
                                break;
                            }
                            case 9: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_AXE);
                                break;
                            }
                            case 10: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_RAM);
                                break;
                            }
                            case 11: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SNOB);
                                break;
                            }
                            case 12: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SPY);
                                break;
                            }
                            case 13: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
                                break;
                            }
                            case 14: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
                                break;
                            }
                            case 15: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
                                break;
                            }
                            case 16: {
                                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MOVE);
                                break;
                            }
                            case 17: {
                                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ZOOM);
                                break;
                            }
                            case 18: {
                                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SHOT);
                                break;
                            }
                            case 19: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_LINE);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Line.class);
                                break;
                            }
                            case 20: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_ARROW);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Arrow.class);
                                break;
                            }
                            case 21: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_FREEFORM);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.FreeForm.class);
                                break;
                            }
                            case 22: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_RECT);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Rectangle.class);
                                break;
                            }
                            case 23: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_CIRCLE);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Circle.class);
                                break;
                            }
                            case 24: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_TEXT);
                                FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.Text.class);
                                break;
                            }
                            case 25: {
                                if (showChurchTools) {
                                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_1);
                                } else {
                                    return;
                                }
                                break;
                            }
                            case 26: {
                                if (showChurchTools) {
                                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_2);
                                } else {
                                    return;
                                }
                                break;
                            }
                            case 27: {
                                if (showChurchTools) {
                                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_3);
                                } else {
                                    return;
                                }
                                break;
                            }
                            case 28: {
                                if (showChurchTools) {
                                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_REMOVE_CHURCH);
                                }
                                break;
                            }
                            case 29: {
                                DSWorkbenchSearchFrame.getSingleton().setVisible(true);
                                break;
                            }
                            case 30: {
                                DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
                                break;
                            }
                            case 31: {
                                ClockFrame.getSingleton().setVisible(true);
                                break;
                            }
                            case 32: {
                                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_NOTE);
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
