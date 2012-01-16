/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.ClickAccountPanel;
import de.tor.tribes.ui.models.FarmTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.TroopSelectionPanel;
import de.tor.tribes.ui.renderer.*;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.FarmInformationDetailsDialog;
import de.tor.tribes.util.*;
import de.tor.tribes.util.farm.FarmManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.decorator.PatternPredicate;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author Torridity
 */
public class DSWorkbenchFarmManager extends AbstractDSWorkbenchFrame implements GenericManagerListener {

    private static DSWorkbenchFarmManager SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    private ClickAccountPanel clickAccount = null;
    private TroopSelectionPanel aTroops = null;
    private TroopSelectionPanel bTroops = null;

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
        jFarmTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jFarmTable.setDefaultRenderer(Float.class, new PercentCellRenderer());
        jFarmTable.setDefaultRenderer(FarmInformation.FARM_STATUS.class, new FarmStatusCellRenderer());
        jFarmTable.setDefaultRenderer(FarmInformation.FARM_RESULT.class, new FarmResultRenderer());
        jFarmTable.setDefaultRenderer(StorageStatus.class, new StorageCellRenderer());
        jFarmTable.setColumnControlVisible(true);
        FarmManager.getSingleton().addManagerListener(DSWorkbenchFarmManager.this);
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(jSettingsPanel, BorderLayout.CENTER);

        PatternPredicate patternPredicate0 = new PatternPredicate(FarmInformation.FARM_STATUS.FARMING.toString(), 0);
        MattePainter mp = new MattePainter(new Color(0, 0, 0, 200));
        PainterHighlighter highlighter = new PainterHighlighter(new HighlightPredicate.AndHighlightPredicate(patternPredicate0), mp);
        jFarmTable.addHighlighter(highlighter);

