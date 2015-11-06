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
package de.tor.tribes.ui.wiz.tap;

import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.TroopSplit;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.components.VillageSelectionPanel;
import de.tor.tribes.ui.models.TAPSourceTableModel;
import de.tor.tribes.ui.panels.TAPAttackInfoPanel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.FakeCellRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.windows.TroopSplitDialog;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackSourceElement;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.*;

/**
 *
 * @author Torridity
 */
public class AttackSourcePanel extends WizardPage {
    
    private static final String GENERAL_INFO = "<html>Du befindest dich in der Dorfauswahl. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, "
            + "mit denen du angreifen m&ouml;chtest. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:"
            + "<ul> <li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>"
            + "<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Gruppen der Gruppen&uuml;bersicht</li>"
            + "</ul></html>";
    private static AttackSourcePanel singleton = null;
    private VillageSelectionPanel villageSelectionPanel = null;
    private VillageOverviewMapPanel overviewPanel = null;
    
    public static synchronized AttackSourcePanel getSingleton() {
        if (singleton == null) {
            singleton = new AttackSourcePanel();
        }
        return singleton;
    }

    /**
     * Creates new form AttackSourcePanel
     */
    AttackSourcePanel() {
        initComponents();
        jVillageTable.setModel(new TAPSourceTableModel());
        jVillageTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jVillageTable.setDefaultRenderer(Boolean.class, new FakeCellRenderer());
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        villageSelectionPanel = new VillageSelectionPanel(new VillageSelectionPanel.VillageSelectionPanelListener() {
            
            @Override
            public void fireVillageSelectionEvent(Village[] pSelection) {
                addVillages(pSelection);
            }
        });
        
        jVillageTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jVillageTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        
        villageSelectionPanel.enableSelectionElement(VillageSelectionPanel.SELECTION_ELEMENT.ALLY, false);
        villageSelectionPanel.enableSelectionElement(VillageSelectionPanel.SELECTION_ELEMENT.TRIBE, false);
        villageSelectionPanel.setUnitSelectionEnabled(true);
        villageSelectionPanel.setFakeSelectionEnabled(true);
        jPanel1.add(villageSelectionPanel, BorderLayout.CENTER);
        jideSplitPane1.setOrientation(JideSplitPane.VERTICAL_SPLIT);
        jideSplitPane1.setProportionalLayout(true);
        jideSplitPane1.setDividerSize(5);
        jideSplitPane1.setShowGripper(true);
        jideSplitPane1.setOneTouchExpandable(true);
        jideSplitPane1.setDividerStepSize(10);
        jideSplitPane1.setInitiallyEven(true);
        jideSplitPane1.add(jDataPanel, JideBoxLayout.FLEXIBLE);
        jideSplitPane1.add(jVillageTablePanel, JideBoxLayout.VARY);
        jideSplitPane1.getDividerAt(0).addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jideSplitPane1.setProportions(new double[]{0.5});
                }
            }
        });
        
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        ActionListener panelListener = new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Paste")) {
                    pasteFromClipboard();
                } else if (e.getActionCommand().equals("Delete")) {
                    deleteSelection();
                }
            }
        };
        jVillageTable.registerKeyboardAction(panelListener, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jVillageTable.registerKeyboardAction(panelListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        capabilityInfoPanel1.addActionListener(panelListener);
        
        jVillageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRows = jVillageTable.getSelectedRowCount();
                if (selectedRows != 0) {
                    jStatusLabel.setText(selectedRows + " Dorf/Dörfer gewählt");
                }
            }
        });
        
        
        jInfoTextPane.setText(GENERAL_INFO);
        overviewPanel = new VillageOverviewMapPanel();
        jPanel2.add(overviewPanel, BorderLayout.CENTER);
    }
    
    public static String getDescription() {
        return "Herkunft";
    }
    
    public static String getStep() {
        return "id-attack-source";
    }
    
    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("tap.source.expert", villageSelectionPanel.isExpertSelection());
        profile.addProperty("tap.source.unit", villageSelectionPanel.getSelectedUnit().getPlainName());
        profile.addProperty("tap.source.fake", villageSelectionPanel.isFake());
    }
    
    public void restoreProperties() {
        getModel().clear();
        UserProfile profile = GlobalOptions.getSelectedProfile();
        villageSelectionPanel.setExpertSelection(Boolean.parseBoolean(profile.getProperty("tap.source.expert")));
        String unit = profile.getProperty("tap.source.unit");
        if (unit != null) {
            villageSelectionPanel.setSelectedUnit(DataHolder.getSingleton().getUnitByPlainName(unit));
        }
        villageSelectionPanel.setFake(Boolean.parseBoolean(profile.getProperty("tap.source.fake")));
        villageSelectionPanel.setup();
        updateOverview(true);
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
        jDataPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jVillageTablePanel = new javax.swing.JPanel();
        jTableScrollPane = new javax.swing.JScrollPane();
        jVillageTable = new org.jdesktop.swingx.JXTable();
        jPanel2 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jStatusLabel = new javax.swing.JLabel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jPanel3 = new javax.swing.JPanel();
        jSetFake = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSetFake1 = new javax.swing.JButton();
        jSetFake2 = new javax.swing.JButton();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jideSplitPane1 = new com.jidesoft.swing.JideSplitPane();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html");
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        jDataPanel.setMinimumSize(new java.awt.Dimension(0, 130));
        jDataPanel.setPreferredSize(new java.awt.Dimension(0, 130));
        jDataPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDataPanel.add(jPanel1, gridBagConstraints);

        jVillageTablePanel.setLayout(new java.awt.GridBagLayout());

        jTableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Verwendete Dörfer"));
        jTableScrollPane.setMinimumSize(new java.awt.Dimension(23, 100));
        jTableScrollPane.setPreferredSize(new java.awt.Dimension(23, 100));

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
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jTableScrollPane, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel2.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel2.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 5, 5);
        jVillageTablePanel.add(jPanel2, gridBagConstraints);

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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jToggleButton1, gridBagConstraints);

        jStatusLabel.setMaximumSize(new java.awt.Dimension(0, 16));
        jStatusLabel.setMinimumSize(new java.awt.Dimension(0, 16));
        jStatusLabel.setPreferredSize(new java.awt.Dimension(0, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jStatusLabel, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(capabilityInfoPanel1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jSetFake.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/fake.png"))); // NOI18N
        jSetFake.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeFakeForSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel3.add(jSetFake, gridBagConstraints);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/no_fake.png"))); // NOI18N
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeFakeForSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel3.add(jButton2, gridBagConstraints);

        jSetFake1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/snob.png"))); // NOI18N
        jSetFake1.setToolTipText("Für Dörfer in denen AGs vorhanden sind die langsamste Einheit auf AG setzen");
        jSetFake1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeToSnobEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel3.add(jSetFake1, gridBagConstraints);

        jSetFake2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/def_fake.png"))); // NOI18N
        jSetFake2.setToolTipText("Gewählte Dörfer duplizieren, indem sie nach Truppen gesplittet werden");
        jSetFake2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireSplitSelectionEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel3.add(jSetFake2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jPanel3, gridBagConstraints);

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
    
    private void fireChangeFakeForSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeFakeForSelectionEvent
        boolean fake = evt.getSource() == jSetFake;
        int[] selection = jVillageTable.getSelectedRows();
        TAPSourceTableModel model = getModel();
        if (selection.length > 0) {
            for (int i : selection) {
                model.getRow(jVillageTable.convertRowIndexToModel(i)).setFake(fake);
            }
        }
        updateOverview(false);
        repaint();
    }//GEN-LAST:event_fireChangeFakeForSelectionEvent
    
    private void fireChangeToSnobEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeToSnobEvent
        UnitHolder snob = DataHolder.getSingleton().getUnitByPlainName("snob");
        int changeCount = 0;
        for (TAPAttackSourceElement elem : getAllElements()) {
            Village v = elem.getVillage();
            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v, TroopsManager.TROOP_TYPE.OWN);
            if (holder != null) {
                if (holder.getAmountForUnit(snob) > 0) {
                    elem.setUnit(snob);
                    changeCount++;
                }
            }
        }
        repaint();
        jStatusLabel.setText("Einheit in " + changeCount + " Dorf/Dörfern auf AG geändert");
    }//GEN-LAST:event_fireChangeToSnobEvent
    
    private void fireSplitSelectionEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSplitSelectionEvent
        int[] selection = jVillageTable.getSelectedRows();
        TAPSourceTableModel model = getModel();
        List<Village> villages = new LinkedList<Village>();
        
        Hashtable<Village, Integer> alreadyInList = new Hashtable<Village, Integer>();
        
        if (selection.length > 0) {
            for (int i : selection) {
                TAPAttackSourceElement element = model.getRow(jVillageTable.convertRowIndexToModel(i));
                if (alreadyInList.get(element.getVillage()) == null) {
                    alreadyInList.put(element.getVillage(), 1);
                    villages.add(element.getVillage());
                } else {
                    alreadyInList.put(element.getVillage(), alreadyInList.get(element.getVillage()) + 1);
                }
            }
        }
        TroopSplitDialog dialog = new TroopSplitDialog(null, true);
        dialog.setupAndShow(villages);
        TroopSplit[] splits = dialog.getSplits();
        List<Village> toAdd = new LinkedList<Village>();
        
        for (TroopSplit split : splits) {
            for (int i = alreadyInList.get(split.getVillage()); i < split.getSplitCount(); i++) {
                toAdd.add(split.getVillage());
            }
        }
        
        
        addVillages(toAdd.toArray(new Village[toAdd.size()]));
        
        
    }//GEN-LAST:event_fireSplitSelectionEvent
    
    private TAPSourceTableModel getModel() {
        return (TAPSourceTableModel) jVillageTable.getModel();
    }
    
    public void addVillages(Village[] pVillages) {
        TAPSourceTableModel model = getModel();
        for (Village v : pVillages) {
            model.addRow(v, villageSelectionPanel.getSelectedUnit(), villageSelectionPanel.isFake());
        }
        if (model.getRowCount() > 0) {
            setProblem(null);
        }
        jStatusLabel.setText(pVillages.length + " Dorf/Dörfer eingefügt");
        updateOverview(false);
    }
    
    private void pasteFromClipboard() {
        String data = "";
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            List<Village> villages = PluginManager.getSingleton().executeVillageParser(data);
            if (!villages.isEmpty()) {
                addVillages(villages.toArray(new Village[villages.size()]));
            }
        } catch (HeadlessException he) {
        } catch (UnsupportedFlavorException ufe) {
        } catch (IOException ioe) {
        }
    }
    
    private void deleteSelection() {
        int[] selection = jVillageTable.getSelectedRows();
        if (selection.length > 0) {
            List<Integer> rows = new LinkedList<Integer>();
            for (int i : selection) {
                rows.add(jVillageTable.convertRowIndexToModel(i));
            }
            Collections.sort(rows);
            for (int i = rows.size() - 1; i >= 0; i--) {
                getModel().removeRow(rows.get(i));
            }
            jStatusLabel.setText(selection.length + " Dorf/Dörfer entfernt");
            updateOverview(true);
            if (getModel().getRowCount() == 0) {
                setProblem("Keine Dörfer gewählt");
            }
        }
    }
    
    private void updateOverview(boolean pReset) {
        if (pReset) {
            overviewPanel.reset();
        }
        int offs = 0;
        int fakes = 0;
        int ignored = 0;
        
        for (TAPAttackSourceElement element : getAllElements()) {
            overviewPanel.addVillage(new Point(element.getVillage().getX(), element.getVillage().getY()), (!element.isIgnored()) ? Color.yellow : Color.lightGray);
            offs++;
            if (element.isIgnored()) {
                ignored++;
            } else {
                if (element.isFake()) {
                    fakes++;
                }
            }
        }
        
        TAPAttackInfoPanel.getSingleton().updateSource(offs, fakes, ignored);
        overviewPanel.repaint();
    }
    
    public Village[] getVillages() {
        List<Village> result = new LinkedList<Village>();
        TAPSourceTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            result.add(model.getRow(i).getVillage());
        }
        return result.toArray(new Village[result.size()]);
    }
    
    public TAPAttackSourceElement[] getAllElements() {
        List<TAPAttackSourceElement> result = new LinkedList<TAPAttackSourceElement>();
        TAPSourceTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            result.add(model.getRow(i));
        }
        return result.toArray(new TAPAttackSourceElement[result.size()]);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jDataPanel;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton jSetFake;
    private javax.swing.JButton jSetFake1;
    private javax.swing.JButton jSetFake2;
    private javax.swing.JLabel jStatusLabel;
    private javax.swing.JScrollPane jTableScrollPane;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXTable jVillageTable;
    private javax.swing.JPanel jVillageTablePanel;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private com.jidesoft.swing.JideSplitPane jideSplitPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getAllElements().length == 0) {
            setProblem("Keine Dörfer gewählt");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        AttackSourceFilterPanel.getSingleton().setup();
        return WizardPanelNavResult.PROCEED;
    }
    
    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        TAPAttackInfoPanel.getSingleton().setVisible(false);
        return WizardPanelNavResult.PROCEED;
        
    }
    
    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }
}
