/*
 * DSWorkbenchTroopsFrame.java
 *
 * Created on 2. Oktober 2008, 13:34
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.TroopInfoChartPanel;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.util.Constants;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.log4j.Logger;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.troops.TroopsManagerListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import de.tor.tribes.ui.renderer.NumberFormatCellRenderer;
import de.tor.tribes.ui.renderer.PercentCellRenderer;
import de.tor.tribes.ui.renderer.TribeCellRenderer;
import de.tor.tribes.ui.renderer.VillageCellRenderer;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.TroopInformationToBBCodeFormater;
import de.tor.tribes.util.html.VillageHTMLTooltipGenerator;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.tag.TagManagerListener;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * @TODO (DIFF) Slow troop removing fixed
 * @TODO (DIFF) Supports are updated on new troop parsing
 * @author  Jejkal
 */
public class DSWorkbenchTroopsFrame extends AbstractDSWorkbenchFrame implements TroopsManagerListener, GenericManagerListener {

    private static Logger logger = Logger.getLogger("TroopsDialog");
    private static DSWorkbenchTroopsFrame SINGLETON = null;
    private DefaultTableCellRenderer mHeaderRenderer = null;
    private List<ImageIcon> mColumnIcons = new LinkedList<ImageIcon>();
    private TroopInfoChartPanel infoPanel = null;

    public static synchronized DSWorkbenchTroopsFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchTroopsFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchTroopsFrame */
    DSWorkbenchTroopsFrame() {
        initComponents();

        getContentPane().setBackground(Constants.DS_BACK);

        try {
            jTroopsInformationAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("troops.frame.alwaysOnTop")));
            setAlwaysOnTop(jTroopsInformationAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        //color scrollpanes of selection dialog
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jTroopsTable.setColumnSelectionAllowed(false);
        jTroopsTable.setModel(TroopsManagerTableModel.getSingleton());
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(TroopsManagerTableModel.getSingleton());
        /*sorter.setSortsOnUpdates(false);
        sorter.setMaxSortKeys(2);*/
        jTroopsTable.setRowSorter(sorter);
        jTroopsTable.setDefaultRenderer(Integer.class, new NumberFormatCellRenderer());
        jTroopsTable.setDefaultRenderer(Double.class, new NumberFormatCellRenderer());
        jTroopsTable.setDefaultRenderer(Float.class, new PercentCellRenderer());
        jTroopsTable.setDefaultRenderer(Tribe.class, new TribeCellRenderer());
        jTroopsTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        jTroopsTable.setDefaultRenderer(Date.class, new DateCellRenderer("dd.MM.yy"));
        jTroopsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = jTroopsTable.getSelectedRows().length;
                if (selected == 0) {
                    setTitle("Truppen");
                } else if (selected == 1) {
                    setTitle("Truppen (1 Dorf ausgewählt)");
                } else if (selected > 1) {
                    setTitle("Truppen (" + selected + " Dörfer ausgewählt)");
                }
            }
        });

        try {
            mColumnIcons.add(new ImageIcon("graphics/icons/att.png"));
            mColumnIcons.add(new ImageIcon("graphics/icons/def.png"));
            mColumnIcons.add(new ImageIcon("graphics/icons/def_cav.png"));
            mColumnIcons.add(new ImageIcon("graphics/icons/def_archer.png"));
            mColumnIcons.add(new ImageIcon("graphics/icons/move_out.png"));
            mColumnIcons.add(new ImageIcon("graphics/icons/move_in.png"));
            mColumnIcons.add(new ImageIcon("graphics/icons/farm.png"));
        } catch (Exception e) {
            logger.error("Failed to read table header icons", e);
        }
        jAddTroopsDialog.pack();
        infoPanel = new TroopInfoChartPanel();
        jPanel2.add(infoPanel);
        TagManager.getSingleton().addManagerListener(this);
        jTroopsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireTableSelectionChangedEvent();
                }
            }
        });

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.troops_view", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
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

        jAddTroopsDialog = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        jVillageBox = new javax.swing.JComboBox();
        jAddButton = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jTroopsInformationAlwaysOnTop = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTroopsTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTroopsViewTypeBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTagList = new javax.swing.JList();
        jLabel11 = new javax.swing.JLabel();
        jRelationType = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jSumPane = new javax.swing.JEditorPane();
        jButton6 = new javax.swing.JButton();

        jAddTroopsDialog.setTitle("Dorf  hinzufügen");
        jAddTroopsDialog.setAlwaysOnTop(true);

        jLabel1.setText("Dorf");

        jAddButton.setText("Hinzufügen");
        jAddButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTroopAddActionEvent(evt);
            }
        });

        jButton4.setText("Schließen");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTroopAddActionEvent(evt);
            }
        });

        javax.swing.GroupLayout jAddTroopsDialogLayout = new javax.swing.GroupLayout(jAddTroopsDialog.getContentPane());
        jAddTroopsDialog.getContentPane().setLayout(jAddTroopsDialogLayout);
        jAddTroopsDialogLayout.setHorizontalGroup(
            jAddTroopsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddTroopsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jVillageBox, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAddTroopsDialogLayout.createSequentialGroup()
                .addContainerGap(57, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAddButton)
                .addContainerGap())
        );
        jAddTroopsDialogLayout.setVerticalGroup(
            jAddTroopsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jAddTroopsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAddTroopsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jVillageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jAddTroopsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAddButton)
                    .addComponent(jButton4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle("Truppen");

        jTroopsInformationAlwaysOnTop.setText("Immer im Vordergrund");
        jTroopsInformationAlwaysOnTop.setOpaque(false);
        jTroopsInformationAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireTroopsFrameOnTopEvent(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jTroopsTable.setBackground(new java.awt.Color(236, 233, 216));
        jTroopsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTroopsTable.setToolTipText("");
        jTroopsTable.setOpaque(false);
        jTroopsTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane1.setViewportView(jTroopsTable);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton1.setToolTipText("Gewählte Truppeninformationen entfernen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTroopsEvent(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton2.setToolTipText("Neues Dorf ohne Truppeninformationen hinzufügen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddTroopsEvent(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(239, 235, 223));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/center.png"))); // NOI18N
        jButton3.setToolTipText("Gewähltes Dorf auf der Karte zentrieren");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterSelectionEvent(evt);
            }
        });

        jTroopsViewTypeBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Im Dorf", "Eigene", "Außerhalb", "Unterwegs", "Unterstützung" }));
        jTroopsViewTypeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeViewTypeEvent(evt);
            }
        });

        jLabel2.setText("Angezeigte Truppen");

        jPanel2.setLayout(new java.awt.BorderLayout());

        jLabel3.setBackground(new java.awt.Color(0, 255, 0));
        jLabel3.setMaximumSize(new java.awt.Dimension(14, 14));
        jLabel3.setMinimumSize(new java.awt.Dimension(14, 14));
        jLabel3.setOpaque(true);
        jLabel3.setPreferredSize(new java.awt.Dimension(14, 14));

        jLabel4.setText("Unterstützung");

        jLabel5.setBackground(new java.awt.Color(0, 0, 255));
        jLabel5.setMaximumSize(new java.awt.Dimension(14, 14));
        jLabel5.setMinimumSize(new java.awt.Dimension(14, 14));
        jLabel5.setOpaque(true);
        jLabel5.setPreferredSize(new java.awt.Dimension(14, 14));

        jLabel6.setText("Eigene");

        jLabel7.setBackground(new java.awt.Color(255, 255, 0));
        jLabel7.setMaximumSize(new java.awt.Dimension(14, 14));
        jLabel7.setMinimumSize(new java.awt.Dimension(14, 14));
        jLabel7.setOpaque(true);
        jLabel7.setPreferredSize(new java.awt.Dimension(14, 14));

        jLabel8.setText("Außerhalb");

        jLabel9.setBackground(new java.awt.Color(255, 0, 0));
        jLabel9.setMaximumSize(new java.awt.Dimension(14, 14));
        jLabel9.setMinimumSize(new java.awt.Dimension(14, 14));
        jLabel9.setOpaque(true);
        jLabel9.setPreferredSize(new java.awt.Dimension(14, 14));

        jLabel10.setText("Unterwegs");

        jButton5.setBackground(new java.awt.Color(239, 235, 223));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_clipboardBB.png"))); // NOI18N
        jButton5.setToolTipText("Truppeninformationen als BB-Code in die Zwischenablage kopieren");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyTroopInformationToClipboardEvent(evt);
            }
        });

        jScrollPane2.setViewportView(jTagList);

        jLabel11.setText("Dorfgruppen");

        jRelationType.setSelected(true);
        jRelationType.setText("Verknüpfung");
        jRelationType.setToolTipText("Verknüpfung der gewählten Dorfgruppen (UND = Dorf muss in allen Gruppen sein, ODER = Dorf muss in mindestens einer Gruppe sein)");
        jRelationType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jRelationType.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_or.png"))); // NOI18N
        jRelationType.setOpaque(false);
        jRelationType.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/logic_and.png"))); // NOI18N
        jRelationType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireRelationChangedEvent(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Summe"));
        jPanel3.setOpaque(false);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jSumPane.setBorder(null);
        jSumPane.setContentType("text/html");
        jSumPane.setEditable(false);
        jScrollPane3.setViewportView(jSumPane);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
        );

        jButton6.setBackground(new java.awt.Color(239, 235, 223));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/place.png"))); // NOI18N
        jButton6.setToolTipText("Truppenübersicht des Versammlungsplatzes im Spiel öffnen");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireOpenPlaceInGameEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10)))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(39, 39, 39)
                                .addComponent(jLabel11)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(jTroopsViewTypeBox, 0, 250, Short.MAX_VALUE)
                            .addComponent(jRelationType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 733, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4))
                                .addGap(14, 14, 14)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTroopsViewTypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRelationType)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTroopsInformationAlwaysOnTop, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTroopsInformationAlwaysOnTop)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireTroopsFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireTroopsFrameOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireTroopsFrameOnTopEvent

