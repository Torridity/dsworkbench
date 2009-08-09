/*
 * DSWorkbenchAttackFrame.java
 *
 * Created on 28. September 2008, 14:58
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.AttackTypeCellEditor;
import de.tor.tribes.ui.models.AttackManagerTableModel;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.attack.AttackManager;
import java.util.List;
import de.tor.tribes.util.attack.AttackManagerListener;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.ui.models.StandardAttackTableModel;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.renderer.ColoredDateCellRenderer;
import de.tor.tribes.util.html.AttackPlanHTMLExporter;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.JOptionPaneHelper;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

// -Dsun.java2d.d3d=true -Dsun.java2d.translaccel=true -Dsun.java2d.ddforcevram=true
/**
 * @author  Charon
 */
public class DSWorkbenchAttackFrame extends AbstractDSWorkbenchFrame implements AttackManagerListener {

    private static Logger logger = Logger.getLogger("AttackView");
    private static DSWorkbenchAttackFrame SINGLETON = null;
    private List<DefaultTableCellRenderer> renderers = new LinkedList<DefaultTableCellRenderer>();
    private NotifyThread mNotifyThread = null;

    public static synchronized DSWorkbenchAttackFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchAttackFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchAttackFrame */
    DSWorkbenchAttackFrame() {
        initComponents();
        getContentPane().setBackground(Constants.DS_BACK);

        try {
            jAttackFrameAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("attack.frame.alwaysOnTop")));
            setAlwaysOnTop(jAttackFrameAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        jTaskPaneGroup1.setBackground(Constants.DS_BACK);
        //color scrollpanes of selection dialog
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jScrollPane2.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jScrollPane3.getViewport().setBackground(Constants.DS_BACK_LIGHT);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>();
        jAttackTable.setRowSorter(sorter);
        jAttackTable.setColumnSelectionAllowed(false);
        jAttackTable.getTableHeader().setReorderingAllowed(false);
        sorter.setModel(AttackManagerTableModel.getSingleton());
        jAttackTable.setModel(AttackManagerTableModel.getSingleton());
        jAttackTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    int selected = jAttackTable.getSelectedRows().length;
                    if (selected == 0) {
                        setTitle("Angriffe");
                    } else if (selected == 1) {
                        setTitle("Angriffe (1 Angriff ausgewählt)");
                    } else if (selected > 1) {
                        setTitle("Angriffe (" + selected + " Angriffe ausgewählt)");
                    }
                } catch (Exception ignored) {
                }
            }
        });

        //instantiate the column renderers
        for (int i = 0; i < jAttackTable.getColumnCount(); i++) {
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
            jAttackTable.getColumn(jAttackTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
            renderers.add(headerRenderer);
        }

        jAttackTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());
        jAttackTable.setDefaultEditor(Integer.class, new AttackTypeCellEditor());
        jAddPlanDialog.pack();
        jCopyToPlanDialog.pack();
        jRenamePlanDialog.pack();
        jSelectionFilterDialog.pack();
        jTimeChangeDialog.pack();
        jMoveToPlanDialog.pack();
        mNotifyThread = new NotifyThread();
        new ColorUpdateThread().start();
        mNotifyThread.start();
        jArriveDateField.setEditor(new DateEditor(jArriveDateField, "dd.MM.yy HH:mm:ss"));

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(jSelectionFilterDialog.getRootPane(), "pages.attack_select_filter", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(jTimeChangeDialog.getRootPane(), "pages.change_attack_times", GlobalOptions.getHelpBroker().getHelpSet());
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.attack_view", GlobalOptions.getHelpBroker().getHelpSet());
// </editor-fold>
        jStandardAttackDialog.pack();
        pack();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSelectionFilterDialog = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSourceTribeBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSourceVillageTable = new javax.swing.JTable();
        jAllSourceVillageButton = new javax.swing.JButton();
        jNoSourceVillageButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTargetTribeBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTargetVillageTable = new javax.swing.JTable();
        jAllTargetVillageButton = new javax.swing.JButton();
        jNoTargetVillageButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTimeChangeDialog = new javax.swing.JDialog();
        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jDayField = new javax.swing.JSpinner();
        jMinuteField = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jHourField = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        jSecondsField = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jArriveDateField = new javax.swing.JSpinner();
        jModifyArrivalOption = new javax.swing.JRadioButton();
        jMoveTimeOption = new javax.swing.JRadioButton();
        jRandomizeOption = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jRandomField = new javax.swing.JFormattedTextField();
        jLabel19 = new javax.swing.JLabel();
        jNotRandomToNightBonus = new javax.swing.JCheckBox();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jAddPlanDialog = new javax.swing.JDialog();
        jLabel10 = new javax.swing.JLabel();
        jAttackPlanName = new javax.swing.JTextField();
        jAddRemoveButton = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jRenamePlanDialog = new javax.swing.JDialog();
        jLabel11 = new javax.swing.JLabel();
        jNewPlanName = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jMoveToPlanDialog = new javax.swing.JDialog();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jCurrentPlanBox = new javax.swing.JTextField();
        jNewPlanBox = new javax.swing.JComboBox();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jCopyToPlanDialog = new javax.swing.JDialog();
        jLabel15 = new javax.swing.JLabel();
        jCurrentPlanField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jCopyTargetBox = new javax.swing.JComboBox();
        jCopyButton = new javax.swing.JButton();
        jCancelCopyButton = new javax.swing.JButton();
        jStandardAttackDialog = new javax.swing.JDialog();
        jScrollPane5 = new javax.swing.JScrollPane();
        jStandardAttackTable = new javax.swing.JTable();
        jButton11 = new javax.swing.JButton();
        jCancelApplyStandardAttacksButton = new javax.swing.JButton();
        jAttackPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAttackTable = new javax.swing.JTable();
        jActiveAttackPlan = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTaskPane1 = new com.l2fprod.common.swing.JTaskPane();
        jTaskPaneGroup1 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jCleanupAttacksButton = new javax.swing.JButton();
        jRemoveAttackButton = new javax.swing.JButton();
        jCopyAttackButton = new javax.swing.JButton();
        jMoveAttacksButton = new javax.swing.JButton();
        jChangeArrivalButton = new javax.swing.JButton();
        jTaskPaneGroup2 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jMarkAllButton = new javax.swing.JButton();
        jMarkFilteredButton = new javax.swing.JButton();
        jFlipMarkButton = new javax.swing.JButton();
        jDrawMarkedButton = new javax.swing.JButton();
        jNotDrawMarkedButton = new javax.swing.JButton();
        jTaskPaneGroup3 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jCopyUnformattedToClipboardButton = new javax.swing.JButton();
        jCopyBBCodeToClipboardButton = new javax.swing.JButton();
        jCopyBBCodeToClipboardButton1 = new javax.swing.JButton();
        jSendAttackButton = new javax.swing.JButton();
        jSendAttackButton1 = new javax.swing.JButton();
        jTaskPaneGroup4 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jNotifyButton = new javax.swing.JToggleButton();
        jAttackFrameAlwaysOnTop = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jSelectionFilterDialog.setTitle(bundle.getString("DSWorkbenchAttackFrame.jSelectionFilterDialog.title")); // NOI18N
        jSelectionFilterDialog.setAlwaysOnTop(true);
        jSelectionFilterDialog.setModal(true);

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("DSWorkbenchAttackFrame.jPanel1.border.title"))); // NOI18N
        jPanel1.setOpaque(false);

        jLabel1.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel1.text")); // NOI18N

        jLabel2.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel2.text")); // NOI18N
        jLabel2.setMaximumSize(new java.awt.Dimension(32, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(32, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(32, 14));

        jSourceTribeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireSourcePlayerChangedEvent(evt);
            }
        });

        jSourceVillageTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Dorf", "Auswählen"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jSourceVillageTable);

        jAllSourceVillageButton.setText(bundle.getString("DSWorkbenchAttackFrame.jAllSourceVillageButton.text")); // NOI18N
        jAllSourceVillageButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectNoneAllEvent(evt);
            }
        });

        jNoSourceVillageButton.setText(bundle.getString("DSWorkbenchAttackFrame.jNoSourceVillageButton.text")); // NOI18N
        jNoSourceVillageButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectNoneAllEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                            .addComponent(jSourceTribeBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 379, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jNoSourceVillageButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAllSourceVillageButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSourceTribeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jAllSourceVillageButton)
                            .addComponent(jNoSourceVillageButton)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(239, 235, 223));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("DSWorkbenchAttackFrame.jPanel2.border.title"))); // NOI18N
        jPanel2.setOpaque(false);

        jLabel3.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel3.text")); // NOI18N

        jTargetTribeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireTargetPlayerChangedEvent(evt);
            }
        });

        jLabel4.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel4.text")); // NOI18N

        jTargetVillageTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Dorf", "Auswählen"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTargetVillageTable);

        jAllTargetVillageButton.setText(bundle.getString("DSWorkbenchAttackFrame.jAllTargetVillageButton.text")); // NOI18N
        jAllTargetVillageButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectNoneAllEvent(evt);
            }
        });

        jNoTargetVillageButton.setText(bundle.getString("DSWorkbenchAttackFrame.jNoTargetVillageButton.text")); // NOI18N
        jNoTargetVillageButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectNoneAllEvent(evt);
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
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                            .addComponent(jTargetTribeBox, 0, 375, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jNoTargetVillageButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAllTargetVillageButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTargetTribeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jAllTargetVillageButton)
                            .addComponent(jNoTargetVillageButton)))
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton1.setText(bundle.getString("DSWorkbenchAttackFrame.jButton1.text_1")); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMarkFilterEvent(evt);
            }
        });

        jButton2.setText(bundle.getString("DSWorkbenchAttackFrame.jButton2.text_1")); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelFilterEvent(evt);
            }
        });

        javax.swing.GroupLayout jSelectionFilterDialogLayout = new javax.swing.GroupLayout(jSelectionFilterDialog.getContentPane());
        jSelectionFilterDialog.getContentPane().setLayout(jSelectionFilterDialogLayout);
        jSelectionFilterDialogLayout.setHorizontalGroup(
            jSelectionFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSelectionFilterDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSelectionFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jSelectionFilterDialogLayout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        jSelectionFilterDialogLayout.setVerticalGroup(
            jSelectionFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSelectionFilterDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(jSelectionFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTimeChangeDialog.setTitle(bundle.getString("DSWorkbenchAttackFrame.jTimeChangeDialog.title")); // NOI18N

        jOKButton.setText(bundle.getString("DSWorkbenchAttackFrame.jOKButton.text")); // NOI18N
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseTimeChangeDialogEvent(evt);
            }
        });

        jCancelButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCancelButton.text")); // NOI18N
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseTimeChangeDialogEvent(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel7.text")); // NOI18N

        jLabel5.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel5.text")); // NOI18N

        jDayField.setModel(new javax.swing.SpinnerNumberModel(0, -31, 31, 1));

        jMinuteField.setModel(new javax.swing.SpinnerNumberModel(0, -59, 59, 1));

        jLabel6.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel6.text")); // NOI18N

        jHourField.setModel(new javax.swing.SpinnerNumberModel(0, -59, 59, 1));

        jLabel14.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel14.text")); // NOI18N

        jSecondsField.setModel(new javax.swing.SpinnerNumberModel(0, -59, 59, 1));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMinuteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSecondsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jHourField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jHourField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSecondsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMinuteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel8.text")); // NOI18N
        jLabel8.setEnabled(false);

        jArriveDateField.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.MINUTE));
        jArriveDateField.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jArriveDateField.toolTipText")); // NOI18N
        jArriveDateField.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jArriveDateField, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jArriveDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        buttonGroup1.add(jModifyArrivalOption);
        jModifyArrivalOption.setText(bundle.getString("DSWorkbenchAttackFrame.jModifyArrivalOption.text")); // NOI18N
        jModifyArrivalOption.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jModifyArrivalOption.toolTipText")); // NOI18N
        jModifyArrivalOption.setOpaque(false);
        jModifyArrivalOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireModifyTimeEvent(evt);
            }
        });

        buttonGroup1.add(jMoveTimeOption);
        jMoveTimeOption.setSelected(true);
        jMoveTimeOption.setText(bundle.getString("DSWorkbenchAttackFrame.jMoveTimeOption.text")); // NOI18N
        jMoveTimeOption.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jMoveTimeOption.toolTipText")); // NOI18N
        jMoveTimeOption.setOpaque(false);
        jMoveTimeOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireModifyTimeEvent(evt);
            }
        });

        buttonGroup1.add(jRandomizeOption);
        jRandomizeOption.setText(bundle.getString("DSWorkbenchAttackFrame.jRandomizeOption.text")); // NOI18N
        jRandomizeOption.setOpaque(false);
        jRandomizeOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireModifyTimeEvent(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel17.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel17.text")); // NOI18N
        jLabel17.setEnabled(false);

        jLabel18.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel18.text")); // NOI18N
        jLabel18.setEnabled(false);

        jRandomField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));
        jRandomField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jRandomField.setText(bundle.getString("DSWorkbenchAttackFrame.jFormattedTextField1.text")); // NOI18N
        jRandomField.setEnabled(false);

        jLabel19.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel19.text")); // NOI18N
        jLabel19.setEnabled(false);

        jNotRandomToNightBonus.setSelected(true);
        jNotRandomToNightBonus.setText(bundle.getString("DSWorkbenchAttackFrame.jNotRandomToNightBonus.text")); // NOI18N
        jNotRandomToNightBonus.setEnabled(false);
        jNotRandomToNightBonus.setOpaque(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jNotRandomToNightBonus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRandomField, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addComponent(jRandomField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jNotRandomToNightBonus)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jTimeChangeDialogLayout = new javax.swing.GroupLayout(jTimeChangeDialog.getContentPane());
        jTimeChangeDialog.getContentPane().setLayout(jTimeChangeDialogLayout);
        jTimeChangeDialogLayout.setHorizontalGroup(
            jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTimeChangeDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRandomizeOption, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jModifyArrivalOption, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jMoveTimeOption, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jTimeChangeDialogLayout.createSequentialGroup()
                        .addComponent(jCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOKButton))
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jTimeChangeDialogLayout.setVerticalGroup(
            jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTimeChangeDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jMoveTimeOption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jModifyArrivalOption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jRandomizeOption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jOKButton)
                    .addComponent(jCancelButton))
                .addContainerGap())
        );

        jAddPlanDialog.setTitle(bundle.getString("DSWorkbenchAttackFrame.jAddPlanDialog.title")); // NOI18N
        jAddPlanDialog.setAlwaysOnTop(true);

        jLabel10.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel10.text")); // NOI18N

        jAttackPlanName.setText(bundle.getString("DSWorkbenchAttackFrame.jAttackPlanName.text")); // NOI18N

        jAddRemoveButton.setText(bundle.getString("DSWorkbenchAttackFrame.jAddRemoveButton.text")); // NOI18N
        jAddRemoveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddNewAttackPlanEvent(evt);
            }
        });

        jButton7.setText(bundle.getString("DSWorkbenchAttackFrame.jButton7.text")); // NOI18N
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelAddNewPlanEvent(evt);
            }
        });

        javax.swing.GroupLayout jAddPlanDialogLayout = new javax.swing.GroupLayout(jAddPlanDialog.getContentPane());
        jAddPlanDialog.getContentPane().setLayout(jAddPlanDialogLayout);
        jAddPlanDialogLayout.setHorizontalGroup(
            jAddPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddPlanDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jAddPlanDialogLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jAttackPlanName, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAddPlanDialogLayout.createSequentialGroup()
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAddRemoveButton)))
                .addContainerGap())
        );
        jAddPlanDialogLayout.setVerticalGroup(
            jAddPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddPlanDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jAttackPlanName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jAddPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAddRemoveButton)
                    .addComponent(jButton7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jRenamePlanDialog.setTitle(bundle.getString("DSWorkbenchAttackFrame.jRenamePlanDialog.title")); // NOI18N
        jRenamePlanDialog.setAlwaysOnTop(true);
        jRenamePlanDialog.setModal(true);

        jLabel11.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel11.text")); // NOI18N

        jNewPlanName.setText(bundle.getString("DSWorkbenchAttackFrame.jNewPlanName.text")); // NOI18N

        jButton6.setText(bundle.getString("DSWorkbenchAttackFrame.jButton6.text")); // NOI18N
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRenameEvent(evt);
            }
        });

        jButton8.setText(bundle.getString("DSWorkbenchAttackFrame.jButton8.text")); // NOI18N
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelRenameEvent(evt);
            }
        });

        javax.swing.GroupLayout jRenamePlanDialogLayout = new javax.swing.GroupLayout(jRenamePlanDialog.getContentPane());
        jRenamePlanDialog.getContentPane().setLayout(jRenamePlanDialogLayout);
        jRenamePlanDialogLayout.setHorizontalGroup(
            jRenamePlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRenamePlanDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jRenamePlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jRenamePlanDialogLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jNewPlanName, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jRenamePlanDialogLayout.createSequentialGroup()
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)))
                .addContainerGap())
        );
        jRenamePlanDialogLayout.setVerticalGroup(
            jRenamePlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRenamePlanDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jRenamePlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jNewPlanName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jRenamePlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6)
                    .addComponent(jButton8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMoveToPlanDialog.setTitle(bundle.getString("DSWorkbenchAttackFrame.jMoveToPlanDialog.title")); // NOI18N
        jMoveToPlanDialog.setAlwaysOnTop(true);
        jMoveToPlanDialog.setModal(true);

        jLabel12.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel12.text")); // NOI18N

        jLabel13.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel13.text")); // NOI18N

        jCurrentPlanBox.setEditable(false);
        jCurrentPlanBox.setText(bundle.getString("DSWorkbenchAttackFrame.jCurrentPlanBox.text")); // NOI18N

        jButton9.setText(bundle.getString("DSWorkbenchAttackFrame.jButton9.text")); // NOI18N
        jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jDoMoveToPlanEvent(evt);
            }
        });

        jButton10.setText(bundle.getString("DSWorkbenchAttackFrame.jButton10.text")); // NOI18N
        jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelMoveToPlanEvent(evt);
            }
        });

        javax.swing.GroupLayout jMoveToPlanDialogLayout = new javax.swing.GroupLayout(jMoveToPlanDialog.getContentPane());
        jMoveToPlanDialog.getContentPane().setLayout(jMoveToPlanDialogLayout);
        jMoveToPlanDialogLayout.setHorizontalGroup(
            jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMoveToPlanDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jMoveToPlanDialogLayout.createSequentialGroup()
                        .addGroup(jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jNewPlanBox, 0, 311, Short.MAX_VALUE)
                            .addComponent(jCurrentPlanBox, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jMoveToPlanDialogLayout.createSequentialGroup()
                        .addComponent(jButton10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9)))
                .addContainerGap())
        );
        jMoveToPlanDialogLayout.setVerticalGroup(
            jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jMoveToPlanDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jCurrentPlanBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jNewPlanBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton10)
                    .addComponent(jButton9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel15.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel15.text")); // NOI18N

        jCurrentPlanField.setEditable(false);
        jCurrentPlanField.setText(bundle.getString("DSWorkbenchAttackFrame.jCurrentPlanField.text")); // NOI18N

        jLabel16.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel16.text")); // NOI18N

        jCopyButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCopyButton.text")); // NOI18N
        jCopyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyEvent(evt);
            }
        });

        jCancelCopyButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCancelCopyButton.text")); // NOI18N
        jCancelCopyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyEvent(evt);
            }
        });

        javax.swing.GroupLayout jCopyToPlanDialogLayout = new javax.swing.GroupLayout(jCopyToPlanDialog.getContentPane());
        jCopyToPlanDialog.getContentPane().setLayout(jCopyToPlanDialogLayout);
        jCopyToPlanDialogLayout.setHorizontalGroup(
            jCopyToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCopyToPlanDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCopyToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jCopyToPlanDialogLayout.createSequentialGroup()
                        .addGroup(jCopyToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jCopyToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCopyTargetBox, 0, 308, Short.MAX_VALUE)
                            .addComponent(jCurrentPlanField, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jCopyToPlanDialogLayout.createSequentialGroup()
                        .addComponent(jCancelCopyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCopyButton)))
                .addContainerGap())
        );
        jCopyToPlanDialogLayout.setVerticalGroup(
            jCopyToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCopyToPlanDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCopyToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jCurrentPlanField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jCopyToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jCopyTargetBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jCopyToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCopyButton)
                    .addComponent(jCancelCopyButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jStandardAttackDialog.setTitle(bundle.getString("DSWorkbenchAttackFrame.jStandardAttackDialog.title")); // NOI18N
        jStandardAttackDialog.setAlwaysOnTop(true);
        jStandardAttackDialog.setModal(true);

        jStandardAttackTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(jStandardAttackTable);

        jButton11.setText(bundle.getString("DSWorkbenchAttackFrame.jButton11.text")); // NOI18N
        jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyStandardAttacksEvent(evt);
            }
        });

        jCancelApplyStandardAttacksButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCancelApplyStandardAttacksButton.text")); // NOI18N
        jCancelApplyStandardAttacksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyStandardAttacksEvent(evt);
            }
        });

        javax.swing.GroupLayout jStandardAttackDialogLayout = new javax.swing.GroupLayout(jStandardAttackDialog.getContentPane());
        jStandardAttackDialog.getContentPane().setLayout(jStandardAttackDialogLayout);
        jStandardAttackDialogLayout.setHorizontalGroup(
            jStandardAttackDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStandardAttackDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jStandardAttackDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jStandardAttackDialogLayout.createSequentialGroup()
                        .addComponent(jCancelApplyStandardAttacksButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton11)))
                .addContainerGap())
        );
        jStandardAttackDialogLayout.setVerticalGroup(
            jStandardAttackDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStandardAttackDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jStandardAttackDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jCancelApplyStandardAttacksButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle(bundle.getString("DSWorkbenchAttackFrame.title")); // NOI18N

        jAttackPanel.setBackground(new java.awt.Color(239, 235, 223));
        jAttackPanel.setMaximumSize(new java.awt.Dimension(750, 377));
        jAttackPanel.setMinimumSize(new java.awt.Dimension(750, 377));
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

        jActiveAttackPlan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireActiveAttackChangedEvent(evt);
            }
        });

        jLabel9.setText(bundle.getString("DSWorkbenchAttackFrame.jLabel9.text")); // NOI18N

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton3.setText(bundle.getString("DSWorkbenchAttackFrame.jButton3.text")); // NOI18N
        jButton3.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jButton3.toolTipText")); // NOI18N
        jButton3.setMaximumSize(new java.awt.Dimension(27, 25));
        jButton3.setMinimumSize(new java.awt.Dimension(27, 25));
        jButton3.setPreferredSize(new java.awt.Dimension(27, 25));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackPlanEvent(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(239, 235, 223));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jButton4.setText(bundle.getString("DSWorkbenchAttackFrame.jButton4.text")); // NOI18N
        jButton4.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jButton4.toolTipText")); // NOI18N
        jButton4.setMaximumSize(new java.awt.Dimension(27, 25));
        jButton4.setMinimumSize(new java.awt.Dimension(27, 25));
        jButton4.setPreferredSize(new java.awt.Dimension(27, 25));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRenameAttackPlanEvent(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(239, 235, 223));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton5.setText(bundle.getString("DSWorkbenchAttackFrame.jButton5.text")); // NOI18N
        jButton5.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jButton5.toolTipText")); // NOI18N
        jButton5.setMaximumSize(new java.awt.Dimension(27, 25));
        jButton5.setMinimumSize(new java.awt.Dimension(27, 25));
        jButton5.setPreferredSize(new java.awt.Dimension(27, 25));
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveAttackPlanEvent(evt);
            }
        });

        jTaskPane1.setOpaque(false);
        com.l2fprod.common.swing.PercentLayout percentLayout1 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout1.setOrientation(1);
        jTaskPane1.setLayout(percentLayout1);

        jTaskPaneGroup1.setTitle(bundle.getString("DSWorkbenchAttackFrame.jTaskPaneGroup1.title")); // NOI18N
        com.l2fprod.common.swing.PercentLayout percentLayout2 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout2.setOrientation(1);
        jTaskPaneGroup1.getContentPane().setLayout(percentLayout2);

        jCleanupAttacksButton.setBackground(new java.awt.Color(239, 235, 223));
        jCleanupAttacksButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/garbage.png"))); // NOI18N
        jCleanupAttacksButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.text")); // NOI18N
        jCleanupAttacksButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.toolTipText")); // NOI18N
        jCleanupAttacksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCleanUpAttacksEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jCleanupAttacksButton);
        jCleanupAttacksButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.AccessibleContext.accessibleDescription")); // NOI18N

        jRemoveAttackButton.setBackground(new java.awt.Color(239, 235, 223));
        jRemoveAttackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jRemoveAttackButton.setText(bundle.getString("DSWorkbenchAttackFrame.jRemoveAttackButton.text")); // NOI18N
        jRemoveAttackButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jRemoveAttackButton.toolTipText")); // NOI18N
        jRemoveAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveAttackEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jRemoveAttackButton);
        jRemoveAttackButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jRemoveAttackButton.AccessibleContext.accessibleDescription")); // NOI18N

        jCopyAttackButton.setBackground(new java.awt.Color(239, 235, 223));
        jCopyAttackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_copy.png"))); // NOI18N
        jCopyAttackButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCopyAttackButton.text")); // NOI18N
        jCopyAttackButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCopyAttackButton.toolTipText")); // NOI18N
        jCopyAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyAttacksEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jCopyAttackButton);
        jCopyAttackButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jCopyAttackButton.AccessibleContext.accessibleDescription")); // NOI18N

        jMoveAttacksButton.setBackground(new java.awt.Color(239, 235, 223));
        jMoveAttacksButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/replace2.png"))); // NOI18N
        jMoveAttacksButton.setText(bundle.getString("DSWorkbenchAttackFrame.jMoveAttacksButton.text")); // NOI18N
        jMoveAttacksButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jMoveAttacksButton.toolTipText")); // NOI18N
        jMoveAttacksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveAttacksEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jMoveAttacksButton);
        jMoveAttacksButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jMoveAttacksButton.AccessibleContext.accessibleDescription")); // NOI18N

        jChangeArrivalButton.setBackground(new java.awt.Color(239, 235, 223));
        jChangeArrivalButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_changeTime.png"))); // NOI18N
        jChangeArrivalButton.setText(bundle.getString("DSWorkbenchAttackFrame.jChangeArrivalButton.text")); // NOI18N
        jChangeArrivalButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jChangeArrivalButton.toolTipText")); // NOI18N
        jChangeArrivalButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeTimesEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jChangeArrivalButton);
        jChangeArrivalButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jChangeArrivalButton.AccessibleContext.accessibleDescription")); // NOI18N

        jTaskPane1.add(jTaskPaneGroup1);

        jTaskPaneGroup2.setTitle(bundle.getString("DSWorkbenchAttackFrame.jTaskPaneGroup2.title")); // NOI18N
        com.l2fprod.common.swing.PercentLayout percentLayout3 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout3.setOrientation(1);
        jTaskPaneGroup2.getContentPane().setLayout(percentLayout3);

        jMarkAllButton.setBackground(new java.awt.Color(239, 235, 223));
        jMarkAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectAllOrNone.gif"))); // NOI18N
        jMarkAllButton.setText(bundle.getString("DSWorkbenchAttackFrame.jButton1.text")); // NOI18N
        jMarkAllButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jButton1.toolTipText")); // NOI18N
        jMarkAllButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMarkAllEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jMarkAllButton);
        jMarkAllButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jMarkAllButton.AccessibleContext.accessibleDescription")); // NOI18N

        jMarkFilteredButton.setBackground(new java.awt.Color(239, 235, 223));
        jMarkFilteredButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectSome.png"))); // NOI18N
        jMarkFilteredButton.setText(bundle.getString("DSWorkbenchAttackFrame.jMarkAllButton.text")); // NOI18N
        jMarkFilteredButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jMarkAllButton.toolTipText")); // NOI18N
        jMarkFilteredButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectFilteredEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jMarkFilteredButton);
        jMarkFilteredButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jMarkFilteredButton.AccessibleContext.accessibleDescription")); // NOI18N

        jFlipMarkButton.setBackground(new java.awt.Color(239, 235, 223));
        jFlipMarkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectInv.gif"))); // NOI18N
        jFlipMarkButton.setText(bundle.getString("DSWorkbenchAttackFrame.jFlipMarkButton.text")); // NOI18N
        jFlipMarkButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jFlipMarkButton.toolTipText")); // NOI18N
        jFlipMarkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFlipMarkEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jFlipMarkButton);
        jFlipMarkButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jFlipMarkButton.AccessibleContext.accessibleDescription")); // NOI18N

        jDrawMarkedButton.setBackground(new java.awt.Color(239, 235, 223));
        jDrawMarkedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectDraw.png"))); // NOI18N
        jDrawMarkedButton.setText(bundle.getString("DSWorkbenchAttackFrame.jButton2.text")); // NOI18N
        jDrawMarkedButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jDrawMarkedButton.toolTipText")); // NOI18N
        jDrawMarkedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDrawSelectedEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jDrawMarkedButton);
        jDrawMarkedButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jDrawMarkedButton.AccessibleContext.accessibleDescription")); // NOI18N

        jNotDrawMarkedButton.setBackground(new java.awt.Color(239, 235, 223));
        jNotDrawMarkedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectNoDraw.png"))); // NOI18N
        jNotDrawMarkedButton.setText(bundle.getString("DSWorkbenchAttackFrame.jNotDrawMarkedButton.text")); // NOI18N
        jNotDrawMarkedButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jNotDrawMarkedButton.toolTipText")); // NOI18N
        jNotDrawMarkedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDrawSelectedEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jNotDrawMarkedButton);
        jNotDrawMarkedButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jNotDrawMarkedButton.AccessibleContext.accessibleDescription")); // NOI18N

        jTaskPane1.add(jTaskPaneGroup2);

        jTaskPaneGroup3.setTitle(bundle.getString("DSWorkbenchAttackFrame.jTaskPaneGroup3.title")); // NOI18N
        com.l2fprod.common.swing.PercentLayout percentLayout4 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout4.setOrientation(1);
        jTaskPaneGroup3.getContentPane().setLayout(percentLayout4);

        jCopyUnformattedToClipboardButton.setBackground(new java.awt.Color(239, 235, 223));
        jCopyUnformattedToClipboardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboard.png"))); // NOI18N
        jCopyUnformattedToClipboardButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCopyUnformattedToClipboardButton.text")); // NOI18N
        jCopyUnformattedToClipboardButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCopyUnformattedToClipboardButton.toolTipText")); // NOI18N
        jCopyUnformattedToClipboardButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCopyUnformattedToClipboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyUnformatedToClipboardEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jCopyUnformattedToClipboardButton);
        jCopyUnformattedToClipboardButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jCopyUnformattedToClipboardButton.AccessibleContext.accessibleDescription")); // NOI18N

        jCopyBBCodeToClipboardButton.setBackground(new java.awt.Color(239, 235, 223));
        jCopyBBCodeToClipboardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        jCopyBBCodeToClipboardButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCopyBBCodeToClipboardButton.text")); // NOI18N
        jCopyBBCodeToClipboardButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCopyBBCodeToClipboardButton.toolTipText")); // NOI18N
        jCopyBBCodeToClipboardButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCopyBBCodeToClipboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyAsBBCodeToClipboardEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jCopyBBCodeToClipboardButton);
        jCopyBBCodeToClipboardButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jCopyBBCodeToClipboardButton.AccessibleContext.accessibleDescription")); // NOI18N

        jCopyBBCodeToClipboardButton1.setBackground(new java.awt.Color(239, 235, 223));
        jCopyBBCodeToClipboardButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_HTML.png"))); // NOI18N
        jCopyBBCodeToClipboardButton1.setText(bundle.getString("DSWorkbenchAttackFrame.jCopyBBCodeToClipboardButton1.text")); // NOI18N
        jCopyBBCodeToClipboardButton1.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCopyBBCodeToClipboardButton1.toolTipText")); // NOI18N
        jCopyBBCodeToClipboardButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCopyBBCodeToClipboardButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireWriteToHTMLEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jCopyBBCodeToClipboardButton1);
        jCopyBBCodeToClipboardButton1.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jCopyBBCodeToClipboardButton1.AccessibleContext.accessibleDescription")); // NOI18N

        jSendAttackButton.setBackground(new java.awt.Color(239, 235, 223));
        jSendAttackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_browser.png"))); // NOI18N
        jSendAttackButton.setText(bundle.getString("DSWorkbenchAttackFrame.jSendAttackButton.text")); // NOI18N
        jSendAttackButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jSendAttackButton.toolTipText")); // NOI18N
        jSendAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSendAttackEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jSendAttackButton);
        jSendAttackButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jSendAttackButton.AccessibleContext.accessibleDescription")); // NOI18N

        jSendAttackButton1.setBackground(new java.awt.Color(239, 235, 223));
        jSendAttackButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/standard_attacks.png"))); // NOI18N
        jSendAttackButton1.setText(bundle.getString("DSWorkbenchAttackFrame.jSendAttackButton1.text")); // NOI18N
        jSendAttackButton1.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jSendAttackButton1.toolTipText")); // NOI18N
        jSendAttackButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSetStandardAttacksEvent(evt);
            }
        });
        jTaskPaneGroup3.getContentPane().add(jSendAttackButton1);

        jTaskPane1.add(jTaskPaneGroup3);

        jTaskPaneGroup4.setTitle(bundle.getString("DSWorkbenchAttackFrame.jTaskPaneGroup4.title")); // NOI18N
        com.l2fprod.common.swing.PercentLayout percentLayout5 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout5.setOrientation(1);
        jTaskPaneGroup4.getContentPane().setLayout(percentLayout5);

        jNotifyButton.setBackground(new java.awt.Color(239, 235, 223));
        jNotifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_alert.png"))); // NOI18N
        jNotifyButton.setText(bundle.getString("DSWorkbenchAttackFrame.jNotifyButton.text")); // NOI18N
        jNotifyButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jNotifyButton.toolTipText")); // NOI18N
        jNotifyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeNotifyEvent(evt);
            }
        });
        jTaskPaneGroup4.getContentPane().add(jNotifyButton);
        jNotifyButton.getAccessibleContext().setAccessibleDescription(bundle.getString("DSWorkbenchAttackFrame.jNotifyButton.AccessibleContext.accessibleDescription")); // NOI18N

        jTaskPane1.add(jTaskPaneGroup4);

        jScrollPane4.setViewportView(jTaskPane1);

        javax.swing.GroupLayout jAttackPanelLayout = new javax.swing.GroupLayout(jAttackPanel);
        jAttackPanel.setLayout(jAttackPanelLayout);
        jAttackPanelLayout.setHorizontalGroup(
            jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jAttackPanelLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(jActiveAttackPlan, 0, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jAttackPanelLayout.setVerticalGroup(
            jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(jActiveAttackPlan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE))
                .addContainerGap())
        );

        jAttackFrameAlwaysOnTop.setText(bundle.getString("DSWorkbenchAttackFrame.jAttackFrameAlwaysOnTop.text")); // NOI18N
        jAttackFrameAlwaysOnTop.setOpaque(false);
        jAttackFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAttackFrameAlwaysOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jAttackPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAttackFrameAlwaysOnTop))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jAttackPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAttackFrameAlwaysOnTop)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireRemoveAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAttackEvent
    int[] rows = jAttackTable.getSelectedRows();
    if (rows.length == 0) {
        return;
    }

    String message = ((rows.length == 1) ? "Angriff " : (rows.length + " Angriffe ")) + "wirklich löschen?";
    if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Angriffe löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
        return;
    }

    jAttackTable.editingCanceled(new ChangeEvent(this));

    for (int r = rows.length - 1; r >= 0; r--) {
        jAttackTable.invalidate();
        int row = jAttackTable.convertRowIndexToModel(rows[r]);
        AttackManagerTableModel.getSingleton().removeRow(row);
        jAttackTable.revalidate();
    }
    jAttackTable.repaint();//.updateUI();
}//GEN-LAST:event_fireRemoveAttackEvent

