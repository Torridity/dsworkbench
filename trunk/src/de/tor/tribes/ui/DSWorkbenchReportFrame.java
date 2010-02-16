/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchReportFrame.java
 *
 * Created on Jan 16, 2010, 2:30:41 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.FightStats;
import de.tor.tribes.types.ReportSet;
import de.tor.tribes.types.SingleAttackerStat;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.models.ReportManagerTableModel;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.FightReportCellRenderer;
import de.tor.tribes.ui.renderer.ReportWallCataCellRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.ui.renderer.TribeCellRenderer;
import de.tor.tribes.ui.renderer.VillageCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.report.ColorFilter;
import de.tor.tribes.util.report.ConqueredFilter;
import de.tor.tribes.util.report.DateFilter;
import de.tor.tribes.util.report.ReportFilterInterface;
import de.tor.tribes.util.report.ReportFormater;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.report.ReportManagerListener;
import de.tor.tribes.util.report.ReportStatBuilder;
import de.tor.tribes.util.report.TribeFilter;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

/**
 *@TODO (DIFF) Report database added including war stats
 * @TODO (2.0) Report to A*Star
 * @TODO (2.0) Manually move troops from report to troop view
 * @author Torridity
 */
public class DSWorkbenchReportFrame extends AbstractDSWorkbenchFrame implements ReportManagerListener {

    private static Logger logger = Logger.getLogger("ReportView");
    private static DSWorkbenchReportFrame SINGLETON = null;
    private DefaultTableCellRenderer headerRenderer = null;
    private FightStats lastStats = null;

