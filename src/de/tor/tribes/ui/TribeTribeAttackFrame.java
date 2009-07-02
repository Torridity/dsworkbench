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
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.algo.MiscSettingsPanel;
import de.tor.tribes.ui.algo.TimePanel;
import de.tor.tribes.ui.editors.AttackTypeCellEditor;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.VillageSelectionListener;
import de.tor.tribes.util.algo.AbstractAttackAlgorithm;
import de.tor.tribes.util.algo.AllInOne;
import de.tor.tribes.util.algo.Blitzkrieg;
import de.tor.tribes.util.algo.BruteForce;
import de.tor.tribes.util.algo.TimeFrame;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.parser.VillageParser;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import java.awt.Color;
import java.awt.Point;
import java.util.StringTokenizer;
import javax.swing.UIManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Collections;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

/**
 * @TODO (1.6) Handle "mark on map" via selection frame
 * @author Jejkal
 */
public class TribeTribeAttackFrame extends javax.swing.JFrame {

    private static Logger logger = Logger.getLogger("AttackPlanner");
    private boolean bChooseSourceRegionMode = false;
    private boolean bChooseTargetRegionMode = false;
    private TimePanel mTimePanel = null;
    private MiscSettingsPanel mMiscPanel = null;

    /** Creates new form TribeTribeAttackFrame */
    public TribeTribeAttackFrame() {
        initComponents();
        mTimePanel = new TimePanel();
        mMiscPanel = new MiscSettingsPanel();
        jTabbedPane1.addTab("Zeiteinstellungen", new ImageIcon(this.getClass().getResource("/res/clock.png")), mTimePanel);
        jTabbedPane1.addTab("Sonstige Einstellungen", new ImageIcon(this.getClass().getResource("/res/settings.png")), mMiscPanel);
        getContentPane().setBackground(Constants.DS_BACK);
        jTransferToAttackManagerDialog.pack();
        jAxeField.setText("6000");
        jLightField.setText("3200");
        jMarcherField.setText("0");
        jHeavyField.setText("0");
        jRamField.setText("300");
        jCataField.setText("10");
        jOffStrengthFrame.pack();
        jAttackResultDetailsFrame.pack();
        jTargetResultDetailsFrame.pack();

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelp(jSourcePanel, "pages.attack_planer_source", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelp(jTargetPanel, "pages.attack_planer_target", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelp(jAlgoPanel, "pages.attack_planer_type", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelp(mTimePanel, "pages.attack_planer_time", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelp(mMiscPanel, "pages.attack_planer_settings", GlobalOptions.getHelpBroker().getHelpSet());

        GlobalOptions.getHelpBroker().enableHelpKey(jResultFrame.getRootPane(), "pages.attack_planer_results", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(jTargetResultDetailsFrame.getRootPane(), "pages.attack_planer_results_details_targets", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(jAttackResultDetailsFrame.getRootPane(), "pages.attack_planer_results_details_sources", GlobalOptions.getHelpBroker().getHelpSet());
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

        jAttackPlanSelectionDialog.getContentPane().setBackground(Constants.DS_BACK_LIGHT);
        jAttackPlanSelectionDialog.pack();
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
            DefaultListModel targetAllyModel = new DefaultListModel();
            targetAllyModel.addElement("<Kein Stamm>");
            for (Ally a : aAllies) {
                targetAllyModel.addElement(a);
            }

            jTargetAllyList.setModel(targetAllyModel);
            jTargetAllyList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    fireFilterTargetByAllyEvent();
                }
            });

            jTargetTribeList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    fireFilterTargetByTribeEvent();
                }
            });

            jTargetContinentList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    fireFilterTargetByContinentEvent();
                }
            });
            //select first ally and initialize all lists
            jTargetAllyList.setSelectedIndex(0);

            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Build user village list ">
            Tag[] tags = TagManager.getSingleton().getTags().toArray(new Tag[]{});
            DefaultListModel tagModel = new DefaultListModel();
            tagModel.addElement("Keinen Tag");
            for (Tag t : tags) {
                tagModel.addElement(t);
            }
            jVillageGroupList.setModel(tagModel);

            jVillageGroupList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    fireFilterSourceVillagesByGroupEvent();
                }
            });

            jSourceContinentList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    fireFilterSourceContinentEvent();
                }
            });

            if (TagManager.getSingleton().getTags().isEmpty()) {
                jVillageGroupList.setEnabled(false);
            } else {
                jVillageGroupList.setEnabled(true);
            }
            //select all groups and initialize lists
            jVillageGroupList.getSelectionModel().setSelectionInterval(0, (tags.length > 0) ? tags.length : tags.length);
            // </editor-fold>

            mTimePanel.reset();
            mMiscPanel.reset();
            jAttacksTable.setDefaultRenderer(Date.class, new DateCellRenderer());
            jAttacksTable.setDefaultEditor(Date.class, new DateSpinEditor());
            jAttacksTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
            jAttacksTable.setDefaultEditor(Village.class, new VillageCellEditor());

            DefaultComboBoxModel unitModel = new DefaultComboBoxModel(DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{}));
            jTroopsList.setModel(unitModel);
            jTroopsList.setSelectedItem(DataHolder.getSingleton().getUnitByPlainName("ram"));
            jResultFrame.pack();
        } catch (Exception e) {
            logger.error("Failed to initialize TribeAttackFrame", e);
        }
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
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
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
        attackTypeGroup = new javax.swing.ButtonGroup();
        jAttackResultDetailsFrame = new javax.swing.JFrame();
        jHideAttackDetailsButton = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        jNotAssignedSourcesTable = new javax.swing.JTable();
        jTargetResultDetailsFrame = new javax.swing.JFrame();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTargetDetailsTable = new javax.swing.JTable();
        jHideTargetDetailsButton = new javax.swing.JButton();
        jAttackPlanSelectionDialog = new javax.swing.JDialog();
        jScrollPane13 = new javax.swing.JScrollPane();
        jAttackPlanTable = new javax.swing.JTable();
        jDoSyncButton = new javax.swing.JButton();
        jCancelSyncButton = new javax.swing.JButton();
        jCalculateButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jSourcePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttacksTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jSourceVillageLabel1 = new javax.swing.JLabel();
        jTroopsList = new javax.swing.JComboBox();
        jSourceVillageLabel2 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jVillageGroupList = new javax.swing.JList();
        jScrollPane5 = new javax.swing.JScrollPane();
        jSourceContinentList = new javax.swing.JList();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jSourceVillageList = new javax.swing.JList();
        jLabel22 = new javax.swing.JLabel();
        jSourceGroupRelation = new javax.swing.JRadioButton();
        jSelectionStart = new javax.swing.JTextField();
        jSelectionEnd = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jSelectionBeginButton = new javax.swing.JButton();
        jPrevSelectionButton = new javax.swing.JButton();
        jNextSelectionButton = new javax.swing.JButton();
        jSelectionEndButton = new javax.swing.JButton();
        jSelectButton = new javax.swing.JButton();
        jMarkAsFakeBox = new javax.swing.JCheckBox();
        jPlayerSourcesOnlyBox = new javax.swing.JCheckBox();
        jButton8 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jSetFakeButton = new javax.swing.JButton();
        jSetNoFakeButton = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jTargetPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jTargetAllyLabel = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTargetAllyList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTargetTribeList = new javax.swing.JList();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTargetContinentList = new javax.swing.JList();
        jLabel24 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTargetVillageList = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        jVictimTable = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jAlgoPanel = new javax.swing.JPanel();
        jBruteForceAlgorithm = new javax.swing.JRadioButton();
        jAllInOneAlgorithm = new javax.swing.JRadioButton();
        jBlitzkriegAlgorithm = new javax.swing.JRadioButton();

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
        jTargetsBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowResultDetailsEvent(evt);
            }
        });

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
        jAttacksBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowResultDetailsEvent(evt);
            }
        });

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png"))); // NOI18N
        jLabel10.setText(bundle.getString("TribeTribeAttackFrame.jLabel10.text")); // NOI18N

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText(bundle.getString("TribeTribeAttackFrame.jLabel5.text")); // NOI18N

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText(bundle.getString("TribeTribeAttackFrame.jLabel8.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                    .addComponent(jTargetsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jAttacksBar, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(jEnoblementsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                    .addComponent(jFullOffsBar, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8))
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
                .addContainerGap(395, Short.MAX_VALUE)
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
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jResultFrameLayout.setVerticalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
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
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)))
                    .addGroup(jOffStrengthFrameLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToleranceValue, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jOffStrengthFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12)
                    .addComponent(jButton13))
                .addContainerGap())
        );

        jAttackResultDetailsFrame.setTitle(bundle.getString("TribeTribeAttackFrame.jAttackResultDetailsFrame.title")); // NOI18N

        jHideAttackDetailsButton.setBackground(new java.awt.Color(239, 235, 223));
        jHideAttackDetailsButton.setText(bundle.getString("TribeTribeAttackFrame.jHideAttackDetailsButton.text")); // NOI18N
        jHideAttackDetailsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideResultDetailsEvent(evt);
            }
        });

        jNotAssignedSourcesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane11.setViewportView(jNotAssignedSourcesTable);

        javax.swing.GroupLayout jAttackResultDetailsFrameLayout = new javax.swing.GroupLayout(jAttackResultDetailsFrame.getContentPane());
        jAttackResultDetailsFrame.getContentPane().setLayout(jAttackResultDetailsFrameLayout);
        jAttackResultDetailsFrameLayout.setHorizontalGroup(
            jAttackResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackResultDetailsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jHideAttackDetailsButton)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
                .addContainerGap())
        );
        jAttackResultDetailsFrameLayout.setVerticalGroup(
            jAttackResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackResultDetailsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jHideAttackDetailsButton)
                .addContainerGap())
        );

        jTargetResultDetailsFrame.setTitle(bundle.getString("TribeTribeAttackFrame.jTargetResultDetailsFrame.title")); // NOI18N

        jTargetDetailsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane12.setViewportView(jTargetDetailsTable);

        jHideTargetDetailsButton.setText(bundle.getString("TribeTribeAttackFrame.jHideTargetDetailsButton.text")); // NOI18N
        jHideTargetDetailsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideResultDetailsEvent(evt);
            }
        });

        javax.swing.GroupLayout jTargetResultDetailsFrameLayout = new javax.swing.GroupLayout(jTargetResultDetailsFrame.getContentPane());
        jTargetResultDetailsFrame.getContentPane().setLayout(jTargetResultDetailsFrameLayout);
        jTargetResultDetailsFrameLayout.setHorizontalGroup(
            jTargetResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTargetResultDetailsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTargetResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jHideTargetDetailsButton)
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE))
                .addContainerGap())
        );
        jTargetResultDetailsFrameLayout.setVerticalGroup(
            jTargetResultDetailsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTargetResultDetailsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jHideTargetDetailsButton)
                .addContainerGap())
        );

        jAttackPlanSelectionDialog.setTitle(bundle.getString("TribeTribeAttackFrame.jAttackPlanSelectionDialog.title")); // NOI18N
        jAttackPlanSelectionDialog.setAlwaysOnTop(true);
        jAttackPlanSelectionDialog.setModal(true);

        jAttackPlanTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Angriffsplan", "Abgleichen"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane13.setViewportView(jAttackPlanTable);

        jDoSyncButton.setBackground(new java.awt.Color(239, 235, 223));
        jDoSyncButton.setText(bundle.getString("TribeTribeAttackFrame.jDoSyncButton.text")); // NOI18N
        jDoSyncButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSynchWithAttackPlansEvent(evt);
            }
        });

        jCancelSyncButton.setBackground(new java.awt.Color(239, 235, 223));
        jCancelSyncButton.setText(bundle.getString("TribeTribeAttackFrame.jCancelSyncButton.text")); // NOI18N
        jCancelSyncButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSynchWithAttackPlansEvent(evt);
            }
        });

        javax.swing.GroupLayout jAttackPlanSelectionDialogLayout = new javax.swing.GroupLayout(jAttackPlanSelectionDialog.getContentPane());
        jAttackPlanSelectionDialog.getContentPane().setLayout(jAttackPlanSelectionDialogLayout);
        jAttackPlanSelectionDialogLayout.setHorizontalGroup(
            jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                        .addComponent(jCancelSyncButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDoSyncButton))
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
                .addContainerGap())
        );
        jAttackPlanSelectionDialogLayout.setVerticalGroup(
            jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDoSyncButton)
                    .addComponent(jCancelSyncButton))
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

        jTroopsList.setToolTipText(bundle.getString("TribeTribeAttackFrame.jTroopsList.toolTipText")); // NOI18N
        jTroopsList.setMaximumSize(new java.awt.Dimension(500, 20));
        jTroopsList.setMinimumSize(new java.awt.Dimension(20, 20));
        jTroopsList.setPreferredSize(new java.awt.Dimension(150, 20));

        jSourceVillageLabel2.setText(bundle.getString("TribeTribeAttackFrame.jSourceVillageLabel2.text")); // NOI18N

        jScrollPane4.setViewportView(jVillageGroupList);

        jScrollPane5.setMaximumSize(new java.awt.Dimension(60, 132));
        jScrollPane5.setMinimumSize(new java.awt.Dimension(60, 132));
        jScrollPane5.setPreferredSize(new java.awt.Dimension(60, 132));

        jScrollPane5.setViewportView(jSourceContinentList);

        jLabel21.setText(bundle.getString("TribeTribeAttackFrame.jLabel21.text")); // NOI18N

        jScrollPane6.setPreferredSize(new java.awt.Dimension(100, 130));

        jScrollPane6.setViewportView(jSourceVillageList);

        jLabel22.setText(bundle.getString("TribeTribeAttackFrame.jLabel22.text")); // NOI18N

        jSourceGroupRelation.setSelected(true);
        jSourceGroupRelation.setText(bundle.getString("TribeTribeAttackFrame.jSourceGroupRelation.text")); // NOI18N
        jSourceGroupRelation.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSourceGroupRelation.toolTipText")); // NOI18N
        jSourceGroupRelation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_and.png"))); // NOI18N
        jSourceGroupRelation.setOpaque(false);
        jSourceGroupRelation.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_or.png"))); // NOI18N
        jSourceGroupRelation.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireSourceRelationChangedEvent(evt);
            }
        });

        jSelectionStart.setText(bundle.getString("TribeTribeAttackFrame.jSelectionStart.text")); // NOI18N
        jSelectionStart.setMaximumSize(new java.awt.Dimension(40, 25));
        jSelectionStart.setMinimumSize(new java.awt.Dimension(40, 25));
        jSelectionStart.setPreferredSize(new java.awt.Dimension(40, 25));

        jSelectionEnd.setText(bundle.getString("TribeTribeAttackFrame.jSelectionEnd.text")); // NOI18N
        jSelectionEnd.setMaximumSize(new java.awt.Dimension(40, 25));
        jSelectionEnd.setMinimumSize(new java.awt.Dimension(40, 25));
        jSelectionEnd.setPreferredSize(new java.awt.Dimension(40, 25));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(bundle.getString("TribeTribeAttackFrame.jLabel4.text")); // NOI18N
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jSelectionBeginButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectionBeginButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/beginning.png"))); // NOI18N
        jSelectionBeginButton.setText(bundle.getString("TribeTribeAttackFrame.jSelectionBeginButton.text")); // NOI18N
        jSelectionBeginButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSelectionBeginButton.toolTipText")); // NOI18N
        jSelectionBeginButton.setEnabled(false);
        jSelectionBeginButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jSelectionBeginButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jSelectionBeginButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jSelectionBeginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });

        jPrevSelectionButton.setBackground(new java.awt.Color(239, 235, 223));
        jPrevSelectionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/prev.png"))); // NOI18N
        jPrevSelectionButton.setText(bundle.getString("TribeTribeAttackFrame.jPrevSelectionButton.text")); // NOI18N
        jPrevSelectionButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jPrevSelectionButton.toolTipText")); // NOI18N
        jPrevSelectionButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jPrevSelectionButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jPrevSelectionButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jPrevSelectionButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });

        jNextSelectionButton.setBackground(new java.awt.Color(239, 235, 223));
        jNextSelectionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/next.png"))); // NOI18N
        jNextSelectionButton.setText(bundle.getString("TribeTribeAttackFrame.jNextSelectionButton.text")); // NOI18N
        jNextSelectionButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jNextSelectionButton.toolTipText")); // NOI18N
        jNextSelectionButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jNextSelectionButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jNextSelectionButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jNextSelectionButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });

        jSelectionEndButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectionEndButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/end.png"))); // NOI18N
        jSelectionEndButton.setText(bundle.getString("TribeTribeAttackFrame.jSelectionEndButton.text")); // NOI18N
        jSelectionEndButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSelectionEndButton.toolTipText")); // NOI18N
        jSelectionEndButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jSelectionEndButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jSelectionEndButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jSelectionEndButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });

        jSelectButton.setBackground(new java.awt.Color(239, 235, 223));
        jSelectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/select.png"))); // NOI18N
        jSelectButton.setText(bundle.getString("TribeTribeAttackFrame.jSelectButton.text")); // NOI18N
        jSelectButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSelectButton.toolTipText")); // NOI18N
        jSelectButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jSelectButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jSelectButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jSelectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateSelectionEvent(evt);
            }
        });

        jMarkAsFakeBox.setText(bundle.getString("TribeTribeAttackFrame.jMarkAsFakeBox.text")); // NOI18N
        jMarkAsFakeBox.setToolTipText(bundle.getString("TribeTribeAttackFrame.jMarkAsFakeBox.toolTipText")); // NOI18N
        jMarkAsFakeBox.setOpaque(false);

        jPlayerSourcesOnlyBox.setSelected(true);
        jPlayerSourcesOnlyBox.setText(bundle.getString("TribeTribeAttackFrame.jPlayerSourcesOnlyBox.text")); // NOI18N
        jPlayerSourcesOnlyBox.setToolTipText(bundle.getString("TribeTribeAttackFrame.jPlayerSourcesOnlyBox.toolTipText")); // NOI18N
        jPlayerSourcesOnlyBox.setOpaque(false);
        jPlayerSourcesOnlyBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireShowPlayerSourcesOnlyChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSourceVillageLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSourceGroupRelation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTroopsList, javax.swing.GroupLayout.Alignment.LEADING, 0, 175, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jSourceVillageLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jMarkAsFakeBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPlayerSourcesOnlyBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jSelectionStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel4)
                            .addGap(11, 11, 11)
                            .addComponent(jSelectionEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jSelectionBeginButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jPrevSelectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jSelectButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jNextSelectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jSelectionEndButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSourceVillageLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSourceVillageLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSourceGroupRelation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTroopsList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22)
                            .addComponent(jMarkAsFakeBox)
                            .addComponent(jPlayerSourcesOnlyBox)))
                    .addComponent(jSelectionEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jSelectionStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4))
                    .addComponent(jNextSelectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSelectionEndButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSelectButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSelectionBeginButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPrevSelectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
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
        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/filter_strength.png"))); // NOI18N
        jButton14.setText(bundle.getString("TribeTribeAttackFrame.jButton14.text")); // NOI18N
        jButton14.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton14.toolTipText")); // NOI18N
        jButton14.setMaximumSize(new java.awt.Dimension(59, 59));
        jButton14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFilterTroopStrengthEvent(evt);
            }
        });

        jLabel20.setText(bundle.getString("TribeTribeAttackFrame.jLabel20.text")); // NOI18N

        jSetFakeButton.setBackground(new java.awt.Color(239, 235, 223));
        jSetFakeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/fake.png"))); // NOI18N
        jSetFakeButton.setText(bundle.getString("TribeTribeAttackFrame.jSetFakeButton.text")); // NOI18N
        jSetFakeButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSetFakeButton.toolTipText")); // NOI18N
        jSetFakeButton.setMaximumSize(new java.awt.Dimension(59, 33));
        jSetFakeButton.setMinimumSize(new java.awt.Dimension(59, 33));
        jSetFakeButton.setPreferredSize(new java.awt.Dimension(59, 33));
        jSetFakeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeSourceFakeStateEvent(evt);
            }
        });

        jSetNoFakeButton.setBackground(new java.awt.Color(239, 235, 223));
        jSetNoFakeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/no_fake.png"))); // NOI18N
        jSetNoFakeButton.setText(bundle.getString("TribeTribeAttackFrame.jSetNoFakeButton.text")); // NOI18N
        jSetNoFakeButton.setToolTipText(bundle.getString("TribeTribeAttackFrame.jSetNoFakeButton.toolTipText")); // NOI18N
        jSetNoFakeButton.setMaximumSize(new java.awt.Dimension(59, 33));
        jSetNoFakeButton.setMinimumSize(new java.awt.Dimension(59, 33));
        jSetNoFakeButton.setPreferredSize(new java.awt.Dimension(59, 33));
        jSetNoFakeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeSourceFakeStateEvent(evt);
            }
        });

        jButton15.setBackground(new java.awt.Color(239, 235, 223));
        jButton15.setText(bundle.getString("TribeTribeAttackFrame.jButton15.text")); // NOI18N
        jButton15.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton15.toolTipText")); // NOI18N
        jButton15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireGetSourceVillagesFromClipboardEvent(evt);
            }
        });

        jButton17.setBackground(new java.awt.Color(239, 235, 223));
        jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/filter_off.png"))); // NOI18N
        jButton17.setText(bundle.getString("TribeTribeAttackFrame.jButton17.text")); // NOI18N
        jButton17.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton17.toolTipText")); // NOI18N
        jButton17.setMaximumSize(new java.awt.Dimension(59, 59));
        jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFilterSourceByAttackPlansEvent(evt);
            }
        });

        javax.swing.GroupLayout jSourcePanelLayout = new javax.swing.GroupLayout(jSourcePanel);
        jSourcePanel.setLayout(jSourcePanelLayout);
        jSourcePanelLayout.setHorizontalGroup(
            jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jSourcePanelLayout.createSequentialGroup()
                        .addComponent(jButton15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton11)
                    .addComponent(jSetNoFakeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSetFakeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jSourcePanelLayout.setVerticalGroup(
            jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jSourcePanelLayout.createSequentialGroup()
                        .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSetNoFakeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSetFakeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton8)
                    .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton7)
                        .addComponent(jButton15)))
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jSourcePanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png")), jSourcePanel); // NOI18N

        jTargetPanel.setBackground(new java.awt.Color(239, 235, 223));

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setOpaque(false);

        jTargetAllyLabel.setText(bundle.getString("TribeTribeAttackFrame.jTargetAllyLabel.text")); // NOI18N

        jScrollPane7.setMaximumSize(new java.awt.Dimension(260, 140));
        jScrollPane7.setMinimumSize(new java.awt.Dimension(260, 140));
        jScrollPane7.setPreferredSize(new java.awt.Dimension(260, 140));

        jTargetAllyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane7.setViewportView(jTargetAllyList);

        jLabel1.setText(bundle.getString("TribeTribeAttackFrame.jLabel1.text")); // NOI18N

        jTargetTribeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane8.setViewportView(jTargetTribeList);

        jLabel23.setText(bundle.getString("TribeTribeAttackFrame.jLabel23.text")); // NOI18N

        jScrollPane9.setMaximumSize(new java.awt.Dimension(60, 132));
        jScrollPane9.setMinimumSize(new java.awt.Dimension(60, 132));
        jScrollPane9.setPreferredSize(new java.awt.Dimension(60, 132));

        jScrollPane9.setViewportView(jTargetContinentList);

        jLabel24.setText(bundle.getString("TribeTribeAttackFrame.jLabel24.text")); // NOI18N

        jScrollPane10.setMaximumSize(new java.awt.Dimension(220, 132));
        jScrollPane10.setMinimumSize(new java.awt.Dimension(100, 132));
        jScrollPane10.setPreferredSize(new java.awt.Dimension(240, 132));

        jScrollPane10.setViewportView(jTargetVillageList);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTargetAllyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTargetAllyLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
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

        jButton16.setBackground(new java.awt.Color(239, 235, 223));
        jButton16.setText(bundle.getString("TribeTribeAttackFrame.jButton16.text")); // NOI18N
        jButton16.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton16.toolTipText")); // NOI18N
        jButton16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireGetTargetVillagesFromClipboardEvent(evt);
            }
        });

        javax.swing.GroupLayout jTargetPanelLayout = new javax.swing.GroupLayout(jTargetPanel);
        jTargetPanel.setLayout(jTargetPanelLayout);
        jTargetPanelLayout.setHorizontalGroup(
            jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTargetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 805, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTargetPanelLayout.createSequentialGroup()
                        .addComponent(jButton16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9)))
                .addContainerGap())
        );
        jTargetPanelLayout.setVerticalGroup(
            jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTargetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton9)
                    .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jTargetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton10)
                        .addComponent(jButton16)))
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jTargetPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jTargetPanel); // NOI18N

        attackTypeGroup.add(jBruteForceAlgorithm);
        jBruteForceAlgorithm.setSelected(true);
        jBruteForceAlgorithm.setText(bundle.getString("TribeTribeAttackFrame.jBruteForceAlgorithm.text")); // NOI18N
        jBruteForceAlgorithm.setIconTextGap(20);
        jBruteForceAlgorithm.setOpaque(false);
        jBruteForceAlgorithm.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlgorithmChangedEvent(evt);
            }
        });

        attackTypeGroup.add(jAllInOneAlgorithm);
        jAllInOneAlgorithm.setText(bundle.getString("TribeTribeAttackFrame.jAllInOneAlgorithm.text")); // NOI18N
        jAllInOneAlgorithm.setActionCommand(bundle.getString("TribeTribeAttackFrame.jAllInOneAlgorithm.actionCommand")); // NOI18N
        jAllInOneAlgorithm.setIconTextGap(20);
        jAllInOneAlgorithm.setOpaque(false);
        jAllInOneAlgorithm.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlgorithmChangedEvent(evt);
            }
        });

        attackTypeGroup.add(jBlitzkriegAlgorithm);
        jBlitzkriegAlgorithm.setText(bundle.getString("TribeTribeAttackFrame.jBlitzkriegAlgorithm.text")); // NOI18N
        jBlitzkriegAlgorithm.setActionCommand(bundle.getString("TribeTribeAttackFrame.jBlitzkriegAlgorithm.actionCommand")); // NOI18N
        jBlitzkriegAlgorithm.setIconTextGap(20);
        jBlitzkriegAlgorithm.setOpaque(false);
        jBlitzkriegAlgorithm.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlgorithmChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jAlgoPanelLayout = new javax.swing.GroupLayout(jAlgoPanel);
        jAlgoPanel.setLayout(jAlgoPanelLayout);
        jAlgoPanelLayout.setHorizontalGroup(
            jAlgoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAlgoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAlgoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBruteForceAlgorithm)
                    .addComponent(jAllInOneAlgorithm)
                    .addComponent(jBlitzkriegAlgorithm))
                .addContainerGap(160, Short.MAX_VALUE))
        );
        jAlgoPanelLayout.setVerticalGroup(
            jAlgoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAlgoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBruteForceAlgorithm)
                .addGap(18, 18, 18)
                .addComponent(jAllInOneAlgorithm)
                .addGap(18, 18, 18)
                .addComponent(jBlitzkriegAlgorithm)
                .addContainerGap(351, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("TribeTribeAttackFrame.jAlgoPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/res/die.png")), jAlgoPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 830, Short.MAX_VALUE)
                    .addComponent(jCalculateButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCalculateButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
    //add selected attack sources
    Object[] values = jSourceVillageList.getSelectedValues();
    if (values == null) {
        return;
    }
    UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
    for (Object value : values) {
        Village vSource = (Village) value;
        ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{vSource, uSource, jMarkAsFakeBox.isSelected()});
    }
}//GEN-LAST:event_fireAddAttackEvent

