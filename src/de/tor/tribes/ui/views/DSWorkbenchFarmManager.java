/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.ClickAccountPanel;
import de.tor.tribes.ui.models.FarmTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.panels.TroopSelectionPanel;
import de.tor.tribes.ui.renderer.*;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.util.*;
import de.tor.tribes.util.farm.FarmManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;

/**
 *
 * @author Torridity
 */
public class DSWorkbenchFarmManager extends AbstractDSWorkbenchFrame implements GenericManagerListener {

    private static DSWorkbenchFarmManager SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    private ClickAccountPanel clickAccount = null;
    private TroopSelectionPanel troopSelectionPanel = new TroopSelectionPanel();

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
        jFarmTable.setDefaultRenderer(new double[3].getClass(), new StashStatusCellRenderer());
        FarmManager.getSingleton().addManagerListener(DSWorkbenchFarmManager.this);
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(jSettingsPanel, BorderLayout.CENTER);

        new Timer("FarmTableUpdate").schedule(new TimerTask() {

            @Override
            public void run() {
                jFarmTable.repaint();
            }
        }, Calendar.getInstance().getTime(), 1000);

        jAllowedList.setCellRenderer(new UnitListCellRenderer());
        jNotAllowedList.setCellRenderer(new UnitListCellRenderer());

        DefaultListModel allowModel = new DefaultListModel();
        allowModel.addElement(DataHolder.getSingleton().getUnitByPlainName("light"));
        allowModel.addElement(DataHolder.getSingleton().getUnitByPlainName("axe"));
        jAllowedList.setModel(allowModel);

        DefaultListModel disAllowModel = new DefaultListModel();
        disAllowModel.addElement(DataHolder.getSingleton().getUnitByPlainName("spear"));
        disAllowModel.addElement(DataHolder.getSingleton().getUnitByPlainName("sword"));
        disAllowModel.addElement(DataHolder.getSingleton().getUnitByPlainName("heavy"));
        jNotAllowedList.setModel(disAllowModel);

        updateTroopSelection();

