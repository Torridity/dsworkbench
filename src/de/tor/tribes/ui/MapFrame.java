/*
 * MapFrame.java
 *
 * Created on 4. September 2007, 18:07
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.ServerList;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Unit;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.ColorChooserCellEditor;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.ui.renderer.ColorCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.MarkerPanelCellRenderer;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author  Charon
 */
public class MapFrame extends javax.swing.JFrame implements DataHolderListener {

    private static Logger logger = Logger.getLogger(MapFrame.class);
    private MapPanel mPanel = null;
    private MinimapPanel mMiniPanel = null;
    private int iCenterX = 456;
    private int iCenterY = 468;
    private List<ImageIcon> mIcons;
    private double dZoomFactor = 1.0;

    /** Creates new form MapFrame */
    public MapFrame() {
        initComponents();
        /*  jMainControlPanel.setupPanel(this, true, true);
        jMainControlPanel.setTitle(getTitle());*/
        pack();
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "proxy.fzk.de");
        System.getProperties().put("proxyPort", "8000");
        try {
            ServerList.loadServerList();
        } catch (Exception e) {
        }
        //show server/player-selection dialog
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String sID : ServerList.getServerIDs()) {
            model.addElement(sID);
        }
        jServerSelection.setModel(model);
        jPlayerSelectionDialog.pack();
        jPlayerSelectionDialog.setVisible(true);
        //init the global options
        try {
            GlobalOptions.initialize(false, this);
        } catch (Exception e) {
            logger.fatal("Failed to initialize global data structures", e);
            System.exit(0);
        }
        setupMaps();
        setupMarkerPanel();
        setupDetailsPanel();
        setupAttackPanel();
        setupDynFrame();
    }

    private void setupMaps() {
        //build the mappanel
        mPanel = new MapPanel(this);
        jPanel1.add(mPanel);
        //build the minimap
        mMiniPanel = new MinimapPanel(this);
        jMinimapPanel.add(mMiniPanel);
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

        //insert loaded markers to marker table
        Enumeration<Integer> tribes = GlobalOptions.getDataHolder().getTribes().keys();
        List<String> tribeMarkers = new LinkedList<String>();
        List<String> allyMarkers = new LinkedList<String>();
        int markerCount = GlobalOptions.getMarkers().size();
        while (tribes.hasMoreElements()) {
            Tribe t = GlobalOptions.getDataHolder().getTribes().get(tribes.nextElement());
            //if(GlobalOptions.gett.getName()
            Color c = GlobalOptions.getMarkers().get(t.getName());
            if (c != null) {
                if (!tribeMarkers.contains(t.getName())) {
                    MarkerCell p = MarkerCell.factoryPlayerMarker(t.getName());
                    ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{p, c});
                    tribeMarkers.add(t.getName());
                    markerCount--;
                }
            }

            if (t.getAlly() != null) {
                c = GlobalOptions.getMarkers().get(t.getAlly().getName());
                if (c != null) {
                    if (!allyMarkers.contains(t.getAlly().getName())) {
                        MarkerCell p = MarkerCell.factoryAllyMarker(t.getAlly().getName());
                        ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{p, c});
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
        DefaultTableModel model = new javax.swing.table   .DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Ziel", "Einheit", "Ankunftszeit", "Einzeichnen"}) {

            Class[] types = new Class[]{
                Village.class, Village.class, Unit.class, Date.class, Boolean.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        jAttackTable.setModel(model);

        jAttackTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jAttackTable.setDefaultEditor(Date.class, new DateSpinEditor());
        jAttackTable.setDefaultEditor(Unit.class, new UnitCellEditor());
        jAttackTable.setDefaultEditor(Village.class, new VillageCellEditor());
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
        jAttackTable.getDefaultEditor(Unit.class).addCellEditorListener(attackChangedListener);
        jAttackTable.getDefaultEditor(Village.class).addCellEditorListener(attackChangedListener);
        for (Attack a : GlobalOptions.getAttacks()) {
            ((DefaultTableModel) jAttackTable.getModel()).addRow(new Object[]{a.getSource(), a.getTarget(), a.getUnit(), a.getArriveTime(), a.isShowOnMap()});
        }
    }

    private void setupDynFrame() {
        /*jDynFrameControlPanel.setupPanel(jDynFrame, true, true);
        jDynFrameControlPanel.setTitle("Toolbox");
         */
        jTabbedPane1.addTab("Entfernung", jDistancePanel);
        jTabbedPane1.addTab("Markierungen", jMarkerPanel);
        jTabbedPane1.addTab("Angriffe", jAttackPanel);
        jDynFrame.pack();
        jDynFrame.setVisible(true);
    }

    @Override
    public void setVisible(boolean v) {
        super.setVisible(v);
        mPanel.updateMap(iCenterX, iCenterY);
        double w = (double) mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        mMiniPanel.setSelection(iCenterX, iCenterY, (int) Math.rint(w), (int) Math.rint(h));
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
        jPlayerSelectionDialog = new javax.swing.JDialog();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPlayerSelection = new javax.swing.JComboBox();
        jServerSelection = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
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
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jDistanceSourceVillage = new javax.swing.JLabel();
        jDistanceTargetVillage = new javax.swing.JLabel();
        jMarkerPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMarkerTable = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jDynFrame = new javax.swing.JFrame();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jDynFrameAlwaysOnTopSelection = new javax.swing.JCheckBox();
        jAttackPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAttackTable = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
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
        jRefresh = new javax.swing.JButton();
        jMoveE1 = new javax.swing.JButton();
        jZoomInButton = new javax.swing.JButton();
        jZoomOutButton = new javax.swing.JButton();
        jMinimapPanel = new javax.swing.JPanel();
        jInfoPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();

        jDetailedInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Info"));

        jVillageInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/forbidden.gif"))); // NOI18N
        jVillageInfo.setText("jLabel3");
        jVillageInfo.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jVillageInfo.setMaximumSize(new java.awt.Dimension(54, 20));
        jVillageInfo.setMinimumSize(new java.awt.Dimension(54, 20));
        jVillageInfo.setPreferredSize(new java.awt.Dimension(54, 20));

        jPlayerInfo.setText("jLabel4");
        jPlayerInfo.setMaximumSize(new java.awt.Dimension(54, 20));
        jPlayerInfo.setMinimumSize(new java.awt.Dimension(54, 20));
        jPlayerInfo.setOpaque(true);
        jPlayerInfo.setPreferredSize(new java.awt.Dimension(54, 20));

        jAllyInfo.setText("jLabel5");
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
                    .addComponent(jVillageInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                    .addComponent(jPlayerInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                    .addComponent(jAllyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE))
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

        jPlayerSelectionDialog.setTitle("Spielerauswahl");
        jPlayerSelectionDialog.setAlwaysOnTop(true);
        jPlayerSelectionDialog.setModal(true);

        jLabel3.setText("Spielername");

        jLabel4.setText("Server");

        jButton1.setText("OK");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectPlayerEvent(evt);
            }
        });

        jButton2.setText("Abbrechen");

        jButton3.setText("Server auswählen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRefreshServerDataEvent(evt);
            }
        });

        javax.swing.GroupLayout jPlayerSelectionDialogLayout = new javax.swing.GroupLayout(jPlayerSelectionDialog.getContentPane());
        jPlayerSelectionDialog.getContentPane().setLayout(jPlayerSelectionDialogLayout);
        jPlayerSelectionDialogLayout.setHorizontalGroup(
            jPlayerSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPlayerSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPlayerSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPlayerSelectionDialogLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(30, 30, 30)
                        .addComponent(jServerSelection, 0, 318, Short.MAX_VALUE))
                    .addGroup(jPlayerSelectionDialogLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPlayerSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPlayerSelectionDialogLayout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addComponent(jPlayerSelection, 0, 318, Short.MAX_VALUE)))
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPlayerSelectionDialogLayout.setVerticalGroup(
            jPlayerSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPlayerSelectionDialogLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPlayerSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jServerSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jPlayerSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPlayerSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jDistancePanel.setMaximumSize(new java.awt.Dimension(750, 96));
        jDistancePanel.setMinimumSize(new java.awt.Dimension(750, 96));

        jSpearTime.setEditable(false);
        jSpearTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSpearTime.setText("00:00:00");
        jSpearTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jSpearTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jSpearTime.setOpaque(false);
        jSpearTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jSwordTime.setEditable(false);
        jSwordTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSwordTime.setText("00:00:00");
        jSwordTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jSwordTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jSwordTime.setOpaque(false);
        jSwordTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jSpyTime.setEditable(false);
        jSpyTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSpyTime.setText("00:00:00");
        jSpyTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jSpyTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jSpyTime.setOpaque(false);
        jSpyTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jLightTime.setEditable(false);
        jLightTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jLightTime.setText("00:00:00");
        jLightTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jLightTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jLightTime.setOpaque(false);
        jLightTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jMArcherTime.setEditable(false);
        jMArcherTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jMArcherTime.setText("00:00:00");
        jMArcherTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jMArcherTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jMArcherTime.setOpaque(false);
        jMArcherTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jAxeTime.setEditable(false);
        jAxeTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jAxeTime.setText("00:00:00");
        jAxeTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jAxeTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jAxeTime.setOpaque(false);
        jAxeTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/axe.png"))); // NOI18N

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/sword.png"))); // NOI18N

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/spy.png"))); // NOI18N

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/light.png"))); // NOI18N

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/heavy.png"))); // NOI18N

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/ram.png"))); // NOI18N

        jHeavyTime.setEditable(false);
        jHeavyTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jHeavyTime.setText("00:00:00");
        jHeavyTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jHeavyTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jHeavyTime.setOpaque(false);
        jHeavyTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/snob.png"))); // NOI18N

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/knight.png"))); // NOI18N

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/spear.png"))); // NOI18N

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/archer.png"))); // NOI18N

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/marcher.png"))); // NOI18N

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skins/symbol/cata.png"))); // NOI18N

        jBowTime.setEditable(false);
        jBowTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jBowTime.setText("00:00:00");
        jBowTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jBowTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jBowTime.setOpaque(false);
        jBowTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jRamTime.setEditable(false);
        jRamTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jRamTime.setText("00:00:00");
        jRamTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jRamTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jRamTime.setOpaque(false);
        jRamTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jCataTime.setEditable(false);
        jCataTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jCataTime.setText("00:00:00");
        jCataTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jCataTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jCataTime.setOpaque(false);
        jCataTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jSnobTime.setEditable(false);
        jSnobTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSnobTime.setText("00:00:00");
        jSnobTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jSnobTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jSnobTime.setOpaque(false);
        jSnobTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jKnightTime.setEditable(false);
        jKnightTime.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jKnightTime.setText("00:00:00");
        jKnightTime.setMaximumSize(new java.awt.Dimension(54, 20));
        jKnightTime.setMinimumSize(new java.awt.Dimension(54, 20));
        jKnightTime.setOpaque(false);
        jKnightTime.setPreferredSize(new java.awt.Dimension(54, 20));

        jLabel18.setText("<html><u>Von</u></html>");

        jLabel19.setText("<html><u>Nach</u></html>");

        jDistanceSourceVillage.setText("------");

        jDistanceTargetVillage.setText("------");

        javax.swing.GroupLayout jDistancePanelLayout = new javax.swing.GroupLayout(jDistancePanel);
        jDistancePanel.setLayout(jDistancePanelLayout);
        jDistancePanelLayout.setHorizontalGroup(
            jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDistancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDistancePanelLayout.createSequentialGroup()
                        .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING))
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
                    .addComponent(jLabel18)
                    .addComponent(jDistanceSourceVillage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jDistancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
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

        jMarkerPanel.setMaximumSize(new java.awt.Dimension(750, 305));
        jMarkerPanel.setMinimumSize(new java.awt.Dimension(750, 305));

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
        jScrollPane1.setViewportView(jMarkerTable);

        jButton6.setText("Löschen");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
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
                .addComponent(jButton6)
                .addContainerGap(211, Short.MAX_VALUE))
        );
        jMarkerPanelLayout.setVerticalGroup(
            jMarkerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMarkerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMarkerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                    .addComponent(jButton6))
                .addContainerGap())
        );

        jDynFrame.setTitle("Toolbox");

        jDynFrameAlwaysOnTopSelection.setText("Immer im Vordergrund");
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

        jAttackPanel.setMinimumSize(new java.awt.Dimension(750, 305));
        jAttackPanel.setPreferredSize(new java.awt.Dimension(750, 305));
        jAttackPanel.setRequestFocusEnabled(false);

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
        jScrollPane2.setViewportView(jAttackTable);

        jButton5.setText("Entfernen");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveAttackEvent(evt);
            }
        });

        jButton7.setText("Überprüfen");
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireValidateAttacksEvent(evt);
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
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addContainerGap())
        );
        jAttackPanelLayout.setVerticalGroup(
            jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                    .addGroup(jAttackPanelLayout.createSequentialGroup()
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DS Workbench 0.9b");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                fireFrameResizedEvent(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Navigation"));

        jMoveE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_e.png"))); // NOI18N
        jMoveE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveNE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_ne.png"))); // NOI18N
        jMoveNE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveNE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveNE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveNE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_n.png"))); // NOI18N
        jMoveN.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveN.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveN.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveNW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_nw.png"))); // NOI18N
        jMoveNW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveNW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveNW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveNW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_w.png"))); // NOI18N
        jMoveW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveSW.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_sw.png"))); // NOI18N
        jMoveSW.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveSW.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveSW.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveSW.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_s.png"))); // NOI18N
        jMoveS.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveS.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveS.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jMoveSE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/map_se.png"))); // NOI18N
        jMoveSE.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveSE.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveSE.setPreferredSize(new java.awt.Dimension(21, 21));
        jMoveSE.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveMapEvent(evt);
            }
        });

        jCenterX.setText("456");
        jCenterX.setMaximumSize(new java.awt.Dimension(40, 20));
        jCenterX.setMinimumSize(new java.awt.Dimension(40, 20));
        jCenterX.setPreferredSize(new java.awt.Dimension(40, 20));

        jLabel1.setText("X");

        jLabel2.setText("Y");

        jCenterY.setText("468");
        jCenterY.setMaximumSize(new java.awt.Dimension(40, 20));
        jCenterY.setMinimumSize(new java.awt.Dimension(40, 20));
        jCenterY.setPreferredSize(new java.awt.Dimension(40, 20));

        jRefresh.setText("Aktualisieren");
        jRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRefreshMapEvent(evt);
            }
        });

        jMoveE1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jMoveE1.setEnabled(false);
        jMoveE1.setMaximumSize(new java.awt.Dimension(21, 21));
        jMoveE1.setMinimumSize(new java.awt.Dimension(21, 21));
        jMoveE1.setPreferredSize(new java.awt.Dimension(21, 21));

        jZoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/zoom_out.png"))); // NOI18N
        jZoomInButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireZoomEvent(evt);
            }
        });

        jZoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/zoom_in.png"))); // NOI18N
        jZoomOutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireZoomEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jMoveSW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMoveS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMoveSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jZoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jZoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jMoveNW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMoveN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jMoveW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMoveE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jMoveNE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCenterX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCenterY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(63, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMoveNE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMoveN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMoveNW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMoveE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMoveW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMoveE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMoveSW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMoveS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMoveSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jZoomInButton)
                    .addComponent(jZoomOutButton))
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jCenterX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jCenterY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(jRefresh)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jMinimapPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Minimap"));
        jMinimapPanel.setLayout(new java.awt.BorderLayout());

        jInfoPanel.setLayout(new java.awt.BorderLayout());

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Spielerinformationen"));

        jLabel5.setText("<html><u>Aktueller Spieler</u></html>");

        jLabel20.setText("<html><u>Server</u></html>");

        jLabel21.setText("<html><u>Dörfer</u></html>");

        jLabel22.setText("jLabel22");

        jLabel23.setText("jLabel23");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel20)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23)
                    .addComponent(jLabel22)
                    .addComponent(jComboBox1, 0, 155, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(93, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jMinimapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jInfoPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 811, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jMinimapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE))
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
    mMiniPanel.setSelection(cx, cy, (int) Math.rint(w), (int) Math.rint(h));
    mPanel.updateMap(iCenterX, iCenterY);
    jPanel1.updateUI();
    jPanel2.updateUI();
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
    mMiniPanel.setSelection(cx, cy, (int) Math.rint(w), (int) Math.rint(h));
    jPanel1.updateUI();
    jPanel2.updateUI();
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
    mMiniPanel.setSelection(Integer.parseInt(jCenterX.getText()), Integer.parseInt(jCenterY.getText()), (int) Math.rint(w), (int) Math.rint(h));
    mPanel.repaint();
}//GEN-LAST:event_fireZoomEvent