private void fireRemoveAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAttackEvent
    //remove selected attack sources
    int[] rows = jAttacksTable.getSelectedRows();
    if ((rows != null) && (rows.length > 0)) {
        String message = "Angriff entfernen?";
        if (rows.length > 1) {
            message = rows.length + " Angriffe entfernen?";
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Angriff entfernen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
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
    //algorithm calculation
    //pre check
    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    DefaultTableModel attackModel = (DefaultTableModel) jAttacksTable.getModel();
    if (attackModel.getRowCount() == 0) {
        JOptionPaneHelper.showErrorBox(this, "Keine Herkunftsdörfer ausgewählt", "Fehler");
        jTabbedPane1.setSelectedIndex(0);
        return;
    }

    if (victimModel.getRowCount() == 0) {
        JOptionPaneHelper.showErrorBox(this, "Keine Ziele ausgewählt", "Fehler");
        jTabbedPane1.setSelectedIndex(1);
        return;
    }

    if (!mTimePanel.validatePanel()) {
        jTabbedPane1.setSelectedIndex(3);
        return;
    }

    if (!mMiscPanel.validatePanel()) {
        jTabbedPane1.setSelectedIndex(4);
        return;
    }

    //reading values
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
    int maxAttacksPerVillage = mMiscPanel.getMaxAttacksPerVillage();

    int minCleanForSnob = mMiscPanel.getCleanOffsPerEnoblement();
    boolean randomize = mMiscPanel.isRandomize();
    TimeFrame timeFrame = mTimePanel.getTimeFrame();
    //</editor-fold>

    //start processing
    List<AbstractTroopMovement> result = new LinkedList<AbstractTroopMovement>();
    AbstractAttackAlgorithm algo = null;
    boolean supportMiscUnits = false;
    int type = 0;
    if (jAllInOneAlgorithm.isSelected()) {
        type = 1;
    } else if (jBlitzkriegAlgorithm.isSelected()) {
        type = 2;
    } //else: algorithm stays 0 (=BruteForce)


    if (type == 0) {
        logger.info("Using 'BruteForce' algorithm");
        algo = new BruteForce();
        supportMiscUnits = true;
    } else if (type == 1) {
        logger.info("Using 'AllInOne' algorithm");
        algo = new AllInOne();
    } else if (type == 2) {
        logger.info("Using 'Blitzkrieg' algorithm");
        algo = new Blitzkrieg();
    }
    //postprocessing = calculating optimal snob locations

    //check misc-units criteria
    if (useMiscUnits && !supportMiscUnits) {
        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Der gewählte Algorithmus unterstützt nur Rammen, Katapulte und AGs als angreifende Einheiten.\n" +
                "Dörfer für die eine andere Einheit gewählt wurde werden ignoriert.\n" +
                "Trotzdem fortfahren?", "Warnung", "Nein", "Ja") == JOptionPane.NO_OPTION) {
            return;
        }
    }

    result = algo.calculateAttacks(sources,
            fakes,
            victimVillages,
            maxAttacksPerVillage,
            minCleanForSnob,
            timeFrame,
            randomize);

    List<Attack> attackList = new LinkedList<Attack>();
    List<Village> targets = new LinkedList<Village>();

    // <editor-fold defaultstate="collapsed" desc=" Post processing ">
    int validEnoblements = 0;

    // <editor-fold defaultstate="collapsed" desc="Additional assignment">

    logger.debug("Algorithm post-processing skipped");
    for (AbstractTroopMovement movement : result) {
        List<Attack> atts = null;
        atts = movement.getAttacks(new Date(timeFrame.getEnd()));
        for (Attack attack : atts) {
            attackList.add(attack);
            if (!targets.contains(attack.getTarget())) {
                targets.add(attack.getTarget());
            }
        }
    }
    validEnoblements = algo.getValidEnoblements();

    //add misc attacks for Blitzkrieg
    if (type == 2) {
        List<Attack> misc = ((Blitzkrieg) algo).getMiscAttacks();
        for (Attack a : misc) {
            attackList.add(a);
        }
    }

    Hashtable<Village, Integer> attackMappings = new Hashtable<Village, Integer>();
    //get targets and attack count
    for (int i = 0; i < jVictimTable.getRowCount(); i++) {
        attackMappings.put((Village) jVictimTable.getValueAt(i, 1), 0);
    }
    for (Attack a : attackList) {
        Village v = a.getTarget();
        Integer val = attackMappings.get(v);
        attackMappings.put(v, val + 1);
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

    // <editor-fold defaultstate="collapsed" desc="Building details tables">
    buildDetailedStatistics(attackMappings, algo.getNotAssignedSources());
    //</editor-fold>

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
    //initialize transfer of results to attack view
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
    //copy results formatted to clipboard
    try {
        boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(jResultFrame, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

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

            if (JOptionPaneHelper.showQuestionConfirmBox(jResultFrame, "Die ausgewählten Angriffe benötigen mehr als 500 BB-Codes\n" +
                    "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {

                return;
            }

            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.yesButtonText", "Yes");
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
        String result = "Daten in Zwischenablage kopiert.";
        JOptionPaneHelper.showInformationBox(jResultFrame, result, "Information");
    } catch (Exception e) {
        logger.error("Failed to copy data to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPaneHelper.showErrorBox(jResultFrame, result, "Fehler");
    }
}//GEN-LAST:event_fireAttacksToClipboardEvent

private void fireUnformattedAttacksToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUnformattedAttacksToClipboardEvent
    //copy results unformatted to clipboard
    try {
        DefaultTableModel resultModel = (DefaultTableModel) jResultsTable.getModel();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i <
                resultModel.getRowCount(); i++) {
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
        JOptionPaneHelper.showInformationBox(jResultFrame, result, "Information");
    } catch (Exception e) {
        logger.error("Failed to copy data to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPaneHelper.showErrorBox(jResultFrame, result, "Fehler");
    }
}//GEN-LAST:event_fireUnformattedAttacksToClipboardEvent

private void fireAddAllPlayerVillages(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAllPlayerVillages
    //add all source villages to source list
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

private void fireRemoveTargetVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveTargetVillageEvent
    //remove selected targets
    int[] rows = jVictimTable.getSelectedRows();
    if ((rows != null) && (rows.length > 0)) {
        String message = "Ziel entfernen?";
        if (rows.length > 1) {
            message = rows.length + " Ziele entfernen?";
        }


        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Ziel entfernen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
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
    //add selected target villages
    Tribe target = (Tribe) jTargetTribeList.getSelectedValue();
    Object[] villages = jTargetVillageList.getSelectedValues();
    if (villages == null) {
        return;
    }

    DefaultTableModel victimModel = (DefaultTableModel) jVictimTable.getModel();
    jVictimTable.invalidate();
    for (Object o : villages) {
        Village village = (Village) o;
        victimModel.addRow(new Object[]{target, village});
    }
    jVictimTable.revalidate();
    jVictimTable.repaint();//.updateUI();
}//GEN-LAST:event_fireAddTargetVillageEvent

private void fireAddAllTargetVillagesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAllTargetVillagesEvent
    //add all current target villages
    Tribe target = (Tribe) jTargetTribeList.getSelectedValue();
    if (target == null) {
        return;
    }

    jVictimTable.invalidate();
    int size = jTargetVillageList.getModel().getSize();
    for (int i = 0; i < size; i++) {
        ((DefaultTableModel) jVictimTable.getModel()).addRow(new Object[]{target, jTargetVillageList.getModel().getElementAt(i)});
    }

    jVictimTable.revalidate();
    jVictimTable.repaint();//.updateUI();
}//GEN-LAST:event_fireAddAllTargetVillagesEvent

private void fireTransferAttacksToPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferAttacksToPlanEvent
    //transfer results to attack view
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
    //select source villages on map
    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
    //MapPanel.getSingleton().setVillageSelectionListener(this);
    DSWorkbenchMainFrame.getSingleton().toFront();
    DSWorkbenchMainFrame.getSingleton().requestFocus();
    bChooseSourceRegionMode = true;
}//GEN-LAST:event_fireChooseSourceRegionEvent

private void fireChooseTargetRegionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChooseTargetRegionEvent
    //select target villages on map
    /*Tribe victim = null;
    try {
    victim = (Tribe) jTargetTribeList.getSelectedValue();
    } catch (Exception e) {
    }
    if (victim == null) {
    JOptionPaneHelper.showInformationBox(this, "Kein gültiger Spieler ausgewählt.", "Fehler");
    return;
    }
    //calculate mass of villages and center to it
    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
    MapPanel.getSingleton().setVillageSelectionListener(this);
    Point com = DSCalculator.calculateCenterOfMass(victim.getVillageList());
    DSWorkbenchMainFrame.getSingleton().centerPosition(com.x, com.y);*/
    DSWorkbenchMainFrame.getSingleton().toFront();
    DSWorkbenchMainFrame.getSingleton().requestFocus();
    bChooseTargetRegionMode = true;
}//GEN-LAST:event_fireChooseTargetRegionEvent

private void fireUseSnobEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUseSnobEvent
    //use snobs in villages where snobs exist
    DefaultTableModel model = (DefaultTableModel) jAttacksTable.getModel();
    int rows = model.getRowCount();
    UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
    jAttacksTable.invalidate();
    Hashtable<Village, Integer> assignedTroops = new Hashtable<Village, Integer>();
    for (int row = 0; row < rows; row++) {
        Village v = (Village) model.getValueAt(row, 0);
        VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(v);
        if (troops != null) {
            int availSnobs = troops.getTroopsInVillage().get(snob);
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

private void fireToleranceChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireToleranceChangedEvent
    //recalculate strength values
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
        JOptionPaneHelper.showErrorBox(jOffStrengthFrame, "Bitte nur ganzzahlige Einträge verwenden.", "Fehler");
    }
}//GEN-LAST:event_fireToleranceChangedEvent

private void fireAcceptStrengthEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAcceptStrengthEvent
    //remove sources with too small strength
    int strength = Integer.parseInt(jStrengthField.getText());
    int diff = (int) Math.floor((double) strength * (double) jToleranceSlider.getValue() / 100);
    int removeCount = 0;
    int noInformation = 0;
    List<Integer> toRemove = new LinkedList<Integer>();
    for (int i = 0; i < jAttacksTable.getRowCount(); i++) {
        Village v = (Village) jAttacksTable.getValueAt(i, 0);
        try {
            VillageTroopsHolder troops = TroopsManager.getSingleton().getTroopsForVillage(v);
            int offValue = 0;
            if (troops != null) {
                //use strength of troops in village
                offValue = (int) troops.getOffValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
                if (offValue < strength - diff) {
                    //int row = jAttacksTable.convertRowIndexToModel(i);
                    //model.removeRow(row);
                    toRemove.add(i);
                    removeCount++;
                }
            } else {
                //no troops available, skip removal
                noInformation++;
            }
        } catch (Exception e) {
        }
    }

    //remove rows
    for (int i = toRemove.size() - 1; i >= 0; i--) {
        jAttacksTable.invalidate();
        int row = jAttacksTable.convertRowIndexToModel(toRemove.get(i));
        ((DefaultTableModel) jAttacksTable.getModel()).removeRow(row);
        jAttacksTable.revalidate();
    }

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
    JOptionPaneHelper.showInformationBox(this, message, "Information");
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

private void fireChangeSourceFakeStateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeSourceFakeStateEvent
    //change marked attacks to fake/no fake
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

private void fireShowResultDetailsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowResultDetailsEvent
    if (evt.getSource() == jAttacksBar) {
        jAttackResultDetailsFrame.setVisible(true);
    } else if (evt.getSource() == jTargetsBar) {
        jTargetResultDetailsFrame.setVisible(true);
    }
}//GEN-LAST:event_fireShowResultDetailsEvent

