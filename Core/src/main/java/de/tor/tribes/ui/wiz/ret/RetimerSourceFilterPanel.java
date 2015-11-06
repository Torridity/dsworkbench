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
package de.tor.tribes.ui.wiz.ret;

import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.TroopFilterDialog;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.models.RETSourceFilterTableModel;
import de.tor.tribes.ui.wiz.ret.types.RETSourceElement;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.UIHelper;
import de.tor.tribes.util.attack.AttackManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.*;

/**
 *
 * @author Torridity
 */
public class RetimerSourceFilterPanel extends WizardPage {

    private static final String GENERAL_INFO = "Hier kannst du vorher angegebene Retime-Dörfer filtern. So können z.B. Dörfer ignoriert werden,<br/>"
            + "die bereits in Angriffsplänen verwendet oder die nicht über eine bestimmte Truppenstärke verfügen. Ignorierte Dörfer werden in der Tabelle<br/>"
            + "entsprechend markiert.";
    private static RetimerSourceFilterPanel singleton = null;
    private TroopFilterDialog troopFilterDialog = null;
    private VillageOverviewMapPanel overviewPanel = null;

    public static synchronized RetimerSourceFilterPanel getSingleton() {
        if (singleton == null) {
            singleton = new RetimerSourceFilterPanel();
        }
        return singleton;
    }

    /**
     * Creates new form AttackSourcePanel
     */
    RetimerSourceFilterPanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jVillageTable.setModel(new RETSourceFilterTableModel());
        jVillageTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jInfoTextPane.setText(GENERAL_INFO);

