/*
 * MapFrame.java
 *
 * Created on 4. September 2007, 18:07
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.ClipboardWatch;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSWorkbenchFrameListener;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ToolChangeListener;
import de.tor.tribes.util.tag.TagManager;
import java.awt.AWTEvent;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import de.tor.tribes.types.Tag;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.MapShotListener;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.map.FormManager;
import de.tor.tribes.util.mark.MarkerManager;
import java.io.File;
import javax.swing.JFileChooser;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.Component;
import java.io.FileWriter;
import java.util.Enumeration;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * @TODO (1.3) Implement question dialog when importing groups with existing group names
 * @TODO (1.3) Change browser access to work with UV
 * @author  Charon
 */
public class DSWorkbenchMainFrame extends javax.swing.JFrame implements
        MapPanelListener,
        MinimapListener,
        ToolChangeListener,
        DSWorkbenchFrameListener,
        MapShotListener {

    private static Logger logger = Logger.getLogger("MainApp");
    private double dCenterX = 500.0;
    private double dCenterY = 500.0;
    private List<ImageIcon> mIcons;
    private double dZoomFactor = 1.0;
    private TribeTribeAttackFrame mTribeTribeAttackFrame = null;
    private AboutDialog mAbout = null;
    private static DSWorkbenchMainFrame SINGLETON = null;
    private boolean initialized = false;
    private JFrame fullscreenFrame = null;

    public static synchronized DSWorkbenchMainFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchMainFrame();
        }
        return SINGLETON;
    }

    /** Creates new form MapFrame */
    DSWorkbenchMainFrame() {
        initComponents();
        setTitle("DS Workbench " + Constants.VERSION + Constants.VERSION_ADDITION);
        jMassAttackItem.setVisible(false);
        jExportDialog.pack();
        // <editor-fold defaultstate="collapsed" desc=" Register ShutdownHook ">

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                logger.info("Performing ShutdownHook");
                GlobalOptions.saveUserData();
                GlobalOptions.addProperty("attack.frame.visible", Boolean.toString(DSWorkbenchAttackFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("marker.frame.visible", Boolean.toString(DSWorkbenchMarkerFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("troops.frame.visible", Boolean.toString(DSWorkbenchTroopsFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("rank.frame.visible", Boolean.toString(DSWorkbenchRankFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("form.frame.visible", Boolean.toString(DSWorkbenchFormFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("search.frame.visible", Boolean.toString(DSWorkbenchSearchFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("attack.frame.alwaysOnTop", Boolean.toString(DSWorkbenchAttackFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("marker.frame.alwaysOnTop", Boolean.toString(DSWorkbenchMarkerFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("troops.frame.alwaysOnTop", Boolean.toString(DSWorkbenchTroopsFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("rank.frame.alwaysOnTop", Boolean.toString(DSWorkbenchRankFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("form.frame.alwaysOnTop", Boolean.toString(DSWorkbenchFormFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("form.config.frame.alwaysOnTop", Boolean.toString(FormConfigFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("search.frame.alwaysOnTop", Boolean.toString(DSWorkbenchSearchFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("zoom.factor", Double.toString(dZoomFactor));
                GlobalOptions.addProperty("last.x", jCenterX.getText());
                GlobalOptions.addProperty("last.y", jCenterY.getText());
                logger.debug("Saving global properties");
                GlobalOptions.saveProperties();
                logger.debug("Shutdown finished");
            }
        }));

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" General UI setup ">

        getContentPane().setBackground(Constants.DS_BACK);
        pack();

        // </editor-fold>        

        // <editor-fold defaultstate="collapsed" desc=" Add global KeyListener ">

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            @Override
            public void eventDispatched(AWTEvent event) {
                if (((KeyEvent) event).getID() == KeyEvent.KEY_PRESSED) {
                    KeyEvent e = (KeyEvent) event;
                    if (DSWorkbenchMainFrame.getSingleton().isActive() || fullscreenFrame != null) {
                        //move shortcuts
                        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                            scroll(0.0, 2.0);
                        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                            scroll(0.0, -2.0);
                        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            scroll(-2.0, 0.0);
                        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            scroll(2.0, 0.0);
                        } else if ((e.getKeyCode() == KeyEvent.VK_1) && e.isShiftDown()) {
                            //shot minimap tool shortcut
                            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_AXE);
                        } else if ((e.getKeyCode() == KeyEvent.VK_2) && e.isShiftDown()) {
                            //attack axe tool shortcut
                            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_RAM);
                        } else if ((e.getKeyCode() == KeyEvent.VK_3) && e.isShiftDown()) {
                            //attack ram tool shortcut
                            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SNOB);
                        } else if ((e.getKeyCode() == KeyEvent.VK_4) && e.isShiftDown()) {
                            //attack snob tool shortcut
                            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SPY);
                        } else if ((e.getKeyCode() == KeyEvent.VK_5) && e.isShiftDown()) {
                            //attack sword tool shortcut
                            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
                        } else if ((e.getKeyCode() == KeyEvent.VK_6) && e.isShiftDown()) {
                            //attack light tool shortcut
                            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
                        } else if ((e.getKeyCode() == KeyEvent.VK_7) && e.isShiftDown()) {
                            //attack heavy tool shortcut
                            MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
                        } else if ((e.getKeyCode() == KeyEvent.VK_S) && e.isControlDown()) {
                            //search frame shortcut
                            DSWorkbenchSearchFrame.getSingleton().setVisible(!DSWorkbenchSearchFrame.getSingleton().isVisible());
                        }
                    }

                    //misc shortcuts
                    if ((e.getKeyCode() == KeyEvent.VK_0) && e.isAltDown()) {
                        //no tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
                    } else if ((e.getKeyCode() == KeyEvent.VK_1) && e.isAltDown()) {
                        //measure tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MEASURE);
                    } else if ((e.getKeyCode() == KeyEvent.VK_2) && e.isAltDown()) {
                        //mark tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MARK);
                    } else if ((e.getKeyCode() == KeyEvent.VK_3) && e.isAltDown()) {
                        //tag tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_TAG);
                    } else if ((e.getKeyCode() == KeyEvent.VK_4) && e.isAltDown()) {
                        //attack ingame tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SUPPORT);
                    } else if ((e.getKeyCode() == KeyEvent.VK_5) && e.isAltDown()) {
                        //attack ingame tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
                    } else if ((e.getKeyCode() == KeyEvent.VK_6) && e.isAltDown()) {
                        //attack ingame tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_RADAR);
                    } else if ((e.getKeyCode() == KeyEvent.VK_7) && e.isAltDown()) {
                        //attack ingame tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_INGAME);
                    } else if ((e.getKeyCode() == KeyEvent.VK_8) && e.isAltDown()) {
                        //res ingame tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SEND_RES_INGAME);
                    } else if ((e.getKeyCode() == KeyEvent.VK_1) && e.isControlDown()) {
                        //move minimap tool shortcut
                        MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MOVE);
                    } else if ((e.getKeyCode() == KeyEvent.VK_2) && e.isControlDown()) {
                        //zoom minimap tool shortcut
                        MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ZOOM);
                    } else if ((e.getKeyCode() == KeyEvent.VK_3) && e.isControlDown()) {
                        //shot minimap tool shortcut
                        MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SHOT);
                    } else if ((e.getKeyCode() == KeyEvent.VK_T) && e.isControlDown()) {
                        //search time shortcut
                        ClockFrame.getSingleton().setVisible(!ClockFrame.getSingleton().isVisible());
                    } else if ((e.getKeyCode() == KeyEvent.VK_F) && e.isControlDown()) {
                        if (fullscreenFrame == null) {
                            jPanel1.remove(MapPanel.getSingleton());
                            fullscreenFrame = new JFrame();
                            fullscreenFrame.add(MapPanel.getSingleton());
                            Dimension fullscreen = Toolkit.getDefaultToolkit().getScreenSize();
                            fullscreenFrame.setSize(fullscreen);
                            fullscreenFrame.setUndecorated(true);
                            fullscreenFrame.setVisible(true);
                        } else {
                            fullscreenFrame.remove(MapPanel.getSingleton());
                            jPanel1.add(MapPanel.getSingleton());
                            jPanel1.updateUI();
                            MapPanel.getSingleton().getMapRenderer().initiateRedraw(0);
                            fullscreenFrame.dispose();
                            fullscreenFrame = null;
                        }

                        if (ServerSettings.getSingleton().getCoordType() != 2) {
                            int[] hier = DSCalculator.hierarchicalToXy(Integer.parseInt(jCenterX.getText()), Integer.parseInt(jCenterY.getText()), 12);
                            if (hier != null) {
                                MapPanel.getSingleton().updateMapPosition(hier[0], hier[1]);
                            }
                        } else {
                            MapPanel.getSingleton().updateMapPosition(Integer.parseInt(jCenterX.getText()), Integer.parseInt(jCenterY.getText()));
                        }
                    } else if ((e.getKeyCode() == KeyEvent.VK_F) && e.isAltDown()) {
                        DSWorkbenchMarkerFrame.getSingleton().firePublicDrawMarkedOnlyChangedEvent();
                    } else if ((e.getKeyCode() == KeyEvent.VK_M) && e.isAltDown()) {
                        switchMarkOnTop();
                    }  else if ((e.getKeyCode() == KeyEvent.VK_S) && e.isAltDown()) {
                        fireCreateMapShotEvent(null);
                    }else if (e.getKeyCode() == KeyEvent.VK_F2) {
                        DSWorkbenchAttackFrame.getSingleton().setVisible(!DSWorkbenchAttackFrame.getSingleton().isVisible());
                    } else if (e.getKeyCode() == KeyEvent.VK_F3) {
                        DSWorkbenchMarkerFrame.getSingleton().setVisible(!DSWorkbenchMarkerFrame.getSingleton().isVisible());
                    } else if (e.getKeyCode() == KeyEvent.VK_F4) {
                        DSWorkbenchTroopsFrame.getSingleton().setVisible(!DSWorkbenchTroopsFrame.getSingleton().isVisible());
                    } else if (e.getKeyCode() == KeyEvent.VK_F5) {
                        DSWorkbenchRankFrame.getSingleton().setVisible(!DSWorkbenchRankFrame.getSingleton().isVisible());
                    } else if (e.getKeyCode() == KeyEvent.VK_F6) {
                        DSWorkbenchFormFrame.getSingleton().setVisible(!DSWorkbenchFormFrame.getSingleton().isVisible());
                    } else if (e.getKeyCode() == KeyEvent.VK_F12) {
                        DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Load UI Icons ">

        try {
            jOnlineLabel.setIcon(new ImageIcon("./graphics/icons/online.png"));
            jCenterIngameButton.setIcon(new ImageIcon("./graphics/icons/center.png"));
            jRefreshButton.setIcon(new ImageIcon("./graphics/icons/refresh.png"));
            jCenterCoordinateIngame.setIcon(new ImageIcon("./graphics/icons/center.png"));
        } catch (Exception e) {
            logger.error("Failed to load status icon(s)", e);
        }
// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Check for desktop support ">
        if (!Desktop.isDesktopSupported()) {
            jCenterIngameButton.setEnabled(false);
            jCenterCoordinateIngame.setEnabled(false);
        }
// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Restore last map position ">
        try {
            String x = GlobalOptions.getProperty("last.x");
            String y = GlobalOptions.getProperty("last.y");
            dCenterX = Double.parseDouble(x);
            dCenterY = Double.parseDouble(y);
            jCenterX.setText(x);
            jCenterY.setText(y);
        } catch (Exception e) {
            if (ServerSettings.getSingleton().getCoordType() != 2) {
                dCenterX = 250.0;
                dCenterY = 250.0;
                int[] hier = DSCalculator.xyToHierarchical(250, 250);
                jCenterX.setText(Integer.toString(hier[0]));
                jCenterY.setText(Integer.toString(hier[1]));
            } else {
                dCenterX = 500.0;
                dCenterY = 500.0;
                jCenterX.setText("500");
                jCenterY.setText("500");
            }
        }

// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Restore other settings ">
        try {
            String val = GlobalOptions.getProperty("show.map.popup");
            if (val == null) {
                jShowMapPopup.setSelected(true);
                GlobalOptions.addProperty("show.map.popup", Boolean.toString(true));
            } else {
                jShowMapPopup.setSelected(Boolean.parseBoolean(val));
            }
        } catch (Exception e) {
            jShowMapPopup.setSelected(true);
            GlobalOptions.addProperty("show.map.popup", Boolean.toString(true));
        }

        try {
            String val = GlobalOptions.getProperty("mark.on.top");
            if (val == null) {
                jMarkOnTopBox.setSelected(false);
                GlobalOptions.addProperty("mark.on.top", Boolean.toString(false));
            } else {
                jMarkOnTopBox.setSelected(Boolean.parseBoolean(val));
            }
        } catch (Exception e) {
            jMarkOnTopBox.setSelected(false);
            GlobalOptions.addProperty("mark.on.top", Boolean.toString(false));
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Setup WindowListeners ">
        WindowListener frameListener = new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (e.getSource() == DSWorkbenchAttackFrame.getSingleton()) {
                    fireShowAttackFrameEvent(null);
                } else if (e.getSource() == DSWorkbenchMarkerFrame.getSingleton()) {
                    fireShowMarkerFrameEvent(null);
                } else if (e.getSource() == DSWorkbenchTroopsFrame.getSingleton()) {
                    fireShowTroopsFrameEvent(null);
                } else if (e.getSource() == DSWorkbenchRankFrame.getSingleton()) {
                    fireShowRangFrameEvent(null);
                } else if (e.getSource() == DSWorkbenchFormFrame.getSingleton()) {
                    fireShowFormsFrameEvent(null);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        };
        DSWorkbenchAttackFrame.getSingleton().addWindowListener(frameListener);

        // </editor-fold>        

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "index", GlobalOptions.getHelpBroker().getHelpSet());
        jHelpItem.addActionListener(GlobalOptions.getHelpDisplay());
        // </editor-fold>

        //update online state
        onlineStateChanged();

    }

    /**Update on server change*/
    public void serverSettingsChangedEvent() {
        logger.info("Updating server settings");
        String playerID = GlobalOptions.getProperty("player." + GlobalOptions.getSelectedServer()) + "@" + GlobalOptions.getSelectedServer();
        jCurrentPlayer.setText(playerID);

        Tribe t = DataHolder.getSingleton().getTribeByName(GlobalOptions.getProperty("player." + GlobalOptions.getSelectedServer()));
        if (t != null) {
            Village[] villages = t.getVillageList().toArray(new Village[]{});
            Arrays.sort(villages);
            jCurrentPlayerVillages.setModel(new DefaultComboBoxModel(villages));
            if ((villages != null && villages.length > 0)) {
                centerVillage(villages[0]);
            }
        } else {
            DefaultComboBoxModel model = new DefaultComboBoxModel(new Object[]{"Keine Dörfer"});
            jCurrentPlayerVillages.setModel(model);
        }
        //update views
        MinimapPanel.getSingleton().redraw();
        MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
        DSWorkbenchMarkerFrame.getSingleton().setupMarkerPanel();
        DSWorkbenchAttackFrame.getSingleton().setupAttackPanel();
        //update troops table and troops view
        TroopsManagerTableModel.getSingleton().setup();
        DSWorkbenchTroopsFrame.getSingleton().setupTroopsPanel();
        //update attack planner
        if (mTribeTribeAttackFrame != null) {
            mTribeTribeAttackFrame.setup();
        }

        DSWorkbenchSettingsDialog.getSingleton().setupAttackColorTable();
        DSWorkbenchRankFrame.getSingleton().updateAllyList();
        DSWorkbenchRankFrame.getSingleton().updateRankTable();
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            jLabel1.setText("K");
            jLabel2.setText("S");
        } else {
            jLabel1.setText("X");
            jLabel2.setText("Y");
        }
        logger.info("Server settings updated");
    }

    /**Update UI depending on online state*/
    public void onlineStateChanged() {
        jOnlineLabel.setEnabled(!GlobalOptions.isOfflineMode());
        if (GlobalOptions.isOfflineMode()) {
            jOnlineLabel.setToolTipText("Offline");
        } else {
            jOnlineLabel.setToolTipText("Online");
        }
    }

    /**Get current zoom factor*/
    public synchronized double getZoomFactor() {
        return dZoomFactor;
    }

    /**Called at startup*/
    protected void init() {
        logger.info("Starting initialization");
        //setup everything
        serverSettingsChangedEvent();

        logger.info(" * Setting up maps");
        setupMaps();

        logger.info(" * Setting up details panel");
        setupDetailsPanel();

        logger.info(" * Setting up views");
        setupFrames();
        //setup toolbox

        /* logger.info(" * Setup toolbox");
        mToolbox = new ToolBoxFrame();
        mToolbox.addWindowListener(new WindowListener() {

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
        jShowToolboxItem.setSelected(false);
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }
        });
         */

        fireToolChangedEvent(ImageManager.CURSOR_DEFAULT);
        logger.info(" * Setting up attack planner");
        //setup frames
        /*mAllyAllyAttackFrame = new AllyAllyAttackFrame();
        mAllyAllyAttackFrame.pack();*/
        mTribeTribeAttackFrame = new TribeTribeAttackFrame();
        mTribeTribeAttackFrame.pack();
        mAbout = new AboutDialog(this, true);
        logger.info("Initialization finished");
        initialized = true;
    }

    protected boolean isInitialized() {
        return initialized;
    }

    /**Setup of all frames*/
    private void setupFrames() {
        DSWorkbenchAttackFrame.getSingleton().addFrameListener(this);
        DSWorkbenchMarkerFrame.getSingleton().addFrameListener(this);
        TroopsManagerTableModel.getSingleton().setup();
        DSWorkbenchTroopsFrame.getSingleton().addFrameListener(this);
        DSWorkbenchRankFrame.getSingleton().addFrameListener(this);
        DSWorkbenchFormFrame.getSingleton().addFrameListener(this);
    }

    /**Setup main map and mini map*/
    private void setupMaps() {
        try {
            dZoomFactor = Double.parseDouble(GlobalOptions.getProperty("zoom.factor"));
            checkZoomRange();
        } catch (Exception e) {
            dZoomFactor = 1.0;
        }
//build the map panel

        logger.info("Adding MapListener");
        MapPanel.getSingleton().addMapPanelListener(this);
        MapPanel.getSingleton().addToolChangeListener(this);
        MinimapPanel.getSingleton().addToolChangeListener(this);
        logger.info("Adding MapPanel");
        jPanel1.add(MapPanel.getSingleton());
        //build the minimap

        MinimapPanel.getSingleton().addMinimapListener(this);
        logger.info("Adding MinimapPanel");
        /*MinimapPanel.getSingleton().setMinimumSize(jMinimapPanel.getMinimumSize());
        MapPanel.getSingleton().setMinimumSize(jPanel1.getMinimumSize());*/
        jMinimapPanel.add(MinimapPanel.getSingleton());
    }

    /**Setup village details panel*/
    private void setupDetailsPanel() {
        //load icons for bonus villages at information panel
        mIcons = new LinkedList<ImageIcon>();
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/forbidden.gif")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/holz.png")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/lehm.png")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/eisen.png")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/face.png")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/barracks.png")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/stable.png")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/smith.png")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/res.png")));
        mIcons.add(new ImageIcon(this.getClass().getResource("/res/speicher.png")));

    }

    @Override
    public void setVisible(boolean v) {
        logger.info("Setting MainWindow visible");
        super.setVisible(v);
        if (v) {
            //only if set to visible
            MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);

            double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
            double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
            MinimapPanel.getSingleton().setSelection((int) Math.floor(dCenterX), (int) Math.floor(dCenterY), (int) Math.rint(w), (int) Math.rint(h));

            // <editor-fold defaultstate="collapsed" desc=" Check frames and toolbar visibility ">

            try {
                if (Boolean.parseBoolean(GlobalOptions.getProperty("attack.frame.visible"))) {
                    jShowAttackFrame.setSelected(true);
                    logger.info("Restoring attack frame");
                    DSWorkbenchAttackFrame.getSingleton().setVisible(true);
                }
            } catch (Exception e) {
            }

            try {
                if (Boolean.parseBoolean(GlobalOptions.getProperty("marker.frame.visible"))) {
                    jShowMarkerFrame.setSelected(true);
                    logger.info("Restoring marker frame");
                    DSWorkbenchMarkerFrame.getSingleton().setVisible(true);
                }

            } catch (Exception e) {
            }

            try {
                if (Boolean.parseBoolean(GlobalOptions.getProperty("troops.frame.visible"))) {
                    jShowTroopsFrame.setSelected(true);
                    logger.info("Restoring troops frame");
                    DSWorkbenchTroopsFrame.getSingleton().setVisible(true);
                }
            } catch (Exception e) {
            }

            try {
                if (Boolean.parseBoolean(GlobalOptions.getProperty("rank.frame.visible"))) {
                    jShowRankFrame.setSelected(true);
                    logger.info("Restoring rank frame");
                    DSWorkbenchRankFrame.getSingleton().setVisible(true);
                }

            } catch (Exception e) {
            }

            try {
                if (Boolean.parseBoolean(GlobalOptions.getProperty("form.frame.visible"))) {
                    jShowFormsFrame.setSelected(true);
                    logger.info("Restoring form frame");
                    DSWorkbenchFormFrame.getSingleton().setVisible(true);
                }

            } catch (Exception e) {
            }

            try {
                if (Boolean.parseBoolean(GlobalOptions.getProperty("search.frame.visible"))) {
                    logger.info("Restoring search frame");
                    DSWorkbenchSearchFrame.getSingleton().setVisible(true);
                }

            } catch (Exception e) {
            }
            // </editor-fold>

            //start ClipboardWatch
            ClipboardWatch.getSingleton();
            //draw map the first time
            fireRefreshMapEvent(null);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMapShotDialog = new javax.swing.JDialog();
        jLabel3 = new javax.swing.JLabel();
        jFileTypeChooser = new javax.swing.JComboBox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jExportDialog = new javax.swing.JDialog();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttackExportTable = new javax.swing.JTable();
        jExportMarks = new javax.swing.JCheckBox();
        jExportTags = new javax.swing.JCheckBox();
        jExportTroops = new javax.swing.JCheckBox();
        jExportForms = new javax.swing.JCheckBox();
        jExportButton = new javax.swing.JButton();
        jCancelExportButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jNavigationPanel = new javax.swing.JPanel();
        jMoveE = new javax.swing.JButton();
        jMoveNE = new javax.swing.JButton();
        jMoveN = new javax.swing.JButton();
        jMoveNW = new javax.swing.JButton();
        jMoveW = new javax.swing.JButton();
        jMoveSW = new javax.swing.JButton();
        jMoveS = new javax.swing.JButton();
        jMoveSE = new javax.swing.JButton();
        jCenterX = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCenterY = new javax.swing.JTextField();
        jRefreshButton = new javax.swing.JButton();
        jMoveE1 = new javax.swing.JButton();
        jZoomInButton = new javax.swing.JButton();
        jZoomOutButton = new javax.swing.JButton();
        jCenterCoordinateIngame = new javax.swing.JButton();
        jMinimapPanel = new javax.swing.JPanel();
        jInformationPanel = new javax.swing.JPanel();
        jCurrentPlayerVillages = new javax.swing.JComboBox();
        jCurrentPlayer = new javax.swing.JLabel();
        jCenterIngameButton = new javax.swing.JButton();
        jOnlineLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jCurrentToolLabel = new javax.swing.JLabel();
        jShowMapPopup = new javax.swing.JCheckBox();
        jMarkOnTopBox = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jSearchItem = new javax.swing.JMenuItem();
        jClockItem = new javax.swing.JMenuItem();
        jTribeTribeAttackItem = new javax.swing.JMenuItem();
        jMassAttackItem = new javax.swing.JMenuItem();
        jUnitOverviewItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jShowAttackFrame = new javax.swing.JCheckBoxMenuItem();
        jShowMarkerFrame = new javax.swing.JCheckBoxMenuItem();
        jShowTroopsFrame = new javax.swing.JCheckBoxMenuItem();
        jShowRankFrame = new javax.swing.JCheckBoxMenuItem();
        jShowFormsFrame = new javax.swing.JCheckBoxMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jHelpItem = new javax.swing.JMenuItem();
        jAboutItem = new javax.swing.JMenuItem();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jLabel3.setText(bundle.getString("DSWorkbenchMainFrame.jLabel3.text")); // NOI18N

        jFileTypeChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "png", "gif", "jpg", "bmp" }));
        jFileTypeChooser.setMaximumSize(new java.awt.Dimension(80, 22));
        jFileTypeChooser.setMinimumSize(new java.awt.Dimension(80, 22));
        jFileTypeChooser.setPreferredSize(new java.awt.Dimension(80, 22));

        jButton2.setText(bundle.getString("DSWorkbenchMainFrame.jButton2.text")); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCreateMapShotEvent(evt);
            }
        });

        jButton3.setText(bundle.getString("DSWorkbenchMainFrame.jButton3.text")); // NOI18N
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelMapShotEvent(evt);
            }
        });

        javax.swing.GroupLayout jMapShotDialogLayout = new javax.swing.GroupLayout(jMapShotDialog.getContentPane());
        jMapShotDialog.getContentPane().setLayout(jMapShotDialogLayout);
        jMapShotDialogLayout.setHorizontalGroup(
            jMapShotDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMapShotDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMapShotDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jMapShotDialogLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jFileTypeChooser, 0, 123, Short.MAX_VALUE))
                    .addGroup(jMapShotDialogLayout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        jMapShotDialogLayout.setVerticalGroup(
            jMapShotDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMapShotDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMapShotDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jFileTypeChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jMapShotDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jExportDialog.setTitle(bundle.getString("DSWorkbenchMainFrame.jExportDialog.title")); // NOI18N
        jExportDialog.setAlwaysOnTop(true);

        jScrollPane1.setOpaque(false);

        jAttackExportTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Angriffplan", "Exportieren"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jAttackExportTable.setOpaque(false);
        jScrollPane1.setViewportView(jAttackExportTable);

        jExportMarks.setText(bundle.getString("DSWorkbenchMainFrame.jExportMarks.text")); // NOI18N
        jExportMarks.setOpaque(false);

        jExportTags.setText(bundle.getString("DSWorkbenchMainFrame.jExportTags.text")); // NOI18N
        jExportTags.setOpaque(false);

        jExportTroops.setText(bundle.getString("DSWorkbenchMainFrame.jExportTroops.text")); // NOI18N
        jExportTroops.setOpaque(false);

        jExportForms.setText(bundle.getString("DSWorkbenchMainFrame.jExportForms.text")); // NOI18N
        jExportForms.setOpaque(false);

        jExportButton.setText(bundle.getString("DSWorkbenchMainFrame.jExportButton.text")); // NOI18N
        jExportButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportEvent(evt);
            }
        });

        jCancelExportButton.setText(bundle.getString("DSWorkbenchMainFrame.jCancelExportButton.text")); // NOI18N
        jCancelExportButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportEvent(evt);
            }
        });

        javax.swing.GroupLayout jExportDialogLayout = new javax.swing.GroupLayout(jExportDialog.getContentPane());
        jExportDialog.getContentPane().setLayout(jExportDialogLayout);
        jExportDialogLayout.setHorizontalGroup(
            jExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jExportDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .addGroup(jExportDialogLayout.createSequentialGroup()
                        .addGroup(jExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jExportMarks)
                            .addComponent(jExportTags))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                        .addGroup(jExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jExportForms)
                            .addComponent(jExportTroops)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jExportDialogLayout.createSequentialGroup()
                        .addComponent(jCancelExportButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jExportButton)))
                .addContainerGap())
        );
        jExportDialogLayout.setVerticalGroup(
            jExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jExportDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jExportMarks)
                    .addComponent(jExportTroops))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jExportTags)
                    .addComponent(jExportForms))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jExportDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jExportButton)
                    .addComponent(jCancelExportButton))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(bundle.getString("DSWorkbenchMainFrame.title")); // NOI18N
        setBackground(new java.awt.Color(225, 213, 190));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                fireFrameResizedEvent(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jNavigationPanel.setBackground(new java.awt.Color(239, 235, 223));
        jNavigationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2), bundle.getString("DSWorkbenchMainFrame.jNavigationPanel.border.title"))); // NOI18N

        jMoveE.setBackground(new java.awt.Color(239, 235, 223));
        jMoveE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_e.png"))); // NOI18N
        jMoveE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveNE.setBackground(new java.awt.Color(239, 235, 223));
        jMoveNE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_ne.png"))); // NOI18N
        jMoveNE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveNE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveNE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveNE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveN.setBackground(new java.awt.Color(239, 235, 223));
        jMoveN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_n.png"))); // NOI18N
        jMoveN.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveN.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveN.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveNW.setBackground(new java.awt.Color(239, 235, 223));
        jMoveNW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_nw.png"))); // NOI18N
        jMoveNW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveNW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveNW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveNW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveW.setBackground(new java.awt.Color(239, 235, 223));
        jMoveW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_w.png"))); // NOI18N
        jMoveW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveSW.setBackground(new java.awt.Color(239, 235, 223));
        jMoveSW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_sw.png"))); // NOI18N
        jMoveSW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveSW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveSW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveSW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveS.setBackground(new java.awt.Color(239, 235, 223));
        jMoveS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_s.png"))); // NOI18N
        jMoveS.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveS.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveS.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveSE.setBackground(new java.awt.Color(239, 235, 223));
        jMoveSE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_se.png"))); // NOI18N
        jMoveSE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveSE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveSE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveSE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jCenterX.setText(bundle.getString("DSWorkbenchMainFrame.jCenterX.text")); // NOI18N
        jCenterX.setMaximumSize(new java.awt.Dimension(40, 20));
        jCenterX.setMinimumSize(new java.awt.Dimension(40, 20));
        jCenterX.setPreferredSize(new java.awt.Dimension(40, 20));

        jLabel1.setText(bundle.getString("DSWorkbenchMainFrame.jLabel1.text")); // NOI18N

        jLabel2.setText(bundle.getString("DSWorkbenchMainFrame.jLabel2.text")); // NOI18N

        jCenterY.setText(bundle.getString("DSWorkbenchMainFrame.jCenterY.text")); // NOI18N
        jCenterY.setMaximumSize(new java.awt.Dimension(40, 20));
        jCenterY.setMinimumSize(new java.awt.Dimension(40, 20));
        jCenterY.setPreferredSize(new java.awt.Dimension(40, 20));

        jRefreshButton.setBackground(new java.awt.Color(239, 235, 223));
        jRefreshButton.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jRefreshButton.toolTipText")); // NOI18N
        jRefreshButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jRefreshButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jRefreshButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jRefreshButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRefreshMapEvent(evt);
            }
        });

        jMoveE1.setBackground(new java.awt.Color(239, 235, 223));
        jMoveE1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jMoveE1.setEnabled(false);
        jMoveE1.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveE1.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveE1.setPreferredSize(new java.awt.Dimension(21, 21));

        jZoomInButton.setBackground(new java.awt.Color(239, 235, 223));
        jZoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/zoom_out.png"))); // NOI18N
        jZoomInButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jZoomInButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jZoomInButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jZoomInButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireZoomEvent(evt);
            }
        });

        jZoomOutButton.setBackground(new java.awt.Color(239, 235, 223));
        jZoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/zoom_in.png"))); // NOI18N
        jZoomOutButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jZoomOutButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jZoomOutButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jZoomOutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireZoomEvent(evt);
            }
        });

        jCenterCoordinateIngame.setBackground(new java.awt.Color(239, 235, 223));
        jCenterCoordinateIngame.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jCenterCoordinateIngame.toolTipText")); // NOI18N
        jCenterCoordinateIngame.setMaximumSize(new java.awt.Dimension(30, 30));
        jCenterCoordinateIngame.setMinimumSize(new java.awt.Dimension(30, 30));
        jCenterCoordinateIngame.setPreferredSize(new java.awt.Dimension(30, 30));
        jCenterCoordinateIngame.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterCurrentPosInGameEvent(evt);
            }
        });

        javax.swing.GroupLayout jNavigationPanelLayout = new javax.swing.GroupLayout(jNavigationPanel);
        jNavigationPanel.setLayout(jNavigationPanelLayout);
        jNavigationPanelLayout.setHorizontalGroup(
            jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jNavigationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jNavigationPanelLayout.createSequentialGroup()
                                .addComponent(jMoveNW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMoveN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jNavigationPanelLayout.createSequentialGroup()
                                .addComponent(jMoveW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMoveE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jMoveNE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addComponent(jMoveSW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMoveS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMoveSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jZoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jZoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCenterX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCenterY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRefreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCenterCoordinateIngame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jNavigationPanelLayout.setVerticalGroup(
            jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jNavigationPanelLayout.createSequentialGroup()
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jMoveNE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveNW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jMoveE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jMoveSW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCenterX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jCenterY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jNavigationPanelLayout.createSequentialGroup()
                            .addComponent(jRefreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jCenterCoordinateIngame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jNavigationPanelLayout.createSequentialGroup()
                            .addComponent(jZoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jZoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jMinimapPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2));
        jMinimapPanel.setLayout(new java.awt.BorderLayout());

        jInformationPanel.setBackground(new java.awt.Color(239, 235, 223));
        jInformationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2), bundle.getString("DSWorkbenchMainFrame.jInformationPanel.border.title"))); // NOI18N

        jCurrentPlayerVillages.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jCurrentPlayerVillages.toolTipText")); // NOI18N
        jCurrentPlayerVillages.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                fireCurrentPlayerVillagePopupEvent(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        jCurrentPlayer.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jCurrentPlayer.setMaximumSize(new java.awt.Dimension(155, 14));
        jCurrentPlayer.setMinimumSize(new java.awt.Dimension(155, 14));
        jCurrentPlayer.setPreferredSize(new java.awt.Dimension(155, 14));

        jCenterIngameButton.setBackground(new java.awt.Color(239, 235, 223));
        jCenterIngameButton.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jCenterIngameButton.toolTipText")); // NOI18N
        jCenterIngameButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jCenterIngameButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jCenterIngameButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jCenterIngameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterVillageIngameEvent(evt);
            }
        });

        jOnlineLabel.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jOnlineLabel.toolTipText")); // NOI18N
        jOnlineLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jOnlineLabel.setMaximumSize(new java.awt.Dimension(30, 30));
        jOnlineLabel.setMinimumSize(new java.awt.Dimension(30, 30));
        jOnlineLabel.setPreferredSize(new java.awt.Dimension(30, 30));

        javax.swing.GroupLayout jInformationPanelLayout = new javax.swing.GroupLayout(jInformationPanel);
        jInformationPanel.setLayout(jInformationPanelLayout);
        jInformationPanelLayout.setHorizontalGroup(
            jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCurrentPlayer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .addComponent(jCurrentPlayerVillages, javax.swing.GroupLayout.Alignment.LEADING, 0, 248, Short.MAX_VALUE)
                    .addGroup(jInformationPanelLayout.createSequentialGroup()
                        .addComponent(jCenterIngameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOnlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jInformationPanelLayout.setVerticalGroup(
            jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCurrentPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCurrentPlayerVillages, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jOnlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCenterIngameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(239, 235, 223));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2), bundle.getString("DSWorkbenchMainFrame.jPanel2.border.title"))); // NOI18N

        jCurrentToolLabel.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jCurrentToolLabel.toolTipText")); // NOI18N
        jCurrentToolLabel.setAlignmentY(0.0F);
        jCurrentToolLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jCurrentToolLabel.setIconTextGap(0);
        jCurrentToolLabel.setMaximumSize(new java.awt.Dimension(35, 35));
        jCurrentToolLabel.setMinimumSize(new java.awt.Dimension(35, 35));
        jCurrentToolLabel.setPreferredSize(new java.awt.Dimension(35, 35));

        jShowMapPopup.setText(bundle.getString("DSWorkbenchMainFrame.jShowMapPopup.text")); // NOI18N
        jShowMapPopup.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jShowMapPopup.toolTipText")); // NOI18N
        jShowMapPopup.setOpaque(false);
        jShowMapPopup.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireShowMapPopupChangedEvent(evt);
            }
        });

        jMarkOnTopBox.setText(bundle.getString("DSWorkbenchMainFrame.jMarkOnTopBox.text")); // NOI18N
        jMarkOnTopBox.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jMarkOnTopBox.toolTipText")); // NOI18N
        jMarkOnTopBox.setOpaque(false);
        jMarkOnTopBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireMarkOnTopChangedEvent(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/camera.png"))); // NOI18N
        jButton1.setText(bundle.getString("DSWorkbenchMainFrame.jButton1.text")); // NOI18N
        jButton1.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jButton1.toolTipText")); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(35, 35));
        jButton1.setMinimumSize(new java.awt.Dimension(35, 35));
        jButton1.setPreferredSize(new java.awt.Dimension(35, 35));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCreateMapShotEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap(178, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCurrentToolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jMarkOnTopBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jShowMapPopup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jShowMapPopup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jMarkOnTopBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCurrentToolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jMenuBar1.setBackground(new java.awt.Color(225, 213, 190));

        jMenu1.setBackground(new java.awt.Color(225, 213, 190));
        jMenu1.setMnemonic('a');
        jMenu1.setText(bundle.getString("DSWorkbenchMainFrame.jMenu1.text")); // NOI18N

        jMenuItem1.setBackground(new java.awt.Color(239, 235, 223));
        jMenuItem1.setMnemonic('t');
        jMenuItem1.setText(bundle.getString("DSWorkbenchMainFrame.jMenuItem1.text")); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowSettingsEvent(evt);
            }
        });
        jMenu1.add(jMenuItem1);
        jMenu1.add(jSeparator2);

        jMenuItem3.setBackground(new java.awt.Color(239, 235, 223));
        jMenuItem3.setText(bundle.getString("DSWorkbenchMainFrame.jMenuItem3.text")); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowImportDialogEvent(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem4.setBackground(new java.awt.Color(239, 235, 223));
        jMenuItem4.setText(bundle.getString("DSWorkbenchMainFrame.jMenuItem4.text")); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireOpenExportDialogEvent(evt);
            }
        });
        jMenu1.add(jMenuItem4);
        jMenu1.add(jSeparator1);

        jMenuItem2.setBackground(new java.awt.Color(239, 235, 223));
        jMenuItem2.setMnemonic('n');
        jMenuItem2.setText(bundle.getString("DSWorkbenchMainFrame.jMenuItem2.text")); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireExitEvent(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu3.setBackground(new java.awt.Color(225, 213, 190));
        jMenu3.setMnemonic('e');
        jMenu3.setText(bundle.getString("DSWorkbenchMainFrame.jMenu3.text")); // NOI18N

        jSearchItem.setBackground(new java.awt.Color(239, 235, 223));
        jSearchItem.setMnemonic('s');
        jSearchItem.setText(bundle.getString("DSWorkbenchMainFrame.jSearchItem.text")); // NOI18N
        jSearchItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireToolsActionEvent(evt);
            }
        });
        jMenu3.add(jSearchItem);

        jClockItem.setBackground(new java.awt.Color(239, 235, 223));
        jClockItem.setText(bundle.getString("DSWorkbenchMainFrame.jClockItem.text")); // NOI18N
        jClockItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireToolsActionEvent(evt);
            }
        });
        jMenu3.add(jClockItem);

        jTribeTribeAttackItem.setBackground(new java.awt.Color(239, 235, 223));
        jTribeTribeAttackItem.setText(bundle.getString("DSWorkbenchMainFrame.jTribeTribeAttackItem.text")); // NOI18N
        jTribeTribeAttackItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireToolsActionEvent(evt);
            }
        });
        jMenu3.add(jTribeTribeAttackItem);

        jMassAttackItem.setBackground(new java.awt.Color(239, 235, 223));
        jMassAttackItem.setText(bundle.getString("DSWorkbenchMainFrame.jMassAttackItem.text")); // NOI18N
        jMassAttackItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireToolsActionEvent(evt);
            }
        });
        jMenu3.add(jMassAttackItem);

        jUnitOverviewItem.setBackground(new java.awt.Color(239, 235, 223));
        jUnitOverviewItem.setText(bundle.getString("DSWorkbenchMainFrame.jUnitOverviewItem.text")); // NOI18N
        jUnitOverviewItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireToolsActionEvent(evt);
            }
        });
        jMenu3.add(jUnitOverviewItem);

        jMenuBar1.add(jMenu3);

        jMenu2.setBackground(new java.awt.Color(225, 213, 190));
        jMenu2.setMnemonic('n');
        jMenu2.setText(bundle.getString("DSWorkbenchMainFrame.jMenu2.text")); // NOI18N

        jShowAttackFrame.setBackground(new java.awt.Color(239, 235, 223));
        jShowAttackFrame.setText(bundle.getString("DSWorkbenchMainFrame.jShowAttackFrame.text")); // NOI18N
        jShowAttackFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowAttackFrameEvent(evt);
            }
        });
        jMenu2.add(jShowAttackFrame);

        jShowMarkerFrame.setBackground(new java.awt.Color(239, 235, 223));
        jShowMarkerFrame.setText(bundle.getString("DSWorkbenchMainFrame.jShowMarkerFrame.text")); // NOI18N
        jShowMarkerFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowMarkerFrameEvent(evt);
            }
        });
        jMenu2.add(jShowMarkerFrame);

        jShowTroopsFrame.setBackground(new java.awt.Color(239, 235, 223));
        jShowTroopsFrame.setText(bundle.getString("DSWorkbenchMainFrame.jShowTroopsFrame.text")); // NOI18N
        jShowTroopsFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowTroopsFrameEvent(evt);
            }
        });
        jMenu2.add(jShowTroopsFrame);

        jShowRankFrame.setBackground(new java.awt.Color(239, 235, 223));
        jShowRankFrame.setText(bundle.getString("DSWorkbenchMainFrame.jShowRankFrame.text")); // NOI18N
        jShowRankFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowRangFrameEvent(evt);
            }
        });
        jMenu2.add(jShowRankFrame);

        jShowFormsFrame.setBackground(new java.awt.Color(239, 235, 223));
        jShowFormsFrame.setText(bundle.getString("DSWorkbenchMainFrame.jShowFormsFrame.text")); // NOI18N
        jShowFormsFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowFormsFrameEvent(evt);
            }
        });
        jMenu2.add(jShowFormsFrame);

        jMenuBar1.add(jMenu2);

        jMenu4.setBackground(new java.awt.Color(225, 213, 190));
        jMenu4.setText(bundle.getString("DSWorkbenchMainFrame.jMenu4.text")); // NOI18N

        jHelpItem.setBackground(new java.awt.Color(239, 235, 223));
        jHelpItem.setText(bundle.getString("DSWorkbenchMainFrame.jHelpItem.text")); // NOI18N
        jMenu4.add(jHelpItem);

        jAboutItem.setBackground(new java.awt.Color(239, 235, 223));
        jAboutItem.setText(bundle.getString("DSWorkbenchMainFrame.jAboutItem.text")); // NOI18N
        jAboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowAboutEvent(evt);
            }
        });
        jMenu4.add(jAboutItem);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jInformationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jMinimapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .addComponent(jNavigationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jMinimapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jNavigationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jInformationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**Update map position*/
private void fireRefreshMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRefreshMapEvent
    double cx = dCenterX;
    double cy = dCenterY;
    try {
        cx = Integer.parseInt(jCenterX.getText());
        cy = Integer.parseInt(jCenterY.getText());
    } catch (Exception e) {
        cx = dCenterX;
        cy = dCenterY;
        jCenterX.setText(Integer.toString((int) cx));
        jCenterY.setText(Integer.toString((int) cy));
    }

    if (ServerSettings.getSingleton().getCoordType() != 2) {
        int[] hier = DSCalculator.hierarchicalToXy((int) cx, (int) cy, 12);
        if (hier != null) {
            dCenterX = hier[0];
            dCenterY = hier[1];
        }
    } else {
        dCenterX = cx;
        dCenterY = cy;
    }
    double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
    double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    MinimapPanel.getSingleton().setSelection((int) Math.floor(dCenterX), (int) Math.floor(dCenterY), (int) Math.rint(w), (int) Math.rint(h));
    MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
}//GEN-LAST:event_fireRefreshMapEvent

    /**Update map movement*/