private void fireSendAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSendAttackEvent
    int[] selectedRows = jAttackTable.getSelectedRows();
    if (selectedRows != null && selectedRows.length < 1) {
        return;
    }

    if (selectedRows.length > 10) {
        JOptionPaneHelper.showInformationBox(this, "Es dürfen maximal 10 Angriffe auf einmal in den Browser übertragen werden.", "Maximum überschritten");
        return;
    }

    for (Integer selectedRow : selectedRows) {
        int row = jAttackTable.convertRowIndexToModel(selectedRow);
        Village source = (Village) AttackManagerTableModel.getSingleton().getValueAt(row, 0);
        Village target = (Village) AttackManagerTableModel.getSingleton().getValueAt(row, 1);
        BrowserCommandSender.sendTroops(source, target);
    }
}//GEN-LAST:event_fireSendAttackEvent

private void fireMarkAllEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMarkAllEvent
    int[] rows = jAttackTable.getSelectedRows();
    if ((rows == null) || (rows.length == 0)) {
        jAttackTable.getSelectionModel().setSelectionInterval(0, jAttackTable.getRowCount() - 1);
    } else {
        jAttackTable.getSelectionModel().removeSelectionInterval(0, jAttackTable.getRowCount() - 1);
    }
}//GEN-LAST:event_fireMarkAllEvent

