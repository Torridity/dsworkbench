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

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountDynamic;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.ClickAccountPanel;
import de.tor.tribes.ui.components.CoordinateSpinner;
import de.tor.tribes.ui.models.FarmTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.TroopSelectionPanelDynamic;
import de.tor.tribes.ui.renderer.*;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.FarmInformationDetailsDialog;
import de.tor.tribes.util.*;
import de.tor.tribes.util.farm.FarmManager;
import de.tor.tribes.util.generator.ui.ReportGenerator;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.sort.TableSortController;

/**
 *
 * @author Torridity
 */
public class DSWorkbenchFarmManager extends AbstractDSWorkbenchFrame implements GenericManagerListener {

    public enum FARM_CONFIGURATION {
        A, B, C, K
    }
    private static Logger logger = Logger.getLogger("FarmManager");
    private static DSWorkbenchFarmManager SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    private ClickAccountPanel clickAccount = null;
    private TroopSelectionPanelDynamic aTroops = null;
    private TroopSelectionPanelDynamic bTroops = null;
    private TroopSelectionPanelDynamic kTroops = null;
    private TroopSelectionPanelDynamic cTroops = null;
    private TroopSelectionPanelDynamic rTroops = null;
    private CoordinateSpinner coordSpinner = null;
    private final static String[] TargetID = { "main", "barracks", "stable", "workshop", "smithy", "market", "none" };
    private static int SelectedCataTarget = 6;
    private static String SelectedFarmGroup = "Alle";
      
    private void setSelectedCataTarget() {
        DSWorkbenchFarmManager.SelectedCataTarget = (int) JCataTarget.getSelectedIndex();
    }
    
    private void setSelectedFarmGroup() {
        DSWorkbenchFarmManager.SelectedFarmGroup = (String) jFarmGroup.getSelectedItem();
    }
    
    public static String getSelectedCataTarget() {        
        return DSWorkbenchFarmManager.TargetID[DSWorkbenchFarmManager.SelectedCataTarget];
    }
    
    public static Village[] getSelectedFarmGroup() {
        List<Village> pactiveFarmGroup = new ArrayList<>();
        String tag = DSWorkbenchFarmManager.SelectedFarmGroup;

        if (tag.equals("Alle")) {
            Collections.addAll(pactiveFarmGroup, GlobalOptions.getSelectedProfile().getTribe().getVillageList());
        } else {
            for (Integer id : TagManager.getSingleton().getTagByName(tag).getVillageIDs()) {

                pactiveFarmGroup.add(DataHolder.getSingleton().getVillagesById().get(id));
            }
        }

        return pactiveFarmGroup.toArray(new Village[pactiveFarmGroup.size()]);
    }

