/*
 * MapPanel.java
 *
 * Created on 4. September 2007, 18:05
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Church;
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
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.MapShotListener;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.VillageSelectionListener;
import de.tor.tribes.util.church.ChurchManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author Charon
 */
public class MapPanel extends JPanel {

    // <editor-fold defaultstate="collapsed" desc=" Member variables ">
    private static Logger logger = Logger.getLogger("MapCanvas");
    private BufferedImage mBuffer = null;
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
    private String sMapShotType = null;
    private File mMapShotFile = null;
    private boolean bMapSHotPlaned = false;
    private MapShotListener mMapShotListener = null;
    private Hashtable<Village, Rectangle> mVillagePositions = null;
    private List<Village> exportVillageList = null;
    private Village radarVillage = null;
    private boolean spaceDown = false;
    private boolean shiftDown = false;
    private List<Village> markedVillages = null;
    private Village actionMenuVillage = null;
    // </editor-fold>

    public static synchronized MapPanel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MapPanel();
        }
        return SINGLETON;
    }

    /** Creates new form MapPanel */
    MapPanel() {
        super();
        initComponents();

        logger.info("Creating MapPanel");
        mMapPanelListeners = new LinkedList<MapPanelListener>();
        mToolChangeListeners = new LinkedList<ToolChangeListener>();
        mMarkerAddFrame = new MarkerAddFrame();
        setCursor(ImageManager.getCursor(iCurrentCursor));
        setIgnoreRepaint(true);
        attackAddFrame = new AttackAddFrame();
        mVirtualBounds = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
        jCopyOwn.setSelected(true);
        jCopyOwnAlly.setSelected(true);
        jCopyEnemyAlly.setSelected(true);
        jCopyBarbarian.setSelected(true);
        jCopyVillagesDialog.pack();
        mVillageSelectionListener = DSWorkbenchSelectionFrame.getSingleton();
        markedVillages = new LinkedList<Village>();
        initListeners();
    }

    public void setSpaceDown(boolean pValue) {
        spaceDown = pValue;
    }

    public void setShiftDown(boolean pValue) {
        shiftDown = pValue;
    }

    public List<Village> getMarkedVillages() {
        return markedVillages;
    }

    public void initBuffer() {
        //  createBufferStrategy(2);
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
                    //second button might show village menu
                    Village v = getVillageAtMousePos();
                    if (v == null) {
                        //show menu
                        MenuRenderer.getSingleton().setMenuLocation(e.getX(), e.getY());
                        MenuRenderer.getSingleton().switchVisibility();
                    } else {
                        //show village menu
                        actionMenuVillage = v;
                        jVillageActionsMenu.show(MapPanel.getSingleton(), e.getX(), e.getY());
                    }
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON1 && shiftDown) {
                    Village v = getVillageAtMousePos();
                    if (v != null) {
                        if (!markedVillages.contains(v)) {
                            markedVillages.add(v);
                        } else {
                            markedVillages.remove(v);
                        }
                    } else {
                        markedVillages.clear();
                    }
                    return;
                } else if (!shiftDown) {
                    markedVillages.clear();
                }

                if (MenuRenderer.getSingleton().isVisible()) {
                    return;
                }
                int unit = -1;
                boolean isAttack = false;
                if (!spaceDown) {
                    isAttack = isAttackCursor();
                }

                int tmpCursor = (spaceDown) ? ImageManager.CURSOR_DEFAULT : iCurrentCursor;
                switch (tmpCursor) {
                    case ImageManager.CURSOR_DEFAULT: {
                        //center village on click with default cursor
                        Village current = getVillageAtMousePos();
                        if (current != null) {
                            Tribe t = DSWorkbenchMainFrame.getSingleton().getCurrentUser();
                            if ((current != null) && (current.getTribe() != null) && (t != null) && (t.equals(current.getTribe()))) {
                                DSWorkbenchMainFrame.getSingleton().setCurrentUserVillage(current);
                            }
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
                            VillageTagFrame.getSingleton().setLocation(e.getPoint());
                            VillageTagFrame.getSingleton().showTagsFrame(current);
                            break;
                        }
                    }
                    case ImageManager.CURSOR_SUPPORT: {
                        Village current = getVillageAtMousePos();
                        if (current != null) {
                            if (current.getTribe() == null) {
                                //empty village
                                return;
                            }
                        } else {
                            //no village
                            return;
                        }
                        VillageSupportFrame.getSingleton().setLocation(e.getPoint());
                        VillageSupportFrame.getSingleton().showSupportFrame(current);
                        break;
                    }
                    case ImageManager.CURSOR_RADAR: {
                        try {
                            if (radarVillage != null && radarVillage.equals(getVillageAtMousePos())) {
                                radarVillage = null;
                            } else {
                                radarVillage = getVillageAtMousePos();
                            }
                        } catch (Exception inner) {
                            radarVillage = getVillageAtMousePos();
                        }
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
                    case ImageManager.CURSOR_CHURCH_1: {
                        Village v = getVillageAtMousePos();
                        if (v != null) {
                            ChurchManager.getSingleton().addChurch(v, Church.RANGE1);

                        }
                        break;
                    }
                    case ImageManager.CURSOR_CHURCH_2: {
                        Village v = getVillageAtMousePos();
                        if (v != null) {
                            ChurchManager.getSingleton().addChurch(v, Church.RANGE2);

                        }
                        break;
                    }
                    case ImageManager.CURSOR_CHURCH_3: {
                        Village v = getVillageAtMousePos();
                        if (v != null) {
                            ChurchManager.getSingleton().addChurch(v, Church.RANGE3);

                        }
                        break;
                    }
                    case ImageManager.CURSOR_REMOVE_CHURCH: {
                        Village v = getVillageAtMousePos();
                        if (v != null) {
                            ChurchManager.getSingleton().removeChurch(v);
                        }
                        break;
                    }
                    case ImageManager.CURSOR_NOTE: {
                        Village v = getVillageAtMousePos();
                        if (v != null) {
                            DSWorkbenchNotepad.getSingleton().addNoteForVillage(v);
                            if (!DSWorkbenchNotepad.getSingleton().isVisible()) {
                                DSWorkbenchNotepad.getSingleton().setVisible(true);
                            }
                        }
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
                if (!spaceDown) {
                    isAttack = isAttackCursor();
                }
                int tmpCursor = (spaceDown) ? ImageManager.CURSOR_DEFAULT : iCurrentCursor;

                switch (tmpCursor) {
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
                int tmpCursor = (spaceDown) ? ImageManager.CURSOR_DEFAULT : iCurrentCursor;

                if ((tmpCursor == ImageManager.CURSOR_DRAW_LINE) ||
                        (tmpCursor == ImageManager.CURSOR_DRAW_RECT) ||
                        (tmpCursor == ImageManager.CURSOR_DRAW_CIRCLE) ||
                        (tmpCursor == ImageManager.CURSOR_DRAW_TEXT) ||
                        (tmpCursor == ImageManager.CURSOR_DRAW_FREEFORM)) {
                    FormConfigFrame.getSingleton().purge();
                } else {
                    if ((tmpCursor == ImageManager.CURSOR_ATTACK_AXE) ||
                            (tmpCursor == ImageManager.CURSOR_ATTACK_SWORD) ||
                            (tmpCursor == ImageManager.CURSOR_ATTACK_SPY) ||
                            (tmpCursor == ImageManager.CURSOR_ATTACK_LIGHT) ||
                            (tmpCursor == ImageManager.CURSOR_ATTACK_HEAVY) ||
                            (tmpCursor == ImageManager.CURSOR_ATTACK_RAM) ||
                            (tmpCursor == ImageManager.CURSOR_ATTACK_SNOB)) {
                        isAttack = true;
                    }

                    switch (tmpCursor) {
                        case ImageManager.CURSOR_DEFAULT: {
                            mouseDownPoint = null;
                            break;
                        }
                        case ImageManager.CURSOR_SELECTION: {
                            if (selectionRect == null) {
                                return;
                            }
                            int xs = (int) Math.floor(selectionRect.getXPos());
                            int ys = (int) Math.floor(selectionRect.getYPos());
                            int xe = (int) Math.floor(selectionRect.getXPosEnd());
                            int ye = (int) Math.floor(selectionRect.getYPosEnd());
                            //if (mVillageSelectionListener != null) {
                            //if a selectionlistener is registered notify it
                            mVillageSelectionListener.fireSelectionFinishedEvent(new Point(xs, ys), new Point(xe, ye));
                            /* } else {
                            exportVillageList = getSelectedVillages(new Point(xs, ys), new Point(xe, ye));
                            if (exportVillageList.size() > 0) {
                            //do selection handling by ourself
                            if (exportVillageList.size() == 1) {
                            jVillageExportDetails.setText("Es wurde 1 Dorf zum Kopieren in die Zwischenablage ausgewählt.");
                            } else {
                            jVillageExportDetails.setText("Es wurden " + exportVillageList.size() + " Dörfer zum Kopieren in die Zwischenablage ausgewählt.");
                            }

                            jCopyVillagesDialog.setLocationRelativeTo(MapPanel.getSingleton());
                            jCopyVillagesDialog.setVisible(true);
                            }
                            }*/
                            selectionRect = null;
                            // mVillageSelectionListener = null;
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
                            try {
                                double d = DSCalculator.calculateDistance(mSourceVillage, mTargetVillage);
                                if (d > ServerSettings.getSingleton().getSnobRange()) {
                                    JOptionPaneHelper.showErrorBox(DSWorkbenchMainFrame.getSingleton(), "Maximale AG Reichweite überschritten", "Fehler");
                                    isAttack = false;
                                }
                            } catch (Exception inner) {
                                isAttack = false;
                            }
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
                if (!spaceDown) {
                    isAttack = isAttackCursor();
                }
                int tmpCursor = (spaceDown) ? ImageManager.CURSOR_DEFAULT : iCurrentCursor;


                switch (tmpCursor) {
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

                        double w = GlobalOptions.getSkin().getCurrentFieldWidth();
                        double h = GlobalOptions.getSkin().getCurrentFieldHeight();
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

    public boolean isAttackCursor() {
        return ((iCurrentCursor == ImageManager.CURSOR_ATTACK_AXE) ||
                (iCurrentCursor == ImageManager.CURSOR_ATTACK_SWORD) ||
                (iCurrentCursor == ImageManager.CURSOR_ATTACK_SPY) ||
                (iCurrentCursor == ImageManager.CURSOR_ATTACK_LIGHT) ||
                (iCurrentCursor == ImageManager.CURSOR_ATTACK_HEAVY) ||
                (iCurrentCursor == ImageManager.CURSOR_ATTACK_RAM) ||
                (iCurrentCursor == ImageManager.CURSOR_ATTACK_SNOB));
    }

    protected void resetServerDependendSettings() {
        radarVillage = null;
        markedVillages.clear();
        actionMenuVillage = null;
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

    public Village getRadarVillage() {
        return radarVillage;
    }

    protected AttackAddFrame getAttackAddFrame() {
        return attackAddFrame;
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
        if (pCurrentCursor == ImageManager.CURSOR_SELECTION) {
            DSWorkbenchSelectionFrame.getSingleton().setVisible(true);
        }
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

        jCopyVillagesDialog = new javax.swing.JDialog();
        jVillageExportDetails = new javax.swing.JLabel();
        jExportTribeName = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jExportAllyName = new javax.swing.JCheckBox();
        jExportPoints = new javax.swing.JCheckBox();
        jExportBBButton = new javax.swing.JButton();
        jExportPlainButton = new javax.swing.JButton();
        jCancelExportButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jCopyOwn = new javax.swing.JCheckBox();
        jCopyOwnAlly = new javax.swing.JCheckBox();
        jCopyEnemyAlly = new javax.swing.JCheckBox();
        jCopyBarbarian = new javax.swing.JCheckBox();
        jVillageActionsMenu = new javax.swing.JPopupMenu();
        jTitledSeparatorCurrent = new javax.swing.JSeparator();
        jCurrentCoordToClipboardItem = new javax.swing.JMenuItem();
        jCurrentCoordAsBBToClipboardItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jCenterItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jCurrentToAttackPlanerAsSourceItem = new javax.swing.JMenuItem();
        jCurrentToAttackPlanerAsTargetItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jCurrentCreateNoteItem = new javax.swing.JMenuItem();
        jCurrentAddToNoteItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jVillageInfoIngame = new javax.swing.JMenuItem();
        jTitledSeparatorAll = new javax.swing.JSeparator();
        jAllCoordToClipboardItem = new javax.swing.JMenuItem();
        jAllCoordAsBBToClipboardItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jAllToAttackPlanerAsSourceItem = new javax.swing.JMenuItem();
        jAllToAttackPlanerAsTargetItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jAllCreateNoteItem = new javax.swing.JMenuItem();
        jAllAddToNoteItem = new javax.swing.JMenuItem();

        jCopyVillagesDialog.setTitle("Dorfinformationen kopieren");
        jCopyVillagesDialog.setAlwaysOnTop(true);

        jVillageExportDetails.setText("Es wurden 0 Dörfer zum Kopieren in die Zwischenablage ausgewählt.");

        jExportTribeName.setText("Besitzer");
        jExportTribeName.setOpaque(false);

        jLabel2.setText("Welche Informationen sollen zusätzlich kopiert werden?");

        jExportAllyName.setText("Stamm");
        jExportAllyName.setOpaque(false);

        jExportPoints.setText("Punktzahl");
        jExportPoints.setOpaque(false);

        jExportBBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        jExportBBButton.setToolTipText("Als BB Codes in die Zwischenablage kopieren");
        jExportBBButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireVillageExportEvent(evt);
            }
        });

        jExportPlainButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboard.png"))); // NOI18N
        jExportPlainButton.setToolTipText("Unformatiert in die Zwischenablage kopieren");
        jExportPlainButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireVillageExportEvent(evt);
            }
        });

        jCancelExportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jCancelExportButton.setToolTipText("Abbrechen");
        jCancelExportButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireVillageExportEvent(evt);
            }
        });

        jLabel3.setText("Welche Dörfer sollen kopiert werden?");

        jCopyOwn.setText("Eigene");
        jCopyOwn.setOpaque(false);

        jCopyOwnAlly.setText("Eigener Stamm");
        jCopyOwnAlly.setOpaque(false);

        jCopyEnemyAlly.setText("Fremde Stämme");
        jCopyEnemyAlly.setOpaque(false);

        jCopyBarbarian.setText("Barbarendörfer");
        jCopyBarbarian.setOpaque(false);

        javax.swing.GroupLayout jCopyVillagesDialogLayout = new javax.swing.GroupLayout(jCopyVillagesDialog.getContentPane());
        jCopyVillagesDialog.getContentPane().setLayout(jCopyVillagesDialogLayout);
        jCopyVillagesDialogLayout.setHorizontalGroup(
            jCopyVillagesDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCopyVillagesDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCopyVillagesDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jCopyVillagesDialogLayout.createSequentialGroup()
                        .addComponent(jCopyOwn)
                        .addGap(18, 18, 18)
                        .addComponent(jCopyOwnAlly)
                        .addGap(18, 18, 18)
                        .addComponent(jCopyEnemyAlly)
                        .addGap(18, 18, 18)
                        .addComponent(jCopyBarbarian))
                    .addComponent(jVillageExportDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .addGroup(jCopyVillagesDialogLayout.createSequentialGroup()
                        .addComponent(jExportTribeName)
                        .addGap(18, 18, 18)
                        .addComponent(jExportAllyName)
                        .addGap(18, 18, 18)
                        .addComponent(jExportPoints))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jCopyVillagesDialogLayout.createSequentialGroup()
                        .addComponent(jCancelExportButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jExportPlainButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jExportBBButton))
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jCopyVillagesDialogLayout.setVerticalGroup(
            jCopyVillagesDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCopyVillagesDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jVillageExportDetails)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jCopyVillagesDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCopyOwn)
                    .addComponent(jCopyOwnAlly)
                    .addComponent(jCopyBarbarian)
                    .addComponent(jCopyEnemyAlly))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jCopyVillagesDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jExportTribeName)
                    .addComponent(jExportAllyName)
                    .addComponent(jExportPoints))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jCopyVillagesDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jExportBBButton)
                    .addComponent(jExportPlainButton)
                    .addComponent(jCancelExportButton))
                .addContainerGap())
        );

        jTitledSeparatorCurrent.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 5, 1), "Aktuelles Dorf", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jTitledSeparatorCurrent.setPreferredSize(new java.awt.Dimension(0, 30));
        jVillageActionsMenu.add(jTitledSeparatorCurrent);

        jCurrentCoordToClipboardItem.setText("Koordinaten in Zwischenablage");
        jCurrentCoordToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jCurrentCoordToClipboardItem);

        jCurrentCoordAsBBToClipboardItem.setText("BB-Code in Zwischenablage");
        jCurrentCoordAsBBToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jCurrentCoordAsBBToClipboardItem);
        jVillageActionsMenu.add(jSeparator1);

        jCenterItem.setText("Zentrieren");
        jCenterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jCenterItem);
        jVillageActionsMenu.add(jSeparator2);

        jCurrentToAttackPlanerAsSourceItem.setText("In Angriffsplaner (Herkunft)");
        jCurrentToAttackPlanerAsSourceItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jCurrentToAttackPlanerAsSourceItem);

        jCurrentToAttackPlanerAsTargetItem.setText("In Angriffsplaner (Ziel)");
        jCurrentToAttackPlanerAsTargetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jCurrentToAttackPlanerAsTargetItem);
        jVillageActionsMenu.add(jSeparator3);

        jCurrentCreateNoteItem.setText("Notiz erstellen");
        jCurrentCreateNoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jCurrentCreateNoteItem);

        jCurrentAddToNoteItem.setText("Der gewählten Notiz hinzufügen");
        jCurrentAddToNoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jCurrentAddToNoteItem);
        jVillageActionsMenu.add(jSeparator4);

        jVillageInfoIngame.setText("Im Spiel zentrieren");
        jVillageInfoIngame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jVillageInfoIngame);

        jTitledSeparatorAll.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 5, 1), "Ausgewählte  Dörfer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        jTitledSeparatorAll.setPreferredSize(new java.awt.Dimension(0, 30));
        jVillageActionsMenu.add(jTitledSeparatorAll);

        jAllCoordToClipboardItem.setText("Koordinaten in Zwischenablage");
        jAllCoordToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jAllCoordToClipboardItem);

        jAllCoordAsBBToClipboardItem.setText("BB-Code in Zwischenablage");
        jAllCoordAsBBToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jAllCoordAsBBToClipboardItem);
        jVillageActionsMenu.add(jSeparator5);

        jAllToAttackPlanerAsSourceItem.setText("In Angriffsplaner (Herkunft)");
        jAllToAttackPlanerAsSourceItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jAllToAttackPlanerAsSourceItem);

        jAllToAttackPlanerAsTargetItem.setText("In Angriffsplaner (Ziel)");
        jAllToAttackPlanerAsTargetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jAllToAttackPlanerAsTargetItem);
        jVillageActionsMenu.add(jSeparator6);

        jAllCreateNoteItem.setText("Notiz erstellen");
        jAllCreateNoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jAllCreateNoteItem);

        jAllAddToNoteItem.setText("Der gewählten Notiz hinzufügen");
        jAllAddToNoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jVillageActionsMenu.add(jAllAddToNoteItem);

        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents

    private void fireVillageExportEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireVillageExportEvent
        try {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            Tribe own = DSWorkbenchMainFrame.getSingleton().getCurrentUser();
            boolean exported = false;
            if (evt.getSource() == jExportBBButton) {
                String result = "";

                for (Village v : exportVillageList) {
                    boolean doExport = false;
                    if (jCopyBarbarian.isSelected()) {
                        if (v.getTribe() == null) {
                            //is barbarian
                            doExport = true;
                        }
                    }
                    if (jCopyOwn.isSelected()) {
                        if (v.getTribe() != null && v.getTribe().equals(own)) {
                            //no barbarian, village tribe equals own
                            doExport = true;
                        }
                    }
                    if (jCopyOwnAlly.isSelected()) {
                        if (v.getTribe() != null && own.getAlly() != null && v.getTribe().getAlly() != null && v.getTribe().getAlly().equals(own.getAlly())) {
                            //no barbarian, own ally not null, village ally not null, village ally equals own
                            if (!jCopyOwn.isSelected()) {
                                //if own is not selected don't use own villages!
                                if (v.getTribe().equals(own)) {
                                    //ignore own villages
                                    doExport = false;
                                } else {
                                    //do export
                                    doExport = true;

                                }
                            } else {
                                //do export 
                                doExport = true;
                            }
                        }
                    }

                    if (jCopyEnemyAlly.isSelected()) {
                        if (v.getTribe() != null && v.getTribe().getAlly() == null || !v.getTribe().getAlly().equals(own.getAlly())) {
                            //no barbarien, no ally or ally not equal own ally
                            doExport = true;
                        }
                    }

                    if (doExport) {
                        exported = true;
                        result += v.toBBCode();
                        if (jExportPoints.isSelected()) {
                            result += " (" + nf.format(v.getPoints()) + ") ";
                        } else {
                            result += "\t";
                        }
                        if (jExportTribeName.isSelected() && v.getTribe() != null) {
                            result += v.getTribe().toBBCode() + " ";
                        } else {
                            if (jExportTribeName.isSelected()) {
                                result += "Barbaren ";
                            } else {
                                result += "\t";
                            }
                        }
                        if (jExportAllyName.isSelected() && v.getTribe() != null && v.getTribe().getAlly() != null) {
                            result += v.getTribe().getAlly().toBBCode() + "\n";
                        } else {
                            if (jExportAllyName.isSelected()) {
                                result += "(kein Stamm)\n";
                            } else {
                                result += "\n";
                            }
                        }
                    }
                }
                if (exported) {
                    StringTokenizer t = new StringTokenizer(result, "[");
                    int cnt = t.countTokens();
                    boolean doExport = true;
                    if (cnt > 500) {
                        if (JOptionPaneHelper.showQuestionConfirmBox(jCopyVillagesDialog, "Die ausgewählten Dörfer benötigen mehr als 500 BB-Codes\n" +
                                "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                            doExport = false;
                        }
                    }
                    if (doExport) {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
                        JOptionPaneHelper.showInformationBox(jCopyVillagesDialog, "Dorfdaten in die Zwischenablage kopiert.", "Daten kopiert");
                    }
                } else {
                    JOptionPaneHelper.showInformationBox(jCopyVillagesDialog, "Mit den gewählten Einstellungen werden keine Dörfer kopiert.", "Information");
                    return;
                }
            } else if (evt.getSource() == jExportPlainButton) {
                String result = "";
                for (Village v : exportVillageList) {
                    boolean doExport = false;
                    if (jCopyBarbarian.isSelected()) {
                        if (v.getTribe() == null) {
                            //is barbarian
                            doExport = true;
                        }
                    }
                    if (jCopyOwn.isSelected()) {
                        if (v.getTribe() != null && v.getTribe().equals(own)) {
                            //no barbarian, village tribe equals own
                            doExport = true;
                        }
                    }
                    if (jCopyOwnAlly.isSelected()) {
                        if (v.getTribe() != null && own.getAlly() != null && v.getTribe().getAlly() != null && v.getTribe().getAlly().equals(own.getAlly())) {
                            //no barbarian, own ally not null, village ally not null, village ally equals own
                            doExport = true;
                        }
                    }


                    if (jCopyEnemyAlly.isSelected()) {
                        if ((v.getTribe() != null && v.getTribe().getAlly() == null) || (v.getTribe() != null && v.getTribe().getAlly() != null && !v.getTribe().getAlly().equals(own.getAlly()))) {
                            //no barbarien, no ally or ally not equal own ally
                            doExport = true;
                        }
                    }

                    if (doExport) {
                        exported = true;
                        result += v + "\t";
                        if (jExportPoints.isSelected()) {
                            result += nf.format(v.getPoints()) + "\t";
                        } else {
                            result += "\t";
                        }
                        if (jExportTribeName.isSelected() && v.getTribe() != null) {
                            result += v.getTribe() + "\t";
                        } else {
                            if (jExportTribeName.isSelected()) {
                                result += "Barbaren\t";
                            } else {
                                result += "\t";
                            }
                        }
                        if (jExportAllyName.isSelected() && v.getTribe() != null && v.getTribe().getAlly() != null) {
                            result += v.getTribe().getAlly() + "\n";
                        } else {
                            if (jExportAllyName.isSelected()) {
                                result += "(kein Stamm)\n";
                            } else {
                                result += "\n";
                            }
                        }
                    }
                }
                if (exported) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
                    JOptionPaneHelper.showInformationBox(jCopyVillagesDialog, "Dorfdaten in die Zwischenablage kopiert.", "Daten kopiert");
                } else {
                    JOptionPaneHelper.showInformationBox(jCopyVillagesDialog, "Mit den gewählten Einstellungen werden keine Dörfer kopiert.", "Information");
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            JOptionPaneHelper.showErrorBox(jCopyVillagesDialog, "Fehler beim Kopieren der Daten.", "Fehler");
        }

        jCopyVillagesDialog.setVisible(false);

    }//GEN-LAST:event_fireVillageExportEvent

    private void fireVillagePopupActionEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireVillagePopupActionEvent

        if (evt.getSource() == jCurrentCoordToClipboardItem) {
            //copy current village coordinates to clipboard
            Village v = actionMenuVillage;
            if (v != null) {
                try {
                    String text = "";
                    if (ServerSettings.getSingleton().getCoordType() != 2) {
                        int[] hier = DSCalculator.xyToHierarchical((int) v.getX(), (int) v.getY());
                        text = hier[0] + ":" + hier[1] + ":" + hier[2];
                    } else {
                        text = v.getX() + "|" + v.getY();
                    }
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
                    JOptionPaneHelper.showInformationBox(this, "Koordinaten in die Zwischenablage kopiert", "Information");
                } catch (Exception e) {
                    JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
                }
            }
        } else if (evt.getSource() == jCurrentCoordAsBBToClipboardItem) {
            //copy current village as bb-code to clipboard
            Village v = actionMenuVillage;
            if (v != null) {
                try {
                    String text = v.toBBCode();
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
                    JOptionPaneHelper.showInformationBox(this, "BB-Code in die Zwischenablage kopiert", "Information");
                } catch (Exception e) {
                    JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
                }
            }
        } else if (evt.getSource() == jCenterItem) {
            //center current village on map
            Village v = actionMenuVillage;
            if (v != null) {
                DSWorkbenchMainFrame.getSingleton().centerVillage(v);
            }
        } else if (evt.getSource() == jCurrentToAttackPlanerAsSourceItem) {
            Village v = actionMenuVillage;
            if (v != null) {
                if (v.getTribe() == null) {
                    JOptionPaneHelper.showInformationBox(this, "Angriffe von Barbarendörfern können nicht geplant werden.", "Information");
                    return;
                }
                List<Village> toAdd = new LinkedList<Village>();
                toAdd.add(v);
                if (!DSWorkbenchMainFrame.getSingleton().getAttackPlaner().isVisible()) {
                    //show attack planer to allow adding data
                    DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setup();
                    DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setVisible(true);
                }
                DSWorkbenchMainFrame.getSingleton().getAttackPlaner().fireAddSourcesEvent(toAdd);
                JOptionPaneHelper.showInformationBox(this, "Dorf in Angriffsplaner eingefügt", "Information");
            }
        } else if (evt.getSource() == jCurrentToAttackPlanerAsTargetItem) {
            Village v = actionMenuVillage;
            if (v != null) {
                if (v.getTribe() == null) {
                    JOptionPaneHelper.showInformationBox(this, "Angriffe auf Barbarendörfer können nicht geplant werden.", "Information");
                    return;
                }
                List<Village> toAdd = new LinkedList<Village>();
                toAdd.add(v);
                if (!DSWorkbenchMainFrame.getSingleton().getAttackPlaner().isVisible()) {
                    //show attack planer to allow adding data
                    DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setup();
                    DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setVisible(true);
                }
                DSWorkbenchMainFrame.getSingleton().getAttackPlaner().fireAddTargetsEvent(toAdd);
                JOptionPaneHelper.showInformationBox(this, "Dorf in Angriffsplaner eingefügt", "Information");
            }
        } else if (evt.getSource() == jCurrentCreateNoteItem) {
            Village v = actionMenuVillage;
            if (v != null) {
                DSWorkbenchNotepad.getSingleton().addNoteForVillage(actionMenuVillage);
                JOptionPaneHelper.showInformationBox(this, "Notiz erstellt", "Information");
            }
        } else if (evt.getSource() == jCurrentAddToNoteItem) {
            Village v = actionMenuVillage;
            if (v != null) {
                if (DSWorkbenchNotepad.getSingleton().addVillageToCurrentNote(actionMenuVillage)) {
                    JOptionPaneHelper.showInformationBox(this, "Dorf hinzugefügt", "Information");
                } else {
                    JOptionPaneHelper.showWarningBox(this, "Es ist keine Notiz ausgewählt.", "Warnung");
                }
            }
        } else if (evt.getSource() == jVillageInfoIngame) {
            //center village ingame
            Village v = actionMenuVillage;
            if (v != null) {
                BrowserCommandSender.centerVillage(v);
            }

        } else if (evt.getSource() == jAllCoordToClipboardItem) {
            //copy selected villages coordinates to clipboard
            if (markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dörfer markiert.", "Information");
                return;
            }
            try {
                String text = "";
                for (Village v : markedVillages) {
                    if (ServerSettings.getSingleton().getCoordType() != 2) {
                        int[] hier = DSCalculator.xyToHierarchical((int) v.getX(), (int) v.getY());
                        text += hier[0] + ":" + hier[1] + ":" + hier[2] + "\n";
                    } else {
                        text += v.getX() + "|" + v.getY() + "\n";
                    }
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
                JOptionPaneHelper.showInformationBox(this, "Koordinaten in die Zwischenablage kopiert", "Information");
            } catch (Exception e) {
                JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
            }
        } else if (evt.getSource() == jAllCoordAsBBToClipboardItem) {
            //copy selected villages as bb-code to clipboard
            if (markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dörfer markiert.", "Information");
                return;
            }
            try {
                String text = "";
                for (Village v : markedVillages) {
                    text += v.toBBCode() + "\n";
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
                JOptionPaneHelper.showInformationBox(this, "BB-Code in die Zwischenablage kopiert", "Information");
            } catch (Exception e) {
                JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
            }
        } else if (evt.getSource() == jAllToAttackPlanerAsSourceItem) {
            if (markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dörfer markiert.", "Information");
                return;
            }
            if (!DSWorkbenchMainFrame.getSingleton().getAttackPlaner().isVisible()) {
                //show attack planer to allow adding data
                DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setup();
                DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setVisible(true);
            }
            DSWorkbenchMainFrame.getSingleton().getAttackPlaner().fireAddSourcesEvent(markedVillages);
            JOptionPaneHelper.showInformationBox(this, "Dörfer in Angriffsplaner eingefügt", "Information");
        } else if (evt.getSource() == jAllToAttackPlanerAsTargetItem) {
            if (markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dörfer markiert.", "Information");
                return;
            }
            if (!DSWorkbenchMainFrame.getSingleton().getAttackPlaner().isVisible()) {
                //show attack planer to allow adding data
                DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setup();
                DSWorkbenchMainFrame.getSingleton().getAttackPlaner().setVisible(true);
            }
            DSWorkbenchMainFrame.getSingleton().getAttackPlaner().fireAddTargetsEvent(markedVillages);
            JOptionPaneHelper.showInformationBox(this, "Dörfer in Angriffsplaner eingefügt", "Information");
        } else if (evt.getSource() == jAllCreateNoteItem) {
            if (markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dörfer markiert.", "Information");
                return;
            }
            Village v = actionMenuVillage;
            if (v != null) {
                DSWorkbenchNotepad.getSingleton().addNoteForVillages(markedVillages);
                JOptionPaneHelper.showInformationBox(this, "Notiz erstellt", "Information");
            }
        } else if (evt.getSource() == jAllAddToNoteItem) {
            if (markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dörfer markiert.", "Information");
                return;
            }
            if (DSWorkbenchNotepad.getSingleton().addVillagesToCurrentNote(markedVillages)) {
                JOptionPaneHelper.showInformationBox(this, "Dörfer hinzugefügt", "Information");
            } else {
                JOptionPaneHelper.showWarningBox(this, "Es ist keine Notiz ausgewählt.", "Warnung");
            }
        }
    }//GEN-LAST:event_fireVillagePopupActionEvent

    /**Draw buffer into panel*/
    @Override
    public void paint(Graphics g) {
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
        double xV = pViewStart.getX();
        double yV = pViewStart.getY();
        double wV = (double) getWidth() / GlobalOptions.getSkin().getCurrentFieldWidth();
        double hV = (double) getHeight() / GlobalOptions.getSkin().getCurrentFieldHeight();
        mVirtualBounds.setRect(xV, yV, wV, hV);
    }

    public Point2D.Double getCurrentPosition() {
        return new Point2D.Double(dCenterX, dCenterY);
    }

    public Point.Double getCurrentVirtualPosition() {
        return new Point.Double(mVirtualBounds.getX(), mVirtualBounds.getY());
    }

    public Point virtualPosToSceenPos(double pXVirt, double pYVirt) {
        double width = GlobalOptions.getSkin().getCurrentFieldWidth();
        double height = GlobalOptions.getSkin().getCurrentFieldHeight();
        double xp = (pXVirt - mVirtualBounds.getX()) * width;
        double yp = (pYVirt - mVirtualBounds.getY()) * height;
        return new Point((int) Math.rint(xp), (int) Math.rint(yp));
    }

    public Point2D.Double mouseToVirtualPos(int pX, int pY) {
        double width = GlobalOptions.getSkin().getCurrentFieldWidth();
        double height = GlobalOptions.getSkin().getCurrentFieldHeight();
        double x = mVirtualBounds.getX() + ((double) pX / (double) width);
        double y = mVirtualBounds.getY() + ((double) pY / (double) height);
        return new Point2D.Double(x, y);
    }

    public Point2D.Double virtualPosToSceenPosDouble(double pXVirt, double pYVirt) {
        double width = GlobalOptions.getSkin().getCurrentFieldWidth();
        double height = GlobalOptions.getSkin().getCurrentFieldHeight();
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

    public Village getVillageAtPoint(Point pPos) {
        if (MenuRenderer.getSingleton().isVisible()) {
            return null;
        }

        if (mVillagePositions == null) {
            return null;
        }

        try {
            Enumeration<Village> villages = mVillagePositions.keys();

            while (villages.hasMoreElements()) {
                Village current = villages.nextElement();
                if (mVillagePositions.get(current).contains(pPos)) {
                    return current;
                }

            }
        } catch (Exception e) {
            //failed getting village (probably getting mousepos failed)
        }

        return null;
    }

    /**Update operation perfomed by the RepaintThread was completed*/
    public void updateComplete(Hashtable<Village, Rectangle> pPositions, BufferedImage pBuffer) {
        mBuffer = pBuffer;
        mVillagePositions = pPositions;
        if (bMapSHotPlaned) {
            saveMapShot(mBuffer);
        }

        if (positionUpdate) {
            DSWorkbenchFormFrame.getSingleton().updateFormList();
        }

        positionUpdate = false;
    }

    /*public BufferStrategy getStrategy() {
    return getBufferStrategy();
    }*/
    protected void planMapShot(String pType, File pLocation, MapShotListener pListener) {
        sMapShotType = pType;
        mMapShotFile = pLocation;
        bMapSHotPlaned = true;
        mMapShotListener = pListener;
    }

    private void saveMapShot(Image pImage) {
        try {
            Point2D.Double pos = getCurrentPosition();
            String first = "";
            if (ServerSettings.getSingleton().getCoordType() != 2) {
                int[] hier = DSCalculator.xyToHierarchical((int) pos.x, (int) pos.y);
                first = "Zentrum: " + hier[0] + ":" + hier[1] + ":" + hier[2];
            } else {
                first = "Zentrum: " + (int) Math.floor(pos.getX()) + "|" + (int) Math.floor(pos.getY());
            }

            BufferedImage result = null;
            result = new BufferedImage(pImage.getWidth(null), pImage.getHeight(null), BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = (Graphics2D) result.getGraphics();
            g2d.drawImage(pImage, 0, 0, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D firstBounds = fm.getStringBounds(first, g2d);
            String second = "Erstellt mit DS Workbench " + Constants.VERSION + Constants.VERSION_ADDITION;
            Rectangle2D secondBounds = fm.getStringBounds(second, g2d);
            g2d.setColor(Constants.DS_BACK_LIGHT);
            g2d.fill3DRect(0, (int) (result.getHeight() - firstBounds.getHeight() - secondBounds.getHeight() - 9), (int) (secondBounds.getWidth() + 6), (int) (firstBounds.getHeight() + secondBounds.getHeight() + 9), true);
            g2d.setColor(Color.BLACK);
            g2d.drawString(first, 3, (int) (result.getHeight() - firstBounds.getHeight() - secondBounds.getHeight() - firstBounds.getY() - 6));
            g2d.drawString(second, 3, (int) (result.getHeight() - secondBounds.getHeight() - secondBounds.getY() - 3));

            ImageIO.write(result, sMapShotType, mMapShotFile);
            g2d.dispose();
            bMapSHotPlaned = false;
            mMapShotListener.fireMapShotDoneEvent();
        } catch (Exception e) {
            bMapSHotPlaned = false;
            logger.error("Creating MapShot failed", e);
            mMapShotListener.fireMapShotFailedEvent();
        }

    }

    public synchronized void fireToolChangedEvents(int pTool) {
        for (ToolChangeListener listener : mToolChangeListeners) {
            listener.fireToolChangedEvent(pTool);
        }

    }

    public synchronized void fireVillageAtMousePosChangedEvents(Village pVillage) {
    }

    public synchronized void fireScrollEvents(double pX, double pY) {
        for (MapPanelListener listener : mMapPanelListeners) {
            listener.fireScrollEvent(pX, pY);
        }

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jAllAddToNoteItem;
    private javax.swing.JMenuItem jAllCoordAsBBToClipboardItem;
    private javax.swing.JMenuItem jAllCoordToClipboardItem;
    private javax.swing.JMenuItem jAllCreateNoteItem;
    private javax.swing.JMenuItem jAllToAttackPlanerAsSourceItem;
    private javax.swing.JMenuItem jAllToAttackPlanerAsTargetItem;
    private javax.swing.JButton jCancelExportButton;
    private javax.swing.JMenuItem jCenterItem;
    private javax.swing.JCheckBox jCopyBarbarian;
    private javax.swing.JCheckBox jCopyEnemyAlly;
    private javax.swing.JCheckBox jCopyOwn;
    private javax.swing.JCheckBox jCopyOwnAlly;
    private javax.swing.JDialog jCopyVillagesDialog;
    private javax.swing.JMenuItem jCurrentAddToNoteItem;
    private javax.swing.JMenuItem jCurrentCoordAsBBToClipboardItem;
    private javax.swing.JMenuItem jCurrentCoordToClipboardItem;
    private javax.swing.JMenuItem jCurrentCreateNoteItem;
    private javax.swing.JMenuItem jCurrentToAttackPlanerAsSourceItem;
    private javax.swing.JMenuItem jCurrentToAttackPlanerAsTargetItem;
    private javax.swing.JCheckBox jExportAllyName;
    private javax.swing.JButton jExportBBButton;
    private javax.swing.JButton jExportPlainButton;
    private javax.swing.JCheckBox jExportPoints;
    private javax.swing.JCheckBox jExportTribeName;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jTitledSeparatorAll;
    private javax.swing.JSeparator jTitledSeparatorCurrent;
    private javax.swing.JPopupMenu jVillageActionsMenu;
    private javax.swing.JLabel jVillageExportDetails;
    private javax.swing.JMenuItem jVillageInfoIngame;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        int[] xs = new int[]{0, 1, 2};
        int[] ys = new int[]{0, 1, 0};
        int tm = 3;

    }
}