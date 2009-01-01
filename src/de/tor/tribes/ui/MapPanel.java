/*
 * MapPanel.java
 *
 * Created on 4. September 2007, 18:05
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.ToolChangeListener;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import de.tor.tribes.ui.renderer.MapRenderer;

/**
 *
 * @author  Charon
 */
public class MapPanel extends javax.swing.JPanel {

    // <editor-fold defaultstate="collapsed" desc=" Member variables ">
    private static Logger logger = Logger.getLogger("MapCanvas");
    private Village[][] mVisibleVillages = null;
    private Image mBuffer = null;
    private int iCenterX = 500;
    private int iCenterY = 500;
    private int iCurrentCursor = ImageManager.CURSOR_DEFAULT;
    private Village mSourceVillage = null;
    private Village mTargetVillage = null;
    private MarkerAddFrame mMarkerAddFrame = null;
    boolean mouseDown = false;
    private boolean isOutside = false;
    private Rectangle2D mapBounds = null;
    private Point mousePos = null;
    private List<MapPanelListener> mMapPanelListeners = null;
    private List<ToolChangeListener> mToolChangeListeners = null;
    private int xDir = 0;
    private int yDir = 0;
    private MapRenderer mMapRenderer = null;
    private static MapPanel SINGLETON = null;
    private AttackAddFrame attackAddFrame = null;
    // </editor-fold>

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
        setIgnoreRepaint(true);
        attackAddFrame = new AttackAddFrame();
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
                if (e.getWheelRotation() < 0) {
                    DSWorkbenchMainFrame.getSingleton().zoomOut();
                } else {
                    DSWorkbenchMainFrame.getSingleton().zoomIn();
                }
            /* iCurrentCursor += e.getWheelRotation();
            if (iCurrentCursor < 0) {
            iCurrentCursor = ImageManager.CURSOR_ATTACK_HEAVY;
            } else if (iCurrentCursor > ImageManager.CURSOR_ATTACK_HEAVY) {
            iCurrentCursor = ImageManager.CURSOR_DEFAULT;
            
            }
            setCursor(ImageManager.getCursor(iCurrentCursor));
            fireToolChangedEvents(iCurrentCursor);*/


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
                    case ImageManager.CURSOR_SUPPORT: {
                        Village current = getVillageAtMousePos();
                        if (current != null) {
                            if (current.getTribe() == null) {
                                //empty village
                                return;
                            }
                        }
                        VillageSupportFrame.getSingleton().setLocation(e.getPoint());
                        VillageSupportFrame.getSingleton().showSupportFrame(current);
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
                        attackAddFrame.setLocation(e.getLocationOnScreen());
                        attackAddFrame.setupAttack(DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage(), getVillageAtMousePos(), unit);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

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
                            //mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                            mMapRenderer.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                        break;
                    }
                    default: {
                        if (isAttack) {
                            mSourceVillage = getVillageAtMousePos();
                            if (mSourceVillage != null) {
                                // mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                                mMapRenderer.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
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
                    attackAddFrame.setLocation(e.getLocationOnScreen());
                    attackAddFrame.setupAttack(mSourceVillage, mTargetVillage, unit);
                }
                mSourceVillage = null;
                mTargetVillage = null;
                mMapRenderer.setDragLine(0, 0, 0, 0);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isOutside = false;
                mapBounds = null;
                mousePos = null;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (mouseDown) {
                    isOutside = true;
                    //handle drag-outside-panel events
                    mousePos = e.getLocationOnScreen();
                    Point panelPos = getLocationOnScreen();
                    mapBounds = new Rectangle2D.Double(panelPos.getX(), panelPos.getY(), getWidth(), getHeight());
                }
            }
        });

        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" MouseMotionListener for dragging operations ">
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
                            //mRepaintThread.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                            mMapRenderer.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                        mTargetVillage = getVillageAtMousePos();

                        fireDistanceEvents(mSourceVillage, mTargetVillage);
                        break;
                    }
                    default: {
                        if (isAttack) {
                            if (mSourceVillage != null) {
                                //mRepaintThread.setDragLine((int) mSourceVillage.getX(), (int) mSourceVillage.getY(), e.getX(), e.getY());
                                mMapRenderer.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
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
    //<editor-fold>
    }

    public MapRenderer getMapRenderer() {
        return mMapRenderer;
    }

    /**Returns true as long as the mouse is outside the mappanel*/
    protected boolean isOutside() {
        return isOutside;
    }

    /**Get start village of drag operation*/
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
    public void paintComponent(Graphics g) {
        try {
            //clean map
            g.fillRect(0, 0, getWidth(), getHeight());
            //calculate move direction if mouse is dragged outside the map
            if ((isOutside) && (mouseDown)) {
                mousePos = MouseInfo.getPointerInfo().getLocation();
                int outcodes = mapBounds.outcode(mousePos);

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
                    sx = 2;
                    xDir = 0;
                } else if (xDir <= -1) {
                    sx = -2;
                    xDir = 0;
                }

                if (yDir >= 1) {
                    sy = 2;
                    yDir = 0;
                } else if (yDir <= -1) {
                    sy = -2;
                    yDir = 0;
                }

                fireScrollEvents(sx, sy);
            }
            //draw off-screen image of map
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(mBuffer, 0, 0, null);
            g2d.dispose();
        } catch (Exception e) {
            logger.error("Failed to paint", e);
        }
    }

    /**Update map to new position -> needs fully update*/
    protected synchronized void updateMapPosition(int pX, int pY) {
        iCenterX = pX;
        iCenterY = pY;
        if (mMapRenderer == null) {
            logger.info("Creating MapRenderer");
            mMapRenderer = new MapRenderer();
            mMapRenderer.start();
        }
        getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
    }

    public Point getCurrentPosition() {
        return new Point(iCenterX, iCenterY);
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
    public void updateComplete(Village[][] pVillages, Image pBuffer) {
        mBuffer = pBuffer;
        mVisibleVillages = pVillages;
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
