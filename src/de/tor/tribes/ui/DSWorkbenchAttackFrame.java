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
import de.tor.tribes.ui.models.AttackManagerTableModel;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
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
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.util.DSCalculator;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

/**
 * @author  Charon
 */
public class DSWorkbenchAttackFrame extends AbstractDSWorkbenchFrame implements AttackManagerListener {

    private static Logger logger = Logger.getLogger("AttackDialog");
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
                int selected = jAttackTable.getSelectedRows().length;
                if (selected == 0) {
                    setTitle("Angriffe");
                } else if (selected == 1) {
                    setTitle("Angriffe (1 Angriff ausgewählt)");
                } else if (selected > 1) {
                    setTitle("Angriffe (" + selected + " Angriffe ausgewählt)");
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

        jAddPlanDialog.pack();

        jRenamePlanDialog.pack();

        jSelectionFilterDialog.pack();

        jTimeChangeDialog.pack();

        jMoveToPlanDialog.pack();
        mNotifyThread = new NotifyThread();

        mNotifyThread.start();

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
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jArriveDateField = new javax.swing.JSpinner();
        jModifyArrivalOption = new javax.swing.JRadioButton();
        jMoveTimeOption = new javax.swing.JRadioButton();
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
        jAttackPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAttackTable = new javax.swing.JTable();
        jRemoveAttackButton = new javax.swing.JButton();
        jCheckAttacksButton = new javax.swing.JButton();
        jSendAttackButton = new javax.swing.JButton();
        jMarkFilteredButton = new javax.swing.JButton();
        jCopyUnformattedToClipboardButton = new javax.swing.JButton();
        jCopyBBCodeToClipboardButton = new javax.swing.JButton();
        jChangeArrivalButton = new javax.swing.JButton();
        jMarkAllButton = new javax.swing.JButton();
        jDrawMarkedButton = new javax.swing.JButton();
        jFlipMarkButton = new javax.swing.JButton();
        jNotDrawMarkedButton = new javax.swing.JButton();
        jNotifyButton = new javax.swing.JToggleButton();
        jActiveAttackPlan = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jRemoveAttackButton1 = new javax.swing.JButton();
        jAttackFrameAlwaysOnTop = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jSelectionFilterDialog.setTitle(bundle.getString("DSWorkbenchAttackFrame.jSelectionFilterDialog.title")); // NOI18N
        jSelectionFilterDialog.setAlwaysOnTop(true);
        jSelectionFilterDialog.setModal(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("DSWorkbenchAttackFrame.jPanel1.border.title"))); // NOI18N

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
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                            .addComponent(jSourceTribeBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 375, Short.MAX_VALUE)))
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

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("DSWorkbenchAttackFrame.jPanel2.border.title"))); // NOI18N

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jMinuteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jHourField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMinuteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jHourField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDayField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addComponent(jArriveDateField, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
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
        jModifyArrivalOption.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireModifyTimeChangedEvent(evt);
            }
        });

        buttonGroup1.add(jMoveTimeOption);
        jMoveTimeOption.setSelected(true);
        jMoveTimeOption.setText(bundle.getString("DSWorkbenchAttackFrame.jMoveTimeOption.text")); // NOI18N
        jMoveTimeOption.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jMoveTimeOption.toolTipText")); // NOI18N
        jMoveTimeOption.setOpaque(false);
        jMoveTimeOption.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireModifyTimeChangedEvent(evt);
            }
        });

        javax.swing.GroupLayout jTimeChangeDialogLayout = new javax.swing.GroupLayout(jTimeChangeDialog.getContentPane());
        jTimeChangeDialog.getContentPane().setLayout(jTimeChangeDialogLayout);
        jTimeChangeDialogLayout.setHorizontalGroup(
            jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTimeChangeDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTimeChangeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jMoveToPlanDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton9)
                    .addComponent(jButton10))
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

        jRemoveAttackButton.setBackground(new java.awt.Color(239, 235, 223));
        jRemoveAttackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jRemoveAttackButton.setText(bundle.getString("DSWorkbenchAttackFrame.jRemoveAttackButton.text")); // NOI18N
        jRemoveAttackButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jRemoveAttackButton.toolTipText")); // NOI18N
        jRemoveAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveAttackEvent(evt);
            }
        });

        jCheckAttacksButton.setBackground(new java.awt.Color(239, 235, 223));
        jCheckAttacksButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_validate.png"))); // NOI18N
        jCheckAttacksButton.setText(bundle.getString("DSWorkbenchAttackFrame.jCheckAttacksButton.text")); // NOI18N
        jCheckAttacksButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCheckAttacksButton.toolTipText")); // NOI18N
        jCheckAttacksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireValidateAttacksEvent(evt);
            }
        });

        jSendAttackButton.setBackground(new java.awt.Color(239, 235, 223));
        jSendAttackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_browser.png"))); // NOI18N
        jSendAttackButton.setText(bundle.getString("DSWorkbenchAttackFrame.jSendAttackButton.text")); // NOI18N
        jSendAttackButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jSendAttackButton.toolTipText")); // NOI18N
        jSendAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSendAttackEvent(evt);
            }
        });

        jMarkFilteredButton.setBackground(new java.awt.Color(239, 235, 223));
        jMarkFilteredButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectSome.png"))); // NOI18N
        jMarkFilteredButton.setText(bundle.getString("DSWorkbenchAttackFrame.jMarkAllButton.text")); // NOI18N
        jMarkFilteredButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jMarkAllButton.toolTipText")); // NOI18N
        jMarkFilteredButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectFilteredEvent(evt);
            }
        });

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

        jChangeArrivalButton.setBackground(new java.awt.Color(239, 235, 223));
        jChangeArrivalButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_changeTime.png"))); // NOI18N
        jChangeArrivalButton.setText(bundle.getString("DSWorkbenchAttackFrame.jChangeArrivalButton.text")); // NOI18N
        jChangeArrivalButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jChangeArrivalButton.toolTipText")); // NOI18N
        jChangeArrivalButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeTimesEvent(evt);
            }
        });

        jMarkAllButton.setBackground(new java.awt.Color(239, 235, 223));
        jMarkAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectAllOrNone.gif"))); // NOI18N
        jMarkAllButton.setText(bundle.getString("DSWorkbenchAttackFrame.jButton1.text")); // NOI18N
        jMarkAllButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jButton1.toolTipText")); // NOI18N
        jMarkAllButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMarkAllEvent(evt);
            }
        });

        jDrawMarkedButton.setBackground(new java.awt.Color(239, 235, 223));
        jDrawMarkedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectDraw.png"))); // NOI18N
        jDrawMarkedButton.setText(bundle.getString("DSWorkbenchAttackFrame.jButton2.text")); // NOI18N
        jDrawMarkedButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jDrawMarkedButton.toolTipText")); // NOI18N
        jDrawMarkedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDrawSelectedEvent(evt);
            }
        });

        jFlipMarkButton.setBackground(new java.awt.Color(239, 235, 223));
        jFlipMarkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectInv.gif"))); // NOI18N
        jFlipMarkButton.setText(bundle.getString("DSWorkbenchAttackFrame.jFlipMarkButton.text")); // NOI18N
        jFlipMarkButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jFlipMarkButton.toolTipText")); // NOI18N
        jFlipMarkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFlipMarkEvent(evt);
            }
        });

        jNotDrawMarkedButton.setBackground(new java.awt.Color(239, 235, 223));
        jNotDrawMarkedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_selectNoDraw.png"))); // NOI18N
        jNotDrawMarkedButton.setText(bundle.getString("DSWorkbenchAttackFrame.jNotDrawMarkedButton.text")); // NOI18N
        jNotDrawMarkedButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jNotDrawMarkedButton.toolTipText")); // NOI18N
        jNotDrawMarkedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDrawSelectedEvent(evt);
            }
        });

        jNotifyButton.setBackground(new java.awt.Color(239, 235, 223));
        jNotifyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_alert.png"))); // NOI18N
        jNotifyButton.setText(bundle.getString("DSWorkbenchAttackFrame.jNotifyButton.text")); // NOI18N
        jNotifyButton.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jNotifyButton.toolTipText")); // NOI18N
        jNotifyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeNotifyEvent(evt);
            }
        });

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

        jRemoveAttackButton1.setBackground(new java.awt.Color(239, 235, 223));
        jRemoveAttackButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/replace2.png"))); // NOI18N
        jRemoveAttackButton1.setText(bundle.getString("DSWorkbenchAttackFrame.jRemoveAttackButton1.text")); // NOI18N
        jRemoveAttackButton1.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jRemoveAttackButton1.toolTipText")); // NOI18N
        jRemoveAttackButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveAttacksEvent(evt);
            }
        });

        javax.swing.GroupLayout jAttackPanelLayout = new javax.swing.GroupLayout(jAttackPanel);
        jAttackPanel.setLayout(jAttackPanelLayout);
        jAttackPanelLayout.setHorizontalGroup(
            jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
                    .addGroup(jAttackPanelLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(jActiveAttackPlan, 0, 394, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jCheckAttacksButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jRemoveAttackButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jRemoveAttackButton1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jNotifyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jNotDrawMarkedButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSendAttackButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jMarkAllButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jMarkFilteredButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jFlipMarkButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jDrawMarkedButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jChangeArrivalButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCopyUnformattedToClipboardButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCopyBBCodeToClipboardButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                .addContainerGap())
        );
        jAttackPanelLayout.setVerticalGroup(
            jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(jActiveAttackPlan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jAttackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                    .addGroup(jAttackPanelLayout.createSequentialGroup()
                        .addComponent(jCheckAttacksButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRemoveAttackButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRemoveAttackButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSendAttackButton)
                        .addGap(30, 30, 30)
                        .addComponent(jMarkAllButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMarkFilteredButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFlipMarkButton)
                        .addGap(30, 30, 30)
                        .addComponent(jChangeArrivalButton)
                        .addGap(30, 30, 30)
                        .addComponent(jDrawMarkedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jNotDrawMarkedButton)
                        .addGap(30, 30, 30)
                        .addComponent(jCopyUnformattedToClipboardButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCopyBBCodeToClipboardButton)
                        .addGap(30, 30, 30)
                        .addComponent(jNotifyButton)))
                .addContainerGap())
        );

        jAttackFrameAlwaysOnTop.setText(bundle.getString("DSWorkbenchAttackFrame.jAttackFrameAlwaysOnTop.text")); // NOI18N
        jAttackFrameAlwaysOnTop.setOpaque(false);
        jAttackFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAttackFrameOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jAttackFrameAlwaysOnTop)
                    .addComponent(jAttackPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 691, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jAttackPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAttackFrameAlwaysOnTop, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    UIManager.put("OptionPane.noButtonText", "Nein");
    UIManager.put("OptionPane.yesButtonText", "Ja");
    int res = JOptionPane.showConfirmDialog(this, message, "Angriff entfernen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    UIManager.put("OptionPane.noButtonText", "No");
    UIManager.put("OptionPane.yesButtonText", "Yes");
    if (res != JOptionPane.YES_OPTION) {
        return;
    }

    jAttackTable.editingCanceled(new ChangeEvent(this));

    for (int r = rows.length - 1; r >= 0; r--) {
        jAttackTable.invalidate();
        int row = jAttackTable.convertRowIndexToModel(rows[r]);
        AttackManagerTableModel.getSingleton().removeRow(row);
        jAttackTable.revalidate();
    }
    jAttackTable.updateUI();
}//GEN-LAST:event_fireRemoveAttackEvent

private void fireValidateAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireValidateAttacksEvent
    Hashtable<Integer, String> errors = new Hashtable<Integer, String>();
    for (int i = 0; i < AttackManagerTableModel.getSingleton().getRowCount(); i++) {
        Date sendTime = (Date) AttackManagerTableModel.getSingleton().getValueAt(i, 3);
        Date arriveTime = (Date) AttackManagerTableModel.getSingleton().getValueAt(i, 4);
        if (arriveTime.getTime() < System.currentTimeMillis()) {
            errors.put(i, "Ankunftzeit liegt in der Vergangenheit");
        } else if (sendTime.getTime() < System.currentTimeMillis()) {
            errors.put(i, "Abschickzeit liegt in der Vergangenheit");
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
        JOptionPane.showMessageDialog(this, message, "Fehler", JOptionPane.WARNING_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this, "Keine Fehler gefunden", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}//GEN-LAST:event_fireValidateAttacksEvent

private void fireSendAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSendAttackEvent
    int selectedRow = jAttackTable.getSelectedRow();
    if (selectedRow < 0) {
        return;
    }
    selectedRow = jAttackTable.convertRowIndexToModel(selectedRow);
    Village source = (Village) AttackManagerTableModel.getSingleton().getValueAt(selectedRow, 0);
    Village target = (Village) AttackManagerTableModel.getSingleton().getValueAt(selectedRow, 1);
    BrowserCommandSender.sendTroops(source, target);
}//GEN-LAST:event_fireSendAttackEvent

private void fireAttackFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAttackFrameOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireAttackFrameOnTopEvent

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
    jAttackTable.updateUI();
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

                String sendtime = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(sTime);
                String arrivetime = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(aTime);
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
            JOptionPane.showMessageDialog(this, result, "Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
        }
    } catch (Exception e) {
        logger.error("Failed to copy data to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPane.showMessageDialog(this, result, "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_fireCopyUnformatedToClipboardEvent

private void fireCopyAsBBCodeToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyAsBBCodeToClipboardEvent
    try {
        UIManager.put("OptionPane.noButtonText", "Nein");
        UIManager.put("OptionPane.yesButtonText", "Ja");
        boolean extended = (JOptionPane.showConfirmDialog(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION);
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
            for (int i : rows) {
                jAttackTable.invalidate();
                int row = jAttackTable.convertRowIndexToModel(i);
                Village sVillage = attacks.get(row).getSource();
                Village tVillage = attacks.get(row).getTarget();
                UnitHolder sUnit = attacks.get(row).getUnit();
                Date aTime = attacks.get(row).getArriveTime();
                Date sTime = new Date(aTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(sVillage, tVillage, sUnit.getSpeed()) * 1000));
                String sendtime = null;
                String arrivetime = null;
                if (extended) {
                    sendtime = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(sTime);
                    arrivetime = new SimpleDateFormat("'[color=green]'dd.MM.yy 'um' HH:mm:ss.'[size=8]'SSS'[/size][/color]'").format(aTime);
                } else {
                    sendtime = new SimpleDateFormat("'[color=red]'dd.MM.yy 'um' HH:mm:ss.SSS'[/color]'").format(sTime);
                    arrivetime = new SimpleDateFormat("'[color=green]'dd.MM.yy 'um' HH:mm:ss.SSS'[/color]'").format(aTime);
                }
                buffer.append("Angriff von ");
                if (sVillage.getTribe() != null) {
                    buffer.append(sVillage.getTribe().toBBCode());
                    buffer.append(" aus ");
                } else {
                    buffer.append(" Barbaren aus ");
                }

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
                    buffer.append(" in ");
                } else {
                    buffer.append(" Barbaren in ");
                }

                buffer.append(tVillage.toBBCode());
                buffer.append(" startet am ");
                buffer.append(sendtime);
                buffer.append(" und kommt am ");
                buffer.append(arrivetime);
                buffer.append(" an\n");
                jAttackTable.revalidate();
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
            JOptionPane.showMessageDialog(this, result, "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (Exception e) {
        logger.error("Failed to copy data to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPane.showMessageDialog(this, result, "Fehler", JOptionPane.ERROR_MESSAGE);
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
            jSourceVillageTable.updateUI();
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
        jTargetVillageTable.updateUI();
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
            jSourceVillageTable.updateUI();
        }

    } else if (evt.getSource() == jNoSourceVillageButton) {
        //update source table values that all villages are unselected
        jSourceVillageTable.invalidate();
        for (int i = 0; i < jSourceVillageTable.getRowCount(); i++) {
            jSourceVillageTable.getModel().setValueAt(Boolean.FALSE, i, 1);
        }

        jSourceVillageTable.revalidate();
        jSourceVillageTable.updateUI();
    } else if (evt.getSource() == jAllTargetVillageButton) {
        //update target table values that all villages are selected
        if (jTargetVillageTable.getRowCount() > 0) {
            jTargetVillageTable.invalidate();
            for (int i = 0; i <
                    jTargetVillageTable.getRowCount(); i++) {
                jTargetVillageTable.getModel().setValueAt(Boolean.TRUE, i, 1);
            }

            jTargetVillageTable.revalidate();
            jTargetVillageTable.updateUI();
        }

    } else if (evt.getSource() == jNoTargetVillageButton) {
        //update target table values that all villages are unselected
        jTargetVillageTable.invalidate();
        for (int i = 0; i <
                jTargetVillageTable.getRowCount(); i++) {
            jTargetVillageTable.getModel().setValueAt(Boolean.FALSE, i, 1);
        }

        jTargetVillageTable.revalidate();
        jTargetVillageTable.updateUI();
    }
}//GEN-LAST:event_fireSelectNoneAllEvent

private void fireCloseTimeChangeDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseTimeChangeDialogEvent
    if (evt.getSource() == jOKButton) {
        int[] rows = jAttackTable.getSelectedRows();
        if ((rows != null) || (rows.length > 0)) {
            if (jMoveTimeOption.isSelected()) {
                Integer min = (Integer) jMinuteField.getValue();
                Integer hour = (Integer) jHourField.getValue();
                Integer day = (Integer) jDayField.getValue();
                //jArriveDateField.getValue()
                List<Attack> attacks = AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan());
                jAttackTable.invalidate();
                for (int i : rows) {
                    int row = jAttackTable.convertRowIndexToModel(i);
                    long arrive = attacks.get(row).getArriveTime().getTime();
                    long diff = min * 60000 + hour * 3600000 + day * 86400000;
                    //later if first index is selected
                    //if later, add diff to arrival, else remove diff from arrival
                    arrive +=
                            diff;
                    AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan()).get(row).setArriveTime(new Date(arrive));
                }

                jAttackTable.revalidate();
                jAttackTable.updateUI();
            } else {
                Date arrive = (Date) jArriveDateField.getValue();
                jAttackTable.invalidate();
                for (int i : rows) {
                    int row = jAttackTable.convertRowIndexToModel(i);
                    //later if first index is selected
                    //if later, add diff to arrival, else remove diff from arrival
                    AttackManager.getSingleton().getAttackPlan(AttackManagerTableModel.getSingleton().getActiveAttackPlan()).get(row).setArriveTime(arrive);
                }

                jAttackTable.revalidate();
                jAttackTable.updateUI();
            }

        }
    }

    jTimeChangeDialog.setVisible(false);
}//GEN-LAST:event_fireCloseTimeChangeDialogEvent