private void fireHideResultDetailsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideResultDetailsEvent
    if (evt.getSource() == jHideAttackDetailsButton) {
        jAttackResultDetailsFrame.setVisible(false);
    } else if (evt.getSource() == jHideTargetDetailsButton) {
        jTargetResultDetailsFrame.setVisible(false);
    }
}//GEN-LAST:event_fireHideResultDetailsEvent

private void fireAlgorithmChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlgorithmChangedEvent
    if (evt.getSource() == jBruteForceAlgorithm && jBruteForceAlgorithm.isSelected()) {
        mMiscPanel.setCleanOffsEnabled(false);
        mMiscPanel.setRandomizeEnabled(true);
        mTimePanel.activateTolerance(false);
    } else if (evt.getSource() == jAllInOneAlgorithm && jAllInOneAlgorithm.isSelected()) {
        mMiscPanel.setCleanOffsEnabled(true);
        mMiscPanel.setRandomizeEnabled(false);
        mTimePanel.activateTolerance(false);
    } else if (evt.getSource() == jBlitzkriegAlgorithm && jBlitzkriegAlgorithm.isSelected()) {
        mMiscPanel.setCleanOffsEnabled(false);
        mMiscPanel.setRandomizeEnabled(false);
        mTimePanel.activateTolerance(true);
    }
}//GEN-LAST:event_fireAlgorithmChangedEvent