private void fireDrawSelectedEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDrawSelectedEvent
    boolean draw = (evt.getSource() == jDrawMarkedButton);

    int[] rows = jAttackTable.getSelectedRows();
    if ((rows != null) && (rows.length > 0)) {
        for (int r : rows) {
            jAttackTable.invalidate();
            int row = jAttackTable.convertRowIndexToModel(r);
            AttackManagerTableModel.getSingleton().setValueAt(new Boolean(draw), row, 5);
            jAttackTable.revalidate();
        }
    }
    jAttackTable.repaint();//.updateUI();
}//GEN-LAST:event_fireDrawSelectedEvent

private void fireCopyUnformatedToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyUnformatedToClipboardEvent
    try {
        int[] rows = jAttackTable.getSelectedRows();
        if ((rows != null) && (rows.length > 0)) {
            StringBuffer buffer = new StringBuffer();
            List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
            for (int i : rows) {
                jAttackTable.invalidate();
                int row = jAttackTable.convertRowIndexToModel(i);
                Village sVillage = attacks.get(row).getSource();
                Village tVillage = attacks.get(row).getTarget();
                UnitHolder sUnit = attacks.get(row).getUnit();
                Date aTime = attacks.get(row).getArriveTime();
                Date sTime = new Date(aTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(sVillage, tVillage, sUnit.getSpeed()) * 1000));
                int type = attacks.get(row).getType();
                String sendtime = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(sTime);
                String arrivetime = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(aTime);

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

                if (sVillage.getTribe() == null) {
                    buffer.append("Barbaren");
                } else {
                    buffer.append(sVillage.getTribe());
                }
                buffer.append("\t");
                buffer.append(sVillage);
                buffer.append("\t");
                buffer.append(sUnit);
                buffer.append("\t");
                if (tVillage.getTribe() == null) {
                    buffer.append("Barbaren");
                } else {
                    buffer.append(tVillage.getTribe());
                }
                buffer.append("\t");
                buffer.append(tVillage);
                buffer.append("\t");
                buffer.append(sendtime);
                buffer.append("\t");
                buffer.append(arrivetime);
                buffer.append("\n");
                jAttackTable.revalidate();
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(buffer.toString()), null);
            String result = "Daten in Zwischenablage kopiert.";
            JOptionPaneHelper.showInformationBox(this, result, "Information");
        } else {
        }
    } catch (Exception e) {
        logger.error("Failed to copy data to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPaneHelper.showErrorBox(this, result, "Fehler");
    }
}//GEN-LAST:event_fireCopyUnformatedToClipboardEvent

private void fireCopyAsBBCodeToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyAsBBCodeToClipboardEvent

    try {
        UIManager.put("OptionPane.noButtonText", "Nein");
        UIManager.put("OptionPane.yesButtonText", "Ja");

        boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.yesButtonText", "Yes");

        int[] rows = jAttackTable.getSelectedRows();
        if ((rows != null) && (rows.length > 0)) {
            StringBuffer buffer = new StringBuffer();
            if (extended) {
                buffer.append("[u][size=12]Angriffsplan[/size][/u]\n\n");
            } else {
                buffer.append("[u]Angriffsplan[/u]\n\n");
            }
            String sUrl = ServerManager.getServerURL(GlobalOptions.getSelectedServer());

            List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
            jAttackTable.invalidate();
            for (int i : rows) {
                int row = jAttackTable.convertRowIndexToModel(i);
                Village sVillage = attacks.get(row).getSource();
                Village tVillage = attacks.get(row).getTarget();
                UnitHolder sUnit = attacks.get(row).getUnit();
                Date aTime = attacks.get(row).getArriveTime();
                Date sTime = new Date(aTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(sVillage, tVillage, sUnit.getSpeed()) * 1000));
                int type = attacks.get(row).getType();
                String sendtime = null;
                String arrivetime = null;
                if (extended) {
                    sendtime = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(sTime);
                    arrivetime = new SimpleDateFormat("'[color=green]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(aTime);
                } else {
                    sendtime = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.SSS'[/color]'").format(sTime);
                    arrivetime = new SimpleDateFormat("'[color=green]'dd.MM.yy 'um' HH:mm:ss.SSS'[/color]'").format(aTime);
                }

                switch (type) {
                    case Attack.CLEAN_TYPE: {
                        buffer.append("Angriff (Clean-Off) ");
                        break;
                    }
                    case Attack.FAKE_TYPE: {
                        buffer.append("Angriff (Fake) ");
                        break;
                    }
                    case Attack.SNOB_TYPE: {
                        buffer.append("Angriff (AG) ");
                        break;
                    }
                    case Attack.SUPPORT_TYPE: {
                        buffer.append("Unterstützung ");
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
                buffer.append(" startet am ");
                buffer.append(sendtime);
                if (Boolean.parseBoolean(GlobalOptions.getProperty("export.arrive.time"))) {
                    buffer.append(" und kommt am ");
                    buffer.append(arrivetime);
                    buffer.append(" an\n");
                } else {
                    buffer.append("\n");
                }
            }
            jAttackTable.revalidate();
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
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Angriffe benötigen mehr als 500 BB-Codes\n" +
                        "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            String result = "Daten in Zwischenablage kopiert.";
            JOptionPaneHelper.showInformationBox(this, result, "Information");
        }
    } catch (Exception e) {
        logger.error("Failed to copy data to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPaneHelper.showErrorBox(this, result, "Fehler");
    }
}//GEN-LAST:event_fireCopyAsBBCodeToClipboardEvent

private void fireMarkFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMarkFilterEvent

    //get selected source villages
    List<Village> source = new LinkedList<Village>();
    for (int i = 0; i < jSourceVillageTable.getRowCount(); i++) {
        Village v = (Village) jSourceVillageTable.getValueAt(i, 0);
        Boolean b = (Boolean) jSourceVillageTable.getValueAt(i, 1);
        if (b.booleanValue()) {
            source.add(v);
        }
    }
    //get selected target villages
    List<Village> target = new LinkedList<Village>();
    for (int i = 0; i < jTargetVillageTable.getRowCount(); i++) {
        Village v = (Village) jTargetVillageTable.getValueAt(i, 0);
        Boolean b = (Boolean) jTargetVillageTable.getValueAt(i, 1);
        if (b.booleanValue()) {
            target.add(v);
        }
    }

    //get the line numbers of attacks which should be selected
    List<Integer> selection = new LinkedList<Integer>();
    int cnt = 0;
    for (Attack a : AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan())) {
        int row = jAttackTable.convertRowIndexToView(cnt);
        if (source.contains(a.getSource())) {
            if (!selection.contains(new Integer(row))) {
                selection.add(new Integer(row));
            }
        }
        if (target.contains(a.getTarget())) {
            if (!selection.contains(new Integer(row))) {
                selection.add(new Integer(row));
            }
        }
        cnt++;
    }
    //remove current selection
    jAttackTable.getSelectionModel().removeIndexInterval(0, jAttackTable.getRowCount() - 1);
    jAttackTable.getSelectionModel().setValueIsAdjusting(true);
    for (Integer i : selection) {
        jAttackTable.getSelectionModel().addSelectionInterval(i, i);
    }
    jAttackTable.getSelectionModel().setValueIsAdjusting(false);

    jSelectionFilterDialog.setVisible(false);
}//GEN-LAST:event_fireMarkFilterEvent