private void fireRefreshServerDataEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRefreshServerDataEvent
    try {
        String serverSelection = (String) jServerSelection.getSelectedItem();
        if (serverSelection != null) {
            GlobalOptions.setSelectedServer(serverSelection);
            GlobalOptions.loadData(false);
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            Enumeration<Integer> tribes = GlobalOptions.getDataHolder().getTribes().keys();
            String[] names = new String[GlobalOptions.getDataHolder().getTribes().size()];
            int idx = 0;
            while (tribes.hasMoreElements()) {
                names[idx] = GlobalOptions.getDataHolder().getTribes().get(tribes.nextElement()).getName();
                idx++;
            }
            final Collator col = Collator.getInstance();
            Arrays.sort(names, null);
            for (String name : names) {
                model.addElement(name);
            }
            jPlayerSelection.setModel(model);
        }
    } catch (Exception e) {
    }
}//GEN-LAST:event_fireRefreshServerDataEvent

private void fireSelectPlayerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectPlayerEvent
    jPlayerSelectionDialog.setVisible(false);
}//GEN-LAST:event_fireSelectPlayerEvent

private void fireRemoveMarkerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveMarkerEvent
    int row = jMarkerTable.getSelectedRow();
    if (row < 0) {
        return;
    }
    int ret = JOptionPane.showConfirmDialog(this, "Ausgewählte Markierung wirklich löschen?", "Löschen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (ret == JOptionPane.YES_OPTION) {
        String value = ((MarkerCell) ((DefaultTableModel) jMarkerTable.getModel()).getValueAt(row, 0)).getMarkerName();
        GlobalOptions.getMarkers().remove(value);
        ((DefaultTableModel) jMarkerTable.getModel()).removeRow(row);
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

        while (keys.hasMoreElements()) {
            int row = keys.nextElement();
            String error = errors.get(row);
            message = "Zeile " + (row + 1) + ": " + error + "\n" + message;
        }
        JOptionPane.showMessageDialog(jDynFrame, message, "Fehler", JOptionPane.WARNING_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(jDynFrame, "Keine Fehler gefunden", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}//GEN-LAST:event_fireValidateAttacksEvent

private void fireAlwaysInFrontChangeEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlwaysInFrontChangeEvent
    jDynFrame.setAlwaysOnTop(jDynFrameAlwaysOnTopSelection.isSelected());
}//GEN-LAST:event_fireAlwaysInFrontChangeEvent

private void fireRemoveAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAttackEvent
    int row = jAttackTable.getSelectedRow();
    if (row < 0) {
        return;
    } else {
        int res = JOptionPane.showConfirmDialog(jDynFrame, "Ausgewählten Angriff wirklich löschen?", "Angriff entfernen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res != JOptionPane.YES_OPTION) {
            return;
        }
        ((DefaultTableModel) jAttackTable.getModel()).removeRow(row);
        updateAttacks();
    }
}//GEN-LAST:event_fireRemoveAttackEvent

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
            name =
                    name.replaceAll("<html>", "").replaceAll("</html>", "");
            Color c = (Color) model.getValueAt(i, 1);
            GlobalOptions.getMarkers().put(name, c);
        }

        GlobalOptions.storeMarkers();
        //update maps
        mPanel.repaint();
        mMiniPanel.redraw();
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
        double dx = 1000 / (double) mMiniPanel.getWidth() * (double) pX;
        double dy = 1000 / (double) mMiniPanel.getHeight() * (double) pY;

        int x = (int) dx;
        int y = (int) dy;
        jCenterX.setText(Integer.toString(x));
        jCenterY.setText(Integer.toString(y));
        iCenterX = x;
        iCenterY = y;
        mPanel.updateMap(iCenterX, iCenterY);

        double w = (double) mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        double h = (double) mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
        mMiniPanel.setSelection(x, y, (int) Math.rint(w), (int) Math.rint(h));
        jPanel1.updateUI();
        jPanel2.updateUI();
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
            logger.debug("No village selected. Details reset");
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
                if (GlobalOptions.getMarkers().containsKey(pVillage.getTribe().getAlly().getName())) {
                    logger.debug("Replace existing ally marker");
                    for (int i = 0; i <
                            ((DefaultTableModel) jMarkerTable.getModel()).getRowCount(); i++) {
                        MarkerCell c = (MarkerCell) ((DefaultTableModel) jMarkerTable.getModel()).getValueAt(i, 0);
                        if (c.getMarkerName().equals(pVillage.getTribe().getAlly().getName())) {
                            ((DefaultTableModel) jMarkerTable.getModel()).setValueAt(pAllyColor, i, 1);
                        }

                    }
                } else {
                    logger.debug("Add new ally marker");
                    GlobalOptions.getMarkers().put(pVillage.getTribe().getAlly().getName(), Color.WHITE);
                    p =
                            MarkerCell.factoryAllyMarker(pVillage.getTribe().getAlly().getName());
                    Color c = (pAllyColor != null) ? pAllyColor : Color.WHITE;
                    ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{p, c});
                }

            } else {
                //tribe has no ally. Skip.
                logger.info("No ally found for selected tribe. Skip marking ally");
            }
        }

        if (pMarkTribe) {
            if (GlobalOptions.getMarkers().containsKey(pVillage.getTribe().getName())) {
                logger.debug("Replace existing tribe marker");
                for (int i = 0; i <
                        ((DefaultTableModel) jMarkerTable.getModel()).getRowCount(); i++) {
                    MarkerCell c = (MarkerCell) ((DefaultTableModel) jMarkerTable.getModel()).getValueAt(i, 0);
                    if (c.getMarkerName().equals(pVillage.getTribe().getName())) {
                        ((DefaultTableModel) jMarkerTable.getModel()).setValueAt(pTribeColor, i, 1);
                    }

                }
            } else {
                logger.debug("Add new tribe marker");
                GlobalOptions.getMarkers().put(pVillage.getTribe().getName(), Color.WHITE);
                Color c = (pTribeColor != null) ? pTribeColor : Color.WHITE;
                p =
                        MarkerCell.factoryPlayerMarker(pVillage.getTribe().getName());
                ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{p, c});
            }
        }
        updateMarkers();
    }

    public void addAttack(Village pSource, Village pTarget, UnitHolder pUnit, Date pArriveTime) {
        ((DefaultTableModel) jAttackTable.getModel()).addRow(new Object[]{pSource, pTarget, pUnit, pArriveTime, false});
        updateAttacks();
    }

    @Override
    public void fireDataHolderEvent(String pMessage) {
    }

    @Override
    public void fireDataLoadedEvent() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DOMConfigurator.configure("log4j.xml");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MapFrame().setVisible(true);
            }
        });

    /*try {
    Runtime.getRuntime().exec("cmd.exe /c start http://de26.die-staemme.de/game.php?village=7462&screen=overview&intro");
    //http://de26.die-staemme.de/game.php?screen=overview_villages
    } catch (Exception e) {
    e.printStackTrace();
    }*/
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jAllyInfo;
    private javax.swing.JPanel jAttackPanel;
    private javax.swing.JTable jAttackTable;
    private javax.swing.JTextField jAxeTime;
    private javax.swing.JTextField jBowTime;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JTextField jCataTime;
    private javax.swing.JTextField jCenterX;
    private javax.swing.JTextField jCenterY;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JPanel jDetailedInfoPanel;
    private javax.swing.JPanel jDistancePanel;
    private javax.swing.JLabel jDistanceSourceVillage;
    private javax.swing.JLabel jDistanceTargetVillage;
    private javax.swing.JFrame jDynFrame;
    private javax.swing.JCheckBox jDynFrameAlwaysOnTopSelection;
    private javax.swing.JTextField jHeavyTime;
    private javax.swing.JPanel jInfoPanel;
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
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jLightTime;
    private javax.swing.JTextField jMArcherTime;
    private javax.swing.JPanel jMarkerPanel;
    private javax.swing.JTable jMarkerTable;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jPlayerInfo;
    private javax.swing.JComboBox jPlayerSelection;
    private javax.swing.JDialog jPlayerSelectionDialog;
    private javax.swing.JTextField jRamTime;
    private javax.swing.JButton jRefresh;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox jServerSelection;
    private javax.swing.JTextField jSnobTime;
    private javax.swing.JTextField jSpearTime;
    private javax.swing.JTextField jSpyTime;
    private javax.swing.JTextField jSwordTime;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel jVillageInfo;
    private javax.swing.JButton jZoomInButton;
    private javax.swing.JButton jZoomOutButton;
    // End of variables declaration//GEN-END:variables
}