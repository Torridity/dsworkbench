/*
 * MapFrame.java
 *
 * Created on 4. September 2007, 18:07
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.ColorChooserCellEditor;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.ui.renderer.ColorCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.MarkerPanelCellRenderer;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author  Charon
 */
public class DSWorkbenchMainFrame extends javax.swing.JFrame implements DataHolderListener {

    private static Logger logger = Logger.getLogger(DSWorkbenchMainFrame.class);
    private MapPanel mPanel = null;
    private int iCenterX = 500;
    private int iCenterY = 500;
    private List<ImageIcon> mIcons;
    private double dZoomFactor = 1.0;
    private ToolBoxFrame mToolbox = null;
//    private AllyAllyAttackFrame mAllyAllyAttackFrame = null;
    private TribeTribeAttackFrame mTribeTribeAttackFrame = null;

    /** Creates new form MapFrame */
    public DSWorkbenchMainFrame() {
        initComponents();
        getContentPane().setBackground(GlobalOptions.DS_BACK);
        jDynFrame.getContentPane().setBackground(GlobalOptions.DS_BACK);

        /*  jMainControlPanel.setupPanel(this, true, true);
        jMainControlPanel.setTitle(getTitle());*/
        pack();
        GlobalOptions.getDataHolder().addListener(this);
        serverSettingsChangedEvent();
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            @Override
            public void eventDispatched(AWTEvent event) {
                if (((KeyEvent) event).getID() == KeyEvent.KEY_PRESSED) {
                    if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_DOWN) {
                        scroll(0, 2);
                    } else if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_UP) {
                        scroll(0, -2);
                    } else if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_LEFT) {
                        scroll(-2, 0);
                    } else if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_RIGHT) {
                        scroll(2, 0);
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);

        try {
            jOnlineLabel.setIcon(new ImageIcon("./graphics/icons/online.png"));
            jUpdateButton.setIcon(new ImageIcon("./graphics/icons/update.png"));
            jCenterIngameButton.setIcon(new ImageIcon("./graphics/icons/center.png"));
            jRefreshButton.setIcon(new ImageIcon("./graphics/icons/refresh.png"));
            jCenterCoordinateIngame.setIcon(new ImageIcon("./graphics/icons/center.png"));
        } catch (Exception e) {
            logger.error("Failed to load status icon(s)", e);
        }
        onlineStateChanged();
        jUpdateButton.setEnabled(false);
    }

    public void serverSettingsChangedEvent() {

        jCurrentPlayer.setText(GlobalOptions.getProperty("player." + GlobalOptions.getSelectedServer()));
        jCurrentServer.setText(GlobalOptions.getSelectedServer());
        Tribe t = GlobalOptions.getDataHolder().getTribeByName(jCurrentPlayer.getText());
        if (t != null) {
            DefaultComboBoxModel model = new DefaultComboBoxModel(t.getVillageList().toArray());
            jCurrentPlayerVillages.setModel(model);
            if (isVisible()) {
                centerVillage(t.getVillageList().get(0));
            } else {
                iCenterX = t.getVillageList().get(0).getX();
                iCenterY = t.getVillageList().get(0).getY();
                jCenterX.setText("" + iCenterX);
                jCenterY.setText("" + iCenterY);
            }
        } else {
            DefaultComboBoxModel model = new DefaultComboBoxModel(new Object[]{"Keine Dörfer"});
            jCurrentPlayerVillages.setModel(model);
        }
        if (MinimapPanel.getGlobalMinimap() != null) {
            MinimapPanel.getGlobalMinimap().redraw();
        }
        setupMarkerPanel();
        jMarkerPanel.updateUI();
        setupAttackPanel();
        jAttackPanel.updateUI();
    }

    public void onlineStateChanged() {
        jOnlineLabel.setEnabled(!GlobalOptions.isOfflineMode());
        if (GlobalOptions.isOfflineMode()) {
            jOnlineLabel.setToolTipText("Offline");
        } else {
            jOnlineLabel.setToolTipText("Online");
        }
    }

    public void updateAvailable() {
        jUpdateButton.setEnabled(true);
    }

    public void init() {
        //setup everything
        setupMaps();
        //setupMarkerPanel();
        setupDetailsPanel();
        // setupAttackPanel();
        setupDynFrame();
        mToolbox = new ToolBoxFrame(mPanel, MinimapPanel.getGlobalMinimap());
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
        /*mAllyAllyAttackFrame = new AllyAllyAttackFrame(this);
        mAllyAllyAttackFrame.pack();*/
        mTribeTribeAttackFrame = new TribeTribeAttackFrame(this);
        mTribeTribeAttackFrame.pack();
    }

    private void setupMaps() {
        logger.info("Initializing maps");
        //build the mappanel
        mPanel = new MapPanel(this);
        jPanel1.add(mPanel);
        //build the minimap
        MinimapPanel.initGlobalMinimap(this);
        jMinimapPanel.add(MinimapPanel.getGlobalMinimap());
    }

    private void setupMarkerPanel() {
        //build the marker table
        jMarkerTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Name", "Markierung"
                }) {

            Class[] types = new Class[]{
                MarkerCell.class, Color.class
            };
            boolean[] canEdit = new boolean[]{
                false, true
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        jMarkerTable.getColumnModel().getColumn(1).setMaxWidth(75);
        jMarkerTable.setDefaultRenderer(Color.class, new ColorCellRenderer());
        jMarkerTable.setDefaultRenderer(MarkerCell.class, new MarkerPanelCellRenderer());
        ColorChooserCellEditor editor = new ColorChooserCellEditor(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //update markers as soon as the colorchooser cell editor has closed
                updateMarkers();
            }
        });

        jMarkerTable.setDefaultEditor(Color.class, editor);
        jScrollPane1.getViewport().setBackground(GlobalOptions.DS_BACK_LIGHT);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                String t = ((DefaultTableCellRenderer) c).getText();
                ((DefaultTableCellRenderer) c).setText("<html><b>" + t + "</b></html>");
                c.setBackground(GlobalOptions.DS_BACK);
                return c;
            }
        };

        for (int i = 0; i < jMarkerTable.getColumnCount(); i++) {
            jMarkerTable.getColumn(jMarkerTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }

        jMarkerTable.getColumn("Markierung").setHeaderRenderer(headerRenderer);
        //insert loaded markers to marker table
        Enumeration<Integer> tribes = GlobalOptions.getDataHolder().getTribes().keys();
        List<String> tribeMarkers = new LinkedList<String>();
        List<String> allyMarkers = new LinkedList<String>();
        int markerCount = GlobalOptions.getMarkers().size();
        while (tribes.hasMoreElements()) {
            Tribe t = GlobalOptions.getDataHolder().getTribes().get(tribes.nextElement());
            Marker m = GlobalOptions.getMarkerByValue(t.getName());
            if (m != null) {
                if (!tribeMarkers.contains(t.getName())) {
                    MarkerCell p = MarkerCell.factoryPlayerMarker(t.getName());
                    ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{p, m.getMarkerColor()});
                    tribeMarkers.add(t.getName());
                    markerCount--;
                }
            }

            if (t.getAlly() != null) {
                m = GlobalOptions.getMarkerByValue(t.getAlly().getName());
                if (m != null) {
                    if (!allyMarkers.contains(t.getAlly().getName())) {
                        MarkerCell p = MarkerCell.factoryAllyMarker(t.getAlly().getName());
                        ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{p, m.getMarkerColor()});
                        allyMarkers.add(t.getAlly().getName());
                        markerCount--;
                    }
                }
            }
            if (markerCount == 0) {
                //all markers read
                break;
            }
        }
    }

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

        jPlayerInfo.setText("");
        jVillageInfo.setText("");
        jVillageInfo.setIcon(null);
        jAllyInfo.setText("");
        jInfoPanel.add(jDetailedInfoPanel);
    }

    private void setupAttackPanel() {
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Ziel", "Einheit", "Ankunftszeit", "Einzeichnen"
                }) {

            Class[] types = new Class[]{
                Village.class, Village.class, UnitHolder.class, Date.class, Boolean.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        jAttackTable.setModel(model);

        jAttackTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jAttackTable.setDefaultEditor(Date.class, new DateSpinEditor());
        jAttackTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
        jAttackTable.setDefaultEditor(Village.class, new VillageCellEditor());
        jScrollPane2.getViewport().setBackground(GlobalOptions.DS_BACK_LIGHT);
        jAttackTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        CellEditorListener attackChangedListener = new CellEditorListener() {

            @Override
            public void editingStopped(ChangeEvent e) {
                //"attack shown" value changed. redraw map panel
                try {
                    updateAttacks();
                } catch (NumberFormatException nfe) {
                }
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
            }
        };
        jAttackTable.getDefaultEditor(Boolean.class).addCellEditorListener(attackChangedListener);
        jAttackTable.getDefaultEditor(Date.class).addCellEditorListener(attackChangedListener);
        jAttackTable.getDefaultEditor(UnitHolder.class).addCellEditorListener(attackChangedListener);
        jAttackTable.getDefaultEditor(Village.class).addCellEditorListener(attackChangedListener);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                String t = ((DefaultTableCellRenderer) c).getText();
                ((DefaultTableCellRenderer) c).setText("<html><b>" + t + "</b></html>");
                c.setBackground(GlobalOptions.DS_BACK);
                return c;
            }
        };

        for (int i = 0; i < jAttackTable.getColumnCount(); i++) {
            jAttackTable.getColumn(jAttackTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }


        for (Attack a : GlobalOptions.getAttacks()) {
            ((DefaultTableModel) jAttackTable.getModel()).addRow(new Object[]{a.getSource(), a.getTarget(), a.getUnit(), a.getArriveTime(), a.isShowOnMap()});
        }
    }

    private void setupDynFrame() {
        jTabbedPane1.addTab("Entfernung", jDistancePanel);
        jTabbedPane1.addTab("Markierungen", jMarkerPanel);
        jTabbedPane1.addTab("Angriffe", jAttackPanel);
        jDynFrame.pack();
        jTabbedPane1.setSelectedIndex(0);
        try {
            jDynFrameAlwaysOnTopSelection.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("dynframe.alwaysOnTop")));
            fireAlwaysInFrontChangeEvent(null);
        } catch (Exception e) {
            //setting not available
        }
    }

    @Override
    public void setVisible(boolean v) {
        super.setVisible(v);
        mPanel.updateMap(iCenterX, iCenterY);
        double w = (double) mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        MinimapPanel.getGlobalMinimap().setSelection(iCenterX, iCenterY, (int) Math.rint(w), (int) Math.rint(h));
        try {
            if (Boolean.parseBoolean(GlobalOptions.getProperty("dynframe.visible"))) {
                jShowDynFrameItem.setSelected(true);
                fireShowDynFrameEvent(null);
            }
        } catch (Exception e) {
        }

        try {
            if (Boolean.parseBoolean(GlobalOptions.getProperty("toolbar.visible"))) {
                jShowToolboxItem.setSelected(true);
                fireShowToolbarEvent(null);
            }
        } catch (Exception e) {
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
        jDistancePanel = new javax.swing.JPanel();
        jSpearTime = new javax.swing.JTextField();
        jSwordTime = new javax.swing.JTextField();
        jSpyTime = new javax.swing.JTextField();
        jLightTime = new javax.swing.JTextField();
        jMArcherTime = new javax.swing.JTextField();
        jAxeTime = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jHeavyTime = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jBowTime = new javax.swing.JTextField();
        jRamTime = new javax.swing.JTextField();
        jCataTime = new javax.swing.JTextField();
        jSnobTime = new javax.swing.JTextField();
        jKnightTime = new javax.swing.JTextField();
        jDistanceFromLabel = new javax.swing.JLabel();
        jDistanceToLabel = new javax.swing.JLabel();
        jDistanceSourceVillage = new javax.swing.JLabel();
        jDistanceTargetVillage = new javax.swing.JLabel();
        jMarkerPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMarkerTable = new javax.swing.JTable();
        jRemoveMarkerButton = new javax.swing.JButton();
        jDynFrame = new javax.swing.JFrame();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jDynFrameAlwaysOnTopSelection = new javax.swing.JCheckBox();
        jAttackPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAttackTable = new javax.swing.JTable();
        jRemoveAttackButton = new javax.swing.JButton();
        jCheckAttacksButton = new javax.swing.JButton();
        jSendAttackButton = new javax.swing.JButton();
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
        jCurrentTribeLabel = new javax.swing.JLabel();
        jCurrentServerLabel = new javax.swing.JLabel();
        jCurrentVillageListLabel = new javax.swing.JLabel();
        jCurrentPlayerVillages = new javax.swing.JComboBox();
        jCurrentPlayer = new javax.swing.JLabel();
        jCurrentServer = new javax.swing.JLabel();
        jCenterIngameButton = new javax.swing.JButton();
        jOnlineLabel = new javax.swing.JLabel();
        jUpdateButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jSearchItem = new javax.swing.JMenuItem();
        jClockItem = new javax.swing.JMenuItem();
        jTribeTribeAttackItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jShowToolboxItem = new javax.swing.JCheckBoxMenuItem();
        jShowDynFrameItem = new javax.swing.JCheckBoxMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jHelpIcon = new javax.swing.JMenuItem();
        jAboutItem = new javax.swing.JMenuItem();

        jDetailedInfoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jDetailedInfoPanel.setOpaque(false);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jVillageInfo.setText(bundle.getString("DSWorkbenchMainFrame.jVillageInfo.text")); // NOI18N
        jVillageInfo.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jVillageInfo.setMaximumSize(new java.awt.Dimension(54, 20));
        jVillageInfo.setMinimumSize(new java.awt.Dimension(54, 20));
        jVillageInfo.setPreferredSize(new java.awt.Dimension(54, 20));

        jPlayerInfo.setText(bundle.getString("DSWorkbenchMainFrame.jPlayerInfo.text")); // NOI18N
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
                    .addComponent(jPlayerInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
                    .addComponent(jAllyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE))
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

        jDistancePanel.setBackground(new java.awt.Color(239, 235, 223));
        jDistancePanel.setMaximumSize(new java.awt.Dimension(750, 96));
        jDistancePanel.setMinimumSize(new java.awt.Dimension(750, 96));

        jSpearTime.setBackground(new java.awt.Color(239, 235, 223));
        jSpearTime.setEditable(false);
        jSpearTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSpearTime.setText(bundle.getString("DSWorkbenchMainFrame.jSpearTime.text")); // NOI18N
        jSpearTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jSpearTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jSpearTime.setOpaque(false);
        jSpearTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jSwordTime.setBackground(new java.awt.Color(239, 235, 223));
        jSwordTime.setEditable(false);
        jSwordTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSwordTime.setText(bundle.getString("DSWorkbenchMainFrame.jSwordTime.text")); // NOI18N
        jSwordTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jSwordTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jSwordTime.setOpaque(false);
        jSwordTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jSpyTime.setBackground(new java.awt.Color(239, 235, 223));
        jSpyTime.setEditable(false);
        jSpyTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSpyTime.setText(bundle.getString("DSWorkbenchMainFrame.jSpyTime.text")); // NOI18N
        jSpyTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jSpyTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jSpyTime.setOpaque(false);
        jSpyTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jLightTime.setBackground(new java.awt.Color(239, 235, 223));
        jLightTime.setEditable(false);
        jLightTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jLightTime.setText(bundle.getString("DSWorkbenchMainFrame.jLightTime.text")); // NOI18N
        jLightTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jLightTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jLightTime.setOpaque(false);
        jLightTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jMArcherTime.setBackground(new java.awt.Color(239, 235, 223));
        jMArcherTime.setEditable(false);
        jMArcherTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jMArcherTime.setText(bundle.getString("DSWorkbenchMainFrame.jMArcherTime.text")); // NOI18N
        jMArcherTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jMArcherTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jMArcherTime.setOpaque(false);
        jMArcherTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jAxeTime.setBackground(new java.awt.Color(239, 235, 223));
        jAxeTime.setEditable(false);
        jAxeTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jAxeTime.setText(bundle.getString("DSWorkbenchMainFrame.jAxeTime.text")); // NOI18N
        jAxeTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jAxeTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jAxeTime.setOpaque(false);
        jAxeTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/axe.png"))); // NOI18N

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/sword.png"))); // NOI18N

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/spy.png"))); // NOI18N

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/light.png"))); // NOI18N

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/heavy.png"))); // NOI18N

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/ram.png"))); // NOI18N

        jHeavyTime.setEditable(false);
        jHeavyTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jHeavyTime.setText(bundle.getString("DSWorkbenchMainFrame.jHeavyTime.text")); // NOI18N
        jHeavyTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jHeavyTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jHeavyTime.setOpaque(false);
        jHeavyTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/snob.png"))); // NOI18N

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/knight.png"))); // NOI18N

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/spear.png"))); // NOI18N

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/archer.png"))); // NOI18N

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/marcher.png"))); // NOI18N

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/cata.png"))); // NOI18N

        jBowTime.setBackground(new java.awt.Color(239, 235, 223));
        jBowTime.setEditable(false);
        jBowTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jBowTime.setText(bundle.getString("DSWorkbenchMainFrame.jBowTime.text")); // NOI18N
        jBowTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jBowTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jBowTime.setOpaque(false);
        jBowTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jRamTime.setEditable(false);
        jRamTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jRamTime.setText(bundle.getString("DSWorkbenchMainFrame.jRamTime.text")); // NOI18N
        jRamTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jRamTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jRamTime.setOpaque(false);
        jRamTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jCataTime.setEditable(false);
        jCataTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jCataTime.setText(bundle.getString("DSWorkbenchMainFrame.jCataTime.text")); // NOI18N
        jCataTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jCataTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jCataTime.setOpaque(false);
        jCataTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jSnobTime.setEditable(false);
        jSnobTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSnobTime.setText(bundle.getString("DSWorkbenchMainFrame.jSnobTime.text")); // NOI18N
        jSnobTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jSnobTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jSnobTime.setOpaque(false);
        jSnobTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jKnightTime.setEditable(false);
        jKnightTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jKnightTime.setText(bundle.getString("DSWorkbenchMainFrame.jKnightTime.text")); // NOI18N
        jKnightTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jKnightTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jKnightTime.setOpaque(false);
        jKnightTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jDistanceFromLabel.setText(bundle.getString("DSWorkbenchMainFrame.jDistanceFromLabel.text")); // NOI18N

        jDistanceToLabel.setText(bundle.getString("DSWorkbenchMainFrame.jDistanceToLabel.text")); // NOI18N

        jDistanceSourceVillage.setText(bundle.getString("DSWorkbenchMainFrame.jDistanceSourceVillage.text")); // NOI18N

        jDistanceTargetVillage.setText(bundle.getString("DSWorkbenchMainFrame.jDistanceTargetVillage.text")); // NOI18N

        javax.swing.GroupLayout jDistancePanelLayout = new javax.swing.GroupLayout(jDistancePanel);
        jDistancePanel.setLayout(jDistancePanelLayout);
        jDistancePanelLayout.setHorizontalGroup(
            jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDistancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDistancePanelLayout.createSequentialGroup()
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jDistanceFromLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jDistanceToLabel, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDistanceTargetVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
                            .addComponent(jDistanceSourceVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE))
                        .addContainerGap(42, Short.MAX_VALUE))
                    .addGroup(jDistancePanelLayout.createSequentialGroup()
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpearTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSwordTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBowTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jAxeTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpyTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLightTime, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMArcherTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jHeavyTime, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jRamTime, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCataTime, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSnobTime, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jKnightTime, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(30, Short.MAX_VALUE))))
        );
        jDistancePanelLayout.setVerticalGroup(
            jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDistancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDistanceFromLabel)
                    .addComponent(jDistanceSourceVillage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDistanceToLabel)
                    .addComponent(jDistanceTargetVillage))
                .addGap(18, 18, 18)
                .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDistancePanelLayout.createSequentialGroup()
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel7)
                            .addComponent(jLabel15)
                            .addComponent(jLabel8)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpearTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSwordTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBowTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jAxeTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpyTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jDistancePanelLayout.createSequentialGroup()
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel17)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel16)
                            .addComponent(jLabel9)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jMArcherTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jHeavyTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLightTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jRamTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCataTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSnobTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jKnightTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(105, Short.MAX_VALUE))
        );

        jMarkerPanel.setBackground(new java.awt.Color(239, 235, 223));
        jMarkerPanel.setMaximumSize(new java.awt.Dimension(750, 305));
        jMarkerPanel.setMinimumSize(new java.awt.Dimension(750, 305));

        jScrollPane1.setBackground(new java.awt.Color(239, 235, 223));
        jScrollPane1.setOpaque(false);

        jMarkerTable.setBackground(new java.awt.Color(239, 235, 223));
        jMarkerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Markierung"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jMarkerTable.setGridColor(new java.awt.Color(239, 235, 223));
        jMarkerTable.setOpaque(false);
        jMarkerTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane1.setViewportView(jMarkerTable);

        jRemoveMarkerButton.setBackground(new java.awt.Color(239, 235, 223));
        jRemoveMarkerButton.setText(bundle.getString("DSWorkbenchMainFrame.jRemoveMarkerButton.text")); // NOI18N
        jRemoveMarkerButton.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jRemoveMarkerButton.toolTipText")); // NOI18N
        jRemoveMarkerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveMarkerEvent(evt);
            }
        });

        javax.swing.GroupLayout jMarkerPanelLayout = new javax.swing.GroupLayout(jMarkerPanel);
        jMarkerPanel.setLayout(jMarkerPanelLayout);
        jMarkerPanelLayout.setHorizontalGroup(
            jMarkerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMarkerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRemoveMarkerButton)
                .addContainerGap(211, Short.MAX_VALUE))
        );
        jMarkerPanelLayout.setVerticalGroup(
            jMarkerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMarkerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMarkerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                    .addComponent(jRemoveMarkerButton))
                .addContainerGap())
        );

        jDynFrame.setTitle(bundle.getString("DSWorkbenchMainFrame.jDynFrame.title")); // NOI18N
        jDynFrame.setBackground(new java.awt.Color(225, 213, 190));
        jDynFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                fireDynFrameClosingEvent(evt);
            }
        });

        jTabbedPane1.setBackground(new java.awt.Color(239, 235, 223));

        jDynFrameAlwaysOnTopSelection.setText(bundle.getString("DSWorkbenchMainFrame.jDynFrameAlwaysOnTopSelection.text")); // NOI18N
        jDynFrameAlwaysOnTopSelection.setOpaque(false);
        jDynFrameAlwaysOnTopSelection.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlwaysInFrontChangeEvent(evt);
            }
        });

        javax.swing.GroupLayout jDynFrameLayout = new javax.swing.GroupLayout(jDynFrame.getContentPane());
        jDynFrame.getContentPane().setLayout(jDynFrameLayout);
        jDynFrameLayout.setHorizontalGroup(
            jDynFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDynFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDynFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jDynFrameAlwaysOnTopSelection)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
                .addContainerGap())
        );
        jDynFrameLayout.setVerticalGroup(
            jDynFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDynFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jDynFrameAlwaysOnTopSelection)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jAttackPanel.setBackground(new java.awt.Color(225, 213, 190));
        jAttackPanel.setMinimumSize(new java.awt.Dimension(750, 305));
        jAttackPanel.setOpaque(false);
        jAttackPanel.setPreferredSize(new java.awt.Dimension(750, 305));
        jAttackPanel.setRequestFocusEnabled(false);

        jAttackTable.setBackground(new java.awt.Color(236, 233, 216));
        jAttackTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Double.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jAttackTable.setOpaque(false);
        jAttackTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane2.setViewportView(jAttackTable);

        jRemoveAttackButton.setText(bundle.getString("DSWorkbenchMainFrame.jRemoveAttackButton.text")); // NOI18N
        jRemoveAttackButton.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jRemoveAttackButton.toolTipText")); // NOI18N
        jRemoveAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveAttackEvent(evt);
            }
        });

        jCheckAttacksButton.setText(bundle.getString("DSWorkbenchMainFrame.jCheckAttacksButton.text")); // NOI18N
        jCheckAttacksButton.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jCheckAttacksButton.toolTipText")); // NOI18N
        jCheckAttacksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireValidateAttacksEvent(evt);
            }
        });

        jSendAttackButton.setText(bundle.getString("DSWorkbenchMainFrame.jSendAttackButton.text")); // NOI18N
        jSendAttackButton.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jSendAttackButton.toolTipText")); // NOI18N
        jSendAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSendAttackEvent(evt);
            }
        });

        javax.swing.GroupLayout jAttackPanelLayout = new javax.swing.GroupLayout(jAttackPanel);
        jAttackPanel.setLayout(jAttackPanelLayout);
        jAttackPanelLayout.setHorizontalGroup(
            jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckAttacksButton, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .addComponent(jRemoveAttackButton, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .addComponent(jSendAttackButton, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addContainerGap())
        );
        jAttackPanelLayout.setVerticalGroup(
            jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                    .addGroup(jAttackPanelLayout.createSequentialGroup()
                        .addComponent(jCheckAttacksButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRemoveAttackButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSendAttackButton)))
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
                        .addComponent(jMoveSW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMoveS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMoveSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                            .addComponent(jMoveE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addComponent(jZoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jZoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCenterCoordinateIngame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRefreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jNavigationPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jCenterX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(jCenterY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(45, 45, 45))
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
                            .addComponent(jLabel1)
                            .addComponent(jCenterY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCenterX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jZoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jZoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jNavigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jCenterCoordinateIngame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jRefreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(74, Short.MAX_VALUE))
        );

        jMinimapPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2));
        jMinimapPanel.setLayout(new java.awt.BorderLayout());

        jInfoPanel.setBackground(new java.awt.Color(239, 235, 223));
        jInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2), bundle.getString("DSWorkbenchMainFrame.jInfoPanel.border.title"))); // NOI18N
        jInfoPanel.setLayout(new java.awt.BorderLayout());

        jInformationPanel.setBackground(new java.awt.Color(239, 235, 223));
        jInformationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 64, 0), 2), bundle.getString("DSWorkbenchMainFrame.jInformationPanel.border.title"))); // NOI18N

        jCurrentTribeLabel.setText(bundle.getString("DSWorkbenchMainFrame.jCurrentTribeLabel.text")); // NOI18N

        jCurrentServerLabel.setText(bundle.getString("DSWorkbenchMainFrame.jCurrentServerLabel.text")); // NOI18N

        jCurrentVillageListLabel.setText(bundle.getString("DSWorkbenchMainFrame.jCurrentVillageListLabel.text")); // NOI18N

        jCurrentPlayerVillages.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeCurrentPlayerVillageEvent(evt);
            }
        });

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

        jUpdateButton.setBackground(new java.awt.Color(239, 235, 223));
        jUpdateButton.setToolTipText(bundle.getString("DSWorkbenchMainFrame.jUpdateButton.toolTipText")); // NOI18N
        jUpdateButton.setMaximumSize(new java.awt.Dimension(30, 30));
        jUpdateButton.setMinimumSize(new java.awt.Dimension(30, 30));
        jUpdateButton.setPreferredSize(new java.awt.Dimension(30, 30));
        jUpdateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateClickedEvent(evt);
            }
        });

        javax.swing.GroupLayout jInformationPanelLayout = new javax.swing.GroupLayout(jInformationPanel);
        jInformationPanel.setLayout(jInformationPanelLayout);
        jInformationPanelLayout.setHorizontalGroup(
            jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCurrentTribeLabel)
                    .addComponent(jCurrentServerLabel)
                    .addComponent(jCurrentVillageListLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCurrentPlayer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                    .addComponent(jCurrentPlayerVillages, javax.swing.GroupLayout.Alignment.LEADING, 0, 171, Short.MAX_VALUE)
                    .addComponent(jCurrentServer, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jInformationPanelLayout.createSequentialGroup()
                        .addComponent(jCenterIngameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jUpdateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOnlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jInformationPanelLayout.setVerticalGroup(
            jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCurrentTribeLabel)
                    .addComponent(jCurrentPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCurrentServerLabel)
                    .addComponent(jCurrentServer, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCurrentVillageListLabel)
                    .addComponent(jCurrentPlayerVillages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCenterIngameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUpdateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jOnlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(57, Short.MAX_VALUE))
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

        jShowDynFrameItem.setBackground(new java.awt.Color(239, 235, 223));
        jShowDynFrameItem.setMnemonic('o');
        jShowDynFrameItem.setText(bundle.getString("DSWorkbenchMainFrame.jShowDynFrameItem.text")); // NOI18N
        jShowDynFrameItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowDynFrameEvent(evt);
            }
        });
        jMenu2.add(jShowDynFrameItem);

        jMenuBar1.add(jMenu2);

        jMenu4.setBackground(new java.awt.Color(225, 213, 190));
        jMenu4.setText(bundle.getString("DSWorkbenchMainFrame.jMenu4.text")); // NOI18N

        jHelpIcon.setBackground(new java.awt.Color(239, 235, 223));
        jHelpIcon.setText(bundle.getString("DSWorkbenchMainFrame.jHelpIcon.text")); // NOI18N
        jHelpIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireShowHelpEvent(evt);
            }
        });
        jMenu4.add(jHelpIcon);

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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jInformationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jNavigationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMinimapPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 823, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jMinimapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jNavigationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jInformationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void fireRefreshMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRefreshMapEvent

    int cx = iCenterX;
    int cy = iCenterY;
    try {
        cx = Integer.parseInt(jCenterX.getText());
        cy = Integer.parseInt(jCenterY.getText());
    } catch (Exception e) {
        cx = iCenterX;
        cy = iCenterY;
    }
    jCenterX.setText(Integer.toString(cx));
    jCenterY.setText(Integer.toString(cy));
    iCenterX = cx;
    iCenterY = cy;

    double w = (double) mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
    double h = (double) mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    MinimapPanel.getGlobalMinimap().setSelection(cx, cy, (int) Math.rint(w), (int) Math.rint(h));
    mPanel.updateMap(iCenterX, iCenterY);
    jPanel1.updateUI();
    jNavigationPanel.updateUI();
}//GEN-LAST:event_fireRefreshMapEvent