private void fireCancelFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelFilterEvent
    jSelectionFilterDialog.setVisible(false);
}//GEN-LAST:event_fireCancelFilterEvent

private void fireSourcePlayerChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireSourcePlayerChangedEvent
    try {
        Tribe source = null;

        try {
            source = (Tribe) jSourceTribeBox.getSelectedItem();
        } catch (Exception inner) {
            //probably a barbarian village was selected. tribe stays 'null'
        }

        List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
        Hashtable<Village, Boolean> villageMarks = new Hashtable<Village, Boolean>();
        //check attacks for the selected player
        int cnt = 0;

        jSourceVillageTable.invalidate();
        for (Attack a : attacks) {
            int row = jAttackTable.convertRowIndexToView(cnt);
            Village v = a.getSource();
            if ((v != null) && (v.getTribe() == source)) {
                if (jAttackTable.getSelectionModel().isSelectedIndex(row)) {
                    //village is selected in the attack table, so select it here too
                    villageMarks.put(v, Boolean.TRUE);
                } else {
                    villageMarks.put(v, Boolean.FALSE);
                }
            }
            cnt++;
            //create model with player villages in attack plan
            setTableModel(jSourceVillageTable, villageMarks);
            jSourceVillageTable.revalidate();
            jSourceVillageTable.repaint();//.updateUI();
        }
    } catch (Exception e) {
        //"please select" selected    
        setTableModel(jSourceVillageTable, new Hashtable<Village, Boolean>());
    }
}//GEN-LAST:event_fireSourcePlayerChangedEvent

private void fireTargetPlayerChangedEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireTargetPlayerChangedEvent
    try {
        Tribe target = null;

        try {
            target = (Tribe) jTargetTribeBox.getSelectedItem();
        } catch (Exception inner) {
            //probably a barbarian village was selected. tribe stays 'null'
        }

        List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
        Hashtable<Village, Boolean> villageMarks = new Hashtable<Village, Boolean>();
        //check attacks for the selected player
        int cnt = 0;

        jTargetVillageTable.invalidate();
        for (Attack a : attacks) {
            int row = jAttackTable.convertRowIndexToView(cnt);
            Village v = a.getTarget();
            if ((v != null) && (v.getTribe() == target)) {
                if (jAttackTable.getSelectionModel().isSelectedIndex(row)) {
                    //village is selected in the attack table, so select it here too
                    villageMarks.put(v, Boolean.TRUE);
                } else {
                    villageMarks.put(v, Boolean.FALSE);
                }
            }
            cnt++;
        }

        //create model with player villages in attack plan
        setTableModel(jTargetVillageTable, villageMarks);
        jTargetVillageTable.revalidate();
        jTargetVillageTable.repaint();//.updateUI();
    } catch (Exception e) {
        //"please select" selected    
        setTableModel(jTargetVillageTable, new Hashtable<Village, Boolean>());
    }
}//GEN-LAST:event_fireTargetPlayerChangedEvent

private void fireSelectFilteredEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectFilteredEvent
    List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
    List<Object> sourceTribes = new LinkedList<Object>();
    List<Object> targetTribes = new LinkedList<Object>();
    //search attacks for source and target tribes
    for (Attack a : attacks) {
        Tribe s = a.getSource().getTribe();
        if (s != null) {
            if (!sourceTribes.contains(s)) {
                sourceTribes.add(s);
            }
        } else {
            String barb = "Barbaren";
            if (!sourceTribes.contains(barb)) {
                sourceTribes.add(barb);
            }
        }
        Tribe t = a.getTarget().getTribe();
        if (t != null) {
            if (!targetTribes.contains(t)) {
                targetTribes.add(t);
            }
        } else {
            String barb = "Barbaren";
            if (!targetTribes.contains(barb)) {
                targetTribes.add(barb);
            }
        }
    }
    //build source and target selection and select default value
    DefaultComboBoxModel sModel = new DefaultComboBoxModel(sourceTribes.toArray(new Object[]{}));

    sModel.insertElementAt("Bitte wählen", 0);
    jSourceTribeBox.setModel(sModel);
    sModel.setSelectedItem("Bitte wählen");
    DefaultComboBoxModel tModel = new DefaultComboBoxModel(targetTribes.toArray(new Object[]{}));
    tModel.insertElementAt("Bitte wählen", 0);
    jTargetTribeBox.setModel(tModel);
    tModel.setSelectedItem("Bitte wählen");
    //initialize tables and scroll panes
    setTableModel(jSourceVillageTable, new Hashtable<Village, Boolean>());
    setTableModel(jTargetVillageTable, new Hashtable<Village, Boolean>());

    jSelectionFilterDialog.setVisible(true);
}//GEN-LAST:event_fireSelectFilteredEvent

