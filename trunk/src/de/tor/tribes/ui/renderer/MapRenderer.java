/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Ally;
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
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.map.FormManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
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
    private double dCenterX = 500.0;
    private double dCenterY = 500.0;
    private int xe = 0;
    private int ye = 0;
    private Village mSourceVillage = null;
    private Image mMarkerImage = null;
    private final NumberFormat nf = NumberFormat.getInstance();
    private Point2D.Double viewStartPoint = null;
    private double currentZoom = 0.0;
    private Village currentUserVillage = null;
    private Image mMainBuffer = null;

    public MapRenderer() {
        mVisibleVillages = new Village[iVillagesX][iVillagesY];
        setDaemon(true);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        try {
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
        while (true) {
            try {
                int w = MapPanel.getSingleton().getWidth();
                int h = MapPanel.getSingleton().getHeight();
                if ((w != 0) && (h != 0)) {
                    Graphics2D g2d = null;
                    if (mMainBuffer == null) {
                        mMainBuffer = MapPanel.getSingleton().createImage(w, h);
                        g2d = (Graphics2D) mMainBuffer.getGraphics();
                        prepareGraphics(g2d);
                        //set redraw required flag if nothin was drawn yet
                        mapRedrawRequired = true;
                    } else {
                        //check if image size is still valid
                        if (mMainBuffer.getWidth(null) != w || mMainBuffer.getHeight(null) != h) {
                            //map panel has resized
                            mMainBuffer = MapPanel.getSingleton().createImage(w, h);
                            g2d = (Graphics2D) mMainBuffer.getGraphics();
                            prepareGraphics(g2d);
                            //set redraw required flag if size has changed
                            mapRedrawRequired = true;
                        } else {
                            //only clear graphics
                            g2d = (Graphics2D) mMainBuffer.getGraphics();
                            Composite c = g2d.getComposite();
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                            g2d.fillRect(0, 0, w, h);
                            g2d.setComposite(c);
                        }
                    }

                    currentUserVillage = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                    if (mapRedrawRequired) {
                        //complete redraw is required
                        calculateVisibleVillages();
                        if (viewStartPoint == null) {
                            throw new Exception("View position is 'null', skip redraw");
                        }
                        renderMap();
                        renderTagMarkers();
                    }
                    //render misc map elements
                    boolean markOnTop = Boolean.parseBoolean(GlobalOptions.getProperty("mark.on.top"));
                    if (markOnTop) {
                        g2d.drawImage(mLayers.get(MAP_LAYER), 0, 0, null);
                        Composite gg = g2d.getComposite();
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                        renderMarkers(g2d);
                        g2d.setComposite(gg);
                    } else {
                        renderMarkers(g2d);
                        g2d.drawImage(mLayers.get(MAP_LAYER), 0, 0, null);
                    }
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

                    //notify MapPanel
                    Hashtable<Village, Rectangle> pos = (Hashtable<Village, Rectangle>) villagePositions.clone();
                    MapPanel.getSingleton().updateComplete(pos, mMainBuffer);
                    MapPanel.getSingleton().repaint();
                    g2d.dispose();

                }
            } catch (Throwable t) {
                logger.error("Redrawing map failed", t);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void setDragLine(int pXS, int pYS, int pXE, int pYE) {
        if (pXS == -1 && pYS == -1 && pXE == -1 && pYE == -1) {
            mSourceVillage = null;
            xe = 0;
            ye = 0;
        } else {
            mSourceVillage = DataHolder.getSingleton().getVillages()[pXS][pYS];
            xe = pXE;
            ye = pYE;
        }
    }

    /**Extract the visible villages (only needed on full repaint)*/
    private void calculateVisibleVillages() {
        dCenterX = MapPanel.getSingleton().getCurrentPosition().x;
        dCenterY =
                MapPanel.getSingleton().getCurrentPosition().y;

        if (DataHolder.getSingleton().getVillages() == null) {
            //probably reloading data
            return;
        }

//get number of drawn villages
        currentZoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        iVillagesX = (int) Math.ceil((double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getWidth(null));
        iVillagesY = (int) Math.ceil((double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getHeight(null));
        iVillagesX++;
        iVillagesY++;

//calculate village coordinates of the upper left corner
        int xStartVillage = (int) Math.floor(dCenterX - iVillagesX / 2.0);
        int yStartVillage = (int) Math.floor(dCenterY - iVillagesY / 2.0);

        double dXStart = dCenterX - (double) iVillagesX / 2.0;
        double dYStart = dCenterY - (double) iVillagesY / 2.0;

        //calculate village coordinates of the lower right corner
        int xEndVillage = (int) Math.ceil((double) dCenterX + (double) iVillagesX / 2.0);
        int yEndVillage = (int) Math.ceil((double) dCenterY + (double) iVillagesY / 2.0);


        //correct village count
        iVillagesX = xEndVillage - xStartVillage;
        iVillagesY = yEndVillage - yStartVillage;

        //add a small drawing buffer for all directions

        mVisibleVillages = new Village[iVillagesX][iVillagesY];

        int x = 0;
        int y = 0;

        for (int i = xStartVillage; i < xEndVillage; i++) {
            for (int j = yStartVillage; j < yEndVillage; j++) {
                int mapW = ServerSettings.getSingleton().getMapDimension().width;
                int mapH = ServerSettings.getSingleton().getMapDimension().height;
                if ((i < 0) || (i > mapW - 1) || (j < 0) || (j > mapH - 1)) {
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

        viewStartPoint = new Point2D.Double(dXStart, dYStart);
        MapPanel.getSingleton().updateVirtualBounds(viewStartPoint);
    }

    private void renderMap() {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        BufferedImage layer = null;
        Graphics2D g2d = null;
        //prepare drawing buffer
        if (mLayers.get(MAP_LAYER) == null) {
            layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
            mLayers.put(MAP_LAYER, layer);
            g2d =
                    layer.createGraphics();
            prepareGraphics(g2d);
        } else {
            //check if image size is still valid
            layer = mLayers.get(MAP_LAYER);
            if (layer.getWidth() != wb || layer.getHeight() != hb) {
                //mappanel has resized
                layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
                mLayers.put(MAP_LAYER, layer);
                g2d =
                        layer.createGraphics();
                prepareGraphics(g2d);
            } else {
                //only clear image
                g2d = (Graphics2D) layer.getGraphics();
                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                g2d.fillRect(0, 0, wb, hb);
                g2d.setComposite(c);
            }

        }

        //disable decoration if field size is not equal the decoration texture size
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, 1).getWidth(null)) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, 1).getHeight(null))) {
            //use decoration if skin field size equals the world skin size
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

        int width = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getWidth(null);
        int height = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom).getHeight(null);

        double xPos = viewStartPoint.x;
        double yPos = viewStartPoint.y;
        int x = 0;
        int y = 0;
        //delta which the village at pos 0|0 is outside the field of view
        double dx = 0 - ((xPos - Math.floor(xPos)) * width);
        double dy = 0 - ((yPos - Math.floor(yPos)) * height);

        HashSet<Integer> xSectors = new HashSet<Integer>();
        HashSet<Integer> ySectors = new HashSet<Integer>();
        HashSet<Integer> xContinents = new HashSet<Integer>();
        HashSet<Integer> yContinents = new HashSet<Integer>();

        Hashtable<Integer, Point> copyRegions = new Hashtable<Integer, Point>();
        Hashtable<Integer, Point> copyRegionsMap = new Hashtable<Integer, Point>();
        villagePositions = new Hashtable<Village, Rectangle>();
        int contSpacing = 100;
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            contSpacing = 50;
        }
        //de.tor.tribes.types.Rectangle selection = MapPanel.getSingleton().getSelectionRect();

        // <editor-fold defaultstate="collapsed" desc="Village drawing">

        for (int i = 0; i < iVillagesX; i++) {
            for (int j = 0; j < iVillagesY; j++) {
                Village v = mVisibleVillages[i][j];
                boolean drawVillage = true;

                // <editor-fold defaultstate="collapsed" desc=" Check if village should be drawn ">
                //check for barbarian
                if (!showBarbarian) {
                    if ((v != null) && (v.getTribe() == null)) {
                        drawVillage = false;
                    }

                }

                if (drawVillage && markedOnly) {
                    if (v != null && currentUserVillage != null) {
                        //valid village
                        if (v.getTribe() != null) {
                            if (!v.getTribe().equals(currentUserVillage.getTribe())) {
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
                            }//village is owned by current user
                        }
                    }
                }

                //filter tags
                List<Tag> villageTags = TagManager.getSingleton().getTags(v);
                if ((drawVillage) && (villageTags != null) && (villageTags.size() != 0)) {
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
                // </editor-fold>

                if (v == null) {
                    //no village at current position
                    int worldId = WorldDecorationHolder.ID_GRAS1;
                    if (useDecoration) {
                        //get texture for current field
                        worldId = WorldDecorationHolder.getTextureId((int) Math.floor(xPos), (int) Math.floor(yPos));
                    } else {
                        worldId = -1;
                    }

                    Point p = copyRegionsMap.get(worldId);
                    if (p == null) {
                        if (worldId != -1) {
                            Image worldImage = WorldDecorationHolder.getTexture((int) Math.floor(xPos), (int) Math.floor(yPos), currentZoom);
                            int xp = (int) Math.floor(x + dx);
                            int yp = (int) Math.floor(y + dy);
                            g2d.drawImage(worldImage, xp, yp, null);
                            //check containment using size tolerance
                            if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                copyRegionsMap.put(worldId, new Point(xp, yp));
                            }
                        } else {
                            Image worldImage = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom);
                            int xp = (int) Math.floor(x + dx);
                            int yp = (int) Math.floor(y + dy);
                            g2d.drawImage(worldImage, xp, yp, null);
                            //check containment using size tolerance
                            if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                copyRegionsMap.put(worldId, new Point(xp, yp));
                            }
                        }
                    } else {
                        g2d.copyArea(p.x, p.y, width, height, (int) Math.floor(x + dx - p.x), (int) Math.floor(y + dy - p.y));
                    }

                } else {
                    // <editor-fold defaultstate="collapsed" desc=" Select village type ">
                    int type = Skin.ID_V1;
                    v.setVisibleOnMap(drawVillage);
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
                        villagePositions.put(v, new Rectangle((int) Math.floor(x + dx), (int) Math.floor(y + dy), width, height));
                    } else {
                        type = Skin.ID_DEFAULT_UNDERGROUND;
                        mVisibleVillages[i][j] = null;
                    }
// </editor-fold>

                    //drawing
                    Point p = copyRegions.get(type);
                    if (p == null) {
                        int xp = (int) Math.floor(x + dx);
                        int yp = (int) Math.floor(y + dy);
                        g2d.drawImage(GlobalOptions.getSkin().getImage(type, currentZoom), xp, yp, null);
                        //check containment using size tolerance
                        if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                            copyRegions.put(type, new Point(xp, yp));
                        }

                    } else {
                        g2d.copyArea(p.x, p.y, width, height, (int) Math.floor(x + dx - p.x), (int) Math.floor(y + dy - p.y));
                    }

                /*  if (selection != null) {
                if (new Rectangle((int) selection.getXPos(), (int) selection.getYPos(), (int) selection.getXPosEnd() - (int) selection.getXPos(), (int) selection.getYPosEnd() - (int) selection.getYPos()).intersects(v.getVirtualBounds())) {
                //!?
                }
                }*/
                }

                y += height;
                yPos++;


                if ((showSectors) && ((int) Math.floor(yPos) % 5 == 0)) {
                    int pos = (int) Math.floor((yPos - viewStartPoint.y) * height);
                    ySectors.add(pos);
                }

                if ((showContinents) && ((int) Math.floor(yPos) % contSpacing == 0)) {
                    int pos = (int) Math.floor((yPos - viewStartPoint.y) * height);
                    yContinents.add(pos);
                }
            }

            y = 0;
            x += width;
            yPos = viewStartPoint.y;
            xPos++;

            if ((showSectors) && ((int) Math.floor(xPos) % 5 == 0)) {
                int pos = (int) Math.floor((xPos - viewStartPoint.x) * width);
                xSectors.add(pos);
            }


            if ((showContinents) && ((int) Math.floor(xPos) % contSpacing == 0)) {
                int pos = (int) Math.floor((xPos - viewStartPoint.x) * width);
                xContinents.add(pos);
            }
        }

        Stroke s = g2d.getStroke();
        if (showSectors) {
            g2d.setStroke(new BasicStroke(0.5f));
            g2d.setColor(Color.BLACK);
            for (Integer xs : xSectors) {
                g2d.drawLine((int) Math.floor(xs + dx), 0, (int) Math.floor(xs + dx), hb);
            }

            for (Integer ys : ySectors) {
                g2d.drawLine(0, (int) Math.floor(ys + dy), wb, (int) Math.floor(ys + dy));
            }

        }

        if (showContinents) {
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.setColor(Color.YELLOW);
            for (Integer xs : xContinents) {
                g2d.drawLine((int) Math.floor(xs + dx), 0, (int) Math.floor(xs + dx), hb);
            }

            for (Integer ys : yContinents) {
                g2d.drawLine(0, (int) Math.floor(ys + dy), wb, (int) Math.floor(ys + dy));
            }

        }
        g2d.setStroke(s);
        //</editor-fold>

        /* Enumeration<Integer> keys1 = copyRegions.keys();
        g2d.setColor(Color.MAGENTA);
        while (keys1.hasMoreElements()) {
        Point2D.Double p = copyRegionsMap.get(keys1.nextElement());
        try {
        g2d.drawRect((int) p.x, (int) p.y, width, height);
        } catch (Exception e) {
        }
        }

        Enumeration<Integer> keys2 = copyRegionsMap.keys();
        g2d.setColor(Color.MAGENTA);
        while (keys2.hasMoreElements()) {
        Point2D.Double p = copyRegionsMap.get(keys2.nextElement());
        try {
        g2d.drawRect((int) p.x, (int) p.y, width, height);
        } catch (Exception e) {
        }
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
        long s = System.currentTimeMillis();
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        BufferedImage layer = null;
        Graphics2D g2d = null;
        //prepare drawing buffer
        if (mLayers.get(EXTENDED_DECORATION_LAYER) == null) {
            layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
            mLayers.put(EXTENDED_DECORATION_LAYER, layer);
            g2d = layer.createGraphics();
            prepareGraphics(g2d);
        } else {
            layer = mLayers.get(EXTENDED_DECORATION_LAYER);
            if (layer.getWidth() != wb || layer.getHeight() != hb) {
                layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
                mLayers.put(EXTENDED_DECORATION_LAYER, layer);
                g2d = layer.createGraphics();
                prepareGraphics(g2d);
            } else {
                g2d = (Graphics2D) layer.getGraphics();
                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                g2d.fillRect(0, 0, wb, hb);
                g2d.setComposite(c);
            }

        }

        Hashtable<Integer, Point> copyRegions = new Hashtable<Integer, Point>();
        int tagsize = (int) Math.rint((double) 18 / currentZoom);

        // <editor-fold defaultstate="collapsed" desc="Graphics drawing">
        Enumeration<Village> villages = villagePositions.keys();
        int cc = 0;
        try {
            while (villages.hasMoreElements()) {
                Village current = villages.nextElement();
                cc++;
                Rectangle r = villagePositions.get(current);
                List<Tag> villageTags = TagManager.getSingleton().getTags(current);
                if (villageTags != null && villageTags.size() != 0) {
                    int xcnt = 1;
                    int ycnt = 2;
                    int cnt = 0;
                    for (Tag tag : TagManager.getSingleton().getTags(current)) {
                        if (tag.isShowOnMap()) {
                            int iconType = tag.getTagIcon();
                            Color color = tag.getTagColor();
                            if (color != null || iconType != -1) {
                                int tagX = r.x + r.width - xcnt * tagsize;
                                int tagY = r.y + r.height - ycnt * tagsize;
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
                                        Image tagImage = ImageManager.getUnitImage(iconType, false).getScaledInstance(tagsize, tagsize, Image.SCALE_FAST);
                                        g2d.drawImage(tagImage, tagX, tagY, null);
                                        //check containment using size tolerance
                                        if (MapPanel.getSingleton().getBounds().contains(new Rectangle(tagX, tagY, tagsize + 2, tagsize + 2))) {
                                            copyRegions.put(iconType, new Point(tagX, tagY));
                                        }

                                    } else {
                                        g2d.copyArea(p.x, p.y, tagsize, tagsize, tagX - p.x, tagY - p.y);
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            if (t == currentUserVillage.getTribe()) {
                if (v.equals(currentUserVillage)) {
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
                    troopMark =
                            troopMark.getScaledInstance((int) Math.rint(troopMark.getWidth(null) / currentZoom), (int) Math.rint(troopMark.getHeight(null) / currentZoom), BufferedImage.SCALE_FAST);
                    g2d.drawImage(troopMark, x - troopMark.getWidth(null) / 2, y - troopMark.getHeight(null), null);
                }

            }

            if (markActiveVillage) {
                if (currentUserVillage != null) {
                    if (v.compareTo(currentUserVillage) == 0) {
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
                    double xStart = (attackLine.getX1() - viewStartPoint.x) * width + width / 2;
                    double yStart = (attackLine.getY1() - viewStartPoint.y) * height + height / 2;
                    double xEnd = (attackLine.getX2() - viewStartPoint.x) * width + width / 2;
                    double yEnd = (attackLine.getY2() - viewStartPoint.y) * height + height / 2;
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
                                perc /=
                                        100;
                                double xTar = xStart + (xEnd - xStart) * perc;
                                double yTar = yStart + (yEnd - yStart) * perc;
                                unitXPos =
                                        (int) xTar - unitIcon.getIconWidth() / 2;
                                unitYPos =
                                        (int) yTar - unitIcon.getIconHeight() / 2;
                            } else if ((start > System.currentTimeMillis()) && (arrive > current)) {
                                //attack not running, draw unit in source village
                                unitXPos = (int) xStart - unitIcon.getIconWidth() / 2;
                                unitYPos =
                                        (int) yStart - unitIcon.getIconHeight() / 2;
                            } else {
                                //attack arrived
                                unitXPos = (int) xEnd - unitIcon.getIconWidth() / 2;
                                unitYPos =
                                        (int) yEnd - unitIcon.getIconHeight() / 2;
                            }

                        }
                    }

                    g2d.setColor(attackColors.get(attack.getUnit().getName()));
                    g2d.drawLine((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                    g2d.setColor(Color.YELLOW);
                    if (bounds.contains(attackLine.getP1())) {
                        g2d.fillRect((int) Math.rint(xStart) - 3, (int) Math.rint(yStart) - 1, 6, 6);
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
            /* boolean drawDistance = false;
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

            }*/
            }
        }
        //</editor-fold>

        if (Boolean.parseBoolean(GlobalOptions.getProperty("show.map.popup"))) {
            renderVillageInfo(g2d, mouseVillage);
        }

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

    private void renderVillageInfo(Graphics2D g2d, Village mouseVillage) {
        if (mouseVillage == null) {
            return;
        }

        Tribe t = mouseVillage.getTribe();
        Ally a = null;
        if (t != null) {
            a = mouseVillage.getTribe().getAlly();
        }

        boolean showMoral = Boolean.parseBoolean(GlobalOptions.getProperty("show.popup.moral"));
        boolean showRanks = Boolean.parseBoolean(GlobalOptions.getProperty("show.popup.ranks"));
        boolean showConquers = Boolean.parseBoolean(GlobalOptions.getProperty("show.popup.conquers"));

        Rectangle villageRect = villagePositions.get(mouseVillage);
        if (villageRect == null) {
            return;
        }

        Font before = g2d.getFont();
        Stroke sBefore = g2d.getStroke();
        g2d.setStroke(new BasicStroke(0.5f));
        Font current = new Font("SansSerif", Font.BOLD, 12);
        FontMetrics metrics = g2d.getFontMetrics(current);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        g2d.setFont(current);
        int width = 430;
        //Village name rect
        int dy = 19;
        g2d.setColor(Constants.DS_BACK);
        g2d.fillRect(villageRect.getLocation().x, villageRect.getLocation().y, width, 19);
        g2d.drawRect(villageRect.getLocation().x, villageRect.getLocation().y, width, 19);
        g2d.setColor(Color.BLACK);
        Rectangle2D bounds = metrics.getStringBounds(mouseVillage.getName(), g2d);
        g2d.drawString(mouseVillage.toString(), villageRect.getLocation().x + 2, villageRect.getLocation().y - (int) Math.rint(bounds.getY()) + 2);
        String bonus = getBonusType(mouseVillage);
        if (bonus != null) {
            drawPopupField(g2d, metrics, villageRect, null, bonus, width, dy);
            dy += 19;
        }

        current = new Font("SansSerif", Font.PLAIN, 12);
        metrics = g2d.getFontMetrics(current);
        g2d.setFont(current);

        //Points rect
        String value = nf.format(mouseVillage.getPoints());
        drawPopupField(g2d, metrics, villageRect, "Punkte", value, width, dy);
        dy += 19;

        //tags
        List<Tag> tags = TagManager.getSingleton().getTags(mouseVillage);
        if ((tags != null) && (!tags.isEmpty())) {
            value = "";
            List<String> tagLines = new LinkedList<String>();
            for (int i = 0; i <
                    tags.size(); i++) {
                bounds = metrics.getStringBounds(value + tags.get(i) + ", ", g2d);
                if (bounds.getWidth() > 260) {
                    tagLines.add(value);
                    value = "";
                } else {
                    value += tags.get(i) + ", ";
                }

            }
            //add last line
            if (value.length() > 1) {
                tagLines.add(value);
            }

//render tags
            if (!tagLines.isEmpty()) {
                String line = tagLines.remove(0);
                if (tagLines.isEmpty()) {
                    line = line.substring(0, line.lastIndexOf(","));
                }

                drawPopupField(g2d, metrics, villageRect, "Tags", line, width, dy);

                int lines = tagLines.size();
                for (int i = 0; i < lines - 1; i++) {
                    dy += 19;
                    drawPopupField(g2d, metrics, villageRect, "", tagLines.remove(0), width, dy);
                }

                if (!tagLines.isEmpty()) {
                    dy += 19;
                    line = tagLines.remove(0);
                    line = line.substring(0, line.lastIndexOf(","));
                    drawPopupField(g2d, metrics, villageRect, "", line, width, dy);
                }
                dy += 19;
            }

        }

        //Tribe rect
        if (t != null) {
            if (showRanks) {
                value = t.getName() + " (" + nf.format(t.getPoints()) + " | " + t.getRank() + ")";
                drawPopupField(g2d, metrics, villageRect, "Besitzer (Punkte | Rang)", value, width, dy);
            } else {
                value = t.getName() + " (" + nf.format(t.getPoints()) + ")";
                drawPopupField(g2d, metrics, villageRect, "Besitzer (Punkte)", value, width, dy);
            }

            dy += 19;
            if (showConquers) {
                if (showRanks) {
                    value = nf.format(t.getKillsAtt()) + " (" + nf.format(t.getRankAtt()) + "), " + nf.format(t.getKillsDef()) + " (" + nf.format(t.getRankDef()) + ")";
                    drawPopupField(g2d, metrics, villageRect, "Besiegte Gegner (Off, Def)", value, width, dy);
                } else {
                    value = nf.format(t.getKillsAtt()) + ", " + nf.format(t.getKillsDef()) + ")";
                    drawPopupField(g2d, metrics, villageRect, "Besiegte Gegner (Off, Def)", value, width, dy);
                }

                dy += 19;
            }

            //Ally rect
            if (a != null) {
                if (showRanks) {
                    value = a.getTag() + " (" + nf.format(a.getAll_points()) + " | " + a.getRank() + ")";
                    drawPopupField(g2d, metrics, villageRect, "Stamm (Punkte | Rang)", value, width, dy);
                } else {
                    value = a.getTag() + " (" + nf.format(a.getAll_points()) + ")";
                    drawPopupField(g2d, metrics, villageRect, "Stamm (Punkte)", value, width, dy);
                }

                dy += 19;
            }

            if (showMoral) {
                double moral = ((mouseVillage.getTribe().getPoints() / currentUserVillage.getTribe().getPoints()) * 3 + 0.3) * 100;
                moral =
                        (moral > 100) ? 100 : moral;
                drawPopupField(g2d, metrics, villageRect, "Moral", nf.format(moral) + "%", width, dy);
                dy += 19;
            }

        } else {
            value = "verlassen";
            drawPopupField(g2d, metrics, villageRect, null, value, width, dy);
            dy += 19;
        }

        //render troop/runtime information
        renderExtendedInformation(g2d, mouseVillage, villageRect, width, dy);
        g2d.setFont(before);
        g2d.setStroke(sBefore);
    }

    private String getBonusType(Village pVillage) {
        int bonusType = DataHolder.getSingleton().getCurrentBonusType();
        if (bonusType == 0) {
            switch (pVillage.getType()) {
                case 1: {
                    //holz
                    return "10% mehr Holzproduktion";
                }

                case 2: {
                    //lehm
                    return "10% mehr Lehmproduktion";
                }

                case 3: {
                    //eisen
                    return "10% mehr Eisenproduktion";
                }

                case 4: {
                    return "10% mehr Bevölkerung";
                }

                case 5: {
                    //kaserne
                    return "10% schnellere Produktion in der Kaserne";
                }

                case 6: {
                    //stall
                    return "10% schnellere Produktion im Stall";
                }

                case 7: {
                    //werkstatt
                    return "10% schnellere Produktion in der Werkstatt";
                }

                case 8: {
                    //alle ressourcen
                    return "3% mehr Rohstoffproduktion (alle Rohstoffe)";
                }

            }
        } else {
            switch (pVillage.getType()) {
                case 1: {
                    //holz
                    return "100% mehr Holzproduktion";
                }

                case 2: {
                    //lehm
                    return "100% mehr Lehmproduktion";
                }

                case 3: {
                    //eisen
                    return "100% mehr Eisenproduktion";
                }

                case 4: {
                    //bevölkerung
                    return "10% mehr Bevölkerung";
                }

                case 5: {
                    //kaserne
                    return "50% schnellere Produktion in der Kaserne";
                }

                case 6: {
                    //stall
                    return "50% schnellere Produktion im Stall";
                }

                case 7: {
                    //werkstatt
                    return "100% schnellere Produktion in der Werkstatt";
                }

                case 8: {
                    //alle rohstoffe
                    return "30% mehr Rohstoffproduktion (alle Rohstoffe)";
                }

                case 9: {
                    //speicher + markt
                    return "50% mehr Speicherkapazität und Händler";
                }

            }
        }
        return null;
    }

    private void drawPopupField(Graphics2D g2d, FontMetrics pMetrics, Rectangle pRect, String pName, String pValue, int pWidth, int pDy) {
        g2d.setColor(Constants.DS_BACK_LIGHT);
        g2d.fillRect(pRect.getLocation().x, pRect.getLocation().y + pDy, pWidth, 19);
        g2d.setColor(Constants.DS_BACK);
        g2d.drawRect(pRect.getLocation().x, pRect.getLocation().y + pDy, pWidth, 19);

        int dx = 0;
        if (pName != null) {
            dx = 150;
            g2d.drawRect(pRect.getLocation().x, pRect.getLocation().y + pDy, dx, 19);
            g2d.setColor(Color.BLACK);
            Rectangle2D bounds = pMetrics.getStringBounds(pName, g2d);
            g2d.drawString(pName, pRect.getLocation().x + 2, pRect.getLocation().y + pDy - (int) Math.rint(bounds.getY()) + 2);
        } else {
            g2d.setColor(Color.BLACK);
        }

        Rectangle2D bounds = pMetrics.getStringBounds(pValue, g2d);
        g2d.drawString(pValue, pRect.getLocation().x + dx + 2, pRect.getLocation().y + pDy - (int) Math.rint(bounds.getY()) + 2);
    }

    private void renderExtendedInformation(Graphics2D g2d, Village pMouseVillage, Rectangle pRect, int pWidth, int pDy) {
        VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(pMouseVillage);
        Font current = new Font("SansSerif", Font.PLAIN, 10);
        boolean drawDist = false;
        if (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_MEASURE) {
            if (mSourceVillage != null) {
                current = new Font("SansSerif", Font.PLAIN, 8);
                drawDist =
                        true;
            }

        }
        //if no runtime drawing, check troops
        if (!drawDist) {
            if ((troops == null) || (troops.getTroops().isEmpty())) {
                return;
            }

        }
        g2d.setFont(current);
        FontMetrics metrics = g2d.getFontMetrics(current);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        //draw runtimes/unit count
        g2d.setColor(Constants.DS_BACK_LIGHT);
        g2d.fillRect(pRect.getLocation().x, pRect.getLocation().y + pDy, pWidth, 35);
        g2d.setColor(Constants.DS_BACK);
        g2d.drawRect(pRect.getLocation().x, pRect.getLocation().y + pDy, pWidth, 35);

        int x = 0;
        int w = (int) Math.floor((double) (pWidth - 4.0) / (double) DataHolder.getSingleton().getUnits().size());
        int unitCount = 0;

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (!drawDist) {
                //draw troop information
                int cnt = troops.getTroopsOfUnit(unit);
                if (cnt > 0) {
                    if (unitCount % 2 == 0) {
                        g2d.setColor(Constants.DS_BACK);
                    } else {
                        g2d.setColor(Constants.DS_BACK_LIGHT);
                    }

                    g2d.fillRect(pRect.getLocation().x + x + 2, pRect.getLocation().y + pDy, w, 35);
                    g2d.drawImage(ImageManager.getUnitImage(unit), pRect.getLocation().x + x + 2 + (int) Math.rint(w / 2.0 - 9), pRect.getLocation().y + pDy + 2, null);
                    String troopsValue = nf.format(cnt);
                    Rectangle2D troopBounds = metrics.getStringBounds(troopsValue, g2d);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(troopsValue, pRect.getLocation().x + x + 2 + (int) Math.rint(w / 2.0 - troopBounds.getWidth() / 2.0), pRect.getLocation().y + pDy + 2 + 25 + (int) Math.rint(troopBounds.getHeight() / 2.0));
                    x +=
                            w;
                    unitCount++;

                }




            } else {
                //draw runtime
                double runtime = DSCalculator.calculateMoveTimeInMinutes(mSourceVillage, pMouseVillage, unit.getSpeed());
                if (unitCount % 2 == 0) {
                    g2d.setColor(Constants.DS_BACK);
                } else {
                    g2d.setColor(Constants.DS_BACK_LIGHT);
                }

                g2d.fillRect(pRect.getLocation().x + x + 2, pRect.getLocation().y + pDy, w, 35);
                g2d.drawImage(ImageManager.getUnitImage(unit), pRect.getLocation().x + x + 2 + (int) Math.rint(w / 2.0 - 9), pRect.getLocation().y + pDy + 2, null);
                String runtimeValue = DSCalculator.formatTimeInMinutes(runtime);
                Rectangle2D troopBounds = metrics.getStringBounds(runtimeValue, g2d);
                g2d.setColor(Color.BLACK);
                g2d.drawString(runtimeValue, pRect.getLocation().x + x + 2 + (int) Math.rint(w / 2.0 - troopBounds.getWidth() / 2.0), pRect.getLocation().y + pDy + 2 + 25 + (int) Math.rint(troopBounds.getHeight() / 2.0));
                x +=
                        w;
                unitCount++;

            }




        }
    }

    private void prepareGraphics(Graphics2D pG2d) {
        pG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        pG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // Speed
        pG2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pG2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        pG2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        pG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    //pG2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
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

