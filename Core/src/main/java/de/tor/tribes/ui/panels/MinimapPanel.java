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
package de.tor.tribes.ui.panels;

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.*;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MinimapListener;
import de.tor.tribes.ui.windows.MinimapZoomFrame;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.interfaces.ToolChangeListener;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 * @author Torridity
 */
public class MinimapPanel extends javax.swing.JPanel implements GenericManagerListener {

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        redraw();
    }
    private static Logger logger = Logger.getLogger("MinimapCanvas");
    private Image mBuffer = null;
    private int iX = 0;
    private int iY = 0;
    private int iWidth = 0;
    private int iHeight = 0;
    private MinimapZoomFrame mZoomFrame = null;
    private int iCurrentCursor = ImageManager.CURSOR_DEFAULT;
    private static MinimapPanel SINGLETON = null;
    private ScreenshotPanel mScreenshotPanel = null;
    private List<MinimapListener> mMinimapListeners = null;
    private List<ToolChangeListener> mToolChangeListeners = null;
    private boolean doRedraw = false;
    private int iXDown = 0;
    private int iYDown = 0;
    private Rectangle2D rDrag = null;
    private Rectangle rVisiblePart = null;
    boolean zoomed = false;
    boolean showControls = false;
    private static final int ID_MINIMAP = 0;
    private static final int ID_ALLY_CHART = 1;
    private static final int ID_TRIBE_CHART = 2;
    private Hashtable<Integer, Rectangle> minimapButtons = new Hashtable<Integer, Rectangle>();
    private Hashtable<Integer, BufferedImage> minimapIcons = new Hashtable<Integer, BufferedImage>();
    private int iCurrentView = ID_MINIMAP;
    private BufferedImage mChartImage;
    private int lastHash = 0;

    public static synchronized MinimapPanel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MinimapPanel();
        }
        return SINGLETON;
    }

    /**
     * Creates new form MinimapPanel
     */
    MinimapPanel() {
        initComponents();
        setSize(300, 300);
        mMinimapListeners = new LinkedList<MinimapListener>();
        mToolChangeListeners = new LinkedList<ToolChangeListener>();
        setCursor(ImageManager.getCursor(iCurrentCursor));
        mScreenshotPanel = new ScreenshotPanel();
        minimapButtons.put(ID_MINIMAP, new Rectangle(2, 2, 26, 26));
        minimapButtons.put(ID_ALLY_CHART, new Rectangle(30, 2, 26, 26));
        minimapButtons.put(ID_TRIBE_CHART, new Rectangle(60, 2, 26, 26));
        try {
            minimapIcons.put(ID_MINIMAP, ImageIO.read(new File("./graphics/icons/minimap.png")));
            minimapIcons.put(ID_ALLY_CHART, ImageIO.read(new File("./graphics/icons/ally_chart.png")));
            minimapIcons.put(ID_TRIBE_CHART, ImageIO.read(new File("./graphics/icons/tribe_chart.png")));
        } catch (Exception e) {
        }
        jPanel1.add(mScreenshotPanel);
        int mapWidth = (int) ServerSettings.getSingleton().getMapDimension().getWidth();
        int mapHeight = (int) ServerSettings.getSingleton().getMapDimension().getHeight();
        rVisiblePart = new Rectangle(0, 0, mapWidth, mapHeight);
        zoomed = false;
        MarkerManager.getSingleton().addManagerListener(this);
        TagManager.getSingleton().addManagerListener(this);
        MinimapRepaintThread.getSingleton().setVisiblePart(rVisiblePart);
        if (!GlobalOptions.isMinimal()) {
            MinimapRepaintThread.getSingleton().start();
        }
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!showControls && e.getButton() != MouseEvent.BUTTON1) {
                    //show controls
                    Point p = e.getPoint();
                    p.translate(-5, -5);
                    showControls(p);
                    return;
                }
                if (!showControls && iCurrentView == ID_MINIMAP) {
                    Point p = mousePosToMapPosition(e.getX(), e.getY());
                    DSWorkbenchMainFrame.getSingleton().centerPosition(p.x, p.y);
                    MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
                    if (mZoomFrame != null) {
                        if (mZoomFrame.isVisible()) {
                            mZoomFrame.toFront();
                        }
                    }
                } else {
                    if (minimapButtons.get(ID_MINIMAP).contains(e.getPoint())) {
                        iCurrentView = ID_MINIMAP;
                        mBuffer = null;
                        showControls = false;
                        MinimapRepaintThread.getSingleton().update();
                    } else if (minimapButtons.get(ID_ALLY_CHART).contains(e.getPoint())) {
                        iCurrentView = ID_ALLY_CHART;
                        lastHash = 0;
                        showControls = false;
                        updateComplete(null);
                    } else if (minimapButtons.get(ID_TRIBE_CHART).contains(e.getPoint())) {
                        iCurrentView = ID_TRIBE_CHART;
                        lastHash = 0;
                        showControls = false;
                        updateComplete(null);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (iCurrentView != ID_MINIMAP) {
                    return;
                }
                if (iCurrentCursor == ImageManager.CURSOR_SHOT || iCurrentCursor == ImageManager.CURSOR_ZOOM) {
                    iXDown = e.getX();
                    iYDown = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (iCurrentView != ID_MINIMAP) {
                    return;
                }
                if (rDrag == null) {
                    return;
                }
                if (iCurrentCursor == ImageManager.CURSOR_SHOT) {
                    try {
                        BufferedImage i = MinimapRepaintThread.getSingleton().getBuffer();
                        int mapWidth = (int) ServerSettings.getSingleton().getMapDimension().getWidth();
                        int mapHeight = (int) ServerSettings.getSingleton().getMapDimension().getHeight();
                        int x = (int) Math.rint((double) mapWidth / (double) getWidth() * rDrag.getX());
                        int y = (int) Math.rint((double) mapHeight / (double) getHeight() * rDrag.getY());
                        int w = (int) Math.rint((double) mapWidth / (double) getWidth() * (rDrag.getWidth() - rDrag.getX()));
                        int h = (int) Math.rint((double) mapHeight / (double) getHeight() * (rDrag.getHeight() - rDrag.getY()));
                        BufferedImage sub = i.getSubimage(x, y, w, h);
                        mScreenshotPanel.setBuffer(sub);
                        jPanel1.setSize(mScreenshotPanel.getSize());
                        jPanel1.setPreferredSize(mScreenshotPanel.getSize());
                        jPanel1.setMinimumSize(mScreenshotPanel.getSize());
                        jPanel1.setMaximumSize(mScreenshotPanel.getSize());
                        jScreenshotPreview.pack();
                        jScreenshotControl.pack();
                        jScreenshotPreview.setVisible(true);
                        jScreenshotControl.setVisible(true);
                    } catch (Exception ie) {
                        logger.error("Failed to initialize mapshot", ie);
                    }
                } else if (iCurrentCursor == ImageManager.CURSOR_ZOOM) {
                    if (!zoomed) {
                        int mapWidth = (int) ServerSettings.getSingleton().getMapDimension().getWidth();
                        int mapHeight = (int) ServerSettings.getSingleton().getMapDimension().getHeight();
                        int x = (int) Math.rint((double) mapWidth / (double) getWidth() * rDrag.getX());
                        int y = (int) Math.rint((double) mapHeight / (double) getHeight() * rDrag.getY());
                        int w = (int) Math.rint((double) mapWidth / (double) getWidth() * (rDrag.getWidth() - rDrag.getX()));

                        if (w >= 10) {
                            rVisiblePart = new Rectangle(x, y, w, w);
                            MinimapRepaintThread.getSingleton().setVisiblePart(rVisiblePart);
                            redraw();
                            zoomed = true;
                        }
                    } else {
                        int mapWidth = (int) ServerSettings.getSingleton().getMapDimension().getWidth();
                        int mapHeight = (int) ServerSettings.getSingleton().getMapDimension().getHeight();
                        rVisiblePart = new Rectangle(0, 0, mapWidth, mapHeight);
                        MinimapRepaintThread.getSingleton().setVisiblePart(rVisiblePart);
                        redraw();
                        zoomed = false;
                    }
                    mZoomFrame.setVisible(false);
                }
                iXDown = 0;
                iYDown = 0;
                rDrag = null;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (iCurrentView != ID_MINIMAP) {
                    return;
                }
                switch (iCurrentCursor) {
                    case ImageManager.CURSOR_ZOOM: {
                        if (mZoomFrame != null) {
                            mZoomFrame.setVisible(true);
                        }
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (mZoomFrame != null) {
                    if (mZoomFrame.isVisible()) {
                        mZoomFrame.setVisible(false);
                    }
                    iXDown = 0;
                    iYDown = 0;
                    rDrag = null;
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (iCurrentView != ID_MINIMAP) {
                    return;
                }
                switch (iCurrentCursor) {
                    case ImageManager.CURSOR_MOVE: {
                        Point p = mousePosToMapPosition(e.getX(), e.getY());
                        DSWorkbenchMainFrame.getSingleton().centerPosition(p.x, p.y);
                        rDrag = null;
                        break;
                    }
                    case ImageManager.CURSOR_SHOT: {
                        rDrag = new Rectangle2D.Double(iXDown, iYDown, e.getX(), e.getY());
                        break;
                    }
                    case ImageManager.CURSOR_ZOOM: {
                        rDrag = new Rectangle2D.Double(iXDown, iYDown, e.getX(), e.getY());
                        break;
                    }
                }

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (iCurrentView == ID_MINIMAP) {
                    switch (iCurrentCursor) {
                        case ImageManager.CURSOR_ZOOM: {
                            if (mZoomFrame != null) {
                                if (!mZoomFrame.isVisible()) {
                                    mZoomFrame.setVisible(true);
                                }
                                int mapWidth = (int) ServerSettings.getSingleton().getMapDimension().getWidth();
                                int mapHeight = (int) ServerSettings.getSingleton().getMapDimension().getHeight();

                                int x = (int) Math.rint((double) mapWidth / (double) getWidth() * (double) e.getX());
                                int y = (int) Math.rint((double) mapHeight / (double) getHeight() * (double) e.getY());
                                mZoomFrame.updatePosition(x, y);
                            }
                            break;
                        }
                        default: {
                            if (mZoomFrame != null) {
                                if (mZoomFrame.isVisible()) {
                                    mZoomFrame.setVisible(false);
                                }
                            }
                        }
                    }
                }
                Point location = minimapButtons.get(ID_MINIMAP).getLocation();
                location.translate(-2, -2);
                if (!new Rectangle(location.x, location.y, 88, 30).contains(e.getPoint())) {
                    //hide controls
                    showControls = false;
                    repaint();
                }
            }
        });

        addMouseWheelListener(
                new MouseWheelListener() {

                    @Override
                    public void mouseWheelMoved(MouseWheelEvent e) {



                        if (iCurrentView != ID_MINIMAP) {
                            return;
                        }
                        iCurrentCursor += e.getWheelRotation();
                        if (iCurrentCursor == ImageManager.CURSOR_DEFAULT + e.getWheelRotation()) {
                            if (e.getWheelRotation() < 0) {
                                iCurrentCursor = ImageManager.CURSOR_SHOT;
                            } else {
                                iCurrentCursor = ImageManager.CURSOR_MOVE;
                            }
                        } else if (iCurrentCursor < ImageManager.CURSOR_MOVE) {
                            iCurrentCursor = ImageManager.CURSOR_DEFAULT;
                        } else if (iCurrentCursor > ImageManager.CURSOR_SHOT) {
                            iCurrentCursor = ImageManager.CURSOR_DEFAULT;
                        }
                        if (iCurrentCursor != ImageManager.CURSOR_ZOOM) {
                            if (mZoomFrame != null) {
                                if (mZoomFrame.isVisible()) {
                                    mZoomFrame.setVisible(false);
                                }
                            }
                        } else {
                            if (mZoomFrame != null) {
                                mZoomFrame.setVisible(true);
                            }
                        }
                        setCurrentCursor(iCurrentCursor);
                    }
                });

    }

    private void showControls(Point p) {
        minimapButtons.get(ID_MINIMAP).setLocation(p.x + 2, p.y + 2);
        minimapButtons.get(ID_ALLY_CHART).setLocation(p.x + 30, p.y + 2);
        minimapButtons.get(ID_TRIBE_CHART).setLocation(p.x + 60, p.y + 2);
        showControls = true;
        repaint();
    }

    public synchronized void addMinimapListener(MinimapListener pListener) {
        mMinimapListeners.add(pListener);
    }

    public synchronized void removeMinimapListener(MinimapListener pListener) {
        mMinimapListeners.remove(pListener);
    }

    public synchronized void addToolChangeListener(ToolChangeListener pListener) {
        mToolChangeListeners.add(pListener);
    }

    public synchronized void removeToolChangeListener(ToolChangeListener pListener) {
        mToolChangeListeners.remove(pListener);
    }

    public Point mousePosToMapPosition(double pX, double pY) {
        int x = rVisiblePart.x;
        int y = rVisiblePart.y;
        //calc dots per village

        double dpvx = (double) getWidth() / (double) rVisiblePart.width;
        double dpvy = (double) getHeight() / (double) rVisiblePart.height;
        x += (int) Math.round(pX / dpvx);
        y += (int) Math.round(pY / dpvy);

        return new Point(x, y);
    }

    public void setCurrentCursor(int pCurrentCursor) {
        iCurrentCursor = pCurrentCursor;
        setCursor(ImageManager.getCursor(iCurrentCursor));
        fireToolChangedEvents(iCurrentCursor);
    }

    public void setSelection(int pX, int pY, int pWidth, int pHeight) {
        iX = pX;
        iY = pY;
        iWidth = pWidth;
        iHeight = pHeight;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        try {
            Graphics2D g2d = (Graphics2D) g;
            g2d.clearRect(0, 0, getWidth(), getHeight());
            g2d.drawImage(mBuffer, 0, 0, null);

            if (iCurrentView == ID_MINIMAP) {
                g2d.setColor(Color.YELLOW);

                int mapWidth = rVisiblePart.width;
                int mapHeight = rVisiblePart.height;

                int w = (int) Math.rint(((double) getWidth() / mapWidth) * (double) iWidth);
                int h = (int) Math.rint(((double) getHeight() / mapHeight) * (double) iHeight);

                double posX = ((double) getWidth() / mapWidth * (double) (iX - rVisiblePart.x)) - w / 2;
                double posY = ((double) getHeight() / mapHeight * (double) (iY - rVisiblePart.y)) - h / 2;

                g2d.drawRect((int) Math.rint(posX), (int) Math.rint(posY), w, h);

                if (iCurrentCursor == ImageManager.CURSOR_SHOT) {
                    if (rDrag != null) {
                        g2d.setColor(Color.ORANGE);
                        g2d.drawRect((int) rDrag.getMinX(), (int) rDrag.getMinY(), (int) (rDrag.getWidth() - rDrag.getX()), (int) (rDrag.getHeight() - rDrag.getY()));
                    }
                } else if (iCurrentCursor == ImageManager.CURSOR_ZOOM) {
                    if (rDrag != null) {
                        g2d.setColor(Color.CYAN);
                        g2d.drawRect((int) rDrag.getMinX(), (int) rDrag.getMinY(), (int) (rDrag.getWidth() - rDrag.getX()), (int) (rDrag.getWidth() - rDrag.getX()));
                    }
                }
            }

            if (showControls) {
                //g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
                Rectangle r = minimapButtons.get(ID_MINIMAP);
                g2d.setColor(Color.WHITE);
                Point menuPos = r.getLocation();
                menuPos.translate(-2, -2);
                //draw border
                g2d.fillRect(menuPos.x, menuPos.y, 88, 30);
                g2d.setColor(Color.BLACK);
                //check if mouse is inside minimap button
                if (getMousePosition() != null && r.contains(getMousePosition())) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(r.x, r.y, r.width, r.height);
                    g2d.setColor(Color.BLACK);
                }
                g2d.drawImage(minimapIcons.get(ID_MINIMAP), r.x, r.y, null);
                g2d.drawRect(r.x, r.y, r.width, r.height);


                r = minimapButtons.get(ID_ALLY_CHART);
                //check if mouse is inside ally chart button
                if (getMousePosition() != null && r.contains(getMousePosition())) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(r.x, r.y, r.width, r.height);
                    g2d.setColor(Color.BLACK);
                }
                g2d.drawImage(minimapIcons.get(ID_ALLY_CHART), r.x, r.y, null);
                g2d.drawRect(r.x, r.y, r.width, r.height);

                r = minimapButtons.get(ID_TRIBE_CHART);
                //check if mouse is inside tribe chart button
                if (getMousePosition() != null && r.contains(getMousePosition())) {
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(r.x, r.y, r.width, r.height);
                    g2d.setColor(Color.BLACK);


                }
                g2d.drawImage(minimapIcons.get(ID_TRIBE_CHART), r.x, r.y, null);
                g2d.drawRect(r.x, r.y, r.width, r.height);
            }
            g2d.dispose();
        } catch (Exception e) {
            logger.error("Failed painting Minimap", e);
        }
    }

    public void makeScreenshot() {
        jScreenshotPreview.setVisible(true);
    }

    public void resetBuffer() {
        mBuffer = null;
        redraw();
    }

    protected void updateComplete(BufferedImage pBuffer) {
        try {
            if (iCurrentView == ID_MINIMAP) {
                if (mZoomFrame == null) {
                    mZoomFrame = new MinimapZoomFrame(pBuffer);
                    mZoomFrame.setSize(300, 300);
                    mZoomFrame.setLocation(0, 0);
                }
                if (mBuffer == null) {
                    if (pBuffer == null) {
                        MinimapRepaintThread.getSingleton().update();
                        return;
                    }

                    mBuffer = pBuffer.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
                } else if ((mBuffer.getWidth(null) != getWidth()) || (mBuffer.getHeight(null) != getHeight())) {
                    mZoomFrame.setMinimap(pBuffer);
                    mBuffer = pBuffer.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
                } else if (doRedraw) {
                    mZoomFrame.setMinimap(pBuffer);
                    mBuffer = pBuffer.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
                }
            } else {
                // long s = System.currentTimeMillis();
                int hash = MapPanel.getSingleton().getMapRenderer().getAllyCount().hashCode();
                if (lastHash != hash) {
                    renderChartInfo();
                    mBuffer = mChartImage;
                    lastHash = hash;
                }
                // System.out.println("dur " + (System.currentTimeMillis() - s));
            }
            repaint();
            doRedraw = false;

        } catch (Exception e) {
            logger.error("Exception while updating Minimap", e);
            //ignore


        }
    }

    private void renderChartInfo() {
        Hashtable<Object, Marker> marks = new Hashtable<Object, Marker>();
        DefaultPieDataset dataset = buildDataset(marks);

        JFreeChart chart = ChartFactory.createPieChart(
                null, // chart title
                dataset, // data
                true, // include legend
                true,
                false);
        chart.setBackgroundPaint(null);
        //chart.setBorderStroke(null);
        chart.setBorderVisible(false);
        final PiePlot plot = (PiePlot) chart.getPlot();
        // plot.setBackgroundPaint(null);
        //  plot.setShadowPaint(null);

        Enumeration<Object> markKeys = marks.keys();


        while (markKeys.hasMoreElements()) {
            if (iCurrentView == ID_ALLY_CHART) {
                Ally a = (Ally) markKeys.nextElement();
                plot.setSectionPaint(a.getTag(), marks.get(a).getMarkerColor());
            } else {
                Tribe t = (Tribe) markKeys.nextElement();
                plot.setSectionPaint(t.getName(), marks.get(t).getMarkerColor());
            }
        }
        //plot.setCircular(true);
        //  plot.setMaximumLabelWidth(30.0);
     /*
         * plot.setLabelGenerator(new StandardPieSectionLabelGenerator( "{0} = {2}", NumberFormat.getNumberInstance(),
         * NumberFormat.getPercentInstance()));
         */
        //   chart.getLegend().setVerticalAlignment(VerticalAlignment.CENTER);
        //  chart.getLegend().setPosition(RectangleEdge.RIGHT);
        // plot.setMaximumLabelWidth(20.0);
        plot.setLabelGenerator(null);
        plot.setBackgroundPaint(Constants.DS_BACK);
        /*
         * plot.setInteriorGap(0.0); plot.setLabelGap(0.0);
         */
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} = {2}", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()));

        /*
         * plot.getL plot.setLabelFont(g2d.getFont().deriveFont(10.0f));
         */


        //plot.setLabelGenerator(null);

        //plot.setMaximumLabelWidth(30.0);
        //plot.getLabelDistributor().distributeLabels(10.0, 20.0);
        //chart.draw(g2d, new Rectangle2D.Float(20, 20, 100, 100));

        //  g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        plot.setOutlineVisible(false);
        mChartImage = chart.createBufferedImage(getWidth(), getHeight());
        //chart.draw(g2d, new Rectangle2D.Float(50, 50, 400, 400));
        //g2d.drawImage(bi, 30, 30, null);

        //  g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        //bi = chart.createBufferedImage(240, 240);
        // g2d.drawImage(bi, 30, 30, null);
    }

    private DefaultPieDataset buildDataset(Hashtable<Object, Marker> marks) {
        DefaultPieDataset dataset = new DefaultPieDataset();


        if (iCurrentView == ID_ALLY_CHART) {
            Hashtable<Ally, Integer> allyCount = MapPanel.getSingleton().getMapRenderer().getAllyCount();
            int overallVillages = 0;
            Enumeration<Ally> keys = allyCount.keys();
            //count all villages
            while (keys.hasMoreElements()) {
                overallVillages += allyCount.get(keys.nextElement());
            }
            keys = allyCount.keys();

            double rest = 0;
            // Hashtable<Ally, Marker> marks = new Hashtable<Ally, Marker>();

            while (keys.hasMoreElements()) {
                Ally a = keys.nextElement();
                Integer v = allyCount.get(a);
                Double perc = new Double((double) v / (double) overallVillages * 100);

                if (perc > 5.0) {
                    dataset.setValue(a.getTag(), perc);
                    Marker m = MarkerManager.getSingleton().getMarker(a);

                    if (m != null) {
                        marks.put(a, m);
                    }
                    dataset.setValue(a.getTag(), new Double((double) v / (double) overallVillages * 100));
                } else {
                    rest += perc;
                }
            }

            dataset.setValue("Sonstige", rest);
        } else {
            Hashtable<Tribe, Integer> tribeCount = MapPanel.getSingleton().getMapRenderer().getTribeCount();

            int overallVillages = 0;
            Enumeration<Tribe> keys = tribeCount.keys();
            //count all villages

            while (keys.hasMoreElements()) {
                overallVillages += tribeCount.get(keys.nextElement());
            }
            keys = tribeCount.keys();

            double rest = 0;
            //  Hashtable<Tribe, Marker> marks = new Hashtable<Tribe, Marker>();

            while (keys.hasMoreElements()) {
                Tribe t = keys.nextElement();

                Integer v = tribeCount.get(t);

                Double perc = new Double((double) v / (double) overallVillages * 100);
                if (perc > 5.0) {
                    dataset.setValue(t.getName(), perc);
                    Marker m = MarkerManager.getSingleton().getMarker(t);
                    if (m != null) {
                        marks.put(t, m);
                    }
                    dataset.setValue(t.getName(), new Double((double) v / (double) overallVillages * 100));
                } else {
                    rest += perc;
                }
            }

            dataset.setValue("Sonstige", rest);
        }
        return dataset;
    }

    public void redraw() {
        doRedraw = true;

        try {
            MinimapRepaintThread.getSingleton().update();
        } catch (Exception e) {
        }
    }

    public void redraw(boolean pResize) {
        int mapWidth = (int) ServerSettings.getSingleton().getMapDimension().getWidth();
        int mapHeight = (int) ServerSettings.getSingleton().getMapDimension().getHeight();
        rVisiblePart = new Rectangle(0, 0, mapWidth, mapHeight);
        MinimapRepaintThread.getSingleton().setVisiblePart(rVisiblePart);
        redraw();
    }

    public synchronized void fireToolChangedEvents(int pTool) {
        for (ToolChangeListener l : mToolChangeListeners) {
            l.fireToolChangedEvent(pTool);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScreenshotControl = new javax.swing.JFrame();
    jScalingSlider = new javax.swing.JSlider();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jFileTypeChooser = new javax.swing.JComboBox();
    jButton1 = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    jLabel3 = new javax.swing.JLabel();
    jTransparancySlider = new javax.swing.JSlider();
    jScreenshotPreview = new javax.swing.JDialog();
    jPanel1 = new javax.swing.JPanel();

    jScreenshotControl.setTitle("Einstellungen");
    jScreenshotControl.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        fireScreenshotControlClosingEvent(evt);
      }
    });

    jScalingSlider.setMajorTickSpacing(1);
    jScalingSlider.setMaximum(10);
    jScalingSlider.setMinimum(1);
    jScalingSlider.setPaintLabels(true);
    jScalingSlider.setPaintTicks(true);
    jScalingSlider.setSnapToTicks(true);
    jScalingSlider.setValue(1);
    jScalingSlider.setOpaque(false);
    jScalingSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        fireChangeScreenshotScalingEvent(evt);
      }
    });

    jLabel1.setText("Zoom");

    jLabel2.setText("Dateityp");

    jFileTypeChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "png", "gif", "jpeg" }));

    jButton1.setText("Schließen");
    jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        fireCloseScreenshotEvent(evt);
      }
    });

    jButton2.setText("Speichern");
    jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        fireSaveScreenshotEvent(evt);
      }
    });

    jLabel3.setText("Legendentransparenz");

    jTransparancySlider.setMajorTickSpacing(1);
    jTransparancySlider.setMaximum(10);
    jTransparancySlider.setPaintLabels(true);
    jTransparancySlider.setPaintTicks(true);
    jTransparancySlider.setSnapToTicks(true);
    jTransparancySlider.setToolTipText("Transparenz der Legende (10 = keine Legende)");
    jTransparancySlider.setValue(0);
    jTransparancySlider.setOpaque(false);

    javax.swing.GroupLayout jScreenshotControlLayout = new javax.swing.GroupLayout(jScreenshotControl.getContentPane());
    jScreenshotControl.getContentPane().setLayout(jScreenshotControlLayout);
    jScreenshotControlLayout.setHorizontalGroup(
      jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jScreenshotControlLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jScreenshotControlLayout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(jButton1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton2))
          .addGroup(jScreenshotControlLayout.createSequentialGroup()
            .addGroup(jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel1)
              .addComponent(jLabel3)
              .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jTransparancySlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
              .addComponent(jScalingSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
              .addComponent(jFileTypeChooser, 0, 236, Short.MAX_VALUE))))
        .addContainerGap())
    );
    jScreenshotControlLayout.setVerticalGroup(
      jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jScreenshotControlLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jScalingSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jTransparancySlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jFileTypeChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel2))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
        .addGroup(jScreenshotControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jButton2)
          .addComponent(jButton1))
        .addContainerGap())
    );

    jScreenshotPreview.setTitle("Vorschau");
    jScreenshotPreview.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        fireMapPreviewClosingEvent(evt);
      }
    });

    jPanel1.setBackground(new java.awt.Color(102, 255, 102));
    jPanel1.setOpaque(false);
    jPanel1.setPreferredSize(new java.awt.Dimension(0, 0));
    jPanel1.setLayout(new java.awt.BorderLayout());

    javax.swing.GroupLayout jScreenshotPreviewLayout = new javax.swing.GroupLayout(jScreenshotPreview.getContentPane());
    jScreenshotPreview.getContentPane().setLayout(jScreenshotPreviewLayout);
    jScreenshotPreviewLayout.setHorizontalGroup(
      jScreenshotPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jScreenshotPreviewLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
        .addContainerGap())
    );
    jScreenshotPreviewLayout.setVerticalGroup(
      jScreenshotPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jScreenshotPreviewLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
        .addContainerGap())
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 304, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 142, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents

private void fireChangeScreenshotScalingEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChangeScreenshotScalingEvent
    mScreenshotPanel.setScaling(jScalingSlider.getValue());
    jPanel1.setSize(mScreenshotPanel.getSize());
    jPanel1.setPreferredSize(mScreenshotPanel.getSize());
    jPanel1.setMinimumSize(mScreenshotPanel.getSize());
    jPanel1.setMaximumSize(mScreenshotPanel.getSize());
    jScreenshotPreview.pack();
}//GEN-LAST:event_fireChangeScreenshotScalingEvent

private void fireCloseScreenshotEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseScreenshotEvent
    jScreenshotPreview.setVisible(false);
    jScreenshotControl.setVisible(false);
}//GEN-LAST:event_fireCloseScreenshotEvent
private void fireSaveScreenshotEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSaveScreenshotEvent
    String dir = GlobalOptions.getProperty("screen.dir");
    if (dir == null) {
        dir = ".";
    }
    JFileChooser chooser = null;
    try {
        chooser = new JFileChooser(dir);
    } catch (Exception e) {
        JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n" + "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
        return;
    }
    chooser.setDialogTitle("Speichern unter...");
    chooser.setSelectedFile(new File("map"));

    final String type = (String) jFileTypeChooser.getSelectedItem();
    chooser.setFileFilter(new FileFilter() {

        @Override
        public boolean accept(File f) {
            return (f != null) && (f.isDirectory() || f.getName().endsWith(type));
        }

        @Override
        public String getDescription() {
            return "*." + type;
        }
    });


    int ret = chooser.showSaveDialog(jScreenshotControl);


    if (ret == JFileChooser.APPROVE_OPTION) {
        try {
            File f = chooser.getSelectedFile();
            String file = f.getCanonicalPath();
            if (!file.endsWith(type)) {
                file += "." + type;
            }
            File target = new File(file);
            if (target.exists()) {
                //ask if overwrite
                if (JOptionPaneHelper.showQuestionConfirmBox(jScreenshotControl, "Existierende Datei überschreiben?", "Überschreiben", "Nein", "Ja") != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            ImageIO.write(mScreenshotPanel.getResult(jTransparancySlider.getValue()), type, target);
            GlobalOptions.addProperty("screen.dir", target.getParent());


        } catch (Exception e) {
            logger.error("Failed to write map shot", e);
        }
    }
}//GEN-LAST:event_fireSaveScreenshotEvent

private void fireMapPreviewClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireMapPreviewClosingEvent
    jScreenshotControl.setVisible(false);
}//GEN-LAST:event_fireMapPreviewClosingEvent
private void fireScreenshotControlClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireScreenshotControlClosingEvent
    jScreenshotPreview.setVisible(false);
}//GEN-LAST:event_fireScreenshotControlClosingEvent

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        DataHolder.getSingleton().loadData(false);
        JFrame f = new JFrame();
        f.getContentPane().add(MinimapPanel.getSingleton());
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

    }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JComboBox jFileTypeChooser;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JSlider jScalingSlider;
  private javax.swing.JFrame jScreenshotControl;
  private javax.swing.JDialog jScreenshotPreview;
  private javax.swing.JSlider jTransparancySlider;
  // End of variables declaration//GEN-END:variables
}