private void fireFlipMarkEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFlipMarkEvent

    int rows[] = jAttackTable.getSelectedRows();
    List<Integer> selected = new LinkedList<Integer>();
    //add currently selected rows to a list
    for (int row : rows) {
        selected.add(new Integer(row));
    }

    int cnt = jAttackTable.getRowCount();
    //look for all rows wether the index is selected or not.
    //selected indices are removed from the existing list, unselected are added
    for (int i = 0; i < cnt; i++) {
        int row = jAttackTable.convertRowIndexToModel(i);
        Integer iV = new Integer(row);
        if (selected.contains(iV)) {
            selected.remove(iV);
        } else {
            selected.add(iV);
        }

    }

    //assign the values of the selected list to the table
    jAttackTable.getSelectionModel().setValueIsAdjusting(true);
    for (int i = 0; i <
            cnt; i++) {
        Integer iV = new Integer(i);
        if (selected.contains(iV)) {
            jAttackTable.getSelectionModel().addSelectionInterval(i, i);
        } else {
            jAttackTable.getSelectionModel().removeSelectionInterval(i, i);
        }

    }
    jAttackTable.getSelectionModel().setValueIsAdjusting(false);
}//GEN-LAST:event_fireFlipMarkEvent

private void fireSelectNoneAllEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectNoneAllEvent
    if (evt.getSource() == jAllSourceVillageButton) {
        if (jSourceVillageTable.getRowCount() > 0) {
            //update source table values that all villages are selected
            jSourceVillageTable.invalidate();
            for (int i = 0; i <
                    jSourceVillageTable.getRowCount(); i++) {
                jSourceVillageTable.getModel().setValueAt(Boolean.TRUE, i, 1);
            }

            jSourceVillageTable.revalidate();
            jSourceVillageTable.repaint();//.updateUI();
        }

    } else if (evt.getSource() == jNoSourceVillageButton) {
        //update source table values that all villages are unselected
        jSourceVillageTable.invalidate();
        for (int i = 0; i < jSourceVillageTable.getRowCount(); i++) {
            jSourceVillageTable.getModel().setValueAt(Boolean.FALSE, i, 1);
        }

        jSourceVillageTable.revalidate();
        jSourceVillageTable.repaint();//.updateUI();
    } else if (evt.getSource() == jAllTargetVillageButton) {
        //update target table values that all villages are selected
        if (jTargetVillageTable.getRowCount() > 0) {
            jTargetVillageTable.invalidate();
            for (int i = 0; i <
                    jTargetVillageTable.getRowCount(); i++) {
                jTargetVillageTable.getModel().setValueAt(Boolean.TRUE, i, 1);
            }

            jTargetVillageTable.revalidate();
            jTargetVillageTable.repaint();//.updateUI();
        }

    } else if (evt.getSource() == jNoTargetVillageButton) {
        //update target table values that all villages are unselected
        jTargetVillageTable.invalidate();
        for (int i = 0; i <
                jTargetVillageTable.getRowCount(); i++) {
            jTargetVillageTable.getModel().setValueAt(Boolean.FALSE, i, 1);
        }

        jTargetVillageTable.revalidate();
        jTargetVillageTable.repaint();//.updateUI();
    }
}//GEN-LAST:event_fireSelectNoneAllEvent

private void fireCloseTimeChangeDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseTimeChangeDialogEvent
    if (evt.getSource() == jOKButton) {
        int[] rows = jAttackTable.getSelectedRows();
        if ((rows != null) || (rows.length > 0)) {
            if (jMoveTimeOption.isSelected()) {
                Integer sec = (Integer) jSecondsField.getValue();
                Integer min = (Integer) jMinuteField.getValue();
                Integer hour = (Integer) jHourField.getValue();
                Integer day = (Integer) jDayField.getValue();

                //jArriveDateField.getValue()
                List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
                jAttackTable.invalidate();
                for (int i : rows) {
                    int row = jAttackTable.convertRowIndexToModel(i);
                    long arrive = attacks.get(row).getArriveTime().getTime();
                    long diff = sec * 1000 + min * 60000 + hour * 3600000 + day * 86400000;
                    //later if first index is selected
                    //if later, add diff to arrival, else remove diff from arrival
                    arrive += diff;
                    attacks.get(row).setArriveTime(new Date(arrive));
                }

                jAttackTable.revalidate();
                jAttackTable.repaint();//.updateUI();
            } else if (jModifyArrivalOption.isSelected()) {
                Date arrive = (Date) jArriveDateField.getValue();
                jAttackTable.invalidate();
                for (int i : rows) {
                    int row = jAttackTable.convertRowIndexToModel(i);
                    //later if first index is selected
                    //if later, add diff to arrival, else remove diff from arrival
                    AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan()).get(row).setArriveTime(arrive);
                }

                jAttackTable.revalidate();
                jAttackTable.repaint();//.updateUI();
            } else if (jRandomizeOption.isSelected()) {
                long rand = (Long) jRandomField.getValue() * 60 * 60 * 1000;
                jAttackTable.invalidate();
                for (int i : rows) {
                    int row = jAttackTable.convertRowIndexToModel(i);
                    List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
                    Calendar c = Calendar.getInstance();
                    boolean valid = false;
                    while (!valid) {
                        //random until valid value was found
                        long arrive = attacks.get(row).getArriveTime().getTime();
                        //later if first index is selected
                        //if later, add diff to arrival, else remove diff from arrival
                        int sign = (Math.random() > .5) ? 1 : -1;
                        arrive = (long) (arrive + (sign * Math.random() * rand));

                        c.setTimeInMillis(arrive);
                        int hours = c.get(Calendar.HOUR_OF_DAY);
                        if (hours >= 0 && hours < 8 && jNotRandomToNightBonus.isSelected()) {
                            //only invalid if in night bonus and this is not allowed
                            valid = false;
                        } else {
                            valid = true;
                        }
                    }

                    attacks.get(row).setArriveTime(c.getTime());
                }
                jAttackTable.revalidate();
                jAttackTable.repaint();//.updateUI();
            }
        }
    }

    jTimeChangeDialog.setVisible(false);
}//GEN-LAST:event_fireCloseTimeChangeDialogEvent

private void fireChangeTimesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeTimesEvent
    int[] rows = jAttackTable.getSelectedRows();
    if ((rows == null) || (rows.length <= 0)) {
        JOptionPaneHelper.showInformationBox(this, "Keine Angriffe markiert", "Information");
        return;

    }
    jTimeChangeDialog.setVisible(true);
}//GEN-LAST:event_fireChangeTimesEvent

private void fireChangeNotifyEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeNotifyEvent
    mNotifyThread.setActive(jNotifyButton.isSelected());
}//GEN-LAST:event_fireChangeNotifyEvent

private void fireAddAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackPlanEvent
    jAddPlanDialog.setLocationRelativeTo(this);
    jAddPlanDialog.setVisible(true);
}//GEN-LAST:event_fireAddAttackPlanEvent

private void fireRenameAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRenameAttackPlanEvent
    String selection = (String) jActiveAttackPlan.getSelectedItem();
    if (selection == null) {
        return;
    }

    if (selection.equals(AttackManager.DEFAULT_PLAN_ID)) {
        JOptionPaneHelper.showInformationBox(this, "Der Standardplan kann nicht umbenannt werden.", "Information");
        return;

    }

    jNewPlanName.setText(selection);
    jRenamePlanDialog.setLocationRelativeTo(this);
    jRenamePlanDialog.setVisible(true);
}//GEN-LAST:event_fireRenameAttackPlanEvent

private void fireRemoveAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAttackPlanEvent
    String selection = (String) jActiveAttackPlan.getSelectedItem();
    if (selection == null) {
        return;
    }

    if (selection.equals(AttackManager.DEFAULT_PLAN_ID)) {
        JOptionPaneHelper.showInformationBox(this, "Der Standardplan kann nicht gelöscht werden.", "Information");
        return;
    }


    if (JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du den Angriffsplan '" + selection + "' und alle enthaltenen Angriffe\n" +
            "wirklich löschen?", "Angriffsplan löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        AttackManagerTableModel.getSingleton().setActiveAttackPlan(AttackManager.DEFAULT_PLAN_ID);
        AttackManager.getSingleton().removePlan(selection);
        buildAttackPlanList();

        jActiveAttackPlan.setSelectedIndex(0);
    }
}//GEN-LAST:event_fireRemoveAttackPlanEvent

private void fireActiveAttackChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireActiveAttackChangedEvent
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        jAttackTable.invalidate();

        try {
            jAttackTable.getCellEditor().cancelCellEditing();
        } catch (Exception e) {
        }
        jAttackTable.getSelectionModel().clearSelection();
        jAttackTable.setRowSorter(new TableRowSorter(AttackManagerTableModel.getSingleton()));
        AttackManagerTableModel.getSingleton().setActiveAttackPlan((String) jActiveAttackPlan.getSelectedItem());

        jAttackTable.repaint();//.updateUI();
        jAttackTable.revalidate();
    }
}//GEN-LAST:event_fireActiveAttackChangedEvent

