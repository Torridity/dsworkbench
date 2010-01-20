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
import de.tor.tribes.ui.renderer.TribeCellRenderer;
import de.tor.tribes.ui.renderer.VillageCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.report.ReportManagerListener;
import de.tor.tribes.util.report.ReportStatBuilder;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class DSWorkbenchReportFrame extends AbstractDSWorkbenchFrame implements ReportManagerListener {

    private static Logger logger = Logger.getLogger("ReportView");
    private static DSWorkbenchReportFrame SINGLETON = null;
    private List<DefaultTableCellRenderer> renderers = new LinkedList<DefaultTableCellRenderer>();
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

        for (int i = 0; i < jReportTable.getColumnCount(); i++) {
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
            jReportTable.getColumn(jReportTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
            renderers.add(headerRenderer);
        }

        jList1.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireAllySelectionChangedEvent();
                }
            }
        });

        jList2.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireTribeSelectionChangedEvent();
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
        jReportTable.getColumnModel().getColumn(0).setMinWidth(20);
        jReportTable.getColumnModel().getColumn(0).setWidth(20);
        jReportTable.getColumnModel().getColumn(0).setMaxWidth(20);
        jReportTable.getColumnModel().getColumn(0).setResizable(false);

        ReportManager.getSingleton().forceUpdate(null);
        buildReportSetList();
        jReportSetBox.setSelectedItem(ReportManager.DEFAULT_SET);
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
        jLabel8 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
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
        jButton6 = new javax.swing.JButton();
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
        jButton10.setText("Erstellen");
        jButton10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoCreateStatsEvent(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Ergebnisse"));
        jPanel3.setOpaque(false);

        jLabel7.setText("Teilnehmer (Stämme)");

        jScrollPane3.setMaximumSize(new java.awt.Dimension(140, 130));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(140, 130));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(140, 130));

        jScrollPane3.setViewportView(jList1);

        jLabel8.setText("Teilnehmer (Spieler)");

        jScrollPane4.setMaximumSize(new java.awt.Dimension(140, 130));
        jScrollPane4.setMinimumSize(new java.awt.Dimension(140, 130));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(140, 130));

        jScrollPane4.setViewportView(jList2);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Auswertung"));
        jPanel4.setOpaque(false);

        jLabel9.setText("Angriffe");

        jLabel10.setText("Fakes");

        jTextField1.setEditable(false);
        jTextField1.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField1.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField1.setPreferredSize(new java.awt.Dimension(60, 20));

        jTextField2.setEditable(false);
        jTextField2.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField2.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField2.setPreferredSize(new java.awt.Dimension(60, 20));

        jLabel11.setText("AG");

        jTextField3.setEditable(false);
        jTextField3.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField3.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField3.setPreferredSize(new java.awt.Dimension(60, 20));

        jLabel13.setText("Besiegt");

        jTextField5.setEditable(false);
        jTextField5.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField5.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField5.setPreferredSize(new java.awt.Dimension(60, 20));

        jLabel14.setText("Verloren");

        jTextField6.setEditable(false);
        jTextField6.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField6.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField6.setPreferredSize(new java.awt.Dimension(60, 20));

        jLabel15.setText("Geadelt");

        jTextField7.setEditable(false);
        jTextField7.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField7.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField7.setPreferredSize(new java.awt.Dimension(60, 20));

        jLabel16.setText("Wall");

        jTextField8.setEditable(false);
        jTextField8.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField8.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField8.setPreferredSize(new java.awt.Dimension(60, 20));

        jLabel17.setText("Gebäude");

        jTextField9.setEditable(false);
        jTextField9.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField9.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField9.setPreferredSize(new java.awt.Dimension(60, 20));

        jTextField10.setEditable(false);
        jTextField10.setMaximumSize(new java.awt.Dimension(60, 20));
        jTextField10.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextField10.setPreferredSize(new java.awt.Dimension(60, 20));

        jLabel18.setText("Stufen");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel13)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(166, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
                    .addComponent(jButton10))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton10)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/medal.png"))); // NOI18N
        jButton6.setToolTipText("Statistiken erzeugen");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCreateStatsEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton6);

        jTaskPane1.add(jTaskPaneGroup1);

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
        jList2.setModel(new DefaultListModel());

    }//GEN-LAST:event_fireDoCreateStatsEvent

    private void fireAllySelectionChangedEvent() {
        Object[] selection = jList1.getSelectedValues();
        DefaultListModel model = new DefaultListModel();
        int wall = 0;
        int building = 0;
        int kills = 0;
        int silentKills = 0;
        int killsAsFarm = 0;
        int deaths = 0;
        int deathsAsFarm = 0;
        int tribes = 0;
        int defAllies = lastStats.getDefendingAllies().length;
        int defTribes = lastStats.getDefendingTribes().length;

        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumFractionDigits(0);
        f.setMaximumFractionDigits(0);
        System.out.println("BorderConditions");
        System.out.println("----------------");
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
        System.out.println("Start: " + df.format(lastStats.getStartDate()));
        System.out.println("End: " + df.format(lastStats.getEndDate()));
        System.out.println("Reports: " + lastStats.getReportCount());
        System.out.println("====================");
        System.out.println("");
        System.out.println("AllyStats");
        System.out.println("---------");
        for (Object o : selection) {
            Ally a = (Ally) o;
            System.out.println("AllyStats (" + a + ")");
            System.out.println("-----------------------");
            int allyWall = 0;
            int allyBuilding = 0;
            int allyKills = 0;
            int allySilentKills = 0;
            int allyKillsAsFarm = 0;
            int allyDeaths = 0;
            int allyDeathsAsFarm = 0;
            int allyTribes = 0;
            int allyAttacks = 0;
            for (Tribe t : lastStats.getAttackingTribes(a)) {
                tribes++;
                allyTribes++;
                model.addElement(t);
                SingleAttackerStat stats = lastStats.getStatsForTribe(t);
                allyAttacks += stats.getOffCount() + stats.getSnobAttackCount() + stats.getSimpleSnobAttackCount() + stats.getFakeCount();
                wall += stats.getDestroyedWallLevels();
                allyWall += stats.getDestroyedWallLevels();
                building += stats.getSummedDestroyedBuildings();
                allyBuilding += stats.getSummedDestroyedBuildings();
                kills += stats.getSummedKills();
                allyKills += stats.getSummedKills();
                silentKills += stats.getSummedSilentKills();
                allySilentKills += stats.getSummedSilentKills();
                kills += stats.getAtLeast2KDamageCount() * 2000;
                kills += stats.getAtLeast4KDamageCount() * 4000;
                kills += stats.getAtLeast6KDamageCount() * 6000;
                kills += stats.getAtLeast8KDamageCount() * 8000;
                allyKills += stats.getAtLeast2KDamageCount() * 2000;
                allyKills += stats.getAtLeast4KDamageCount() * 4000;
                allyKills += stats.getAtLeast6KDamageCount() * 6000;
                allyKills += stats.getAtLeast8KDamageCount() * 8000;
                deaths += stats.getSummedLosses();
                allyDeaths += stats.getSummedLosses();
                killsAsFarm += stats.getSummedKillsAsFarmSpace();
                killsAsFarm += stats.getAtLeast2KDamageCount() * 2000 * 1.5;
                killsAsFarm += stats.getAtLeast4KDamageCount() * 4000 * 1.5;
                killsAsFarm += stats.getAtLeast6KDamageCount() * 6000 * 1.5;
                killsAsFarm += stats.getAtLeast8KDamageCount() * 8000 * 1.5;
                allyKillsAsFarm += stats.getSummedKillsAsFarmSpace();
                allyKillsAsFarm += stats.getAtLeast2KDamageCount() * 2000 * 1.5;
                allyKillsAsFarm += stats.getAtLeast4KDamageCount() * 4000 * 1.5;
                allyKillsAsFarm += stats.getAtLeast6KDamageCount() * 6000 * 1.5;
                allyKillsAsFarm += stats.getAtLeast8KDamageCount() * 8000 * 1.5;
                deathsAsFarm += stats.getSummedLossesAsFarmSpace();
                allyDeathsAsFarm += stats.getSummedLossesAsFarmSpace();
                System.out.println("  TribeStats (" + t + ")");
                System.out.println("  -----------------------");
                System.out.println("    * Attacks: " + (stats.getOffCount() + stats.getSnobAttackCount() + stats.getSimpleSnobAttackCount()));
                System.out.println("    * Off/AG: " + stats.getOffCount() + "/" + (stats.getSnobAttackCount() + stats.getSimpleSnobAttackCount()));
                System.out.println("    * Enoblements: " + stats.getEnoblementCount());
                System.out.println("    * Losses: " + f.format(stats.getSummedLosses()) + " (" + f.format(stats.getSummedLossesAsFarmSpace()) + ")");
                System.out.println("    * KnownKills: " + f.format(stats.getSummedKills()) + " (" + f.format(stats.getSummedKillsAsFarmSpace()) + ")");
                System.out.println("      * Silent: " + f.format(stats.getSummedSilentKills()) + " (" + f.format(stats.getSummedSilentKillsAsFarmSpace()) + ")");
                int unknown = stats.getAtLeast2KDamageCount() * 2000 + stats.getAtLeast4KDamageCount() * 4000 + stats.getAtLeast6KDamageCount() * 6000 + stats.getAtLeast8KDamageCount() * 8000;
                System.out.println("    * ApproxKills: " + f.format(unknown));
                System.out.println("    * UnknownKills: " + stats.getUnknownDamageCount());
                System.out.println("    * WallDestruction: " + stats.getDestroyedWallLevels());
                System.out.println("    * BuildingDestruction: " + stats.getSummedDestroyedBuildings());
            }
            System.out.println("============");
            System.out.println("OverallAlly");
            System.out.println(" + Tribes: " + allyTribes);
            System.out.println(" + Attacks: " + allyAttacks);
            System.out.println(" + Kills: " + f.format(allyKills) + " (" + f.format(allyKillsAsFarm) + ")");
            System.out.println("   + Silent: " + f.format(allySilentKills));
            System.out.println(" + Deaths: " + f.format(allyDeaths) + " (" + f.format(allyDeathsAsFarm) + ")");
            System.out.println(" + WallDestruction: " + f.format(allyWall));
            System.out.println(" + BuildingDestruction: " + f.format(allyBuilding));

        }
        System.out.println("");
        System.out.println("====================");

        System.out.println("OverallStats");
        System.out.println("- Attackers: " + tribes + " (" + selection.length + ")");
        System.out.println("- Defenders: " + defTribes + " (" + defAllies + ")");
        System.out.println("- Kills: " + f.format(kills) + " (" + f.format(killsAsFarm) + ")");
        System.out.println("  - Silent: " + f.format(silentKills));
        System.out.println("- Deaths: " + f.format(deaths) + " (" + f.format(deathsAsFarm) + ")");
        System.out.println("- Death/Attacker: " + f.format((deaths / tribes)));
        System.out.println("- Death/Defender: " + f.format((kills / defTribes)));
        System.out.println("- WallDestruction: " + f.format(wall));
        System.out.println("- BuildingDestruction: " + f.format(building));
        jList2.setModel(model);
    }

    private void fireTribeSelectionChangedEvent() {
        Tribe t = (Tribe) jList2.getSelectedValue();
        if (t == null) {
            return;
        }
        SingleAttackerStat stats = lastStats.getStatsForTribe(t);
        jTextField1.setText("" + stats.getOffCount());
        jTextField2.setText("" + stats.getFakeCount());
        jTextField3.setText("" + stats.getSnobAttackCount() + stats.getSimpleSnobAttackCount());
        jTextField5.setText("" + stats.getSummedKills());
        jTextField6.setText("" + stats.getSummedLosses());
        jTextField7.setText("" + stats.getEnoblementCount());
        jTextField8.setText("" + stats.getDestroyedWallLevels());
        int buildings = 0;
        int levels = 0;
        Enumeration<String> keys = stats.getDestroyedBuildings().keys();
        while (keys.hasMoreElements()) {
            buildings++;
            levels += stats.getDestroyedBuildings().get(keys.nextElement());
        }
        jTextField9.setText("" + buildings);
        jTextField10.setText("" + levels);
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
    private javax.swing.JCheckBox jAlwaysOnTopBox;
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
    private javax.swing.JFrame jCreateStatsFrame;
    private javax.swing.JTextField jCurrentSetField;
    private javax.swing.JButton jDoAddNewSetButton;
    private javax.swing.JButton jDoMoveButton;
    private javax.swing.JButton jDoRenameButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JDialog jMoveToSetDialog;
    private javax.swing.JTextField jNewReportSetField;
    private javax.swing.JComboBox jNewSetBox;
    private javax.swing.JTextField jNewSetNameField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JDialog jRenameReportSetDialog;
    private javax.swing.JComboBox jReportSetBox;
    private javax.swing.JList jReportSetsForStatsList;
    private javax.swing.JTable jReportTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private com.l2fprod.common.swing.JTaskPane jTaskPane1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireReportsChangedEvent(String pPlan) {
        try {
            jReportTable.invalidate();
            for (int i = 0; i < jReportTable.getColumnCount(); i++) {
                jReportTable.getColumn(jReportTable.getColumnName(i)).setHeaderRenderer(renderers.get(i));
            }
            jReportTable.revalidate();
            jReportTable.repaint();
        } catch (Exception e) {
            logger.error("Failed to update attacks table", e);
        }
    }
}