    public static synchronized DSWorkbenchReportFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchReportFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchReportFrame */
    DSWorkbenchReportFrame() {
        initComponents();

        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("report.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }

        jTaskPaneGroup1.setBackground(Constants.DS_BACK);
        //color scrollpanes of selection dialog
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>();
        jReportTable.setRowSorter(sorter);
        sorter.setModel(ReportManagerTableModel.getSingleton());
        jReportTable.setColumnSelectionAllowed(false);
        jReportTable.getTableHeader().setReorderingAllowed(false);
        jReportTable.setModel(ReportManagerTableModel.getSingleton());

        MouseListener l = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON2) {
                    ReportManagerTableModel.getSingleton().getPopup().show(jReportTable, e.getX(), e.getY());
                    ReportManagerTableModel.getSingleton().getPopup().requestFocusInWindow();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };

        jReportTable.addMouseListener(l);
        jScrollPane1.addMouseListener(l);
        jReportTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    int selected = jReportTable.getSelectedRows().length;
                    if (selected == 0) {
                        setTitle("Berichtdatenbank");
                    } else if (selected == 1) {
                        setTitle("Berichtdatenbank (1 Bericht ausgewählt)");
                    } else if (selected > 1) {
                        setTitle("Berichtdatenbank (" + selected + " Berichte ausgewählt)");
                    }
                } catch (Exception ignored) {
                }
            }
        });

        headerRenderer = new SortableTableHeaderRenderer();
        fireReportsChangedEvent(null);

        jList1.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireRebuildStatsEvent();
                }
            }
        });

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        // GlobalOptions.getHelpBroker().enableHelpKey(jSelectionFilterDialog.getRootPane(), "pages.attack_select_filter", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
        jMoveToSetDialog.pack();
        jRenameReportSetDialog.pack();
        jAddReportSetDialog.pack();
        jCreateStatsFrame.pack();

        pack();
    }

    public void setup() {
        ReportManager.getSingleton().addReportManagerListener(this);
        jReportTable.setDefaultRenderer(Date.class, new DateCellRenderer("dd.MM.yy HH:mm"));
        jReportTable.setDefaultRenderer(FightReport.class, new FightReportCellRenderer());
        jReportTable.setDefaultRenderer(Tribe.class, new TribeCellRenderer());
        jReportTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        jReportTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());

        jReportTable.setDefaultRenderer(Boolean.class, new ReportWallCataCellRenderer());
        jReportTable.setDefaultRenderer(Byte.class, new ReportWallCataCellRenderer());
        jReportTable.setRowHeight(20);
        ReportManager.getSingleton().forceUpdate(null);
        buildReportSetList();
        jReportSetBox.setSelectedItem(ReportManager.DEFAULT_SET);
        jTribeList.setModel(new DefaultListModel());
        buildTribesList(null);
    }

    public void buildReportSetList() {
        Enumeration<String> sets = ReportManager.getSingleton().getReportSets();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        while (sets.hasMoreElements()) {
            model.addElement(sets.nextElement());
        }
        jReportSetBox.setModel(model);
        jReportSetBox.setSelectedItem(ReportManagerTableModel.getSingleton().getActiveReportSet());
    }

    private void buildTribesList(String pFilter) {
        Enumeration<Integer> tribeIds = DataHolder.getSingleton().getTribes().keys();
        List<Tribe> tribes = new LinkedList<Tribe>();
        while (tribeIds.hasMoreElements()) {
            Tribe t = DataHolder.getSingleton().getTribes().get(tribeIds.nextElement());
            if (t != null &&
                    (pFilter == null ||
                    (pFilter.length() == 0) ||
                    (t.getName().toLowerCase().indexOf(pFilter.toLowerCase()) >= 0))) {
                tribes.add(t);
            }

        }
        Collections.sort(tribes);
        jTribeSelectionBox.setModel(new DefaultComboBoxModel(tribes.toArray(new Tribe[]{})));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMoveToSetDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        jCurrentSetField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jNewSetBox = new javax.swing.JComboBox();
        jDoMoveButton = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jAddReportSetDialog = new javax.swing.JDialog();
        jLabel4 = new javax.swing.JLabel();
        jNewReportSetField = new javax.swing.JTextField();
        jDoAddNewSetButton = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jRenameReportSetDialog = new javax.swing.JDialog();
        jLabel5 = new javax.swing.JLabel();
        jNewSetNameField = new javax.swing.JTextField();
        jDoRenameButton = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jCreateStatsFrame = new javax.swing.JFrame();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jReportSetsForStatsList = new javax.swing.JList();
        jButton10 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jOverallStatsArea = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jAllyStatsArea = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTribeStatsArea = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jGuessUnknownLosses = new javax.swing.JCheckBox();
        jUseSilentKillsBox = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jShowPercentsBox = new javax.swing.JCheckBox();
        jFilterDialog = new javax.swing.JDialog();
        jPanel8 = new javax.swing.JPanel();
        jTribeSelectionBox = new javax.swing.JComboBox();
        jFilterByTribeBox = new javax.swing.JCheckBox();
        jTribeSelectionFilter = new javax.swing.JTextField();
        jAddTribeButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTribeList = new javax.swing.JList();
        jRemoveTribeButton = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jShowHiddenAttackerReports = new javax.swing.JCheckBox();
        jShowRedReports = new javax.swing.JCheckBox();
        jShowYellowReports = new javax.swing.JCheckBox();
        jShowGreenReports = new javax.swing.JCheckBox();
        jShowBlueReports = new javax.swing.JCheckBox();
        jShowSnobReports = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jStartDate = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jEndDate = new javax.swing.JSpinner();
        jFilterByDate = new javax.swing.JCheckBox();
        jDoFilterButton = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jReportSetBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTaskPane1 = new com.l2fprod.common.swing.JTaskPane();
        jTaskPaneGroup1 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jTaskPaneGroup2 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jButton11 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jReportTable = new javax.swing.JTable();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        jMoveToSetDialog.setTitle("Berichte verschieben");
        jMoveToSetDialog.setAlwaysOnTop(true);

        jLabel2.setText("Aktuelles Set");

        jCurrentSetField.setEditable(false);

        jLabel3.setText("Neues Set");

        jDoMoveButton.setText("Verschieben");
        jDoMoveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoMoveReportsEvent(evt);
            }
        });

        jButton7.setText("Abbrechen");
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoMoveReportsEvent(evt);
            }
        });

        javax.swing.GroupLayout jMoveToSetDialogLayout = new javax.swing.GroupLayout(jMoveToSetDialog.getContentPane());
        jMoveToSetDialog.getContentPane().setLayout(jMoveToSetDialogLayout);
        jMoveToSetDialogLayout.setHorizontalGroup(
            jMoveToSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMoveToSetDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMoveToSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jMoveToSetDialogLayout.createSequentialGroup()
                        .addGroup(jMoveToSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(jMoveToSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jNewSetBox, 0, 300, Short.MAX_VALUE)
                            .addComponent(jCurrentSetField, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jMoveToSetDialogLayout.createSequentialGroup()
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDoMoveButton)))
                .addContainerGap())
        );
        jMoveToSetDialogLayout.setVerticalGroup(
            jMoveToSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMoveToSetDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMoveToSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jCurrentSetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jMoveToSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jNewSetBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jMoveToSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDoMoveButton)
                    .addComponent(jButton7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jAddReportSetDialog.setTitle("Reportset hinzufügen");
        jAddReportSetDialog.setAlwaysOnTop(true);

        jLabel4.setText("Neues Set");

        jDoAddNewSetButton.setText("Hinzufügen");
        jDoAddNewSetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoAddNewReportSetEvent(evt);
            }
        });

        jButton8.setText("Abbrechen");
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoAddNewReportSetEvent(evt);
            }
        });

        javax.swing.GroupLayout jAddReportSetDialogLayout = new javax.swing.GroupLayout(jAddReportSetDialog.getContentPane());
        jAddReportSetDialog.getContentPane().setLayout(jAddReportSetDialogLayout);
        jAddReportSetDialogLayout.setHorizontalGroup(
            jAddReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddReportSetDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jAddReportSetDialogLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(jNewReportSetField, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAddReportSetDialogLayout.createSequentialGroup()
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDoAddNewSetButton)))
                .addContainerGap())
        );
        jAddReportSetDialogLayout.setVerticalGroup(
            jAddReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddReportSetDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jNewReportSetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAddReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDoAddNewSetButton)
                    .addComponent(jButton8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jRenameReportSetDialog.setTitle("Reportset umbenennen");
        jRenameReportSetDialog.setAlwaysOnTop(true);

        jLabel5.setText("Neuer Name");

        jDoRenameButton.setText("Umbenennen");
        jDoRenameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoRenameEvent(evt);
            }
        });

        jButton9.setText("Abbrechen");
        jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoRenameEvent(evt);
            }
        });

        javax.swing.GroupLayout jRenameReportSetDialogLayout = new javax.swing.GroupLayout(jRenameReportSetDialog.getContentPane());
        jRenameReportSetDialog.getContentPane().setLayout(jRenameReportSetDialogLayout);
        jRenameReportSetDialogLayout.setHorizontalGroup(
            jRenameReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRenameReportSetDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jRenameReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jRenameReportSetDialogLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(jNewSetNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jRenameReportSetDialogLayout.createSequentialGroup()
                        .addComponent(jButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDoRenameButton)))
                .addContainerGap())
        );
        jRenameReportSetDialogLayout.setVerticalGroup(
            jRenameReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRenameReportSetDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jRenameReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jNewSetNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jRenameReportSetDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDoRenameButton)
                    .addComponent(jButton9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCreateStatsFrame.setTitle("Kampfstatistiken");

        jPanel2.setBackground(new java.awt.Color(239, 235, 223));

        jLabel6.setText("Verwendete Berichtsets");

        jScrollPane2.setViewportView(jReportSetsForStatsList);

        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/select.png"))); // NOI18N
        jButton10.setText("Auswerten");
        jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoCreateStatsEvent(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Ergebnisse"));
        jPanel3.setOpaque(false);

        jLabel7.setText("Angezeigte Stämme");

        jScrollPane3.setMaximumSize(new java.awt.Dimension(140, 130));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(140, 130));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(140, 130));

        jScrollPane3.setViewportView(jList1);

        jTabbedPane1.setBackground(new java.awt.Color(239, 235, 223));

        jPanel4.setOpaque(false);

        jOverallStatsArea.setColumns(20);
        jOverallStatsArea.setRows(5);
        jScrollPane5.setViewportView(jOverallStatsArea);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Gesamtübersicht", new javax.swing.ImageIcon(getClass().getResource("/res/ui/chart.png")), jPanel4); // NOI18N

        jPanel5.setBackground(new java.awt.Color(239, 235, 223));

        jAllyStatsArea.setColumns(20);
        jAllyStatsArea.setRows(5);
        jScrollPane6.setViewportView(jAllyStatsArea);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Stämme", new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jPanel5); // NOI18N

        jPanel6.setBackground(new java.awt.Color(239, 235, 223));

        jTribeStatsArea.setColumns(20);
        jTribeStatsArea.setRows(5);
        jScrollPane7.setViewportView(jTribeStatsArea);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 565, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 139, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Spieler", new javax.swing.ImageIcon(getClass().getResource("/res/face.png")), jPanel6); // NOI18N

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Optionen"));
        jPanel7.setOpaque(false);

        jGuessUnknownLosses.setSelected(true);
        jGuessUnknownLosses.setText("Gegnerische Verluste schätzen, falls unbekannt");
        jGuessUnknownLosses.setOpaque(false);
        jGuessUnknownLosses.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireStatOptionsChangedEvent(evt);
            }
        });

        jUseSilentKillsBox.setSelected(true);
        jUseSilentKillsBox.setText("Auswärtige Einheiten bei Adelung als Verlust werten");
        jUseSilentKillsBox.setOpaque(false);
        jUseSilentKillsBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireStatOptionsChangedEvent(evt);
            }
        });

        jCheckBox3.setSelected(true);
        jCheckBox3.setText("Verluste pro Angreifer/Verteidiger anzeigen");
        jCheckBox3.setOpaque(false);
        jCheckBox3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireStatOptionsChangedEvent(evt);
            }
        });

        jShowPercentsBox.setText("Prozentuale Anteile anzeigen");
        jShowPercentsBox.setOpaque(false);
        jShowPercentsBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireStatOptionsChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jGuessUnknownLosses)
                    .addComponent(jUseSilentKillsBox)
                    .addComponent(jCheckBox3)
                    .addComponent(jShowPercentsBox)))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jGuessUnknownLosses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jUseSilentKillsBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowPercentsBox)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE))
                    .addComponent(jButton10, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout jCreateStatsFrameLayout = new javax.swing.GroupLayout(jCreateStatsFrame.getContentPane());
        jCreateStatsFrame.getContentPane().setLayout(jCreateStatsFrameLayout);
        jCreateStatsFrameLayout.setHorizontalGroup(
            jCreateStatsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCreateStatsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jCreateStatsFrameLayout.setVerticalGroup(
            jCreateStatsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCreateStatsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jFilterDialog.setTitle("Berichte filtern");
        jFilterDialog.setAlwaysOnTop(true);

        jPanel8.setBackground(new java.awt.Color(239, 235, 223));
        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtern nach Angreifer/Verteidiger"));

        jTribeSelectionBox.setEnabled(false);
        jTribeSelectionBox.setMinimumSize(new java.awt.Dimension(28, 20));

        jFilterByTribeBox.setSelected(true);
        jFilterByTribeBox.setText("Alle Berichte anzeigen");
        jFilterByTribeBox.setOpaque(false);
        jFilterByTribeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireFilterByTribeChangedEvent(evt);
            }
        });

        jTribeSelectionFilter.setEnabled(false);
        jTribeSelectionFilter.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireFilterChangedEvent(evt);
            }
        });

        jAddTribeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jAddTribeButton.setEnabled(false);
        jAddTribeButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jAddTribeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jAddTribeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTribeFilterChangedEvent(evt);
            }
        });

        jScrollPane4.setViewportView(jTribeList);

        jRemoveTribeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jRemoveTribeButton.setEnabled(false);
        jRemoveTribeButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jRemoveTribeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jRemoveTribeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTribeFilterChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jFilterByTribeBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTribeSelectionBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTribeSelectionFilter, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(4, 4, 4)
                .addComponent(jAddTribeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRemoveTribeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jFilterByTribeBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAddTribeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(jRemoveTribeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jTribeSelectionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTribeSelectionFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel10.setBackground(new java.awt.Color(239, 235, 223));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Sonstige Filter"));

        jShowHiddenAttackerReports.setSelected(true);
        jShowHiddenAttackerReports.setText("Berichte mit verborgenen Truppeninformationen anzeigen");
        jShowHiddenAttackerReports.setOpaque(false);

        jShowRedReports.setSelected(true);
        jShowRedReports.setText("Rote Berichte anzeigen");
        jShowRedReports.setOpaque(false);

        jShowYellowReports.setSelected(true);
        jShowYellowReports.setText("Gelbe Berichte anzeigen");
        jShowYellowReports.setOpaque(false);

        jShowGreenReports.setSelected(true);
        jShowGreenReports.setText("Grüne Berichte anzeigen");
        jShowGreenReports.setOpaque(false);

        jShowBlueReports.setSelected(true);
        jShowBlueReports.setText("Blaue Berichte anzeigen");
        jShowBlueReports.setOpaque(false);

        jShowSnobReports.setSelected(true);
        jShowSnobReports.setText("Berichte mit Zustimmungssenkung anzeigen");
        jShowSnobReports.setOpaque(false);

        jLabel8.setText("Berichte vom");
        jLabel8.setEnabled(false);

        jStartDate.setModel(new javax.swing.SpinnerDateModel());
        jStartDate.setEnabled(false);

        jLabel9.setText("bis zum");
        jLabel9.setEnabled(false);

        jEndDate.setModel(new javax.swing.SpinnerDateModel());
        jEndDate.setEnabled(false);

        jFilterByDate.setText("Nach Datum filtern");
        jFilterByDate.setOpaque(false);
        jFilterByDate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireFilterByDateChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFilterByDate)
                    .addComponent(jShowHiddenAttackerReports)
                    .addComponent(jShowRedReports)
                    .addComponent(jShowYellowReports)
                    .addComponent(jShowGreenReports)
                    .addComponent(jShowBlueReports)
                    .addComponent(jShowSnobReports)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(142, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jShowHiddenAttackerReports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowRedReports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowYellowReports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowGreenReports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowBlueReports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jShowSnobReports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jFilterByDate)
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jDoFilterButton.setText("Filtern");
        jDoFilterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyFilterEvent(evt);
            }
        });

        jButton17.setText("Abbrechen");
        jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyFilterEvent(evt);
            }
        });

        javax.swing.GroupLayout jFilterDialogLayout = new javax.swing.GroupLayout(jFilterDialog.getContentPane());
        jFilterDialog.getContentPane().setLayout(jFilterDialogLayout);
        jFilterDialogLayout.setHorizontalGroup(
            jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFilterDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFilterDialogLayout.createSequentialGroup()
                        .addComponent(jButton17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDoFilterButton)))
                .addContainerGap())
        );
        jFilterDialogLayout.setVerticalGroup(
            jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFilterDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDoFilterButton)
                    .addComponent(jButton17))
                .addContainerGap())
        );

        setTitle("Berichtdatenbank");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jReportSetBox.setMaximumSize(new java.awt.Dimension(200, 20));
        jReportSetBox.setMinimumSize(new java.awt.Dimension(200, 20));
        jReportSetBox.setPreferredSize(new java.awt.Dimension(200, 20));
        jReportSetBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireReportSetChangedEvent(evt);
            }
        });

        jLabel1.setText("Berichtset");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(27, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(27, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(27, 25));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddReportSetEvent(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(27, 25));
        jButton2.setMinimumSize(new java.awt.Dimension(27, 25));
        jButton2.setPreferredSize(new java.awt.Dimension(27, 25));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRenameReportSetEvent(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton3.setMaximumSize(new java.awt.Dimension(27, 25));
        jButton3.setMinimumSize(new java.awt.Dimension(27, 25));
        jButton3.setPreferredSize(new java.awt.Dimension(27, 25));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveReportSetEvent(evt);
            }
        });

        com.l2fprod.common.swing.PercentLayout percentLayout1 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout1.setGap(14);
        percentLayout1.setOrientation(1);
        jTaskPane1.setLayout(percentLayout1);

        jTaskPaneGroup1.setTitle("Verwaltung");
        com.l2fprod.common.swing.PercentLayout percentLayout2 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout2.setGap(2);
        percentLayout2.setOrientation(1);
        jTaskPaneGroup1.getContentPane().setLayout(percentLayout2);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveReportsEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton4);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/replace2.png"))); // NOI18N
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveReportsEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton5);

        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/filter_off.png"))); // NOI18N
        jButton12.setToolTipText("Angezeigte Berichte filtern");
        jButton12.setMaximumSize(new java.awt.Dimension(59, 33));
        jButton12.setMinimumSize(new java.awt.Dimension(59, 33));
        jButton12.setPreferredSize(new java.awt.Dimension(59, 33));
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowFilterDialogEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton12);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/medal.png"))); // NOI18N
        jButton6.setToolTipText("Statistiken erzeugen");
        jButton6.setMaximumSize(new java.awt.Dimension(59, 33));
        jButton6.setMinimumSize(new java.awt.Dimension(59, 33));
        jButton6.setPreferredSize(new java.awt.Dimension(59, 33));
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCreateStatsEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton6);

        jTaskPane1.add(jTaskPaneGroup1);

        jTaskPaneGroup2.setTitle("Übertragen");
        com.l2fprod.common.swing.PercentLayout percentLayout3 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout3.setGap(2);
        percentLayout3.setOrientation(1);
        jTaskPaneGroup2.getContentPane().setLayout(percentLayout3);

        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        jButton11.setToolTipText("Markierte Berichte als BB-Code in die Zwischenablage kopieren");
        jButton11.setMaximumSize(new java.awt.Dimension(59, 33));
        jButton11.setMinimumSize(new java.awt.Dimension(59, 33));
        jButton11.setPreferredSize(new java.awt.Dimension(59, 33));
        jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyReportsAsBBCodesToClipboardEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jButton11);

        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/report_toAStar.png"))); // NOI18N
        jButton13.setToolTipText("Truppen aus markiertem Bericht nach A*Star übertragen");
        jButton13.setMaximumSize(new java.awt.Dimension(59, 33));
        jButton13.setMinimumSize(new java.awt.Dimension(59, 33));
        jButton13.setPreferredSize(new java.awt.Dimension(59, 33));
        jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferToAStarEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jButton13);

        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/moveToTable.png"))); // NOI18N
        jButton14.setToolTipText("Überlebende, verteidigende Truppen aus markierten Berichten in Truppenübersicht einfügen");
        jButton14.setMaximumSize(new java.awt.Dimension(59, 33));
        jButton14.setMinimumSize(new java.awt.Dimension(59, 33));
        jButton14.setPreferredSize(new java.awt.Dimension(59, 33));
        jButton14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveTroopsToTroopManagerEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jButton14);

        jTaskPane1.add(jTaskPaneGroup2);

        jReportTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jReportTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jReportSetBox, 0, 294, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTaskPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jReportSetBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jTaskPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE))
                .addContainerGap())
        );

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlwaysOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAlwaysOnTopBox, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAlwaysOnTopBox)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireReportSetChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireReportSetChangedEvent
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            jReportTable.invalidate();

            try {
                jReportTable.getCellEditor().cancelCellEditing();
            } catch (Exception e) {
            }
            jReportTable.getSelectionModel().clearSelection();
            jReportTable.setRowSorter(new TableRowSorter(ReportManagerTableModel.getSingleton()));
            ReportManagerTableModel.getSingleton().setActiveReportSet((String) jReportSetBox.getSelectedItem());
            ReportManager.getSingleton().updateFilters();
            jReportTable.repaint();//.updateUI();
            jReportTable.revalidate();
        }
    }//GEN-LAST:event_fireReportSetChangedEvent

    private void fireAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopEvent

    private void fireRemoveReportsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveReportsEvent
        int[] rows = jReportTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }

        String message = ((rows.length == 1) ? "Bericht " : (rows.length + " Berichte ")) + "wirklich löschen?";
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Berichte löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
            return;
        }

        jReportTable.editingCanceled(new ChangeEvent(this));

        for (int r = rows.length - 1; r >= 0; r--) {
            jReportTable.invalidate();
            int row = jReportTable.convertRowIndexToModel(rows[r]);
            ReportManagerTableModel.getSingleton().removeRow(row);
            jReportTable.revalidate();
        }
        jReportTable.repaint();

    }//GEN-LAST:event_fireRemoveReportsEvent

    private void fireMoveReportsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveReportsEvent
        String current = (String) jReportSetBox.getSelectedItem();
        if (current == null) {
            return;
        }
        jCurrentSetField.setText(current);
        Enumeration<String> plans = ReportManager.getSingleton().getReportSets();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        while (plans.hasMoreElements()) {
            String plan = plans.nextElement();
            if (!plan.equals(current)) {
                model.addElement(plan);
            }
        }
        jNewSetBox.setModel(model);
        jNewSetBox.setSelectedItem(current);
        jMoveToSetDialog.setLocationRelativeTo(this);
        jMoveToSetDialog.setVisible(true);
    }//GEN-LAST:event_fireMoveReportsEvent

    private void fireRemoveReportSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveReportSetEvent
        String selection = (String) jReportSetBox.getSelectedItem();
        if (selection == null) {
            return;
        }

        if (selection.equals(ReportManager.DEFAULT_SET)) {
            JOptionPaneHelper.showInformationBox(this, "Das Standardset kann nicht gelöscht werden.", "Information");
            return;
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du das Berichtsset '" + selection + "' und alle enthaltenen Berichte\n" +
                "wirklich löschen?", "Berichtsset löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            ReportManagerTableModel.getSingleton().setActiveReportSet(ReportManager.DEFAULT_SET);
            ReportManager.getSingleton().removeReportSet(selection);
            buildReportSetList();

            jReportSetBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_fireRemoveReportSetEvent

    private void fireRenameReportSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRenameReportSetEvent
        String selection = (String) jReportSetBox.getSelectedItem();
        if (selection == null) {
            return;
        }

        if (selection.equals(ReportManager.DEFAULT_SET)) {
            JOptionPaneHelper.showInformationBox(this, "Das Standardset kann nicht umbenannt werden.", "Information");
            return;

        }

        jNewSetNameField.setText(selection);
        jRenameReportSetDialog.setLocationRelativeTo(this);
        jRenameReportSetDialog.setVisible(true);

    }//GEN-LAST:event_fireRenameReportSetEvent

    private void fireDoMoveReportsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDoMoveReportsEvent
        if (evt.getSource() == jDoMoveButton) {
            try {
                String oldSet = jCurrentSetField.getText();
                String newSet = (String) jNewSetBox.getSelectedItem();

                if (newSet == null) {
                    JOptionPaneHelper.showInformationBox(jMoveToSetDialog, "Kein neues Set ausgewählt", "Information");
                    return;
                }
                int[] rows = jReportTable.getSelectedRows();
                if ((rows != null) && (rows.length > 0)) {
                    ReportSet sourceSet = ReportManager.getSingleton().getReportSet(oldSet);
                    List<FightReport> tmpReports = new LinkedList<FightReport>();
                    jReportTable.invalidate();
                    int[] correctIds = new int[rows.length];
                    int cnt = 0;
                    for (int i : rows) {
                        int row = jReportTable.convertRowIndexToModel(i);
                        correctIds[cnt] = row;
                        tmpReports.add(sourceSet.getReports()[row]);
                        cnt++;
                    }

                    ReportManager.getSingleton().removeReports(oldSet, correctIds);
                    ReportManager.getSingleton().createReportSet(newSet);
                    for (FightReport r : tmpReports) {
                        ReportManager.getSingleton().getReportSet(newSet).addReport(r);
                    }
                    jReportTable.revalidate();
                }
            } catch (Exception e) {
                logger.error("Failed to move reports", e);
            }
        }
        jMoveToSetDialog.setVisible(false);
    }//GEN-LAST:event_fireDoMoveReportsEvent

    private void fireAddReportSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddReportSetEvent
        jNewReportSetField.setText("");
        jAddReportSetDialog.setLocationRelativeTo(this);
        jAddReportSetDialog.setVisible(true);
    }//GEN-LAST:event_fireAddReportSetEvent

    private void fireDoAddNewReportSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDoAddNewReportSetEvent
        if (evt.getSource() == jDoAddNewSetButton) {
            String name = jNewReportSetField.getText();
            if (ReportManager.getSingleton().getReportSet(name) != null) {
                JOptionPaneHelper.showWarningBox(jAddReportSetDialog, "Ein Set mit dem angegebenen Namen existiert bereits.\n" +
                        "Bitte wähle einen anderen Namen oder lösche zuerst das bestehende Set.", "Warnung");
                return;
            }

            ReportManager.getSingleton().createReportSet(name);
            buildReportSetList();
        }
        jAddReportSetDialog.setVisible(false);
    }//GEN-LAST:event_fireDoAddNewReportSetEvent

    private void fireDoRenameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDoRenameEvent
        if (evt.getSource() == jDoRenameButton) {
            String selection = (String) jReportSetBox.getSelectedItem();
            String newName = jNewSetNameField.getText();
            if (ReportManager.getSingleton().getReportSet(newName) != null) {
                JOptionPaneHelper.showWarningBox(jRenameReportSetDialog, "Ein Set mit dem Namen '" + newName + "' existiert bereits.\n" +
                        "Bitte wähle einen anderen Namen oder lösche zuerst das bestehende Set.", "Warnung");
                return;

            }
            ReportManager.getSingleton().renameReportSet(selection, newName);
            buildReportSetList();
        }
        jRenameReportSetDialog.setVisible(false);
    }//GEN-LAST:event_fireDoRenameEvent

    private void fireCreateStatsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateStatsEvent
        Enumeration<String> plans = ReportManager.getSingleton().getReportSets();
        DefaultListModel model = new DefaultListModel();
        while (plans.hasMoreElements()) {
            model.addElement(plans.nextElement());
        }

        jReportSetsForStatsList.setModel(model);
        jCreateStatsFrame.setLocationRelativeTo(this);
        jCreateStatsFrame.setVisible(true);

    }//GEN-LAST:event_fireCreateStatsEvent

    private void fireDoCreateStatsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDoCreateStatsEvent

        Object[] selection = jReportSetsForStatsList.getSelectedValues();
        if (selection == null || selection.length == 0) {
            JOptionPaneHelper.showInformationBox(jCreateStatsFrame, "Kein Berichtset ausgewählt", "Information");
            return;
        }

        List<String> reportSets = new LinkedList<String>();
        for (Object o : selection) {
            reportSets.add((String) o);
        }

        DefaultListModel model = new DefaultListModel();
        lastStats = ReportStatBuilder.buildStats(reportSets);
        for (Ally a : lastStats.getAttackingAllies()) {
            model.addElement(a);
        }

        jList1.setModel(model);
        jList1.setSelectionInterval(0, model.size() - 1);

    }//GEN-LAST:event_fireDoCreateStatsEvent

    private void fireStatOptionsChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireStatOptionsChangedEvent
        fireRebuildStatsEvent();
    }//GEN-LAST:event_fireStatOptionsChangedEvent

    private void fireCopyReportsAsBBCodesToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyReportsAsBBCodesToClipboardEvent

        int[] selection = jReportTable.getSelectedRows();
        if (selection == null || selection.length == 0) {
            return;
        }
        String activeSet = ReportManagerTableModel.getSingleton().getActiveReportSet();
        ReportSet set = ReportManager.getSingleton().getReportSet(activeSet);
        StringBuffer b = new StringBuffer();
        for (int i : selection) {
            int row = jReportTable.convertRowIndexToModel(i);
            b.append(ReportFormater.format(set.getReports()[row]) + "\n");
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            String result = "Daten in Zwischenablage kopiert.";
            JOptionPaneHelper.showInformationBox(this, result, "Information");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            JOptionPaneHelper.showErrorBox(this, result, "Fehler");
        }
    }//GEN-LAST:event_fireCopyReportsAsBBCodesToClipboardEvent

    private void fireFilterByDateChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireFilterByDateChangedEvent
        jLabel8.setEnabled(jFilterByDate.isSelected());
        jLabel9.setEnabled(jFilterByDate.isSelected());
        jStartDate.setEnabled(jFilterByDate.isSelected());
        jEndDate.setEnabled(jFilterByDate.isSelected());
    }//GEN-LAST:event_fireFilterByDateChangedEvent

    private void fireFilterByTribeChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireFilterByTribeChangedEvent
        jAddTribeButton.setEnabled(!jFilterByTribeBox.isSelected());
        jRemoveTribeButton.setEnabled(!jFilterByTribeBox.isSelected());
        jTribeList.setEnabled(!jFilterByTribeBox.isSelected());
        jTribeSelectionFilter.setEnabled(!jFilterByTribeBox.isSelected());
        jTribeSelectionBox.setEnabled(!jFilterByTribeBox.isSelected());
    }//GEN-LAST:event_fireFilterByTribeChangedEvent

    private void fireFilterChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireFilterChangedEvent
        buildTribesList(jTribeSelectionFilter.getText());
    }//GEN-LAST:event_fireFilterChangedEvent

    private void fireTribeFilterChangedEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTribeFilterChangedEvent
        if (evt.getSource() == jAddTribeButton) {
            //add tribe
            jTribeSelectionBox.firePopupMenuCanceled();
            Tribe t = (Tribe) jTribeSelectionBox.getSelectedItem();
            if (t != null) {
                if (((DefaultListModel) jTribeList.getModel()).indexOf(t) < 0) {
                    ((DefaultListModel) jTribeList.getModel()).addElement(t);
                }
            }
        } else {
            //remove tribe
            Tribe filter = (Tribe) jTribeList.getSelectedValue();
            if (filter != null) {
                if (JOptionPaneHelper.showQuestionConfirmBox(jFilterDialog, "Gewählten Spieler entfernen?", "Spieler entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    ((DefaultListModel) jTribeList.getModel()).removeElement(filter);
                }
            }
        }
    }//GEN-LAST:event_fireTribeFilterChangedEvent

    private void fireShowFilterDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowFilterDialogEvent
        jFilterDialog.pack();
        jFilterDialog.setLocationRelativeTo(this);
        jFilterDialog.setVisible(true);
    }//GEN-LAST:event_fireShowFilterDialogEvent

    private void fireApplyFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireApplyFilterEvent
        if (evt.getSource() == jDoFilterButton) {
            List<ReportFilterInterface> filters = new LinkedList<ReportFilterInterface>();
            if (!jFilterByTribeBox.isSelected()) {
                DefaultListModel model = (DefaultListModel) jTribeList.getModel();
                if (model.isEmpty()) {
                    JOptionPaneHelper.showInformationBox(jFilterDialog, "Keine Spieler ausgewählt", "Information");
                    return;
                }
                List<Tribe> tribes = new LinkedList<Tribe>();
                for (int i = 0; i < model.size(); i++) {
                    tribes.add((Tribe) model.getElementAt(i));
                }
                TribeFilter filter = new TribeFilter();
                filter.setup(tribes);
                filters.add(filter);

            }
            int colorFilter = 0;
            if (jShowHiddenAttackerReports.isSelected()) {
                colorFilter += ColorFilter.GREY;
            }
            if (jShowRedReports.isSelected()) {
                colorFilter += ColorFilter.RED;
            }
            if (jShowYellowReports.isSelected()) {
                colorFilter += ColorFilter.YELLOW;
            }
            if (jShowGreenReports.isSelected()) {
                colorFilter += ColorFilter.GREEN;
            }
            if (jShowBlueReports.isSelected()) {
                colorFilter += ColorFilter.BLUE;
            }
            ColorFilter colFilter = new ColorFilter();
            colFilter.setup(colorFilter);
            filters.add(colFilter);
            if (!jShowSnobReports.isSelected()) {
                filters.add(new ConqueredFilter());
            }
            if (jFilterByDate.isSelected()) {
                DateFilter dateFilter = new DateFilter();
                List<Long> dates = new LinkedList<Long>();
                Date start = (Date) jStartDate.getValue();
                Date end = (Date) jEndDate.getValue();
                if (start.getTime() > end.getTime()) {
                    jStartDate.setValue(end);
                    jEndDate.setValue(start);
                    start = (Date) jStartDate.getValue();
                    end = (Date) jEndDate.getValue();
                }
                dates.add(start.getTime());
                dates.add(end.getTime());
                dateFilter.setup(dates);
                filters.add(dateFilter);
            }
            ReportManager.getSingleton().setFilters(filters);
        }
        jFilterDialog.setVisible(false);
        ReportManagerTableModel.getSingleton().fireTableDataChanged();
    }//GEN-LAST:event_fireApplyFilterEvent

    private void fireTransferToAStarEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferToAStarEvent
        int row = jReportTable.getSelectedRow();
        if (row == -1) {
            return;
        }
        row = jReportTable.convertRowIndexToModel(row);
        FightReport report = ReportManager.getSingleton().getFilteredElement(row);
        Hashtable<String, Double> values = new Hashtable<String, Double>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (!report.areAttackersHidden()) {
                values.put("att_" + unit.getPlainName(), (double) report.getAttackers().get(unit));
            }
            if (!report.wasLostEverything()) {
                values.put("def_" + unit.getPlainName(), (double) report.getDefenders().get(unit));
            }
        }
        if (report.wasBuildingDamaged()) {
            values.put("building", (double) report.getBuildingBefore());
        }
        if (report.wasWallDamaged()) {
            values.put("wall", (double) report.getWallBefore());
        }
        values.put("luck", report.getLuck());
        values.put("moral", report.getMoral());
        if (!DSWorkbenchSimulatorFrame.getSingleton().isVisible()) {
            DSWorkbenchSimulatorFrame.getSingleton().setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
            DSWorkbenchSimulatorFrame.getSingleton().showIntegratedVersion(GlobalOptions.getSelectedServer());
        }

        DSWorkbenchSimulatorFrame.getSingleton().insertValuesExternally(values);
    }//GEN-LAST:event_fireTransferToAStarEvent

    private void fireMoveTroopsToTroopManagerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveTroopsToTroopManagerEvent
        int[] rows = jReportTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }
        for (int i = 0; i < rows.length; i++) {
            int row = jReportTable.convertRowIndexToModel(rows[i]);
            FightReport report = ReportManager.getSingleton().getFilteredElement(row);
            if (!report.wasLostEverything()) {
                Hashtable<UnitHolder, Integer> defenders = report.getDefenders();
                Hashtable<UnitHolder, Integer> diedDefenders = report.getDiedDefenders();
                Village target = report.getTargetVillage();
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(target);
                if (holder == null) {
                    TroopsManager.getSingleton().addTroopsForVillage(target, new LinkedList<Integer>());
                    holder = TroopsManager.getSingleton().getTroopsForVillage(target);
                }
                Hashtable<UnitHolder, Integer> inVillage = holder.getTroopsInVillage();
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    int amount = defenders.get(unit) - diedDefenders.get(unit);
                    inVillage.put(unit, amount);
                }
            }
        }
        JOptionPaneHelper.showInformationBox(this, "Truppen übertragen", "Information");
        TroopsManager.getSingleton().forceUpdate();
        MapPanel.getSingleton().getMapRenderer().initiateRedraw(0);

    }//GEN-LAST:event_fireMoveTroopsToTroopManagerEvent

    private void fireRebuildStatsEvent() {
        Object[] selection = jList1.getSelectedValues();
        if (selection == null || selection.length == 0) {
            jOverallStatsArea.setText("<Kein Stamm ausgewählt>");
            jAllyStatsArea.setText("<Kein Stamm ausgewählt>");
            jTribeStatsArea.setText("<Kein Stamm ausgewählt>");
            return;
        }
        int overallDefAllies = lastStats.getDefendingAllies().length;
        int overallDefTribes = lastStats.getDefendingTribes().length;

        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumFractionDigits(0);
        f.setMaximumFractionDigits(0);

        StringBuffer allyBuffer = new StringBuffer();
        StringBuffer tribeBuffer = new StringBuffer();
        Hashtable<Ally, AllyStatResult> allyResults = new Hashtable<Ally, AllyStatResult>();
        OverallStatResult overallResult = new OverallStatResult();
        for (Object o : selection) {
            Ally a = (Ally) o;
            AllyStatResult result = new AllyStatResult();
            allyResults.put(a, result);
            for (Tribe t : lastStats.getAttackingTribes(a)) {
                TribeStatResult tribeResult = new TribeStatResult();
                SingleAttackerStat stats = lastStats.getStatsForTribe(t);
                tribeResult.setTribeStats(stats, jGuessUnknownLosses.isSelected());
                result.addTribeStatResult(tribeResult);
            }
            overallResult.addAllyStatsResult(result);
        }

        Enumeration<Ally> keys = allyResults.keys();
        while (keys.hasMoreElements()) {
            Ally a = keys.nextElement();
            AllyStatResult res = allyResults.get(a);
            for (TribeStatResult tribeResult : res.getTribeStats()) {
                double attacksOfTribe = 100.0 * (double) tribeResult.getAttacks() / (double) res.getAttacks();
                f.setMinimumFractionDigits(2);
                f.setMaximumFractionDigits(2);
                tribeBuffer.append("[quote]" + tribeResult.getTribe().toBBCode() + "\n");
                tribeBuffer.append("[b][color=#555555][b]Angriffe (Gesamt/Off/AG");
                if (jShowPercentsBox.isSelected()) {
                    tribeBuffer.append("/Anteil am Stamm");
                }
                tribeBuffer.append("):[/color] " + tribeResult.getAttacks() + "/" + tribeResult.getOffs() + "/" + tribeResult.getSnobs());
                if (jShowPercentsBox.isSelected()) {
                    tribeBuffer.append("/" + f.format(attacksOfTribe) + "%");
                }
                tribeBuffer.append("[/b]\n");


                tribeBuffer.append("[b][color=#555555]Adelungen:[/color] " + tribeResult.getEnoblements() + "[/b]\n");
                tribeBuffer.append("\n");

                double killsOfTribe = 100.0 * (double) tribeResult.getKills() / (double) res.getKills();
                f.setMinimumFractionDigits(0);
                f.setMaximumFractionDigits(0);
                tribeBuffer.append("[b][color=#888888]Besiegte Einheiten (Anzahl/Bauernhofplätze");
                if (jShowPercentsBox.isSelected()) {
                    tribeBuffer.append("/Anteil am Stamm");
                }
                tribeBuffer.append("):[/color] ");

                tribeBuffer.append(f.format(tribeResult.getKills()) + "/" + f.format(tribeResult.getKillsAsFarm()));
                if (jShowPercentsBox.isSelected()) {
                    f.setMinimumFractionDigits(2);
                    f.setMaximumFractionDigits(2);
                    tribeBuffer.append("/" + (f.format(killsOfTribe)) + "%[/b]");
                }
                tribeBuffer.append("\n");

                double lossesOfTribe = 100.0 * (double) tribeResult.getLosses() / (double) res.getLosses();
                f.setMinimumFractionDigits(0);
                f.setMaximumFractionDigits(0);
                tribeBuffer.append("[b][color=#888888]Verlorene Einheiten (Anzahl/Bauernhofplätze");

                if (jShowPercentsBox.isSelected()) {
                    tribeBuffer.append("/Anteil am Stamm");
                }

                tribeBuffer.append("):[/color] ");
                tribeBuffer.append(f.format(tribeResult.getLosses()) + "/" + f.format(tribeResult.getLossesAsFarm()));
                if (jShowPercentsBox.isSelected()) {
                    f.setMinimumFractionDigits(2);
                    f.setMaximumFractionDigits(2);
                    tribeBuffer.append("/" + (f.format(lossesOfTribe)) + "%[/b]");
                }
                tribeBuffer.append("\n\n");
                tribeBuffer.append("[b][color=#555555]Zerstörte Wallstufen:[/color] " + tribeResult.getWallDestruction() + "[/b]\n");
                tribeBuffer.append("[b][color=#555555]Zerstörte Gebäudestufen:[/color] " + tribeResult.getBuildingDestruction() + "[/b]\n");
                tribeBuffer.append("[/quote]\n");
            }

            allyBuffer.append("[quote]" + a.toBBCode() + "\n");
            double attackers = 100.0 * (double) res.getAttackers() / (double) overallResult.getAttackers();
            f.setMinimumFractionDigits(2);
            f.setMaximumFractionDigits(2);
            allyBuffer.append("[b][color=#555555]Angreifer (Anzahl");
            if (jShowPercentsBox.isSelected()) {
                allyBuffer.append("/Gesamtanteil");
            }
            allyBuffer.append("):[/color] " + res.getAttackers());
            if (jShowPercentsBox.isSelected()) {
                allyBuffer.append("/" + f.format(attackers) + "%");
            }
            allyBuffer.append("[/b]\n");
            double attacksOfAlly = 100.0 * (double) res.getAttacks() / (double) overallResult.getAttacks();
            f.setMinimumFractionDigits(2);
            f.setMaximumFractionDigits(2);
            allyBuffer.append("[b][color=#555555]Angriffe (Gesamt/Off/AG");

            if (jShowPercentsBox.isSelected()) {
                allyBuffer.append("/Anteil");
            }
            allyBuffer.append("):[/color] " + res.getAttacks() + "/" + res.getOffs() + "/" + res.getSnobs());
            if (jShowPercentsBox.isSelected()) {
                allyBuffer.append("/" + f.format(attacksOfAlly) + "%");
            }
            allyBuffer.append("[/b]\n");
            allyBuffer.append("[b][color=#555555]Adelungen:[/color] " + res.getEnoblements() + "[/b]\n");
            allyBuffer.append("\n");
            double killsOfAlly = 100.0 * (double) res.getKills() / (double) overallResult.getKills();
            f.setMinimumFractionDigits(0);
            f.setMaximumFractionDigits(0);
            allyBuffer.append("[b][color=#888888]Besiegte Einheiten (Anzahl/Bauernhofplätze");
            if (jShowPercentsBox.isSelected()) {
                allyBuffer.append("/Gesamtanteil");
            }
            allyBuffer.append("):[/color] ");
            allyBuffer.append(f.format(res.getKills()) + "/" + f.format(res.getKillsAsFarm()));
            if (jShowPercentsBox.isSelected()) {
                f.setMinimumFractionDigits(2);
                f.setMaximumFractionDigits(2);
                allyBuffer.append("/" + (f.format(killsOfAlly)) + "%");
            }
            allyBuffer.append("[/b]\n");
            double lossesOfAlly = 100.0 * (double) res.getLosses() / (double) overallResult.getLosses();
            f.setMinimumFractionDigits(0);
            f.setMaximumFractionDigits(0);
            allyBuffer.append("[b][color=#888888]Verlorene Einheiten (Anzahl/Bauernhofplätze");
            if (jShowPercentsBox.isSelected()) {
                allyBuffer.append("/Gesamtanteil");
            }

            allyBuffer.append("):[/color] ");
            allyBuffer.append(f.format(res.getLosses()) + "/" + f.format(res.getLossesAsFarm()));
            if (jShowPercentsBox.isSelected()) {
                f.setMinimumFractionDigits(2);
                f.setMaximumFractionDigits(2);
                allyBuffer.append("/" + (f.format(lossesOfAlly)) + "%");
            }
            allyBuffer.append("[/b]\n");
            allyBuffer.append("\n");
            allyBuffer.append("[b][color=#555555]Zerstörte Wallstufen:[/color] " + res.getWallDestruction() + "[/b]\n");
            allyBuffer.append("[b][color=#555555]Zerstörte Gebäudestufen:[/color] " + res.getBuildingDestruction() + "[/b]\n");
            allyBuffer.append("[/quote]\n");
        }


        StringBuffer overallBuffer = new StringBuffer();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
        overallBuffer.append("[quote]");
        overallBuffer.append("[b][color=#555555]Start:[/color] " + df.format(lastStats.getStartDate()) + "[/b]\n");
        overallBuffer.append("[b][color=#555555]Ende:[/color] " + df.format(lastStats.getEndDate()) + "[/b]\n");
        overallBuffer.append("[b][color=#555555]Ausgewertete Berichte:[/color] " + lastStats.getReportCount() + "[/b]\n\n");
        overallBuffer.append("[b][color=#888888]Ausgewertete Angreifer (Stämme):[/color] " + overallResult.getAttackers() + " (" + selection.length + ")[/b]\n");
        overallBuffer.append("[b][color=#888888]Verteidiger (Stämme):[/color] " + overallDefTribes + " (" + overallDefAllies + ")[/b]\n\n");
        overallBuffer.append("[b][color=#555555]Besiegte Einheiten (Bauernhofplätze):[/color] " + f.format(overallResult.getKills()) + " (" + f.format(overallResult.getKillsAsFarm()) + ")[/b]\n\n");
        overallBuffer.append("[b][color=#555555]Verlorene Einheiten (Bauernhofplätze):[/color] " + f.format(overallResult.getLosses()) + " (" + f.format(overallResult.getLossesAsFarm()) + ")[/b]\n\n");
        overallBuffer.append("[b][color=#888888]Verluste pro Angreifer:[/color] " + f.format((overallResult.getLosses() / overallResult.getAttackers())) + "[/b]\n");
        overallBuffer.append("[b][color=#888888]Verluste pro Verteidiger:[/color] " + f.format((overallResult.getKills() / overallDefTribes)) + "[/b]\n\n");
        overallBuffer.append("[b][color=#555555]Zerstörte Wallstufen:[/color] " + f.format(overallResult.getWallDestruction()) + "[/b]\n");
        overallBuffer.append("[b][color=#555555]Zerstörte Gebäudestufen:[/color] " + f.format(overallResult.getBuildingDestruction()) + "[/b]\n");
        overallBuffer.append("[/quote]\n");

        jOverallStatsArea.setText(overallBuffer.toString());
        jAllyStatsArea.setText(allyBuffer.toString());
        jTribeStatsArea.setText(tribeBuffer.toString());
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
        System.out.println(pVillages);
    }


    static class TribeStatResult {

        Tribe tribe = null;
        private int attacks = 0;
        private int offs = 0;
        private int snobs = 0;
        private int fakes = 0;
        private int enoblements = 0;
        private int losses = 0;
        private int lossesAsFarm = 0;
        private int kills = 0;
        private int killsAsFarm = 0;
        private int wallDestruction = 0;
        private int buildingDestruction = 0;

        public TribeStatResult() {
        }

        public void setTribeStats(SingleAttackerStat pStat, boolean pUseApproxValues) {
            tribe = pStat.getAttacker();
            attacks = pStat.getOffCount() + pStat.getFakeCount() + pStat.getSnobAttackCount() + pStat.getSimpleSnobAttackCount();
            offs = pStat.getOffCount();
            snobs = pStat.getSimpleSnobAttackCount() + pStat.getSnobAttackCount();
            fakes = pStat.getFakeCount();
            enoblements = pStat.getEnoblementCount();
            losses = pStat.getSummedLosses();
            kills = pStat.getSummedKills();
            lossesAsFarm = pStat.getSummedLossesAsFarmSpace();
            killsAsFarm = pStat.getSummedKillsAsFarmSpace();
            if (pUseApproxValues) {
                kills += pStat.getAtLeast2KDamageCount() * 2000;
                kills += pStat.getAtLeast4KDamageCount() * 4000;
                kills += pStat.getAtLeast6KDamageCount() * 6000;
                kills += pStat.getAtLeast8KDamageCount() * 8000;
                killsAsFarm += pStat.getAtLeast2KDamageCount() * 2000 * 1.5;
                killsAsFarm += pStat.getAtLeast4KDamageCount() * 4000 * 1.5;
                killsAsFarm += pStat.getAtLeast6KDamageCount() * 6000 * 1.5;
                killsAsFarm += pStat.getAtLeast8KDamageCount() * 8000 * 1.5;
            }
            wallDestruction = pStat.getDestroyedWallLevels();
            buildingDestruction = pStat.getSummedDestroyedBuildings();
        }

        /**
         * @return the tribeStats
         */
        public Tribe getTribe() {
            return tribe;
        }

        /**
         * @return the attacks
         */
        public int getAttacks() {
            return attacks;
        }

        /**
         * @return the offs
         */
        public int getOffs() {
            return offs;
        }

        /**
         * @return the snobs
         */
        public int getSnobs() {
            return snobs;
        }

        /**
         * @return the fakes
         */
        public int getFakes() {
            return fakes;
        }

        /**
         * @return the enoblements
         */
        public int getEnoblements() {
            return enoblements;
        }

        /**
         * @return the losses
         */
        public int getLosses() {
            return losses;
        }

        /**
         * @return the lossesAsFarm
         */
        public int getLossesAsFarm() {
            return lossesAsFarm;
        }

        /**
         * @return the kills
         */
        public int getKills() {
            return kills;
        }

        /**
         * @return the killsAsFarm
         */
        public int getKillsAsFarm() {
            return killsAsFarm;
        }

        /**
         * @return the wallDestruction
         */
        public int getWallDestruction() {
            return wallDestruction;
        }

        /**
         * @return the buildingDestruction
         */
        public int getBuildingDestruction() {
            return buildingDestruction;
        }
    }

    static class AllyStatResult {

        private List<TribeStatResult> tribeStats = null;
        private int attacks = 0;
        private int offs = 0;
        private int snobs = 0;
        private int fakes = 0;
        private int enoblements = 0;
        private int losses = 0;
        private int lossesAsFarm = 0;
        private int kills = 0;
        private int killsAsFarm = 0;
        private int wallDestruction = 0;
        private int buildingDestruction = 0;

        public AllyStatResult() {
            tribeStats = new LinkedList<TribeStatResult>();
        }

        public void addTribeStatResult(TribeStatResult pStat) {
            getTribeStats().add(pStat);
            attacks += pStat.getOffs() + pStat.getFakes() + pStat.getSnobs();
            offs += pStat.getOffs();
            snobs += pStat.getSnobs();
            fakes += pStat.getFakes();
            enoblements += pStat.getEnoblements();
            losses += pStat.getLosses();
            kills += pStat.getKills();
            lossesAsFarm += pStat.getLossesAsFarm();
            killsAsFarm += pStat.getKillsAsFarm();

            wallDestruction += pStat.getWallDestruction();
            buildingDestruction += pStat.getBuildingDestruction();
        }

        public int getAttackers() {
            return tribeStats.size();
        }

        /**
         * @return the tribeStats
         */
        public List<TribeStatResult> getTribeStats() {
            return tribeStats;
        }

        /**
         * @return the attacks
         */
        public int getAttacks() {
            return attacks;
        }

        /**
         * @return the offs
         */
        public int getOffs() {
            return offs;
        }

        /**
         * @return the snobs
         */
        public int getSnobs() {
            return snobs;
        }

        /**
         * @return the fakes
         */
        public int getFakes() {
            return fakes;
        }

        /**
         * @return the enoblements
         */
        public int getEnoblements() {
            return enoblements;
        }

        /**
         * @return the losses
         */
        public int getLosses() {
            return losses;
        }

        /**
         * @return the lossesAsFarm
         */
        public int getLossesAsFarm() {
            return lossesAsFarm;
        }

        /**
         * @return the kills
         */
        public int getKills() {
            return kills;
        }

        /**
         * @return the killsAsFarm
         */
        public int getKillsAsFarm() {
            return killsAsFarm;
        }

        /**
         * @return the wallDestruction
         */
        public int getWallDestruction() {
            return wallDestruction;
        }

        /**
         * @return the buildingDestruction
         */
        public int getBuildingDestruction() {
            return buildingDestruction;
        }
    }

    static class OverallStatResult {

        private List<AllyStatResult> allyStats = null;
        private int attackers = 0;
        private int attacks = 0;
        private int offs = 0;
        private int snobs = 0;
        private int fakes = 0;
        private int enoblements = 0;
        private int losses = 0;
        private int lossesAsFarm = 0;
        private int kills = 0;
        private int killsAsFarm = 0;
        private int wallDestruction = 0;
        private int buildingDestruction = 0;

        public OverallStatResult() {
            allyStats = new LinkedList<AllyStatResult>();
        }

        public void addAllyStatsResult(AllyStatResult pStat) {
            allyStats.add(pStat);
            attackers += pStat.getAttackers();
            attacks += pStat.getOffs() + pStat.getFakes() + pStat.getSnobs();
            offs += pStat.getOffs();
            snobs += pStat.getSnobs();
            fakes += pStat.getFakes();
            enoblements += pStat.getEnoblements();
            losses += pStat.getLosses();
            kills += pStat.getKills();
            lossesAsFarm += pStat.getLossesAsFarm();
            killsAsFarm += pStat.getKillsAsFarm();
            wallDestruction += pStat.getWallDestruction();
            buildingDestruction += pStat.getBuildingDestruction();
        }

        /**
         * @return the tribeStats
         */
        public List<AllyStatResult> getAllyStats() {
            return allyStats;
        }

        public int getAttackers() {
            return attackers;
        }

        /**
         * @return the attacks
         */
        public int getAttacks() {
            return attacks;
        }

        /**
         * @return the offs
         */
        public int getOffs() {
            return offs;
        }

        /**
         * @return the snobs
         */
        public int getSnobs() {
            return snobs;
        }

        /**
         * @return the fakes
         */
        public int getFakes() {
            return fakes;
        }

        /**
         * @return the enoblements
         */
        public int getEnoblements() {
            return enoblements;
        }

        /**
         * @return the losses
         */
        public int getLosses() {
            return losses;
        }

        /**
         * @return the lossesAsFarm
         */
        public int getLossesAsFarm() {
            return lossesAsFarm;
        }

        /**
         * @return the kills
         */
        public int getKills() {
            return kills;
        }

        /**
         * @return the killsAsFarm
         */
        public int getKillsAsFarm() {
            return killsAsFarm;
        }

        /**
         * @return the wallDestruction
         */
        public int getWallDestruction() {
            return wallDestruction;
        }

        /**
         * @return the buildingDestruction
         */
        public int getBuildingDestruction() {
            return buildingDestruction;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchReportFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog jAddReportSetDialog;
    private javax.swing.JButton jAddTribeButton;
    private javax.swing.JTextArea jAllyStatsArea;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JFrame jCreateStatsFrame;
    private javax.swing.JTextField jCurrentSetField;
    private javax.swing.JButton jDoAddNewSetButton;
    private javax.swing.JButton jDoFilterButton;
    private javax.swing.JButton jDoMoveButton;
    private javax.swing.JButton jDoRenameButton;
    private javax.swing.JSpinner jEndDate;
    private javax.swing.JCheckBox jFilterByDate;
    private javax.swing.JCheckBox jFilterByTribeBox;
    private javax.swing.JDialog jFilterDialog;
    private javax.swing.JCheckBox jGuessUnknownLosses;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JDialog jMoveToSetDialog;
    private javax.swing.JTextField jNewReportSetField;
    private javax.swing.JComboBox jNewSetBox;
    private javax.swing.JTextField jNewSetNameField;
    private javax.swing.JTextArea jOverallStatsArea;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JButton jRemoveTribeButton;
    private javax.swing.JDialog jRenameReportSetDialog;
    private javax.swing.JComboBox jReportSetBox;
    private javax.swing.JList jReportSetsForStatsList;
    private javax.swing.JTable jReportTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JCheckBox jShowBlueReports;
    private javax.swing.JCheckBox jShowGreenReports;
    private javax.swing.JCheckBox jShowHiddenAttackerReports;
    private javax.swing.JCheckBox jShowPercentsBox;
    private javax.swing.JCheckBox jShowRedReports;
    private javax.swing.JCheckBox jShowSnobReports;
    private javax.swing.JCheckBox jShowYellowReports;
    private javax.swing.JSpinner jStartDate;
    private javax.swing.JTabbedPane jTabbedPane1;
    private com.l2fprod.common.swing.JTaskPane jTaskPane1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup2;
    private javax.swing.JList jTribeList;
    private javax.swing.JComboBox jTribeSelectionBox;
    private javax.swing.JTextField jTribeSelectionFilter;
    private javax.swing.JTextArea jTribeStatsArea;
    private javax.swing.JCheckBox jUseSilentKillsBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireReportsChangedEvent(String pPlan) {
        try {
            jReportTable.invalidate();
            for (int i = 0; i < jReportTable.getColumnCount(); i++) {
                jReportTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            }
            jReportTable.revalidate();
            jReportTable.repaint();
        } catch (Exception e) {
            logger.error("Failed to update attacks table", e);
        }
    }
}