    public static synchronized DSWorkbenchFarmManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchFarmManager();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchFarmManager
     */
    DSWorkbenchFarmManager() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jCenterPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jFarmPanel);
        buildMenu();
        jFarmTable.setModel(new FarmTableModel());
        jFarmTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jFarmTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jFarmTable.setDefaultRenderer(Boolean.class, new ResourcesInStorageCellRenderer());
        jFarmTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jFarmTable.setDefaultRenderer(Float.class, new PercentCellRenderer());
        jFarmTable.setDefaultRenderer(FarmInformation.FARM_STATUS.class, new FarmStatusCellRenderer());
        jFarmTable.setDefaultRenderer(FarmInformation.FARM_RESULT.class, new FarmResultRenderer());
        jFarmTable.setDefaultRenderer(StorageStatus.class, new StorageCellRenderer());
        jFarmTable.setDefaultRenderer(FarmInformation.SIEGE_STATUS.class, new SiegeWeaponsOnWayRenderer());
        jFarmTable.setColumnControlVisible(true);
        jFarmTable.setSortsOnUpdates(false);
        FarmManager.getSingleton().addManagerListener(DSWorkbenchFarmManager.this);
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(jSettingsPanel, BorderLayout.CENTER);

        new Timer("FarmTableUpdate").schedule(new TimerTask() {

            @Override
            public void run() {
                jFarmTable.repaint();
            }
        }, Calendar.getInstance().getTime(), 1000);

        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        KeyStroke farmA = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false);
        KeyStroke farmB = KeyStroke.getKeyStroke(KeyEvent.VK_B, 0, false);
        KeyStroke farmK = KeyStroke.getKeyStroke(KeyEvent.VK_K, 0, false);
        KeyStroke farmC = KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false);
        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelection();
            }
        };

        capabilityInfoPanel1.addActionListener(listener);

        jFarmTable.setSortsOnUpdates(false);
        jFarmTable.registerKeyboardAction(listener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jFarmTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                farmA();
            }
        }, "FarmA", farmA, JComponent.WHEN_IN_FOCUSED_WINDOW);
        jFarmTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                farmB();
            }
        }, "FarmB", farmB, JComponent.WHEN_IN_FOCUSED_WINDOW);
        jFarmTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                farmK();
            }
        }, "FarmK", farmK, JComponent.WHEN_IN_FOCUSED_WINDOW);
        jFarmTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                farmC();
            }
        }, "FarmC", farmC, JComponent.WHEN_IN_FOCUSED_WINDOW);

        aTroops = new TroopSelectionPanelDynamic();
        aTroops.setupFarm(true);
        bTroops = new TroopSelectionPanelDynamic();
        bTroops.setupFarm(true);
        kTroops = new TroopSelectionPanelDynamic();
        kTroops.setupFarm(true);
        cTroops = new TroopSelectionPanelDynamic();
        cTroops.setupFarm(true);
        rTroops = new TroopSelectionPanelDynamic();
        rTroops.setupFarm(true);
        jATroopsPanel.add(aTroops, BorderLayout.CENTER);
        jBTroopsPanel.add(bTroops, BorderLayout.CENTER);
        jKTroopsPanel.add(kTroops, BorderLayout.CENTER);
        jCTroopsPanel.add(cTroops, BorderLayout.CENTER);
        jRSettingsTab.add(rTroops, BorderLayout.CENTER);
        jXLabel1.setLineWrap(true);

        jFarmTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                showInfo(jFarmTable.getSelectedRowCount() + " Farm(en) gewählt");
            }
        });

        coordSpinner = new CoordinateSpinner();
        coordSpinner.setEnabled(false);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(coordSpinner, gridBagConstraints);

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "farmManager",
                    GlobalOptions.getHelpBroker().getHelpSet());
        } // </editor-fold>
    }

    public IntRange getFarmRange(FARM_CONFIGURATION pConfig) {
        if (pConfig == null) {
            pConfig = FARM_CONFIGURATION.C;
        }
        switch (pConfig) {
            case A:
            return new IntRange(UIHelper.parseIntFromField(jMinFarmRuntimeA, 0),
                    UIHelper.parseIntFromField(jMaxFarmRuntimeA, 60));
            case B:
            return new IntRange(UIHelper.parseIntFromField(jMinFarmRuntimeB, 0),
                    UIHelper.parseIntFromField(jMaxFarmRuntimeB, 60));
        case K:
            return new IntRange(UIHelper.parseIntFromField(jMinFarmRuntimeK, 0),
                    UIHelper.parseIntFromField(jMaxFarmRuntimeK, 60));
            default:
            return new IntRange(UIHelper.parseIntFromField(jMinFarmRuntimeC, 0),
                    UIHelper.parseIntFromField(jMaxFarmRuntimeC, 60));
        }
    }

    public int getMinHaul(FARM_CONFIGURATION pConfig) {
        if (pConfig == null) {
            pConfig = FARM_CONFIGURATION.C;
        }
        switch (pConfig) {
            case A:
                return UIHelper.parseIntFromField(jMinHaulA, 1000);
            case B:
                return UIHelper.parseIntFromField(jMinHaulB, 1000);
            case K:
                return 0;
            default:
            return 0;
        }
    }

    public boolean isConsiderSuccessRate() {
        return jConsiderSucessRateC.isSelected();
    }

    public boolean isBlockFarmWithWall() {
        return jBlockFarmWithWall.isSelected();
    }

    public boolean isUseFarmLimit() {
        return jUseFarmLimit.isSelected();
    }

    public int getFarmLimitTime() {
        return UIHelper.parseIntFromField(jFarmlimit, 120);
    }

    public UnitHolder[] getAllowedFarmUnits(FARM_CONFIGURATION pConfig, Village pVillage) {
        if (pConfig == null) {
            pConfig = FARM_CONFIGURATION.C;
        }
        TroopAmountDynamic troops;
        switch (pConfig) {
            case A:
                troops = aTroops.getAmounts();
                break;
            case B:
                troops = bTroops.getAmounts();
                break;
        case K:
            troops = kTroops.getAmounts();
            break;
            default:
                troops = cTroops.getAmounts();
                break;
        }

        List<UnitHolder> allowed = new LinkedList<>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (troops.getAmountForUnit(unit, pVillage) > 0 && !unit.getPlainName().equals("spy")) {
                logger.debug("Adding " + unit + " to allowed farm units");
                allowed.add(unit);
            }
        }
        return allowed.toArray(new UnitHolder[allowed.size()]);
    }

    public TroopAmountFixed getMinUnits(FARM_CONFIGURATION pConfig, Village pVillage) {
        if (pConfig == null) {
            pConfig = FARM_CONFIGURATION.C;
        }
        switch (pConfig) {
            case A:
                return aTroops.getAmounts().transformToFixed(pVillage);
            case B:
                return bTroops.getAmounts().transformToFixed(pVillage);
        case K:
            return bTroops.getAmounts().transformToFixed(pVillage);
            default:
                return cTroops.getAmounts().transformToFixed(pVillage);
        }
    }

    public boolean isUseRams(FARM_CONFIGURATION pConfig) {
        switch (pConfig) {
            case A:
                return jSendRamsA.isSelected();
            case B:
                return jSendRamsB.isSelected();
        case K:
            return jSendRamsK.isSelected();
            default:
                return jSendRamsC.isSelected();
        }
    }

    public TroopAmountFixed getBackupUnits(Village pVillage) {
        return rTroops.getAmounts().transformToFixed(pVillage);
    }

    public boolean allowPartlyFarming() {
        return !jNotAllowPartlyFarming.isSelected();
    }

    public String getCataTarget() {
        return (String) TargetID[JCataTarget.getSelectedIndex()];
    }

    private void buildMenu() {
        clickAccount = new ClickAccountPanel();
        JXTaskPane farmSourcePane = new JXTaskPane();
        farmSourcePane.setTitle("Farmen suchen");
        JXButton searchBarbs = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/search_barbs.png")));
        searchBarbs.setToolTipText("Barbarendörfer im Umkreis suchen");
        searchBarbs.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                Tribe yourTribe = GlobalOptions.getSelectedProfile().getTribe();
                Point center = DSCalculator.calculateCenterOfMass(Arrays.asList(yourTribe.getVillageList()));
                coordSpinner.setValue(center);
                jFarmFromBarbarianSelectionDialog.pack();
                jFarmFromBarbarianSelectionDialog.setLocationRelativeTo(DSWorkbenchFarmManager.getSingleton());
                jFarmFromBarbarianSelectionDialog.setVisible(true);
            }
        });

        farmSourcePane.getContentPane().add(searchBarbs);

        JXButton searchBarbsFromClipboard = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/farms_from_clipboard.png")));
        searchBarbsFromClipboard.setToolTipText("Barbarendörfer in der Zwischenablage suchen");
        searchBarbsFromClipboard.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                FarmManager.getSingleton().findFarmsInClipboard();
            }
        });

        farmSourcePane.getContentPane().add(searchBarbsFromClipboard);

        JXButton searchReports = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/search_reports.png")));

        searchReports.setToolTipText("Farmen in Berichtdatenbank suchen");
        searchReports.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
                model.addElement("Alle");
                for (String set : ReportManager.getSingleton().getGroups()) {
                    if (!set.equals(ReportManager.FARM_SET)) {
                        model.addElement(set);
                    }
                }
                jReportSetBox.setModel(model);
                jFarmFromReportSelectionDialog.pack();
                jFarmFromReportSelectionDialog.setLocationRelativeTo(DSWorkbenchFarmManager.getSingleton());
                jFarmFromReportSelectionDialog.setVisible(true);
            }
        });

        farmSourcePane.getContentPane().add(searchReports);

        JXButton centerFarm = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/center_ingame.png")));

        centerFarm.setToolTipText("Öffnet die Dorfübersicht der gewählten Farm im Spiel");
        centerFarm.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                openVillageInfo();
            }
        });
        farmSourcePane.getContentPane().add(centerFarm);

        JXButton markLastFarm = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/last_report.png")));

        markLastFarm.setToolTipText("Wählt die letzte Farm aus, für die ein Bericht eingelesen wurde");
        markLastFarm.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                selectLastFarm();
            }
        });
        farmSourcePane.getContentPane().add(markLastFarm);
        JXTaskPane farmPane = new JXTaskPane();
        farmPane.setTitle("Farmaktionen");

        JXButton farmA = new JXButton(new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/farmA.png")));

        farmA.setToolTipText("Farmtruppen vom Typ A zur gewählten Farm schicken");
        farmA.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                farmA();
            }
        });

        farmPane.getContentPane().add(farmA);

        JXButton farmB = new JXButton(new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/farmB.png")));

        farmB.setToolTipText("Farmtruppen vom Typ B zur gewählten Farm schicken");
        farmB.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                farmB();
            }
        });

        farmPane.getContentPane().add(farmB);

        JXButton farmK = new JXButton(new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/farmK.png")));

        farmK.setToolTipText("Farmtruppen vom Typ K zur gewählten Farm schicken");
        farmK.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                farmK();
            }
        });

        farmPane.getContentPane().add(farmK);

        JXButton farmC = new JXButton(new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/farmC.png")));

        farmC.setToolTipText("Farmtruppen entsprechend der in der Farm vorhandenen Ressourcen schicken");
        farmC.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                farmC();
            }
        });

        farmPane.getContentPane().add(farmC);

        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");

        JXButton clearStatus = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/clear_fs.png")));

        clearStatus.setToolTipText("Laufenden Farmangriff für die gewählte Farmen zurücksetzen");
        clearStatus.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                resetStatus();
            }
        });

        miscPane.getContentPane().add(clearStatus);

        JXButton clearSiegeStatus = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/clear_fs.png")));

        clearSiegeStatus.setToolTipText("Laufenden Katapultangriff für die gewählte Farmen zurücksetzen");
        clearSiegeStatus.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                resetSiegeStatus();
            }
        });

        miscPane.getContentPane().add(clearSiegeStatus);
        JXButton revalidateFarms = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/check_farms.png")));

        revalidateFarms.setToolTipText("Farmen auf Adelungen und sonstige Veränderungen prüfen");
        revalidateFarms.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                FarmManager.getSingleton().revalidateFarms();
                showInfo("Prüfung abgeschlossen");
            }
        });

        miscPane.getContentPane().add(revalidateFarms);
        JXButton showFarmInfo = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/farm_info.png")));

        showFarmInfo.setToolTipText("Informationen über die gewählte Farm anzeigen");
        showFarmInfo.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                new FarmInformationDetailsDialog(DSWorkbenchFarmManager.this, false)
                        .setupAndShow(getSelectedInformation());
            }
        });

        miscPane.getContentPane().add(showFarmInfo);

        JXButton resetLockedStatus = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/lock_open.png")));

        resetLockedStatus.setToolTipText(
                "Gewählte Farmen entsperren und wieder für Farmangriffe freigeben oder für Farmangriffe sperren");
        resetLockedStatus.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                lockUnlockSelection();
            }
        });

        miscPane.getContentPane().add(resetLockedStatus);
        // Work in progress: implement farming by groups
        JXButton farmByGroups = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/Advanced_options.png")));

        farmByGroups.setToolTipText("Gruppe zum farmen auswählen");
        farmByGroups.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
                model.addElement("Alle");
                for (String g : TagManager.getSingleton().getAllTagNames()) {
                    model.addElement(g);
                }

                jFarmGroup.setModel(model);
                jFarmGroup.setSelectedItem(DSWorkbenchFarmManager.SelectedFarmGroup);
                
                jAdvancedSettingsDialog.pack();
                jAdvancedSettingsDialog.setLocationRelativeTo(DSWorkbenchFarmManager.getSingleton());
                jAdvancedSettingsDialog.setVisible(true);
            }

        });

        miscPane.getContentPane().add(farmByGroups);

        JXButton showOverallStatus = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/chart.png")));

        showOverallStatus.setToolTipText("Zeigt Informationen über alle eingetragenen Farmen");
        showOverallStatus.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                showOverallStatus();
            }
        });

        miscPane.getContentPane().add(showOverallStatus);

        JXButton resortButton = new JXButton(
                new ImageIcon(DSWorkbenchFarmManager.class.getResource("/res/ui/replace2.png")));

        resortButton.setToolTipText("Aktualisiert die Sortierung der Tabelle");
        resortButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                ((TableSortController) jFarmTable.getRowSorter()).sort();
            }
        });

        miscPane.getContentPane().add(resortButton);

        centerPanel.setupTaskPane(clickAccount, farmSourcePane, farmPane, miscPane);
    }

    private void showOverallStatus() {
        int farmCount = 0;
        int attacks = 0;
        int hauledWood = 0;
        int hauledClay = 0;
        int hauledIron = 0;
        int woodPerHour = 0;
        int clayPerHour = 0;
        int ironPerHour = 0;
        
        for (ManageableType type : FarmManager.getSingleton().getAllElements()) {
            FarmInformation info = (FarmInformation) type;
            attacks += info.getAttackCount();
            hauledWood += info.getHauledWood();
            hauledClay += info.getHauledClay();
            hauledIron += info.getHauledIron();
            woodPerHour += DSCalculator.calculateResourcesPerHour(info.getWoodLevel());
            clayPerHour += DSCalculator.calculateResourcesPerHour(info.getClayLevel());
            ironPerHour += DSCalculator.calculateResourcesPerHour(info.getIronLevel());
            farmCount++;
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        int overall = hauledWood + hauledClay + hauledIron;
        JOptionPane optionPane = new JOptionPane();
        StringBuilder b = new StringBuilder();
        b.append("<html>");
        b.append(nf.format(farmCount));
        b.append(" Farmen<br>");
        b.append("Resourcenproduktion pro Stunde (Summe)<br>");
        b.append("<ul>");
        b.append("<li>");
        b.append("Holz: ").append(nf.format(woodPerHour)).append("</li>");
        b.append("<li>");
        b.append("Lehm: ").append(nf.format(clayPerHour)).append("</li>");
        b.append("<li>");
        b.append("Eisen: ").append(nf.format(ironPerHour)).append("</li>");
        b.append("</ul>");
        b.append(nf.format(attacks));
        b.append(" durchgef&uuml;hrte Angriffe<br>");
        b.append(nf.format(hauledWood + hauledClay + hauledIron));
        b.append(" gepl&uuml;nderte Rohstoffe (&#216; ").append(nf.format(overall / attacks)).append(")<br>");
        b.append("<ul>");
        b.append("<li>");
        b.append(nf.format(hauledWood));
        b.append(" Holz (&#216; ").append(nf.format(hauledWood / attacks)).append(")</li>");
        b.append("<li>");
        b.append(nf.format(hauledClay));
        b.append(" Lehm (&#216; ").append(nf.format(hauledClay / attacks)).append(")</li>");
        b.append("<li>");
        b.append(nf.format(hauledIron));
        b.append(" Eisen (&#216; ").append(nf.format(hauledIron / attacks)).append(")</li>");
        b.append("</ul></html>");
        optionPane.setMessage(b.toString());
        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optionPane.createDialog(this, "Status");
        dialog.setVisible(true);
    }

    /**
     * Delete all selected farms
     */
    private void deleteSelection() {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Farm gewählt");
            return;
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this,
                rows.length + " Farm(en) und alle Informationen wirklich löschen?", "Löschen", "Nein",
                "Ja") != JOptionPane.YES_OPTION) {
            return;
        }

        FarmManager.getSingleton().invalidate();
        List<FarmInformation> toDelete = new LinkedList<>();
        for (int row : rows) {
            toDelete.add((FarmInformation) FarmManager.getSingleton().getAllElements()
                    .get(jFarmTable.convertRowIndexToModel(row)));
        }

        for (FarmInformation delete : toDelete) {
            FarmManager.getSingleton().removeFarm(delete.getVillage());
        }

        FarmManager.getSingleton().revalidate(true);
        showInfo(rows.length + " Farm(en) gelöscht");
    }

    private void selectLastFarm() {
        Village last = FarmManager.getSingleton().getLastUpdatedFarm();
        if (last == null) {
            showInfo("Keine letzte Farm bekannt");
            return;
        }

        for (int i = 0; i < jFarmTable.getRowCount(); i++) {
            int row = jFarmTable.convertRowIndexToModel(i);
            FarmInformation info = (FarmInformation) FarmManager.getSingleton().getAllElements().get(row);
            if (info.getVillage().equals(last)) {
                jFarmTable.getSelectionModel().clearSelection();
                jFarmTable.getSelectionModel().addSelectionInterval(i, i);
                jFarmTable.scrollRowToVisible(i);
                jFarmTable.requestFocus();
                break;
            }
        }
    }

    /**
     * Unlock all selected farms if they are locked
     */
    private void lockUnlockSelection() {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Farm gewählt");
            return;
        }
        int activateCnt = 0;
        int deactivateCnt = 0;
        for (int row : rows) {
            FarmInformation info = (FarmInformation) FarmManager.getSingleton().getAllElements()
                    .get(jFarmTable.convertRowIndexToModel(row));
            if (info.isInactive()) {
                info.activateFarm();
                activateCnt++;
            } else {
                info.deactivateFarm();
                deactivateCnt++;
            }
        }
        showInfo(activateCnt + " Farm(en) aktiviert, " + deactivateCnt + " Farm(en) deaktiviert");
    }

    /**
     * Center the selected farm ingame
     */
    private void openVillageInfo() {
        FarmInformation v = getSelectedInformation();
        if (v != null) {
            BrowserInterface.showVillageInfoInGame(v.getVillage());
        } else {
            showInfo("Keine Farm gewählt");
        }
    }

    /**
     * Farm selection using type A
     */
    private void farmA() {
        if (!aTroops.getAmounts().hasUnits()) {
            showInfo("Keine gültigen Farmtruppen für Konfiguration A gefunden");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                farm(FARM_CONFIGURATION.A);
            }
        }).start();
    }

    /**
     * Farm selection using type B
     */
    private void farmB() {
        if (!bTroops.getAmounts().hasUnits()) {
            showInfo("Keine gültigen Farmtruppen für Konfiguration B gefunden");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                farm(FARM_CONFIGURATION.B);
            }
        }).start();
    }

    private void farmK() {
        if (!kTroops.getAmounts().hasUnits()) {
            showInfo("Keine gültigen Farmtruppen für Konfiguration K gefunden");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                farm(FARM_CONFIGURATION.K);
            }
        }).start();
    }

    /**
     * Farm selection using type C
     */
    private void farmC() {
        if (!cTroops.getAmounts().hasUnits()) {
            showInfo("Keine gültigen Farmtruppen für Konfiguration C gefunden");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                farm(FARM_CONFIGURATION.C);
            }
        }).start();
    }

    public TroopAmountDynamic getTroops(FARM_CONFIGURATION pConfig) {
        switch (pConfig) {
            case A:
                return aTroops.getAmounts();
            case B:
                return bTroops.getAmounts();
        case K:
            return kTroops.getAmounts();
            default:
                return cTroops.getAmounts();
        }
    }

    /**
     * Get all selected items
     */
    private FarmInformation getSelectedInformation() {
        int row = jFarmTable.getSelectedRow();
        if (row == -1) {
            return null;
        }
        int modelRow = jFarmTable.convertRowIndexToModel(row);
        return (FarmInformation) FarmManager.getSingleton().getAllElements().get(modelRow);
    }

    /**
     * Farm all selected items (using troops of A, B or C (null-argument))
     */
    private void farm(FARM_CONFIGURATION pConfig) {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Einträge gewählt");
            return;
        }

        // Sorts the farms by min dist to minimize inefficiencies
        final HashMap<FarmInformation, Double> MinFarmDistAllFarms = new HashMap<FarmInformation, Double>();
        List<FarmInformation> farmListcomplete = new LinkedList<>();
        // (Row Code) auxilliary to make new code compatable with old code.
        // Re-coding would clean up the code
        List<Integer> rowIndex = new LinkedList<>();
        final HashMap<Integer, Double> rowMap = new HashMap<Integer, Double>();

        for (int row : rows) {
            int modelRow = jFarmTable.convertRowIndexToModel(row);
            FarmInformation farm = (FarmInformation) FarmManager.getSingleton().getAllElements().get(modelRow);

            final HashMap<Village, Double> SingleFarmDistance = new HashMap<Village, Double>();

            for (Village v : DSWorkbenchFarmManager.getSelectedFarmGroup()) {
                SingleFarmDistance.put(v, DSCalculator.calculateDistance(v, farm.getVillage()));

            }
            logger.debug("loop returned: " + SingleFarmDistance.size() + " elements");

            Map.Entry<Village, Double> minEntry = null;

            for (Map.Entry<Village, Double> entry : SingleFarmDistance.entrySet()) {

                if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                    minEntry = entry;
                }
            }

            MinFarmDistAllFarms.put(farm, minEntry.getValue());
            farmListcomplete.add(farm);
            rowMap.put(row, minEntry.getValue());
            rowIndex.add(row);

        }

        Collections.sort(farmListcomplete, new Comparator<FarmInformation>() {
            @Override
            public int compare(FarmInformation o1, FarmInformation o2) {

                double Dist1 = MinFarmDistAllFarms.get(o1);
                double Dist2 = MinFarmDistAllFarms.get(o2);

                return new Double(Dist1).compareTo(Dist2);
            }
        });

        // (Row code) for compatability with old code
        Collections.sort(rowIndex, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {

                double Dist1 = rowMap.get(o1);
                double Dist2 = rowMap.get(o2);

                return new Double(Dist1).compareTo(Dist2);
            }
        });

        // Hotfix for a bug due to incompatability old and new code (allowing multiple
        // attacks)
        rowIndex.add(rows.length + 1);

        int impossible = 0;
        int farmInactive = 0;
        int alreadyFarming = 0;
        int siegeOnWay = 0;
        int farmFinal = 0;
        int bigFarm = 0;
        int wallTooHigh = 0;

        int opened = 0;
        int i = 0;
        String miscMessage = null;
        for (FarmInformation f : farmListcomplete) {
            int row = rowIndex.get(i);
            int modelRow = jFarmTable.convertRowIndexToModel(row);

            if ((f.getSiegeStatus().equals(FarmInformation.SIEGE_STATUS.BOTH_ON_WAY)
                    || f.getSiegeStatus().equals(FarmInformation.SIEGE_STATUS.CATA_ON_WAY)
                    || f.getSiegeStatus().equals(FarmInformation.SIEGE_STATUS.RAM_ON_WAY))
                    && pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.K)) {
                siegeOnWay++;
                jFarmTable.getSelectionModel().removeSelectionInterval(row, row);
                if (rowIndex.get(i + 1) < jFarmTable.getRowCount()) {
                    jFarmTable.getSelectionModel().addSelectionInterval(rowIndex.get(i + 1), rowIndex.get(i + 1));
                    jFarmTable.requestFocus();
                }
            } else if (f.getSiegeStatus().equals(FarmInformation.SIEGE_STATUS.FINAL_FARM)
                    && pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.K)) {
                farmFinal++;
                jFarmTable.getSelectionModel().removeSelectionInterval(row, row);
                if (rowIndex.get(i + 1) < jFarmTable.getRowCount()) {
                    jFarmTable.getSelectionModel().addSelectionInterval(rowIndex.get(i + 1), rowIndex.get(i + 1));
                    jFarmTable.requestFocus();
                }
            } else if (pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.K)
                    && f.getVillage().getPoints() > 600) {
                bigFarm++;
                jFarmTable.getSelectionModel().removeSelectionInterval(row, row);
                if (rowIndex.get(i + 1) < jFarmTable.getRowCount()) {
                    jFarmTable.getSelectionModel().addSelectionInterval(rowIndex.get(i + 1), rowIndex.get(i + 1));
                    jFarmTable.requestFocus();
                }
            } else if (!this.isUseRams(pConfig) && isBlockFarmWithWall() && f.getWallLevel() > 1) {
                wallTooHigh++;
                jFarmTable.getSelectionModel().removeSelectionInterval(row, row);
                if (rowIndex.get(i + 1) < jFarmTable.getRowCount()) {
                    jFarmTable.getSelectionModel().addSelectionInterval(rowIndex.get(i + 1), rowIndex.get(i + 1));
                    jFarmTable.requestFocus();
                }
            } else if ((!f.getStatus().equals(FarmInformation.FARM_STATUS.FARMING)
                    && !f.getStatus().equals(FarmInformation.FARM_STATUS.REPORT_EXPECTED))
                    || pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.K)) {
                boolean clickUsed = rows.length == 1 || clickAccount.useClick();
                if (clickUsed || rows.length == 1) {
                    boolean success = false;
                    boolean fatal = false;
                    boolean send = false;
                    switch (f.farmFarm(pConfig)) {
                        case FAILED:
                            miscMessage = "Keine Truppeninformationen gefunden oder Fehler beim Öffnen des Browsers";
                            fatal = true;
                            break;
                        case IMPOSSIBLE:
                            impossible++;
                            break;
                        case FARM_INACTIVE:
                            farmInactive++;
                            break;
                        case OK:
                            success = true;
                            send = true;
                            opened++;
                            break;
                    default:
                        break;
                    }
                    getModel().fireTableRowsUpdated(modelRow, modelRow);
                    jFarmTable.getSelectionModel().removeSelectionInterval(row, row);
                    if (success || !fatal) {
                        if (rowIndex.get(i + 1) < jFarmTable.getRowCount()) {
                            jFarmTable.getSelectionModel().addSelectionInterval(rowIndex.get(i + 1),
                                    rowIndex.get(i + 1));
                            jFarmTable.requestFocus();
                        }
                        if (!send && clickUsed && rows.length > 1) {
                            clickAccount.giveClickBack();
                        }
                    } else {
                        jFarmTable.getSelectionModel().addSelectionInterval(row, row);
                        if (clickUsed && rows.length > 1) {
                            clickAccount.giveClickBack();
                        }
                        if (fatal) {
                            break;
                        }
                    }
                } else {
                    miscMessage = "Das Klick-Konto ist leer";
                    break;
                }
            } else {
                alreadyFarming++;
                jFarmTable.getSelectionModel().removeSelectionInterval(row, row);
                if (row + 1 < jFarmTable.getRowCount()) {
                    jFarmTable.getSelectionModel().addSelectionInterval(rowIndex.get(i + 1), rowIndex.get(i + 1));
                    jFarmTable.requestFocus();
                }
            }
            i++;
            logger.debug("Not possible: " + impossible + " inactive: " + farmInactive + " already farming: "
                    + alreadyFarming + "\n Catas on the way" + siegeOnWay + " No catas needed: " + farmFinal
                    + " farm too big: " + bigFarm + " opened: " + opened);
        }

        if (miscMessage == null) {
            if (!pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.K)) {
                showInfo("Geöffnete Tabs: " + opened + "/" + rows.length + "\n" + " - " + impossible
                        + " Mal keine passenden Dörfer gefunden\n" + " - " + farmInactive + " Farmen deaktiviert\n"
                        + " - " + alreadyFarming + " Mal Truppen bereits unterwegs oder Bericht erwartet\n" + " - "
                        + wallTooHigh + " Mal Wall höher als Stufe 1");
        } else {
                showInfo("Geöffnete Tabs: " + opened + "/" + rows.length + "\n" + " - " + impossible
                        + " Mal keine passenden Dörfer gefunden\n" + " - " + farmInactive + " Farmen deaktiviert\n"
                        + " - " + siegeOnWay + " Mal Kataulte bereits unterwegs\n" + " - " + farmFinal
                        + " Mal keine Kattas nötig (optimale Farm)\n" + " - " + bigFarm
                        + " Farm zu groß für Kataangriff\n" + " - " + wallTooHigh + " Mal Wall höher als Stufe 1");
        }
        } else {
            if (!pConfig.equals(DSWorkbenchFarmManager.FARM_CONFIGURATION.K)) {
                showInfo("FEHLER: '" + miscMessage + "'\n" + "Geöffnete Tabs: " + opened + "/" + rows.length + "\n"
                        + " - " + impossible + " Mal keine passenden Dörfer gefunden\n" + " - " + farmInactive
                        + " Farmen deaktiviert\n" + " - " + alreadyFarming
                        + " Mal Truppen bereits unterwegs oder Bericht erwartet\n" + " - " + wallTooHigh
                        + " Mal Wall höher als Stufe 1");
            } else {
                showInfo("FEHLER: '" + miscMessage + "'\n" + "Geöffnete Tabs: " + opened + "/" + rows.length + "\n"
                        + " - " + impossible + " Mal keine passenden Dörfer gefunden\n" + " - " + farmInactive
                        + " Farmen deaktiviert\n" + " - " + siegeOnWay + " Mal Kataulte bereits unterwegs\n" + " - "
                        + farmFinal + " Mal keine Kattas nötig (optimale Farm)\n" + " - " + bigFarm
                        + " Farm zu groß für Kataangriff\n" + " - " + wallTooHigh + " Mal Wall höher als Stufe 1");
    }
        }
    }

    private void resetStatus() {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Einträge gewählt");
            return;
        }

        for (int row : rows) {
            FarmInformation farm = (FarmInformation) FarmManager.getSingleton().getAllElements()
                    .get(jFarmTable.convertRowIndexToModel(row));
            farm.resetFarmStatus();
        }
        showInfo("Status zurückgesetzt. Es wird empfohlen, bei Gelegenheit die DS Workbench Truppeninformationen zu aktualisieren.");
    }

    private void resetSiegeStatus() {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Einträge gewählt");
            return;
        }

        for (int row : rows) {
            FarmInformation farm = (FarmInformation) FarmManager.getSingleton().getAllElements()
                    .get(jFarmTable.convertRowIndexToModel(row));
            farm.resetSiegeStatus();
        }
        showInfo("Status zurückgesetzt. Es wird empfohlen, bei Gelegenheit die DS Workbench Truppeninformationen zu aktualisieren.");
    }

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(getBackground()));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFarmPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jFarmTable = new org.jdesktop.swingx.JXTable();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        settingsPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jSettingsPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jASettingsTab = new javax.swing.JPanel();
        jATroopsPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jFarmASettings = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jMinFarmRuntimeA = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jMaxFarmRuntimeA = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jMinHaulA = new javax.swing.JTextField();
        jSendRamsA = new javax.swing.JCheckBox();
        jBSettingsTab = new javax.swing.JPanel();
        jBTroopsPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jFarmBSettings = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jMinFarmRuntimeB = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jMaxFarmRuntimeB = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jMinHaulB = new javax.swing.JTextField();
        jSendRamsB = new javax.swing.JCheckBox();
        jKSettingsTab = new javax.swing.JPanel();
        jKTroopsPanel = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jFarmKSettings = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jMinFarmRuntimeK = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jMaxFarmRuntimeK = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jSendRamsK = new javax.swing.JCheckBox();
        JCataTarget = new javax.swing.JComboBox<>();
        jCSettingsTab = new javax.swing.JPanel();
        jCTroopsPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jFarmCSettings = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jMinFarmRuntimeC = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jMaxFarmRuntimeC = new javax.swing.JTextField();
        jNotAllowPartlyFarming = new javax.swing.JCheckBox();
        jSendRamsC = new javax.swing.JCheckBox();
        jRSettingsTab = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jFarmFromBarbarianSelectionDialog = new javax.swing.JDialog();
        jLabel14 = new javax.swing.JLabel();
        jRangeField = new javax.swing.JTextField();
        jByVillageCenter = new javax.swing.JRadioButton();
        jByCenter = new javax.swing.JRadioButton();
        jByVillage = new javax.swing.JRadioButton();
        jButton1 = new javax.swing.JButton();
        jSearchButton = new javax.swing.JButton();
        jByCurrent = new javax.swing.JRadioButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jFarmFromReportSelectionDialog = new javax.swing.JDialog();
        jLabel15 = new javax.swing.JLabel();
        jReportSetBox = new javax.swing.JComboBox();
        jAllowAloneTribes = new javax.swing.JCheckBox();
        jAllowAllyTribes = new javax.swing.JCheckBox();
        jAllowTribesInOwnAlly = new javax.swing.JCheckBox();
        jCancelFindInReportsButton = new javax.swing.JButton();
        jFindInReportsButton = new javax.swing.JButton();
        jAdvancedSettingsDialog = new javax.swing.JDialog();
        jLabel13 = new javax.swing.JLabel();
        jFarmGroup = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jFarmlimit = new javax.swing.JTextField();
        jUseFarmLimit = new javax.swing.JCheckBox();
        jBlockFarmWithWall = new javax.swing.JCheckBox();
        jAdvancedSettingsCloseButton = new javax.swing.JButton();
        jConsiderSucessRateC = new javax.swing.JCheckBox();
        jCenterPanel = new org.jdesktop.swingx.JXPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jAlwaysOnTop = new javax.swing.JCheckBox();

        jFarmPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jFarmTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jFarmTable);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setText("Keine Meldung");
        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jXLabel1fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jPanel1.add(infoPanel, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jFarmPanel.add(jPanel1, gridBagConstraints);

        jToggleButton1.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jToggleButton1.setText("Einstellungen");
        jToggleButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireShowHideSettingsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jFarmPanel.add(jToggleButton1, gridBagConstraints);

        settingsPanel.setCollapsed(true);
        settingsPanel.setInheritAlpha(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jFarmPanel.add(settingsPanel, gridBagConstraints);

        jSettingsPanel.setMinimumSize(new java.awt.Dimension(750, 300));
        jSettingsPanel.setPreferredSize(new java.awt.Dimension(750, 300));
        jSettingsPanel.setLayout(new java.awt.GridBagLayout());

        jASettingsTab.setLayout(new java.awt.GridBagLayout());

        jATroopsPanel.setPreferredSize(new java.awt.Dimension(217, 34));
        jATroopsPanel.setLayout(new java.awt.BorderLayout());

        jLabel5.setBackground(new java.awt.Color(153, 153, 153));
        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jLabel5.setText("<html><table><tr><td>123</td><td>= 123 Einheiten verwenden</td></tr>\n<tr><td>0</td><td>= Einheit nicht verwenden</td></tr></table></html>");
        jATroopsPanel.add(jLabel5, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jASettingsTab.add(jATroopsPanel, gridBagConstraints);

        jFarmASettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));
        jFarmASettings.setMinimumSize(new java.awt.Dimension(311, 183));
        jFarmASettings.setPreferredSize(new java.awt.Dimension(311, 183));
        jFarmASettings.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Min. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmASettings.add(jLabel1, gridBagConstraints);

        jMinFarmRuntimeA.setText("0");
        jMinFarmRuntimeA.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinFarmRuntimeA.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmASettings.add(jMinFarmRuntimeA, gridBagConstraints);

        jLabel2.setText("Max. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmASettings.add(jLabel2, gridBagConstraints);

        jMaxFarmRuntimeA.setText("60");
        jMaxFarmRuntimeA.setMinimumSize(new java.awt.Dimension(80, 24));
        jMaxFarmRuntimeA.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmASettings.add(jMaxFarmRuntimeA, gridBagConstraints);

        jLabel3.setText("Min. Beute");
        jLabel3.setMaximumSize(new java.awt.Dimension(92, 14));
        jLabel3.setMinimumSize(new java.awt.Dimension(92, 14));
        jLabel3.setPreferredSize(new java.awt.Dimension(92, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmASettings.add(jLabel3, gridBagConstraints);

        jMinHaulA.setText("1000");
        jMinHaulA.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinHaulA.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmASettings.add(jMinHaulA, gridBagConstraints);

        jSendRamsA.setText("Rammen bei Bedarf mitschicken");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmASettings.add(jSendRamsA, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jASettingsTab.add(jFarmASettings, gridBagConstraints);

        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/res/ui/farmA.png")), jASettingsTab); // NOI18N

        jBSettingsTab.setLayout(new java.awt.GridBagLayout());

        jBTroopsPanel.setPreferredSize(new java.awt.Dimension(217, 34));
        jBTroopsPanel.setLayout(new java.awt.BorderLayout());

        jLabel6.setBackground(new java.awt.Color(153, 153, 153));
        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jLabel6.setText("<html><table><tr><td>123</td><td>= 123 Einheiten verwenden</td></tr>\n<tr><td>0</td><td>= Einheit nicht verwenden</td></tr></table></html>");
        jBTroopsPanel.add(jLabel6, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jBSettingsTab.add(jBTroopsPanel, gridBagConstraints);

        jFarmBSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));
        jFarmBSettings.setMinimumSize(new java.awt.Dimension(311, 183));
        jFarmBSettings.setPreferredSize(new java.awt.Dimension(311, 183));
        jFarmBSettings.setLayout(new java.awt.GridBagLayout());

        jLabel8.setText("Min. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmBSettings.add(jLabel8, gridBagConstraints);

        jMinFarmRuntimeB.setText("0");
        jMinFarmRuntimeB.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinFarmRuntimeB.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmBSettings.add(jMinFarmRuntimeB, gridBagConstraints);

        jLabel9.setText("Max. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmBSettings.add(jLabel9, gridBagConstraints);

        jMaxFarmRuntimeB.setText("60");
        jMaxFarmRuntimeB.setMinimumSize(new java.awt.Dimension(80, 24));
        jMaxFarmRuntimeB.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmBSettings.add(jMaxFarmRuntimeB, gridBagConstraints);

        jLabel10.setText("Min. Beute");
        jLabel10.setMaximumSize(new java.awt.Dimension(92, 14));
        jLabel10.setMinimumSize(new java.awt.Dimension(92, 14));
        jLabel10.setPreferredSize(new java.awt.Dimension(92, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmBSettings.add(jLabel10, gridBagConstraints);

        jMinHaulB.setText("1000");
        jMinHaulB.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinHaulB.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmBSettings.add(jMinHaulB, gridBagConstraints);

        jSendRamsB.setText("Rammen bei Bedarf mitschicken");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmBSettings.add(jSendRamsB, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jBSettingsTab.add(jFarmBSettings, gridBagConstraints);

        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/res/ui/farmB.png")), jBSettingsTab); // NOI18N

        jKSettingsTab.setLayout(new java.awt.GridBagLayout());

        jKTroopsPanel.setPreferredSize(new java.awt.Dimension(217, 34));
        jKTroopsPanel.setLayout(new java.awt.BorderLayout());

        jLabel16.setBackground(new java.awt.Color(153, 153, 153));
        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jLabel16.setText("<html><table><tr><td>123</td><td>= 123 Einheiten verwenden</td></tr>\n<tr><td>0</td><td>= Einheit nicht verwenden</td></tr></table></html>");
        jKTroopsPanel.add(jLabel16, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jKSettingsTab.add(jKTroopsPanel, gridBagConstraints);

        jFarmKSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));
        jFarmKSettings.setMinimumSize(new java.awt.Dimension(311, 183));
        jFarmKSettings.setPreferredSize(new java.awt.Dimension(311, 183));
        jFarmKSettings.setLayout(new java.awt.GridBagLayout());

        jLabel17.setText("Min. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmKSettings.add(jLabel17, gridBagConstraints);

        jMinFarmRuntimeK.setText("0");
        jMinFarmRuntimeK.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinFarmRuntimeK.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmKSettings.add(jMinFarmRuntimeK, gridBagConstraints);

        jLabel18.setText("Max. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmKSettings.add(jLabel18, gridBagConstraints);

        jMaxFarmRuntimeK.setText("60");
        jMaxFarmRuntimeK.setMinimumSize(new java.awt.Dimension(80, 24));
        jMaxFarmRuntimeK.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmKSettings.add(jMaxFarmRuntimeK, gridBagConstraints);

        jLabel19.setText("Kata-Ziel:");
        jLabel19.setToolTipText("");
        jLabel19.setMaximumSize(new java.awt.Dimension(92, 14));
        jLabel19.setMinimumSize(new java.awt.Dimension(92, 14));
        jLabel19.setPreferredSize(new java.awt.Dimension(92, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmKSettings.add(jLabel19, gridBagConstraints);

        jSendRamsK.setText("Rammen bei Bedarf mitschicken");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmKSettings.add(jSendRamsK, gridBagConstraints);

        JCataTarget.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hauptgebäude", "Kaserne", "Stall", "Werkstatt", "Schmiede", "Marktplatz", "Kein Ziel" }));
        JCataTarget.setSelectedIndex(DSWorkbenchFarmManager.SelectedCataTarget);
        JCataTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChangeCataTarget(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmKSettings.add(JCataTarget, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jKSettingsTab.add(jFarmKSettings, gridBagConstraints);

        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/res/ui/farmK.png")), jKSettingsTab); // NOI18N

        jCSettingsTab.setLayout(new java.awt.GridBagLayout());

        jCTroopsPanel.setLayout(new java.awt.BorderLayout());

        jLabel4.setBackground(new java.awt.Color(153, 153, 153));
        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jLabel4.setText("<html><table><tr><td>123</td><td>= Min. 123 Einheiten verwenden, wenn allein</td></tr>\n<tr><td>0</td><td>= Einheit nicht verwenden</td></tr></table></html>");
        jCTroopsPanel.add(jLabel4, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jCSettingsTab.add(jCTroopsPanel, gridBagConstraints);

        jFarmCSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));
        jFarmCSettings.setMinimumSize(new java.awt.Dimension(311, 183));
        jFarmCSettings.setPreferredSize(new java.awt.Dimension(311, 183));
        jFarmCSettings.setLayout(new java.awt.GridBagLayout());

        jLabel11.setText("Min. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmCSettings.add(jLabel11, gridBagConstraints);

        jMinFarmRuntimeC.setText("0");
        jMinFarmRuntimeC.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinFarmRuntimeC.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmCSettings.add(jMinFarmRuntimeC, gridBagConstraints);

        jLabel12.setText("Max. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmCSettings.add(jLabel12, gridBagConstraints);

        jMaxFarmRuntimeC.setText("60");
        jMaxFarmRuntimeC.setMinimumSize(new java.awt.Dimension(80, 24));
        jMaxFarmRuntimeC.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmCSettings.add(jMaxFarmRuntimeC, gridBagConstraints);

        jNotAllowPartlyFarming.setText("Nur angreifen, wenn Farm komplett geleert werden kann");
        jNotAllowPartlyFarming.setMinimumSize(new java.awt.Dimension(100, 23));
        jNotAllowPartlyFarming.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmCSettings.add(jNotAllowPartlyFarming, gridBagConstraints);

        jSendRamsC.setText("Rammen bei Bedarf mitschicken");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmCSettings.add(jSendRamsC, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jCSettingsTab.add(jFarmCSettings, gridBagConstraints);

        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/res/ui/farmC.png")), jCSettingsTab); // NOI18N

        jRSettingsTab.setLayout(new java.awt.BorderLayout());

        jLabel7.setBackground(new java.awt.Color(153, 153, 153));
        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jLabel7.setText("<html><table><tr><td>123</td><td>= 123 Einheiten zur&uuml;ckhalten</td></tr>\n<tr><td>0</td><td>= Alle Einheiten verwenden</td></tr></table></html>");
        jRSettingsTab.add(jLabel7, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/res/ui/farmR.png")), jRSettingsTab); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSettingsPanel.add(jTabbedPane1, gridBagConstraints);

        jFarmFromBarbarianSelectionDialog.setTitle("Barbarendörfer suchen...");
        jFarmFromBarbarianSelectionDialog.setModal(true);
        jFarmFromBarbarianSelectionDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel14.setText("Radius [Felder]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(jLabel14, gridBagConstraints);

        jRangeField.setText("20");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(jRangeField, gridBagConstraints);

        buttonGroup1.add(jByVillageCenter);
        jByVillageCenter.setText("Um das Dorfzentrum");
        jByVillageCenter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireSelectionByCenterChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(jByVillageCenter, gridBagConstraints);

        buttonGroup1.add(jByCenter);
        jByCenter.setText("Um eine Koordinate");
        jByCenter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireSelectionByCenterChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(jByCenter, gridBagConstraints);

        buttonGroup1.add(jByVillage);
        jByVillage.setSelected(true);
        jByVillage.setText("Um jedes Dorf");
        jByVillage.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireSelectionByCenterChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(jByVillage, gridBagConstraints);

        jButton1.setText("Abbrechen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCloseFarmSearchEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(jButton1, gridBagConstraints);

        jSearchButton.setText("Suchen...");
        jSearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCloseFarmSearchEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(jSearchButton, gridBagConstraints);

        buttonGroup1.add(jByCurrent);
        jByCurrent.setText("Um das aktuelle Dorf");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromBarbarianSelectionDialog.getContentPane().add(jByCurrent, gridBagConstraints);

        jFarmFromReportSelectionDialog.setTitle("Berichte durchsuchen...");
        jFarmFromReportSelectionDialog.setModal(true);
        jFarmFromReportSelectionDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel15.setText("Berichtset");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromReportSelectionDialog.getContentPane().add(jLabel15, gridBagConstraints);

        jReportSetBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromReportSelectionDialog.getContentPane().add(jReportSetBox, gridBagConstraints);

        jAllowAloneTribes.setSelected(true);
        jAllowAloneTribes.setText("Angriffe auf stammlose Spieler berücksichtigen");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromReportSelectionDialog.getContentPane().add(jAllowAloneTribes, gridBagConstraints);

        jAllowAllyTribes.setSelected(true);
        jAllowAllyTribes.setText("Angriffe auf Spieler in Stämmen berücksichtigen");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromReportSelectionDialog.getContentPane().add(jAllowAllyTribes, gridBagConstraints);

        jAllowTribesInOwnAlly.setText("Angriffe auf Spieler im eigenen Stamm berücksichtigen");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromReportSelectionDialog.getContentPane().add(jAllowTribesInOwnAlly, gridBagConstraints);

        jCancelFindInReportsButton.setText("Abbrechen");
        jCancelFindInReportsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireFindFarmsInReportsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromReportSelectionDialog.getContentPane().add(jCancelFindInReportsButton, gridBagConstraints);

        jFindInReportsButton.setText("Suchen...");
        jFindInReportsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireFindFarmsInReportsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFarmFromReportSelectionDialog.getContentPane().add(jFindInReportsButton, gridBagConstraints);

        jAdvancedSettingsDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel13.setText("Farmlimit [Produktion in min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAdvancedSettingsDialog.getContentPane().add(jLabel13, gridBagConstraints);

        jFarmGroup.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jFarmGroup.setSelectedItem(DSWorkbenchFarmManager.SelectedFarmGroup);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAdvancedSettingsDialog.getContentPane().add(jFarmGroup, gridBagConstraints);

        jLabel20.setText("Gruppe wählen:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAdvancedSettingsDialog.getContentPane().add(jLabel20, gridBagConstraints);

        jFarmlimit.setText("120");
        jFarmlimit.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAdvancedSettingsDialog.getContentPane().add(jFarmlimit, gridBagConstraints);

        jUseFarmLimit.setText("Farmlimit aktivieren");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAdvancedSettingsDialog.getContentPane().add(jUseFarmLimit, gridBagConstraints);

        jBlockFarmWithWall.setText("Farmen mit Wall ignorieren");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAdvancedSettingsDialog.getContentPane().add(jBlockFarmWithWall, gridBagConstraints);

        jAdvancedSettingsCloseButton.setText("OK");
        jAdvancedSettingsCloseButton.setMargin(new java.awt.Insets(0, 30, 0, 30));
        jAdvancedSettingsCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireFarmGroupSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAdvancedSettingsDialog.getContentPane().add(jAdvancedSettingsCloseButton, gridBagConstraints);

        jConsiderSucessRateC.setText("Erfolgsquote berücksichtigen");
        jConsiderSucessRateC.setMinimumSize(new java.awt.Dimension(100, 23));
        jConsiderSucessRateC.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAdvancedSettingsDialog.getContentPane().add(jConsiderSucessRateC, gridBagConstraints);

        setTitle("Farmmanager");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jCenterPanel.setBackground(new java.awt.Color(239, 235, 223));
        jCenterPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 500;
        gridBagConstraints.ipady = 300;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jCenterPanel, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        jAlwaysOnTop.setText("Immer im Vordergrund");
        jAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jAlwaysOnTopfireChurchFrameOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTop, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jAlwaysOnTopfireChurchFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jAlwaysOnTopfireChurchFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_jAlwaysOnTopfireChurchFrameOnTopEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
    }//GEN-LAST:event_jXLabel1fireHideInfoEvent

    private void fireShowHideSettingsEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireShowHideSettingsEvent
        settingsPanel.setCollapsed(!jToggleButton1.isSelected());
    }//GEN-LAST:event_fireShowHideSettingsEvent

    private void fireCloseFarmSearchEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseFarmSearchEvent
        if (evt.getSource() == jSearchButton) {
            int added = 0;
            if (jByCenter.isSelected()) {
                added = FarmManager.getSingleton().findFarmsFromBarbarians(coordSpinner.getValue(), UIHelper.parseIntFromField(jRangeField, 20));
            } else if (jByVillage.isSelected()) {
                added = FarmManager.getSingleton().findFarmsFromBarbarians(UIHelper.parseIntFromField(jRangeField, 20));
            } else if (jByVillageCenter.isSelected()) {
                Tribe yourTribe = GlobalOptions.getSelectedProfile().getTribe();
                Point center = DSCalculator.calculateCenterOfMass(Arrays.asList(yourTribe.getVillageList()));
                added = FarmManager.getSingleton().findFarmsFromBarbarians(center, UIHelper.parseIntFromField(jRangeField, 20));
            } else if(jByCurrent.isSelected()) {
                added = FarmManager.getSingleton().findFarmsFromBarbarians(
                        DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage().getPosition(),
                        UIHelper.parseIntFromField(jRangeField, 20));
            }

            if (added == 0) {
                showInfo("Keine neuen Farmen gefunden");
            } else {
                showInfo(added + " Farmen hinzugefügt");
            }
        }
        jFarmFromBarbarianSelectionDialog.setVisible(false);

    }//GEN-LAST:event_fireCloseFarmSearchEvent

    private void fireSelectionByCenterChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireSelectionByCenterChangedEvent
        coordSpinner.setEnabled(evt.getSource() == jByCenter);
    }//GEN-LAST:event_fireSelectionByCenterChangedEvent

    private void fireFindFarmsInReportsEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireFindFarmsInReportsEvent
        if (evt.getSource() == jFindInReportsButton) {
            int found = 0;
            if (jReportSetBox.getSelectedIndex() <= 0) {//search in all report sets
                found = FarmManager.getSingleton().findFarmsInReports(jAllowAloneTribes.isSelected(), jAllowAllyTribes.isSelected(), jAllowTribesInOwnAlly.isSelected());
            } else {//search in specific report set
                found = FarmManager.getSingleton().findFarmsInReports((String) jReportSetBox.getSelectedItem(), jAllowAloneTribes.isSelected(), jAllowAllyTribes.isSelected(), jAllowTribesInOwnAlly.isSelected());
            }

            if (found == 0) {
                showInfo("Keine neuen Farmen gefunden");
            } else {
                showInfo(found + " Farm(en) hinzugefügt");
            }
        }

        jFarmFromReportSelectionDialog.setVisible(false);
    }//GEN-LAST:event_fireFindFarmsInReportsEvent

    private void fireFarmGroupSelectionEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireFarmGroupSelectionEvent
        setSelectedFarmGroup();
        jAdvancedSettingsDialog.setVisible(false);
    }//GEN-LAST:event_fireFarmGroupSelectionEvent

    private void ChangeCataTarget(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChangeCataTarget
        setSelectedCataTarget();
    }//GEN-LAST:event_ChangeCataTarget

    private FarmTableModel getModel() {
        return TableHelper.getTableModel(jFarmTable);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchFarmManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        Logger.getRootLogger().setLevel(Level.ERROR);
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        DataHolder.getSingleton().loadData(false);
        GlobalOptions.loadUserData();

        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            VillageTroopsHolder h = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN, true);
            TroopAmountFixed troops = new TroopAmountFixed(0);
            troops.setAmountForUnit(DataHolder.getSingleton().getUnitByPlainName("axe"), 2000);
            troops.setAmountForUnit(DataHolder.getSingleton().getUnitByPlainName("light"), 2000);
            troops.setAmountForUnit(DataHolder.getSingleton().getUnitByPlainName("spy"), 100);
            h.setTroops(troops);
        }

        DataHolder.getSingleton().getUnitByPlainName("light").setSpeed(.1);
        DataHolder.getSingleton().getUnitByPlainName("spy").setSpeed(.01);
        new ReportGenerator().setVisible(true);
        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                DSWorkbenchFarmManager.getSingleton().resetView();
                /*
                 * for (int i = 0; i < 10; i++) { trayIcon.displayMessage("Test", "Hello World " + Math.random(),
                 * TrayIcon.MessageType.INFO); try { Thread.sleep(500); } catch (Exception e) { } }
                 */
                DSWorkbenchFarmManager.getSingleton().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> JCataTarget;
    private javax.swing.ButtonGroup buttonGroup1;
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JPanel jASettingsTab;
    private javax.swing.JPanel jATroopsPanel;
    private javax.swing.JButton jAdvancedSettingsCloseButton;
    private javax.swing.JDialog jAdvancedSettingsDialog;
    private javax.swing.JCheckBox jAllowAllyTribes;
    private javax.swing.JCheckBox jAllowAloneTribes;
    private javax.swing.JCheckBox jAllowTribesInOwnAlly;
    private javax.swing.JCheckBox jAlwaysOnTop;
    private javax.swing.JPanel jBSettingsTab;
    private javax.swing.JPanel jBTroopsPanel;
    private javax.swing.JCheckBox jBlockFarmWithWall;
    private javax.swing.JButton jButton1;
    private javax.swing.JRadioButton jByCenter;
    private javax.swing.JRadioButton jByCurrent;
    private javax.swing.JRadioButton jByVillage;
    private javax.swing.JRadioButton jByVillageCenter;
    private javax.swing.JPanel jCSettingsTab;
    private javax.swing.JPanel jCTroopsPanel;
    private javax.swing.JButton jCancelFindInReportsButton;
    private org.jdesktop.swingx.JXPanel jCenterPanel;
    private javax.swing.JCheckBox jConsiderSucessRateC;
    private javax.swing.JPanel jFarmASettings;
    private javax.swing.JPanel jFarmBSettings;
    private javax.swing.JPanel jFarmCSettings;
    private javax.swing.JDialog jFarmFromBarbarianSelectionDialog;
    private javax.swing.JDialog jFarmFromReportSelectionDialog;
    private javax.swing.JComboBox<String> jFarmGroup;
    private javax.swing.JPanel jFarmKSettings;
    private javax.swing.JPanel jFarmPanel;
    private org.jdesktop.swingx.JXTable jFarmTable;
    private javax.swing.JTextField jFarmlimit;
    private javax.swing.JButton jFindInReportsButton;
    private javax.swing.JPanel jKSettingsTab;
    private javax.swing.JPanel jKTroopsPanel;
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
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jMaxFarmRuntimeA;
    private javax.swing.JTextField jMaxFarmRuntimeB;
    private javax.swing.JTextField jMaxFarmRuntimeC;
    private javax.swing.JTextField jMaxFarmRuntimeK;
    private javax.swing.JTextField jMinFarmRuntimeA;
    private javax.swing.JTextField jMinFarmRuntimeB;
    private javax.swing.JTextField jMinFarmRuntimeC;
    private javax.swing.JTextField jMinFarmRuntimeK;
    private javax.swing.JTextField jMinHaulA;
    private javax.swing.JTextField jMinHaulB;
    private javax.swing.JCheckBox jNotAllowPartlyFarming;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jRSettingsTab;
    private javax.swing.JTextField jRangeField;
    private javax.swing.JComboBox jReportSetBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jSearchButton;
    private javax.swing.JCheckBox jSendRamsA;
    private javax.swing.JCheckBox jSendRamsB;
    private javax.swing.JCheckBox jSendRamsC;
    private javax.swing.JCheckBox jSendRamsK;
    private javax.swing.JPanel jSettingsPanel;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JCheckBox jUseFarmLimit;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXCollapsiblePane settingsPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void resetView() {
        aTroops.setupFarm(true);
        bTroops.setupFarm(true);
        kTroops.setupFarm(true);
        cTroops.setupFarm(true);
    }
/*
 * private static int SelectedCataTarget = 6;
    private static String SelectedFarmGroup = "Alle";
    
    
        
    (non-Javadoc)
 * @see de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame#storeCustomProperties(org.apache.commons.configuration.Configuration)
 */
    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTop.isSelected());
        pConfig.setProperty(getPropertyPrefix() + ".min.haul.a", jMinHaulA.getText());
        pConfig.setProperty(getPropertyPrefix() + ".use.ram.a", jSendRamsA.isSelected());
        pConfig.setProperty(getPropertyPrefix() + ".min.farm.dist.a", jMinFarmRuntimeA.getText());
        pConfig.setProperty(getPropertyPrefix() + ".max.farm.dist.a", jMaxFarmRuntimeA.getText());
        pConfig.setProperty(getPropertyPrefix() + ".min.haul.b", jMinHaulB.getText());
        pConfig.setProperty(getPropertyPrefix() + ".use.ram.b", jSendRamsB.isSelected());
        pConfig.setProperty(getPropertyPrefix() + ".min.farm.dist.b", jMinFarmRuntimeB.getText());
        pConfig.setProperty(getPropertyPrefix() + ".max.farm.dist.b", jMaxFarmRuntimeB.getText());
        pConfig.setProperty(getPropertyPrefix() + ".use.ram.k", jSendRamsK.isSelected());
        pConfig.setProperty(getPropertyPrefix() + ".min.farm.dist.k", jMinFarmRuntimeK.getText());
        pConfig.setProperty(getPropertyPrefix() + ".max.farm.dist.k", jMaxFarmRuntimeK.getText());
        pConfig.setProperty(getPropertyPrefix() + ".max.farm.limit", jFarmlimit.getText());
        pConfig.setProperty(getPropertyPrefix() + ".use.ram.c", jSendRamsC.isSelected());
        pConfig.setProperty(getPropertyPrefix() + ".min.farm.dist.c", jMinFarmRuntimeC.getText());
        pConfig.setProperty(getPropertyPrefix() + ".max.farm.dist.c", jMaxFarmRuntimeC.getText());
        pConfig.setProperty(getPropertyPrefix() + ".farmA.units", aTroops.getAmounts().toProperty());
        pConfig.setProperty(getPropertyPrefix() + ".farmB.units", bTroops.getAmounts().toProperty());
        pConfig.setProperty(getPropertyPrefix() + ".farmK.units", kTroops.getAmounts().toProperty());
        pConfig.setProperty(getPropertyPrefix() + ".farmC.units", cTroops.getAmounts().toProperty());
        pConfig.setProperty(getPropertyPrefix() + ".farmR.units", rTroops.getAmounts().toProperty());
        pConfig.setProperty(getPropertyPrefix() + ".disallow.partly.farming", jNotAllowPartlyFarming.isSelected());
        pConfig.setProperty(getPropertyPrefix() + ".use.success.rate", isConsiderSuccessRate());
        pConfig.setProperty(getPropertyPrefix() + ".block.farm.wall", isBlockFarmWithWall());
        pConfig.setProperty(getPropertyPrefix() + ".use.farm.limit", isUseFarmLimit());
        pConfig.setProperty(getPropertyPrefix() + ".farm.group", DSWorkbenchFarmManager.SelectedFarmGroup);
        pConfig.setProperty(getPropertyPrefix() + ".Cata.Target", DSWorkbenchFarmManager.SelectedCataTarget);
        PropertyHelper.storeTableProperties(jFarmTable, pConfig, getPropertyPrefix());
    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        try {
            jAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        try {
            jConsiderSucessRateC.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".use.success.rate"));
        } catch (Exception ignored) {
        }

        try {
            jBlockFarmWithWall.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".block.farm.wall"));
        } catch (Exception ignored) {
        }

        try {
            jUseFarmLimit.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".use.farm.limit"));
        } catch (Exception ignored) {
        }

        try {
            jNotAllowPartlyFarming.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".disallow.partly.farming"));
        } catch (Exception ignored) {
        }
        
        try {
            DSWorkbenchFarmManager.SelectedFarmGroup = pConfig.getString(getPropertyPrefix() + ".farm.group");
        } catch (Exception ignored) {
        }
        
        try {
            JCataTarget.setSelectedIndex(pConfig.getInt(getPropertyPrefix() + ".Cata.Target"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jAlwaysOnTop.isSelected());
        UIHelper.setText(jMinHaulA, pConfig.getProperty(getPropertyPrefix() + ".min.haul.a"), 1000);
        jSendRamsA.setSelected(Boolean.parseBoolean((String) pConfig.getProperty(getPropertyPrefix() + ".use.ram.a")));
        UIHelper.setText(jMinFarmRuntimeA, pConfig.getProperty(getPropertyPrefix() + ".min.farm.dist.a"), 0);
        UIHelper.setText(jMaxFarmRuntimeA, pConfig.getProperty(getPropertyPrefix() + ".max.farm.dist.a"), 60);
        UIHelper.setText(jMinHaulB, pConfig.getProperty(getPropertyPrefix() + ".min.haul.b"), 1000);
        jSendRamsB.setSelected(Boolean.parseBoolean((String) pConfig.getProperty(getPropertyPrefix() + ".use.ram.b")));
        UIHelper.setText(jMinFarmRuntimeB, pConfig.getProperty(getPropertyPrefix() + ".min.farm.dist.b"), 0);
        UIHelper.setText(jMaxFarmRuntimeB, pConfig.getProperty(getPropertyPrefix() + ".max.farm.dist.b"), 60);
        jSendRamsK.setSelected(Boolean.parseBoolean((String) pConfig.getProperty(getPropertyPrefix() + ".use.ram.k")));
        UIHelper.setText(jMinFarmRuntimeK, pConfig.getProperty(getPropertyPrefix() + ".min.farm.dist.k"), 0);
        UIHelper.setText(jMaxFarmRuntimeK, pConfig.getProperty(getPropertyPrefix() + ".max.farm.dist.k"), 60);
        UIHelper.setText(jFarmlimit, pConfig.getProperty(getPropertyPrefix() + ".max.farm.limit"), 120);
        jSendRamsC.setSelected(Boolean.parseBoolean((String) pConfig.getProperty(getPropertyPrefix() + ".use.ram.c")));
        UIHelper.setText(jMinFarmRuntimeC, pConfig.getProperty(getPropertyPrefix() + ".min.farm.dist.c"), 0);
        UIHelper.setText(jMaxFarmRuntimeC, pConfig.getProperty(getPropertyPrefix() + ".max.farm.dist.c"), 60);
        String farmA = (String) pConfig.getProperty(getPropertyPrefix() + ".farmA.units");
        if (farmA != null) {
            aTroops.setAmounts(new TroopAmountDynamic(0).loadFromProperty(farmA));
        }
        String farmB = (String) pConfig.getProperty(getPropertyPrefix() + ".farmB.units");
        if (farmB != null) {
            bTroops.setAmounts(new TroopAmountDynamic(0).loadFromProperty(farmB));
        }
        String farmK = (String) pConfig.getProperty(getPropertyPrefix() + ".farmK.units");
        if (farmK != null) {
            kTroops.setAmounts(new TroopAmountDynamic(0).loadFromProperty(farmK));
        }
        String farmC = (String) pConfig.getProperty(getPropertyPrefix() + ".farmC.units");
        if (farmC != null) {
            cTroops.setAmounts(new TroopAmountDynamic(0).loadFromProperty(farmC));
        }
        String farmR = (String) pConfig.getProperty(getPropertyPrefix() + ".farmR.units");
        if (farmR != null) {
            rTroops.setAmounts(new TroopAmountDynamic(0).loadFromProperty(farmR));
        }
        PropertyHelper.restoreTableProperties(jFarmTable, pConfig, getPropertyPrefix());
    }

    @Override
    public String getPropertyPrefix() {
        return "farm.manager";
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    @Override
    public void dataChangedEvent() {
        getModel().fireTableDataChanged();
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        dataChangedEvent();
    }
}
