/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.FormConfigFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.ServerSettings;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
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
public class MapRenderer extends Thread {

    private static Logger logger = Logger.getLogger("MapRenderer");
    public static final int ALL_LAYERS = 0;
    public static final int MAP_LAYER = 1;
    public static final int MARKER_LAYER = 2;
    public static final int BASIC_DECORATION_LAYER = 3;
    public static final int ATTACK_LAYER = 4;
    public static final int TAG_MARKER_LAYER = 5;
    public static final int LIVE_LAYER = 6;
    public static final int NOTE_LAYER = 7;
    private boolean mapRedrawRequired = true;
    private Village[][] mVisibleVillages = null;
    private HashMap<Village, Rectangle> villagePositions = null;
    private Hashtable<Integer, BufferedImage> mLayers = null;
    private int iVillagesX = 0;
    private int iVillagesY = 0;
    private double dCenterX = 500.0;
    private double dCenterY = 500.0;
    private int xe = 0;
    private int ye = 0;
    private Village mSourceVillage = null;
    private Image mMarkerImage = null;
    private Point2D.Double viewStartPoint = null;
    private double currentFieldWidth = 1.0;
    private double currentFieldHeight = 1.0;
    private double currentZoom = 0.0;
    private Village currentUserVillage = null;
    private BufferedImage mConquerWarning = null;
    private List<Integer> drawOrder = null;
    private Popup infoPopup = null;
    private Village popupVillage = null;
    private BufferedImage mBackBuffer = null;
    private BufferedImage mFrontBuffer = null;
    private long lRenderedLast = 0;
    private long lCurrentSleepTime = 50;
    private int iCurrentFPS = 0;
    private float alpha = 0.0f;
    private Hashtable<Ally, Integer> allyCount = new Hashtable<Ally, Integer>();
    private Hashtable<Tribe, Integer> tribeCount = new Hashtable<Tribe, Integer>();
    private Hashtable<Village, AnimatedVillageInfoRenderer> animators = new Hashtable<Village, AnimatedVillageInfoRenderer>();

    /* private Canvas mCanvas = null;
    BufferStrategy strategy;*/

    /*  public MapRenderer(Canvas pCanvas) {
    mCanvas = pCanvas;
     */
    public MapRenderer() {
        mVisibleVillages = new Village[iVillagesX][iVillagesY];

        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true);
        try {
            //load flag marker
            mMarkerImage = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/res/marker.png"));
            mConquerWarning = ImageIO.read(new File("./graphics/icons/warning.png"));
        } catch (Exception e) {
            logger.error("Failed to load marker images", e);
        }

        mLayers = new Hashtable<Integer, BufferedImage>();
        drawOrder = new LinkedList<Integer>();
        Vector<String> layerVector = new Vector<String>(Constants.LAYER_COUNT);
        for (int i = 0; i < Constants.LAYER_COUNT; i++) {
            layerVector.add("");
        }

        Enumeration<String> values = Constants.LAYERS.keys();
        while (values.hasMoreElements()) {
            String layer = values.nextElement();
            layerVector.set(Constants.LAYERS.get(layer), layer);
        }