private void fireMoveMapEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveMapEvent

    int cx = iCenterX;
    int cy = iCenterY;
    try {
        cx = Integer.parseInt(jCenterX.getText());
        cy = Integer.parseInt(jCenterY.getText());
    } catch (Exception e) {
        cx = iCenterX;
        cy = iCenterY;
    }
    if (evt.getSource() == jMoveN) {
        cy -= mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveNE) {
        cx += mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy -= mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveE) {
        cx += mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveSE) {
        cx += mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy += mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveS) {
        cy += mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveSW) {
        cx -= mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy += mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveW) {
        cx -= mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveNW) {
        cx -= mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy -= mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    }

    jCenterX.setText(Integer.toString(cx));
    jCenterY.setText(Integer.toString(cy));
    iCenterX = cx;
    iCenterY = cy;
    mPanel.updateMap(iCenterX, iCenterY);

    double w = (double) mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
    double h = (double) mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    MinimapPanel.getGlobalMinimap().setSelection(cx, cy, (int) Math.rint(w), (int) Math.rint(h));
    jPanel1.updateUI();
    jNavigationPanel.updateUI();
}//GEN-LAST:event_fireMoveMapEvent

private void fireFrameResizedEvent(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_fireFrameResizedEvent
    try {
        mPanel.updateMap(iCenterX, iCenterY);
    } catch (Exception e) {
    }
}//GEN-LAST:event_fireFrameResizedEvent

