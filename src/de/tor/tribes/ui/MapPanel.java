/*
 * MapPanel.java
 *
 * Created on 4. September 2007, 18:05
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
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
import de.tor.tribes.util.VillageSelectionListener;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *@TODO Add flag-marker for single villages/notes? -> notes as forms? (Version 2.0)
 * @author  Charon
 */
public class MapPanel extends javax.swing.JPanel {

    // <editor-fold defaultstate="collapsed" desc=" Member variables ">
    private static Logger logger = Logger.getLogger("MapCanvas");
    private Image mBuffer = null;
    /* private int iCenterX = 500;
    private int iCenterY = 500;*/
    private double dCenterX = 500.0;
    private double dCenterY = 500.0;
    private Rectangle2D.Double mVirtualBounds = null;
    private int iCurrentCursor = ImageManager.CURSOR_DEFAULT;
    private Village mSourceVillage = null;
    private Village mTargetVillage = null;
    private MarkerAddFrame mMarkerAddFrame = null;
    boolean mouseDown = false;
    private boolean isOutside = false;
    private Rectangle2D mapBounds = null;
    private Point mousePos = null;
    private Point mouseDownPoint = null;
    private List<MapPanelListener> mMapPanelListeners = null;
    private List<ToolChangeListener> mToolChangeListeners = null;
    private int xDir = 0;
    private int yDir = 0;
    private MapRenderer mMapRenderer = null;
    private static MapPanel SINGLETON = null;
    private AttackAddFrame attackAddFrame = null;
    private boolean positionUpdate = false;
    private de.tor.tribes.types.Rectangle selectionRect = null;
    private VillageSelectionListener mVillageSelectionListener = null;
    // </editor-fold>
    private Hashtable<Village, Rectangle> mVillagePositions = null;
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
        mVirtualBounds = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
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