private void fireMoveMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveMapEvent
    double cx = dCenterX;
    double cy = dCenterY;
    try {
        cx = Integer.parseInt(jCenterX.getText());
        cy = Integer.parseInt(jCenterY.getText());
    } catch (Exception e) {
        cx = dCenterX;
        cy = dCenterY;
    }

    if (ServerSettings.getSingleton().getCoordType() != 2) {
        int[] hier = DSCalculator.hierarchicalToXy((int) cx, (int) cy, 12);
        if (hier != null) {
            cx = hier[0];
            cy = hier[1];
        }
    }

    if (evt.getSource() == jMoveN) {
        cy -= (double) MapPanel.getSingleton().getHeight() / (double) GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveNE) {
        cx += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveE) {
        cx += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveSE) {
        cx += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveS) {
        cy += (double) MapPanel.getSingleton().getHeight() / (double) GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveSW) {
        cx -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy += (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveW) {
        cx -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveNW) {
        cx -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy -= (double) MapPanel.getSingleton().getWidth() / (double) GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    }

    if (ServerSettings.getSingleton().getCoordType() != 2) {
        int[] hier = DSCalculator.xyToHierarchical((int) cx, (int) cy);
        if (hier != null) {
            cx = hier[0];
            cy = hier[1];
        }
    }

    jCenterX.setText(Integer.toString((int) Math.floor(cx)));
    jCenterY.setText(Integer.toString((int) Math.floor(cy)));
    dCenterX = cx;
    dCenterY = cy;
    MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
    double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
    double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    MinimapPanel.getSingleton().setSelection((int) Math.floor(cx), (int) Math.floor(cy), (int) Math.rint(w), (int) Math.rint(h));
}//GEN-LAST:event_fireMoveMapEvent

    /**React on resize events*/