private void fireChangeTimesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeTimesEvent
    int[] rows = jAttackTable.getSelectedRows();
    if ((rows == null) || (rows.length <= 0)) {
        JOptionPane.showMessageDialog(this, "Keine Angriffe markiert", "Information", JOptionPane.INFORMATION_MESSAGE);
        return;

    }
    jTimeChangeDialog.setVisible(true);
}//GEN-LAST:event_fireChangeTimesEvent

private void fireModifyTimeChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireModifyTimeChangedEvent
    boolean moveMode = true;
    if (evt.getSource() == jModifyArrivalOption) {
        moveMode = false;
    }

    jLabel5.setEnabled(moveMode);
    jLabel6.setEnabled(moveMode);
    jLabel7.setEnabled(moveMode);
    jMinuteField.setEnabled(moveMode);
    jHourField.setEnabled(moveMode);
    jDayField.setEnabled(moveMode);
    jLabel8.setEnabled(!moveMode);
    jArriveDateField.setEnabled(!moveMode);
}//GEN-LAST:event_fireModifyTimeChangedEvent

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
        JOptionPane.showMessageDialog(this, "Der Standardplan kann nicht umbenannt werden.", "Information", JOptionPane.INFORMATION_MESSAGE);
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
        JOptionPane.showMessageDialog(this, "Der Standardplan kann nicht gelöscht werden.");
        return;
    }

    if (JOptionPane.showConfirmDialog(this, "Willst du den Angriffsplan '" + selection + "' und alle enthaltenen Angriffe\n" +
            "wirklich löschen?", "Angriffsplan löschen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
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

        jAttackTable.updateUI();
        jAttackTable.revalidate();
    }
}//GEN-LAST:event_fireActiveAttackChangedEvent