    public de.tor.tribes.types.Rectangle getSelectionRect() {
        return selectionRect;
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
            }
        });
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="MouseListener for cursor events">

        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    MenuRenderer.getSingleton().setMenuLocation(e.getX(), e.getY());
                    MenuRenderer.getSingleton().switchVisibility();
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
                            Tribe t = DSWorkbenchMainFrame.getSingleton().getCurrentUser();
                            if ((current != null) && (current.getTribe() != null) && (t != null) && (t.equals(current.getTribe()))) {
                                DSWorkbenchMainFrame.getSingleton().setCurrentUserVillage(current);
                            }
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
                        break;
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
                    case ImageManager.CURSOR_DEFAULT: {
                        mouseDownPoint = MouseInfo.getPointerInfo().getLocation();
                        break;
                    }
                    case ImageManager.CURSOR_SELECTION: {
                        selectionRect = new de.tor.tribes.types.Rectangle();
                        selectionRect.setDrawColor(Color.YELLOW);
                        selectionRect.setFilled(true);
                        selectionRect.setDrawAlpha(0.2f);
                        selectionRect.setDrawName(true);
                        selectionRect.setTextColor(Color.BLUE);
                        selectionRect.setTextAlpha(0.7f);
                        selectionRect.setTextSize(24);
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        selectionRect.setXPos(pos.x);
                        selectionRect.setYPos(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_MEASURE: {
                        //start drag if attack tool is active
                        mSourceVillage = getVillageAtMousePos();
                        if (mSourceVillage != null) {
                            mMapRenderer.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_FREEFORM: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(pos.x);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_LINE: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(pos.x);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_RECT: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(pos.x);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_CIRCLE: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(pos.x);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_TEXT: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(pos.x);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(pos.y);
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
                        (iCurrentCursor == ImageManager.CURSOR_DRAW_TEXT) ||
                        (iCurrentCursor == ImageManager.CURSOR_DRAW_FREEFORM)) {
                    FormConfigFrame.getSingleton().purge();
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
                        case ImageManager.CURSOR_DEFAULT: {
                            mouseDownPoint = null;
                            break;
                        }
                        case ImageManager.CURSOR_SELECTION: {

                            int xs = (int) Math.floor(selectionRect.getXPos());
                            int ys = (int) Math.floor(selectionRect.getYPos());
                            int xe = (int) Math.floor(selectionRect.getXPosEnd());
                            int ye = (int) Math.floor(selectionRect.getYPosEnd());
                            if (mVillageSelectionListener != null) {
                                //if a selectionlistener is registered notify it
                                mVillageSelectionListener.fireSelectionFinishedEvent(new Point(xs, ys), new Point(xe, ye));
                            } else {
                                List<Village> selection = getSelectedVillages(new Point(xs, ys), new Point(xe, ye));
                                if (selection.size() > 0) {
                                    //do selection handling by ourself
                                    UIManager.put("OptionPane.cancelButtonText", "Verwerfen");
                                    UIManager.put("OptionPane.noButtonText", "Unformatiert");
                                    UIManager.put("OptionPane.yesButtonText", "BB-Code");
                                    String message = "Es wurden ";
                                    if (selection.size() == 1) {
                                        message = "Es wurde 1 Dorf ausgewählt.\n";
                                    } else {
                                        message += selection.size() + " Dörfer ausgewählt.\n";
                                    }
                                    message += "In welchem Format soll die Auswahl in die Zwischenablage\nkopiert werden?";

                                    int res = JOptionPane.showConfirmDialog(MapPanel.getSingleton(), message, "Dorfauswahl", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                                    switch (res) {
                                        case JOptionPane.NO_OPTION: {
                                            //unformatted
                                            String result = "";

                                            for (Village v : selection) {
                                                result += v.getX() + "\t" + v.getY() + "\n";
                                            }
                                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
                                            break;
                                        }
                                        case JOptionPane.YES_OPTION: {
                                            //as BB code
                                            String result = "";
                                            for (Village v : selection) {
                                                result += v.toBBCode() + "\n";
                                            }
                                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
                                            break;
                                        }
                                        default: {
                                            //cancel
                                        }
                                    }
                                    UIManager.put("OptionPane.cancelButtonText", "Cancel");
                                    UIManager.put("OptionPane.noButtonText", "No");
                                    UIManager.put("OptionPane.yesButtonText", "Yes");
                                }
                            }
                            selectionRect = null;
                            mVillageSelectionListener = null;
                            break;
                        }
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
                mMapRenderer.setDragLine(-1, -1, -1, -1);
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
                // fireVillageAtMousePosChangedEvents(getVillageAtMousePos());
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
                        if (isOutside) {
                            return;
                        }
                        Point location = MouseInfo.getPointerInfo().getLocation();
                        if ((mouseDownPoint == null) || (location == null)) {
                            break;
                        }
                        double dx = (double) location.getX() - (double) mouseDownPoint.getX();
                        double dy = (double) location.getY() - (double) mouseDownPoint.getY();
                        mouseDownPoint = location;
                        double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
                        Image i = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, zoom);
                        double w = (double) i.getWidth(null);
                        double h = (double) i.getHeight(null);
                        fireScrollEvents(-dx / w, -dy / h);
                        break;
                    }
                    case ImageManager.CURSOR_SELECTION: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        int xs = (int) Math.floor(selectionRect.getXPos());
                        int ys = (int) Math.floor(selectionRect.getYPos());
                        int xe = (int) Math.floor(selectionRect.getXPosEnd());
                        int ye = (int) Math.floor(selectionRect.getYPosEnd());

                        int cnt = countVillages(new Point(xs, ys), new Point(xe, ye));
                        String name = "";
                        if (cnt == 1) {
                            name = "1 Dorf";
                        } else {
                            name = cnt + " Dörfer";
                        }

                        selectionRect.setFormName(name);
                        selectionRect.setXPosEnd(pos.x);
                        selectionRect.setYPosEnd(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_MEASURE: {
                        //update drag if attack tool is active
                        if (mSourceVillage != null) {
                            mMapRenderer.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                        mTargetVillage = getVillageAtMousePos();

                        //fireDistanceEvents(mSourceVillage, mTargetVillage);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_LINE: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.Line) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(pos.x);
                        ((de.tor.tribes.types.Line) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_RECT: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.Rectangle) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(pos.x);
                        ((de.tor.tribes.types.Rectangle) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_CIRCLE: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.Circle) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(pos.x);
                        ((de.tor.tribes.types.Circle) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_TEXT: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.Text) FormConfigFrame.getSingleton().getCurrentForm()).setXPos(pos.x);
                        ((de.tor.tribes.types.Text) FormConfigFrame.getSingleton().getCurrentForm()).setYPos(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_FREEFORM: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.FreeForm) FormConfigFrame.getSingleton().getCurrentForm()).addPoint(pos);
                        break;
                    }
                    default: {
                        if (isAttack) {
                            if (mSourceVillage != null) {
                                mMapRenderer.setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                                mTargetVillage = getVillageAtMousePos();
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // fireVillageAtMousePosChangedEvents(getVillageAtMousePos());

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

    //return bounds without border
   /* public Rectangle getCorrectedBounds() {
    Rectangle b = super.getBounds();
    int dx = 0 - (int) b.getX();
    int dy = 0 - (int) b.getY();
    return new Rectangle(0, 0, (int) b.getWidth() - dx, (int) b.getHeight() - dy);
    }*/
    public MapRenderer getMapRenderer() {
        return mMapRenderer;
    }

    private int countVillages(Point pStart, Point pEnd) {
        int cnt = 0;
        //sort coordinates
        int xStart = (pStart.x < pEnd.x) ? pStart.x : pEnd.x;
        int xEnd = (pEnd.x > pStart.x) ? pEnd.x : pStart.x;
        int yStart = (pStart.y < pEnd.y) ? pStart.y : pEnd.y;
        int yEnd = (pEnd.y > pStart.y) ? pEnd.y : pStart.y;
        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                try {
                    Village v = DataHolder.getSingleton().getVillages()[x][y];
                    if (v != null && v.isVisibleOnMap()) {
                        cnt++;
                    }
                } catch (Exception e) {
                    //avoid IndexOutOfBounds if selection is too small
                }
            }
        }
        return cnt;
    }

    private List<Village> getSelectedVillages(Point pStart, Point pEnd) {
        int xStart = (pStart.x < pEnd.x) ? pStart.x : pEnd.x;
        int xEnd = (pEnd.x > pStart.x) ? pEnd.x : pStart.x;
        int yStart = (pStart.y < pEnd.y) ? pStart.y : pEnd.y;
        int yEnd = (pEnd.y > pStart.y) ? pEnd.y : pStart.y;
        List<Village> villages = new LinkedList<Village>();
        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                try {
                    Village v = DataHolder.getSingleton().getVillages()[x][y];
                    if (v != null && v.isVisibleOnMap()) {
                        villages.add(v);
                    }
                } catch (Exception e) {
                    //avoid IndexOutOfBounds if selection is too small
                }
            }
        }
        return villages;
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

    public int getCurrentCursor() {
        return iCurrentCursor;
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

            if ((isOutside) && (mouseDown) && (iCurrentCursor != ImageManager.CURSOR_DEFAULT)) {
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
    protected synchronized void updateMapPosition(double pX, double pY) {
        dCenterX = pX;
        dCenterY = pY;

        if (mMapRenderer == null) {
            logger.info("Creating MapRenderer");
            mMapRenderer = new MapRenderer();
            mMapRenderer.start();
        }

        positionUpdate = true;
        getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
    }

    public void updateVirtualBounds(Point2D.Double pViewStart) {
        double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        double xV = pViewStart.getX();
        double yV = pViewStart.getY();
        double wV = (double) getWidth() / ((double) GlobalOptions.getSkin().getFieldWidth() / zoom);
        double hV = (double) getHeight() / ((double) GlobalOptions.getSkin().getFieldHeight() / zoom);
        mVirtualBounds.setRect(xV, yV, wV, hV);
    }

    public Point2D.Double getCurrentPosition() {
        return new Point2D.Double(dCenterX, dCenterY);
    }

    public void setVillageSelectionListener(VillageSelectionListener pListener) {
        mVillageSelectionListener = pListener;
    }

    public Point.Double getCurrentVirtualPosition() {
        return new Point.Double(mVirtualBounds.getX(), mVirtualBounds.getY());
    }

    public Point virtualPosToSceenPos(double pXVirt, double pYVirt) {
        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        Image tmp = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, z);
        double width = (double) tmp.getWidth(null);
        double height = (double) tmp.getHeight(null);

        double xp = (pXVirt - mVirtualBounds.getX()) * width;
        double yp = (pYVirt - mVirtualBounds.getY()) * height;
        return new Point((int) Math.rint(xp), (int) Math.rint(yp));
    }

    public Point2D.Double mouseToVirtualPos(int pX, int pY) {
        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        Image tmp = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, z);
        double width = (double) tmp.getWidth(null);
        double height = (double) tmp.getHeight(null);
        double x = mVirtualBounds.getX() + ((double) pX / (double) width);
        double y = mVirtualBounds.getY() + ((double) pY / (double) height);
        return new Point2D.Double(x, y);
    }

    public Point2D.Double virtualPosToSceenPosDouble(double pXVirt, double pYVirt) {
        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        //calculate real pos in current frame
        Image tmp = GlobalOptions.getSkin().getImage(Skin.ID_DEFAULT_UNDERGROUND, z);
        double width = (double) tmp.getWidth(null);
        double height = (double) tmp.getHeight(null);
        double xp = (pXVirt - mVirtualBounds.getX()) * width;
        double yp = (pYVirt - mVirtualBounds.getY()) * height;
        return new Point2D.Double(xp, yp);
    }

    public Rectangle2D getVirtualBounds() {
        return mVirtualBounds;
    }

    /**Get village at current mouse position, null if there is no village*/
    public Village getVillageAtMousePos() {
        if (MenuRenderer.getSingleton().isVisible()) {
            return null;
        }

        if (mVillagePositions == null) {
            return null;
        }

        try {
            Enumeration<Village> villages = mVillagePositions.keys();
            Point mouse = getMousePosition();
            while (villages.hasMoreElements()) {
                Village current = villages.nextElement();
                if (mVillagePositions.get(current).contains(mouse)) {
                    return current;
                }
            }
        } catch (Exception e) {
            //failed getting village (probably getting mousepos failed)
        }

        return null;
    }

    /**Update operation perfomed by the RepaintThread was completed*/
    public void updateComplete(Hashtable<Village, Rectangle> pPositions, Image pBuffer) {
        mBuffer = pBuffer;
        mVillagePositions = pPositions;
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
        /*  for (MapPanelListener listener : mMapPanelListeners) {
        listener.fireVillageAtMousePosChangedEvent(pVillage);
        }*/
    }

    /* public synchronized void fireDistanceEvents(Village pSource, Village pTarget) {
    for (MapPanelListener listener : mMapPanelListeners) {
    listener.fireDistanceEvent(pSource, pTarget);
    }
    }*/
    public synchronized void fireScrollEvents(double pX, double pY) {
        for (MapPanelListener listener : mMapPanelListeners) {
            listener.fireScrollEvent(pX, pY);
        }

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        int[] xs = new int[]{0, 1, 2};
        int[] ys = new int[]{0, 1, 0};
        int tm = 3;



    }
}