private void fireFrameResizedEvent(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_fireFrameResizedEvent
    try {
        MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
    } catch (Exception e) {
        logger.error("Failed to resize map for (" + dCenterX + ", " + dCenterY + ")", e);
    }
}//GEN-LAST:event_fireFrameResizedEvent

    /**Zoom main map*/
private void fireZoomEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireZoomEvent
    if (evt.getSource() == jZoomInButton) {
        zoomIn();
    } else {
        zoomOut();
    }
}//GEN-LAST:event_fireZoomEvent

    protected synchronized void zoomIn() {
        dZoomFactor += 1.0 / 10.0;
        checkZoomRange();

        dZoomFactor = Double.parseDouble(NumberFormat.getInstance().format(dZoomFactor).replaceAll(",", "."));

        double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        int xPos = Integer.parseInt(jCenterX.getText());
        int yPos = Integer.parseInt(jCenterY.getText());
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] hier = DSCalculator.hierarchicalToXy((int) xPos, (int) yPos, 12);
            if (hier != null) {
                xPos = hier[0];
                yPos = hier[1];
            }
        }
        MinimapPanel.getSingleton().setSelection(xPos, yPos, (int) Math.rint(w), (int) Math.rint(h));
        MapPanel.getSingleton().updateMapPosition(xPos, yPos);
    }

    protected synchronized void zoomOut() {
        dZoomFactor -= 1.0 / 10.0;
        checkZoomRange();

        dZoomFactor = Double.parseDouble(NumberFormat.getInstance().format(dZoomFactor).replaceAll(",", "."));

        double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        int xPos = Integer.parseInt(jCenterX.getText());
        int yPos = Integer.parseInt(jCenterY.getText());

        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] hier = DSCalculator.hierarchicalToXy((int) xPos, (int) yPos, 12);
            if (hier != null) {
                xPos = hier[0];
                yPos = hier[1];
            }
        }

        MinimapPanel.getSingleton().setSelection(xPos, yPos, (int) Math.rint(w), (int) Math.rint(h));
        MapPanel.getSingleton().updateMapPosition(xPos, yPos);
    }

    /**Change active player village*/
    /**Show settings dialog*/
