/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Church;
import de.tor.tribes.types.drawing.AbstractForm;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.FormConfigFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.ServerSettings;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.*;
import java.util.List;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import org.apache.log4j.Logger;

/**
 * Map Renderer which supports "dirty layers" defining which layer has to be
 * redrawn.<BR/> Layer order with z-ID:<BR/> 0: Marker Layer -> redraw after
 * marker changes (triggered by MarkerManager) 1: Village Layer: Redraw on
 * global events (e.g. init or resize) or after changes on scaling or skin 2:
 * Misc. Basic Map Decoration: e.g. Sector or continent drawing on demand
 * (triggered by SettingsDialog) 3: Attack Vector Layer: Redraw after attack
 * changes (triggered by AttackManager) 4: Misc. Extended Map Decoration: e.g.
 * troop qualification or active village marker 5: Live Layer: Redraw in every
 * drawing cycle e.g. Drag line, tool popup(?), (troop movement?) 6-16: Free
 * assignable
 *
 * @author Charon
 */
public class MapRenderer {

    private static Logger logger = Logger.getLogger("MapRenderer");
    public static final int ALL_LAYERS = 0;
    public static final int MAP_LAYER = 1;
    public static final int MARKER_LAYER = 2;
    public static final int BASIC_DECORATION_LAYER = 3;
    public static final int ATTACK_LAYER = 4;
    public static final int TAG_MARKER_LAYER = 5;
    public static final int LIVE_LAYER = 6;
    public static final int NOTE_LAYER = 7;
    public static final int TROOP_LAYER = 8;
    private boolean bMapRedrawRequired = true;
    private Village[][] mVisibleVillages = null;
    private HashMap<Village, Rectangle> mVillagePositions = null;
    private int iVillagesX = 0;
    private int iVillagesY = 0;
    private double dCenterX = 500.0;
    private double dCenterY = 500.0;
    private int xDragLineEnd = 0;
    private int yDragLineEnd = 0;
    private Village mSourceVillage = null;
    private Point2D.Double viewStartPoint = null;
    private double dCurrentFieldWidth = 1.0;
    private double dCurrentFieldHeight = 1.0;
    private double dCurrentZoom = 0.0;
    private List<Integer> mDrawOrder = null;
    private Popup mInfoPopup = null;
    private Village mPopupVillage = null;
    private BufferedImage mBackBuffer = null;
    private Hashtable<Ally, Integer> mAllyCount = new Hashtable<>();
    private Hashtable<Tribe, Integer> mTribeCount = new Hashtable<>();
    private Hashtable<Village, AnimatedVillageInfoRenderer> mAnimators = new Hashtable<>();
    //rendering layers
    private MapLayerRenderer mMapLayer = new MapLayerRenderer();
    private TroopDensityLayerRenderer mTroopDensityLayer = new TroopDensityLayerRenderer();
    private ChurchLayerRenderer mChurchLayer = new ChurchLayerRenderer();
    private FormLayerRenderer mFormsLayer = new FormLayerRenderer();
    private TagMarkerLayerRenderer mTagLayer = new TagMarkerLayerRenderer();
    private AttackLayerRenderer mAttackLayer = new AttackLayerRenderer();
    private SupportLayerRenderer mSupportLayer = new SupportLayerRenderer();
    private NoteLayerRenderer mNoteLayer = new NoteLayerRenderer();
    /**
     * RenderSettings used within the last rendering cycle
     */
    private RenderSettings mRenderSettings = null;

    public MapRenderer() {
        mVisibleVillages = new Village[iVillagesX][iVillagesY];
        mDrawOrder = new LinkedList<>();
        Vector<String> layerVector = new Vector<>(Constants.LAYER_COUNT);
        for (int i = 0; i < Constants.LAYER_COUNT; i++) {
            layerVector.add("");
        }

        Enumeration<String> values = Constants.LAYERS.keys();
        while (values.hasMoreElements()) {
            String layer = values.nextElement();
            layerVector.set(Constants.LAYERS.get(layer), layer);
        }

        for (String s : layerVector) {
            mDrawOrder.add(Constants.LAYERS.get(s));
        }
    }

    /**
     * Set the order all layers are drawn
     *
     * @param pDrawOrder
     */
    public void setDrawOrder(List<Integer> pDrawOrder) {
        mDrawOrder = new LinkedList<>(pDrawOrder);
    }