private void fireZoomEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireZoomEvent

    if (evt.getSource() == jZoomInButton) {
        dZoomFactor += 1.0 / 10.0;
    } else {
        dZoomFactor -= 1.0 / 10;
    }

    if (dZoomFactor < 0.1) {
        dZoomFactor = 0.1;
        jZoomOutButton.setEnabled(false);
    } else if (dZoomFactor > 2.5) {
        dZoomFactor = 2.5;
        jZoomInButton.setEnabled(false);
    } else {
        jZoomInButton.setEnabled(true);
        jZoomOutButton.setEnabled(true);
    }

    dZoomFactor = Double.parseDouble(NumberFormat.getInstance().format(dZoomFactor).replaceAll(",", "."));

    mPanel.setZoom(dZoomFactor);
    double w = (double) mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
    double h = (double) mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    MinimapPanel.getGlobalMinimap().setSelection(Integer.parseInt(jCenterX.getText()), Integer.parseInt(jCenterY.getText()), (int) Math.rint(w), (int) Math.rint(h));
    mPanel.repaint();
}//GEN-LAST:event_fireZoomEvent

private void fireRemoveMarkerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveMarkerEvent

    int[] rows = jMarkerTable.getSelectedRows();
    if (rows.length == 0) {
        return;
    }
    String message = ((rows.length == 1) ? "Markierung " : (rows.length + " Markierungen ")) + "wirklich löschen?";

    int ret = JOptionPane.showConfirmDialog(jDynFrame, message, "Löschen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (ret == JOptionPane.YES_OPTION) {
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = rows[i];
            String value = ((MarkerCell) ((DefaultTableModel) jMarkerTable.getModel()).getValueAt(row, 0)).getMarkerName();
            GlobalOptions.getMarkers().remove(value);
            ((DefaultTableModel) jMarkerTable.getModel()).removeRow(row);
        }
    }

    updateMarkers();
}//GEN-LAST:event_fireRemoveMarkerEvent

