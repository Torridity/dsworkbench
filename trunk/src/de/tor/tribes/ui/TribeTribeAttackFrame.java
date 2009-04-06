/*
 * AllyAllyAttackFrame.java
 *
 * Created on 29. Juli 2008, 11:17
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Fake;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.tree.AllyNode;
import de.tor.tribes.ui.tree.DSWorkbenchTreeExpansionListener;
import de.tor.tribes.ui.tree.NodeCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.VillageSelectionListener;
import de.tor.tribes.util.algo.AbstractAttackAlgorithm;
import de.tor.tribes.util.algo.AllInOne;
import de.tor.tribes.util.algo.Blitzkrieg;
import de.tor.tribes.util.algo.BruteForce;
import de.tor.tribes.util.algo.DistanceMapping;
import de.tor.tribes.util.algo.TimeFrame;
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
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.Color;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import javax.swing.UIManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.Collections;
import javax.swing.JSpinner.DateEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * @TODO (1.4) Change AG assignment for Blitzkrieg
 * @TODO (1.4) Combination of groups for source selection
 * @author Jejkal
 */
public class TribeTribeAttackFrame extends javax.swing.JFrame implements VillageSelectionListener {

    private static Logger logger = Logger.getLogger("AttackPlanner");
    private boolean bChooseSourceRegionMode = false;
    private boolean bChooseTargetRegionMode = false;