private void fireShowSettingsEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowSettingsEvent
    DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
}//GEN-LAST:event_fireShowSettingsEvent

    /**Exit the application*/
private void fireExitEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireExitEvent
    GlobalOptions.saveProperties();
    System.exit(0);
}//GEN-LAST:event_fireExitEvent

    /**Show the toolbar*/
    /**Center village Ingame*/
private void fireCenterVillageIngameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterVillageIngameEvent
    if (!jCenterIngameButton.isEnabled()) {
        return;
    }

    Village v = (Village) jCurrentPlayerVillages.getSelectedItem();
    if (v != null) {
        BrowserCommandSender.centerVillage(v);
    }
}//GEN-LAST:event_fireCenterVillageIngameEvent

    /**Center pos Ingame*/
private void fireCenterCurrentPosInGameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterCurrentPosInGameEvent
    if (!jCenterCoordinateIngame.isEnabled()) {
        return;
    }
    BrowserCommandSender.centerCoordinate(Integer.parseInt(jCenterX.getText()), Integer.parseInt(jCenterY.getText()));
}//GEN-LAST:event_fireCenterCurrentPosInGameEvent

    /**Do tool action*/
private void fireToolsActionEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireToolsActionEvent
    if (evt.getSource() == jSearchItem) {
        DSWorkbenchSearchFrame.getSingleton().setVisible(true);
    } else if (evt.getSource() == jClockItem) {
        ClockFrame.getSingleton().setVisible(true);
    } else if (evt.getSource() == jTribeTribeAttackItem) {
        mTribeTribeAttackFrame.setup();
        mTribeTribeAttackFrame.setVisible(true);
    } else if (evt.getSource() == jMassAttackItem) {
        // mAllyAllyAttackFrame.setVisible(true);
    } else if (evt.getSource() == jUnitOverviewItem) {
        UnitOrderBuilder.showUnitOrder(null, null);
    }
}//GEN-LAST:event_fireToolsActionEvent

