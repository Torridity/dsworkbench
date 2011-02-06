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
import de.tor.tribes.types.ArmyCamp;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.BarbarianAlly;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Church;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.FreeForm;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.DSWorkbenchTroopsFrame;
import de.tor.tribes.ui.FormConfigFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.TwoD.ShapeStroke;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.algo.ChurchRangeCalculator;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.map.FormManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.note.NoteManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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
 * @TODO (DIFF) Added arrow form
 *
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

    private boolean drawVillage(Village pVillage, boolean pShowBarbarian, boolean pMarkedOnly) {
        boolean drawVillage = true;
        //check for barbarian

        if (!pShowBarbarian) {
            if ((pVillage != null) && (pVillage.getTribe().equals(Barbarians.getSingleton()))) {
                drawVillage = false;
            }
        }
        //check marked only

        if (drawVillage && pMarkedOnly) {
            if (pVillage != null && currentUserVillage != null) {
                //valid village
                if (!pVillage.getTribe().equals(currentUserVillage.getTribe())) {
                    //check tribe marker
                    Marker m = MarkerManager.getSingleton().getMarker(pVillage);
                    if (m == null) {
                        drawVillage = false;
                    } else {
                        drawVillage = true;
                    }
                }//village is owned by current user
            }
        }
        List<Tag> villageTags = TagManager.getSingleton().getTags(pVillage);
        if ((drawVillage) && (villageTags != null) && (!villageTags.isEmpty())) {
            boolean notShown = true;
            for (Tag tag : TagManager.getSingleton().getTags(pVillage)) {
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
        return drawVillage;
    }

    /**Render village graphics and create village rectangles for further rendering*/
    private void renderMap() {
        long s = System.currentTimeMillis();
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }
        int width = (int) Math.rint(currentFieldWidth);//GlobalOptions.getSkin().getBasicFieldWidth();
        int height = (int) Math.rint(currentFieldHeight);//GlobalOptions.getSkin().getBasicFieldHeight();
        BufferedImage layer = null;
        Graphics2D g2d = null;
        long drawTime = 0;

        //prepare drawing buffer
        if (mLayers.get(MAP_LAYER) == null) {
            layer = ImageUtils.createCompatibleBufferedImage(wb, hb, Transparency.BITMASK);
            mLayers.put(MAP_LAYER, layer);
            g2d = layer.createGraphics();
            ImageUtils.setupGraphics(g2d);
        } else {
            //check if image size is still valid
            layer = mLayers.get(MAP_LAYER);
            if (layer.getWidth() != wb || layer.getHeight() != hb) {
                //mappanel has resized
                layer = ImageUtils.createCompatibleBufferedImage(wb, hb, Transparency.BITMASK);
                mLayers.put(MAP_LAYER, layer);
                g2d = layer.createGraphics();
                ImageUtils.setupGraphics(g2d);
            } else {
                //only clear image
                g2d = (Graphics2D) layer.getGraphics();
                ImageUtils.setupGraphics(g2d);
                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                g2d.fillRect(0, 0, wb, hb);
                g2d.setComposite(c);
            }
        }

        //disable decoration if field size is not equal the decoration texture size
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getBasicFieldWidth()) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getBasicFieldHeight())) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }
        boolean showBarbarian = true;
        try {
            showBarbarian = Boolean.parseBoolean(GlobalOptions.getProperty("show.barbarian"));
        } catch (Exception e) {
            showBarbarian = true;
        }

        boolean markedOnly = false;
        try {
            markedOnly = Boolean.parseBoolean(GlobalOptions.getProperty("draw.marked.only"));
        } catch (Exception e) {
            markedOnly = false;
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

        HashMap<Integer, Point> copyRegions = new HashMap<Integer, Point>();
        HashMap<Integer, Point> copyRegionsMap = new HashMap<Integer, Point>();
        villagePositions = new HashMap<Village, Rectangle>();

        int contSpacing = 100;
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            contSpacing = 50;
        }

        boolean minimapSkin = GlobalOptions.getSkin().isMinimapSkin();
        // <editor-fold defaultstate="collapsed" desc="Village drawing">