private void fireRemoveTroopsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveTroopsEvent
    int[] rows = jTroopsTable.getSelectedRows();
    if (rows.length == 0) {
        return;
    }

    String message = ((rows.length == 1) ? "Eintrag " : (rows.length + " Einträge ")) + "wirklich löschen?";
    if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Truppeninformationen entfernen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
        return;
    }

    jTroopsTable.editingCanceled(new ChangeEvent(this));
    jTroopsTable.invalidate();
    List<Integer> vIds = new LinkedList<Integer>();
    for (int r = rows.length - 1; r >= 0; r--) {
        int row = jTroopsTable.convertRowIndexToModel(rows[r]);
        vIds.add(row);
    }
    TroopsManagerTableModel.getSingleton().removeRows(vIds.toArray(new Integer[]{}));

    jTroopsTable.revalidate();
    jTroopsTable.repaint();
}//GEN-LAST:event_fireRemoveTroopsEvent

private void fireAddTroopsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddTroopsEvent
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    boolean empty = true;
    try {
        String playerID = GlobalOptions.getSelectedProfile().getTribeName();
        Tribe t = DataHolder.getSingleton().getTribeByName(playerID);
        Village[] villages = t.getVillageList();
        Arrays.sort(villages, Village.CASE_INSENSITIVE_ORDER);
        for (Village v : villages) {
            if (TroopsManager.getSingleton().getTroopsForVillage(v) == null) {
                model.addElement(v);
                empty = false;
            }
        }
    } catch (Exception e) {
        logger.error("Failed to update tribe villages model", e);
        model = new DefaultComboBoxModel(new String[]{"Keine Dörfer gefunden"});
    }
    if (empty) {
        JOptionPaneHelper.showInformationBox(this, "Es sind bereits Truppeninformationen zu allen deinen Dörfern vorhanden.", "Information");
        return;
    }
    jVillageBox.setModel(model);
    jAddTroopsDialog.setLocationRelativeTo(this);
    jAddTroopsDialog.setVisible(true);
}//GEN-LAST:event_fireAddTroopsEvent

