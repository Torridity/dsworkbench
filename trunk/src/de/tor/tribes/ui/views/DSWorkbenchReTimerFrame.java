/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchReTimerFrame.java
 *
 * Created on 22.12.2009, 13:43:21
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.TroopFilterElement;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.renderer.AlternatingColorCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.UnitListCellRenderer;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.tag.TagManagerListener;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

/**
 * @TODO (DIFF) Fixed time parsing for second-based servers
 * @author Jejkal
 */
public class DSWorkbenchReTimerFrame extends AbstractDSWorkbenchFrame implements TagManagerListener {

    private static Logger logger = Logger.getLogger("ReTimeTool");
    private static DSWorkbenchReTimerFrame SINGLETON = null;

    public static synchronized DSWorkbenchReTimerFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchReTimerFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchReTimerFrame */
    DSWorkbenchReTimerFrame() {
        initComponents();

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.retime_tool", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
    }


    /*OPERA
    Herkunft	Spieler:	Rattenfutter
    Dorf:	001 Rattennest (486|833) K84
    Ziel	Spieler:	Rattenfutter
    Dorf:	005 Rattennest (486|834) K84
    Ankunft:	22.12.09 13:57:44:321
    Ankunft in:	0:08:34
     *
     *
     *
     * FF
    Herkunft	Spieler:	Rattenfutter
    Dorf:	001 Rattennest (486|833) K84
    Ziel	Spieler:	Rattenfutter
    Dorf:	005 Rattennest (486|834) K84
    Dauer:	0:09:00
    Ankunft:	22.12.09 14:02:30:232
    Ankunft in:	0:08:41
    » abbrechen
     */
    public void resetView() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            model.addElement(unit);
        }
        jUnitBox.setModel(model);
        jUnitBox.setRenderer(new UnitListCellRenderer());
        DefaultListModel tagModel = new DefaultListModel();
        tagModel.addElement(NoTag.getSingleton());
        for (ManageableType e : TagManager.getSingleton().getAllElements()) {
            Tag t = (Tag) e;
            tagModel.addElement(t);
        }
        jTagList.setModel(tagModel);
        jRelationBox.setSelected(true);
        // <editor-fold defaultstate="collapsed" desc="Build filter dialog">
        jFilterUnitBox.setModel(new DefaultComboBoxModel(DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{})));
        jFilterUnitBox.setRenderer(new UnitListCellRenderer());
        jFilterList.setModel(new DefaultListModel());
// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build attack plan table">
        DefaultTableModel attackPlabTableModel = new javax.swing.table.DefaultTableModel(
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
        Iterator<String> plans = AttackManager.getSingleton().getGroupIterator();
        List<String> planList = new LinkedList<String>();
        while (plans.hasNext()) {
            planList.add(plans.next());
        }

        Collections.sort(planList);
        for (String plan : planList) {
            attackPlabTableModel.addRow(new Object[]{plan, false});
        }

        jAttackPlanTable.setModel(attackPlabTableModel);
        jAttackPlanTable.revalidate();
        DefaultTableCellRenderer headerRenderer = new SortableTableHeaderRenderer();
        for (int i = 0; i < jAttackPlanTable.getColumnCount(); i++) {
            jAttackPlanTable.getColumn(jAttackPlanTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        // </editor-fold>
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jResultFrame = new javax.swing.JFrame();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jResultTable = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jAttackPlanSelectionDialog = new javax.swing.JDialog();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jNewAttackPlanField = new javax.swing.JTextField();
        jExistingAttackPlanBox = new javax.swing.JComboBox();
        jInsertButton = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jFilterDialog = new javax.swing.JDialog();
        jPanel6 = new javax.swing.JPanel();
        jFilterUnitBox = new javax.swing.JComboBox();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jButton17 = new javax.swing.JButton();
        jMinValue = new javax.swing.JTextField();
        jMaxValue = new javax.swing.JTextField();
        jButton20 = new javax.swing.JButton();
        jApplyFiltersButton = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        jFilterList = new javax.swing.JList();
        jLabel28 = new javax.swing.JLabel();
        jButton18 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jComandArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSourceVillage = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTargetVillage = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jArriveField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jAxeBox = new javax.swing.JCheckBox();
        jSwordBox = new javax.swing.JCheckBox();
        jSpyBox = new javax.swing.JCheckBox();
        jLightBox = new javax.swing.JCheckBox();
        jHeavyBox = new javax.swing.JCheckBox();
        jRamBox = new javax.swing.JCheckBox();
        jPalaBox = new javax.swing.JCheckBox();
        jSnobBox = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jEstSendTime = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jReturnField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jParserInfo = new javax.swing.JTextPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTagList = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();
        jRelationBox = new javax.swing.JCheckBox();
        jUnitBox = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jAttackPlanTable = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        jMainAlwaysOnTopBox = new javax.swing.JCheckBox();

        jResultFrame.setTitle("Re-Timing Ergebnisse");

        jPanel5.setBackground(new java.awt.Color(239, 235, 223));
        jPanel5.setToolTipText("Ergebnisse");

        jResultTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(jResultTable);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton2.setText("Schließen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseResultsEvent(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_overview.png"))); // NOI18N
        jButton3.setToolTipText("Markierte Angriffe in die Angriffsübersicht einfügen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowAttackPlanSelectionDialogEvent(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/filter_strength.png"))); // NOI18N
        jButton4.setToolTipText("Herkunftsdörfer nach Kampfkraft filtern");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowFilterDialogEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2)
                        .addComponent(jButton3))
                    .addComponent(jButton4))
                .addContainerGap())
        );

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireResultAlwaysOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout jResultFrameLayout = new javax.swing.GroupLayout(jResultFrame.getContentPane());
        jResultFrame.getContentPane().setLayout(jResultFrameLayout);
        jResultFrameLayout.setHorizontalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAlwaysOnTopBox))
                .addContainerGap())
        );
        jResultFrameLayout.setVerticalGroup(
            jResultFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAlwaysOnTopBox)
                .addContainerGap())
        );

        jAttackPlanSelectionDialog.setTitle("Angriffsplanauswahl");
        jAttackPlanSelectionDialog.setAlwaysOnTop(true);

        jLabel10.setText("Existierender Plan");

        jLabel11.setText("Neuer Plan");

        jInsertButton.setText("Einfügen");
        jInsertButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferAttacksToAttackViewEvent(evt);
            }
        });

        jButton5.setText("Abbrechen");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferAttacksToAttackViewEvent(evt);
            }
        });

        javax.swing.GroupLayout jAttackPlanSelectionDialogLayout = new javax.swing.GroupLayout(jAttackPlanSelectionDialog.getContentPane());
        jAttackPlanSelectionDialog.getContentPane().setLayout(jAttackPlanSelectionDialogLayout);
        jAttackPlanSelectionDialogLayout.setHorizontalGroup(
            jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                        .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jNewAttackPlanField, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                            .addComponent(jExistingAttackPlanBox, 0, 284, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAttackPlanSelectionDialogLayout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jInsertButton)))
                .addContainerGap())
        );
        jAttackPlanSelectionDialogLayout.setVerticalGroup(
            jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAttackPlanSelectionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jExistingAttackPlanBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jNewAttackPlanField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAttackPlanSelectionDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jInsertButton)
                    .addComponent(jButton5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("TribeTribeAttackFrame.jPanel3.border.title"))); // NOI18N

        jFilterUnitBox.setMaximumSize(new java.awt.Dimension(51, 25));
        jFilterUnitBox.setMinimumSize(new java.awt.Dimension(51, 25));
        jFilterUnitBox.setPreferredSize(new java.awt.Dimension(51, 25));

        jLabel25.setText(bundle.getString("TribeTribeAttackFrame.jLabel25.text")); // NOI18N

        jLabel26.setText(bundle.getString("TribeTribeAttackFrame.jLabel26.text")); // NOI18N

        jLabel27.setText(bundle.getString("TribeTribeAttackFrame.jLabel27.text")); // NOI18N

        jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton17.setText(bundle.getString("TribeTribeAttackFrame.jButton17.text")); // NOI18N
        jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddTroopFilterEvent(evt);
            }
        });

        jMinValue.setText(bundle.getString("TribeTribeAttackFrame.jMinValue.text")); // NOI18N
        jMinValue.setMaximumSize(new java.awt.Dimension(51, 20));
        jMinValue.setMinimumSize(new java.awt.Dimension(51, 20));
        jMinValue.setPreferredSize(new java.awt.Dimension(51, 20));

        jMaxValue.setText(bundle.getString("TribeTribeAttackFrame.jMaxValue.text")); // NOI18N
        jMaxValue.setMaximumSize(new java.awt.Dimension(51, 20));
        jMaxValue.setMinimumSize(new java.awt.Dimension(51, 20));
        jMaxValue.setPreferredSize(new java.awt.Dimension(51, 20));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(jLabel26))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jMinValue, 0, 0, Short.MAX_VALUE)
                            .addComponent(jFilterUnitBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                        .addComponent(jLabel27)
                        .addGap(18, 18, 18)
                        .addComponent(jMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton17, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jFilterUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(jLabel27)
                    .addComponent(jMinValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton17)
                .addContainerGap())
        );

        jButton20.setText(bundle.getString("TribeTribeAttackFrame.jButton20.text")); // NOI18N
        jButton20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyTroopFiltersEvent(evt);
            }
        });

        jApplyFiltersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jApplyFiltersButton.setText(bundle.getString("TribeTribeAttackFrame.jApplyFiltersButton.text")); // NOI18N
        jApplyFiltersButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireApplyTroopFiltersEvent(evt);
            }
        });

        jScrollPane14.setViewportView(jFilterList);

        jLabel28.setText(bundle.getString("TribeTribeAttackFrame.jLabel28.text")); // NOI18N

        jButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton18.setText(bundle.getString("TribeTribeAttackFrame.jButton18.text")); // NOI18N
        jButton18.setToolTipText(bundle.getString("TribeTribeAttackFrame.jButton18.toolTipText")); // NOI18N
        jButton18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTroopFilterEvent(evt);
            }
        });

        javax.swing.GroupLayout jFilterDialogLayout = new javax.swing.GroupLayout(jFilterDialog.getContentPane());
        jFilterDialog.getContentPane().setLayout(jFilterDialogLayout);
        jFilterDialogLayout.setHorizontalGroup(
            jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFilterDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jFilterDialogLayout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFilterDialogLayout.createSequentialGroup()
                                .addComponent(jButton20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jApplyFiltersButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jFilterDialogLayout.setVerticalGroup(
            jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFilterDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel28)
                    .addComponent(jScrollPane14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFilterDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jApplyFiltersButton)
                    .addComponent(jButton20))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 203, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 169, Short.MAX_VALUE)
        );

        setTitle("Re-Time Werkzeug");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jScrollPane1.setToolTipText("");

        jComandArea.setColumns(20);
        jComandArea.setRows(5);
        jComandArea.setToolTipText("Angriffsbefehl hierhin kopieren");
        jComandArea.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                fireComandDataChangedEvent(evt);
            }
        });
        jScrollPane1.setViewportView(jComandArea);

        jLabel1.setText("Angriffsbefehl");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Gelesene Werte"));
        jPanel2.setOpaque(false);

        jLabel2.setText("Herkunft");

        jSourceVillage.setToolTipText("Gelesene Herkunft des Angriffs");

        jLabel3.setText("Ziel");

        jTargetVillage.setToolTipText("Gelesenes Ziel des Angriffs");

        jLabel4.setText("Ankunft");

        jArriveField.setToolTipText("Gelesene Ankunftszeit des Angriffs");

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.GridLayout(4, 2));

        buttonGroup1.add(jAxeBox);
        jAxeBox.setText("Axt");
        jAxeBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jAxeBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jAxeBox.setDoubleBuffered(true);
        jAxeBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jAxeBox.setOpaque(false);
        jAxeBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jAxeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jAxeBox);

        buttonGroup1.add(jSwordBox);
        jSwordBox.setText("Schwert");
        jSwordBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSwordBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSwordBox.setDoubleBuffered(true);
        jSwordBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jSwordBox.setOpaque(false);
        jSwordBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jSwordBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jSwordBox);

        buttonGroup1.add(jSpyBox);
        jSpyBox.setText("Späher");
        jSpyBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSpyBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSpyBox.setDoubleBuffered(true);
        jSpyBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jSpyBox.setOpaque(false);
        jSpyBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jSpyBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jSpyBox);

        buttonGroup1.add(jLightBox);
        jLightBox.setText("LKav");
        jLightBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jLightBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jLightBox.setDoubleBuffered(true);
        jLightBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jLightBox.setOpaque(false);
        jLightBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jLightBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jLightBox);

        buttonGroup1.add(jHeavyBox);
        jHeavyBox.setText("SKav");
        jHeavyBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jHeavyBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jHeavyBox.setDoubleBuffered(true);
        jHeavyBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jHeavyBox.setOpaque(false);
        jHeavyBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jHeavyBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jHeavyBox);

        buttonGroup1.add(jRamBox);
        jRamBox.setText("Ramme");
        jRamBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jRamBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jRamBox.setDoubleBuffered(true);
        jRamBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jRamBox.setOpaque(false);
        jRamBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jRamBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jRamBox);

        buttonGroup1.add(jPalaBox);
        jPalaBox.setText("Paladin");
        jPalaBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jPalaBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jPalaBox.setDoubleBuffered(true);
        jPalaBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jPalaBox.setOpaque(false);
        jPalaBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jPalaBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jPalaBox);

        buttonGroup1.add(jSnobBox);
        jSnobBox.setText("AG");
        jSnobBox.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSnobBox.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_red.png"))); // NOI18N
        jSnobBox.setDoubleBuffered(true);
        jSnobBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_grey.png"))); // NOI18N
        jSnobBox.setOpaque(false);
        jSnobBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bullet_ball_green.png"))); // NOI18N
        jSnobBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireEstUnitChangedEvent(evt);
            }
        });
        jPanel3.add(jSnobBox);

        jLabel6.setText("Abschickzeit");

        jEstSendTime.setToolTipText("Abschickzeit des Angriffs unter Verwendung der gewählten Einheit");
        jEstSendTime.setEnabled(false);

        jLabel9.setText("Rückkehr");

        jReturnField.setToolTipText("Rückkehr der Truppen unter Verwendung der gewählten Einheit");
        jReturnField.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jReturnField, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jArriveField, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jSourceVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jTargetVillage, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jEstSendTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jSourceVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jTargetVillage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jEstSendTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jArriveField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jReturnField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel5.setText("Status");

        jScrollPane2.setBorder(null);
        jScrollPane2.setMaximumSize(new java.awt.Dimension(32767, 50));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(21, 50));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(2, 50));

        jParserInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jParserInfo.setEditable(false);
        jParserInfo.setToolTipText("Statusmeldungen über das Einlesen des Angriffsbefehls");
        jScrollPane2.setViewportView(jParserInfo);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen für das Gegentimen"));
        jPanel4.setOpaque(false);

        jTagList.setToolTipText("Zu verwendende Gruppen");
        jScrollPane3.setViewportView(jTagList);

        jLabel7.setText("Dorfgruppe");

        jRelationBox.setSelected(true);
        jRelationBox.setText("Verknüpfung (UND)");
        jRelationBox.setToolTipText("Verknüpfung der gewählten Dorfgruppen (UND = Dorf muss in allen Gruppen sein, ODER = Dorf muss in mindestens einer Gruppe sein)");
        jRelationBox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jRelationBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_or.png"))); // NOI18N
        jRelationBox.setOpaque(false);
        jRelationBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_and.png"))); // NOI18N
        jRelationBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireRelationChangedEvent(evt);
            }
        });

        jUnitBox.setToolTipText("Langsamste Einheit mit der gegengetimed wird");
        jUnitBox.setMaximumSize(new java.awt.Dimension(40, 25));
        jUnitBox.setMinimumSize(new java.awt.Dimension(40, 25));
        jUnitBox.setPreferredSize(new java.awt.Dimension(40, 25));

        jLabel8.setText("Einheit");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/axe.png"))); // NOI18N
        jButton1.setText("Berechnen");
        jButton1.setToolTipText("Mögliche Angriffe zum Gegentimen berechnen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateReTimingsEvent(evt);
            }
        });

        jAttackPlanTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Title 1", "Title 2"
            }
        ));
        jScrollPane4.setViewportView(jAttackPlanTable);

        jLabel12.setText("Abgleichen mit");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jRelationBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel8))
                .addGap(14, 14, 14)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addComponent(jButton1)
                    .addComponent(jUnitBox, 0, 193, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                    .addComponent(jLabel7)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jUnitBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                            .addComponent(jLabel12))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRelationBox)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jMainAlwaysOnTopBox.setText("Immer im Vordergrund");
        jMainAlwaysOnTopBox.setOpaque(false);
        jMainAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopChangedEvent(evt);
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
                    .addComponent(jMainAlwaysOnTopBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jMainAlwaysOnTopBox)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireComandDataChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireComandDataChangedEvent
        List<Village> villages = PluginManager.getSingleton().executeVillageParser(jComandArea.getText());
        if (villages == null || villages.isEmpty() || villages.size() < 2) {
            jParserInfo.setBackground(Color.YELLOW);
            jParserInfo.setText("Keine Dörfer gefunden.\n" + "Möglicherweise handelt es sich nicht um einen gültigen Angriffsbefehl.");
            return;
        }

        Village source = villages.get(0);
        Village target = villages.get(1);
        if (jComandArea.getText().indexOf(PluginManager.getSingleton().getVariableValue("sos.arrive.time")) > -1) {
            //change village order for SOS requests
            source = villages.get(1);
            target = villages.get(0);
        }
        jSourceVillage.setText(source.toString());
        jTargetVillage.setText(target.toString());
        boolean fromSelection = false;
        Date arriveDate = null;
        SimpleDateFormat f = null;
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
        } else {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
        }

        try {
            String text = jComandArea.getText();
            String selection = jComandArea.getSelectedText();
            String arrive = null;
            if (selection == null) {
                String arriveLine = null;
                if (text.indexOf(PluginManager.getSingleton().getVariableValue("attack.arrive.time")) > -1) {
                    arriveLine = text.substring(text.indexOf(PluginManager.getSingleton().getVariableValue("attack.arrive.time")));
                } else {
                    arriveLine = text.substring(text.indexOf(PluginManager.getSingleton().getVariableValue("sos.arrive.time")));
                }

                /*
                Befehl
                Herkunft	Spieler:	Rattenfutter
                Dorf:	015 R.I.P. Frankfurt Lions 01 (382|891) K83
                Ziel	Spieler:	Rattenfutter
                Dorf:	Metropolis L06 (384|891) K83
                Dauer:	1:00:00
                Ankunft:	01.02.11 22:54:00:670
                Ankunft in:	0:59:57
                » abbrechen
                » Versammlungsplatz
                 */
                StringTokenizer tokenizer = new StringTokenizer(arriveLine, " \t");
                tokenizer.nextToken();
                String date = tokenizer.nextToken();
                String time = tokenizer.nextToken();
                arrive = date.trim() + " " + time.trim();
            } else {
                fromSelection = true;
                arrive = selection;
            }
            arriveDate = f.parse(arrive);

            jArriveField.setText(f.format(arriveDate));
            jParserInfo.setBackground(Color.GREEN);
            jParserInfo.setText("Angriffsbefehl erfolgreich gelesen.");
        } catch (Exception e) {
            if (!fromSelection) {
                jParserInfo.setBackground(Color.RED);
                jParserInfo.setText("Es konnte keine Ankunftszeit gefunden werden.\nBitte markiere im oberen Textfeld die Ankunftszeit und -datum.");
                return;
            } else {
                jParserInfo.setBackground(Color.RED);
                jParserInfo.setText("Aus der Auswahl konnte keine Ankunftszeit bestimmt werden.\nBitte versuche, den Angriffsbefehl erneut zu kopieren oder wende dich an den DS Workbench Support.");
                return;
            }
        }

        //calc possible units
        double dist = DSCalculator.calculateDistance(source, target);

        Hashtable<String, JCheckBox> unitsCheckboxMappings = new Hashtable<String, JCheckBox>();

        unitsCheckboxMappings.put("axe", jAxeBox);
        unitsCheckboxMappings.put("sword", jSwordBox);
        unitsCheckboxMappings.put("spy", jSpyBox);
        unitsCheckboxMappings.put("light", jLightBox);
        unitsCheckboxMappings.put("heavy", jHeavyBox);
        unitsCheckboxMappings.put("ram", jRamBox);
        unitsCheckboxMappings.put("knight", jPalaBox);
        unitsCheckboxMappings.put("snob", jSnobBox);

        Enumeration<String> unitKeys = unitsCheckboxMappings.keys();
        while (unitKeys.hasMoreElements()) {
            String plainName = unitKeys.nextElement();
            JCheckBox unitBox = unitsCheckboxMappings.get(plainName);
            UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(plainName);
            if (unit != null) {
                long dur = (long) Math.floor(dist * unit.getSpeed() * 60000.0);
                if (arriveDate.getTime() - dur > System.currentTimeMillis()) {
                    unitBox.setEnabled(false);
                } else {
                    unitBox.setEnabled(true);
                }
            } else {
                unitBox.setEnabled(false);
            }
        }

        if (jRamBox.isEnabled()) {
            jRamBox.setSelected(true);
            fireEstUnitChangedEvent(new ItemEvent(jRamBox, 0, null, 0));
        } else if (jAxeBox.isEnabled()) {
            jAxeBox.setSelected(true);
            fireEstUnitChangedEvent(new ItemEvent(jAxeBox, 0, null, 0));
        } else {
            jSpyBox.setSelected(true);
            jEstSendTime.setText("(unbekannt)");
        }
    }//GEN-LAST:event_fireComandDataChangedEvent

    private void fireEstUnitChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireEstUnitChangedEvent
        UnitHolder unit = null;
        if (evt.getSource() == jAxeBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("axe");
        } else if (evt.getSource() == jSwordBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("sword");
        } else if (evt.getSource() == jSpyBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("spy");
        } else if (evt.getSource() == jLightBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("light");
        } else if (evt.getSource() == jHeavyBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("heavy");
        } else if (evt.getSource() == jRamBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("ram");
        } else if (evt.getSource() == jPalaBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("knight");
        } else if (evt.getSource() == jSnobBox) {
            unit = DataHolder.getSingleton().getUnitByPlainName("snob");
        }
        SimpleDateFormat f = null;

        if (!ServerSettings.getSingleton().isMillisArrival()) {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
        } else {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
        }
        try {
            Date arrive = f.parse(jArriveField.getText());
            Village source = PluginManager.getSingleton().executeVillageParser(jSourceVillage.getText()).get(0);
            Village target = PluginManager.getSingleton().executeVillageParser(jTargetVillage.getText()).get(0);
            // double dist = DSCalculator.calculateDistance(source, target);
            double dur = DSCalculator.calculateMoveTimeInSeconds(source, target, unit.getSpeed()) * 1000.0;
            long send = arrive.getTime() - (long) dur;
            double ret = (double) arrive.getTime() + dur;
            ret /= 1000;
            ret = Math.round(ret + .5);
            ret *= 1000;
            if (ServerSettings.getSingleton().isMillisArrival()) {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss:SSS");
            } else {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss");
            }
            jEstSendTime.setText("~ " + f.format(new Date(send)));
            jReturnField.setText("~ " + f.format(new Date((long) ret)));
        } catch (Exception e) {
            jEstSendTime.setText("(unbekannt)");
            jReturnField.setText("(unbekannt)");
        }
    }//GEN-LAST:event_fireEstUnitChangedEvent

    private void fireCalculateReTimingsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateReTimingsEvent


        DefaultTableModel model = (DefaultTableModel) jAttackPlanTable.getModel();

        List<String> selectedPlans = new LinkedList<String>();
        for (int i = 0; i < jAttackPlanTable.getRowCount(); i++) {
            int row = jAttackPlanTable.convertRowIndexToModel(i);
            if ((Boolean) model.getValueAt(row, 1)) {
                selectedPlans.add((String) model.getValueAt(row, 0));
            }
        }

        // Enumeration<String> plans = AttackManager.getSingleton().getPlans();
        List<Village> ignore = new LinkedList<Village>();
        //process all plans
        //  while (plans.hasMoreElements()) {
        for (String plan : selectedPlans) {
            //String plan = plans.nextElement();
            logger.debug("Checking plan '" + plan + "'");
            List<ManageableType> elements = AttackManager.getSingleton().getAllElements(plan);

            //process all attacks
            for (ManageableType element : elements) {
                Attack a = (Attack) element;
                if (!ignore.contains(a.getSource())) {
                    ignore.add(a.getSource());

                }
            }
        }



        Object[] tags = jTagList.getSelectedValues();
        if (tags == null || tags.length == 0) {
            JOptionPaneHelper.showInformationBox(this, "Keine Dorfgruppe ausgewählt", "Information");
            return;
        }

        List<Village> candidates = new LinkedList<Village>();
        for (Object o : tags) {
            Tag t = (Tag) o;
            List<Integer> ids = t.getVillageIDs();
            for (Integer id : ids) {
                //add all villages tagged by current tag
                Village v = DataHolder.getSingleton().getVillagesById().get(id);
                if (!candidates.contains(v) && !ignore.contains(v)) {
                    candidates.add(v);
                }
            }
        }

        if (jRelationBox.isSelected()) {
            //remove all villages that are not tagges by the current tag
            boolean oneFailed = false;
            Village[] aCandidates = candidates.toArray(new Village[]{});
            for (Village v_tmp : aCandidates) {
                for (Object o : tags) {
                    Tag t = (Tag) o;
                    if (!t.tagsVillage(v_tmp.getId())) {
                        oneFailed = true;
                        break;
                    }
                }

                if (oneFailed) {
                    //at least one tag is not valid for village
                    candidates.remove(v_tmp);
                    oneFailed = false;
                }
            }
        }

        Village target = null;
        try {
            target = PluginManager.getSingleton().executeVillageParser(jSourceVillage.getText()).get(0);
        } catch (Exception e) {
            //no target set
            return;
        }
        UnitHolder unit = (UnitHolder) jUnitBox.getSelectedItem();
        Hashtable<Village, Date> timings = new Hashtable<Village, Date>();

        for (Village candidate : candidates) {
            double dist = DSCalculator.calculateDistance(candidate, target);
            long runtime = Math.round(dist * unit.getSpeed() * 60000.0);
            SimpleDateFormat f = null;
            if (ServerSettings.getSingleton().isMillisArrival()) {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss:SSS");
            } else {
                f = new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss");
            }
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(candidate, TroopsManager.TROOP_TYPE.OWN);
            boolean useVillage = true;
            if (holder != null) {
                if (holder.getTroopsOfUnitInVillage(unit) == 0) {
                    useVillage = false;
                }
            }
            if (useVillage) {
                try {
                    Date ret = f.parse(jReturnField.getText().replaceAll("~", "").trim());
                    long sendTime = ret.getTime() - runtime;
                    if (sendTime > System.currentTimeMillis() + 60000) {
                        timings.put(candidate, new Date(sendTime));
                    }
                } catch (Exception e) {
                }
            }
        }

        buildResults(timings, target, unit);
    }//GEN-LAST:event_fireCalculateReTimingsEvent

    private void fireResultAlwaysOnTopEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireResultAlwaysOnTopEvent
        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
    }//GEN-LAST:event_fireResultAlwaysOnTopEvent

    private void fireCloseResultsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseResultsEvent
        jResultFrame.setVisible(false);
    }//GEN-LAST:event_fireCloseResultsEvent

    private void fireTransferAttacksToAttackViewEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferAttacksToAttackViewEvent
        if (evt.getSource() == jInsertButton) {
            String planName = jNewAttackPlanField.getText();
            if (planName.length() == 0) {
                planName = (String) jExistingAttackPlanBox.getSelectedItem();
            } else {
                AttackManager.getSingleton().addGroup(planName);
            }
            int[] rows = jResultTable.getSelectedRows();
            AttackManager.getSingleton().invalidate();
            for (int row : rows) {
                Village source = (Village) jResultTable.getValueAt(row, 0);
                UnitHolder unit = (UnitHolder) jResultTable.getValueAt(row, 1);
                Village target = (Village) jResultTable.getValueAt(row, 2);
                Date sendTime = (Date) jResultTable.getValueAt(row, 3);
                double dist = DSCalculator.calculateDistance(source, target);
                long runtime = Math.round(dist * unit.getSpeed() * 60000);
                AttackManager.getSingleton().addAttack(source, target, unit, new Date(sendTime.getTime() + runtime), false, planName, Attack.NO_TYPE, false);
            }
            AttackManager.getSingleton().revalidate();
        }
        jAttackPlanSelectionDialog.setVisible(false);
    }//GEN-LAST:event_fireTransferAttacksToAttackViewEvent

    private void fireShowAttackPlanSelectionDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowAttackPlanSelectionDialogEvent
        int[] rows = jResultTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            JOptionPaneHelper.showInformationBox(jResultFrame, "Keine Angriffe ausgewählt", "Information");
            return;
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(AttackManager.getSingleton().getGroups());
        jExistingAttackPlanBox.setModel(model);
        jExistingAttackPlanBox.setSelectedItem(AttackManager.DEFAULT_GROUP);
        jNewAttackPlanField.setText("");
        jAttackPlanSelectionDialog.pack();
        jAttackPlanSelectionDialog.setLocationRelativeTo(jResultFrame);
        jAttackPlanSelectionDialog.setVisible(true);
    }//GEN-LAST:event_fireShowAttackPlanSelectionDialogEvent

    private void fireAlwaysOnTopChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangedEvent
        setAlwaysOnTop(jMainAlwaysOnTopBox.isSelected());
    }//GEN-LAST:event_fireAlwaysOnTopChangedEvent

    private void fireRelationChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireRelationChangedEvent
        if (jRelationBox.isSelected()) {
            jRelationBox.setText("Verknüpfung (UND)");
        } else {
            jRelationBox.setText("Verknüpfung (ODER)");
        }
    }//GEN-LAST:event_fireRelationChangedEvent

    private void fireAddTroopFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddTroopFilterEvent
        UnitHolder unit = (UnitHolder) jFilterUnitBox.getSelectedItem();
        DefaultListModel filterModel = (DefaultListModel) jFilterList.getModel();
        TroopFilterElement elem = null;
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        try {
            min = Integer.parseInt(jMinValue.getText());
        } catch (Exception e) {
            min = Integer.MIN_VALUE;
        }
        try {
            max = Integer.parseInt(jMaxValue.getText());
        } catch (Exception e) {
            max = Integer.MAX_VALUE;
        }
        if (min > max) {
            int tmp = min;
            min = max;
            max = tmp;
            jMinValue.setText("" + min);
            jMaxValue.setText("" + max);
        }

        for (int i = 0; i < filterModel.size(); i++) {
            TroopFilterElement listElem = (TroopFilterElement) filterModel.get(i);
            if (listElem.getUnit().equals(unit)) {
                //update min and max and return
                listElem.setMin(min);
                listElem.setMax(max);
                jFilterList.repaint();
                return;
            }
        }
        if (elem == null) {
            elem = new TroopFilterElement(unit, min, max);
            filterModel.addElement(elem);
        }
}//GEN-LAST:event_fireAddTroopFilterEvent

    private void fireApplyTroopFiltersEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireApplyTroopFiltersEvent
        if (evt.getSource() == jApplyFiltersButton) {
            DefaultListModel filterModel = (DefaultListModel) jFilterList.getModel();
            List<Integer> rowsToRemove = new LinkedList<Integer>();
            int removeCount = 0;
            for (int i = 0; i < jResultTable.getRowCount(); i++) {
                //go through all rows in attack table and get source village
                Village v = (Village) jResultTable.getValueAt(i, 0);
                for (int j = 0; j < filterModel.size(); j++) {
                    //check for all filters if villag is allowed
                    if (!((TroopFilterElement) filterModel.get(j)).allowsVillage(v)) {
                        //village is not allowed, add to remove list
                        int row = jResultTable.convertRowIndexToModel(i);
                        rowsToRemove.add(row);
                        removeCount++;
                    }
                }
            }

            jResultTable.invalidate();
            for (int i = rowsToRemove.size() - 1; i >= 0; i--) {
                int row = rowsToRemove.get(i);
                ((DefaultTableModel) jResultTable.getModel()).removeRow(row);
            }
            jResultTable.revalidate();
            String message = "Es wurden keine Angriffe entfernt.";
            if (removeCount == 1) {
                message = "Es wurde ein Angriff entfernt.";
            } else if (removeCount > 1) {
                message = "Es wurden " + removeCount + " Angriffe entfernt.";
            }

            JOptionPaneHelper.showInformationBox(jFilterDialog, message, "Information");
        }
        jFilterDialog.setVisible(false);
}//GEN-LAST:event_fireApplyTroopFiltersEvent

    private void fireRemoveTroopFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveTroopFilterEvent
        Object[] selection = jFilterList.getSelectedValues();
        if (selection == null || selection.length == 0) {
            return;
        }
        List<TroopFilterElement> toRemove = new LinkedList<TroopFilterElement>();
        for (Object elem : selection) {
            toRemove.add((TroopFilterElement) elem);
        }
        DefaultListModel filterModel = (DefaultListModel) jFilterList.getModel();
        for (TroopFilterElement elem : toRemove) {
            filterModel.removeElement(elem);
        }
        jFilterList.repaint();
}//GEN-LAST:event_fireRemoveTroopFilterEvent

    private void fireShowFilterDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowFilterDialogEvent
        jFilterDialog.pack();
        jFilterDialog.setLocationRelativeTo(jResultFrame);
        jFilterDialog.setVisible(true);
    }//GEN-LAST:event_fireShowFilterDialogEvent

    public void setCustomAttack(String pAttack) {
        jComandArea.setText(pAttack);
        fireComandDataChangedEvent(null);
    }

    private void buildResults(Hashtable<Village, Date> pTimings, Village pTarget, UnitHolder pUnit) {
        DefaultTableModel resultModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Einheit", "Ziel", "Startzeit"}) {

            Class[] types = new Class[]{
                Village.class, UnitHolder.class, Village.class, Date.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        jResultTable.setModel(resultModel);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultModel);
        jResultTable.setRowSorter(sorter);
        Enumeration<Village> sourceKeys = pTimings.keys();
        while (sourceKeys.hasMoreElements()) {
            Village source = sourceKeys.nextElement();
            Date send = pTimings.get(source);
            resultModel.addRow(new Object[]{source, pUnit, pTarget, send});
        }
        jResultTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        AlternatingColorCellRenderer rend = new AlternatingColorCellRenderer();
        jResultTable.setDefaultRenderer(Village.class, rend);
        jResultTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jResultTable.setRowHeight(20);
        DefaultTableCellRenderer headerRenderer = new SortableTableHeaderRenderer();
        for (int i = 0; i < jResultTable.getColumnCount(); i++) {
            jResultTable.getColumn(jResultTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        jResultFrame.pack();
        jResultFrame.setVisible(true);
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchReTimerFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jApplyFiltersButton;
    private javax.swing.JTextField jArriveField;
    private javax.swing.JDialog jAttackPlanSelectionDialog;
    private javax.swing.JTable jAttackPlanTable;
    private javax.swing.JCheckBox jAxeBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JTextArea jComandArea;
    private javax.swing.JTextField jEstSendTime;
    private javax.swing.JComboBox jExistingAttackPlanBox;
    private javax.swing.JDialog jFilterDialog;
    private javax.swing.JList jFilterList;
    private javax.swing.JComboBox jFilterUnitBox;
    private javax.swing.JCheckBox jHeavyBox;
    private javax.swing.JButton jInsertButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JCheckBox jLightBox;
    private javax.swing.JCheckBox jMainAlwaysOnTopBox;
    private javax.swing.JTextField jMaxValue;
    private javax.swing.JTextField jMinValue;
    private javax.swing.JTextField jNewAttackPlanField;
    private javax.swing.JCheckBox jPalaBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTextPane jParserInfo;
    private javax.swing.JCheckBox jRamBox;
    private javax.swing.JCheckBox jRelationBox;
    private javax.swing.JFrame jResultFrame;
    private javax.swing.JTable jResultTable;
    private javax.swing.JTextField jReturnField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JCheckBox jSnobBox;
    private javax.swing.JTextField jSourceVillage;
    private javax.swing.JCheckBox jSpyBox;
    private javax.swing.JCheckBox jSwordBox;
    private javax.swing.JList jTagList;
    private javax.swing.JTextField jTargetVillage;
    private javax.swing.JComboBox jUnitBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void fireTagsChangedEvent() {
        resetView();
    }
}
