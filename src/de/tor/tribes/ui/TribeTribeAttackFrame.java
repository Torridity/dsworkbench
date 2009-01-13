/*
 * AllyAllyAttackFrame.java
 *
 * Created on 29. Juli 2008, 11:17
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.VillageSelectionListener;
import de.tor.tribes.util.attack.AttackManager;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Point;
import java.util.StringTokenizer;
import javax.swing.UIManager;

/**
 * @TODO "attacks per village" configurable
 * @TODO "time frame" configurable
 * @TODO reasign timeframe if no attack found
 * @author  Jejkal
 */
public class TribeTribeAttackFrame extends javax.swing.JFrame implements VillageSelectionListener {

    private static Logger logger = Logger.getLogger("AttackPlanner");
    private boolean bChooseSourceRegionMode = false;
    private boolean bChooseTargetRegionMode = false;

    /** Creates new form AllyAllyAttackFrame */
    public TribeTribeAttackFrame() {
        initComponents();
        getContentPane().setBackground(Constants.DS_BACK);
        jTransferToAttackManagerDialog.pack();
    }

    protected void setup() {

        // <editor-fold defaultstate="collapsed" desc=" Attack table setup ">

        DefaultTableModel attackModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Einheit", "Zeitrahmen"
                }) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        jAttacksTable.setModel(attackModel);
        TableRowSorter<TableModel> attackSorter = new TableRowSorter<TableModel>(jAttacksTable.getModel());
        jAttacksTable.setRowSorter(attackSorter);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Victim table setup ">

        DefaultTableModel victimModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf"
                }) {

            Class[] types = new Class[]{
                Tribe.class, Village.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        jVictimTable.setModel(victimModel);
        TableRowSorter<TableModel> victimSorter = new TableRowSorter<TableModel>(jVictimTable.getModel());
        jVictimTable.setRowSorter(victimSorter);

        // </editor-fold>        

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                r.setText("<html><b>" + r.getText() + "</b></html>");
                c.setBackground(Constants.DS_BACK);
                return c;
            }
        };

        for (int i = 0; i < jAttacksTable.getColumnCount(); i++) {
            jAttacksTable.getColumn(jAttacksTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }

        for (int i = 0; i < jVictimTable.getColumnCount(); i++) {
            jVictimTable.getColumn(jVictimTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }

        for (int i = 0; i < jResultsTable.getColumnCount(); i++) {
            jResultsTable.getColumn(jResultsTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }

        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jScrollPane2.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jScrollPane3.getViewport().setBackground(Constants.DS_BACK_LIGHT);

        try {

            // <editor-fold defaultstate="collapsed" desc=" Build target allies list ">
            Enumeration<Integer> allyKeys = DataHolder.getSingleton().getAllies().keys();
            List<Ally> allies = new LinkedList();
            while (allyKeys.hasMoreElements()) {
                allies.add(DataHolder.getSingleton().getAllies().get(allyKeys.nextElement()));
            }

            Ally[] aAllies = allies.toArray(new Ally[]{});
            allies = null;
            Arrays.sort(aAllies, Ally.CASE_INSENSITIVE_ORDER);
            DefaultComboBoxModel targetAllyModel = new DefaultComboBoxModel();
            targetAllyModel.addElement("<Kein Stamm>");
            for (Ally a : aAllies) {
                targetAllyModel.addElement(a);
            }

            jTargetAllyList.setModel(targetAllyModel);
            jTargetAllyList.setSelectedIndex(0);
            fireTargetAllyChangedEvent(null);
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Build user village list ">

            Tag[] tags = TagManager.getSingleton().getTags().toArray(new Tag[]{});
            DefaultComboBoxModel tagModel = new DefaultComboBoxModel(tags);
            tagModel.insertElementAt("Alle", 0);
            jVillageGroupChooser.setModel(tagModel);
            jVillageGroupChooser.setSelectedIndex(0);
            fireVillageGroupChangedEvent(null);
            /*Village vCurrent = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
            if (vCurrent != null) {
            Tribe tCurrent = vCurrent.getTribe();
            if (tCurrent == null) {
            logger.warn("Could not get current user village. Probably no active user is selected.");
            return;
            } else {
            jSourceVillageList.setModel(new DefaultComboBoxModel(tCurrent.getVillageList().toArray()));
            }
            }*/
            // </editor-fold>

            jArriveTime.setValue(Calendar.getInstance().getTime());

            jAttacksTable.setDefaultRenderer(Date.class, new DateCellRenderer());
            jAttacksTable.setDefaultEditor(Date.class, new DateSpinEditor());
            jAttacksTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
            jAttacksTable.setDefaultEditor(Village.class, new VillageCellEditor());

            DefaultComboBoxModel unitModel = new DefaultComboBoxModel(DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{}));
            jTroopsList.setModel(unitModel);

            jResultFrame.pack();
        } catch (Exception e) {
            logger.error("Failed to initialize TribeAttackFrame", e);
        }

        jResultsTable.setDefaultRenderer(Date.class, new DateCellRenderer());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jResultFrame = new javax.swing.JFrame();
        jScrollPane2 = new javax.swing.JScrollPane();
        jResultsTable = new javax.swing.JTable();
        jCloseResultsButton = new javax.swing.JButton();
        jCopyToClipboardAsBBButton = new javax.swing.JButton();
        jAddToAttacksButton = new javax.swing.JButton();
        jCopyToClipboardButton = new javax.swing.JButton();
        jTransferToAttackManagerDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        jAttackPlansBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jNewPlanName = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jCalculateButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttacksTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jSourceVillageLabel1 = new javax.swing.JLabel();
        jVillageGroupChooser = new javax.swing.JComboBox();
        jSourceVillageList = new javax.swing.JComboBox();
        jSourceVillageLabel = new javax.swing.JLabel();
        jSourceUnitLabel = new javax.swing.JLabel();
        jTroopsList = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        jTimeFrameLabel = new javax.swing.JLabel();
        jTimeFrame = new javax.swing.JComboBox();
        jButton8 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jTargetAllyLabel = new javax.swing.JLabel();
        jTargetTribeLabel = new javax.swing.JLabel();
        jTargetAllyList = new javax.swing.JComboBox();
        jTargetTribeList = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jTargetVillageBox = new javax.swing.JComboBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        jVictimTable = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jMaxAttacksPerVillageLabel = new javax.swing.JLabel();
        jMaxAttacksPerVillage = new javax.swing.JComboBox();
        jButton4 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jStartTimeLabel = new javax.swing.JLabel();
        jSendTime = new javax.swing.JSpinner();
        jArriveTimeLabel = new javax.swing.JLabel();
        jArriveTime = new javax.swing.JSpinner();
        jPanel9 = new javax.swing.JPanel();
        jNoNightLabel = new javax.swing.JLabel();
        jNightForbidden = new javax.swing.JCheckBox();
        jRandomizeLabel = new javax.swing.JLabel();
        jRandomizeTribes = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jResultFrame.setTitle(bundle.getString("TribeTribeAttackFrame.jResultFrame.title")); // NOI18N

        jResultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jResultsTable.setOpaque(false);
        jScrollPane2.setViewportView(jResultsTable);

        jCloseResultsButton.setText(bundle.getString("TribeTribeAttackFrame.jCloseResultsButton.text")); // NOI18N
        jCloseResultsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideResultsEvent(evt);
            }
        });

        jCopyToClipboardAsBBButton.setText(bundle.getString("TribeTribeAttackFrame.jCopyToClipboardAsBBButton.text")); // NOI18N
        jCopyToClipboardAsBBButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jCopyToClipboardAsBBButton.toolTipText")); // NOI18N
        jCopyToClipboardAsBBButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAttacksToClipboardEvent(evt);
            }
        });

        jAddToAttacksButton.setText(bundle.getString("TribeTribeAttackFrame.jAddToAttacksButton.text")); // NOI18N
        jAddToAttacksButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jAddToAttacksButton.toolTipText")); // NOI18N
        jAddToAttacksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferToAttackPlanningEvent(evt);
            }
        });

        jCopyToClipboardButton.setText(bundle.getString("TribeTribeAttackFrame.jCopyToClipboardButton.text")); // NOI18N
        jCopyToClipboardButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jCopyToClipboardButton.toolTipText")); // NOI18N
        jCopyToClipboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUnformattedAttacksToClipboardEvent(evt);
            }
        });

        javax.swing.GroupLayout jResultFrameLayout = new javax.swing.GroupLayout(jResultFrame.getContentPane());
        jResultFrame.getContentPane().setLayout(jResultFrameLayout);
        jResultFrameLayout.setHorizontalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                    .addGroup(jResultFrameLayout.createSequentialGroup()
                        .addComponent(jAddToAttacksButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCopyToClipboardButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCopyToClipboardAsBBButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCloseResultsButton)))
                .addContainerGap())
        );
        jResultFrameLayout.setVerticalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCloseResultsButton)
                    .addComponent(jCopyToClipboardAsBBButton)
                    .addComponent(jCopyToClipboardButton)
                    .addComponent(jAddToAttacksButton))
                .addContainerGap())
        );

        jTransferToAttackManagerDialog.setTitle(bundle.getString("TribeTribeAttackFrame.jTransferToAttackManagerDialog.title")); // NOI18N
        jTransferToAttackManagerDialog.setAlwaysOnTop(true);
        jTransferToAttackManagerDialog.setModal(true);

        jLabel2.setText(bundle.getString("TribeTribeAttackFrame.jLabel2.text")); // NOI18N

        jLabel3.setText(bundle.getString("TribeTribeAttackFrame.jLabel3.text")); // NOI18N

        jNewPlanName.setText(bundle.getString("TribeTribeAttackFrame.jNewPlanName.text")); // NOI18N

        jButton5.setText(bundle.getString("TribeTribeAttackFrame.jButton5.text")); // NOI18N
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferAttacksToPlanEvent(evt);
            }
        });

        jButton6.setText(bundle.getString("TribeTribeAttackFrame.jButton6.text")); // NOI18N
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelTransferEvent(evt);
            }
        });

        javax.swing.GroupLayout jTransferToAttackManagerDialogLayout = new javax.swing.GroupLayout(jTransferToAttackManagerDialog.getContentPane());
        jTransferToAttackManagerDialog.getContentPane().setLayout(jTransferToAttackManagerDialogLayout);
        jTransferToAttackManagerDialogLayout.setHorizontalGroup(
            jTransferToAttackManagerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTransferToAttackManagerDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTransferToAttackManagerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jTransferToAttackManagerDialogLayout.createSequentialGroup()
                        .addGroup(jTransferToAttackManagerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(jTransferToAttackManagerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jAttackPlansBox, 0, 267, Short.MAX_VALUE)
                            .addComponent(jNewPlanName, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTransferToAttackManagerDialogLayout.createSequentialGroup()
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5)))
                .addContainerGap())
        );
        jTransferToAttackManagerDialogLayout.setVerticalGroup(
            jTransferToAttackManagerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTransferToAttackManagerDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTransferToAttackManagerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jAttackPlansBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jTransferToAttackManagerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jNewPlanName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jTransferToAttackManagerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jButton6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTransferToAttackManagerDialog.getAccessibleContext().setAccessibleParent(null);

        setTitle(bundle.getString("TribeTribeAttackFrame.title")); // NOI18N
        setBackground(new java.awt.Color(239, 235, 223));

        jCalculateButton.setText(bundle.getString("TribeTribeAttackFrame.jCalculateButton.text")); // NOI18N
        jCalculateButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jCalculateButton.toolTipText")); // NOI18N
        jCalculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateAttackEvent(evt);
            }
        });

        jTabbedPane1.setBackground(new java.awt.Color(239, 235, 223));

        jPanel2.setBackground(new java.awt.Color(239, 235, 223));

        jAttacksTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jAttacksTable.setOpaque(false);
        jAttacksTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane1.setViewportView(jAttacksTable);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton1.toolTipText")); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackEvent(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton3.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton3.toolTipText")); // NOI18N
        jButton3.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton3.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton3.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveAttackEvent(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setOpaque(false);

        jSourceVillageLabel1.setText(bundle.getString("TribeTribeAttackFrame.jSourceVillageLabel1.text")); // NOI18N

        jVillageGroupChooser.setToolTipText(bundle.getString("TribeTribeAttackFrame.jVillageGroupChooser.toolTipText")); // NOI18N
        jVillageGroupChooser.setMaximumSize(new java.awt.Dimension(150, 20));
        jVillageGroupChooser.setMinimumSize(new java.awt.Dimension(150, 20));
        jVillageGroupChooser.setPreferredSize(new java.awt.Dimension(150, 20));
        jVillageGroupChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireVillageGroupChangedEvent(evt);
            }
        });

        jSourceVillageList.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSourceVillageList.toolTipText")); // NOI18N
        jSourceVillageList.setMaximumSize(new java.awt.Dimension(150, 20));
        jSourceVillageList.setMinimumSize(new java.awt.Dimension(150, 20));
        jSourceVillageList.setPreferredSize(new java.awt.Dimension(150, 20));

        jSourceVillageLabel.setText(bundle.getString("TribeTribeAttackFrame.jSourceVillageLabel.text")); // NOI18N

        jSourceUnitLabel.setText(bundle.getString("TribeTribeAttackFrame.jSourceUnitLabel.text")); // NOI18N

        jTroopsList.setToolTipText(bundle.getString("TribeTribeAttackFrame.jTroopsList.toolTipText")); // NOI18N
        jTroopsList.setMaximumSize(new java.awt.Dimension(150, 20));
        jTroopsList.setMinimumSize(new java.awt.Dimension(150, 20));
        jTroopsList.setPreferredSize(new java.awt.Dimension(150, 20));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSourceVillageLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSourceVillageLabel1, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(jSourceUnitLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSourceVillageList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTroopsList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jVillageGroupChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSourceVillageLabel1)
                    .addComponent(jVillageGroupChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSourceVillageLabel)
                    .addComponent(jSourceVillageList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSourceUnitLabel)
                    .addComponent(jTroopsList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setOpaque(false);

        jTimeFrameLabel.setText(bundle.getString("TribeTribeAttackFrame.jTimeFrameLabel.text")); // NOI18N

        jTimeFrame.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "egal", "Früh (6-8)", "Vormittag (8-12)", "Nachmittag (12-18)", "Abend (18-0)", "Nacht (0-6)" }));
        jTimeFrame.setToolTipText(bundle.getString("TribeTribeAttackFrame.jTimeFrame.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTimeFrameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTimeFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTimeFrameLabel)
                    .addComponent(jTimeFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(63, Short.MAX_VALUE))
        );

        jButton8.setBackground(new java.awt.Color(239, 235, 223));
        jButton8.setText(bundle.getString("TribeTribeAttackFrame.jButton8.text")); // NOI18N
        jButton8.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton8.toolTipText")); // NOI18N
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAllPlayerVillages(evt);
            }
        });

        jButton7.setBackground(new java.awt.Color(239, 235, 223));
        jButton7.setText(bundle.getString("TribeTribeAttackFrame.jButton7.text")); // NOI18N
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChooseSourceRegionEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jButton7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton8))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton8)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton7))
                .addGap(45, 45, 45))
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jPanel2.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jPanel2); // NOI18N

        jPanel3.setBackground(new java.awt.Color(239, 235, 223));

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setOpaque(false);

        jTargetAllyLabel.setText(bundle.getString("TribeTribeAttackFrame.jTargetAllyLabel.text")); // NOI18N

        jTargetTribeLabel.setText(bundle.getString("TribeTribeAttackFrame.jTargetTribeLabel.text")); // NOI18N
        jTargetTribeLabel.setMaximumSize(new java.awt.Dimension(74, 14));
        jTargetTribeLabel.setMinimumSize(new java.awt.Dimension(74, 14));
        jTargetTribeLabel.setPreferredSize(new java.awt.Dimension(74, 14));

        jTargetAllyList.setToolTipText(bundle.getString("TribeTribeAttackFrame.jTargetAllyList.toolTipText")); // NOI18N
        jTargetAllyList.setMaximumSize(new java.awt.Dimension(150, 20));
        jTargetAllyList.setMinimumSize(new java.awt.Dimension(150, 20));
        jTargetAllyList.setPreferredSize(new java.awt.Dimension(150, 20));
        jTargetAllyList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireTargetAllyChangedEvent(evt);
            }
        });

        jTargetTribeList.setToolTipText(bundle.getString("TribeTribeAttackFrame.jTargetTribeList.toolTipText")); // NOI18N
        jTargetTribeList.setMaximumSize(new java.awt.Dimension(150, 20));
        jTargetTribeList.setMinimumSize(new java.awt.Dimension(150, 20));
        jTargetTribeList.setPreferredSize(new java.awt.Dimension(150, 20));
        jTargetTribeList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireTargetTribeChangedEvent(evt);
            }
        });

        jLabel1.setText(bundle.getString("TribeTribeAttackFrame.jLabel1.text")); // NOI18N

        jTargetVillageBox.setToolTipText(bundle.getString("TribeTribeAttackFrame.jTargetVillageBox.toolTipText")); // NOI18N
        jTargetVillageBox.setMaximumSize(new java.awt.Dimension(150, 20));
        jTargetVillageBox.setMinimumSize(new java.awt.Dimension(150, 20));
        jTargetVillageBox.setPreferredSize(new java.awt.Dimension(150, 20));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jTargetAllyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTargetTribeLabel, 0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTargetVillageBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTargetTribeList, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTargetAllyList, javax.swing.GroupLayout.Alignment.TRAILING, 0, 248, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jTargetAllyList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTargetTribeList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTargetVillageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jTargetAllyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTargetTribeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jVictimTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jVictimTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane3.setViewportView(jVictimTable);

        jPanel7.setOpaque(false);

        jMaxAttacksPerVillageLabel.setText(bundle.getString("TribeTribeAttackFrame.jMaxAttacksPerVillageLabel.text")); // NOI18N

        jMaxAttacksPerVillage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        jMaxAttacksPerVillage.setToolTipText(bundle.getString("TribeTribeAttackFrame.jMaxAttacksPerVillage.toolTipText")); // NOI18N
        jMaxAttacksPerVillage.setMaximumSize(new java.awt.Dimension(150, 20));
        jMaxAttacksPerVillage.setMinimumSize(new java.awt.Dimension(150, 20));
        jMaxAttacksPerVillage.setPreferredSize(new java.awt.Dimension(150, 20));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jMaxAttacksPerVillageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jMaxAttacksPerVillage, 0, 81, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMaxAttacksPerVillageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMaxAttacksPerVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(67, Short.MAX_VALUE))
        );

        jButton4.setBackground(new java.awt.Color(239, 235, 223));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton4.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton4.toolTipText")); // NOI18N
        jButton4.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton4.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton4.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTargetVillageEvent(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton2.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton2.toolTipText")); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton2.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton2.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddTargetVillageEvent(evt);
            }
        });

        jButton9.setBackground(new java.awt.Color(239, 235, 223));
        jButton9.setText(bundle.getString("TribeTribeAttackFrame.jButton9.text")); // NOI18N
        jButton9.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton9.toolTipText")); // NOI18N
        jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAllTargetVillagesEvent(evt);
            }
        });

        jButton10.setBackground(new java.awt.Color(239, 235, 223));
        jButton10.setText(bundle.getString("TribeTribeAttackFrame.jButton10.text")); // NOI18N
        jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChooseTargetRegionEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jButton10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton9))
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton9)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton10))
                .addGap(41, 41, 41))
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jPanel3.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jPanel3); // NOI18N

        jPanel4.setBackground(new java.awt.Color(239, 235, 223));

        jPanel8.setOpaque(false);

        jStartTimeLabel.setText(bundle.getString("TribeTribeAttackFrame.jStartTimeLabel.text")); // NOI18N

        jSendTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.SECOND));
        jSendTime.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSendTime.toolTipText")); // NOI18N
        jSendTime.setMaximumSize(new java.awt.Dimension(150, 20));
        jSendTime.setMinimumSize(new java.awt.Dimension(150, 20));
        jSendTime.setPreferredSize(new java.awt.Dimension(150, 20));

        jArriveTimeLabel.setText(bundle.getString("TribeTribeAttackFrame.jArriveTimeLabel.text")); // NOI18N

        jArriveTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.SECOND));
        jArriveTime.setToolTipText(bundle.getString("TribeTribeAttackFrame.jArriveTime.toolTipText")); // NOI18N
        jArriveTime.setMaximumSize(new java.awt.Dimension(150, 20));
        jArriveTime.setMinimumSize(new java.awt.Dimension(150, 20));
        jArriveTime.setPreferredSize(new java.awt.Dimension(150, 20));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jStartTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jArriveTimeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSendTime, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStartTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSendTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jArriveTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setOpaque(false);

        jNoNightLabel.setText(bundle.getString("TribeTribeAttackFrame.jNoNightLabel.text")); // NOI18N
        jNoNightLabel.setMaximumSize(new java.awt.Dimension(74, 14));
        jNoNightLabel.setMinimumSize(new java.awt.Dimension(74, 14));
        jNoNightLabel.setPreferredSize(new java.awt.Dimension(74, 14));

        jNightForbidden.setToolTipText(bundle.getString("TribeTribeAttackFrame.jNightForbidden.toolTipText")); // NOI18N
        jNightForbidden.setOpaque(false);

        jRandomizeLabel.setText(bundle.getString("TribeTribeAttackFrame.jRandomizeLabel.text")); // NOI18N

        jRandomizeTribes.setToolTipText(bundle.getString("TribeTribeAttackFrame.jRandomizeTribes.toolTipText")); // NOI18N
        jRandomizeTribes.setOpaque(false);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRandomizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .addComponent(jNoNightLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jNightForbidden, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                    .addComponent(jRandomizeTribes, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jNoNightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jNightForbidden))
                .addGap(4, 4, 4)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRandomizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRandomizeTribes))
                .addGap(10, 10, 10))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(207, 207, 207))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(281, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jPanel4.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/settings.png")), jPanel4); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1)
                    .addComponent(jCalculateButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCalculateButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
    Village vSource = (Village) jSourceVillageList.getSelectedItem();
    UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
    String timeFrame = (String) jTimeFrame.getSelectedItem();
    ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{vSource, uSource, timeFrame});
}//GEN-LAST:event_fireAddAttackEvent