private void fireUpdateSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateSelectionEvent
    int start = 1;
    int end = 10;
    try {
        start = Integer.parseInt(jSelectionStart.getText());
        end = Integer.parseInt(jSelectionEnd.getText());
    } catch (Exception e) {
    }
    try {
        //switch numbers if start larger than end
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        int diff = end - start + 1;
        if (evt == null || evt.getSource() == jSelectButton) {
            //do nothing
        } else if (evt.getSource() == jSelectionBeginButton && jSelectionBeginButton.isEnabled()) {
            start = 1;
            end = diff;
        } else if (evt.getSource() == jPrevSelectionButton) {
            start = start - diff;
            if (start <= 0) {
                start = 1;
            }
            end = start + diff - 1;
        } else if (evt.getSource() == jNextSelectionButton) {
            start = end + 1;
            end = (start + diff - 1 > jSourceVillageList.getModel().getSize()) ? jSourceVillageList.getModel().getSize() : (start + diff - 1);
        } else if (evt.getSource() == jSelectionEndButton && jSelectionEnd.isEnabled()) {
            end = jSourceVillageList.getModel().getSize();
            start = end - diff + 1;
        }


        jSelectionEndButton.setEnabled(!(end == jSourceVillageList.getModel().getSize()));
        jSelectionBeginButton.setEnabled(!(start == 1));
        jSelectionStart.setText(Integer.toString(start));
        jSelectionEnd.setText(Integer.toString(end));

        jSourceVillageList.getSelectionModel().setSelectionInterval(start - 1, end - 1);
        jSourceVillageList.scrollRectToVisible(jSourceVillageList.getCellBounds(start - 1, end - 1));
    } catch (Exception e) {
        logger.warn("Error while calculating source selection step", e);
    }
}//GEN-LAST:event_fireUpdateSelectionEvent