    /**
     * Complete redraw on resize or scroll
     *
     * @param pType
     */
    public synchronized void initiateRedraw(int pType) {
        if (pType == TAG_MARKER_LAYER) {
            mTagLayer.reset();
            //System.out.println("TAG");
        }

        if (pType == MARKER_LAYER) {
            ////  System.out.println("MARK");
            mMapLayer.reset();
            bMapRedrawRequired = true;
        }

        if (pType == NOTE_LAYER) {
            //  System.out.println("NOTE");
            mNoteLayer.reset();
        }
        if (pType == TROOP_LAYER) {
            //  System.out.println("TROOP");
            mTroopDensityLayer.reset();
        }
        if (pType == ALL_LAYERS) {
            mMapLayer.reset();
            mTroopDensityLayer.reset();
            mTagLayer.reset();
            mNoteLayer.reset();
            bMapRedrawRequired = true;
        }
        if (pType == MAP_LAYER) {
            //   System.out.println("MAP");
            bMapRedrawRequired = true;
        }
    }

    public synchronized boolean isRedrawScheduled() {
        return bMapRedrawRequired;
    }

    public RenderSettings getRenderSettings() {
        return mRenderSettings;
    }

    public void renderAll(Graphics2D pG2d) {
        if (GlobalOptions.isMinimal()) {
            return;
        }
        try {
            int w = MapPanel.getSingleton().getWidth();
            int h = MapPanel.getSingleton().getHeight();
            if ((w != 0) && (h != 0)) {
                Graphics2D g2d = null;
                if (mRenderSettings == null) {
                    //settings not set yet, use current map position
                    mRenderSettings = new RenderSettings(MapPanel.getSingleton().getVirtualBounds());
                }
                if (mBackBuffer == null) {
                    //create main buffer during first iteration
                    mBackBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.OPAQUE);
                    mBackBuffer.setAccelerationPriority(1);
                    g2d = (Graphics2D) mBackBuffer.getGraphics();
                    ImageUtils.setupGraphics(g2d);
                    //set redraw required flag if nothin was drawn yet
                    bMapRedrawRequired = true;
                } else {
                    //check if image size is still valid
                    //if not re-create main buffer
                    if (mBackBuffer.getWidth(null) != w || mBackBuffer.getHeight(null) != h) {
                        //map panel has resized
                        mBackBuffer = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.OPAQUE);
                        mBackBuffer.setAccelerationPriority(1);
                        g2d = (Graphics2D) mBackBuffer.getGraphics();
                        ImageUtils.setupGraphics(g2d);
                        //set redraw required flag if size has changed
                        initiateRedraw(ALL_LAYERS);
                        bMapRedrawRequired = true;
                    } else {
                        //only get graphics
                        g2d = (Graphics2D) mBackBuffer.getGraphics();
                    }
                }
                if (bMapRedrawRequired) {
                    //if the entire map has to be redrawn, reset the render settings
                    mRenderSettings = new RenderSettings(MapPanel.getSingleton().getVirtualBounds());
                }
                g2d.setClip(0, 0, w, h);
                //get currently selected user village for marking -> one call reduces sync effort
                dCurrentZoom = mRenderSettings.getZoom();

                if (bMapRedrawRequired) {
                    //complete redraw is required
                    calculateVisibleVillages();
                    if (viewStartPoint == null) {
                        throw new Exception("View position is 'null', skip redraw");
                    }
                    bMapRedrawRequired = false;
                }
                mRenderSettings.setVisibleVillages(mVisibleVillages);
                //get the movement of the map relative to a) the last reset or b) the last rendering cycle
                mRenderSettings.calculateSettings(MapPanel.getSingleton().getVirtualBounds());

                boolean markOnTop = false;
                if (mDrawOrder.indexOf(0) > mDrawOrder.indexOf(1)) {
                    markOnTop = true;
                }
                boolean gotMap = false;
                boolean gotMarkers = false;

                for (Integer layer : mDrawOrder) {
                    //check for marker and map layer
                    if (layer == 0) {
                        gotMarkers = true;
                    } else if (layer == 1) {
                        gotMap = true;
                    }

                    if (gotMap && gotMarkers) {
                        //we can now draw the map layer and draw all following layers
                        try {
                            mMapLayer.setMarkOnTop(markOnTop);
                            mMapLayer.performRendering(mRenderSettings, g2d);
                        } catch (Exception e) {
                            logger.warn("Failed to render map/marker layer", e);
                        }
                        gotMap = false;
                        gotMarkers = false;
                    }

                    //check for all other layers
                    if (layer == 2) {
                        try {
                            mTagLayer.performRendering(mRenderSettings, g2d);
                        } catch (Exception e) {
                            logger.warn("Failed to render group layer");
                        }
                    } else if (layer == 3) {
                        //render troop density
                        try {
                            mTroopDensityLayer.performRendering(mRenderSettings, g2d);
                        } catch (Exception e) {
                            logger.warn("Failed to render troop density layer");
                        }
                    } else if (layer == 4) {
                        try {
                            mNoteLayer.performRendering(mRenderSettings, g2d);
                        } catch (Exception e) {
                            logger.warn("Failed to render note layer", e);
                        }
                    } else if (layer == 5) {
                        try {
                            mAttackLayer.performRendering(mRenderSettings, g2d);
                        } catch (Exception e) {
                            logger.warn("Failed to render attack layer");
                        }
                    } else if (layer == 6) {
                        try {
                            mSupportLayer.performRendering(mRenderSettings, g2d);
                        } catch (Exception e) {
                            logger.warn("Failed to render support layer");
                        }
                    } else if (layer == 7) {
                        try {
                            mFormsLayer.performRendering(mRenderSettings, g2d);
                        } catch (Exception e) {
                            logger.warn("Failed to render forms layer");
                        }
                    } else if (layer == 8) {
                        try {
                            mChurchLayer.performRendering(mRenderSettings, g2d);
                        } catch (Exception e) {
                            logger.warn("Failed to render church layer");
                        }
                    }
                }

                //draw live layer -> always on top
                try {
                    //new FarmLayerRenderer(mVillagePositions).performRendering(mRenderSettings, g2d);
                    renderLiveLayer(g2d);
                } catch (Exception e) {
                    logger.warn("Failed to render live layer");
                }
                //render selection
                de.tor.tribes.types.drawing.Rectangle selection = MapPanel.getSingleton().getSelectionRect();
                if (selection != null) {
                    selection.renderForm(g2d);
                }
                g2d.dispose();
                //store the map position rendered in this cycle in the render settings
                mRenderSettings = new RenderSettings(mRenderSettings.getMapBounds());
                pG2d.drawImage(mBackBuffer, 0, 0, null);
                MapPanel.getSingleton().updateComplete(mVillagePositions, mBackBuffer);
            }
        } catch (Throwable t) {
            logger.error("Redrawing map failed", t);
        }
    }

    /**
     * Set the drag line externally (done by MapPanel class)
     *
     * @param pXS
     * @param pYS
     * @param pXE
     * @param pYE
     */
    public void setDragLine(int pXS, int pYS, int pXE, int pYE) {
        if (pXS == -1 && pYS == -1 && pXE == -1 && pYE == -1) {
            mSourceVillage = null;
            xDragLineEnd = 0;
            yDragLineEnd = 0;
        } else {
            mSourceVillage = DataHolder.getSingleton().getVillages()[pXS][pYS];
            xDragLineEnd = pXE;
            yDragLineEnd = pYE;
        }
    }

    /**
     * Extract the visible villages (only needed on full repaint)
     */
    private void calculateVisibleVillages() {
        dCenterX = MapPanel.getSingleton().getCurrentPosition().x;
        dCenterY = MapPanel.getSingleton().getCurrentPosition().y;
        mVillagePositions = new HashMap<>();
        mAllyCount.clear();
        mTribeCount.clear();
        if (DataHolder.getSingleton().isLoading()) {
            //probably reloading data
            return;
        }

        dCurrentFieldWidth = GlobalOptions.getSkin().getCurrentFieldWidth(dCurrentZoom);
        dCurrentFieldHeight = GlobalOptions.getSkin().getCurrentFieldHeight(dCurrentZoom);

        //ceil
        iVillagesX = (int) Math.ceil((double) MapPanel.getSingleton().getWidth() / dCurrentFieldWidth);
        iVillagesY = (int) Math.ceil((double) MapPanel.getSingleton().getHeight() / dCurrentFieldHeight);
        //village start
        int xStartVillage = (int) Math.floor(dCenterX - iVillagesX / 2.0);
        int yStartVillage = (int) Math.floor(dCenterY - iVillagesY / 2.0);
        //double start

        double dXStart = dCenterX - (double) iVillagesX / 2.0;
        double dYStart = dCenterY - (double) iVillagesY / 2.0;

        //village end
        int xEndVillage = (int) Math.floor(dXStart + iVillagesX);
        int yEndVillage = (int) Math.floor(dYStart + iVillagesY);

        //correct village count
        viewStartPoint = new Point2D.Double(dXStart, dYStart);

        double dx = 0 - ((viewStartPoint.x - Math.floor(viewStartPoint.x)) * dCurrentFieldWidth);
        double dy = 0 - ((viewStartPoint.y - Math.floor(viewStartPoint.y)) * dCurrentFieldHeight);

        if (dx * dCurrentFieldWidth + iVillagesX * dCurrentFieldWidth < MapPanel.getSingleton().getWidth()) {
            xEndVillage++;
        }
        if (dy * dCurrentFieldHeight + iVillagesY * dCurrentFieldHeight < MapPanel.getSingleton().getHeight()) {
            yEndVillage++;
        }
        iVillagesX = xEndVillage - xStartVillage;
        iVillagesY = yEndVillage - yStartVillage;

        mVisibleVillages = new Village[iVillagesX + 1][iVillagesY + 1];

        int x = 0;
        int y = 0;
        int mapW = ServerSettings.getSingleton().getMapDimension().width;
        int mapH = ServerSettings.getSingleton().getMapDimension().height;
        for (int i = xStartVillage; i <= xEndVillage; i++) {
            for (int j = yStartVillage; j <= yEndVillage; j++) {
                if ((i < 0) || (i > mapW - 1) || (j < 0) || (j > mapH - 1)) {
                    //handle villages outside map
                    mVisibleVillages[x][y] = null;
                } else {
                    mVisibleVillages[x][y] = DataHolder.getSingleton().getVillages()[i][j];
                    if (mVisibleVillages[x][y] != null) {
                        Point villagePos = new Point((int) Math.floor(dx + x * dCurrentFieldWidth), (int) Math.floor(dy + y * dCurrentFieldHeight));
                        mVillagePositions.put(mVisibleVillages[x][y], new Rectangle(villagePos.x, villagePos.y, (int) Math.floor(dCurrentFieldWidth), (int) Math.floor(dCurrentFieldHeight)));
                        Tribe t = mVisibleVillages[x][y].getTribe();
                        if (t != Barbarians.getSingleton()) {
                            if (!mTribeCount.containsKey(t)) {
                                mTribeCount.put(t, 1);
                            } else {
                                mTribeCount.put(t, mTribeCount.get(t) + 1);
                            }
                            Ally a = t.getAlly();
                            if (a == null) {
                                a = NoAlly.getSingleton();
                            }
                            if (!mAllyCount.containsKey(a)) {
                                mAllyCount.put(a, 1);
                            } else {
                                mAllyCount.put(a, mAllyCount.get(a) + 1);
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
        return dCurrentZoom;
    }

    public Hashtable<Tribe, Integer> getTribeCount() {
        return (Hashtable<Tribe, Integer>) mTribeCount.clone();
    }

    public Hashtable<Ally, Integer> getAllyCount() {
        return (Hashtable<Ally, Integer>) mAllyCount.clone();
    }

    /**
     * Render e.g. drag line, radar, popup
     */
    private void renderLiveLayer(Graphics2D g2d) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }
        int cursor = MapPanel.getSingleton().getCurrentCursor();
        Village mouseVillage = MapPanel.getSingleton().getVillageAtMousePos();
        //render temp form
        if (!FormConfigFrame.getSingleton().isInEditMode()) {
            //only render in create mode to avoid multi-drawing
            AbstractForm f = FormConfigFrame.getSingleton().getCurrentForm();

            if (f != null) {
                f.renderForm(g2d);
            }
        }
        // <editor-fold defaultstate="collapsed" desc=" Draw Drag line (Foreground)">
        Line2D.Double dragLine = new Line2D.Double(-1, -1, xDragLineEnd, yDragLineEnd);
        if (mSourceVillage != null) {
            //must draw drag line
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(5.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            //get map regions for source and target village
            Rectangle sourceRect = mVillagePositions.get(mSourceVillage);
            Rectangle targetRect = null;
            if (mouseVillage != null) {
                targetRect = mVillagePositions.get(mouseVillage);
            }

            //check which region is visible
            if (sourceRect != null && targetRect != null) {
                //source and target are visible and selected. Draw drag line between them
                dragLine.setLine(sourceRect.x + sourceRect.width / 2, sourceRect.y + sourceRect.height / 2, targetRect.x + targetRect.width / 2, targetRect.y + targetRect.height / 2);
            } else if (sourceRect != null && targetRect == null) {
                //source region is visible, target village is not selected
                dragLine.setLine(sourceRect.x + sourceRect.width / 2, sourceRect.y + sourceRect.height / 2, xDragLineEnd, yDragLineEnd);
            } else {
                //source and target region not invisible/selected
                dragLine.setLine((mSourceVillage.getX() - viewStartPoint.x) * (int) Math.rint(dCurrentFieldWidth), (mSourceVillage.getY() - viewStartPoint.y) * (int) Math.rint(dCurrentFieldHeight), xDragLineEnd, yDragLineEnd);
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

        boolean mouseDown = MapPanel.getSingleton().isMouseDown();

        // <editor-fold defaultstate="collapsed" desc="Mark current players villages">

        if (!mouseDown && GlobalOptions.getProperties().getBoolean("highlight.tribes.villages", false)) {
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
                Set<Map.Entry<Village, Rectangle>> entries = mVillagePositions.entrySet();

                for (Map.Entry<Village, Rectangle> entry : entries) {
                    Village v = entry.getKey();
                    if ((v.getTribe() == null && mouseTribe.equals(Barbarians.getSingleton()))
                            || (v.getTribe() != null && mouseTribe.getId() == v.getTribe().getId())) {
                        Rectangle r = entry.getValue();
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
        // <editor-fold defaultstate="collapsed" desc=" Draw radar information ">
        Village radarVillage = MapPanel.getSingleton().getRadarVillage();
        List<Village> radarVillages = new LinkedList<>();
        //add radar village
        if (radarVillage != null) {
            radarVillages.add(radarVillage);
        }

        //add mouse village if radar tool is selected
        if (mouseVillage != null && cursor == ImageManager.CURSOR_RADAR) {
            //check if radar village == mouse village
            if (!mouseDown && !radarVillages.contains(mouseVillage)) {
                radarVillages.add(mouseVillage);
            }
        }

        for (Village v : radarVillages) {
            try {
                int cnt = 0;
                for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                    de.tor.tribes.types.drawing.Circle c = new de.tor.tribes.types.drawing.Circle();
                    int r = GlobalOptions.getProperties().getInt("radar.size", 1);
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
                    c.setStrokeWidth(3f);
                    c.setXPosEnd(xp + diam);
                    c.setYPosEnd(yp + diam);
                    Color co = Color.decode(GlobalOptions.getProperties().getString(u.getName() + ".color", "#FF0000"));
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

        // <editor-fold defaultstate="collapsed" desc="Draw live church">
        Church tmpChurch = new Church();
        if (mouseVillage != null && mVillagePositions != null) {
            tmpChurch.setVillage(mouseVillage);
            if (cursor == ImageManager.CURSOR_CHURCH_1) {
                tmpChurch.setRange(2);
            } else if (cursor == ImageManager.CURSOR_CHURCH_2) {
                tmpChurch.setRange(6);
            } else if (cursor == ImageManager.CURSOR_CHURCH_3) {
                tmpChurch.setRange(8);
            } else {
                tmpChurch = null;
            }
            if (tmpChurch != null) {
                Rectangle r = mVillagePositions.get(mouseVillage);
                if (r != null) {
                    Composite cb = g2d.getComposite();
                    Color cob = g2d.getColor();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
                    GeneralPath p = ChurchLayerRenderer.calculateChurchPath(tmpChurch, mouseVillage, r.width, r.height);
                    g2d.setColor(Constants.DS_BACK_LIGHT);
                    g2d.fill(p);
                    g2d.setComposite(cb);
                    g2d.setColor(Constants.DS_BACK);
                    g2d.draw(p);
                    g2d.setColor(cob);
                }
            }
        }

        // </editor-fold>


        //draw additional info
        if (!mouseDown && mouseVillage != null && Boolean.parseBoolean(GlobalOptions.getProperty("show.mouseover.info"))) {
            Rectangle rect = mVillagePositions.get(mouseVillage);
            AnimatedVillageInfoRenderer animator = mAnimators.get(mouseVillage);
            if (animator == null) {
                animator = new AnimatedVillageInfoRenderer(mouseVillage);
                mAnimators.put(mouseVillage, animator);
            }
            animator.update(mouseVillage, rect, g2d);
        }

        Enumeration<Village> iterator = mAnimators.keys();
        while (iterator.hasMoreElements()) {
            Village next = iterator.nextElement();
            AnimatedVillageInfoRenderer animator = mAnimators.get(next);
            if (animator.isFinished()) {
                mAnimators.remove(next);
            } else {
                animator.update(mouseVillage, mVillagePositions.get(animator.getVillage()), g2d);
            }
        }

        List<Village> marked = Arrays.asList(MapPanel.getSingleton().getMarkedVillages());
        if (!marked.isEmpty()) {
            Set<Map.Entry<Village, Rectangle>> entries = mVillagePositions.entrySet();
            Color cBefore = g2d.getColor();
            for (Map.Entry<Village, Rectangle> entry : entries) {
                Village v = entry.getKey();
                Rectangle villageRect = entry.getValue();
                if (marked.contains(v)) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillOval(villageRect.x + villageRect.width - 10, villageRect.y, 10, 10);
                }
            }
            g2d.setColor(cBefore);
        }
        if (GlobalOptions.getProperties().getBoolean("show.ruler", true)) {
            //ruler drawing
            HashMap<Color, Rectangle> vertRulerParts = new HashMap<>();
            HashMap<Color, Rectangle> horRulerParts = new HashMap<>();
            double xVillage = Math.floor(viewStartPoint.x);
            double yVillage = Math.floor(viewStartPoint.y);
            double rulerStart = -1 * dCurrentFieldWidth * (viewStartPoint.x - xVillage);
            double rulerEnd = -1 * dCurrentFieldHeight * (viewStartPoint.y - yVillage);
            Composite com = g2d.getComposite();
            Color c = g2d.getColor();

            Rectangle activeRow = null;
            Rectangle activeCol = null;
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
                            rulerPart = new Rectangle(0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(dCurrentFieldHeight), (int) Math.rint(dCurrentFieldWidth), (int) Math.rint(dCurrentFieldHeight));
                            if (MapPanel.getSingleton().getBounds().contains(rulerPart)) {
                                vertRulerParts.put(g2d.getColor(), rulerPart);
                            }

                            if (g2d.getColor() == Constants.DS_BACK) {
                                g2d.fill3DRect(0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(dCurrentFieldHeight), (int) Math.rint(dCurrentFieldWidth), (int) Math.rint(dCurrentFieldHeight), true);
                            } else {
                                g2d.fillRect(0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(dCurrentFieldHeight), (int) Math.rint(dCurrentFieldWidth), (int) Math.rint(dCurrentFieldHeight));
                            }
                        } else {
                            g2d.copyArea(rulerPart.x, rulerPart.y, rulerPart.width, rulerPart.height, 0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(dCurrentFieldHeight) - rulerPart.y);
                        }

                        if (mouseVillage != null && mouseVillage.getY() == (yVillage + j)) {
                            activeRow = new Rectangle(0, rulerPart.y, rulerPart.width, rulerPart.height);
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
                            rulerPart = new Rectangle((int) Math.floor(rulerStart) + i * (int) Math.rint(dCurrentFieldWidth), 0, (int) Math.rint(dCurrentFieldWidth), (int) Math.rint(dCurrentFieldHeight));
                            if (MapPanel.getSingleton().getBounds().contains(rulerPart)) {
                                horRulerParts.put(g2d.getColor(), rulerPart);
                            }

                            if (g2d.getColor() == Constants.DS_BACK) {
                                g2d.fill3DRect((int) Math.floor(rulerStart) + i * (int) Math.rint(dCurrentFieldWidth), 0, (int) Math.rint(dCurrentFieldWidth), (int) Math.rint(dCurrentFieldHeight), true);
                            } else {
                                g2d.fillRect((int) Math.floor(rulerStart) + i * (int) Math.rint(dCurrentFieldWidth), 0, (int) Math.rint(dCurrentFieldWidth), (int) Math.rint(dCurrentFieldHeight));
                            }

                        } else {
                            g2d.copyArea(rulerPart.x, rulerPart.y, rulerPart.width, rulerPart.height, (int) Math.floor(rulerStart) + i * (int) Math.rint(dCurrentFieldWidth) - rulerPart.x, 0);
                        }

                        if (mouseVillage != null && mouseVillage.getX() == (xVillage + i)) {
                            activeCol = new Rectangle(rulerPart.x, 0, rulerPart.width, rulerPart.height);
                        }
                    }
                }
            }

            //draw active village marker
            if (activeRow != null && activeCol != null) {
                g2d.setColor(Color.YELLOW);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.fillRect(0, activeRow.y, activeRow.width, activeRow.height);
                g2d.fillRect(activeCol.x, 0, activeCol.width, activeCol.height);
            }

            g2d.setComposite(com);

            //draw font
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
                        double fact = (double) ((int) Math.rint(dCurrentFieldWidth) - 2) / w;
                        AffineTransform f = g2d.getTransform();
                        AffineTransform t = AffineTransform.getTranslateInstance(0, (int) Math.floor(rulerEnd) + j * (int) Math.rint(dCurrentFieldHeight));
                        t.scale(fact, 1);
                        g2d.setTransform(t);
                        g2d.drawString(coord, 1, (int) Math.rint(dCurrentFieldHeight) - 2);
                        g2d.setTransform(f);
                    } else if (i != 0 && j == 0) {
                        //draw horizontal values
                        String coord = Integer.toString((int) xVillage + i);
                        double w = g2d.getFontMetrics().getStringBounds(coord, g2d).getWidth();
                        double fact = (double) ((int) Math.rint(dCurrentFieldWidth) - 2) / w;
                        int dy = -2;
                        if ((xVillage + i) % 2 == 0) {
                            g2d.setColor(Color.DARK_GRAY);
                        } else {
                            g2d.setColor(Color.BLACK);
                        }

                        AffineTransform f = g2d.getTransform();
                        AffineTransform t = AffineTransform.getTranslateInstance((int) Math.floor(rulerStart) + i * (int) Math.rint(dCurrentFieldWidth), (int) Math.rint(dCurrentFieldHeight));
                        t.scale(fact, 1);
                        g2d.setTransform(t);
                        g2d.drawString(coord, 1, dy);
                        g2d.setTransform(f);
                    }
                }
            }
            //insert 'stopper'
            g2d.setColor(Constants.DS_BACK);
            g2d.fill3DRect(0, 0, (int) Math.rint(dCurrentFieldWidth), (int) Math.rint(dCurrentFieldHeight), true);
            g2d.setColor(c);
        }

        if (!MapPanel.getSingleton().isMouseDown()
                && Boolean.parseBoolean(GlobalOptions.getProperty("show.map.popup"))
                && !DSWorkbenchMainFrame.getSingleton().isGlasspaneVisible()) {
            try {
                if (DSWorkbenchMainFrame.getSingleton().isActive() && MapPanel.getSingleton().getMousePosition() != null) {
                    if (mouseVillage == null) {
                        if (mInfoPopup != null) {
                            mInfoPopup.hide();
                            mInfoPopup = null;
                        }

                        mPopupVillage = null;
                    } else {
                        if (!mouseVillage.equals(mPopupVillage)) {
                            if (mInfoPopup != null) {
                                mInfoPopup.hide();
                            }

                            mPopupVillage = mouseVillage;
                            JToolTip tt = new JToolTip();

                            tt.setTipText(mPopupVillage.getExtendedTooltip());
                            PopupFactory popupFactory = PopupFactory.getSharedInstance();
                            mInfoPopup = popupFactory.getPopup(MapPanel.getSingleton(), tt, MouseInfo.getPointerInfo().getLocation().x + 10, MouseInfo.getPointerInfo().getLocation().y + 10);
                            // JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                            mInfoPopup.show();
                        }
                    }
                } else {
                    if (mInfoPopup != null) {
                        mInfoPopup.hide();
                        mInfoPopup = null;
                    }

                    mPopupVillage = null;
                }

            } catch (Exception e) {
                if (mInfoPopup != null) {
                    mInfoPopup.hide();
                    mInfoPopup = null;
                }

                mPopupVillage = null;
            }

        } else {
            //no popup shown
            if (mInfoPopup != null) {
                mInfoPopup.hide();
                mPopupVillage = null;
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
