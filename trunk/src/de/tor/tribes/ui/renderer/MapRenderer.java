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
import de.tor.tribes.types.Church;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.DSWorkbenchTroopsFrame;
import de.tor.tribes.ui.FormConfigFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
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
 * @TODO (DIFF) Fixed bonus village graphics error on 9k
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
    public static final int TAG_MARKER_LAYER = 5;
    public static final int LIVE_LAYER = 6;
    public static final int NOTE_LAYER = 7;
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
    private Point2D.Double viewStartPoint = null;
    private double currentZoom = 0.0;
    private Village currentUserVillage = null;
    private VolatileImage mMainBuffer = null;
    private BufferedImage mConquerWarning = null;
    private Path2D.Double ARROW = createArrow();

    public MapRenderer() {
        mVisibleVillages = new Village[iVillagesX][iVillagesY];
        setDaemon(true);
        try {
            //load flag marker
            mMarkerImage = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/res/marker.png"));
            mConquerWarning = ImageIO.read(new File("./graphics/icons/warning.png"));
        } catch (Exception e) {
            logger.error("Failed to load marker images", e);
        }

        mLayers = new Hashtable<Integer, BufferedImage>();
    }

    /**Complete redraw on resize or scroll*/
    public void initiateRedraw(int pType) {
        mapRedrawRequired = true;
    }

    /**Render loop*/
    @Override
    public void run() {
        logger.debug("Entering render loop");
        while (true) {
            boolean completeRedraw = false;
            try {

                int w = MapPanel.getSingleton().getWidth();
                int h = MapPanel.getSingleton().getHeight();
                if ((w != 0) && (h != 0)) {
                    Graphics2D g2d = null;
                    if (mMainBuffer == null) {
                        //create main buffer during first iteration
                        //mMainBuffer = MapPanel.getSingleton().createImage(w, h);
                        mMainBuffer = MapPanel.getSingleton().createVolatileImage(w, h);
                        g2d = (Graphics2D) mMainBuffer.getGraphics();
                        prepareGraphics(g2d);
                        //set redraw required flag if nothin was drawn yet
                        mapRedrawRequired = true;
                    } else {
                        //check if image size is still valid
                        //if not re-create main buffer
                        if (mMainBuffer.getWidth(null) != w || mMainBuffer.getHeight(null) != h) {
                            //map panel has resized
                            mMainBuffer = MapPanel.getSingleton().createVolatileImage(w, h);

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
                    //get currently selected user village for marking -> one call reduces sync effort
                    currentUserVillage = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                    if (mapRedrawRequired) {
                        completeRedraw = true;
                        //complete redraw is required
                        calculateVisibleVillages();
                        if (viewStartPoint == null) {
                            throw new Exception("View position is 'null', skip redraw");
                        }
                        renderMap();
                        renderTagMarkers();
                    }
                    //render misc map elements needed  to be redrawn in every iteration
                    boolean markOnTop = Boolean.parseBoolean(GlobalOptions.getProperty("mark.on.top"));
                    if (markOnTop) {
                        //draw markers above map layer
                        g2d.drawImage(mLayers.get(MAP_LAYER), 0, 0, null);
                        Composite gg = g2d.getComposite();
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                        renderMarkers(g2d);
                        g2d.setComposite(gg);
                    } else {
                        //draw markers below map layer
                        renderMarkers(g2d);
                        g2d.drawImage(mLayers.get(MAP_LAYER), 0, 0, null);
                    }
                    //render tag markers (graphics created only on complete redraw)
                    g2d.drawImage(mLayers.get(TAG_MARKER_LAYER), 0, 0, null);
                    //render other layers (active village, troop type)
                    renderBasicDecoration(g2d);
                    renderNoteMarkers();
                    g2d.drawImage(mLayers.get(NOTE_LAYER), 0, 0, null);
                    //attacks layer
                    renderAttacks(g2d);
                    //forms, churches
                    renderExtendedDecoration(g2d);
                    //draw live layer
                    renderLiveLayer(g2d);
                    //render selection
                    de.tor.tribes.types.Rectangle selection = MapPanel.getSingleton().getSelectionRect();
                    if (selection != null) {
                        selection.renderForm(g2d);
                    }
                    //render menu
                    MenuRenderer.getSingleton().renderMenu(g2d);

                    //notify MapPanel to bring buffer to screen
                    Hashtable<Village, Rectangle> pos = (Hashtable<Village, Rectangle>) villagePositions.clone();
                    MapPanel.getSingleton().updateComplete(pos, mMainBuffer);
                    MapPanel.getSingleton().repaint();
                    g2d.dispose();

                }
            } catch (Throwable t) {
                logger.error("Redrawing map failed", t);
            /*logger.info("Memstat");
            logger.info("  Free: " + Runtime.getRuntime().freeMemory());
            logger.info("  Max: " + Runtime.getRuntime().maxMemory());
            logger.info("  Total: " + Runtime.getRuntime().totalMemory());*/
            }
            try {
                //if (completeRedraw) {
                Thread.sleep(60);
            /*} else {
            Thread.sleep(250);
            }*/
            } catch (InterruptedException ie) {
            }
        }
    }

    private Path2D.Double createArrow() {
        int length = 0;
        int barb = 15;
        double angle = Math.toRadians(20);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-length / 2, 0);
        path.lineTo(length / 2, 0);
        double x = length / 2 - barb * Math.cos(angle);
        double y = barb * Math.sin(angle);
        path.lineTo(x, y);
        double xold = x;
        double yold = y;
        x = length / 2 - barb * Math.cos(-angle);
        y = barb * Math.sin(-angle);
        path.moveTo(length / 2, 0);
        path.lineTo(x, y);
        path.lineTo(xold, yold);
        return path;
    }

    /**Set the drag line externally (done by MapPanel class)*/
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

        if (DataHolder.getSingleton().getVillages() == null) {
            //probably reloading data
            return;
        }

        //get number of drawn villages
        currentZoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        double width = GlobalOptions.getSkin().getCurrentFieldWidth();
        double height = GlobalOptions.getSkin().getCurrentFieldHeight();
        iVillagesX = (int) Math.ceil((double) MapPanel.getSingleton().getWidth() / (double) width);
        iVillagesY = (int) Math.ceil((double) MapPanel.getSingleton().getHeight() / (double) height);
        //add small buffer
        iVillagesX++;
        iVillagesY++;

        //village start
        int xStartVillage = (int) Math.floor(dCenterX - iVillagesX / 2.0);
        int yStartVillage = (int) Math.floor(dCenterY - iVillagesY / 2.0);
        //double start
        double dXStart = dCenterX - (double) iVillagesX / 2.0;
        double dYStart = dCenterY - (double) iVillagesY / 2.0;

        //village end
        int xEndVillage = (int) Math.ceil((double) dCenterX + (double) iVillagesX / 2.0);
        int yEndVillage = (int) Math.ceil((double) dCenterY + (double) iVillagesY / 2.0);

        //correct village count
        iVillagesX = xEndVillage - xStartVillage;
        iVillagesY = yEndVillage - yStartVillage;

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

    /**Render village graphics and create village rectangles for further rendering*/
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
            g2d = layer.createGraphics();
            prepareGraphics(g2d);
        } else {
            //check if image size is still valid
            layer = mLayers.get(MAP_LAYER);
            if (layer.getWidth() != wb || layer.getHeight() != hb) {
                //mappanel has resized
                layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
                mLayers.put(MAP_LAYER, layer);
                g2d = layer.createGraphics();
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

        int width = GlobalOptions.getSkin().getBasicFieldWidth();
        int height = GlobalOptions.getSkin().getBasicFieldHeight();
        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != width) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != height)) {
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

        width = GlobalOptions.getSkin().getCurrentFieldWidth();
        height = GlobalOptions.getSkin().getCurrentFieldHeight();

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
        boolean minimapSkin = GlobalOptions.getSkin().isMinimapSkin();
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
                            //world skin does not fit -> only default underground
                            if (!minimapSkin) {
                                Image worldImage = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, currentZoom);
                                int xp = (int) Math.floor(x + dx);
                                int yp = (int) Math.floor(y + dy);
                                g2d.drawImage(worldImage, xp, yp, null);
                                //check containment using size tolerance
                                if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                    copyRegionsMap.put(worldId, new Point(xp, yp));
                                }
                            } else {
                                int xp = (int) Math.floor(x + dx);
                                int yp = (int) Math.floor(y + dy);
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
                                    type = Skin.ID_B5;
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
                        if (!minimapSkin) {
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
                                if (MapPanel.getSingleton().getBounds().contains(new Rectangle(xp, yp, width + 2, height + 2))) {
                                    copyRegions.put(type, new Point(xp, yp));
                                }
                            }
                            g2d.setColor(cb);
                        }
                    } else {
                        g2d.copyArea(p.x, p.y, width, height, (int) Math.floor(x + dx - p.x), (int) Math.floor(y + dy - p.y));
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

        g2d.dispose();
        mapRedrawRequired = false;
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
            layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
            mLayers.put(TAG_MARKER_LAYER, layer);
            g2d = layer.createGraphics();
            prepareGraphics(g2d);
        } else {
            layer = mLayers.get(TAG_MARKER_LAYER);
            if (layer.getWidth() != wb || layer.getHeight() != hb) {
                layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
                mLayers.put(TAG_MARKER_LAYER, layer);
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

        if (tagsize > GlobalOptions.getSkin().getCurrentFieldHeight() ||
                tagsize > GlobalOptions.getSkin().getCurrentFieldWidth()) {
            return;
        }
        Hashtable<Integer, Point> copyRegions = new Hashtable<Integer, Point>();

        // <editor-fold defaultstate="collapsed" desc="Tag marker graphics drawing">
        try {
            Enumeration<Village> villages = villagePositions.keys();
            Rectangle conquerCopyRegion = null;
            while (villages.hasMoreElements()) {
                Village current = villages.nextElement();
                Conquer c = ConquerManager.getSingleton().getConquer(current);
                Rectangle r = villagePositions.get(current);
                List<Tag> villageTags = TagManager.getSingleton().getTags(current);
                if (villageTags != null && villageTags.size() != 0) {
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

        BufferedImage layer = null;
        Graphics2D g2d = null;
        //prepare drawing buffer
        if (mLayers.get(NOTE_LAYER) == null) {
            layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
            mLayers.put(NOTE_LAYER, layer);
            g2d = layer.createGraphics();
            prepareGraphics(g2d);
        } else {
            layer = mLayers.get(NOTE_LAYER);
            if (layer.getWidth() != wb || layer.getHeight() != hb) {
                layer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_ARGB);
                mLayers.put(NOTE_LAYER, layer);
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

        //render note icons
        Enumeration<Village> keys = villagePositions.keys();
        Hashtable<Integer, Rectangle> markPositions = new Hashtable<Integer, Rectangle>();
        while (keys.hasMoreElements()) {
            Village v = keys.nextElement();
            Rectangle villageRect = villagePositions.get(v);
            Note n = NoteManager.getSingleton().getNoteForVillage(v);
            if (n != null) {
                int nodeIcon = n.getMapMarker();
                int markX = villageRect.x + (int) Math.round(villageRect.width / 2);
                int markY = villageRect.y + (int) Math.round(villageRect.height / 2);
                Rectangle rect = markPositions.get(nodeIcon);

                if (rect == null) {
                    BufferedImage icon = ImageManager.getNoteIcon(nodeIcon);

                    if (MapPanel.getSingleton().getBounds().contains(new Rectangle(markX - 10, markY - icon.getHeight() + 10, icon.getWidth() + 2, icon.getHeight() + 2))) {
                        rect = new Rectangle(markX, markY, icon.getWidth(), icon.getHeight());
                        markPositions.put(nodeIcon, rect);
                    }

                    g2d.drawImage(icon, rect.x - 10, rect.y - icon.getHeight() + 10, null);
                } else {
                    g2d.copyArea(rect.x - 10, rect.y - rect.height + 10, rect.width, rect.height, markX - rect.x, markY - rect.y);
                }
            }
        }
    }

    /**Render marker layer -> drawn on same buffer as map*/
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

        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, wb, hb);


        Marker tribe = null;
        Marker ally = null;
        boolean own = false;
        Color before = g2d.getColor();
        Enumeration<Village> villages = villagePositions.keys();
        while (villages.hasMoreElements()) {
            Village v = villages.nextElement();
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
                if (t != null) {
                    tribe = MarkerManager.getSingleton().getMarker(t);
                    if (t.getAlly() != null) {
                        ally = MarkerManager.getSingleton().getMarker(t.getAlly());
                    }

                }
            }
            if (markerColor != null || tribe != null || ally != null) {
                Rectangle vRect = villagePositions.get(v);

                if (tribe != null && ally != null && !own) {
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
                    g2d.setColor(tribe.getMarkerColor());
                    g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                } else if (ally != null && !own) {
                    g2d.setColor(ally.getMarkerColor());
                    g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                } else {
                    g2d.setColor(markerColor);
                    g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
                }
            } else {
                Rectangle vRect = villagePositions.get(v);
                if (t != null) {
                    g2d.setColor(DEFAULT);
                } else {
                    g2d.setColor(Color.LIGHT_GRAY);
                }
                g2d.fillRect(vRect.x, vRect.y, vRect.width, vRect.height);
            }
        }
        g2d.setColor(before);
    }

    /**Render basic decoration e.g. Active village mark and troops type mark*/
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

        boolean showTroopDensity = false;
        try {
            showTroopDensity = Boolean.parseBoolean(GlobalOptions.getProperty("show.troops.density"));
        } catch (Exception e) {
            showTroopDensity = false;
        }

        Hashtable<Village, Double> values = new Hashtable<Village, Double>();
        Enumeration<Village> villages = villagePositions.keys();
        double maxDef = 0;
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

            if (showTroopDensity) {
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                if (holder != null) {
                    double def = holder.getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
                    values.put(v, def);
                    if (def > maxDef) {
                        maxDef = def;
                    }
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

        if (showTroopDensity) {
            Enumeration<Village> keys = values.keys();
            while (keys.hasMoreElements()) {
                Village v = keys.nextElement();
                Rectangle villageRect = villagePositions.get(v);
                double perc = values.get(v) / maxDef;
                int alpha = (int) Math.rint(perc * 60);
                alpha = (alpha > 80) ? 80 : alpha;
                alpha = 110 - alpha;

                int r = (int) Math.rint((float) 255 * (1.0f - perc) + (float) 180 * perc);
                int g = (int) Math.rint((float) 0 * (1.0f - perc) + (float) 255 * perc);
                int b = (int) Math.rint((float) 0 * (1.0f - perc) + (float) 0 * perc);

                //Color c = new Color(0, 255, 0, alpha);
                Color c = new Color(r, g, b, alpha);
                int size = (int) Math.rint(perc * 5 * villageRect.width);
                if (size > 0) {
                    Color cb = g2d.getColor();
                    g2d.setColor(c);
                    g2d.fillOval(villageRect.x - (int) Math.rint((size - villageRect.width) / 2), villageRect.y - (int) Math.rint((size - villageRect.height) / 2), size, size);// 3 * villageRect.width, 3 * villageRect.height);
                    g2d.setColor(cb);
                }
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
        g2d.setStroke(new BasicStroke(2.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        int width = GlobalOptions.getSkin().getCurrentFieldWidth();
        int height = GlobalOptions.getSkin().getCurrentFieldHeight();
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
                //renader if shown on map or if either source or target are visible
                if (attack.isShowOnMap() && (attack.getSource().isVisibleOnMap() || attack.getTarget().isVisibleOnMap())) {
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
        Color b = g2d.getColor();
        Stroke s = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2.5f));
        Point2D.Double error = GlobalOptions.getSkin().getError();
        try {
            Rectangle2D.Double bounds = new Rectangle2D.Double(viewStartPoint.x, viewStartPoint.y, iVillagesX, iVillagesY);

            for (Village v : DSWorkbenchTroopsFrame.getSingleton().getSelectedTroopsVillages()) {
                List<Village> drawnTargets = new LinkedList<Village>();
                //process source villages
                for (Village target : TroopsManager.getSingleton().getTroopsForVillage(v).getSupportTargets()) {
                    drawnTargets.add(target);
                    Line2D.Double supportLine = new Line2D.Double(v.getX(), v.getY(), target.getX(), target.getY());
                    width += error.x;
                    height += error.y;
                    double xStart = (supportLine.getX1() - viewStartPoint.x) * width + width / 2;
                    double yStart = (supportLine.getY1() - viewStartPoint.y) * height + height / 2;
                    double xEnd = (supportLine.getX2() - viewStartPoint.x) * width + width / 2;
                    double yEnd = (supportLine.getY2() - viewStartPoint.y) * height + height / 2;

                    g2d.setColor(Color.MAGENTA);
                    g2d.drawLine((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                    if (bounds.contains(supportLine.getP1())) {
                        // g2d.fillRect((int) Math.rint(xStart) - 3, (int) Math.rint(yStart) - 1, 6, 6);
                    }

                    if (bounds.contains(supportLine.getP2())) {
                        //g2d.fillOval((int) xEnd - 3, (int) yEnd - 3, 6, 6);


                        AffineTransform at = AffineTransform.getTranslateInstance(xEnd, yEnd);
                        double dx = xEnd - xStart;
                        double dy = yEnd - yStart;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        double theta = Math.asin(dy / dist);
                        at.rotate(theta);
                        //at.scale(2.0, 2.0);
                        Shape shape = at.createTransformedShape(ARROW);
                        g2d.fill(shape);
                    }
                }


                //process target villages
                Enumeration<Village> supportKeys = TroopsManager.getSingleton().getTroopsForVillage(v).getSupports().keys();
                while (supportKeys.hasMoreElements()) {
                    Village source = supportKeys.nextElement();
                    if (!drawnTargets.contains(source)) {
                        Line2D.Double supportLine = new Line2D.Double(source.getX(), source.getY(), v.getX(), v.getY());
                        double xStart = (supportLine.getX1() - viewStartPoint.x) * width + width / 2;
                        double yStart = (supportLine.getY1() - viewStartPoint.y) * height + height / 2;
                        double xEnd = (supportLine.getX2() - viewStartPoint.x) * width + width / 2;
                        double yEnd = (supportLine.getY2() - viewStartPoint.y) * height + height / 2;

                        g2d.setColor(Color.MAGENTA);
                        g2d.drawLine((int) Math.rint(xStart), (int) Math.rint(yStart), (int) Math.rint(xEnd), (int) Math.rint(yEnd));
                        if (bounds.contains(supportLine.getP1())) {
                            //    g2d.fillRect((int) Math.rint(xStart) - 3, (int) Math.rint(yStart) - 1, 6, 6);
                        }

                        if (bounds.contains(supportLine.getP2())) {
                            //  g2d.fillOval((int) xEnd - 3, (int) yEnd - 3, 6, 6);
                        }
                    }
                }


            }
            g2d.setStroke(s);
        } catch (Exception e) {
            g2d.setStroke(s);
        }
        g2d.setColor(b);
    }

    /**Render e.g. forms and church ranges*/
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

        boolean drawChurchRange = Boolean.parseBoolean(GlobalOptions.getProperty("show.church.range"));
        if (!drawChurchRange) {
            //do not draw
            return;
        }
        renderChurches(g2d);
    }

    /**Separate church rendering due to church villages might be outside visible rect,
     * but range drawing is inside
     */
    private void renderChurches(Graphics2D g2d) {
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
        for (Village v : churchVillages) {
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

            // <editor-fold defaultstate="collapsed" desc=" Church calculation">
            if (drawVillage) {
                Church c = ChurchManager.getSingleton().getChurch(v);
                int vx = MapPanel.getSingleton().virtualPosToSceenPos(v.getX(), v.getY()).x;
                int vy = MapPanel.getSingleton().virtualPosToSceenPos(v.getX(), v.getY()).y;
                g = new Rectangle(vx, vy, GlobalOptions.getSkin().getCurrentFieldWidth(), GlobalOptions.getSkin().getCurrentFieldHeight());

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
                Color cb = g2d.getColor();
                Composite com = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                Color radiusColor = c.getRangeColor();
                /*  try {
                if (v.getTribe().equals(currentUserVillage.getTribe())) {
                radiusColor = Color.YELLOW;
                }
                } catch (Exception e) {
                if (v.getTribe() == null) {
                radiusColor = Color.LIGHT_GRAY;
                } else {
                radiusColor = null;
                }
                }
                if (radiusColor == null) {
                Marker m = MarkerManager.getSingleton().getMarker(v.getTribe());
                if (m != null) {
                radiusColor = m.getMarkerColor();
                } else {
                try {
                if (Integer.parseInt(GlobalOptions.getProperty("default.mark")) == 1) {
                radiusColor = Color.RED;
                } else {
                radiusColor = Color.WHITE;
                }
                } catch (Exception e) {
                radiusColor = Color.WHITE;
                }
                }
                }*/
                g2d.setColor(radiusColor);
                //g2d.setPaint(new RoundGradientPaint(g.getCenterX(), g.getCenterY(), new Color(0, 0, 255, 155), new Point2D.Double(rad * g.getWidth() + g.getWidth(), rad * g.getHeight() + g.getHeight()), new Color(0, 0, 255, 0)));
                g2d.setStroke(new BasicStroke(10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(p);
                g2d.fill(p);
                g2d.setComposite(com);
                g2d.setColor(cb);
            }
        //</editor-fold>
        }
    }

    /**Render e.g. drag line, radar, popup*/
    private void renderLiveLayer(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }

        int width = GlobalOptions.getSkin().getCurrentFieldWidth();
        int height = GlobalOptions.getSkin().getCurrentFieldHeight();
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
            } catch (Exception e) {
            }
        }
// </editor-fold>

        if (Boolean.parseBoolean(GlobalOptions.getProperty("show.map.popup"))) {
            renderVillageInfo(g2d, mouseVillage);
        }
    }

    /**Rendering map popup (called by renderLiveLayer())*/
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

        Rectangle rect = villagePositions.get(mouseVillage);
        if (rect == null) {
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
        int width = 500;
        Rectangle villageRect = (Rectangle) rect.clone();
        int delta = 500 + villageRect.getLocation().x - MapPanel.getSingleton().getWidth();
        if (delta > 20) {
            //relevant part of popup might be outside the FOV
            villageRect.setLocation(villageRect.x - delta, villageRect.y);
        }

        int xc = (int) villageRect.getCenterX();
        int yc = (int) villageRect.getCenterY();
        //Village name rect
        int dy = 19;
        g2d.setColor(Constants.DS_BACK);
        g2d.fillRect(xc, yc, width, 19);
        g2d.drawRect(xc, yc, width, 19);
        g2d.setColor(Color.BLACK);
        Rectangle2D bounds = metrics.getStringBounds(mouseVillage.getName(), g2d);
        g2d.drawString(mouseVillage.toString(), xc + 17, yc - (int) Math.rint(bounds.getY()) + 2);
        String bonus = getBonusType(mouseVillage);
        if (bonus != null) {
            drawPopupField(g2d, metrics, xc, yc, null, bonus, width, dy);
            dy += 19;
        }

        current = new Font("SansSerif", Font.PLAIN, 12);
        metrics = g2d.getFontMetrics(current);
        g2d.setFont(current);

        //Points rect
        String value = nf.format(mouseVillage.getPoints());
        drawPopupField(g2d, metrics, xc, yc, "Punkte", value, width, dy);
        dy += 19;

        //tags
        List<Tag> tags = TagManager.getSingleton().getTags(mouseVillage);
        if ((tags != null) && (!tags.isEmpty())) {
            value = "";
            List<String> tagLines = new LinkedList<String>();
            for (int i = 0; i < tags.size(); i++) {
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

                drawPopupField(g2d, metrics, xc, yc, "Tags", line, width, dy);

                int lines = tagLines.size();
                for (int i = 0; i < lines - 1; i++) {
                    dy += 19;
                    drawPopupField(g2d, metrics, xc, yc, "", tagLines.remove(0), width, dy);
                }

                if (!tagLines.isEmpty()) {
                    dy += 19;
                    line = tagLines.remove(0);
                    line = line.substring(0, line.lastIndexOf(","));
                    drawPopupField(g2d, metrics, xc, yc, "", line, width, dy);
                }
                dy += 19;
            }

        }

        //Tribe rect
        if (t != null) {
            if (showRanks) {
                value = t.getName() + " (" + nf.format(t.getPoints()) + " | " + t.getRank() + ")";
                drawPopupField(g2d, metrics, xc, yc, "Besitzer (Punkte | Rang)", value, width, dy);
            } else {
                value = t.getName() + " (" + nf.format(t.getPoints()) + ")";
                drawPopupField(g2d, metrics, xc, yc, "Besitzer (Punkte)", value, width, dy);
            }

            dy += 19;
            if (showConquers) {
                if (showRanks) {
                    value = nf.format(t.getKillsAtt()) + " (" + nf.format(t.getRankAtt()) + "), " + nf.format(t.getKillsDef()) + " (" + nf.format(t.getRankDef()) + ")";
                    drawPopupField(g2d, metrics, xc, yc, "Besiegte Gegner (Off, Def)", value, width, dy);
                } else {
                    value = nf.format(t.getKillsAtt()) + ", " + nf.format(t.getKillsDef()) + ")";
                    drawPopupField(g2d, metrics, xc, yc, "Besiegte Gegner (Off, Def)", value, width, dy);
                }

                dy += 19;
            }

            //Ally rect
            if (a != null) {
                if (showRanks) {
                    value = a.getTag() + " (" + nf.format(a.getAll_points()) + " | " + a.getRank() + ")";
                    drawPopupField(g2d, metrics, xc, yc, "Stamm (Punkte | Rang)", value, width, dy);
                } else {
                    value = a.getTag() + " (" + nf.format(a.getAll_points()) + ")";
                    drawPopupField(g2d, metrics, xc, yc, "Stamm (Punkte)", value, width, dy);
                }

                dy += 19;
            }

            if (showMoral) {
                double moral = ((mouseVillage.getTribe().getPoints() / currentUserVillage.getTribe().getPoints()) * 3 + 0.3) * 100;
                moral = (moral > 100) ? 100 : moral;
                drawPopupField(g2d, metrics, xc, yc, "Moral", nf.format(moral) + "%", width, dy);
                dy += 19;
            }

            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Conquer c = ConquerManager.getSingleton().getConquer(mouseVillage);
            if (c != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis((long) c.getTimestamp() * 1000);
                drawPopupField(g2d, metrics, xc, yc, "Adelung am", f.format(cal.getTime()), width, dy);
                dy += 19;
                drawPopupField(g2d, metrics, xc, yc, "Zustimmung", Integer.toString(c.getCurrentAcceptance()), width, dy);
                dy += 19;
            }
        } else {
            value = "verlassen";
            drawPopupField(g2d, metrics, xc, yc, null, value, width, dy);
            dy += 19;
        }

        //render troop/runtime information
        renderExtendedInformation(g2d, mouseVillage, rect, xc, yc, width, dy);
        g2d.setFont(before);
        g2d.setStroke(sBefore);
    }

    /**Get bonus text, depending on server version*/
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

    /**Draw one single field of map popup*/
    private void drawPopupField(Graphics2D g2d, FontMetrics pMetrics, int pX, int pY, String pName, String pValue, int pWidth, int pDy) {
        g2d.setColor(Constants.DS_BACK_LIGHT);
        g2d.fillRect(pX, pY + pDy, pWidth, 19);
        g2d.setColor(Constants.DS_BACK);
        g2d.drawRect(pX, pY + pDy, pWidth, 19);

        int dx = 0;
        if (pName != null) {
            dx = 150;
            g2d.drawRect(pX, pY + pDy, dx, 19);
            g2d.setColor(Color.BLACK);
            Rectangle2D bounds = pMetrics.getStringBounds(pName, g2d);
            g2d.drawString(pName, pX + 2, pY + pDy - (int) Math.rint(bounds.getY()) + 2);
        } else {
            g2d.setColor(Color.BLACK);
        }

        Rectangle2D bounds = pMetrics.getStringBounds(pValue, g2d);
        g2d.drawString(pValue, pX + dx + 2, pY + pDy - (int) Math.rint(bounds.getY()) + 2);
    }

    /**Render extended information to map popup e.g. troop information*/
    private void renderExtendedInformation(Graphics2D g2d, Village pMouseVillage, Rectangle pRect, int pX, int pY, int pWidth, int pDy) {
        VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(pMouseVillage);
        Font current = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        boolean drawDist = false;
        if (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_MEASURE) {
            if (mSourceVillage != null) {
                current = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
                drawDist = true;
            }

        }
        //if no runtime drawing, check troops
        if (!drawDist) {
            if ((troops == null) || (troops.getTroopsInVillage().isEmpty())) {
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
        g2d.fillRect(pX, pY + pDy, pWidth, 35);
        g2d.setColor(Constants.DS_BACK);
        g2d.drawRect(pX, pY + pDy, pWidth, 35);

        int x = 0;
        int w = (int) Math.floor((double) (pWidth - 4.0) / (double) DataHolder.getSingleton().getUnits().size());
        int unitCount = 0;

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (!drawDist) {
                //draw troop information
                int cnt = troops.getTroopsInVillage().get(unit);
                if (cnt > 0) {
                    if (unitCount % 2 == 0) {
                        g2d.setColor(Constants.DS_BACK);
                    } else {
                        g2d.setColor(Constants.DS_BACK_LIGHT);
                    }

                    g2d.fillRect(pX + x + 2, pY + pDy, w, 35);
                    g2d.drawImage(ImageManager.getUnitImage(unit), pX + x + 2 + (int) Math.rint(w / 2.0 - 9), pY + pDy + 2, null);
                    String troopsValue = nf.format(cnt);
                    Rectangle2D troopBounds = metrics.getStringBounds(troopsValue, g2d);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(troopsValue, pX + x + 2 + (int) Math.rint(w / 2.0 - troopBounds.getWidth() / 2.0), pY + pDy + 2 + 25 + (int) Math.rint(troopBounds.getHeight() / 2.0));
                    x += w;
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

                g2d.fillRect(pY + x + 2, pY + pDy, w, 35);
                g2d.drawImage(ImageManager.getUnitImage(unit), pX + x + 2 + (int) Math.rint(w / 2.0 - 9), pY + pDy + 2, null);
                String runtimeValue = DSCalculator.formatTimeInMinutes(runtime);
                Rectangle2D troopBounds = metrics.getStringBounds(runtimeValue, g2d);
                g2d.setColor(Color.BLACK);
                g2d.drawString(runtimeValue, pX + x + 2 + (int) Math.rint(w / 2.0 - troopBounds.getWidth() / 2.0), pY + pDy + 2 + 25 + (int) Math.rint(troopBounds.getHeight() / 2.0));
                x += w;
                unitCount++;
            }
        }
    }

    /**Prepare any g2d object with same parameters*/
    private void prepareGraphics(Graphics2D pG2d) {
        pG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        pG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        // Speed
        pG2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        pG2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        pG2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        pG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
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
class RoundGradientPaint implements Paint {

    protected Point2D point;
    protected Point2D mRadius;
    protected Color mPointColor,  mBackgroundColor;

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

    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
            Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
        Point2D transformedPoint = xform.transform(point, null);
        Point2D transformedRadius = xform.deltaTransform(mRadius, null);
        return new RoundGradientContext(transformedPoint, mPointColor,
                transformedRadius, mBackgroundColor);
    }

    public int getTransparency() {
        int a1 = mPointColor.getAlpha();
        int a2 = mBackgroundColor.getAlpha();
        return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
    }
}

class RoundGradientContext implements PaintContext {

    protected Point2D mPoint;
    protected Point2D mRadius;
    protected Color color1,  color2;

    public RoundGradientContext(Point2D p, Color c1, Point2D r, Color c2) {
        mPoint = p;
        color1 = c1;
        mRadius = r;
        color2 = c2;
    }

    public void dispose() {
    }

    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    public Raster getRaster(int x, int y, int w, int h) {
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(
                w, h);

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

