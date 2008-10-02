/*
 * MapPanel.java
 *
 * Created on 4. September 2007, 18:05
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.ToolChangeListener;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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

/**
 *
 * @author  Charon
 */
public class MapPanel extends javax.swing.JPanel {

    private static Logger logger = Logger.getLogger(MapPanel.class);
    private Village[][] mVisibleVillages = null;
    private Image mBuffer = null;
    private int mX = 500;
    private int mY = 500;
    private RepaintThread mRepaintThread = null;
    boolean updating = false;
    private int iCurrentCursor = ImageManager.CURSOR_DEFAULT;
    private Village mSourceVillage = null;
    private Village mTargetVillage = null;
    private MarkerAddFrame mMarkerAddFrame = null;
    private VillageTagFrame mTagFrame = null;
    boolean mouseDown = false;
    private boolean isOutside = false;
    private Rectangle2D screenRect = null;
    private Point mousePos = null;
    private static MapPanel SINGLETON = null;
    private List<MapPanelListener> mMapPanelListeners = null;
    private List<ToolChangeListener> mToolChangeListeners = null;
    private int xDir = 0;
    private int yDir = 0;

    public static synchronized MapPanel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MapPanel();
        }
        return SINGLETON;
    }

    /** Creates new form MapPanel */
    MapPanel() {
        initComponents();
        logger.info("Creating MapPanel");
        mMapPanelListeners = new LinkedList<MapPanelListener>();
        mToolChangeListeners = new LinkedList<ToolChangeListener>();
        mMarkerAddFrame = new MarkerAddFrame();
        mTagFrame = new VillageTagFrame();
        setCursor(ImageManager.getCursor(iCurrentCursor));
        initListeners();
    }

    public synchronized void addMapPanelListener(MapPanelListener pListener) {
        mMapPanelListeners.add(pListener);
    }

    public synchronized void removeMapPanelListener(MapPanelListener pListener) {
        mMapPanelListeners.remove(pListener);
    }

    public synchronized void addToolChangeListener(ToolChangeListener pListener) {
        mToolChangeListeners.add(pListener);
    }

    public synchronized void removeMapPanelListener(ToolChangeListener pListener) {
        mMapPanelListeners.remove(pListener);
    }

    private void initListeners() {

        // <editor-fold defaultstate="collapsed" desc="MouseWheelListener for Tool changes">
        addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

                iCurrentCursor += e.getWheelRotation();
                if (iCurrentCursor < 0) {
                    iCurrentCursor = ImageManager.CURSOR_ATTACK_HEAVY;
                } else if (iCurrentCursor > ImageManager.CURSOR_ATTACK_HEAVY) {
                    iCurrentCursor = ImageManager.CURSOR_DEFAULT;

                }
                setCursor(ImageManager.getCursor(iCurrentCursor));
                fireToolChangedEvents(iCurrentCursor);
            }
        });
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="MouseListener for cursor events">
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int unit = -1;
                boolean isAttack = false;
                if ((iCurrentCursor == ImageManager.CURSOR_ATTACK_AXE) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SWORD) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SPY) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_LIGHT) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_HEAVY) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_RAM) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SNOB)) {
                    isAttack = true;
                }
                switch (iCurrentCursor) {
                    case ImageManager.CURSOR_MARK: {
                        Village current = getVillageAtMousePos();
                        if (current != null) {
                            if (current.getTribe() == null) {
                                //empty village
                                return;
                            }
                            mMarkerAddFrame.setLocation(e.getPoint());
                            mMarkerAddFrame.setVillage(current);
                            mMarkerAddFrame.setVisible(true);
                        }
                        break;
                    }
                    case ImageManager.CURSOR_TAG: {
                        Village current = getVillageAtMousePos();
                        if (current != null) {
                            if (current.getTribe() == null) {
                                //empty village
                                return;
                            }
                        }
                        mTagFrame.setLocation(e.getPoint());
                        mTagFrame.showTagsFrame(current);
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_INGAME: {
                        if (e.getClickCount() == 2) {
                            Village v = getVillageAtMousePos();
                            Village u = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                            if ((u != null) && (v != null)) {
                                if (Desktop.isDesktopSupported()) {
                                    BrowserCommandSender.sendTroops(u, v);
                                }
                            }
                        }
                    }
                    case ImageManager.CURSOR_SEND_RES_INGAME: {
                        if (e.getClickCount() == 2) {
                            Village v = getVillageAtMousePos();
                            Village u = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                            if ((u != null) && (v != null)) {
                                if (Desktop.isDesktopSupported()) {
                                    BrowserCommandSender.sendRes(u, v);
                                }
                            }
                        }
                    }
                    case ImageManager.CURSOR_ATTACK_AXE: {
                        unit = DataHolder.getSingleton().getUnitID("Axtkämpfer");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_SWORD: {
                        unit = DataHolder.getSingleton().getUnitID("Schwertkämpfer");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_SPY: {
                        unit = DataHolder.getSingleton().getUnitID("Späher");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_LIGHT: {
                        unit = DataHolder.getSingleton().getUnitID("Leichte Kavallerie");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_HEAVY: {
                        unit = DataHolder.getSingleton().getUnitID("Schwere Kavallerie");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_RAM: {
                        unit = DataHolder.getSingleton().getUnitID("Ramme");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_SNOB: {
                        unit = DataHolder.getSingleton().getUnitID("Adelsgeschlecht");
                        break;
                    }
                }

                if (e.getClickCount() == 2) {
                    if (isAttack) {
                        AttackAddFrame aAdd = new AttackAddFrame();
                        aAdd.setLocation(e.getLocationOnScreen());
                        aAdd.setupAttack(DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage(), getVillageAtMousePos(), unit);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                boolean isAttack = false;
                mouseDown = true;
                if ((iCurrentCursor == ImageManager.CURSOR_ATTACK_AXE) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SWORD) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SPY) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_LIGHT) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_HEAVY) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_RAM) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SNOB)) {
                    isAttack = true;
                }

                switch (iCurrentCursor) {
                    case ImageManager.CURSOR_MEASURE: {
                        //start drag if attack tool is active
                        mSourceVillage = getVillageAtMousePos();
                        if (mSourceVillage != null) {
                            mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                        break;
                    }
                    default: {
                        if (isAttack) {
                            mSourceVillage = getVillageAtMousePos();
                            if (mSourceVillage != null) {
                                mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int unit = -1;
                xDir = 0;
                yDir = 0;
                boolean isAttack = false;
                if ((iCurrentCursor == ImageManager.CURSOR_ATTACK_AXE) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SWORD) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SPY) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_LIGHT) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_HEAVY) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_RAM) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SNOB)) {
                    isAttack = true;
                }

                switch (iCurrentCursor) {
                    case ImageManager.CURSOR_MEASURE: {
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_AXE: {
                        unit = DataHolder.getSingleton().getUnitID("Axtkämpfer");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_SWORD: {
                        unit = DataHolder.getSingleton().getUnitID("Schwertkämpfer");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_SPY: {
                        unit = DataHolder.getSingleton().getUnitID("Späher");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_LIGHT: {
                        unit = DataHolder.getSingleton().getUnitID("Leichte Kavallerie");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_HEAVY: {
                        unit = DataHolder.getSingleton().getUnitID("Schwere Kavallerie");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_RAM: {
                        unit = DataHolder.getSingleton().getUnitID("Ramme");
                        break;
                    }
                    case ImageManager.CURSOR_ATTACK_SNOB: {
                        unit = DataHolder.getSingleton().getUnitID("Adelsgeschlecht");
                        break;
                    }
                }

                mouseDown = false;
                if (isAttack) {
                    AttackAddFrame aAdd = new AttackAddFrame();
                    aAdd.setLocation(e.getLocationOnScreen());
                    aAdd.setupAttack(mSourceVillage, mTargetVillage, unit);
                }
                mSourceVillage = null;
                mTargetVillage = null;
                mRepaintThread.setDragLine(0, 0, 0, 0);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isOutside = false;
                screenRect = null;
                mousePos = null;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (mouseDown) {
                    isOutside = true;
                    //handle drag-outside-panel events
                    mousePos = e.getLocationOnScreen();
                    Point panelPos = getLocationOnScreen();
                    screenRect = new Rectangle2D.Double(panelPos.getX(), panelPos.getY(), getWidth(), getHeight());
                }
            }
        });
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="MouseMotionListener for dragging operations">
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                fireVillageAtMousePosChangedEvents(getVillageAtMousePos());
                boolean isAttack = false;
                if ((iCurrentCursor == ImageManager.CURSOR_ATTACK_AXE) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SWORD) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SPY) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_LIGHT) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_HEAVY) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_RAM) ||
                        (iCurrentCursor == ImageManager.CURSOR_ATTACK_SNOB)) {
                    isAttack = true;
                }

                switch (iCurrentCursor) {
                    case ImageManager.CURSOR_MEASURE: {
                        //update drag if attack tool is active
                        if (mSourceVillage != null) {
                            mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                        mTargetVillage = getVillageAtMousePos();

                        fireDistanceEvents(mSourceVillage, mTargetVillage);
                        break;
                    }
                    default: {
                        if (isAttack) {
                            if (mSourceVillage != null) {
                                mRepaintThread.setDragLine((int) mSourceVillage.getX(), (int) mSourceVillage.getY(), e.getX(), e.getY());
                                mTargetVillage = getVillageAtMousePos();
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                fireVillageAtMousePosChangedEvents(getVillageAtMousePos());
                if (isOutside) {
                    mousePos = e.getLocationOnScreen();
                }
            }
        });
    //</editor-fold>
    }

    protected boolean isOutside() {
        return isOutside;
    }

    public Village getSourceVillage() {
        return mSourceVillage;
    }

    public void setCurrentCursor(int pCurrentCursor) {
        iCurrentCursor = pCurrentCursor;
        setCursor(ImageManager.getCursor(iCurrentCursor));
        fireToolChangedEvents(iCurrentCursor);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 207, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 89, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**Draw buffer into panel*/
    @Override
    public void paint(Graphics g) {
        try {
            g.fillRect(0, 0, getWidth(), getHeight());
            if (isOutside) {
                mousePos = MouseInfo.getPointerInfo().getLocation();
                int outcodes = screenRect.outcode(mousePos);

                if ((outcodes & Rectangle2D.OUT_LEFT) != 0) {
                    xDir += -1;
                } else if ((outcodes & Rectangle2D.OUT_RIGHT) != 0) {
                    xDir += 1;
                }

                if ((outcodes & Rectangle2D.OUT_TOP) != 0) {
                    yDir += -1;
                } else if ((outcodes & Rectangle2D.OUT_BOTTOM) != 0) {
                    yDir += 1;
                }

                //lower scroll speed
                int sx = 0;
                int sy = 0;
                if (xDir >= 1) {
                    sx = 1;
                    xDir = 0;
                } else if (xDir <= -1) {
                    sx = -1;
                    xDir = 0;
                }

                if (yDir >= 1) {
                    sy = 1;
                    yDir = 0;
                } else if (yDir <= -1) {
                    sy = -1;
                    yDir = 0;
                }

                fireScrollEvents(sx, sy);
            }
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(mBuffer, 0, 0, null);
            g2d.dispose();
        } catch (Exception e) {
            logger.error("Failed to paint", e);
        }
    }

    /**Update map to new position*/
    protected synchronized void updateMap(int pX, int pY) {
        updating = true;
        if (mRepaintThread == null) {
            logger.info("Creating MapPaintThread");
            mRepaintThread = new RepaintThread(pX, pY);
            mRepaintThread.start();
            mX = pX;
            mY = pY;
        } else {
            mX = pX;
            mY = pY;
            mRepaintThread.setCoordinates(mX, mY);
        }
    }

    /**Get village at current mouse position, null if there is no village*/
    public Village getVillageAtMousePos() {
        try {
            int x = (int) getMousePosition().getX();
            int y = (int) getMousePosition().getY();

            x /= GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoom()).getWidth(null);
            y /= GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoom()).getHeight(null);

            return mVisibleVillages[x][y];
        } catch (Exception e) {
            //failed getting village (probably getting mousepos failed)
        }
        return null;
    }

    /**Update operation perfomed by the RepaintThread was completed*/
    protected void updateComplete(Village[][] pVillages, Image pBuffer) {
        mBuffer = pBuffer;
        mVisibleVillages = pVillages;
        updating = false;
    }

    public synchronized void fireToolChangedEvents(int pTool) {
        for (ToolChangeListener listener : mToolChangeListeners) {
            listener.fireToolChangedEvent(pTool);
        }
    }

    public synchronized void fireVillageAtMousePosChangedEvents(Village pVillage) {
        for (MapPanelListener listener : mMapPanelListeners) {
            listener.fireVillageAtMousePosChangedEvent(pVillage);
        }
    }

    public synchronized void fireDistanceEvents(Village pSource, Village pTarget) {
        for (MapPanelListener listener : mMapPanelListeners) {
            listener.fireDistanceEvent(pSource, pTarget);
        }
    }

    public synchronized void fireScrollEvents(int pX, int pY) {
        for (MapPanelListener listener : mMapPanelListeners) {
            listener.fireScrollEvent(pX, pY);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

/**Thread for updating after scroll operations*/
class RepaintThread extends Thread {

    private static Logger logger = Logger.getLogger(RepaintThread.class);
    private Village[][] mVisibleVillages = null;
    private BufferedImage mBuffer = null;
    private int iVillagesX = 0;
    private int iVillagesY = 0;
    private int iX = 500;
    private int iY = 500;
    private int xe = 0;
    private int ye = 0;
    private Village mSourceVillage = null;
    private BufferedImage mDistBorder = null;
    private Image mMarkerImage = null;
    private final NumberFormat nf = NumberFormat.getInstance();

    public RepaintThread(int pX, int pY) {
        mVisibleVillages = new Village[iVillagesX][iVillagesY];
        setCoordinates(pX, pY);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        try {
            mDistBorder = ImageIO.read(new File("./graphics/dist_border.png"));
            mMarkerImage = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/res/marker.png"));
        } catch (Exception e) {
            logger.error("Failed to load border images", e);
        }
    }

    protected void setCoordinates(int pX, int pY) {
        iX = pX;
        iY = pY;
    }

    @Override
    public void run() {
        while (true) {
            try {
                updateMap(iX, iY);
                if (mBuffer != null) {
                    MapPanel.getSingleton().updateComplete(mVisibleVillages, mBuffer);
                    MapPanel.getSingleton().repaint();
                }
            } catch (Throwable t) {
                logger.error("Redrawing map failed", t);
            }
            try {
                Thread.sleep(80);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void setDragLine(int pXS, int pYS, int pXE, int pYE) {
        mSourceVillage = DataHolder.getSingleton().getVillages()[pXS][pYS];
        xe = pXE;
        ye = pYE;
    }

    /**Extract the selected villages*/
    private void updateMap(int pX, int pY) {
        iX = pX;
        iY = pY;

        Village[][] villages = DataHolder.getSingleton().getVillages();

        if (villages == null) {
            //probably reloading data
            return;
        }
        iVillagesX = (int) Math.rint((double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoom()).getWidth(null));
        iVillagesY = (int) Math.rint((double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoom()).getHeight(null));

        if (iVillagesX % 2 == 0) {
            iVillagesX++;
        }
        if (iVillagesY % 2 == 0) {
            iVillagesY++;
        }
        int xStart = (int) Math.rint((double) pX - (double) iVillagesX / 2.0);
        int yStart = (int) Math.rint((double) pY - (double) iVillagesY / 2.0);
        xStart = (xStart < 0) ? 0 : xStart;
        yStart = (yStart < 0) ? 0 : yStart;
        int xEnd = (int) Math.rint((double) iX + (double) iVillagesX / 2);
        int yEnd = (int) Math.rint((double) iY + (double) iVillagesY / 2);

        xEnd = (xEnd > 999) ? 999 : xEnd;
        yEnd = (yEnd > 999) ? 999 : yEnd;

        iVillagesX += 3;
        iVillagesY += 3;
        mVisibleVillages = new Village[iVillagesX][iVillagesY];

        int x = 0;
        int y = 0;

        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                mVisibleVillages[x][y] = villages[i][j];
                y++;
            }
            x++;
            y = 0;
        }
        redraw(xStart, yStart);
    }

    /**Redraw the buffer*/
    private void redraw(int pXStart, int pYStart) {
        int x = 0;
        int y = 0;

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

        Graphics2D g2d = null;
        int width = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoom()).getWidth(null);
        int height = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoom()).getHeight(null);

        if ((mBuffer == null) || ((mBuffer.getWidth(null) * mBuffer.getHeight(null)) != (MapPanel.getSingleton().getWidth() * MapPanel.getSingleton().getHeight()))) {
            int w = MapPanel.getSingleton().getWidth();
            int h = MapPanel.getSingleton().getHeight();
            if (w == 0 || h == 0) {
                //both are 0 if map was not drawn yet
                return;
            }
            mBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            g2d = (Graphics2D) mBuffer.getGraphics();
        } else {
            g2d = (Graphics2D) mBuffer.getGraphics();
            g2d.fillRect(0, 0, mBuffer.getWidth(null), mBuffer.getHeight(null));
        }
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // Speed
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int xPos = pXStart;
        int yPos = pYStart;
        //disable decoration if field size is not equal the decoration texture size
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, 1).getWidth(null)) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, 1).getHeight(null))) {
            useDecoration = false;
        }

        Line2D.Double dragLine = new Line2D.Double(-1, -1, xe, ye);
        int markX = -1;
        int markY = -1;
        boolean markActiveVillage = false;
        Village mouseVillage = MapPanel.getSingleton().getVillageAtMousePos();
        try {
            markActiveVillage = Boolean.parseBoolean(GlobalOptions.getProperty("mark.active.village"));
        } catch (Exception e) {
            markActiveVillage = false;
        }
        boolean markedOnly = false;
        try {
            markedOnly = Boolean.parseBoolean(GlobalOptions.getProperty("draw.marked.only"));
        } catch (Exception e) {
            markedOnly = false;
        }
        for (int i = 0; i < iVillagesX; i++) {
            for (int j = 0; j < iVillagesY; j++) {
                Village v = mVisibleVillages[i][j];

                // <editor-fold defaultstate="collapsed" desc="Marker settings">
                Color marker = Color.WHITE;
                g2d.setColor(marker);
                if (v != null) {
                    if (v.getTribe() != null) {
                        if (v.getTribe().getName().equals(GlobalOptions.getProperty("player." + GlobalOptions.getProperty("default.server")))) {
                            marker = Color.YELLOW;
                        } else {
                            try {
                                Marker m = MarkerManager.getSingleton().getMarkerByValue(v.getTribe().getName());
                                if (m == null) {
                                    m = MarkerManager.getSingleton().getMarkerByValue(v.getTribe().getAlly().getName());
                                    if (m != null) {
                                        marker = m.getMarkerColor();
                                    } else {
                                        //abort this mark
                                        throw new NullPointerException("");
                                    }
                                } else {
                                    marker = m.getMarkerColor();
                                }
                            } catch (Throwable t) {
                                marker = Color.WHITE;
                                if (markedOnly) {
                                    marker = null;
                                }
                            }
                        }
                    }
                }
                //</editor-fold>

                if ((v != null) && (mSourceVillage != null)) {
                    if ((mSourceVillage.getX() == v.getX()) && (mSourceVillage.getY() == v.getY())) {
                        dragLine.setLine(x + width / 2, y + height / 2, dragLine.getX2(), dragLine.getY2());
                    }
                }

                // <editor-fold defaultstate="collapsed" desc="Village drawing">
                if (v == null) {
                    if (useDecoration) {
                        g2d.drawImage(WorldDecorationHolder.getTexture(xPos, yPos, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                    } else {
                        g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                    }

                } else {
                    if (marker != null) {
                        boolean isLeft = false;
                        if (v.getTribe() == null) {
                            isLeft = true;
                        }

                        if (v.getPoints() < 300) {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V1, DSWorkbenchMainFrame.getSingleton().getZoom());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B1, DSWorkbenchMainFrame.getSingleton().getZoom());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V1_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B1_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                }
                            }
                        } else if (v.getPoints() < 1000) {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V2, DSWorkbenchMainFrame.getSingleton().getZoom());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B2, DSWorkbenchMainFrame.getSingleton().getZoom());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V2_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B2_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                }
                            }
                        } else if (v.getPoints() < 3000) {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V3, DSWorkbenchMainFrame.getSingleton().getZoom());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B3, DSWorkbenchMainFrame.getSingleton().getZoom());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V3_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B3_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                }
                            }
                        } else if (v.getPoints() < 9000) {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V4, DSWorkbenchMainFrame.getSingleton().getZoom());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B4, DSWorkbenchMainFrame.getSingleton().getZoom());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V4_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B4_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                }
                            }
                        } else if (v.getPoints() < 11000) {
                            /* if (xC == 0) {
                            xC = x;
                            yC = y;
                            }*/
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V5, DSWorkbenchMainFrame.getSingleton().getZoom());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B5, DSWorkbenchMainFrame.getSingleton().getZoom());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V5_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B5_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                }
                            }
                        } else {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V6, DSWorkbenchMainFrame.getSingleton().getZoom());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B6, DSWorkbenchMainFrame.getSingleton().getZoom());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V6_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B6_LEFT, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                                }
                            }
                        }

                        if (markActiveVillage) {
                            Village current = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                            if (current != null) {
                                if (v.compareTo(current) == 0) {
                                    markX = x + (int) Math.round(width / 2);
                                    markY = y + (int) Math.round(height / 2);
                                }
                            }
                        }
                    } else {
                        g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoom()), x, y, null);
                    }
                }

                //</editor-fold>

                y += height;
                yPos++;
            }
            y = 0;
            x += width;
            yPos = pYStart;
            xPos++;
        }

        // <editor-fold defaultstate="collapsed" desc=" Draw Drag line">

        if (mSourceVillage != null) {
            //draw dragging line for distance and attack
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(5.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            if (dragLine.getX1() == -1) {
                int xx = pXStart + xe / width;
                int yy = pYStart + ye / height;
                int tx = xx - mSourceVillage.getX();
                int ty = yy - mSourceVillage.getY();

                tx = xe - width * tx;
                ty = ye - height * ty;
                dragLine.setLine(tx, ty, xe, ye);
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

                        Rectangle2D b = g2d.getFontMetrics().getStringBounds(dist, g2d);
                        g2d.drawImage(mDistBorder, null, x2 - 6, y2 - (int) Math.rint(b.getHeight()));
                        g2d.drawString(dist, x2, y2);
                    }
                }
            }
        }

        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Attack-line drawing">

        g2d.setStroke(new BasicStroke(2.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        Enumeration<String> keys = AttackManager.getSingleton().getPlans();

        while (keys.hasMoreElements()) {
            String plan = keys.nextElement();
            List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(plan);
            for (Attack attack : attacks) {
                //go through all attacks
                if (attack.isShowOnMap()) {
                    //only enter if attack should be visible
                    //get line for this attack
                    Line2D.Double attackLine = new Line2D.Double(attack.getSource().getX(), attack.getSource().getY(), attack.getTarget().getX(), attack.getTarget().getY());
                    Rectangle2D.Double bounds = new Rectangle2D.Double(pXStart, pYStart, iVillagesX, iVillagesY);
                    String value = GlobalOptions.getProperty("attack.movement");
                    boolean showAttackMovement = (value == null) ? false : Boolean.parseBoolean(value);
                    int xStart = ((int) attackLine.getX1() - pXStart) * width + width / 2;
                    int yStart = ((int) attackLine.getY1() - pYStart) * height + height / 2;
                    int xEnd = (int) (attackLine.getX2() - pXStart) * width + width / 2;
                    int yEnd = (int) (attackLine.getY2() - pYStart) * height + height / 2;
                    ImageIcon unitIcon = null;
                    int unitXPos = 0;
                    int unitYPos = 0;
                    if (showAttackMovement) {

                        if (attack.getUnit().getName().equals("Speerträger")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_SPEAR);
                        } else if (attack.getUnit().getName().equals("Schwertkämpfer")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_SWORD);
                        } else if (attack.getUnit().getName().equals("Axtkämpfer")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_AXE);
                        } else if (attack.getUnit().getName().equals("Bogenschütze")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_ARCHER);
                        } else if (attack.getUnit().getName().equals("Späher")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_SPY);
                        } else if (attack.getUnit().getName().equals("Leichte Kavallerie")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_LKAV);
                        } else if (attack.getUnit().getName().equals("Berittener Bogenschütze")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_MARCHER);
                        } else if (attack.getUnit().getName().equals("Schwere Kavallerie")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_HEAVY);
                        } else if (attack.getUnit().getName().equals("Ramme")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_RAM);
                        } else if (attack.getUnit().getName().equals("Katapult")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_CATA);
                        } else if (attack.getUnit().getName().equals("Adelsgeschlecht")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_SNOB);
                        } else if (attack.getUnit().getName().equals("Paladin")) {
                            unitIcon = ImageManager.getUnitIcon(ImageManager.ICON_KNIGHT);
                        }

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

                    //System.err.println("get " + attack.getUnit() + " " + attackColors.get(attack.getUnit()));
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
        }
        //</editor-fold>

        /*
        g2d.setColor(Color.GREEN);
        Composite a = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
        g2d.setComposite(a);
        g2d.fillOval(xC, yC, width, height);
         */

        //Cursor problem workaround
        /*Point pos = MapPanel.getSingleton().getMousePosition();
        if (pos != null) {
        g2d.drawImage(ImageManager.getUnitIcon(ImageManager.ICON_AXE).getImage(), pos.x, pos.y, null);
        }
         */

        //mark current player village
        if (markX >= 0 && markY >= 0) {
            g2d.drawImage(mMarkerImage, markX, markY - mMarkerImage.getHeight(null), null);
        }

        if (mouseVillage != null) {
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(mouseVillage);
            if ((holder != null) && (!holder.getTroops().isEmpty())) {
                //get half the units for the current server
                int unitCount = DataHolder.getSingleton().getUnits().size();
                FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
                // int fontHeight = metrics.getHeight();
                Point pos = MapPanel.getSingleton().getMousePosition();
                //number format without fraction digits 
                NumberFormat numFormat = NumberFormat.getInstance();
                numFormat.setMaximumFractionDigits(0);
                numFormat.setMinimumFractionDigits(0);
                //default width for unit number
                int unitWidth = metrics.stringWidth("1.234.567");
                //get largest unit value
                for (Integer i : holder.getTroops()) {
                    int w = metrics.stringWidth(numFormat.format(i));
                    if (w > unitWidth) {
                        unitWidth = w;
                    }
                }

                int textHeight = metrics.getHeight();
                int unitHeight = ImageManager.getUnitIcon(0).getImage().getHeight(null);

                g2d.setColor(Constants.DS_BACK_LIGHT);
                int popupWidth = 12 + unitWidth + unitHeight;
                int popupHeight = unitCount * unitHeight + 10 + textHeight + 2;
                g2d.fill3DRect(pos.x - popupWidth, pos.y, popupWidth, popupHeight, true);

                g2d.setColor(Color.BLACK);

                //draw state
                String state = "(" + new SimpleDateFormat("dd.MM.yyyy").format(holder.getState()) + ")";
                double dY = metrics.getStringBounds(state, g2d).getY();
                g2d.drawString(state, pos.x - popupWidth + 5, pos.y - (int) Math.rint(dY) + 5);

                double sx = textHeight / (double) ImageManager.getUnitIcon(0).getImage().getHeight(null);
                for (int i = 0; i < unitCount; i++) {
                    //draw unit with a border of 5px
                    AffineTransform xform = AffineTransform.getTranslateInstance(pos.x - popupWidth + 5, pos.y + i * unitHeight + 5 + textHeight + 2);
                    xform.scale(sx, sx);
                    g2d.drawImage(ImageManager.getUnitIcon(i).getImage(), xform, null);
                    //draw the unit count
                    dY = metrics.getStringBounds(numFormat.format(holder.getTroops().get(i)), g2d).getY();
                    g2d.drawString(numFormat.format(holder.getTroops().get(i)), pos.x - popupWidth + 5 + unitHeight + 2, pos.y + i * unitHeight - (int) Math.rint(dY) + 5 + textHeight + 2);
                }
            } else {
                Point pos = MapPanel.getSingleton().getMousePosition();
                String noInfo = "keine Informationen";
                FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
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


        g2d.dispose();
    }
}