private void fireShowAboutEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowAboutEvent
    mAbout.setVisible(true);
}//GEN-LAST:event_fireShowAboutEvent

private void fireShowAttackFrameEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowAttackFrameEvent
    DSWorkbenchAttackFrame.getSingleton().setVisible(!DSWorkbenchAttackFrame.getSingleton().isVisible());
    jShowAttackFrame.setSelected(DSWorkbenchAttackFrame.getSingleton().isVisible());
}//GEN-LAST:event_fireShowAttackFrameEvent

private void fireShowMarkerFrameEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowMarkerFrameEvent
    DSWorkbenchMarkerFrame.getSingleton().setVisible(!DSWorkbenchMarkerFrame.getSingleton().isVisible());
    jShowMarkerFrame.setSelected(DSWorkbenchMarkerFrame.getSingleton().isVisible());
}//GEN-LAST:event_fireShowMarkerFrameEvent

private void fireShowTroopsFrameEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowTroopsFrameEvent
    DSWorkbenchTroopsFrame.getSingleton().setVisible(!DSWorkbenchTroopsFrame.getSingleton().isVisible());
    jShowTroopsFrame.setSelected(DSWorkbenchTroopsFrame.getSingleton().isVisible());
}//GEN-LAST:event_fireShowTroopsFrameEvent

