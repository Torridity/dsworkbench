/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchMerchantDistibutor.java
 *
 * Created on 18.06.2010, 08:12:57
 */
package de.tor.tribes.ui.views;

import com.jidesoft.swing.JideTabbedPane;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.renderer.BooleanCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.NumberFormatCellRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.ui.renderer.TradeDirectionCellRenderer;
import de.tor.tribes.ui.renderer.TransportCellRenderer;
import de.tor.tribes.ui.renderer.VillageCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.algo.MerchantDestination;
import de.tor.tribes.util.algo.MerchantDistributor;
import de.tor.tribes.util.algo.MerchantSource;
import de.tor.tribes.util.algo.Order;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;

/**
 *@TODO (DIFF) Added merchant distributor
 * @TODO (DIFF) Add "ignore transports < X" field
 * @TODO (DIFF) Add confirm box on removing villages
 * @TODO (DIFF) Add confirm box on impossible calculation
 * @author Jejkal
 */
public class DSWorkbenchMerchantDistibutor extends AbstractDSWorkbenchFrame implements ListSelectionListener, ActionListener {

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jMerchantTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Eintrag gewählt" : " Einträge gewählt"));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Paste")) {
            readMerchantInfoFromClipboard();
        } else if (e.getActionCommand().equals("Delete")) {
            removeSelection();
        }
    }
    private static Logger logger = Logger.getLogger("MerchantDistributor");
    private static DSWorkbenchMerchantDistibutor SINGLETON = null;
    private List<VillageMerchantInfo> merchantInfos = new LinkedList<VillageMerchantInfo>();
    private int iClickAccount = 0;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchMerchantDistibutor getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchMerchantDistibutor();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchMerchantDistibutor */
    DSWorkbenchMerchantDistibutor() {
        initComponents();
        centerPanel = new GenericTestPanel(true);
        jMerchantPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildPanel(merchantTabbedPane);
        buildMenu();

        merchantTabbedPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        merchantTabbedPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        merchantTabbedPane.setBoldActiveTab(true);
        merchantTabbedPane.addTab("Eingelesene Dörfer", jxMerchantTablePanel);
        merchantTabbedPane.addTab("Errechnete Transporte", jXResultTransportsPanel);
        merchantTabbedPane.addTab("Resultierende Rohstoffverteilung", jXResultDistributionPanel);

        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jMerchantTable.registerKeyboardAction(DSWorkbenchMerchantDistibutor.this, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jMerchantTable.registerKeyboardAction(DSWorkbenchMerchantDistibutor.this, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jMerchantTable.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //ignore find
            }
        });

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        //  GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.merchant_distributor", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
    }

    @Override
    public void resetView() {
        merchantInfos.clear();
        rebuildTable(jMerchantTable, merchantInfos);
        buildResults(new LinkedList<VillageMerchantInfo>(), new LinkedList<List<MerchantSource>>(), new int[]{0, 0, 0});
    }

    private void buildMenu() {
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bearbeiten");
        JXButton bothButton = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/trade_both.png")));
        bothButton.setToolTipText("Ändert die Handelsrichtung für die gewählten Einträge in 'Lieferant und Empfänger'");
        bothButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                changeDirection(VillageMerchantInfo.Direction.BOTH);
            }
        });
        editPane.getContentPane().add(bothButton);

        JXButton inButton = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/trade_in.png")));
        inButton.setToolTipText("Ändert die Handelsrichtung für die gewählten Einträge in 'Empfänger'");
        inButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                changeDirection(VillageMerchantInfo.Direction.INCOMING);
            }
        });
        editPane.getContentPane().add(inButton);
        JXButton outButton = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/trade_out.png")));
        outButton.setToolTipText("Ändert die Handelsrichtung für die gewählten Einträge in 'Lieferant'");
        outButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                changeDirection(VillageMerchantInfo.Direction.OUTGOING);
            }
        });
        editPane.getContentPane().add(outButton);




        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");
        JXButton toBrowser = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/att_browser.png")));
        toBrowser.setToolTipText("<html>Markierte Transporte in den Browser &uuml;bertragen. Im Normalfall werden nur einzelne Transporte &uuml;bertragen. F&uuml;r das &Uuml;bertragen mehrerer Transporte ist zuerst das Klickkonto entsprechend zu f&uuml;llen</html>");
        toBrowser.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                transferSelectionToBrowser();
            }
        });
        transferPane.getContentPane().add(toBrowser);

        JXTaskPane miscPane = new JXTaskPane();
        transferPane.setTitle("Sonstiges");
        JXButton calculateButton = new JXButton(new ImageIcon(DSWorkbenchTagFrame.class.getResource("/res/ui/att_validate.png")));
        calculateButton.setToolTipText("Startet die Berechnung möglicher Transporte");
        calculateButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                calculateTransports();
            }
        });
        miscPane.getContentPane().add(calculateButton);

        centerPanel.setupTaskPane(editPane, transferPane, miscPane);
    }

    private void changeDirection(VillageMerchantInfo.Direction pDirection) {
        int[] selectedRows = jMerchantTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            showInfo("Keine Einträge ausgewählt");
            return;
        }

        for (Integer selectedRow : selectedRows) {
            int row = jMerchantTable.convertRowIndexToModel(selectedRow);
            merchantInfos.get(row).setDirection(pDirection);
        }
        rebuildTable(jMerchantTable, merchantInfos);
        showSuccess("Handelsrichtung angepasst");
    }

    private void transferSelectionToBrowser() {
    }

    private void calculateTransports() {
        //@TODO change settings dialog to fit Nimbus design
        if (merchantInfos.size() < 2) {
            showError("Es müssen mindestens 2 Dörfer eingetragen sein");
            return;
        }
        boolean haveInc = false;
        boolean haveSend = false;
        boolean haveDual = false;
        for (VillageMerchantInfo info : merchantInfos) {
            if (info.getDirection() == VillageMerchantInfo.Direction.INCOMING) {
                haveInc = true;
            } else if (info.getDirection() == VillageMerchantInfo.Direction.OUTGOING) {
                haveSend = true;
            } else {
                haveDual = true;
            }
        }

        if (!haveInc && !haveDual) {
            showError("Keine Rohstoffempfänger angegeben");
            return;
        } else if (!haveSend && !haveDual) {
            showError("Keine Rohstofflieferanten angegeben");
            return;
        }

        jCalculationSettingsDialog.pack();
        jCalculationSettingsDialog.setLocationRelativeTo(DSWorkbenchMerchantDistibutor.this);
        jCalculationSettingsDialog.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMerchantDataTable = new javax.swing.JTable();
        jTaskPane1 = new com.l2fprod.common.swing.JTaskPane();
        jTaskPaneGroup2 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jInsertBothButton = new javax.swing.JButton();
        jInsertReceiveButton = new javax.swing.JButton();
        jInsertSendButton = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTaskPaneGroup1 = new com.l2fprod.common.swing.JTaskPaneGroup();
        jTradeBothButton = new javax.swing.JButton();
        jTradeInButton = new javax.swing.JButton();
        jTradeOutButton = new javax.swing.JButton();
        jxMerchantTablePanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jClickAccountLabel = new javax.swing.JLabel();
        merchantTabbedPane = new com.jidesoft.swing.JideTabbedPane();
        jXResultTransportsPanel = new org.jdesktop.swingx.JXPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jResultsTable = new org.jdesktop.swingx.JXTable();
        jXResultDistributionPanel = new org.jdesktop.swingx.JXPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jUsedMerchants = new javax.swing.JLabel();
        jUsedTransports = new javax.swing.JLabel();
        jPerfectResults = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jResultsDataTable = new org.jdesktop.swingx.JXTable();
        jCalculationSettingsDialog = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        jEqualDistribution = new javax.swing.JRadioButton();
        jAdjustingDistribution = new javax.swing.JRadioButton();
        jTargetWood = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTargetClay = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTargetIron = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jRemainWood = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jRemainClay = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jRemainIron = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jMinTransportAmount = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jIgnoreTransportsButton = new javax.swing.JCheckBox();
        jMaxFilling = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jCalculateButton = new javax.swing.JButton();
        jMerchantPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();
        jCheckBox1 = new javax.swing.JCheckBox();

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jMerchantDataTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jMerchantDataTable);

        jTaskPane1.setBackground(new java.awt.Color(239, 235, 223));
        com.l2fprod.common.swing.PercentLayout percentLayout1 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout1.setGap(14);
        percentLayout1.setOrientation(1);
        jTaskPane1.setLayout(percentLayout1);

        jTaskPaneGroup2.setTitle("Einlesen");
        com.l2fprod.common.swing.PercentLayout percentLayout3 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout3.setGap(2);
        percentLayout3.setOrientation(1);
        jTaskPaneGroup2.getContentPane().setLayout(percentLayout3);

        jInsertBothButton.setBackground(new java.awt.Color(239, 235, 223));
        jInsertBothButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/res_both_from_clipboard.png"))); // NOI18N
        jInsertBothButton.setToolTipText("Daten aus der Zwischenablage lesen (Empfang und Lieferung)");
        jInsertBothButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReadDataEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jInsertBothButton);

        jInsertReceiveButton.setBackground(new java.awt.Color(239, 235, 223));
        jInsertReceiveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/res_in_from_clipboard.png"))); // NOI18N
        jInsertReceiveButton.setToolTipText("Daten aus der Zwischenablage lesen (Nur Empfang)");
        jInsertReceiveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReadDataEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jInsertReceiveButton);

        jInsertSendButton.setBackground(new java.awt.Color(239, 235, 223));
        jInsertSendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/res_out_from_clipboard.png"))); // NOI18N
        jInsertSendButton.setToolTipText("Daten aus der Zwischenablage lesen (Nur Lieferung)");
        jInsertSendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReadDataEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jInsertSendButton);

        jButton5.setBackground(new java.awt.Color(239, 235, 223));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton5.setToolTipText("Markierte Einträge löschen");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveMarkedEntriesEvent(evt);
            }
        });
        jTaskPaneGroup2.getContentPane().add(jButton5);

        jTaskPane1.add(jTaskPaneGroup2);

        jTaskPaneGroup1.setTitle("Herkunft/Ziel");
        com.l2fprod.common.swing.PercentLayout percentLayout2 = new com.l2fprod.common.swing.PercentLayout();
        percentLayout2.setGap(2);
        percentLayout2.setOrientation(1);
        jTaskPaneGroup1.getContentPane().setLayout(percentLayout2);

        jTradeBothButton.setBackground(new java.awt.Color(239, 235, 223));
        jTradeBothButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/trade_both.png"))); // NOI18N
        jTradeBothButton.setToolTipText("Markierte Dörfer sind Ressourcenlieferer und -empfänger");
        jTradeBothButton.setMaximumSize(new java.awt.Dimension(57, 33));
        jTradeBothButton.setMinimumSize(new java.awt.Dimension(57, 33));
        jTradeBothButton.setPreferredSize(new java.awt.Dimension(57, 33));
        jTradeBothButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeTradeDirectionEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jTradeBothButton);

        jTradeInButton.setBackground(new java.awt.Color(239, 235, 223));
        jTradeInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/trade_in.png"))); // NOI18N
        jTradeInButton.setToolTipText("Markierte Dörfer sind nur Ressourcenempfänger");
        jTradeInButton.setMaximumSize(new java.awt.Dimension(57, 33));
        jTradeInButton.setMinimumSize(new java.awt.Dimension(57, 33));
        jTradeInButton.setPreferredSize(new java.awt.Dimension(57, 33));
        jTradeInButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeTradeDirectionEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jTradeInButton);

        jTradeOutButton.setBackground(new java.awt.Color(239, 235, 223));
        jTradeOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/trade_out.png"))); // NOI18N
        jTradeOutButton.setToolTipText("Markierte Dörfer sind nur Ressourcenlieferer");
        jTradeOutButton.setMaximumSize(new java.awt.Dimension(57, 33));
        jTradeOutButton.setMinimumSize(new java.awt.Dimension(57, 33));
        jTradeOutButton.setPreferredSize(new java.awt.Dimension(57, 33));
        jTradeOutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireChangeTradeDirectionEvent(evt);
            }
        });
        jTaskPaneGroup1.getContentPane().add(jTradeOutButton);

        jTaskPane1.add(jTaskPaneGroup1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTaskPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTaskPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(212, 212, 212)))
                .addContainerGap())
        );

        jxMerchantTablePanel.setLayout(new java.awt.BorderLayout());

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jXLabel1fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jxMerchantTablePanel.add(infoPanel, java.awt.BorderLayout.SOUTH);

        jMerchantTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(jMerchantTable);

        jxMerchantTablePanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jClickAccountLabel.setBackground(new java.awt.Color(255, 255, 255));
        jClickAccountLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/LeftClick.png"))); // NOI18N
        jClickAccountLabel.setText("Klick-Konto [0]");
        jClickAccountLabel.setToolTipText("0 Klick(s) aufgeladen");
        jClickAccountLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jClickAccountLabel.setMaximumSize(new java.awt.Dimension(110, 40));
        jClickAccountLabel.setMinimumSize(new java.awt.Dimension(110, 40));
        jClickAccountLabel.setOpaque(true);
        jClickAccountLabel.setPreferredSize(new java.awt.Dimension(110, 40));
        jClickAccountLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireClickAccountChangedEvent(evt);
            }
        });

        jXResultTransportsPanel.setLayout(new java.awt.BorderLayout());

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
        jScrollPane3.setViewportView(jResultsTable);

        jXResultTransportsPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jXResultDistributionPanel.setLayout(new java.awt.BorderLayout());

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Sonstige Ergebnisse"));
        jPanel4.setOpaque(false);

        jLabel8.setText("Verwendete Händler");

        jLabel9.setText("Verwendete Transporte");

        jLabel10.setText("Optimale Ergebnisse");

        jUsedMerchants.setMaximumSize(new java.awt.Dimension(40, 14));
        jUsedMerchants.setMinimumSize(new java.awt.Dimension(40, 14));
        jUsedMerchants.setPreferredSize(new java.awt.Dimension(40, 14));

        jUsedTransports.setMaximumSize(new java.awt.Dimension(40, 14));
        jUsedTransports.setMinimumSize(new java.awt.Dimension(40, 14));
        jUsedTransports.setPreferredSize(new java.awt.Dimension(40, 14));

        jPerfectResults.setToolTipText("Dörfer in denen sich nach den Transporten die geforderte Anzahl von Rohstoffen befindet (Vorausgesetzt, dass keine Transporte ignoriert werden)");
        jPerfectResults.setMaximumSize(new java.awt.Dimension(40, 14));
        jPerfectResults.setMinimumSize(new java.awt.Dimension(40, 14));
        jPerfectResults.setPreferredSize(new java.awt.Dimension(40, 14));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jUsedMerchants, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                    .addComponent(jUsedTransports, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                    .addComponent(jPerfectResults, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jUsedMerchants, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jUsedTransports, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jPerfectResults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jXResultDistributionPanel.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jResultsDataTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(jResultsDataTable);

        jXResultDistributionPanel.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(560, 100));

        buttonGroup1.add(jEqualDistribution);
        jEqualDistribution.setSelected(true);
        jEqualDistribution.setText("Gleichverteilung");
        jEqualDistribution.setOpaque(false);
        jEqualDistribution.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireCalculationTypeChangedEvent(evt);
            }
        });

        buttonGroup1.add(jAdjustingDistribution);
        jAdjustingDistribution.setText("Gewünschter Lagerbestand");
        jAdjustingDistribution.setOpaque(false);
        jAdjustingDistribution.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireCalculationTypeChangedEvent(evt);
            }
        });

        jTargetWood.setText("400000");
        jTargetWood.setEnabled(false);
        jTargetWood.setMaximumSize(new java.awt.Dimension(50, 20));
        jTargetWood.setMinimumSize(new java.awt.Dimension(50, 20));
        jTargetWood.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/holz.png"))); // NOI18N
        jLabel1.setEnabled(false);

        jTargetClay.setText("400000");
        jTargetClay.setEnabled(false);
        jTargetClay.setMaximumSize(new java.awt.Dimension(50, 20));
        jTargetClay.setMinimumSize(new java.awt.Dimension(50, 20));
        jTargetClay.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/lehm.png"))); // NOI18N
        jLabel2.setEnabled(false);

        jTargetIron.setText("400000");
        jTargetIron.setEnabled(false);
        jTargetIron.setMaximumSize(new java.awt.Dimension(50, 20));
        jTargetIron.setMinimumSize(new java.awt.Dimension(50, 20));
        jTargetIron.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/eisen.png"))); // NOI18N
        jLabel3.setEnabled(false);

        jRemainWood.setText("100000");
        jRemainWood.setEnabled(false);
        jRemainWood.setMaximumSize(new java.awt.Dimension(50, 20));
        jRemainWood.setMinimumSize(new java.awt.Dimension(50, 20));
        jRemainWood.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/lehm.png"))); // NOI18N
        jLabel4.setEnabled(false);

        jRemainClay.setText("100000");
        jRemainClay.setEnabled(false);
        jRemainClay.setMaximumSize(new java.awt.Dimension(50, 20));
        jRemainClay.setMinimumSize(new java.awt.Dimension(50, 20));
        jRemainClay.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/eisen.png"))); // NOI18N
        jLabel5.setEnabled(false);

        jRemainIron.setText("100000");
        jRemainIron.setEnabled(false);
        jRemainIron.setMaximumSize(new java.awt.Dimension(50, 20));
        jRemainIron.setMinimumSize(new java.awt.Dimension(50, 20));
        jRemainIron.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/holz.png"))); // NOI18N
        jLabel6.setEnabled(false);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Min. Füllstand");
        jLabel7.setEnabled(false);

        jMinTransportAmount.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jMinTransportAmount.setText("10000");
        jMinTransportAmount.setMaximumSize(new java.awt.Dimension(40, 20));
        jMinTransportAmount.setMinimumSize(new java.awt.Dimension(40, 20));
        jMinTransportAmount.setPreferredSize(new java.awt.Dimension(40, 20));

        jLabel14.setText("Rohstoffen ignorieren.");

        jIgnoreTransportsButton.setText("Transporte mit weniger als");
        jIgnoreTransportsButton.setOpaque(false);

        jMaxFilling.setText("95");
        jMaxFilling.setMinimumSize(new java.awt.Dimension(59, 20));

        jLabel13.setText("Maximaler Füllstand");

        jLabel15.setText("%");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jIgnoreTransportsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jMinTransportAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jMaxFilling, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                            .addComponent(jAdjustingDistribution, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jEqualDistribution, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTargetWood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTargetClay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTargetIron, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRemainWood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRemainClay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRemainIron, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(115, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jEqualDistribution)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAdjustingDistribution)
                    .addComponent(jTargetWood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jTargetClay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jTargetIron, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jRemainIron, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRemainWood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRemainClay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jMinTransportAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jIgnoreTransportsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jMaxFilling, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(20, 20, 20))
        );

        jButton6.setBackground(new java.awt.Color(239, 235, 223));
        jButton6.setText("Abbrechen");
        jButton6.setToolTipText("");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateEvent(evt);
            }
        });

        jCalculateButton.setBackground(new java.awt.Color(239, 235, 223));
        jCalculateButton.setText("Berechnen");
        jCalculateButton.setToolTipText("");
        jCalculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCalculateEvent(evt);
            }
        });

        javax.swing.GroupLayout jCalculationSettingsDialogLayout = new javax.swing.GroupLayout(jCalculationSettingsDialog.getContentPane());
        jCalculationSettingsDialog.getContentPane().setLayout(jCalculationSettingsDialogLayout);
        jCalculationSettingsDialogLayout.setHorizontalGroup(
            jCalculationSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jCalculationSettingsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCalculationSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                    .addGroup(jCalculationSettingsDialogLayout.createSequentialGroup()
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCalculateButton)))
                .addContainerGap())
        );
        jCalculationSettingsDialogLayout.setVerticalGroup(
            jCalculationSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCalculationSettingsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jCalculationSettingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCalculateButton)
                    .addComponent(jButton6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setTitle("Rohstoffverteiler");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jMerchantPanel.setBackground(new java.awt.Color(239, 235, 223));
        jMerchantPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 628;
        gridBagConstraints.ipady = 437;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jMerchantPanel, gridBagConstraints);

        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        jCheckBox1.setText("Immer im Vordergrund");
        jCheckBox1.setOpaque(false);
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jCheckBox1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireReadDataEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireReadDataEvent
        /* try {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        List<VillageMerchantInfo> infos = PluginManager.getSingleton().executeMerchantParser(data);
        if (infos.isEmpty()) {
        JOptionPaneHelper.showInformationBox(this, "Keine Einträge gefunden", "Information");
        return;
        }
        
        VillageMerchantInfo.Direction currentDir = VillageMerchantInfo.Direction.BOTH;
        if (evt.getSource() == jInsertSendButton) {
        currentDir = VillageMerchantInfo.Direction.OUTGOING;
        } else if (evt.getSource() == jInsertReceiveButton) {
        currentDir = VillageMerchantInfo.Direction.INCOMING;
        }
        
        for (VillageMerchantInfo newInfo : infos) {
        newInfo.setDirection(currentDir);
        }
        
        int changesToBoth = 0;
        int dirChanges = 0;
        for (VillageMerchantInfo existingInfo : merchantInfos) {
        VillageMerchantInfo toRemove = null;
        for (VillageMerchantInfo newInfo : infos) {
        if (existingInfo.getVillage().equals(newInfo.getVillage())) {
        //info exists
        if (existingInfo.getDirection() == VillageMerchantInfo.Direction.INCOMING && newInfo.getDirection() == VillageMerchantInfo.Direction.OUTGOING
        || existingInfo.getDirection() == VillageMerchantInfo.Direction.OUTGOING && newInfo.getDirection() == VillageMerchantInfo.Direction.INCOMING) {
        //village acceptes only one and gets the other -> change to both
        existingInfo.setDirection(VillageMerchantInfo.Direction.BOTH);
        changesToBoth++;
        } else if (existingInfo.getDirection() != currentDir) {
        //set to new direction
        existingInfo.setDirection(currentDir);
        dirChanges++;
        }
        toRemove = newInfo;
        break;
        }
        }
        
        if (toRemove != null) {
        infos.remove(toRemove);
        }
        }
        int newInfos = infos.size();
        Collections.addAll(merchantInfos, infos.toArray(new VillageMerchantInfo[]{}));
        
        rebuildTable(jMerchantDataTable, merchantInfos);
        String message = "";
        if (newInfos == 0 && changesToBoth == 0 && dirChanges == 0) {
        message = "Keine Veränderung durch neue Einträge";
        } else {
        message = "Veränderung der erfassten Dörfer:\n";
        message += " * " + newInfos + ((newInfos == 1) ? " neuer Eintrag\n" : " neue Einträge\n");
        message += " * " + changesToBoth + " Wechsel zu 'Lieferant+Empfänger'\n";
        message += " * " + dirChanges + " Wechsel der Handelsrichtung\n";
        
        }
        //JOptionPaneHelper.showInformationBox(this, message, "Information");
        NotifierFrame.doNotification("DS Workbench hat " + infos.size() + " Handelspartner in der Zwischenablage gefunden.\n" + message, NotifierFrame.NOTIFY_INFO);
        // }
        
        } catch (Exception e) {
        logger.error("Failed to read merchant data", e);
        JOptionPaneHelper.showErrorBox(this, "Fehler beim Lesen aus der Zwischenablage", "Fehler");
        }*/
    }//GEN-LAST:event_fireReadDataEvent

    private void fireChangeTradeDirectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeTradeDirectionEvent
        /*  VillageMerchantInfo.Direction dir = VillageMerchantInfo.Direction.BOTH;
        if (evt.getSource() == jTradeBothButton) {
        dir = VillageMerchantInfo.Direction.BOTH;
        } else if (evt.getSource() == jTradeInButton) {
        dir = VillageMerchantInfo.Direction.INCOMING;
        } else if (evt.getSource() == jTradeOutButton) {
        dir = VillageMerchantInfo.Direction.OUTGOING;
        }
        
        int[] rows = jMerchantDataTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
        return;
        }
        
        for (int i = 0; i < rows.length; i++) {
        int row = jMerchantDataTable.convertRowIndexToModel(rows[i]);
        merchantInfos.get(row).setDirection(dir);
        }
        rebuildTable(jMerchantDataTable, merchantInfos);*/
    }//GEN-LAST:event_fireChangeTradeDirectionEvent

    private void fireRemoveMarkedEntriesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveMarkedEntriesEvent

        /*   int[] rows = jMerchantDataTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
        return;
        }
        
        if (JOptionPaneHelper.showQuestionConfirmBox(this, (rows.length + ((rows.length == 1) ? " Eintrag wirklich löschen?" : " Einträge wirklich löschen?")), "Einträge löschen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
        return;
        }
        Arrays.sort(rows);
        List<Village> villagesToRemove = new LinkedList<Village>();
        DefaultTableModel model = ((DefaultTableModel) jMerchantDataTable.getModel());
        jMerchantDataTable.invalidate();
        for (int i = rows.length - 1; i >= 0; i--) {
        int row = jMerchantDataTable.convertRowIndexToModel(rows[i]);
        villagesToRemove.add((Village) model.getValueAt(row, 0));
        model.removeRow(row);
        }
        jMerchantDataTable.revalidate();
        
        List<VillageMerchantInfo> infosToRemove = new LinkedList<VillageMerchantInfo>();
        for (VillageMerchantInfo info : merchantInfos) {
        if (villagesToRemove.contains(info.getVillage())) {
        infosToRemove.add(info);
        }
        }
        
        for (VillageMerchantInfo info : infosToRemove) {
        merchantInfos.remove(info);
        }*/
    }//GEN-LAST:event_fireRemoveMarkedEntriesEvent

    private void fireCalculateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateEvent
        if (evt.getSource() == jCalculateButton) {
            ArrayList<Village> incomingOnly = new ArrayList<Village>();
            ArrayList<Village> outgoingOnly = new ArrayList<Village>();
            int dualDirectionVillages = 0;
            for (VillageMerchantInfo info : merchantInfos) {
                if (info.getDirection() == VillageMerchantInfo.Direction.INCOMING) {
                    incomingOnly.add(info.getVillage());
                } else if (info.getDirection() == VillageMerchantInfo.Direction.OUTGOING) {
                    outgoingOnly.add(info.getVillage());
                } else {
                    dualDirectionVillages++;
                }
            }

            int[] targetRes = null;
            int[] remainRes = null;
            if (jAdjustingDistribution.isSelected()) {
                try {
                    targetRes = new int[]{Integer.parseInt(jTargetWood.getText()), Integer.parseInt(jTargetClay.getText()), Integer.parseInt(jTargetIron.getText())};
                    remainRes = new int[]{Integer.parseInt(jRemainWood.getText()), Integer.parseInt(jRemainClay.getText()), Integer.parseInt(jRemainIron.getText())};
                } catch (Exception e) {
                    JOptionPaneHelper.showWarningBox(this, "Ressourcenangaben fehlerhaft", "Fehler");
                    return;
                }
            } else {
                int woodSum = 0;
                int claySum = 0;
                int ironSum = 0;
                for (VillageMerchantInfo info : merchantInfos) {
                    woodSum += info.getWoodStock();
                    claySum += info.getClayStock();
                    ironSum += info.getIronStock();
                }
                targetRes = new int[]{(int) Math.rint(woodSum / merchantInfos.size()), (int) Math.rint(claySum / merchantInfos.size()), (int) Math.rint(ironSum / merchantInfos.size())};
                jTargetWood.setText(Integer.toString(targetRes[0]));
                jTargetClay.setText(Integer.toString(targetRes[1]));
                jTargetIron.setText(Integer.toString(targetRes[2]));
                remainRes = targetRes;
            }

            int maxFilling = 95;
            try {
                maxFilling = Integer.parseInt(jMaxFilling.getText());
            } catch (Exception e) {
                maxFilling = 95;
                jMaxFilling.setText("95");
            }

            List<VillageMerchantInfo> copy = new LinkedList<VillageMerchantInfo>();
            for (int i = 0; i < merchantInfos.size(); i++) {
                VillageMerchantInfo info = merchantInfos.get(i).clone();
                info.adaptStashCapacity(maxFilling);
                copy.add(info);
            }

            List<List<MerchantSource>> results = new MerchantDistributor().calculate(copy, incomingOnly, outgoingOnly, targetRes, remainRes);
            buildResults(copy, results, targetRes);
        }

        jCalculationSettingsDialog.setVisible(false);

    }//GEN-LAST:event_fireCalculateEvent

    private void fireCalculationTypeChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireCalculationTypeChangedEvent
        jTargetWood.setEnabled(jAdjustingDistribution.isSelected());
        jTargetClay.setEnabled(jAdjustingDistribution.isSelected());
        jTargetIron.setEnabled(jAdjustingDistribution.isSelected());
        jLabel1.setEnabled(jAdjustingDistribution.isSelected());
        jLabel2.setEnabled(jAdjustingDistribution.isSelected());
        jLabel3.setEnabled(jAdjustingDistribution.isSelected());
        jLabel4.setEnabled(jAdjustingDistribution.isSelected());
        jLabel5.setEnabled(jAdjustingDistribution.isSelected());
        jLabel6.setEnabled(jAdjustingDistribution.isSelected());
        jLabel7.setEnabled(jAdjustingDistribution.isSelected());
        jRemainWood.setEnabled(jAdjustingDistribution.isSelected());
        jRemainClay.setEnabled(jAdjustingDistribution.isSelected());
        jRemainIron.setEnabled(jAdjustingDistribution.isSelected());
    }//GEN-LAST:event_fireCalculationTypeChangedEvent

    private void fireAlwaysOnTopEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopEvent

    private void fireClickAccountChangedEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireClickAccountChangedEvent
        iClickAccount++;
        updateClickAccount();
    }//GEN-LAST:event_fireClickAccountChangedEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(getBackground()));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    /**
     * 
     * @param pMessage
     */
    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.RED));
        jXLabel1.setForeground(Color.WHITE);
        jXLabel1.setText(pMessage);
    }

    private void readMerchantInfoFromClipboard() {
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            List<VillageMerchantInfo> infos = PluginManager.getSingleton().executeMerchantParser(data);
            if (infos.isEmpty()) {
                showInfo("Keine Einträge in der Zwischenablage gefunden");
                return;
            }


            String message = "In der Zwischenablage" + ((infos.size() == 1) ? " wurde 1 Eintrag" : " wurden " + infos.size() + " Einträge") + " gefunden.\n"
                    + "Für welche Transportrichtung" + ((infos.size() == 1) ? " soll dieser Eintrag" : " sollen diese Einträge") + " verwendet werden?";

            int result = JOptionPaneHelper.showQuestionThreeChoicesBox(this, message, "Einträge einfügen", "Empfänger", "Lieferanten", "Beides");
            VillageMerchantInfo.Direction currentDir = VillageMerchantInfo.Direction.BOTH;
            if (result == JOptionPane.NO_OPTION) {
                //receiver
                currentDir = VillageMerchantInfo.Direction.INCOMING;
            } else if (result == JOptionPane.CANCEL_OPTION) {
                //both
                currentDir = VillageMerchantInfo.Direction.BOTH;
            } else {
                //sender
                currentDir = VillageMerchantInfo.Direction.OUTGOING;
            }

            for (VillageMerchantInfo newInfo : infos) {
                newInfo.setDirection(currentDir);
            }

            int changesToBoth = 0;
            int dirChanges = 0;
            for (VillageMerchantInfo existingInfo : merchantInfos) {
                VillageMerchantInfo toRemove = null;
                for (VillageMerchantInfo newInfo : infos) {
                    if (existingInfo.getVillage().equals(newInfo.getVillage())) {
                        //info exists, set new dir
                        existingInfo.setDirection(currentDir);
                        dirChanges++;
                        toRemove = newInfo;
                        break;
                    }
                }

                if (toRemove != null) {
                    infos.remove(toRemove);
                }
            }
            Collections.addAll(merchantInfos, infos.toArray(new VillageMerchantInfo[]{}));

            rebuildTable(jMerchantTable, merchantInfos);
            showSuccess("<html>" + ((infos.size() == 1) ? "1 neuen Eintrag " : infos.size() + " neue Eintr&auml;ge") + " hinzugef&uuml;gt<br/>"
                    + ((changesToBoth + dirChanges == 1) ? "1 Eintrag " : (changesToBoth + dirChanges) + " Eintr&auml;ge") + " ver&auml;ndert</html>");
        } catch (Exception e) {
            logger.error("Failed to read merchant data", e);
            showError("Fehler beim Lesen aus der Zwischenablage");
        }
    }

    private void removeSelection() {
        int[] selectedRows = jMerchantTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            showInfo("Keine Einträge ausgewählt");
            return;
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du " + ((selectedRows.length == 1) ? "den gewählten Eintrag " : "die gewählten Einträge ") + "wirklich löschen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            for (Integer selectedRow : selectedRows) {
                int row = jMerchantTable.convertRowIndexToModel(selectedRow);
                merchantInfos.remove(row);
            }
            rebuildTable(jMerchantTable, merchantInfos);
        }
        showSuccess("Einträge gelöscht");
    }

    private void updateClickAccount() {
        jClickAccountLabel.setToolTipText(iClickAccount + " Klick(s) aufgeladen");
        jClickAccountLabel.setText("Klick-Konto [" + iClickAccount + "]");
    }

    private void rebuildTable(JXTable pTable, List<VillageMerchantInfo> pMerchantInfos) {
        DefaultTableModel model = null;
        model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Dorf", "Holz", "Lehm", "Eisen", "Speicher", "Händler", "Handelsrichtung"
                }) {

            Class[] types = new Class[]{
                Village.class, Integer.class, Integer.class, Integer.class, Integer.class, String.class, VillageMerchantInfo.Direction.class
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

        for (VillageMerchantInfo info : pMerchantInfos) {
            //add table rows
            model.addRow(new Object[]{DataHolder.getSingleton().getVillages()[info.getVillage().getX()][info.getVillage().getY()], info.getWoodStock(), info.getClayStock(), info.getIronStock(), info.getStashCapacity(), info.getAvailableMerchants() + "/" + info.getOverallMerchants(), info.getDirection()});
        }

        //set model
        pTable.setModel(model);
        pTable.setRowHeight(20);

        //set cell renderers
        jMerchantTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        pTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));

        pTable.setDefaultRenderer(VillageMerchantInfo.Direction.class, new TradeDirectionCellRenderer());
        pTable.setDefaultRenderer(Integer.class, new NumberFormatCellRenderer());
    }

    public static class Resource {

        public enum Type {

            WOOD, CLAY, IRON
        }
        private int amount;
        private Type type;

        public Resource(int pAmount, Type pType) {
            setAmount(pAmount);
            setType(pType);
        }

        /**
         * @return the amount
         */
        public int getAmount() {
            return amount;
        }

        /**
         * @param amount the amount to set
         */
        public void setAmount(int amount) {
            this.amount = amount;
        }

        /**
         * @return the type
         */
        public Type getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(Type type) {
            this.type = type;
        }
    }

    public static class Transport {

        private List<Resource> resourceTransports;

        public Transport(List<Resource> pResourceTransports) {
            setSingleTransports(pResourceTransports);
        }

        /**
         * @return the amount
         */
        public List<Resource> getSingleTransports() {
            return resourceTransports;
        }

        /**
         * @param amount the amount to set
         */
        public void setSingleTransports(List<Resource> pTransports) {
            resourceTransports = new LinkedList<Resource>();
            resourceTransports.add(new Resource(0, Resource.Type.WOOD));
            resourceTransports.add(new Resource(0, Resource.Type.CLAY));
            resourceTransports.add(new Resource(0, Resource.Type.IRON));
            for (Resource r : pTransports) {
                if (r.getType() == Resource.Type.WOOD) {
                    resourceTransports.get(0).setAmount(r.getAmount());
                } else if (r.getType() == Resource.Type.CLAY) {
                    resourceTransports.get(1).setAmount(r.getAmount());
                } else if (r.getType() == Resource.Type.IRON) {
                    resourceTransports.get(2).setAmount(r.getAmount());
                }
            }
        }

        public boolean hasGoods() {
            return resourceTransports.get(0).getAmount() > 0 || resourceTransports.get(1).getAmount() > 0 || resourceTransports.get(2).getAmount() > 0;
        }
    }

    private void buildResults(List<VillageMerchantInfo> pInfos, List<List<MerchantSource>> pResults, int[] pTargetRes) {

        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Herkunft", "Rohstoff", "Ziel", "Übertragen"
                }) {

            Class[] types = new Class[]{
                Village.class, Transport.class, Village.class, Boolean.class
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

        int usedMerchants = 0;
        int usedTransports = 0;
        int minAmount = 0;

        try {
            if (jIgnoreTransportsButton.isSelected()) {
                minAmount = Integer.parseInt(jMinTransportAmount.getText()) / 1000;
            } else {
                minAmount = 1;
            }
        } catch (Exception e) {
            minAmount = 1;
        }
        if (!pResults.isEmpty()) {
            Hashtable<Village, Hashtable<Village, List<Resource>>> transports = new Hashtable<Village, Hashtable<Village, List<Resource>>>();
            for (int i = 0; i < 3; i++) {
                Resource.Type current = null;
                switch (i) {
                    case 0:
                        current = Resource.Type.WOOD;
                        break;
                    case 1:
                        current = Resource.Type.CLAY;
                        break;
                    case 2:
                        current = Resource.Type.IRON;
                        break;
                }
                List<MerchantSource> resultForResource = pResults.get(i);

                for (MerchantSource source : resultForResource) {
                    Village sourceVillage = DataHolder.getSingleton().getVillages()[source.getC().getX()][source.getC().getY()];
                    Hashtable<Village, List<Resource>> transportsForSource = transports.get(sourceVillage);

                    if (transportsForSource == null) {
                        transportsForSource = new Hashtable<Village, List<Resource>>();
                        transports.put(sourceVillage, transportsForSource);
                    }

                    for (Order order : source.getOrders()) {
                        MerchantDestination dest = (MerchantDestination) order.getDestination();
                        Village targetVillage = DataHolder.getSingleton().getVillages()[dest.getC().getX()][dest.getC().getY()];
                        List<Resource> transportsFromSourceToDest = transportsForSource.get(targetVillage);
                        if (transportsFromSourceToDest == null) {
                            transportsFromSourceToDest = new LinkedList<Resource>();
                            transportsForSource.put(targetVillage, transportsFromSourceToDest);
                        }
                        int amount = order.getAmount();
                        int merchants = amount;
                        if (merchants >= minAmount) {
                            Resource res = new Resource(merchants * 1000, current);
                            transportsFromSourceToDest.add(res);
                            usedTransports++;
                            usedMerchants += merchants;
                        }
                    }
                }
            }


            // System.out.println(transports);
            Enumeration<Village> sourceKeys = transports.keys();
            while (sourceKeys.hasMoreElements()) {
                Village sourceVillage = sourceKeys.nextElement();

                Hashtable<Village, List<Resource>> transportsFromSource = transports.get(sourceVillage);
                Enumeration<Village> destKeys = transportsFromSource.keys();
                while (destKeys.hasMoreElements()) {
                    Village targetVillage = destKeys.nextElement();
                    Transport trans = new Transport(transportsFromSource.get(targetVillage));
                    if (trans.hasGoods()) {
                        model.addRow(new Object[]{sourceVillage, trans, targetVillage, false});
                    }
                }
            }
        }
        jResultsTable.setModel(model);
        int perfectResults = 0;
        for (VillageMerchantInfo info : pInfos) {
            if (info.getWoodStock() >= pTargetRes[0] && info.getClayStock() >= pTargetRes[1] && info.getIronStock() >= pTargetRes[2]) {
                perfectResults++;
            }
        }
        //set additional infos
        jUsedMerchants.setText(Integer.toString(usedMerchants));
        jUsedTransports.setText(Integer.toString(usedTransports));
        jPerfectResults.setText(Integer.toString(perfectResults));

        //set sorter
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>();
        jResultsTable.setRowSorter(sorter);
        sorter.setModel(model);
        jResultsTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        jResultsTable.setDefaultRenderer(Transport.class, new TransportCellRenderer());
        jResultsTable.setDefaultRenderer(Boolean.class, new BooleanCellRenderer());
        SortableTableHeaderRenderer mHeaderRenderer = new SortableTableHeaderRenderer();
        for (int i = 0; i < jResultsTable.getColumnCount(); i++) {
            jResultsTable.getColumn(jResultsTable.getColumnName(i)).setHeaderRenderer(mHeaderRenderer);
        }
        rebuildTable(jResultsDataTable, pInfos);
        if (!pResults.isEmpty()) {
            merchantTabbedPane.setSelectedIndex(1);
        }
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        DataHolder.getSingleton().loadData(false);
        MouseGestures mMouseGestures = new MouseGestures();
        mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
        mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
        mMouseGestures.start();
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }

        DSWorkbenchMerchantDistibutor.getSingleton().setSize(600, 400);
        DSWorkbenchMerchantDistibutor.getSingleton().resetView();
        DSWorkbenchMerchantDistibutor.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchMerchantDistibutor.getSingleton().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JRadioButton jAdjustingDistribution;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JDialog jCalculationSettingsDialog;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jClickAccountLabel;
    private javax.swing.JRadioButton jEqualDistribution;
    private javax.swing.JCheckBox jIgnoreTransportsButton;
    private javax.swing.JButton jInsertBothButton;
    private javax.swing.JButton jInsertReceiveButton;
    private javax.swing.JButton jInsertSendButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jMaxFilling;
    private javax.swing.JTable jMerchantDataTable;
    private javax.swing.JPanel jMerchantPanel;
    private static final org.jdesktop.swingx.JXTable jMerchantTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JTextField jMinTransportAmount;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel jPerfectResults;
    private javax.swing.JTextField jRemainClay;
    private javax.swing.JTextField jRemainIron;
    private javax.swing.JTextField jRemainWood;
    private org.jdesktop.swingx.JXTable jResultsDataTable;
    private org.jdesktop.swingx.JXTable jResultsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTextField jTargetClay;
    private javax.swing.JTextField jTargetIron;
    private javax.swing.JTextField jTargetWood;
    private com.l2fprod.common.swing.JTaskPane jTaskPane1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup1;
    private com.l2fprod.common.swing.JTaskPaneGroup jTaskPaneGroup2;
    private javax.swing.JButton jTradeBothButton;
    private javax.swing.JButton jTradeInButton;
    private javax.swing.JButton jTradeOutButton;
    private javax.swing.JLabel jUsedMerchants;
    private javax.swing.JLabel jUsedTransports;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXPanel jXResultDistributionPanel;
    private org.jdesktop.swingx.JXPanel jXResultTransportsPanel;
    private org.jdesktop.swingx.JXPanel jxMerchantTablePanel;
    private com.jidesoft.swing.JideTabbedPane merchantTabbedPane;
    // End of variables declaration//GEN-END:variables
}