private void fireRemoveAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAttackEvent
    int[] rows = jAttacksTable.getSelectedRows();
    if ((rows != null) && (rows.length > 0)) {
        String message = "Angriff entfernen?";
        if (rows.length > 1) {
            message = rows.length + " Angriffe entfernen?";
        }
        UIManager.put("OptionPane.noButtonText", "Nein");
        UIManager.put("OptionPane.yesButtonText", "Ja");
        int res = JOptionPane.showConfirmDialog(this, message, "Angriff entfernen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.yesButtonText", "Yes");
        if (res != JOptionPane.YES_OPTION) {
            return;
        }
        for (int i = rows.length - 1; i >= 0; i--) {
            jAttacksTable.invalidate();
            int row = jAttacksTable.convertRowIndexToModel(rows[i]);
            ((DefaultTableModel) jAttacksTable.getModel()).removeRow(row);
            jAttacksTable.revalidate();
        }
    }
}//GEN-LAST:event_fireRemoveAttackEvent

private void fireCalculateAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateAttackEvent

    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    if (victimModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Keine Ziele ausgewählt", "Fehler", JOptionPane.ERROR_MESSAGE);
        return;
    }

    List<Village> victimVillages = new LinkedList<Village>();
    for (int i = 0; i < victimModel.getRowCount(); i++) {
        victimVillages.add((Village) victimModel.getValueAt(i, 1));
    }

    Hashtable<Village, Hashtable<Village, UnitHolder>> attacks = new Hashtable<Village, Hashtable<Village, UnitHolder>>();
    List<Village> notAssigned = new LinkedList<Village>();
    Hashtable<Tribe, Integer> attacksPerTribe = new Hashtable<Tribe, Integer>();
    for (int i = 0; i < jAttacksTable.getRowCount(); i++) {
        Village vSource = (Village) jAttacksTable.getValueAt(i, 0);
        UnitHolder uSource = (UnitHolder) jAttacksTable.getValueAt(i, 1);
        String sTimeFrame = (String) jAttacksTable.getValueAt(i, 2);
        //time when the fist attacks should begin
        long minSendTime = ((Date) jSendTime.getValue()).getTime();
        //time frame for sending troops
        int timeFrame = ((DefaultComboBoxModel) jTimeFrame.getModel()).getIndexOf(sTimeFrame);
        //time when the attacks should arrive
        long arrive = ((Date) jArriveTime.getValue()).getTime();
        //max. number of attacks per target village
        int maxAttacksPerVillage = jMaxAttacksPerVillage.getSelectedIndex() + 1;

        Village vTarget = null;

        //search all tribes and villages for targets
        for (Village v : victimVillages) {
            double time = DSCalculator.calculateMoveTimeInSeconds(vSource, v, uSource.getSpeed());
            long sendTime = arrive - (long) time * 1000;
            //check if attack is somehow possible

            if (sendTime > minSendTime) {
                //check time frame
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(sendTime);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                int second = c.get(Calendar.SECOND);
                boolean inTimeFrame = false;
                switch (timeFrame) {
                    case 1: {
                        //6 to 8
                        if ((hour >= 6) && ((hour <= 7) && (minute <= 59) && (second <= 59))) {
                            inTimeFrame = true;
                        }
                        break;
                    }
                    case 2: {
                        //8 to 12
                        if ((hour >= 8) && ((hour <= 11) && (minute <= 59) && (second <= 59))) {
                            inTimeFrame = true;
                        }
                        break;
                    }
                    case 3: {
                        //12 to 18
                        if ((hour >= 12) && ((hour <= 17) && (minute <= 59) && (second <= 59))) {
                            inTimeFrame = true;
                        }
                        break;
                    }
                    case 4: {
                        //18 to 0
                        if ((hour >= 18) && ((hour <= 23) && (minute <= 59) && (second <= 59))) {
                            inTimeFrame = true;
                        }
                        break;
                    }
                    case 5: {
                        //0 to 6
                        if ((hour >= 0) && (hour < 6)) {
                            inTimeFrame = true;
                        }
                        break;
                    }
                    default: {
                        //doesn't matter
                        inTimeFrame = true;
                    }
                }

                //check sleep mode of still in time frame
                if (inTimeFrame) {
                    if (jNightForbidden.isSelected()) {
                        hour = c.get(Calendar.HOUR_OF_DAY);
                        int min = c.get(Calendar.MINUTE);
                        int sec = c.get(Calendar.SECOND);
                        if (((hour <= 23) && (min <= 59) && (sec <= 59)) && ((hour >= 8) && (min >= 0) && (sec >= 0))) {
                            //time between 23:59:59 and 08:00:00, outside night mode
                            } else {
                            inTimeFrame = false;
                        }
                    }
                }

                if (inTimeFrame) {
                    //only calculate if time is in time frame
                    //get list of source villages for current target
                    Hashtable<Village, UnitHolder> attacksForVillage = attacks.get(v);
                    if (attacksForVillage == null) {
                        //no attack found for this village
                        //get number of attacks on this tribe
                        Integer cnt = attacksPerTribe.get(v.getTribe());
                        if (cnt == null) {
                            //no attacks on this tribe yet
                            cnt = 0;
                        }
                        //create new table of attacks
                        attacksForVillage = new Hashtable<Village, UnitHolder>();
                        attacksForVillage.put(vSource, uSource);
                        attacks.put(v, attacksForVillage);
                        attacksPerTribe.put(v.getTribe(), cnt + 1);
                        vTarget = v;
                    } else {
                        //there are already attacks on this village
                        if (attacksForVillage.keySet().size() < maxAttacksPerVillage) {
                            //more attacks on this village are allowed
                            Integer cnt = attacksPerTribe.get(v.getTribe());
                            if (cnt == null) {
                                cnt = 0;
                            }
                            //max number of attacks neither for villages nor for player reached
                            attacksForVillage.put(vSource, uSource);
                            attacksPerTribe.put(v.getTribe(), cnt + 1);
                            vTarget = v;
                        } else {
                            //max number of attacks per village reached, continue search
                        }
                    }
                }
            }
            if (vTarget != null) {
                break;
            }
        }

        if (vTarget == null) {
            notAssigned.add(vSource);
        }
    }

    showResults(attacks);
    if (notAssigned.size() > 0) {
        String notAssignedMessage = "Für das Dorf " + notAssigned.get(0) + " konnte kein Ziel ";
        if (notAssigned.size() > 1) {
            notAssignedMessage = "Für " + notAssigned.size() + " Dörfer konnten keine Ziele ";
        }
        notAssignedMessage += "gefunden werden.\nDu kannst nun wie folgt vorgehen:\n";
        notAssignedMessage += "   * Veränderung der Abschick-, Ankunftzeit oder der Zeitrahmen\n";
        notAssignedMessage += "   * Deaktivieren der Nacht-Sperre\n";
        notAssignedMessage += "   * Mehr Angriffe auf ein gegnerisches Dorf erlauben\n";
        notAssignedMessage += "   * Diese Meldung ignorieren\n";
        JOptionPane.showMessageDialog(jResultFrame, notAssignedMessage, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}//GEN-LAST:event_fireCalculateAttackEvent

private void fireHideResultsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideResultsEvent
    jResultFrame.setVisible(false);
}//GEN-LAST:event_fireHideResultsEvent

private void fireTransferToAttackPlanningEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferToAttackPlanningEvent
    jNewPlanName.setText("");
    Enumeration<String> plans = AttackManager.getSingleton().getPlans();
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    while (plans.hasMoreElements()) {
        model.addElement(plans.nextElement());
    }
    jAttackPlansBox.setModel(model);
    jTransferToAttackManagerDialog.setLocationRelativeTo(jResultFrame);
    jTransferToAttackManagerDialog.setVisible(true);
}//GEN-LAST:event_fireTransferToAttackPlanningEvent

private void fireAttacksToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAttacksToClipboardEvent
    try {
        UIManager.put("OptionPane.noButtonText", "Nein");
        UIManager.put("OptionPane.yesButtonText", "Ja");
        boolean extended = (JOptionPane.showConfirmDialog(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION);
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.yesButtonText", "Yes");

        String sUrl = ServerManager.getServerURL(GlobalOptions.getSelectedServer());

        DefaultTableModel resultModel = (DefaultTableModel) jResultsTable.getModel();
        StringBuffer buffer = new StringBuffer();
        if (extended) {
            buffer.append("[u][size=12]Angriffsplan[/size][/u]\n\n");
        } else {
            buffer.append("[u]Angriffsplan[/u]\n\n");
        }
        for (int i = 0; i < resultModel.getRowCount(); i++) {
            Village sVillage = (Village) resultModel.getValueAt(i, 0);
            UnitHolder sUnit = (UnitHolder) resultModel.getValueAt(i, 1);
            Village tVillage = (Village) resultModel.getValueAt(i, 2);
            Date dTime = (Date) resultModel.getValueAt(i, 3);
            String time = null;
            if (extended) {
                time = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(dTime);
            } else {
                time = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.SSS'[/color]'").format(dTime);
            }
            buffer.append("Angriff aus ");
            buffer.append(sVillage.toBBCode());
            buffer.append(" mit ");
            if (extended) {
                buffer.append("[img]" + sUrl + "/graphic/unit/unit_" + sUnit.getPlainName() + ".png[/img]");
            } else {
                buffer.append(sUnit.getName());
            }
            buffer.append(" auf ");
            if (tVillage.getTribe() != null) {
                buffer.append(tVillage.getTribe().toBBCode());
            } else {
                buffer.append("Barbaren");
            }
            buffer.append(" in ");
            buffer.append(tVillage.toBBCode());
            buffer.append(" am ");
            buffer.append(time);
            buffer.append("\n");
        }
        if (extended) {
            buffer.append("\n[size=8]Erstellt am ");
            buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
            buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS Workbench ");
            buffer.append(Constants.VERSION + Constants.VERSION_ADDITION + "[/url][/size]\n");
        } else {
            buffer.append("\nErstellt am ");
            buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
            buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS Workbench ");
            buffer.append(Constants.VERSION + Constants.VERSION_ADDITION + "[/url]\n");
        }
        String b = buffer.toString();
        StringTokenizer t = new StringTokenizer(b, "[");
        int cnt = t.countTokens();
        if (cnt > 500) {
            UIManager.put("OptionPane.noButtonText", "Nein");
            UIManager.put("OptionPane.yesButtonText", "Ja");
            if (JOptionPane.showConfirmDialog(this, "Die ausgewählten Angriffe benötigen mehr als 500 BB-Codes\n" +
                    "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                UIManager.put("OptionPane.noButtonText", "No");
                UIManager.put("OptionPane.yesButtonText", "Yes");
                return;
            }
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.yesButtonText", "Yes");
        }


        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
        String result = "Daten in Zwischenablage kopiert.";
        JOptionPane.showMessageDialog(jResultFrame, result, "Information", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
        logger.error("Failed to copy data to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPane.showMessageDialog(jResultFrame, result, "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_fireAttacksToClipboardEvent

private void fireUnformattedAttacksToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUnformattedAttacksToClipboardEvent
    try {
        DefaultTableModel resultModel = (DefaultTableModel) jResultsTable.getModel();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < resultModel.getRowCount(); i++) {
            Village sVillage = (Village) resultModel.getValueAt(i, 0);
            UnitHolder sUnit = (UnitHolder) resultModel.getValueAt(i, 1);
            Village tVillage = (Village) resultModel.getValueAt(i, 2);
            Date dTime = (Date) resultModel.getValueAt(i, 3);
            String time = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(dTime);
            buffer.append(sVillage);
            buffer.append("\t");
            buffer.append(sUnit);
            buffer.append("\t");
            buffer.append(tVillage.getTribe());
            buffer.append("\t");
            buffer.append(tVillage);
            buffer.append("\t");
            buffer.append(time);
            buffer.append("\n");
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(buffer.toString()), null);
        String result = "Daten in Zwischenablage kopiert.";
        JOptionPane.showMessageDialog(jResultFrame, result, "Information", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
        logger.error("Failed to copy data to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPane.showMessageDialog(jResultFrame, result, "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_fireUnformattedAttacksToClipboardEvent

private void fireAddAllPlayerVillages(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAllPlayerVillages
    Village current = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
    UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
    String timeFrame = (String) jTimeFrame.getSelectedItem();
    jAttacksTable.invalidate();
    boolean error = false;
    if (current != null) {
        Tribe t = current.getTribe();
        if (t != null) {
            for (Village v : t.getVillageList()) {
                ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{v, uSource, timeFrame});
            }
        } else {
            error = true;
        }
    } else {
        error = true;
    }
    jAttacksTable.revalidate();
    if (error) {
        JOptionPane.showMessageDialog(this, "Es ist ein Fehler bei der Bestimmung des verwendeten In-Game Accounts aufgetreten.\nBitte überprüfe deine Servereinstellungen.", "Fehler", JOptionPane.ERROR_MESSAGE);
    }

}//GEN-LAST:event_fireAddAllPlayerVillages

private void fireTargetAllyChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireTargetAllyChangedEvent
    Ally a = null;
    try {
        a = (Ally) jTargetAllyList.getSelectedItem();
    } catch (Exception e) {
    }

    if (a != null) {
        //ally selected
        Tribe[] tribes = a.getTribes().toArray(new Tribe[]{});
        if ((tribes != null) && (tribes.length != 0)) {
            Arrays.sort(tribes, Tribe.CASE_INSENSITIVE_ORDER);
            jTargetTribeList.setModel(new DefaultComboBoxModel(tribes));
            jTargetTribeList.setSelectedIndex(0);
            fireTargetTribeChangedEvent(null);
        } else {
            jTargetTribeList.setModel(new DefaultComboBoxModel());
            fireTargetTribeChangedEvent(null);
        }
    } else {
        //no ally selected, show no-ally tribes
        Enumeration<Integer> tribeIDs = DataHolder.getSingleton().getTribes().keys();
        List<Tribe> noAlly = new LinkedList<Tribe>();
        while (tribeIDs.hasMoreElements()) {
            Tribe t = DataHolder.getSingleton().getTribes().get(tribeIDs.nextElement());
            if (t.getAlly() == null) {
                noAlly.add(t);
            }
        }
        Tribe[] noAllyTribes = noAlly.toArray(new Tribe[]{});
        Arrays.sort(noAllyTribes, Tribe.CASE_INSENSITIVE_ORDER);
        jTargetTribeList.setModel(new DefaultComboBoxModel(noAllyTribes));
        jTargetTribeList.setSelectedIndex(0);
        fireTargetTribeChangedEvent(null);
    }
}//GEN-LAST:event_fireTargetAllyChangedEvent

private void fireRemoveTargetVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveTargetVillageEvent
    int[] rows = jVictimTable.getSelectedRows();
    if ((rows != null) && (rows.length > 0)) {
        String message = "Ziel entfernen?";
        if (rows.length > 1) {
            message = rows.length + " Ziele entfernen?";
        }
        UIManager.put("OptionPane.noButtonText", "Nein");
        UIManager.put("OptionPane.yesButtonText", "Ja");
        int res = JOptionPane.showConfirmDialog(this, message, "Ziel entfernen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.yesButtonText", "Yes");
        if (res != JOptionPane.YES_OPTION) {
            return;
        }
        for (int i = rows.length - 1; i >= 0; i--) {
            jVictimTable.invalidate();
            int row = jVictimTable.convertRowIndexToModel(rows[i]);
            ((DefaultTableModel) jVictimTable.getModel()).removeRow(row);
            jVictimTable.revalidate();
        }
    }
}//GEN-LAST:event_fireRemoveTargetVillageEvent

private void fireAddTargetVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddTargetVillageEvent
    Village village = (Village) jTargetVillageBox.getSelectedItem();
    if (village == null) {
        return;
    }
    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    jVictimTable.invalidate();
    victimModel.addRow(new Object[]{village.getTribe(), village});
    jVictimTable.revalidate();
    jVictimTable.updateUI();
}//GEN-LAST:event_fireAddTargetVillageEvent

private void fireAddAllTargetVillagesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAllTargetVillagesEvent
    Tribe target = (Tribe) jTargetTribeList.getSelectedItem();
    if (target == null) {
        return;
    }
    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    jVictimTable.invalidate();
    for (Village v : target.getVillageList()) {
        victimModel.addRow(new Object[]{target, v});
    }
    jVictimTable.revalidate();
    jVictimTable.updateUI();
}//GEN-LAST:event_fireAddAllTargetVillagesEvent

private void fireTargetTribeChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireTargetTribeChangedEvent
    try {
        Tribe t = (Tribe) jTargetTribeList.getSelectedItem();
        if (t != null) {
            Village[] villages = t.getVillageList().toArray(new Village[]{});
            Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
            jTargetVillageBox.setModel(new DefaultComboBoxModel(villages));
        } else {
            jTargetVillageBox.setModel(new DefaultComboBoxModel());
        }
    } catch (Exception e) {
        jTargetVillageBox.setModel(new DefaultComboBoxModel());
    }
}//GEN-LAST:event_fireTargetTribeChangedEvent

private void fireVillageGroupChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireVillageGroupChangedEvent
    Tag t = null;
    try {
        t = (Tag) jVillageGroupChooser.getSelectedItem();
    } catch (Exception e) {
        //first element "All" selected
        List<Village> playerVillages = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe().getVillageList();
        Village[] villages = playerVillages.toArray(new Village[]{});
        Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
        jSourceVillageList.setModel(new DefaultComboBoxModel(villages));
        return;
    }

    Tribe current = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe();
    List<Village> selectedVillages = new LinkedList<Village>();
    for (Village v : current.getVillageList()) {
        for (Tag ts : TagManager.getSingleton().getTags(v)) {
            if (t.getName().equals(ts.getName())) {
                if (!selectedVillages.contains(v)) {
                    selectedVillages.add(v);
                }
            }
        }
    }
    Village[] villages = selectedVillages.toArray(new Village[]{});
    Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
    jSourceVillageList.setModel(new DefaultComboBoxModel(villages));
}//GEN-LAST:event_fireVillageGroupChangedEvent