        for (String s : layerVector) {
            drawOrder.add(Constants.LAYERS.get(s));
        }
    }

    /**Set the order all layers are drawn
     * @param pDrawOrder
     */
    public void setDrawOrder(List<Integer> pDrawOrder) {
        System.out.println("Order " + pDrawOrder);
        drawOrder = new LinkedList<Integer>(pDrawOrder);
    }

    /**Complete redraw on resize or scroll
     * @param pType
     */
    public synchronized void initiateRedraw(int pType) {
        /* if (mapRedrawRequired) {
        return;
        }*/

        if (pType == TAG_MARKER_LAYER) {
            rend5.reset();
            //System.out.println("TAG");
        }

        if (pType == MARKER_LAYER) {
            ////  System.out.println("MARK");
            rend.reset();
            mapRedrawRequired = true;
        }

        if (pType == NOTE_LAYER) {
            //  System.out.println("NOTE");
            rend8.reset();
        }

        if (pType == ALL_LAYERS) {
            rend.reset();
            rend2.reset();
            rend5.reset();
            rend8.reset();
            mapRedrawRequired = true;
        }
        if (pType == MAP_LAYER) {
            //   System.out.println("MAP");
            mapRedrawRequired = true;
        }
    }

    public synchronized boolean isRedrawScheduled() {
        return mapRedrawRequired;
    }
    private MapLayerRenderer rend = new MapLayerRenderer();
    private TroopDensityLayerRenderer rend2 = new TroopDensityLayerRenderer();
    private ChurchLayerRenderer rend3 = new ChurchLayerRenderer();
    private FormLayerRenderer rend4 = new FormLayerRenderer();
    private TagMarkerLayerRenderer rend5 = new TagMarkerLayerRenderer();
    private AttackLayerRenderer rend6 = new AttackLayerRenderer();
    private SupportLayerRenderer rend7 = new SupportLayerRenderer();
    private NoteLayerRenderer rend8 = new NoteLayerRenderer();

    public void renderAll(Graphics2D pG2d) {
        try {
            int w = MapPanel.getSingleton().getWidth();
            int h = MapPanel.getSingleton().getHeight();
            //Graphics2D g2d = (Graphics2D) MapPanel.getSingleton().getStrategy().getDrawGraphics();
            if ((w != 0) && (h != 0)) {
                Graphics2D g2d = null;
                if (mBackBuffer == null) {
                    //create main buffer during first iteration
                    mBackBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.OPAQUE);
                    mBackBuffer.setAccelerationPriority(1);
                    mFrontBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.TRANSLUCENT);
                    BufferedImage bi = ImageUtils.createCompatibleBufferedImage(3, 3, Transparency.TRANSLUCENT);
                    Graphics2D g2 = bi.createGraphics();
                    ImageUtils.setupGraphics(g2);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, 3, 3);
                    g2.setColor(new Color(168, 168, 168));
                    g2.drawLine(0, 2, 2, 0);

                    g2.dispose();
                    TexturePaint tp = new TexturePaint(bi, new Rectangle2D.Double(0, 0, 3, 3));
                    Graphics2D g2d1 = mFrontBuffer.createGraphics();
                    ImageUtils.setupGraphics(g2d1);
                    g2d1.setPaint(tp);
                    g2d1.fillRect(0, 0, w, h);
                    g2d1.dispose();
                    g2d = (Graphics2D) mBackBuffer.getGraphics();
                    ImageUtils.setupGraphics(g2d);
                    //set redraw required flag if nothin was drawn yet
                    mapRedrawRequired = true;
                } else {
                    //check if image size is still valid
                    //if not re-create main buffer
                    if (mBackBuffer.getWidth(null) != w || mBackBuffer.getHeight(null) != h) {
                        //map panel has resized
                        mBackBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.OPAQUE);
                        mBackBuffer.setAccelerationPriority(1);
                        mFrontBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.TRANSLUCENT);
                        BufferedImage bi = ImageUtils.createCompatibleBufferedImage(3, 3, Transparency.TRANSLUCENT);
                        Graphics2D g2 = bi.createGraphics();
                        ImageUtils.setupGraphics(g2);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
                        g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, 3, 3);
                        g2.setColor(new Color(107, 107, 107));
                        g2.drawLine(0, 2, 2, 0);
                        g2.dispose();
                        TexturePaint tp = new TexturePaint(bi, new Rectangle2D.Double(0, 0, 3, 3));
                        Graphics2D g2d1 = mFrontBuffer.createGraphics();
                        ImageUtils.setupGraphics(g2d1);
                        g2d1.setPaint(tp);
                        g2d1.fillRect(0, 0, w, h);
                        g2d1.dispose();
                        g2d = (Graphics2D) mBackBuffer.getGraphics();
                        ImageUtils.setupGraphics(g2d);
                        //set redraw required flag if size has changed
                        initiateRedraw(ALL_LAYERS);
                        mapRedrawRequired = true;
                    } else {
                        //only get graphics
                        g2d = (Graphics2D) mBackBuffer.getGraphics();
                    }
                }
                g2d.setClip(0, 0, w, h);
                //get currently selected user village for marking -> one call reduces sync effort
                currentUserVillage = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                //check if redraw required
                RenderSettings settings = new RenderSettings(MapPanel.getSingleton().getVirtualBounds());
                currentZoom = settings.getZoom();
                if (mapRedrawRequired) {
                    //complete redraw is required
                    calculateVisibleVillages();
                    if (viewStartPoint == null) {
                        throw new Exception("View position is 'null', skip redraw");
                    }
                    mapRedrawRequired = false;
                }
                settings.setVisibleVillages(mVisibleVillages);
                settings.calculateSettings(MapPanel.getSingleton().getVirtualBounds());
                boolean mapDrawn = false;
                for (Integer layer : drawOrder) {
                    if (layer == 0) {
                        rend.setMarkOnTop(mapDrawn);
                        rend.performRendering(settings, g2d);
                    } else if (layer == 1) {
                        //Here the mapDrawn flag is set.
                        //If this flag is set before layer 0 was drawn, MarkOnTop mode is active.
                        mapDrawn = true;
                    } else if (layer == 2) {
                        rend5.performRendering(settings, g2d);
                    } else if (layer == 3) {
                        //render other layers (active village, troop type)
                        // if (mapDrawn) {
                        //only draw layer if map is drawn
                        //If not, this layer is hidden behind the map
                        //  renderDecoration(g2d);
                        // System.out.println("DTD " + (System.currentTimeMillis() - s));
                        //  }
                        //  logger.info(" - DECO " + (System.currentTimeMillis() - s));
                    } else if (layer == 4) {
                        //render troop density
                        rend2.performRendering(settings, g2d);
                    } else if (layer == 5) {
                        rend8.performRendering(settings, g2d);
                    } else if (layer == 6) {
                        rend6.performRendering(settings, g2d);
                    } else if (layer == 7) {
                        rend7.performRendering(settings, g2d);
                    } else if (layer == 8) {
                        rend4.performRendering(settings, g2d);
                    } else if (layer == 9) {
                        rend3.performRendering(settings, g2d);
                    }
                }
                //draw live layer -> always on top
                renderLiveLayer(g2d);
                //render selection
                de.tor.tribes.types.Rectangle selection = MapPanel.getSingleton().getSelectionRect();
                if (selection != null) {
                    selection.renderForm(g2d);
                }
                //render menu
                MenuRenderer.getSingleton().renderMenu(g2d);
                /*   if (MapPanel.getSingleton().requiresAlphaBlending()) {
                g2d.drawImage(mFrontBuffer, 0, 0, null);
                }*/
                g2d.dispose();

                pG2d.drawImage(mBackBuffer, 0, 0, null);
                MapPanel.getSingleton().updateComplete(villagePositions, mBackBuffer);
                //  System.out.println("Complete " + mBackBuffer);

            }
        } catch (Throwable t) {
            lRenderedLast = 0;
            logger.error("Redrawing map failed", t);
        }
    }

    /**Render loop*/
    @Override
    public void run() {
        logger.debug("Entering render loop");
        if (true) {
            return;
        }
        while (true) {
            long s1 = System.currentTimeMillis();
            //get global max. fps
            int fps = 10;
            try {
                fps = Integer.parseInt(GlobalOptions.getProperty("max.fps"));
            } catch (Exception e) {
                fps = 10;
            }
            try {
                int w = MapPanel.getSingleton().getWidth();
                int h = MapPanel.getSingleton().getHeight();
                //Graphics2D g2d = (Graphics2D) MapPanel.getSingleton().getStrategy().getDrawGraphics();
                if ((w != 0) && (h != 0)) {
                    Graphics2D g2d = null;
                    if (mBackBuffer == null) {
                        //create main buffer during first iteration
                        mBackBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.OPAQUE);
                        mBackBuffer.setAccelerationPriority(1);
                        mFrontBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.TRANSLUCENT);
                        BufferedImage bi = ImageUtils.createCompatibleBufferedImage(3, 3, Transparency.TRANSLUCENT);
                        Graphics2D g2 = bi.createGraphics();
                        ImageUtils.setupGraphics(g2);
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
                        g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, 3, 3);
                        g2.setColor(new Color(168, 168, 168));
                        g2.drawLine(0, 2, 2, 0);

                        g2.dispose();
                        TexturePaint tp = new TexturePaint(bi, new Rectangle2D.Double(0, 0, 3, 3));
                        Graphics2D g2d1 = mFrontBuffer.createGraphics();
                        ImageUtils.setupGraphics(g2d1);
                        g2d1.setPaint(tp);
                        g2d1.fillRect(0, 0, w, h);
                        g2d1.dispose();
                        g2d = (Graphics2D) mBackBuffer.getGraphics();
                        ImageUtils.setupGraphics(g2d);
                        //set redraw required flag if nothin was drawn yet
                        mapRedrawRequired = true;
                    } else {
                        //check if image size is still valid
                        //if not re-create main buffer
                        if (mBackBuffer.getWidth(null) != w || mBackBuffer.getHeight(null) != h) {
                            //map panel has resized
                            mBackBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.OPAQUE);
                            mBackBuffer.setAccelerationPriority(1);
                            mFrontBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.TRANSLUCENT);
                            BufferedImage bi = ImageUtils.createCompatibleBufferedImage(3, 3, Transparency.TRANSLUCENT);
                            Graphics2D g2 = bi.createGraphics();
                            ImageUtils.setupGraphics(g2);
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
                            g2.setColor(Color.WHITE);
                            g2.fillRect(0, 0, 3, 3);
                            g2.setColor(new Color(107, 107, 107));
                            g2.drawLine(0, 2, 2, 0);
                            g2.dispose();
                            TexturePaint tp = new TexturePaint(bi, new Rectangle2D.Double(0, 0, 3, 3));
                            Graphics2D g2d1 = mFrontBuffer.createGraphics();
                            ImageUtils.setupGraphics(g2d1);
                            g2d1.setPaint(tp);
                            g2d1.fillRect(0, 0, w, h);
                            g2d1.dispose();
                            g2d = (Graphics2D) mBackBuffer.getGraphics();
                            ImageUtils.setupGraphics(g2d);
                            //set redraw required flag if size has changed
                            initiateRedraw(ALL_LAYERS);
                            mapRedrawRequired = true;
                        } else {
                            //only get graphics
                            g2d = (Graphics2D) mBackBuffer.getGraphics();
                        }
                    }
                    g2d.setClip(0, 0, w, h);
                    //get currently selected user village for marking -> one call reduces sync effort
                    currentUserVillage = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                    //check if redraw required
                    long s = System.currentTimeMillis();
                    RenderSettings settings = new RenderSettings(MapPanel.getSingleton().getVirtualBounds());
                    currentZoom = settings.getZoom();
                    if (mapRedrawRequired) {
                        //complete redraw is required
                        calculateVisibleVillages();
                        if (viewStartPoint == null) {
                            throw new Exception("View position is 'null', skip redraw");
                        }
                        mapRedrawRequired = false;
                    }
                    settings.setVisibleVillages(mVisibleVillages);
                    settings.calculateSettings(MapPanel.getSingleton().getVirtualBounds());
                    boolean mapDrawn = false;
                    for (Integer layer : drawOrder) {
                        if (layer == 0) {
                            rend.setMarkOnTop(mapDrawn);
                            rend.performRendering(settings, g2d);
                        } else if (layer == 1) {
                            //Here the mapDrawn flag is set.
                            //If this flag is set before layer 0 was drawn, MarkOnTop mode is active.
                            mapDrawn = true;
                        } else if (layer == 2) {
                            rend5.performRendering(settings, g2d);
                        } else if (layer == 3) {
                            //render other layers (active village, troop type)
                            // if (mapDrawn) {
                            //only draw layer if map is drawn
                            //If not, this layer is hidden behind the map
                            //  renderDecoration(g2d);
                            // System.out.println("DTD " + (System.currentTimeMillis() - s));
                            //  }
                            //  logger.info(" - DECO " + (System.currentTimeMillis() - s));
                        } else if (layer == 4) {
                            //render troop density 
                            rend2.performRendering(settings, g2d);
                        } else if (layer == 5) {
                            rend8.performRendering(settings, g2d);
                        } else if (layer == 6) {
                            rend6.performRendering(settings, g2d);
                        } else if (layer == 7) {
                            rend7.performRendering(settings, g2d);
                        } else if (layer == 8) {
                            rend4.performRendering(settings, g2d);
                        } else if (layer == 9) {
                            rend3.performRendering(settings, g2d);
                        }
                    }
                    //draw live layer -> always on top
                    renderLiveLayer(g2d);
                    //render selection
                    de.tor.tribes.types.Rectangle selection = MapPanel.getSingleton().getSelectionRect();
                    if (selection != null) {
                        selection.renderForm(g2d);
                    }
                    //render menu
                    MenuRenderer.getSingleton().renderMenu(g2d);
                    /*
                    if (MapPanel.getSingleton().requiresAlphaBlending()) {
                    g2d.drawImage(mFrontBuffer, 0, 0, null);
                    }*/
                    g2d.dispose();

                    MapPanel.getSingleton().updateComplete(villagePositions, mBackBuffer);
                }

            } catch (Throwable t) {
                lRenderedLast = 0;
                logger.error("Redrawing map failed", t);
            }
            System.out.println("RenRun: " + (System.currentTimeMillis() - s1));
            yield();

            //FPS steering
            if (lRenderedLast == 0) {
                iCurrentFPS++;
                lRenderedLast = System.currentTimeMillis();
            } else {
                iCurrentFPS++;
                long dur = System.currentTimeMillis() - lRenderedLast;
                if (dur >= 1000) {
                    System.out.println("FPS: " + iCurrentFPS);
                    if (iCurrentFPS < fps && lCurrentSleepTime > 40) {
                        lCurrentSleepTime -= 10;
                    } else {
                        lCurrentSleepTime += 10;
                    }
                    lRenderedLast = System.currentTimeMillis();
                    iCurrentFPS = 0;
                }
            }
        }
    }

    /**Set the drag line externally (done by MapPanel class)
     * @param pXS
     * @param pYS
     * @param pXE
     * @param pYE 
     */
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
        dCenterY = MapPanel.getSingleton().getCurrentPosition().y;
        villagePositions = new HashMap<Village, Rectangle>();
        allyCount.clear();
        tribeCount.clear();
        if (DataHolder.getSingleton().getVillages() == null) {
            //probably reloading data
            return;
        }

        currentFieldWidth = GlobalOptions.getSkin().getCurrentFieldWidth(currentZoom);
        currentFieldHeight = GlobalOptions.getSkin().getCurrentFieldHeight(currentZoom);

        //ceil
        iVillagesX = (int) Math.ceil((double) MapPanel.getSingleton().getWidth() / currentFieldWidth);
        iVillagesY = (int) Math.ceil((double) MapPanel.getSingleton().getHeight() / currentFieldHeight);
        //add small buffer
     /*   iVillagesX++;
        iVillagesY++;*/



        //village start

        int xStartVillage = (int) Math.floor(dCenterX - iVillagesX / 2.0);
        int yStartVillage = (int) Math.floor(dCenterY - iVillagesY / 2.0);
        //double start

        double dXStart = dCenterX - (double) iVillagesX / 2.0;
        double dYStart = dCenterY - (double) iVillagesY / 2.0;

        //village end

        /*int xEndVillage = (int) Math.ceil(dCenterX + (double) iVillagesX / 2.0);
        int yEndVillage = (int) Math.ceil(dCenterY + (double) iVillagesY / 2.0);
         */
        int xEndVillage = (int) Math.floor(dXStart + iVillagesX);
        int yEndVillage = (int) Math.floor(dYStart + iVillagesY);
        /*  xEndVillage++;
        yEndVillage++;*/

        //   System.out.println("Start: " + dXStart + "/" + dYStart);

        //correct village count
        viewStartPoint = new Point2D.Double(dXStart, dYStart);

        //       System.out.println(viewStartPoint);
        double dx = 0 - ((viewStartPoint.x - Math.floor(viewStartPoint.x)) * currentFieldWidth);
        double dy = 0 - ((viewStartPoint.y - Math.floor(viewStartPoint.y)) * currentFieldHeight);

        if (dx * currentFieldWidth + iVillagesX * currentFieldWidth < MapPanel.getSingleton().getWidth()) {
            xEndVillage++;
        }
        if (dy * currentFieldHeight + iVillagesY * currentFieldHeight < MapPanel.getSingleton().getHeight()) {
            yEndVillage++;
        }
        iVillagesX = xEndVillage - xStartVillage;
        iVillagesY = yEndVillage - yStartVillage;
        /*  System.out.println("Start: " + xStartVillage + "/" + yStartVillage);
        System.out.println("End: " + xEndVillage + "/" + yEndVillage);
        System.out.println("XY " + iVillagesX + "/" + iVillagesY);*/
        mVisibleVillages = new Village[iVillagesX + 1][iVillagesY + 1];

        int x = 0;
        int y = 0;

        for (int i = xStartVillage; i <= xEndVillage; i++) {
            for (int j = yStartVillage; j <= yEndVillage; j++) {
                int mapW = ServerSettings.getSingleton().getMapDimension().width;
                int mapH = ServerSettings.getSingleton().getMapDimension().height;
                if ((i < 0) || (i > mapW - 1) || (j < 0) || (j > mapH - 1)) {
                    //handle villages outside map
                    mVisibleVillages[x][y] = null;
                } else {
                    mVisibleVillages[x][y] = DataHolder.getSingleton().getVillages()[i][j];
                    if (mVisibleVillages[x][y] != null) {

                        Point villagePos = new Point((int) Math.floor(dx + x * currentFieldWidth), (int) Math.floor(dy + y * currentFieldHeight));
                        villagePositions.put(mVisibleVillages[x][y], new Rectangle(villagePos.x, villagePos.y, (int) Math.floor(currentFieldWidth), (int) Math.floor(currentFieldHeight)));
                        Tribe t = mVisibleVillages[x][y].getTribe();
                        if (t != Barbarians.getSingleton()) {
                            if (tribeCount.get(t) == null) {
                                tribeCount.put(t, 1);
                            } else {
                                tribeCount.put(t, tribeCount.get(t) + 1);
                            }
                            Ally a = t.getAlly();
                            if (a == null) {
                                a = NoAlly.getSingleton();
                            }
                            if (allyCount.get(a) == null) {
                                allyCount.put(a, 1);
                            } else {
                                allyCount.put(a, allyCount.get(a) + 1);
                            }
                        }
                    }
                }
                y++;
            }
            x++;
            y = 0;
        }

        MapPanel.getSingleton().updateVirtualBounds(viewStartPoint);
    }

    public double getCurrentZoom() {
        return currentZoom;
    }

    public Hashtable<Tribe, Integer> getTribeCount() {
        return tribeCount;
    }

    public Hashtable<Ally, Integer> getAllyCount() {
        return allyCount;
    }

    /**Render e.g. drag line, radar, popup*/
    private void renderLiveLayer(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        Village mouseVillage = MapPanel.getSingleton().getVillageAtMousePos();
        //render temp form
        if (!FormConfigFrame.getSingleton().isInEditMode()) {
            //only render in create mode to avoid multi-drawing
            AbstractForm f = FormConfigFrame.getSingleton().getCurrentForm();

            if (f != null) {
                f.renderForm(g2d);
            }
        }
        // <editor-fold defaultstate="collapsed" desc="Mark current players villages">

        if (Boolean.parseBoolean(GlobalOptions.getProperty("highlight.tribes.villages"))) {
            Tribe mouseTribe = Barbarians.getSingleton();
            if (mouseVillage != null) {
                mouseTribe = mouseVillage.getTribe();
                if (mouseTribe == null) {
                    mouseTribe = Barbarians.getSingleton();
                }

            } else {
                mouseTribe = null;
            }

            Composite c = g2d.getComposite();

            Paint p = g2d.getPaint();
            if (mouseTribe != null) {
                //Rectangle copy = null;
                Iterator<Village> keys = villagePositions.keySet().iterator();
                while (keys.hasNext()) {
                    Village v = keys.next();
                    if ((v.getTribe() == null && mouseTribe.equals(Barbarians.getSingleton())) || (v.getTribe() != null && mouseTribe.equals(v.getTribe()))) {
                        Rectangle r = villagePositions.get(v);
                        // if (copy == null) {
                        Ellipse2D ellipse = new Ellipse2D.Float(r.x, r.y, r.height, r.height);
                        g2d.setPaint(new RoundGradientPaint(r.getCenterX(), r.getCenterY(), Color.yellow, new Point2D.Double(0, r.height / 2), new Color(0, 0, 0, 0)));
                        g2d.fill(ellipse);
                    }
                }
            }
            g2d.setPaint(p);
            g2d.setComposite(c);
        }
// </editor-fold>
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
                dragLine.setLine((mSourceVillage.getX() - viewStartPoint.x) * (int) Math.rint(currentFieldWidth), (mSourceVillage.getY() - viewStartPoint.y) * (int) Math.rint(currentFieldHeight), xe, ye);
            }

            if ((dragLine.getX2() != 0) && (dragLine.getY2() != 0)) {
                int x1 = (int) dragLine.getX1();
                int y1 = (int) dragLine.getY1();
                int x2 = (int) dragLine.getX2();
                int y2 = (int) dragLine.getY2();
                g2d.drawLine(x1, y1, x2, y2);
            }

        }
        //</editor-fold>
        // <editor-fold defaultstate="collapsed" desc=" Draw radar information ">
        Village radarVillage = MapPanel.getSingleton().getRadarVillage();
        List<Village> radarVillages = new LinkedList<Village>();
        //add radar village
        if (radarVillage != null) {
            radarVillages.add(radarVillage);
        }

