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
import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Church;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanelListener;
import de.tor.tribes.ui.dnd.VillageTransferable;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.ui.views.*;
import de.tor.tribes.ui.windows.*;
import de.tor.tribes.ui.wiz.tap.AttackSourcePanel;
import de.tor.tribes.ui.wiz.tap.AttackTargetPanel;
import de.tor.tribes.ui.wiz.tap.TacticsPlanerWizard;
import de.tor.tribes.util.*;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.bb.VillageListFormatter;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.interfaces.MapShotListener;
import de.tor.tribes.util.interfaces.ToolChangeListener;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.note.NoteManager;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.stat.StatManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * @author Charon
 */
public class MapPanel extends JPanel implements DragGestureListener, // For recognizing the start of drags
        DragSourceListener, // For processing drag source events
        DropTargetListener,
        ActionListener// For processing drop target events
{

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e == null || e.getActionCommand() == null) {
            return;
        }
        if (e.getActionCommand().equals("Copy")) {
            if (markedVillages != null && !markedVillages.isEmpty()) {
                try {
                    StringBuilder b = new StringBuilder();
                    for (Village v : markedVillages) {
                        b.append(v.toString()).append("\n");
                    }

                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
                    DSWorkbenchMainFrame.getSingleton().showSuccess(((markedVillages.size() == 1) ? "Ein Dorf " : markedVillages.size() + " Dörfer ") + "in die Zwischenablage kopiert");
                } catch (Exception ex) {
                    logger.error("Failed to copy village from map to clipboard", ex);
                }
            } else {
                DSWorkbenchMainFrame.getSingleton().showSuccess("Keine Dörfer zum Kopieren markiert");
            }
        } else if (e.getActionCommand().equals("BBCopy")) {
            if (markedVillages != null && !markedVillages.isEmpty()) {
                try {
                    boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(MapPanel.this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);
                    String result = new VillageListFormatter().formatElements(markedVillages, extended);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
                    DSWorkbenchMainFrame.getSingleton().showSuccess(((markedVillages.size() == 1) ? "Ein Dorf " : markedVillages.size() + " Dörfer ") + "als BB-Codes in die Zwischenablage kopiert");
                } catch (Exception ex) {
                    logger.error("Failed to copy village from map to clipboard", ex);
                }
            } else {
                DSWorkbenchMainFrame.getSingleton().showSuccess("Keine Dörfer zum Kopieren markiert");
            }
        }
    }
    // <editor-fold defaultstate="collapsed" desc=" Member variables ">
    private static Logger logger = Logger.getLogger("MapCanvas");
    private BufferedImage mBuffer = null;
    //private VolatileImage mBuffer = null;
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
    private de.tor.tribes.types.drawing.Rectangle selectionRect = null;
    // private VillageSelectionListener mVillageSelectionListener = null;
    private String sMapShotType = null;
    private File mMapShotFile = null;
    private boolean bMapSHotPlaned = false;
    private MapShotListener mMapShotListener = null;
    private HashMap<Village, Rectangle> mVillagePositions = null;
    private Village radarVillage = null;
    private boolean spaceDown = false;
    private boolean shiftDown = false;
    private List<Village> markedVillages = null;
    private Village actionMenuVillage = null;
    DragSource dragSource; // A central DnD object
    boolean dragMode; // Are we dragging or scribbling?
    boolean dragMove = false;
    private ScreenshotSaver mScreenSaver = null;
    // </editor-fold>

    public static synchronized MapPanel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new MapPanel();
        }
        return SINGLETON;
    }

    /**
     * Creates new form MapPanel
     */
    MapPanel() {
        initComponents();
        logger.info("Creating MapPanel");
        mMapPanelListeners = new LinkedList<>();
        mToolChangeListeners = new LinkedList<>();
        mMarkerAddFrame = new MarkerAddFrame();
        setCursor(ImageManager.getCursor(iCurrentCursor));
        setOpaque(true);
        setIgnoreRepaint(true);
        attackAddFrame = new AttackAddFrame();
        mVirtualBounds = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
        markedVillages = new LinkedList<>();
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        this.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        this.registerKeyboardAction(this, "BBCopy", bbCopy, JComponent.WHEN_IN_FOCUSED_WINDOW);

        initListeners();
        if (!GlobalOptions.isMinimal()) {
            new Timer("RepaintTimer", true).schedule(new TimerTask() {

                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            repaint();
                        }
                    });
                }
            }, 0, 100);
        }
        mScreenSaver = new ScreenshotSaver();
        mScreenSaver.start();

        //adding manager listeners
        TagManager.getSingleton().addManagerListener(new GenericManagerListener() {

            @Override
            public void dataChangedEvent() {
                dataChangedEvent(null);
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
            }
        });

        AttackManager.getSingleton().addManagerListener(new GenericManagerListener() {

            @Override
            public void dataChangedEvent() {
                dataChangedEvent(null);
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                getMapRenderer().initiateRedraw(MapRenderer.ATTACK_LAYER);
            }
        });
        MarkerManager.getSingleton().addManagerListener(new GenericManagerListener() {

            @Override
            public void dataChangedEvent() {
                dataChangedEvent(null);
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                getMapRenderer().initiateRedraw(MapRenderer.MARKER_LAYER);
            }
        });
        NoteManager.getSingleton().addManagerListener(new GenericManagerListener() {

            @Override
            public void dataChangedEvent() {
                dataChangedEvent(null);
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                getMapRenderer().initiateRedraw(MapRenderer.NOTE_LAYER);
            }
        });
        TroopsManager.getSingleton().addManagerListener(new GenericManagerListener() {

            @Override
            public void dataChangedEvent() {
                dataChangedEvent(null);
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                getMapRenderer().initiateRedraw(MapRenderer.TROOP_LAYER);
            }
        });
    }

    public void setSpaceDown(boolean pValue) {
        spaceDown = pValue;
    }

    public void setShiftDown(boolean pValue) {
        shiftDown = pValue;

        if (!shiftDown) {
            int cursor = iCurrentCursor;
            boolean villagesHandled = false;
            if (cursor == ImageManager.CURSOR_TAG) {
                //tag selected villages
                if (markedVillages != null && !markedVillages.isEmpty()) {
                    VillageTagFrame.getSingleton().setLocationRelativeTo(this);
                    VillageTagFrame.getSingleton().showTagsFrame(markedVillages);
                    villagesHandled = true;
                }
            } else if (cursor == ImageManager.CURSOR_NOTE) {
                //add note for selected villages
                if (markedVillages != null && !markedVillages.isEmpty()) {
                    DSWorkbenchNotepad.getSingleton().addNoteForVillages(markedVillages);
                    villagesHandled = true;
                }
            }
            if (villagesHandled) {
                markedVillages.clear();
            }
        }
    }

    public Village[] getMarkedVillages() {
        if (markedVillages == null) {
            return new Village[]{};
        }
        return markedVillages.toArray(new Village[markedVillages.size()]);
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

    public synchronized void removeToolChangeListener(ToolChangeListener pListener) {
        mToolChangeListeners.remove(pListener);
    }

    public de.tor.tribes.types.drawing.Rectangle getSelectionRect() {
        return selectionRect;
    }

    private void initListeners() {
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, // What component
                DnDConstants.ACTION_COPY_OR_MOVE, // What drag types?
                this);// the listener

        // Create and set up a DropTarget that will listen for drags and
        // drops over this component, and will notify the DropTargetListener
        DropTarget dropTarget = new DropTarget(this, // component to monitor
                this); // listener to notify
        this.setDropTarget(dropTarget); // Tell the component about it.

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
                    if (v != null) {
                        //show village menu
                        actionMenuVillage = v;
                        jVillageActionsMenu.show(MapPanel.getSingleton().getParent(), e.getX(), e.getY());
                    }
                    return;
                }

                int tmpCursor = (spaceDown) ? ImageManager.CURSOR_DEFAULT : iCurrentCursor;

                Village v = getVillageAtMousePos();
                if (!shiftDown) {
                    //left click, no shift and no opened menu clears selected villages
                    markedVillages.clear();
                    //DSWorkbenchSelectionFrame.getSingleton().resetView();
                }

                if (shiftDown && (tmpCursor == ImageManager.CURSOR_SELECTION || tmpCursor == ImageManager.CURSOR_TAG || tmpCursor == ImageManager.CURSOR_NOTE)) {
                    //add current mouse village if there is one
                    if (v != null) {
                        if (!markedVillages.contains(v)) {
                            markedVillages.add(v);
                        } else {
                            markedVillages.remove(v);
                        }
                    }
                    DSWorkbenchSelectionFrame.getSingleton().addVillages(markedVillages);
                    return;
                }

                int unit = -1;
                boolean isAttack = false;
                if (!spaceDown) {
                    isAttack = isAttackCursor();
                }

                switch (tmpCursor) {
                    case ImageManager.CURSOR_DEFAULT: {
                        //center village on click with default cursor
                        if (v != null) {
                            Tribe t = GlobalOptions.getSelectedProfile().getTribe();
                            if ((v != null)
                                    && (t != null)
                                    && (v.getTribe() != Barbarians.getSingleton())
                                    && (t != Barbarians.getSingleton())
                                    && (t.getId() == v.getTribe().getId())) {
                                DSWorkbenchMainFrame.getSingleton().setCurrentUserVillage(v);
                            }
                        }
                        break;
                    }
                    case ImageManager.CURSOR_MARK: {
                        if (v != null) {
                            if (v.getTribe() == Barbarians.getSingleton()) {
                                //empty village
                                return;
                            }
                            mMarkerAddFrame.setLocation(e.getPoint());
                            mMarkerAddFrame.setVillage(v);
                            mMarkerAddFrame.setVisible(true);
                        }
                        break;
                    }
                    case ImageManager.CURSOR_TAG: {
                        if (v != null) {
                            if (v.getTribe() == Barbarians.getSingleton()) {
                                //empty village
                                DSWorkbenchMainFrame.getSingleton().showInfo("Barbarendörfern können keine Gruppen zugewiesen werden");
                                return;
                            }
                            List<Village> marked = Arrays.asList(getMarkedVillages());
                            if (marked == null || marked.isEmpty()) {
                                VillageTagFrame.getSingleton().setLocation(e.getPoint());
                                VillageTagFrame.getSingleton().showTagsFrame(v);
                            } else {
                                VillageTagFrame.getSingleton().setLocation(e.getPoint());
                                VillageTagFrame.getSingleton().showTagsFrame(marked);
                            }
                            break;
                        }
                        break;
                    }
                    case ImageManager.CURSOR_SUPPORT: {
                        if (v != null) {
                            if (v.getTribe() == Barbarians.getSingleton()) {
                                //empty village
                                return;
                            }
                        } else {
                            //no village
                            return;
                        }
                        VillageSupportFrame.getSingleton().setLocation(e.getPoint());
                        VillageSupportFrame.getSingleton().showSupportFrame(v);
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
                        if (v != null) {
                            ChurchManager.getSingleton().addChurch(v, Church.RANGE1);
                        }
                        break;
                    }
                    case ImageManager.CURSOR_CHURCH_2: {
                        if (v != null) {
                            ChurchManager.getSingleton().addChurch(v, Church.RANGE2);
                        }
                        break;
                    }
                    case ImageManager.CURSOR_CHURCH_3: {
                        if (v != null) {
                            ChurchManager.getSingleton().addChurch(v, Church.RANGE3);
                        }
                        break;
                    }
                    case ImageManager.CURSOR_REMOVE_CHURCH: {
                        if (v != null) {
                            ChurchManager.getSingleton().removeChurch(v);
                        }
                        break;
                    }
                    case ImageManager.CURSOR_NOTE: {
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

                /*
                 * if (MenuRenderer.getSingleton().isVisible()) { return; }
                 */
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
                        if (!shiftDown) {
                            markedVillages.clear();
                            DSWorkbenchSelectionFrame.getSingleton().resetView();
                        }
                        selectionRect = new de.tor.tribes.types.drawing.Rectangle();
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
                        selectionRect.setXPos(pos.x);
                        selectionRect.setYPos(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_MEASURE: {
                        //start drag if attack tool is active
                        mSourceVillage = getVillageAtMousePos();
                        if (mSourceVillage != null) {
                            getMapRenderer().setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
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
                    case ImageManager.CURSOR_DRAW_ARROW: {
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
                                getMapRenderer().setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    dragMove = false;
                    return;
                }

                dragMove = false;
                int unit = -1;
                xDir = 0;
                yDir = 0;
                boolean isAttack = false;
                int tmpCursor = (spaceDown) ? ImageManager.CURSOR_DEFAULT : iCurrentCursor;

                if ((tmpCursor == ImageManager.CURSOR_DRAW_LINE) || (tmpCursor == ImageManager.CURSOR_DRAW_ARROW) || (tmpCursor == ImageManager.CURSOR_DRAW_RECT) || (tmpCursor == ImageManager.CURSOR_DRAW_CIRCLE) || (tmpCursor == ImageManager.CURSOR_DRAW_TEXT) || (tmpCursor == ImageManager.CURSOR_DRAW_FREEFORM)) {
                    FormConfigFrame.getSingleton().purge();
                } else {
                    if ((tmpCursor == ImageManager.CURSOR_ATTACK_AXE) || (tmpCursor == ImageManager.CURSOR_ATTACK_SWORD) || (tmpCursor == ImageManager.CURSOR_ATTACK_SPY) || (tmpCursor == ImageManager.CURSOR_ATTACK_LIGHT) || (tmpCursor == ImageManager.CURSOR_ATTACK_HEAVY) || (tmpCursor == ImageManager.CURSOR_ATTACK_RAM) || (tmpCursor == ImageManager.CURSOR_ATTACK_SNOB)) {
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

                            //notify selection listener (see DSWorkbenchSelectionFrame)
                            if (!shiftDown) {
                                DSWorkbenchSelectionFrame.getSingleton().fireSelectionFinishedEvent(new Point(xs, ys), new Point(xe, ye));
                            }
                            List<Village> villages = DataHolder.getSingleton().getVillagesInRegion(new Point(xs, ys), new Point(xe, ye));
                            for (Village v : villages) {
                                if (!markedVillages.contains(v)) {
                                    markedVillages.add(v);
                                }
                            }
                            if (!markedVillages.isEmpty()) {
                                DSWorkbenchMainFrame.getSingleton().showInfo(((markedVillages.size() == 1) ? "Dorf " : markedVillages.size() + " Dörfer ") + "in die Auswahlübersicht übertragen");
                            }
                            selectionRect = null;
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
                getMapRenderer().setDragLine(-1, -1, -1, -1);
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

                boolean isAttack = false;
                if (!spaceDown) {
                    isAttack = isAttackCursor();
                }
                int tmpCursor = (spaceDown) ? ImageManager.CURSOR_DEFAULT : iCurrentCursor;

                switch (tmpCursor) {
                    case ImageManager.CURSOR_DEFAULT: {
                        if (isOutside) {
                            dragMove = false;
                            return;
                        }

                        Point location = MouseInfo.getPointerInfo().getLocation();
                        if ((mouseDownPoint == null) || (location == null)) {
                            dragMove = false;
                            break;
                        }
                        dragMove = true;
                        double dx = location.getX() - mouseDownPoint.getX();
                        double dy = location.getY() - mouseDownPoint.getY();
                        mouseDownPoint = location;

                        double w = GlobalOptions.getSkin().getCurrentFieldWidth();
                        final double h = GlobalOptions.getSkin().getCurrentFieldHeight();

                        fireScrollEvents(-dx / w, -dy / h);

                        break;
                    }
                    case ImageManager.CURSOR_SELECTION: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        if (selectionRect == null) {
                            return;
                        }
                        int xs = (int) Math.floor(selectionRect.getXPos());
                        int ys = (int) Math.floor(selectionRect.getYPos());
                        int xe = (int) Math.floor(selectionRect.getXPosEnd());
                        int ye = (int) Math.floor(selectionRect.getYPosEnd());

                        int cnt = DataHolder.getSingleton().countVisibleVillages(new Point(xs, ys), new Point(xe, ye));
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
                            getMapRenderer().setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                        }
                        mTargetVillage = getVillageAtMousePos();
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_LINE: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.drawing.Line) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(pos.x);
                        ((de.tor.tribes.types.drawing.Line) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_ARROW: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.drawing.Arrow) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(pos.x);
                        ((de.tor.tribes.types.drawing.Arrow) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_RECT: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.drawing.Rectangle) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(pos.x);
                        ((de.tor.tribes.types.drawing.Rectangle) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_CIRCLE: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.drawing.Circle) FormConfigFrame.getSingleton().getCurrentForm()).setXPosEnd(pos.x);
                        ((de.tor.tribes.types.drawing.Circle) FormConfigFrame.getSingleton().getCurrentForm()).setYPosEnd(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_TEXT: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        FormConfigFrame.getSingleton().getCurrentForm().setXPos(pos.x);
                        FormConfigFrame.getSingleton().getCurrentForm().setYPos(pos.y);
                        break;
                    }
                    case ImageManager.CURSOR_DRAW_FREEFORM: {
                        Point2D.Double pos = mouseToVirtualPos(e.getX(), e.getY());
                        ((de.tor.tribes.types.drawing.FreeForm) FormConfigFrame.getSingleton().getCurrentForm()).addPoint(pos);
                        break;
                    }
                    default: {
                        if (isAttack) {
                            if (mSourceVillage != null) {
                                getMapRenderer().setDragLine(mSourceVillage.getX(), mSourceVillage.getY(), e.getX(), e.getY());
                                mTargetVillage = getVillageAtMousePos();
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (isOutside) {
                    mousePos = e.getLocationOnScreen();
                }
            }
        });

        //<editor-fold>
    }

    public boolean isAttackCursor() {
        return ((iCurrentCursor == ImageManager.CURSOR_ATTACK_AXE) || (iCurrentCursor == ImageManager.CURSOR_ATTACK_SWORD) || (iCurrentCursor == ImageManager.CURSOR_ATTACK_SPY) || (iCurrentCursor == ImageManager.CURSOR_ATTACK_LIGHT) || (iCurrentCursor == ImageManager.CURSOR_ATTACK_HEAVY) || (iCurrentCursor == ImageManager.CURSOR_ATTACK_RAM) || (iCurrentCursor == ImageManager.CURSOR_ATTACK_SNOB));
    }

    public void resetServerDependendSettings() {
        radarVillage = null;
        markedVillages.clear();
        actionMenuVillage = null;
    }

    public boolean isMouseDown() {
        return mouseDown && iCurrentCursor != ImageManager.CURSOR_MEASURE;
    }

    public Village getToolSourceVillage() {
        return mSourceVillage;
    }

    public synchronized MapRenderer getMapRenderer() {
        if (mMapRenderer == null) {
            logger.info("Creating MapRenderer");
            mMapRenderer = new MapRenderer();
        }
        return mMapRenderer;
    }

    public Village getRadarVillage() {
        return radarVillage;
    }

    public AttackAddFrame getAttackAddFrame() {
        return attackAddFrame;
    }

    /**
     * Returns true as long as the mouse is outside the mappanel
     */
    protected boolean isOutside() {
        return isOutside;
    }

    /**
     * Get start village of drag operation
     */
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jVillageActionsMenu = new javax.swing.JPopupMenu();
        jTribeSubmenu = new javax.swing.JMenu();
        jCopyPlayerVillagesToClipboardItem = new javax.swing.JMenuItem();
        jCopyPlayerVillagesAsBBCodeToClipboardItem = new javax.swing.JMenuItem();
        jMonitorPlayerItem = new javax.swing.JMenuItem();
        jAllySubmenu = new javax.swing.JMenu();
        jMonitorAllyItem = new javax.swing.JMenuItem();
        jCurrentVillageSubmenu = new javax.swing.JMenu();
        jLastReport = new javax.swing.JMenuItem();
        jCurrentCoordToClipboardItem = new javax.swing.JMenuItem();
        jVillageInfoIngame = new javax.swing.JMenuItem();
        jVillagePlaceIngame = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jCurrentCreateNoteItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jCurrentToAttackPlanerAsTargetItem = new javax.swing.JMenuItem();
        jCurrentToAttackPlanerAsSourceItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jCurrentToAStarAsAttacker = new javax.swing.JMenuItem();
        jCurrentToAStarAsDefender = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        jCenterItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jCurrentCoordAsBBToClipboardItem = new javax.swing.JMenuItem();
        jMarkedVillageSubmenu = new javax.swing.JMenu();
        jAllCoordToClipboardItem = new javax.swing.JMenuItem();
        jAllCoordAsBBToClipboardItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jAllToAttackPlanerAsSourceItem = new javax.swing.JMenuItem();
        jAllToAttackPlanerAsTargetItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jAllCreateNoteItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        jCenterVillagesIngameItem = new javax.swing.JMenuItem();

        jTribeSubmenu.setText("Spieler");

        jCopyPlayerVillagesToClipboardItem.setText("Spielerdörfer in Zwischenablage kopieren");
        jCopyPlayerVillagesToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jTribeSubmenu.add(jCopyPlayerVillagesToClipboardItem);

        jCopyPlayerVillagesAsBBCodeToClipboardItem.setText("Spielerdörfer als BB-Code in Zwischenablage");
        jCopyPlayerVillagesAsBBCodeToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jTribeSubmenu.add(jCopyPlayerVillagesAsBBCodeToClipboardItem);

        jMonitorPlayerItem.setText("Spieler überwachen");
        jMonitorPlayerItem.setToolTipText("Fügt den Spieler zu den Statistiken hinzu");
        jMonitorPlayerItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jTribeSubmenu.add(jMonitorPlayerItem);

        jVillageActionsMenu.add(jTribeSubmenu);

        jAllySubmenu.setText("Stamm");

        jMonitorAllyItem.setText("Stamm überwachen");
        jMonitorAllyItem.setToolTipText("Fügt alle Spieler des Stammes zu den Statistiken hinzu");
        jMonitorAllyItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jAllySubmenu.add(jMonitorAllyItem);

        jVillageActionsMenu.add(jAllySubmenu);

        jCurrentVillageSubmenu.setText("Dieses Dorf");

        jLastReport.setText("Letzten Bericht anzeigen");
        jLastReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jLastReport);

        jCurrentCoordToClipboardItem.setText("Koordinaten in Zwischenablage");
        jCurrentCoordToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jCurrentCoordToClipboardItem);

        jVillageInfoIngame.setText("Im Spiel zentrieren");
        jVillageInfoIngame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jVillageInfoIngame);

        jVillagePlaceIngame.setText("Truppenübersicht im Spiel öffnen");
        jVillagePlaceIngame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jVillagePlaceIngame);
        jCurrentVillageSubmenu.add(jSeparator4);

        jCurrentCreateNoteItem.setText("Notiz erstellen");
        jCurrentCreateNoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jCurrentCreateNoteItem);
        jCurrentVillageSubmenu.add(jSeparator3);

        jCurrentToAttackPlanerAsTargetItem.setText("In Taktikplaner (Ziel)");
        jCurrentToAttackPlanerAsTargetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jCurrentToAttackPlanerAsTargetItem);

        jCurrentToAttackPlanerAsSourceItem.setText("In Taktikplaner (Herkunft)");
        jCurrentToAttackPlanerAsSourceItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jCurrentToAttackPlanerAsSourceItem);
        jCurrentVillageSubmenu.add(jSeparator2);

        jCurrentToAStarAsAttacker.setText("Truppen als Angreifer nach A*Star");
        jCurrentToAStarAsAttacker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jCurrentToAStarAsAttacker);

        jCurrentToAStarAsDefender.setText("Truppen als Verteidiger nach A*Star");
        jCurrentToAStarAsDefender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jCurrentToAStarAsDefender);
        jCurrentVillageSubmenu.add(jSeparator7);

        jCenterItem.setText("Auf der Karte zentrieren");
        jCenterItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jCenterItem);
        jCurrentVillageSubmenu.add(jSeparator1);

        jCurrentCoordAsBBToClipboardItem.setText("BB-Code in Zwischenablage");
        jCurrentCoordAsBBToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jCurrentVillageSubmenu.add(jCurrentCoordAsBBToClipboardItem);

        jVillageActionsMenu.add(jCurrentVillageSubmenu);

        jMarkedVillageSubmenu.setText("Markierte Dörfer");

        jAllCoordToClipboardItem.setText("Koordinaten in Zwischenablage");
        jAllCoordToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jMarkedVillageSubmenu.add(jAllCoordToClipboardItem);

        jAllCoordAsBBToClipboardItem.setText("BB-Code in Zwischenablage");
        jAllCoordAsBBToClipboardItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jMarkedVillageSubmenu.add(jAllCoordAsBBToClipboardItem);
        jMarkedVillageSubmenu.add(jSeparator5);

        jAllToAttackPlanerAsSourceItem.setText("In Taktikplaner (Herkunft)");
        jAllToAttackPlanerAsSourceItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jMarkedVillageSubmenu.add(jAllToAttackPlanerAsSourceItem);

        jAllToAttackPlanerAsTargetItem.setText("In Taktikplaner (Ziel)");
        jAllToAttackPlanerAsTargetItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jMarkedVillageSubmenu.add(jAllToAttackPlanerAsTargetItem);
        jMarkedVillageSubmenu.add(jSeparator6);

        jAllCreateNoteItem.setText("Notiz erstellen");
        jAllCreateNoteItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jMarkedVillageSubmenu.add(jAllCreateNoteItem);
        jMarkedVillageSubmenu.add(jSeparator8);

        jCenterVillagesIngameItem.setText("Im Spiel zentrieren (max. 10 Dörfer)");
        jCenterVillagesIngameItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillagePopupActionEvent(evt);
            }
        });
        jMarkedVillageSubmenu.add(jCenterVillagesIngameItem);

        jVillageActionsMenu.add(jMarkedVillageSubmenu);

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                fireResizeEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 548, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 410, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fireVillagePopupActionEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireVillagePopupActionEvent
        if (evt.getSource() == jCopyPlayerVillagesToClipboardItem) {
            Village v = actionMenuVillage;
            if (v != null) {
                if (v.getTribe() != Barbarians.getSingleton()) {
                    try {
                        StringBuilder builder = new StringBuilder();
                        Village[] list = v.getTribe().getVillageList();
                        Arrays.sort(list);
                        for (Village current : list) {
                            if (ServerSettings.getSingleton().getCoordType() != 2) {
                                int[] hier = DSCalculator.xyToHierarchical((int) current.getX(), (int) current.getY());
                                builder.append(hier[0]).append(":").append(hier[1]).append(":").append(hier[2]).append("\n");
                            } else {
                                builder.append(current.getX()).append("|").append(current.getY()).append("\n");
                            }
                        }
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
                        JOptionPaneHelper.showInformationBox(this, "Dörfer in die Zwischenablage kopiert", "Information");
                    } catch (Exception e) {
                        JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
                    }
                } else {
                    JOptionPaneHelper.showWarningBox(this, "Für Barbarendörfer nicht möglich", "Warnung");
                }
            }
        } else if (evt.getSource() == jCopyPlayerVillagesAsBBCodeToClipboardItem) {
            Village v = actionMenuVillage;
            if (v != null) {
                if (v.getTribe() != Barbarians.getSingleton()) {
                    try {
                        StringBuilder builder = new StringBuilder();
                        Village[] list = v.getTribe().getVillageList();
                        Arrays.sort(list);
                        for (Village current : list) {
                            builder.append(current.toBBCode()).append("\n");
                        }
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
                        JOptionPaneHelper.showInformationBox(this, "Dörfer in die Zwischenablage kopiert", "Information");
                    } catch (Exception e) {
                        JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
                    }
                } else {
                    JOptionPaneHelper.showWarningBox(this, "Für Barbarendörfer nicht möglich", "Warnung");
                }
            }
        } else if (evt.getSource() == jMonitorPlayerItem) {
            Village v = actionMenuVillage;
            if (v != null && v.getTribe() != Barbarians.getSingleton()) {
                StatManager.getSingleton().monitorTribe(v.getTribe());
                DSWorkbenchStatsFrame.getSingleton().resetView();
            }
        } else if (evt.getSource() == jMonitorAllyItem) {
            Village v = actionMenuVillage;
            if (v != null && v.getTribe() != Barbarians.getSingleton()) {
                Ally a = v.getTribe().getAlly();
                if (a == null) {
                    StatManager.getSingleton().monitorTribe(v.getTribe());
                } else {
                    StatManager.getSingleton().monitorAlly(a);
                }
                DSWorkbenchStatsFrame.getSingleton().resetView();
            }
        } else if (evt.getSource() == jCurrentCoordToClipboardItem) {
            //copy current village coordinates to clipboard
            Village v = actionMenuVillage;
            if (v != null) {
                try {
                    StringBuilder builder = new StringBuilder();
                    if (ServerSettings.getSingleton().getCoordType() != 2) {
                        int[] hier = DSCalculator.xyToHierarchical((int) v.getX(), (int) v.getY());
                        builder.append(hier[0]).append(":").append(hier[1]).append(":").append(hier[2]);
                    } else {
                        builder.append(v.getX()).append("|").append(v.getY());
                    }
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
                    JOptionPaneHelper.showInformationBox(this, "Koordinaten in die Zwischenablage kopiert", "Information");
                } catch (Exception e) {
                    JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
                }
            }
        } else if (evt.getSource() == jLastReport) {
            FightReport report = ReportManager.getSingleton().findLastReportForVillage(actionMenuVillage);
            if (report != null) {
                new ReportShowDialog(DSWorkbenchMainFrame.getSingleton(), false).setupAndShow(report);
            }
        } else if (evt.getSource() == jCurrentCoordAsBBToClipboardItem) {
            //copy current village as bb-code to clipboard
            Village v = actionMenuVillage;
            if (v != null) {
                try {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(v.toBBCode()), null);
                    JOptionPaneHelper.showInformationBox(this, "BB-Code in die Zwischenablage kopiert", "Information");
                } catch (Exception e) {
                    JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
                }
            }
        } else if (evt.getSource() == jCenterItem) {
            //center current village on map
            DSWorkbenchMainFrame.getSingleton().centerVillage(actionMenuVillage);
        } else if (evt.getSource() == jCurrentToAttackPlanerAsSourceItem) {
            Village v = actionMenuVillage;
            if (v != null) {
                if (v.getTribe() == Barbarians.getSingleton()) {
                    JOptionPaneHelper.showInformationBox(this,
                            "Angriffe von Barbarendörfern können nicht geplant werden.", "Information");
                    return;
                }
                AttackSourcePanel.getSingleton().addVillages(new Village[]{v});
                TacticsPlanerWizard.show();
                JOptionPaneHelper.showInformationBox(this, "Dorf in Angriffsplaner eingefügt", "Information");
            }
        } else if (evt.getSource() == jCurrentToAttackPlanerAsTargetItem) {
            Village v = actionMenuVillage;
            if (v != null) {
                if (v.getTribe() == Barbarians.getSingleton()) {
                    JOptionPaneHelper.showInformationBox(this, "Angriffe auf Barbarendörfer können nicht geplant werden.", "Information");
                    return;
                }

                AttackTargetPanel.getSingleton().addVillages(new Village[]{v});
                TacticsPlanerWizard.show();
                JOptionPaneHelper.showInformationBox(this, "Dorf in Angriffsplaner eingefügt", "Information");
            }
        } else if (evt.getSource() == jCurrentCreateNoteItem) {
            if (actionMenuVillage != null) {
                DSWorkbenchNotepad.getSingleton().addNoteForVillage(actionMenuVillage);
                JOptionPaneHelper.showInformationBox(this, "Notiz erstellt", "Information");
            }
        } else if (evt.getSource() == jCurrentToAStarAsAttacker || evt.getSource() == jCurrentToAStarAsDefender) {
            VillageTroopsHolder own = TroopsManager.getSingleton().getTroopsForVillage(actionMenuVillage, TroopsManager.TROOP_TYPE.OWN);

            Hashtable<String, Double> values = new Hashtable<>();
            if (evt.getSource() == jCurrentToAStarAsAttacker && own == null) {
                JOptionPaneHelper.showInformationBox(this, "Keine Truppeninformationen (Eigene) vorhanden", "Information");
                return;
            }

            VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(actionMenuVillage, TroopsManager.TROOP_TYPE.IN_VILLAGE);
            if (evt.getSource() == jCurrentToAStarAsDefender && inVillage == null) {
                JOptionPaneHelper.showInformationBox(this, "Keine Truppeninformationen (im Dorf) vorhanden", "Information");
                return;
            }

            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                if (evt.getSource() == jCurrentToAStarAsAttacker) {
                    values.put("att_" + unit.getPlainName(), (double) own.getTroopsOfUnitInVillage(unit));
                }
                if (evt.getSource() == jCurrentToAStarAsDefender) {
                    values.put("def_" + unit.getPlainName(), (double) inVillage.getTroopsOfUnitInVillage(unit));
                }
            }
            if (!GlobalOptions.isOfflineMode()) {
                if (!DSWorkbenchSimulatorFrame.getSingleton().isVisible()) {
                    DSWorkbenchSimulatorFrame.getSingleton().setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                    DSWorkbenchSimulatorFrame.getSingleton().showIntegratedVersion(DSWorkbenchSettingsDialog.getSingleton().getWebProxy(), GlobalOptions.getSelectedServer());
                }
                DSWorkbenchSimulatorFrame.getSingleton().toFront();
                DSWorkbenchSimulatorFrame.getSingleton().insertValuesExternally(values);
            } else {
                JOptionPaneHelper.showInformationBox(this, "A*Star ist im Offline-Modus leider nicht verfügbar.", "Information");
            }
        } else if (evt.getSource() == jVillageInfoIngame) {
            //center village ingame
            if (actionMenuVillage != null) {
                BrowserCommandSender.centerVillage(actionMenuVillage);
            }
        } else if (evt.getSource() == jVillagePlaceIngame) {
            //open place ingame
            if (actionMenuVillage != null) {
                BrowserCommandSender.openPlaceTroopsView(actionMenuVillage);
            }
        } else if (evt.getSource() == jAllCoordToClipboardItem) {
            //copy selected villages coordinates to clipboard
            if (markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dörfer markiert.", "Information");
                return;
            }
            try {
                StringBuilder builder = new StringBuilder();
                for (Village v : markedVillages) {
                    if (ServerSettings.getSingleton().getCoordType() != 2) {
                        int[] hier = DSCalculator.xyToHierarchical((int) v.getX(), (int) v.getY());
                        builder.append(hier[0]).append(":").append(hier[1]).append(":").append(hier[2]).append("\n");
                    } else {
                        builder.append(v.getX()).append("|").append(v.getY()).append("\n");
                    }
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
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
                StringBuilder builder = new StringBuilder();
                for (Village v : markedVillages) {
                    builder.append(v.toBBCode()).append("\n");
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
                JOptionPaneHelper.showInformationBox(this, "BB-Code in die Zwischenablage kopiert", "Information");
            } catch (Exception e) {
                JOptionPaneHelper.showErrorBox(this, "Fehler beim Kopieren in die Zwischenablage", "Fehler");
            }
        } else if (evt.getSource() == jAllToAttackPlanerAsSourceItem) {
            if (markedVillages == null || markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this,
                        "Keine Dörfer gewählt.", "Information");
                return;
            }
            AttackSourcePanel.getSingleton().addVillages(markedVillages.toArray(new Village[]{}));
            TacticsPlanerWizard.show();
            JOptionPaneHelper.showInformationBox(this, "Dörfer in Angriffsplaner eingefügt", "Information");
        } else if (evt.getSource() == jAllToAttackPlanerAsTargetItem) {
            if (markedVillages == null || markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this,
                        "Keine Dörfer gewählt.", "Information");
                return;
            }
            AttackTargetPanel.getSingleton().addVillages(markedVillages.toArray(new Village[]{}));
            TacticsPlanerWizard.show();
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
        } else if (evt.getSource() == jCenterVillagesIngameItem) {
            if (markedVillages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Keine Dörfer markiert.", "Information");
                return;
            }
            int cnt = 0;
            for (Village v : markedVillages) {
                if (v != null) {
                    BrowserCommandSender.centerVillage(v);
                    cnt++;
                }
                if (cnt == 10) {
                    //allow max 10 villages
                    return;
                }
            }
        }
    }//GEN-LAST:event_fireVillagePopupActionEvent

    private void fireResizeEvent(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_fireResizeEvent
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
    }//GEN-LAST:event_fireResizeEvent

    @Override
    public void paintComponent(Graphics g) {
        /*
          Draw buffer into panel
         */
        try {
            //calculate move direction if mouse is dragged outside the map
            if ((isOutside) && (mouseDown) && (iCurrentCursor != ImageManager.CURSOR_DEFAULT)) {
                mousePos = MouseInfo.getPointerInfo().getLocation();

                int outcodes = mapBounds.outcode(mousePos);
                if ((outcodes & Rectangle2D.OUT_LEFT) != 0) {
                    xDir -= 1;
                } else if ((outcodes & Rectangle2D.OUT_RIGHT) != 0) {
                    xDir += 1;
                }

                if ((outcodes & Rectangle2D.OUT_TOP) != 0) {
                    yDir -= 1;
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
            AffineTransform t0 = g2d.getTransform();
            Color c0 = g2d.getColor();
            Paint p0 = g2d.getPaint();
            Shape cl0 = g2d.getClip();
            Stroke s0 = g2d.getStroke();
            getMapRenderer().renderAll(g2d);
            g2d.setTransform(t0);
            g2d.setPaint(p0);
            g2d.setColor(c0);
            g2d.setClip(cl0);
            g2d.setStroke(s0);
        } catch (Exception e) {
            logger.error("Failed to paint", e);
        }
    }

    /**
     * Update map to new position -> needs fully update
     */
    public synchronized void updateMapPosition(double pX, double pY, boolean pZoomed) {
        dCenterX = pX;
        dCenterY = pY;

        positionUpdate = true;
        if (pZoomed) {
            getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
        } else {
            getMapRenderer().initiateRedraw(MapRenderer.MAP_LAYER);
        }
    }

    /**
     * Update map to new position -> needs fully update
     */
    public synchronized void updateMapPosition(double pX, double pY) {
        updateMapPosition(pX, pY, false);
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
        double x = mVirtualBounds.getX() + (pX / width);
        double y = mVirtualBounds.getY() + (pY / height);
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
        return (Rectangle2D) mVirtualBounds.clone();
    }

    /**
     * Get village at current mouse position, null if there is no village
     */
    public Village getVillageAtMousePos() {
        if (mVillagePositions == null) {
            return null;
        }

        try {
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            mouse.x -= getLocationOnScreen().x;
            mouse.y -= getLocationOnScreen().y;

            for (Village current : mVillagePositions.keySet()) {
                if (current != null && mVillagePositions.get(current).contains(mouse.x, mouse.y)) {
                    if (current.isVisibleOnMap()) {
                        return current;
                    }
                }
            }

        } catch (Exception e) {
            //failed getting village (probably getting mousepos failed)
        }

        return null;
    }

    public Village getVillageAtPoint(Point pPos) {
        if (mVillagePositions == null) {
            return null;
        }

        try {

            for (Village current : mVillagePositions.keySet()) {
                if (mVillagePositions.get(current).contains(pPos)) {
                    return current;
                }
            }
        } catch (Exception e) {
            //failed getting village (probably getting mousepos failed)
        }

        return null;
    }

    public List<Village> getVillagesInShape(Shape pShape) {
        if (mVillagePositions == null) {
            return null;
        }
        try {
            List<Village> result = new ArrayList<>();

            for (Village currentVillage : mVillagePositions.keySet()) {
                Rectangle current = mVillagePositions.get(currentVillage);
                if (pShape.intersects(current)) {
                    result.add(currentVillage);
                }
            }
            return result;
        } catch (Exception e) {
            //failed getting village (probably getting mousepos failed)
        }
        return null;
    }

    public List<Village> getVillagesOnLine(Line2D.Double pShape) {
        if (mVillagePositions == null) {
            return null;
        }
        try {
            List<Village> result = new ArrayList<>();

            for (Village currentVillage : mVillagePositions.keySet()) {
                Rectangle current = mVillagePositions.get(currentVillage);
                if (current.intersectsLine(pShape)) {
                    result.add(currentVillage);
                }
            }
            return result;
        } catch (Exception e) {
            //failed getting village (probably getting mousepos failed)
        }

        return null;
    }

    /**
     * Update operation perfomed by the RepaintThread was completed
     */
    public void updateComplete(final HashMap<Village, Rectangle> pPositions, final BufferedImage pBuffer) {
        mVillagePositions = (HashMap<Village, Rectangle>) pPositions.clone();
        if (bMapSHotPlaned) {
            mScreenSaver.planMapShot(mMapShotFile, sMapShotType, pBuffer);
            bMapSHotPlaned = false;
        }
        if (positionUpdate) {
            DSWorkbenchFormFrame.getSingleton().updateVisibility();
        }
        positionUpdate = false;
    }

    public boolean requiresAlphaBlending() {
        return (mouseDown && iCurrentCursor == ImageManager.CURSOR_DEFAULT);
    }

    public void planMapShot(String pType, File pLocation, MapShotListener pListener) {
        sMapShotType = pType;
        mMapShotFile = pLocation;
        bMapSHotPlaned = true;
        mMapShotListener = pListener;
    }

    public synchronized void fireToolChangedEvents(int pTool) {
        for (ToolChangeListener listener : mToolChangeListeners) {
            listener.fireToolChangedEvent(pTool);
        }
    }

    public synchronized void fireScrollEvents(double pX, double pY) {
        for (MapPanelListener listener : mMapPanelListeners) {
            listener.fireScrollEvent(pX, pY);
        }
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        if (iCurrentCursor != ImageManager.CURSOR_DEFAULT) {
            return;
        }

        Village v = getVillageAtMousePos();
        if (v == null) {
            return;
        }
        Cursor c = null;
        if (!markedVillages.isEmpty()) {
            c = ImageManager.createVillageDragCursor(markedVillages.size());
            setCursor(c);
            dge.startDrag(c, new VillageTransferable(markedVillages), this);
        } else {
            c = ImageManager.createVillageDragCursor(1);
            setCursor(c);
            dge.startDrag(c, new VillageTransferable(v), this);
        }
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        setCurrentCursor(iCurrentCursor);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor) || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (dtde.isDataFlavorSupported(VillageTransferable.villageDataFlavor) || dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            dtde.rejectDrop();
            return;
        }

        Transferable t = dtde.getTransferable();
        List<Village> v;
        setCurrentCursor(iCurrentCursor);
        try {
            v = (List<Village>) t.getTransferData(VillageTransferable.villageDataFlavor);
            Village target = getVillageAtMousePos();
            if (target == null) {
                return;
            }
            attackAddFrame.setupAttack(v, target, DataHolder.getSingleton().getUnitID("Ramme"), null);
        } catch (Exception ex) {
            logger.error("Failed to drop villages", ex);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jAllCoordAsBBToClipboardItem;
    private javax.swing.JMenuItem jAllCoordToClipboardItem;
    private javax.swing.JMenuItem jAllCreateNoteItem;
    private javax.swing.JMenuItem jAllToAttackPlanerAsSourceItem;
    private javax.swing.JMenuItem jAllToAttackPlanerAsTargetItem;
    private javax.swing.JMenu jAllySubmenu;
    private javax.swing.JMenuItem jCenterItem;
    private javax.swing.JMenuItem jCenterVillagesIngameItem;
    private javax.swing.JMenuItem jCopyPlayerVillagesAsBBCodeToClipboardItem;
    private javax.swing.JMenuItem jCopyPlayerVillagesToClipboardItem;
    private javax.swing.JMenuItem jCurrentCoordAsBBToClipboardItem;
    private javax.swing.JMenuItem jCurrentCoordToClipboardItem;
    private javax.swing.JMenuItem jCurrentCreateNoteItem;
    private javax.swing.JMenuItem jCurrentToAStarAsAttacker;
    private javax.swing.JMenuItem jCurrentToAStarAsDefender;
    private javax.swing.JMenuItem jCurrentToAttackPlanerAsSourceItem;
    private javax.swing.JMenuItem jCurrentToAttackPlanerAsTargetItem;
    private javax.swing.JMenu jCurrentVillageSubmenu;
    private javax.swing.JMenuItem jLastReport;
    private javax.swing.JMenu jMarkedVillageSubmenu;
    private javax.swing.JMenuItem jMonitorAllyItem;
    private javax.swing.JMenuItem jMonitorPlayerItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JMenu jTribeSubmenu;
    private javax.swing.JPopupMenu jVillageActionsMenu;
    private javax.swing.JMenuItem jVillageInfoIngame;
    private javax.swing.JMenuItem jVillagePlaceIngame;
    // End of variables declaration//GEN-END:variables
}