//        long loopStart = -1;
//        long loopDur = 0;
//        long loopCnt = 0;
        for (int i = 0; i < iVillagesX; i++) {
            for (int j = 0; j < iVillagesY; j++) {
//                if (loopStart == -1) {
//                    loopStart = System.nanoTime();
//                } else {
//                    loopDur += System.nanoTime() - loopStart;
//                    loopStart = System.nanoTime();
//                    loopCnt++;
//                }

                Village v = mVisibleVillages[i][j];
                boolean drawVillage = true;
                boolean isArmyCamp = false;
                if (v instanceof ArmyCamp) {
                    isArmyCamp = true;
                    drawVillage = true;
                } else {
                    drawVillage = drawVillage(v, showBarbarian, markedOnly);
                }
                int xp = (int) Math.floor(x + dx);
                int yp = (int) Math.floor(y + dy);
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

                            g2d.drawImage(worldImage, xp, yp, null);
                            //check containment using size tolerance
                            if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                copyRegionsMap.put(worldId, new Point(xp, yp));
                            }
                        } else {
                            //world skin does not fit -> only default underground
                            if (!minimapSkin) {
                                Image worldImage = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom);
                                g2d.drawImage(worldImage, xp, yp, null);

                                //check containment using size tolerance
                                if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                    copyRegionsMap.put(worldId, new Point(xp, yp));
                                }
                            } else {
                                Color cb = g2d.getColor();
                                g2d.setColor(new Color(35, 125, 0));
                                g2d.fillRect(xp, yp, width, height);
                                g2d.setColor(cb);
                                //check containment using size tolerance
                                if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                    copyRegionsMap.put(worldId, new Point(xp, yp));
                                }
                            }
                        }
                    } else {
                        s = System.currentTimeMillis();
                        g2d.copyArea(p.x, p.y, width, height, (int) Math.floor(xp - p.x), (int) Math.floor(yp - p.y));
                        drawTime += (System.currentTimeMillis() - s);

                    }
                } else {
                    // <editor-fold defaultstate="collapsed" desc=" Select village type ">
                    int type = Skin.ID_V1;
                    v.setVisibleOnMap(drawVillage);
                    if (drawVillage && !isArmyCamp) {
                        type = v.getGraphicsType();
                        //store village rectangle
                        villagePositions.put(v, new Rectangle(xp, yp, width, height));
                    } else {
                        type = Skin.ID_DEFAULT_UNDERGROUND;
                        mVisibleVillages[i][j] = null;
                    }
                    // </editor-fold>

                    //drawing
                    Point p = copyRegions.get(type);

                    if (p == null) {
                        if (!minimapSkin) {
                            if (isArmyCamp) {
                                g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom), xp, yp, null);
                            }
                            g2d.drawImage(GlobalOptions.getSkin().getImage(type, currentZoom), xp, yp, null);
                            //check containment using size tolerance
                            if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                copyRegions.put(type, new Point(xp, yp));
                            }
                        } else {
                            Color cb = g2d.getColor();
                            if (!drawVillage) {
                                g2d.setColor(new Color(35, 125, 0));
                                g2d.fillRect(xp, yp, width, height);
                            } else {
                                g2d.setColor(Color.BLACK);
                                g2d.drawRect(xp, yp, width, height);
                                if (!isArmyCamp) {
                                    if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                        copyRegions.put(type, new Point(xp, yp));
                                    }
                                } else {
                                    g2d.fillRect(xp, yp, width, height);
                                }
                            }
                            g2d.setColor(cb);
                        }
                    } else {
                        s = System.currentTimeMillis();
                        g2d.copyArea(p.x, p.y, width, height, (int) Math.floor(xp - p.x), (int) Math.floor(yp - p.y));
                        drawTime += (System.currentTimeMillis() - s);
                    }
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
//        System.out.println("Dur: " + loopDur);
//        System.out.println("LD " + ((double) loopDur / (double) loopCnt));

        Stroke st = g2d.getStroke();
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
        System.out.println("DrawTime: " + drawTime);
        g2d.setStroke(st);
        //</editor-fold>
        g2d.dispose();
    }

    /**Render tag markers*/
    private void renderTagMarkers() {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        int tagsize = (int) Math.rint((double) 18 / currentZoom);
        int conquerSize = (int) Math.rint((double) 16 / currentZoom);
        BufferedImage layer = null;
        Graphics2D g2d = null;
        //prepare drawing buffer
        if (mLayers.get(TAG_MARKER_LAYER) == null) {
            layer = ImageUtils.createCompatibleBufferedImage(wb, hb, Transparency.TRANSLUCENT);//new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
            mLayers.put(TAG_MARKER_LAYER, layer);
            g2d = layer.createGraphics();
            ImageUtils.setupGraphics(g2d);
        } else {
            layer = mLayers.get(TAG_MARKER_LAYER);
            if (layer.getWidth() != wb || layer.getHeight() != hb) {
                layer = ImageUtils.createCompatibleBufferedImage(wb, hb, Transparency.TRANSLUCENT);//new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
                mLayers.put(TAG_MARKER_LAYER, layer);
                g2d = layer.createGraphics();
                ImageUtils.setupGraphics(g2d);
            } else {
                g2d = (Graphics2D) layer.getGraphics();
                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                g2d.fillRect(0, 0, wb, hb);
                g2d.setComposite(c);
            }

        }

        if (tagsize > currentFieldHeight || tagsize > currentFieldWidth) {
            return;
        }

        Hashtable<Integer, Point> copyRegions = new Hashtable<Integer, Point>();

        // <editor-fold defaultstate="collapsed" desc="Tag marker graphics drawing">
        try {
            Iterator<Village> villages = villagePositions.keySet().iterator();

            Rectangle conquerCopyRegion = null;
            while (villages.hasNext()) {
                Village current = villages.next();
                Conquer c = ConquerManager.getSingleton().getConquer(current);
                Rectangle r = villagePositions.get(current);
                List<Tag> villageTags = TagManager.getSingleton().getTags(current);
                if (villageTags != null && !villageTags.isEmpty()) {
                    int xcnt = 1;
                    int ycnt = 2;
                    int cnt = 0;
                    for (Tag tag : TagManager.getSingleton().getTags(current)) {
                        // <editor-fold defaultstate="collapsed" desc="Draw tag if active">
                        if (tag.isShowOnMap()) {
                            int iconType = tag.getTagIcon();
                            Color color = tag.getTagColor();
                            int key = iconType;
                            if (color != null) {
                                key = color.getRGB() + iconType;
                            }

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
                                    Point p = copyRegions.get(key);
                                    if (p == null) {
                                        Image tagImage = ImageManager.getUnitImage(iconType, false).getScaledInstance(tagsize, tagsize, Image.SCALE_FAST);
                                        g2d.drawImage(tagImage, tagX, tagY, null);
                                        //check containment using size tolerance
                                        if (MapPanel.getSingleton().getBounds().contains(new Rectangle(tagX - 2, tagY - 2, tagsize + 2, tagsize + 2))) {
                                            copyRegions.put(key, new Point(tagX, tagY));
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
                        // </editor-fold>
                    }
                }
                if (c != null) {
                    //village was recently conquered
                    if (conquerCopyRegion != null) {
                        g2d.copyArea(conquerCopyRegion.x, conquerCopyRegion.y, conquerCopyRegion.width, conquerCopyRegion.height, r.x + r.width - conquerSize - conquerCopyRegion.x, r.y + r.height - conquerSize - conquerCopyRegion.y);
                    } else {
                        g2d.drawImage(mConquerWarning, r.x + r.width - conquerSize, r.y + r.height - conquerSize, conquerSize, conquerSize, null);
                        if (MapPanel.getSingleton().getBounds().contains(new Rectangle(r.x + r.width - conquerSize - 2, r.y + r.height - conquerSize - 2, conquerSize + 2, conquerSize + 2))) {
                            conquerCopyRegion = new Rectangle(r.x + r.width - conquerSize, r.y + r.height - conquerSize, conquerSize, conquerSize);
                        }

                    }
                }
            }
        } catch (Exception e) {
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

    private void renderNoteMarkers() {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        BufferedImage layer = mLayers.get(NOTE_LAYER);
        Graphics2D g2d = null;
        //prepare drawing buffer
        if (layer == null) {
            layer = ImageUtils.createCompatibleBufferedImage(wb, hb + 100, Transparency.TRANSLUCENT);//new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
            mLayers.put(NOTE_LAYER, layer);
            g2d = layer.createGraphics();
            ImageUtils.setupGraphics(g2d);
        } else {
            if (layer.getWidth() != wb || layer.getHeight() != hb + 100) {
                layer = ImageUtils.createCompatibleBufferedImage(wb, hb + 100, Transparency.TRANSLUCENT);//new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
                mLayers.put(NOTE_LAYER, layer);
                g2d = layer.createGraphics();
                ImageUtils.setupGraphics(g2d);
            } else {
                g2d = (Graphics2D) layer.getGraphics();
                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                g2d.fillRect(0, 0, wb, hb + 100);
                g2d.setComposite(c);
            }
        }
        try {
            int iconWidth = ImageManager.getNoteIcon(0).getWidth();
            int iconHeight = ImageManager.getNoteIcon(0).getHeight();
            //render note icons
            Iterator<Village> keys = villagePositions.keySet().iterator();

            for (int i = 0; i < 6; i++) {
                BufferedImage icon = ImageManager.getNoteIcon(i);
                g2d.drawImage(icon, i * 32, hb + 68, null);
            }

            /**Build note mapping table to speed up looking for notes for a village*/
            Hashtable<Integer, List<Note>> vnMappings = new Hashtable<Integer, List<Note>>();
            for (Note n : NoteManager.getSingleton().getNotes()) {
                for (Integer id : n.getVillageIds()) {
                    if (vnMappings.containsKey(id)) {
                        List<Note> notes = vnMappings.get(id);
                        notes.add(n);
                    } else {
                        List<Note> notes = new LinkedList<Note>();
                        notes.add(n);
                        vnMappings.put(id, notes);
                    }

                }
            }

            while (keys.hasNext()) {
                Village v = keys.next();
                Rectangle villageRect = villagePositions.get(v);
                // Note n = NoteManager.getSingleton().getNoteForVillage(v);
                List<Note> notes = vnMappings.get(v.getId());
                if (notes != null) {
                    int ncnt = notes.size();
                    int half = (int) Math.rint((double) ncnt / 2.0);
                    int cnt = -half;
                    for (Note n : notes) {
                        int noteIcon = n.getMapMarker();
                        int markX = villageRect.x + Math.round(villageRect.width / 2);
                        int markY = villageRect.y + Math.round(villageRect.height / 2);
                        //sometimes the icons seemed to be null
                        int dx = cnt * 3;
                        int dy = dx;
                        g2d.copyArea(noteIcon * 32, hb + 68, iconWidth, iconHeight, markX - noteIcon * 32 - 10 + dx, markY - hb - 68 - iconHeight + 10 + dy);
                        cnt++;

                    }


                }
            }
        } catch (Exception e) {
            logger.error("Failed to render note icons", e);
        }

    }

    /**Render marker layer -> drawn on same buffer as map*/
    private void renderMarkers(boolean pDrawStandard) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        BufferedImage layer = null;
        Graphics2D g2d = null;
        //prepare drawing buffer
        if (mLayers.get(MARKER_LAYER) == null) {
            layer = ImageUtils.createCompatibleBufferedImage(wb, hb, Transparency.TRANSLUCENT);//new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
            mLayers.put(MARKER_LAYER, layer);
            g2d = layer.createGraphics();
            ImageUtils.setupGraphics(g2d);
        } else {
            layer = mLayers.get(MARKER_LAYER);
            if (layer.getWidth() != wb || layer.getHeight() != hb) {
                layer = ImageUtils.createCompatibleBufferedImage(wb, hb, Transparency.TRANSLUCENT);//new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
                mLayers.put(MARKER_LAYER, layer);
                g2d = layer.createGraphics();
                ImageUtils.setupGraphics(g2d);
            } else {
                g2d = (Graphics2D) layer.getGraphics();
                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                g2d.fillRect(0, 0, wb, hb);
                g2d.setComposite(c);
            }
        }

        Color DEFAULT = null;
        if (pDrawStandard) {
            try {
                int mark = Integer.parseInt(GlobalOptions.getProperty("default.mark"));
                if (mark == 0) {
                    DEFAULT = Constants.DS_DEFAULT_MARKER;
                } else if (mark == 1) {
                    DEFAULT = Color.RED;
                } else if (mark == 2) {
                    DEFAULT = Color.WHITE;
                }

            } catch (Exception e) {
                DEFAULT = Constants.DS_DEFAULT_MARKER;
            }
        }
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, wb, hb);

        Marker tribe = null;
        Marker ally = null;
        boolean own = false;
        Color before = g2d.getColor();
        Iterator<Village> villages = villagePositions.keySet().iterator();
        Rectangle emptyRect = null;
        boolean minimapSkin = GlobalOptions.getSkin().isMinimapSkin();

        while (villages.hasNext()) {
            Village v = villages.next();
            own = false;
            tribe = null;
            ally = null;
            Tribe t = (v == null) ? null : v.getTribe();
            Color markerColor = null;
            if (currentUserVillage != null && t == currentUserVillage.getTribe()) {
                if (v.equals(currentUserVillage)) {
                    markerColor = Color.WHITE;
                } else {
                    markerColor = Color.YELLOW;
                }

                own = true;
            } else {
                if (t != Barbarians.getSingleton()) {
                    tribe = MarkerManager.getSingleton().getMarker(t);
                    if (t.getAlly() != null) {
                        ally = MarkerManager.getSingleton().getMarker(t.getAlly());
                    }

                }
            }

            if (markerColor != null || tribe != null || ally != null) {
                Rectangle vRect = villagePositions.get(v);
                if (tribe != null && ally != null && !own) {
                    //draw two-part marker
                    GeneralPath p = new GeneralPath();
                    p.moveTo(vRect.getX(), vRect.getY());
                    p.lineTo(vRect.getX() + vRect.getWidth(), vRect.getY() + vRect.getHeight());
                    p.lineTo(vRect.getX(), vRect.getY() + vRect.getHeight());
                    p.closePath();
                    g2d.setColor(tribe.getMarkerColor());
                    g2d.fill(p);
                    p = new GeneralPath();
                    p.moveTo(vRect.getX(), vRect.getY());
                    p.lineTo(vRect.getX() + vRect.getWidth(), vRect.getY());
                    p.lineTo(vRect.getX() + vRect.getWidth(), vRect.getY() + vRect.getHeight());
                    p.closePath();
                    g2d.setColor(ally.getMarkerColor());
                    g2d.fill(p);
                } else if (tribe != null && !own) {
                    //draw tribe marker       
                    g2d.setColor(tribe.getMarkerColor());
                    g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                } else if (ally != null && !own) {
                    //draw ally marker
                    g2d.setColor(ally.getMarkerColor());
                    g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                } else {
                    //draw misc marker
                    g2d.setColor(markerColor);
                    g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                }

            } else {
                Rectangle vRect = villagePositions.get(v);
                if (t != Barbarians.getSingleton()) {
                    if (DEFAULT != null) {
                        //no mark-on-top mode
                        g2d.setColor(DEFAULT);
                        g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                    } else {
                        //mark-on-top mode
                        if (!minimapSkin) {
                            if (emptyRect == null) {
                                //get transparent region to copy
                                Image du = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom);
                                g2d.drawImage(du, vRect.x, vRect.y, null);
                                if (MapPanel.getSingleton().getBounds().contains(new Rectangle(vRect.x - 10, vRect.y - 10, du.getWidth(null) + 10, du.getHeight(null) + 10))) {
                                    emptyRect = (Rectangle) vRect.clone();
                                }
                            } else {
                                g2d.copyArea(emptyRect.x, emptyRect.y, emptyRect.width, emptyRect.height, vRect.x - emptyRect.x, vRect.y - emptyRect.y);
                            }

                        } else {
                            //for minimap skin use uniform color
                            g2d.setColor(new Color(35, 125, 0));
                            g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                        }

                    }
                } else {
                    //barbarian marker
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                }

            }
        }
        g2d.setColor(before);
    }

    /**Render basic decoration e.g. Active village mark and troops type mark*/
    private void renderDecoration(Graphics2D g2d) {
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

        if (!markTroopTypes && !markActiveVillage) {
            //return if nothing to do
            return;
        }
        Iterator<Village> villages = villagePositions.keySet().iterator();

        while (villages.hasNext()) {
            Village v = villages.next();
            Rectangle villageRect = villagePositions.get(v);
            if (markTroopTypes) {
                Image troopMark = TroopsManager.getSingleton().getTroopsMarkerForVillage(v);
                if (troopMark != null) {
                    int x = villageRect.x + Math.round(villageRect.width / 2);
                    int y = villageRect.y + Math.round(villageRect.width / 2);
                    troopMark = troopMark.getScaledInstance((int) Math.rint(troopMark.getWidth(null) / currentZoom), (int) Math.rint(troopMark.getHeight(null) / currentZoom), BufferedImage.SCALE_FAST);
                    g2d.drawImage(troopMark, x - troopMark.getWidth(null) / 2, y - troopMark.getHeight(null), null);
                }

            }

            if (markActiveVillage) {
                Village toolSource = MapPanel.getSingleton().getToolSourceVillage();
                if (currentUserVillage != null || toolSource != null) {
                    if (toolSource == null) {
                        if (v.compareTo(currentUserVillage) == 0) {
                            int markX = villageRect.x + Math.round(villageRect.width / 2);
                            int markY = villageRect.y + Math.round(villageRect.height / 2);
                            g2d.drawImage(mMarkerImage, markX, markY - mMarkerImage.getHeight(null), null);
                        }

                    } else {
                        if (v.compareTo(toolSource) == 0) {
                            int markX = villageRect.x + Math.round(villageRect.width / 2);
                            int markY = villageRect.y + Math.round(villageRect.height / 2);
                            g2d.drawImage(mMarkerImage, markX, markY - mMarkerImage.getHeight(null), null);
                        }

                    }
                }
            }
        }
    }

    private void renderTroopDensity(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        Iterator<Village> villages = villagePositions.keySet().iterator();
        Hashtable<Village, VillageTroopsHolder> values = new Hashtable<Village, VillageTroopsHolder>();

        int maxDef = 650000;
        try {
            maxDef = Integer.parseInt(GlobalOptions.getProperty("max.density.troops"));
        } catch (Exception e) {
            maxDef = 650000;
        }

        while (villages.hasNext()) {
            Village v = villages.next();
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
            if (holder != null) {
                values.put(v, holder);
            }
        }

        Enumeration<Village> keys = values.keys();
        List<Village> keyV = new LinkedList<Village>();
        while (keys.hasMoreElements()) {
            keyV.add(keys.nextElement());
        }

        int order = Village.getOrderType();
        Village.setOrderType(Village.ORDER_BY_COORDINATES);
        Collections.sort(keyV);
        Village.setOrderType(order);

        Arc2D.Double arc = new Arc2D.Double();
        Ellipse2D.Double ellipse = new Ellipse2D.Double();
        Color cb = g2d.getColor();
        for (Village v : keyV) {
            Rectangle villageRect = villagePositions.get(v);
            VillageTroopsHolder holder = values.get(v);
            double defIn = holder.getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
            double defOwn = holder.getDefValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
            double percOfMax = defIn / maxDef;
            double percOwn = defOwn / defIn;
            double percForeign = 1 - percOwn;
            //limit to 100%
            percOfMax = (percOfMax > 1) ? 1 : percOfMax;

            //the less the density is the more alpha comes to its full value
            int alpha2 = (int) Math.rint((1 - percOfMax) * 255);
            if (alpha2 < 60) {
                //limit alpha min to 60
                alpha2 = 60;
            }

            double half = (double) maxDef / 2.0;

            Color col = null;
            if (defIn <= maxDef && defIn > half) {
                float ratio = (float) (defIn - half) / (float) half;
                Color c1 = Color.YELLOW;
                Color c2 = Color.GREEN;
                int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                col = new Color(red, green, blue, alpha2);
            } else if (defIn <= half) {
                float ratio = (float) defIn / (float) half;
                Color c1 = Color.RED;
                Color c2 = Color.YELLOW;
                int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                col = new Color(red, green, blue, alpha2);
            } else {
                col = new Color(0, 255, 0, alpha2);
            }


            Color c = col;//new Color(0, r, g, alpha2);
            Color cc = new Color(0, col.getRed(), col.getGreen(), col.getAlpha());//new Color(r, g, 0, alpha2);
            //calculate circle size
            int size = (int) Math.rint(percOfMax * 3 * villageRect.width);
            if (size < 0.5 * villageRect.width) {
                //limit min. size to half a village size
                size = (int) Math.rint(0.5 * villageRect.width);
            }

            if (size > 0) {
                g2d.setColor(Color.BLACK);
                int partOwn = (int) Math.rint(360 * percOwn);
                int partForeign = (int) Math.rint(360 * percForeign);
                //fill part blue (own) or other color (foreign)
                g2d.setColor(c);
                arc.setArc(villageRect.x - (int) Math.rint((size - villageRect.width) / 2), villageRect.y - (int) Math.rint((size - villageRect.height) / 2), size, size, 0, partOwn, Arc2D.PIE);
                g2d.fill(arc);
                g2d.setColor(cc);
                arc.setArc(villageRect.x - (int) Math.rint((size - villageRect.width) / 2), villageRect.y - (int) Math.rint((size - villageRect.height) / 2), size, size, partOwn, partForeign, Arc2D.PIE);
                g2d.fill(arc);
                //draw border
                ellipse.setFrame(villageRect.x - (int) Math.rint((size - villageRect.width) / 2), villageRect.y - (int) Math.rint((size - villageRect.height) / 2), size, size);
                g2d.setColor(Color.BLACK);
                g2d.draw(ellipse);
                g2d.setColor(cb);
            }

        }
    }

    /**Render attack vectors*/
    private void renderAttacks(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

// <editor-fold defaultstate="collapsed" desc="Attack-line drawing (Foreground)">
        Stroke s = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

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

        GeneralPath p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(10, 5);
        p.lineTo(0, 10);
        p.lineTo(0, 0);
        ShapeStroke stroke_attack = new ShapeStroke(
                new Shape[]{
                    p,
                    new Rectangle2D.Float(0, 0, 10, 2)
                },
                10.0f);

        p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(5, 3);
        p.lineTo(0, 6);
        p.lineTo(0, 0);
        ShapeStroke stroke_fake = new ShapeStroke(
                new Shape[]{
                    p,
                    new Rectangle2D.Float(0, 0, 10, 2)
                },
                20.0f);


        Enumeration<String> keys = AttackManager.getSingleton().getPlans();
        while (keys.hasMoreElements()) {
            String plan = keys.nextElement();
            Attack[] attacks = AttackManager.getSingleton().getAttackPlan(plan).toArray(new Attack[]{});
            for (Attack attack : attacks) {
                //go through all attacks
                //render if shown on map or if either source or target are visible
                if (attack.isShowOnMap() && (attack.getSource().isVisibleOnMap() || attack.getTarget().isVisibleOnMap())) {
                    //only enter if attack should be visible
                    //get line for this attack
                    Line2D.Double attackLine = new Line2D.Double(attack.getSource().getX(), attack.getSource().getY(), attack.getTarget().getX(), attack.getTarget().getY());
                    String value = GlobalOptions.getProperty("attack.movement");
                    boolean showAttackMovement = (value == null) ? false : Boolean.parseBoolean(value);
                    double xStart = (attackLine.getX1() - viewStartPoint.x) * currentFieldWidth + currentFieldWidth / 2;
                    double yStart = (attackLine.getY1() - viewStartPoint.y) * currentFieldHeight + currentFieldHeight / 2;
                    double xEnd = (attackLine.getX2() - viewStartPoint.x) * currentFieldWidth + currentFieldWidth / 2;
                    double yEnd = (attackLine.getY2() - viewStartPoint.y) * currentFieldHeight + currentFieldHeight / 2;
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
                                //attack not running, draw unit between source and target
                                double perc = .5;
                                double xTar = xStart + (xEnd - xStart) * perc;
                                double yTar = yStart + (yEnd - yStart) * perc;
                                unitXPos = (int) xTar - unitIcon.getIconWidth() / 2;
                                unitYPos = (int) yTar - unitIcon.getIconHeight() / 2;
                            } else {
                                //attack arrived
                                unitXPos = (int) xEnd - unitIcon.getIconWidth() / 2;
                                unitYPos = (int) yEnd - unitIcon.getIconHeight() / 2;
                            }

                        }
                    }

                    g2d.setColor(attackColors.get(attack.getUnit().getName()));
                    if (attack.getType() == Attack.FAKE_TYPE || attack.getType() == Attack.FAKE_DEFF_TYPE) {
                        /* g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));*/
                        g2d.setStroke(stroke_fake);
                    } else {
                        g2d.setStroke(stroke_attack);
                    }


                    g2d.drawLine((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));

                    if (unitIcon != null) {
                        g2d.drawImage(unitIcon.getImage(), unitXPos, unitYPos, null);
                    }

                }
            }
            attacks = null;
        }

        g2d.setStroke(s);