    /** Creates new form TribeTribeAttackFrame */
    public TribeTribeAttackFrame() {
        initComponents();
        getContentPane().setBackground(Constants.DS_BACK);
        jTransferToAttackManagerDialog.pack();
        jSendTimeFrame.setMinimumValue(0);
        jSendTimeFrame.setSliderBackground(Constants.DS_BACK);
        jSendTimeFrame.setMaximumColor(Constants.DS_BACK_LIGHT);
        jSendTimeFrame.setMinimumColor(Constants.DS_BACK_LIGHT);
        jSendTimeFrame.setMaximumValue(24);
        jSendTimeFrame.setSegmentSize(1);
        jSendTimeFrame.setUnit("h");
        jSendTimeFrame.setDecimalFormater(new DecimalFormat("##"));
        jSendTimeFrame.setBackground(jSettingsPanel.getBackground());

        jSnobDistance.setMinimumValue(1);
        jSnobDistance.setSliderBackground(Color.DARK_GRAY);
        jSnobDistance.setMaximumColor(Color.LIGHT_GRAY);
        jSnobDistance.setMinimumColor(Color.LIGHT_GRAY);
        jSnobDistance.setMaximumValue(70);
        jSnobDistance.setSegmentSize(1);
        jSnobDistance.setUnit(" Feld(er)");
        jSnobDistance.setDecimalFormater(new DecimalFormat("##"));
        jSnobDistance.setBackground(jSettingsPanel.getBackground());
        jSnobDistance.setMinimumColoredValue(1);
        jSnobDistance.setMaximumColoredValue(3);
        jAxeField.setText("6000");
        jLightField.setText("3200");
        jMarcherField.setText("0");
        jHeavyField.setText("0");
        jRamField.setText("300");
        jCataField.setText("10");
        jOffStrengthFrame.pack();
        jSendTime.setEditor(new DateEditor(jSendTime, "dd.MM.yy HH:mm:ss"));
        jArriveTime.setEditor(new DateEditor(jSendTime, "dd.MM.yy HH:mm:ss"));

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelp(jSourcePanel, "pages.attack_planer_source", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelp(jTargetPanel, "pages.attack_planer_target", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelp(jSettingsPanel, "pages.attack_planer_settings", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(jResultFrame.getRootPane(), "pages.attack_planer_results", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.attack_planer", GlobalOptions.getHelpBroker().getHelpSet());
    // </editor-fold>
    }

    protected void setup() {

        // <editor-fold defaultstate="collapsed" desc=" Attack table setup ">

        DefaultTableModel attackModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Einheit", "Fake"
                }) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Boolean.class
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

            jSendTime.repaint();//.updateUI();
            jArriveTime.repaint();//.updateUI();
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

        System.out.println("Setting up tree");
        long s = System.currentTimeMillis();
        Enumeration<Integer> allyIDs = DataHolder.getSingleton().getAllies().keys();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Herkunft");
        DefaultMutableTreeNode allAllies = new DefaultMutableTreeNode("Alle Stämme");
        root.add(allAllies);
        while (allyIDs.hasMoreElements()) {
            Integer allyID = allyIDs.nextElement();
            Ally a = DataHolder.getSingleton().getAllies().get(allyID);
            AllyNode allyNode = new AllyNode(a);
            allAllies.add(allyNode);
            allyNode.add(new DefaultMutableTreeNode(""));
            //DefaultMutableTreeNode allyNode = new DefaultMutableTreeNode(a);
            /*for (Tribe t : a.getTribes()) {
                DSWorkbenchTreeNode<Tribe> tribeNode = new DSWorkbenchTreeNode<Tribe>(t);
                allyNode.add(tribeNode);
            }*/

           // allTribes.add(allyNode);

        }
        DefaultTreeModel model = new DefaultTreeModel(root);
        jTree1.setModel(model);
        jTree1.setCellRenderer(new NodeCellRenderer());
        jTree1.addTreeExpansionListener(new DSWorkbenchTreeExpansionListener(model));
        System.out.println("Dur: " + (System.currentTimeMillis() - s));
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
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTargetsBar = new javax.swing.JProgressBar();
        jLabel7 = new javax.swing.JLabel();
        jEnoblementsBar = new javax.swing.JProgressBar();
        jLabel9 = new javax.swing.JLabel();
        jFullOffsBar = new javax.swing.JProgressBar();
        jAttacksBar = new javax.swing.JProgressBar();
        jLabel10 = new javax.swing.JLabel();
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
        jOffStrengthFrame = new javax.swing.JFrame();
        jAxeField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLightField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jMarcherField = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jHeavyField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jRamField = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jCataField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jToleranceSlider = new javax.swing.JSlider();
        jAxeRange = new javax.swing.JTextField();
        jLightRange = new javax.swing.JTextField();
        jMarcherRange = new javax.swing.JTextField();
        jHeavyRange = new javax.swing.JTextField();
        jRamRange = new javax.swing.JTextField();
        jCataRange = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jStrengthField = new javax.swing.JTextField();
        jStrengthRange = new javax.swing.JTextField();
        jToleranceValue = new javax.swing.JTextField();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jCalculateButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jSourcePanel = new javax.swing.JPanel();
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
        jSourceVillageLabel2 = new javax.swing.JLabel();
        jSourceVillageContinentChooser = new javax.swing.JComboBox();
        jButton8 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jMarkAsFakeBox = new javax.swing.JCheckBox();
        jSetFakeButton = new javax.swing.JButton();
        jSetNoFakeButton = new javax.swing.JButton();
        jTargetPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jTargetAllyLabel = new javax.swing.JLabel();
        jTargetTribeLabel = new javax.swing.JLabel();
        jTargetAllyList = new javax.swing.JComboBox();
        jTargetTribeList = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jTargetVillageBox = new javax.swing.JComboBox();
        jTargetTribeLabel1 = new javax.swing.JLabel();
        jTargetVillageContinentChooser = new javax.swing.JComboBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        jVictimTable = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jSettingsPanel = new javax.swing.JPanel();
        jAlgorithmChooser = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jArriveTimeLabel = new javax.swing.JLabel();
        jStartTimeLabel = new javax.swing.JLabel();
        jSendTime = new javax.swing.JSpinner();
        jArriveTime = new javax.swing.JSpinner();
        jMaxAttacksPerVillageLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jMaxAttacksPerVillage = new javax.swing.JTextField();
        jCleanOffs = new javax.swing.JTextField();
        jNoNightLabel = new javax.swing.JLabel();
        jNightForbidden = new javax.swing.JCheckBox();
        jRandomizeLabel = new javax.swing.JLabel();
        jRandomizeTribes = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jSnobDistance = new com.visutools.nav.bislider.BiSlider();
        jPanel4 = new javax.swing.JPanel();
        jSendTimeFrame = new com.visutools.nav.bislider.BiSlider();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jResultFrame.setTitle(bundle.getString("TribeTribeAttackFrame.jResultFrame.title")); // NOI18N

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/target.png"))); // NOI18N
        jLabel6.setText(bundle.getString("TribeTribeAttackFrame.jLabel6.text")); // NOI18N
        jLabel6.setMaximumSize(new java.awt.Dimension(18, 18));
        jLabel6.setMinimumSize(new java.awt.Dimension(18, 18));
        jLabel6.setPreferredSize(new java.awt.Dimension(18, 18));

        jTargetsBar.setBackground(new java.awt.Color(255, 255, 51));
        jTargetsBar.setForeground(new java.awt.Color(51, 153, 0));
        jTargetsBar.setToolTipText(bundle.getString("TribeTribeAttackFrame.jTargetsBar.toolTipText")); // NOI18N
        jTargetsBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTargetsBar.setStringPainted(true);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/snob.png"))); // NOI18N
        jLabel7.setText(bundle.getString("TribeTribeAttackFrame.jLabel7.text")); // NOI18N

        jEnoblementsBar.setBackground(new java.awt.Color(255, 0, 0));
        jEnoblementsBar.setForeground(new java.awt.Color(51, 153, 0));
        jEnoblementsBar.setMaximum(0);
        jEnoblementsBar.setToolTipText(bundle.getString("TribeTribeAttackFrame.jEnoblementsBar.toolTipText")); // NOI18N
        jEnoblementsBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jEnoblementsBar.setStringPainted(true);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/ram.png"))); // NOI18N
        jLabel9.setText(bundle.getString("TribeTribeAttackFrame.jLabel9.text")); // NOI18N

        jFullOffsBar.setBackground(new java.awt.Color(255, 255, 51));
        jFullOffsBar.setForeground(new java.awt.Color(51, 153, 0));
        jFullOffsBar.setToolTipText(bundle.getString("TribeTribeAttackFrame.jFullOffsBar.toolTipText")); // NOI18N
        jFullOffsBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jFullOffsBar.setStringPainted(true);

        jAttacksBar.setBackground(new java.awt.Color(255, 0, 0));
        jAttacksBar.setForeground(new java.awt.Color(51, 153, 0));
        jAttacksBar.setToolTipText(bundle.getString("TribeTribeAttackFrame.jAttacksBar.toolTipText")); // NOI18N
        jAttacksBar.setValue(50);
        jAttacksBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jAttacksBar.setStringPainted(true);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png"))); // NOI18N
        jLabel10.setText(bundle.getString("TribeTribeAttackFrame.jLabel10.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jTargetsBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jAttacksBar, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jEnoblementsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                    .addComponent(jFullOffsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(2, 2, 2)))
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTargetsBar, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jAttacksBar, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jEnoblementsBar, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jFullOffsBar, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        jCloseResultsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jCloseResultsButton.setText(bundle.getString("TribeTribeAttackFrame.jCloseResultsButton.text")); // NOI18N
        jCloseResultsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideResultsEvent(evt);
            }
        });

        jCopyToClipboardAsBBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        jCopyToClipboardAsBBButton.setText(bundle.getString("TribeTribeAttackFrame.jCopyToClipboardAsBBButton.text")); // NOI18N
        jCopyToClipboardAsBBButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jCopyToClipboardAsBBButton.toolTipText")); // NOI18N
        jCopyToClipboardAsBBButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAttacksToClipboardEvent(evt);
            }
        });

        jAddToAttacksButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_overview.png"))); // NOI18N
        jAddToAttacksButton.setText(bundle.getString("TribeTribeAttackFrame.jAddToAttacksButton.text")); // NOI18N
        jAddToAttacksButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jAddToAttacksButton.toolTipText")); // NOI18N
        jAddToAttacksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferToAttackPlanningEvent(evt);
            }
        });

        jCopyToClipboardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboard.png"))); // NOI18N
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
                .addContainerGap(387, Short.MAX_VALUE)
                .addComponent(jAddToAttacksButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCopyToClipboardButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCopyToClipboardAsBBButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCloseResultsButton)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 673, Short.MAX_VALUE))
                .addContainerGap())
        );
        jResultFrameLayout.setVerticalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
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

        jOffStrengthFrame.setTitle(bundle.getString("TribeTribeAttackFrame.jOffStrengthFrame.title")); // NOI18N

        jAxeField.setText(bundle.getString("TribeTribeAttackFrame.jAxeField.text")); // NOI18N
        jAxeField.setMaximumSize(new java.awt.Dimension(80, 20));
        jAxeField.setMinimumSize(new java.awt.Dimension(80, 20));
        jAxeField.setPreferredSize(new java.awt.Dimension(80, 20));
        jAxeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fireTroopStrengthFocusEvent(evt);
            }
        });
        jAxeField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireTroopStrengthChangedEvent(evt);
            }
        });

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/axe.png"))); // NOI18N
        jLabel12.setText(bundle.getString("TribeTribeAttackFrame.jLabel12.text")); // NOI18N

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/light.png"))); // NOI18N
        jLabel13.setText(bundle.getString("TribeTribeAttackFrame.jLabel13.text")); // NOI18N

        jLightField.setText(bundle.getString("TribeTribeAttackFrame.jLightField.text")); // NOI18N
        jLightField.setMaximumSize(new java.awt.Dimension(80, 20));
        jLightField.setMinimumSize(new java.awt.Dimension(80, 20));
        jLightField.setPreferredSize(new java.awt.Dimension(80, 20));
        jLightField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fireTroopStrengthFocusEvent(evt);
            }
        });
        jLightField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireTroopStrengthChangedEvent(evt);
            }
        });

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/marcher.png"))); // NOI18N
        jLabel14.setText(bundle.getString("TribeTribeAttackFrame.jLabel14.text")); // NOI18N

        jMarcherField.setText(bundle.getString("TribeTribeAttackFrame.jMarcherField.text")); // NOI18N
        jMarcherField.setMaximumSize(new java.awt.Dimension(80, 20));
        jMarcherField.setMinimumSize(new java.awt.Dimension(80, 20));
        jMarcherField.setPreferredSize(new java.awt.Dimension(80, 20));
        jMarcherField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fireTroopStrengthFocusEvent(evt);
            }
        });
        jMarcherField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireTroopStrengthChangedEvent(evt);
            }
        });

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/heavy.png"))); // NOI18N
        jLabel15.setText(bundle.getString("TribeTribeAttackFrame.jLabel15.text")); // NOI18N

        jHeavyField.setText(bundle.getString("TribeTribeAttackFrame.jHeavyField.text")); // NOI18N
        jHeavyField.setMaximumSize(new java.awt.Dimension(80, 20));
        jHeavyField.setMinimumSize(new java.awt.Dimension(80, 20));
        jHeavyField.setPreferredSize(new java.awt.Dimension(80, 20));
        jHeavyField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fireTroopStrengthFocusEvent(evt);
            }
        });
        jHeavyField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireTroopStrengthChangedEvent(evt);
            }
        });

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/ram.png"))); // NOI18N
        jLabel16.setText(bundle.getString("TribeTribeAttackFrame.jLabel16.text")); // NOI18N

        jRamField.setText(bundle.getString("TribeTribeAttackFrame.jRamField.text")); // NOI18N
        jRamField.setMaximumSize(new java.awt.Dimension(80, 20));
        jRamField.setMinimumSize(new java.awt.Dimension(80, 20));
        jRamField.setPreferredSize(new java.awt.Dimension(80, 20));
        jRamField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fireTroopStrengthFocusEvent(evt);
            }
        });
        jRamField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireTroopStrengthChangedEvent(evt);
            }
        });

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/cata.png"))); // NOI18N
        jLabel17.setText(bundle.getString("TribeTribeAttackFrame.jLabel17.text")); // NOI18N

        jCataField.setText(bundle.getString("TribeTribeAttackFrame.jCataField.text")); // NOI18N
        jCataField.setMaximumSize(new java.awt.Dimension(80, 20));
        jCataField.setMinimumSize(new java.awt.Dimension(80, 20));
        jCataField.setPreferredSize(new java.awt.Dimension(80, 20));
        jCataField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fireTroopStrengthFocusEvent(evt);
            }
        });
        jCataField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireTroopStrengthChangedEvent(evt);
            }
        });

        jLabel18.setText(bundle.getString("TribeTribeAttackFrame.jLabel18.text")); // NOI18N

        jToleranceSlider.setBackground(new java.awt.Color(239, 235, 223));
        jToleranceSlider.setForeground(new java.awt.Color(239, 235, 223));
        jToleranceSlider.setMajorTickSpacing(10);
        jToleranceSlider.setMaximum(50);
        jToleranceSlider.setMinorTickSpacing(1);
        jToleranceSlider.setPaintTicks(true);
        jToleranceSlider.setValue(10);
        jToleranceSlider.setOpaque(false);
        jToleranceSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireToleranceChangedEvent(evt);
            }
        });

        jAxeRange.setEditable(false);
        jAxeRange.setText(bundle.getString("TribeTribeAttackFrame.jAxeRange.text")); // NOI18N
        jAxeRange.setMaximumSize(new java.awt.Dimension(160, 20));
        jAxeRange.setMinimumSize(new java.awt.Dimension(160, 20));
        jAxeRange.setOpaque(false);
        jAxeRange.setPreferredSize(new java.awt.Dimension(160, 20));

        jLightRange.setEditable(false);
        jLightRange.setText(bundle.getString("TribeTribeAttackFrame.jLightRange.text")); // NOI18N
        jLightRange.setMaximumSize(new java.awt.Dimension(160, 20));
        jLightRange.setMinimumSize(new java.awt.Dimension(160, 20));
        jLightRange.setOpaque(false);
        jLightRange.setPreferredSize(new java.awt.Dimension(160, 20));

        jMarcherRange.setEditable(false);
        jMarcherRange.setText(bundle.getString("TribeTribeAttackFrame.jMarcherRange.text")); // NOI18N
        jMarcherRange.setMaximumSize(new java.awt.Dimension(160, 20));
        jMarcherRange.setMinimumSize(new java.awt.Dimension(160, 20));
        jMarcherRange.setOpaque(false);
        jMarcherRange.setPreferredSize(new java.awt.Dimension(160, 20));

        jHeavyRange.setEditable(false);
        jHeavyRange.setText(bundle.getString("TribeTribeAttackFrame.jHeavyRange.text")); // NOI18N
        jHeavyRange.setMaximumSize(new java.awt.Dimension(160, 20));
        jHeavyRange.setMinimumSize(new java.awt.Dimension(160, 20));
        jHeavyRange.setOpaque(false);
        jHeavyRange.setPreferredSize(new java.awt.Dimension(160, 20));

        jRamRange.setEditable(false);
        jRamRange.setText(bundle.getString("TribeTribeAttackFrame.jRamRange.text")); // NOI18N
        jRamRange.setMaximumSize(new java.awt.Dimension(160, 20));
        jRamRange.setMinimumSize(new java.awt.Dimension(160, 20));
        jRamRange.setOpaque(false);
        jRamRange.setPreferredSize(new java.awt.Dimension(160, 20));

        jCataRange.setEditable(false);
        jCataRange.setText(bundle.getString("TribeTribeAttackFrame.jCataRange.text")); // NOI18N
        jCataRange.setMaximumSize(new java.awt.Dimension(160, 20));
        jCataRange.setMinimumSize(new java.awt.Dimension(160, 20));
        jCataRange.setOpaque(false);
        jCataRange.setPreferredSize(new java.awt.Dimension(160, 20));

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png"))); // NOI18N
        jLabel19.setText(bundle.getString("TribeTribeAttackFrame.jLabel19.text")); // NOI18N

        jStrengthField.setEditable(false);
        jStrengthField.setText(bundle.getString("TribeTribeAttackFrame.jStrengthField.text")); // NOI18N
        jStrengthField.setMaximumSize(new java.awt.Dimension(80, 20));
        jStrengthField.setMinimumSize(new java.awt.Dimension(80, 20));
        jStrengthField.setPreferredSize(new java.awt.Dimension(80, 20));

        jStrengthRange.setEditable(false);
        jStrengthRange.setText(bundle.getString("TribeTribeAttackFrame.jStrengthRange.text")); // NOI18N
        jStrengthRange.setMaximumSize(new java.awt.Dimension(160, 20));
        jStrengthRange.setMinimumSize(new java.awt.Dimension(160, 20));
        jStrengthRange.setOpaque(false);
        jStrengthRange.setPreferredSize(new java.awt.Dimension(160, 20));

        jToleranceValue.setEditable(false);
        jToleranceValue.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jToleranceValue.setText(bundle.getString("TribeTribeAttackFrame.jToleranceValue.text")); // NOI18N
        jToleranceValue.setOpaque(false);

        jButton12.setBackground(new java.awt.Color(239, 235, 223));
        jButton12.setText(bundle.getString("TribeTribeAttackFrame.jButton12.text")); // NOI18N
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAcceptStrengthEvent(evt);
            }
        });

        jButton13.setBackground(new java.awt.Color(239, 235, 223));
        jButton13.setText(bundle.getString("TribeTribeAttackFrame.jButton13.text")); // NOI18N
        jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelStrengthEvent(evt);
            }
        });

        javax.swing.GroupLayout jOffStrengthFrameLayout = new javax.swing.GroupLayout(jOffStrengthFrame.getContentPane());
        jOffStrengthFrame.getContentPane().setLayout(jOffStrengthFrameLayout);
        jOffStrengthFrameLayout.setHorizontalGroup(
            jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jOffStrengthFrameLayout.createSequentialGroup()
                        .addComponent(jButton13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton12))
                    .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addGap(18, 18, 18)
                                .addComponent(jAxeField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addGap(18, 18, 18)
                                .addComponent(jLightField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addGap(18, 18, 18)
                                .addComponent(jMarcherField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(18, 18, 18)
                                .addComponent(jHeavyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(18, 18, 18)
                                .addComponent(jRamField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                                .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jStrengthField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jCataField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jStrengthRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCataRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jRamRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jHeavyRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMarcherRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLightRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jAxeRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToleranceSlider, 0, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToleranceValue, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jOffStrengthFrameLayout.setVerticalGroup(
            jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jOffStrengthFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jAxeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLightField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jMarcherField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jHeavyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(jRamField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(jCataField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addComponent(jStrengthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                        .addComponent(jAxeRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLightRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMarcherRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jHeavyRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRamRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCataRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jStrengthRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToleranceSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)))
                    .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToleranceValue, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12)
                    .addComponent(jButton13))
                .addContainerGap())
        );

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

        jSourcePanel.setBackground(new java.awt.Color(239, 235, 223));

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

        jSourceVillageLabel2.setText(bundle.getString("TribeTribeAttackFrame.jSourceVillageLabel2.text")); // NOI18N

        jSourceVillageContinentChooser.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSourceVillageContinentChooser.toolTipText")); // NOI18N
        jSourceVillageContinentChooser.setMaximumSize(new java.awt.Dimension(150, 20));
        jSourceVillageContinentChooser.setMinimumSize(new java.awt.Dimension(150, 20));
        jSourceVillageContinentChooser.setPreferredSize(new java.awt.Dimension(150, 20));
        jSourceVillageContinentChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireSourceVillageContinentChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSourceVillageLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSourceVillageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSourceVillageLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSourceUnitLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSourceVillageContinentChooser, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSourceVillageList, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTroopsList, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jVillageGroupChooser, 0, 244, Short.MAX_VALUE))
                .addContainerGap(10, Short.MAX_VALUE))
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
                    .addComponent(jSourceVillageLabel2)
                    .addComponent(jSourceVillageContinentChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSourceVillageLabel)
                    .addComponent(jSourceVillageList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSourceUnitLabel)
                    .addComponent(jTroopsList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
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
        jButton7.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton7.toolTipText")); // NOI18N
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChooseSourceRegionEvent(evt);
            }
        });

        jButton11.setBackground(new java.awt.Color(239, 235, 223));
        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/use_snob.png"))); // NOI18N
        jButton11.setText(bundle.getString("TribeTribeAttackFrame.jButton11.text")); // NOI18N
        jButton11.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton11.toolTipText")); // NOI18N
        jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUseSnobEvent(evt);
            }
        });

        jButton14.setBackground(new java.awt.Color(239, 235, 223));
        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/filter_off.png"))); // NOI18N
        jButton14.setText(bundle.getString("TribeTribeAttackFrame.jButton14.text")); // NOI18N
        jButton14.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton14.toolTipText")); // NOI18N
        jButton14.setMaximumSize(new java.awt.Dimension(59, 59));
        jButton14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFilterTroopStrengthEvent(evt);
            }
        });

        jLabel20.setText(bundle.getString("TribeTribeAttackFrame.jLabel20.text")); // NOI18N

        jMarkAsFakeBox.setText(bundle.getString("TribeTribeAttackFrame.jMarkAsFakeBox.text")); // NOI18N
        jMarkAsFakeBox.setToolTipText(bundle.getString("TribeTribeAttackFrame.jMarkAsFakeBox.toolTipText")); // NOI18N
        jMarkAsFakeBox.setOpaque(false);

        jSetFakeButton.setBackground(new java.awt.Color(239, 235, 223));
        jSetFakeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/fake.png"))); // NOI18N
        jSetFakeButton.setText(bundle.getString("TribeTribeAttackFrame.jSetFakeButton.text")); // NOI18N
        jSetFakeButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSetFakeButton.toolTipText")); // NOI18N
        jSetFakeButton.setMaximumSize(new java.awt.Dimension(65, 33));
        jSetFakeButton.setMinimumSize(new java.awt.Dimension(65, 33));
        jSetFakeButton.setPreferredSize(new java.awt.Dimension(65, 33));
        jSetFakeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeSourceFakeStateEvent(evt);
            }
        });

        jSetNoFakeButton.setBackground(new java.awt.Color(239, 235, 223));
        jSetNoFakeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/no_fake.png"))); // NOI18N
        jSetNoFakeButton.setText(bundle.getString("TribeTribeAttackFrame.jSetNoFakeButton.text")); // NOI18N
        jSetNoFakeButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSetNoFakeButton.toolTipText")); // NOI18N
        jSetNoFakeButton.setMaximumSize(new java.awt.Dimension(65, 33));
        jSetNoFakeButton.setMinimumSize(new java.awt.Dimension(65, 33));
        jSetNoFakeButton.setPreferredSize(new java.awt.Dimension(65, 33));
        jSetNoFakeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeSourceFakeStateEvent(evt);
            }
        });

        javax.swing.GroupLayout jSourcePanelLayout = new javax.swing.GroupLayout(jSourcePanel);
        jSourcePanel.setLayout(jSourcePanelLayout);
        jSourcePanelLayout.setHorizontalGroup(
            jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jSourcePanelLayout.createSequentialGroup()
                        .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jSourcePanelLayout.createSequentialGroup()
                                .addComponent(jButton7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton8))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jMarkAsFakeBox)
                            .addGroup(jSourcePanelLayout.createSequentialGroup()
                                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jSetNoFakeButton, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                    .addComponent(jButton14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jSetFakeButton, 0, 0, Short.MAX_VALUE)
                                    .addComponent(jButton11))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jSourcePanelLayout.setVerticalGroup(
            jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jSourcePanelLayout.createSequentialGroup()
                        .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton11)
                            .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSetFakeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSetNoFakeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton8)
                        .addComponent(jMarkAsFakeBox))
                    .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jSourcePanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jSourcePanel); // NOI18N

        jTargetPanel.setBackground(new java.awt.Color(239, 235, 223));

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

        jTargetTribeLabel1.setText(bundle.getString("TribeTribeAttackFrame.jTargetTribeLabel1.text")); // NOI18N
        jTargetTribeLabel1.setMaximumSize(new java.awt.Dimension(74, 14));
        jTargetTribeLabel1.setMinimumSize(new java.awt.Dimension(74, 14));
        jTargetTribeLabel1.setPreferredSize(new java.awt.Dimension(74, 14));

        jTargetVillageContinentChooser.setToolTipText(bundle.getString("TribeTribeAttackFrame.jTargetVillageContinentChooser.toolTipText")); // NOI18N
        jTargetVillageContinentChooser.setMaximumSize(new java.awt.Dimension(150, 20));
        jTargetVillageContinentChooser.setMinimumSize(new java.awt.Dimension(150, 20));
        jTargetVillageContinentChooser.setPreferredSize(new java.awt.Dimension(150, 20));
        jTargetVillageContinentChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireTargetContinentChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTargetAllyLabel)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jTargetTribeLabel, 0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTargetTribeLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))))
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTargetAllyList, 0, 234, Short.MAX_VALUE)
                    .addComponent(jTargetTribeList, 0, 234, Short.MAX_VALUE)
                    .addComponent(jTargetVillageContinentChooser, 0, 234, Short.MAX_VALUE)
                    .addComponent(jTargetVillageBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 234, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTargetAllyList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTargetAllyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTargetTribeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTargetTribeList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTargetTribeLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTargetVillageContinentChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTargetVillageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
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
        jButton10.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton10.toolTipText")); // NOI18N
        jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChooseTargetRegionEvent(evt);
            }
        });

        javax.swing.GroupLayout jTargetPanelLayout = new javax.swing.GroupLayout(jTargetPanel);
        jTargetPanel.setLayout(jTargetPanelLayout);
        jTargetPanelLayout.setHorizontalGroup(
            jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTargetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jTargetPanelLayout.createSequentialGroup()
                            .addComponent(jButton10)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton9))
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))
                .addContainerGap())
        );
        jTargetPanelLayout.setVerticalGroup(
            jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTargetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton9)
                    .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton10))
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jTargetPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jTargetPanel); // NOI18N

        jSettingsPanel.setBackground(new java.awt.Color(239, 235, 223));

        jAlgorithmChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Brute Force", "All In One", "Blitzkrieg" }));
        jAlgorithmChooser.setToolTipText(bundle.getString("TribeTribeAttackFrame.jAlgorithmChooser.toolTipText")); // NOI18N
        jAlgorithmChooser.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlgorithmChangedEvent(evt);
            }
        });

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText(bundle.getString("TribeTribeAttackFrame.jLabel8.text")); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText(bundle.getString("TribeTribeAttackFrame.jLabel4.text")); // NOI18N

        jArriveTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jArriveTimeLabel.setText(bundle.getString("TribeTribeAttackFrame.jArriveTimeLabel.text")); // NOI18N

        jStartTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jStartTimeLabel.setText(bundle.getString("TribeTribeAttackFrame.jStartTimeLabel.text")); // NOI18N

        jSendTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.SECOND));
        jSendTime.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSendTime.toolTipText")); // NOI18N
        jSendTime.setMaximumSize(new java.awt.Dimension(150, 20));
        jSendTime.setMinimumSize(new java.awt.Dimension(150, 20));
        jSendTime.setPreferredSize(new java.awt.Dimension(150, 20));

        jArriveTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.SECOND));
        jArriveTime.setToolTipText(bundle.getString("TribeTribeAttackFrame.jArriveTime.toolTipText")); // NOI18N
        jArriveTime.setMaximumSize(new java.awt.Dimension(150, 20));
        jArriveTime.setMinimumSize(new java.awt.Dimension(150, 20));
        jArriveTime.setPreferredSize(new java.awt.Dimension(150, 20));

        jMaxAttacksPerVillageLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jMaxAttacksPerVillageLabel.setText(bundle.getString("TribeTribeAttackFrame.jMaxAttacksPerVillageLabel.text")); // NOI18N

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText(bundle.getString("TribeTribeAttackFrame.jLabel5.text")); // NOI18N

        jMaxAttacksPerVillage.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jMaxAttacksPerVillage.setText(bundle.getString("TribeTribeAttackFrame.jMaxAttacksPerVillage.text")); // NOI18N

        jCleanOffs.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jCleanOffs.setText(bundle.getString("TribeTribeAttackFrame.jCleanOffs.text")); // NOI18N
        jCleanOffs.setToolTipText(bundle.getString("TribeTribeAttackFrame.jCleanOffs.toolTipText")); // NOI18N
        jCleanOffs.setEnabled(false);

        jNoNightLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jNoNightLabel.setText(bundle.getString("TribeTribeAttackFrame.jNoNightLabel.text")); // NOI18N
        jNoNightLabel.setMaximumSize(new java.awt.Dimension(74, 14));
        jNoNightLabel.setMinimumSize(new java.awt.Dimension(74, 14));
        jNoNightLabel.setPreferredSize(new java.awt.Dimension(74, 14));

        jNightForbidden.setToolTipText(bundle.getString("TribeTribeAttackFrame.jNightForbidden.toolTipText")); // NOI18N
        jNightForbidden.setOpaque(false);
        jNightForbidden.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChangeNightBlockEvent(evt);
            }
        });

        jRandomizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jRandomizeLabel.setText(bundle.getString("TribeTribeAttackFrame.jRandomizeLabel.text")); // NOI18N

        jRandomizeTribes.setToolTipText(bundle.getString("TribeTribeAttackFrame.jRandomizeTribes.toolTipText")); // NOI18N
        jRandomizeTribes.setOpaque(false);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText(bundle.getString("TribeTribeAttackFrame.jLabel11.text")); // NOI18N

        jPanel3.setOpaque(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSnobDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSnobDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setOpaque(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSendTimeFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSendTimeFrame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jSettingsPanelLayout = new javax.swing.GroupLayout(jSettingsPanel);
        jSettingsPanel.setLayout(jSettingsPanelLayout);
        jSettingsPanelLayout.setHorizontalGroup(
            jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSettingsPanelLayout.createSequentialGroup()
                        .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                            .addComponent(jArriveTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                            .addComponent(jStartTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                        .addGap(53, 53, 53)
                        .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jAlgorithmChooser, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jArriveTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSendTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jSettingsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(53, 53, 53)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jSettingsPanelLayout.createSequentialGroup()
                        .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                            .addComponent(jMaxAttacksPerVillageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                        .addGap(53, 53, 53)
                        .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jMaxAttacksPerVillage, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCleanOffs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jSettingsPanelLayout.createSequentialGroup()
                        .addComponent(jNoNightLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                        .addGap(53, 53, 53)
                        .addComponent(jNightForbidden, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jSettingsPanelLayout.createSequentialGroup()
                        .addComponent(jRandomizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                        .addGap(53, 53, 53)
                        .addComponent(jRandomizeTribes, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(248, 248, 248))
        );
        jSettingsPanelLayout.setVerticalGroup(
            jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jStartTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSendTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jArriveTimeLabel)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jAlgorithmChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMaxAttacksPerVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMaxAttacksPerVillageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jCleanOffs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jNoNightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jNightForbidden))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRandomizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRandomizeTribes))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jSettingsPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/settings.png")), jSettingsPanel); // NOI18N

        jScrollPane4.setViewportView(jTree1);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(495, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCalculateButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCalculateButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
    Village vSource = (Village) jSourceVillageList.getSelectedItem();
    UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
    ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{vSource, uSource, jMarkAsFakeBox.isSelected()});
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
    DefaultTableModel attackModel = (DefaultTableModel) jAttacksTable.getModel();
    if (attackModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Keine Herkunftsdörfer ausgewählt", "Fehler", JOptionPane.ERROR_MESSAGE);
        jTabbedPane1.setSelectedIndex(0);
        return;
    }

    if (victimModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Keine Ziele ausgewählt", "Fehler", JOptionPane.ERROR_MESSAGE);
        jTabbedPane1.setSelectedIndex(1);
        return;
    }

    List<Village> victimVillages = new LinkedList<Village>();
    for (int i = 0; i < victimModel.getRowCount(); i++) {
        victimVillages.add((Village) victimModel.getValueAt(i, 1));
    }

    //build source-unit map
    int snobSources = 0;

    // <editor-fold defaultstate="collapsed" desc=" Build attacks and fakes">
    Hashtable<UnitHolder, List<Village>> sources = new Hashtable<UnitHolder, List<Village>>();
    Hashtable<UnitHolder, List<Village>> fakes = new Hashtable<UnitHolder, List<Village>>();
    for (int i = 0; i < attackModel.getRowCount(); i++) {
        Village vSource = (Village) attackModel.getValueAt(i, 0);
        UnitHolder uSource = (UnitHolder) attackModel.getValueAt(i, 1);
        boolean fake = (Boolean) attackModel.getValueAt(i, 2);
        if (!fake) {
            List<Village> sourcesForUnit = sources.get(uSource);
            if (uSource.getPlainName().equals("snob")) {
                if (sourcesForUnit == null) {
                    snobSources = 0;
                } else {
                    snobSources = sourcesForUnit.size();
                }
            }
            if (sourcesForUnit == null) {
                sourcesForUnit = new LinkedList<Village>();
                sourcesForUnit.add(vSource);
                sources.put(uSource, sourcesForUnit);
            } else {
                sourcesForUnit.add(vSource);
            }
        } else {
            List<Village> fakesForUnit = fakes.get(uSource);
            if (fakesForUnit == null) {
                fakesForUnit = new LinkedList<Village>();
                fakesForUnit.add(vSource);
                fakes.put(uSource, fakesForUnit);
            } else {
                fakesForUnit.add(vSource);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Check for units not supported by the algorithm">
    boolean useMiscUnits = false;
    Enumeration<UnitHolder> involvedUnits = sources.keys();
    while (involvedUnits.hasMoreElements()) {
        UnitHolder u = involvedUnits.nextElement();
        //check for misc unit
        if (!u.getPlainName().equals("ram") && !u.getPlainName().equals("catapult") && !u.getPlainName().equals("snob")) {
            useMiscUnits = true;
            break;
        }

    }
    if (!useMiscUnits) {
        involvedUnits = fakes.keys();
        while (involvedUnits.hasMoreElements()) {
            UnitHolder u = involvedUnits.nextElement();
            //check for misc unit
            if (!u.getPlainName().equals("ram") && !u.getPlainName().equals("catapult") && !u.getPlainName().equals("snob")) {
                useMiscUnits = true;
                break;
            }
        }
    }

    // </editor-fold>

    int maxEnoblements = (int) Math.floor(snobSources / 4);
    int numInputAttacks = attackModel.getRowCount();
    int numInputTargets = victimVillages.size();

    // <editor-fold defaultstate="collapsed" desc="Obtain other parameters">
    int maxAttacksPerVillage = 0;
    try {
        maxAttacksPerVillage = Integer.parseInt(jMaxAttacksPerVillage.getText());
        jMaxAttacksPerVillage.setBackground(Color.WHITE);
    } catch (Exception e) {
        jMaxAttacksPerVillage.setBackground(Color.RED);
        jTabbedPane1.setSelectedIndex(1);
        return;
    }
    int minCleanForSnob = 0;
    if (jCleanOffs.isEnabled()) {
        try {
            minCleanForSnob = Integer.parseInt(jCleanOffs.getText());
            jCleanOffs.setBackground(Color.WHITE);
        } catch (Exception e) {
            jCleanOffs.setBackground(Color.RED);
            jTabbedPane1.setSelectedIndex(2);
            return;
        }
    } else {
        jCleanOffs.setBackground(Color.LIGHT_GRAY);
    }

    Date minSendTime = ((Date) jSendTime.getValue());
    Date arrive = ((Date) jArriveTime.getValue());
    int min = (int) Math.rint(jSendTimeFrame.getMinimumColoredValue());
    int max = (int) Math.rint(jSendTimeFrame.getMaximumColoredValue()) - 1;
    //</editor-fold>

    //start processing
    List<AbstractTroopMovement> result = new LinkedList<AbstractTroopMovement>();
    AbstractAttackAlgorithm algo = null;
    boolean needPostProcessing = false;
    if (jAlgorithmChooser.getSelectedIndex() == 0) {
        logger.info("Using 'BruteForce' algorithm");
        algo = new BruteForce();
    } else if (jAlgorithmChooser.getSelectedIndex() == 1) {
        logger.info("Using 'AllInOne' algorithm");
        algo = new AllInOne();
        if (useMiscUnits) {
            UIManager.put("OptionPane.noButtonText", "Nein");
            UIManager.put("OptionPane.yesButtonText", "Ja");

            if (JOptionPane.showConfirmDialog(this, "Der gewählte Algorithmus unterstützt nur Rammen, Katapulte und AGs als angreifende Einheiten.\n" +
                    "Dörfer für die eine andere Einheit gewählt wurde werden ignoriert.\n" +
                    "Trotzdem fortfahren?", "Warnung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                UIManager.put("OptionPane.noButtonText", "No");
                UIManager.put("OptionPane.yesButtonText", "Yes");
                return;
            }
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.yesButtonText", "Yes");
        }
    } else if (jAlgorithmChooser.getSelectedIndex() == 2) {
        logger.info("Using 'Blitzkrieg' algorithm");
        algo = new Blitzkrieg();
        //postprocessing = calculating optimal snob locations
        needPostProcessing = true;
        if (useMiscUnits) {
            UIManager.put("OptionPane.noButtonText", "Nein");
            UIManager.put("OptionPane.yesButtonText", "Ja");

            if (JOptionPane.showConfirmDialog(this, "Der gewählte Algorithmus unterstützt nur Rammen und Katapulte als angreifende Einheiten.\n" +
                    "Dörfer für die eine andere Einheit gewählt wurde werden ignoriert.\n" +
                    "Trotzdem fortfahren?", "Warnung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                UIManager.put("OptionPane.noButtonText", "No");
                UIManager.put("OptionPane.yesButtonText", "Yes");
                return;
            }
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.yesButtonText", "Yes");
        }

        if (maxAttacksPerVillage < minCleanForSnob) {
            UIManager.put("OptionPane.noButtonText", "Nein");
            UIManager.put("OptionPane.yesButtonText", "Ja");
            if (JOptionPane.showConfirmDialog(this, "Die maximale Anzahl der Angriffe pro Dorf ist kleiner als die mindestens notwendige Anzahl der Clean Offs vor einer Adelung.\n" +
                    "Mit dem gewählten Algorithmus werden daher keine AGs zugewiesen.\n" +
                    "Trotzdem fortfahren?", "Warnung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                UIManager.put("OptionPane.noButtonText", "No");
                UIManager.put("OptionPane.yesButtonText", "Yes");
                return;
            }
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.yesButtonText", "Yes");
        }
    }

    result = algo.calculateAttacks(sources,
            fakes,
            victimVillages,
            maxAttacksPerVillage,
            minCleanForSnob,
            minSendTime,
            arrive,
            min,
            max,
            jNightForbidden.isSelected(), jRandomizeTribes.isSelected());

    //System.out.println("Generating attacks");
    List<Attack> attackList = new LinkedList<Attack>();
    List<Village> targets = new LinkedList<Village>();

    // <editor-fold defaultstate="collapsed" desc=" Post processing ">
    int validEnoblements = 0;
    if (needPostProcessing) {
        //do algorithm postprocessing
        if (jAlgorithmChooser.getSelectedIndex() == 2) {
            logger.debug("Start algorithm post-processing");
            //try to find fastest snob sources
            UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
            Hashtable<Village, Attack> enoblements = new Hashtable<Village, Attack>();
            List<Village> attackSourceVillages = new LinkedList<Village>();
            int snobMinDist = (int) Math.rint(jSnobDistance.getMinimumColoredValue());
            int snobMaxDist = (int) Math.rint(jSnobDistance.getMaximumColoredValue());
            logger.debug("Building attack source list");
            for (int i = 0; i < numInputAttacks; i++) {
                Village vSource = (Village) attackModel.getValueAt(i, 0);
                attackSourceVillages.add(vSource);
            }
            snobSources = 0;
            logger.debug("Start checking for possible enoblements");
            for (AbstractTroopMovement movement : result) {
                List<Attack> atts = movement.getAttacks(arrive);
                boolean isFake = (movement instanceof Fake);
                for (Attack attack : atts) {
                    attackList.add(attack);
                    if (!targets.contains(attack.getTarget())) {
                        targets.add(attack.getTarget());
                    }
                }
                //check if movement is no fake and if clean off size is reached

                if (!isFake && atts.size() >= minCleanForSnob) {
                    //possible enoblement
                    Village target = movement.getTarget();
                    for (DistanceMapping mapping : AbstractAttackAlgorithm.buildSourceTargetsMapping(target, attackSourceVillages)) {
                        long sendTime = arrive.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(mapping.getSource(), mapping.getTarget(), snob.getSpeed()) * 1000);
                        TimeFrame f = new TimeFrame(minSendTime, arrive, min, max);
                        if (f.inside(new Date(sendTime))) {
                            //   logger.debug(" - found snob source in time frame");
                            Village snobSource = mapping.getTarget();
                            if (!enoblements.containsKey(snobSource)) {
                                double dist = mapping.getDistance();
                                if ((dist > snobMinDist) && (dist < snobMaxDist)) {
                                    Attack snobAttack = new Attack();
                                    snobAttack.setSource(snobSource);
                                    snobAttack.setTarget(mapping.getSource());
                                    snobAttack.setArriveTime(arrive);
                                    snobAttack.setUnit(snob);
                                    snobAttack.setType(Attack.SNOB_TYPE);
                                    enoblements.put(snobSource, snobAttack);
                                    int cnt = 0;
                                    VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(snobSource);
                                    if (troops != null) {
                                        cnt = troops.getTroopsOfUnit(snob);
                                    }
                                    if (cnt >= 4) {
                                        validEnoblements++;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            logger.debug("Possible enoblements assigned");

            if (enoblements.size() > 0) {
                UIManager.put("OptionPane.noButtonText", "Nein");
                UIManager.put("OptionPane.yesButtonText", "Ja");
                if (JOptionPane.showConfirmDialog(this, "Es konnten " + enoblements.size() + " Adelungen in der angegebenen Entfernung bestimmt werden.\n" +
                        "Achtung: Es wird nicht überprüft, ob die benötigte Anzahl AGs in den Dörfern vorhanden ist. AGs müssen ggf. rekrutiert werden.\n" +
                        "Sollen diese zu den bestehenden Angriffen hinzugefügt werden?", "Nachbearbeitung", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    Enumeration<Village> keys = enoblements.keys();
                    maxEnoblements = enoblements.size();
                    while (keys.hasMoreElements()) {
                        logger.debug(" - adding possible enoblement");
                        Attack a = enoblements.get(keys.nextElement());
                        for (int i = 0; i < 4; i++) {
                            attackList.add(a);
                        }
                    }
                }
                UIManager.put("OptionPane.noButtonText", "No");
                UIManager.put("OptionPane.yesButtonText", "Yes");
            } else {
                JOptionPane.showMessageDialog(this, "Mit den gewählten Einstellungen konnten keine AG Angriffe bestimmt werden.", "Nachbearbeitung", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    } else {
        logger.debug("Algorithm post-processing skipped");
        for (AbstractTroopMovement movement : result) {
            List<Attack> atts = movement.getAttacks(arrive);
            for (Attack attack : atts) {
                attackList.add(attack);
                if (!targets.contains(attack.getTarget())) {
                    targets.add(attack.getTarget());
                }
            }
        }
        validEnoblements = algo.getValidEnoblements();
    }

    // </editor-fold>

    int numOutputTargets = targets.size();
    int fullOffs = algo.getFullOffs();
    int calculatedAttacks = attackList.size();

    jTargetsBar.setMaximum(numInputTargets);
    jTargetsBar.setValue(numOutputTargets);
    jTargetsBar.setString(numOutputTargets + " / " + numInputTargets);

    if (maxEnoblements == validEnoblements) {
        //to get green bar in case if both are 0
        jEnoblementsBar.setMaximum(1);
        jEnoblementsBar.setValue(1);
    } else {
        jEnoblementsBar.setMaximum(maxEnoblements);
        jEnoblementsBar.setValue(validEnoblements);
    }

    jEnoblementsBar.setString(validEnoblements + " / " + maxEnoblements);
    jFullOffsBar.setMaximum(result.size());
    jFullOffsBar.setValue(fullOffs);
    jFullOffsBar.setString(fullOffs + " / " + result.size());
    jAttacksBar.setMaximum(numInputAttacks);
    jAttacksBar.setValue(calculatedAttacks);
    jAttacksBar.setString(calculatedAttacks + " / " + numInputAttacks);

    logger.debug("Sorting attacks by runtime");
    //sort result by start time
    Collections.sort(attackList, AbstractTroopMovement.RUNTIME_SORT);
    logger.debug("Building results...");

    showResults(attackList);
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
        boolean extended = (JOptionPane.showConfirmDialog(jResultFrame, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION);
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
            int type = (Integer) resultModel.getValueAt(i, 4);
            String time = null;
            if (extended) {
                time = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(dTime);
            } else {
                time = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.SSS'[/color]'").format(dTime);
            }

            switch (type) {
                case Attack.CLEAN_TYPE: {
                    buffer.append("Angriff (Clean-Off) ");
                    buffer.append("\n");
                    break;
                }
                case Attack.FAKE_TYPE: {
                    buffer.append("Angriff (Fake) ");
                    buffer.append("\n");
                    break;
                }
                case Attack.SNOB_TYPE: {
                    buffer.append("Angriff (AG) ");
                    buffer.append("\n");
                    break;
                }
                case Attack.SUPPORT_TYPE: {
                    buffer.append("Unterstützung ");
                    buffer.append("\n");
                    break;
                }
                default: {
                    buffer.append("Angriff ");
                }
            }

            if (Boolean.parseBoolean(GlobalOptions.getProperty("export.tribe.names"))) {
                buffer.append(" von ");
                if (sVillage.getTribe() != null) {
                    buffer.append(sVillage.getTribe().toBBCode());
                } else {
                    buffer.append("Barbaren");
                }

            }
            buffer.append(" aus ");
            buffer.append(sVillage.toBBCode());
            if (Boolean.parseBoolean(GlobalOptions.getProperty("export.units"))) {
                buffer.append(" mit ");
                if (extended) {
                    buffer.append("[img]" + sUrl + "/graphic/unit/unit_" + sUnit.getPlainName() + ".png[/img]");
                } else {
                    buffer.append(sUnit.getName());
                }

            }
            buffer.append(" auf ");
            if (Boolean.parseBoolean(GlobalOptions.getProperty("export.tribe.names"))) {
                if (tVillage.getTribe() != null) {
                    buffer.append(tVillage.getTribe().toBBCode());
                } else {
                    buffer.append("Barbaren");
                }
                buffer.append(" in ");
            }

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
            if (JOptionPane.showConfirmDialog(jResultFrame, "Die ausgewählten Angriffe benötigen mehr als 500 BB-Codes\n" +
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
            int type = (Integer) resultModel.getValueAt(i, 4);
            String time = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(dTime);

            switch (type) {
                case Attack.CLEAN_TYPE: {
                    buffer.append("(Clean-Off)");
                    buffer.append("\t");
                    break;
                }
                case Attack.FAKE_TYPE: {
                    buffer.append("(Fake)");
                    buffer.append("\t");
                    break;
                }
                case Attack.SNOB_TYPE: {
                    buffer.append("(AG)");
                    buffer.append("\t");
                    break;
                }
                case Attack.SUPPORT_TYPE: {
                    buffer.append("(Unterstützung)");
                    buffer.append("\t");
                    break;
                }
            }

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
    UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
    jAttacksTable.invalidate();
    try {
        int size = jSourceVillageList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{jSourceVillageList.getModel().getElementAt(i), uSource, jMarkAsFakeBox.isSelected()});
        }
    } catch (Exception e) {
        logger.error("Failed to add current group as source", e);
    }

    jAttacksTable.revalidate();
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

        for (int i = rows.length - 1; i >=
                0; i--) {
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
    jVictimTable.repaint();//.updateUI();
}//GEN-LAST:event_fireAddTargetVillageEvent

private void fireAddAllTargetVillagesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAllTargetVillagesEvent
    Tribe target = (Tribe) jTargetTribeList.getSelectedItem();
    if (target == null) {
        return;
    }
    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    jVictimTable.invalidate();
    for (int i = 0; i < jTargetVillageBox.getItemCount(); i++) {
        Village v = (Village) jTargetVillageBox.getItemAt(i);
        victimModel.addRow(new Object[]{target, v});
    }

    jVictimTable.revalidate();
    jVictimTable.repaint();//.updateUI();
}//GEN-LAST:event_fireAddAllTargetVillagesEvent

private void fireTargetTribeChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireTargetTribeChangedEvent
    try {
        Tribe t = (Tribe) jTargetTribeList.getSelectedItem();
        if (t != null) {
            Village[] villages = t.getVillageList().toArray(new Village[]{});
            Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
            jTargetVillageBox.setModel(new DefaultComboBoxModel(villages));
            buildTargetContinentList(villages);
        } else {
            jTargetVillageBox.setModel(new DefaultComboBoxModel());
            buildTargetContinentList(new Village[]{});
        }

    } catch (Exception e) {
        jTargetVillageBox.setModel(new DefaultComboBoxModel());
        buildTargetContinentList(new Village[]{});
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
        buildSourceContinentList(villages);
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
    buildSourceContinentList(villages);
}//GEN-LAST:event_fireVillageGroupChangedEvent

    private void buildSourceContinentList(Village[] pVillages) {
        List<String> continents = new LinkedList<String>();
        continents.add("Alle");
        for (Village v : pVillages) {
            int cont = 0;
            if (ServerSettings.getSingleton().getCoordType() != 2) {
                cont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
            } else {
                cont = DSCalculator.getContinent(v.getX(), v.getY());
            }

            String contString = "K" + cont;
            if (!continents.contains(contString)) {
                continents.add(contString);
            }
        }
        Collections.sort(continents, String.CASE_INSENSITIVE_ORDER);
        jSourceVillageContinentChooser.setModel(new DefaultComboBoxModel(continents.toArray(new String[]{})));
        jSourceVillageContinentChooser.repaint();//.updateUI();
        jSourceVillageContinentChooser.setSelectedIndex(0);
    }

    private void buildTargetContinentList(Village[] pVillages) {
        List<String> continents = new LinkedList<String>();
        if (pVillages.length > 0) {
            continents.add("Alle");
            for (Village v : pVillages) {
                int cont = 0;
                if (ServerSettings.getSingleton().getCoordType() != 2) {
                    cont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
                } else {
                    cont = DSCalculator.getContinent(v.getX(), v.getY());
                }

                String contString = "K" + cont;
                if (!continents.contains(contString)) {
                    continents.add(contString);
                }
            }
            Collections.sort(continents, String.CASE_INSENSITIVE_ORDER);

            jTargetVillageContinentChooser.setModel(new DefaultComboBoxModel(continents.toArray(new String[]{})));
            jTargetVillageContinentChooser.repaint();//.updateUI();
            jTargetVillageContinentChooser.setSelectedIndex(0);
        } else {
            jTargetVillageContinentChooser.setModel(new DefaultComboBoxModel());
            jTargetVillageContinentChooser.repaint();//.updateUI();
        }
    }

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
    boolean showOnMap = false;
    try {
        showOnMap = Boolean.parseBoolean(GlobalOptions.getProperty("draw.attacks.by.default"));
    } catch (Exception e) {
    }

    for (int i = 0; i < resultModel.getRowCount(); i++) {
        Village source = (Village) resultModel.getValueAt(i, 0);
        UnitHolder unit = (UnitHolder) resultModel.getValueAt(i, 1);
        Village target = (Village) resultModel.getValueAt(i, 2);
        Date sendTime = (Date) resultModel.getValueAt(i, 3);
        Integer type = (Integer) resultModel.getValueAt(i, 4);
        long arriveTime = sendTime.getTime() + (long) (DSCalculator.calculateMoveTimeInSeconds(source, target, unit.getSpeed()) * 1000);
        AttackManager.getSingleton().addAttackFast(source, target, unit, new Date(arriveTime), showOnMap, planName, type);
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

private void fireChangeNightBlockEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChangeNightBlockEvent
    if (jNightForbidden.isSelected()) {
        jSendTimeFrame.setMinimumValue(8);
    } else {
        jSendTimeFrame.setMinimumValue(0);
    }
}//GEN-LAST:event_fireChangeNightBlockEvent

private void fireUseSnobEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUseSnobEvent
    DefaultTableModel model = (DefaultTableModel) jAttacksTable.getModel();
    int rows = model.getRowCount();
    UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
    jAttacksTable.invalidate();
    Hashtable<Village, Integer> assignedTroops = new Hashtable<Village, Integer>();
    for (int row = 0; row < rows; row++) {
        Village v = (Village) model.getValueAt(row, 0);
        VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(v);
        if (troops != null) {
            int availSnobs = troops.getTroopsOfUnit(snob);
            Integer assignedSnobs = assignedTroops.get(v);
            if (assignedSnobs == null) {
                assignedSnobs = 0;
            } else {
                assignedSnobs += 1;
            }

            availSnobs -= assignedSnobs;
            assignedTroops.put(v, assignedSnobs);
            //snob avail
            if (availSnobs > 0) {
                model.setValueAt(snob, row, 1);
            }
        }
    }

    jAttacksTable.revalidate();
}//GEN-LAST:event_fireUseSnobEvent

private void fireAlgorithmChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlgorithmChangedEvent
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        boolean specialAlgorithm = false;
        int selected = jAlgorithmChooser.getSelectedIndex();
        if (selected != 0) {
            specialAlgorithm = true;
        }
        if (selected != 2) {
            jSnobDistance.setSliderBackground(Color.DARK_GRAY);
            jSnobDistance.setMaximumColor(Color.LIGHT_GRAY);
            jSnobDistance.setMinimumColor(Color.LIGHT_GRAY);

        } else {
            jSnobDistance.setSliderBackground(Constants.DS_BACK);
            jSnobDistance.setMaximumColor(Constants.DS_BACK_LIGHT);
            jSnobDistance.setMinimumColor(Constants.DS_BACK_LIGHT);

        }
        jSnobDistance.repaint();

        jCleanOffs.setEnabled(specialAlgorithm);
        if (!specialAlgorithm) {
            jCleanOffs.setBackground(Color.LIGHT_GRAY);
        } else {
            jCleanOffs.setBackground(Color.WHITE);
        }
        jRandomizeTribes.setEnabled(!specialAlgorithm);
    }
}//GEN-LAST:event_fireAlgorithmChangedEvent

private void fireToleranceChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireToleranceChangedEvent
    int v = jToleranceSlider.getValue();
    jToleranceValue.setText(v + " %");
    try {
        if (jAxeField.getText().length() == 0) {
            jAxeField.setText("0");
        }
        if (jLightField.getText().length() == 0) {
            jLightField.setText("0");
        }
        if (jMarcherField.getText().length() == 0) {
            jMarcherField.setText("0");
        }
        if (jHeavyField.getText().length() == 0) {
            jHeavyField.setText("0");
        }
        if (jRamField.getText().length() == 0) {
            jRamField.setText("0");
        }
        if (jCataField.getText().length() == 0) {
            jCataField.setText("0");
        }

        int axe = Integer.parseInt(jAxeField.getText());
        int light = Integer.parseInt(jLightField.getText());
        int marcher = Integer.parseInt(jMarcherField.getText());
        int heavy = Integer.parseInt(jHeavyField.getText());
        int ram = Integer.parseInt(jRamField.getText());
        int cata = Integer.parseInt(jCataField.getText());

        int diff = (int) Math.floor((double) axe * (double) v / 100);
        jAxeRange.setText((axe - diff) + " - " + (axe + diff));
        diff = (int) Math.floor((double) light * (double) v / 100);
        jLightRange.setText((light - diff) + " - " + (light + diff));
        diff = (int) Math.floor((double) marcher * (double) v / 100);
        jMarcherRange.setText((marcher - diff) + " - " + (marcher + diff));
        diff = (int) Math.floor((double) heavy * (double) v / 100);
        jHeavyRange.setText((heavy - diff) + " - " + (heavy + diff));
        diff = (int) Math.floor((double) ram * (double) v / 100);
        jRamRange.setText((ram - diff) + " - " + (ram + diff));
        diff = (int) Math.floor((double) cata * (double) v / 100);
        jCataRange.setText((cata - diff) + " - " + (cata + diff));
        double strength = 0;
        UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName("axe");
        strength += axe * unit.getAttack();
        unit = DataHolder.getSingleton().getUnitByPlainName("light");
        strength += light * unit.getAttack();
        unit = DataHolder.getSingleton().getUnitByPlainName("marcher");
        if (unit != null) {
            strength += marcher * unit.getAttack();
        }
        unit = DataHolder.getSingleton().getUnitByPlainName("heavy");
        strength += heavy * unit.getAttack();
        unit = DataHolder.getSingleton().getUnitByPlainName("ram");
        strength += ram * unit.getAttack();
        unit = DataHolder.getSingleton().getUnitByPlainName("catapult");
        strength += cata * unit.getAttack();
        jStrengthField.setText("" + (int) Math.rint(strength));
        diff = (int) Math.floor((double) strength * (double) v / 100);
        jStrengthRange.setText("min. " + ((int) strength - diff));
    } catch (Exception e) {
        JOptionPane.showMessageDialog(jOffStrengthFrame, "Bitte nur ganzzahlige Truppenzahlen verwenden.", "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_fireToleranceChangedEvent

private void fireAcceptStrengthEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAcceptStrengthEvent
    //recalculate again
    DefaultTableModel model = (DefaultTableModel) jAttacksTable.getModel();
    jAttacksTable.invalidate();
    int strength = Integer.parseInt(jStrengthField.getText());
    int diff = (int) Math.floor((double) strength * (double) jToleranceSlider.getValue() / 100);
    int removeCount = 0;
    int noInformation = 0;
    for (int i = 0; i < jAttacksTable.getRowCount(); i++) {
        Village v = (Village) jAttacksTable.getValueAt(i, 0);
        try {
            VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(v);
            int offValue = 0;
            if (troops != null) {
                offValue = (int) troops.getOffValue();
                if (offValue < strength - diff) {
                    int row = jAttacksTable.convertRowIndexToModel(i);
                    model.removeRow(row);
                    removeCount++;
                }
            } else {
                //no troops available, skip removal
                noInformation++;
            }

        } catch (Exception e) {
        }
    }
    jAttacksTable.revalidate();

    jOffStrengthFrame.setVisible(false);
    String message = "Es wurden keine Angriffe entfernt.";
    if (removeCount == 1) {
        message = "Es wurde ein Angriff entfernt.";
    } else {
        message = "Es wurden " + removeCount + " Angriffe entfernt.";
    }
    if (noInformation > 0) {
        if (noInformation == 1) {
            message += "\nZu einem Dorf lagen keine Truppeninformationen vor.";
        } else {
            message += "\nZu " + noInformation + " Dörfern lagen keine Truppeninformationen vor.";
        }
    }
    JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_fireAcceptStrengthEvent

private void fireCancelStrengthEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelStrengthEvent
    jOffStrengthFrame.setVisible(false);
}//GEN-LAST:event_fireCancelStrengthEvent

private void fireFilterTroopStrengthEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFilterTroopStrengthEvent
    fireToleranceChangedEvent(null);
    jOffStrengthFrame.setVisible(true);
}//GEN-LAST:event_fireFilterTroopStrengthEvent

private void fireTroopStrengthChangedEvent(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fireTroopStrengthChangedEvent
    if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
        fireToleranceChangedEvent(null);
    }
}//GEN-LAST:event_fireTroopStrengthChangedEvent

private void fireTroopStrengthFocusEvent(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fireTroopStrengthFocusEvent
    fireToleranceChangedEvent(null);
}//GEN-LAST:event_fireTroopStrengthFocusEvent

private void fireSourceVillageContinentChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireSourceVillageContinentChangedEvent
    String cont = (String) jSourceVillageContinentChooser.getSelectedItem();
    if (cont == null) {
        return;
    }
    if (cont.equals("Alle")) {
        fireVillageGroupChangedEvent(null);
    } else {
        fireVillageGroupChangedEvent(null);
        jSourceVillageContinentChooser.setSelectedItem(cont);
        try {
            int contId = Integer.parseInt(cont.replaceAll("K", ""));
            List<Village> toRemove = new LinkedList<Village>();
            for (int i = 0; i < jSourceVillageList.getItemCount(); i++) {
                Village v = (Village) jSourceVillageList.getItemAt(i);
                int vCont = 0;
                if (ServerSettings.getSingleton().getCoordType() != 2) {
                    vCont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
                } else {
                    vCont = DSCalculator.getContinent(v.getX(), v.getY());
                }
                if (vCont != contId) {
                    toRemove.add(v);
                }
            }
            for (Village v : toRemove) {
                jSourceVillageList.removeItem(v);
            }
            jSourceVillageList.repaint();//.updateUI();
        } catch (Exception e) {
        }
    }

}//GEN-LAST:event_fireSourceVillageContinentChangedEvent

private void fireTargetContinentChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireTargetContinentChangedEvent
    String cont = (String) jTargetVillageContinentChooser.getSelectedItem();
    if (cont == null) {
        return;
    }
    if (cont.equals("Alle")) {
        fireTargetTribeChangedEvent(null);
    } else {
        fireTargetTribeChangedEvent(null);
        jTargetVillageContinentChooser.setSelectedItem(cont);
        try {
            int contId = Integer.parseInt(cont.replaceAll("K", ""));
            List<Village> toRemove = new LinkedList<Village>();
            for (int i = 0; i < jTargetVillageBox.getItemCount(); i++) {
                Village v = (Village) jTargetVillageBox.getItemAt(i);
                int vCont = 0;
                if (ServerSettings.getSingleton().getCoordType() != 2) {
                    vCont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
                } else {
                    vCont = DSCalculator.getContinent(v.getX(), v.getY());
                }
                if (vCont != contId) {
                    toRemove.add(v);
                }
            }
            for (Village v : toRemove) {
                jTargetVillageBox.removeItem(v);
            }
            jTargetVillageBox.repaint();//.updateUI();
        } catch (Exception e) {
        }
    }
}//GEN-LAST:event_fireTargetContinentChangedEvent

private void fireChangeSourceFakeStateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeSourceFakeStateEvent
    boolean toFake = (evt.getSource() == jSetFakeButton);

    int[] rows = jAttacksTable.getSelectedRows();
    if (rows == null) {
        return;
    }
    jAttacksTable.invalidate();
    for (int row : rows) {

        jAttacksTable.setValueAt(toFake, row, 2);
    }
    jAttacksTable.revalidate();
}//GEN-LAST:event_fireChangeSourceFakeStateEvent

    private void showResults(List<Attack> pAttacks) {
        jResultsTable.invalidate();
        DefaultTableModel resultModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Truppen", "Ziel", "Startzeit", "Typ"}) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Village.class, Date.class, Integer.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        jResultsTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());

        jResultsTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        /* Enumeration<Village> targets = pAttacks.keys();

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
        }*/

        for (Attack a : pAttacks) {
            long targetTime = a.getArriveTime().getTime();
            long startTime = targetTime - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
            resultModel.addRow(new Object[]{a.getSource(), a.getUnit(), a.getTarget(), new Date(startTime), a.getType()});
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
        jResultsTable.revalidate();
        jResultFrame.setVisible(true);
    }

    @Override
    public void fireSelectionFinishedEvent(Point pStart, Point pEnd) {
        int xStart = (pStart.x < pEnd.x) ? pStart.x : pEnd.x;
        int xEnd = (pEnd.x > pStart.x) ? pEnd.x : pStart.x;
        int yStart = (pStart.y < pEnd.y) ? pStart.y : pEnd.y;
        int yEnd = (pEnd.y > pStart.y) ? pEnd.y : pStart.y;
        if (bChooseSourceRegionMode) {
            Tribe you = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe();
            UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
            jAttacksTable.invalidate();
            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    Village v = DataHolder.getSingleton().getVillages()[x][y];
                    if (v != null) {
                        Tribe t = v.getTribe();
                        if (t != null) {
                            if (t.equals(you)) {
                                ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{v, uSource});
                            }
                        }
                    }
                }
            }
            jAttacksTable.revalidate();
        } else if (bChooseTargetRegionMode) {
            Tribe victim = (Tribe) jTargetTribeList.getSelectedItem();
            jVictimTable.invalidate();
            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
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
            jVictimTable.repaint();//.updateUI();
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
    private javax.swing.JComboBox jAlgorithmChooser;
    private javax.swing.JSpinner jArriveTime;
    private javax.swing.JLabel jArriveTimeLabel;
    private javax.swing.JComboBox jAttackPlansBox;
    private javax.swing.JProgressBar jAttacksBar;
    private javax.swing.JTable jAttacksTable;
    private javax.swing.JTextField jAxeField;
    private javax.swing.JTextField jAxeRange;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JTextField jCataField;
    private javax.swing.JTextField jCataRange;
    private javax.swing.JTextField jCleanOffs;
    private javax.swing.JButton jCloseResultsButton;
    private javax.swing.JButton jCopyToClipboardAsBBButton;
    private javax.swing.JButton jCopyToClipboardButton;
    private javax.swing.JProgressBar jEnoblementsBar;
    private javax.swing.JProgressBar jFullOffsBar;
    private javax.swing.JTextField jHeavyField;
    private javax.swing.JTextField jHeavyRange;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jLightField;
    private javax.swing.JTextField jLightRange;
    private javax.swing.JTextField jMarcherField;
    private javax.swing.JTextField jMarcherRange;
    private javax.swing.JCheckBox jMarkAsFakeBox;
    private javax.swing.JTextField jMaxAttacksPerVillage;
    private javax.swing.JLabel jMaxAttacksPerVillageLabel;
    private javax.swing.JTextField jNewPlanName;
    private javax.swing.JCheckBox jNightForbidden;
    private javax.swing.JLabel jNoNightLabel;
    private javax.swing.JFrame jOffStrengthFrame;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextField jRamField;
    private javax.swing.JTextField jRamRange;
    private javax.swing.JLabel jRandomizeLabel;
    private javax.swing.JCheckBox jRandomizeTribes;
    private javax.swing.JFrame jResultFrame;
    private javax.swing.JTable jResultsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSpinner jSendTime;
    private com.visutools.nav.bislider.BiSlider jSendTimeFrame;
    private javax.swing.JButton jSetFakeButton;
    private javax.swing.JButton jSetNoFakeButton;
    private javax.swing.JPanel jSettingsPanel;
    private com.visutools.nav.bislider.BiSlider jSnobDistance;
    private javax.swing.JPanel jSourcePanel;
    private javax.swing.JLabel jSourceUnitLabel;
    private javax.swing.JComboBox jSourceVillageContinentChooser;
    private javax.swing.JLabel jSourceVillageLabel;
    private javax.swing.JLabel jSourceVillageLabel1;
    private javax.swing.JLabel jSourceVillageLabel2;
    private javax.swing.JComboBox jSourceVillageList;
    private javax.swing.JLabel jStartTimeLabel;
    private javax.swing.JTextField jStrengthField;
    private javax.swing.JTextField jStrengthRange;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel jTargetAllyLabel;
    private javax.swing.JComboBox jTargetAllyList;
    private javax.swing.JPanel jTargetPanel;
    private javax.swing.JLabel jTargetTribeLabel;
    private javax.swing.JLabel jTargetTribeLabel1;
    private javax.swing.JComboBox jTargetTribeList;
    private javax.swing.JComboBox jTargetVillageBox;
    private javax.swing.JComboBox jTargetVillageContinentChooser;
    private javax.swing.JProgressBar jTargetsBar;
    private javax.swing.JSlider jToleranceSlider;
    private javax.swing.JTextField jToleranceValue;
    private javax.swing.JDialog jTransferToAttackManagerDialog;
    private javax.swing.JTree jTree1;
    private javax.swing.JComboBox jTroopsList;
    private javax.swing.JTable jVictimTable;
    private javax.swing.JComboBox jVillageGroupChooser;
    // End of variables declaration//GEN-END:variables
    }