private void fireGetSourceVillagesFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireGetSourceVillagesFromClipboardEvent
    try {
        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        List<Village> villages = VillageParser.parse((String) t.getTransferData(DataFlavor.stringFlavor));
        if (villages == null || villages.isEmpty()) {
            JOptionPaneHelper.showInformationBox(this, "Es konnten keine Dorfkoodinaten in der Zwischenablage gefunden werden.", "Information");
            return;
        } else {
            UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
            for (Village v : villages) {
                ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{v, uSource, jMarkAsFakeBox.isSelected()});
            }
            String message = (villages.size() == 1) ? "1 Dorf " : villages.size() + " Dörfer ";
            JOptionPaneHelper.showInformationBox(this, message + " übertragen.", "Information");
        }
    } catch (Exception e) {
        logger.error("Failed to parse source villages from clipboard", e);
    }
}//GEN-LAST:event_fireGetSourceVillagesFromClipboardEvent

private void fireGetTargetVillagesFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireGetTargetVillagesFromClipboardEvent
    try {
        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        List<Village> villages = VillageParser.parse((String) t.getTransferData(DataFlavor.stringFlavor));
        if (villages == null || villages.isEmpty()) {
            JOptionPaneHelper.showInformationBox(this, "Es konnten keine Dorfkoodinaten in der Zwischenablage gefunden werden.", "Information");
            return;
        } else {
            for (Village v : villages) {
                Tribe victim = v.getTribe();
                if (victim != null) {
                    ((DefaultTableModel) jVictimTable.getModel()).addRow(new Object[]{victim, v});
                }
            }
            String message = (villages.size() == 1) ? "1 Dorf " : villages.size() + " Dörfer ";
            JOptionPaneHelper.showInformationBox(this, message + " übertragen.", "Information");
        }
    } catch (Exception e) {
        logger.error("Failed to parse victim villages from clipboard", e);
    }
}//GEN-LAST:event_fireGetTargetVillagesFromClipboardEvent

private void fireFilterSourceByAttackPlansEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFilterSourceByAttackPlansEvent

    DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{
                "Angriffsplan", "Abgleichen"}) {

        Class[] types = new Class[]{
            String.class, Boolean.class
        };

        @Override
        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0) {
                return false;
            }
            return true;
        }
    };

    jAttackPlanTable.invalidate();
    Enumeration<String> plans = AttackManager.getSingleton().getPlans();
    List<String> planList = new LinkedList<String>();
    while (plans.hasMoreElements()) {
        planList.add(plans.nextElement());
    }
    Collections.sort(planList);
    for (String plan : planList) {
        model.addRow(new Object[]{plan, false});
    }
    jAttackPlanTable.setModel(model);
    jAttackPlanTable.revalidate();
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
    for (int i = 0; i < jAttackPlanTable.getColumnCount(); i++) {
        jAttackPlanTable.getColumn(jAttackPlanTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
    }
    jAttackPlanSelectionDialog.setLocationRelativeTo(this);
    jAttackPlanSelectionDialog.setVisible(true);

}//GEN-LAST:event_fireFilterSourceByAttackPlansEvent

private void fireShowPlayerSourcesOnlyChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireShowPlayerSourcesOnlyChangedEvent
    fireFilterSourceVillagesByGroupEvent();
}//GEN-LAST:event_fireShowPlayerSourcesOnlyChangedEvent

private void fireSourceRelationChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireSourceRelationChangedEvent
    if (jSourceGroupRelation.isSelected()) {
        jSourceGroupRelation.setText("Verknüpfung (ODER)");
    } else {
        jSourceGroupRelation.setText("Verknüpfung (UND)");
    }
    fireFilterSourceVillagesByGroupEvent();
}//GEN-LAST:event_fireSourceRelationChangedEvent

private void fireSynchWithAttackPlansEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSynchWithAttackPlansEvent

    jAttackPlanSelectionDialog.setVisible(false);
    if (evt.getSource() == jCancelSyncButton) {
        return;
    }

    DefaultTableModel model = (DefaultTableModel) jAttackPlanTable.getModel();

    List<String> selectedPlans = new LinkedList<String>();
    for (int i = 0; i < jAttackPlanTable.getRowCount(); i++) {
        int row = jAttackPlanTable.convertRowIndexToModel(i);
        if ((Boolean) model.getValueAt(row, 1)) {
            selectedPlans.add((String) model.getValueAt(row, 0));
        }
    }

    // Enumeration<String> plans = AttackManager.getSingleton().getPlans();
    List<Integer> toRemove = new LinkedList<Integer>();
    //process all plans
    //  while (plans.hasMoreElements()) {
    for (String plan : selectedPlans) {
        //String plan = plans.nextElement();
        logger.debug("Checking plan '" + plan + "'");
        List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(plan);

        //process all attacks
        for (Attack a : attacks) {
            //search attack source village in all table rows
            for (int i = 0; i < jAttacksTable.getRowCount(); i++) {
                Village v = (Village) jAttacksTable.getValueAt(i, 0);
                if (a.getSource().equals(v)) {
                    if (!toRemove.contains(i)) {
                        toRemove.add(i);
                    }
                }
            }
        }
    }

    String message = "";
    if (toRemove.size() == 0) {
        JOptionPaneHelper.showInformationBox(this, "Keine Herkunftsdörfer zu entfernen.", "Information");
        return;
    } else {
        message = (toRemove.size() == 1) ? "Ein Herkunftsdorf " : toRemove.size() + " Herkunftsdörfer ";
    }
    if (JOptionPaneHelper.showQuestionConfirmBox(this, message + "entfernen?", "Entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        try {
            logger.debug("Removing " + toRemove.size() + " source villages");
            jAttacksTable.invalidate();
            Collections.sort(toRemove);

            while (toRemove.size() > 0) {
                Integer row = toRemove.remove(toRemove.size() - 1);
                ((DefaultTableModel) jAttacksTable.getModel()).removeRow(jAttacksTable.convertRowIndexToModel(row));
            }
            jAttacksTable.revalidate();
        } catch (Exception e) {
            logger.error("Removal failed", e);
        }
    }

}//GEN-LAST:event_fireSynchWithAttackPlansEvent

    private void showResults(List<Attack> pAttacks) {
        jResultsTable.invalidate();
        DefaultTableModel resultModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Truppen", "Ziel", "Startzeit", "Typ", ""}) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Village.class, Date.class, Integer.class, Boolean.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == 5) {
                    return false;
                }
                return true;
            }
        };

        //renderer, which hides the boolean table column
        DefaultTableCellRenderer invis = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.WHITE);
                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                r.setText("");
                r.setVisible(false);
                // r.setText(r.getText());
                return c;
            }
        };

        //renderer, which marks send times red if attack is impossible to send
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
                Boolean impossible = (Boolean) table.getModel().getValueAt(row, 5);
                if (impossible.booleanValue()) {
                    c.setBackground(Color.RED);
                }

                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                r.setText((value == null) ? "" : f.format(value));
                // r.setText(r.getText());
                return c;
            }
        };

        jResultsTable.setDefaultRenderer(Date.class, renderer);
        jResultsTable.setDefaultRenderer(Boolean.class, invis);
        jResultsTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());
        jResultsTable.setDefaultEditor(Integer.class, new AttackTypeCellEditor());

        //jResultsTable.setDefaultRenderer(Date.class, new DateCellRenderer());

        List<Long> startTimes = new LinkedList<Long>();
        int impossibleAttacks = 0;
        for (Attack a : pAttacks) {
            long targetTime = a.getArriveTime().getTime();
            long startTime = targetTime - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
            boolean impossible = false;
            if (!startTimes.contains(startTime)) {
                startTimes.add(startTime);
            } else {
                impossibleAttacks++;
                impossible = true;
            }
            resultModel.addRow(new Object[]{a.getSource(), a.getUnit(), a.getTarget(), new Date(startTime), a.getType(), impossible});
        }

        jResultsTable.setModel(resultModel);

        TableColumn tc = jResultsTable.getColumnModel().getColumn(5);
        tc.setPreferredWidth(0);
        tc.setMaxWidth(0);
        tc.setWidth(0);
        tc.setResizable(false);
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

        if (impossibleAttacks > 0) {
            String message = "";
            if (impossibleAttacks == 1) {
                message = "Ein berechneter Angriff kann vermutlich nicht abgeschickt werden.\nDer entsprechende Angriff ist in der Tabelle rot markiert.";
            } else {
                message = impossibleAttacks + " berechnete Angriffe können vermutlich nicht abgeschickt werden.\nDie entsprechenden Angriffe sind in der Tabelle rot markiert.";
            }
            JOptionPaneHelper.showWarningBox(jResultFrame, message, "Warnung");
        }
    }

    private void buildDetailedStatistics(Hashtable<Village, Integer> attackMappings, List<Village> pNotAssignedVillages) {
        // <editor-fold defaultstate="collapsed" desc="Build not assigned source table">

        Collections.sort(pNotAssignedVillages);
        DefaultTableModel sourcesModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf"}) {

            Class[] types = new Class[]{
                Tribe.class, Village.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        for (Village notAssigned : pNotAssignedVillages) {
            Tribe t = notAssigned.getTribe();
            if (t == null) {
                sourcesModel.addRow(new Object[]{"Barbaren", notAssigned});
            } else {
                sourcesModel.addRow(new Object[]{t, notAssigned});
            }
        }
        jNotAssignedSourcesTable.setModel(sourcesModel);
        TableRowSorter<TableModel> sourcesSorter = new TableRowSorter<TableModel>(sourcesModel);
        jNotAssignedSourcesTable.setRowSorter(sourcesSorter);
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
        for (int i = 0; i < jNotAssignedSourcesTable.getColumnCount(); i++) {
            jNotAssignedSourcesTable.getColumn(jNotAssignedSourcesTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }

        jNotAssignedSourcesTable.revalidate();
//</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build attacks per target table">


        DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Spieler", "Dorf", "Angriffe"}) {

            Class[] types = new Class[]{
                Tribe.class, Village.class, Integer.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        List<Village> notFullTargets = new LinkedList<Village>();
        Enumeration<Village> keys = attackMappings.keys();
        int max = mMiscPanel.getMaxAttacksPerVillage();
        while (keys.hasMoreElements()) {
            Village key = keys.nextElement();
            Tribe t = key.getTribe();
            int cnt = attackMappings.get(key);
            if (t != null) {
                tableModel.addRow(new Object[]{t, key, cnt});
            } else {
                tableModel.addRow(new Object[]{"Barbaren", key, cnt});
            }
            if (cnt < max) {
                notFullTargets.add(key);
            }
        }

        jTargetDetailsTable.setModel(tableModel);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);

        jTargetDetailsTable.setRowSorter(sorter);
        DefaultTableCellRenderer coloredRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                String t = ((DefaultTableCellRenderer) c).getText();
                ((DefaultTableCellRenderer) c).setText(t);
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int r = table.convertRowIndexToModel(row);
                Integer v = (Integer) model.getValueAt(r, 2);
                int max = mMiscPanel.getMaxAttacksPerVillage();
                long diff = max - v;
                Color back = Color.RED;
                if (v == 0) {
                    //color stays red
                } else if (v == max) {
                    back = Color.GREEN;
                } else {
                    float posv = 100.0f * (float) diff / (float) max;
                    posv = (int) ((int) posv / 10) * 10;
                    posv /= 100;
                    Color LAST_SEGMENT = new Color(255, 100, 0);
                    int red = (int) Math.rint((float) LAST_SEGMENT.getRed() * (1.0f - posv) + (float) Color.YELLOW.getRed() * posv);
                    int green = (int) Math.rint((float) LAST_SEGMENT.getGreen() * (1.0f - posv) + (float) Color.YELLOW.getGreen() * posv);
                    int blue = (int) Math.rint((float) LAST_SEGMENT.getBlue() * (1.0f - posv) + (float) Color.YELLOW.getBlue() * posv);
                    back = new Color(red, green, blue);
                }
                DefaultTableCellRenderer renderer = ((DefaultTableCellRenderer) c);
                renderer.setBackground(back);
                return c;
            }
        };
        jTargetDetailsTable.setDefaultRenderer(Village.class, coloredRenderer);
        jTargetDetailsTable.setDefaultRenderer(Integer.class, coloredRenderer);
        jTargetDetailsTable.setDefaultRenderer(String.class, coloredRenderer);
        jTargetDetailsTable.setDefaultRenderer(Tribe.class, coloredRenderer);


        for (int i = 0; i < jTargetDetailsTable.getColumnCount(); i++) {
            jTargetDetailsTable.getColumn(jTargetDetailsTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        jTargetDetailsTable.revalidate();
    //</editor-fold>
    }

    public void fireSelectionTransferEvent(List<Village> pSelection) {

        UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
        if (bChooseSourceRegionMode) {
            jAttacksTable.invalidate();
        } else if (bChooseTargetRegionMode) {
            jVictimTable.invalidate();
        }

        for (Village v : pSelection.toArray(new Village[]{})) {
            if (bChooseSourceRegionMode) {
                ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{v, uSource});
            } else if (bChooseTargetRegionMode) {
                ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{v, uSource});
            }
        }

        if (bChooseSourceRegionMode) {
            jAttacksTable.revalidate();
            jAttacksTable.repaint();
        } else if (bChooseTargetRegionMode) {
            jVictimTable.revalidate();
            jVictimTable.repaint();
        }

        // <editor-fold defaultstate="collapsed" desc=" OLD HANDLING">
       /*    if (bChooseSourceRegionMode) {
        UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
        // Tribe you = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe();
        jAttacksTable.invalidate();
        // List<Village> groupFiltered = getGroupFilteredSourceVillages();
        for (Village v : pSelection.toArray(new Village[]{})) {
        //  if (v != null && v.getTribe() != null && v.getTribe().equals(you) && groupFiltered.contains(v)) {
        ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{v, uSource});
        //                }
        }
        jAttacksTable.revalidate();
        /*  Tribe you = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getTribe();
        UnitHolder uSource = (UnitHolder) jTroopsList.getSelectedItem();
        jAttacksTable.invalidate();
        List<Village> groupFiltered = getGroupFilteredSourceVillages();
        for (int x = xStart; x <= xEnd; x++) {
        for (int y = yStart; y <= yEnd; y++) {
        Village v = DataHolder.getSingleton().getVillages()[x][y];
        if (v != null) {
        Tribe t = v.getTribe();
        if (t != null) {
        if (t.equals(you)) {
        if (groupFiltered.contains(v)) {
        //add only villages which are currently allowed
        ((DefaultTableModel) jAttacksTable.getModel()).addRow(new Object[]{v, uSource});
        }
        }
        }
        }
        }
        }
        jAttacksTable.revalidate();*/
        /*       } else if (bChooseTargetRegionMode) {
        Tribe victim = (Tribe) jTargetTribeList.getSelectedValue();
        jVictimTable.invalidate();
        /* Tribe victim = (Tribe) jTargetTribeList.getSelectedValue();
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
        jVictimTable.repaint();//.updateUI();*/
        //   }

        //</editor-fold>

        bChooseSourceRegionMode = false;
        bChooseTargetRegionMode = false;

        toFront();
        requestFocus();
    }