private void fireTroopAddActionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTroopAddActionEvent
    if (evt.getSource() == jAddButton) {
        try {
            Village v = (Village) jVillageBox.getSelectedItem();
            if (v != null) {
                int units = DataHolder.getSingleton().getUnits().size();
                List<Integer> emptyUnitList = new LinkedList<Integer>();
                for (int i = 0; i < units; i++) {
                    emptyUnitList.add(0);
                }

                if (TroopsManager.getSingleton().getTroopsForVillage(v) == null) {
                    TroopsManager.getSingleton().addTroopsForVillage(v, emptyUnitList);
                    JOptionPaneHelper.showInformationBox(jAddTroopsDialog, "Truppen hinzugefügt", "Information");
                } else {
                    JOptionPaneHelper.showInformationBox(jAddTroopsDialog, "Für das gewählte Dorf sind bereits Truppeninformationen vorhanden.", "Information");
                }

            }
        } catch (Exception e) {
            logger.error("Failed to add empty troop list", e);
            JOptionPaneHelper.showWarningBox(jAddTroopsDialog, "Fehler beim hinzufügen der Truppen", "Warnung");
        }

    } else {
        jAddTroopsDialog.setVisible(false);
    }
}//GEN-LAST:event_fireTroopAddActionEvent

private void fireCenterSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterSelectionEvent
    try {
        int row = jTroopsTable.convertRowIndexToModel(jTroopsTable.getSelectedRow());
        Village v = (Village) jTroopsTable.getModel().getValueAt(row, 1);
        DSWorkbenchMainFrame.getSingleton().centerVillage(v);
    } catch (Exception e) {
        logger.error("Failed to center village", e);
    }

}//GEN-LAST:event_fireCenterSelectionEvent

private void fireChangeViewTypeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeViewTypeEvent
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        int index = jTroopsViewTypeBox.getSelectedIndex();
        if (index == -1) {
            jTroopsViewTypeBox.setSelectedIndex(0);
            index = 0;
        }

        TroopsManagerTableModel.getSingleton().setViewType(index);
        updateSumTooltip();
    }
}//GEN-LAST:event_fireChangeViewTypeEvent

private void fireCopyTroopInformationToClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyTroopInformationToClipboardEvent
    try {
        int[] rows = jTroopsTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }

        boolean copyAll = true;
        int index = jTroopsViewTypeBox.getSelectedIndex();
        if (index == -1) {
            jTroopsViewTypeBox.setSelectedIndex(0);
            index = 0;
        }
        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Welche Informationen möchtest du kopieren?", "Datenauswahl", "Aktuelle Ansicht", "Alle") == JOptionPane.NO_OPTION) {
            copyAll = false;

        }
        boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);
        String sUrl = ServerManager.getServerURL(GlobalOptions.getSelectedServer());

        String result = "";
        for (int row : rows) {
            int r = jTroopsTable.convertRowIndexToModel(row);
            Village v = (Village) jTroopsTable.getModel().getValueAt(r, 1);
            result += TroopInformationToBBCodeFormater.formatTroopInformation(v, index, copyAll, sUrl, extended) + "\n";
        }

        StringTokenizer t = new StringTokenizer(result, "[");
        int cnt = t.countTokens();
        if (cnt > 500) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Truppeninformationen benötigen mehr als 500 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                return;
            }
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
        JOptionPaneHelper.showInformationBox(this, "Daten in Zwischenablage kopiert.", "Information");
    } catch (Exception e) {
        logger.error("Failed to copy troop information to clipboard", e);
        String result = "Fehler beim Kopieren in die Zwischenablage.";
        JOptionPaneHelper.showErrorBox(this, result, "Fehler");
    }
}//GEN-LAST:event_fireCopyTroopInformationToClipboardEvent

private void fireRelationChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireRelationChangedEvent
    jTagList.getSelectionModel().clearSelection();
}//GEN-LAST:event_fireRelationChangedEvent