class MinimapRepaintThread extends Thread {

    private static Logger logger = Logger.getLogger("MinimapRenderer");
    private BufferedImage mBuffer = null;
    private boolean drawn = false;
    private Dimension mapDim = null;
    private static MinimapRepaintThread SINGLETON = null;
    private Rectangle visiblePart = null;

    public static synchronized MinimapRepaintThread getSingleton() {
        if (SINGLETON == null) {
            try {
                SINGLETON = new MinimapRepaintThread();
            } catch (Exception e) {
                SINGLETON = null;
            }
        }

        return SINGLETON;
    }

    MinimapRepaintThread() {
        setName("MinimapUpdater");
        setDaemon(true);
    }

    public void setVisiblePart(Rectangle pVisible) {
        visiblePart = (Rectangle) pVisible.clone();
    }

    public void update() {
        Dimension currentDim = ServerSettings.getSingleton().getMapDimension();
        if (currentDim == null) {
            return;
        }
        if ((mapDim == null) || (mapDim.width != currentDim.width) || (mapDim.height != currentDim.height)) {
            if (mapDim == null) {
                mapDim = (Dimension) currentDim.clone();
            } else {
                mapDim.setSize(currentDim);
            }
            mBuffer = ImageUtils.createCompatibleBufferedImage(mapDim.width, mapDim.height, BufferedImage.OPAQUE);
        }
        drawn = false;
    }