private void fireValidateAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireValidateAttacksEvent
    DefaultTableModel model = (DefaultTableModel) jAttackTable.getModel();

    Hashtable<Integer, String> errors = new Hashtable<Integer, String>();
    for (int i = 0; i < model.getRowCount(); i++) {
        Village source = (Village) model.getValueAt(i, 0);
        Village target = (Village) model.getValueAt(i, 1);
        UnitHolder unit = (UnitHolder) model.getValueAt(i, 2);
        Date arriveTime = (Date) model.getValueAt(i, 3);
        long time = (long) DSCalculator.calculateMoveTimeInMinutes(source, target, unit.getSpeed()) * 60000;
        if (arriveTime.getTime() < System.currentTimeMillis()) {
            errors.put(i, "Ankunftszeit liegt in der Vergangenheit");
        } else if (arriveTime.getTime() - time < System.currentTimeMillis()) {
            errors.put(i, "Notwendige Abschickzeit liegt in der Vergangenheit");
        }
    }

    if (errors.size() != 0) {
        String message = "";
        Enumeration<Integer> keys = errors.keys();
        ListSelectionModel sModel = jAttackTable.getSelectionModel();
        sModel.removeSelectionInterval(0, jAttackTable.getRowCount());
        while (keys.hasMoreElements()) {
            int row = keys.nextElement();
            String error = errors.get(row);
            message = "Zeile " + (row + 1) + ": " + error + "\n" + message;
            sModel.addSelectionInterval(row, row);
        }
        JOptionPane.showMessageDialog(jDynFrame, message, "Fehler", JOptionPane.WARNING_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(jDynFrame, "Keine Fehler gefunden", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}//GEN-LAST:event_fireValidateAttacksEvent

private void fireAlwaysInFrontChangeEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlwaysInFrontChangeEvent

    jDynFrame.setAlwaysOnTop(jDynFrameAlwaysOnTopSelection.isSelected());
    GlobalOptions.addProperty("dynframe.alwaysOnTop", Boolean.toString(jDynFrameAlwaysOnTopSelection.isSelected()));
    GlobalOptions.saveProperties();
}//GEN-LAST:event_fireAlwaysInFrontChangeEvent

private void fireRemoveAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAttackEvent

    int[] rows = jAttackTable.getSelectedRows();
    if (rows.length == 0) {
        return;
    }

    String message = ((rows.length == 1) ? "Angriff " : (rows.length + " Angriffe ")) + "wirklich löschen?";
    int res = JOptionPane.showConfirmDialog(jDynFrame, message, "Angriff entfernen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (res != JOptionPane.YES_OPTION) {
        return;
    }
    for (int i = rows.length - 1; i >= 0; i--) {
        int row = rows[i];
        ((DefaultTableModel) jAttackTable.getModel()).removeRow(row);
    }
    updateAttacks();
}//GEN-LAST:event_fireRemoveAttackEvent

private void fireChangeCurrentPlayerVillageEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeCurrentPlayerVillageEvent

    if (evt.getStateChange() == ItemEvent.SELECTED) {
        centerVillage((Village) jCurrentPlayerVillages.getSelectedItem());
        jCurrentPlayerVillages.transferFocus();
    }
}//GEN-LAST:event_fireChangeCurrentPlayerVillageEvent

private void fireShowSettingsEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowSettingsEvent

    DSWorkbenchSettingsDialog.getGlobalSettingsFrame().setVisible(true);
}//GEN-LAST:event_fireShowSettingsEvent

private void fireExitEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireExitEvent

    GlobalOptions.saveProperties();
    GlobalOptions.storeAttacks();
    GlobalOptions.storeMarkers();
    System.exit(0);
}//GEN-LAST:event_fireExitEvent

private void fireShowToolbarEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowToolbarEvent

    mToolbox.setVisible(jShowToolboxItem.isSelected());
    GlobalOptions.addProperty("toolbar.visible", Boolean.toString(jShowToolboxItem.isSelected()));
    GlobalOptions.saveProperties();
}//GEN-LAST:event_fireShowToolbarEvent

private void fireSendAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSendAttackEvent

    int selectedRow = jAttackTable.getSelectedRow();
    if (selectedRow < 0) {
        return;
    }
    Village source = (Village) ((DefaultTableModel) jAttackTable.getModel()).getValueAt(selectedRow, 0);
    Village target = (Village) ((DefaultTableModel) jAttackTable.getModel()).getValueAt(selectedRow, 1);
    BrowserCommandSender.sendTroops(source, target);
}//GEN-LAST:event_fireSendAttackEvent