private void fireCurrentPlayerVillagePopupEvent(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_fireCurrentPlayerVillagePopupEvent
    if (jCurrentPlayerVillages.getSelectedIndex() < 0) {
        return;
    }
    centerVillage((Village) jCurrentPlayerVillages.getSelectedItem());
}//GEN-LAST:event_fireCurrentPlayerVillagePopupEvent

private void fireShowRangFrameEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowRangFrameEvent
    DSWorkbenchRankFrame.getSingleton().setVisible(!DSWorkbenchRankFrame.getSingleton().isVisible());
    jShowRankFrame.setSelected(DSWorkbenchRankFrame.getSingleton().isVisible());
}//GEN-LAST:event_fireShowRangFrameEvent

private void fireShowFormsFrameEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowFormsFrameEvent
    DSWorkbenchFormFrame.getSingleton().setVisible(!DSWorkbenchFormFrame.getSingleton().isVisible());
    jShowFormsFrame.setSelected(DSWorkbenchFormFrame.getSingleton().isVisible());
}//GEN-LAST:event_fireShowFormsFrameEvent

private void fireShowMapPopupChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireShowMapPopupChangedEvent
    GlobalOptions.addProperty("show.map.popup", Boolean.toString(jShowMapPopup.isSelected()));
}//GEN-LAST:event_fireShowMapPopupChangedEvent

private void fireMarkOnTopChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireMarkOnTopChangedEvent
    GlobalOptions.addProperty("mark.on.top", Boolean.toString(jMarkOnTopBox.isSelected()));
}//GEN-LAST:event_fireMarkOnTopChangedEvent