    protected BufferedImage getBuffer() {
        return mBuffer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!drawn) {
                    drawn = redraw();
                }

                MinimapPanel.getSingleton().updateComplete(mBuffer);
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            } catch (Exception oe) {
                if (mBuffer == null) {
                    update();
                } else {
                    logger.error("Failed to re-render minimap", oe);
                }
            }
        }
    }

    private boolean redraw() {
        Village[][] mVisibleVillages = DataHolder.getSingleton().getVillages();

        if (mVisibleVillages == null || mBuffer == null) {
            return false;
        }

        Graphics2D g2d = (Graphics2D) mBuffer.getGraphics();
        g2d.setColor(new Color(35, 125, 0));
        g2d.fillRect(0, 0, mBuffer.getWidth(null), mBuffer.getHeight(null));
        boolean markPlayer = GlobalOptions.getProperties().getBoolean("mark.villages.on.minimap", true);
        if (ServerSettings.getSingleton().getMapDimension() == null) {
            //could not draw minimap if dimensions are not loaded yet
            return false;
        }
        boolean showBarbarian = GlobalOptions.getProperties().getBoolean("show.barbarian", true);

        Color DEFAULT = Constants.DS_DEFAULT_MARKER;
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

        double wField = ServerSettings.getSingleton().getMapDimension().getWidth() / (double) visiblePart.width;
        double hField = ServerSettings.getSingleton().getMapDimension().getHeight() / (double) visiblePart.height;
        
        UserProfile profile = GlobalOptions.getSelectedProfile();
        Tribe currentTribe = InvalidTribe.getSingleton();
        if(profile != null){
            currentTribe = profile.getTribe();
        }

        for (int i = visiblePart.x; i < (visiblePart.width + visiblePart.x); i += 3) {
            for (int j = visiblePart.y; j < (visiblePart.height + visiblePart.y); j += 3) {
                Village v = mVisibleVillages[i][j];
                if (v != null) {
                    Color markerColor = null;
                    boolean isLeft = false;
                    if (v.getTribe() == Barbarians.getSingleton()) {
                        isLeft = true;
                    } else {
                        if ((currentTribe != null) && (v.getTribe().getId() == currentTribe.getId())) {
                            //village is owned by current player. mark it dependent on settings
                            if (markPlayer) {
                                markerColor = Color.YELLOW;
                            }
                        } else {
                            try {
                                Marker marker = MarkerManager.getSingleton().getMarker(v.getTribe());
                                if (marker != null && !marker.isShownOnMap()) {
                                    marker = null;
                                    markerColor = DEFAULT;
                                }

                                if (marker == null) {
                                    marker = MarkerManager.getSingleton().getMarker(v.getTribe().getAlly());
                                    if (marker != null && marker.isShownOnMap()) {
                                        markerColor = marker.getMarkerColor();
                                    } else {
                                        markerColor = DEFAULT;
                                    }
                                } else {
                                    if (!marker.isShownOnMap()) {
                                        markerColor = DEFAULT;
                                    } else {
                                        markerColor = marker.getMarkerColor();
                                    }
                                }
                            } catch (Exception e) {
                                markerColor = null;
                            }
                        }
                    }

                    if (!isLeft) {
                        if (markerColor != null) {
                            g2d.setColor(markerColor);
                        } else {
                            g2d.setColor(DEFAULT);
                        }
                        g2d.fillRect((int) Math.round((i - visiblePart.x) * wField), (int) Math.round((j - visiblePart.y) * hField), 3, 3);
                    } else {
                        if (showBarbarian) {
                            g2d.setColor(Color.LIGHT_GRAY);
                            g2d.fillRect((int) Math.round((i - visiblePart.x) * wField), (int) Math.round((j - visiblePart.y) * hField), 3, 3);
                        }
                    }
                }
            }
        }

        try {
            if (GlobalOptions.getProperties().getBoolean("map.showcontinents", true)) {
                g2d.setColor(Color.BLACK);
                Composite c = g2d.getComposite();
                Composite a = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
                Font f = g2d.getFont();
                Font t = new Font("Serif", Font.BOLD, (int) Math.round(30 * hField));
                int coordType = ServerSettings.getSingleton().getCoordType();
                if (coordType != 2) {
                    t = new Font("Serif", Font.BOLD, 20);
                }
                g2d.setFont(t);
                int fact = 10;
                int mid = 50;
                if (coordType != 2) {
                    fact = 5;
                    mid = 25;
                }

                mid = (int) Math.round(mid * wField);

                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        g2d.setComposite(a);

                        String conti = "K" + (j * 10 + i);
                        Rectangle2D bounds = g2d.getFontMetrics(t).getStringBounds(conti, g2d);
                        int cx = i * fact * 10 - visiblePart.x;
                        int cy = j * fact * 10 - visiblePart.y;
                        cx = (int) Math.round(cx * wField);
                        cy = (int) Math.round(cy * hField);
                        g2d.drawString(conti, (int) Math.rint(cx + mid - bounds.getWidth() / 2), (int) Math.rint(cy + mid + bounds.getHeight() / 2));
                        g2d.setComposite(c);
                        int wk = 100;
                        int hk = 100;

                        if (coordType != 2) {
                            wk = 50;
                            hk = 50;
                        }
                        if (i == 9) {
                            wk -= 1;
                        }
                        if (j == 9) {
                            hk -= 1;
                        }

                        g2d.drawRect(cx, cy, (int) Math.round(wk * wField), (int) Math.round(hk * hField));
                    }
                }
                g2d.setFont(f);
            }
        } catch (Exception e) {
            logger.error("Creation of Minimap failed", e);
        }
        g2d.dispose();
        return true;
    }
}