private void fireShowDynFrameEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowDynFrameEvent

    jDynFrame.setVisible(jShowDynFrameItem.isSelected());
    GlobalOptions.addProperty("dynframe.visible", Boolean.toString(jShowDynFrameItem.isSelected()));
    GlobalOptions.saveProperties();
}//GEN-LAST:event_fireShowDynFrameEvent

private void fireDynFrameClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireDynFrameClosingEvent

    jShowDynFrameItem.setSelected(false);
    GlobalOptions.addProperty("dynframe.visible", Boolean.toString(false));
    GlobalOptions.saveProperties();
}//GEN-LAST:event_fireDynFrameClosingEvent

private void fireCenterVillageIngameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterVillageIngameEvent

    Village v = (Village) jCurrentPlayerVillages.getSelectedItem();
    if (v != null) {
        BrowserCommandSender.centerVillage(v);
    }
}//GEN-LAST:event_fireCenterVillageIngameEvent

private void fireCenterCurrentPosInGameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterCurrentPosInGameEvent

    BrowserCommandSender.centerCoordinate(Integer.parseInt(jCenterX.getText()), Integer.parseInt(jCenterY.getText()));
}//GEN-LAST:event_fireCenterCurrentPosInGameEvent

private void fireToolsActionEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireToolsActionEvent

    if (evt.getSource() == jSearchItem) {
        SearchFrame.getGlobalSearchFrame().setVisible(true);
    } else if (evt.getSource() == jClockItem) {
        ClockFrame.getGlobalClockFrame().setVisible(true);
    } else if (evt.getSource() == jTribeTribeAttackItem) {
        mTribeTribeAttackFrame.setVisible(true);
    }
}//GEN-LAST:event_fireToolsActionEvent

private void fireUpdateClickedEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateClickedEvent
    if (!jUpdateButton.isEnabled()) {
        return;
    }
    new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                GlobalOptions.getDataHolder().loadData(true);
            } catch (Exception e) {
                logger.error("Failed to execute auto update", e);
                fireDataLoadedEvent();
            }
        }
    }).start();
    jUpdateButton.setEnabled(false);
}//GEN-LAST:event_fireUpdateClickedEvent

private void fireShowAboutEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowAboutEvent
// TODO add your handling code here:
}//GEN-LAST:event_fireShowAboutEvent

