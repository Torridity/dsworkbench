/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchReportFrame.java
 *
 * Created on Jan 16, 2010, 2:30:41 PM
 */
package de.tor.tribes.ui.views;

import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.swing.TabEditingEvent;
import com.jidesoft.swing.TabEditingListener;
import com.jidesoft.swing.TabEditingValidator;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.FightStats;
import de.tor.tribes.types.SingleAttackerStat;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.ReportTableTab;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.MouseGestureHandler;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.report.ReportStatBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**

 * @author Torridity
 */
public class DSWorkbenchReportFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        ReportTableTab activeTab = getActiveTab();
        if (e.getActionCommand() != null && activeTab != null) {
            if (e.getActionCommand().equals("Copy")) {
                activeTab.transferSelection(ReportTableTab.TRANSFER_TYPE.COPY_TO_INTERNAL_CLIPBOARD);
            } else  if (e.getActionCommand().equals("BBCopy")) {
                activeTab.transferSelection(ReportTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
            } else if (e.getActionCommand().equals("Cut")) {
                activeTab.transferSelection(ReportTableTab.TRANSFER_TYPE.CUT_TO_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Paste")) {
                activeTab.transferSelection(ReportTableTab.TRANSFER_TYPE.FROM_INTERNAL_CLIPBOARD);
            } else if (e.getActionCommand().equals("Delete")) {
                activeTab.deleteSelection(true);
            } else if (e.getActionCommand().equals("Find")) {
                BufferedImage back = ImageUtils.createCompatibleBufferedImage(3, 3, BufferedImage.TRANSLUCENT);
                Graphics g = back.getGraphics();
                g.setColor(new Color(120, 120, 120, 120));
                g.fillRect(0, 0, back.getWidth(), back.getHeight());
                g.setColor(new Color(120, 120, 120));
                g.drawLine(0, 0, 3, 3);
                g.dispose();
                TexturePaint paint = new TexturePaint(back, new Rectangle2D.Double(0, 0, back.getWidth(), back.getHeight()));
                jxSearchPane.setBackgroundPainter(new MattePainter(paint));
                DefaultListModel model = new DefaultListModel();

                for (int i = 0; i < activeTab.getReportTable().getColumnCount(); i++) {
                    TableColumnExt col = activeTab.getReportTable().getColumnExt(i);
                    if (col.isVisible()) {
                        model.addElement(col.getTitle());
                    }
                }
                jXColumnList.setModel(model);
                jXColumnList.setSelectedIndex(0);
                jxSearchPane.setVisible(true);
            }
        }
    }

    @Override
    public void dataChangedEvent() {
        generateReportTabs();
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        ReportTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
        }
    }
    private static Logger logger = Logger.getLogger("ReportView");
    private static DSWorkbenchReportFrame SINGLETON = null;
    private FightStats lastStats = null;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchReportFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchReportFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchReportFrame */
    DSWorkbenchReportFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jReportsPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jXReportsPanel);
        buildMenu();
        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("report.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        jReportsTabbedPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jReportsTabbedPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jReportsTabbedPane.setBoldActiveTab(true);
        jReportsTabbedPane.setCloseAction(new AbstractAction("closeAction") {

            public void actionPerformed(ActionEvent e) {
                ReportTableTab tab = (ReportTableTab) e.getSource();
                if (JOptionPaneHelper.showQuestionConfirmBox(jReportsTabbedPane, "Berichtset '" + tab.getReportSet() + "' und alle darin enthaltenen Berichte wirklich löschen? ", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    ReportManager.getSingleton().removeGroup(tab.getReportSet());
                }
            }
        });
        jReportsTabbedPane.addTabEditingListener(new TabEditingListener() {

            @Override
            public void editingStarted(TabEditingEvent tee) {
            }

            @Override
            public void editingStopped(TabEditingEvent tee) {
                ReportManager.getSingleton().renameGroup(tee.getOldTitle(), tee.getNewTitle());
            }

            @Override
            public void editingCanceled(TabEditingEvent tee) {
            }
        });
        jReportsTabbedPane.setTabEditingValidator(new TabEditingValidator() {

            @Override
            public boolean alertIfInvalid(int tabIndex, String tabText) {
                if (tabText.trim().length() == 0) {
                    JOptionPaneHelper.showWarningBox(jReportsTabbedPane, "'" + tabText + "' ist ein ungültiger Setname", "Fehler");
                    return false;
                }

                if (ReportManager.getSingleton().groupExists(tabText)) {
                    JOptionPaneHelper.showWarningBox(jReportsTabbedPane, "Es existiert bereits ein Berichtset mit dem Namen '" + tabText + "'", "Fehler");
                    return false;
                }
                return true;
            }

            @Override
            public boolean isValid(int tabIndex, String tabText) {
                if (tabText.trim().length() == 0) {
                    return false;
                }

                if (ReportManager.getSingleton().groupExists(tabText)) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean shouldStartEdit(int tabIndex, MouseEvent event) {
                return !(tabIndex == 0);
            }
        });
        jReportsTabbedPane.getModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                ReportTableTab activeTab = getActiveTab();
                if (activeTab != null) {
                    activeTab.updateSet();
                }
            }
        });
        jXColumnList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateFilter();
            }
        });
        setGlassPane(jxSearchPane);

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        // GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.reports_view", GlobalOptions.getHelpBroker().getHelpSet());
        //  GlobalOptions.getHelpBroker().enableHelpKey(jFilterDialog.getRootPane(), "pages.reports_view_filters", GlobalOptions.getHelpBroker().getHelpSet());
        //  GlobalOptions.getHelpBroker().enableHelpKey(jCreateStatsFrame.getRootPane(), "pages.reports_view_stats", GlobalOptions.getHelpBroker().getHelpSet());

        // </editor-fold>

        jCreateStatsFrame.pack();
        pack();
    }
    public void storeCustomProperties(Configuration pCconfig) {
    }
 public void restoreCustomProperties(Configuration pConfig) {
    }
    public String getPropertyPrefix() {
        return "report.view";
    }
    private void buildMenu() {
        JXTaskPane transferTaskPane = new JXTaskPane();
        transferTaskPane.setTitle("Übertragen");
        JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/report_toAStar.png")));
        transferVillageList.setToolTipText("Überträgt den gewählten Berichte nach A*Star");
        transferVillageList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                ReportTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.transferSelection(ReportTableTab.TRANSFER_TYPE.ASTAR);
                }
            }
        });
        transferTaskPane.getContentPane().add(transferVillageList);
      /*  JXButton transferNotes = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/att_clipboardBB.png")));
        transferNotes.setToolTipText("Überträgt den gewählten Bericht als BB-Code in die Zwischenablage");
        transferNotes.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                ReportTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.transferSelection(ReportTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
                }
            }
        });
        transferTaskPane.getContentPane().add(transferNotes);

*/
        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");
        JXButton centerVillage = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/medal.png")));
        centerVillage.setToolTipText("Statistiken zu den gewählten Berichten erstellen");
        centerVillage.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                ReportTableTab tab = getActiveTab();
                if (tab != null) {
                    String[] groups = ReportManager.getSingleton().getGroups();
                    DefaultListModel model = new DefaultListModel();
                    for (String group : groups) {
                        model.addElement(group);
                    }

                    jReportSetsForStatsList.setModel(model);
                    jCreateStatsFrame.setLocationRelativeTo(DSWorkbenchReportFrame.this);
                    jCreateStatsFrame.setVisible(true);
                }
            }
        });

        miscPane.getContentPane().add(centerVillage);
        centerPanel.setupTaskPane(transferTaskPane, miscPane);
    }

    @Override
    public void resetView() {
        ReportManager.getSingleton().addManagerListener(this);
        generateReportTabs();
    }

    /**Initialize and add one tab for each report set to jTabbedPane1*/
    public void generateReportTabs() {
        jReportsTabbedPane.invalidate();
        while (jReportsTabbedPane.getTabCount() > 0) {
            ReportTableTab tab = (ReportTableTab) jReportsTabbedPane.getComponentAt(0);
            tab.deregister();
            jReportsTabbedPane.removeTabAt(0);
        }

        LabelUIResource lr = new LabelUIResource();
        lr.setLayout(new BorderLayout());
        lr.add(jNewPlanPanel, BorderLayout.CENTER);
        jReportsTabbedPane.setTabLeadingComponent(lr);
        String[] plans = ReportManager.getSingleton().getGroups();

        //insert default tab to first place
        int cnt = 0;
        for (String plan : plans) {
            ReportTableTab tab = new ReportTableTab(plan, this);
            jReportsTabbedPane.addTab(plan, tab);
            cnt++;
        }
        jReportsTabbedPane.setTabClosableAt(0, false);
        jReportsTabbedPane.revalidate();
        ReportTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
        }
    }

    /**Get the currently selected tab*/
    private ReportTableTab getActiveTab() {
        try {
            if (jReportsTabbedPane.getModel().getSelectedIndex() < 0) {
                return null;
            }
            return ((ReportTableTab) jReportsTabbedPane.getComponentAt(jReportsTabbedPane.getModel().getSelectedIndex()));
        } catch (ClassCastException cce) {
            return null;
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
        java.awt.GridBagConstraints gridBagConstraints;

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
        jAddAsDefender = new javax.swing.JCheckBox();
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
        jXReportsPanel = new org.jdesktop.swingx.JXPanel();
        jReportsTabbedPane = new com.jidesoft.swing.JideTabbedPane();
        jNewPlanPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jxSearchPane = new org.jdesktop.swingx.JXPanel();
        jXPanel2 = new org.jdesktop.swingx.JXPanel();
        jButton15 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jFilterRows = new javax.swing.JCheckBox();
        jFilterCaseSensitive = new javax.swing.JCheckBox();
        jScrollPane8 = new javax.swing.JScrollPane();
        jXColumnList = new org.jdesktop.swingx.JXList();
        jLabel22 = new javax.swing.JLabel();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jReportsPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

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

        jAddAsDefender.setText("Als Verteidiger einfügen");
        jAddAsDefender.setOpaque(false);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAddAsDefender, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jFilterByTribeBox)
                    .addComponent(jTribeSelectionBox, 0, 180, Short.MAX_VALUE)
                    .addComponent(jTribeSelectionFilter))
                .addGap(4, 4, 4)
                .addComponent(jAddTribeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
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
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(jRemoveTribeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jTribeSelectionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTribeSelectionFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jAddAsDefender))
                    .addComponent(jAddTribeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addContainerGap(152, Short.MAX_VALUE))
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

        jXReportsPanel.setLayout(new java.awt.BorderLayout());

        jReportsTabbedPane.setShowCloseButton(true);
        jReportsTabbedPane.setShowCloseButtonOnTab(true);
        jReportsTabbedPane.setShowGripper(true);
        jReportsTabbedPane.setTabEditingAllowed(true);
        jXReportsPanel.add(jReportsTabbedPane, java.awt.BorderLayout.CENTER);

        jNewPlanPanel.setLayout(new java.awt.BorderLayout());

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_new_24x24.png"))); // NOI18N
        jLabel10.setToolTipText("Leeren Angriffsplan erstellen");
        jLabel10.setMaximumSize(new java.awt.Dimension(40, 40));
        jLabel10.setMinimumSize(new java.awt.Dimension(40, 40));
        jLabel10.setOpaque(true);
        jLabel10.setPreferredSize(new java.awt.Dimension(40, 40));
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel10fireEnterEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel10fireMouseExitEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jLabel10fireCreateAttackPlanEvent(evt);
            }
        });
        jNewPlanPanel.add(jLabel10, java.awt.BorderLayout.CENTER);

        jxSearchPane.setOpaque(false);
        jxSearchPane.setLayout(new java.awt.GridBagLayout());

        jXPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jXPanel2.setInheritAlpha(false);

        jButton15.setText("Anwenden");
        jButton15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButton15fireHideGlassPaneEvent(evt);
            }
        });

        jTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField1fireHighlightEvent(evt);
            }
        });

        jLabel21.setText("Suchbegriff");

        jFilterRows.setText("Nur gefilterte Zeilen anzeigen");
        jFilterRows.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jFilterRowsfireUpdateFilterEvent(evt);
            }
        });

        jFilterCaseSensitive.setText("Groß-/Kleinschreibung beachten");
        jFilterCaseSensitive.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jFilterCaseSensitivefireUpdateFilterEvent(evt);
            }
        });

        jXColumnList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane8.setViewportView(jXColumnList);

        jLabel22.setText("Spalten");

        javax.swing.GroupLayout jXPanel2Layout = new javax.swing.GroupLayout(jXPanel2);
        jXPanel2.setLayout(jXPanel2Layout);
        jXPanel2Layout.setHorizontalGroup(
            jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jXPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jFilterRows, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jFilterCaseSensitive, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton15)))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jXPanel2Layout.setVerticalGroup(
            jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jXPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jXPanel2Layout.createSequentialGroup()
                            .addComponent(jFilterCaseSensitive)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jFilterRows)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton15))
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel22))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jxSearchPane.add(jXPanel2, new java.awt.GridBagConstraints());

        setTitle("Berichtsdatenbank");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTopBox, gridBagConstraints);

        jReportsPanel.setBackground(new java.awt.Color(239, 235, 223));
        jReportsPanel.setPreferredSize(new java.awt.Dimension(500, 400));
        jReportsPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 500;
        gridBagConstraints.ipady = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jReportsPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAlwaysOnTopEvent

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

    private void fireFilterByDateChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireFilterByDateChangedEvent
        /* jLabel8.setEnabled(jFilterByDate.isSelected());
        jLabel9.setEnabled(jFilterByDate.isSelected());
        jStartDate.setEnabled(jFilterByDate.isSelected());
        jEndDate.setEnabled(jFilterByDate.isSelected());*/
    }//GEN-LAST:event_fireFilterByDateChangedEvent

    private void fireFilterByTribeChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireFilterByTribeChangedEvent
        /* jAddTribeButton.setEnabled(!jFilterByTribeBox.isSelected());
        jRemoveTribeButton.setEnabled(!jFilterByTribeBox.isSelected());
        jTribeList.setEnabled(!jFilterByTribeBox.isSelected());
        jTribeSelectionFilter.setEnabled(!jFilterByTribeBox.isSelected());
        jTribeSelectionBox.setEnabled(!jFilterByTribeBox.isSelected());*/
    }//GEN-LAST:event_fireFilterByTribeChangedEvent

    private void fireFilterChangedEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_fireFilterChangedEvent
        // buildTribesList(jTribeSelectionFilter.getText());
    }//GEN-LAST:event_fireFilterChangedEvent

    private void fireTribeFilterChangedEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTribeFilterChangedEvent
        /*  if (evt.getSource() == jAddTribeButton) {
        //add tribe
        jTribeSelectionBox.firePopupMenuCanceled();
        Tribe t = (Tribe) jTribeSelectionBox.getSelectedItem();
        if (t != Barbarians.getSingleton()) {
        String appendix = " (Angreifer)";
        if (jAddAsDefender.isSelected()) {
        appendix = " (Verteidiger)";
        }
        String value = t.getName() + appendix;
        if (((DefaultListModel) jTribeList.getModel()).indexOf(value) < 0) {
        ((DefaultListModel) jTribeList.getModel()).addElement(value);
        }
        }
        } else {
        //remove tribe
        String filter = (String) jTribeList.getSelectedValue();
        if (filter != null) {
        if (JOptionPaneHelper.showQuestionConfirmBox(jFilterDialog, "Gewählten Spieler entfernen?", "Spieler entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
        ((DefaultListModel) jTribeList.getModel()).removeElement(filter);
        }
        }
        }*/
    }//GEN-LAST:event_fireTribeFilterChangedEvent

    private void fireApplyFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireApplyFilterEvent
        /* if (evt.getSource() == jDoFilterButton) {
        List<ReportFilterInterface> filters = new LinkedList<ReportFilterInterface>();
        if (!jFilterByTribeBox.isSelected()) {
        DefaultListModel model = (DefaultListModel) jTribeList.getModel();
        if (model.isEmpty()) {
        JOptionPaneHelper.showInformationBox(jFilterDialog, "Keine Spieler ausgewählt", "Information");
        return;
        }
        List<Tribe> attackers = new LinkedList<Tribe>();
        List<Tribe> defenders = new LinkedList<Tribe>();
        for (int i = 0; i < model.size(); i++) {
        //tribes.add((Tribe) model.getElementAt(i));
        String value = (String) model.getElementAt(i);
        if (value.indexOf("(Angreifer)") > 0) {
        attackers.add(DataHolder.getSingleton().getTribeByName(value.replaceAll("\\(Angreifer\\)", "").trim()));
        } else {
        defenders.add(DataHolder.getSingleton().getTribeByName(value.replaceAll("\\(Verteidiger\\)", "").trim()));
        }
        }
        
        if (attackers.size() > 0) {
        AttackerFilter attackerFilter = new AttackerFilter();
        attackerFilter.setup(attackers);
        filters.add(attackerFilter);
        }
        if (defenders.size() > 0) {
        DefenderFilter defenderFilter = new DefenderFilter();
        defenderFilter.setup(defenders);
        filters.add(defenderFilter);
        }
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
        ReportManagerTableModel.getSingleton().fireTableDataChanged();*/
    }//GEN-LAST:event_fireApplyFilterEvent

    private void jButton15fireHideGlassPaneEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton15fireHideGlassPaneEvent
        jxSearchPane.setBackgroundPainter(null);
        jxSearchPane.setVisible(false);
}//GEN-LAST:event_jButton15fireHideGlassPaneEvent

    private void jTextField1fireHighlightEvent(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField1fireHighlightEvent
        updateFilter();
}//GEN-LAST:event_jTextField1fireHighlightEvent

    private void jFilterRowsfireUpdateFilterEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jFilterRowsfireUpdateFilterEvent
        updateFilter();
}//GEN-LAST:event_jFilterRowsfireUpdateFilterEvent

    private void jFilterCaseSensitivefireUpdateFilterEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jFilterCaseSensitivefireUpdateFilterEvent
        updateFilter();
}//GEN-LAST:event_jFilterCaseSensitivefireUpdateFilterEvent

    private void jLabel10fireCreateAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10fireCreateAttackPlanEvent
        int unusedId = 1;
        while (unusedId < 1000) {
            if (ReportManager.getSingleton().addGroup("Neues Set " + unusedId)) {
                break;
            }
            unusedId++;
        }
        if (unusedId == 1000) {
            JOptionPaneHelper.showErrorBox(DSWorkbenchReportFrame.this, "Du hast mehr als 1000 Berichtsets. Bitte lösche zuerst ein paar bevor du Neue erstellst.", "Fehler");
            return;
        }
}//GEN-LAST:event_jLabel10fireCreateAttackPlanEvent

    private void jLabel10fireMouseExitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10fireMouseExitEvent
        jLabel10.setBackground(getBackground());
}//GEN-LAST:event_jLabel10fireMouseExitEvent

    private void jLabel10fireEnterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10fireEnterEvent
        jLabel10.setBackground(getBackground().darker());
}//GEN-LAST:event_jLabel10fireEnterEvent

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

    /**Update the attack plan filter*/
    private void updateFilter() {
        ReportTableTab tab = getActiveTab();
        if (tab != null) {
            final List<String> selection = new LinkedList<String>();
            for (Object o : jXColumnList.getSelectedValues()) {
                selection.add((String) o);
            }
            tab.updateFilter(jTextField1.getText(), selection, jFilterCaseSensitive.isSelected(), jFilterRows.isSelected());
        }
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
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

    // <editor-fold defaultstate="collapsed" desc="Gesture Handling">
    @Override
    public void fireExportAsBBGestureEvent() {
        ReportTableTab tab = getActiveTab();
        if (tab != null) {
            tab.transferSelection(ReportTableTab.TRANSFER_TYPE.CLIPBOARD_BB);
        }
    }

    @Override
    public void fireNextPageGestureEvent() {
        int current = jReportsTabbedPane.getSelectedIndex();
        int size = jReportsTabbedPane.getTabCount();
        if (current + 1 > size - 1) {
            current = 0;
        } else {
            current += 1;
        }
        jReportsTabbedPane.setSelectedIndex(current);
    }

    @Override
    public void firePreviousPageGestureEvent() {
        int current = jReportsTabbedPane.getSelectedIndex();
        int size = jReportsTabbedPane.getTabCount();
        if (current - 1 < 0) {
            current = size - 1;
        } else {
            current -= 1;
        }
        jReportsTabbedPane.setSelectedIndex(current);
    }

    @Override
    public void fireRenameGestureEvent() {
        int idx = jReportsTabbedPane.getSelectedIndex();
        if (idx != 0 && idx != 1) {
            jReportsTabbedPane.editTabAt(idx);
        }
    }
// </editor-fold>

    public static void main(String[] args) {
        MouseGestures mMouseGestures = new MouseGestures();
        mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
        mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
        mMouseGestures.start();

        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        DSWorkbenchReportFrame.getSingleton().setSize(800, 600);
        FightReport r = new FightReport();
        r.setAcceptanceAfter((byte) 100);
        r.setAcceptanceBefore((byte) 100);
        r.setAimedBuilding("Wall");
        r.setAttacker(Barbarians.getSingleton());
        r.setBuildingAfter((byte) 10);
        r.setBuildingBefore((byte) 20);
        r.setConquered(false);
        r.setDefender(Barbarians.getSingleton());
        r.setLuck(0d);
        r.setMoral(100d);
        r.setSourceVillage(new DummyVillage());
        r.setTargetVillage(new DummyVillage());
        r.setWallAfter((byte) 20);
        r.setWallBefore((byte) 20);
        ReportManager.getSingleton().addManagedElement(r);
        ReportManager.getSingleton().addGroup("test1");
        ReportManager.getSingleton().addGroup("asd2");
        ReportManager.getSingleton().addGroup("awe3");

        DSWorkbenchReportFrame.getSingleton().resetView();
        DSWorkbenchReportFrame.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchReportFrame.getSingleton().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JCheckBox jAddAsDefender;
    private javax.swing.JButton jAddTribeButton;
    private javax.swing.JTextArea jAllyStatsArea;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton17;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JFrame jCreateStatsFrame;
    private javax.swing.JButton jDoFilterButton;
    private javax.swing.JSpinner jEndDate;
    private javax.swing.JCheckBox jFilterByDate;
    private javax.swing.JCheckBox jFilterByTribeBox;
    private javax.swing.JCheckBox jFilterCaseSensitive;
    private javax.swing.JDialog jFilterDialog;
    private javax.swing.JCheckBox jFilterRows;
    private javax.swing.JCheckBox jGuessUnknownLosses;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jNewPlanPanel;
    private javax.swing.JTextArea jOverallStatsArea;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JButton jRemoveTribeButton;
    private javax.swing.JList jReportSetsForStatsList;
    private javax.swing.JPanel jReportsPanel;
    private com.jidesoft.swing.JideTabbedPane jReportsTabbedPane;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JCheckBox jShowBlueReports;
    private javax.swing.JCheckBox jShowGreenReports;
    private javax.swing.JCheckBox jShowHiddenAttackerReports;
    private javax.swing.JCheckBox jShowPercentsBox;
    private javax.swing.JCheckBox jShowRedReports;
    private javax.swing.JCheckBox jShowSnobReports;
    private javax.swing.JCheckBox jShowYellowReports;
    private javax.swing.JSpinner jStartDate;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JList jTribeList;
    private javax.swing.JComboBox jTribeSelectionBox;
    private javax.swing.JTextField jTribeSelectionFilter;
    private javax.swing.JTextArea jTribeStatsArea;
    private javax.swing.JCheckBox jUseSilentKillsBox;
    private org.jdesktop.swingx.JXList jXColumnList;
    private org.jdesktop.swingx.JXPanel jXPanel2;
    private org.jdesktop.swingx.JXPanel jXReportsPanel;
    private org.jdesktop.swingx.JXPanel jxSearchPane;
    // End of variables declaration//GEN-END:variables
}
