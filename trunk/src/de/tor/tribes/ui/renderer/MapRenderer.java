/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.FormConfigFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.PatchFontMetrics;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.map.FormManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/**Map Renderer which supports "dirty layers" defining which layer has to be redrawn.<BR/>
 * Layer order with z-ID:<BR/>
 * 0: Marker Layer -> redraw after marker changes (triggered by MarkerManager)
 * 1: Village Layer: Redraw on global events (e.g. init or resize) or after changes on scaling or skin
 * 2: Misc. Basic Map Decoration: e.g. Sector or continent drawing on demand (triggered by SettingsDialog)
 * 3: Attack Vector Layer: Redraw after attack changes (triggered by AttackManager)
 * 4: Misc. Extended Map Decoration: e.g. troop qualification or active village marker
 * 5: Live Layer: Redraw in every drawing cycle e.g. Drag line, tool popup(?), (troop movement?)
 * 6-16: Free assignable
 * @author Charon
 */
/**Thread for updating after scroll operations
 */
public class MapRenderer extends Thread {

    private static Logger logger = Logger.getLogger("MapRenderer");
    public static final int ALL_LAYERS = 0;
    public static final int MAP_LAYER = 1;
    public static final int MARKER_LAYER = 2;
    public static final int BASIC_DECORATION_LAYER = 3;
    public static final int ATTACK_LAYER = 4;
    public static final int EXTENDED_DECORATION_LAYER = 5;
    public static final int LIVE_LAYER = 6;
    private boolean mapRedrawRequired = true;
    private Village[][] mVisibleVillages = null;
    private Hashtable<Village, Rectangle> villagePositions = null;
    private Hashtable<Integer, BufferedImage> mLayers = null;
    private int iVillagesX = 0;
    private int iVillagesY = 0;
    private int iCenterX = 500;
    private int iCenterY = 500;
    private int xe = 0;
    private int ye = 0;
    private Village mSourceVillage = null;
    private BufferedImage mDistBorder = null;
    private Image mMarkerImage = null;
    private final NumberFormat nf = NumberFormat.getInstance();
    private Point viewStartPoint = null;
    private double currentZoom = 0.0;

    public MapRenderer() {
        mVisibleVillages = new Village[iVillagesX][iVillagesY];
        setDaemon(true);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        try {
            mDistBorder = ImageIO.read(new File("./graphics/dist_border.png"));
            mMarkerImage = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/res/marker.png"));
        } catch (Exception e) {
            logger.error("Failed to load border images", e);
        }
        mLayers = new Hashtable<Integer, BufferedImage>();
    }

    public void initiateRedraw(int pType) {
        mapRedrawRequired = true;
    }

