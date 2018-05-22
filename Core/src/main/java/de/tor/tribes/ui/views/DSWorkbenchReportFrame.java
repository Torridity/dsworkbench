/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.views;

import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.swing.TabEditingEvent;
import com.jidesoft.swing.TabEditingListener;
import com.jidesoft.swing.TabEditingValidator;
import com.smardec.mousegestures.MouseGestures;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.*;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.ReportTableTab;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.ReportRulesDialog;
import de.tor.tribes.util.*;
import de.tor.tribes.util.bb.AllyReportStatsFormatter;
import de.tor.tribes.util.bb.OverallReportStatsFormatter;
import de.tor.tribes.util.bb.TribeReportStatsFormatter;
import de.tor.tribes.util.farm.FarmManager;
import de.tor.tribes.util.generator.ui.ReportGenerator;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.report.ReportStatBuilder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
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
            } else if (e.getActionCommand().equals("BBCopy")) {
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
                        if (!col.getTitle().equals("Status") && !col.getTitle().equals("Typ") && !col.getTitle().equals("Sonstiges")) {
                            model.addElement(col.getTitle());
                        }
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
    private String overallResultCodes = null;
    private String allyResultCodes = null;
    private String tribeResultCodes = null;
    private ReportRulesDialog rulesDialog = null;

    public static synchronized DSWorkbenchReportFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchReportFrame();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchReportFrame
     */
    DSWorkbenchReportFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jReportsPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jXReportsPanel);
        buildMenu();
        capabilityInfoPanel1.addActionListener(this);

        jReportsTabbedPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);
        jReportsTabbedPane.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        jReportsTabbedPane.setBoldActiveTab(true);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);

        ActionListener resultListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                copyResultBBToClipboardEvent();
            }
        };

        capabilityInfoPanel2.addActionListener(resultListener);
        jResultTabbedPane.registerKeyboardAction(resultListener, "BBCopy", bbCopy, JComponent.WHEN_IN_FOCUSED_WINDOW);

        jReportsTabbedPane.setCloseAction(new AbstractAction("closeAction") {

            @Override
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
                return tabText.trim().length() != 0 && !ReportManager.getSingleton().groupExists(tabText);

            }

            @Override
            public boolean shouldStartEdit(int tabIndex, MouseEvent event) {
                return !(tabIndex == 0 || tabIndex == 1);
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
        jList1.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireRebuildStatsEvent();
                }
            }
        });

        setGlassPane(jxSearchPane);
        rulesDialog = new ReportRulesDialog(this, true);

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.reports_view", GlobalOptions.getHelpBroker().getHelpSet());
            GlobalOptions.getHelpBroker().enableHelpKey(jCreateStatsFrame.getRootPane(), "pages.reports_view_stats", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>

        jCreateStatsFrame.pack();
        pack();
    }

    @Override
    public void toBack() {
        jAlwaysOnTopBox.setSelected(false);
        fireAlwaysOnTopEvent(null);
        super.toBack();
    }

    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTopBox.isSelected());

        int selectedIndex = jReportsTabbedPane.getModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            pConfig.setProperty(getPropertyPrefix() + ".tab.selection", selectedIndex);
        }


        ReportTableTab tab = ((ReportTableTab) jReportsTabbedPane.getComponentAt(0));
        PropertyHelper.storeTableProperties(tab.getReportTable(), pConfig, getPropertyPrefix());
    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        try {
            jReportsTabbedPane.setSelectedIndex(pConfig.getInteger(getPropertyPrefix() + ".tab.selection", 0));
        } catch (Exception ignored) {
        }
        try {
            jAlwaysOnTopBox.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jAlwaysOnTopBox.isSelected());

        ReportTableTab tab = ((ReportTableTab) jReportsTabbedPane.getComponentAt(0));
        PropertyHelper.restoreTableProperties(tab.getReportTable(), pConfig, getPropertyPrefix());

    }

    @Override
    public String getPropertyPrefix() {
        return "report.view";
    }

    private void buildMenu() {

        JXTaskPane viewTaskPane = new JXTaskPane();
        viewTaskPane.setTitle("Anzeigen");
        JXButton viewReport = new JXButton(new ImageIcon(DSWorkbenchReportFrame.class.getResource("/res/ui/view_report.png")));
        viewReport.setToolTipText("Die gewählten Berichte öffnen");
        viewReport.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                ReportTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.viewReport();
                }
            }
        });
        viewTaskPane.getContentPane().add(viewReport);


        JXTaskPane transferTaskPane = new JXTaskPane();
        transferTaskPane.setTitle("Übertragen");

        JXButton transferTroopInfo = new JXButton(new ImageIcon(DSWorkbenchReportFrame.class.getResource("/res/ui/troop_info_add.png")));
        transferTroopInfo.setToolTipText("<html>&Uuml;bertr&auml;gt die &uuml;berlebenden Truppen des Verteidigers<br/>"
                + "aus den gew&auml;hlten Berichten in die Truppen&uuml;bersicht</html>");
        transferTroopInfo.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                ReportTableTab tab = getActiveTab();
                if (tab != null) {
                    tab.transferTroopInfos();
                }
            }
        });
        transferTaskPane.getContentPane().add(transferTroopInfo);

        JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchReportFrame.class.getResource("/res/ui/report_toAStar.png")));
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

        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");
        JXButton createStats = new JXButton(new ImageIcon(DSWorkbenchReportFrame.class.getResource("/res/ui/medal.png")));
        createStats.setToolTipText("Statistiken zu den gewählten Berichten erstellen");
        createStats.addMouseListener(new MouseAdapter() {

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

        miscPane.getContentPane().add(createStats);

        JXButton cleanupReports = new JXButton(new ImageIcon(DSWorkbenchReportFrame.class.getResource("/res/ui/report_cleanup.png")));
        cleanupReports.setSize(createStats.getSize());
        cleanupReports.setMinimumSize(createStats.getMinimumSize());
        cleanupReports.setMaximumSize(createStats.getMaximumSize());
        cleanupReports.setPreferredSize(createStats.getPreferredSize());
        cleanupReports.setToolTipText("Veraltete und doppelte Berichte im gewählten Berichtset löschen");
        cleanupReports.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                cleanupReports();
            }
        });

        miscPane.getContentPane().add(cleanupReports);

        JXButton showRuleDialog = new JXButton(new ImageIcon(DSWorkbenchReportFrame.class.getResource("/res/ui/index_edit.png")));
        showRuleDialog.setSize(createStats.getSize());
        showRuleDialog.setMinimumSize(createStats.getMinimumSize());
        showRuleDialog.setMaximumSize(createStats.getMaximumSize());
        showRuleDialog.setPreferredSize(createStats.getPreferredSize());
        showRuleDialog.setToolTipText("Definierte Regeln bearbeiten oder neu erstellen");
        showRuleDialog.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                rulesDialog.rebuildRuleList();
                rulesDialog.setLocationRelativeTo(DSWorkbenchReportFrame.this);
                rulesDialog.setVisible(true);
            }
        });

        miscPane.getContentPane().add(showRuleDialog);

        JXButton applyRules = new JXButton(new ImageIcon(DSWorkbenchReportFrame.class.getResource("/res/ui/index_refresh.png")));
        applyRules.setSize(createStats.getSize());
        applyRules.setMinimumSize(createStats.getMinimumSize());
        applyRules.setMaximumSize(createStats.getMaximumSize());
        applyRules.setPreferredSize(createStats.getPreferredSize());
        applyRules.setToolTipText("Definierte Regeln jetzt auf das gewählte Berichtset anwenden");
        applyRules.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                ReportTableTab tab = getActiveTab();
                if (tab != null) {
                    int changed = ReportManager.getSingleton().filterNow(tab.getReportSet());
                    tab = getActiveTab();
                    if (tab != null) {
                        tab.showInfo(changed + " Berichte neu eingeordnet");
                    }
                }
            }
        });

        miscPane.getContentPane().add(applyRules);

        centerPanel.setupTaskPane(viewTaskPane, transferTaskPane, miscPane);
    }

    private void cleanupReports() {
        Village source;
        Village target;
        ReportTableTab tab = getActiveTab();
        if (tab == null) {
            return;
        }
        String set = tab.getReportSet();
        List<FightReport> old = new LinkedList<>();
        int currentIndex = 0;
        for (ManageableType elem : ReportManager.getSingleton().getAllElements(set)) {
            FightReport r = (FightReport) elem;
            if (!old.contains(r)) {
                source = r.getSourceVillage();
                target = r.getTargetVillage();
                FarmInformation info = FarmManager.getSingleton().getFarmInformation(target);
                boolean removeByFarmInfo = false;
                if (info != null) {
                    if (info.getLastReport() > r.getTimestamp()) {
                        old.add(r);
                        removeByFarmInfo = true;
                    }
                }

                if (!removeByFarmInfo) {
                    long time = r.getTimestamp();
                    int secondaryIndex = 0;
                    for (ManageableType elem2 : ReportManager.getSingleton().getAllElements(set)) {
                        FightReport r1 = (FightReport) elem2;
                        if (!old.contains(r1) && r1.getSourceVillage().equals(source) && r1.getTargetVillage().equals(target)) {
                            if (currentIndex != secondaryIndex) {
                                if (r1.getTimestamp() > time || r.equals(r1)) {
                                    old.add(r);
                                    break;
                                } else {
                                    old.add(r1);
                                }
                            }
                        }
                        secondaryIndex++;
                    }
                }
            }
            currentIndex++;
        }

        if (!old.isEmpty()) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, old.size() + " veraltete Berichte gefunden. Jetzt löschen?", "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                ReportManager.getSingleton().removeElements(set, old);
            }
            tab.showInfo(old.size() + " Berichte gelöscht");
        } else {
            tab.showInfo("Keine alten Berichte gefunden");
        }
    }

    @Override
    public void resetView() {
        overallResultCodes = null;
        allyResultCodes = null;
        tribeResultCodes = null;
        jOverallStatsArea.setText("");
        jAllyStatsArea.setText("");
        jTribeStatsArea.setText("");
        ReportManager.getSingleton().addManagerListener(DSWorkbenchReportFrame.this);
        generateReportTabs();
    }

    /**
     * Initialize and add one tab for each report set to jTabbedPane1
     */
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
        jReportsTabbedPane.setTabClosableAt(1, false);
        jReportsTabbedPane.revalidate();
        ReportTableTab tab = getActiveTab();
        if (tab != null) {
            tab.updateSet();
        }
    }

    /**
     * Get the currently selected tab
     */
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
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
        jResultTabbedPane = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jOverallStatsArea = new javax.swing.JTextPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jAllyStatsArea = new javax.swing.JTextPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTribeStatsArea = new javax.swing.JTextPane();
        capabilityInfoPanel2 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
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
        jPanel7 = new javax.swing.JPanel();
        jGuessUnknownLosses = new javax.swing.JCheckBox();
        jUseSilentKillsBox = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jShowPercentsBox = new javax.swing.JCheckBox();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();
        jReportsPanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jCreateStatsFrame.setTitle("Kampfstatistiken");

        jPanel2.setBackground(new java.awt.Color(239, 235, 223));

        jLabel6.setText("Verwendete Berichtsets");

        jScrollPane2.setPreferredSize(new java.awt.Dimension(258, 100));

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

        jResultTabbedPane.setBackground(new java.awt.Color(239, 235, 223));

        jPanel4.setOpaque(false);
        jPanel4.setLayout(new java.awt.BorderLayout());

        jOverallStatsArea.setContentType("text/html");
        jOverallStatsArea.setEditable(false);
        jScrollPane1.setViewportView(jOverallStatsArea);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jResultTabbedPane.addTab("Gesamtübersicht", new javax.swing.ImageIcon(getClass().getResource("/res/ui/chart.png")), jPanel4); // NOI18N

        jPanel5.setBackground(new java.awt.Color(239, 235, 223));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jAllyStatsArea.setContentType("text/html");
        jAllyStatsArea.setEditable(false);
        jScrollPane5.setViewportView(jAllyStatsArea);

        jPanel5.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jResultTabbedPane.addTab("Stämme", new javax.swing.ImageIcon(getClass().getResource("/res/ally.png")), jPanel5); // NOI18N

        jPanel6.setBackground(new java.awt.Color(239, 235, 223));
        jPanel6.setLayout(new java.awt.BorderLayout());

        jTribeStatsArea.setContentType("text/html");
        jTribeStatsArea.setEditable(false);
        jScrollPane6.setViewportView(jTribeStatsArea);

        jPanel6.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jResultTabbedPane.addTab("Spieler", new javax.swing.ImageIcon(getClass().getResource("/res/face.png")), jPanel6); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jResultTabbedPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jResultTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE))
                    .addComponent(jButton10))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        capabilityInfoPanel2.setCopyable(false);
        capabilityInfoPanel2.setDeletable(false);
        capabilityInfoPanel2.setPastable(false);
        capabilityInfoPanel2.setSearchable(false);

        javax.swing.GroupLayout jCreateStatsFrameLayout = new javax.swing.GroupLayout(jCreateStatsFrame.getContentPane());
        jCreateStatsFrame.getContentPane().setLayout(jCreateStatsFrameLayout);
        jCreateStatsFrameLayout.setHorizontalGroup(
            jCreateStatsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCreateStatsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jCreateStatsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(capabilityInfoPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jCreateStatsFrameLayout.setVerticalGroup(
            jCreateStatsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jCreateStatsFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(capabilityInfoPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jXReportsPanel.setLayout(new java.awt.BorderLayout());

        jReportsTabbedPane.setShowCloseButton(true);
        jReportsTabbedPane.setShowCloseButtonOnTab(true);
        jReportsTabbedPane.setShowGripper(true);
        jReportsTabbedPane.setTabEditingAllowed(true);
        jXReportsPanel.add(jReportsTabbedPane, java.awt.BorderLayout.CENTER);

        jNewPlanPanel.setOpaque(false);
        jNewPlanPanel.setLayout(new java.awt.BorderLayout());

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/document_new_24x24.png"))); // NOI18N
        jLabel10.setToolTipText("Leeres Berichtset erstellen");
        jLabel10.setEnabled(false);
        jLabel10.setMaximumSize(new java.awt.Dimension(40, 40));
        jLabel10.setMinimumSize(new java.awt.Dimension(40, 40));
        jLabel10.setPreferredSize(new java.awt.Dimension(40, 40));
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fireEnterEvent(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fireExitEvent(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCreateAttackPlanEvent(evt);
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
        jXColumnList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
        List selection = jReportSetsForStatsList.getSelectedValuesList();
        if (selection == null || selection.isEmpty()) {
            JOptionPaneHelper.showInformationBox(jCreateStatsFrame, "Kein Berichtset ausgewählt", "Information");
            return;
        }

        List<String> reportSets = new LinkedList<>();
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
        fireRebuildStatsEvent();
    }//GEN-LAST:event_fireDoCreateStatsEvent

    private void fireStatOptionsChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireStatOptionsChangedEvent
        fireRebuildStatsEvent();
    }//GEN-LAST:event_fireStatOptionsChangedEvent

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

    private void fireCreateAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateAttackPlanEvent
        int unusedId = 1;
        while (unusedId < 1000) {
            if (ReportManager.getSingleton().addGroup("Neues Set " + unusedId)) {
                break;
            }
            unusedId++;
        }
        if (unusedId == 1000) {
            JOptionPaneHelper.showErrorBox(DSWorkbenchReportFrame.this, "Du hast mehr als 1000 Berichtsets. Bitte lösche zuerst ein paar bevor du Neue erstellst.", "Fehler");
        }
}//GEN-LAST:event_fireCreateAttackPlanEvent

    private void fireExitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireExitEvent
        jLabel10.setEnabled(false);
}//GEN-LAST:event_fireExitEvent

    private void fireEnterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireEnterEvent
        jLabel10.setEnabled(true);
}//GEN-LAST:event_fireEnterEvent

    private void fireRebuildStatsEvent() {
        List selection = jList1.getSelectedValuesList();
        if (selection == null || selection.isEmpty()) {
            jOverallStatsArea.setText("<html>Kein Stamm ausgewählt</html>");
            jAllyStatsArea.setText("<html>Kein Stamm ausgewählt</html>");
            jTribeStatsArea.setText("<html>Kein Stamm ausgewählt</html>");
            return;
        }
        int overallDefAllies = lastStats.getDefendingAllies().length;
        int overallDefTribes = lastStats.getDefendingTribes().length;

        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumFractionDigits(0);
        f.setMaximumFractionDigits(0);

        StringBuilder allyBuffer = new StringBuilder();
        StringBuilder tribeBuffer = new StringBuilder();
        Hashtable<Ally, AllyStatResult> allyResults = new Hashtable<>();
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
        overallResult.setStartDate(lastStats.getStartDate());
        overallResult.setEndDate(lastStats.getEndDate());
        overallResult.setReportCount(lastStats.getReportCount());
        overallResult.setAttackerAllies(selection.size());
        overallResult.setDefenders(overallDefTribes);
        overallResult.setDefenderAllies(overallDefAllies);

        Enumeration<Ally> keys = allyResults.keys();
        while (keys.hasMoreElements()) {
            Ally a = keys.nextElement();
            AllyStatResult res = allyResults.get(a);
            res.setAlly(a);
            res.setOverallKills(overallResult.getKills());
            res.setOverallLosses(overallResult.getLosses());

            for (TribeStatResult tRes : res.getTribeStats()) {
                tRes.setOverallKills(res.getOverallKills());
                tRes.setOverallLosses(res.getOverallLosses());
                tRes.setAllyKills(res.getKills());
                tRes.setAllyLosses(res.getLosses());
            }
        }

        try {
            List<OverallStatResult> list = Arrays.asList(overallResult);
            overallResultCodes = new OverallReportStatsFormatter().formatElements(list, true);
            jOverallStatsArea.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(overallResultCodes) + "</body></html>");
        } catch (Exception e) {
            overallResultCodes = null;
            jOverallStatsArea.setText("<html>Fehler bei der Darstellung der Auswertung</html>");
            logger.error("Failed to render overall BB representation", e);
        }
        try {
            List<AllyStatResult> list = new LinkedList<>();
            Enumeration<Ally> allyKeys = allyResults.keys();
            while (allyKeys.hasMoreElements()) {
                Ally a = allyKeys.nextElement();
                list.add(allyResults.get(a));
            }
            allyResultCodes = new AllyReportStatsFormatter().formatElements(list, true);
            jAllyStatsArea.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(allyResultCodes) + "</body></html>");
        } catch (Exception e) {
            allyResultCodes = null;
            jAllyStatsArea.setText("<html>Fehler bei der Darstellung der Auswertung</html>");
            logger.error("Failed to render BB representation for allies", e);
        }

        try {
            List<TribeStatResult> list = new LinkedList<>();
            Enumeration<Ally> allyKeys = allyResults.keys();
            while (allyKeys.hasMoreElements()) {
                AllyStatResult allyStat = allyResults.get(allyKeys.nextElement());
                Collections.addAll(list, allyStat.getTribeStats().toArray(new TribeStatResult[allyStat.getTribeStats().size()]));
            }
            tribeResultCodes = new TribeReportStatsFormatter().formatElements(list, true);
            jTribeStatsArea.setText("<html><head>" + BBCodeFormatter.getStyles() + "</head><body>" + BBCodeFormatter.toHtml(tribeResultCodes) + "</body></html>");
        } catch (Exception e) {
            tribeResultCodes = null;
            jTribeStatsArea.setText("<html>Fehler bei der Darstellung der Auswertung</html>");
            logger.error("Failed to render BB representation for tribes", e);
        }
        jResultTabbedPane.setSelectedIndex(0);
    }

    private void copyResultBBToClipboardEvent() {
        StringSelection toCopy = null;
        String dataName = null;
        switch (jResultTabbedPane.getSelectedIndex()) {
            case 0:
                toCopy = new StringSelection(overallResultCodes);
                dataName = "Gesamtübersicht";
                break;
            case 1:
                toCopy = new StringSelection(allyResultCodes);
                dataName = "Stämme";
                break;
            case 2:
                toCopy = new StringSelection(tribeResultCodes);
                dataName = "Spieler";
                break;
        }
        if (toCopy == null) {
            JOptionPaneHelper.showInformationBox(jCreateStatsFrame, "Bitte zuerst einen Tab mit einer Auswertung wählen.", "Information");
            return;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(toCopy, null);
            JOptionPaneHelper.showInformationBox(jCreateStatsFrame, "Auswertung '" + dataName + "' in Zwischenablage kopiert.", "Information");
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            JOptionPaneHelper.showErrorBox(jCreateStatsFrame, "Fehler beim Kopieren in die Zwischenablage.", "Fehler");
        }
    }

    /**
     * Update the attack plan filter
     */
    private void updateFilter() {
        ReportTableTab tab = getActiveTab();
        if (tab != null) {
            final List<String> selection = new LinkedList<>();
            for (Object o : jXColumnList.getSelectedValues()) {
                selection.add((String) o);
            }
            tab.updateFilter(jTextField1.getText(), selection, jFilterCaseSensitive.isSelected(), jFilterRows.isSelected());
        }
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
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
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        MouseGestures mMouseGestures = new MouseGestures();
        mMouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
        mMouseGestures.addMouseGesturesListener(new MouseGestureHandler());
        mMouseGestures.start();
        GlobalOptions.setSelectedServer("de43");
        DataHolder.getSingleton().loadData(false);
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        GlobalOptions.loadUserData();
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }


        ReportGenerator rgen = new ReportGenerator();

        rgen.setVisible(true);

        DSWorkbenchReportFrame.getSingleton().setSize(800, 600);
        DSWorkbenchReportFrame.getSingleton().resetView();
        DSWorkbenchReportFrame.getSingleton().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        DSWorkbenchReportFrame.getSingleton().setVisible(true);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel2;
    private javax.swing.JTextPane jAllyStatsArea;
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton15;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JFrame jCreateStatsFrame;
    private javax.swing.JCheckBox jFilterCaseSensitive;
    private javax.swing.JCheckBox jFilterRows;
    private javax.swing.JCheckBox jGuessUnknownLosses;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jNewPlanPanel;
    private javax.swing.JTextPane jOverallStatsArea;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JList jReportSetsForStatsList;
    private javax.swing.JPanel jReportsPanel;
    private com.jidesoft.swing.JideTabbedPane jReportsTabbedPane;
    private javax.swing.JTabbedPane jResultTabbedPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JCheckBox jShowPercentsBox;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTribeStatsArea;
    private javax.swing.JCheckBox jUseSilentKillsBox;
    private org.jdesktop.swingx.JXList jXColumnList;
    private org.jdesktop.swingx.JXPanel jXPanel2;
    private org.jdesktop.swingx.JXPanel jXReportsPanel;
    private org.jdesktop.swingx.JXPanel jxSearchPane;
    // End of variables declaration//GEN-END:variables
}