//</editor-fold>

    }

    private void renderSupports(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }
// <editor-fold defaultstate="collapsed" desc=" Support drawing">

        Color b = g2d.getColor();
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        List<Village> drawnTargets = new LinkedList<Village>();
        Rectangle2D bo = MapPanel.getSingleton().getVirtualBounds();

        for (Village v : DSWorkbenchTroopsFrame.getSingleton().getSelectedTroopsVillages()) {
            //process source villages

            List<Village> villages = new LinkedList<Village>();

            for (Village target : TroopsManager.getSingleton().getTroopsForVillage(v).getSupportTargets()) {
                villages.add(target);
            }

            Enumeration<Village> sources = TroopsManager.getSingleton().getTroopsForVillage(v).getSupports().keys();
            while (sources.hasMoreElements()) {
                Village source = sources.nextElement();
                if (!villages.contains(source)) {
                    villages.add(source);
                }

            }

            for (Village target : villages) {
                Line2D.Double supportLine = new Line2D.Double(v.getX() * currentFieldWidth, v.getY() * currentFieldHeight, target.getX() * currentFieldWidth, target.getY() * currentFieldHeight);

                //draw full line
                double xStart = (supportLine.getX1() - bo.getX() * currentFieldWidth) + currentFieldWidth / 2;
                double yStart = (supportLine.getY1() - bo.getY() * currentFieldHeight) + currentFieldHeight / 2;
                double xEnd = (supportLine.getX2() - bo.getX() * currentFieldWidth) + currentFieldWidth / 2;
                double yEnd = (supportLine.getY2() - bo.getY() * currentFieldHeight) + currentFieldHeight / 2;

                if (villagePositions.containsKey(v) && villagePositions.containsKey(target)) {
                    g2d.setColor(Color.YELLOW);
                    g2d.drawLine((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                    g2d.drawOval((int) xEnd - 2, (int) yEnd - 2, 4, 4);
                } else if (villagePositions.containsKey(v) && !villagePositions.containsKey(target)) {
                    g2d.setColor(Color.GREEN);
                    g2d.setClip((int) Math.rint(xStart - 50), (int) Math.rint(yStart - 50), 100, 100);
                    supportLine = new Line2D.Double((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                    g2d.draw(supportLine);
                    g2d.setClip(null);
                    Line2D.Double top = new Line2D.Double(xStart - 50.0, yStart - 50.0, xStart + 50.0, yStart - 50.0);
                    Line2D.Double right = new Line2D.Double(xStart + 50.0, yStart - 50.0, xStart + 50.0, yStart + 50.0);
                    Line2D.Double bottom = new Line2D.Double(xStart - 50.0, yStart + 50.0, xStart + 50.0, yStart + 50.0);
                    Line2D.Double left = new Line2D.Double(xStart - 50.0, yStart - 50.0, xStart - 50.0, yStart + 50.0);
                    double x1 = xStart;
                    double x2 = xEnd;
                    double y1 = yStart;
                    double y2 = yEnd;

                    double x3 = xStart + 50.0;
                    double x4 = xStart + 50.0;
                    double y3 = yStart - 50.0;
                    double y4 = yStart + 50.0;

                    if (supportLine.intersectsLine(top)) {
                        x3 = top.x1;
                        x4 = top.x2;
                        y3 = top.y1;
                        y4 = top.y2;
                    } else if (supportLine.intersectsLine(right)) {
                        x3 = right.x1;
                        x4 = right.x2;
                        y3 = right.y1;
                        y4 = right.y2;
                    } else if (supportLine.intersectsLine(bottom)) {
                        x3 = bottom.x1;
                        x4 = bottom.x2;
                        y3 = bottom.y1;
                        y4 = bottom.y2;
                    } else {
                        x3 = left.x1;
                        x4 = left.x2;
                        y3 = left.y1;
                        y4 = left.y2;
                    }

                    double u0 = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
                    //double u1 = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
                    double x = x1 + u0 * (x2 - x1);
                    double y = y1 + u0 * (y2 - y1);

                    double dist = DSCalculator.calculateDistance(v, target);
                    String d = NumberFormat.getInstance().format(dist);
                    Rectangle2D bb = g2d.getFontMetrics().getStringBounds(d, g2d);
                    g2d.fillRect((int) (x + bb.getX()), (int) (y + bb.getY()), (int) bb.getWidth(), (int) bb.getHeight());
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(d, (int) x, (int) y);
                } else if (!villagePositions.containsKey(v) && villagePositions.containsKey(target)) {
                    g2d.setColor(Color.RED);
                    g2d.setClip((int) Math.rint(xEnd - 50), (int) Math.rint(yEnd - 50), 100, 100);
                    supportLine = new Line2D.Double((int) Math.rint(xEnd), (int) Math.rint(yEnd), (int) Math.rint(xStart), (int) Math.rint(yStart));
                    g2d.draw(supportLine);
                    g2d.setClip(null);
                    Line2D.Double top = new Line2D.Double(xEnd - 50.0, yEnd - 50.0, xEnd + 50.0, yEnd - 50.0);
                    Line2D.Double right = new Line2D.Double(xEnd + 50.0, yEnd - 50.0, xEnd + 50.0, yEnd + 50.0);
                    Line2D.Double bottom = new Line2D.Double(xEnd - 50.0, yEnd + 50.0, xEnd + 50.0, yEnd + 50.0);
                    Line2D.Double left = new Line2D.Double(xEnd - 50.0, yEnd - 50.0, xEnd - 50.0, yEnd + 50.0);
                    double x1 = xEnd;
                    double x2 = xStart;
                    double y1 = yEnd;
                    double y2 = yStart;

                    double x3 = xStart + 50.0;
                    double x4 = xStart + 50.0;
                    double y3 = yStart - 50.0;
                    double y4 = yStart + 50.0;

                    if (supportLine.intersectsLine(top)) {
                        x3 = top.x1;
                        x4 = top.x2;
                        y3 = top.y1;
                        y4 = top.y2;
                    } else if (supportLine.intersectsLine(right)) {
                        x3 = right.x1;
                        x4 = right.x2;
                        y3 = right.y1;
                        y4 = right.y2;
                    } else if (supportLine.intersectsLine(bottom)) {
                        x3 = bottom.x1;
                        x4 = bottom.x2;
                        y3 = bottom.y1;
                        y4 = bottom.y2;
                    } else {
                        x3 = left.x1;
                        x4 = left.x2;
                        y3 = left.y1;
                        y4 = left.y2;
                    }

                    double u0 = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
                    //double u1 = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
                    double x = x1 + u0 * (x2 - x1);
                    double y = y1 + u0 * (y2 - y1);

                    double dist = DSCalculator.calculateDistance(v, target);
                    String d = NumberFormat.getInstance().format(dist);
                    Rectangle2D bb = g2d.getFontMetrics().getStringBounds(d, g2d);
                    g2d.fillRect((int) (x + bb.getX()), (int) (y + bb.getY()), (int) bb.getWidth(), (int) bb.getHeight());
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(d, (int) x, (int) y);

                } else {
                }
            }
        }
        g2d.setColor(b);
        // </editor-fold>
    }

    /**Render e.g. forms and church ranges*/
    private void renderForms(Graphics2D g2d) {
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
    }

    private void renderChurches(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        Rectangle g = null;
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

        List<Village> churchVillages = ChurchManager.getSingleton().getChurchVillages();
        List<GeneralPath> paths = new LinkedList<GeneralPath>();
        for (Village v : churchVillages) {
            boolean drawVillage = true;

            // <editor-fold defaultstate="collapsed" desc=" Check if village should be drawn ">
            //check for barbarian
            if (!showBarbarian) {
                if ((v != null) && (v.getTribe() == Barbarians.getSingleton())) {
                    drawVillage = false;
                }
            }

            if (drawVillage && markedOnly) {
                if (v != null && currentUserVillage != null) {
                    //valid village
                    if (v.getTribe() != Barbarians.getSingleton()) {
                        if (!v.getTribe().equals(currentUserVillage.getTribe())) {
                            //check tribe marker
                            Marker m = MarkerManager.getSingleton().getMarker(v.getTribe());
                            if (m == null) {
                                //tribe is not marked check ally marker
                                if (v.getTribe().getAlly() != null && v.getTribe().getAlly() != BarbarianAlly.getSingleton()) {
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
            if ((drawVillage) && (villageTags != null) && (!villageTags.isEmpty())) {
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

            // <editor-fold defaultstate="collapsed" desc=" Church calculation">
            if (drawVillage) {
                Church c = ChurchManager.getSingleton().getChurch(v);
                int vx = MapPanel.getSingleton().virtualPosToSceenPos(v.getX(), v.getY()).x;
                int vy = MapPanel.getSingleton().virtualPosToSceenPos(v.getX(), v.getY()).y;
                g = new Rectangle(vx, vy, (int) Math.rint(currentFieldWidth), (int) Math.rint(currentFieldHeight));
                List<Point2D.Double> positions = ChurchRangeCalculator.getChurchRange(v.getX(), v.getY(), c.getRange());
                GeneralPath p = new GeneralPath();
                p.moveTo(g.getX(), g.getY() - (c.getRange() - 1) * g.getHeight());
                int quad = 0;
                Point2D.Double lastPos = positions.get(0);
                for (Point2D.Double pos : positions) {
                    if (quad == 0) {
                        //north village
                        p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() - g.getHeight());
                        p.lineTo(p.getCurrentPoint().getX() + g.getWidth(), p.getCurrentPoint().getY());
                        quad = 1;
                    } else if (pos.getX() == v.getX() + c.getRange() && pos.getY() == v.getY()) {
                        //east village
                        p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                        p.lineTo(p.getCurrentPoint().getX() + g.getWidth(), p.getCurrentPoint().getY());
                        p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                        quad = 2;
                    } else if (pos.getX() == v.getX() && pos.getY() == v.getY() + c.getRange()) {
                        //south village
                        p.lineTo(p.getCurrentPoint().getX() - g.getWidth(), p.getCurrentPoint().getY());
                        p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + g.getHeight());
                        p.lineTo(p.getCurrentPoint().getX() - g.getWidth(), p.getCurrentPoint().getY());
                        quad = 3;
                    } else if (pos.getX() == v.getX() - c.getRange() && pos.getY() == v.getY()) {
                        //west village
                        p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() - g.getHeight());
                        p.lineTo(p.getCurrentPoint().getX() - g.getWidth(), p.getCurrentPoint().getY());
                        p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() - g.getHeight());
                        quad = 4;
                    } else {
                        //no special point
                        int dx = (int) (pos.getX() - lastPos.getX());
                        int dy = (int) (pos.getY() - lastPos.getY());

                        if (quad == 1) {
                            p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + dy * g.getHeight());
                            p.lineTo(p.getCurrentPoint().getX() + dx * g.getWidth(), p.getCurrentPoint().getY());
                        } else if (quad == 2) {
                            p.lineTo(p.getCurrentPoint().getX() + dx * g.getWidth(), p.getCurrentPoint().getY());
                            p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + dy * g.getHeight());
                        } else if (quad == 3) {
                            p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + dy * g.getHeight());
                            p.lineTo(p.getCurrentPoint().getX() + dx * g.getWidth(), p.getCurrentPoint().getY());
                        } else if (quad == 4) {
                            p.lineTo(p.getCurrentPoint().getX() + dx * g.getWidth(), p.getCurrentPoint().getY());
                            p.lineTo(p.getCurrentPoint().getX(), p.getCurrentPoint().getY() + dy * g.getHeight());
                        }
                    }
                    lastPos = pos;
                }

                p.closePath();
                /* Color cb = g2d.getColor();
                Composite com = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                Color radiusColor = c.getRangeColor();
                g2d.setColor(radiusColor);
                g2d.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(p);
                g2d.fill(p);
                g2d.setComposite(com);
                g2d.setColor(cb);*/
                paths.add(p);
            }

//</editor-fold>
        }

        Area a = null;
        for (GeneralPath p : paths) {
            if (a == null) {
                a = new Area(p);
            } else {
                a.add(new Area(p));
            }
        }
        if (a == null) {
            return;
        }
        Color cb = g2d.getColor();
        Composite com = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        Color radiusColor = Color.YELLOW;
        g2d.setColor(radiusColor);
        g2d.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(a);
        g2d.fill(a);
        g2d.setComposite(com);
        g2d.setColor(cb);
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
                    c.setDrawAlpha(0.5f);
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
/*
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
 */