private void fireAddNewAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddNewAttackPlanEvent
    String name = jAttackPlanName.getText();
    if (AttackManager.getSingleton().getAttackPlan(name) != null) {
        JOptionPaneHelper.showWarningBox(jAddPlanDialog, "Ein Plan mit dem angegebenen Namen existiert bereits.\n" +
                "Bitte wähle einen anderen Namen oder lösche zuerst den bestehenden Plan.", "Warnung");
        return;

    }
    AttackManager.getSingleton().addEmptyPlan(name);
    buildAttackPlanList();

    jAddPlanDialog.setVisible(false);
}//GEN-LAST:event_fireAddNewAttackPlanEvent

private void fireRenameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRenameEvent
    String selection = (String) jActiveAttackPlan.getSelectedItem();
    String newName = jNewPlanName.getText();
    if (AttackManager.getSingleton().getAttackPlan(newName) != null) {
        JOptionPaneHelper.showWarningBox(jRenamePlanDialog, "Ein Plan mit dem Namen '" + newName + "' existiert bereits.\n" +
                "Bitte wähle einen anderen Namen oder lösche zuerst den bestehenden Plan.", "Warnung");
        return;

    }
    AttackManager.getSingleton().renamePlan(selection, newName);
    buildAttackPlanList();
    jRenamePlanDialog.setVisible(false);
}//GEN-LAST:event_fireRenameEvent

private void fireCancelRenameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelRenameEvent
    jRenamePlanDialog.setVisible(false);
}//GEN-LAST:event_fireCancelRenameEvent

private void fireCancelAddNewPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelAddNewPlanEvent
    jAddPlanDialog.setVisible(false);
}//GEN-LAST:event_fireCancelAddNewPlanEvent

private void fireMoveAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveAttacksEvent
    String current = (String) jActiveAttackPlan.getSelectedItem();
    if (current == null) {
        return;
    }
    jCurrentPlanBox.setText(current);
    Enumeration<String> plans = AttackManager.getSingleton().getPlans();
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    while (plans.hasMoreElements()) {
        String plan = plans.nextElement();
        if (!plan.equals(current)) {
            model.addElement(plan);
        }
    }
    jNewPlanBox.setModel(model);
    jNewPlanBox.setSelectedItem(current);
    jMoveToPlanDialog.setVisible(true);
}//GEN-LAST:event_fireMoveAttacksEvent

private void fireCancelMoveToPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelMoveToPlanEvent
    jMoveToPlanDialog.setVisible(false);
}//GEN-LAST:event_fireCancelMoveToPlanEvent

private void jDoMoveToPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jDoMoveToPlanEvent
    try {
        String oldPlan = jCurrentPlanBox.getText();
        String newPlan = (String) jNewPlanBox.getSelectedItem();

        if (newPlan == null) {
            JOptionPaneHelper.showInformationBox(jMoveToPlanDialog, "Kein neuer Plan ausgewählt", "Information");
            return;
        }
        int[] rows = jAttackTable.getSelectedRows();
        if ((rows != null) && (rows.length > 0)) {
            List<Attack> sourcePlan = AttackManager.getSingleton().getAttackPlan(oldPlan);
            List<Attack> tmpPlan = new LinkedList<Attack>();
            jAttackTable.invalidate();
            for (int i : rows) {
                int row = jAttackTable.convertRowIndexToModel(i);
                tmpPlan.add(sourcePlan.get(row));
            }

            AttackManager.getSingleton().removeAttacks(oldPlan, rows);
            for (Attack a : tmpPlan) {
                AttackManager.getSingleton().addAttack(a.getSource(), a.getTarget(), a.getUnit(), a.getArriveTime(), newPlan);
            }
            jAttackTable.revalidate();
        }
    } catch (Exception e) {
        logger.error("Failed to move attacks", e);
    }
    jMoveToPlanDialog.setVisible(false);
}//GEN-LAST:event_jDoMoveToPlanEvent

private void fireModifyTimeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireModifyTimeEvent
    boolean moveMode = false;
    boolean arriveMode = false;
    boolean randomMode = false;
    if (evt.getSource() == jMoveTimeOption) {
        moveMode = true;
    } else if (evt.getSource() == jModifyArrivalOption) {
        arriveMode = true;
    } else if (evt.getSource() == jRandomizeOption) {
        randomMode = true;
    }
    jLabel5.setEnabled(moveMode);
    jLabel6.setEnabled(moveMode);
    jLabel7.setEnabled(moveMode);
    jMinuteField.setEnabled(moveMode);
    jHourField.setEnabled(moveMode);
    jDayField.setEnabled(moveMode);
    //set arrive options
    jLabel8.setEnabled(arriveMode);
    jArriveDateField.setEnabled(arriveMode);
    //random options
    jLabel17.setEnabled(randomMode);
    jLabel18.setEnabled(randomMode);
    jLabel19.setEnabled(randomMode);
    jRandomField.setEnabled(randomMode);
    jNotRandomToNightBonus.setEnabled(randomMode);
}//GEN-LAST:event_fireModifyTimeEvent

private void fireCopyAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyAttacksEvent
    int[] rows = jAttackTable.getSelectedRows();
    if (rows == null || rows.length == 0) {
        JOptionPaneHelper.showInformationBox(this, "Keine Angriffe ausgewählt.", "Kopieren");
        return;
    }

    String active = (String) jActiveAttackPlan.getSelectedItem();
    if (active == null) {
        //no plan selected?
        active = AttackManager.DEFAULT_PLAN_ID;
    }
    //build plan list
    jCopyTargetBox.setModel(new DefaultComboBoxModel(AttackManager.getSingleton().getPlansAsArray()));
    jCurrentPlanField.setText(active);
    jCopyTargetBox.setSelectedItem(active);
    jCopyToPlanDialog.setVisible(true);
}//GEN-LAST:event_fireCopyAttacksEvent

private void fireCopyEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyEvent
    if (evt.getSource() == jCopyButton) {
        int[] rows = jAttackTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            return;
        }
        jAttackTable.editingCanceled(new ChangeEvent(this));
        String newPlan = (String) jCopyTargetBox.getSelectedItem();
        String current = (String) jActiveAttackPlan.getSelectedItem();
        logger.debug("Copying attacks from plan '" + current + "' to '" + newPlan + "'");
        List<Attack> currentPlan = AttackManager.getSingleton().getAttackPlan(current);
        for (int r = rows.length - 1; r >= 0; r--) {
            jAttackTable.invalidate();
            int row = jAttackTable.convertRowIndexToModel(rows[r]);
            Attack toCopy = currentPlan.get(row);
            AttackManager.getSingleton().addAttackFast(toCopy.getSource(), toCopy.getTarget(), toCopy.getUnit(), toCopy.getArriveTime(), toCopy.isShowOnMap(), newPlan, toCopy.getType());
            jAttackTable.revalidate();
        }
        AttackManager.getSingleton().forceUpdate(current);
        jAttackTable.repaint();//.updateUI();
    }

    jCopyToPlanDialog.setVisible(false);
}//GEN-LAST:event_fireCopyEvent

private void fireAttackFrameAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAttackFrameAlwaysOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireAttackFrameAlwaysOnTopEvent

private void fireWriteToHTMLEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireWriteToHTMLEvent
    String dir = GlobalOptions.getProperty("screen.dir");
    if (dir == null) {
        dir = ".";
    }
    String selectedPlan = AttackManagerTableModel.getSingleton().getActiveAttackPlan();
    JFileChooser chooser = null;
    try {
        chooser = new JFileChooser(dir);
    } catch (Exception e) {
        JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n" +
                "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
        return;
    }

    chooser.setDialogTitle("Datei auswählen");

    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

        @Override
        public boolean accept(File f) {
            if ((f != null) && (f.isDirectory() || f.getName().endsWith(".html"))) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "*.html";
        }
    });
    chooser.setSelectedFile(new File(dir + "/" + selectedPlan + ".html"));
    int ret = chooser.showSaveDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
        try {
            File f = chooser.getSelectedFile();
            String file = f.getCanonicalPath();
            if (!file.endsWith(".html")) {
                file += ".html";
            }

            File target = new File(file);
            if (target.exists()) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Bestehende Datei überschreiben?", "Überschreiben", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    //do not overwrite
                    return;
                }
            }
            int[] rows = jAttackTable.getSelectedRows();
            List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(selectedPlan);
            List<Attack> toExport = new LinkedList<Attack>();
            for (int i : rows) {
                int row = jAttackTable.convertRowIndexToModel(i);
                toExport.add(attacks.get(row));
            }
            AttackPlanHTMLExporter.doExport(target, selectedPlan, toExport);
            //store current directory
            GlobalOptions.addProperty("screen.dir", target.getParent());
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Angriffe erfolgreich gespeichert.\nWillst du die erstellte Datei jetzt im Browser betrachten?", "Information", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                BrowserCommandSender.openPage(target.toURI().toURL().toString());
            }
        } catch (Exception e) {
            logger.error("Failed to write attacks to HTML", e);
            JOptionPaneHelper.showErrorBox(this, "Fehler beim Speichern.", "Fehler");
        }
    }
}//GEN-LAST:event_fireWriteToHTMLEvent

private void fireCleanUpAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCleanUpAttacksEvent
    String selectedPlan = AttackManagerTableModel.getSingleton().getActiveAttackPlan();
    List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(selectedPlan);
    List<Attack> toRemove = new LinkedList<Attack>();
    for (Attack a : attacks) {
        long sendTime = a.getArriveTime().getTime() - ((long) DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
        if (sendTime < System.currentTimeMillis()) {
            toRemove.add(a);
        }
    }

    logger.debug("Cleaning up " + toRemove.size() + " attacks");

    for (Attack a : toRemove) {
        AttackManager.getSingleton().getAttackPlan(selectedPlan).remove(a);
    }

    AttackManager.getSingleton().forceUpdate(selectedPlan);


}//GEN-LAST:event_fireCleanUpAttacksEvent