private void fireShowHelpEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireShowHelpEvent
// TODO add your handling code here:
}//GEN-LAST:event_fireShowHelpEvent

    public void changeTool(int pTool) {
        switch (pTool) {
            case GlobalOptions.CURSOR_MARK: {
                jTabbedPane1.setSelectedIndex(1);
                jDynFrame.pack();
                break;
            }

            case GlobalOptions.CURSOR_MEASURE: {
                jTabbedPane1.setSelectedIndex(0);
                jDynFrame.pack();
                break;
            }
            case GlobalOptions.CURSOR_DEFAULT: {
                //do nothing
                break;
            }
            default: {
                //one of the attack tools
                jTabbedPane1.setSelectedIndex(2);
                jDynFrame.pack();
                break;
            }
        }
    }

    /**Update the marker table and the maps if one marker added or removed
     */
    private void updateMarkers() {
        //update global markers and maps
        DefaultTableModel model = (DefaultTableModel) jMarkerTable.getModel();
        GlobalOptions.getMarkers().clear();
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = ((MarkerCell) model.getValueAt(i, 0)).getMarkerName();
            int type = ((MarkerCell) model.getValueAt(i, 0)).getType();
            name = name.replaceAll("<html>", "").replaceAll("</html>", "");
            Color c = (Color) model.getValueAt(i, 1);
            Marker m = GlobalOptions.getMarkerByValue(name);
            if (m != null) {
                m.setMarkerColor(c);
            } else {
                m = new Marker();
                m.setMarkerType(type);
                m.setMarkerValue(name);
                m.setMarkerColor(c);
                GlobalOptions.getMarkers().add(m);
            }
        }

        GlobalOptions.storeMarkers();
        //update maps
        mPanel.repaint();
        MinimapPanel.getGlobalMinimap().redraw();
    }

    /**Update the globally stored list of attacks with the contents of the  attack table
     */
    private void updateAttacks() {
        DefaultTableModel model = (DefaultTableModel) jAttackTable.getModel();
        GlobalOptions.getAttacks().clear();
        for (int i = 0; i < model.getRowCount(); i++) {
            Village source = (Village) model.getValueAt(i, 0);
            Village target = (Village) model.getValueAt(i, 1);
            UnitHolder unit = (UnitHolder) model.getValueAt(i, 2);
            Date arriveTime = (Date) model.getValueAt(i, 3);
            Boolean showOnMap = (Boolean) model.getValueAt(i, 4);
            Attack a = new Attack();
            a.setSource(source);
            a.setTarget(target);
            a.setUnit(unit);
            a.setArriveTime(arriveTime);
            a.setShowOnMap(showOnMap.booleanValue());
            GlobalOptions.getAttacks().add(a);
        }
        GlobalOptions.storeAttacks();
    }

    /**Update the MapPanel when dragging the ROI at the MiniMap
     */
    public void updateLocationByMinimap(int pX, int pY) {
        double dx = 1000 / (double) MinimapPanel.getGlobalMinimap().getWidth() * (double) pX;
        double dy = 1000 / (double) MinimapPanel.getGlobalMinimap().getHeight() * (double) pY;

        int x = (int) dx;
        int y = (int) dy;
        jCenterX.setText(Integer.toString(x));
        jCenterY.setText(Integer.toString(y));
        iCenterX = x;
        iCenterY = y;
        mPanel.updateMap(iCenterX, iCenterY);

        double w = (double) mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        MinimapPanel.getGlobalMinimap().setSelection(x, y, (int) Math.rint(w), (int) Math.rint(h));
        jPanel1.updateUI();
        jNavigationPanel.updateUI();
    }

    public void scroll(int pXDir, int pYDir) {
        iCenterX = iCenterX + pXDir;
        iCenterY = iCenterY + pYDir;
        jCenterX.setText(Integer.toString(iCenterX));
        jCenterY.setText(Integer.toString(iCenterY));

        mPanel.updateMap(iCenterX, iCenterY);

        double w = (double) mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        MinimapPanel.getGlobalMinimap().setSelection(iCenterX, iCenterY, (int) Math.rint(w), (int) Math.rint(h));
        jPanel1.updateUI();
        jNavigationPanel.updateUI();
    }

    public void centerVillage(Village pVillage) {
        if (pVillage == null) {
            return;
        }
        jCenterX.setText(Integer.toString(pVillage.getX()));
        jCenterY.setText(Integer.toString(pVillage.getY()));
        fireRefreshMapEvent(null);
    }

    /**Update the distance panel if it is visible
     */
    public void updateDistancePanel(Village pSource, Village pTarget) {
        if (!jDistancePanel.isVisible()) {
            return;
        }

        boolean calculate = true;

        if ((pSource == null) || (pTarget == null)) {
            jSpearTime.setText("------");
            jSwordTime.setText("------");
            jAxeTime.setText("------");
            jBowTime.setText("------");
            jSpyTime.setText("------");
            jLightTime.setText("------");
            jMArcherTime.setText("------");
            jHeavyTime.setText("------");
            jRamTime.setText("------");
            jCataTime.setText("------");
            jSnobTime.setText("------");
            jKnightTime.setText("------");
            jDistanceTargetVillage.setText("------");
            calculate = false;
        }

        String text = "";
        if (pSource != null) {
            text = "<html>";
            if (pSource.getTribe() == null) {
                text += "kein Besitzer - ";
            } else {
                text += pSource.getTribe().getName() + " - ";
            }

            text += pSource.getName() + " (" + pSource.getX() + "|" + pSource.getY() + ")</html>";

            jDistanceSourceVillage.setText(text);
        } else {
            jDistanceSourceVillage.setText("------");
        }

        if (!calculate) {
            return;
        }

        text = "<html>";
        if (pTarget.getTribe() == null) {
            text += "kein Besitzer - ";
        } else {
            text += pTarget.getTribe().getName() + " - ";
        }

        text += pTarget.getName() + " (" + pTarget.getX() + "|" + pTarget.getY() + ")</html>";

        jDistanceTargetVillage.setText(text);

        List<UnitHolder> units = GlobalOptions.getDataHolder().getUnits();
        for (UnitHolder unit : units) {
            String result = DSCalculator.formatTimeInMinutes(DSCalculator.calculateMoveTimeInMinutes(pSource, pTarget, unit.getSpeed()));

            if (unit.getName().equals("Speerträger")) {
                jSpearTime.setText(result);
            } else if (unit.getName().equals("Schwertkämpfer")) {
                jSwordTime.setText(result);
            } else if (unit.getName().equals("Axtkämpfer")) {
                jAxeTime.setText(result);
            } else if (unit.getName().equals("Bogenschütze")) {
                jBowTime.setText(result);
            } else if (unit.getName().equals("Späher")) {
                jSpyTime.setText(result);
            } else if (unit.getName().equals("Leichte Kavallerie")) {
                jLightTime.setText(result);
            } else if (unit.getName().equals("Berittener Bogenschütze")) {
                jMArcherTime.setText(result);
            } else if (unit.getName().equals("Schwere Kavallerie")) {
                jHeavyTime.setText(result);
            } else if (unit.getName().equals("Ramme")) {
                jRamTime.setText(result);
            } else if (unit.getName().equals("Katapult")) {
                jCataTime.setText(result);
            } else if (unit.getName().equals("Adelsgeschlecht")) {
                jSnobTime.setText(result);
            } else if (unit.getName().equals("Paladin")) {
                jKnightTime.setText(result);
            }
        }
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

    /**Add a new marker to the marker table and finally update the maps if the marker panel is visible
     */
    public void updateMarkerPanel(Village pVillage, boolean pMarkTribe, boolean pMarkAlly, Color pTribeColor, Color pAllyColor) {
        if (pVillage.getTribe() == null) {
            //can not mark empty villages
            logger.debug("Cannot mark empty villages");
            return;
        }

        MarkerCell p = null;
        if (pMarkAlly) {
            if (pVillage.getTribe().getAlly() != null) {
                Marker m = GlobalOptions.getMarkerByValue(pVillage.getTribe().getAlly().getName());
                if (m != null) {
                    logger.debug("Replacing existing ally marker");
                    for (int i = 0; i < ((DefaultTableModel) jMarkerTable.getModel()).getRowCount(); i++) {
                        MarkerCell c = (MarkerCell) ((DefaultTableModel) jMarkerTable.getModel()).getValueAt(i, 0);
                        if (c.getMarkerName().equals(pVillage.getTribe().getAlly().getName())) {
                            ((DefaultTableModel) jMarkerTable.getModel()).setValueAt(pAllyColor, i, 1);
                        }

                    }
                } else {
                    logger.debug("Adding new ally marker");
                    m = new Marker();
                    m.setMarkerType(Marker.ALLY_MARKER_TYPE);
                    m.setMarkerValue(pVillage.getTribe().getAlly().getName());
                    m.setMarkerColor(Color.WHITE);
                    GlobalOptions.getMarkers().add(m);
                    p = MarkerCell.factoryAllyMarker(pVillage.getTribe().getAlly().getName());
                    Color c = (pAllyColor != null) ? pAllyColor : Color.WHITE;
                    ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{p, c});
                }

            } else {
                //tribe has no ally. Skip.
                logger.info("No ally found for selected tribe. Skip marking ally");
            }
        }

        if (pMarkTribe) {
            Marker m = GlobalOptions.getMarkerByValue(pVillage.getTribe().getName());
            if (m != null) {
                logger.debug("Replacing existing tribe marker");
                for (int i = 0; i < ((DefaultTableModel) jMarkerTable.getModel()).getRowCount(); i++) {
                    MarkerCell c = (MarkerCell) ((DefaultTableModel) jMarkerTable.getModel()).getValueAt(i, 0);
                    if (c.getMarkerName().equals(pVillage.getTribe().getName())) {
                        ((DefaultTableModel) jMarkerTable.getModel()).setValueAt(pTribeColor, i, 1);
                    }
                }
            } else {
                logger.debug("Adding new tribe marker");
                m = new Marker();
                m.setMarkerType(Marker.TRIBE_MARKER_TYPE);
                m.setMarkerValue(pVillage.getTribe().getName());
                m.setMarkerColor(Color.WHITE);
                GlobalOptions.getMarkers().add(m);
                Color c = (pTribeColor != null) ? pTribeColor : Color.WHITE;
                p = MarkerCell.factoryPlayerMarker(pVillage.getTribe().getName());
                ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{p, c});
            }
        }
        updateMarkers();
    }

    public void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime) {
        ((DefaultTableModel) jAttackTable.getModel()).addRow(new Object[]{pSource, pTarget, pUnit, pArriveTime, false});
        updateAttacks();
    }

    public Village getCurrentUserVillage() {
        return (Village) jCurrentPlayerVillages.getSelectedItem();
    }

    @Override
    public void fireDataHolderEvent(String pFile) {
        //do nothing
    }

    @Override
    public void fireDataLoadedEvent() {
        jUpdateButton.setEnabled(false);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchMainFrame().setVisible(true);
            }
        });
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Variables">

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jAboutItem;
    private javax.swing.JLabel jAllyInfo;
    private javax.swing.JPanel jAttackPanel;
    private javax.swing.JTable jAttackTable;
    private javax.swing.JTextField jAxeTime;
    private javax.swing.JTextField jBowTime;
    private javax.swing.JTextField jCataTime;
    private javax.swing.JButton jCenterCoordinateIngame;
    private javax.swing.JButton jCenterIngameButton;
    private javax.swing.JTextField jCenterX;
    private javax.swing.JTextField jCenterY;
    private javax.swing.JButton jCheckAttacksButton;
    private javax.swing.JMenuItem jClockItem;
    private javax.swing.JLabel jCurrentPlayer;
    private javax.swing.JComboBox jCurrentPlayerVillages;
    private javax.swing.JLabel jCurrentServer;
    private javax.swing.JLabel jCurrentServerLabel;
    private javax.swing.JLabel jCurrentTribeLabel;
    private javax.swing.JLabel jCurrentVillageListLabel;
    private javax.swing.JPanel jDetailedInfoPanel;
    private javax.swing.JLabel jDistanceFromLabel;
    private javax.swing.JPanel jDistancePanel;
    private javax.swing.JLabel jDistanceSourceVillage;
    private javax.swing.JLabel jDistanceTargetVillage;
    private javax.swing.JLabel jDistanceToLabel;
    private javax.swing.JFrame jDynFrame;
    private javax.swing.JCheckBox jDynFrameAlwaysOnTopSelection;
    private javax.swing.JTextField jHeavyTime;
    private javax.swing.JMenuItem jHelpIcon;
    private javax.swing.JPanel jInfoPanel;
    private javax.swing.JPanel jInformationPanel;
    private javax.swing.JTextField jKnightTime;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jLightTime;
    private javax.swing.JTextField jMArcherTime;
    private javax.swing.JPanel jMarkerPanel;
    private javax.swing.JTable jMarkerTable;
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
    private javax.swing.JLabel jPlayerInfo;
    private javax.swing.JTextField jRamTime;
    private javax.swing.JButton jRefreshButton;
    private javax.swing.JButton jRemoveAttackButton;
    private javax.swing.JButton jRemoveMarkerButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuItem jSearchItem;
    private javax.swing.JButton jSendAttackButton;
    private javax.swing.JCheckBoxMenuItem jShowDynFrameItem;
    private javax.swing.JCheckBoxMenuItem jShowToolboxItem;
    private javax.swing.JTextField jSnobTime;
    private javax.swing.JTextField jSpearTime;
    private javax.swing.JTextField jSpyTime;
    private javax.swing.JTextField jSwordTime;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JMenuItem jTribeTribeAttackItem;
    private javax.swing.JButton jUpdateButton;
    private javax.swing.JLabel jVillageInfo;
    private javax.swing.JButton jZoomInButton;
    private javax.swing.JButton jZoomOutButton;
    // End of variables declaration//GEN-END:variables
//</editor-fold>
}