private void fireTransferAttacksToPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferAttacksToPlanEvent
    String planName = jNewPlanName.getText();
    if (planName.length() < 1) {
        int idx = jAttackPlansBox.getSelectedIndex();
        if (idx < 0) {
            planName = null;
        } else {
            planName = (String) jAttackPlansBox.getSelectedItem();
        }
    }
    if (AttackManager.getSingleton().getAttackPlan(planName) == null) {
        AttackManager.getSingleton().addEmptyPlan(planName);
        DSWorkbenchAttackFrame.getSingleton().buildAttackPlanList();
    }

    if (logger.isDebugEnabled()) {
        logger.debug("Adding attacks to plan '" + planName + "'");
    }

    DefaultTableModel resultModel = (DefaultTableModel) jResultsTable.getModel();
    for (int i = 0; i < resultModel.getRowCount(); i++) {
        Village source = (Village) resultModel.getValueAt(i, 0);
        UnitHolder unit = (UnitHolder) resultModel.getValueAt(i, 1);
        Village target = (Village) resultModel.getValueAt(i, 2);
        Date sendTime = (Date) resultModel.getValueAt(i, 3);
        long arriveTime = sendTime.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(source, target, unit.getSpeed()) * 1000);
        AttackManager.getSingleton().addAttackFast(source, target, unit, new Date(arriveTime), planName);
    }
    AttackManager.getSingleton().forceUpdate(planName);
    jTransferToAttackManagerDialog.setVisible(false);
}//GEN-LAST:event_fireTransferAttacksToPlanEvent