private void fireAddNewAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddNewAttackPlanEvent
    String name = jAttackPlanName.getText();
    if (AttackManager.getSingleton().getAttackPlan(name) != null) {
        JOptionPane.showMessageDialog(jAddPlanDialog, "Ein Plan mit dem angegebenen Namen existiert bereits.\n" +
                "Bitte wähle einen anderen Namen oder lösche zuerst den bestehenden Plan.", "Warnung", JOptionPane.WARNING_MESSAGE);
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
        JOptionPane.showMessageDialog(jRenamePlanDialog, "Ein Plan mit dem Namen '" + newName + "' existiert bereits.\n" +
                "Bitte wähle einen anderen Namen oder lösche zuerst den bestehenden Plan.", "Warnung", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(jMoveToPlanDialog, "Kein neuer Plan ausgewählt", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int[] rows = jAttackTable.getSelectedRows();
        if ((rows != null) && (rows.length > 0)) {
            List<Attack> sourcePlan = AttackManager.getSingleton().getAttackPlan(oldPlan);
            List<Attack> targetPlan = AttackManager.getSingleton().getAttackPlan(newPlan);
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
        jAttackTable.setDefaultRenderer(Date.class,
                new DateCellRenderer());


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
            jAttackTable.updateUI();
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
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JButton jChangeArrivalButton;
    private javax.swing.JButton jCheckAttacksButton;
    private javax.swing.JButton jCopyBBCodeToClipboardButton;
    private javax.swing.JButton jCopyUnformattedToClipboardButton;
    private javax.swing.JTextField jCurrentPlanBox;
    private javax.swing.JSpinner jDayField;
    private javax.swing.JButton jDrawMarkedButton;
    private javax.swing.JButton jFlipMarkButton;
    private javax.swing.JSpinner jHourField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JRadioButton jMoveTimeOption;
    private javax.swing.JDialog jMoveToPlanDialog;
    private javax.swing.JComboBox jNewPlanBox;
    private javax.swing.JTextField jNewPlanName;
    private javax.swing.JButton jNoSourceVillageButton;
    private javax.swing.JButton jNoTargetVillageButton;
    private javax.swing.JButton jNotDrawMarkedButton;
    private javax.swing.JToggleButton jNotifyButton;
    private javax.swing.JButton jOKButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton jRemoveAttackButton;
    private javax.swing.JButton jRemoveAttackButton1;
    private javax.swing.JDialog jRenamePlanDialog;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JDialog jSelectionFilterDialog;
    private javax.swing.JButton jSendAttackButton;
    private javax.swing.JComboBox jSourceTribeBox;
    private javax.swing.JTable jSourceVillageTable;
    private javax.swing.JComboBox jTargetTribeBox;
    private javax.swing.JTable jTargetVillageTable;
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
                            long sendTime = a.getArriveTime().getTime() - (long) DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000;
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

//</editor-fold>