    @Override
    public void run() {
        logger.debug("Entering render loop");
        //long s = System.currentTimeMillis();
        while (true) {
            // System.out.println("Dur: " + (System.currentTimeMillis() - s));
            //  s = System.currentTimeMillis();
            try {

                int w = MapPanel.getSingleton().getWidth();
                int h = MapPanel.getSingleton().getHeight();
                if ((w != 0) && (h != 0)) {
                    Image iBuffer = MapPanel.getSingleton().createImage(w, h);
                    Graphics2D g2d = (Graphics2D) iBuffer.getGraphics();
                    prepareGraphics(g2d);
                    if (mapRedrawRequired) {
                        calculateVisibleVillages();
                        if (viewStartPoint == null) {
                            throw new Exception("View position is 'null', skip redraw");
                        }
                        renderMap();
                        renderTagMarkers();
                    }
                    renderMarkers(g2d);
                    g2d.drawImage(mLayers.get(MAP_LAYER), 0, 0, null);
                    g2d.drawImage(mLayers.get(EXTENDED_DECORATION_LAYER), 0, 0, null);
                    renderBasicDecoration(g2d);
                    renderAttacks(g2d);
                    renderExtendedDecoration(g2d);

                    //draw live layer
                    renderLiveLayer(g2d);
                    de.tor.tribes.types.Rectangle selection = MapPanel.getSingleton().getSelectionRect();
                    if (selection != null) {
                        selection.renderForm(g2d);
                    }
                    MenuRenderer.getSingleton().renderMenu(g2d);
                    MapPanel.getSingleton().updateComplete(mVisibleVillages, iBuffer);
                    MapPanel.getSingleton().repaint();
                    g2d.dispose();
                }
            } catch (Throwable t) {
                logger.error("Redrawing map failed", t);
            }
            try {
                Thread.sleep(40);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void setDragLine(int pXS, int pYS, int pXE, int pYE) {
        mSourceVillage = DataHolder.getSingleton().getVillages()[pXS][pYS];
        xe = pXE;
        ye = pYE;
    }
    private Rectangle2D.Double drawRegion = new Rectangle2D.Double(0, 0, 0, 0);

    /**Extract the visible villages (only needed on full repaint)*/
    private void calculateVisibleVillages() {
        iCenterX = MapPanel.getSingleton().getCurrentPosition().x;
        iCenterY = MapPanel.getSingleton().getCurrentPosition().y;

        if (DataHolder.getSingleton().getVillages() == null) {
            //probably reloading data
            return;
        }
        //get number of drawn villages
        currentZoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        iVillagesX = (int) Math.rint((double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getWidth(null));
        iVillagesY = (int) Math.rint((double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getHeight(null));

        //make number odd to avoid spaces at the borders
        if (iVillagesX % 2 == 0) {
            iVillagesX++;
        }
        if (iVillagesY % 2 == 0) {
            iVillagesY++;
        }
        //calculate village coordinates of the upper left corner
        int xStart = (int) Math.rint((double) iCenterX - (double) iVillagesX / 2.0);
        int yStart = (int) Math.rint((double) iCenterY - (double) iVillagesY / 2.0);

        /*xStart = (xStart < 0) ? 0 : xStart;
        yStart = (yStart < 0) ? 0 : yStart;
         */
        //calculate village coordinates of the lower right corner
        int xEnd = (int) Math.rint((double) iCenterX + (double) iVillagesX / 2);
        int yEnd = (int) Math.rint((double) iCenterY + (double) iVillagesY / 2);

        /*xEnd = (xEnd > 1000) ? 1000 : xEnd;
        yEnd = (yEnd > 1000) ? 1000 : yEnd;
         */
        //add a small drawing buffer for all directions
        iVillagesX += 1;
        iVillagesY += 1;
        mVisibleVillages = new Village[iVillagesX][iVillagesY];

        int x = 0;
        int y = 0;

        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                if ((i < 0) || (i > 999) || (j < 0) || (j > 999)) {
                    //handle villages outside map
                    mVisibleVillages[x][y] = null;
                } else {
                    mVisibleVillages[x][y] = DataHolder.getSingleton().getVillages()[i][j];
                }
                y++;
            }
            x++;
            y = 0;
        }

        viewStartPoint = new Point(xStart, yStart);
        MapPanel.getSingleton().updateVirtualBounds(viewStartPoint);
    }

    private void renderMap() {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        BufferedImage layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
        mLayers.put(MAP_LAYER, layer);
        Graphics2D g2d = layer.createGraphics();
        prepareGraphics(g2d);

        villagePositions = new Hashtable<Village, Rectangle>();

        //disable decoration if field size is not equal the decoration texture size
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, 1).getWidth(null)) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, 1).getHeight(null))) {
            useDecoration = false;
        }

        boolean markedOnly = false;
        try {
            markedOnly = Boolean.parseBoolean(GlobalOptions.getProperty("draw.marked.only"));
        } catch (Exception e) {
            markedOnly = false;
        }

        boolean showBarbarian = true;
        try {
            showBarbarian = Boolean.parseBoolean(GlobalOptions.getProperty("show.barbarian"));
        } catch (Exception e) {
            showBarbarian = true;
        }

        int width = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getWidth(null);
        int height = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getHeight(null);

        int x = 0;
        int y = 0;
        int xPos = viewStartPoint.x;
        int yPos = viewStartPoint.y;
        boolean showSectors = false;
        try {
            showSectors = Boolean.parseBoolean(GlobalOptions.getProperty("show.sectors"));
        } catch (Exception e) {
            showSectors = false;
        }

        boolean showContinents = false;
        try {
            showContinents = Boolean.parseBoolean(GlobalOptions.getProperty("map.showcontinents"));
        } catch (Exception e) {
            showContinents = false;
        }

        List<Integer> xSectors = new LinkedList<Integer>();
        List<Integer> ySectors = new LinkedList<Integer>();
        List<Integer> xContinents = new LinkedList<Integer>();
        List<Integer> yContinents = new LinkedList<Integer>();
        Hashtable<Integer, Point> copyRegions = new Hashtable<Integer, Point>();

        de.tor.tribes.types.Rectangle selection = MapPanel.getSingleton().getSelectionRect();

        // <editor-fold defaultstate="collapsed" desc="Village drawing">
        for (int i = 0; i < iVillagesX; i++) {
            for (int j = 0; j < iVillagesY; j++) {
                Village v = mVisibleVillages[i][j];
                boolean drawVillage = true;

                //check for barbarian
                if (!showBarbarian) {
                    if ((v != null) && (v.getTribe() == null)) {
                        drawVillage = false;
                    }
                }

                if (markedOnly && drawVillage) {
                    if (v != null) {
                        //valid village
                        if (v.getTribe() != null) {
                            if (!v.getTribe().equals(DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe())) {

                                //check tribe marker
                                Marker m = MarkerManager.getSingleton().getMarker(v.getTribe());
                                if (m == null) {
                                    //tribe is not marked check ally marker
                                    if (v.getTribe().getAlly() != null) {
                                        m = MarkerManager.getSingleton().getMarker(v.getTribe().getAlly());
                                        if (m == null) {
                                            //tribe and ally are not marked
                                            drawVillage = false;
                                        }

                                    } else {
                                        drawVillage = false;
                                    }
                                }
                            }
                        }
                    }
                }

                //filter tags
                List<Tag> villageTags = TagManager.getSingleton().getTags(v);
                if ((villageTags.size() != 0) && (drawVillage)) {
                    boolean notShown = true;
                    for (Tag tag : TagManager.getSingleton().getTags(v)) {
                        if (tag.isShowOnMap()) {
                            //at least one of the tags for the village is visible
                            notShown = false;
                            break;
                        }
                    }
                    if (notShown) {
                        drawVillage = false;
                    }
                }

                if (v == null) {
                    Image underground = null;
                    if (useDecoration) {
                        underground = WorldDecorationHolder.getTexture(xPos, yPos, currentZoom);
                    }

                    if (underground == null) {
                        Point p = copyRegions.get(Skin.ID_DEFAULT_UNDERGROUND);
                        if (p == null) {
                            g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom), x, y, null);
                            if (MapPanel.getSingleton().getBounds().contains(new Rectangle(x, y, width, height))) {
                                copyRegions.put(Skin.ID_DEFAULT_UNDERGROUND, new Point(x, y));
                            }
                        } else {
                            g2d.copyArea(p.x, p.y, width, height, x - p.x, y - p.y);
                        }
                    } else {
                        g2d.drawImage(underground, x, y, null);
                    }
                } else {
                    int type = Skin.ID_V1;
                    if (drawVillage) {
                        boolean isLeft = false;
                        if (v.getTribe() == null) {
                            isLeft = true;
                        }
                        if (v.getPoints() < 300) {
                            if (!isLeft) {
                                //changed
                                if (v.getType() != 0) {
                                    type = Skin.ID_B1;
                                }
                            } else {
                                if (v.getType() == 0) {
                                    type = Skin.ID_V1_LEFT;
                                } else {
                                    type = Skin.ID_B1_LEFT;
                                }
                            }
                        } else if (v.getPoints() < 1000) {
                            type = Skin.ID_V2;
                            if (!isLeft) {
                                if (v.getType() != 0) {
                                    type = Skin.ID_B2;
                                }
                            } else {
                                if (v.getType() == 0) {
                                    type = Skin.ID_V2_LEFT;
                                } else {
                                    type = Skin.ID_B2_LEFT;
                                }
                            }
                        } else if (v.getPoints() < 3000) {
                            type = Skin.ID_V3;
                            if (!isLeft) {
                                if (v.getType() != 0) {
                                    type = Skin.ID_B3;
                                }
                            } else {
                                if (v.getType() == 0) {
                                    type = Skin.ID_V3_LEFT;
                                } else {
                                    type = Skin.ID_B3_LEFT;
                                }
                            }
                        } else if (v.getPoints() < 9000) {
                            type = Skin.ID_V4;
                            if (!isLeft) {
                                if (v.getType() != 0) {
                                    type = Skin.ID_B4;
                                }
                            } else {
                                if (v.getType() == 0) {
                                    type = Skin.ID_V4_LEFT;
                                } else {
                                    type = Skin.ID_B4_LEFT;
                                }
                            }
                        } else if (v.getPoints() < 11000) {
                            type = Skin.ID_V5;
                            if (!isLeft) {
                                if (v.getType() != 0) {
                                    type = Skin.ID_B4;
                                }

                            } else {
                                if (v.getType() == 0) {
                                    type = Skin.ID_V5_LEFT;
                                } else {
                                    type = Skin.ID_B5_LEFT;
                                }
                            }
                        } else {
                            type = Skin.ID_V6;
                            if (!isLeft) {
                                if (v.getType() != 0) {
                                    type = Skin.ID_B6;
                                }
                            } else {
                                if (v.getType() == 0) {
                                    type = Skin.ID_V6_LEFT;
                                } else {
                                    type = Skin.ID_B6_LEFT;
                                }
                            }
                        }
                        //store village rectangle
                        villagePositions.put(v, new Rectangle(x, y, width, height));
                    } else {
                        type = Skin.ID_DEFAULT_UNDERGROUND;
                        mVisibleVillages[i][j] = null;
                    }

                    //drawing
                    Point p = copyRegions.get(type);
                    if (p == null) {
                        g2d.drawImage(GlobalOptions.getSkin().getImage(type, currentZoom), x, y, null);
                        //check containment using size tolerance
                        if (MapPanel.getSingleton().getBounds().contains(new Rectangle(x, y, width + 2, height + 2))) {
                            copyRegions.put(type, new Point(x, y));
                        }
                    } else {
                        g2d.copyArea(p.x, p.y, width, height, x - p.x, y - p.y);
                    }

                    if (selection != null) {
                        if (new Rectangle((int) selection.getXPos(), (int) selection.getYPos(), (int) selection.getXPosEnd() - (int) selection.getXPos(), (int) selection.getYPosEnd() - (int) selection.getYPos()).intersects(v.getVirtualBounds())) {
                        }
                    }
                }

                y += height;
                yPos++;

                if ((showSectors) && (yPos % 5 == 0)) {
                    int pos = (yPos - viewStartPoint.y) * height;
                    ySectors.add(pos);
                }

                if ((showContinents) && (yPos % 100 == 0)) {
                    int pos = (yPos - viewStartPoint.y) * height;
                    yContinents.add(pos);
                }
            }
            y = 0;
            x += width;
            yPos = viewStartPoint.y;
            xPos++;

            if ((showSectors) && (xPos % 5 == 0)) {
                int pos = (xPos - viewStartPoint.x) * width;
                xSectors.add(pos);
            }

            if ((showContinents) && (xPos % 100 == 0)) {
                int pos = (xPos - viewStartPoint.x) * width;
                xContinents.add(pos);
            }
        }

        g2d.setStroke(new BasicStroke(0.5f));
        if (showSectors) {
            g2d.setColor(Color.BLACK);
            for (Integer xs : xSectors) {
                g2d.drawLine(xs, 0, xs, hb);
            }

            for (Integer ys : ySectors) {
                g2d.drawLine(0, ys, wb, ys);
            }
        }
        g2d.setStroke(new BasicStroke(1.0f));
        if (showContinents) {
            g2d.setColor(Color.YELLOW);
            for (Integer xs : xContinents) {
                g2d.drawLine(xs, 0, xs, hb);
            }

            for (Integer ys : yContinents) {
                g2d.drawLine(0, ys, wb, ys);
            }
        }
        //</editor-fold>

        /* Enumeration<Integer> keys = copyRegions.keys();
        g2d.setColor(Color.MAGENTA);
        while (keys.hasMoreElements()) {
        Point p = copyRegions.get(keys.nextElement());
        g2d.drawRect(p.x, p.y, width, height);
        }*/

        /*  System.out.println("Regions " + copyRegions);
        System.out.println("Village complete: " + (System.currentTimeMillis() - s));
        // System.out.println("Preparation: " + (preTime));
        // System.out.println("Transform: " + (transTime));
        System.out.println("Drawing: " + (drawTime));
        System.out.println("Copying: " + (copyTime));
        //System.out.println("Count: " + cnt);
        System.out.println("-------");*/
        // System.out.println(copyRegions);
        g2d.dispose();
        mapRedrawRequired = false;
    }

    private void renderTagMarkers() {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        BufferedImage layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
        mLayers.put(EXTENDED_DECORATION_LAYER, layer);
        Graphics2D g2d = layer.createGraphics();
        prepareGraphics(g2d);

        int width = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getWidth(null);
        int height = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getHeight(null);

        int x = 0;
        int y = 0;
        int xPos = viewStartPoint.x;
        int yPos = viewStartPoint.y;

        Hashtable<Integer, Point> copyRegions = new Hashtable<Integer, Point>();
        int tagsize = (int) Math.rint((double) 18 / currentZoom);
        // <editor-fold defaultstate="collapsed" desc="Graphics drawing">
        for (int i = 0; i < iVillagesX; i++) {
            for (int j = 0; j < iVillagesY; j++) {
                Village v = mVisibleVillages[i][j];
                if (v != null) {
                    //filter tags
                    List<Tag> villageTags = TagManager.getSingleton().getTags(v);
                    if (villageTags.size() != 0) {
                        int xcnt = 1;
                        int ycnt = 2;
                        int cnt = 0;
                        for (Tag tag : TagManager.getSingleton().getTags(v)) {
                            if (tag.isShowOnMap()) {
                                int iconType = tag.getTagIcon();
                                Color color = tag.getTagColor();
                                int tagX = x + width - xcnt * tagsize;
                                int tagY = y + height - ycnt * tagsize;
                                if (color != null) {
                                    Color before = g2d.getColor();
                                    g2d.setColor(color);
                                    g2d.fillRect(tagX, tagY, tagsize, tagsize);
                                    g2d.setColor(before);
                                }
                                if (iconType != -1) {
                                    //drawing
                                    Point p = copyRegions.get(iconType);
                                    if (p == null) {
                                        Image tagImage = ImageManager.getUnitImage(iconType).getScaledInstance(tagsize, tagsize, Image.SCALE_FAST);
                                        g2d.drawImage(tagImage, tagX, tagY, null);
                                        //check containment using size tolerance
                                        if (MapPanel.getSingleton().getBounds().contains(new Rectangle(tagX, tagY, tagsize + 2, tagsize + 2))) {
                                            copyRegions.put(iconType, new Point(tagX, tagY));
                                        }
                                    } else {
                                        g2d.copyArea(p.x, p.y, tagsize, tagsize, tagX - p.x, tagY - p.y);
                                    }
                                }
                            }
                            //calculate positioning
                            cnt++;
                            xcnt++;
                            if (cnt == 2) {
                                //show only 2 icons in the first line to avoid marker overlay
                                xcnt = 1;
                                ycnt--;
                            }
                        }
                    }
                }
                y += height;
                yPos++;
            }
            y = 0;
            x += width;
            yPos = viewStartPoint.y;
            xPos++;
        }


        /*Enumeration<Integer> keys = copyRegions.keys();
        while (keys.hasMoreElements()) {
        Point p = copyRegions.get(keys.nextElement());
        g2d.setColor(Color.MAGENTA);
        g2d.drawRect(p.x, p.y, tagsize, tagsize);
        }
         */
        //</editor-fold>
        g2d.dispose();
    }

    private void renderMarkers(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        Color DEFAULT = Color.WHITE;
        try {
            if (Integer.parseInt(GlobalOptions.getProperty("default.mark")) == 1) {
                DEFAULT = Color.RED;
            }

        } catch (Exception e) {
            DEFAULT = Color.WHITE;
        }

        g2d.setColor(DEFAULT);
        g2d.fillRect(0, 0, wb, hb);

        Enumeration<Village> villages = villagePositions.keys();
        while (villages.hasMoreElements()) {
            Village v = villages.nextElement();
            Tribe t = v.getTribe();
            Color markerColor = null;
            if (t == DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe()) {
                if (v.equals(DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage())) {
                    markerColor = Color.WHITE;
                } else {
                    markerColor = Color.YELLOW;
                }
            } else {
                Marker m = null;
                if (t != null) {
                    m = MarkerManager.getSingleton().getMarker(t);
                    if (m == null) {
                        if (t.getAlly() != null) {
                            m = MarkerManager.getSingleton().getMarker(t.getAlly());
                        }
                    }
                }
                if (m != null) {
                    markerColor = m.getMarkerColor();
                }
            }
            if (markerColor != null) {
                Rectangle vRect = villagePositions.get(v);
                g2d.setColor(markerColor);
                g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
            }
        }
    }

    private void renderBasicDecoration(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        boolean markActiveVillage = false;

        try {
            markActiveVillage = Boolean.parseBoolean(GlobalOptions.getProperty("mark.active.village"));
        } catch (Exception e) {
            markActiveVillage = false;
        }

        boolean markTroopTypes = false;
        try {
            markTroopTypes = Boolean.parseBoolean(GlobalOptions.getProperty("paint.troops.type"));
        } catch (Exception e) {
            markTroopTypes = false;
        }

        Enumeration<Village> villages = villagePositions.keys();
        while (villages.hasMoreElements()) {
            Village v = villages.nextElement();
            Rectangle villageRect = villagePositions.get(v);
            if (markTroopTypes) {
                Image troopMark = TroopsManager.getSingleton().getTroopsMarkerForVillage(v);
                if (troopMark != null) {
                    int x = villageRect.x + (int) Math.round(villageRect.width / 2);
                    int y = villageRect.y + (int) Math.round(villageRect.width / 2);
                    troopMark = troopMark.getScaledInstance((int) Math.rint(troopMark.getWidth(null) / currentZoom), (int) Math.rint(troopMark.getHeight(null) / currentZoom), BufferedImage.SCALE_FAST);
                    g2d.drawImage(troopMark, x - troopMark.getWidth(null) / 2, y - troopMark.getHeight(null), null);
                }
            }

            if (markActiveVillage) {
                Village current = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                if (current != null) {
                    if (v.compareTo(current) == 0) {
                        int markX = villageRect.x + (int) Math.round(villageRect.width / 2);
                        int markY = villageRect.y + (int) Math.round(villageRect.height / 2);
                        g2d.drawImage(mMarkerImage, markX, markY - mMarkerImage.getHeight(null), null);
                    }
                }
            }
        }
    }

    private void renderAttacks(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }
// <editor-fold defaultstate="collapsed" desc="Attack-line drawing (Foreground)">

        g2d.setStroke(new BasicStroke(2.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        int width = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getWidth(null);
        int height = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getHeight(null);
        //get attack colors
        Hashtable<String, Color> attackColors = new Hashtable<String, Color>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Color unitColor = Color.RED;
            try {
                unitColor = Color.decode(GlobalOptions.getProperty(unit.getName() + ".color"));
            } catch (Exception e) {
                unitColor = Color.RED;
            }

            attackColors.put(unit.getName(), unitColor);
        }

        Enumeration<String> keys = AttackManager.getSingleton().getPlans();

        while (keys.hasMoreElements()) {
            String plan = keys.nextElement();
            Attack[] attacks = AttackManager.getSingleton().getAttackPlan(plan).toArray(new Attack[]{});
            for (Attack attack : attacks) {
                //go through all attacks
                if (attack.isShowOnMap()) {
                    //only enter if attack should be visible
                    //get line for this attack
                    Line2D.Double attackLine = new Line2D.Double(attack.getSource().getX(), attack.getSource().getY(), attack.getTarget().getX(), attack.getTarget().getY());
                    Rectangle2D.Double bounds = new Rectangle2D.Double(viewStartPoint.x, viewStartPoint.y, iVillagesX, iVillagesY);
                    String value = GlobalOptions.getProperty("attack.movement");
                    boolean showAttackMovement = (value == null) ? false : Boolean.parseBoolean(value);
                    int xStart = ((int) attackLine.getX1() - viewStartPoint.x) * width + width / 2;
                    int yStart = ((int) attackLine.getY1() - viewStartPoint.y) * height + height / 2;
                    int xEnd = (int) (attackLine.getX2() - viewStartPoint.x) * width + width / 2;
                    int yEnd = (int) (attackLine.getY2() - viewStartPoint.y) * height + height / 2;
                    ImageIcon unitIcon = null;
                    int unitXPos = 0;
                    int unitYPos = 0;
                    if (showAttackMovement) {
                        unitIcon = ImageManager.getUnitIcon(attack.getUnit());
                        if (unitIcon != null) {
                            long dur = (long) (DSCalculator.calculateMoveTimeInSeconds(attack.getSource(), attack.getTarget(), attack.getUnit().getSpeed()) * 1000);
                            long arrive = attack.getArriveTime().getTime();
                            long start = arrive - dur;
                            long current = System.currentTimeMillis();

                            if ((start < current) && (arrive > current)) {
                                //attack running
                                long runTime = System.currentTimeMillis() - start;
                                double perc = 100 * runTime / dur;
                                perc /= 100;
                                double xTar = xStart + (xEnd - xStart) * perc;
                                double yTar = yStart + (yEnd - yStart) * perc;
                                unitXPos = (int) xTar - unitIcon.getIconWidth() / 2;
                                unitYPos = (int) yTar - unitIcon.getIconHeight() / 2;
                            } else if ((start > System.currentTimeMillis()) && (arrive > current)) {
                                //attack not running, draw unit in source village
                                unitXPos = (int) xStart - unitIcon.getIconWidth() / 2;
                                unitYPos = (int) yStart - unitIcon.getIconHeight() / 2;
                            } else {
                                //attack arrived
                                unitXPos = (int) xEnd - unitIcon.getIconWidth() / 2;
                                unitYPos = (int) yEnd - unitIcon.getIconHeight() / 2;
                            }
                        }
                    }

                    g2d.setColor(attackColors.get(attack.getUnit().getName()));
                    g2d.drawLine(xStart, yStart, xEnd, yEnd);
                    g2d.setColor(Color.YELLOW);
                    if (bounds.contains(attackLine.getP1())) {
                        g2d.fillRect((int) xStart - 3, yStart - 1, 6, 6);
                    }

                    if (bounds.contains(attackLine.getP2())) {
                        g2d.fillOval((int) xEnd - 3, (int) yEnd - 3, 6, 6);
                    }

                    if (unitIcon != null) {
                        g2d.drawImage(unitIcon.getImage(), unitXPos, unitYPos, null);
                    }

                }
            }
            attacks = null;
        }
//</editor-fold>

    }

    private void renderExtendedDecoration(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        AbstractForm[] forms = FormManager.getSingleton().getForms().toArray(new AbstractForm[]{});
        for (AbstractForm form : forms) {
            form.renderForm(g2d);
        }
        if (!FormConfigFrame.getSingleton().isInEditMode()) {
            //only render in create mode to avoid multi-drawing
            AbstractForm f = FormConfigFrame.getSingleton().getCurrentForm();

            if (f != null) {
                f.renderForm(g2d);
            }
        }
    }

    private void renderLiveLayer(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        int width = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getWidth(null);
        int height = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getHeight(null);

        Village mouseVillage = MapPanel.getSingleton().getVillageAtMousePos();

        // <editor-fold defaultstate="collapsed" desc=" Draw Drag line (Foreground)">
        Line2D.Double dragLine = new Line2D.Double(-1, -1, xe, ye);
        if (mSourceVillage != null) {
            //must draw drag line
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(5.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            //get map regions for source and target village
            Rectangle sourceRect = villagePositions.get(mSourceVillage);
            Rectangle targetRect = null;
            if (mouseVillage != null) {
                targetRect = villagePositions.get(mouseVillage);
            }

//check which region is visible
            if (sourceRect != null && targetRect != null) {
                //source and target are visible and selected. Draw drag line between them
                dragLine.setLine(sourceRect.x + sourceRect.width / 2, sourceRect.y + sourceRect.height / 2, targetRect.x + targetRect.width / 2, targetRect.y + targetRect.height / 2);
            } else if (sourceRect != null && targetRect == null) {
                //source region is visible, target village is not selected
                dragLine.setLine(sourceRect.x + sourceRect.width / 2, sourceRect.y + sourceRect.height / 2, xe, ye);
            } else {
                //source and target region not invisible/selected
                dragLine.setLine((mSourceVillage.getX() - viewStartPoint.x) * width, (mSourceVillage.getY() - viewStartPoint.y) * height, xe, ye);
            }

            if ((dragLine.getX2() != 0) && (dragLine.getY2() != 0)) {
                int x1 = (int) dragLine.getX1();
                int y1 = (int) dragLine.getY1();
                int x2 = (int) dragLine.getX2();
                int y2 = (int) dragLine.getY2();
                g2d.drawLine(x1, y1, x2, y2);
                boolean drawDistance = false;
                try {
                    drawDistance = Boolean.parseBoolean(GlobalOptions.getProperty("draw.distance"));
                } catch (Exception e) {
                }
                if (drawDistance) {
                    if (mouseVillage != null) {
                        double d = DSCalculator.calculateDistance(mSourceVillage, mouseVillage);
                        String dist = nf.format(d);

                        double hf = PatchFontMetrics.patch(g2d.getFontMetrics()).getStringBounds(dist, g2d).getHeight();

                        g2d.drawImage(mDistBorder, null, targetRect.x, targetRect.y);
                        g2d.drawString(dist, targetRect.x + 6, targetRect.y + (int) Math.rint(hf));
                    }

                }
            }
        }
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Troop information (Foreground)">

        boolean showTroopInfo = false;
        try {
            showTroopInfo = Boolean.parseBoolean(GlobalOptions.getProperty("show.troop.info"));
        } catch (Exception e) {
            showTroopInfo = false;
        }

        if (showTroopInfo) {
            if (mouseVillage != null) {
                Rectangle villageRect = villagePositions.get(mouseVillage);
                if (villageRect != null) {
                    VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(mouseVillage);
                    if ((holder != null) && (!holder.getTroops().isEmpty())) {
                        //get half the units for the current server
                        int unitCount = DataHolder.getSingleton().getUnits().size();
                        FontMetrics metrics = PatchFontMetrics.patch(g2d.getFontMetrics(g2d.getFont()));

                        float textHeight = g2d.getFontMetrics().getLineMetrics("1.234.567", g2d).getHeight();
                        Point pos = villageRect.getLocation();
                        //number format without fraction digits
                        NumberFormat numFormat = NumberFormat.getInstance();
                        numFormat.setMaximumFractionDigits(0);
                        numFormat.setMinimumFractionDigits(0);
                        //default width for unit number
                        double maxWidth = metrics.getStringBounds("1.234.567", g2d).getWidth();
                        int unitHeight = ImageManager.getUnitIcon(0).getImage().getHeight(null);

                        //get largest unit value
                        for (Integer i : holder.getTroops()) {
                            int w = metrics.stringWidth(numFormat.format(i));
                            if (w > maxWidth) {
                                maxWidth = w;
                            }
                        }

                        g2d.setColor(Constants.DS_BACK_LIGHT);
                        int popupWidth = 12 + (int) Math.rint(maxWidth) + unitHeight;
                        int popupHeight = unitCount * unitHeight + 10 + (int) Math.rint(textHeight) + 2;
                        g2d.fill3DRect(pos.x - popupWidth, pos.y, popupWidth, popupHeight, true);

                        g2d.setColor(Color.BLACK);

                        //draw state
                        String state = "(" + new SimpleDateFormat("dd.MM.yyyy").format(holder.getState()) + ")";
                        double dY = metrics.getStringBounds(state, g2d).getY();
                        g2d.drawString(state, pos.x - popupWidth + 5, pos.y - (int) Math.rint(dY) + 5);

                        //fixed value for linux issues
                        double sx = 0.84;//(double) textHeight / (double) unitHeight;
                        for (int i = 0; i < unitCount; i++) {
                            //draw unit with a border of 5px
                            AffineTransform xform = AffineTransform.getTranslateInstance(pos.x - popupWidth + 5, pos.y + i * unitHeight + 5 + textHeight + 2);
                            xform.scale(sx, sx);
                            g2d.drawImage(ImageManager.getUnitIcon(i).getImage(), xform, null);
                            //draw the unit count
                            dY = metrics.getStringBounds(numFormat.format(holder.getTroops().get(i)), g2d).getY();
                            g2d.drawString(numFormat.format(holder.getTroops().get(i)), pos.x - popupWidth + 5 + unitHeight + 2, pos.y + i * unitHeight - (int) Math.rint(dY) + 5 + (int) Math.rint(textHeight) + 2);
                        }

                    } else {
                        Point pos = villageRect.getLocation();
                        if (pos != null) {
                            String noInfo = "keine Informationen";
                            FontMetrics metrics = PatchFontMetrics.patch(g2d.getFontMetrics(g2d.getFont()));
                            int textWidth = metrics.stringWidth(noInfo);
                            int popupX = pos.x - textWidth - 10;
                            int popupY = pos.y;
                            Rectangle2D bounds = metrics.getStringBounds(noInfo, g2d);

                            g2d.setColor(Constants.DS_BACK_LIGHT);
                            g2d.fill3DRect(popupX, popupY, 10 + textWidth, metrics.getHeight() + 4, true);
                            g2d.setColor(Color.BLACK);
                            g2d.drawString(noInfo, popupX + 5, popupY - (int) Math.rint(bounds.getY()) + 2);
                        }

                    }
                }
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Draw radar information ">
        if (mouseVillage != null) {
            try {
                if (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_RADAR) {
                    int cnt = 0;
                    for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                        de.tor.tribes.types.Circle c = new de.tor.tribes.types.Circle();
                        int r = Integer.parseInt(GlobalOptions.getProperty("radar.size"));
                        double diam = 2 * (double) r / u.getSpeed();
                        double xp = mouseVillage.getX() + 0.5 - diam / 2;
                        double yp = mouseVillage.getY() + 0.5 - diam / 2;
                        double cx = mouseVillage.getX() + 0.5;
                        double cy = mouseVillage.getY() + 0.5;

                        double xi = Math.cos(Math.toRadians(270 + cnt * (10))) * diam / 2;
                        double yi = Math.sin(Math.toRadians(270 + cnt * 10)) * diam / 2;
                        c.setXPos(xp);
                        c.setYPos(yp);
                        c.setXPosEnd(xp + diam);
                        c.setYPosEnd(yp + diam);
                        Color co = Color.RED;
                        try {
                            co = Color.decode(GlobalOptions.getProperty(u.getName() + ".color"));
                        } catch (Exception e) {
                        }
                        c.setDrawColor(co);
                        c.renderForm(g2d);
                        Image unit = ImageManager.getUnitIcon(u).getImage();
                        Point p = MapPanel.getSingleton().virtualPosToSceenPos((cx + xi), (cy + yi));
                        g2d.drawImage(unit, p.x - (int) ((double) unit.getWidth(null) / 2), (int) ((double) p.y - unit.getHeight(null) / 2), null);
                        cnt++;
                    }
                }
            } catch (Exception e) {
            }
        }
// </editor-fold>
    }

    private void prepareGraphics(Graphics2D pG2d) {
        pG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        pG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // Speed
        pG2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        pG2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        pG2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        pG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}

/*
class TagAnimator {

private Village mVillage = null;
private int iX = 0;
private int iY = 0;
private int iDistance = 0;
private boolean pRise = false;
private boolean bFinished = false;

public TagAnimator(Village pVillage, int pVillageX, int pVillageY) {
mVillage = pVillage;
iX = pVillageX;
iY = pVillageY;
iDistance = 1;
pRise = true;
}

public Village getVillage() {
return mVillage;
}

public void setRise(boolean pValue) {
pRise = pValue;
}

public boolean isFinished() {
return bFinished;
}

public void update(Graphics2D g2d) {
if (pRise) {
if (iDistance < 51) {
iDistance += 25;
}
} else {
iDistance -= 25;
if (iDistance <= 0) {
bFinished = true;
iDistance = 0;
}
}

//degree for every village to get a circle
double deg = 360 / TagManager.getSingleton().getTags(mVillage).size();
int cnt = 0;
for (String tag : TagManager.getSingleton().getTags(mVillage)) {
Image tagImage = TagManager.getSingleton().getUserTagIcon(tag);
//take next degree
double cd = cnt * deg;
int xv = (int) Math.rint(iX + iDistance * Math.cos(2 * Math.PI * cd / 360));
int yv = (int) Math.rint(iY + iDistance * Math.sin(2 * Math.PI * cd / 360));
int width = (int) Math.rint(tagImage.getWidth(null) * iDistance * 0.05);
int height = (int) Math.rint(tagImage.getHeight(null) * iDistance * 0.05);
if (width < tagImage.getWidth(null) || height < tagImage.getHeight(null)) {
width = tagImage.getWidth(null);
height = tagImage.getHeight(null);
}
g2d.drawImage(tagImage.getScaledInstance(width, height, Image.SCALE_FAST), xv, yv, null);
cnt++;
}
}
}
}
 */