//add mouse village if radar tool is selected
        if (mouseVillage != null && MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_RADAR) {
            //check if radar village == mouse village
            if (!radarVillages.contains(mouseVillage)) {
                radarVillages.add(mouseVillage);
            }

        }

        for (Village v : radarVillages) {
            try {
                int cnt = 0;
                for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                    de.tor.tribes.types.Circle c = new de.tor.tribes.types.Circle();
                    int r = Integer.parseInt(GlobalOptions.getProperty("radar.size"));
                    double diam = 2 * (double) r / u.getSpeed();
                    double xp = v.getX() + 0.5 - diam / 2;
                    double yp = v.getY() + 0.5 - diam / 2;
                    double cx = v.getX() + 0.5;
                    double cy = v.getY() + 0.5;

                    double xi = Math.cos(Math.toRadians(270 + cnt * (10))) * diam / 2;
                    double yi = Math.sin(Math.toRadians(270 + cnt * 10)) * diam / 2;
                    c.setXPos(xp);
                    c.setYPos(yp);
                    c.setFilled(false);
                    c.setXPosEnd(xp + diam);
                    c.setYPosEnd(yp + diam);
                    Color co = Color.RED;
                    try {
                        co = Color.decode(GlobalOptions.getProperty(u.getName() + ".color"));
                    } catch (Exception e) {
                    }
                    c.setDrawColor(co);
                    c.setDrawAlpha(0.8f);
                    c.renderForm(g2d);
                    Image unit = ImageManager.getUnitIcon(u).getImage();
                    Point p = MapPanel.getSingleton().virtualPosToSceenPos((cx + xi), (cy + yi));
                    g2d.drawImage(unit, p.x - (int) ((double) unit.getWidth(null) / 2), (int) ((double) p.y - unit.getHeight(null) / 2), null);
                    cnt++;
                }
                g2d.setClip(null);

            } catch (Exception e) {
            }
        }