        new Timer("FarmTableUpdate").schedule(new TimerTask() {

            @Override
            public void run() {
                jFarmTable.repaint();
            }
        }, Calendar.getInstance().getTime(), 1000);

        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        KeyStroke farmA = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false);
        KeyStroke farmB = KeyStroke.getKeyStroke(KeyEvent.VK_B, 0, false);
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
        }, "FarmA", farmA, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jFarmTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                farmB();
            }
        }, "FarmB", farmB, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jFarmTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                farmC();
            }
        }, "FarmC", farmC, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jAllowedList.setCellRenderer(new UnitListCellRenderer());
        jAllowedList.setModel(new DefaultListModel());
        jNotAllowedList.setCellRenderer(new UnitListCellRenderer());
        jNotAllowedList.setModel(new DefaultListModel());

        aTroops = new TroopSelectionPanel();
        aTroops.setupFarm(true);
        bTroops = new TroopSelectionPanel();
        bTroops.setupFarm(true);

        jASettingsTab.add(aTroops, BorderLayout.CENTER);
        jBSettingsTab.add(bTroops, BorderLayout.CENTER);
    }

    public IntRange getFarmRange() {
        return new IntRange(UIHelper.parseIntFromField(jMinFarmRuntime, 0), UIHelper.parseIntFromField(jMaxFarmRuntime, 60));
    }

    public int getMinHaul() {
        return UIHelper.parseIntFromField(jMinHaul, 1000);
    }

    public UnitHolder[] getAllowedFarmUnits() {
        DefaultListModel model = (DefaultListModel) jAllowedList.getModel();
        List<UnitHolder> allowed = new LinkedList<UnitHolder>();
        for (int i = 0; i < model.getSize(); i++) {
            allowed.add((UnitHolder) model.getElementAt(i));
        }
        return allowed.toArray(new UnitHolder[allowed.size()]);
    }

    public int getMinUnits() {
        return UIHelper.parseIntFromField(jMinUnits, 30);
    }

    private UnitHolder[] getDefaultAllowedUnits() {
        return new UnitHolder[]{DataHolder.getSingleton().getUnitByPlainName("light"), DataHolder.getSingleton().getUnitByPlainName("axe")};
    }

    private UnitHolder[] getDefaultDisallowedUnits() {
        List<UnitHolder> units = new LinkedList<UnitHolder>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            units.add(unit);
        }

        for (UnitHolder remove : getDefaultAllowedUnits()) {
            units.remove(remove);
        }
        return units.toArray(new UnitHolder[units.size()]);
    }

    private void buildMenu() {
        clickAccount = new ClickAccountPanel();
        JXTaskPane farmSourcePane = new JXTaskPane();
        farmSourcePane.setTitle("Farmen suchen");
        JXButton searchBarbs = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/search_barbs.png")));
        searchBarbs.setToolTipText("Barbarendörfer im Umkreis suchen");
        searchBarbs.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                String result = JOptionPane.showInputDialog(DSWorkbenchFarmManager.this, "Bitte gib den Radius (Felder) um dein Dorfzentrum an,\nin dem nach Farmen gesucht werden soll.", 20);
                if (result == null) {
                    showInfo("Keine Farmen hinzugefügt");
                } else {
                    try {
                        int added = FarmManager.getSingleton().findFarmsFromBarbarians(Integer.parseInt(result));
                        if (added > 0) {
                            showInfo(added + " Farm(en) hinzugefügt");
                        } else {
                            showInfo("Keine neuen Farmen gefunden");
                        }
                    } catch (Exception ex) {
                        showInfo("Eingabe für Radius ungültig");
                    }
                }
            }
        });

        farmSourcePane.getContentPane().add(searchBarbs);

        JXButton searchReports = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/search_reports.png")));
        searchReports.setToolTipText("Barbarendörfer in Berichtdatenbank suchen");
        searchReports.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                String result = JOptionPane.showInputDialog(DSWorkbenchFarmManager.this, "Bitte gib den Radius (Felder) um dein Dorfzentrum an,\nin dem nach verwendbaren Berichten gesucht werden soll.", 20);
                if (result == null) {
                    showInfo("Keine Farmen hinzugefügt");
                } else {
                    try {
                        int added = FarmManager.getSingleton().findFarmsInReports(Integer.parseInt(result));
                        if (added > 0) {
                            showInfo(added + " Farm(en) hinzugefügt");
                        } else {
                            showInfo("Keine neuen Farmen gefunden");
                        }
                    } catch (Exception ex) {
                        showInfo("Eingabe für Radius ungültig");
                    }
                }
            }
        });

        farmSourcePane.getContentPane().add(searchReports);

        JXTaskPane actionPane = new JXTaskPane();
        actionPane.setTitle("Aktionen");


        JXButton farmA = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/farmA.png")));
        farmA.setToolTipText("Farmtruppen vom Typ A auf die gewählte Farm schicken");
        farmA.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                farmA();
            }
        });

        actionPane.getContentPane().add(farmA);


        JXButton farmB = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/farmB.png")));
        farmB.setToolTipText("Farmtruppen vom Typ B auf die gewählte Farm schicken");
        farmB.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                farmB();
            }
        });

        actionPane.getContentPane().add(farmB);

        JXButton farmC = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/farmC.png")));
        farmC.setToolTipText("Farmtruppen entsprechend der im Dorf vorhandenen Ressourcen schicken");
        farmC.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                farmC();
            }
        });

        actionPane.getContentPane().add(farmC);

        JXButton clearStatus = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/clear_fs.png")));
        clearStatus.setToolTipText("Laufenden Angriff für die gewählte Farmen zurücksetzen");
        clearStatus.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                resetStatus();
            }
        });

        actionPane.getContentPane().add(clearStatus);
        JXButton revalidateFarms = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/check_farms.png")));
        revalidateFarms.setToolTipText("Farmen auf Adelungen und sonstige Veränderungen prüfen");
        revalidateFarms.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                FarmManager.getSingleton().revalidateFarms();
                showInfo("Prüfung abgeschlossen");
            }
        });

        actionPane.getContentPane().add(revalidateFarms);
        JXButton showFarmInfo = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/farm_info.png")));
        showFarmInfo.setToolTipText("Informationen über die gewählte Farm anzeigen");
        showFarmInfo.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                new FarmInformationDetailsDialog(DSWorkbenchFarmManager.this, false).setupAndShow(getSelectedInformation());
            }
        });

        actionPane.getContentPane().add(showFarmInfo);

        centerPanel.setupTaskPane(clickAccount, farmSourcePane, actionPane);
    }

    private void deleteSelection() {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Farm gewählt");
            return;
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, rows.length + " Farm(en) und alle Informationen wirklich löschen?", "Löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
            return;
        }

        FarmManager.getSingleton().invalidate();
        List<FarmInformation> toDelete = new LinkedList<FarmInformation>();
        for (int row : rows) {
            toDelete.add((FarmInformation) FarmManager.getSingleton().getAllElements().get(jFarmTable.convertRowIndexToModel(row)));
        }

        for (FarmInformation delete : toDelete) {
            FarmManager.getSingleton().removeElement(delete);
        }

        FarmManager.getSingleton().revalidate(true);

        showInfo(rows.length + " Farm(en) gelöscht");
    }

    private void farmA() {
        Hashtable<UnitHolder, Integer> troops = aTroops.getAmounts();
        if (TroopHelper.getPopulation(troops) == 0) {
            showInfo("Keine Truppen für Konfiguration A gefunden");
            return;
        }
        farm(troops);
    }

    private void farmB() {
        Hashtable<UnitHolder, Integer> troops = bTroops.getAmounts();
        if (TroopHelper.getPopulation(troops) == 0) {
            showInfo("Keine Truppen für Konfiguration B gefunden");
            return;
        }
        farm(troops);
    }

    private void farmC() {
        farm(null);
    }

    public FarmInformation getSelectedInformation() {
        int row = jFarmTable.getSelectedRow();
        if (row == -1) {
            return null;
        }
        int modelRow = jFarmTable.convertRowIndexToModel(row);
        return (FarmInformation) FarmManager.getSingleton().getAllElements().get(modelRow);
    }

    private void farm(Hashtable<UnitHolder, Integer> pUnitConfiguration) {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Einträge gewählt");
            return;
        }

        int noAdequateSourceByNeededTroops = 0;
        int noAdequateSourceByRange = 0;
        int noAdequateSourceByMinHaul = 0;
        int alreadyFarming = 0;

        int opened = 0;
        String miscMessage = null;
        for (int row : rows) {
            int modelRow = jFarmTable.convertRowIndexToModel(row);
            FarmInformation farm = (FarmInformation) FarmManager.getSingleton().getAllElements().get(modelRow);
            if (!farm.getStatus().equals(FarmInformation.FARM_STATUS.FARMING)) {
                if (clickAccount.useClick() || rows.length == 1) {
                    boolean success = false;
                    boolean fatal = false;
                    boolean send = false;
                    switch (farm.farmFarm(pUnitConfiguration)) {
                        case NO_TROOPS:
                            miscMessage = "Keine Truppeninformationen gefunden";
                            fatal = true;
                            break;
                        case NO_ADEQUATE_SOURCE_BY_NEEDED_TROOPS:
                            noAdequateSourceByNeededTroops++;
                            break;
                        case NO_ADEQUATE_SOURCE_BY_RANGE:
                            noAdequateSourceByRange++;
                            break;
                        case NO_ADEQUATE_SOURCE_BY_MIN_HAUL:
                            noAdequateSourceByMinHaul++;
                            break;
                        case FAILED_OPEN_BROWSER:
                            miscMessage = "Fehler beim Öffnen des Browsers";
                            fatal = true;
                            break;
                        case OK:
                            success = true;
                            send = true;
                            opened++;
                            break;
                    }
                    getModel().fireTableRowsUpdated(modelRow, modelRow);
                    jFarmTable.getSelectionModel().removeSelectionInterval(row, row);
                    if (success || !fatal) {
                        if (row + 1 < jFarmTable.getRowCount()) {
                            jFarmTable.getSelectionModel().addSelectionInterval(row + 1, row + 1);
                            jFarmTable.requestFocus();
                        }
                        if (!send) {
                            clickAccount.giveClickBack();
                        }
                    } else {
                        jFarmTable.getSelectionModel().addSelectionInterval(row, row);
                        clickAccount.giveClickBack();
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
                    jFarmTable.getSelectionModel().addSelectionInterval(row + 1, row + 1);
                    jFarmTable.requestFocus();
                }
            }
        }

        if (miscMessage == null) {
            showInfo("<html>Ge&ouml;ffnete Tabs: " + opened + "/" + rows.length + "<br/>"
                    + " - " + noAdequateSourceByNeededTroops + "  Mal minimale Truppenzahl nicht erreicht oder kein Herkunftsdorf mit ben&ouml;tigter Truppenanzahl<br/>"
                    + " - " + noAdequateSourceByRange + " Mal kein passendes Herkunftsdorf in Reichweite<br/>"
                    + " - " + noAdequateSourceByMinHaul + " Mal nicht gen&uuml;gend Rohstoffe<br/>"
                    + " - " + alreadyFarming + " Mal Truppen bereits unterwegs</html>");
        } else {
            showInfo("<html><b>Abbruch: '" + miscMessage + "'</b><br/>"
                    + "Ge&ouml;ffnete Tabs: " + opened + "/" + rows.length + "<br/>"
                    + " - " + noAdequateSourceByNeededTroops + " Mal minimale Truppenzahl nicht erreicht oder kein Herkunftsdorf mit ben&ouml;tigter Truppenanzahl<br/>"
                    + " - " + noAdequateSourceByRange + " Mal kein passendes Herkunftsdorf in Reichweite<br/>"
                    + " - " + noAdequateSourceByMinHaul + " Mal nicht gen&uuml;gend Rohstoffe<br/>"
                    + " - " + alreadyFarming + " Mal Truppen bereits unterwegs</html>");
        }

        //@TODO check hide status!
        //   updateHideStatus();
    }

    private void updateHideStatus() {
        /*
         * if (jHideFamedFarms.isSelected()) { jFarmTable.setRowFilter(new RowFilter<TableModel, Integer>() {
         *
         * @Override public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) { FarmInformation.FARM_STATUS
         * status = (FarmInformation.FARM_STATUS) entry.getValue(0); return !status.equals(FarmInformation.FARM_STATUS.FARMING); } }); }
         */
    }

    private void resetStatus() {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Einträge gewählt");
            return;
        }

        for (int row : rows) {
            FarmInformation farm = (FarmInformation) FarmManager.getSingleton().getAllElements().get(jFarmTable.convertRowIndexToModel(row));
            farm.resetFarmStatus();
        }
        showInfo("Status zurückgesetzt");
        getModel().fireTableDataChanged();
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
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jASettingsTab = new javax.swing.JPanel();
        jBSettingsTab = new javax.swing.JPanel();
        jCSettingsTab = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jAllowedList = new javax.swing.JList();
        jScrollPane4 = new javax.swing.JScrollPane();
        jNotAllowedList = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jMinUnits = new org.jdesktop.swingx.JXTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jMinFarmRuntime = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jMaxFarmRuntime = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jMinHaul = new javax.swing.JTextField();
        jConsiderSucessRate = new javax.swing.JCheckBox();
        jHideFamedFarms = new javax.swing.JCheckBox();
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

        jSettingsPanel.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Farmeinheiten"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jASettingsTab.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/res/ui/farmA.png")), jASettingsTab); // NOI18N

        jBSettingsTab.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/res/ui/farmB.png")), jBSettingsTab); // NOI18N

        jCSettingsTab.setLayout(new java.awt.GridBagLayout());

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Erlaubt"));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(90, 155));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(90, 155));

        jAllowedList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jAllowedList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jCSettingsTab.add(jScrollPane2, gridBagConstraints);

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Nicht erlaubt"));
        jScrollPane4.setMinimumSize(new java.awt.Dimension(90, 155));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(90, 155));

        jNotAllowedList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane4.setViewportView(jNotAllowedList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jCSettingsTab.add(jScrollPane4, gridBagConstraints);

        jButton1.setText("<<");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAllowEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(50, 5, 5, 5);
        jCSettingsTab.add(jButton1, gridBagConstraints);

        jButton2.setText(">>");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDisallowEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 50, 5);
        jCSettingsTab.add(jButton2, gridBagConstraints);

        jLabel4.setText("Min. Einheiten");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jCSettingsTab.add(jLabel4, gridBagConstraints);

        jMinUnits.setText("30");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jCSettingsTab.add(jMinUnits, gridBagConstraints);

        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/res/ui/farmC.png")), jCSettingsTab); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jTabbedPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSettingsPanel.add(jPanel2, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Farmauswahl"));
        jPanel3.setMinimumSize(new java.awt.Dimension(311, 183));
        jPanel3.setPreferredSize(new java.awt.Dimension(311, 183));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Min. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel1, gridBagConstraints);

        jMinFarmRuntime.setText("0");
        jMinFarmRuntime.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinFarmRuntime.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jMinFarmRuntime, gridBagConstraints);

        jLabel2.setText("Max. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        jMaxFarmRuntime.setText("60");
        jMaxFarmRuntime.setMinimumSize(new java.awt.Dimension(80, 24));
        jMaxFarmRuntime.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jMaxFarmRuntime, gridBagConstraints);

        jLabel3.setText("Min. Beute");
        jLabel3.setMaximumSize(new java.awt.Dimension(92, 14));
        jLabel3.setMinimumSize(new java.awt.Dimension(92, 14));
        jLabel3.setPreferredSize(new java.awt.Dimension(92, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel3, gridBagConstraints);

        jMinHaul.setText("1000");
        jMinHaul.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinHaul.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jMinHaul, gridBagConstraints);

        jConsiderSucessRate.setText("Erfolgsquote berücksichtigen");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jConsiderSucessRate, gridBagConstraints);

        jHideFamedFarms.setText("<html>Momentan angegriffene<br/>Farmen ausblenden</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jHideFamedFarms, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSettingsPanel.add(jPanel3, gridBagConstraints);

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
        jAlwaysOnTop.setOpaque(false);
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

    private void fireAllowEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAllowEvent
        UnitHolder unit = (UnitHolder) jNotAllowedList.getSelectedValue();
        if (unit != null) {
            ((DefaultListModel) jAllowedList.getModel()).addElement(unit);
            ((DefaultListModel) jNotAllowedList.getModel()).removeElement(unit);
        }
    }//GEN-LAST:event_fireAllowEvent

    private void fireDisallowEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDisallowEvent
        UnitHolder unit = (UnitHolder) jAllowedList.getSelectedValue();
        if (unit != null) {
            if (jAllowedList.getModel().getSize() == 1) {
                showInfo("Es muss mindestens eine Einheit erlaubt sein");
                return;
            }
            ((DefaultListModel) jAllowedList.getModel()).removeElement(unit);
            ((DefaultListModel) jNotAllowedList.getModel()).addElement(unit);
        }
    }//GEN-LAST:event_fireDisallowEvent

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchFarmManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchFarmManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchFarmManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DSWorkbenchFarmManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        DataHolder.getSingleton().loadData(false);
        GlobalOptions.loadUserData();

        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            VillageTroopsHolder h = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN, true);
            Hashtable<UnitHolder, Integer> troops = new Hashtable<UnitHolder, Integer>();
            troops.put(DataHolder.getSingleton().getUnitByPlainName("axe"), 2000);
            troops.put(DataHolder.getSingleton().getUnitByPlainName("light"), 2000);
            troops.put(DataHolder.getSingleton().getUnitByPlainName("spy"), 100);
            h.setTroops(troops);
        }

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                DSWorkbenchFarmManager.getSingleton().resetView();
                DSWorkbenchFarmManager.getSingleton().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JPanel jASettingsTab;
    private javax.swing.JList jAllowedList;
    private javax.swing.JCheckBox jAlwaysOnTop;
    private javax.swing.JPanel jBSettingsTab;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jCSettingsTab;
    private org.jdesktop.swingx.JXPanel jCenterPanel;
    private javax.swing.JCheckBox jConsiderSucessRate;
    private javax.swing.JPanel jFarmPanel;
    private org.jdesktop.swingx.JXTable jFarmTable;
    private javax.swing.JCheckBox jHideFamedFarms;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jMaxFarmRuntime;
    private javax.swing.JTextField jMinFarmRuntime;
    private javax.swing.JTextField jMinHaul;
    private org.jdesktop.swingx.JXTextField jMinUnits;
    private javax.swing.JList jNotAllowedList;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel jSettingsPanel;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXCollapsiblePane settingsPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void resetView() {
    }

    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jAlwaysOnTop.isSelected());
        pConfig.setProperty(getPropertyPrefix() + ".min.units", jMinUnits.getText());
        pConfig.setProperty(getPropertyPrefix() + ".min.haul", jMinHaul.getText());
        pConfig.setProperty(getPropertyPrefix() + ".min.farm.dist", jMinFarmRuntime.getText());
        pConfig.setProperty(getPropertyPrefix() + ".max.farm.dist", jMaxFarmRuntime.getText());
        pConfig.setProperty(getPropertyPrefix() + ".allowed.units", TroopHelper.unitListToProperty(jAllowedList));
        pConfig.setProperty(getPropertyPrefix() + ".disallowed.units", TroopHelper.unitListToProperty(jNotAllowedList));
        pConfig.setProperty(getPropertyPrefix() + ".farmA.units", TroopHelper.unitTableToProperty(aTroops.getAmounts()));
        pConfig.setProperty(getPropertyPrefix() + ".farmB.units", TroopHelper.unitTableToProperty(bTroops.getAmounts()));
        // pConfig.setProperty(getPropertyPrefix() + ".hide.farming", jHideFamedFarms.isSelected());
        pConfig.setProperty(getPropertyPrefix() + ".use.success.rate", jConsiderSucessRate.isSelected());
        PropertyHelper.storeTableProperties(jFarmTable, pConfig, getPropertyPrefix());
    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));
        try {
            jAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception e) {
        }

        /*
         * try { jHideFamedFarms.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".hide.farming")); updateHideStatus(); } catch
         * (Exception e) { }
         */
        try {
            jConsiderSucessRate.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".use.success.rate"));
        } catch (Exception e) {
        }

        setAlwaysOnTop(jAlwaysOnTop.isSelected());
        UIHelper.setText(jMinUnits, pConfig.getProperty(getPropertyPrefix() + ".min.units"), 30);
        UIHelper.setText(jMinHaul, pConfig.getProperty(getPropertyPrefix() + ".min.haul"), 1000);
        UIHelper.setText(jMinFarmRuntime, pConfig.getProperty(getPropertyPrefix() + ".min.farm.dist"), 0);
        UIHelper.setText(jMaxFarmRuntime, pConfig.getProperty(getPropertyPrefix() + ".max.farm.dist"), 60);
        String farmA = (String) pConfig.getProperty(getPropertyPrefix() + ".farmA.units");
        if (farmA != null) {
            aTroops.setAmounts(TroopHelper.propertyToUnitTable(farmA));
        }
        String farmB = (String) pConfig.getProperty(getPropertyPrefix() + ".farmB.units");
        if (farmB != null) {
            bTroops.setAmounts(TroopHelper.propertyToUnitTable(farmB));
        }

        jAllowedList.setModel(TroopHelper.unitListPropertyToModel((String) pConfig.getProperty(getPropertyPrefix() + ".allowed.units"), getDefaultAllowedUnits()));
        jNotAllowedList.setModel(TroopHelper.unitListPropertyToModel((String) pConfig.getProperty(getPropertyPrefix() + ".disallowed.units"), getDefaultDisallowedUnits()));
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
        FarmTableModel model = TableHelper.getTableModel(jFarmTable);
        model.fireTableDataChanged();
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        dataChangedEvent();
    }
}