private void fireOpenPlaceInGameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireOpenPlaceInGameEvent
    int row = jTroopsTable.convertRowIndexToModel(jTroopsTable.getSelectedRow());
    Village v = (Village) jTroopsTable.getModel().getValueAt(row, 1);
    BrowserCommandSender.openPlaceTroopsView(v);
}//GEN-LAST:event_fireOpenPlaceInGameEvent

    public List<Village> getSelectedTroopsVillages() {
        List<Village> villages = new LinkedList<Village>();
        int[] rows = jTroopsTable.getSelectedRows();
        if (rows == null) {
            return villages;
        }

        for (int row : rows) {
            row = jTroopsTable.convertRowIndexToModel(row);
            Village v = (Village) TroopsManagerTableModel.getSingleton().getValueAt(row, 1);
            villages.add(v);
        }
        return villages;
    }

    protected void fireTableSelectionChangedEvent() {
        try {
            int row = jTroopsTable.getSelectedRow();
            if (row == -1) {
                return;
            }
            Village v = (Village) jTroopsTable.getValueAt(row, 1);
            VillageTroopsHolder holder = null;
            if (v != null) {
                holder = TroopsManager.getSingleton().getTroopsForVillage(v);
            }
            if (holder != null) {
                infoPanel.setVillage(v);
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        infoPanel.updateUI();
                    }
                });

                int own = holder.getTroopPopCount(TroopsManagerTableModel.SHOW_OWN_TROOPS);
                int inVillage = holder.getTroopPopCount(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
                int outside = holder.getTroopPopCount(TroopsManagerTableModel.SHOW_TROOPS_OUTSIDE);
                int ontheway = holder.getTroopPopCount(TroopsManagerTableModel.SHOW_TROOPS_ON_THE_WAY);
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                nf.setMinimumFractionDigits(0);
                jLabel4.setText("Unterstützung (" + nf.format(((inVillage - own) >= 0) ? (inVillage - own) : 0) + ")");
                jLabel6.setText("Eigene (" + nf.format(own) + ")");
                jLabel8.setText("Außerhalb (" + nf.format(outside) + ")");
                jLabel10.setText("Unterwegs (" + nf.format(ontheway) + ")");
            } else {
                jLabel4.setText("Unterstützung");
                jLabel6.setText("Eigene");
                jLabel8.setText("Außerhalb");
                jLabel10.setText("Unterwegs");
            }

        } catch (Exception e) {
        }
    }

    public void resetView() {
        jTroopsTable.invalidate();
        jTroopsTable.setModel(new DefaultTableModel());
        jTroopsTable.revalidate();
        jTroopsTable.invalidate();
        jTroopsTable.setModel(TroopsManagerTableModel.getSingleton());
        TroopsManager.getSingleton().addTroopsManagerListener(this);
        //setup renderer and general view
        jTroopsTable.getTableHeader().setReorderingAllowed(false);

        mHeaderRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                c.setBackground(Constants.DS_BACK);
                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                int unitCount = DataHolder.getSingleton().getUnits().size();
                r.setHorizontalAlignment(JLabel.CENTER);
                if (column < 3) {
                    r.setText("<html><b>" + r.getText() + "</b></html>");
                } else if (column < 3 + unitCount) {
                    try {
                        r.setIcon(ImageManager.getUnitIcon(column - 3));
                    } catch (Exception e) {
                        r.setText(DataHolder.getSingleton().getUnits().get(column - 3).getName());
                    }

                } else {
                    if (column == unitCount + 3) {
                        //off col
                        r.setIcon(mColumnIcons.get(0));
                    } else if (column == unitCount + 4) {
                        //def col
                        r.setIcon(mColumnIcons.get(1));
                    } else if (column == unitCount + 5) {
                        //def cav col
                        r.setIcon(mColumnIcons.get(2));
                    } else if (column == unitCount + 6) {
                        //def archer col
                        r.setIcon(mColumnIcons.get(3));
                    } else if (column == unitCount + 7) {
                        //target villages
                        r.setIcon(mColumnIcons.get(4));
                    } else if (column == unitCount + 8) {
                        //source villages
                        r.setIcon(mColumnIcons.get(5));
                    } else if (column == unitCount + 9) {
                        //farm place
                        r.setIcon(mColumnIcons.get(6));
                    }
                }
                return r;
            }
        };

        for (int i = 0; i < jTroopsTable.getColumnCount(); i++) {
            TableColumn column = jTroopsTable.getColumnModel().getColumn(i);
            column.setHeaderRenderer(mHeaderRenderer);
            if ((i > 2 && i < DataHolder.getSingleton().getUnits().size() + 3)) {
                column.setWidth(60);
                column.setPreferredWidth(60);
                //column.setResizable(false);
            }
        }

        buildTagList();
        jTagList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object[] tags = jTagList.getSelectedValues();
                List<Tag> selection = new LinkedList<Tag>();
                for (Object tag : tags) {
                    selection.add((Tag) tag);
                }
                TroopsManagerTableModel.getSingleton().setVisibleTags(selection, jRelationType.isSelected());
                updateSumTooltip();
                TroopsManagerTableModel.getSingleton().fireTableDataChanged();
            }
        });

        TroopsManagerTableModel.getSingleton().updateSum();
        updateSumTooltip();
        TroopsManager.getSingleton().forceUpdate();
        TroopsManagerTableModel.getSingleton().fireTableDataChanged();
        jTroopsTable.revalidate();
    }

    private void updateSumTooltip() {
        String tooltip = "<html>";
        tooltip += "<table style=\"background-color:#FFFFFF;font-size:95%;font-family:Verdana\">";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            tooltip += "<tr>";
            tooltip += "<td><div align=\"center\"><img src=\"" + VillageHTMLTooltipGenerator.class.getResource("/res/ui/" + unit.getPlainName() + ".png") + "\"/></div></td>";
            Integer v = TroopsManagerTableModel.getSingleton().getSummedAmounts().get(unit);
            if (v == null) {
                v = 0;
            }
            tooltip += "<td><div align=\"center\">" + nf.format(v) + "</div></td>";
            tooltip += "</tr>";
        }

        tooltip += "</table>";
        tooltip += "</html>";
        jSumPane.setText(tooltip);
    }

    private void buildTagList() {
        DefaultListModel model = new DefaultListModel();
        model.addElement(NoTag.getSingleton());
        for (ManageableType e : TagManager.getSingleton().getAllElements()) {
            Tag t = (Tag) e;
            model.addElement(t);
        }
        jTagList.setModel(model);
        jTagList.getSelectionModel().clearSelection();
    }

    @Override
    public void fireTroopsChangedEvent() {
        try {
            jTroopsTable.invalidate();
            jTroopsTable.getTableHeader().setReorderingAllowed(false);

            for (int i = 0; i < jTroopsTable.getColumnCount(); i++) {
                jTroopsTable.getColumnModel().getColumn(i).setHeaderRenderer(mHeaderRenderer);
            }

            jTroopsTable.revalidate();
            updateSumTooltip();
            jTroopsTable.repaint();
        } catch (Exception e) {
            logger.error("Failed to update troops table", e);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Gesture handling">
    @Override
    public void fireExportAsBBGestureEvent() {
        fireCopyTroopInformationToClipboardEvent(null);
    }

    @Override
    public void fireNextPageGestureEvent() {
        int current = jTroopsViewTypeBox.getSelectedIndex();
        int size = jTroopsViewTypeBox.getModel().getSize();
        if (current + 1 > size - 1) {
            current = 0;
        } else {
            current += 1;
        }
        jTroopsViewTypeBox.setSelectedIndex(current);
        fireChangeViewTypeEvent(new ItemEvent(jTroopsViewTypeBox, 0, null, ItemEvent.SELECTED));
    }

    @Override
    public void firePreviousPageGestureEvent() {
        int current = jTroopsViewTypeBox.getSelectedIndex();
        int size = jTroopsViewTypeBox.getModel().getSize();
        if (current - 1 < 0) {
            current = size - 1;
        } else {
            current -= 1;
        }
        jTroopsViewTypeBox.setSelectedIndex(current);
        fireChangeViewTypeEvent(new ItemEvent(jTroopsViewTypeBox, 0, null, ItemEvent.SELECTED));
    }
// </editor-fold>
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAddButton;
    private javax.swing.JDialog jAddTroopsDialog;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JCheckBox jRelationType;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JEditorPane jSumPane;
    private javax.swing.JList jTagList;
    private javax.swing.JCheckBox jTroopsInformationAlwaysOnTop;
    private javax.swing.JTable jTroopsTable;
    private javax.swing.JComboBox jTroopsViewTypeBox;
    private javax.swing.JComboBox jVillageBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        buildTagList();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
}
