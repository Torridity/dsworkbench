/*
 * MapPanel.java
 *
 * Created on 4. September 2007, 18:05
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.GlobalOptions;
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
import de.tor.tribes.ui.renderer.MenuRenderer;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.map.FormManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

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
    private Rectangle2D mVirtualBounds = null;
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
    private boolean positionUpdate = false;
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
        mVirtualBounds = new Rectangle(0, 0, 0, 0);
        /*
        lf = new DSWorkbenchRectangle();
        lf.setBounds(new Point2D.Double(40, 40), new Point2D.Double(200, 80));
        // Add all figures to a drawing
        drawing = new DSWorkbenchDefaultDrawing();
        drawing.add(lf);

        view = new DSWorkbenchDrawingView();
        view.setDrawing(drawing);
        //view.setBounds(getBounds());
        view.setBackground(null);
        editor = new DefaultDrawingEditor();
        editor.add(view);
        add(view);
        // Activate the following line to see the SelectionTool in full
        // action.
        //drawing.setCanvasSize(new Dimension2DDouble(1000 * 56, 1000 * 38));
        t = new DSWorkbenchCreationTool(new DSWorkbenchRectangle());//new SelectionTool();
        editor.setTool(t);*/
        initListeners();
    }
    /* DSWorkbenchRectangle lf;
    DSWorkbenchDrawingView view;
    DrawingEditor editor;
    DSWorkbenchCreationTool t;
    public Drawing drawing;*/

    /* public DSWorkbenchDrawingView getDrawView() {
    return view;
    }

    public DrawingEditor getEditor() {
    return editor;
    }

    public Tool getTool() {
    return t;
    }*/
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
                    if (e.getButton() == MouseEvent.BUTTON2) {
                        MenuRenderer.getSingleton().setMenuLocation(e.getX(), e.getY());
                        MenuRenderer.getSingleton().switchVisibility();
                    }
                    return;
                }

                if (MenuRenderer.getSingleton().isVisible()) {
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

                if (MenuRenderer.getSingleton().isVisible()) {
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
                    case ImageManager.CURSOR_DRAW_LINE: {
                        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(mVirtualBounds.getX() + (double) e.getX() * z);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(mVirtualBounds.getY() + (double) e.getY() * z);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_RECT: {
                        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(mVirtualBounds.getX() + (double) e.getX() * z);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(mVirtualBounds.getY() + (double) e.getY() * z);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_CIRCLE: {
                        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(mVirtualBounds.getX() + (double) e.getX() * z);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(mVirtualBounds.getY() + (double) e.getY() * z);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_TEXT: {
                        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(mVirtualBounds.getX() + (double) e.getX() * z);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(mVirtualBounds.getY() + (double) e.getY() * z);
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
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                if (MenuRenderer.getSingleton().isVisible()) {
                    return;
                }
                int unit = -1;
                xDir = 0;
                yDir = 0;
                boolean isAttack = false;
                if ((iCurrentCursor == ImageManager.CURSOR_DRAW_LINE) ||
                        (iCurrentCursor == ImageManager.CURSOR_DRAW_RECT) ||
                        (iCurrentCursor == ImageManager.CURSOR_DRAW_CIRCLE) ||
                        (iCurrentCursor == ImageManager.CURSOR_DRAW_TEXT)) {
                    FormManager.getSingleton().addForm(FormConfigFrame.getSingleton().getCurrentForm());
                    FormConfigFrame.getSingleton().setupAndShow(FormConfigFrame.getSingleton().getCurrentForm().getClass());
                } else {
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

        addMouseListener(MenuRenderer.getSingleton());
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" MouseMotionListener for dragging operations ">
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (MenuRenderer.getSingleton().isVisible()) {
                    return;
                }
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
                            mMapRenderer.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                        mTargetVillage = getVillageAtMousePos();

                        fireDistanceEvents(mSourceVillage, mTargetVillage);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_LINE: {
                        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        ((de.tor.tribes.types.Line) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(mVirtualBounds.getX() + (double) (e.getX()) * z);
                        ((de.tor.tribes.types.Line) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(mVirtualBounds.getY() + (double) (e.getY()) * z);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_RECT: {
                        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        ((de.tor.tribes.types.Rectangle) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(mVirtualBounds.getX() + (double) (e.getX()) * z);
                        ((de.tor.tribes.types.Rectangle) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(mVirtualBounds.getY() + (double) (e.getY()) * z);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_CIRCLE: {
                        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        ((de.tor.tribes.types.Circle) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(mVirtualBounds.getX() + (double) (e.getX()) * z);
                        ((de.tor.tribes.types.Circle) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(mVirtualBounds.getY() + (double) (e.getY()) * z);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_TEXT: {
                        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        ((de.tor.tribes.types.Text) FormConfigFrame.getSingleton().getCurrentForm()).setXPos(mVirtualBounds.getX() + (double) (e.getX()) * z);
                        ((de.tor.tribes.types.Text) FormConfigFrame.getSingleton().getCurrentForm()).setYPos(mVirtualBounds.getY() + (double) (e.getY()) * z);
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
                if (MenuRenderer.getSingleton().isVisible()) {
                    return;
                }
            }
        });

        addMouseMotionListener(MenuRenderer.getSingleton());

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
            g2d.drawImage(mBuffer, 0, 0, getWidth(), getHeight(), null);
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
        positionUpdate = true;
        getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
    }

    public void updateVirtualBounds(Point pViewStart) {
        double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        double xV = pViewStart.x * GlobalOptions.getSkin().getFieldWidth();
        double yV = pViewStart.y * GlobalOptions.getSkin().getFieldHeight();
        double wV = (int) Math.rint(getWidth() * zoom);
        double hV = (int) Math.rint(getHeight() * zoom);
        mVirtualBounds.setRect(xV, yV, wV, hV);
    //getDrawView().setBounds(getBounds());
    }

    public Point getCurrentPosition() {
        return new Point(iCenterX, iCenterY);
    }

    public Point.Double getCurrentVirtualPosition() {
        return new Point.Double(mVirtualBounds.getX(), mVirtualBounds.getY());
    }

    public Point virtualPosToSceenPos(double pXVirt, double pYVirt) {
        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        Point2D.Double error = GlobalOptions.getSkin().getError();
        //calculate real pos in current frame
        double xp = (pXVirt - mVirtualBounds.getX());
        double yp = (pYVirt - mVirtualBounds.getY());
        //correct error and add scaling
        int x = (int) Math.rint((xp - xp * error.getX()) / z);
        int y = (int) Math.rint((yp - yp * error.getY()) / z);
        return new Point(x, y);
    }

    public Point2D.Double virtualPosToSceenPosDouble(double pXVirt, double pYVirt) {
        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        Point2D.Double error = GlobalOptions.getSkin().getError();
        //calculate real pos in current frame
        double xp = (pXVirt - mVirtualBounds.getX());
        double yp = (pYVirt - mVirtualBounds.getY());
        //correct error and add scaling
        double x = (xp - xp * error.getX()) / z;
        double y = (yp - yp * error.getY()) / z;
        return new Point2D.Double(x, y);
    }

    public Rectangle2D getVirtualBounds() {
        return mVirtualBounds;
    }

    /**Get village at current mouse position, null if there is no village*/
    public Village getVillageAtMousePos() {
        if (MenuRenderer.getSingleton().isVisible()) {
            return null;
        }
        try {
            int x = (int) getMousePosition().getX();
            int y = (int) getMousePosition().getY();

            x /= GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()).getWidth(null);
            y /= GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, DSWorkbenchMainFrame.getSingleton().getZoomFactor()).getHeight(null);

            return mVisibleVillages[x][y];
        } catch (Exception e) {
            //failed getting village (probably getting mousepos failed)
        }
        /*try {
        int x = (int) getMousePosition().getX();
        int y = (int) getMousePosition().getY();

        double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        return virtualPosToVillage(iVirtualX + x / zoom, iVirtualY + y / zoom);
        } catch (Exception e) {
        //failed getting village (probably getting mousepos failed)
        }*/
        return null;
    }

    // <editor-fold defaultstate="collapsed" desc=" Virt Stuff ">

    /*
    public Village virtualPosToVillage(double pX, double pY) {
    double vWidth = getVirtualMapSize().getWidth();
    double vHeight = getVirtualMapSize().getHeight();
    Dimension mapDim = ServerSettings.getSingleton().getMapDimension();
    double realX = pX / vWidth * mapDim.getWidth();
    double realY = pY / vHeight * mapDim.getHeight();
    int xf = (int) Math.round(realX - 0.5);
    int yf = (int) Math.round(realY - 0.5);
    System.out.println(xf + "||" + yf);
    System.out.println(ServerSettings.getSingleton().getMapDimension());
    if ((xf >= ServerSettings.getSingleton().getMapDimension().getWidth()) || (yf >= ServerSettings.getSingleton().getMapDimension().getHeight())) {

    return null;
    }
    return DataHolder.getSingleton().getVillages()[xf][yf];
    }

    public Point virtualPosToVillagePos(double pX, double pY) {
    double vWidth = getVirtualMapSize().getWidth();
    double vHeight = getVirtualMapSize().getHeight();
    Dimension mapDim = ServerSettings.getSingleton().getMapDimension();
    double realX = pX / vWidth * mapDim.getWidth();
    double realY = pY / vHeight * mapDim.getHeight();
    int xf = (int) Math.round(realX - 0.5);
    int yf = (int) Math.round(realY - 0.5);
    return new Point(xf, yf);
    }

    public Dimension getVirtualMapSize() {
    Dimension dim = ServerSettings.getSingleton().getMapDimension();
    return new Dimension((int) dim.getWidth() * GlobalOptions.getSkin().getFieldWidth(), (int) dim.getHeight() * GlobalOptions.getSkin().getFieldHeight());
    }
     */
// </editor-fold>
    /**Update operation perfomed by the RepaintThread was completed*/
    public void updateComplete(Village[][] pVillages, Image pBuffer) {
        mBuffer = pBuffer;
        mVisibleVillages = pVillages;
        if (positionUpdate) {
            DSWorkbenchFormFrame.getSingleton().updateFormList();
        }
        positionUpdate = false;
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

    public static void main(String[] args) {
        int vx = 28000;//400,400
        int vy = 19000;
        double sx = 53;
        double sy = 38;
        //mouse pos
        int mx = 100;
        int my = 100;
        double z = 1.0;

        double pX = vx + mx / z;
        double pY = vy + my / z;
        double vWidth = 1000 * 53;
        double vHeight = 1000 * 38;
        Dimension mapDim = new Dimension(1000, 1000);
        double realX = pX / vWidth * mapDim.getWidth();
        double realY = pY / vHeight * mapDim.getHeight();
        int xf = (int) Math.round(realX - 0.5);
        int yf = (int) Math.round(realY - 0.5);
        System.out.println("POs is " + xf + "," + yf);
    }
}
