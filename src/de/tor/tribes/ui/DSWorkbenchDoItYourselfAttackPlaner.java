/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchDoItYourselflAttackPlaner.java
 *
 * Created on Nov 25, 2009, 10:27:45 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.AttackTypeCellEditor;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.models.DoItYourselfAttackTableModel;
import de.tor.tribes.ui.renderer.AlternatingColorCellRenderer;
import de.tor.tribes.ui.renderer.AttackTypeCellRenderer;
import de.tor.tribes.ui.renderer.AttackTypeListCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.UnitListCellRenderer;
import de.tor.tribes.ui.renderer.VillageCellRenderer;
import de.tor.tribes.util.AttackToBBCodeFormater;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.html.AttackPlanHTMLExporter;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultFormatter;
import org.apache.log4j.Logger;

/**
 * @author Torridity
 */
public class DSWorkbenchDoItYourselfAttackPlaner extends AbstractDSWorkbenchFrame {

    private static Logger logger = Logger.getLogger("DoItYourselflAttackPlaner");
    private static DSWorkbenchDoItYourselfAttackPlaner SINGLETON = null;
    private TableCellRenderer mHeaderRenderer = null;

    /** Creates new form DSWorkbenchDoItYourselflAttackPlaner */
    DSWorkbenchDoItYourselfAttackPlaner() {
        initComponents();
        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("doityourself.attack.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        mHeaderRenderer = new SortableTableHeaderRenderer();

        fireRebuildTableEvent();
        // jAttackTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        jAttackTable.setDefaultEditor(Village.class, new DefaultCellEditor(new JTextField("")));
        jAttackTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        jAttackTable.setDefaultEditor(Date.class, new DateSpinEditor());
        jAttackTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jAttackTable.setDefaultRenderer(String.class, new AlternatingColorCellRenderer());
        jAttackTable.setDefaultEditor(Integer.class, new AttackTypeCellEditor());
        jAttackTable.setDefaultRenderer(Integer.class, new AttackTypeCellRenderer());
        jAttackTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = jAttackTable.getSelectedRows().length;
                if (selected == 0) {
                    setTitle("Manueller Angriffsplaner");
                } else if (selected == 1) {
                    setTitle("Angriffe (1 Angriff ausgewählt)");
                } else if (selected > 1) {
                    setTitle("Angriffe (" + selected + " Angriffe ausgewählt)");
                }
            }
        });

        jArriveTime.setDate(Calendar.getInstance().getTime());
        jNewArriveSpinner.setDate(Calendar.getInstance().getTime());
        MouseListener l = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON2) {
                    DoItYourselfAttackTableModel.getSingleton().getPopup().show(jAttackTable, e.getX(), e.getY());
                    DoItYourselfAttackTableModel.getSingleton().getPopup().requestFocusInWindow();
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

        jAttackTable.addMouseListener(l);
        jScrollPane1.addMouseListener(l);

        DoItYourselfCountdownThread thread = new DoItYourselfCountdownThread();
        thread.start();

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.manual_attack_planer", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
        pack();
    }

    public static synchronized DSWorkbenchDoItYourselfAttackPlaner getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchDoItYourselfAttackPlaner();
        }
        return SINGLETON;
    }

    public void resetView() {
        jAttackTable.invalidate();
        //setup renderer and general view
        DoItYourselfAttackTableModel.getSingleton().clear();
        DoItYourselfAttackTableModel.getSingleton().resetRowSorter(jAttackTable);
        DoItYourselfAttackTableModel.getSingleton().loadColumnState();

        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        for (int i = 0; i < jAttackTable.getColumnCount(); i++) {
            jAttackTable.getColumn(jAttackTable.getColumnName(i)).setHeaderRenderer(mHeaderRenderer);
        }
        jAttackTable.revalidate();
        jAttackTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jAttackTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
        jAttackTable.setRowHeight(24);
        jAttackTable.repaint();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        DefaultComboBoxModel model2 = new DefaultComboBoxModel();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            model.addElement(unit);
            model2.addElement(unit);
        }
        jUnitBox.setModel(model);
        jUnitComboBox.setModel(model2);
        jUnitBox.setSelectedItem(DataHolder.getSingleton().getUnitByPlainName("ram"));
        jUnitComboBox.setSelectedItem(DataHolder.getSingleton().getUnitByPlainName("ram"));
        jUnitBox.setRenderer(new UnitListCellRenderer());
        DefaultComboBoxModel typeModel = new DefaultComboBoxModel();
        jAttackTypeComboBox.setRenderer(new AttackTypeListCellRenderer());
        typeModel.addElement(Attack.NO_TYPE);
        typeModel.addElement(Attack.CLEAN_TYPE);
        typeModel.addElement(Attack.SNOB_TYPE);
        typeModel.addElement(Attack.FAKE_TYPE);
        typeModel.addElement(Attack.FAKE_DEFF_TYPE);
        typeModel.addElement(Attack.SUPPORT_TYPE);
        jAttackTypeComboBox.setModel(typeModel);

        jUnitComboBox.setRenderer(new UnitListCellRenderer());

        if (ServerSettings.getSingleton().getCoordType() != 2) {
            jSourceVillage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new DefaultFormatter()));
            jSourceVillage.setText("00:00:00");
            jTargetVillage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new DefaultFormatter()));
            jTargetVillage.setText("00:00:00");
        } else {
            try {
                jSourceVillage.setText("000|000");
                jSourceVillage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###|###")));
                jTargetVillage.setText("000|000");
                jTargetVillage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###|###")));
            } catch (java.text.ParseException ex) {
            }
        }
        jSourceVillage.updateUI();
        jTargetVillage.updateUI();
    }

    protected void updateCountdown() {
        jAttackTable.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTransferToAttackViewDialog = new javax.swing.JDialog();
        jLabel5 = new javax.swing.JLabel();
        jExistingPlansBox = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jNewPlanField = new javax.swing.JTextField();
        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttackTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSourceVillage = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jTargetVillage = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jUnitBox = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jArriveTime = new de.tor.tribes.ui.components.DateTimeField();
        jTaskPane1 = new com.l2fprod.common.swing.JTaskPane();
        jTaskPaneGroup1 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTaskPaneGroup2 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jButton7 = new javax.swing.JButton();
        jUnformattedExport = new javax.swing.JButton();
        jExportAsBBCode = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jAdeptTimeButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jUnitComboBox = new javax.swing.JComboBox();
        jAdeptUnitButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jAttackTypeComboBox = new javax.swing.JComboBox();
        jAdeptTypeButton = new javax.swing.JButton();
        jNewArriveSpinner = new de.tor.tribes.ui.components.DateTimeField();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        jLabel5.setText("Existierender Plan");

        jLabel6.setText("Neuer Plan");

        jOKButton.setText("OK");
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoMoveAttacksToAttackViewEvent(evt);
            }
        });

        jCancelButton.setText("Abbrechen");
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoMoveAttacksToAttackViewEvent(evt);
            }
        });

        javax.swing.GroupLayout jTransferToAttackViewDialogLayout = new javax.swing.GroupLayout(jTransferToAttackViewDialog.getContentPane());
        jTransferToAttackViewDialog.getContentPane().setLayout(jTransferToAttackViewDialogLayout);
        jTransferToAttackViewDialogLayout.setHorizontalGroup(
            jTransferToAttackViewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTransferToAttackViewDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTransferToAttackViewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jTransferToAttackViewDialogLayout.createSequentialGroup()
                        .addGroup(jTransferToAttackViewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jTransferToAttackViewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jNewPlanField, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                            .addComponent(jExistingPlansBox, 0, 290, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jTransferToAttackViewDialogLayout.createSequentialGroup()
                        .addComponent(jCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOKButton)))
                .addContainerGap())
        );
        jTransferToAttackViewDialogLayout.setVerticalGroup(
            jTransferToAttackViewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jTransferToAttackViewDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jTransferToAttackViewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jExistingPlansBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jTransferToAttackViewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jNewPlanField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jTransferToAttackViewDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jOKButton)
                    .addComponent(jCancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle("Manueller Angriffsplaner");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jScrollPane1.setMinimumSize(new java.awt.Dimension(0, 0));

        jAttackTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jAttackTable);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Neuer Angriff"));
        jPanel2.setOpaque(false);

        jLabel1.setText("Herkunft");

        jSourceVillage.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSourceVillage.setToolTipText("Herkunftsdorf");

        jLabel2.setText("Ziel");

        jTargetVillage.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTargetVillage.setToolTipText("Zieldorf");

        jLabel3.setText("Einheit");

        jLabel4.setText("Ankunft");

        jUnitBox.setToolTipText("Langsamste Einheit");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setText("Hinzufügen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addGap(43, 43, 43)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(49, 49, 49)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(jUnitBox, 0, 206, Short.MAX_VALUE)
                    .addComponent(jSourceVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(jTargetVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSourceVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTargetVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap())
        );

        jTaskPane1.setOpaque(false);
        com.l2fprod.common.swing.PercentLayout percentLayout5 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout5.setGap(14);
        percentLayout5.setOrientation(1);
        jTaskPane1.setLayout(percentLayout5);

        jTaskPaneGroup1.setTitle("Editieren");
        com.l2fprod.common.swing.PercentLayout percentLayout6 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout6.setGap(2);
        percentLayout6.setOrientation(1);
        jTaskPaneGroup1.getContentPane().setLayout(percentLayout6);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton2.setToolTipText("Markierte Angriffe löschen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton2);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_copy.png"))); // NOI18N
        jButton3.setToolTipText("Markierte Angriffe kopieren");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jButton3);

        jTaskPane1.add(jTaskPaneGroup1);

        jTaskPaneGroup2.setTitle("Übertragen");
        com.l2fprod.common.swing.PercentLayout percentLayout7 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout7.setGap(2);
        percentLayout7.setOrientation(1);
        jTaskPaneGroup2.getContentPane().setLayout(percentLayout7);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_overview.png"))); // NOI18N
        jButton7.setToolTipText("Markierte Angriffe in Angriffsübersicht einfügen");
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireMoveAttacksToAttackViewEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jButton7);

        jUnformattedExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboard.png"))); // NOI18N
        jUnformattedExport.setToolTipText("Markierte Angriffe unformatiert in die Zwischenablage kopieren");
        jUnformattedExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportAttacksEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jUnformattedExport);

        jExportAsBBCode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        jExportAsBBCode.setToolTipText("Markierte Angriffe als BB-Code in die Zwischenablage kopieren");
        jExportAsBBCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExportAttacksEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jExportAsBBCode);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_HTML.png"))); // NOI18N
        jButton6.setToolTipText("Markierte Angriffe als HTML Datei speichern");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireWriteToHTMLEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jButton6);

        jTaskPane1.add(jTaskPaneGroup2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Anpassen"));
        jPanel3.setOpaque(false);

        jLabel7.setText("Ankunftszeit");

        jAdeptTimeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jAdeptTimeButton.setToolTipText("Ankunftszeit für alle markierten Angriffe anpassen");
        jAdeptTimeButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jAdeptTimeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jAdeptTimeButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jAdeptTimeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAdeptEvent(evt);
            }
        });

        jLabel8.setText("Einheit");

        jAdeptUnitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jAdeptUnitButton.setToolTipText("Einheit für alle markierten Angriffe anpassen");
        jAdeptUnitButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jAdeptUnitButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jAdeptUnitButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jAdeptUnitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAdeptEvent(evt);
            }
        });

        jLabel9.setText("Angriffstyp");

        jAdeptTypeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jAdeptTypeButton.setToolTipText("Angriffstyp für alle markierten Angriffe anpassen");
        jAdeptTypeButton.setMaximumSize(new java.awt.Dimension(25, 25));
        jAdeptTypeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        jAdeptTypeButton.setPreferredSize(new java.awt.Dimension(25, 25));
        jAdeptTypeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAdeptEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jNewArriveSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jAdeptTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jUnitComboBox, 0, 206, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAdeptUnitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jAttackTypeComboBox, 0, 206, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jAdeptTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAdeptTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jNewArriveSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAdeptUnitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jUnitComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jAdeptTypeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jAttackTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(70, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTaskPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTaskPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAttackFrameOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAlwaysOnTopBox))
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

    private void fireAttackFrameOnTopEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAttackFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAttackFrameOnTopEvent

    private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
        String source = jSourceVillage.getText();

        List<Village> sourceList = PluginManager.getSingleton().executeVillageParser(source);
        if (sourceList.isEmpty()) {
            JOptionPaneHelper.showWarningBox(this, "Kein gültiges Herkunftsdorf gewählt.", "Warnung");
            return;
        }
        String target = jTargetVillage.getText();
        List<Village> targetList = PluginManager.getSingleton().executeVillageParser(target);
        if (targetList.isEmpty()) {
            JOptionPaneHelper.showWarningBox(this, "Kein gültiges Zieldorf gewählt.", "Warnung");
            return;
        }
        Date arrive = jArriveTime.getSelectedDate();
        if (arrive.getTime() < System.currentTimeMillis()) {
            JOptionPaneHelper.showWarningBox(this, "Ankunftszeit darf nicht in der Vergangenheit liegen.", "Warnung");
            return;
        }
        UnitHolder unit = (UnitHolder) jUnitBox.getSelectedItem();
        int type = Attack.NO_TYPE;
        if (unit.equals(DataHolder.getSingleton().getUnitByPlainName("snob"))) {
            type = Attack.SNOB_TYPE;
        } else if (unit.equals(DataHolder.getSingleton().getUnitByPlainName("ram"))) {
            type = Attack.CLEAN_TYPE;
        }
        DoItYourselfAttackTableModel.getSingleton().addAttack(sourceList.get(0), targetList.get(0), arrive, unit, type);
        jAttackTable.repaint();
    }//GEN-LAST:event_fireAddAttackEvent

    private void fireRemoveEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveEvent
        int[] rows = jAttackTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            JOptionPaneHelper.showInformationBox(this, "Keine Angriffe ausgewählt", "Information");
            return;
        }
        String message = "";
        if (rows.length == 1) {
            message = "Markierten Angriff wirklich entfernen?";
        } else {
            message = "Markierte Angriffe wirklich entfernen?";
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            for (int r = rows.length - 1; r >= 0; r--) {
                int row = jAttackTable.convertRowIndexToModel(rows[r]);
                jAttackTable.invalidate();
                DoItYourselfAttackTableModel.getSingleton().removeRow(row);
                jAttackTable.revalidate();
            }

        }
        jAttackTable.repaint();
    }//GEN-LAST:event_fireRemoveEvent

    private void fireCopyEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyEvent
        int[] rows = jAttackTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            JOptionPaneHelper.showInformationBox(this, "Keine Angriffe ausgewählt", "Information");
            return;
        }

        List<Attack> copies = new LinkedList<Attack>();
        for (int r = rows.length - 1; r >= 0; r--) {
            int row = jAttackTable.convertRowIndexToModel(rows[r]);
            Attack a = DoItYourselfAttackTableModel.getSingleton().getAttack(row);
            Attack b = new Attack();
            b.setSource(a.getSource());
            b.setTarget(a.getTarget());
            b.setUnit(a.getUnit());
            b.setArriveTime(a.getArriveTime());
            b.setType(a.getType());
            copies.add(b);
        }
        jAttackTable.invalidate();
        for (Attack a : copies) {
            DoItYourselfAttackTableModel.getSingleton().addAttack(a.getSource(), a.getTarget(), a.getArriveTime(), a.getUnit(), a.getType());
        }
        jAttackTable.revalidate();
        jAttackTable.repaint();

    }//GEN-LAST:event_fireCopyEvent

    private void fireExportAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireExportAttacksEvent
        int[] rows = jAttackTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            JOptionPaneHelper.showInformationBox(this, "Keine Angriffe ausgewählt", "Information");
            return;
        }

        if (evt.getSource() == jUnformattedExport) {
            //unformatted export
            try {
                StringBuilder buffer = new StringBuilder();
                List<Attack> attacks = AttackManager.getSingleton().getDoItYourselfAttacks();
                for (int i : rows) {
                    jAttackTable.invalidate();
                    int row = jAttackTable.convertRowIndexToModel(i);
                    Village sVillage = attacks.get(row).getSource();
                    Village tVillage = attacks.get(row).getTarget();
                    UnitHolder sUnit = attacks.get(row).getUnit();
                    Date aTime = attacks.get(row).getArriveTime();
                    Date sTime = new Date(aTime.getTime() - (long) (DSCalculator.calculateMoveTimeInSeconds(sVillage, tVillage, sUnit.getSpeed()) * 1000));
                    int type = attacks.get(row).getType();
                    String sendtime = null;
                    String arrivetime = null;

                    if (ServerSettings.getSingleton().isMillisArrival()) {
                        sendtime = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(sTime);
                        arrivetime = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS").format(aTime);
                    } else {
                        sendtime = new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(sTime);
                        arrivetime = new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(aTime);
                    }

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

                    if (sVillage.getTribe() == Barbarians.getSingleton()) {
                        buffer.append("Barbaren");
                    } else {
                        buffer.append(sVillage.getTribe());
                    }
                    buffer.append("\t");
                    buffer.append(sVillage);
                    buffer.append("\t");
                    buffer.append(sUnit);
                    buffer.append("\t");
                    if (tVillage.getTribe() == Barbarians.getSingleton()) {
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

            } catch (Exception e) {
                logger.error("Failed to copy data to clipboard", e);
                String result = "Fehler beim Kopieren in die Zwischenablage.";
                JOptionPaneHelper.showErrorBox(this, result, "Fehler");
            }
        } else {
            try {
                boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);
                StringBuilder buffer = new StringBuilder();
                if (extended) {
                    buffer.append("[u][size=12]Angriffsplan[/size][/u]\n\n");
                } else {
                    buffer.append("[u]Angriffsplan[/u]\n\n");
                }
                String sUrl = ServerManager.getServerURL(GlobalOptions.getSelectedServer());

                List<Attack> attacks = AttackManager.getSingleton().getDoItYourselfAttacks();
                jAttackTable.invalidate();
                for (int i : rows) {
                    int row = jAttackTable.convertRowIndexToModel(i);
                    buffer.append(AttackToBBCodeFormater.formatAttack(attacks.get(row), sUrl, extended));
                }

                jAttackTable.revalidate();
                if (extended) {
                    buffer.append("\n[size=8]Erstellt am ");
                    buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                    buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS Workbench ");
                    buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/url][/size]\n");
                } else {
                    buffer.append("\nErstellt am ");
                    buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                    buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS Workbench ");
                    buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/url]\n");
                }

                String b = buffer.toString();
                StringTokenizer t = new StringTokenizer(b, "[");
                int cnt = t.countTokens();
                if (cnt > 500) {
                    if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Angriffe benötigen mehr als 500 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                        return;
                    }
                }

                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
                String result = "Daten in Zwischenablage kopiert.";
                JOptionPaneHelper.showInformationBox(this, result, "Information");

            } catch (Exception e) {
                logger.error("Failed to copy data to clipboard", e);
                String result = "Fehler beim Kopieren in die Zwischenablage.";
                JOptionPaneHelper.showErrorBox(this, result, "Fehler");
            }
        }
    }//GEN-LAST:event_fireExportAttacksEvent

    private void fireWriteToHTMLEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireWriteToHTMLEvent
        String dir = GlobalOptions.getProperty("screen.dir");
        if (dir == null) {
            dir = ".";
        }
        String selectedPlan = AttackManager.getSingleton().getActiveAttackPlan();
        JFileChooser chooser = null;
        try {
            chooser = new JFileChooser(dir);
        } catch (Exception e) {
            JOptionPaneHelper.showErrorBox(this, "Konnte Dateiauswahldialog nicht öffnen.\nMöglicherweise verwendest du Windows Vista. Ist dies der Fall, beende DS Workbench, klicke mit der rechten Maustaste auf DSWorkbench.exe,\n" + "wähle 'Eigenschaften' und deaktiviere dort unter 'Kompatibilität' den Windows XP Kompatibilitätsmodus.", "Fehler");
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
                List<Attack> attacks = AttackManager.getSingleton().getDoItYourselfAttacks();
                List<Attack> toExport = new LinkedList<Attack>();
                for (int i : rows) {
                    int row = jAttackTable.convertRowIndexToModel(i);
                    toExport.add(attacks.get(row));
                }
                AttackPlanHTMLExporter.doExport(target, "-kein Plan-", toExport);
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

    private void fireMoveAttacksToAttackViewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireMoveAttacksToAttackViewEvent
        int[] rows = jAttackTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            JOptionPaneHelper.showInformationBox(this, "Keine Angriffe ausgewählt", "Information");
            return;
        }

        jExistingPlansBox.setModel(new DefaultComboBoxModel(AttackManager.getSingleton().getPlansAsArray()));
        jNewPlanField.setText("");
        jTransferToAttackViewDialog.pack();
        jTransferToAttackViewDialog.setVisible(true);
    }//GEN-LAST:event_fireMoveAttacksToAttackViewEvent

    private void fireDoMoveAttacksToAttackViewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDoMoveAttacksToAttackViewEvent
        if (evt.getSource() == jOKButton) {
            int[] rows = jAttackTable.getSelectedRows();
            if (rows == null || rows.length == 0) {
                JOptionPaneHelper.showInformationBox(this, "Keine Angriffe ausgewählt", "Information");
                return;
            }
            String plan = AttackManager.DEFAULT_PLAN_ID;
            if (jNewPlanField.getText().length() > 0) {
                //new plan
                plan = jNewPlanField.getText();
                if (AttackManager.getSingleton().getAttackPlan(plan) == null) {
                    AttackManager.getSingleton().addEmptyPlan(plan);
                    DSWorkbenchAttackFrame.getSingleton().buildAttackPlanList();
                }
            } else {
                plan = (String) jExistingPlansBox.getSelectedItem();
            }

            for (int r = rows.length - 1; r >= 0; r--) {
                int row = jAttackTable.convertRowIndexToModel(rows[r]);
                Attack a = DoItYourselfAttackTableModel.getSingleton().getAttack(row);
                AttackManager.getSingleton().addAttackFast(a.getSource(), a.getTarget(), a.getUnit(), a.getArriveTime(), false, plan, a.getType());
            }
            AttackManager.getSingleton().forceUpdate(plan);
        }
        jTransferToAttackViewDialog.setVisible(false);
    }//GEN-LAST:event_fireDoMoveAttacksToAttackViewEvent

    private void fireAdeptEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAdeptEvent
        int[] rows = jAttackTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            JOptionPaneHelper.showInformationBox(this, "Keine Angriffe ausgewählt", "Information");
            return;
        }
        if (evt.getSource() == jAdeptTimeButton) {
            Date newArrive = jNewArriveSpinner.getSelectedDate();
            if (newArrive.getTime() < System.currentTimeMillis()) {
                JOptionPaneHelper.showInformationBox(this, "Ankunftszeit darf nicht in der Vergangenheit liegen.", "Information");
                return;
            }
            for (int r = rows.length - 1; r >= 0; r--) {
                int row = jAttackTable.convertRowIndexToModel(rows[r]);
                Attack a = DoItYourselfAttackTableModel.getSingleton().getAttack(row);
                a.setArriveTime(newArrive);
            }

        } else if (evt.getSource() == jAdeptUnitButton) {
            UnitHolder newUnit = (UnitHolder) jUnitComboBox.getSelectedItem();
            if (newUnit == null) {
                JOptionPaneHelper.showInformationBox(this, "Keine Einheit ausgewählt.", "Information");
                return;
            }

            for (int r = rows.length - 1; r >= 0; r--) {
                int row = jAttackTable.convertRowIndexToModel(rows[r]);
                Attack a = DoItYourselfAttackTableModel.getSingleton().getAttack(row);
                a.setUnit(newUnit);
            }
        } else if (evt.getSource() == jAdeptTypeButton) {
            Integer newType = (Integer) jAttackTypeComboBox.getSelectedItem();
            if (newType == null) {
                JOptionPaneHelper.showInformationBox(this, "Kein Angriffstyp ausgewählt.", "Information");
                return;
            }

            for (int r = rows.length - 1; r >= 0; r--) {
                int row = jAttackTable.convertRowIndexToModel(rows[r]);
                Attack a = DoItYourselfAttackTableModel.getSingleton().getAttack(row);
                a.setType(newType);
            }
        }
        jAttackTable.repaint();
    }//GEN-LAST:event_fireAdeptEvent

    public final void fireRebuildTableEvent() {
        try {
            jAttackTable.invalidate();
            for (int i = 0; i < jAttackTable.getColumnCount(); i++) {
                jAttackTable.getColumn(jAttackTable.getColumnName(i)).setHeaderRenderer(mHeaderRenderer);
            }
            jAttackTable.revalidate();
            jAttackTable.repaint();
        } catch (Exception e) {
            logger.error("Failed to update attacks table", e);
        }
        DoItYourselfAttackTableModel.getSingleton().resetRowSorter(jAttackTable);
        DoItYourselfAttackTableModel.getSingleton().loadColumnState();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAdeptTimeButton;
    private javax.swing.JButton jAdeptTypeButton;
    private javax.swing.JButton jAdeptUnitButton;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private de.tor.tribes.ui.components.DateTimeField jArriveTime;
    private javax.swing.JTable jAttackTable;
    private javax.swing.JComboBox jAttackTypeComboBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JComboBox jExistingPlansBox;
    private javax.swing.JButton jExportAsBBCode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private de.tor.tribes.ui.components.DateTimeField jNewArriveSpinner;
    private javax.swing.JTextField jNewPlanField;
    private javax.swing.JButton jOKButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JFormattedTextField jSourceVillage;
    private javax.swing.JFormattedTextField jTargetVillage;
    private com.l2fprod.common.swing.JTaskPane jTaskPane1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup2;
    private javax.swing.JDialog jTransferToAttackViewDialog;
    private javax.swing.JButton jUnformattedExport;
    private javax.swing.JComboBox jUnitBox;
    private javax.swing.JComboBox jUnitComboBox;
    // End of variables declaration//GEN-END:variables
}

class DoItYourselfCountdownThread extends Thread {

    public DoItYourselfCountdownThread() {
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (DSWorkbenchDoItYourselfAttackPlaner.getSingleton().isVisible()) {
                    DSWorkbenchDoItYourselfAttackPlaner.getSingleton().updateCountdown();
                    sleep(100);
                } else {
                    sleep(1000);
                }
            } catch (Exception e) {
            }
        }
    }
}