        jMinTroopsPanel.add(troopSelectionPanel, BorderLayout.CENTER);
    }

    private void updateTroopSelection() {
        List<UnitHolder> units = new LinkedList<UnitHolder>();
        Hashtable<UnitHolder, Integer> am = troopSelectionPanel.getAmounts();
        DefaultListModel allowed = (DefaultListModel) jAllowedList.getModel();
        for (int i = 0; i < allowed.size(); i++) {
            units.add((UnitHolder) allowed.getElementAt(i));
        }
        troopSelectionPanel.setup(units);
        Enumeration<UnitHolder> keys = am.keys();
        while (keys.hasMoreElements()) {
            UnitHolder key = keys.nextElement();
            Integer val = am.get(key);
            if (val == null || val < 1) {
                val = 30;
            }
            troopSelectionPanel.setAmountForUnit(key, val);
        }
    }

    public IntRange getFarmRange() {
        return new IntRange(UIHelper.parseIntFromField(jMinFarmFields, 0), UIHelper.parseIntFromField(jMinFarmFields, 60));
    }

    public int getMinHaul() {
        return UIHelper.parseIntFromField(jMinHaul, 1000);
    }

    public UnitHolder[] getAllowedFarmUnits() {
        return new UnitHolder[]{DataHolder.getSingleton().getUnitByPlainName("axe"), DataHolder.getSingleton().getUnitByPlainName("light")};
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
                int result = FarmManager.getSingleton().findFarmsFromBarbarians(20);
                if (result > 0) {
                    showInfo(result + " Farm(en) hinzugefügt");
                } else {
                    showInfo("Keine neuen Farmen gefunden");
                }
            }
        });

        farmSourcePane.getContentPane().add(searchBarbs);

        JXButton searchReports = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/search_reports.png")));
        searchReports.setToolTipText("Barbarendörfer in Berichtdatenbank suchen");
        searchReports.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                int result = FarmManager.getSingleton().findFarmsInReports(50);
                if (result > 0) {
                    showInfo(result + " Farm(en) hinzugefügt");
                } else {
                    showInfo("Keine neuen Farmen gefunden");
                }
            }
        });

        farmSourcePane.getContentPane().add(searchReports);

        JXTaskPane actionPane = new JXTaskPane();
        actionPane.setTitle("Aktionen");
        JXButton doFarm = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/att_browser.png")));
        doFarm.setToolTipText("Angriffe auf gewählte Farmen in den Browser übertragen");
        doFarm.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                farm();
            }
        });

        actionPane.getContentPane().add(doFarm);

        JXButton clearStatus = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/att_browser.png")));
        clearStatus.setToolTipText("Status für gewählte Farmen zurücksetzen");
        clearStatus.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                resetStatus();
            }
        });

        actionPane.getContentPane().add(clearStatus);

        centerPanel.setupTaskPane(clickAccount, farmSourcePane, actionPane);
    }

    private void farm() {
        int rows[] = jFarmTable.getSelectedRows();
        if (rows == null || rows.length == 0) {
            showInfo("Keine Einträge gewählt");
            return;
        }

        for (int row : rows) {
            FarmInformation farm = (FarmInformation) FarmManager.getSingleton().getAllElements().get(jFarmTable.convertRowIndexToModel(row));
            if (clickAccount.useClick()) {
                boolean success = false;
                switch (farm.farmFarm()) {
                    case NO_TROOPS:
                        showInfo("Keine Truppeninformationen gefunden");
                        break;
                    case NO_ADEQUATE_SOURCE:
                        showInfo("Kein passendes Dorf gefunden");
                        break;
                    case FAILED_OPEN_BROWSER:
                        showInfo("Fehler beim Öffnen des Browsers");
                        break;
                    case OK:
                        success = true;
                        break;
                }
                if (success) {
                    jFarmTable.getSelectionModel().removeIndexInterval(row, row);
                } else {
                    clickAccount.giveClickBack();
                    break;
                }
            } else {
                showInfo("Das Klick-Konto ist leer");
                break;
            }
        }
        getModel().fireTableDataChanged();
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
        jScrollPane2 = new javax.swing.JScrollPane();
        jAllowedList = new javax.swing.JList();
        jScrollPane4 = new javax.swing.JScrollPane();
        jNotAllowedList = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jMinTroopsPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jMinFarmFields = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jMaxFarmFields = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jMinHaul = new javax.swing.JTextField();
        jCenterPanel = new org.jdesktop.swingx.JXPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jChurchFrameAlwaysOnTop = new javax.swing.JCheckBox();

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

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Erlaubt"));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(120, 155));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(120, 155));

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
        jPanel2.add(jScrollPane2, gridBagConstraints);

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Nicht erlaubt"));
        jScrollPane4.setMinimumSize(new java.awt.Dimension(120, 155));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(120, 155));

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
        jPanel2.add(jScrollPane4, gridBagConstraints);

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
        jPanel2.add(jButton1, gridBagConstraints);

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
        jPanel2.add(jButton2, gridBagConstraints);

        jMinTroopsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Minimale Anzahl"));
        jMinTroopsPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jMinTroopsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSettingsPanel.add(jPanel2, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Farmradius"));
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

        jMinFarmFields.setText("0");
        jMinFarmFields.setMinimumSize(new java.awt.Dimension(80, 24));
        jMinFarmFields.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jMinFarmFields, gridBagConstraints);

        jLabel2.setText("Max. Laufzeit [min]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        jMaxFarmFields.setText("60");
        jMaxFarmFields.setMinimumSize(new java.awt.Dimension(80, 24));
        jMaxFarmFields.setPreferredSize(new java.awt.Dimension(80, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jMaxFarmFields, gridBagConstraints);

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

        jChurchFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jChurchFrameAlwaysOnTop.setOpaque(false);
        jChurchFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jChurchFrameAlwaysOnTopfireChurchFrameOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jChurchFrameAlwaysOnTop, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jChurchFrameAlwaysOnTopfireChurchFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jChurchFrameAlwaysOnTopfireChurchFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_jChurchFrameAlwaysOnTopfireChurchFrameOnTopEvent

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
            updateTroopSelection();
        }
    }//GEN-LAST:event_fireAllowEvent

    private void fireDisallowEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDisallowEvent
        UnitHolder unit = (UnitHolder) jAllowedList.getSelectedValue();
        if (unit != null) {
            ((DefaultListModel) jAllowedList.getModel()).removeElement(unit);
            ((DefaultListModel) jNotAllowedList.getModel()).addElement(unit);
            updateTroopSelection();
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
        GlobalOptions.setSelectedServer("de77");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de77")[0]);
        DataHolder.getSingleton().loadData(false);
        GlobalOptions.loadUserData();
        //   FarmManager.getSingleton().loadElements("test.xml");
        //FarmManager.getSingleton().addFromReports();

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
    private javax.swing.JList jAllowedList;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private org.jdesktop.swingx.JXPanel jCenterPanel;
    private javax.swing.JCheckBox jChurchFrameAlwaysOnTop;
    private javax.swing.JPanel jFarmPanel;
    private org.jdesktop.swingx.JXTable jFarmTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jMaxFarmFields;
    private javax.swing.JTextField jMinFarmFields;
    private javax.swing.JTextField jMinHaul;
    private javax.swing.JPanel jMinTroopsPanel;
    private javax.swing.JList jNotAllowedList;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel jSettingsPanel;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXCollapsiblePane settingsPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void resetView() {
    }

    @Override
    public void storeCustomProperties(Configuration pConfig) {
    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
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