private void fireCreateMapShotEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateMapShotEvent
    String dir = GlobalOptions.getProperty("screen.dir");
    if (dir == null) {
        dir = ".";
    }
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setDialogTitle("Speichern unter...");
    chooser.setSelectedFile(new File("map"));

    final String type = (String) jFileTypeChooser.getSelectedItem();
    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

        @Override
        public boolean accept(File f) {
            if ((f != null) && (f.isDirectory() || f.getName().endsWith(type))) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "*." + type;
        }
    });
    int ret = chooser.showSaveDialog(jMapShotDialog);
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
                UIManager.put("OptionPane.noButtonText", "Nein");
                UIManager.put("OptionPane.yesButtonText", "Ja");
                if (JOptionPane.showConfirmDialog(jMapShotDialog, "Existierende Datei überschreiben?", "Überschreiben", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
                    UIManager.put("OptionPane.noButtonText", "No");
                    UIManager.put("OptionPane.yesButtonText", "Yes");
                    return;
                }
                UIManager.put("OptionPane.noButtonText", "No");
                UIManager.put("OptionPane.yesButtonText", "Yes");
            }
            MapPanel.getSingleton().planMapShot(type, target, this);
            GlobalOptions.addProperty("screen.dir", target.getParent());
        } catch (Exception e) {
            logger.error("Failed to write map shot", e);
        }
    }
    jMapShotDialog.setVisible(false);
}//GEN-LAST:event_fireCreateMapShotEvent

private void fireCancelMapShotEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelMapShotEvent
    jMapShotDialog.setVisible(false);
}//GEN-LAST:event_fireCancelMapShotEvent

private void fireShowImportDialogEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowImportDialogEvent
    String dir = GlobalOptions.getProperty("screen.dir");
    if (dir == null) {
        dir = ".";
    }
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setDialogTitle("Datei auswählen");

    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

        @Override
        public boolean accept(File f) {
            if ((f != null) && (f.isDirectory() || f.getName().endsWith(".xml"))) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "*.xml";
        }
    });
    int ret = chooser.showOpenDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
        try {
            File f = chooser.getSelectedFile();
            String file = f.getCanonicalPath();
            if (!file.endsWith(".xml")) {
                file += ".xml";
            }
            File target = new File(file);
            if (target.exists()) {
                //do import
                boolean attackImported = AttackManager.getSingleton().importAttacks(target);
                boolean markersImported = MarkerManager.getSingleton().importMarkers(target);
                boolean tagImported = TagManager.getSingleton().importTags(target);
                boolean troopsImported = TroopsManager.getSingleton().importTroops(target);
                boolean formsImported = FormManager.getSingleton().importForms(target);

                String message = "Import beendet.\n";
                if (!attackImported) {
                    message += "  * Fehler beim Import der Angriffe\n";
                }
                if (!markersImported) {
                    message += "  * Fehler beim Import der Markierungen\n";
                }
                if (!tagImported) {
                    message += "  * Fehler beim Import der Tags\n";
                }
                if (!troopsImported) {
                    message += "  * Fehler beim Import der Truppen\n";
                }
                if (!formsImported) {
                    message += "  * Fehler beim Import der Formen\n";
                }

                JOptionPane.showMessageDialog(this, message, "Import", JOptionPane.INFORMATION_MESSAGE);
            }
            GlobalOptions.addProperty("screen.dir", target.getParent());
        } catch (Exception e) {
            logger.error("Failed to import data", e);
            JOptionPane.showMessageDialog(this, "Import fehlgeschlagen.", "Import", JOptionPane.ERROR_MESSAGE);
        }
    }
}//GEN-LAST:event_fireShowImportDialogEvent