// </editor-fold>

        //draw additional info
        if (mouseVillage != null && Boolean.parseBoolean(GlobalOptions.getProperty("show.mouseover.info"))) {
            Rectangle rect = villagePositions.get(mouseVillage);
            AnimatedVillageInfoRenderer animator = animators.get(mouseVillage);
            if (animator == null) {
                animator = new AnimatedVillageInfoRenderer(mouseVillage);
                animators.put(mouseVillage, animator);
            }
            animator.update(mouseVillage, rect, g2d);
        }
        Enumeration<Village> animatorKeys = animators.keys();
        while (animatorKeys.hasMoreElements()) {
            Village keyVillage = animatorKeys.nextElement();
            AnimatedVillageInfoRenderer animator = animators.get(keyVillage);
            if (animator.isFinished()) {
                animators.remove(keyVillage);
            } else {
                animator.update(mouseVillage, villagePositions.get(animator.getVillage()), g2d);
            }
        }

        List<Village> marked = MapPanel.getSingleton().getMarkedVillages();
        if (!marked.isEmpty()) {
            Iterator<Village> villages = villagePositions.keySet().iterator();
            Color cBefore = g2d.getColor();
            while (villages.hasNext()) {
                Village v = villages.next();
                Rectangle villageRect = villagePositions.get(v);
                if (marked.contains(v)) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillOval(villageRect.x + villageRect.width - 10, villageRect.y, 10, 10);
                }
            }
            g2d.setColor(cBefore);
        }
        if (Boolean.parseBoolean(GlobalOptions.getProperty("show.ruler"))) {
            //ruler drawing
            Hashtable<Color, Rectangle> vertRulerParts = new Hashtable<Color, Rectangle>();
            Hashtable<Color, Rectangle> horRulerParts = new Hashtable<Color, Rectangle>();
            double xVillage = Math.floor(viewStartPoint.x);
            double yVillage = Math.floor(viewStartPoint.y);
            double rulerStart = -1 * currentFieldWidth * (viewStartPoint.x - xVillage);
            double rulerEnd = -1 * currentFieldHeight * (viewStartPoint.y - yVillage);
            Composite com = g2d.getComposite();
            Color c = g2d.getColor();
            for (int i = 0; i < mVisibleVillages.length; i++) {
                for (int j = 0; j < mVisibleVillages[0].length; j++) {
                    if (i == 0) {
                        //draw vertical ruler
                        if ((yVillage + j) % 2 == 0) {
                            g2d.setColor(Constants.DS_BACK_LIGHT);
                        } else {
                            g2d.setColor(Constants.DS_BACK);
                        }

                        Rectangle rulerPart = vertRulerParts.get(g2d.getColor());
                        if (rulerPart == null) {
                            rulerPart = new Rectangle(0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(currentFieldHeight), (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight));
                            if (MapPanel.getSingleton().getBounds().contains(rulerPart)) {
                                vertRulerParts.put(g2d.getColor(), rulerPart);
                            }

                            if (g2d.getColor() == Constants.DS_BACK) {
                                g2d.fill3DRect(0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(currentFieldHeight), (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight), true);
                            } else {
                                g2d.fillRect(0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(currentFieldHeight), (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight));
                            }

                        } else {
                            g2d.copyArea(rulerPart.x, rulerPart.y, rulerPart.width, rulerPart.height, (int) Math.floor(rulerStart) - rulerPart.x, (int) Math.floor(rulerEnd) + j * (int) Math.rint(currentFieldHeight) - rulerPart.y);
                        }

                        if (mouseVillage != null && mouseVillage.getY() == (yVillage + j)) {
                            g2d.setColor(Color.YELLOW);
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                            g2d.fillRect(0, rulerPart.y, rulerPart.width, rulerPart.height);
                            g2d.setComposite(com);
                        }
                    }
                    if (j == mVisibleVillages[0].length - 1) {
                        //draw horizontal ruler
                        if ((xVillage + i) % 2 == 0) {
                            g2d.setColor(Constants.DS_BACK_LIGHT);
                        } else {
                            g2d.setColor(Constants.DS_BACK);
                        }

                        Rectangle rulerPart = horRulerParts.get(g2d.getColor());
                        if (rulerPart == null) {
                            rulerPart = new Rectangle((int) Math.floor(rulerStart) + i * (int) Math.rint(currentFieldWidth), 0, (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight));
                            if (MapPanel.getSingleton().getBounds().contains(rulerPart)) {
                                horRulerParts.put(g2d.getColor(), rulerPart);
                            }

                            if (g2d.getColor() == Constants.DS_BACK) {
                                g2d.fill3DRect((int) Math.floor(rulerStart) + i * (int) Math.rint(currentFieldWidth), 0, (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight), true);
                            } else {
                                g2d.fillRect((int) Math.floor(rulerStart) + i * (int) Math.rint(currentFieldWidth), 0, (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight));
                            }

                        } else {
                            g2d.copyArea(rulerPart.x, rulerPart.y, rulerPart.width, rulerPart.height, (int) Math.floor(rulerStart) + i * (int) Math.rint(currentFieldWidth) - rulerPart.x, (int) Math.floor(rulerEnd) - rulerPart.y);
                        }

                        if (mouseVillage != null && mouseVillage.getX() == (xVillage + i)) {
                            g2d.setColor(Color.YELLOW);
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                            g2d.fillRect(rulerPart.x, 0, rulerPart.width, rulerPart.height);
                            g2d.setComposite(com);
                        }
                    }
                }
            }

            g2d.setComposite(com);
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < mVisibleVillages.length; i++) {
                for (int j = 0; j < mVisibleVillages[0].length; j++) {
                    if (i == 0 && j != 0) {
                        //draw vertical values
                        if ((yVillage + j) % 2 == 0) {
                            g2d.setColor(Color.DARK_GRAY);
                        } else {
                            g2d.setColor(Color.BLACK);
                        }

                        String coord = Integer.toString((int) yVillage + j);
                        double w = g2d.getFontMetrics().getStringBounds(coord, g2d).getWidth();
                        double fact = (double) ((int) Math.rint(currentFieldWidth) - 2) / w;
                        AffineTransform f = g2d.getTransform();
                        AffineTransform t = AffineTransform.getTranslateInstance(0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(currentFieldHeight));
                        t.scale(fact, 1);
                        g2d.setTransform(t);
                        g2d.drawString(coord, 1, (int) Math.rint(currentFieldHeight) - 2);
                        g2d.setTransform(f);
                    } else if (i != 0 && j == 0) {
                        //draw horizontal values
                        String coord = Integer.toString((int) xVillage + i);
                        double w = g2d.getFontMetrics().getStringBounds(coord, g2d).getWidth();
                        double fact = (double) ((int) Math.rint(currentFieldWidth) - 2) / w;
                        int dy = -2;
                        if ((xVillage + i) % 2 == 0) {
                            g2d.setColor(Color.DARK_GRAY);
                        } else {
                            g2d.setColor(Color.BLACK);
                        }

                        AffineTransform f = g2d.getTransform();
                        AffineTransform t = AffineTransform.getTranslateInstance((int) Math.floor(rulerStart) + i * (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight));
                        t.scale(fact, 1);
                        g2d.setTransform(t);
                        g2d.drawString(coord, 1, dy);
                        g2d.setTransform(f);
                    }
                }
            }
            //insert 'stopper'
            g2d.setColor(Constants.DS_BACK);
            g2d.fill3DRect(0, 0, (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight), true);
            g2d.setColor(c);
        }

        if (Boolean.parseBoolean(GlobalOptions.getProperty("show.map.popup"))) {
            try {
                if (DSWorkbenchMainFrame.getSingleton().isActive() && MapPanel.getSingleton().getMousePosition() != null) {
                    if (mouseVillage == null) {
                        if (infoPopup != null) {
                            infoPopup.hide();
                            infoPopup = null;
                        }

                        popupVillage = null;
                    } else {
                        if (!mouseVillage.equals(popupVillage)) {
                            if (infoPopup != null) {
                                infoPopup.hide();
                            }

                            popupVillage = mouseVillage;
                            JToolTip tt = new JToolTip();

                            tt.setTipText(popupVillage.getExtendedTooltip());
                            PopupFactory popupFactory = PopupFactory.getSharedInstance();
                            infoPopup = popupFactory.getPopup(MapPanel.getSingleton(), tt, MouseInfo.getPointerInfo().getLocation().x + 10, MouseInfo.getPointerInfo().getLocation().y + 10);
                            // JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                            infoPopup.show();
                        }
                    }
                } else {
                    if (infoPopup != null) {
                        infoPopup.hide();
                        infoPopup = null;
                    }

                    popupVillage = null;
                }

            } catch (Exception e) {
                if (infoPopup != null) {
                    infoPopup.hide();
                    infoPopup = null;
                }

                popupVillage = null;
            }

        } else {
            //no popup shown
            if (infoPopup != null) {
                infoPopup.hide();
                popupVillage = null;
            }
        }
    }
}

