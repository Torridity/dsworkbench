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
import de.tor.tribes.types.Tag;
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
import de.tor.tribes.util.tag.TagManager;

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
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
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
                    case ImageManager.CURSOR_DEFAULT: {
                        //center village on click with default cursor
                        Village current = getVillageAtMousePos();
                        if (current != null) {
                            DSWorkbenchMainFrame.getSingleton().centerVillage(current);
                        }
                        break;
                    }
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
                        VillageTagFrame.getSingleton().setLocation(e.getPoint());
                        VillageTagFrame.getSingleton().showTagsFrame(current);
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
                    //create attack on double clicking a village
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

            x /= GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()).getWidth(null);
            y /= GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()).getHeight(null);

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
        setDaemon(true);
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
                    Image iBuffer = MapPanel.getSingleton().createImage(mBuffer.getWidth(), mBuffer.getHeight());
                    Graphics2D g2d = (Graphics2D) iBuffer.getGraphics();
                    g2d.drawImage(mBuffer, null, 0, 0);
                    g2d.dispose();
                    mBuffer = null;
                    MapPanel.getSingleton().updateComplete(mVisibleVillages, iBuffer);
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

        if (DataHolder.getSingleton().getVillages() == null) {
            //probably reloading data
            return;
        }
        //get number of drawn villages
        iVillagesX = (int) Math.rint((double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()).getWidth(null));
        iVillagesY = (int) Math.rint((double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()).getHeight(null));

        //make number odd to avoid spaces at the borders
        if (iVillagesX % 2 == 0) {
            iVillagesX++;
        }
        if (iVillagesY % 2 == 0) {
            iVillagesY++;
        }
        //calculate village coordinates of the upper left corner
        int xStart = (int) Math.rint((double) pX - (double) iVillagesX / 2.0);
        int yStart = (int) Math.rint((double) pY - (double) iVillagesY / 2.0);
        xStart = (xStart < 0) ? 0 : xStart;
        yStart = (yStart < 0) ? 0 : yStart;

        //calculate village coordinates of the lower right corner
        int xEnd = (int) Math.rint((double) iX + (double) iVillagesX / 2);
        int yEnd = (int) Math.rint((double) iY + (double) iVillagesY / 2);

        xEnd = (xEnd > 999) ? 999 : xEnd;
        yEnd = (yEnd > 999) ? 999 : yEnd;

        //add some villages to have a small drawing buffer in all directions
        iVillagesX += 1;
        iVillagesY += 1;
        mVisibleVillages = new Village[iVillagesX][iVillagesY];

        int x = 0;
        int y = 0;

        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                mVisibleVillages[x][y] = DataHolder.getSingleton().getVillages()[i][j];
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
        int width = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()).getWidth(null);
        int height = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()).getHeight(null);

        //  if ((mBuffer == null) || ((mBuffer.getWidth(null) * mBuffer.getHeight(null)) != (MapPanel.getSingleton().getWidth() * MapPanel.getSingleton().getHeight()))) {
        int wb = MapPanel.getSingleton().getWidth();
        int hb = MapPanel.getSingleton().getHeight();
        if (wb == 0 || hb == 0) {
            //both are 0 if map was not drawn yet
            return;
        }
        mBuffer = new BufferedImage(wb, hb, BufferedImage.TYPE_INT_RGB);
        g2d = (Graphics2D) mBuffer.getGraphics();
        g2d.fillRect(0, 0, mBuffer.getWidth(null), mBuffer.getHeight(null));
        /*    } else {
        g2d = (Graphics2D) mBuffer.getGraphics();
        g2d.fillRect(0, 0, mBuffer.getWidth(null), mBuffer.getHeight(null));
        }*/
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
        // g2d.fillRect(0, 0, width, height);
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

        boolean markTroopTypes = false;
        try {
            markTroopTypes = Boolean.parseBoolean(GlobalOptions.getProperty("paint.troops.type"));
        } catch (Exception e) {
            markTroopTypes = false;
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

        List<Integer> xSectors = new LinkedList<Integer>();
        List<Integer> ySectors = new LinkedList<Integer>();
        List<Integer> xContinents = new LinkedList<Integer>();
        List<Integer> yContinents = new LinkedList<Integer>();
        //       Hashtable<Village, Point> tagIconPoints = new Hashtable<Village, Point>();
        Hashtable<Point, Image> troopMarkPoints = new Hashtable<Point, Image>();
        for (int i = 0; i < iVillagesX; i++) {
            for (int j = 0; j < iVillagesY; j++) {
                Village v = mVisibleVillages[i][j];
                boolean drawVillage = true;

                // <editor-fold defaultstate="collapsed" desc="Marker settings">

                Color DEFAULT = Color.WHITE;
                try {
                    if (Integer.parseInt(GlobalOptions.getProperty("default.mark")) == 1) {
                        DEFAULT = Color.RED;
                    }
                } catch (Exception e) {
                    DEFAULT = Color.WHITE;
                }
                Color marker = DEFAULT;
                g2d.setColor(marker);
                if (v != null) {
                    if (v.getTribe() != null) {
                        if (v.getTribe().getName().equals(GlobalOptions.getProperty("player." + GlobalOptions.getProperty("default.server")))) {
                            marker = Color.YELLOW;

                            // <editor-fold defaultstate="collapsed" desc=" Tag filtering ">
                            //user villages are not drawn by default but with accepted tags
                            drawVillage = false;

                            List<Tag> villageTags = TagManager.getSingleton().getTags(v);
                            if ((villageTags == null) || (villageTags.size() == 0)) {
                                //if no tag found draw village
                                drawVillage = true;
                            } else {
                                for (Tag tag : TagManager.getSingleton().getTags(v)) {
                                    if (tag.isShowOnMap()) {
                                        //at least one of the tags for the village are visible
                                        drawVillage = true;
                                        break;
                                    }
                                }
                            }
                        //</editor-fold>
                        } else {
                            try {
                                Marker m = MarkerManager.getSingleton().getMarker(v.getTribe());
                                if (m == null) {
                                    m = MarkerManager.getSingleton().getMarker(v.getTribe().getAlly());
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
                                marker = DEFAULT;
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
                    Image underground = null;
                    if (useDecoration) {
                        underground = WorldDecorationHolder.getTexture(xPos, yPos, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                    }
                    if (underground == null) {
                        //either no decoration used or map part is outside map bounds
                        underground = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                    }
                    g2d.drawImage(underground, x, y, null);

                } else {
                    if ((marker != null) && (drawVillage)) {
                        boolean isLeft = false;
                        if (v.getTribe() == null) {
                            isLeft = true;
                        }

                        if (v.getPoints() < 300) {
                            if (!isLeft) {
                                //changed
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V1, DSWorkbenchMainFrame.getSingleton().getZoomFactor());

                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B1, DSWorkbenchMainFrame.getSingleton().getZoomFactor());

                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V1_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B1_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                }
                            }
                        } else if (v.getPoints() < 1000) {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V2, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B2, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V2_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B2_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                }
                            }
                        } else if (v.getPoints() < 3000) {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V3, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B3, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V3_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B3_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                }
                            }
                        } else if (v.getPoints() < 9000) {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V4, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B4, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V4_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B4_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                }
                            }
                        } else if (v.getPoints() < 11000) {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V5, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B5, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V5_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B5_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                }
                            }
                        } else {
                            if (!isLeft) {
                                Image img = GlobalOptions.getSkin().getImage(Skin.ID_V6, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                if (v.getType() != 0) {
                                    img = GlobalOptions.getSkin().getImage(Skin.ID_B6, DSWorkbenchMainFrame.getSingleton().getZoomFactor());
                                }
                                g2d.setColor(marker);
                                g2d.fillRect(x, y, img.getWidth(null), img.getHeight(null));
                                g2d.drawImage(img, x, y, null);
                            } else {
                                if (v.getType() == 0) {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_V6_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                } else {
                                    g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_B6_LEFT, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                                }
                            }
                        }

                        /*                        if ((TagManager.getSingleton().getTags(v) != null) &&
                        (!TagManager.getSingleton().getTags(v).isEmpty())) {
                        int xc = x + (int) Math.round(width / 2);
                        int yc = y + (int) Math.round(height / 2);
                        tagIconPoints.put(v, new Point(xc - 10, yc - 10));
                        }
                         */

                        if (markTroopTypes) {
                            Image troopMark = TroopsManager.getSingleton().getTroopsMarkerForVillage(v);
                            if (troopMark != null) {
                                Point center = new Point(x + (int) Math.round(width / 2), y + (int) Math.round(height / 2));
                                troopMarkPoints.put(center, troopMark);
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
                        g2d.drawImage(GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()), x, y, null);
                        mVisibleVillages[i][j] = null;
                    }
                }
                //</editor-fold>

                y += height;
                yPos++;

                if ((showSectors) && (yPos % 5 == 0)) {
                    int pos = (yPos - pYStart) * height;
                    ySectors.add(pos);
                }
                if ((showContinents) && (yPos % 100 == 0)) {
                    int pos = (yPos - pYStart) * height;
                    yContinents.add(pos);
                }
            }
            y = 0;
            x += width;
            yPos = pYStart;
            xPos++;
            if ((showSectors) && (xPos % 5 == 0)) {
                int pos = (xPos - pXStart) * width;
                xSectors.add(pos);
            }
            if ((showContinents) && (xPos % 100 == 0)) {
                int pos = (xPos - pXStart) * width;
                xContinents.add(pos);
            }
        }

        // showVillageInfo(g2d);

        // <editor-fold defaultstate="collapsed" desc=" Tag Icon drawing (NOT USED) ">

        /*        Composite old = g2d.getComposite();
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        if (!tagIconPoints.isEmpty()) {
        Enumeration<Village> villages = tagIconPoints.keys();
        
        while (villages.hasMoreElements()) {
        Village current = villages.nextElement();
        if (!mAnimators.containsKey(current)) {
        //don't draw icon if animation is running
        List<String> tags = TagManager.getSingleton().getTags(current);
        //for (String tag : tags) {
        //show only one tag
        Image tagImage = null;
        for (String t : TagManager.getSingleton().getTags(current)) {
        tagImage = TagManager.getSingleton().getUserTagIcon(t);
        if (tagImage != null) {
        break;
        }
        }
        if (tagImage != null) {
        g2d.drawImage(tagImage, tagIconPoints.get(current).x, tagIconPoints.get(current).y, null);
        
        }
        }
        }
        }
        
        try {
        Village v = MapPanel.getSingleton().getVillageAtMousePos();
        if ((v != null) && (tagIconPoints.get(v) != null)) {
        if (!mAnimators.containsKey(v)) {
        mAnimators.put(v, new TagAnimator(v, tagIconPoints.get(v).x, tagIconPoints.get(v).y));
        }
        }
        } catch (Exception e) {
        }
        
        updateTagMovement(g2d);
        
        g2d.setComposite(old);
        
         */
        //</editor-fold>

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
            Attack[] attacks = AttackManager.getSingleton().getAttackPlan(plan).toArray(new Attack[]{});
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

        if (showSectors) {
            g2d.setColor(Color.BLACK);
            for (Integer xs : xSectors) {
                g2d.drawLine(xs, 0, xs, hb);
            }
            for (Integer ys : ySectors) {
                g2d.drawLine(0, ys, wb, ys);
            }
        }
        if (showContinents) {
            g2d.setColor(Color.YELLOW);
            for (Integer xs : xContinents) {
                g2d.drawLine(xs, 0, xs, hb);
            }
            for (Integer ys : yContinents) {
                g2d.drawLine(0, ys, wb, ys);
            }
        }
        //"paint.tag.icons"

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

        Enumeration<Point> troopMarkKeys = troopMarkPoints.keys();
        while (troopMarkKeys.hasMoreElements()) {
            Point next = troopMarkKeys.nextElement();
            Image mark = troopMarkPoints.get(next);
            mark = mark.getScaledInstance((int) Math.rint(mark.getWidth(null) / DSWorkbenchMainFrame.getSingleton().getZoomFactor()), (int) Math.rint(mark.getHeight(null) / DSWorkbenchMainFrame.getSingleton().getZoomFactor()), width);
            g2d.drawImage(mark, next.x - mark.getWidth(null) / 2, next.y - mark.getHeight(null), null);
        }


        // <editor-fold defaultstate="collapsed" desc=" Troop movement ">

        boolean showTroopInfo = false;
        try {
            showTroopInfo = Boolean.parseBoolean(GlobalOptions.getProperty("show.troop.info"));
        } catch (Exception e) {
            showTroopInfo = false;
        }
        if (showTroopInfo) {
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
                    if (pos != null) {
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
            }
        }
        // </editor-fold>

        g2d.dispose();
    }
    /*
    private void showVillageInfo(Graphics g2d) {
    Village v = MapPanel.getSingleton().getVillageAtMousePos();
    if (v == null) {
    return;
    }
    String aInfo = "-kein Stamm-";
    String tInfo = "Barbaren";
    String vInfo = v.getHTMLInfo();
    Tribe t = v.getTribe();
    if (t != null) {
    tInfo = v.getTribe().getHTMLInfo();
    Ally a = t.getAlly();
    if (a != null) {
    aInfo = v.getTribe().getAlly().getHTMLInfo();
    }
    }
    
    int wV = SwingUtilities.computeStringWidth(g2d.getFontMetrics(), vInfo);
    int wT = SwingUtilities.computeStringWidth(g2d.getFontMetrics(), tInfo);
    int wA = SwingUtilities.computeStringWidth(g2d.getFontMetrics(), aInfo);
    int width = (wV > wT) ? ((wV > wA) ? wV : wA) : ((wT > wA) ? wT : wA);
    int height = g2d.getFontMetrics().getHeight();
    g2d.setColor(Constants.DS_BACK_LIGHT);
    try {
    Point p = MouseInfo.getPointerInfo().getLocation();
    SwingUtilities.convertPointFromScreen(p, MapPanel.getSingleton());
    g2d.fillRect(p.x, p.y, width + 10, 3 * height + 6 + 10);
    
    } catch (Exception e) {
    //point outside
    }
    }
    
    private Hashtable<Village, TagAnimator> mAnimators = new Hashtable<Village, TagAnimator>();
    
    private void updateTagMovement(Graphics2D pG2d) {
    Village current = MapPanel.getSingleton().getVillageAtMousePos();
    Enumeration<Village> keys = mAnimators.keys();
    while (keys.hasMoreElements()) {
    //for (TagAnimator t : mAnimators) {
    TagAnimator t = mAnimators.get(keys.nextElement());
    if ((current == null) || (!t.getVillage().equals(current))) {
    t.setRise(false);
    } else {
    if (t.getVillage().equals(current)) {
    t.setRise(true);
    }
    }
    t.update(pG2d);
    }
    
    //cleanup
    keys = mAnimators.keys();
    while (keys.hasMoreElements()) {
    current = keys.nextElement();
    if (mAnimators.get(current).isFinished()) {
    mAnimators.remove(current);
    }
    }*/
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