private void fireExportEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireExportEvent
    if (evt.getSource() == jExportButton) {
        //do export
        logger.debug("Building export data");

        List<String> plansToExport = new LinkedList<String>();
        for (int i = 0; i < jAttackExportTable.getRowCount(); i++) {
            String plan = (String) jAttackExportTable.getValueAt(i, 0);
            Boolean export = (Boolean) jAttackExportTable.getValueAt(i, 1);
            if (export) {
                logger.debug("Selecting attack plan '" + plan + "' to export list");
                plansToExport.add(plan);
            }
        }

        boolean needExport = false;
        needExport = (plansToExport.size() > 0);
        needExport |= jExportMarks.isSelected();
        needExport |= jExportTags.isSelected();
        needExport |= jExportTroops.isSelected();
        needExport |= jExportForms.isSelected();
        if (!needExport) {
            JOptionPane.showMessageDialog(jExportDialog, "Keine Daten für den Export gewählt", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String dir = GlobalOptions.getProperty("screen.dir");
        if (dir == null) {
            dir = ".";
        }
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogTitle("Datei auswählen");
        chooser.setSelectedFile(new File("export.xml"));
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                if ((f != null) && (f.isDirectory() || f.getName().endsWith(".xml"))) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "*.xml";
            }
        });
        int ret = chooser.showSaveDialog(jExportDialog);
        if (ret == JFileChooser.APPROVE_OPTION) {
            try {
                File f = chooser.getSelectedFile();
                String file = f.getCanonicalPath();
                if (!file.endsWith(".xml")) {
                    file += ".xml";
                }
                File target = new File(file);
                if (target.exists()) {
                    if (JOptionPane.showConfirmDialog(jExportDialog, "Bestehende Datei überschreiben?", "Export", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }

                String exportString = "<export>\n";
                exportString += AttackManager.getSingleton().getExportData(plansToExport);
                if (jExportMarks.isSelected()) {
                    exportString += MarkerManager.getSingleton().getExportData();
                }
                if (jExportTags.isSelected()) {
                    exportString += TagManager.getSingleton().getExportData();
                }
                if (jExportTroops.isSelected()) {
                    exportString += TroopsManager.getSingleton().getExportData();
                }

                if (jExportForms.isSelected()) {
                    exportString += FormManager.getSingleton().getExportData();
                }
                exportString += "</export>";
                logger.debug("Writing data to disk");
                FileWriter w = new FileWriter(target);
                w.write(exportString);
                logger.debug("Finalizing writer");
                w.flush();
                w.close();
                logger.debug("Export finished successfully");
                JOptionPane.showMessageDialog(jExportDialog, "Export erfolgreich beendet.", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                logger.error("Failed to export data", e);
                JOptionPane.showMessageDialog(this, "Export fehlgeschlagen.", "Import", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            //cancel pressed
            return;
        }
    }
    jExportDialog.setVisible(false);
}//GEN-LAST:event_fireExportEvent

private void fireOpenExportDialogEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireOpenExportDialogEvent
    Enumeration<String> plans = AttackManager.getSingleton().getPlans();
    jAttackExportTable.invalidate();
    for (int i = 0; i < jAttackExportTable.getColumnCount(); i++) {
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                c.setBackground(Constants.DS_BACK);
                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                r.setText("<html><b>" + r.getText() + "</b></html>");
                return c;
            }
        };
        jAttackExportTable.getColumn(jAttackExportTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
    }
    DefaultTableModel model = (DefaultTableModel) jAttackExportTable.getModel();
    int rows = model.getRowCount();
    for (int i = 0; i < rows; i++) {
        model.removeRow(0);
    }

    while (plans.hasMoreElements()) {
        String next = plans.nextElement();
        model.addRow(new Object[]{next, Boolean.FALSE});
    }
    jAttackExportTable.revalidate();
    jAttackExportTable.updateUI();

    jExportDialog.setVisible(true);
}//GEN-LAST:event_fireOpenExportDialogEvent

    protected void switchMarkOnTop() {
        jMarkOnTopBox.setSelected(!jMarkOnTopBox.isSelected());
    }

    /**Check if zoom factor is valid and correct if needed*/
    private void checkZoomRange() {
        if (dZoomFactor <= 0.1) {
            dZoomFactor = 0.1;
            jZoomOutButton.setEnabled(false);
        } else if (dZoomFactor >= 2.5) {
            dZoomFactor = 2.5;
            jZoomInButton.setEnabled(false);
        } else {
            jZoomInButton.setEnabled(true);
            jZoomOutButton.setEnabled(true);
        }

    }

    /**Update the MapPanel when dragging the ROI at the MiniMap
     */
    private void updateLocationByMinimap(int pX, int pY) {
        double dx = ServerSettings.getSingleton().getMapDimension().getWidth() / (double) MinimapPanel.getSingleton().getWidth() * (double) pX;
        double dy = ServerSettings.getSingleton().getMapDimension().getHeight() / (double) MinimapPanel.getSingleton().getHeight() * (double) pY;
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] hier = DSCalculator.xyToHierarchical((int) dx, (int) dy);
            if (hier != null) {
                jCenterX.setText(Integer.toString(hier[0]));
                jCenterY.setText(Integer.toString(hier[1]));
            }
        } else {
            jCenterX.setText(Integer.toString((int) Math.floor(dx)));
            jCenterY.setText(Integer.toString((int) Math.floor(dy)));
        }
        dCenterX = dx;
        dCenterY = dy;
        MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
        double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        MinimapPanel.getSingleton().setSelection((int) Math.floor(dx), (int) Math.floor(dy), (int) Math.rint(w), (int) Math.rint(h));
    }

    /**Scroll the map*/
    public void scroll(double pXDir, double pYDir) {
        dCenterX = dCenterX + pXDir;
        dCenterY = dCenterY + pYDir;
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] hier = DSCalculator.xyToHierarchical((int) dCenterX, (int) dCenterY);
            if (hier != null) {
                jCenterX.setText(Integer.toString(hier[0]));
                jCenterY.setText(Integer.toString(hier[1]));
            }
        } else {
            jCenterX.setText(Integer.toString((int) Math.floor(dCenterX)));
            jCenterY.setText(Integer.toString((int) Math.floor(dCenterY)));
        }
        double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        MinimapPanel.getSingleton().setSelection((int) Math.floor(dCenterX), (int) Math.floor(dCenterY), (int) Math.rint(w), (int) Math.rint(h));
        MapPanel.getSingleton().updateMapPosition(dCenterX, dCenterY);
    }

    /**Center a village*/
    public void centerVillage(Village pVillage) {
        if (pVillage == null) {
            return;
        }
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] hier = DSCalculator.xyToHierarchical((int) pVillage.getX(), (int) pVillage.getY());
            if (hier != null) {
                jCenterX.setText(Integer.toString(hier[0]));
                jCenterY.setText(Integer.toString(hier[1]));
            }
        } else {
            jCenterX.setText(Integer.toString(pVillage.getX()));
            jCenterY.setText(Integer.toString(pVillage.getY()));
        }
        fireRefreshMapEvent(null);
    }

    public void centerPosition(int xPos, int yPos) {
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            int[] hier = DSCalculator.xyToHierarchical((int) xPos, (int) yPos);
            if (hier != null) {
                jCenterX.setText(Integer.toString(hier[0]));
                jCenterY.setText(Integer.toString(hier[1]));
            }
        } else {
            jCenterX.setText(Integer.toString(xPos));
            jCenterY.setText(Integer.toString(yPos));
        }
        fireRefreshMapEvent(null);
    }

    /**Get active user village*/
    public Village getCurrentUserVillage() {
        try {
            if (jCurrentPlayerVillages.getSelectedIndex() < 0) {
                if (jCurrentPlayerVillages.getItemCount() > 0) {
                    jCurrentPlayerVillages.setSelectedIndex(0);
                } else {
                    //don't try to get village, list is still empty
                    return null;
                }
            }
            return (Village) jCurrentPlayerVillages.getSelectedItem();
        } catch (ClassCastException cce) {
            //if no player was selected yet
            return null;
        } catch (Exception e) {
            logger.warn("Could not get current user village.", e);
            return null;
        }
    }

    public void setCurrentUserVillage(Village pVillage) {
        jCurrentPlayerVillages.setSelectedItem(pVillage);
    }

    public Tribe getCurrentUser() {
        try {
            Village v = (Village) jCurrentPlayerVillages.getItemAt(0);
            return v.getTribe();
        } catch (Exception e) {
        }
        return null;
    }

    // <editor-fold defaultstate="collapsed" desc=" Listener EventHandlers ">
    @Override
    public void fireToolChangedEvent(int pTool) {
        jCurrentToolLabel.setIcon(ImageManager.getCursorImage(pTool));
    }

    @Override
    public void fireScrollEvent(double pX, double pY) {
        scroll(pX, pY);
    }

    @Override
    public void fireUpdateLocationByMinimap(int pX, int pY) {
        updateLocationByMinimap(pX, pY);
    }

    @Override
    public void fireVisibilityChangedEvent(JFrame pSource, boolean v) {
        if (pSource == DSWorkbenchAttackFrame.getSingleton()) {
            jShowAttackFrame.setSelected(DSWorkbenchAttackFrame.getSingleton().isVisible());
        } else if (pSource == DSWorkbenchMarkerFrame.getSingleton()) {
            jShowMarkerFrame.setSelected(DSWorkbenchMarkerFrame.getSingleton().isVisible());
        } else if (pSource == DSWorkbenchTroopsFrame.getSingleton()) {
            jShowTroopsFrame.setSelected(DSWorkbenchTroopsFrame.getSingleton().isVisible());
        } else if (pSource == DSWorkbenchRankFrame.getSingleton()) {
            jShowRankFrame.setSelected(DSWorkbenchRankFrame.getSingleton().isVisible());
        } else if (pSource == DSWorkbenchFormFrame.getSingleton()) {
            jShowFormsFrame.setSelected(DSWorkbenchFormFrame.getSingleton().isVisible());
        }
    }

    public void fireGroupParserEvent(Hashtable<String, List<Village>> pParserResult) {
        String[] groups = pParserResult.keySet().toArray(new String[]{});
        String message = "DS Workbench hat in deiner Zwischenablage Informationen zu den folgenden Gruppen gefunden:\n";
        for (String s : groups) {
            int size = pParserResult.get(s).size();
            if (size == 0) {
                message += "* keine Dörfer)\n";
            } else if (size == 1) {
                message += "* " + s + " (" + pParserResult.get(s).size() + " Dorf)\n";
            } else {
                message += "* " + s + " (" + pParserResult.get(s).size() + " Dörfer)\n";
            }
        }

        message += "Willst du diese Informationen in DS Workbench übernehmen oder sie verwerfen und aus der Zwischenablage entfernen?";
        UIManager.put("OptionPane.noButtonText", "Verwerfen");
        UIManager.put("OptionPane.yesButtonText", "Übernehmen");
        if (JOptionPane.showConfirmDialog(this, message, "Gruppeninformationen gefunden", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            //remove all tags
            for (String group : groups) {
                List<Village> villagesForGroup = pParserResult.get(group);
                if (villagesForGroup != null) {
                    for (Village v : villagesForGroup) {
                        TagManager.getSingleton().removeTags(v);
                    }
                }
            }

            for (String group : groups) {
                //add new groups
                TagManager.getSingleton().addTag(group);
                //get (added) group
                Tag t = TagManager.getSingleton().getTagByName(group);
                //add villages to group
                List<Village> villagesForGroup = pParserResult.get(group);
                if (villagesForGroup != null) {
                    //remove all tags

                    //set new tags
                    for (Village v : villagesForGroup) {
                        t.tagVillage(v.getId());
                    }
                }
            }
            //update tag panel in settings
            DSWorkbenchSettingsDialog.getSingleton().setupTagsPanel();
        }

        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.yesButtonText", "Yes");
    }

    @Override
    public void fireMapShotDoneEvent() {
        JOptionPane.showMessageDialog(this, "Kartengrafik erfolgreich gespeichert.", "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void fireMapShotFailedEvent() {
        JOptionPane.showMessageDialog(this, "Fehler beim Speichern der Kartengrafik.", "Fehler", JOptionPane.ERROR_MESSAGE);
    }

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Generated Variables">

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jAboutItem;
    private javax.swing.JTable jAttackExportTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jCancelExportButton;
    private javax.swing.JButton jCenterCoordinateIngame;
    private javax.swing.JButton jCenterIngameButton;
    private javax.swing.JTextField jCenterX;
    private javax.swing.JTextField jCenterY;
    private javax.swing.JMenuItem jClockItem;
    private javax.swing.JLabel jCurrentPlayer;
    private javax.swing.JComboBox jCurrentPlayerVillages;
    private javax.swing.JLabel jCurrentToolLabel;
    private javax.swing.JButton jExportButton;
    private javax.swing.JDialog jExportDialog;
    private javax.swing.JCheckBox jExportForms;
    private javax.swing.JCheckBox jExportMarks;
    private javax.swing.JCheckBox jExportTags;
    private javax.swing.JCheckBox jExportTroops;
    private javax.swing.JComboBox jFileTypeChooser;
    private javax.swing.JMenuItem jHelpItem;
    private javax.swing.JPanel jInformationPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JDialog jMapShotDialog;
    private javax.swing.JCheckBox jMarkOnTopBox;
    private javax.swing.JMenuItem jMassAttackItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jMinimapPanel;
    private javax.swing.JButton jMoveE;
    private javax.swing.JButton jMoveE1;
    private javax.swing.JButton jMoveN;
    private javax.swing.JButton jMoveNE;
    private javax.swing.JButton jMoveNW;
    private javax.swing.JButton jMoveS;
    private javax.swing.JButton jMoveSE;
    private javax.swing.JButton jMoveSW;
    private javax.swing.JButton jMoveW;
    private javax.swing.JPanel jNavigationPanel;
    private javax.swing.JLabel jOnlineLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jRefreshButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem jSearchItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JCheckBoxMenuItem jShowAttackFrame;
    private javax.swing.JCheckBoxMenuItem jShowFormsFrame;
    private javax.swing.JCheckBox jShowMapPopup;
    private javax.swing.JCheckBoxMenuItem jShowMarkerFrame;
    private javax.swing.JCheckBoxMenuItem jShowRankFrame;
    private javax.swing.JCheckBoxMenuItem jShowTroopsFrame;
    private javax.swing.JMenuItem jTribeTribeAttackItem;
    private javax.swing.JMenuItem jUnitOverviewItem;
    private javax.swing.JButton jZoomInButton;
    private javax.swing.JButton jZoomOutButton;
    // End of variables declaration//GEN-END:variables
//</editor-fold>
}