private void fireSetStandardAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSetStandardAttacksEvent
    //build table
    try{
        System.out.println("setup");
    StandardAttackTableModel.getSingleton().setup();
        System.out.println("set model");
    jStandardAttackTable.setModel(StandardAttackTableModel.getSingleton());
     jStandardAttackDialog.setLocationRelativeTo(this);
    System.out.println("show");
    jStandardAttackDialog.setVisible(true);
    }catch(Exception e){
        e.printStackTrace();
    }

   
}//GEN-LAST:event_fireSetStandardAttacksEvent

private void fireApplyStandardAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireApplyStandardAttacksEvent
    if (evt.getSource() != jCancelApplyStandardAttacksButton) {
        //set standard attacks
    }
    jStandardAttackDialog.setVisible(false);
}//GEN-LAST:event_fireApplyStandardAttacksEvent

    /**Set table model for filteres selection*/
    private void setTableModel(JTable pTable, Hashtable<Village, Boolean> pVillages) {
        //create default table model
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Dorf", "Markieren"
                }) {

            Class[] types = new Class[]{
                Village.class, Boolean.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == 1) {
                    return true;
                }
                return false;
            }
        };

        //walk villages for row values
        if (pVillages.size() != 0) {
            Enumeration<Village> villages = pVillages.keys();
            while (villages.hasMoreElements()) {
                Village v = villages.nextElement();
                model.addRow(new Object[]{v, pVillages.get(v)});
            }

        }

        //set model and header renders
        pTable.setModel(model);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                c.setBackground(Constants.DS_BACK);
                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                r.setText("<html><b>" + r.getText() + "</b></html>");
                return c;
            }
        };

        for (int i = 0; i <
                pTable.getColumnCount(); i++) {
            pTable.getColumn(pTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }

        //set max width
        pTable.getColumnModel().getColumn(1).setMaxWidth(75);
        //set sorter
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        pTable.setRowSorter(sorter);
    }

    protected void setupAttackPanel() {
        AttackManager.getSingleton().addAttackManagerListener(this);
        //setup renderer and general view
        jAttackTable.setDefaultRenderer(Date.class, new ColoredDateCellRenderer());

        jAttackTable.setDefaultEditor(Date.class, new DateSpinEditor());
        jAttackTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
        jAttackTable.setDefaultEditor(Village.class, new VillageCellEditor());
        AttackManager.getSingleton().forceUpdate(null);
        buildAttackPlanList();
        jActiveAttackPlan.setSelectedItem(AttackManager.DEFAULT_PLAN_ID);
    }

    public void buildAttackPlanList() {
        Enumeration<String> plans = AttackManager.getSingleton().getPlans();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        while (plans.hasMoreElements()) {
            model.addElement(plans.nextElement());
        }
        jActiveAttackPlan.setModel(model);
        jActiveAttackPlan.setSelectedItem(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
    }

    public String getActiveAttackPlan() {
        return (String) jActiveAttackPlan.getSelectedItem();
    }

    @Override
    public void fireAttacksChangedEvent(String pPlan) {
        try {
            jAttackTable.invalidate();

            for (int i = 0; i < jAttackTable.getColumnCount(); i++) {
                jAttackTable.getColumn(jAttackTable.getColumnName(i)).setHeaderRenderer(renderers.get(i));
            }
            jAttackTable.revalidate();
            jAttackTable.repaint();//.updateUI();
        } catch (Exception e) {
            logger.error("Failed to update attacks table", e);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox jActiveAttackPlan;
    private javax.swing.JDialog jAddPlanDialog;
    private javax.swing.JButton jAddRemoveButton;
    private javax.swing.JButton jAllSourceVillageButton;
    private javax.swing.JButton jAllTargetVillageButton;
    private javax.swing.JSpinner jArriveDateField;
    private javax.swing.JCheckBox jAttackFrameAlwaysOnTop;
    private javax.swing.JPanel jAttackPanel;
    private javax.swing.JTextField jAttackPlanName;
    private javax.swing.JTable jAttackTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jCancelApplyStandardAttacksButton;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JButton jCancelCopyButton;
    private javax.swing.JButton jChangeArrivalButton;
    private javax.swing.JButton jCleanupAttacksButton;
    private javax.swing.JButton jCopyAttackButton;
    private javax.swing.JButton jCopyBBCodeToClipboardButton;
    private javax.swing.JButton jCopyBBCodeToClipboardButton1;
    private javax.swing.JButton jCopyButton;
    private javax.swing.JComboBox jCopyTargetBox;
    private javax.swing.JDialog jCopyToPlanDialog;
    private javax.swing.JButton jCopyUnformattedToClipboardButton;
    private javax.swing.JTextField jCurrentPlanBox;
    private javax.swing.JTextField jCurrentPlanField;
    private javax.swing.JSpinner jDayField;
    private javax.swing.JButton jDrawMarkedButton;
    private javax.swing.JButton jFlipMarkButton;
    private javax.swing.JSpinner jHourField;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton jMarkAllButton;
    private javax.swing.JButton jMarkFilteredButton;
    private javax.swing.JSpinner jMinuteField;
    private javax.swing.JRadioButton jModifyArrivalOption;
    private javax.swing.JButton jMoveAttacksButton;
    private javax.swing.JRadioButton jMoveTimeOption;
    private javax.swing.JDialog jMoveToPlanDialog;
    private javax.swing.JComboBox jNewPlanBox;
    private javax.swing.JTextField jNewPlanName;
    private javax.swing.JButton jNoSourceVillageButton;
    private javax.swing.JButton jNoTargetVillageButton;
    private javax.swing.JButton jNotDrawMarkedButton;
    private javax.swing.JCheckBox jNotRandomToNightBonus;
    private javax.swing.JToggleButton jNotifyButton;
    private javax.swing.JButton jOKButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JFormattedTextField jRandomField;
    private javax.swing.JRadioButton jRandomizeOption;
    private javax.swing.JButton jRemoveAttackButton;
    private javax.swing.JDialog jRenamePlanDialog;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSpinner jSecondsField;
    private javax.swing.JDialog jSelectionFilterDialog;
    private javax.swing.JButton jSendAttackButton;
    private javax.swing.JButton jSendAttackButton1;
    private javax.swing.JComboBox jSourceTribeBox;
    private javax.swing.JTable jSourceVillageTable;
    private javax.swing.JDialog jStandardAttackDialog;
    private javax.swing.JTable jStandardAttackTable;
    private javax.swing.JComboBox jTargetTribeBox;
    private javax.swing.JTable jTargetVillageTable;
    private com.l2fprod.common.swing.JTaskPane jTaskPane1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup2;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup3;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup4;
    private javax.swing.JDialog jTimeChangeDialog;
    // End of variables declaration//GEN-END:variables
}

// <editor-fold defaultstate="collapsed" desc=" NOTIFY THREAD ">
class NotifyThread extends Thread {

    private static Logger logger = Logger.getLogger("AttackNotificationHelper");
    private boolean active = false;
    private long nextCheck = 0;
    private final int TEN_MINUTES = 10 * 60 * 1000;

    public NotifyThread() {
        setDaemon(true);
        setPriority(MIN_PRIORITY);
    }

    public void setActive(boolean pValue) {
        active = pValue;
        if (active) {
            logger.debug("Starting notification cycle");
            nextCheck = System.currentTimeMillis();
        }
    }

    public void run() {

        while (true) {
            if (active) {
                long now = System.currentTimeMillis();
                if (now > nextCheck) {
                    logger.debug("Checking attacks");
                    //do next check
                    Hashtable<String, Integer> outstandingAttacks = new Hashtable<String, Integer>();
                    Enumeration<String> plans = AttackManager.getSingleton().getPlans();
                    while (plans.hasMoreElements()) {
                        String plan = plans.nextElement();
                        Attack[] attacks = AttackManager.getSingleton().getAttackPlan(plan).toArray(new Attack[]{});
                        int attackCount = 0;
                        for (Attack a : attacks) {
                            long sendTime = a.getArriveTime().getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
                            //find send times between now and in 10 minutes
                            if ((sendTime >= now) && (sendTime <= now + TEN_MINUTES)) {
                                attackCount++;
                            }
                        }
                        if (attackCount > 0) {
                            outstandingAttacks.put(plan, attackCount);
                        }
                    }
                    /* Attack[] attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan()).toArray(new Attack[]{});
                    int attackCount = 0;
                    for (Attack a : attacks) {
                    long sendTime = a.getArriveTime().getTime() - (long) DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000;
                    //find send times between now and in 10 minutes
                    if ((sendTime >= now) && (sendTime <= now + TEN_MINUTES)) {
                    attackCount++;
                    }
                    }
                     */
                    if (outstandingAttacks.size() > 0) {
                        // if (attackCount > 0) {
                        String message = "In den kommenden 10 Minuten müssen Angriffe aus den folgenden Plänen abgeschickt werden:\n";
                        Enumeration<String> outstandingPlans = outstandingAttacks.keys();
                        while (outstandingPlans.hasMoreElements()) {
                            String nextPlan = outstandingPlans.nextElement();
                            Integer cnt = outstandingAttacks.get(nextPlan);
                            message += nextPlan + " (" + cnt + ")\n";
                        }
                        NotifierFrame.doNotification(message, NotifierFrame.NOTIFY_ATTACK);
                        outstandingAttacks = null;
                        logger.debug("Scheduling next check in 10 minutes");
                        nextCheck = now + TEN_MINUTES;
                    } else {
                        //no attacks in next 10 minutes
                        logger.debug("Scheduling next check in 1 minute");
                        nextCheck = now + TEN_MINUTES / 10;
                    }
                }//wait for next check

            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
            }
        }
    }
}

class ColorUpdateThread extends Thread {

    public ColorUpdateThread() {
        setDaemon(true);
    }

    public void run() {
        while (true) {
            try {
                DSWorkbenchAttackFrame.getSingleton().repaint();
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                }
            } catch (Throwable t) {
            }
        }
    }
}

//</editor-fold>