class RoundGradientPaint implements Paint {

    protected Point2D point;
    protected Point2D mRadius;
    protected Color mPointColor, mBackgroundColor;

    public RoundGradientPaint(double x, double y, Color pointColor,
            Point2D radius, Color backgroundColor) {
        if (radius.distance(0, 0) <= 0) {
            throw new IllegalArgumentException("Radius must be greater than 0.");
        }
        point = new Point2D.Double(x, y);
        mPointColor = pointColor;
        mRadius = radius;
        mBackgroundColor = backgroundColor;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
            Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
        Point2D transformedPoint = xform.transform(point, null);
        Point2D transformedRadius = xform.deltaTransform(mRadius, null);
        return new RoundGradientContext(transformedPoint, mPointColor,
                transformedRadius, mBackgroundColor);
    }

    @Override
    public int getTransparency() {
        int a1 = mPointColor.getAlpha();
        int a2 = mBackgroundColor.getAlpha();
        return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
    }
}

class RoundGradientContext implements PaintContext {

    protected Point2D mPoint;
    protected Point2D mRadius;
    protected Color color1, color2;

    public RoundGradientContext(Point2D p, Color c1, Point2D r, Color c2) {
        mPoint = p;
        color1 = c1;
        mRadius = r;
        color2 = c2;
    }

    @Override
    public void dispose() {
    }

    @Override
    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    @Override
    public Raster getRaster(int x, int y, int w, int h) {
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);

        int[] data = new int[w * h * 4];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                double distance = mPoint.distance(x + i, y + j);
                double radius = mRadius.distance(0, 0);
                double ratio = distance / radius;
                if (ratio > 1.0) {
                    ratio = 1.0;
                }

                int base = (j * w + i) * 4;
                data[base + 0] = (int) (color1.getRed() + ratio * (color2.getRed() - color1.getRed()));
                data[base + 1] = (int) (color1.getGreen() + ratio * (color2.getGreen() - color1.getGreen()));
                data[base + 2] = (int) (color1.getBlue() + ratio * (color2.getBlue() - color1.getBlue()));
                data[base + 3] = (int) (color1.getAlpha() + ratio * (color2.getAlpha() - color1.getAlpha()));
            }
        }
        raster.setPixels(0, 0, w, h, data);

        return raster;
    }
}