// <editor-fold defaultstate="collapsed" desc="Source selection handlers">
    private void fireFilterSourceVillagesByGroupEvent() {
        List<Village> villageList = getGroupFilteredSourceVillages();
        //build continents list
        List<String> continentList = new LinkedList<String>();
        for (Village v : villageList) {
            String cont = "K" + v.getContinent();
            if (!continentList.contains(cont)) {
                continentList.add(cont);
            }
        }

        Collections.sort(continentList, String.CASE_INSENSITIVE_ORDER);
        DefaultListModel contModel = new DefaultListModel();
        for (String cont : continentList) {
            contModel.addElement(cont);
        }

//set continents list -> village list updates automatically via continent list listener
        jSourceContinentList.setModel(contModel);
        jSourceContinentList.getSelectionModel().setSelectionInterval(0, continentList.size() - 1);
    }

    private void fireFilterSourceContinentEvent() {
        int[] conts = jSourceContinentList.getSelectedIndices();
        if (conts == null) {
            return;
        }
//build list of allowed continents

        List<Integer> allowedContinents = new LinkedList<Integer>();
        for (Integer cont : conts) {
            int contId = Integer.parseInt(((String) jSourceContinentList.getModel().getElementAt(cont)).replaceAll("K", ""));
            allowedContinents.add(contId);
        }

        List<Village> villageList = getGroupFilteredSourceVillages();

        try {
            List<Village> toRemove = new LinkedList<Village>();
            for (Village v : villageList) {
                int vCont = 0;
                if (ServerSettings.getSingleton().getCoordType() != 2) {
                    vCont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
                } else {
                    vCont = DSCalculator.getContinent(v.getX(), v.getY());
                }

                if (!allowedContinents.contains(vCont)) {
                    toRemove.add(v);
                }

            }

            //remove villages with wrong continent
            for (Village v : toRemove) {
                villageList.remove(v);
            }

//build village list
            DefaultListModel villageModel = new DefaultListModel();
            for (Village v : villageList) {
                villageModel.addElement(v);
            }

            jSourceVillageList.setModel(villageModel);
            jSourceVillageList.repaint();//.updateUI();
        } catch (Exception e) {
        }
    }

    private List<Village> getGroupFilteredSourceVillages() {
        Object[] values = (Object[]) jVillageGroupList.getSelectedValues();
        List<Village> villageList = new LinkedList<Village>();

        if (jVillageGroupList.isEnabled()) {
            List<Tag> tags = new LinkedList<Tag>();
            boolean useNoTag = false;
            for (Object o : values) {
                try {
                    tags.add((Tag) o);
                } catch (Exception e) {
                    //no-tag villages contained
                    useNoTag = true;
                }
            }
            boolean onlyPlayerVillages = jPlayerSourcesOnlyBox.isSelected();
            Tribe current = DSWorkbenchMainFrame.getSingleton().getCurrentUser();
            if (jSourceGroupRelation.isSelected()) {

                //default OR relation
                //tags available, use them
                for (Tag t : tags) {
                    for (Integer vId : t.getVillageIDs()) {
                        Village v = DataHolder.getSingleton().getVillagesById().get(vId);
                        if (v.getTribe() != null && !villageList.contains(v)) {
                            //add only if a players villages is tagged
                            if (!onlyPlayerVillages || (onlyPlayerVillages && v.getTribe().equals(current))) {
                                //use village if all villages are allowed or if owner is current player
                                villageList.add(v);
                            }
                        }

                    }
                }
                if (useNoTag) {
                    //use villages of current user which are not tagged
                    List<Village> villages = DSWorkbenchMainFrame.getSingleton().getCurrentUser().getVillageList();
                    for (Village v : villages) {
                        List<Tag> vtags = TagManager.getSingleton().getTags(v);
                        if (vtags == null || vtags.isEmpty() && !villageList.contains(v)) {
                            if (!onlyPlayerVillages || (onlyPlayerVillages && v.getTribe().equals(current))) {
                                //use village if all villages are allowed or if owner is current player
                                villageList.add(v);
                            }
                        }

                    }
                }
            } else {
                //AND relation
                if (tags.isEmpty() && useNoTag) {
                    //only use non tagges villages
                    //-> if one tag is selected for AND relation, ignore non-tagged due to logical error
                    if (useNoTag) {
                        //use villages of current user which are not tagged
                        List<Village> villages = DSWorkbenchMainFrame.getSingleton().getCurrentUser().getVillageList();
                        for (Village v : villages) {
                            List<Tag> vtags = TagManager.getSingleton().getTags(v);
                            if (vtags == null || vtags.isEmpty() && !villageList.contains(v)) {
                                if (!onlyPlayerVillages || (onlyPlayerVillages && v.getTribe().equals(current))) {
                                    //use village if all villages are allowed or if owner is current player
                                    villageList.add(v);
                                }
                            }

                        }
                    }
                } else {
                    //perform AND relation
                    List<Integer> villageIds = new LinkedList<Integer>();
                    //get all villages tagged by any tag
                    for (Tag t : TagManager.getSingleton().getTags()) {
                        for (Integer i : t.getVillageIDs()) {
                            if (!villageIds.contains(i)) {
                                villageIds.add(i);
                            }
                        }
                    }
                    //check for all villages, if they are tagged by all selected tags
                    for (Integer i : villageIds) {
                        boolean use = true;
                        for (Tag t : tags) {
                            if (!t.tagsVillage(i)) {
                                //at least one tag does not tag current village
                                use = false;
                                break;
                            }
                        }
                        if (use) {
                            //all tags tag current village, so use them
                            Village v = DataHolder.getSingleton().getVillagesById().get(i);
                            if (!villageList.contains(v)) {
                                if (!onlyPlayerVillages || (onlyPlayerVillages && v.getTribe().equals(current))) {
                                    //use village if all villages are allowed or if owner is current player
                                    villageList.add(v);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            //no tags available, take current users villages
            List<Village> villages = DSWorkbenchMainFrame.getSingleton().getCurrentUser().getVillageList();
            for (Village v : villages) {
                //use village if all villages are allowed or if owner is current player
                villageList.add(v);
            }
        }
        return villageList;
    }

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Target selection handlers">
    private void fireFilterTargetByAllyEvent() {
        Ally a = null;
        try {
            a = (Ally) jTargetAllyList.getSelectedValue();
        } catch (Exception e) {
        }

        if (a != null) {
            //ally selected
            Tribe[] tribes = a.getTribes().toArray(new Tribe[]{});
            if ((tribes != null) && (tribes.length != 0)) {
                Arrays.sort(tribes, Tribe.CASE_INSENSITIVE_ORDER);
                jTargetTribeList.setModel(new DefaultComboBoxModel(tribes));
                jTargetTribeList.setSelectedIndex(0);
            } else {
                //not tribes in ally -> should never happen
                jTargetTribeList.setModel(new DefaultListModel());
                jTargetContinentList.setModel(new DefaultListModel());
                jTargetVillageList.setModel(new DefaultListModel());
            }

        } else {
            //no ally selected, show no-ally tribes
            Enumeration<Integer> tribeIDs = DataHolder.getSingleton().getTribes().keys();
            List<Tribe> noAlly = new LinkedList<Tribe>();
            while (tribeIDs.hasMoreElements()) {
                Tribe t = DataHolder.getSingleton().getTribes().get(tribeIDs.nextElement());
                if (t.getAlly() == null && t.getVillageList() != null && !t.getVillageList().isEmpty()) {
                    //only add tribes which are attackable
                    noAlly.add(t);
                }
            }
            Tribe[] noAllyTribes = noAlly.toArray(new Tribe[]{});
            Arrays.sort(noAllyTribes, Tribe.CASE_INSENSITIVE_ORDER);
            jTargetTribeList.setModel(new DefaultComboBoxModel(noAllyTribes));
            jTargetTribeList.setSelectedIndex(0);
        }

    }

    private void fireFilterTargetByTribeEvent() {
        try {
            Tribe t = (Tribe) jTargetTribeList.getSelectedValue();
            if (t != null && t.getVillageList() != null && t.getVillageList().size() > 0) {
                Village[] villages = t.getVillageList().toArray(new Village[]{});
                List<String> continents = new LinkedList<String>();

                for (Village v : villages) {
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

                DefaultListModel contModel = new DefaultListModel();
                for (String cont : continents) {
                    contModel.addElement(cont);
                }

                jTargetContinentList.setModel(contModel);
                jTargetContinentList.repaint();//.updateUI();
                jTargetContinentList.getSelectionModel().setSelectionInterval(0, continents.size() - 1);
            } else {
                //no tribe selected -> should never happen!
                jTargetContinentList.setModel(new DefaultListModel());
                jTargetVillageList.setModel(new DefaultListModel());
            }

        } catch (Exception e) {
            jTargetContinentList.setModel(new DefaultListModel());
            jTargetVillageList.setModel(new DefaultListModel());
        }

    }

    private void fireFilterTargetByContinentEvent() {
        //build list of selected/valid continents
        Object[] conts = jTargetContinentList.getSelectedValues();
        List<Integer> validConts = new LinkedList<Integer>();
        for (Object cont : conts) {
            String c = (String) cont;
            c = c.replaceAll("K", "").trim();
            validConts.add(Integer.parseInt(c));
        }

        Tribe t = (Tribe) jTargetTribeList.getSelectedValue();
        Village[] villages = t.getVillageList().toArray(new Village[]{});
        Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
        DefaultListModel villageModel = new DefaultListModel();
        for (Village v : villages) {
            int cont = 0;
            if (ServerSettings.getSingleton().getCoordType() != 2) {
                cont = DSCalculator.xyToHierarchical(v.getX(), v.getY())[0];
            } else {
                cont = DSCalculator.getContinent(v.getX(), v.getY());
            }

            if (validConts.contains(cont)) {
                villageModel.addElement(v);
            }

        }
        jTargetVillageList.setModel(villageModel);
    }
// </editor-fold>

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new TribeTribeAttackFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup attackTypeGroup;
    private javax.swing.JButton jAddToAttacksButton;
    private javax.swing.JPanel jAlgoPanel;
    private javax.swing.JRadioButton jAllInOneAlgorithm;
    private javax.swing.JDialog jAttackPlanSelectionDialog;
    private javax.swing.JTable jAttackPlanTable;
    private javax.swing.JComboBox jAttackPlansBox;
    private javax.swing.JFrame jAttackResultDetailsFrame;
    private javax.swing.JProgressBar jAttacksBar;
    private javax.swing.JTable jAttacksTable;
    private javax.swing.JTextField jAxeField;
    private javax.swing.JTextField jAxeRange;
    private javax.swing.JRadioButton jBlitzkriegAlgorithm;
    private javax.swing.JRadioButton jBruteForceAlgorithm;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JButton jCancelSyncButton;
    private javax.swing.JTextField jCataField;
    private javax.swing.JTextField jCataRange;
    private javax.swing.JButton jCloseResultsButton;
    private javax.swing.JButton jCopyToClipboardAsBBButton;
    private javax.swing.JButton jCopyToClipboardButton;
    private javax.swing.JButton jDoSyncButton;
    private javax.swing.JProgressBar jEnoblementsBar;
    private javax.swing.JProgressBar jFullOffsBar;
    private javax.swing.JTextField jHeavyField;
    private javax.swing.JTextField jHeavyRange;
    private javax.swing.JButton jHideAttackDetailsButton;
    private javax.swing.JButton jHideTargetDetailsButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JLabel jLabel24;
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
    private javax.swing.JTextField jNewPlanName;
    private javax.swing.JButton jNextSelectionButton;
    private javax.swing.JTable jNotAssignedSourcesTable;
    private javax.swing.JFrame jOffStrengthFrame;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JCheckBox jPlayerSourcesOnlyBox;
    private javax.swing.JButton jPrevSelectionButton;
    private javax.swing.JTextField jRamField;
    private javax.swing.JTextField jRamRange;
    private javax.swing.JFrame jResultFrame;
    private javax.swing.JTable jResultsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JButton jSelectButton;
    private javax.swing.JButton jSelectionBeginButton;
    private javax.swing.JTextField jSelectionEnd;
    private javax.swing.JButton jSelectionEndButton;
    private javax.swing.JTextField jSelectionStart;
    private javax.swing.JButton jSetFakeButton;
    private javax.swing.JButton jSetNoFakeButton;
    private javax.swing.JList jSourceContinentList;
    private javax.swing.JRadioButton jSourceGroupRelation;
    private javax.swing.JPanel jSourcePanel;
    private javax.swing.JLabel jSourceVillageLabel1;
    private javax.swing.JLabel jSourceVillageLabel2;
    private javax.swing.JList jSourceVillageList;
    private javax.swing.JTextField jStrengthField;
    private javax.swing.JTextField jStrengthRange;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel jTargetAllyLabel;
    private javax.swing.JList jTargetAllyList;
    private javax.swing.JList jTargetContinentList;
    private javax.swing.JTable jTargetDetailsTable;
    private javax.swing.JPanel jTargetPanel;
    private javax.swing.JFrame jTargetResultDetailsFrame;
    private javax.swing.JList jTargetTribeList;
    private javax.swing.JList jTargetVillageList;
    private javax.swing.JProgressBar jTargetsBar;
    private javax.swing.JSlider jToleranceSlider;
    private javax.swing.JTextField jToleranceValue;
    private javax.swing.JDialog jTransferToAttackManagerDialog;
    private javax.swing.JComboBox jTroopsList;
    private javax.swing.JTable jVictimTable;
    private javax.swing.JList jVillageGroupList;
    // End of variables declaration//GEN-END:variables
    }
