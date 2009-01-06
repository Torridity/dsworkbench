/*
 * MapFrame.java
 *
 * Created on 4. September 2007, 18:07
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import de.tor.tribes.types.Tag;
import de.tor.tribes.ui.renderer.MapRenderer;
import de.tor.tribes.util.ServerSettings;

/**
 * @author  Charon
 * @Todo Remove form entry for former versions
 */
public class DSWorkbenchMainFrame extends javax.swing.JFrame implements
        MapPanelListener,
        MinimapListener,
        ToolChangeListener,
        DSWorkbenchFrameListener {

    private static Logger logger = Logger.getLogger("MainApp");
    private int iCenterX = 500;
    private int iCenterY = 500;
    private List<ImageIcon> mIcons;
    private double dZoomFactor = 1.0;
    private ToolBoxFrame mToolbox = null;
    private AllyAllyAttackFrame mAllyAllyAttackFrame = null;
    private TribeTribeAttackFrame mTribeTribeAttackFrame = null;
    private AboutDialog mAbout = null;
    private static DSWorkbenchMainFrame SINGLETON = null;
    private boolean initialized = false;

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

        // <editor-fold defaultstate="collapsed" desc=" Register ShutdownHook ">

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                logger.info("Performing ShutdownHook");
                GlobalOptions.saveUserData();
                GlobalOptions.addProperty("attack.frame.visible", Boolean.toString(DSWorkbenchAttackFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("marker.frame.visible", Boolean.toString(DSWorkbenchMarkerFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("troops.frame.visible", Boolean.toString(DSWorkbenchTroopsFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("distance.frame.visible", Boolean.toString(DSWorkbenchDistanceFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("rank.frame.visible", Boolean.toString(DSWorkbenchRankFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("form.frame.visible", Boolean.toString(DSWorkbenchFormFrame.getSingleton().isVisible()));
                GlobalOptions.addProperty("distance.frame.alwaysOnTop", Boolean.toString(DSWorkbenchDistanceFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("attack.frame.alwaysOnTop", Boolean.toString(DSWorkbenchAttackFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("marker.frame.alwaysOnTop", Boolean.toString(DSWorkbenchMarkerFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("troops.frame.alwaysOnTop", Boolean.toString(DSWorkbenchTroopsFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("rank.frame.alwaysOnTop", Boolean.toString(DSWorkbenchRankFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("form.frame.alwaysOnTop", Boolean.toString(DSWorkbenchFormFrame.getSingleton().isAlwaysOnTop()));
                GlobalOptions.addProperty("zoom.factor", Double.toString(dZoomFactor));
                GlobalOptions.addProperty("last.x", jCenterX.getText());
                GlobalOptions.addProperty("last.y", jCenterY.getText());
                GlobalOptions.saveProperties();
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
                    if (DSWorkbenchMainFrame.getSingleton().isActive()) {
                        //move shortcuts
                        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                            scroll(0, 2);
                        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                            scroll(0, -2);
                        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            scroll(-2, 0);
                        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            scroll(2, 0);
                        }
                    }

                    //misc shortcuts
                    if ((e.getKeyCode() == KeyEvent.VK_1) && e.isAltDown()) {
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
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_INGAME);
                    } else if ((e.getKeyCode() == KeyEvent.VK_6) && e.isAltDown()) {
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
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
                    } else if ((e.getKeyCode() == KeyEvent.VK_6) && e.isShiftDown()) {
                        //attack light tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
                    } else if ((e.getKeyCode() == KeyEvent.VK_7) && e.isShiftDown()) {
                        //attack heavy tool shortcut
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
                    } else if ((e.getKeyCode() == KeyEvent.VK_S) && e.isControlDown()) {
                        //search frame shortcut
                        SearchFrame.getSingleton().setVisible(true);
                    } else if ((e.getKeyCode() == KeyEvent.VK_T) && e.isControlDown()) {
                        //search time shortcut
                        ClockFrame.getSingleton().setVisible(true);
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
            iCenterX = Integer.parseInt(x);
            iCenterY = Integer.parseInt(y);
            jCenterX.setText(x);
            jCenterY.setText(y);
        } catch (Exception e) {
            iCenterX = 500;
            iCenterY =
                    500;
            jCenterX.setText("500");
            jCenterY.setText("500");
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
                } else if (e.getSource() == DSWorkbenchDistanceFrame.getSingleton()) {
                    fireShowDistanceFrameEvent(null);
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
        MapPanel.getSingleton().updateMapPosition(iCenterX, iCenterY);
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

        logger.info(" * Setup toolbox");
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
        fireToolChangedEvent(ImageManager.CURSOR_DEFAULT);

        logger.info(" * Setting up attack planner");
        //setup frames
        mAllyAllyAttackFrame = new AllyAllyAttackFrame();
        mAllyAllyAttackFrame.pack();
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
        DSWorkbenchDistanceFrame.getSingleton().addFrameListener(this);
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

        jPlayerInfo.setText("");
        jVillageInfo.setText("");
        jVillageInfo.setIcon(null);
        jAllyInfo.setText("");
        jInfoPanel.add(jDetailedInfoPanel);
    }

    @Override
    public void setVisible(boolean v) {
        logger.info("Setting MainWindow visible");
        super.setVisible(v);
        if (v) {
            //only if set to visible
            MapPanel.getSingleton().updateMapPosition(iCenterX, iCenterY);

            double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
            double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
            MinimapPanel.getSingleton().setSelection(iCenterX, iCenterY, (int) Math.rint(w), (int) Math.rint(h));

            // <editor-fold defaultstate="collapsed" desc=" Check frames and toolbar visibility ">

            try {
                if (Boolean.parseBoolean(GlobalOptions.getProperty("attack.frame.visible"))) {
                    jShowAttackFrame.setSelected(true);
                    logger.info("Restoring attack frame");
                    DSWorkbenchAttackFrame.getSingleton().setVisible(true);
                }

            } catch (Exception e) {
                logger.error("Failed to show main screen", e);
                System.exit(-1);
            }

            try {
                if (Boolean.parseBoolean(GlobalOptions.getProperty("distance.frame.visible"))) {
                    jShowDistanceFrame.setSelected(true);
                    logger.info("Restoring distance frame");
                    DSWorkbenchDistanceFrame.getSingleton().setVisible(true);
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
                if (Boolean.parseBoolean(GlobalOptions.getProperty("toolbar.visible"))) {
                    jShowToolboxItem.setSelected(true);
                    logger.info("Restoring toolbar frame");
                    fireShowToolbarEvent(null);
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

        jDetailedInfoPanel = new javax.swing.JPanel();
        jVillageInfo = new javax.swing.JLabel();
        jPlayerInfo = new javax.swing.JLabel();
        jAllyInfo = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
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
        jInfoPanel = new javax.swing.JPanel();
        jInformationPanel = new javax.swing.JPanel();
        jCurrentPlayerVillages = new javax.swing.JComboBox();
        jCurrentPlayer = new javax.swing.JLabel();
        jCenterIngameButton = new javax.swing.JButton();
        jOnlineLabel = new javax.swing.JLabel();
        jCurrentToolLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jSearchItem = new javax.swing.JMenuItem();
        jClockItem = new javax.swing.JMenuItem();
        jTribeTribeAttackItem = new javax.swing.JMenuItem();
        jMassAttackItem = new javax.swing.JMenuItem();
        jUnitOverviewItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jShowToolboxItem = new javax.swing.JCheckBoxMenuItem();
        jShowAttackFrame = new javax.swing.JCheckBoxMenuItem();
        jShowDistanceFrame = new javax.swing.JCheckBoxMenuItem();
        jShowMarkerFrame = new javax.swing.JCheckBoxMenuItem();
        jShowTroopsFrame = new javax.swing.JCheckBoxMenuItem();
        jShowRankFrame = new javax.swing.JCheckBoxMenuItem();
        jShowFormsFrame = new javax.swing.JCheckBoxMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jAboutItem = new javax.swing.JMenuItem();

        jDetailedInfoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jDetailedInfoPanel.setOpaque(false);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jVillageInfo.setText(bundle.getString("DSWorkbenchMainFrame.jVillageInfo.text")); // NOI18N
        jVillageInfo.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jVillageInfo.setMaximumSize(new java.awt.Dimension(54, 20));
        jVillageInfo.setMinimumSize(new java.awt.Dimension(54, 20));
        jVillageInfo.setPreferredSize(new java.awt.Dimension(54, 20));

        jPlayerInfo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jPlayerInfo.setText(bundle.getString("DSWorkbenchMainFrame.jPlayerInfo.text")); // NOI18N
        jPlayerInfo.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPlayerInfo.setMaximumSize(new java.awt.Dimension(54, 20));
        jPlayerInfo.setMinimumSize(new java.awt.Dimension(54, 20));
        jPlayerInfo.setPreferredSize(new java.awt.Dimension(54, 20));

        jAllyInfo.setText(bundle.getString("DSWorkbenchMainFrame.jAllyInfo.text")); // NOI18N
        jAllyInfo.setMaximumSize(new java.awt.Dimension(54, 20));
        jAllyInfo.setMinimumSize(new java.awt.Dimension(54, 20));
        jAllyInfo.setPreferredSize(new java.awt.Dimension(54, 20));

        javax.swing.GroupLayout jDetailedInfoPanelLayout = new javax.swing.GroupLayout(jDetailedInfoPanel);
        jDetailedInfoPanel.setLayout(jDetailedInfoPanelLayout);
        jDetailedInfoPanelLayout.setHorizontalGroup(
            jDetailedInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDetailedInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDetailedInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jVillageInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
                    .addComponent(jAllyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
                    .addComponent(jPlayerInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE))
                .addContainerGap())
        );
        jDetailedInfoPanelLayout.setVerticalGroup(
            jDetailedInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDetailedInfoPanelLayout.createSequentialGroup()
                .addComponent(jVillageInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jPlayerInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAllyInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBackground(new java.awt.Color(153, 255, 153));

        jLabel3.setText(bundle.getString("DSWorkbenchMainFrame.jLabel3.text")); // NOI18N

        jEditorPane1.setContentType(bundle.getString("DSWorkbenchMainFrame.jEditorPane1.contentType")); // NOI18N
        jEditorPane1.setText(bundle.getString("DSWorkbenchMainFrame.jEditorPane1.text")); // NOI18N
        jScrollPane1.setViewportView(jEditorPane1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addContainerGap(32, Short.MAX_VALUE))
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

        jInfoPanel.setBackground(new java.awt.Color(239, 235, 223));
        jInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2), bundle.getString("DSWorkbenchMainFrame.jInfoPanel.border.title"))); // NOI18N
        jInfoPanel.setLayout(new java.awt.BorderLayout());

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

        jCurrentToolLabel.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jCurrentToolLabel.toolTipText")); // NOI18N
        jCurrentToolLabel.setAlignmentY(0.0F);
        jCurrentToolLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jCurrentToolLabel.setIconTextGap(0);
        jCurrentToolLabel.setMaximumSize(new java.awt.Dimension(35, 35));
        jCurrentToolLabel.setMinimumSize(new java.awt.Dimension(35, 35));
        jCurrentToolLabel.setPreferredSize(new java.awt.Dimension(35, 35));

        javax.swing.GroupLayout jInformationPanelLayout = new javax.swing.GroupLayout(jInformationPanel);
        jInformationPanel.setLayout(jInformationPanelLayout);
        jInformationPanelLayout.setHorizontalGroup(
            jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCurrentPlayer, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInformationPanelLayout.createSequentialGroup()
                        .addComponent(jCenterIngameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOnlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCurrentPlayerVillages, 0, 238, Short.MAX_VALUE)
                    .addComponent(jCurrentToolLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jInformationPanelLayout.setVerticalGroup(
            jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCurrentPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCurrentPlayerVillages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jInformationPanelLayout.createSequentialGroup()
                        .addComponent(jOnlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 93, Short.MAX_VALUE)
                        .addComponent(jCurrentToolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCenterIngameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jShowToolboxItem.setBackground(new java.awt.Color(239, 235, 223));
        jShowToolboxItem.setMnemonic('w');
        jShowToolboxItem.setText(bundle.getString("DSWorkbenchMainFrame.jShowToolboxItem.text")); // NOI18N
        jShowToolboxItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowToolbarEvent(evt);
            }
        });
        jMenu2.add(jShowToolboxItem);

        jShowAttackFrame.setBackground(new java.awt.Color(239, 235, 223));
        jShowAttackFrame.setText(bundle.getString("DSWorkbenchMainFrame.jShowAttackFrame.text")); // NOI18N
        jShowAttackFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowAttackFrameEvent(evt);
            }
        });
        jMenu2.add(jShowAttackFrame);

        jShowDistanceFrame.setBackground(new java.awt.Color(239, 235, 223));
        jShowDistanceFrame.setText(bundle.getString("DSWorkbenchMainFrame.jShowDistanceFrame.text")); // NOI18N
        jShowDistanceFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowDistanceFrameEvent(evt);
            }
        });
        jMenu2.add(jShowDistanceFrame);

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jInfoPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jMinimapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jNavigationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jInformationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jMinimapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jNavigationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jInformationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**Update map position*/
private void fireRefreshMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRefreshMapEvent
    int cx = iCenterX;
    int cy = iCenterY;
    try {
        cx = Integer.parseInt(jCenterX.getText());
        cy =
                Integer.parseInt(jCenterY.getText());
    } catch (Exception e) {
        cx = iCenterX;
        cy =
                iCenterY;
    }

    jCenterX.setText(Integer.toString(cx));
    jCenterY.setText(Integer.toString(cy));
    iCenterX =
            cx;
    iCenterY =
            cy;

    double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
    double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    MinimapPanel.getSingleton().setSelection(cx, cy, (int) Math.rint(w), (int) Math.rint(h));
    MapPanel.getSingleton().updateMapPosition(iCenterX, iCenterY);
}//GEN-LAST:event_fireRefreshMapEvent

    /**Update map movement*/
private void fireMoveMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveMapEvent

    int cx = iCenterX;
    int cy = iCenterY;
    try {
        cx = Integer.parseInt(jCenterX.getText());
        cy =
                Integer.parseInt(jCenterY.getText());
    } catch (Exception e) {
        cx = iCenterX;
        cy =
                iCenterY;
    }

    if (evt.getSource() == jMoveN) {
        cy -= MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveNE) {
        cx += MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy -=
                MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveE) {
        cx += MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveSE) {
        cx += MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy +=
                MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveS) {
        cy += MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveSW) {
        cx -= MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy +=
                MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveW) {
        cx -= MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveNW) {
        cx -= MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy -=
                MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    }

    jCenterX.setText(Integer.toString(cx));
    jCenterY.setText(Integer.toString(cy));
    iCenterX =
            cx;
    iCenterY =
            cy;
    MapPanel.getSingleton().updateMapPosition(iCenterX, iCenterY);
    double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
    double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    MinimapPanel.getSingleton().setSelection(cx, cy, (int) Math.rint(w), (int) Math.rint(h));
}//GEN-LAST:event_fireMoveMapEvent

    /**React on resize events*/
private void fireFrameResizedEvent(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_fireFrameResizedEvent
    try {
        MapPanel.getSingleton().updateMapPosition(iCenterX, iCenterY);
    } catch (Exception e) {
        logger.error("Failed to resize map for (" + iCenterX + ", " + iCenterY + ")", e);
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
        MinimapPanel.getSingleton().setSelection(Integer.parseInt(jCenterX.getText()), Integer.parseInt(jCenterY.getText()), (int) Math.rint(w), (int) Math.rint(h));
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
    }

    protected synchronized void zoomOut() {
        dZoomFactor -= 1.0 / 10;
        checkZoomRange();

        dZoomFactor = Double.parseDouble(NumberFormat.getInstance().format(dZoomFactor).replaceAll(",", "."));

        double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        MinimapPanel.getSingleton().setSelection(Integer.parseInt(jCenterX.getText()), Integer.parseInt(jCenterY.getText()), (int) Math.rint(w), (int) Math.rint(h));
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.ALL_LAYERS);
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
private void fireShowToolbarEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowToolbarEvent
    mToolbox.setVisible(jShowToolboxItem.isSelected());
    GlobalOptions.addProperty("toolbar.visible", Boolean.toString(jShowToolboxItem.isSelected()));
    GlobalOptions.saveProperties();
}//GEN-LAST:event_fireShowToolbarEvent

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
        SearchFrame.getSingleton().setVisible(true);
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

private void fireShowDistanceFrameEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowDistanceFrameEvent
    DSWorkbenchDistanceFrame.getSingleton().setVisible(!DSWorkbenchDistanceFrame.getSingleton().isVisible());
    jShowDistanceFrame.setSelected(DSWorkbenchDistanceFrame.getSingleton().isVisible());
}//GEN-LAST:event_fireShowDistanceFrameEvent

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

        int x = (int) dx;
        int y = (int) dy;
        jCenterX.setText(Integer.toString(x));
        jCenterY.setText(Integer.toString(y));
        iCenterX = x;
        iCenterY = y;
        MapPanel.getSingleton().updateMapPosition(iCenterX, iCenterY);
        double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        MinimapPanel.getSingleton().setSelection(x, y, (int) Math.rint(w), (int) Math.rint(h));
    }

    /**Scroll the map*/
    public void scroll(int pXDir, int pYDir) {
        iCenterX = iCenterX + pXDir;
        iCenterY =
                iCenterY + pYDir;
        jCenterX.setText(Integer.toString(iCenterX));
        jCenterY.setText(Integer.toString(iCenterY));

        double w = (double) MapPanel.getSingleton().getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) MapPanel.getSingleton().getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        MinimapPanel.getSingleton().setSelection(iCenterX, iCenterY, (int) Math.rint(w), (int) Math.rint(h));
        MapPanel.getSingleton().updateMapPosition(iCenterX, iCenterY);
    }

    /**Center a village*/
    public void centerVillage(Village pVillage) {
        if (pVillage == null) {
            return;
        }

        jCenterX.setText(Integer.toString(pVillage.getX()));
        jCenterY.setText(Integer.toString(pVillage.getY()));
        fireRefreshMapEvent(null);
    }

    /**Update the detailed information panel if it is visible
     */
    public void updateDetailedInfoPanel(Village pVillage) {
        if (!jDetailedInfoPanel.isVisible()) {
            return;
        }

        if (pVillage == null) {
            jPlayerInfo.setText("");
            jVillageInfo.setText("");
            jAllyInfo.setText("");
            jVillageInfo.setIcon(null);
            return;

        }



        jVillageInfo.setText(pVillage.getHTMLInfo());
        jLabel3.setMinimumSize(new Dimension(SwingUtilities.computeStringWidth(jVillageInfo.getGraphics().getFontMetrics(), pVillage.getHTMLInfo()), 20));
        jVillageInfo.setIcon(mIcons.get(pVillage.getType()));

        try {
            Tribe tribe = pVillage.getTribe();
            jPlayerInfo.setText(tribe.getHTMLInfo());

            Ally ally = tribe.getAlly();
            if (ally == null) {
                jAllyInfo.setText("kein Stamm");
            } else {
                jAllyInfo.setText(ally.getHTMLInfo());
            }

        } catch (NullPointerException e) {
            jPlayerInfo.setText("kein Besitzer");
            jAllyInfo.setText("kein Stamm");
        }

    }

    /**Get active user village*/
    public Village getCurrentUserVillage() {
        try {
            if (jCurrentPlayerVillages.getSelectedIndex() < 0) {
                jCurrentPlayerVillages.setSelectedIndex(0);
            }

            return (Village) jCurrentPlayerVillages.getSelectedItem();
        } catch (Exception e) {
            logger.warn("Could not get current user village. Probably no active player was selected.");
            return null;
        }

    }
    // <editor-fold defaultstate="collapsed" desc=" Listener EventHandlers ">

    @Override
    public void fireToolChangedEvent(int pTool) {
        jCurrentToolLabel.setIcon(ImageManager.getCursorImage(pTool));
    }

    @Override
    public void fireVillageAtMousePosChangedEvent(Village pVillage) {
        updateDetailedInfoPanel(pVillage);
    }

    @Override
    public void fireDistanceEvent(Village pSource, Village pTarget) {
        DSWorkbenchDistanceFrame.getSingleton().updateDistances(pSource, pTarget);
    }

    @Override
    public void fireScrollEvent(int pX, int pY) {
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
        } else if (pSource == DSWorkbenchDistanceFrame.getSingleton()) {
            jShowDistanceFrame.setSelected(DSWorkbenchDistanceFrame.getSingleton().isVisible());
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
            message += "* " + s + " (" + pParserResult.get(s).size() + " Dörfer)\n";
        }

        message += "Willst du diese Informationen in DS Workbench übernehmen oder sie verwerfen und aus der Zwischenablage entfernen?";
        UIManager.put("OptionPane.noButtonText", "Verwerfen");
        UIManager.put("OptionPane.yesButtonText", "Übernehmen");
        if (JOptionPane.showConfirmDialog(this, message, "Gruppeninformationen gefunden", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            for (String group : groups) {
                TagManager.getSingleton().addTag(group);
                Tag t = TagManager.getSingleton().getTagByName(group);
                List<Village> villagesForGroup = pParserResult.get(group);
                if (villagesForGroup != null) {
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

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Generated Variables">

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jAboutItem;
    private javax.swing.JLabel jAllyInfo;
    private javax.swing.JButton jCenterCoordinateIngame;
    private javax.swing.JButton jCenterIngameButton;
    private javax.swing.JTextField jCenterX;
    private javax.swing.JTextField jCenterY;
    private javax.swing.JMenuItem jClockItem;
    private javax.swing.JLabel jCurrentPlayer;
    private javax.swing.JComboBox jCurrentPlayerVillages;
    private javax.swing.JLabel jCurrentToolLabel;
    private javax.swing.JPanel jDetailedInfoPanel;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jInfoPanel;
    private javax.swing.JPanel jInformationPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuItem jMassAttackItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jPlayerInfo;
    private javax.swing.JButton jRefreshButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem jSearchItem;
    private javax.swing.JCheckBoxMenuItem jShowAttackFrame;
    private javax.swing.JCheckBoxMenuItem jShowDistanceFrame;
    private javax.swing.JCheckBoxMenuItem jShowFormsFrame;
    private javax.swing.JCheckBoxMenuItem jShowMarkerFrame;
    private javax.swing.JCheckBoxMenuItem jShowRankFrame;
    private javax.swing.JCheckBoxMenuItem jShowToolboxItem;
    private javax.swing.JCheckBoxMenuItem jShowTroopsFrame;
    private javax.swing.JMenuItem jTribeTribeAttackItem;
    private javax.swing.JMenuItem jUnitOverviewItem;
    private javax.swing.JLabel jVillageInfo;
    private javax.swing.JButton jZoomInButton;
    private javax.swing.JButton jZoomOutButton;
    // End of variables declaration//GEN-END:variables
//</editor-fold>
}