private void fireCancelTransferEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelTransferEvent
    jTransferToAttackManagerDialog.setVisible(false);
}//GEN-LAST:event_fireCancelTransferEvent

private void fireChooseSourceRegionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChooseSourceRegionEvent
    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
    MapPanel.getSingleton().setVillageSelectionListener(this);
    DSWorkbenchMainFrame.getSingleton().toFront();
    DSWorkbenchMainFrame.getSingleton().requestFocus();
    bChooseSourceRegionMode = true;
}//GEN-LAST:event_fireChooseSourceRegionEvent

private void fireChooseTargetRegionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChooseTargetRegionEvent
    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
    MapPanel.getSingleton().setVillageSelectionListener(this);
    Tribe victim = null;
    try {
        victim = (Tribe) jTargetTribeList.getSelectedItem();
    } catch (Exception e) {
    }
    if (victim == null) {
        JOptionPane.showMessageDialog(this, "Kein gültiger Spieler ausgewählt.", "Fehler", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    //calculate mass of villages and center to it
    Point com = DSCalculator.calculateCenterOfMass(victim.getVillageList());
    DSWorkbenchMainFrame.getSingleton().centerPosition(com.x, com.y);
    DSWorkbenchMainFrame.getSingleton().toFront();
    DSWorkbenchMainFrame.getSingleton().requestFocus();
    bChooseTargetRegionMode = true;
}//GEN-LAST:event_fireChooseTargetRegionEvent

    private void showResults(Hashtable<Village, Hashtable<Village, UnitHolder>> pAttacks) {
        DefaultTableModel resultModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Truppen", "Ziel", "Startzeit"}) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Village.class, Date.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        Enumeration<Village> targets = pAttacks.keys();
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            Hashtable<Village, UnitHolder> sources = pAttacks.get(target);
            Enumeration<Village> sourceEnum = sources.keys();
            while (sourceEnum.hasMoreElements()) {
                Village source = sourceEnum.nextElement();
                UnitHolder unit = sources.get(source);
                long targetTime = ((Date) jArriveTime.getValue()).getTime();
                long startTime = targetTime - (long) DSCalculator.calculateMoveTimeInSeconds(source, target, unit.getSpeed()) * 1000;
                resultModel.addRow(new Object[]{source, unit, target, new Date(startTime)});
            }
        }
        jResultsTable.setModel(resultModel);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jResultsTable.getModel());
        jResultsTable.setRowSorter(sorter);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                String t = ((DefaultTableCellRenderer) c).getText();
                ((DefaultTableCellRenderer) c).setText(t);
                c.setBackground(Constants.DS_BACK);
                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                r.setText("<html><b>" + r.getText() + "</b></html>");
                return c;
            }
        };

        for (int i = 0; i < jResultsTable.getColumnCount(); i++) {
            jResultsTable.getColumn(jResultsTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        jResultFrame.setVisible(true);
    }

    @Override
    public void fireSelectionFinishedEvent(Point vStart, Point vEnd) {
        if (bChooseSourceRegionMode) {
            Tribe you = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe();
            UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
            String timeFrame = (String) jTimeFrame.getSelectedItem();
            jAttacksTable.invalidate();
            for (int x = vStart.x; x <= vEnd.x; x++) {
                for (int y = vStart.y; y <= vEnd.y; y++) {
                    Village v = DataHolder.getSingleton().getVillages()[x][y];
                    if (v != null) {
                        Tribe t = v.getTribe();
                        if (t != null) {
                            if (t.equals(you)) {
                                ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{v, uSource, timeFrame});
                            }
                        }
                    }
                }
            }
            jAttacksTable.revalidate();
        } else if (bChooseTargetRegionMode) {
            Tribe victim = (Tribe) jTargetTribeList.getSelectedItem();
            jVictimTable.invalidate();
            for (int x = vStart.x; x <= vEnd.x; x++) {
                for (int y = vStart.y; y <= vEnd.y; y++) {
                    Village v = DataHolder.getSingleton().getVillages()[x][y];
                    if (v != null) {
                        Tribe t = v.getTribe();
                        if (t != null) {
                            if (t.equals(victim)) {
                                ((DefaultTableModel) jVictimTable.getModel()).addRow(new Object[]{t, v});
                            }
                        }
                    }
                }
            }
            jVictimTable.revalidate();
            jVictimTable.updateUI();
        }
        bChooseSourceRegionMode = false;
        bChooseTargetRegionMode = false;
        toFront();
        requestFocus();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TribeTribeAttackFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAddToAttacksButton;
    private javax.swing.JSpinner jArriveTime;
    private javax.swing.JLabel jArriveTimeLabel;
    private javax.swing.JComboBox jAttackPlansBox;
    private javax.swing.JTable jAttacksTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JButton jCloseResultsButton;
    private javax.swing.JButton jCopyToClipboardAsBBButton;
    private javax.swing.JButton jCopyToClipboardButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox jMaxAttacksPerVillage;
    private javax.swing.JLabel jMaxAttacksPerVillageLabel;
    private javax.swing.JTextField jNewPlanName;
    private javax.swing.JCheckBox jNightForbidden;
    private javax.swing.JLabel jNoNightLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel jRandomizeLabel;
    private javax.swing.JCheckBox jRandomizeTribes;
    private javax.swing.JFrame jResultFrame;
    private javax.swing.JTable jResultsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSpinner jSendTime;
    private javax.swing.JLabel jSourceUnitLabel;
    private javax.swing.JLabel jSourceVillageLabel;
    private javax.swing.JLabel jSourceVillageLabel1;
    private javax.swing.JComboBox jSourceVillageList;
    private javax.swing.JLabel jStartTimeLabel;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel jTargetAllyLabel;
    private javax.swing.JComboBox jTargetAllyList;
    private javax.swing.JLabel jTargetTribeLabel;
    private javax.swing.JComboBox jTargetTribeList;
    private javax.swing.JComboBox jTargetVillageBox;
    private javax.swing.JComboBox jTimeFrame;
    private javax.swing.JLabel jTimeFrameLabel;
    private javax.swing.JDialog jTransferToAttackManagerDialog;
    private javax.swing.JComboBox jTroopsList;
    private javax.swing.JTable jVictimTable;
    private javax.swing.JComboBox jVillageGroupChooser;
    // End of variables declaration//GEN-END:variables
}