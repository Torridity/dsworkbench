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
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.ColorChooserCellEditor;
import de.tor.tribes.ui.renderes.ColorChooserCellRenderer;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Color;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import net.java.dev.colorchooser.ColorChooser;
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

        try {
            GlobalOptions.initialize(false, this);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        mPanel = new MapPanel(this);
        jPanel1.add(mPanel);
        mMiniPanel = new MinimapPanel(this);
        jMinimapPanel.add(mMiniPanel);
        //jDynPanel.add(jDistancePanel);
        jMarkerTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Name", "Markierung"
                }) {

            Class[] types = new Class[]{
                java.lang.String.class, ColorChooser.class
            };
            boolean[] canEdit = new boolean[]{
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        jMarkerTable.getColumnModel().getColumn(1).setMaxWidth(75);
        GlobalOptions.getMarkers().put("Torridity", Color.WHITE);
        jMarkerTable.setDefaultRenderer(ColorChooser.class, new ColorChooserCellRenderer());
        jMarkerTable.setDefaultEditor(ColorChooser.class, new ColorChooserCellEditor());
        ((DefaultTableModel) jMarkerTable.getModel()).addRow(new Object[]{"Torridity", new ColorChooser()});
        jDynPanel.add(jMarkerPanel);

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
        jMarkerPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMarkerTable = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
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
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jSearchTerm = new javax.swing.JTextField();
        jSearchPlayer = new javax.swing.JCheckBox();
        jSearchAlly = new javax.swing.JCheckBox();
        jButton4 = new javax.swing.JButton();
        jMinimapPanel = new javax.swing.JPanel();
        jDynPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jGeneralMenu = new javax.swing.JMenu();
        jOptionsItem = new javax.swing.JMenuItem();
        jExitItem = new javax.swing.JMenuItem();
        jViewMenu = new javax.swing.JMenu();
        jWorldMapItem = new javax.swing.JMenuItem();
        jToolsMenu = new javax.swing.JMenu();
        jInfoMenu = new javax.swing.JMenu();

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

        org.jdesktop.layout.GroupLayout jDetailedInfoPanelLayout = new org.jdesktop.layout.GroupLayout(jDetailedInfoPanel);
        jDetailedInfoPanel.setLayout(jDetailedInfoPanelLayout);
        jDetailedInfoPanelLayout.setHorizontalGroup(
            jDetailedInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jDetailedInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jDetailedInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jVillageInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPlayerInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                    .add(jAllyInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE))
                .addContainerGap())
        );
        jDetailedInfoPanelLayout.setVerticalGroup(
            jDetailedInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jDetailedInfoPanelLayout.createSequentialGroup()
                .add(jVillageInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(jPlayerInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jAllyInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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

        org.jdesktop.layout.GroupLayout jPlayerSelectionDialogLayout = new org.jdesktop.layout.GroupLayout(jPlayerSelectionDialog.getContentPane());
        jPlayerSelectionDialog.getContentPane().setLayout(jPlayerSelectionDialogLayout);
        jPlayerSelectionDialogLayout.setHorizontalGroup(
            jPlayerSelectionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPlayerSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPlayerSelectionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPlayerSelectionDialogLayout.createSequentialGroup()
                        .add(jLabel4)
                        .add(30, 30, 30)
                        .add(jServerSelection, 0, 318, Short.MAX_VALUE))
                    .add(jPlayerSelectionDialogLayout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPlayerSelectionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPlayerSelectionDialogLayout.createSequentialGroup()
                                .add(jButton2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton1))
                            .add(jPlayerSelection, 0, 318, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton3))
                .addContainerGap())
        );
        jPlayerSelectionDialogLayout.setVerticalGroup(
            jPlayerSelectionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPlayerSelectionDialogLayout.createSequentialGroup()
                .add(14, 14, 14)
                .add(jPlayerSelectionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jServerSelection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPlayerSelectionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jPlayerSelection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPlayerSelectionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButton1)
                    .add(jButton2))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jDistancePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Entfernung"));

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

        org.jdesktop.layout.GroupLayout jDistancePanelLayout = new org.jdesktop.layout.GroupLayout(jDistancePanel);
        jDistancePanel.setLayout(jDistancePanelLayout);
        jDistancePanelLayout.setHorizontalGroup(
            jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jDistancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSpearTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSwordTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jBowTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jAxeTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSpyTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLightTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jMArcherTime, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(6, 6, 6)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jHeavyTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jRamTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCataTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSnobTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jKnightTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jDistancePanelLayout.setVerticalGroup(
            jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jDistancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jDistancePanelLayout.createSequentialGroup()
                        .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel14)
                            .add(jLabel7)
                            .add(jLabel15)
                            .add(jLabel8)
                            .add(jLabel6))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jSpearTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jSwordTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jBowTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jAxeTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jSpyTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jDistancePanelLayout.createSequentialGroup()
                        .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel10)
                            .add(jLabel17)
                            .add(jLabel12)
                            .add(jLabel13)
                            .add(jLabel16)
                            .add(jLabel9)
                            .add(jLabel11))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jDistancePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jMArcherTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jHeavyTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLightTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jRamTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jCataTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jSnobTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jKnightTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        jButton5.setText("Aktualisieren");

        jButton6.setText("Löschen");

        org.jdesktop.layout.GroupLayout jMarkerPanelLayout = new org.jdesktop.layout.GroupLayout(jMarkerPanel);
        jMarkerPanel.setLayout(jMarkerPanelLayout);
        jMarkerPanelLayout.setHorizontalGroup(
            jMarkerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jMarkerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jMarkerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButton5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jMarkerPanelLayout.setVerticalGroup(
            jMarkerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jMarkerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jMarkerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jMarkerPanelLayout.createSequentialGroup()
                        .add(jButton5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton6))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                fireFrameResizedEvent(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setOpaque(false);
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

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jMoveSW, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jMoveS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jMoveSE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jZoomInButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jZoomOutButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jMoveNW, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jMoveN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jMoveW, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jMoveE1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jMoveNE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jMoveE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jCenterX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jCenterY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jRefresh, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(59, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jMoveNE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jMoveN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jMoveNW, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(4, 4, 4)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jMoveE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jMoveW, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jMoveE1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jMoveSW, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jMoveS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jMoveSE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jZoomInButton)
                    .add(jZoomOutButton))
                .addContainerGap())
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jCenterX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(jCenterY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(4, 4, 4)
                .add(jRefresh)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Suche"));

        jLabel5.setText("Suchbegriff");

        jSearchPlayer.setSelected(true);
        jSearchPlayer.setText("Spieler");

        jSearchAlly.setSelected(true);
        jSearchAlly.setText("Stämme");

        jButton4.setText("Suchen");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoSearchEvent(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jSearchPlayer)
                                .add(18, 18, 18)
                                .add(jSearchAlly)
                                .addContainerGap())
                            .add(jSearchTerm, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton4)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jSearchTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jSearchPlayer)
                    .add(jSearchAlly))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 33, Short.MAX_VALUE)
                .add(jButton4))
        );

        jMinimapPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Minimap"));
        jMinimapPanel.setLayout(new java.awt.BorderLayout());

        jDynPanel.setLayout(new java.awt.BorderLayout());

        jGeneralMenu.setText("Allgemein");

        jOptionsItem.setText("Einstellungen");
        jGeneralMenu.add(jOptionsItem);

        jExitItem.setText("Beenden");
        jGeneralMenu.add(jExitItem);

        jMenuBar1.add(jGeneralMenu);

        jViewMenu.setText("Ansicht");

        jWorldMapItem.setText("Weltkarte");
        jViewMenu.add(jWorldMapItem);

        jMenuBar1.add(jViewMenu);

        jToolsMenu.setText("Tools");
        jMenuBar1.add(jToolsMenu);

        jInfoMenu.setText("Info");
        jMenuBar1.add(jInfoMenu);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jMinimapPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 270, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jDynPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 807, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jMinimapPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 233, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jDynPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
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
        cx -= mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy += mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveE) {
        cx += mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveSE) {
        cx += mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy += mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveS) {
        cy += mPanel.getHeight() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
    } else if (evt.getSource() == jMoveSW) {
        cx += mPanel.getWidth() / GlobalOptions.getSkin().getFieldWidth() * dZoomFactor;
        cy -= mPanel.getWidth() / GlobalOptions.getSkin().getFieldHeight() * dZoomFactor;
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

private void fireDoSearchEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDoSearchEvent
    if (!jSearchAlly.isSelected() && !jSearchPlayer.isSelected()) {
        JOptionPane.showMessageDialog(this, "Spieler oder/und Stamm muss für eine Suche aktiviert sein", "Fehler", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    String term = jSearchTerm.getText();
    if (term.length() < 3) {
        JOptionPane.showMessageDialog(this, "Suchbegriff muss mindestens 3 Zeichen enthalten", "Fehler", JOptionPane.WARNING_MESSAGE);
        return;
    }
    term = term.toLowerCase();

    if (jSearchPlayer.isSelected()) {
        Enumeration<Integer> tribes = GlobalOptions.getDataHolder().getTribes().keys();
        while (tribes.hasMoreElements()) {
            Integer next = tribes.nextElement();
            Tribe t = GlobalOptions.getDataHolder().getTribes().get(next);
            if (t.getName().toLowerCase().indexOf(term) >= 0) {
                System.out.println("Found Tribe: " + t.getName());
            }
        }
    }
    if (jSearchAlly.isSelected()) {
        Enumeration<Integer> allies = GlobalOptions.getDataHolder().getAllies().keys();
        while (allies.hasMoreElements()) {
            Integer next = allies.nextElement();
            Ally a = GlobalOptions.getDataHolder().getAllies().get(next);
            if (a.getName().toLowerCase().indexOf(term) >= 0) {
                System.out.println("Found Ally: " + a.getName());
            }
        }
    }

}//GEN-LAST:event_fireDoSearchEvent

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

    public void switchDynPanel(int pType) {
        jDynPanel.removeAll();
        switch (pType) {
            default:
                jDynPanel.add(jDetailedInfoPanel);
                break;
        }
    }

    public void updateDistancePanel(Village pSource, Village pTarget) {
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
            return;
        }

        int xs = pSource.getX();
        int ys = pSource.getY();
        int xt = pTarget.getX();
        int yt = pTarget.getY();

        double dist = Math.sqrt(Math.pow(xt - xs, 2) + Math.pow(yt - ys, 2));

        List<UnitHolder> units = GlobalOptions.getDataHolder().getUnits();
        for (UnitHolder unit : units) {
            double dur = unit.getSpeed() * dist;
            int hour = (int) Math.floor(dur / 60);
            dur -= hour * 60;
            int min = (int) Math.floor(dur);
            int sec = (int) Math.rint((dur - min) * 60);

            String result = "";
            if (hour < 10) {
                result += "0" + hour + ":";
            } else {
                result += hour + ":";
            }
            if (min < 10) {
                result += "0" + min + ":";
            } else {
                result += min + ":";
            }
            if (sec < 10) {
                result += "0" + sec;
            } else {
                result += sec;
            }

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

    public void updateDetailedInfoPanel(Village pVillage) {
        if (pVillage == null) {
            jPlayerInfo.setText("");
            jVillageInfo.setText("");
            jAllyInfo.setText("");
            jVillageInfo.setIcon(null);
            return;
        }
        String villageInfo = "<html><b>Dorf:</b> " + pVillage.getName() + " (" + pVillage.getX() + "|" + pVillage.getY() + "), <b>Punkte:</b> " + pVillage.getPoints() + ", <b>Bonus:</b> ";
        switch (pVillage.getType()) {
            case 1:
                villageInfo += "+ 10% </html>";
                break;
            case 2:
                villageInfo += "+ 10% </html>";
                break;
            case 3:
                villageInfo += "+ 10% </html>";
                break;
            case 4:
                villageInfo += "+ 10% </html>";
                break;
            case 5:
                villageInfo += "+ 10% </html>";
                break;
            case 6:
                villageInfo += "+ 10% </html>";
                break;
            case 7:
                villageInfo += "+ 10% </html>";
                break;
            case 8:
                villageInfo += "+ 3% </html>";
                break;
        }

        jVillageInfo.setText(villageInfo);
        jVillageInfo.setIcon(mIcons.get(pVillage.getType()));

        try {
            NumberFormat nf = NumberFormat.getInstance();
            Tribe player = pVillage.getTribe();
            String playerInfo = "<html><b>Name:</b> " + player.getName();
            playerInfo += " <b>Punkte (Rang):</b> " + nf.format(player.getPoints()) + " (" + nf.format(player.getRank()) + ")";
            playerInfo += " <b>Dörfer:</b> " + nf.format(player.getVillages()) + "</html>";
            jPlayerInfo.setText(playerInfo);

            Ally ally = player.getAlly();
            if (ally == null) {
                jAllyInfo.setText("kein Stamm");
            } else {
                String allyInfo = "<html><b>Name (Tag):</b> " + ally.getName() + " (" + ally.getTag() + ")";
                allyInfo += " <b>Punkte (Rang):</b> " + nf.format(ally.getPoints()) + " (" + nf.format(ally.getRank()) + ")";
                allyInfo += " <b>Member (Dörfer):</b> " + nf.format(ally.getMembers()) + " (" + nf.format(ally.getVillages()) + ")</html>";
                jAllyInfo.setText(allyInfo);
            }

        } catch (NullPointerException e) {
            jPlayerInfo.setText("kein Besitzer");
            jAllyInfo.setText("kein Stamm");
        }
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
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jAllyInfo;
    private javax.swing.JTextField jAxeTime;
    private javax.swing.JTextField jBowTime;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JTextField jCataTime;
    private javax.swing.JTextField jCenterX;
    private javax.swing.JTextField jCenterY;
    private javax.swing.JPanel jDetailedInfoPanel;
    private javax.swing.JPanel jDistancePanel;
    private javax.swing.JPanel jDynPanel;
    private javax.swing.JMenuItem jExitItem;
    private javax.swing.JMenu jGeneralMenu;
    private javax.swing.JTextField jHeavyTime;
    private javax.swing.JMenu jInfoMenu;
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
    private javax.swing.JMenuBar jMenuBar1;
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
    private javax.swing.JMenuItem jOptionsItem;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jPlayerInfo;
    private javax.swing.JComboBox jPlayerSelection;
    private javax.swing.JDialog jPlayerSelectionDialog;
    private javax.swing.JTextField jRamTime;
    private javax.swing.JButton jRefresh;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox jSearchAlly;
    private javax.swing.JCheckBox jSearchPlayer;
    private javax.swing.JTextField jSearchTerm;
    private javax.swing.JComboBox jServerSelection;
    private javax.swing.JTextField jSnobTime;
    private javax.swing.JTextField jSpearTime;
    private javax.swing.JTextField jSpyTime;
    private javax.swing.JTextField jSwordTime;
    private javax.swing.JMenu jToolsMenu;
    private javax.swing.JMenu jViewMenu;
    private javax.swing.JLabel jVillageInfo;
    private javax.swing.JMenuItem jWorldMapItem;
    private javax.swing.JButton jZoomInButton;
    private javax.swing.JButton jZoomOutButton;
    // End of variables declaration//GEN-END:variables
}