        jideSplitPane1.setOrientation(JideSplitPane.VERTICAL_SPLIT);
        jideSplitPane1.setProportionalLayout(true);
        jideSplitPane1.setDividerSize(5);
        jideSplitPane1.setShowGripper(true);
        jideSplitPane1.setOneTouchExpandable(true);
        jideSplitPane1.setDividerStepSize(10);
        jideSplitPane1.setInitiallyEven(true);
        jideSplitPane1.add(jFilterPanel, JideBoxLayout.FLEXIBLE);
        jideSplitPane1.add(jVillagePanel, JideBoxLayout.VARY);
        jideSplitPane1.getDividerAt(0).addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jideSplitPane1.setProportions(new double[]{0.5});
                }
            }
        });
        troopFilterDialog = new TroopFilterDialog(new JFrame(), true);
        updateFilterPanel(new LinkedList<RETSourceElement>());
        overviewPanel = new VillageOverviewMapPanel();
        jPanel2.add(overviewPanel, BorderLayout.CENTER);
        AttackManager.getSingleton().addManagerListener(new GenericManagerListener() {

            @Override
            public void dataChangedEvent() {
                updateAttackList();
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                dataChangedEvent();
            }
        });
    }

    public static String getDescription() {
        return "Filterung (Herkunft)";
    }

    public static String getStep() {
        return "id-ret-source-filter";
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("ret.filter.min.farm", UIHelper.parseIntFromField(jMinFarmSpace, 0));
        profile.addProperty("ret.filter.min.farm.bonus", UIHelper.parseIntFromField(jMinFarmSpaceBonus, 0));
    }

    public void restoreProperties() {
        getModel().clear();
        UserProfile profile = GlobalOptions.getSelectedProfile();
        UIHelper.setText(jMinFarmSpace, profile.getProperty("ret.filter.min.farm"), null);
        if (jMinFarmSpace.getText().equals("0")) {
            jMinFarmSpace.setText("");
        }
        UIHelper.setText(jMinFarmSpaceBonus, profile.getProperty("ret.filter.min.farm.bonus"), null);
        if (jMinFarmSpaceBonus.getText().equals("0")) {
            jMinFarmSpaceBonus.setText("");
        }
        updateVillageOverview();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jInfoScrollPane = new javax.swing.JScrollPane();
        jInfoTextPane = new javax.swing.JTextPane();
        jFilterPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jAttackPlanList = new org.jdesktop.swingx.JXList();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPlayerVillagesOnly = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTroopFilterButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jMinFarmSpace = new org.jdesktop.swingx.JXTextField();
        jMinFarmSpaceBonus = new org.jdesktop.swingx.JXTextField();
        jVillagePanel = new javax.swing.JPanel();
        jTableScrollPane = new javax.swing.JScrollPane();
        jVillageTable = new org.jdesktop.swingx.JXTable();
        jPanel2 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        jIgnoreButton = new javax.swing.JButton();
        jNotIgnoreButton = new javax.swing.JButton();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jideSplitPane1 = new com.jidesoft.swing.JideSplitPane();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html");
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        jFilterPanel.setPreferredSize(new java.awt.Dimension(389, 300));
        jFilterPanel.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Angriffspläne"));
        jPanel3.setMinimumSize(new java.awt.Dimension(160, 88));
        jPanel3.setPreferredSize(new java.awt.Dimension(160, 195));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jAttackPlanList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jAttackPlanList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel3.add(jScrollPane3, gridBagConstraints);

        jButton1.setText("Keinen auswählen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireRemoveAttackPlanSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jButton1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFilterPanel.add(jPanel3, gridBagConstraints);

        jButton3.setText("Filterung aktualisieren");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireUpdateFilterEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 17, 5);
        jFilterPanel.add(jButton3, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Truppen & Sonstiges"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPlayerVillagesOnly.setSelected(true);
        jPlayerVillagesOnly.setText("Nur Spielerdörfer verwenden");
        jPlayerVillagesOnly.setToolTipText("Ignoriert alle Herkunftsdörfer die nicht dem Spieler gehören, für den das aktuelle Profil gilt.");
        jPlayerVillagesOnly.setIconTextGap(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jPlayerVillagesOnly, gridBagConstraints);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/face.png"))); // NOI18N
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/barracks.png"))); // NOI18N
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel4, gridBagConstraints);

        jTroopFilterButton.setBackground(new java.awt.Color(255, 51, 51));
        jTroopFilterButton.setText("Inaktiv (klicken)");
        jTroopFilterButton.setMaximumSize(new java.awt.Dimension(120, 23));
        jTroopFilterButton.setMinimumSize(new java.awt.Dimension(120, 23));
        jTroopFilterButton.setPreferredSize(new java.awt.Dimension(120, 23));
        jTroopFilterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowTroopFilterEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jTroopFilterButton, gridBagConstraints);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/face_bonus.png"))); // NOI18N
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel5, gridBagConstraints);

        jMinFarmSpace.setToolTipText("");
        jMinFarmSpace.setPrompt("Min. BH Plätze");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jMinFarmSpace, gridBagConstraints);

        jMinFarmSpaceBonus.setToolTipText("");
        jMinFarmSpaceBonus.setPrompt("Min. BH Plätze (Bonus)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jMinFarmSpaceBonus, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFilterPanel.add(jPanel1, gridBagConstraints);

        jVillagePanel.setLayout(new java.awt.GridBagLayout());

        jTableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Gefilterte Dörfer"));

        jVillageTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableScrollPane.setViewportView(jVillageTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillagePanel.add(jTableScrollPane, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel2.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel2.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 5, 5);
        jVillagePanel.add(jPanel2, gridBagConstraints);

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/search.png"))); // NOI18N
        jToggleButton1.setToolTipText("Informationskarte vergrößern");
        jToggleButton1.setMaximumSize(new java.awt.Dimension(100, 23));
        jToggleButton1.setMinimumSize(new java.awt.Dimension(100, 23));
        jToggleButton1.setPreferredSize(new java.awt.Dimension(100, 23));
        jToggleButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireViewStateChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillagePanel.add(jToggleButton1, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jIgnoreButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jIgnoreButton.setToolTipText("Gewählte Dörfer ignorieren");
        jIgnoreButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeIgnoreSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jIgnoreButton, gridBagConstraints);

        jNotIgnoreButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox_disabled.png"))); // NOI18N
        jNotIgnoreButton.setToolTipText("Gewählte Dörfer nicht ignorieren");
        jNotIgnoreButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeIgnoreSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jNotIgnoreButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillagePanel.add(jPanel4, gridBagConstraints);

        setPreferredSize(new java.awt.Dimension(600, 600));
        setLayout(new java.awt.GridBagLayout());

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jXCollapsiblePane1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Informationen einblenden");
        jLabel1.setToolTipText("Blendet Informationen zu dieser Ansicht und zu den Datenquellen ein/aus");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jideSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        if (jXCollapsiblePane1.isCollapsed()) {
            jXCollapsiblePane1.setCollapsed(false);
            jLabel1.setText("Informationen ausblenden");
        } else {
            jXCollapsiblePane1.setCollapsed(true);
            jLabel1.setText("Informationen einblenden");
        }
    }//GEN-LAST:event_fireHideInfoEvent

    private void fireRemoveAttackPlanSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAttackPlanSelectionEvent
        jAttackPlanList.getSelectionModel().clearSelection();
    }//GEN-LAST:event_fireRemoveAttackPlanSelectionEvent

    private void fireUpdateFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateFilterEvent
        updateFilters();
    }//GEN-LAST:event_fireUpdateFilterEvent

    private void fireShowTroopFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowTroopFilterEvent
        if (troopFilterDialog.showDialog()) {
            jTroopFilterButton.setBackground(Color.GREEN);
            jTroopFilterButton.setText("Aktiv");
        } else {
            jTroopFilterButton.setBackground(Color.RED);
            jTroopFilterButton.setText("Inaktiv (klicken)");
        }
    }//GEN-LAST:event_fireShowTroopFilterEvent

    private void fireViewStateChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireViewStateChangeEvent
        if (jToggleButton1.isSelected()) {
            overviewPanel.setOptimalSize();
            jTableScrollPane.setViewportView(overviewPanel);
            jPanel2.remove(overviewPanel);
        } else {
            jTableScrollPane.setViewportView(jVillageTable);
            jPanel2.add(overviewPanel, BorderLayout.CENTER);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    jPanel2.updateUI();
                }
            });
        }
    }//GEN-LAST:event_fireViewStateChangeEvent

    private void fireChangeIgnoreSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeIgnoreSelectionEvent
        boolean ignore = (evt.getSource() == jIgnoreButton);
        List<RETSourceElement> selection = getSelection();
        for (RETSourceElement element : selection) {
            element.setIgnored(ignore);
        }
        updateFilterPanel(selection);
        updateVillageOverview();
        repaint();
    }//GEN-LAST:event_fireChangeIgnoreSelectionEvent

    private void updateFilters() {
        List<RETSourceElement> elements = getAllElements();
        filterMisc(elements);
        filterByAttackPlans(elements);
        filterTroops(elements);
        updateFilterPanel(elements);
        updateVillageOverview();
        getModel().fireTableDataChanged();
    }

    private void filterMisc(List<RETSourceElement> pAllElements) {
        Tribe t = GlobalOptions.getSelectedProfile().getTribe();
        for (RETSourceElement elem : pAllElements) {
            if (jPlayerVillagesOnly.isSelected()) {
                elem.setIgnored(elem.getVillage().getTribe().getId() != t.getId());
            }
        }
    }

    private void filterByAttackPlans(List<RETSourceElement> pAllElements) {
        Object[] selection = jAttackPlanList.getSelectedValues();
        List<String> groups = new ArrayList<String>();
        for (Object o : selection) {
            groups.add((String) o);
        }

        List<ManageableType> attacks = AttackManager.getSingleton().getAllElements(groups);
        for (ManageableType type : attacks) {
            Attack a = (Attack) type;
            for (RETSourceElement element : pAllElements) {
                if (a.getSource().getId() == element.getVillage().getId()) {
                    element.setIgnored(true);
                }
            }
        }
    }

    private void filterTroops(List<RETSourceElement> pAllElements) {
        //filter by farm space
        int requiredTroopAmount = UIHelper.parseIntFromField(jMinFarmSpace, 0);
        int requiredTroopAmountBonus = UIHelper.parseIntFromField(jMinFarmSpaceBonus, 0);
        if (requiredTroopAmount > 0 || requiredTroopAmountBonus > 0) {
            for (RETSourceElement element : pAllElements) {//go through all elements
                if (!element.isIgnored()) {
                    VillageTroopsHolder troopsForVillage = TroopsManager.getSingleton().getTroopsForVillage(element.getVillage(), TroopsManager.TROOP_TYPE.OWN);
                    if (troopsForVillage != null) {//troop information available
                        if (element.getVillage().getType() == Village.FARM_BONUS) {//bonus village, set ignored if not enough troops
                            element.setIgnored(troopsForVillage.getTroopPopCount() < requiredTroopAmountBonus);
                        } else {//no bonus village, set ignored if not enough troops
                            element.setIgnored(troopsForVillage.getTroopPopCount() < requiredTroopAmount);
                        }
                    } else {//no troop information available
                        if (element.getVillage().getType() == Village.FARM_BONUS) {//ignore if needed troops contains value != 0
                            element.setIgnored(requiredTroopAmountBonus > 0);
                        } else {//ignore if needed troops contains value != 0
                            element.setIgnored(requiredTroopAmount > 0);
                        }
                    }
                }
            }
        }
        //filter single amounts
        if (troopFilterDialog.canFilter()) {
            for (RETSourceElement elem : pAllElements) {
                if (troopFilterDialog.getIgnoredVillages(new Village[]{elem.getVillage()}).length != 0) {
                    elem.setIgnored(true);
                }
            }
        }
    }

    private void updateVillageOverview() {
        overviewPanel.reset();
        List<RETSourceElement> elements = getAllElements();
        for (RETSourceElement element : elements) {
            overviewPanel.addVillage(new Point(element.getVillage().getX(), element.getVillage().getY()), (!element.isIgnored()) ? Color.yellow : Color.lightGray);
        }
        overviewPanel.repaint();
    }

    private RETSourceFilterTableModel getModel() {
        return (RETSourceFilterTableModel) jVillageTable.getModel();
    }

    protected void setup() {
        RETSourceElement[] elements = RetimerSourcePanel.getSingleton().getAllElements();
        getModel().clear();
        overviewPanel.reset();
        for (RETSourceElement element : elements) {
            getModel().addRow(element, false);
            overviewPanel.addVillage(new Point(element.getVillage().getX(), element.getVillage().getY()), (!element.isIgnored()) ? Color.yellow : Color.lightGray);
        }
        getModel().fireTableDataChanged();
        overviewPanel.repaint();
    }

    private void updateAttackList() {
        DefaultListModel attackModel = new DefaultListModel();
        for (String plan : AttackManager.getSingleton().getGroups()) {
            attackModel.addElement(plan);
        }
        jAttackPlanList.setModel(attackModel);
    }

    protected void updateFilterPanel(List<RETSourceElement> pAllElements) {
        updateAttackList();
        int ignoreCount = 0;
        for (RETSourceElement elem : pAllElements) {
            ignoreCount += (elem.isIgnored()) ? 1 : 0;
        }

        if (ignoreCount == getModel().getRowCount()) {
            setProblem("Alle Dörfer werden ignoriert");
        } else {
            setProblem(null);
        }
    }

    public RETSourceElement[] getFilteredElements() {
        List<RETSourceElement> filtered = new LinkedList<RETSourceElement>();
        RETSourceFilterTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            RETSourceElement elem = model.getRow(i);
            if (!elem.isIgnored()) {
                filtered.add(model.getRow(i));
            }
        }
        return filtered.toArray(new RETSourceElement[filtered.size()]);
    }

    public List<RETSourceElement> getSelection() {
        List<RETSourceElement> elements = new LinkedList<RETSourceElement>();
        RETSourceFilterTableModel model = getModel();
        for (int i : jVillageTable.getSelectedRows()) {
            elements.add(model.getRow(jVillageTable.convertRowIndexToModel(i)));
        }
        return elements;
    }

    public List<RETSourceElement> getAllElements() {
        List<RETSourceElement> elements = new LinkedList<RETSourceElement>();
        RETSourceFilterTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            elements.add(model.getRow(i));
        }
        return elements;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXList jAttackPlanList;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jFilterPanel;
    private javax.swing.JButton jIgnoreButton;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private org.jdesktop.swingx.JXTextField jMinFarmSpace;
    private org.jdesktop.swingx.JXTextField jMinFarmSpaceBonus;
    private javax.swing.JButton jNotIgnoreButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JCheckBox jPlayerVillagesOnly;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jTableScrollPane;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton jTroopFilterButton;
    private javax.swing.JPanel jVillagePanel;
    private org.jdesktop.swingx.JXTable jVillageTable;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private com.jidesoft.swing.JideSplitPane jideSplitPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getFilteredElements().length == 0) {
            setProblem("Alle Dörfer werden ignoriert");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }

        RetimerCalculationPanel.getSingleton().updateStatus();
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }
}
